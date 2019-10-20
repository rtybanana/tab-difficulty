import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

/** TabDatabase - class
 *
 */
public class TabDatabase {
    private ArrayList<Tab> tabs;

    public TabDatabase() {
        this.tabs = new ArrayList<>();
    }

    /** createDatabase -
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

    public Tab getTab(int index){
        if (tabs.size() > index) return tabs.get(index);
        else return null;
    }

    public int size(){
        return tabs.size();
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
                System.out.println(e.getMessage());
            }

            Tab tab = new Tab(name, lines, grade);
            tabs.add(tab);

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
