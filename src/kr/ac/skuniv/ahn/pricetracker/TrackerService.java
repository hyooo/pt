package kr.ac.skuniv.ahn.pricetracker;

import java.net.MalformedURLException;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;

public class TrackerService extends Service {

	boolean isRunning; // 서비스의 무한 루프 중단
	ProductDbHelper helper;
	SQLiteDatabase danawaDb;

	// 최초 생성 때 한 번
	@Override
	public void onCreate() {
		super.onCreate();
		isRunning = true;
	}

	// bindService() 에 의해, onCreate() 이후 실행
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	// 소멸 때
	@Override
	public void onDestroy() {
		isRunning = false;
		danawaDb = null;
		helper.close();
	}

	// startService() 에 의해, 최초 생성 때는 onCreate() 이후 실행
	// 이미 실행 중일 때는 onCreate() 건너 뛰고 실행
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		isRunning = true;

		// UrlGetterThread 실행
		UrlGetterThread urlThread = new UrlGetterThread(getApplicationContext());
		urlThread.start();
		try {
			urlThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String[] url = urlThread.getResult(); // DB 에서 꺼낸 URL 목록

		// HtmlDownloaderThread 실행
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
		StringBuilder[] htmlData = dnThread.getResult(); // 다운로드한 HTML 문자열들

		// ParseThread 실행
		ParserThread parserThread = null;
		try {
			parserThread = new ParserThread(getApplicationContext(), htmlData);
			parserThread.start();
			parserThread.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		String[][] productInfo = parserThread.getResult(); // 추출한 정보들

		// WriterThread 실행
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
