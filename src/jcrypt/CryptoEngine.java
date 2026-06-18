

package jcrypt;

import java.util.HashMap;
import java.security.SecureRandom;

public class CryptoEngine {
    HashMap<Byte, Byte> sBox;
    HashMap<Byte, Byte> sBoxInverse;
    SecureRandom random = new SecureRandom();
    public CryptoEngine() {
        sBox = new HashMap<>();
        sBoxInverse = new HashMap<>();
        buildSBox();
    }

    void buildSBox() {
        byte[] table = new byte[256];
        for (int i = 0; i < 256; i++) {
            table[i] = (byte) i;
        }

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

    byte[] substitute(byte[] data) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = sBox.get(data[i]);
        }
        return result;
    }
    byte[] substituteInverse(byte[] data) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = sBoxInverse.get(data[i]);
        }
        return result;
    }
    byte[] permute(byte[] data) {
        if (data.length <= 1)
            return data;
        byte[] result = new byte[data.length];
        int shift = 0;
        for (int i = 0; i < data.length; i++) {
            shift = (shift + (data[i] & 0xFF)) % data.length;
        }
        for (int i = 0; i < data.length; i++) {
            result[i] = data[(i + shift) % data.length];
        }
        return result;
    }
    byte[] permuteInverse(byte[] data) {
        if (data.length <= 1)
            return data;
        byte[] result = new byte[data.length];
        int shift = 0;
        for (int i = 0; i < data.length; i++) {
            shift = (shift + (data[i] & 0xFF)) % data.length;
        }
        for (int i = 0; i < data.length; i++) {
            result[(i + shift) % data.length] = data[i];
        }
        return result;
    }
    byte[] generateSalt(int length) {
        byte[] salt = new byte[length];
        random.nextBytes(salt);
        return salt;
    }


    byte[] xorBytes(byte[] data, byte[] key) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key[i % key.length]);
        }
        return result;
    }


    byte checksum(byte[] data) {
        int sum = 0;
        for (int i = 0; i < data.length; i++) {
            sum = (sum + (data[i] & 0xFF)) % 256;
        }
        return (byte) sum;
    }

    byte[] deriveKey(String password, byte[] salt) {
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
    public byte[] encrypt(byte[] data, String password) {

        byte[] salt = generateSalt(Constants.SALT_LENGTH);

        byte[] key = deriveKey(password, salt);

      
        byte[] processed = data;
        for (int round = 0; round < Constants.ENCRYPTION_ROUNDS; round++) {
            processed = xorBytes(processed, key);
            processed = substitute(processed);
            if (processed.length > 1) {
                processed = permute(processed);
            }
        }

        byte check = checksum(data);

        byte[] result = new byte[1 + Constants.SALT_LENGTH + processed.length];
        result[0] = check;
        System.arraycopy(salt, 0, result, 1, Constants.SALT_LENGTH);
        System.arraycopy(processed, 0, result, 1 + Constants.SALT_LENGTH, processed.length);

        return result;
    }

    public byte[] decrypt(byte[] encryptedData, String password) throws WrongPasswordException {

        if (encryptedData.length < 1 + Constants.SALT_LENGTH) {
            throw new WrongPasswordException("file too small or corrupted");
        }
        byte storedCheck = encryptedData[0];

      
        byte[] salt = new byte[Constants.SALT_LENGTH];
        System.arraycopy(encryptedData, 1, salt, 0, Constants.SALT_LENGTH);

        byte[] cipherData = new byte[encryptedData.length - 1 - Constants.SALT_LENGTH];
        System.arraycopy(encryptedData, 1 + Constants.SALT_LENGTH, cipherData, 0, cipherData.length);
        byte[] key = deriveKey(password, salt);

        byte[] processed = cipherData;
        for (int round = 0; round < Constants.ENCRYPTION_ROUNDS; round++) {
            if (processed.length > 1) {
                processed = permuteInverse(processed);
            }
            processed = substituteInverse(processed);
            processed = xorBytes(processed, key);
        }

        byte computedCheck = checksum(processed);
        if (computedCheck != storedCheck) {
            throw new WrongPasswordException("wrong password or file corrupted!");
        }

        return processed;
    }
}
