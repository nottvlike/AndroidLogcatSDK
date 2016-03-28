package org.c2man.logcat.service;

import java.io.IOException;
import java.util.Calendar;

import org.c2man.logcat.receiver.LogTaskReceiver;
import org.c2man.logcat.receiver.SDStateMonitorReceiver;
import org.c2man.logcat.thread.LogCollectorThread;
import org.c2man.logcat.utils.Constant;
import org.c2man.logcat.utils.LogHelper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;

/**
 * ��־������־Ĭ�ϻ�洢��SDcar�����û��SDcard��洢���ڴ��еİ�װĿ¼���档 1.������Ĭ����SDcard��ÿ������һ����־�ļ�,
 * 2.�����SDCard�Ļ��Ὣ֮ǰ�ڴ��е��ļ�������SDCard�� 3.���û��SDCard���ڰ�װĿ¼��ֻ���浱ǰ��д��־
 * 4.SDcard��װ��ж�ض������ڲ���2,3���л� 5.SDcard�е���־�ļ�ֻ����7��
 * 
 * @author Administrator
 * 
 */
public class LogService extends Service {

	private SDStateMonitorReceiver _sdStateReceiver; // SDcard״̬���
	private LogTaskReceiver _logTaskReceiver;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		init();
		register();
		deploySwitchLogFileTask();
		new LogCollectorThread().start();
	}

	private void init() {
		LogHelper.Instance().Init(this);
		PowerManager pm = (PowerManager) getApplicationContext()
				.getSystemService(Context.POWER_SERVICE);
		LogHelper.Instance().PartialWakeLock = pm.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK, Constant.TAG);
	}

	private void register() {
		IntentFilter sdCarMonitorFilter = new IntentFilter();
		sdCarMonitorFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		sdCarMonitorFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		sdCarMonitorFilter.addDataScheme("file");
		_sdStateReceiver = new SDStateMonitorReceiver();
		registerReceiver(_sdStateReceiver, sdCarMonitorFilter);

		IntentFilter logTaskFilter = new IntentFilter();
		logTaskFilter.addAction(Constant.MONITOR_LOG_SIZE_ACTION);
		logTaskFilter.addAction(Constant.SWITCH_LOG_FILE_ACTION);
		_logTaskReceiver = new LogTaskReceiver();
		registerReceiver(_logTaskReceiver, logTaskFilter);
	}

	/**
	 * ������־�л�����ÿ���賿�л���־�ļ�
	 */
	private void deploySwitchLogFileTask() {
		Intent intent = new Intent(Constant.SWITCH_LOG_FILE_ACTION);
		PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);

		// ��������
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
				AlarmManager.INTERVAL_DAY, sender);
		LogHelper.Instance().RecordLogServiceLog(
				"deployNextTask succ,next task time is:"
						+ Constant.MyLogSdf.format(calendar.getTime()));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		LogHelper.Instance().RecordLogServiceLog("LogService onDestroy");
		if (LogHelper.Instance().LogWriter != null) {
			try {
				LogHelper.Instance().LogWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (LogHelper.Instance().ComandProcess != null) {
			LogHelper.Instance().ComandProcess.destroy();
		}

		unregisterReceiver(_sdStateReceiver);
		unregisterReceiver(_logTaskReceiver);
	}

}