J-Crypt - File Security System
TCS-408 Java Project | Team CodeCrafters
Team
Aman Verma | Aniket Singh | Shrishti Yadav | Atulya Gupta
How to Run on Windows
Requirements

JDK 8 or higher installed

Step 1 - Clone
git clone https://github.com/shrishtiyadav388-ship-it/jcryp.git
cd jcryp
Step 2 - Compile
javac src\jcrypt\*.java
Step 3 - Start Server (Terminal 1)
java -cp src jcrypt.Server
Step 4 - Start Client (Terminal 2)
java -cp src jcrypt.Client
Step 5 - Use
1 - encrypt any file
2 - decrypt .jcrypt file
3 - exit
Note

Start server before client
Password must be minimum 8 characters
Encrypted files are saved as filename.jcrypt
Wrong password = cannot decrypt
