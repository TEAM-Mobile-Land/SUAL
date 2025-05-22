import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.stream.*;

public class NoticeServer {
    public static void main(String[] args) {
        try {
            Path currentDir = Paths.get(System.getProperty("user.dir"));
            Path jsonPath = currentDir.resolve("crawled_json");

            System.out.println("JSON 파일 경로: " + jsonPath.toAbsolutePath());

            if (!Files.exists(jsonPath)) {
                System.err.println("경고: 디렉토리를 찾을 수 없습니다: " + jsonPath);
                return;
            }

            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

            server.createContext("/api/notices", exchange -> {
                if ("GET".equals(exchange.getRequestMethod())) {
                    try {
                        String jsonData = readRequiredNoticeData(jsonPath.toString());

                        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
                        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

                        byte[] response = jsonData.getBytes("UTF-8");
                        exchange.sendResponseHeaders(200, response.length);

                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        String errorJson = "{\"error\": \"서버 오류가 발생했습니다.\"}";
                        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
                        exchange.sendResponseHeaders(500, errorJson.length());
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(errorJson.getBytes("UTF-8"));
                        }
                    }
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            });

            server.start();
            System.out.println("서버가 8080 포트에서 시작되었습니다.");
            System.out.println("API 엔드포인트: http://localhost:8080/api/notices");

        } catch (IOException e) {
            System.err.println("서버 시작 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String readRequiredNoticeData(String directoryPath) {
        try {
            Path dir = Paths.get(directoryPath);
            StringBuilder jsonArray = new StringBuilder("[\n");

            try (Stream<Path> files = Files.list(dir)) {
                boolean isFirst = true;

                for (Path file : files.filter(p -> p.toString().endsWith(".json"))
                        .collect(Collectors.toList())) {
                    try {
                        String content = new String(Files.readAllBytes(file), "UTF-8");
                        String filteredContent = extractRequiredFields(content);

                        if (!isFirst) {
                            jsonArray.append(",\n");
                        }
                        jsonArray.append("  ").append(filteredContent);
                        isFirst = false;

                    } catch (IOException e) {
                        System.err.println("파일 읽기 실패: " + file.getFileName());
                        e.printStackTrace();
                    }
                }
            }

            jsonArray.append("\n]");
            return jsonArray.toString();

        } catch (IOException e) {
            System.err.println("디렉토리 읽기 실패: " + directoryPath);
            e.printStackTrace();
            return "[]";
        }
    }

    private static String extractRequiredFields(String jsonContent) {
        // 간단한 문자열 처리로 필요한 필드만 추출
        try {
            String title = extractField(jsonContent, "title");
            String date = extractField(jsonContent, "date");
            String aiSummary = extractField(jsonContent, "aiSummary");
            String url = extractField(jsonContent, "url");

            return String.format("{" +
                    "\"title\": %s, " +
                    "\"date\": %s, " +
                    "\"aiSummary\": %s, " +
                    "\"url\": %s" +
                    "}", title, date, aiSummary, url);
        } catch (Exception e) {
            System.err.println("JSON 파싱 실패");
            e.printStackTrace();
            return "{}";
        }
    }

    private static String extractField(String json, String fieldName) {
        int fieldStart = json.indexOf("\"" + fieldName + "\"");
        if (fieldStart == -1) return "\"\"";

        int valueStart = json.indexOf(":", fieldStart) + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        if (json.charAt(valueStart) != '"') return "\"\"";

        valueStart++; // 첫 따옴표 다음으로 이동
        int valueEnd = valueStart;

        while (valueEnd < json.length()) {
            if (json.charAt(valueEnd) == '"' && json.charAt(valueEnd - 1) != '\\') {
                break;
            }
            valueEnd++;
        }

        String value = json.substring(valueStart, valueEnd);
        return "\"" + value + "\"";
    }
}