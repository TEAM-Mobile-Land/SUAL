import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.JSONObject;

import javax.net.ssl.*;
import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
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

    private static final String BASE_DIRECTORY = "crawled_json";
    private static final String ACADEMIC_DIRECTORY = BASE_DIRECTORY + "/academic";
    private static final String SCHOLARSHIP_DIRECTORY = BASE_DIRECTORY + "/scholarship";
    private static final String EVENT_DIRECTORY = BASE_DIRECTORY + "/event";
    private static final String PROJECT_ID = "sual-notice";
    private static final String SERVICE_ACCOUNT_PATH = "firebase/sual-notice-firebase-adminsdk-fbsvc-dd3c8067c4.json";

    private static Map<String, Set<String>> processedTitles = new HashMap<>();
    private static final int maxNotices = 1;

    public static void main(String[] args) {
        try {
            createDirectories();
            disableSslVerification();

            crawl(ACADEMIC_URL, ACADEMIC_DIRECTORY, "academic");
            crawl(SCHOLARSHIP_URL, SCHOLARSHIP_DIRECTORY, "scholarship");
            crawl(EVENT_URL, EVENT_DIRECTORY, "event");

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
                if (titles == null) titles = new HashSet<>();
                processedTitles.put(directory, titles);
            } catch (IOException e) {
                System.err.println("제목 목록 로드 오류: " + e.getMessage());
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
            System.err.println("제목 저장 오류: " + e.getMessage());
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
        Document doc = Jsoup.connect(targetUrl).userAgent(USER_AGENT).timeout(CONNECTION_TIMEOUT).get();
        Elements links = doc.select("td.step2 a");

        loadProcessedTitles(directory);
        int processedCount = 0;

        for (Element link : links) {
            if (processedCount >= maxNotices) break;

            String contentUrl = link.absUrl("href");
            Document contentDoc = Jsoup.connect(contentUrl).userAgent(USER_AGENT).timeout(CONNECTION_TIMEOUT).get();
            String title = contentDoc.select(".md_m_tit").text();

            Set<String> currentProcessedTitles = processedTitles.get(directory);
            if (currentProcessedTitles.contains(title)) {
                System.out.println("이미 처리된 공지: " + title);
            } else {
                NoticeDto notice = new NoticeDto();
                notice.setTitle(title);
                notice.setContent(contentDoc.select(".single_cont").text());
                notice.setDate(contentDoc.select(".meta_item").first().text());
                notice.setUrl(contentUrl);
                notice.setType(noticeType);
                notice.setParsedContent(new HashMap<>());

                saveNoticeAsJson(notice, processedCount, directory);
                sendFcmNotification(title);

                currentProcessedTitles.add(title);
                Thread.sleep(CRAWL_DELAY);
            }
            processedCount++;
        }
        saveProcessedTitles(directory);
    }

    private static void saveNoticeAsJson(NoticeDto notice, int index, String directory) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        long timestamp = System.currentTimeMillis() + (maxNotices - index);
        String fileName = String.format("%s/notice_%d.json", directory, timestamp);

        try (FileWriter writer = new FileWriter(fileName)) {
            gson.toJson(notice, writer);
            System.out.println("JSON 저장 완료: " + fileName);
        } catch (IOException e) {
            System.err.println("JSON 저장 오류: " + fileName);
            e.printStackTrace();
        }
    }

    private static void sendFcmNotification(String title) {
        try {
            InputStream serviceAccount = Main.class.getClassLoader().getResourceAsStream(SERVICE_ACCOUNT_PATH);
            if (serviceAccount == null) throw new IllegalStateException("❌ 서비스 계정 키를 찾을 수 없습니다.");

            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount)
                    .createScoped("https://www.googleapis.com/auth/firebase.messaging");
            credentials.refreshIfExpired();
            String accessToken = credentials.getAccessToken().getTokenValue();

            JSONObject notification = new JSONObject();
            notification.put("title", "새 공지");
            notification.put("body", title);

            JSONObject messageObject = new JSONObject();
            messageObject.put("topic", "all");
            messageObject.put("notification", notification);

            JSONObject finalMessage = new JSONObject();
            finalMessage.put("message", messageObject);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://fcm.googleapis.com/v1/projects/" + PROJECT_ID + "/messages:send"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json; UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(finalMessage.toString()))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("📬 FCM 전송 완료: " + title);
            System.out.println("응답: " + response.body());

        } catch (Exception e) {
            System.err.println("❌ FCM 전송 실패: " + e.getMessage());
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
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getAiSummary() { return aiSummary; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }
    public Map<String, Object> getParsedContent() { return parsedContent; }
    public void setParsedContent(Map<String, Object> parsedContent) { this.parsedContent = parsedContent; }
}
