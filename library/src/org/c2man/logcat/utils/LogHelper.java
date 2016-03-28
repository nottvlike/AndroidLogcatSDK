package org.c2man.logcat.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.c2man.logcat.AndroidLogcatSdk;
import org.c2man.logcat.thread.LogCollectorThread;
import org.c2man.logcat.thread.StreamConsumer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class LogHelper {

	private static LogHelper _instance;

	public String MemoryLogPath;
	public String ServiceLogPath;
	public String SdcardLogPath;
	public String CurrentInstallLogName = "";
	public int CurrentLogType = 0;

	public Process ComandProcess;
	public WakeLock PartialWakeLock;
	public Service LogService;

	public OutputStreamWriter LogWriter;

	private boolean logSizeMoniting = false;

	public static LogHelper Instance() {
		if (_instance == null)
			_instance = new LogHelper();
		return _instance;
	}

	public void Init(Service service) {
		Log.d(Constant.TAG, "LogHelper Init");
		LogService = service;

		MemoryLogPath = service.getFilesDir().getAbsolutePath()
				+ File.separator + "log";
		ServiceLogPath = LogHelper.Instance().MemoryLogPath + File.separator
				+ Constant.LogServiceLogName;
		SdcardLogPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ File.separator
				+ "Android/data/"
				+ AndroidLogcatSdk.Instance().MainActivity.getPackageName()
				+ File.separator + "log";
		Log.d(Constant.TAG, MemoryLogPath);
		CreateLogDirectory();

		try {
			LogWriter = new OutputStreamWriter(new FileOutputStream(
					ServiceLogPath, true));
		} catch (FileNotFoundException e) {
			Log.e(Constant.TAG, e.getMessage(), e);
		}

		CurrentLogType = FileHelper.Instance().GetCurrLogType();
	}

	/**
	 * �����־�ļ���С�Ƿ񳬹��˹涨��С ������������¿���һ����־�ռ�����
	 */
	public void CheckLogSize() {
		if (CurrentInstallLogName != null && !"".equals(CurrentInstallLogName)) {
			String path = MemoryLogPath + File.separator
					+ CurrentInstallLogName;
			File file = new File(path);
			if (!file.exists()) {
				return;
			}
			Log.d(Constant.TAG,
					"checkLog() ==> The size of the log is too big?");
			if (file.length() >= Constant.MEMORY_LOG_FILE_MAX_SIZE) {
				Log.d(Constant.TAG, "The log's size is too big!");
				new LogCollectorThread().start();
			}
		}
	}

	/**
	 * ������־Ŀ¼
	 */
	public void CreateLogDirectory() {
		if (!(FileHelper.Instance().CreateDirectory(MemoryLogPath)))
			Log.d(Constant.TAG,
					String.format("CreateMemoryLogFailed %s!", MemoryLogPath));

		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			if (!(FileHelper.Instance().CreateDirectory(SdcardLogPath)))
				Log.d(Constant.TAG, String.format("CreateSdcardLogFailed %s!",
						SdcardLogPath));
		}
	}

	/**
	 * ����־�ļ�ת�Ƶ�SD������
	 */
	public void MoveLogfile() {
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			RecordLogServiceLog("move file failed, sd card does not mount");
			return;
		}

		FileHelper.Instance().CopyDirectory(MemoryLogPath, SdcardLogPath);
	}

	/**
	 * ɾ���ڴ��¹��ڵ���־
	 */
	public void DeleteSDcardExpiredLog() {
		FileHelper.Instance().ClearDirectory(SdcardLogPath,
				Constant.SDCARD_LOG_FILE_SAVE_DAYS);
	}

	/**
	 * ɾ���ڴ��еĹ�����־��ɾ������ ���˵�ǰ����־���뵱ǰʱ���������־���������Ķ�ɾ��
	 */
	public void DeleteMemoryExpiredLog() {
		FileHelper.Instance().ClearDirectory(MemoryLogPath,
				Constant.SDCARD_LOG_FILE_SAVE_DAYS);
	}

	/**
	 * ��ʼ�ռ���־��Ϣ
	 */
	public void CreateLogCollector() {
		CurrentInstallLogName = Constant.Sdf.format(new Date()) + ".log";// ��־�ļ�����
		List<String> commandList = new ArrayList<String>();
		commandList.add("logcat");
		commandList.add("-f");
		// commandList.add(LOG_PATH_INSTALL_DIR + File.separator + logFileName);
		commandList.add(GetLogPath() + CurrentInstallLogName);
		commandList.add("-v");
		commandList.add("time");
		commandList.add("*:I");

		// commandList.add("*:E");// �������еĴ�����Ϣ

		// ����ָ��TAG����Ϣ
		// commandList.add("MyAPP:V");
		// commandList.add("*:S");
		try {
			ComandProcess = Runtime.getRuntime().exec(
					commandList.toArray(new String[commandList.size()]));
			RecordLogServiceLog("start collecting the log,and log name is:"
					+ CurrentInstallLogName);
			// process.waitFor();
		} catch (Exception e) {
			Log.e(Constant.TAG, "CollectorThread == >" + e.getMessage(), e);
			RecordLogServiceLog("CollectorThread == >" + e.getMessage());
		}
	}

	/**
	 * ÿ�μ�¼��־֮ǰ�������־�Ļ���, ��Ȼ����������־�ļ��м�¼�ظ�����־
	 */
	public void ClearLogCache() {
		Process proc = null;
		List<String> commandList = new ArrayList<String>();
		commandList.add("logcat");
		commandList.add("-c");
		try {
			proc = Runtime.getRuntime().exec(
					commandList.toArray(new String[commandList.size()]));
			StreamConsumer errorGobbler = new StreamConsumer(
					proc.getErrorStream());

			StreamConsumer outputGobbler = new StreamConsumer(
					proc.getInputStream());

			errorGobbler.start();
			outputGobbler.start();
			if (proc.waitFor() != 0) {
				Log.e(Constant.TAG, " clearLogCache proc.waitFor() != 0");
				RecordLogServiceLog("clearLogCache clearLogCache proc.waitFor() != 0");
			}
		} catch (Exception e) {
			Log.e(Constant.TAG, "clearLogCache failed", e);
			RecordLogServiceLog("clearLogCache failed");
		} finally {
			try {
				proc.destroy();
			} catch (Exception e) {
				Log.e(Constant.TAG, "clearLogCache failed", e);
				RecordLogServiceLog("clearLogCache failed");
			}
		}
	}

	/**
	 * ���ݵ�ǰ�Ĵ洢λ�õõ���־�ľ��Դ洢·��
	 * 
	 * @return
	 */
	public String GetLogPath() {
		if (CurrentLogType == Constant.MEMORY_TYPE) {
			return MemoryLogPath + File.separator;
		} else {
			return SdcardLogPath + File.separator;
		}
	}

	/**
	 * ������־�ļ� 1.�����־�ļ��洢λ���л����ڴ��У�ɾ����������д����־�ļ� ���Ҳ�����־��С������񣬿�����־��С�������涨ֵ
	 * 2.�����־�ļ��洢λ���л���SDCard�У�ɾ��7��֮ǰ����־���� �����д洢���ڴ��е���־��SDCard�У�����֮ǰ�������־��С ���ȡ��
	 */
	public void HandleLog() {
		if (CurrentLogType == Constant.MEMORY_TYPE) {
			DeployLogSizeMonitorTask();
			DeleteMemoryExpiredLog();
		} else {
			MoveLogfile();
			CancelLogSizeMonitorTask();
			DeleteSDcardExpiredLog();
		}
	}

	/**
	 * ������־��С�������
	 */
	public void DeployLogSizeMonitorTask() {
		if (logSizeMoniting) { // �����ǰ���ڼ���ţ�����Ҫ��������
			return;
		}
		logSizeMoniting = true;
		Intent intent = new Intent(Constant.MONITOR_LOG_SIZE_ACTION);
		PendingIntent sender = PendingIntent.getBroadcast(LogService, 0,
				intent, 0);
		AlarmManager am = (AlarmManager) LogService
				.getSystemService(LogService.ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
				Constant.MEMORY_LOG_FILE_MONITOR_INTERVAL, sender);
		Log.d(Constant.TAG, "deployLogSizeMonitorTask() succ !");
	}

	/**
	 * ȡ��������־��С�������
	 */
	public void CancelLogSizeMonitorTask() {
		logSizeMoniting = false;
		AlarmManager am = (AlarmManager) LogService
				.getSystemService(LogService.ALARM_SERVICE);
		Intent intent = new Intent(Constant.MONITOR_LOG_SIZE_ACTION);
		PendingIntent sender = PendingIntent.getBroadcast(LogService, 0,
				intent, 0);
		am.cancel(sender);

		Log.d(Constant.TAG, "canelLogSizeMonitorTask() succ");
	}

	/**
	 * ��¼��־����Ļ�����Ϣ ��ֹ��־�����д���LogCat��־���޷����� ����־����ΪLog.log
	 * 
	 * @param msg
	 */
	public void RecordLogServiceLog(String msg) {
		if (LogWriter != null) {
			try {
				Date time = new Date();
				LogWriter.write(Constant.MyLogSdf.format(time) + " : " + msg);
				LogWriter.write("\n");
				LogWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(Constant.TAG, e.getMessage(), e);
			}
		}
	}
}
