import java.util.*;

class Tab {
    private String name;
    private int grade;
    private ArrayList<String> events;
    private static final Character[] NOTE_EVENTS = {'0', '1', '2', '3', '4', '5', 'x', 'w', 'W', '#', 'Y', 'y'};
    private static final Character[] CHORD_CHARS = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'n', 'o', 'p'};

    public Tab(String name, ArrayList<String> events, int grade){
        this.name = name;
        this.grade = grade;
        this.events = events;
    }

    public String toString() { 
        return this.name + ", grade " + this.grade; 
    }

    private boolean isNoteEvent(String event) {
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

    /** getNumberOfBars
     * 
     * @return the number of bars in this tab
     */
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

    /** getStretch
     * 
     * @param weight    -   
     * @param singles   -
     * 
     * @return a hashmap   
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

    public double averageNumberOfFingers() {
        int noOfEvents = 0;
        Double totalFingers = (double) 0;
        for (String event : events) {
            if (isNoteEvent(event)) {
                int fingers = 0;
                for (char c : event.toCharArray()) {
                    int fret = (int)c - 97;
                    if (fret > 0 && fret < 15) {                            // in range for fretted notes
                        fingers++;
                    }
                }

                if (fingers > 0) {
                    totalFingers += fingers;
                    noOfEvents++;
                }
            }
        }

        return totalFingers / noOfEvents;
    }
    
    public double getPositionVariance(){
        int noOfEvents = 0;
        Double totalPositionChange = (double) 0;
        int previousFret = 0;                                               // start from 0 - gives an initial score based on the starting position
        for (String event : events) {
            if (isNoteEvent(event)) {
                int thisFret = 100;                                         // higher than any fret on a guitar
                for (char c : event.toCharArray()) {
                    int fret = (int)c - 97;
                    if (fret > 0 && fret < 15) {                            // in range for fretted notes
                        if (fret < thisFret) thisFret = fret;
                    }
                }

                if (thisFret != 100) {
                    int currentChange = Math.abs(thisFret - previousFret);
                    totalPositionChange += currentChange;
                    previousFret = thisFret;
                }
                noOfEvents++;
            }
        }

        return totalPositionChange / noOfEvents;
    }

    // getters
    public String getName() {
        return name;
    }

    public ArrayList<String> getEvents() {
        return events;
    }

    public int getGrade() {
        return grade;
    }
}
