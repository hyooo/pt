package kr.ac.skuniv.ahn.pricetracker;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class UrlGetterThread extends Thread {

	// @formatter:off
	Context context;
	boolean isRunning = true;
	ProductDbHelper helper;
	SQLiteDatabase danawaDb;
	String[] url;				// URL 목록
	// @formatter:on

	// 생성자
	public UrlGetterThread(Context c) {
		context = c;
	}

	public String[] getResult() {
		return url;
	}

	// 스레드 본체
	@Override
	public void run() {

		// open DB
		helper = new ProductDbHelper(context, "danawa.db", null, 2);
		try {
			danawaDb = helper.getReadableDatabase();
		} catch (SQLiteException e) {
		}

		Cursor cursor = danawaDb.rawQuery("SELECT html_url FROM product;", null);

		int index = 0;
		while(cursor.moveToNext()) {
			url[index++] = new String(cursor.getString(0));
		}

		cursor.close();
		helper.close();

	} // end of run()
}
