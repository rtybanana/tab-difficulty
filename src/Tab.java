import java.util.ArrayList;
import java.util.Arrays;

class Tab {
    private String name;
    private int grade;
    private ArrayList<String> events;
    private int courses;
    private static final Character[] NOTE_EVENTS = {'0', '1', '2', '3', '4', '5', 'x', 'w', 'W', '#', 'Y', 'y'};
    private static final Character[] FRETTED_NOTES = {'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'n', 'o', 'p'};

    public Tab(String name, ArrayList<String> events, int grade, int courses){
        this.name = name;
        this.grade = grade;
        this.events = events;
        this.courses = courses;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getEvents() {
        return events;
    }

    public int getGrade() {
        return grade;
    }

    public int getCourses() { return courses; }

    public String toString() { return this.name; }

    /**
     * TODO unfinished method
     * @param event - tab file text line to check
     * @return whether the event is a note event or not
     */
    private boolean isNoteEvent(String event) {
        //boolean isNoteEvent = true;
        return Arrays.asList(NOTE_EVENTS).contains(event.charAt(0));
    }

    public int getNumberOfBars() {
        int bars = 0;
        boolean potentialBar = false;
        for (String event : events) {
            if (potentialBar) {
                if (!(event.charAt(0) == 'b' || event.equals("B") || event.charAt(0) == 'e')) {
                    bars++;
                    potentialBar = false;
                }
            }
            else if (event.charAt(0) == 'b' || event.equals("B")) potentialBar = true;
        }

        return bars;
    }

    /**
     * return an integer representation of an array of doubles indicating the percentages of the chords in the piece
     * that appeared with that stretch. [0] is no stretch i.e. all the same fret. [5] is a difference of 5 frets
     * (long stretch).
     * we can ~possibly~ represent this as an integer by calculating the sum of each proportion multiplied by it's
     * index + 1. the higher the number the more tough stretch chords occurred.
     *
     * for example   [0]  [1]  [2]  [3]  [4]  [5]      |      [0]  [1]  [2]  [3]  [4]  [5]
     *               33,  27,  20,  10,   0,   0       |      12,  18,  25,  20,  15,  10
     *                                                 |
     *               33 + 54 + 60 + 40  + 0  + 0       |      12 + 36 + 75 + 80 + 75 + 60
     *               = 187                             |      = 238
     */
    public double[] getStretch(){
        int totalChordEvents = 0;
        double[] stretchProportion = {0, 0, 0, 0, 0, 0, 0};
        for (String event : events) {
            int lowestFret = 100;                       //larger than any fret number on a guitar
            int highestFret = 0;                        //lower than any fret number on a guitar
            if (isNoteEvent(event)) {
                int fingers = 0;
                for (char c : event.toCharArray()) {
                    int fret = (int)c - 97;
                    if (fret > 0 && fret < 15) {                            //in range for fretted notes
                        fingers++;
                        if (fret < lowestFret) lowestFret = fret;
                        if (fret > highestFret) highestFret = fret;
                    }
                }
                if (fingers > 1) {
                    totalChordEvents++;
                    stretchProportion[highestFret - lowestFret]++;
                }
            }
        }
        for (int i = 0; i < stretchProportion.length; i++) {
            stretchProportion[i] /= totalChordEvents;
        }
        return stretchProportion;            //stretch
    }

    public int getPositionVariance(){
        //loop through and add each position change to an array list starting from position 0 (a), so if starting in
        //position 5 (f) the first item added to the list is 5 (difference from 0 to 5).
        //think about how a position change is defined, when does a human have to change positions.

        //return the variance of this list of position changes

        return 0;
    }
}
