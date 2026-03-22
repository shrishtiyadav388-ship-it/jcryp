package jcrypt;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    Socket clientSocket;
    FileProcessor fileProcessor;
    int clientNum;

    public ClientHandler(Socket clientSocket, int clientNum) {
        this.clientSocket = clientSocket;
        this.clientNum = clientNum;
        this.fileProcessor = new FileProcessor();
    }

    public void run() {

        System.out.println("handling client " + clientNum);

        DataInputStream in = null;
        DataOutputStream out = null;

        try {
            in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
            byte operation = in.readByte();

            int passLen = in.readInt();
            byte[] passBytes = new byte[passLen];
            in.readFully(passBytes);
            String password = new String(passBytes);
            int nameLen = in.readInt();
            byte[] nameBytes = new byte[nameLen];
            in.readFully(nameBytes);
            String filename = new String(nameBytes);

            long fileSize = in.readLong();
            System.out.println("receiving file: " + filename + " size: " + fileSize + " bytes");

            byte[] fileData = new byte[(int) fileSize];
            in.readFully(fileData);
            System.out.println("file received!");
            String tempPath = System.getProperty("java.io.tmpdir")
                    + File.separator + "jcrypt_" + filename;

            FileOutputStream fos = new FileOutputStream(tempPath);
            fos.write(fileData);
            fos.close();
            String resultPath;
            if (operation == Constants.OP_ENCRYPT) {
                System.out.println("encrypting...");
                resultPath = fileProcessor.encryptFile(tempPath, password);
            } else if (operation == Constants.OP_DECRYPT) {
                System.out.println("decrypting...");
                resultPath = fileProcessor.decryptFile(tempPath, password);
            } else {
                throw new IOException("unknown operation: " + operation);
            }
            FileInputStream fis = new FileInputStream(resultPath);
            byte[] resultData = fis.readAllBytes();
            fis.close();
            String resultFilename = new File(resultPath).getName().replace("jcrypt_", "");
            byte[] resultNameBytes = resultFilename.getBytes();

            out.writeByte(Constants.STATUS_OK);
            out.writeInt(resultNameBytes.length);
            out.write(resultNameBytes);
            out.writeLong(resultData.length);
            out.write(resultData);
            out.flush();

            System.out.println("done! sent result to client " + clientNum);
            new File(tempPath).delete();
            new File(resultPath).delete();

        } catch (WrongPasswordException e) {
            System.out.println("wrong password from client " + clientNum);
            sendError(out, Constants.STATUS_WRONG_PASSWORD, e.getMessage());

        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
            sendError(out, Constants.STATUS_ERROR, "server error: " + e.getMessage());

        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
            }
            try {
                if (out != null)
                    out.close();
            } catch (IOException e) {
            }
            try {
                clientSocket.close();
            } catch (IOException e) {
            }
            System.out.println("client " + clientNum + " disconnected");
        }
    }

    void sendError(DataOutputStream out, byte statusCode, String message) {
        if (out == null)
            return;
        try {
            out.writeByte(statusCode);
            byte[] msgBytes = message.getBytes();
            out.writeInt(msgBytes.length);
            out.write(msgBytes);
            out.flush();
        } catch (IOException e) {
            System.out.println("could not send error to client");
        }
    }
}