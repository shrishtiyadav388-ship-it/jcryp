package jcrypt;

import java.util.HashMap;

public class CryptoEngine {

    private final HashMap<Byte, Byte> sBox;
    private final HashMap<Byte, Byte> sBoxInverse;

    public CryptoEngine() {
        sBox = new HashMap<>();
        sBoxInverse = new HashMap<>();
        buildSBox();
    }

    private void buildSBox() {
        byte[] table = new byte[256];
        for (int i = 0; i < 256; i++)
            table[i] = (byte) i;
        long seed = 0xDEADBEEFL;
        for (int i = 255; i > 0; i--) {
            seed = (seed * 6364136223846793005L + 1442695040888963407L);
            int j = (int) ((seed >>> 33) % (i + 1));
            if (j < 0)
                j = -j;
            byte tmp = table[i];
            table[i] = table[j];
            table[j] = tmp;
        }

        for (int i = 0; i < 256; i++) {
            sBox.put((byte) i, table[i]);
            sBoxInverse.put(table[i], (byte) i);
        }
    }

    private byte[] substitute(byte[] data) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = sBox.get(data[i]);
        }
        return result;
    }

    private byte[] substituteInverse(byte[] data) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = sBoxInverse.get(data[i]);
        }
        return result;
    }

    private byte[] permute(byte[] data) {
        if (data.length <= 1)
            return data;
        byte[] result = new byte[data.length];
        int shift = 0;
        for (byte b : data)
            shift = (shift + (b & 0xFF)) % data.length;
        for (int i = 0; i < data.length; i++) {
            result[i] = data[(i + shift) % data.length];
        }
        return result;
    }

    private byte[] permuteInverse(byte[] data) {
        if (data.length <= 1)
            return data;
        byte[] result = new byte[data.length];
        int shift = 0;
        for (byte b : data)
            shift = (shift + (b & 0xFF)) % data.length;
        for (int i = 0; i < data.length; i++) {
            result[(i + shift) % data.length] = data[i];
        }
        return result;
    }

    public byte[] encrypt(byte[] data, String password) {
        byte[] salt = Utils.generateSalt(Constants.SALT_LENGTH);
        byte[] key = Utils.deriveKey(password, salt);

        byte[] processed = data;
        for (int round = 0; round < Constants.ENCRYPTION_ROUNDS; round++) {
            processed = Utils.xorBytes(processed, key);
            processed = substitute(processed);
            if (processed.length > 1) {
                processed = permute(processed);
            }
        }

        byte checksum = Utils.checksum(data); // checksum of ORIGINAL data
        byte[] result = new byte[1 + Constants.SALT_LENGTH + processed.length];
        result[0] = checksum;
        System.arraycopy(salt, 0, result, 1, Constants.SALT_LENGTH);
        System.arraycopy(processed, 0, result, 1 + Constants.SALT_LENGTH, processed.length);
        return result;
    }

    public byte[] decrypt(byte[] encryptedData, String password) throws WrongPasswordException {
        if (encryptedData.length < 1 + Constants.SALT_LENGTH) {
            throw new WrongPasswordException("File is too small or corrupted.");
        }

        byte storedChecksum = encryptedData[0];
        byte[] salt = new byte[Constants.SALT_LENGTH];
        System.arraycopy(encryptedData, 1, salt, 0, Constants.SALT_LENGTH);

        byte[] cipherData = new byte[encryptedData.length - 1 - Constants.SALT_LENGTH];
        System.arraycopy(encryptedData, 1 + Constants.SALT_LENGTH, cipherData, 0, cipherData.length);

        byte[] key = Utils.deriveKey(password, salt);

        byte[] processed = cipherData;
        for (int round = 0; round < Constants.ENCRYPTION_ROUNDS; round++) {
            if (processed.length > 1) {
                processed = permuteInverse(processed);
            }
            processed = substituteInverse(processed);
            processed = Utils.xorBytes(processed, key);
        }

        byte computedChecksum = Utils.checksum(processed);
        if (computedChecksum != storedChecksum) {
            throw new WrongPasswordException("Wrong password or file corrupted!");
        }

        return processed;
    }
}