package fsi.fsicli;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

public class Util {
	public static byte[] checksum(String algorithm, InputStream in) throws IOException {
		MessageDigest md;

		try {
			md = MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		}

		return checksum(md, in);
	}

	public static byte[] checksum(MessageDigest md, InputStream in) throws IOException {
		byte[] buf = new byte[8 * 1024];

		while (true) {
			int read = in.read(buf);

			if (read == -1) {
				break;
			}

			md.update(buf, 0, read);
		}

		return md.digest();
	}

	public static String toHex(byte[] data) {
		return DatatypeConverter.printHexBinary(data).toLowerCase();
	}

	public static String SHA256(String s) {
		try {
			byte[] data = s.getBytes("UTF-8");

			return toHex(checksum("SHA-256", new ByteArrayInputStream(data)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
