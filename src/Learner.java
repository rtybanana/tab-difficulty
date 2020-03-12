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
    private static String[] VOTE_SCHEMES = new String[]{"majority", "weighted", "probablistic", "probablistic_avg"};

    public Learner(TabDatabase tabdb) {
        this.dataPath = "ARFFs\\";
        this.tabdb = tabdb;
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
        if (data == null) {
            System.err.println("No data to test.");
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

        String name = "single: " + usedRelations.toString();
        evaluateLearner(name.toString(), cMatrix);
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
        ArrayList<String> usedRelations = new ArrayList<>();
        Instances data = null;
        for (int i = 0; i < relations.length; i++) {
            if (usedRelations.size() == 0) {
                data = WekaTools.readARFF(this.dataPath + relations[i] + ".arff");
                if (data != null) usedRelations.add(relations[i]);
            }
            else {
                Instances data2 = WekaTools.readARFF(this.dataPath + relations[i] + ".arff");
                if (data2 != null) {
                    data2.deleteAttributeAt(data2.attribute("grade").index());
                    data = Instances.mergeInstances(data, data2);
                    usedRelations.add(relations[i]);
                }
            }
        }
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

        String name = "homogenous ensemble: " + usedRelations.toString();
        evaluateLearner(name.toString(), cMatrix);
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
        ArrayList<Instances> dataset = new ArrayList<>();
        ArrayList<ArrayList<String>> datasetName = new ArrayList<>();
        for (int i = 0; i < relations.length; i++) {
            ArrayList<String> relationName = new ArrayList<>();
            Instances relation = null;
            for (int j = 0; j < relations[i].length; j++) {
                if (relationName.size() == 0) {
                    relation = WekaTools.readARFF(this.dataPath + relations[i][j] + ".arff");
                    if (dataset != null) relationName.add(relations[i][j]);
                    else {
                        System.err.println("Error loading file \"" + relations[i][j] + ".arff\"");
                    }
                }
                else {
                    Instances relation2 = WekaTools.readARFF(this.dataPath + relations[i][j] + ".arff");
                    if (relation2 != null) {
                        relation2.deleteAttributeAt(relation2.attribute("grade").index());
                        relation = Instances.mergeInstances(relation, relation2);
                        relationName.add(relations[i][j]);
                    }
                    else {
                        System.err.println("Error loading file \"" + relations[i][j] + ".arff\"");
                    }
                }
            }
            relation.setClassIndex(relation.attribute("grade").index());
            dataset.add(relation);
            datasetName.add(relationName);
        }
        if (dataset.size() < 1) {
            System.err.println("No data to test.");
            return;
        }

        if(!Arrays.asList(VOTE_SCHEMES).contains(voteScheme)) {
            System.err.println("Vote weighting scheme not recognised, defaulting to majority");
            voteScheme = "majority";
        }

        // test and build confusion matrix
        ArrayList<Classifier> classifiers = new ArrayList<>(Arrays.asList(AbstractClassifier.makeCopies(classifier, dataset.size())));
        int[][] cMatrix = new int[8][8];
        for (int r = 0; r < runs; r++) {
            for (Instances relation : dataset) {
                relation.randomize(new Random(seed + r));
                relation.stratify(folds);
            }

            for (int n = 0; n < folds; n++) {
                ArrayList<Instances> trainingsets = new ArrayList<>();
                ArrayList<Instances> testsets = new ArrayList<>();
                ArrayList<Double> weights = new ArrayList<>();

                for (Instances relation : dataset) {
                    Instances train = relation.trainCV(folds, n, new Random(seed + r));
                    trainingsets.add(train);
                    testsets.add(relation.testCV(folds, n));
                }

                for (int c = 0; c < classifiers.size(); c++) {
                    classifiers.get(c).buildClassifier(trainingsets.get(c));
                    if (voteScheme.equals("weighted") || voteScheme.equals("probablistic") || voteScheme.equals("probablistic_avg")) {
                        weights.add(weightClassifier(classifiers.get(c), trainingsets.get(c)));
                    }
                }

                System.out.println(weights);

                // confusion matrix
                for (int t = 0; t < testsets.get(0).size(); t++) {
                    double meanClass = (double) 0;
                    double totalWeight = (double) 0;
                    double[] averageDist = new double[testsets.get(0).numClasses()];
                    for (int c = 0; c < classifiers.size(); c++) {
                        Instance instance = testsets.get(c).get(t);
                        int cls = (int)classifiers.get(c).classifyInstance(instance);

                        // weighted probabalistic average vote and weighted probablistic vote
                        if (voteScheme.contains("probablistic")) {
                            double weight = weights.get(c);
                            double[] distribution = classifiers.get(c).distributionForInstance(instance);
                            Arrays.setAll(averageDist, i -> averageDist[i] + distribution[i] * (i + 1) * weight);
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
                    
                    // weighted probabalistic average vote and mean
                    if (voteScheme.equals("probablistic_avg")) {
                        estimate = (int)Math.round(Arrays.stream(averageDist).sum() / totalWeight) - 1;
                    }

                    // weighted probabalistic vote and mean
                    else if (voteScheme.equals("probablistic")) {
                        double finalTotalWeight = totalWeight;
                        Arrays.setAll(averageDist, i -> averageDist[i] / finalTotalWeight);
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

                    // printing debugging
                    // double avg = Math.round(Arrays.stream(averageDist).sum() / classifiers.size());
                    // if ((int) (meanClass + 1) != (int)avg) {
                    //     System.out.println(Arrays.toString(averageDist) + ": "
                    //                             + testsets.get(0).get(t).toString(testsets.get(0).attribute("grade")) + " | "
                    //                             + (int) (meanClass + 1) + ", " + (int)avg);
                    // }

                    cMatrix[Integer.parseInt(testsets.get(0).get(t).toString(testsets.get(0).attribute("grade"))) - 1][estimate]++;
                }
            }
        }

        String name = "heterogenous ensemble: " + datasetName.toString();
        evaluateLearner(name.toString(), cMatrix);
    }

    private double weightClassifier(Classifier classifier, Instances train) throws Exception {
        Evaluation eval = new Evaluation(train);
        eval.crossValidateModel(classifier, train, 10, new Random());
        double weight = (-MMAE(eval.confusionMatrix()) + train.numClasses()) / train.numClasses();

        return Math.pow(weight, 4);
        // return Math.pow(1 / MMAE(eval.confusionMatrix()), 3);
    }

    private void evaluateLearner(String name, int[][] cMatrix) {
        int totalTested = 0;
        System.out.println(name);
        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 8; j++){
                totalTested += cMatrix[i][j];
                System.out.print(cMatrix[i][j]);
                if (j < 7) System.out.print(" & ");
            }
            System.out.println();
        }

        int correct = 0;                    // standard accuracy
        int ballpark = 0;                   // ballpark accuracy
        int trend = 0;                      // trend score
        double MMAE = 0;                    // macroaverageed mean absolute error
        for (int i = 0; i < cMatrix.length; i++) {
            correct += cMatrix[i][i];
            ballpark += cMatrix[i][i];
            if (i > 0) ballpark += cMatrix[i-1][i];
            if (i < 7) ballpark += cMatrix[i+1][i];

            int inClass = 0;
            int absError = 0;
            for (int j = 0; j < cMatrix.length; j++) {
                trend += cMatrix[i][j] * (-Math.abs(i - j) + 1);
                inClass += cMatrix[i][j];
                absError += cMatrix[i][j] * Math.abs(i - j);
            }
            MMAE += (double)absError / inClass;
        }

        double accuracy = (double)correct/totalTested * 100;
        double ballpark_accuracy = (double)ballpark/totalTested * 100;
        double trend_score = (double)trend/totalTested;
        MMAE = MMAE / cMatrix.length;

        // print evaluation results
        System.out.println("Accuracy: " + String.format("%.2f", accuracy) +"%");
        System.out.println("Ballpark Accuracy: " + String.format("%.2f", ballpark_accuracy) +"%");
        System.out.println("Trend Score: " + String.format("%.2f", trend_score));
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

    /**
     * Creates the ARFF file for the 'Number of Bars' feature extraction. Ad
     */
    public void createNumberOfBarsARFF() {
        String fileName = "numberOfBars.arff";
        String header = "@relation numberOfBars\n\n" +
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
     * Creates the ARFF file for the 'Number Of Fingers' feature extraction.
     */
    public void createAverageNoOfFingersARFF() {
        String fileName = "averageNoOfFingers.arff";
        String header = "@relation averageNoOfFingers\n\n" +
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
    public void createPositionVarianceARFF() {
        String fileName = "positionVariance.arff";
        String header = "@relation positionVariance\n\n" +
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
    public void createDiscreteChordsARFF(String tfWeight, String idfWeight) {
        HashMap<String, Double> documentFreq = new HashMap<>();
        for (Tab tab : tabdb.getTabsFlat()) {
            for (String k : tab.getDiscreteChords(tfWeight).keySet()){
                documentFreq.merge(k, (double) 1, Double::sum);                                 //incrementing count if exists in document
            }
        }
        if (idfWeight.equals("idf")) documentFreq.replaceAll((k, v) -> Math.log(tabdb.size() / v));                     //inverse document frequency
        else if (idfWeight.equals("idfs")) documentFreq.replaceAll((k, v) -> Math.log(tabdb.size() / (1 + v)) + 1);     //inverse document frequency smooth
        else documentFreq.replaceAll((k, v) -> (double) 1);                                                             //unary

        String fileName = "discreteChords.arff";
        StringBuilder header = new StringBuilder("@relation discreteChords\n\n");
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
    public void createChordStretchARFF(String tfWeight, String idfWeight, boolean singles) {
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

        String fileName = "chordStretch.arff";
        StringBuilder header = new StringBuilder("@relation chordStretch\n\n");
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
}