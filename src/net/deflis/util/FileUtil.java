package net.deflis.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.util.Log;

public class FileUtil {
	public static void copyFile(File srcPath, File dstPath) throws IOException {
		FileChannel srcChannel = new FileInputStream(srcPath).getChannel();
		FileChannel dstChannel = new FileOutputStream(dstPath).getChannel();
		try {
			srcChannel.transferTo(0, srcChannel.size(), dstChannel);
		} finally {
			srcChannel.close();
			dstChannel.close();
		}
	}

	public static String fileNameToFileHash(String fileName) {
		return Hash.getHash(fileName, "SHA-1") + getExtension(fileName);
	}

	public static String urlToFileHash(String url) {
		try {
			return urlToFileHash(new URL(url));
		} catch (MalformedURLException e) {
			return Hash.getHash(url, "SHA-1") + getExtension(url);
		}
	}

	public static String urlToFileHash(URL url) {
		return Hash.getHash(url.toString(), "SHA-1")
				+ getExtension(url.getFile());
	}

	public static String getExtension(String filename) {
		String[] strs = filename.split("\\.");
		if (strs.length > 1)
			return "." + strs[strs.length - 1];
		return "";
	}

	public static void cacheLimit(File cache, long limit) {
		String[] children = cache.list(); // ディレクトリにあるすべてのファイルを処理する
		long size = 0L; // 合計ファイルサイズ
		File oldest = null; // 最古更新ファイルを入れる
		long date = Long.MAX_VALUE; // とりあえず最大値にしておく。

		for (int i = 0; i < children.length; i++) {
			File file = new File(children[i]);
			size += file.length();
			// 更新日時がもっとも古い物を選ぶ
			if (date > file.lastModified()) {
				date = file.lastModified();
				oldest = file;
			}
		}
		if (size > limit && oldest != null) {
			Log.v("FileUtil", "delete: " + oldest.getName());
			size -= oldest.length(); // 削除した場合の合計サイズを計算。
			oldest.delete(); // 削除
			// まだ多い場合はもう1回
			if (size > limit) {
				cacheLimit(cache, limit);
			}
		}

	}

	public static boolean cacheClean(File cache) {
		String[] children = cache.list(); // ディレクトリにあるすべてのファイルを処理する
		for (int i = 0; i < children.length; i++) {
			boolean success = deleteFile(new File(cache, children[i]));
			if (!success) {
				return false;
			}
		}

		return cache.delete();
	}

	public static boolean cacheClean(Context context) {
		File cache = context.getCacheDir(); // キャッシュディレクトリの取得
		String[] children = cache.list(); // ディレクトリにあるすべてのファイルを処理する
		for (int i = 0; i < children.length; i++) {
			boolean success = deleteFile(new File(cache, children[i]));
			if (!success) {
				return false;
			}
		}

		return cache.delete();
	}

	public static boolean deleteFile(File dirOrFile) {
		if (dirOrFile.isDirectory()) { // ディレクトリの場合
			String[] children = dirOrFile.list(); // ディレクトリにあるすべてのファイルを処理する
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteFile(new File(dirOrFile, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		return dirOrFile.delete();
	}
}
