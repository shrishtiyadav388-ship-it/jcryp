package jcrypt;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        System.out.println("welcome to jcrypt");
        System.out.println("file encryption tool");
        System.out.println();
        String ip = "localhost";
        if (args.length > 0) {
            ip = args[0];
        }
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println();
            System.out.println("what do you want to do?");
            System.out.println("1 - encrypt a file");
            System.out.println("2 - decrypt a file");
            System.out.println("3 - exit");
            System.out.print("enter: ");
            String choice = sc.nextLine();
            if (choice.equals("1")) {
                encrypt(sc, ip);
            } else if (choice.equals("2")) {
                decrypt(sc, ip);
            } else if (choice.equals("3")) {
                System.out.println("bye bye!");
                break;
            } else {
                System.out.println("wrong input try again");
            }
        }
    }

    static void encrypt(Scanner sc, String ip) {

        System.out.print("enter file path: ");
        String path = sc.nextLine().trim().replace("\"", "");
        File f = new File(path);
        if (!f.exists()) {
            System.out.println("file not found bro");
            return;
        }

        System.out.print("enter password (min 8 chars): ");
        String pass = sc.nextLine().trim();

        if (pass.length() < 8) {
            System.out.println("password too short!");
            return;
        }
        boolean success = sendFile(ip, Constants.OP_ENCRYPT, pass, f);
        if (success) {
            System.out.print("do you want to delete original file? (y/n): ");
            String ans = sc.nextLine().trim();
            if (ans.equalsIgnoreCase("y")) {
                if (f.delete()) {
                    System.out.println("original file deleted!");
                } else {
                    System.out.println("could not delete file, please delete manually");
                }
            }
        }
    }

    static void decrypt(Scanner sc, String ip) {

        System.out.print("enter .jcrypt file path: ");
        String path = sc.nextLine().trim().replace("\"", "");
        if (!path.endsWith(".jcrypt")) {
            System.out.println("please select a .jcrypt file only");
            return;
        }

        File f = new File(path);
        if (!f.exists()) {
            System.out.println("file not found");
            return;
        }

        System.out.print("enter password: ");
        String pass = sc.nextLine().trim();
        System.out.print("confirm password: ");
        String pass2 = sc.nextLine().trim();
        if (!pass.equals(pass2)) {
            System.out.println("passwords dont match!");
            return;
        }
        sendFile(ip, Constants.OP_DECRYPT, pass, f);
    }

    static boolean sendFile(String ip, byte op, String pass, File f) {

        try {
            System.out.println("connecting to server...");
            Socket s = new Socket(ip, Constants.PORT);
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());
            DataInputStream din = new DataInputStream(s.getInputStream());
            FileInputStream fis = new FileInputStream(f);
            byte[] data = fis.readAllBytes();
            fis.close();

            System.out.println("sending " + data.length + " bytes...");
            dout.writeByte(op);

            byte[] pb = pass.getBytes();
            dout.writeInt(pb.length);
            dout.write(pb);

            byte[] nb = f.getName().getBytes();
            dout.writeInt(nb.length);
            dout.write(nb);

            dout.writeLong(data.length);
            dout.write(data);
            dout.flush();
            byte status = din.readByte();

            if (status == Constants.STATUS_OK) {
                byte[] nameB = new byte[din.readInt()];
                din.readFully(nameB);
                String outName = new String(nameB);

                byte[] outData = new byte[(int) din.readLong()];
                din.readFully(outData);
                String outPath = f.getParent() + File.separator + outName;
                FileOutputStream fos = new FileOutputStream(outPath);
                fos.write(outData);
                fos.close();

                System.out.println("done! file saved: " + outPath);
                s.close();
                return true;

            } else if (status == Constants.STATUS_WRONG_PASSWORD) {
                System.out.println("wrong password!");
            } else {
                System.out.println("something went wrong on server");
            }

            s.close();

        } catch (java.net.ConnectException e) {
            System.out.println("server not running! start server first");
        } catch (IOException e) {
            System.out.println("error: " + e.getMessage());
        }

        return false;
    }
}
