// made by team codecrafters
// tcs-408 java project semester 4
// aman, aniket, shrishti, atulya
// this file does the actual encryption and decryption
// we made our own algorithm instead of using java built in aes

package jcrypt;

import java.util.HashMap;
import java.security.SecureRandom;

public class CryptoEngine {

    // sbox is a lookup table that maps each byte to another byte
    // used for substitution in encryption
    HashMap<Byte, Byte> sBox;

    // inverse sbox is used to reverse the substitution during decryption
    HashMap<Byte, Byte> sBoxInverse;

    // secure random for generating salt
    SecureRandom random = new SecureRandom();

    // constructor builds the sbox when object is created
    public CryptoEngine() {
        sBox = new HashMap<>();
        sBoxInverse = new HashMap<>();
        buildSBox();
    }

    // this creates the substitution table using fisher yates shuffle
    // we learned about this algorithm while researching encryption
    void buildSBox() {

        // first fill table with 0 to 255
        byte[] table = new byte[256];
        for (int i = 0; i < 256; i++) {
            table[i] = (byte) i;
        }

        // shuffle the table using a fixed seed
        // fixed seed means same shuffle every time which is important
        long seed = 0xDEADBEEFL;
        for (int i = 255; i > 0; i--) {
            seed = (seed * 6364136223846793005L + 1442695040888963407L);
            int j = (int) ((seed >>> 33) % (i + 1));
            if (j < 0)
                j = -j;

            // swap table[i] and table[j]
            byte tmp = table[i];
            table[i] = table[j];
            table[j] = tmp;
        }

        // store in both hashmaps
        // sbox for encrypt, sboxinverse for decrypt
        for (int i = 0; i < 256; i++) {
            sBox.put((byte) i, table[i]);
            sBoxInverse.put(table[i], (byte) i);
        }
    }

    // replace each byte with its sbox value
    byte[] substitute(byte[] data) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = sBox.get(data[i]);
        }
        return result;
    }

    // reverse substitute using inverse sbox
    byte[] substituteInverse(byte[] data) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = sBoxInverse.get(data[i]);
        }
        return result;
    }

    // rotate array left by shift positions
    byte[] permute(byte[] data) {
        if (data.length <= 1)
            return data;
        byte[] result = new byte[data.length];

        // calculate shift amount from data itself
        int shift = 0;
        for (int i = 0; i < data.length; i++) {
            shift = (shift + (data[i] & 0xFF)) % data.length;
        }

        // rotate left
        for (int i = 0; i < data.length; i++) {
            result[i] = data[(i + shift) % data.length];
        }
        return result;
    }

    // rotate array right to reverse the permute
    byte[] permuteInverse(byte[] data) {
        if (data.length <= 1)
            return data;
        byte[] result = new byte[data.length];

        // same shift calculation
        int shift = 0;
        for (int i = 0; i < data.length; i++) {
            shift = (shift + (data[i] & 0xFF)) % data.length;
        }

        // rotate right (opposite of left)
        for (int i = 0; i < data.length; i++) {
            result[(i + shift) % data.length] = data[i];
        }
        return result;
    }

    // ─── UTILS METHODS MOVED HERE ───
    // we moved these from Utils.java so we dont need separate file

    // generates random salt bytes using securerandom
    // salt makes sure same password gives different key each time
    byte[] generateSalt(int length) {
        byte[] salt = new byte[length];
        random.nextBytes(salt);
        return salt;
    }

    // xor two byte arrays together
    // key cycles if shorter than data
    byte[] xorBytes(byte[] data, byte[] key) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key[i % key.length]);
        }
        return result;
    }

    // simple checksum - sum of all bytes mod 256
    // used to verify correct password during decryption
    byte checksum(byte[] data) {
        int sum = 0;
        for (int i = 0; i < data.length; i++) {
            sum = (sum + (data[i] & 0xFF)) % 256;
        }
        return (byte) sum;
    }

    // creates a strong 32 byte key from password and salt
    // does 1000 rounds to slow down brute force attacks
    byte[] deriveKey(String password, byte[] salt) {
        byte[] passBytes = password.getBytes();
        byte[] key = new byte[32];

        // fill key with password bytes
        for (int i = 0; i < 32; i++) {
            key[i] = passBytes[i % passBytes.length];
        }

        // mix in the salt
        for (int i = 0; i < 32; i++) {
            key[i] ^= salt[i % salt.length];
        }

        // 1000 rounds of mixing
        // more rounds = harder to brute force
        for (int round = 0; round < 1000; round++) {
            for (int i = 0; i < 32; i++) {
                key[i] ^= key[(i + 1) % 32];
                key[i] = (byte) ((key[i] << 1) | ((key[i] & 0xFF) >>> 7));
                key[i] ^= (byte) round;
            }
        }
        return key;
    }

    // ─── MAIN ENCRYPT METHOD ───
    // takes file bytes and password
    // returns encrypted bytes with checksum and salt at start
    public byte[] encrypt(byte[] data, String password) {

        // generate random salt
        byte[] salt = generateSalt(Constants.SALT_LENGTH);

        // derive key from password and salt
        byte[] key = deriveKey(password, salt);

        // run 3 rounds of encryption
        // each round: xor -> substitute -> permute
        byte[] processed = data;
        for (int round = 0; round < Constants.ENCRYPTION_ROUNDS; round++) {
            processed = xorBytes(processed, key);
            processed = substitute(processed);
            if (processed.length > 1) {
                processed = permute(processed);
            }
        }

        // calculate checksum of original data
        // stored at start so we can verify decryption later
        byte check = checksum(data);

        // final format: [1 byte checksum][16 bytes salt][encrypted data]
        byte[] result = new byte[1 + Constants.SALT_LENGTH + processed.length];
        result[0] = check;
        System.arraycopy(salt, 0, result, 1, Constants.SALT_LENGTH);
        System.arraycopy(processed, 0, result, 1 + Constants.SALT_LENGTH, processed.length);

        return result;
    }

    // ─── MAIN DECRYPT METHOD ───
    // takes encrypted bytes and password
    // returns original file bytes
    public byte[] decrypt(byte[] encryptedData, String password) throws WrongPasswordException {

        // file must have at least checksum + salt
        if (encryptedData.length < 1 + Constants.SALT_LENGTH) {
            throw new WrongPasswordException("file too small or corrupted");
        }

        // read checksum from byte 0
        byte storedCheck = encryptedData[0];

        // read salt from bytes 1 to 16
        byte[] salt = new byte[Constants.SALT_LENGTH];
        System.arraycopy(encryptedData, 1, salt, 0, Constants.SALT_LENGTH);

        // rest is the actual encrypted data
        byte[] cipherData = new byte[encryptedData.length - 1 - Constants.SALT_LENGTH];
        System.arraycopy(encryptedData, 1 + Constants.SALT_LENGTH, cipherData, 0, cipherData.length);

        // derive same key using same password and stored salt
        byte[] key = deriveKey(password, salt);

        // reverse the 3 rounds
        // must be exact opposite order of encryption
        // encrypt was: xor -> substitute -> permute
        // decrypt is: permuteInverse -> substituteInverse -> xor
        byte[] processed = cipherData;
        for (int round = 0; round < Constants.ENCRYPTION_ROUNDS; round++) {
            if (processed.length > 1) {
                processed = permuteInverse(processed);
            }
            processed = substituteInverse(processed);
            processed = xorBytes(processed, key);
        }

        // verify checksum to confirm correct password
        byte computedCheck = checksum(processed);
        if (computedCheck != storedCheck) {
            throw new WrongPasswordException("wrong password or file corrupted!");
        }

        return processed;
    }
}