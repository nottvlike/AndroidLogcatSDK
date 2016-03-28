package org.c2man.logcat.utils;

import java.util.ArrayList;
import java.util.List;

import org.c2man.logcat.thread.StreamConsumer;
import org.c2man.logcat.process.ProcessInfo;

import android.util.Log;

public class ProcessHelper {

	private static ProcessHelper _instance;

	public static ProcessHelper Instance() {
		if (_instance == null)
			_instance = new ProcessHelper();
		return _instance;
	}

	/**
	 * �ر��ɱ���������logcat���̣� �����û�����ɱ������(����Ǳ�������̿�����Logcat�ռ�������ô���ߵ�USERһ��)
	 * ������رջ��ж�����̶�ȡlogcat��־������Ϣд����־�ļ�
	 * 
	 * @param allProcList
	 * @return
	 */
	public void KillLogcatProc(List<ProcessInfo> allProcList) {
		if (LogHelper.Instance().ComandProcess != null) {
			LogHelper.Instance().ComandProcess.destroy();
		}
		String packName = LogHelper.Instance().LogService.getPackageName();
		String myUser = GetAppUser(packName, allProcList);

		for (ProcessInfo processInfo : allProcList) {
			if (processInfo.name.toLowerCase().equals("logcat")
					&& processInfo.user.equals(myUser)) {
				android.os.Process.killProcess(Integer.parseInt(processInfo.pid));
			}
		}
	}

	/**
	 * ��ȡ��������û�����
	 * 
	 * @param packName
	 * @param allProcList
	 * @return
	 */
	public String GetAppUser(String packName, List<ProcessInfo> allProcList) {
		for (ProcessInfo processInfo : allProcList) {
			if (processInfo.name.equals(packName)) {
				return processInfo.user;
			}
		}
		return null;
	}

	/**
	 * ����ps����õ������ݻ�ȡPID��User��name����Ϣ
	 * 
	 * @param orgProcessList
	 * @return
	 */
	public List<ProcessInfo> GetProcessInfoList(List<String> orgProcessList) {
		List<ProcessInfo> procInfoList = new ArrayList<ProcessInfo>();
		for (int i = 1; i < orgProcessList.size(); i++) {
			String processInfo = orgProcessList.get(i);
			String[] proStr = processInfo.split(" ");
			// USER PID PPID VSIZE RSS WCHAN PC NAME
			// root 1 0 416 300 c00d4b28 0000cd5c S /init
			List<String> orgInfo = new ArrayList<String>();
			for (String str : proStr) {
				if (!"".equals(str)) {
					orgInfo.add(str);
				}
			}
			if (orgInfo.size() == 9) {
				ProcessInfo pInfo = new ProcessInfo();
				pInfo.user = orgInfo.get(0);
				pInfo.pid = orgInfo.get(1);
				pInfo.ppid = orgInfo.get(2);
				pInfo.name = orgInfo.get(8);
				procInfoList.add(pInfo);
			}
		}
		return procInfoList;
	}

	/**
	 * ����PS����õ�������Ϣ
	 * 
	 * @return USER PID PPID VSIZE RSS WCHAN PC NAME root 1 0 416 300 c00d4b28
	 *         0000cd5c S /init
	 */
	public List<String> GetAllProcess() {
		List<String> orgProcList = new ArrayList<String>();
		Process proc = null;
		try {
			proc = Runtime.getRuntime().exec("ps");
			StreamConsumer errorConsumer = new StreamConsumer(
					proc.getErrorStream());
			StreamConsumer outputConsumer = new StreamConsumer(
					proc.getInputStream(), orgProcList);

			errorConsumer.start();
			outputConsumer.start();
			if (proc.waitFor() != 0) {
				Log.e(Constant.TAG, "getAllProcess proc.waitFor() != 0");
				LogHelper.Instance().RecordLogServiceLog("getAllProcess proc.waitFor() != 0");
			}
		} catch (Exception e) {
			Log.e(Constant.TAG, "getAllProcess failed", e);
			LogHelper.Instance().RecordLogServiceLog("getAllProcess failed");
		} finally {
			try {
				proc.destroy();
			} catch (Exception e) {
				Log.e(Constant.TAG, "getAllProcess failed", e);
				LogHelper.Instance().RecordLogServiceLog("getAllProcess failed");
			}
		}
		return orgProcList;
	}
}
