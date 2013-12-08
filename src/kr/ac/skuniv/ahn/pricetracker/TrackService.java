package kr.ac.skuniv.ahn.pricetracker;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleXmlSerializer;
import org.htmlcleaner.TagNode;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.IBinder;
import android.widget.Toast;

public class TrackService extends Service {

	boolean bRun; // 서비스의 무한 루프 중단
	private String[] prodInfo; // html 파일에서 추출한 하나의 상품 정보 저장
	ProductDbHelper helper;
	SQLiteDatabase danawaDb;

	// 최초 생성 때 한 번
	@Override
	public void onCreate() {
		super.onCreate();
		bRun = true;
		prodInfo = new String[7];
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	// 소멸 때
	@Override
	public void onDestroy() {
		bRun = false;
		danawaDb = null;
		helper.close();
		prodInfo = null;
	}

	// 다른 컴포넌트에 의해 startService() 호출 때
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		bRun = true;

		// Database
		helper = new ProductDbHelper(getApplicationContext(), "danawa.db", null, 2);
		try {
			danawaDb = helper.getWritableDatabase();
		} catch (SQLiteException e) {
//			danawaDb = helper.getReadableDatabase();
		}

		Cursor cursor;

		// 0 _id	// 1 html_url	// 2 product_code	// 3 img_url	// 4 product_name
		cursor = danawaDb.rawQuery("SELECT * FROM product;", null);
		ArrayList<String> url = new ArrayList<String>();
		while (cursor.moveToNext()) {
			url.add(cursor.getString(1));
		}
		cursor.close();

		int urlCount = url.size();
		for (int index = 0; bRun == true; index = ++index % urlCount) {
			try {
				Toast.makeText(getApplicationContext(), "ServiceForLoop", Toast.LENGTH_SHORT).show();
				new DownloadHtmlTask().execute(new URL(url.get(index)));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	private class DownloadHtmlTask extends AsyncTask<URL, Integer, StringBuilder> {

		@Override
		protected void onPreExecute() {
		}

		// 작업 스레드에서 수행해야 하는 작업. 자동 실행.
		@Override
		protected StringBuilder doInBackground(URL... urls) {
			HttpURLConnection conn = null;
			InputStreamReader isr = null;
			StringBuilder sb = new StringBuilder();

			try {
				conn = (HttpURLConnection) urls[0].openConnection();
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
						while ((line = br.readLine()) != null) {
							sb.append(line + "\n");
						}
						if (br != null) {
							br.close();
						}
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
			return sb; // to onPostExecute()
		} // end of doInBackground()

		@Override
		protected void onProgressUpdate(Integer... htmlSize) {
		}

		// doInBackground() 의 결과 전달 받아 UI 스레드에서 실행
		@Override
		protected void onPostExecute(StringBuilder sb) {
			new ParseTask().execute(sb);
		}
	} // end of class DownloadHtmlTask

	////////////////////////////////////////////////////////////////////////////////////////////////

	private class ParseTask extends AsyncTask<StringBuilder, Void, StringBuilder> {

		ArrayList<String> attr0Tmp = new ArrayList<String>();
		ArrayList<String> attr1Tmp = new ArrayList<String>();

		@Override
		protected void onPreExecute() {
		}

		// 작업 스레드에서 수행해야 하는 작업. 자동 실행.
		@Override
		protected StringBuilder doInBackground(StringBuilder... argSb) {
			StringBuilder sb = new StringBuilder();

			try {
				// HtmlCleaner, http://htmlcleaner.sourceforge.net/
				HtmlCleaner cleaner = new HtmlCleaner();
				CleanerProperties props = cleaner.getProperties();
				props.setOmitComments(true); // 주석제거
				TagNode node = null;
				node = cleaner.clean(argSb[0].toString());
				SimpleXmlSerializer sxs = new SimpleXmlSerializer(props);
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				sxs.writeToStream(node, os, "UTF-8");
				String str = os.toString();
				InputStream is = new ByteArrayInputStream(str.getBytes("UTF-8"));

				// XmlPullParser
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				XmlPullParser parser = factory.newPullParser();
				parser.setInput(is, "UTF-8");
				parser.next();
				int parserEvent = parser.getEventType();
				String tagName = null, str0 = null, str1 = null;
				int attrCount;
				int useful = -1;
				int counter = 0;
				while (parserEvent != XmlPullParser.END_DOCUMENT) {
					switch (parserEvent) {
					case XmlPullParser.START_DOCUMENT:
						break;
					case XmlPullParser.START_TAG:
						tagName = parser.getName();
						attrCount = parser.getAttributeCount();
						// 상품 이미지
						if (attr0Tmp != null && tagName != null && tagName.equals("img")) {
							if (attrCount == 2 || attrCount == 3) {
								str0 = parser.getAttributeValue(0);
								str1 = parser.getAttributeValue(1);
								attr0Tmp.add(str0);
								attr1Tmp.add(str1);
							}
						}
						// 상품 이름 * 2. product name
						if (tagName != null && tagName.equals("p") && attrCount == 1) {
							str0 = parser.getAttributeValue(0);
							if (str0.equals("goods_title")) {
								useful = 2;
							}
						}
						// 상품 최저가 * 4. lowest price
						// 또는 배송비 * 5. delivery charge
						if (tagName != null && tagName.equals("span") && attrCount == 1) {
							str0 = parser.getAttributeValue(0);
							if (str0.equals("big_price")) {
								useful = 4;
							} else if (str0.equals("delivery_charge")) {
								useful = 5;
							}
						}
						// 오픈마켓 최저가 * 6. open market price
						if (tagName != null && tagName.equals("em") && attrCount == 1) {
							str0 = parser.getAttributeValue(0);
							if (str0.equals("red_price")) {
								useful = 6;
							}
						}
						break;
					case XmlPullParser.TEXT:
						if (useful > 0) {
							if (useful == 2) {
								// 상품 이름 저장
								prodInfo[useful] = parser.getText().trim();
								for (int index = 0; index < attr0Tmp.size(); index++) {
									// 상품 이미지 url 저장 * 3. image url
									if (attr1Tmp.get(index).toString()
											.equals(prodInfo[useful] + "_이미지")) {
										prodInfo[3] = attr0Tmp.get(index).toString();
										break;
									}
								}
								// 상품 이미지 url 구했으면 ArrayList 제거
								attr0Tmp = null;
								attr1Tmp = null;
							} else {
								// 상품 죄저가 또는 배송비 또는 오픈마켓 최저가 저장
								// \D	A non-digit: [^0-9]
								prodInfo[useful] = parser.getText().trim().replaceAll("\\D", "");
							}
							useful = -1;
						}
						break;
					case XmlPullParser.END_TAG:
						tagName = null;
						str0 = null;
						str1 = null;
						break;
					}
					if (counter >= 6) break; // 필요한 정보 얻었으면 파싱 중단
					parserEvent = parser.next();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			}
			return sb; // to onPostExecute()

		} // end of doInBackground()

		@Override
		protected void onProgressUpdate(Void... voids) {
		}

		// doInBackground() 의 결과 전달 받아 UI 스레드에서 실행
		@Override
		protected void onPostExecute(StringBuilder sb) {

			/*
			 * 0. html url
			 * 1. product code
			 * 2. product name
			 * 3. image url
			 * 4. lowest price
			 * 5. delivery charge
			 * 6. open market price
			 */
			// @formatter:off
			// table price 에 레코드 추가
			danawaDb.execSQL("INSERT INTO price VALUES (null, '"
						+ prodInfo[1] + "', "	// product code			TEXT
						+ prodInfo[4] + ", "	// lowest price			INTEGER
						+ prodInfo[5] + ", "	// delivery charge		INTEGER
						+ prodInfo[6] + ", "	// open market price	INTEGER
						+ "datetime('now'));");
			// @formatter:on
		}
	} // end of class ParseTask

}
