package net.deflis.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {
	public static String getHash(String origin, String algorithm) {
		if ((origin == null) || (algorithm == null))
			return null;
		MessageDigest md = null;

		try {
			md = MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			return null;
		}

		md.reset();
		md.update(origin.getBytes());
		byte[] hash = md.digest();

		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < hash.length; i++) {
			sb.append(Integer.toHexString((hash[i] >> 4) & 0x0F));
			sb.append(Integer.toHexString(hash[i] & 0x0F));
		}

		return sb.toString();
	}
}