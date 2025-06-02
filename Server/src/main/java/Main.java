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
    private static final String ACADEMIC_URL = "https://www.syu.ac.kr/academic/academic-notice/";
    private static final String SCHOLARSHIP_URL = "https://www.syu.ac.kr/academic/scholarship-information/scholarship-notice/";
    private static final String EVENT_URL = "https://www.syu.ac.kr/university-square/notice/event/";
    private static final int CONNECTION_TIMEOUT = 10000;
    private static final int CRAWL_DELAY = 1000;
    private static final Set<String> savedUrls = new HashSet<>();

    private static final String BASE_DIRECTORY = "crawled_json";
    private static final String ACADEMIC_DIRECTORY = BASE_DIRECTORY + "/academic";
    private static final String SCHOLARSHIP_DIRECTORY = BASE_DIRECTORY + "/scholarship";
    private static final String EVENT_DIRECTORY = BASE_DIRECTORY + "/event";

    private static Map<String, Set<String>> processedTitles = new HashMap<>();
    private static final int maxNotices = 5;

    public static void main(String[] args) {
        try {
            createDirectories();
            disableSslVerification();

            // 학사공지 크롤링
            crawl(ACADEMIC_URL, ACADEMIC_DIRECTORY, "academic");

            // 장학공지 크롤링 (URL이 추가되면 활성화)
            if (!SCHOLARSHIP_URL.isEmpty()) {
                crawl(SCHOLARSHIP_URL, SCHOLARSHIP_DIRECTORY, "scholarship");
            }

            // 행사공지 크롤링 (URL이 추가되면 활성화)
            if (!EVENT_URL.isEmpty()) {
                crawl(EVENT_URL, EVENT_DIRECTORY, "event");
            }
        } catch (Exception e) {
            System.err.println("프로그램 실행 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createDirectories() {
        new File(ACADEMIC_DIRECTORY).mkdirs();
        new File(SCHOLARSHIP_DIRECTORY).mkdirs();
        new File(EVENT_DIRECTORY).mkdirs();
    }

    private static void loadProcessedTitles(String directory) {
        File file = new File(directory + "/processed_titles.json");
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Gson gson = new Gson();
                Type setType = new TypeToken<HashSet<String>>(){}.getType();
                Set<String> titles = gson.fromJson(reader, setType);
                if (titles == null) {
                    titles = new HashSet<>();
                }
                processedTitles.put(directory, titles);
            } catch (IOException e) {
                System.err.println("제목 목록 로드 중 오류 발생: " + e.getMessage());
                processedTitles.put(directory, new HashSet<>());
            }
        } else {
            processedTitles.put(directory, new HashSet<>());
        }
    }

    private static void saveProcessedTitles(String directory) {
        try (FileWriter writer = new FileWriter(directory + "/processed_titles.json")) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(processedTitles.get(directory), writer);
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

    private static void crawl(String targetUrl, String directory, String noticeType) throws IOException, InterruptedException {
        if (targetUrl.isEmpty()) return;

        Document doc = Jsoup.connect(targetUrl)
                .userAgent(USER_AGENT)
                .timeout(CONNECTION_TIMEOUT)
                .ignoreHttpErrors(true)
                .sslSocketFactory(createSSLSocketFactory())
                .get();

        Elements links = doc.select("td.step2 a");
        System.out.println(noticeType + " 공지사항 발견된 수: " + links.size());

        List<NoticeDto> notices = new ArrayList<>();
        loadProcessedTitles(directory);

        int processedCount = 0;
        int newNoticesCount = 0;

        for (Element link : links) {
            if (processedCount >= maxNotices) break;

            String contentUrl = link.absUrl("href");
            Document contentDoc = Jsoup.connect(contentUrl)
                    .userAgent(USER_AGENT)
                    .timeout(CONNECTION_TIMEOUT)
                    .get();
            String title = contentDoc.select(".md_m_tit").text();

            Set<String> currentProcessedTitles = processedTitles.get(directory);
            if (currentProcessedTitles.contains(title)) {
                System.out.println("이미 처리된 공지입니다: " + title);
            } else {
                try {
                    NoticeDto notice = crawlNotice(link, noticeType);
                    notices.add(notice);
                    currentProcessedTitles.add(title);
                    saveNoticeAsJson(notice, processedCount, directory);
                    newNoticesCount++;
                    Thread.sleep(CRAWL_DELAY);
                } catch (IOException e) {
                    System.err.println("개별 URL 처리 중 오류 발생: " + contentUrl);
                    e.printStackTrace();
                }
            }
            processedCount++;
        }

        saveProcessedTitles(directory);
        System.out.println(noticeType + " 새로 처리된 공지사항 수: " + newNoticesCount);
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

    private static NoticeDto crawlNotice(Element link, String noticeType) throws IOException {
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
        notice.setType(noticeType);

        Element contentBody = contentDoc.select("div.content-body").first();
        Map<String, Object> parsedContent = parseHtmlContent(contentBody);
        notice.setParsedContent(parsedContent);

        String prompt = String.format(
                """
                다음 공지사항을 대학생들이 빠르게 이해할 수 있도록 요약해주세요.
                
                [규칙]
                
                1. 본문 최상단에 아래 항목들을 표준 포맷으로 정리:
                **# 날짜**\n
                    <yyyy년 mm월 dd일 ~ yyyy년 mm월 dd일> (기간일 경우)
                    
                **# 필요한 준비 서류**\n 
                    항목1, 항목2 (예: 신분증, 신청서)
                    
                **# 장소**\n 
                    구체적인 장소나 제출처 (예: 한국장학재단 홈페이지)
                
                ※ 각 항목은 해당 정보가 없으면 생략, 서류제출 장소 부분은 온라인 제출일 경우 "제출처"로 바꾼다
                
                2. 그 아래에 본문의 핵심 내용을 3~4줄로 요약
                
                3. 전문용어나 어려운 표현은 쉬운 말로 풀어 적기
                
                4. 전체 내용을 친근하고 예의 바른 존댓말로 작성 (예: ~입니다, ~해주세요)
                
                5. 강조가 필요한 부분은 볼드 마크다운(포맷에 명시된 부분은 제외), 별표, 이모티콘을 절대 쓰지 말고
                꺽새(< >)로 감싸서 표시 (예: <YYYY.MM.DD>, <중요 안내>)
                
                ────────────────────────────
                예시 출력
                
                **# 날짜**
                  <2025년 6월 1일 ~ 2025년 6월 30일>
                
                **# 필요한 준비 서류**
                  <신청서>, <공인인증서>
                
                **# 장소**
                  <한국장학재단 홈페이지>
                
                국가장학금 신청이 6월 30일까지 진행됩니다.
                홈페이지 또는 모바일 앱을 통해 신청할 수 있습니다.
                신청 전 준비물을 확인하고 기간 내에 꼭 제출해주세요.
                        
                제목: %s
                
                내용: %s
                """,
                title,
                content
        );

        String summary = GeminiService.generateSummary(prompt);
        notice.setAiSummary(summary);

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

    private static void saveNoticeAsJson(NoticeDto notice, int index, String directory) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        long timestamp = System.currentTimeMillis() + (maxNotices - index);
        String fileName = String.format("%s/notice_%d.json", directory, timestamp);

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
    private String type;
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
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}