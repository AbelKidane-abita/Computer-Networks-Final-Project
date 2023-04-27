package Client;

import java.io.IOException;
import java.io.RandomAccessFile;

public class FileReadExample {
    public static void main(String[] args) {
        String filePath = "C:\\Users\\abelk\\eclipse-workspace\\New_Project\\Myfile\\myfile.docx";
        byte[] first = new byte[1024];
        byte[] second = new byte[1024];
        byte[] third = new byte[1024];

        try {
        	RandomAccessFile file = new RandomAccessFile(filePath, "r");
            // Read the first 1024 bytes
            file.read(first, 0, 1024);

            // Seek to the position of the second 1024 bytes
            file.seek(1024);

            // Read the second 1024 bytes
            file.read(second, 0, 1024);

            // Seek to the position of the third 1024 bytes
            file.seek(2048);

            // Read the third 1024 bytes
            file.read(third, 0, 1024);

            // Output the results for verification
            System.out.println("First 1024 bytes: " + new String(first));
            System.out.println("Second 1024 bytes: " + new String(second));
            System.out.println("Third 1024 bytes: " + new String(third));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}