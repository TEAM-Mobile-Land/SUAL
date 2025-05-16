import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.*;
import java.io.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;

public class Main {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    private static final String TARGET_URL = "https://www.syu.ac.kr/academic/academic-notice/";
    private static final int CONNECTION_TIMEOUT = 10000;
    private static final int CRAWL_DELAY = 1000;
    private static final Set<String> savedUrls = new HashSet<>();
    private static final String JSON_DIRECTORY = "crawled_json";

    public static void main(String[] args) {
        try {
            createDirectory();
            disableSslVerification();
            crawl();
        } catch (Exception e) {
            System.err.println("프로그램 실행 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createDirectory() {
        File directory = new File(JSON_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    private static void disableSslVerification() throws Exception {
        // SSL 검증을 비활성화하는 TrustManager 생성
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        // SSL 컨텍스트 설정
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // 호스트네임 검증 비활성화
        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    private static void crawl() throws IOException, InterruptedException {
        Document doc = Jsoup.connect(TARGET_URL)
                .userAgent(USER_AGENT)
                .timeout(CONNECTION_TIMEOUT)
                .ignoreHttpErrors(true)
                .sslSocketFactory(createSSLSocketFactory())
                .get();

        Elements links = doc.select("td.step2 a");
        System.out.println("발견된 공지사항 수: " + links.size());

        List<NoticeDto> notices = new ArrayList<>();

        for (Element link : links) {
            String contentUrl = link.absUrl("href");

            if (savedUrls.contains(contentUrl)) {
                System.out.println("이미 저장된 공지입니다: " + contentUrl);
                continue;
            }

            try {
                NoticeDto notice = crawlNotice(link);
                notices.add(notice);
                savedUrls.add(contentUrl);

                saveNoticeAsJson(notice);

                Thread.sleep(CRAWL_DELAY);
            } catch (IOException e) {
                System.err.println("개별 URL 처리 중 오류 발생: " + contentUrl);
                e.printStackTrace();
            }
        }

        saveNoticesListAsJson(notices);
    }

    private static SSLSocketFactory createSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[]{}; }
            }}, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static NoticeDto crawlNotice(Element link) throws IOException {
        String contentUrl = link.absUrl("href");
        Document contentDoc = Jsoup.connect(contentUrl)
                .userAgent(USER_AGENT)
                .timeout(CONNECTION_TIMEOUT)
                .ignoreHttpErrors(true)
                .sslSocketFactory(createSSLSocketFactory())
                .get();

        NoticeDto notice = new NoticeDto();
        notice.setTitle(contentDoc.select("h3.title").text());
        notice.setContent(contentDoc.select("div.content-body").html());
        notice.setDate(contentDoc.select("span.date").text());
        notice.setUrl(contentUrl);

        Element contentBody = contentDoc.select("div.content-body").first();
        Map<String, Object> parsedContent = parseHtmlContent(contentBody);
        notice.setParsedContent(parsedContent);

        return notice;
    }

    private static Map<String, Object> parseHtmlContent(Element content) {
        Map<String, Object> result = new HashMap<>();
        if (content != null) {
            Elements tables = content.select("table");
            if (!tables.isEmpty()) {
                result.put("tables", parseTablesContent(tables));
            }

            result.put("textContent", content.text());

            Elements links = content.select("a");
            if (!links.isEmpty()) {
                result.put("links", parseLinks(links));
            }

            Elements images = content.select("img");
            if (!images.isEmpty()) {
                result.put("images", parseImages(images));
            }
        }
        return result;
    }

    private static List<Map<String, String>> parseLinks(Elements links) {
        List<Map<String, String>> linksList = new ArrayList<>();
        for (Element link : links) {
            Map<String, String> linkMap = new HashMap<>();
            linkMap.put("text", link.text());
            linkMap.put("href", link.attr("abs:href"));
            linksList.add(linkMap);
        }
        return linksList;
    }

    private static List<String> parseImages(Elements images) {
        List<String> imageUrls = new ArrayList<>();
        for (Element image : images) {
            imageUrls.add(image.attr("abs:src"));
        }
        return imageUrls;
    }

    private static List<List<String>> parseTablesContent(Elements tables) {
        List<List<String>> tableData = new ArrayList<>();
        for (Element table : tables) {
            Elements rows = table.select("tr");
            for (Element row : rows) {
                List<String> rowData = new ArrayList<>();
                Elements cells = row.select("td, th");
                for (Element cell : cells) {
                    rowData.add(cell.text());
                }
                tableData.add(rowData);
            }
        }
        return tableData;
    }

    private static void saveNoticeAsJson(NoticeDto notice) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String fileName = String.format("%s/notice_%d.json", JSON_DIRECTORY, System.currentTimeMillis());

        try (FileWriter writer = new FileWriter(fileName)) {
            gson.toJson(notice, writer);
            System.out.println("JSON 파일 저장 완료: " + fileName);
        } catch (IOException e) {
            System.err.println("JSON 파일 저장 중 오류 발생: " + fileName);
            e.printStackTrace();
        }
    }

    private static void saveNoticesListAsJson(List<NoticeDto> notices) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String fileName = String.format("%s/notices_list_%d.json", JSON_DIRECTORY, System.currentTimeMillis());

        try (FileWriter writer = new FileWriter(fileName)) {
            gson.toJson(notices, writer);
            System.out.println("전체 목록 JSON 파일 저장 완료: " + fileName);
        } catch (IOException e) {
            System.err.println("전체 목록 JSON 파일 저장 중 오류 발생: " + fileName);
            e.printStackTrace();
        }
    }
}

class NoticeDto {
    private String title;
    private String content;
    private String date;
    private String url;
    private Map<String, Object> parsedContent;

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Map<String, Object> getParsedContent() { return parsedContent; }
    public void setParsedContent(Map<String, Object> parsedContent) { this.parsedContent = parsedContent; }
}

// 테스트용