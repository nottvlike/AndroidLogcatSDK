package org.c2man.logcat.utils;

import java.text.SimpleDateFormat;

public class Constant {
	public static final String TAG = "LogService";

	public static final int MEMORY_LOG_FILE_MAX_SIZE = 10 * 1024 * 1024; // �ڴ�����־�ļ����ֵ��10M
	public static final int MEMORY_LOG_FILE_MONITOR_INTERVAL = 10 * 60 * 1000; // �ڴ��е���־�ļ���С���ʱ������10����
	public static final int SDCARD_LOG_FILE_SAVE_DAYS = 7; // sd������־�ļ�����ౣ������

	public static String MONITOR_LOG_SIZE_ACTION = "MONITOR_LOG_SIZE"; // ��־�ļ����action
	public static String SWITCH_LOG_FILE_ACTION = "SWITCH_LOG_FILE_ACTION"; // �л���־�ļ�action

	public static final int SDCARD_TYPE = 0; // ��ǰ����־��¼����Ϊ�洢��SD������
	public static final int MEMORY_TYPE = 1; // ��ǰ����־��¼����Ϊ�洢���ڴ���

	public static final String LogServiceLogName = "Log.log";// �������������־�ļ�����
	public static final SimpleDateFormat MyLogSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static final SimpleDateFormat Sdf = new SimpleDateFormat("yyyy-MM-dd HHmmss");// ��־���Ƹ�ʽ

}
