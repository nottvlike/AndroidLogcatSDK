package org.c2man.logcat.thread;

import java.util.List;

import org.c2man.logcat.process.ProcessInfo;
import org.c2man.logcat.utils.Constant;
import org.c2man.logcat.utils.LogHelper;
import org.c2man.logcat.utils.ProcessHelper;

import android.util.Log;

/**
 * ��־�ռ� 1.�����־���� 2.ɱ��Ӧ�ó����ѿ�����Logcat���̷�ֹ�������д��һ����־�ļ� 3.������־�ռ����� 4.������־�ļ� �ƶ� OR ɾ��
 */
public class LogCollectorThread extends Thread {

	public LogCollectorThread() {
		super("LogCollectorThread");
		Log.d(Constant.TAG, "LogCollectorThread is create");
	}

	@Override
	public void run() {
		try {
			LogHelper.Instance().PartialWakeLock.acquire(); // �����ֻ�

			LogHelper.Instance().ClearLogCache();

			List<String> orgProcessList = ProcessHelper.Instance()
					.GetAllProcess();
			List<ProcessInfo> processInfoList = ProcessHelper.Instance()
					.GetProcessInfoList(orgProcessList);
			ProcessHelper.Instance().KillLogcatProc(processInfoList);

			LogHelper.Instance().CreateLogCollector();

			Thread.sleep(1000);// ���ߣ������ļ���Ȼ�����ļ�����Ȼ���ļ���û��������Ӱ���ļ�ɾ��

			LogHelper.Instance().HandleLog();

			LogHelper.Instance().PartialWakeLock.release(); // �ͷ�
		} catch (Exception e) {
			e.printStackTrace();
			LogHelper.Instance()
					.RecordLogServiceLog(Log.getStackTraceString(e));
		}
	}
}