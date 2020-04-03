import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;

public class Main {
    public static void main(String[] args) throws Exception {
        TabDatabase tabdb = new TabDatabase();
        tabdb.createDatabase("tab-uea\\lutestuff");
        System.out.println("Total tabs: " + tabdb.size());

        Learner learner = new Learner(tabdb);
        Classifier nb = new NaiveBayesRP();

        // learner.createNumberOfBarsARFF();

        // learner.createChordStretchARFF("tf", "unary", false);                  //original configuration for chordStretch
        // learner.createChordStretchARFF("lognorm", "unary", true);               //best configuration for chordStretch

        // learner.createDiscreteChordsARFF("raw", "unary");                       //original configuration for discreteChords
        // learner.createDiscreteChordsARFF("binary", "unary");                    //best configuration for discreteChords             

        // learner.createPositionVarianceARFF();
        // learner.createAverageNoOfFingersARFF();
        // learner.createHighestFretARFF();

        try {
            // int seed = 54654;                                        // seed used for the results in the final report
            int seed = 54654;
            int runs = 5;
            int folds = 10;

            // all individual 
            // learner.testSingleLearner(new String[]{"numberOfBars"}, folds, runs, seed, nb);
            // learner.testSingleLearner(new String[]{"highestFret"}, folds, runs, seed, nb);
            // learner.testSingleLearner(new String[]{"chordStretch"}, folds, runs, seed, nb);
            // learner.testSingleLearner(new String[]{"positionVariance"}, folds, runs, seed, nb);
            // learner.testSingleLearner(new String[]{"averageNoOfFingers"}, folds, runs, seed, nb);

            // combining all possible pairs
            // learner.testSingleLearner(new String[]{"chordStretch", "positionVariance"}, folds, runs, seed, nb);
            // learner.testSingleLearner(new String[]{"chordStretch", "averageNoOfFingers"}, folds, runs, seed, nb);
            // learner.testSingleLearner(new String[]{"chordStretch", "numberOfBars"}, folds, runs, seed, nb);  
            // learner.testSingleLearner(new String[]{"chordStretch", "highestFret"}, folds, runs, seed, nb);                                        
            // learner.testSingleLearner(new String[]{"positionVariance", "averageNoOfFingers"}, folds, runs, seed, nb); 
            // learner.testSingleLearner(new String[]{"positionVariance", "numberOfBars"}, folds, runs, seed, nb);
            // learner.testSingleLearner(new String[]{"positionVariance", "highestFret"}, folds, runs, seed, nb);
            // learner.testSingleLearner(new String[]{"averageNoOfFingers", "numberOfBars"}, folds, runs, seed, nb);
            // learner.testSingleLearner(new String[]{"averageNoOfFingers", "highestFret"}, folds, runs, seed, nb);
            // learner.testSingleLearner(new String[]{"numberOfBars", "highestFret"}, folds, runs, seed, nb);

            // combining all possible triplets
            // learner.testSingleLearner(new String[]{"chordStretch", "positionVariance", "averageNoOfFingers"}, folds, runs, seed, nb);
            // learner.testSingleLearner(new String[]{"chordStretch", "positionVariance", "numberOfBars"}, folds, runs, seed, nb);
            // learner.testSingleLearner(new String[]{"chordStretch", "positionVariance", "highestFret"}, folds, runs, seed, nb);
            // learner.testSingleLearner(new String[]{"chordStretch", "averageNoOfFingers", "numberOfBars"}, folds, runs, seed, nb);
            // learner.testSingleLearner(new String[]{"chordStretch", "averageNoOfFingers", "highestFret"}, folds, runs, seed, nb);
            // learner.testSingleLearner(new String[]{"chordStretch", "numberOfBars", "highestFret"}, folds, runs, seed, nb);
            // learner.testSingleLearner(new String[]{"positionVariance", "averageNoOfFingers", "numberOfBars"}, folds, runs, seed, nb);
            // learner.testSingleLearner(new String[]{"positionVariance", "averageNoOfFingers", "highestFret"}, folds, runs, seed, nb);
            // learner.testSingleLearner(new String[]{"positionVariance", "numberOfBars", "highestFret"}, folds, runs, seed, nb);
            // learner.testSingleLearner(new String[]{"averageNoOfFingers", "numberOfBars", "highestFret"}, folds, runs, seed, nb);

            // combining all possible quadruplets
            // learner.testSingleLearner(new String[]{"numberOfBars", "chordStretch", "positionVariance", "averageNoOfFingers"}, folds, runs, seed, nb);
            // learner.testSingleLearner(new String[]{"chordStretch", "positionVariance", "averageNoOfFingers", "highestFret"}, folds, runs, seed, nb);
            // learner.testSingleLearner(new String[]{"positionVariance", "averageNoOfFingers", "highestFret", "numberOfBars"}, folds, runs, seed, nb);
            // learner.testSingleLearner(new String[]{"averageNoOfFingers", "highestFret", "numberOfBars", "chordStretch"}, folds, runs, seed, nb);
            // learner.testSingleLearner(new String[]{"highestFret", "numberOfBars", "chordStretch", "positionVariance"}, folds, runs, seed, nb);

            //combining all possible quintets
            // learner.testSingleLearner(new String[]{"numberOfBars", "chordStretch", "positionVariance", "averageNoOfFingers", "highestFret"}, folds, runs, seed, nb);

            // discrete chords
            // learner.testSingleLearner(new String[]{"discreteChords"}, folds, runs, seed, nb);

            //testing
            // learner.testSingleLearner(new String[]{"discreteChords", "highestFret", "chordStretch", "numberOfBars"}, folds, runs, seed, nb);
            // learner.testAllUnique(new String[]{"averageNoOfFingers", "chordStretch", "numberOfBars", "positionVariance", "highestFret"}, folds, runs, seed, nb);

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

            learner.testHeterogenousLearner(new String[][] {
                {"discreteChords"},
                {"numberOfBars", "chordStretch"},
                {"positionVariance", "averageNoOfFingers"},
                {"chordStretch", "positionVariance", "averageNoOfFingers", "highestFret"}
            }, folds, runs, seed, nb, "windowed");
             
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }
}
