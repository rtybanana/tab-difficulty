import java.util.ArrayList;

class Tab {
    private String name;
    private int grade;
    private ArrayList<String> events;
    private int numberOfBars;

    public Tab(String name, ArrayList<String> events, int grade){
        this.name = name;
        this.grade = grade;
        this.events = events;
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

    public int getNumberOfBars() {
        int bars = 0;
        boolean potentialBar = false;
        for (String line : events) {
            if (potentialBar) {
                if (!(line.charAt(0) == 'b' || line.equals("B") || line.charAt(0) == 'e')) {
                    bars++;
                    potentialBar = false;
                }
            }
            else if (line.charAt(0) == 'b' || line.equals("B")) potentialBar = true;
        }

        return bars;
    }

    public int getStretch(){
        //return an integer representation of an array of doubles indicating the percentages of the chords in the piece
        //that appeared with that stretch. [0] is no stretch i.e. all the same fret. [5] is a difference of 5 frets
        //(long stretch).
        //we can ~possibly~ represent this as an integer by calculating the sum of each proportion multiplied by it's
        //index + 1. the higher the number the more tough stretch chords occurred.
        //
        //for example   [0]  [1]  [2]  [3]  [4]  [5]      |      [0]  [1]  [2]  [3]  [4]  [5]
        //              33,  27,  20,  10,   0,   0       |      12,  18,  25,  20,  15,  10
        //                                                |
        //              33 + 54 + 60 + 40  + 0  + 0       |      12 + 36 + 75 + 80 + 75 + 60
        //              = 187                             |      = 238

        return 0;
    }

    public int getPositionVariance(){
        //loop through and add each position change to an array list starting from position 0 (a), so if starting in
        //position 5 (f) the first item added to the list is 5 (difference from 0 to 5).
        //think about how a position change is defined, when does a human have to change positions.

        //return the variance of this list of position changes

        return 0;
    }
}
