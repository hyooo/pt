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
	int size;						// HTML data �� ��
	StringBuilder[] htmlData;		// HTML data ���� ���ڿ�
	String[][] productInfo;			// �Ľ� ���, ������ ��ǰ ����
	// @formatter:on

	// ������
	public ParserThread(Context c, StringBuilder[] data) {
		context = c;
		htmlData = data;
	}

	public String[][] getResult() {
		return productInfo;
	}

	// ������ ��ü
	@Override
	public void run() {

		// You cannot create an array of generic type, instead, you should do:
		// ArrayList<ArrayList<Individual>> group = new ArrayList<ArrayList<Individual>>(4);
		ArrayList<String> attr0Tmp = new ArrayList<String>();
		ArrayList<String> attr1Tmp = new ArrayList<String>();

		try {
			// productNo �� �� ��ǰ�� ����
			for (int productNo = 0; productNo < size; productNo++) {

				// HtmlCleaner, http://htmlcleaner.sourceforge.net/
				HtmlCleaner cleaner = new HtmlCleaner();
				CleanerProperties props = cleaner.getProperties();
				props.setOmitComments(true); // �ּ�����
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
						// ��ǰ �̹���
						if (attr0Tmp != null && tagName != null && tagName.equals("img")) {
							if (attrCount == 2 || attrCount == 3) {
								str0 = parser.getAttributeValue(0);
								str1 = parser.getAttributeValue(1);
								attr0Tmp.add(str0);
								attr1Tmp.add(str1);
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
								productInfo[productNo][useful] = parser.getText().trim();
								for (int index = 0; index < attr0Tmp.size(); index++) {
									// ��ǰ �̹��� url ���� * 3. image url
									if (attr1Tmp.get(index).toString()
											.equals(productInfo[productNo][useful] + "_�̹���")) {
										productInfo[productNo][3] = attr0Tmp.get(index).toString();
										break;
									}
								}
								// ��ǰ �̹��� url �������� ArrayList ����
								attr0Tmp = null;
								attr1Tmp = null;
							} else {
								// ��ǰ ������ �Ǵ� ��ۺ� �Ǵ� ���¸��� ������ ����
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
					if (counter >= 6) break; // �ʿ��� ���� ������� �Ľ� �ߴ�
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
