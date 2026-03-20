// package jcrypt;

// import java.io.*;
// import java.util.Scanner;

// public class Main {

// public static void main(String[] args) {

// System.out.println(" J-Crypt File Security Tool ");
// System.out.println();

// Scanner sc = new Scanner(System.in);
// FileProcessor fp = new FileProcessor();

// while (true) {
// // show menu
// System.out.println(" 1. Encrypt file");
// System.out.println(" 2. Decrypt file");
// System.out.println(" 3. Exit");
// System.out.print("Enter choice: ");
// String ch = sc.nextLine().trim();

// if (ch.equals("1")) {
// // encrypt
// System.out.print("Enter file path: ");
// String path = sc.nextLine().trim();
// path = path.replace("\"", "");

// System.out.print("Enter password: ");
// String pass = sc.nextLine().trim();

// try {
// String result = fp.encryptFile(path, pass);
// System.out.println("Done! Encrypted file: " + result);
// } catch (IOException e) {
// System.out.println("Error: " + e.getMessage());
// }

// } else if (ch.equals("2")) {
// // decrypt
// System.out.print("Enter file path: ");
// String path = sc.nextLine().trim();
// path = path.replace("\"", "");

// System.out.print("Enter password: ");
// String pass = sc.nextLine().trim();

// try {
// String result = fp.decryptFile(path, pass);
// System.out.println("Done! Decrypted file: " + result);
// } catch (IOException e) {
// System.out.println("Error: " + e.getMessage());
// } catch (WrongPasswordException e) {
// System.out.println("Wrong password!");
// }

// } else if (ch.equals("3")) {
// System.out.println("Bye!");
// sc.close();
// System.exit(0);
// } else {
// System.out.println("Invalid choice!");
// }

// System.out.println();
// }
// }
// }
