import weka.core.Instances;
import java.io.FileReader;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

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

    public static Instances[] splitData(Instances all, double proportion, long seed) {
        all.randomize(new Random(seed));
        all.stratify((int)(1/proportion));

        Instances[] split = new Instances[2];
        split[0] = new Instances(all);
        split[1] = new Instances(all, 0);
        for (int i = 0; i < all.size() * proportion; i++){
            split[1].add(split[0].remove(i));
        }

        return split;
    }

    public static void checkStratification(Instances[] split) {
        int[] strat1 = new int[split[0].numClasses()];
        int[] strat2 = new int[split[1].numClasses()];

        for (int i = 0; i < split[0].size(); i++) {
            strat1[(int)split[0].instance(i).classValue()]++;
        }

        for (int i = 0; i < split[1].size(); i++) {
            strat2[(int)split[1].instance(i).classValue()]++;
        }

        System.out.println(split[0].numClasses() + " classes");
        System.out.println(Arrays.toString(strat1));
        System.out.println(Arrays.toString(strat2));
    }
}
