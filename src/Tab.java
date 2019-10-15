import java.util.ArrayList;
import java.io.BufferedReader;

class Tab {
    private String name;
    private ArrayList<String> lines;
    private int grade;

    public Tab(String name, ArrayList<String> lines, int grade){
        this.name = name;
        this.lines = lines;
        this.grade = grade;
    }
}
