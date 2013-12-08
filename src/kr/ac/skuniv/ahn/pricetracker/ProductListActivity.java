package kr.ac.skuniv.ahn.pricetracker;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ProductListActivity extends Activity {

	ListView lvProd;
	ProductDbHelper helper;
	SQLiteDatabase danawaDb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_product_list);

		helper = new ProductDbHelper(getApplicationContext(), "danawa.db", null, 2);
		try {
			danawaDb = helper.getReadableDatabase();
		} catch (SQLiteException e) {
		}

		// Loader ����
//		getLoaderManager().initLoader(0, // A unique identifier for this loader.
//				null, // Optional arguments to supply to the loader at construction.
//				// Interface the LoaderManager will call to report about changes
//				// in the state of the loader.
//				// Loader �� ���� �ݹ� �ޱ� ���� LoaderManager.LoaderCallbacks ������ ��ü.
//				this);

		/*
		 * SELECT [ALL | DISTINCT] �÷��� [,�÷���...]
		 * FROM ���̺�� [,���̺��...]
		 * [WHERE ���ǽ�]
		 * [GROUP BY �÷��� [HAVING ���ǽ�]]
		 * [ORDER BY �÷���]
		 * GROUP BY �÷���[,�÷���...]
		 * ORDER BY �÷���[,�÷���...]
		 */
		// INNER JOIN
		// http://ikpil.com/1206
		// @formatter:off
		Cursor cursor =
				danawaDb.rawQuery("SELECT product_name, lowest_price, delivery_charge, _id "
						+ "FROM product INNER JOIN price "
						+ "ON product.product_code = price.product_code;", null);
		// @formatter:on

		// use CursorLoader instead.
		// http://developer.android.com/reference/android/content/CursorLoader.html
		// http://blog.naver.com/PostView.nhn?blogId=xicnt&logNo=10152628630
		// http://www.vogella.com/articles/AndroidSQLite/article.html#loader
		startManagingCursor(cursor);

		String[] from = { "_id", "product_name", "lowest_price", "delivery_charge" };
		int[] to = { R.id._id, R.id.product_name, R.id.lowest_price, R.id.delivery_charge };
		// SimpleCursorAdapter
		// http://developer.android.com/reference/android/widget/SimpleCursorAdapter.html
		// The Cursor must include a column named "_id" or this class will not work.
		SimpleCursorAdapter adapter =
				new SimpleCursorAdapter(getApplicationContext(), R.layout.product_list_item,
						cursor, from, to);
		lvProd = (ListView) findViewById(R.id.activity_product_list);
		lvProd.setAdapter(adapter);
//		cursor.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.product_list, menu);
		return true;
	}
}
