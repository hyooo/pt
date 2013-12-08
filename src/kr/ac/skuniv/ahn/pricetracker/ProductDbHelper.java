package kr.ac.skuniv.ahn.pricetracker;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class ProductDbHelper extends SQLiteOpenHelper {

	public ProductDbHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	public ProductDbHelper(Context context, String name, CursorFactory factory, int version,
			DatabaseErrorHandler errorHandler) {
		super(context, name, factory, version, errorHandler);
	}

	/*
	 * http://www.androidpub.com/467799
	 * http://overoid.tistory.com/19
	 * http://www.sqlite.org/lang_datefunc.html
	 * sqlite> SELECT datetime('now');
	 * 2004-08-19 18:51:06
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE product ( _id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "html_url TEXT, "
				+ "product_code TEXT, "
				+ "img_url TEXT, "
				+ "product_name TEXT);");
		db.execSQL("CREATE TABLE price ( _id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "product_code TEXT, "
				+ "lowest_price INTEGER, "
				+ "delivery_charge INTEGER, "
				+ "open_market_price INTEGER, "
				+ "date_and_time TEXT);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
		db.execSQL("DROP TABLE IF EXISTS product");
		db.execSQL("DROP TABLE IF EXISTS price");
		onCreate(db);
	}

}
