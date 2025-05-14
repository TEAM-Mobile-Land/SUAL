import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;
import javax.net.ssl.*;

public class Main {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    private static final String TARGET_URL = "https://www.syu.ac.kr/academic/academic-notice/";
    private static final int CONNECTION_TIMEOUT = 10000;
    private static final int CRAWL_DELAY = 1000;
    private static final Set<String> savedUrls = new HashSet<>();

    public static void main(String[] args) {
        try {
            disableSslVerification();
            crawl();
        } catch (Exception e) {
            System.err.println("프로그램 실행 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void crawl() throws IOException, InterruptedException {
        Document doc = Jsoup.connect(TARGET_URL)
                .userAgent(USER_AGENT)
                .timeout(CONNECTION_TIMEOUT)
                .get();

        Elements links = doc.select("td.step2 a");
        System.out.println("발견된 공지사항 수: " + links.size());

        for (Element link : links) {
            String contentUrl = link.absUrl("href");
            
            if (savedUrls.contains(contentUrl)) {
                System.out.println("이미 저장된 공지입니다: " + contentUrl);
                continue;
            }

            try {
                Document contentDoc = Jsoup.connect(contentUrl)
                        .userAgent(USER_AGENT)
                        .timeout(CONNECTION_TIMEOUT)
                        .get();

                // 공지사항 제목
                String title = contentDoc.select("h3.title").text();
                // 공지사항 내용
                String content = contentDoc.select("div.content-body").html();
                // 공지사항 날짜
                String date = contentDoc.select("span.date").text();

                save(contentUrl, String.format(
                    "제목: %s\n날짜: %s\n내용:\n%s", 
                    title, date, content
                ));

                savedUrls.add(contentUrl);
                System.out.println("저장 완료: " + title);
                
                Thread.sleep(CRAWL_DELAY);
            } catch (IOException e) {
                System.err.println("개별 URL 처리 중 오류 발생: " + contentUrl);
                e.printStackTrace();
            }
        }
    }

    private static void save(String url, String content) {
        String fileName = "notice_" + System.currentTimeMillis() + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("URL: " + url + "\n\n");
            writer.write(content);
            System.out.println("파일 저장 완료: " + fileName);
        } catch (IOException e) {
            System.err.println("파일 저장 중 오류 발생: " + fileName);
            e.printStackTrace();
        }
    }

    private static void disableSslVerification() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (Exception e) {
            System.err.println("SSL 검증 비활성화 중 오류 발생");
            e.printStackTrace();
        }
    }
}