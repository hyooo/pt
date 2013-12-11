package kr.ac.skuniv.ahn.pricetracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class WriterThread extends Thread {

	// @formatter:off
	Context context;
	boolean isRunning = true;
	SQLiteDatabase danawaDb;
	String[][] product;			// ��ǰ ����
	// @formatter:on

	// ������
	public WriterThread(Context c, String[][] info) {
		context = c;
		product = info;
	}

	// ������ ��ü
	@Override
	public void run() {
		// open DB
		ProductDbHelper helper = new ProductDbHelper(context, "danawa.db", null, 2);
		try {
			danawaDb = helper.getWritableDatabase();
		} catch (SQLiteException e) {
		}

		// num �� �� ��ǰ�� ����
		int size = product.length; // ���� �� �� ��ǰ�� ��
		for (int num = 0; num < size; num++) {
			// table price �� ���ڵ� �߰�
			// @formatter:off
			danawaDb.execSQL("INSERT INTO price VALUES (null, '"
					+ product[num][1] + "', "	// product code			TEXT
					+ product[num][4] + ", "	// lowest price			INTEGER
					+ product[num][5] + ", "	// delivery charge		INTEGER
					+ product[num][6] + ", "	// open market price	INTEGER
					+ "datetime('now'));");		// ���� ����			TEXT
			// @formatter:on
		}

		helper.close();

	} // end of run()
}
