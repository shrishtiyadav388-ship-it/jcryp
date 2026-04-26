package jcrypt;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class JCryptGUI extends JFrame {
    JTextField fileField = new JTextField("no file selected");
    JPasswordField passField = new JPasswordField();
    JLabel status = new JLabel("select file and enter password");
    JCheckBox deletebox = new JCheckBox("delete original after encryption");
    JCheckBox showpass = new JCheckBox("show password");

    public JCryptGUI() {
        setTitle("J-Crypt");
        setSize(400, 295);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        p.setBackground(Color.WHITE);

        JLabel t = new JLabel("🔒 J-Crypt - File Security");
        t.setFont(new Font("Arial", Font.BOLD, 17));
        t.setAlignmentX(CENTER_ALIGNMENT);
        p.add(t);
        p.add(Box.createVerticalStrut(12));

        fileField.setForeground(Color.GRAY);
        fileField.setEditable(false);
        JButton browse = new JButton("Browse");
        browse.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                fileField.setText(fc.getSelectedFile().getAbsolutePath());
                fileField.setForeground(Color.BLACK);
            }
        });
        p.add(row("File: ", fileField, browse));
        p.add(Box.createVerticalStrut(8));

        passField.setEchoChar('●');
        p.add(row("Pass: ", passField, null));
        p.add(Box.createVerticalStrut(5));

        for (JCheckBox cb : new JCheckBox[] { showpass, deletebox }) {
            cb.setBackground(Color.WHITE);
            cb.setFont(new Font("Arial", Font.PLAIN, 11));
            p.add(cb);
        }
        showpass.addActionListener(e -> passField.setEchoChar(showpass.isSelected() ? (char) 0 : '●'));
        p.add(Box.createVerticalStrut(10));

        JButton enc = btn("Encrypt 🔒", new Color(200, 50, 50));
        JButton dec = btn("Decrypt 🔓", new Color(30, 100, 180));
        enc.addActionListener(e -> doWork(true));
        dec.addActionListener(e -> doWork(false));
        JPanel br = new JPanel(new GridLayout(1, 2, 10, 0));
        br.setBackground(Color.WHITE);
        br.add(enc);
        br.add(dec);
        p.add(br);
        p.add(Box.createVerticalStrut(10));

        status.setFont(new Font("Arial", Font.PLAIN, 11));
        status.setForeground(Color.GRAY);
        status.setAlignmentX(CENTER_ALIGNMENT);
        p.add(status);
        add(p);
        setVisible(true);
    }

    JPanel row(String label, JTextField field, JButton btn) {
        JPanel r = new JPanel(new BorderLayout(5, 0));
        r.setBackground(Color.WHITE);
        r.add(new JLabel(label), BorderLayout.WEST);
        r.add(field, BorderLayout.CENTER);
        if (btn != null)
            r.add(btn, BorderLayout.EAST);
        return r;
    }

    JButton btn(String txt, Color bg) {
        JButton b = new JButton(txt);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Arial", Font.BOLD, 13));
        b.setFocusPainted(false);
        return b;
    }

    void doWork(boolean enc) {
        String path = fileField.getText().trim();
        String pass = new String(passField.getPassword()).trim();

        if (path.equals("no file selected")) {
            setStatus("select a file first!", Color.RED);
            return;
        }
        if (!new File(path).exists()) {
            setStatus("file not found!", Color.RED);
            return;
        }
        if (!enc && !path.endsWith(".jcrypt")) {
            setStatus("select a .jcrypt file!", Color.RED);
            return;
        }
        if (pass.length() < 8) {
            setStatus("password min 8 chars!", Color.RED);
            return;
        }

        File f = new File(path);
        setStatus(enc ? "encrypting..." : "decrypting...", Color.BLUE);

        new Thread(() -> {
            boolean ok = send(enc ? Constants.OP_ENCRYPT : Constants.OP_DECRYPT, pass, f, enc);
            SwingUtilities.invokeLater(() -> {
                passField.setText("");
                setStatus(ok ? (enc ? "encrypted!" : "decrypted!") : "failed! is server running?",
                        ok ? new Color(0, 140, 0) : Color.RED);
            });
        }).start();
    }

    boolean send(byte op, String pass, File f, boolean enc) {
        try {
            Socket s = new Socket("localhost", Constants.PORT);
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());
            DataInputStream din = new DataInputStream(s.getInputStream());

            FileInputStream fis = new FileInputStream(f);
            byte[] data = fis.readAllBytes();
            fis.close();

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

            if (din.readByte() == Constants.STATUS_OK) {
                byte[] name = new byte[din.readInt()];
                din.readFully(name);
                byte[] out = new byte[(int) din.readLong()];
                din.readFully(out);
                FileOutputStream fos = new FileOutputStream(f.getParent() + File.separator + new String(name));
                fos.write(out);
                fos.close();
                s.close();

                if (enc && deletebox.isSelected())
                    f.delete();
                return true;
            }
            s.close();

        } catch (java.net.ConnectException e) {
            SwingUtilities.invokeLater(() -> setStatus("server not running!", Color.RED));
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> setStatus("error: " + e.getMessage(), Color.RED));
        }
        return false;
    }

    void setStatus(String msg, Color c) {
        status.setText(msg);
        status.setForeground(c);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JCryptGUI());
    }
}
