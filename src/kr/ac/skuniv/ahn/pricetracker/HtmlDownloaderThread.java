package kr.ac.skuniv.ahn.pricetracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.content.Context;

public class HtmlDownloaderThread extends Thread {

	// @formatter:off
	Context context;
	boolean isRunning = true;
	ArrayList<URL> url;					// URL 목록
	int size;							// URL 수
	private StringBuilder[] htmlData;	// 결과 문자열
	// @formatter:on

	// 생성자
	public HtmlDownloaderThread(Context c, String[] s) throws MalformedURLException {
		context = c;
		url = new ArrayList<URL>(); // ArrayList 객체 생성

		// String 을 URL 로 바꿔 ArrayList 에 추가
		size = s.length;
		URL tmpUrl;
		for (int index = 0; index < size; index++) {
			tmpUrl = new URL(s[index]);
			url.add(tmpUrl);
		}
	}

	public StringBuilder[] getResult() {
		return htmlData;
	}

	// 스레드 본체
	@Override
	public void run() {

		// download htmls
		HttpURLConnection conn = null;
		InputStreamReader isr = null;
		for (int index = 0; index < size; index++)
			htmlData[index] = new StringBuilder();

		for (int index = 0; index < size; index++) {
			try {
				conn = (HttpURLConnection) url.get(index).openConnection();
				// http request 의 User-Agent 필드를 PC 브라우저의 값으로 변경
				conn.setRequestProperty("User-Agent",
						"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) "
								+ "Chrome/28.0.1500.95 Safari/537.36");
				// openConnection() 성공했다면
				if (conn != null) {
					conn.setConnectTimeout(10000); // 10초 시간 주기
					conn.setUseCaches(false);
					// 연결 성공하면
					if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
						isr = new InputStreamReader(conn.getInputStream(), "euc-kr");
						BufferedReader br = new BufferedReader(isr);
						String line = "";
						while ((line = br.readLine()) != null)
							htmlData[index].append(line + "\n");
						if (br != null) br.close();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally { // close() 는 반드시 finally로 묶어서
				if (conn != null) {
					try {
						conn.disconnect();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} // end of for
	} // end of run()
}
