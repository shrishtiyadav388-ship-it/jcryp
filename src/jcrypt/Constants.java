package jcrypt;

public class Constants {
    public static final int PORT = 9999;
    public static final int BUFFER_SIZE = 8192;
    public static final int ENCRYPTION_ROUNDS = 3;
    public static final int SALT_LENGTH = 16;
    public static final String ENCRYPTED_EXTENSION = ".jcrypt";
    public static final byte OP_ENCRYPT = 1;
    public static final byte OP_DECRYPT = 2;
    public static final byte STATUS_OK = 0;
    public static final byte STATUS_WRONG_PASSWORD = 1;
    public static final byte STATUS_ERROR = 2;
}
