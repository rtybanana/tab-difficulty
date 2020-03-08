public class Main {
    public static void main(String[] args){
        TabDatabase tabdb = new TabDatabase();
        tabdb.createDatabase("tab-uea\\lutestuff");

        //for (Tab t : tabdb.getTabs()) {
            //System.out.println(t.getNumberOfBars() + "\t" + t.getGrade());
            //System.out.println(t.getCourses());
        //}

        tabdb.learner.createNumberOfBarsARFF();
        tabdb.learner.createChordStretchARFF("raw", "unary", true);
        tabdb.learner.createDiscreteChordsARFF("binary", "unary");
        try {
            tabdb.learner.testLearner("numberOfBars", 10, 5, 4532);
            tabdb.learner.testLearner("chordStretch", 10, 5, 678);
            tabdb.learner.testLearner("discreteChords", 10, 5, 4534);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

//        ArrayList<Tab> tabs = tabdb.getTabs();
//        Tab t = tabdb.getTab(1, 2);
//        HashMap<String, Double> tabMap = t.getDiscreteChords("raw");
//        System.out.println(tabMap);
//
//        HashMap<String, Double> documentFreq = new HashMap<>();
//        for (Tab tab : tabdb.getTabs()) {
//            for (String k : tab.getDiscreteChords("tf").keySet()){
//                documentFreq.merge(k, (double) 1, Double::sum);             //incrementing count if exists in document
//            }
//        }
//        //documentFreq.replaceAll((K, v) -> Math.log(tabdb.size() / v));
//        documentFreq.replaceAll((k, v) -> (double) 1);
//        System.out.println(documentFreq);
//
//        tabMap.replaceAll((k, v) -> v * documentFreq.get(k));
//        System.out.println(tabMap);
    }
}
