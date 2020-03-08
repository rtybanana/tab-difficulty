import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instance;
import weka.core.Instances;
import java.lang.Math;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

/**
 * TabDatabase - class
 */
public class TabDatabase {
    private ArrayList<ArrayList<Tab>> tabs;
    public Learner learner;
    //private static final double TRAINING_PROPORTION = 0.8;

    public TabDatabase() {
        this.tabs = new ArrayList<>();
        for (int grade = 0; grade < 8; grade++) {
            tabs.add(new ArrayList<>());
        }
        this.learner = new Learner();
    }

    public ArrayList<Tab> getTabs() {
        ArrayList<Tab> list = new ArrayList<>();
        for (ArrayList<Tab> grade : tabs) {
            list.addAll(grade);
        }
        return list;
    }

    public Tab getTab(int grade, int index) {
        if (tabs.get(grade - 1).size() > index) return tabs.get(grade - 1).get(index);
        else return null;
    }

    public int size() {
        int size = 0;
        for (ArrayList<Tab> grade : tabs) {
            size += grade.size();
        }
        return size;
    }

    public int size(int grade) {
        return tabs.get(grade - 1).size();
    }


    /**
     * createDatabase
     * Given a root folder containing folders named 'grade1' through 'grade8' which contain pieces in tab format, this
     * function will walk through each of the folders, construct a Tab object from each of the pieces and add it to the
     * database so that it can be processed into training data for the Naive Bayes Classifier later.
     *
     * @param root - root directory for the function to walk
     */
    public void createDatabase(String root) {
        Path startingDir = Paths.get(root);
        TabFileVisitor visitor = new TabFileVisitor(root);
        try {
            Files.walkFileTree(startingDir, visitor);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private class TabFileVisitor implements FileVisitor<Path> {
        private String basePath;
        private int grade;

        private TabFileVisitor(String path) {
            this.basePath = path;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            String path = dir.toString().replace(basePath, "");
            if (!path.isEmpty()) {
                if (!path.matches("\\\\grade[1-8]")) return SKIP_SUBTREE;
                grade = Integer.parseInt(path.substring(path.length() - 1));
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            String name = file.toString().replace(basePath + "\\grade" + this.grade + "\\", "");
            name = name.replace(".tab", "");

            ArrayList<String> lines = new ArrayList<>();
            BufferedReader tabReader;
            try {
                String line;
                tabReader = new BufferedReader(new FileReader(file.toString()));
                while ((line = tabReader.readLine()) != null) {
                    if (!line.startsWith("{") && !line.startsWith("%") && !line.isEmpty()) {    //ignore comments and title
                        lines.add(line);
                    }
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }

            Tab tab = new Tab(name, lines, grade);
            tabs.get(grade - 1).add(tab);

            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            return CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            return CONTINUE;
        }
    }

    public class Learner {
        private String dataPath;

        public Learner() {
            this.dataPath = "ARFFs\\";
        }

        private void writeARFF(String path, String content) {
            try (Writer ARFFWriter = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(path, true), StandardCharsets.UTF_8))) {
                ARFFWriter.write(content);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }

        private void writeARFF(String path, String content, boolean replace) {
            try (Writer ARFFWriter = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(path, !replace), StandardCharsets.UTF_8))) {
                ARFFWriter.write(content);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }

        private Instances readARFF(String path) {
            try {
                FileReader reader = new FileReader(path);
                return new Instances(reader);
            } catch (IOException e) {
                System.err.println(e.getMessage());
                return null;
            }
        }

        public void testLearner(String relation, int folds, int runs, int seed) throws Exception {
            String fileName = relation + ".arff";
            Instances data = readARFF(this.dataPath + fileName);
            if (data == null) {
                return;
            }

            Classifier naiveBayes = new NaiveBayes();
            data.setClassIndex(data.numAttributes() - 1);
            int[][] cMatrix = new int[8][8];
            int totaltested = 0;
            for (int r = 0; r < runs; r++) {
                Random rand = new Random(seed);
                data.randomize(rand);
                data.stratify(folds);

                for (int n = 0; n < folds; n++) {
                    Instances train = data.trainCV(folds, n, rand);
                    Instances test = data.testCV(folds, n);
                    totaltested += test.numInstances();

                    naiveBayes.buildClassifier(train);
                    for (Instance t : test) {
                        cMatrix[Integer.parseInt(t.toString(train.attribute("grade"))) - 1][(int) (naiveBayes.classifyInstance(t))]++;
                    }
                }
                seed++;
            }

            System.out.println(fileName);
            for (int i = 0; i < 8; i++){
                for (int j = 0; j < 8; j++){
                    System.out.print(cMatrix[i][j]);
                    if (j < 7) System.out.print(" & ");
                }
                System.out.println();
            }

            final int TREND_CONSTANT = 1;
            int correct = 0;
            int ballpark = 0;
            int trend = 0;
            for (int i = 0; i < 8; i++) {
                correct += cMatrix[i][i];
                ballpark += cMatrix[i][i];
                if (i > 0) ballpark += cMatrix[i-1][i];
                if (i < 7) ballpark += cMatrix[i+1][i];
                for (int j = 0; j < 8; j++) trend += cMatrix[i][j] * (-Math.abs(i - j) + TREND_CONSTANT);
            }
            double accuracy = (double)correct/totaltested * 100;
            double ballpark_accuracy = (double)ballpark/totaltested * 100;
            double trend_score = ((double)trend/totaltested) / TREND_CONSTANT;
            System.out.println("Accuracy: " + String.format("%.2f", accuracy) +"%");
            System.out.println("Ballpark Accuracy: " + String.format("%.2f", ballpark_accuracy) +"%");
            System.out.println("Trend Score: " + String.format("%.2f", trend_score));
            System.out.println();
        }

        /**
         * Creates the ARFF file for the 'Number of Bars' feature extraction.
         */
        public void createNumberOfBarsARFF() {
            String fileName = "numberOfBars.arff";
            String header = "@relation numberOfBars\n\n" +
                            "@attribute bars NUMERIC\n" +
                            "@attribute grade {1,2,3,4,5,6,7,8}\n\n" +
                            "@data\n";

            writeARFF(dataPath + fileName, header, true);

            for (ArrayList<Tab> grade : tabs) {
                for (int i = 0; i < grade.size(); i++) {
                    Tab t = grade.get(i);
                    writeARFF(dataPath + fileName, t.getNumberOfBars() + ", " + t.getGrade() + "\n");
                }
            }
        }

        /**
         * Create the ARFF file for the 'Discrete Chords' feature extraction method with a couple of options to
         * configure to adjust the output.
         *
         * @param idfWeight    - inverse document frequency weight variant
         * @param tfWeight  - term frequency weight variant
         */
        public void createDiscreteChordsARFF(String tfWeight, String idfWeight) {
            HashMap<String, Double> documentFreq = new HashMap<>();
            for (Tab tab : getTabs()) {
                for (String k : tab.getDiscreteChords(tfWeight).keySet()){
                    documentFreq.merge(k, (double) 1, Double::sum);             //incrementing count if exists in document
                }
            }
            if (idfWeight.equals("idf")) documentFreq.replaceAll((k, v) -> Math.log(size() / v));                   //inverse document frequency
            else if (idfWeight.equals("idfs")) documentFreq.replaceAll((k, v) -> Math.log(size() / (1 + v)) + 1);   //inverse document frequency smooth
            else documentFreq.replaceAll((k, v) -> (double) 1);                                                     //unary

            System.out.println(documentFreq);

            String fileName = "discreteChords.arff";
            StringBuilder header = new StringBuilder("@relation discreteChords\n\n");
            ArrayList<String> attributes = new ArrayList<>();
            for (String attr : documentFreq.keySet()) {
                attributes.add(attr);
                header.append("@attribute ").append(attr).append(" NUMERIC\n");
            }
            header.append("@attribute grade {1,2,3,4,5,6,7,8}\n\n@data\n");
            writeARFF(dataPath + fileName, header.toString(), true);

            for (ArrayList<Tab> grade : tabs) {
                for (int i = 0; i < grade.size(); i++) {
                    Tab t = grade.get(i);
                    HashMap<String, Double> tabChordMap = t.getDiscreteChords(tfWeight);

                    StringBuilder instance = new StringBuilder();
                    for (String attr : attributes) {
                        if (tabChordMap.containsKey(attr)) instance.append(tabChordMap.get(attr) * documentFreq.get(attr)).append(", ");
                        else instance.append("0, ");
                    }
                    instance.append(t.getGrade()).append('\n');

                    writeARFF(dataPath + fileName, instance.toString());
                }
            }
        }

        /**
         * Creates the ARFF file for the 'Chord Stretch' feature extraction.
         */
        public void createChordStretchARFF(String tfWeight, String idfWeight, boolean singles) {
            HashMap<Integer, Double> documentFreq = new HashMap<>();
            for (Tab t : getTabs()){
                for (Integer k : t.getStretch(tfWeight, singles).keySet()){
                    documentFreq.merge(k, (double) 1, Double::sum);
                }
            }
            if (idfWeight.equals("idf")) documentFreq.replaceAll((k, v) -> Math.log(size() / v));                   //inverse document frequency
            else if (idfWeight.equals("idfs")) documentFreq.replaceAll((k, v) -> Math.log(size() / (1 + v)) + 1);   //inverse document frequency smooth
            else documentFreq.replaceAll((k, v) -> (double) 1);
            System.out.println(documentFreq);

            String fileName = "chordStretch.arff";
            StringBuilder header = new StringBuilder("@relation chordStretch\n\n");
            ArrayList<Integer> attributes = new ArrayList<>();
            for (Integer attr : documentFreq.keySet()) {
                attributes.add(attr);
                header.append("@attribute ").append(attr).append(" NUMERIC\n");
            }
            header.append("@attribute grade {1,2,3,4,5,6,7,8}\n\n@data\n");
            writeARFF(dataPath + fileName, header.toString(), true);

            for (ArrayList<Tab> grade : tabs) {
                for (int i = 0; i < grade.size(); i++) {
                    Tab t = grade.get(i);
                    HashMap<Integer, Double> stretchMap = t.getStretch(tfWeight, singles);

                    StringBuilder instance = new StringBuilder();
                    for (Integer attr : attributes) {
                        if (stretchMap.containsKey(attr)) instance.append(stretchMap.get(attr) * documentFreq.get(attr)).append(", ");
                        else instance.append("0, ");
                    }
                    instance.append(t.getGrade()).append('\n');

                    writeARFF(dataPath + fileName, instance.toString());
                }
            }
        }

        public void createPositionVarianceARFF() {
        }
    }
}
