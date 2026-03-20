package jcrypt;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    private static final SecureRandom secureRandom = new SecureRandom();

    public static byte[] generateSalt(int length) {
        byte[] salt = new byte[length];
        secureRandom.nextBytes(salt);
        return salt;
    }

    public static byte[] xorBytes(byte[] data, byte[] key) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key[i % key.length]);
        }
        return result;
    }

    public static byte checksum(byte[] data) {
        int sum = 0;
        for (byte b : data) {
            sum = (sum + (b & 0xFF)) % 256;
        }
        return (byte) sum;
    }

    public static byte[] deriveKey(String password, byte[] salt) {
        byte[] passBytes = password.getBytes();
        byte[] key = new byte[32];

        for (int i = 0; i < 32; i++) {
            key[i] = passBytes[i % passBytes.length];
        }
        for (int i = 0; i < 32; i++) {
            key[i] ^= salt[i % salt.length];
        }

        for (int round = 0; round < 1000; round++) {
            for (int i = 0; i < 32; i++) {
                key[i] ^= key[(i + 1) % 32];
                key[i] = (byte) ((key[i] << 1) | ((key[i] & 0xFF) >>> 7));
                key[i] ^= (byte) round;
            }
        }
        return key;
    }

    public static String timestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public static void log(String tag, String message) {
        System.out.println("[" + timestamp() + "] [" + tag + "] " + message);
    }

    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public static String formatSize(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }
}
