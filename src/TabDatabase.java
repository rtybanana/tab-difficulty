import java.util.ArrayList;

/** TabDatabase - class
 *
 */
public class TabDatabase {
    private ArrayList<Tab> tabs;

    public TabDatabase() {
        this.tabs = new ArrayList<>();
    }

    /** createDatabase -
     * Given a root folder containing folders named 'grade1' through 'grade8' which contain pieces in tab format, this
     * function will walk through each of the folders, construct a Tab object from each of the pieces and add it to the
     * database so that it can be processed into training data for the Naive Bayes Classifier later.
     *
     * @param root  :  root directory for the function to walk
     */
    public void createDatabase(String root) {

    }
}
