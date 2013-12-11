package kr.ac.skuniv.ahn.pricetracker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.SimpleXmlSerializer;
import org.htmlcleaner.TagNode;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;

public class ParserThread extends Thread {

	// @formatter:off
	Context context;
	boolean isRunning = true;
	int size;						// HTML data 의 수
	StringBuilder[] htmlData;		// HTML data 원본 문자열
	String[][] productInfo;			// 파스 결과, 추출한 상품 정보
	// @formatter:on

	// 생성자
	public ParserThread(Context c, StringBuilder[] data) {
		context = c;
		htmlData = data;
	}

	public String[][] getResult() {
		return productInfo;
	}

	// 스레드 본체
	@Override
	public void run() {

		// You cannot create an array of generic type, instead, you should do:
		// ArrayList<ArrayList<Individual>> group = new ArrayList<ArrayList<Individual>>(4);
		ArrayList<String> attr0Tmp = new ArrayList<String>();
		ArrayList<String> attr1Tmp = new ArrayList<String>();

		try {
			// productNo 는 각 상품을 구분
			for (int productNo = 0; productNo < size; productNo++) {

				// HtmlCleaner, http://htmlcleaner.sourceforge.net/
				HtmlCleaner cleaner = new HtmlCleaner();
				CleanerProperties props = cleaner.getProperties();
				props.setOmitComments(true); // 주석제거
				TagNode node = null;
				node = cleaner.clean(htmlData[productNo].toString());
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
								productInfo[productNo][useful] = parser.getText().trim();
								for (int index = 0; index < attr0Tmp.size(); index++) {
									// 상품 이미지 url 저장 * 3. image url
									if (attr1Tmp.get(index).toString()
											.equals(productInfo[productNo][useful] + "_이미지")) {
										productInfo[productNo][3] = attr0Tmp.get(index).toString();
										break;
									}
								}
								// 상품 이미지 url 구했으면 ArrayList 제거
								attr0Tmp = null;
								attr1Tmp = null;
							} else {
								// 상품 죄저가 또는 배송비 또는 오픈마켓 최저가 저장
								// \D	A non-digit: [^0-9]
								productInfo[productNo][useful] =
										parser.getText().trim().replaceAll("\\D", "");
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
					if (counter >= 6) break; // 필요한 정보 얻었으면 파스 중단
					parserEvent = parser.next();

				} // end of while (parserEvent != XmlPullParser.END_DOCUMENT)
			} // end of for

		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}

	} // end of run()
}
