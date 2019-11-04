import java.util.Arrays;

public class Main {
    public static void main(String[] args){
        TabDatabase tabdb = new TabDatabase();
        tabdb.createDatabase("tab-uea\\lutestuff");

        //for (Tab t : tabdb.getTabs()) {
            //System.out.println(t.getNumberOfBars() + "\t" + t.getGrade());
            //System.out.println(t.getCourses());
        //}

        tabdb.learner.createNumberOfBarsARFF();
        tabdb.learner.createChordStretchARFF();
        try {
            tabdb.learner.testLearner("numberOfBars");
            tabdb.learner.testLearner("chordStretch");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }



        //System.out.println(tabdb.size());

//        Tab tab8 = tabdb.getTab(8, 6);
//        System.out.println("name: " + tab8.getName());
//        System.out.println("grade: " + tab8.getGrade());
//        System.out.println(Arrays.toString(tab8.getStretch()));
//
//        Tab tab1 = tabdb.getTab(7, 2);
//        System.out.println("name: " + tab1.getName());
//        System.out.println("grade: " + tab1.getGrade());
//        System.out.println(Arrays.toString(tab1.getStretch()));
        //
//
//        ArrayList<String> lines = tab.getLines();
//        String Btest = lines.get(lines.size() - 2);
//        System.out.println(Btest.equals("B"));
//
//        System.out.println(tab.numberOfBars());
    }
}
