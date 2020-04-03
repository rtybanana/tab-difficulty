import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.*;
import weka.core.Instance;
import weka.core.Instances;
import java.lang.Math;
import java.util.*;

public class Learner {
    private String dataPath;
    private TabDatabase tabdb;
    private static String[] VOTE_SCHEMES = new String[]{"majority", "weighted", "probablistic", "windowed"};
    private static long splitSeed = 2194879874365L;

    public Learner(TabDatabase tabdb) {
        this.dataPath = "ARFFs\\";
        this.tabdb = tabdb;
    }

    public Instances unifyAttributes(String[] relations)  {
        ArrayList<String> usedRelations = new ArrayList<>();
        Instances data = null;
        for (int i = 0; i < relations.length; i++) {
            if (usedRelations.size() == 0) {
                data = WekaTools.readARFF(this.dataPath + relations[i] + ".arff");
                if (data != null) usedRelations.add(relations[i]);
                else {
                    System.err.println("Error loading file \"" + relations[i] + ".arff\"");
                }
            }
            else {
                Instances data2 = WekaTools.readARFF(this.dataPath + relations[i] + ".arff");
                if (data2 != null) {
                    data2.deleteAttributeAt(data2.attribute("grade").index());
                    data = Instances.mergeInstances(data, data2);
                    usedRelations.add(relations[i]);
                }
                else {
                    System.err.println("Error loading file \"" + relations[i] + ".arff\"");
                }
            }
        }
        if (data == null) return null;

        data.setRelationName(usedRelations.toString());
        return data;
    }

    /**
     * Combines the provided ARFF files into one set of instances and trains a single model
     * 
     * @param relations     - array of arff file names to look for
     * @param folds         - number of folds in the cross validation
     * @param runs          - number of runs in the cross validation
     * @param seed          - the random seed of the cross validation
     * @param classifier    - the classifier type to train - most likely naive bayes
     * @throws Exception
     */
    public void testSingleLearner(String[] relations, int folds, int runs, int seed, Classifier classifier) throws Exception {
        Instances data = unifyAttributes(relations);
        if (data == null) {
            System.err.println("No data to test.");
            return;
        }

        // test and build confusion matrix
        data.setClassIndex(data.attribute("grade").index());
        // Instances[] split = WekaTools.splitData(data, 0.2, splitSeed);
        // WekaTools.checkStratification(split);
        int[][] cMatrix = new int[8][8];
        for (int r = 0; r < runs; r++) {
            Random rand = new Random(seed);
            data.randomize(rand);
            data.stratify(folds);

            for (int n = 0; n < folds; n++) {
                Instances train = data.trainCV(folds, n, rand);
                Instances test = data.testCV(folds, n);
                classifier.buildClassifier(train);

                // confusion matrix
                for (Instance t : test) {
                    cMatrix[Integer.parseInt(t.toString(train.attribute("grade"))) - 1][(int) (classifier.classifyInstance(t))]++;
                }
            }
            seed++;
        }

        System.out.println("single: " + data.relationName());
        evaluateLearner(cMatrix);
    }

    /**
     * Combines the provided ARFF files into one set of instances and trains a number of models in a homogenous ensemble
     * 
     * @param relations     - array of arff file names to look for
     * @param folds         - number of folds in the cross validation
     * @param runs          - number of runs in the cross validation
     * @param seed          - the random seed of the cross validation
     * @param classifier    - the classifier type to train - most likely naive bayes
     * @throws Exception
     */
    public void testHomogenousLearner(String[] relations, int folds, int runs, int seed, Classifier classifier) throws Exception {
        Instances data = unifyAttributes(relations);
        if (data == null) {
            System.out.println("File(s) not found.");
            return;
        }

        // test and build confusion matrix
        data.setClassIndex(data.attribute("grade").index());
        int[][] cMatrix = new int[8][8];
        for (int r = 0; r < runs; r++) {
            Random rand = new Random(seed);
            data.randomize(rand);
            data.stratify(folds);

            for (int n = 0; n < folds; n++) {
                Instances train = data.trainCV(folds, n, rand);
                Instances test = data.testCV(folds, n);
                classifier.buildClassifier(train);

                // confusion matrix
                for (Instance t : test) {
                    cMatrix[Integer.parseInt(t.toString(train.attribute("grade"))) - 1][(int) (classifier.classifyInstance(t))]++;
                }
            }
            seed++;
        }

        System.out.println("homogenous ensemble: " + data.relationName());
        evaluateLearner(cMatrix);
    }

