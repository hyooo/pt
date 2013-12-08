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

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class HttpConnParseDbActivity extends Activity {

	private TextView mMessage;
	private EditText mUrl;
	private TextView mSource;
	private Button btnDl;
	private Button btnParse;
	private Button btnShowDb;
	private StringBuilder mStringBuilder; // html ���� ����
	private String prodInfo[]; // html ���Ͽ��� ������ �ϳ��� ��ǰ ���� ����

	ProductDbHelper helper;
	SQLiteDatabase danawaDb;

	String strUrl1;
	String strUrl2;
	String strUrl3;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_http_conn_parse_db);
		mMessage = (TextView) findViewById(R.id.message);
		mUrl = (EditText) findViewById(R.id.url);
		mSource = (TextView) findViewById(R.id.source);
		btnDl = (Button) findViewById(R.id.download);
		btnParse = (Button) findViewById(R.id.parse);
		btnShowDb = (Button) findViewById(R.id.showdb);

		mStringBuilder = new StringBuilder();
		prodInfo = new String[7];

		strUrl1 =
				"http://prod.danawa.com/info/?pcode=1596012"
						+ "&cate1=861&cate2=874&cate3=11043&cate4=0";
		strUrl2 =
				"http://prod.danawa.com/info/?pcode=1304396"
						+ "&cate1=861&cate2=874&cate3=11043&cate4=0";
		strUrl3 =
				"http://prod.danawa.com/info/?pcode=1853510"
						+ "&cate1=861&cate2=32617&cate3=32623&cate4=0";

		// Database
		helper = new ProductDbHelper(getApplicationContext(), "danawa.db", null, 2);
		try {
			danawaDb = helper.getWritableDatabase();
		} catch (SQLiteException e) {
//			danawaDb = helper.getReadableDatabase();
		}

		// click download button
