import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

/** TabDatabase - class
 *
 */
public class TabDatabase {
    private ArrayList<ArrayList<Tab>> tabs;
    public Learner learner;

    public TabDatabase() {
        this.tabs = new ArrayList<>();
        for (int grade = 0; grade < 8; grade++) {
            tabs.add(new ArrayList<>());
        }
        this.learner = new Learner("ARFFs\\");
    }

    public ArrayList<Tab> getTabs() {
        ArrayList<Tab> list = new ArrayList<>();
        for (ArrayList<Tab> grade : tabs) {
            list.addAll(grade);
        }
        return list;
    }

    public Tab getTab(int grade, int index) {
        if (tabs.get(grade).size() > index) return tabs.get(grade).get(index);
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


    /** createDatabase
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
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private class TabFileVisitor implements FileVisitor<Path> {
        private String basePath;
        private int grade;

        private TabFileVisitor(String path){
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
            //!line.startsWith("-") &&          //ignore command line parameters.
            try {
                String line;
                tabReader = new BufferedReader(new FileReader(file.toString()));
                while ((line = tabReader.readLine()) != null){
                    if (!line.startsWith("{") && !line.startsWith("%") && !line.isEmpty()) {    //ignore comments and title
                        lines.add(line);
                    }
                }
            } catch(IOException e){
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
        private String trainPath;
        private String testPath;

        public Learner(String path){
            this.trainPath = path + "train\\";
            this.testPath = path + "test\\";
        }

        private void writeARFF(String path, String content){
            try (Writer ARFFWriter = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(path, true), StandardCharsets.UTF_8))) {
                ARFFWriter.write(content);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }

        private void writeARFF(String path, String content, boolean replace){
            try (Writer ARFFWriter = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(path, !replace), StandardCharsets.UTF_8))) {
                ARFFWriter.write(content);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }

        private Instances readARFF(String path){
            try {
                FileReader reader = new FileReader(path);
                return new Instances(reader);
            } catch (IOException e){
                System.err.println(e.getMessage());
                return null;
            }
        }

        public void testLearner(String relation) throws Exception {
            String fileName = relation + ".arff";
            Instances train = readARFF(this.trainPath + fileName);
            Instances test = readARFF(this.testPath + fileName);
//            System.out.println(train.numInstances());
//            System.out.println(train.numAttributes());
//            System.out.println(Arrays.toString(train.instance(4).toDoubleArray()));
            if (train == null || test == null){
                return;
            }

            train.setClassIndex(train.numAttributes() - 1);
            test.setClassIndex(test.numAttributes() - 1);
            System.out.println(train.classIndex());

            Classifier naiveBayes = new NaiveBayes();
            naiveBayes.buildClassifier(train);


            for (Instance t : test){
                System.out.println("actual: " + t.toString(1) + "classification: " + (naiveBayes.classifyInstance(t) + 1));
            }

        }

        public void createNumberOfBarsARFF() {
            String fileName = "numberOfBars.arff";
            String header = "@relation numberOfBars\n\n" +
                            "@attribute bars NUMERIC\n" +
                            "@attribute grade {1,2,3,4,5,6,7,8}\n\n"+
                            "@data\n";

            writeARFF(trainPath + fileName, header, true);
            writeARFF(testPath + fileName, header, true);

            for (ArrayList<Tab> grade : tabs) {
                int trainAmount = (int) (grade.size() * 0.7);
                System.out.println(grade.size() + ", " + trainAmount);
                Collections.shuffle(grade);                                 //shuffle to get different train and test data
                for (int i = 0; i < grade.size(); i++){
                    Tab t = grade.get(i);
                    if (i < trainAmount) {
                        writeARFF(trainPath + fileName,t.getNumberOfBars() + ", " + t.getGrade() + "\n");
                    }
                    else {
                        writeARFF(testPath + fileName, t.getNumberOfBars() + ", " + t.getGrade() + "\n");
                    }
                }
            }
        }
    }
}