    /**
     * Keeps the individual ARFF files in separate instances objects and trains a number of models in a heterogenous ensemble
     * 
     * @param relations     - array of arff file names to look for
     * @param folds         - number of folds in the cross validation
     * @param runs          - number of runs in the cross validation
     * @param seed          - the random seed of the cross validation
     * @param classifier    - the classifier type to train - most likely naive bayes
     * @throws Exception
     */
    public void testHeterogenousLearner(String[][] relations, int folds, int runs, int seed, Classifier classifier, String voteScheme) throws Exception {
        ArrayList<String> datasetName = new ArrayList<>();
        ArrayList<Instances> dataset = new ArrayList<>();
        // ArrayList<Instances> testset = new ArrayList<>();
        for (int i = 0; i < relations.length; i++) {
            // ArrayList<String> relationName = new ArrayList<>();
            Instances relation = unifyAttributes(relations[i]);
            relation.setClassIndex(relation.attribute("grade").index());

            // Instances[] split = WekaTools.splitData(relation, 0.2, splitSeed);
            // dataset.add(split[0]);
            // testset.add(split[1]);

            dataset.add(relation);
            datasetName.add(relation.relationName());
        }
        if (dataset.size() < 1) {
            System.err.println("No data to test.");
            return;
        }

        if(!Arrays.asList(VOTE_SCHEMES).contains(voteScheme)) {
            System.err.println("Vote weighting scheme not recognised, defaulting to majority");
            voteScheme = "majority";
        }

        System.out.println("heterogenous ensemble: " + datasetName.toString());



        // test and build confusion matrix
        ArrayList<Classifier> classifiers = new ArrayList<>(Arrays.asList(AbstractClassifier.makeCopies(classifier, dataset.size())));
        int[][] cMatrix = new int[8][8];
        // for (int r = 0; r < runs; r++) {
        //     for (Instances relation : dataset) {
        //         relation.randomize(new Random(seed + r));
        //         relation.stratify(folds);
        //     }

        //     for (int n = 0; n < 10; n++) {
        //         for (Instances relation : dataset) {
        //             Instances train = relation.trainCV(folds, n, new Random(seed + r));
        //         }

        //         for (int in = 0; in < folds; in++) {
        //             ArrayList<Instances> trainingsets = new ArrayList<>();
        //             ArrayList<Instances> testsets = new ArrayList<>();
        //             ArrayList<Double> weights = new ArrayList<>();
    
        //             for (Instances relation : dataset) {
        //                 Instances train = relation.trainCV(folds, in, new Random(seed + r));
        //                 trainingsets.add(train);
        //                 testsets.add(relation.testCV(folds, in));
        //             }
    
        //             for (int c = 0; c < classifiers.size(); c++) {
        //                 classifiers.get(c).buildClassifier(trainingsets.get(c));
        //                 if (voteScheme.equals("weighted") || voteScheme.equals("probablistic") || voteScheme.equals("windowed")) {
        //                     weights.add(weightClassifier(classifiers.get(c), trainingsets.get(c)));
        //                 }
        //             }
    
        //             // System.out.println(weights);
    
        //             // confusion matrix
        //             for (int t = 0; t < testsets.get(0).size(); t++) {
        //                 double meanClass = (double) 0;
        //                 double totalWeight = (double) 0;
        //                 double[] averageDist = new double[testsets.get(0).numClasses()];
        //                 for (int c = 0; c < classifiers.size(); c++) {
        //                     Instance instance = testsets.get(c).get(t);
        //                     int cls = (int)classifiers.get(c).classifyInstance(instance);
    
        //                     // weighted probabalistic average vote and weighted probablistic vote
        //                     if (voteScheme.equals("probablistic") || voteScheme.equals("windowed") ) {
        //                         double weight = weights.get(c);
        //                         double[] distribution = classifiers.get(c).distributionForInstance(instance);
        //                         Arrays.setAll(averageDist, i -> averageDist[i] + distribution[i] * weight);
        //                         totalWeight += weight;
        //                     }
                            
        //                     // weighted vote and mean
        //                     else if (voteScheme.equals("weighted")) {
        //                         meanClass += (cls + 1) * weights.get(c);
        //                         totalWeight += weights.get(c);
        //                     }
    
        //                     // majority vote and mean
        //                     else {
        //                         meanClass += cls;
        //                     }
        //                 }
                        
        //                 int estimate;
                        
        //                 // windowed probabalistic mean
        //                 if (voteScheme.equals("windowed")) {
        //                     int naive = 0;
        //                     for (int d = 1; d < averageDist.length; d++) {
        //                         if (averageDist[d] > averageDist[naive]) naive = d;
        //                     }
        //                     naive++;                                        //shift the naive estimate to be used in averages
    
        //                     int window = Math.abs(averageDist.length - naive) < Math.abs(naive - 1) ? (int)Math.abs(averageDist.length - naive) : (int)Math.abs(naive - 1);
        //                     int start = (int)naive - window - 1;
        //                     double[] windowedDist = Arrays.copyOfRange(averageDist, start, (int)naive + window);
    
        //                     double divisor = (double) 0;
        //                     double windowedMean = (double) 0;
        //                     for (int i = 0; i < windowedDist.length; i++) {
        //                         windowedMean += windowedDist[i] * (i + start + 1);
        //                         divisor += windowedDist[i];
        //                     }
        //                     windowedMean = (int)Math.round(windowedMean / divisor);
    
        //                     estimate = (int)windowedMean - 1;
    
    
        //                     // probablistic mean (no window) (commonly misclassifies grades 1 and 8 due to mean pulling class towards the middle)
        //                     // double mean = (double) 0;
        //                     // for (int i = 0; i < averageDist.length; i++) {
        //                     //     mean += averageDist[i] * (i + 1);
        //                     // }
    
        //                     // System.out.println((int)Math.round(mean / totalWeight) + " | " + testsets.get(0).get(t).toString(testsets.get(0).attribute("grade")));
    
        //                     // estimate = (int)Math.round(mean / totalWeight) - 1;
        //                 }
    
        //                 // weighted probabalistic vote
        //                 else if (voteScheme.equals("probablistic")) {
        //                     estimate = 0;
        //                     for (int d = 1; d < averageDist.length; d++) {
        //                         if (averageDist[d] > averageDist[estimate]) estimate = d;
        //                     }
        //                 }
    
        //                 // weighted vote and mean
        //                 else if (voteScheme.equals("weighted")) {
        //                     estimate = (int)Math.round(meanClass / totalWeight) - 1;
        //                 }
    
        //                 // majority vote and mean
        //                 else {
        //                     estimate = (int)Math.round(meanClass / classifiers.size());
        //                 }
    
        //                 cMatrix[Integer.parseInt(testsets.get(0).get(t).toString(testsets.get(0).attribute("grade"))) - 1][estimate]++;
        //             }
        //         }
        //     }
        // }
        for (int r = 0; r < runs; r++) {
            for (Instances relation : dataset) {
                relation.randomize(new Random(seed + r));
                relation.stratify(folds);
            }

            for (int in = 0; in < folds; in++) {
                ArrayList<Instances> trainingsets = new ArrayList<>();
                ArrayList<Instances> testsets = new ArrayList<>();
                ArrayList<Double> weights = new ArrayList<>();

                for (Instances relation : dataset) {
                    Instances train = relation.trainCV(folds, in, new Random(seed + r));
                    trainingsets.add(train);
                    testsets.add(relation.testCV(folds, in));
                }

                for (int c = 0; c < classifiers.size(); c++) {
                    classifiers.get(c).buildClassifier(trainingsets.get(c));
                    if (voteScheme.equals("weighted") || voteScheme.equals("probablistic") || voteScheme.equals("windowed")) {
                        weights.add(weightClassifier(classifiers.get(c), trainingsets.get(c)));
                    }
                }

                // System.out.println(weights);

                // confusion matrix
                for (int t = 0; t < testsets.get(0).size(); t++) {
                    double meanClass = (double) 0;
                    double totalWeight = (double) 0;
                    double[] averageDist = new double[testsets.get(0).numClasses()];
                    for (int c = 0; c < classifiers.size(); c++) {
                        Instance instance = testsets.get(c).get(t);
                        int cls = (int)classifiers.get(c).classifyInstance(instance);

                        // weighted probabalistic average vote and weighted probablistic vote
                        if (voteScheme.equals("probablistic") || voteScheme.equals("windowed") ) {
                            double weight = weights.get(c);
                            double[] distribution = classifiers.get(c).distributionForInstance(instance);
                            Arrays.setAll(averageDist, i -> averageDist[i] + distribution[i] * weight);
                            totalWeight += weight;
                        }
                        
                        // weighted vote and mean
                        else if (voteScheme.equals("weighted")) {
                            meanClass += (cls + 1) * weights.get(c);
                            totalWeight += weights.get(c);
                        }

                        // majority vote and mean
                        else {
                            meanClass += cls;
                        }
                    }
                    
                    int estimate;
                    
                    // windowed probabalistic mean
                    if (voteScheme.equals("windowed")) {
                        int naive = 0;
                        for (int d = 1; d < averageDist.length; d++) {
                            if (averageDist[d] > averageDist[naive]) naive = d;
                        }
                        naive++;                                        //shift the naive estimate to be used in averages

                        int window = Math.abs(averageDist.length - naive) < Math.abs(naive - 1) ? (int)Math.abs(averageDist.length - naive) : (int)Math.abs(naive - 1);
                        int start = (int)naive - window - 1;
                        double[] windowedDist = Arrays.copyOfRange(averageDist, start, (int)naive + window);

                        double divisor = (double) 0;
                        double windowedMean = (double) 0;
                        for (int i = 0; i < windowedDist.length; i++) {
                            windowedMean += windowedDist[i] * (i + start + 1);
                            divisor += windowedDist[i];
                        }
                        windowedMean = (int)Math.round(windowedMean / divisor);

                        estimate = (int)windowedMean - 1;


                        // probablistic mean (no window) (commonly misclassifies grades 1 and 8 due to mean pulling class towards the middle)
                        // double mean = (double) 0;
                        // for (int i = 0; i < averageDist.length; i++) {
                        //     mean += averageDist[i] * (i + 1);
                        // }

                        // System.out.println((int)Math.round(mean / totalWeight) + " | " + testsets.get(0).get(t).toString(testsets.get(0).attribute("grade")));

                        // estimate = (int)Math.round(mean / totalWeight) - 1;
                    }

                    // weighted probabalistic vote
                    else if (voteScheme.equals("probablistic")) {
                        estimate = 0;
                        for (int d = 1; d < averageDist.length; d++) {
                            if (averageDist[d] > averageDist[estimate]) estimate = d;
                        }
                    }

                    // weighted vote and mean
                    else if (voteScheme.equals("weighted")) {
                        estimate = (int)Math.round(meanClass / totalWeight) - 1;
                    }

                    // majority vote and mean
                    else {
                        estimate = (int)Math.round(meanClass / classifiers.size());
                    }

                    cMatrix[Integer.parseInt(testsets.get(0).get(t).toString(testsets.get(0).attribute("grade"))) - 1][estimate]++;
                }
            }
        }

        evaluateLearner(cMatrix);
    }

