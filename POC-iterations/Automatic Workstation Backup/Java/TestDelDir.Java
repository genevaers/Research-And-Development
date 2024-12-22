//import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.io.BufferedReader;
import java.io.InputStreamReader;

//FileUtils.deleteDirectory(new File(destination));

class TestDelDir {

	public static void main(String[] args) {

        String deldir = "C:\\testdel";

        System.out.println("testDelDir Started for:" + deldir);

        File directory = new File(deldir);

        if (directory.exists()) {
            System.out.println("directory exists:" + directory);
            deleteDirectory(directory);
        }

    }
   
    private static void deleteDirectory(File directory) {
            if (directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        System.out.println("deleting file:" + file);
                        deleteDirectory(file);
                    }
                }
                System.out.println("deleting directory:" + directory);
            }
            directory.delete();
    }
}
