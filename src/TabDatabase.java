import java.io.*;
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
        // this.learner = new Learner();
    }

    public ArrayList<ArrayList<Tab>> getTabs() {
        return this.tabs;
    }

    public ArrayList<Tab> getTabsFlat() {
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
}
