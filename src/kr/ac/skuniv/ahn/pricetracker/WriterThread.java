package kr.ac.skuniv.ahn.pricetracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class WriterThread extends Thread {

	// @formatter:off
	Context context;
	boolean isRunning = true;
	SQLiteDatabase danawaDb;
	String[][] product;			// 상품 정보
	// @formatter:on

	// 생성자
	public WriterThread(Context c, String[][] info) {
		context = c;
		product = info;
	}

	// 스레드 본체
	@Override
	public void run() {
		// open DB
		ProductDbHelper helper = new ProductDbHelper(context, "danawa.db", null, 2);
		try {
			danawaDb = helper.getWritableDatabase();
		} catch (SQLiteException e) {
		}

		// num 는 각 상품을 구분
		int size = product.length; // 행의 수 즉 상품의 수
		for (int num = 0; num < size; num++) {
			// table price 에 레코드 추가
			// @formatter:off
			danawaDb.execSQL("INSERT INTO price VALUES (null, '"
					+ product[num][1] + "', "	// product code			TEXT
					+ product[num][4] + ", "	// lowest price			INTEGER
					+ product[num][5] + ", "	// delivery charge		INTEGER
					+ product[num][6] + ", "	// open market price	INTEGER
					+ "datetime('now'));");		// 삽입 시점			TEXT
			// @formatter:on
		}

		helper.close();

	} // end of run()
}
