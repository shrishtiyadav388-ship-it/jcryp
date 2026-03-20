~package jcrypt;

import java.io.*;

public class FileProcessor {

    private final CryptoEngine cryptoEngine;

    public FileProcessor() {
        this.cryptoEngine = new CryptoEngine();
    }

    /** Read entire file into byte array */
    public byte[] readFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) throw new FileNotFoundException("File not found: " + filePath);
        if (!file.isFile()) throw new IOException("Not a file: " + filePath);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (BufferedInputStream bis = new BufferedInputStream(
                new FileInputStream(file), Constants.BUFFER_SIZE)) {
            byte[] chunk = new byte[Constants.BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = bis.read(chunk)) != -1) {
                buffer.write(chunk, 0, bytesRead);
            }
        }
        return buffer.toByteArray();
    }


    public void writeFile(String filePath, byte[] data) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(filePath), Constants.BUFFER_SIZE)) {
            bos.write(data);
        }
    }


    public String encryptFile(String inputPath, String password) throws IOException {
        Utils.log("FileProcessor", "Reading file: " + inputPath);
        byte[] data = readFile(inputPath);
        Utils.log("FileProcessor", "File size: " + Utils.formatSize(data.length));

        Utils.log("FileProcessor", "Encrypting...");
        byte[] encrypted = cryptoEngine.encrypt(data, password);

        String outputPath = inputPath + Constants.ENCRYPTED_EXTENSION;
        writeFile(outputPath, encrypted);
        Utils.log("FileProcessor", "Encrypted file saved: " + outputPath
                + " (" + Utils.formatSize(encrypted.length) + ")");
        return outputPath;
    }


    public String decryptFile(String inputPath, String password)
            throws IOException, WrongPasswordException {

        if (!inputPath.endsWith(Constants.ENCRYPTED_EXTENSION)) {
            throw new IOException("File does not have .jcrypt extension: " + inputPath);
        }

        Utils.log("FileProcessor", "Reading encrypted file: " + inputPath);
        byte[] encryptedData = readFile(inputPath);

        Utils.log("FileProcessor", "Decrypting...");
        byte[] decrypted = cryptoEngine.decrypt(encryptedData, password);


        String outputPath = inputPath.substring(0, inputPath.length() - Constants.ENCRYPTED_EXTENSION.length());

    
        if (new File(outputPath).exists()) {
            outputPath = outputPath + ".decrypted";
        }

        writeFile(outputPath, decrypted);
        Utils.log("FileProcessor", "Decrypted file saved: " + outputPath
                + " (" + Utils.formatSize(decrypted.length) + ")");
        return outputPath;
    }
}
