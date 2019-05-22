package com.bnrc.bnrcbus.util;

import org.bouncycastle.util.encoders.Base64;

import java.security.MessageDigest;

public class MyCipher {


	public MyCipher(String key) {
		byte[] k = this._md5(key).getBytes();
		this.s_box = this._get_s_box(k);
	}

	public int[] s_box = new int[256];

	public String _md5(String data) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			byte[] btInput = data.getBytes();
			// 获得MD5摘要算法的 MessageDigest 对象
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			// 使用指定的字节更新摘要
			mdInst.update(btInput);
			// 获得密文
			byte[] md = mdInst.digest();
			// 把密文转换成十六进制的字符串形式
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public int[] _get_s_box(byte[] key) {
		int[] s_box = new int[256];
		for (int i = 0; i < s_box.length; i++)
			s_box[i] = i;

		int j = 0;
		for (int i = 0; i < s_box.length; i++) {
			j = (j + s_box[i] + key[i % key.length]) % 256;
			int tmp = s_box[i];
			s_box[i] = s_box[j];
			s_box[j] = tmp;
		}
		return s_box;
	}

	public int[] calc(byte[] data) {
		int[] s_box = new int[this.s_box.length];
		for (int i = 0; i < this.s_box.length; i++)
			s_box[i] = this.s_box[i];
		int[] results = new int[data.length];
		for (int i = 0; i < results.length; i++)
			results[i] = i;
		int j = 0;
		for (int i = 0; i < data.length; i++) {
			int k = (i + 1) % 256;
			j = (j + s_box[k]) % 256;
			int tmp = s_box[k];
			s_box[k] = s_box[j];
			s_box[j] = tmp;
			int n = (s_box[j] + s_box[k]) % 256;
			results[i] = data[i] ^ s_box[n];
		}
		return results;
	}

	public String decrypt(String message) {
		// TODO 没写完.
		if (message.length() > 0) {
			byte[] data = Base64.decode(message);
			int[] value = this.calc(data);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < value.length; i++) {
				data[i] = (byte) value[i];
			}
			return new String(data);
		} else {
			return message;
		}
	}

	public String encrypt(String message) {
		// TODO 没写完.
		byte[] data = message.getBytes();
		int[] value = this.calc(data);
		for (int i : value) {
			// System.out.println(i);
		}
		return null;
	}
}
