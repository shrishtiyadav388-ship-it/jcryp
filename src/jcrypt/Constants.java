package jcrypt;

public class Constants {
    static int PORT = 9999;
    static int BUFFER_SIZE = 8192;
    static int ENCRYPTION_ROUNDS = 3;
    static int SALT_LENGTH = 16;
    static String ENCRYPTED_EXTENSION = ".jcrypt";
    static byte OP_ENCRYPT = 1;
    static byte OP_DECRYPT = 2;
    static byte STATUS_OK = 0;
    static byte STATUS_WRONG_PASSWORD = 1;
    static byte STATUS_ERROR = 2;
}
