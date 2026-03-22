package jcrypt;

import java.io.*;

public class FileProcessor {
    CryptoEngine cryptoEngine;

    public FileProcessor() {
        this.cryptoEngine = new CryptoEngine();
    }

    byte[] readFile(String filePath) throws IOException {

        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("file not found: " + filePath);
        }
        if (!file.isFile()) {
            throw new IOException("this is not a file: " + filePath);
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        BufferedInputStream bis = new BufferedInputStream(
                new FileInputStream(file), Constants.BUFFER_SIZE);

        byte[] chunk = new byte[Constants.BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = bis.read(chunk)) != -1) {
            buffer.write(chunk, 0, bytesRead);
        }

        bis.close();
        return buffer.toByteArray();
    }

    void writeFile(String filePath, byte[] data) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(filePath), Constants.BUFFER_SIZE);
        bos.write(data);
        bos.flush();
        bos.close();
    }

    public String encryptFile(String inputPath, String password) throws IOException {

        System.out.println("reading file: " + inputPath);
        byte[] data = readFile(inputPath);
        System.out.println("file size: " + data.length + " bytes");

        System.out.println("encrypting...");
        byte[] encrypted = cryptoEngine.encrypt(data, password);
        String outputPath = inputPath + Constants.ENCRYPTED_EXTENSION;
        writeFile(outputPath, encrypted);

        System.out.println("encrypted file saved: " + outputPath);
        return outputPath;
    }

    public String decryptFile(String inputPath, String password)
            throws IOException, WrongPasswordException {
        if (!inputPath.endsWith(Constants.ENCRYPTED_EXTENSION)) {
            throw new IOException("not a .jcrypt file: " + inputPath);
        }

        System.out.println("reading encrypted file: " + inputPath);
        byte[] encryptedData = readFile(inputPath);

        System.out.println("decrypting...");
        byte[] decrypted = cryptoEngine.decrypt(encryptedData, password);
        File inputFile = new File(inputPath);
        String originalName = inputFile.getName()
                .replace(Constants.ENCRYPTED_EXTENSION, "");
        String outputPath = inputFile.getParent() + File.separator + originalName;
        if (new File(outputPath).exists()) {
            outputPath = inputFile.getParent() + File.separator + "decrypted_" + originalName;
        }

        writeFile(outputPath, decrypted);
        System.out.println("decrypted file saved: " + outputPath);
        return outputPath;
    }
}