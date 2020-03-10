import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;

/**
 * An ensemble class wrapper used to group (naive bayes) classifiers homogenously or heterogenously
 */
public class NBEnsemble extends AbstractClassifier {
    private static final long serialVersionUID = 8557630175542299278L;
    private List<NaiveBayes> ensemble;
    private int ensembleSize;
    private

    NBEnsemble() {
        initEnsemble(100);
    }

    NBEnsemble(int ensembleSize) {
        initEnsemble(ensembleSize);
    }
    
    /**
     * build method for the homogenous version of the ensemble classifier
     * @param instances     - the instances to train with
     * @throws Exception
     */
    public void buildClassifier(Instances instances) throws Exception {
        Resample resample = new Resample();
        resample.setRandomSeed(12);
        resample.setBiasToUniformClass(0);
        resample.setSampleSizePercent(100);
        resample.setNoReplacement(false);
        resample.setInputFormat(instances);
            
        for (NaiveBayes nb : ensemble) {
            Instances subset = Filter.useFilter(instances, resample);
            nb.buildClassifier(subset);
        }

    }

    /**
     * build method for the heterogenous version of the ensemble classifier
     * @param instanceses   - the list of different feature extraction training sets
     * @throws Exception
     */
    public void buildClassifier(Instances[] instanceses) throws Exception {
        initEnsemble(instanceses.length);
        ArrayList<Evaluation> evals = new ArrayList<>();
        for (int i = 0; i < instanceses.length; i++) {
            evals.add(new Evaluation(instanceses[i]));
        }

        // for (NaiveBayes nb :)
    }

    public double classifyInstance(Instance instance) throws Exception {
        double[] dist = distributeForInstance(instance);

        int most = 0;
        for (int i = 1; i < dist.length; i++) {
            if (dist[i] > dist[most]) most = i;
        }

        return (double)most;
    }

    public double[] distributeForInstance(Instance instance) throws Exception {
        double[] votes = new double[instance.numClasses()];
        for (NaiveBayes nb : ensemble) {
            votes[(int)nb.classifyInstance(instance)]++;
        }

        Arrays.setAll(votes, i -> (votes[i] / ensembleSize));
        System.out.println(Arrays.toString(votes));

        return votes;
    }

    public void initEnsemble(int ensembleSize) {
        this.ensembleSize = ensembleSize;
        this.ensemble = new ArrayList<>();

        for (int i = 0; i < ensembleSize; i++) {
            this.ensemble.add(new NaiveBayes());
        }
    }
}