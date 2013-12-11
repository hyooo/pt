package kr.ac.skuniv.ahn.pricetracker;

import java.net.MalformedURLException;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;

public class TrackerService extends Service {

	boolean isRunning; // ������ ���� ���� �ߴ�
	ProductDbHelper helper;
	SQLiteDatabase danawaDb;

	// ���� ���� �� �� ��
	@Override
	public void onCreate() {
		super.onCreate();
		isRunning = true;
	}

	// bindService() �� ����, onCreate() ���� ����
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	// �Ҹ� ��
	@Override
	public void onDestroy() {
		isRunning = false;
		danawaDb = null;
		helper.close();
	}

	// startService() �� ����, ���� ���� ���� onCreate() ���� ����
	// �̹� ���� ���� ���� onCreate() �ǳ� �ٰ� ����
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		isRunning = true;

		// UrlGetterThread ����
		UrlGetterThread urlThread = new UrlGetterThread(getApplicationContext());
		urlThread.start();
		try {
			urlThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String[] url = urlThread.getResult(); // DB ���� ���� URL ���

		// HtmlDownloaderThread ����
		HtmlDownloaderThread dnThread = null;
		try {
			dnThread = new HtmlDownloaderThread(getApplicationContext(), url);
			dnThread.start();
			dnThread.join();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		StringBuilder[] htmlData = dnThread.getResult(); // �ٿ�ε��� HTML ���ڿ���

		// ParseThread ����
		ParserThread parserThread = null;
		try {
			parserThread = new ParserThread(getApplicationContext(), htmlData);
			parserThread.start();
			parserThread.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		String[][] productInfo = parserThread.getResult(); // ������ ������

		// WriterThread ����
		WriterThread writerThread;
		writerThread = new WriterThread(getApplicationContext(), productInfo);
		writerThread.start();
		try {
			writerThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return START_NOT_STICKY;
	}
}
