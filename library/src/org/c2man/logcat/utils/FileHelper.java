package org.c2man.logcat.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

public class FileHelper {

	private static FileHelper _instance = null;

	public static FileHelper Instance() {
		if (_instance == null) {
			_instance = new FileHelper();
		}
		return _instance;
	}

	public boolean CreateDirectory(String path) {
		boolean ok = false;
		File file = new File(path);

		if (!file.exists()) {
			try {
				// ����ָ����·�������ļ���
				ok = file.mkdirs();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}

		return ok;
	}

	/**
	 * �����ļ�
	 * 
	 * @param source
	 * @param target
	 * @return
	 */
	public boolean CopyFile(File source, File target) {
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			if (!target.exists()) {
				boolean createSucc = target.createNewFile();
				if (!createSucc) {
					return false;
				}
			}
			in = new FileInputStream(source);
			out = new FileOutputStream(target);
			byte[] buffer = new byte[8 * 1024];
			int count;
			while ((count = in.read(buffer)) != -1) {
				out.write(buffer, 0, count);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(Constant.TAG, e.getMessage(), e);
			LogHelper.Instance().RecordLogServiceLog("copy file fail");
			return false;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(Constant.TAG, e.getMessage(), e);
				LogHelper.Instance().RecordLogServiceLog("copy file fail");
				return false;
			}
		}

	}

	public void CopyDirectory(String source, String target) {
		if (!CreateDirectory(target))
			return;

		File file = new File(source);
		if (file.isDirectory()) {
			File[] allFiles = file.listFiles();
			for (File logFile : allFiles) {
				String fileName = logFile.getName();
				if (Constant.LogServiceLogName.equals(fileName)) {
					continue;
				}

				boolean isSucc = CopyFile(logFile, new File(target
						+ File.separator + fileName));
				if (isSucc) {
					logFile.delete();
				}
			}
		}
	}

	/**
	 * ȥ���ļ�����չ���ͣ�.log��
	 * 
	 * @param fileName
	 * @return
	 */
	public String GetFileNameWithoutExtension(String fileName) {
		return fileName.substring(0, fileName.indexOf("."));
	}

	public int Compare(File file1, File file2) {
		if (Constant.LogServiceLogName.equals(file1.getName())) {
			return -1;
		} else if (Constant.LogServiceLogName.equals(file2.getName())) {
			return 1;
		}

		String createInfo1 = GetFileNameWithoutExtension(file1.getName());
		String createInfo2 = GetFileNameWithoutExtension(file2.getName());

		try {
			Date create1 = Constant.Sdf.parse(createInfo1);
			Date create2 = Constant.Sdf.parse(createInfo2);
			if (create1.before(create2)) {
				return -1;
			} else {
				return 1;
			}
		} catch (ParseException e) {
			return 0;
		}
	}

	/**
	 * ����ļ���
	 */
	public void ClearDirectory(String path, int outOfDate) {
		File file = new File(path);
		if (file.isDirectory()) {
			File[] allFiles = file.listFiles();
			for (File logFile : allFiles) {
				String fileName = logFile.getName();
				if (Constant.LogServiceLogName.equals(fileName)) {
					continue;
				}
				String createDateInfo = GetFileNameWithoutExtension(fileName);
				if (IsOutOfDate(createDateInfo, outOfDate)) {
					logFile.delete();
					Log.d(Constant.TAG,
							"delete expired log success,the log path is:"
									+ logFile.getAbsolutePath());

				}
			}
		}
	}

	/**
	 * �ж�sdcard�ϵ���־�ļ��Ƿ����ɾ��
	 * 
	 * @param createDateStr
	 * @return
	 */
	public boolean IsOutOfDate(String createDateStr, int days) {
		boolean canDel = false;
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, -1 * days);// ɾ��7��֮ǰ��־
		Date expiredDate = calendar.getTime();
		try {
			Date createDate = Constant.Sdf.parse(createDateStr);
			canDel = createDate.before(expiredDate);
		} catch (ParseException e) {
			Log.e(Constant.TAG, e.getMessage(), e);
			canDel = false;
		}
		return canDel;
	}

	/**
	 * ��ȡ��ǰӦ�洢���ڴ��л��Ǵ洢��SDCard��
	 * 
	 * @return
	 */
	public int GetCurrLogType() {
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return Constant.MEMORY_TYPE;
		} else {
			return Constant.SDCARD_TYPE;
		}
	}

	public boolean CheckFileSize() {

		return false;
	}
}
