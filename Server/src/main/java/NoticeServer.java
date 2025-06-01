import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.*;
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

            // 모든 공지사항을 가져오는 엔드포인트
            server.createContext("/api/notices", exchange -> {
                if ("GET".equals(exchange.getRequestMethod())) {
                    handleNoticesRequest(exchange, jsonPath, null);
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            });

            // 타입별 필터링된 공지사항을 가져오는 엔드포인트
            server.createContext("/api/notices/academic", exchange -> {
                if ("GET".equals(exchange.getRequestMethod())) {
                    handleNoticesRequest(exchange, jsonPath, "academic");
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            });

            server.createContext("/api/notices/scholarship", exchange -> {
                if ("GET".equals(exchange.getRequestMethod())) {
                    handleNoticesRequest(exchange, jsonPath, "scholarship");
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            });

            server.createContext("/api/notices/event", exchange -> {
                if ("GET".equals(exchange.getRequestMethod())) {
                    handleNoticesRequest(exchange, jsonPath, "event");
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            });

            server.start();
            System.out.println("\n서버가 8080 포트에서 시작되었습니다.");
            System.out.println("\nAPI 엔드포인트:");
            System.out.println("전체 공지: http://localhost:8080/api/notices");
            System.out.println("\n카테고리별 공지:");
            System.out.println("학사 공지: http://localhost:8080/api/notices/academic");
            System.out.println("장학 공지: http://localhost:8080/api/notices/scholarship");
            System.out.println("행사 공지: http://localhost:8080/api/notices/event");

        } catch (IOException e) {
            System.err.println("서버 시작 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleNoticesRequest(com.sun.net.httpserver.HttpExchange exchange, Path jsonPath, String filterType) throws IOException {
        try {
            String jsonData = readRequiredNoticeData(jsonPath.toString(), filterType);

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
    }

    private static String readRequiredNoticeData(String directoryPath, String filterType) {
        try {
            Path dir = Paths.get(directoryPath);
            StringBuilder jsonArray = new StringBuilder("[\n");
            boolean isFirst = true;

            // 각 하위 디렉토리(academic, scholarship, event)를 순회
            for (String subDir : new String[]{"academic", "scholarship", "event"}) {
                Path subDirPath = dir.resolve(subDir);
                if (!Files.exists(subDirPath)) continue;

                // 필터링이 설정되어 있고 현재 디렉토리가 필터와 일치하지 않으면 건너뛰기
                if (filterType != null && !subDir.equals(filterType)) continue;

                try (Stream<Path> files = Files.list(subDirPath)) {
                    for (Path file : files.filter(p -> p.toString().endsWith(".json"))
                            .collect(Collectors.toList())) {
                        try {
                            String content = new String(Files.readAllBytes(file), "UTF-8");
                            String filteredContent = extractRequiredFields(content, subDir);

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
            }

            jsonArray.append("\n]");
            return jsonArray.toString();

        } catch (IOException e) {
            System.err.println("디렉토리 읽기 실패: " + directoryPath);
            e.printStackTrace();
            return "[]";
        }
    }

    private static String extractRequiredFields(String jsonContent, String type) {
        try {
            String title = extractField(jsonContent, "title");
            String date = extractField(jsonContent, "date");
            String aiSummary = extractField(jsonContent, "aiSummary");
            String url = extractField(jsonContent, "url");

            return String.format("{" +
                    "\"title\": %s, " +
                    "\"date\": %s, " +
                    "\"aiSummary\": %s, " +
                    "\"url\": %s, " +
                    "\"type\": \"%s\"" +
                    "}", title, date, aiSummary, url, type);
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