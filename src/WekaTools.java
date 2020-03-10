import weka.core.Instances;
import java.io.FileReader;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class WekaTools {
    public static void writeARFF(String path, String content) {
        try (Writer ARFFWriter = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(path, true), StandardCharsets.UTF_8))) {
            ARFFWriter.write(content);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void writeARFF(String path, String content, boolean replace) {
        try (Writer ARFFWriter = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(path, !replace), StandardCharsets.UTF_8))) {
            ARFFWriter.write(content);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public static Instances readARFF(String path) {
        try {
            FileReader reader = new FileReader(path);
            return new Instances(reader);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
}
