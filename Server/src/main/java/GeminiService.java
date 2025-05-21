import java.io.FileInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class GeminiService {
    private static final String API_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";
    private static String API_KEY;
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();

    static {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("config.properties.template"));
            String apiKey = properties.getProperty("API_KEY", "").trim();
            API_KEY = apiKey.startsWith("#") ? apiKey.substring(1) : apiKey;
        } catch (Exception e) {
            throw new RuntimeException("설정 초기화 실패", e);
        }
    }

    // static 메소드로 변경
    public static String generateSummary(String prompt) {
        try {
            JsonObject requestBody = new JsonObject();
            JsonObject contents = new JsonObject();
            contents.addProperty("text", prompt);
            requestBody.add("contents", contents);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_ENDPOINT + "?key=" + API_KEY))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                return jsonResponse.getAsJsonArray("candidates")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .get("text").getAsString();
            } else {
                return "Error: " + response.statusCode() + " - " + response.body();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error generating response: " + e.getMessage();
        }
    }

    // 기존 메소드도 유지
    public String generateResponse(String prompt) {
        return generateSummary(prompt);
    }

    public static void main(String[] args) {
        String response = generateSummary("안녕하세요!");
        System.out.println("Response: " + response);
    }
}