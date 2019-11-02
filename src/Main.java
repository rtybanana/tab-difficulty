public class Main {
    public static void main(String[] args){
        TabDatabase tabdb = new TabDatabase();
        tabdb.createDatabase("tab-uea\\lutestuff");

//        for (Tab t : tabdb.getTabs()) {
//            System.out.println(t.getNumberOfBars() + "\t" + t.getGrade());
//        }

        tabdb.learner.createNumberOfBarsARFF();
        try {
            tabdb.learner.testLearner("numberOfBars");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        //System.out.println(tabdb.size());

//        Tab tab = tabdb.getTab(3);
//        System.out.println("name: " + tab.getName());
//        System.out.println("grade: " + tab.getGrade());
//        for (String line : tab.getLines()){
//            System.out.println(line);
//        }
//
//        ArrayList<String> lines = tab.getLines();
//        String Btest = lines.get(lines.size() - 2);
//        System.out.println(Btest.equals("B"));
//
//        System.out.println(tab.numberOfBars());
    }
}
