package jcrypt;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {

        System.out.println("\n J-Crypt File Security Tool \n");
        System.out.println(" Java pbl project ");
        System.out.println();

        String ip = "localhost";
        if (args.length > 0) {
            ip = args[0];
        }

        Scanner sc = new Scanner(System.in);
        System.out.println();
        while (true) {
            showMenu();
            System.out.print("Enter choice: ");
            String ch = sc.nextLine().trim();

            if (ch.equals("1")) {
                doOperation(sc, ip, Constants.OP_ENCRYPT);
            } else if (ch.equals("2")) {
                doOperation(sc, ip, Constants.OP_DECRYPT);
            } else if (ch.equals("3")) {
                System.out.println("Exit");
                sc.close();
                System.exit(0);
            } else {
                System.out.println("Wrong choice please enter 1, 2 or 3");
            }
        }
    }

    static void showMenu() {

        System.out.println("Menu : features select operation to perform");

        System.out.println(" 1. Encrypt file");
        System.out.println(" 2. Decrypt file");
        System.out.println(" 3. Exit");

    }

    static void doOperation(Scanner sc, String ip, byte op) {

        if (op == Constants.OP_ENCRYPT) {
            System.out.println("\n-- ENCRYPT --");
        } else {
            System.out.println("\n-- DECRYPT --");
        }

        System.out.print("Enter file path: ");
        String path = sc.nextLine().trim();

        path = path.replace("\"", "");

        File f = new File(path);
        if (!f.exists() || !f.isFile()) {
            System.out.println("File not found: " + path);
            System.out.println();
            return;
        }

        if (op == Constants.OP_DECRYPT &&
                !path.endsWith(Constants.ENCRYPTED_EXTENSION)) {
            System.out.println("Error--select a .jcrypt file for decryption");
            System.out.println();
            return;
        }
        System.out.print("Enter password: ");
        String pass = sc.nextLine().trim();

        if (pass.length() == 0 && pass.length() < 8) {
            System.out.println("Password cannot be empty");
            System.out.println();
            return;
        }
        if (pass.length() < 8) {
            System.out.println("Password must 8 characters long!");
            System.out.println();
            return;
        }

        if (op == Constants.OP_DECRYPT) {
            System.out.print("Re-enter password: ");
            String pass2 = sc.nextLine().trim();
            if (!pass.equals(pass2)) {
                System.out.println("Passwords dont match");
                System.out.println();
                return;
            }
        }

        System.out.print("Save folder ");
        String folder = sc.nextLine().trim();

        if (folder.length() == 0) {
            folder = f.getParent();
            if (folder == null)
                folder = ".";
        }

        File saveFolder = new File(folder);
        if (!saveFolder.exists()) {
            System.out.println("Folder not found: " + folder);
            System.out.println();
            return;
        }

        System.out.println("\nConnecting to server...");
        sendToServer(ip, op, pass, f, folder);
    }

    static void sendToServer(String ip, byte op, String pass, File f, String saveFolder) {

        try {
            Socket socket = new Socket(ip, Constants.PORT);
            DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            DataInputStream din = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            System.out.println("Connected! Sending file...");

            FileInputStream fis = new FileInputStream(f);
            byte[] fileData = fis.readAllBytes();
            fis.close();

            dout.writeByte(op);

            byte[] passBytes = pass.getBytes();
            dout.writeInt(passBytes.length);
            dout.write(passBytes);

            byte[] nameBytes = f.getName().getBytes();
            dout.writeInt(nameBytes.length);
            dout.write(nameBytes);

            dout.writeLong(fileData.length);
            dout.write(fileData);
            dout.flush();

            System.out.println("File sent! Size: " + fileData.length + " bytes");
            System.out.println("Waiting for server to response--");

            byte status = din.readByte();

            if (status == Constants.STATUS_OK) {

                int nlen = din.readInt();
                byte[] nBytes = new byte[nlen];
                din.readFully(nBytes);
                String outName = new String(nBytes);

                long outSize = din.readLong();
                byte[] outData = new byte[(int) outSize];
                din.readFully(outData);

                String outPath = saveFolder + File.separator + outName;

                if (new File(outPath).exists()) {
                    outPath = saveFolder + File.separator + "copy_" + outName;
                }

                FileOutputStream fos = new FileOutputStream(outPath);
                fos.write(outData);
                fos.close();

                System.out.println();
                System.out.println(" File saved at: " + outPath);
                System.out.println("Size: " + outData.length + " bytes");

            } else if (status == Constants.STATUS_WRONG_PASSWORD) {

                int mlen = din.readInt();
                byte[] mBytes = new byte[mlen];
                din.readFully(mBytes);
                System.out.println("Wrong password! " + new String(mBytes));

            } else {

                int mlen = din.readInt();
                byte[] mBytes = new byte[mlen];
                din.readFully(mBytes);
                System.out.println("Server error: " + new String(mBytes));
            }

            din.close();
            dout.close();
            socket.close();

        } catch (java.net.ConnectException e) {
            System.out.println("Cannot connect to server " + ip + ":" + Constants.PORT);
            System.out.println("Make sure server is running first!");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.println();
    }
}