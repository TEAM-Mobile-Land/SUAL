import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.*;
import java.io.*;
import java.lang.reflect.Type;
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
    private static final String TITLES_FILE = "crawled_json/processed_titles.json";
    private static Set<String> processedTitles = new HashSet<>();

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

    private static void loadProcessedTitles() {
        File file = new File(TITLES_FILE);
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Gson gson = new Gson();
                Type setType = new TypeToken<HashSet<String>>(){}.getType();
                processedTitles = gson.fromJson(reader, setType);
                if (processedTitles == null) {
                    processedTitles = new HashSet<>();
                }
            } catch (IOException e) {
                System.err.println("제목 목록 로드 중 오류 발생: " + e.getMessage());
                processedTitles = new HashSet<>();
            }
        } else {
            processedTitles = new HashSet<>();
        }
    }

    private static void saveProcessedTitles() {
        try (FileWriter writer = new FileWriter(TITLES_FILE)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(processedTitles, writer);
        } catch (IOException e) {
            System.err.println("제목 목록 저장 중 오류 발생: " + e.getMessage());
        }
    }

    private static void disableSslVerification() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
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

        loadProcessedTitles();

        int maxNotices = 5;
        int processedCount = 0;
        int newNoticesCount = 0;

        for (Element link : links) {
            String contentUrl = link.absUrl("href");
            Document contentDoc = Jsoup.connect(contentUrl)
                    .userAgent(USER_AGENT)
                    .timeout(CONNECTION_TIMEOUT)
                    .get();
            String title = contentDoc.select(".md_m_tit").text();

            if (processedCount >= maxNotices) {
                break;
            }

            if (processedTitles.contains(title)) {
                System.out.println("이미 처리된 공지입니다: " + title);
            } else {
                try {
                    NoticeDto notice = crawlNotice(link);
                    notices.add(notice);
                    processedTitles.add(title);
                    saveNoticeAsJson(notice);
                    newNoticesCount++;
                    Thread.sleep(CRAWL_DELAY);
                } catch (IOException e) {
                    System.err.println("개별 URL 처리 중 오류 발생: " + contentUrl);
                    e.printStackTrace();
                }
            }
            processedCount++;
        }

        saveProcessedTitles();
        System.out.println("새로 처리된 공지사항 수: " + newNoticesCount);
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
        String title = contentDoc.select(".md_m_tit").text();
        String content = contentDoc.select(".single_cont").text();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setDate(contentDoc.select(".meta_item").first().text());
        notice.setUrl(contentUrl);

        String prompt = String.format(
                """
                다음 공지사항을 대학생들이 빠르게 이해할 수 있도록 요약해주세요.
                
                규칙:
                1. 반드시 알아야 할 중요 날짜나 기한이 있다면 ⏰ 이모지와 함께 먼저 표시
                2. 핵심 내용을 3-4줄로 요약
                3. 필요한 준비물이나 서류가 있다면 📝 이모지와 함께 목록 표시
                4. 친근하고 명확한 언어 사용
                5. 전문용어가 있다면 쉬운 말로 풀어서 설명
                
                제목: %s
                
                내용: %s
                """,
                title,
                content
        );

        String summary = GeminiService.generateSummary(prompt);
        notice.setAiSummary(summary);

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
}

class NoticeDto {
    private String title;
    private String content;
    private String date;
    private String url;
    private String aiSummary;
    private Map<String, Object> parsedContent;

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
    public String getAiSummary() { return aiSummary; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }
}