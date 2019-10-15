public class Main {
    public static void main(String[] args){
        TabDatabase tabdb = new TabDatabase();
        tabdb.createDatabase("C:\\Users\\roryp\\Desktop\\tab-uea\\lutestuff");

        Tab tab = tabdb.getTab(12);
        System.out.println("name: " + tab.getName());
        System.out.println("grade: " + tab.getGrade());
        for (String line : tab.getLines()){
            System.out.println(line);
        }
    }
}
