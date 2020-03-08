import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;

public class Main {
    public static void main(String[] args){
        TabDatabase tabdb = new TabDatabase();
        tabdb.createDatabase("tab-uea\\lutestuff");

        Classifier nb = new NaiveBayes();
//        Classifier nn = new IBk(10);

        tabdb.learner.createNumberOfBarsARFF();
//        tabdb.learner.createChordStretchARFF("tf", "unary", false);
        tabdb.learner.createChordStretchARFF("binary", "unary", false);
        tabdb.learner.createDiscreteChordsARFF("binary", "unary");
        try {
            tabdb.learner.testLearner("numberOfBars", 10, 5, 42, nb);
            tabdb.learner.testLearner("chordStretch", 10, 5, 678, nb);
            tabdb.learner.testLearner("discreteChords", 10, 5, 4534, nb);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
