import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Main {

    // 저장된 URL을 기억하는 Set (중복 저장 방지용)
    private static Set<String> savedUrls = new HashSet<>();

    /**
     * 삼육대학교 공지사항 페이지에 접속하여 HTML을 가져오고,
     * 공지사항의 링크를 추출하여 내용을 저장한다.
     */
    public void crawl() {

        SSLBypass.disableSslVerification();

        String url = "https://www.syu.ac.kr/academic/academic-notice/";

        try {
            // 공지 목록 페이지 크롤링
            Document doc = Jsoup.connect(url).get();
            // 공지 박스 내 링크들을 모두 가져옴
            Elements links = doc.select("td.step2 a");

            for (Element link : links) {
                String contentUrl = link.absUrl("href");

                // 중복 확인
                if (isAlreadySaved(contentUrl)) {
                    System.out.println("이미 저장된 공지입니다: " + contentUrl);
                    continue;
                }

                // 상세 공지 페이지 크롤링
                Document contentDoc = Jsoup.connect(contentUrl).get();
                String contentHtml = contentDoc.html();

                // 저장
                save(contentUrl, contentHtml);
            }

        } catch (IOException e) {
            System.err.println("크롤링 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 저장 여부 확인 함수
     */
    private boolean isAlreadySaved(String url) {
        return savedUrls.contains(url);
    }

    /**
     * 공지사항 본문 내용을 저장하는 함수
     */
    private void save(String url, String html) {
        savedUrls.add(url); // 저장한 URL 기록
        System.out.println("공지사항 저장됨: " + url);
        // 저장 로직 구현 필요 (예: DB insert, 파일 저장 등)
    }

    /**
     * 프로그램 진입점: 일정 주기로 crawl() 실행
     */
    public static void main(String[] args) {
        Main m = new Main();

        while (true) {
            m.crawl(); // 크롤링 수행

            try {
                // 1시간 대기 (3600000 ms)
                Thread.sleep(60 * 60 * 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}

