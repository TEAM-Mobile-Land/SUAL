import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FCMV1Sender {
    private static final String PROJECT_ID = "your-project-id";
    private static final String NOTICE_JSON = "notice.json";

    public static void main(String[] args) throws Exception {
        // 1. access token 발급 (클래스패스에서 읽기)
        InputStream serviceAccount = FCMV1Sender.class
                .getClassLoader()
                .getResourceAsStream("firebase/sual-notice-firebase-adminsdk-fbsvc-dd3c8067c4.json");

        if (serviceAccount == null) {
            throw new IllegalStateException("❌ Firebase 서비스 계정 JSON을 찾을 수 없습니다.");
        }

        GoogleCredentials credentials = GoogleCredentials
                .fromStream(serviceAccount)
                .createScoped("https://www.googleapis.com/auth/firebase.messaging");

        credentials.refreshIfExpired();
        String accessToken = credentials.getAccessToken().getTokenValue();

        // 2. 공지 JSON에서 title만 추출
        String notice = Files.readString(Paths.get(NOTICE_JSON)); // 이건 여전히 로컬 파일
        JsonObject noticeObj = JsonParser.parseString(notice).getAsJsonObject();
        String title = noticeObj.get("title").getAsString();

        // 3. FCM 메시지 구성
        JSONObject message = new JSONObject();
        message.put("topic", "all");

        JSONObject notification = new JSONObject();
        notification.put("title", "새 공지");
        notification.put("body", title);

        JSONObject messageObject = new JSONObject();
        messageObject.put("notification", notification);
        message.put("message", messageObject);

        // 4. HTTP 요청 전송
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://fcm.googleapis.com/v1/projects/" + PROJECT_ID + "/messages:send"))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json; UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(message.toString()))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("📬 FCM 응답:");
        System.out.println(response.body());
    }
}