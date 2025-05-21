import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class GeminiService {
    private static final String API_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    private static String API_KEY;
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();

    static {
        try {
            File configFile = new File("config.properties.template");
            if (!configFile.exists()) {
                System.err.println("\n=== 설정 파일을 찾을 수 없습니다 ===");
                System.err.println("현재 작업 디렉토리: " + new File(".").getAbsolutePath());
                System.err.println("찾고 있는 파일: " + configFile.getAbsolutePath());
                throw new FileNotFoundException("config.properties.template 파일이 없습니다.");
            }

            Properties properties = new Properties();
            try (FileInputStream fis = new FileInputStream(configFile)) {
                properties.load(fis);
            }

            API_KEY = properties.getProperty("gemini.api.key");
            System.out.println("설정 파일 읽기 완료");
            System.out.println("API_KEY 설정 상태: " + (API_KEY != null ? "설정됨" : "설정되지 않음"));

            if (API_KEY == null) {
                System.err.println("\n=== API 키 설정이 없습니다 ===");
                System.err.println("config.properties.template 파일에 다음 내용이 있어야 합니다:");
                System.err.println("gemini.api.key=your-api-key-here");
                System.err.println("(주석 # 기호가 있다면 제거해주세요)");
                throw new IllegalStateException("gemini.api.key 설정이 없습니다");
            }

            API_KEY = API_KEY.trim();
            if (API_KEY.isEmpty()) {
                System.err.println("\n=== API 키가 비어있습니다 ===");
                System.err.println("config.properties.template 파일의 gemini.api.key= 뒤에");
                System.err.println("실제 API 키 값을 입력해주세요.");
                throw new IllegalStateException("API 키가 비어있습니다");
            }

            System.out.println("API 키 설정이 완료되었습니다.");

        } catch (Exception e) {
            String errorMsg = "\n=== 설정 초기화 실패 ===\n" +
                    "오류 내용: " + e.getMessage() + "\n" +
                    "해결 방법:\n" +
                    "1. config.properties.template 파일이 프로젝트 루트에 있는지 확인\n" +
                    "2. 파일 내용에서 'gemini.api.key=' 앞의 # 기호를 제거\n" +
                    "3. API 키 값이 올바르게 입력되어 있는지 확인";
            System.err.println(errorMsg);
            throw new RuntimeException(errorMsg, e);
        }
    }

    public static String generateSummary(String prompt) {
        if (API_KEY == null || API_KEY.trim().isEmpty()) {
            return "오류: API 키가 설정되지 않았습니다. config.properties.template 파일을 확인해주세요.";
        }

        int maxRetries = 3;
        int currentTry = 0;
        int baseWaitTime = 30; // 30초 대기 시간으로 증가

        while (currentTry < maxRetries) {
            try {
                JsonObject requestBody = new JsonObject();
                JsonArray contents = new JsonArray();
                JsonObject content = new JsonObject();
                JsonObject parts = new JsonObject();
                parts.addProperty("text", prompt);
                content.add("parts", gson.toJsonTree(new JsonObject[]{parts}));
                contents.add(content);
                requestBody.add("contents", contents);

                String requestUrl = API_ENDPOINT + "?key=" + API_KEY;
                System.out.println("API 요청을 시작합니다... (시도 " + (currentTry + 1) + "/" + maxRetries + ")");

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(requestUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                // 상세한 응답 로깅
                System.out.println("응답 상태 코드: " + response.statusCode());
                System.out.println("응답 본문: " + response.body());

                if (response.statusCode() == 200) {
                    JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                    return jsonResponse.getAsJsonArray("candidates")
                            .get(0).getAsJsonObject()
                            .getAsJsonObject("content")
                            .getAsJsonArray("parts")
                            .get(0).getAsJsonObject()
                            .get("text").getAsString();
                } else if (response.statusCode() == 429) {
                    System.out.println("할당량 초과, " + baseWaitTime + "초 후 재시도합니다...");
                    Thread.sleep(baseWaitTime * 1000);
                    currentTry++;

                    if (currentTry >= maxRetries) {
                        return "할당량 초과로 인해 요청을 완료할 수 없습니다. 잠시 후 다시 시도해주세요.";
                    }
                } else {
                    String errorMsg = "API 응답 오류 (상태 코드: " + response.statusCode() + ")\n" +
                            "응답 내용: " + response.body();
                    System.err.println(errorMsg);
                    return errorMsg;
                }
            } catch (Exception e) {
                String errorMsg = "AI 요약 생성 중 오류 발생:\n" + e.getMessage();
                System.err.println(errorMsg);
                e.printStackTrace();
                currentTry++;

                if (currentTry >= maxRetries) {
                    return errorMsg;
                }

                try {
                    Thread.sleep(baseWaitTime * 1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return "최대 재시도 횟수(" + maxRetries + ")를 초과했습니다. 나중에 다시 시도해주세요.";
    }
}