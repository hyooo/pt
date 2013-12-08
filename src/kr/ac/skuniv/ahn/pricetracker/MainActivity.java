package kr.ac.skuniv.ahn.pricetracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	Button btnDn;
	Button btnList;
	Button btnSrvStart;
	Button btnSrvStop;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		btnDn = (Button) findViewById(R.id.btn_dn);
		btnList = (Button) findViewById(R.id.btn_list);
		btnSrvStart = (Button) findViewById(R.id.btn_service_start);
		btnSrvStop = (Button) findViewById(R.id.btn_service_stop);

//		onClick(btnDn);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_dn:
			startActivity(new Intent(this, HttpConnParseDbActivity.class));
			break;
		case R.id.btn_list:
			startActivity(new Intent(this, ProductListActivity.class));
			break;
		case R.id.btn_service_start:
			startService(new Intent(this, TrackService.class));
			break;
		case R.id.btn_service_stop:
			stopService(new Intent(this, TrackService.class));
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
