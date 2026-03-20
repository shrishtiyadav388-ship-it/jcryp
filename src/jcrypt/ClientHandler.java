package jcrypt;

import java.io.*;
import java.net.Socket;
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final FileProcessor fileProcessor;
    private final String clientId;

    public ClientHandler(Socket clientSocket, int clientNumber) {
        this.clientSocket = clientSocket;
        this.fileProcessor = new FileProcessor();
        this.clientId = "Client#" + clientNumber + " [" +
                clientSocket.getInetAddress().getHostAddress() + "]";
    }

    @Override
    public void run() {
        Utils.log(clientId, "Connected.");
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
            Utils.log(clientId, "Receiving file: " + filename + " (" +
                    Utils.formatSize(fileSize) + ")");

            byte[] fileData = new byte[(int) fileSize];
            in.readFully(fileData);
            Utils.log(clientId, "File received successfully.");

        
            String tempPath = System.getProperty("java.io.tmpdir") + File.separator +
                    "jcrypt_" + filename;
            try (FileOutputStream fos = new FileOutputStream(tempPath)) {
                fos.write(fileData);
            
            String resultPath;
            if (operation == Constants.OP_ENCRYPT) {
                Utils.log(clientId, "Operation: ENCRYPT");
                resultPath = fileProcessor.encryptFile(tempPath, password);
            } else if (operation == Constants.OP_DECRYPT) {
                Utils.log(clientId, "Operation: DECRYPT");
                resultPath = fileProcessor.decryptFile(tempPath, password);
            } else {
                throw new IOException("Unknown operation: " + operation);
            }

            byte[] resultData;
            try (FileInputStream fis = new FileInputStream(resultPath)) {
                resultData = fis.readAllBytes();
            }

            
            out.writeByte(Constants.STATUS_OK);
            String resultFilename = new File(resultPath).getName()
                    .replace("jcrypt_", ""); 
            byte[] resultNameBytes = resultFilename.getBytes();
            out.writeInt(resultNameBytes.length);
            out.write(resultNameBytes);
            out.writeLong(resultData.length);
            out.write(resultData);
            out.flush();

            Utils.log(clientId, "Response sent. Result: " + resultFilename
                    + " (" + Utils.formatSize(resultData.length) + ")");

    
            new File(tempPath).delete();
            new File(resultPath).delete();

        } catch (WrongPasswordException e) {
            Utils.log(clientId, "WRONG PASSWORD: " + e.getMessage());
            sendError(out, Constants.STATUS_WRONG_PASSWORD, e.getMessage());
        } catch (Exception e) {
            Utils.log(clientId, "ERROR: " + e.getMessage());
            sendError(out, Constants.STATUS_ERROR, "Server error: " + e.getMessage());
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException ignored) {
            }
            try {
                if (out != null)
                    out.close();
            } catch (IOException ignored) {
            }
            try {
                clientSocket.close();
            } catch (IOException ignored) {
            }
            Utils.log(clientId, "Disconnected.");
        }
    }

    private void sendError(DataOutputStream out, byte statusCode, String message) {
        if (out == null)
            return;
        try {
            out.writeByte(statusCode);
            byte[] msgBytes = message.getBytes();
            out.writeInt(msgBytes.length);
            out.write(msgBytes);
            out.flush();
        } catch (IOException ignored) {
        }
    }
}
