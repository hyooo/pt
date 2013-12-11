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
	ArrayList<URL> url;					// URL ���
	int size;							// URL ��
	private StringBuilder[] htmlData;	// ��� ���ڿ�
	// @formatter:on

	// ������
	public HtmlDownloaderThread(Context c, String[] s) throws MalformedURLException {
		context = c;
		url = new ArrayList<URL>(); // ArrayList ��ü ����

		// String �� URL �� �ٲ� ArrayList �� �߰�
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

	// ������ ��ü
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
				// http request �� User-Agent �ʵ带 PC �������� ������ ����
				conn.setRequestProperty("User-Agent",
						"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) "
								+ "Chrome/28.0.1500.95 Safari/537.36");
				// openConnection() �����ߴٸ�
				if (conn != null) {
					conn.setConnectTimeout(10000); // 10�� �ð� �ֱ�
					conn.setUseCaches(false);
					// ���� �����ϸ�
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
			} finally { // close() �� �ݵ�� finally�� ���
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
