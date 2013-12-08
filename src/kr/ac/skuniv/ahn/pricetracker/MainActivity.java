package kr.ac.skuniv.ahn.pricetracker;

import kr.ac.skuniv.hyosang.testhttpurlconnection.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	Button btn1 = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		btn1 = (Button) findViewById(R.id.btn_main_run_test);

		onClick(btn1);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_main_run_test:
			Intent intent = new Intent(this, TestHttpURLConnection.class);
			startActivity(intent);
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