//		onClick(btnDl);
	}

	public void onClick(View v) {

		URL url1 = null;
		URL url2 = null;
		URL url3 = null;
		try {
			url1 = new URL(strUrl1);
			url2 = new URL(strUrl2);
			url3 = new URL(strUrl3);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		switch (v.getId()) {
		case R.id.download:
			mUrl.setText(url1.toString());
			new DownloadHtmlTask().execute(url1);
			break;
		case R.id.dl2:
			mUrl.setText(url2.toString());
			new DownloadHtmlTask().execute(url2);
			break;
		case R.id.dl3:
			mUrl.setText(url3.toString());
			new DownloadHtmlTask().execute(url3);
			break;
		case R.id.parse:
			new ParseTask().execute(mStringBuilder);
			break;
		case R.id.showdb:
			StringBuilder sbTmp = new StringBuilder();

			// product

			Cursor cursor = danawaDb.rawQuery("SELECT * FROM product;", null);
			int colCnt = cursor.getColumnCount();
			for (int index = 0; index < colCnt; index++) {
				sbTmp.append(cursor.getColumnName(index));
				sbTmp.append(" ");
			}
			sbTmp.append("\n");
			while (cursor.moveToNext()) {
				for (int index = 0; index < colCnt; index++) {
					sbTmp.append(cursor.getString(index));
					sbTmp.append(" ");
				}
				sbTmp.append("\n");
			}

			// price

			cursor = danawaDb.rawQuery("SELECT * FROM price;", null);
			colCnt = cursor.getColumnCount();
			for (int index = 0; index < colCnt; index++) {
				sbTmp.append(cursor.getColumnName(index));
				sbTmp.append(" ");
			}
			sbTmp.append("\n");
			while (cursor.moveToNext()) {
				for (int index = 0; index < colCnt; index++) {
					sbTmp.append(cursor.getString(index));
					sbTmp.append(" ");
				}
				sbTmp.append("\n");
			}

			cursor.close();
			// http://www.androidpub.com/431954
			// provider �� �� �� ��.
//			helper.close();

			mSource.setText(mSource.getText() + sbTmp.toString());
			sbTmp = null;

			break;
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////

	private class DownloadHtmlTask extends AsyncTask<URL, Integer, StringBuilder> {
		long time;

		@Override
		protected void onPreExecute() {
			time = System.nanoTime();
		}

		// �۾� �����忡�� �����ؾ� �ϴ� �۾�. �ڵ� ����.
		@Override
		protected StringBuilder doInBackground(URL... urls) {
			HttpURLConnection conn = null;
			InputStreamReader isr = null;
			StringBuilder sb = new StringBuilder();

			/*
			 * 0. html url
			 * 1. product code
			 * 2. product name
			 * 3. image url
			 * 4. lowest price
			 * 5. delivery charge
			 * 6. open market price
			 */

			// 0. html url
			// 1. product code
			String strTmp = urls[0].toString();
			int firstAmpersand = strTmp.indexOf('&');
			prodInfo[0] = strTmp.substring(0, firstAmpersand);
			strTmp = null;
			int firstEqualSign = prodInfo[0].indexOf('=');
			prodInfo[1] = prodInfo[0].substring(firstEqualSign + 1, prodInfo[0].length());

			try {
				conn = (HttpURLConnection) urls[0].openConnection();
				// http request �� User-Agent �ʵ带 PC �������� ������ ����
				conn.setRequestProperty("User-Agent",
				// "Mozilla/5.0 (Linux; Android 4.0.4; IM-A800S Build/IMM761) " +
				// "AppleWebKit/537.36 (KHTML, like Gecko) " +
				// "Chrome/30.0.1599.92 Mobile Safari/537.36"
						"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) "
								+ "Chrome/28.0.1500.95 Safari/537.36");
				// openConnection() �����ߴٸ�
				if (conn != null) {
					conn.setConnectTimeout(10000); // 10�� �ð� �ֱ�
					conn.setUseCaches(false);
					// ���� �����ϸ�
					if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
						isr = new InputStreamReader(conn.getInputStream(), "euc-kr");

						// �ٿ�ε� �ӵ� ���
						BufferedReader br = new BufferedReader(isr);
						String line = "";
						while ((line = br.readLine()) != null) {
							sb.append(line + "\n");
						}
						publishProgress(sb.length());
						if (br != null) {
							br.close();
						}

					} // end of if (���� ����)

				} // end of if (openConnection() ����)
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
			return sb; // to onPostExecute()
		} // end of doInBackground()

		@Override
		protected void onProgressUpdate(Integer... htmlSize) {
			time = System.nanoTime() - time;
			mSource.setText(mSource.getText() + "download: " + time + " ns ("
					+ (int) (htmlSize[0] * 1000000.0 / time) + " kBps)\n");
		}

		// doInBackground() �� ��� ���� �޾� UI �����忡�� ����
		@Override
		protected void onPostExecute(StringBuilder sb) {
			mStringBuilder = null;
			mStringBuilder = new StringBuilder(sb);
			mMessage.setText(mMessage.getText() + "d");

			onClick(btnParse);
		}
	} // end of class DownloadHtmlTask

	////////////////////////////////////////////////////////////////////////////////////////////////

	private class ParseTask extends AsyncTask<StringBuilder, Void, StringBuilder> {

		private long time;

		// You cannot create an array of generic type, instead, you should do:
		// ArrayList<ArrayList<Individual>> group = new ArrayList<ArrayList<Individual>>(4);
		ArrayList<String> attr0Tmp = new ArrayList<String>();
		ArrayList<String> attr1Tmp = new ArrayList<String>();

		@Override
		protected void onPreExecute() {
			time = System.nanoTime();
		}

		// �۾� �����忡�� �����ؾ� �ϴ� �۾�. �ڵ� ����.
		@Override
		protected StringBuilder doInBackground(StringBuilder... argSb) {
			StringBuilder sb = new StringBuilder();

			try {
				// HtmlCleaner, http://htmlcleaner.sourceforge.net/
				HtmlCleaner cleaner = new HtmlCleaner();
				CleanerProperties props = cleaner.getProperties();
				props.setOmitComments(true); // �ּ�����
				TagNode node = null;
				node = cleaner.clean(argSb[0].toString());
				SimpleXmlSerializer sxs = new SimpleXmlSerializer(props);
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				sxs.writeToStream(node, os, "UTF-8");
				String str = os.toString();
				InputStream is = new ByteArrayInputStream(str.getBytes("UTF-8"));

				// HtmlCleaner ��� ���
				publishProgress();

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
						// 3���� ���ݵ��� �׷��� �Ǵ� ��ǰ �̹���
						if (attr0Tmp != null && tagName != null && tagName.equals("img")) {
							if (attrCount == 2 || attrCount == 3) {
								str0 = parser.getAttributeValue(0);
								str1 = parser.getAttributeValue(1);
								attr0Tmp.add(str0);
								attr1Tmp.add(str1);
								/*
								if (str0.equals("���ݵ��� �׷���")) {
									counter++;
									sb.append(counter);
									sb.append(". ");
									sb.append(str0);
									sb.append(": ");
									sb.append(str1);
									sb.append(str1.length());
									sb.append("\n");
								}
								*/
							}
						}
						// ��ǰ �̸� * 2. product name
						if (tagName != null && tagName.equals("p") && attrCount == 1) {
							str0 = parser.getAttributeValue(0);
							if (str0.equals("goods_title")) {
								useful = 2;
							}
						}
						// ��ǰ ������ * 4. lowest price
						// �Ǵ� ��ۺ� * 5. delivery charge
						if (tagName != null && tagName.equals("span") && attrCount == 1) {
							str0 = parser.getAttributeValue(0);
							if (str0.equals("big_price")) {
								useful = 4;
							} else if (str0.equals("delivery_charge")) {
								useful = 5;
							}
						}
						// ���¸��� ������ * 6. open market price
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
								// ��ǰ �̸� ����
								prodInfo[useful] = parser.getText().trim();
								for (int index = 0; index < attr0Tmp.size(); index++) {
									// ��ǰ �̹��� url ���� * 3. image url
									if (attr1Tmp.get(index).toString()
											.equals(prodInfo[useful] + "_�̹���")) {
										prodInfo[3] = attr0Tmp.get(index).toString();
										break;
									}
								}
								// ��ǰ �̹��� url �������� ArrayList ����
								attr0Tmp = null;
								attr1Tmp = null;
							} else {
								// ��ǰ ������ �Ǵ� ��ۺ� �Ǵ� ���¸��� ������ ����
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
					if (counter >= 6) break; // �ʿ��� ���� ������� �Ľ� �ߴ�
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
			time = System.nanoTime() - time;
			mSource.setText(mSource.getText() + "HtmlCleaner: " + time + " ns\n");
			mMessage.setText(mMessage.getText() + "h");
		}

		// doInBackground() �� ��� ���� �޾� UI �����忡�� ����
		@Override
		protected void onPostExecute(StringBuilder sb) {
			mStringBuilder = null;
			mStringBuilder = new StringBuilder(mSource.getText());
			mStringBuilder.append(sb);
//			mSource.setText(mStringBuilder); // ���� ����

//			StringBuilder tmp = new StringBuilder();
//			for (int index = 0; index < prodInfo.length; index++) {
//				tmp.append(prodInfo[index]);
//				tmp.append("\n");
//			}
//			mSource.setText(tmp); // ���� ����

			mMessage.setText(mMessage.getText() + "p");

			// table product �� �ߺ� ���ڵ� �ִ��� product code �� �˻��ؼ� ������ ���ڵ� �߰�
			Cursor cursor =
					danawaDb.rawQuery("SELECT product_code " + "FROM product "
							+ "WHERE product_code = " + prodInfo[1] + ";", null);
			if (cursor.getCount() == 0) {
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
				danawaDb.execSQL("INSERT INTO product VALUES (null, '"
						+ prodInfo[0] + "', '"	// html url
						+ prodInfo[1] + "', '"	// product code
						+ prodInfo[3] + "', '"	// image url
						+ prodInfo[2] + "');");	// product name
				cursor.close();
			}
			// table price �� ���ڵ� �߰�
			danawaDb.execSQL("INSERT INTO price VALUES (null, '"
						+ prodInfo[1] + "', "	// product code
						+ prodInfo[4] + ", "	// lowest price			INTEGER
						+ prodInfo[5] + ", "	// delivery charge		INTEGER
						+ prodInfo[6] + ", "	// open market price	INTEGER
						+ "datetime('now'));");
			// @formatter:on
		}
	} // end of class ParseTask

}