    private double weightClassifier(Classifier classifier, Instances train) throws Exception {
        Evaluation eval = new Evaluation(train);
        eval.crossValidateModel(classifier, train, 10, new Random());
        double weight = (-MMAE(eval.confusionMatrix()) + train.numClasses()) / train.numClasses();

        return Math.pow(weight, 4);
        // return Math.pow(1 / MMAE(eval.confusionMatrix()), 3);
    }

    private void evaluateLearner(int[][] cMatrix) {
        int totalTested = 0;
        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 8; j++){
                totalTested += cMatrix[i][j];
                System.out.print(cMatrix[i][j]);
                if (j < 7) System.out.print(" & ");
            }
            System.out.println();
        }

        int correct = 0;                                // standard accuracy
        int ballpark = 0;                               // ballpark accuracy
        int trend = 0;                                  // trend score
        double MAE = 0;                                 // mean absolute error
        double MMAE = 0;                                // macroaverageed mean absolute error
        for (int i = 0; i < cMatrix.length; i++) {
            correct += cMatrix[i][i];
            ballpark += cMatrix[i][i];
            if (i > 0) ballpark += cMatrix[i-1][i];
            if (i < 7) ballpark += cMatrix[i+1][i];

            int inClass = 0;
            int absError = 0;
            for (int j = 0; j < cMatrix.length; j++) {
                MAE += cMatrix[i][j] * Math.abs(i - j);
                trend += cMatrix[i][j] * (-Math.abs(i - j) + 1);
                inClass += cMatrix[i][j];
                absError += cMatrix[i][j] * Math.abs(i - j);
            }
            MMAE += (double)absError / inClass;
        }

        double accuracy = (double)correct/totalTested * 100;
        double ballpark_accuracy = (double)ballpark/totalTested * 100;
        double trend_score = (double)trend/totalTested;
        MAE = MAE / totalTested;
        MMAE = MMAE / cMatrix.length;

        // print evaluation results
        System.out.println("Accuracy: " + String.format("%.2f", accuracy) +"%");
        System.out.println("Ballpark Accuracy: " + String.format("%.2f", ballpark_accuracy) +"%");
        System.out.println("Trend Score: " + String.format("%.2f", trend_score));
        System.out.println("Mean Absolute Error: " + String.format("%.2f", MAE));
        System.out.println("Macroaveraged Mean Absolute Error: " + String.format("%.2f", MMAE));
        System.out.println();
    }

    private double MMAE(double[][] cMatrix) {
        double MMAE = 0;                    // macroaverageed mean absolute error
        for (int i = 0; i < 8; i++) {

            int inClass = 0;
            int absError = 0;
            for (int j = 0; j < 8; j++) {
                inClass += cMatrix[i][j];
                absError += cMatrix[i][j] * Math.abs(i - j);
            }
            MMAE += (double)absError / inClass;
        }

        return MMAE / cMatrix.length;
    }

    public void testAllUnique(String[] relations, int folds, int runs, int seed, Classifier classifier) throws Exception {
        long pow_set_size = (long)Math.pow(2, relations.length); 
        int counter, j; 
        for(counter = 0; counter < pow_set_size; counter++) { 
            HashSet<String> set = new HashSet<>();
            for(j = 0; j < relations.length; j++) { 
                if((counter & (1 << j)) > 0) {
                    set.add(relations[j]);
                    // System.out.print(relations[j]); 
                }
            }

            testSingleLearner(set.toArray(new String[set.size()]), folds, runs, seed, classifier);
        } 
    }

    /**
     * Creates the ARFF file for the 'Number of Bars' feature extraction.
     */
    public void createNumberOfBarsARFF(String name) {
        String fileName = name + ".arff";
        String header = "@relation " + name + "\n\n" +
                        "@attribute bars NUMERIC\n" +
                        "@attribute grade {1,2,3,4,5,6,7,8}\n\n" +
                        "@data\n";

                        WekaTools.writeARFF(dataPath + fileName, header, true);

        for (ArrayList<Tab> grade : tabdb.getTabs()) {
            for (Tab t : grade) {
                WekaTools.writeARFF(dataPath + fileName, t.getNumberOfBars() + ", " + t.getGrade() + "\n");
            }
        }
    }

    /**
     * Creates the ARFF file for the 'Highest Fret' feature extraction.
     */
    public void createHighestFretARFF(String name) {
        String fileName = name + ".arff";
        String header = "@relation " + name + "\n\n" +
                        "@attribute highestFret NUMERIC\n" +
                        "@attribute grade {1,2,3,4,5,6,7,8}\n\n" +
                        "@data\n";

                        WekaTools.writeARFF(dataPath + fileName, header, true);

        for (ArrayList<Tab> grade : tabdb.getTabs()) {
            for (Tab t : grade) {
                WekaTools.writeARFF(dataPath + fileName, t.getHighestFret() + ", " + t.getGrade() + "\n");
            }
        }
    }

    /**
     * Creates the ARFF file for the 'Number Of Fingers' feature extraction.
     */
    public void createAverageNoOfFingersARFF(String name) {
        String fileName = name + ".arff";
        String header = "@relation " + name + "\n\n" +
                        "@attribute fingers NUMERIC\n" +
                        "@attribute grade {1,2,3,4,5,6,7,8}\n\n" +
                        "@data\n";

        WekaTools.writeARFF(dataPath + fileName, header, true);

        for (ArrayList<Tab> grade : tabdb.getTabs()) {
            for (Tab t : grade) {
                WekaTools.writeARFF(dataPath + fileName, t.averageNumberOfFingers() + ", " + t.getGrade() + "\n");
            }
        }
    }

    /**
     * Creates the ARFF file for the 'Position Variance' feature extraction.
     */
    public void createPositionVarianceARFF(String name) {
        String fileName = name + ".arff";
        String header = "@relation " + name + "\n\n" +
                        "@attribute variance NUMERIC\n" +
                        "@attribute grade {1,2,3,4,5,6,7,8}\n\n" +
                        "@data\n";

        WekaTools.writeARFF(dataPath + fileName, header, true);

        for (ArrayList<Tab> grade : tabdb.getTabs()) {
            for (Tab t : grade) {
                WekaTools.writeARFF(dataPath + fileName, t.getPositionVariance() + ", " + t.getGrade() + "\n");
            }
        }
    }

    /**
     * Create the ARFF file for the 'Discrete Chords' feature extraction method with a couple of options to
     * configure to adjust the output.
     *
     * @param tfWeight  -   term frequency weight variant
     *                          "tf": term frequency,
     *                          "lognorm": logarithmic normalisation,
     *                          "doublenorm": double normalisation,
     *                          "binary": binary indication of presence in document,
     * @param idfWeight -   document frequency weight variant
     *                          "idf": inverse document frequency,
     *                          "idfs": inverse document frequency smooth,
     *                          "unary": no weighting
     */
    public void createDiscreteChordsARFF(String tfWeight, String idfWeight, String name) {
        HashMap<String, Double> documentFreq = new HashMap<>();
        for (Tab tab : tabdb.getTabsFlat()) {
            for (String k : tab.getDiscreteChords(tfWeight).keySet()){
                documentFreq.merge(k, (double) 1, Double::sum);                                 //incrementing count if exists in document
            }
        }
        if (idfWeight.equals("idf")) documentFreq.replaceAll((k, v) -> Math.log(tabdb.size() / v));                     //inverse document frequency
        else if (idfWeight.equals("idfs")) documentFreq.replaceAll((k, v) -> Math.log(tabdb.size() / (1 + v)) + 1);     //inverse document frequency smooth
        else documentFreq.replaceAll((k, v) -> (double) 1);                                                             //unary

        String fileName = name + ".arff";
        StringBuilder header = new StringBuilder("@relation " + name + "\n\n");
        ArrayList<String> attributes = new ArrayList<>();
        for (String attr : documentFreq.keySet()) {
            attributes.add(attr);
            header.append("@attribute ").append(attr).append(" NUMERIC\n");
        }
        header.append("@attribute grade {1,2,3,4,5,6,7,8}\n\n@data\n");
        WekaTools.writeARFF(dataPath + fileName, header.toString(), true);

        for (ArrayList<Tab> grade : tabdb.getTabs()) {
            for (Tab t : grade) {
                HashMap<String, Double> tabChordMap = t.getDiscreteChords(tfWeight);

                StringBuilder instance = new StringBuilder();
                for (String attr : attributes) {
                    if (tabChordMap.containsKey(attr))
                        instance.append(tabChordMap.get(attr) * documentFreq.get(attr)).append(", ");
                    else instance.append("0, ");
                }
                instance.append(t.getGrade()).append('\n');

                WekaTools.writeARFF(dataPath + fileName, instance.toString());
            }
        }
    }

    /**
     * Creates the ARFF file for the 'Chord Stretch' feature extraction.
     *
     * @param tfWeight  -   term frequency weight variant
     *                          "tf": term frequency,
     *                          "lognorm": logarithmic normalisation,
     *                          "doublenorm": double normalisation,
     *                          "binary": binary indication of presence in document,
     * @param idfWeight -   document frequency weight variant
     *                          "idf": inverse document frequency,
     *                          "idfs": inverse document frequency smooth,
     *                          "unary": no weighting
     * @param singles   -   include single note chords
     */
    public void createChordStretchARFF(String tfWeight, String idfWeight, boolean singles, String name) {
        HashMap<Integer, Double> documentFreq = new HashMap<>();
        for (Tab t : tabdb.getTabsFlat()){
            for (Integer k : t.getStretch(tfWeight, singles).keySet()){
                documentFreq.merge(k, (double) 1, Double::sum);
            }
        }
        if (idfWeight.equals("idf")) documentFreq.replaceAll((k, v) -> Math.log(tabdb.size() / v));                   //inverse document frequency
        else if (idfWeight.equals("idfs")) documentFreq.replaceAll((k, v) -> Math.log(tabdb.size() / (1 + v)) + 1);   //inverse document frequency smooth
        else documentFreq.replaceAll((k, v) -> (double) 1);
        // System.out.println(documentFreq);

        String fileName = name + ".arff";
        StringBuilder header = new StringBuilder("@relation " + name + "\n\n");
        ArrayList<Integer> attributes = new ArrayList<>();
        for (Integer attr : documentFreq.keySet()) {
            attributes.add(attr);
            header.append("@attribute ").append(attr).append(" NUMERIC\n");
        }
        header.append("@attribute grade {1,2,3,4,5,6,7,8}\n\n@data\n");
//            header.append("@attribute grade NUMERIC\n\n@data\n");
        WekaTools.writeARFF(dataPath + fileName, header.toString(), true);

        for (ArrayList<Tab> grade : tabdb.getTabs()) {
            for (Tab t : grade) {
                HashMap<Integer, Double> stretchMap = t.getStretch(tfWeight, singles);

                StringBuilder instance = new StringBuilder();
                for (Integer attr : attributes) {
                    if (stretchMap.containsKey(attr))
                        instance.append(stretchMap.get(attr) * documentFreq.get(attr)).append(", ");
                    else instance.append("0, ");
                }
                instance.append(t.getGrade()).append('\n');

                WekaTools.writeARFF(dataPath + fileName, instance.toString());
            }
        }
    }

    /**
     * ARFF naming overloads
     * 
     * Overloads for all of the above methods to faciliate optional file naming (to save alternative data 
     * configurations without overwriting existing ARFFs). These methods provide a wrapper by calling their
     * relevant method and passing a default name.
     */
    public void createNumberOfBarsARFF() {
        createNumberOfBarsARFF("numberOfBars");
    }

    public void createHighestFretARFF() {
        createHighestFretARFF("highestFret");
    }

    public void createAverageNoOfFingersARFF() {
        createAverageNoOfFingersARFF("averageNoOfFingers");
    }

    public void createPositionVarianceARFF() {
        createPositionVarianceARFF("positionVariance");
    }

    public void createDiscreteChordsARFF(String tfWeight, String idfWeight) {
        createDiscreteChordsARFF(tfWeight, idfWeight, "discreteChords");
    }

    public void createChordStretchARFF(String tfWeight, String idfWeight, boolean singles) {
        createChordStretchARFF(tfWeight, idfWeight, singles, "chordStretch");
    }
}