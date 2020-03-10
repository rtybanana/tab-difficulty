import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;

public class Main {
    public static void main(String[] args) throws Exception {
        TabDatabase tabdb = new TabDatabase();
        tabdb.createDatabase("tab-uea\\lutestuff");
        Learner learner = new Learner(tabdb);

        Classifier nb = new NaiveBayes();

        // learner.createNumberOfBarsARFF();
        // learner.createChordStretchARFF("tf", "unary", false);
        // learner.createChordStretchARFF("binary", "unary", false);
        // learner.createDiscreteChordsARFF("binary", "unary");
        // learner.createPositionVarianceARFF();
        // learner.createAverageNoOfFingersARFF();
        try {
            int seed = 6543;
            int runs = 5;
            int folds = 10;

            // all individual 
            // learner.testSingleLearner(new String[]{"numberOfBars"}, 10, 5, seed, nb);
            // learner.testSingleLearner(new String[]{"chordStretch"}, 10, 5, seed, nb);                                                               // best performer
            // learner.testSingleLearner(new String[]{"positionVariance"}, 10, 5, seed, nb);
            // learner.testSingleLearner(new String[]{"averageNoOfFingers"}, 10, 5, seed, nb);                                                         // worst performer by a country mile

            // combining all possible pairs
            // learner.testSingleLearner(new String[]{"chordStretch", "positionVariance"}, folds, runs, seed, nb);
            // learner.testSingleLearner(new String[]{"chordStretch", "averageNoOfFingers"}, folds, runs, seed, nb);
            // learner.testSingleLearner(new String[]{"chordStretch", "numberOfBars"}, folds, runs, seed, nb);                                               // best performer
            // learner.testSingleLearner(new String[]{"positionVariance", "averageNoOfFingers"}, folds, runs, seed, nb);                                     // worst performer
            // learner.testSingleLearner(new String[]{"positionVariance", "numberOfBars"}, folds, runs, seed, nb);
            // learner.testSingleLearner(new String[]{"averageNoOfFingers", "numberOfBars"}, folds, runs, seed, nb);

            // combining all possible triplets
            // learner.testSingleLearner(new String[]{"numberOfBars", "chordStretch", "positionVariance"}, folds, runs, seed, nb);                           // not much performance increase over chordStretch + numberOfBars
            // learner.testSingleLearner(new String[]{"chordStretch", "positionVariance", "averageNoOfFingers"}, folds, runs, seed, nb);                     // worst performer
            // learner.testSingleLearner(new String[]{"positionVariance", "averageNoOfFingers", "numberOfBars"}, folds, runs, seed, nb);                     // best performer
            // learner.testSingleLearner(new String[]{"averageNoOfFingers", "numberOfBars", "chordStretch"}, folds, runs, seed, nb);                         // not much performance increase over chordStretch + numberOfBars

            // combining all possible quadruplets
            // learner.testSingleLearner(new String[]{"numberOfBars", "chordStretch", "positionVariance", "averageNoOfFingers"}, folds, runs, seed, nb);

            // discrete chords
            learner.testSingleLearner(new String[]{"discreteChords"}, folds, runs, seed, nb);

            // learner.testHeterogenousLearner(new String[][] {
            //     {"discreteChords"},
            //     {"numberOfBars"},
            //     {"chordStretch"},
            //     {"positionVariance"},
            //     {"averageNoOfFingers"},
            //     {"chordStretch", "positionVariance"},
            //     {"chordStretch", "averageNoOfFingers"},
            //     {"positionVariance", "averageNoOfFingers"},
            //     {"chordStretch", "numberOfBars"},
            //     {"positionVariance", "averageNoOfFingers", "numberOfBars"},
            //     {"numberOfBars", "chordStretch", "positionVariance"},
            //     {"chordStretch", "positionVariance", "averageNoOfFingers"},
            //     {"averageNoOfFingers", "numberOfBars", "chordStretch"},
            //     {"numberOfBars", "chordStretch", "positionVariance", "averageNoOfFingers"},
            // }, folds, runs, seed, nb, "weighted");

            // learner.testHeterogenousLearner(new String[][] {
            //     {"discreteChords"},
            //     {"chordStretch", "numberOfBars"},
            //     {"positionVariance", "averageNoOfFingers", "numberOfBars"},
            // }, folds, runs, seed, nb, "probablistic");
             
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }
}
