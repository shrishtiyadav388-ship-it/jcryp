 How to Run
Prerequisites

JDK 8 or higher installed
All machines on same LAN (for multi-client use)
Port 9999 not blocked by firewall


Step 1 — Clone the Repository
bashgit clone https://github.com/shrishtiyadav388-ship-it/JCrypt.git
cd JCrypt

Step 2 — Compile
bash# Windows
mkdir out
javac -d out src\jcrypt\*.java


Step 3 — Start the Server
Run this on the server machine (or same PC for testing):

java -cp out jcrypt.Server

Expected output:
================================
   J-Crypt File Security Tool
   TCS-408 Project
================================
[Server] Started on port 9999
[Server] Waiting for clients...

Step 4 — Start the Client
Open a second terminal and run:

bash# Connect to localhost (same machine)

java -cp out jcrypt.Client

# Connect to lab server (replace with server IP)
java -cp out jcrypt.Client 10.0.0.10
You will see:
================================
   J-Crypt File Security Tool
   TCS-408 Project
================================
------------------------------
  1. Encrypt file
  2. Decrypt file
  3. Exit
------------------------------
Enter choice:

Step 5 — Encrypt a File
Enter choice: 1
Enter file path: C:\Users\Shrishti\Desktop\report.pdf
Enter password: mysecretpassword
Save folder (press Enter for same folder):

Connecting to server...
Connected! Sending file...
File sent! Size: 245300 bytes
Done! File saved at: C:\Users\Shrishti\Desktop\report.pdf.jcrypt

Step 6 — Decrypt a File
Enter choice: 2
Enter file path: C:\Users\Shrishti\Desktop\report.pdf.jcrypt
Enter password: mysecretpassword
Re-enter password: mysecretpassword

Done! File saved at: C:\Users\Shrishti\Desktop\report.pdf
