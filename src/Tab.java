import java.util.ArrayList;

class Tab {
    private String name;
    private ArrayList<String> lines;
    private int grade;

    public Tab(String name, ArrayList<String> lines, int grade){
        this.name = name;
        this.lines = lines;
        this.grade = grade;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getLines() {
        return lines;
    }

    public int getGrade() {
        return grade;
    }
}
