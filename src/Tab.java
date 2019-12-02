import java.util.*;

class Tab {
    private String name;
    private int grade;
    private ArrayList<String> events;
//    private int courses;
    private static final Character[] NOTE_EVENTS = {'0', '1', '2', '3', '4', '5', 'x', 'w', 'W', '#', 'Y', 'y'};
    private static final Character[] CHORD_CHARS = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'n', 'o', 'p'};

    public Tab(String name, ArrayList<String> events, int grade){
        this.name = name;
        this.grade = grade;
        this.events = events;
//        this.courses = courses;
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

//    public int getCourses() { return courses; }

    public String toString() { return this.name; }

    /**
     * TODO unfinished method - still needs to allow for triplet notes i think
     * @param event - tab file text line to check
     * @return whether the event is a note event or not
     */
    private boolean isNoteEvent(String event) {
        //boolean isNoteEvent = true;
        return Arrays.asList(NOTE_EVENTS).contains(event.charAt(0));
    }

    public HashMap<String, Double> getDiscreteChords(String weight) {
        int noOfEvents = 0;
        HashMap<String, Double> chords = new HashMap<>();
        for (String event : events){
            if (isNoteEvent(event)){
                StringBuilder chord = new StringBuilder();
                int spaces = 0;
                for (char c : event.toCharArray()){
                    if (Arrays.asList(CHORD_CHARS).contains(c)) {
                        while (spaces > 0){
                            chord.append('-');
                            spaces--;
                        }
                        chord.append(c);
                    }
                    else if (c == ' ') spaces++;
                }

                //some of the files use a note event (1,2,3,x,etc) with no strings played instead of a rest event (R),
                //ignoring chord strings which are empty allows us to filter these out.
                if (!chord.toString().equals("")) {
                    chords.merge(chord.toString(), (double) 1, Double::sum);
                }
                noOfEvents++;
            }
        }

        int finalNoOfEvents = noOfEvents;
        switch (weight) {
            case "tf":
                chords.replaceAll((k, v) -> v / finalNoOfEvents);                   //classic term frequency (thisEventCount/totalEvents)
                break;
            case "lognorm":
                chords.replaceAll((k, v) -> Math.log(1 + v));                                   //log normalization
                break;
            case "doublenorm":
                chords.replaceAll((k, v) -> (0.5 + 0.5 * (v / Collections.max(chords.entrySet(), Map.Entry.comparingByValue()).getValue())));
                break;
            case "binary":
                chords.replaceAll((k, v) -> v > 0 ? (double) 1 : (double) 0);       //convert to binary 0, 1
                break;
        }


        return chords;
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
     * index + 1. the higher the number the more tough stretch chords occurred. Though it may just be better to leave it
     * as a double array.
     *
     * for example   [0]  [1]  [2]  [3]  [4]  [5]      |      [0]  [1]  [2]  [3]  [4]  [5]
     *               33,  27,  20,  10,   0,   0       |      12,  18,  25,  20,  15,  10
     *                                                 |
     *               33 + 54 + 60 + 40  + 0  + 0       |      12 + 36 + 75 + 80 + 75 + 60
     *               = 187                             |      = 238
     */
    public HashMap<Integer, Double> getStretch(String weight, boolean singles){
        int noOfEvents = 0;
        HashMap<Integer, Double> stretches = new HashMap<>();
        for (String event : events) {
            int lowestFret = 100;                                           //larger than any fret number on a guitar
            int highestFret = 0;                                            //lower than any fret number on a guitar
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
                    stretches.merge(highestFret - lowestFret, (double) 1, Double::sum);
                    noOfEvents++;
                }
                else if (singles) {
                    stretches.merge(0, (double) 1, Double::sum);                           //include one note chords?
                    noOfEvents++;
                }

            }
        }

        int finalNoOfEvents = noOfEvents;
        switch (weight) {
            case "tf":
                stretches.replaceAll((k, v) -> v / finalNoOfEvents);                        //classic term frequency (thisEventCount/totalEvents)
                break;
            case "lognorm":
                stretches.replaceAll((k, v) -> Math.log(1 + v));                            //log normalization
                break;
            case "doublenorm":                                                              //double normalization
                stretches.replaceAll((k, v) -> (0.5 + 0.5 * (v / Collections.max(stretches.entrySet(), Map.Entry.comparingByValue()).getValue())));
                break;
            case "binary":
                stretches.replaceAll((k, v) -> v > 0 ? (double) 1 : (double) 0);            //convert to binary 0, 1
                break;
        }

        return stretches;
    }

    public int getPositionVariance(){
        //loop through and add each position change to an array list starting from position 0 (a), so if starting in
        //position 5 (f) the first item added to the list is 5 (difference from 0 to 5).
        //think about how a position change is defined, when does a human have to change positions.

        //return the variance of this list of position changes

        return 0;
    }
}
