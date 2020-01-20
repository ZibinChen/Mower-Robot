import java.util.ArrayList;

/**
 * Class representing a lawn which is a primary construct of the simulation system.
 */
public class Lawn {

    //Class variables
    private int width;
    private int height;
    public final int START_NUMBER_GOPHERS;
    public final int START_NUMBER_MOWERS;
    public final int START_NUMBER_UNCUT;
    public final int MAX_MOWER_ENERGY;
    private int currentNumberCut;
    private int gopherPeriod;
    public final int MAX_PAR;
    private int currentTurnNumber;
    private Square[][] theGrid;
    private boolean isVerbose;

    /**
     * Constructor for the Lawn object. Initialize and set variables that represent lawn state.
     * @param width - x value indicating how wide the lawn is
     * @param height - y value indicating how high the lawn is
     * @param numMowers - number of mowers on this lawn
     * @param numGophers - number of gophers on this lawn
     * @param gopherPeriod - number of turns between each gopher move
     * @param maxPar - max number of turns that it should take to cut this lawn
     */
    public Lawn(int width, int height, int numMowers, int numGophers, int gopherPeriod, int maxPar, int maxMowerEnergy, boolean isVerbose){
        this.width = width;
        this.height = height;
        this.START_NUMBER_MOWERS = numMowers;
        this.START_NUMBER_GOPHERS = numGophers;
        this.START_NUMBER_UNCUT = (width * height);
        this.currentNumberCut = 0;
        this.gopherPeriod = gopherPeriod;
        this.MAX_PAR = maxPar;
        this.MAX_MOWER_ENERGY = maxMowerEnergy;
        this.isVerbose = isVerbose;
        this.currentTurnNumber = 0;
        theGrid = new Square[width+2][height+2];
    }

    /**
     * Populates the grid with instances of Squares that comprise of the lawn.
     * @param gopherConfigs Data on location of the gophers so that they can be set at correct corresponding places on the grid.
     */
    public void populateSquaresOnLawn(ArrayList<String> gopherConfigs){

        //Start with lawn full of grass (no fence, no crater, no nothing)
        for (int i = 0; i < (width+2); i++){
            for (int j = 0; j < (height+2); j++){
                theGrid[i][j] = new Square(i-1, j-1, "grass");
            }
        }

        //Make the border fence squares
        this.populateFencesOnLawn();

        //Place gophers at their initial location
        this.placeGophersOnLawn(gopherConfigs);


    }

    /**
     * Instantiates the fence Squares and places them at the edges of the lawn's grid.
     */
    private void populateFencesOnLawn(){
        //Variables needed for setting fence around the lawn
        int fenceMaxX = width + 2;
        int fenceMaxY = height + 2;

        //Populate the bottom of fence
        for (int i = 0; i < fenceMaxX; i++){
            theGrid[i][0] = new Square(-1+i, -1, "fence");
        }
        //Populate the top of fence
        for (int i = 0; i < fenceMaxX; i++){
            theGrid[i][fenceMaxY-1] = new Square(-1+1, height, "fence");
        }
        //Populate the left of fence
        for (int j = 0; j < fenceMaxY; j++){
            theGrid[0][j] = new Square(-1, -1+j, "fence");
        }
        //Populate the right of fence
        for (int j = 0; j < fenceMaxY; j++){
            theGrid[fenceMaxX-1][j] = new Square(width, -1+j, "fence");
        }

    }

    /**
     * Sets the Squares where gophers are at to appropriate SquareKind.
     */
    private void placeGophersOnLawn(ArrayList<String> gopherConfigs){
        //Get the data about each gopher's location
        for (int i = 0; i < gopherConfigs.size(); i++) {
            String theConfig = gopherConfigs.get(i);
            String[] tokens = theConfig.split(",");
            int gopherX = Integer.parseInt(tokens[0]);
            int gopherY = Integer.parseInt(tokens[1]);

            //Add each gopher to the grid
            Square squareToChange = theGrid[gopherX + 1][gopherY + 1];
            squareToChange.setType(SquareKind.GOPHER_GRASS);
        }
    }

    /**
     * Moves gopher from its previous location XY to its new location XY
     * @param prevX Previous X coordinate where gopher was
     * @param prevY Previous Y coordinate where gopher was
     * @param newX New X coordinate where gopher will go
     * @param newY New Y coordinate where gopher will go
     * @return Whether gopher has successfully moved (True) or stayed at previous location (False)
     */
    public boolean moveGopher(int prevX, int prevY, int newX, int newY){
        boolean answer = false;

        //Check that the gopher is moving only +/- 1 in both X and Y
        if (newX >= prevX-1 && newX <= prevX+1){
            if (newY >= prevY-1 && newY <= prevY+1){

                Square prevSquare = theGrid[prevX + 1][prevY + 1];
                Square newSquare = theGrid[newX + 1][newY + 1];

                if (newSquare.getType() == SquareKind.GRASS){
                    // Move gopher and update Square kind to Gopher_Grass
                    answer = true;
                    newSquare.setType(SquareKind.GOPHER_GRASS);
                }
                else if (newSquare.getType() == SquareKind.EMPTY){
                    answer = true;
                    newSquare.setType(SquareKind.GOPHER_EMPTY);
                }
                else if (newSquare.getType() == SquareKind.GOPHER_GRASS || newSquare.getType() == SquareKind.GOPHER_EMPTY || newSquare.getType() == SquareKind.FENCE){
                    answer = false;
                }
                else if (newSquare.getType() == SquareKind.MOWER){
                    // Moved and killed a mower at that square
                    answer = true;
                    newSquare.setType(SquareKind.GOPHER_EMPTY);
                }
                if (answer) {
                    // Gopher successfully moved, update its prevSquare to free it up
                    if (prevSquare.getType() == SquareKind.GOPHER_GRASS){
                        prevSquare.setType(SquareKind.GRASS);
                    }
                    else if (prevSquare.getType() == SquareKind.GOPHER_EMPTY){
                        prevSquare.setType(SquareKind.EMPTY);
                    }
                }
            }
        }
        return answer;
    }

    /**
     * Moves or puts a mower onto a particular Square within the lawn's grid.
     * The Square can disable or crash a mower and also track whether a mower currently occupies it.
     * @param m Mower which could be disabled or crashed by a Square on the lawn
     * @param mowerX X coordinate representing the Mower's placed location
     * @param mowerY Y coordinate representing the Mower's placed location
     * @return Status of the Mower (whether the Square on which it is placed has disabled the it)
     */
    public boolean placeMower(Mower m, int mowerX, int mowerY){
        //Get Square from grid and mark it as occupied by a mower
        Square mowerCurrentSquare = theGrid[mowerX+1][mowerY+1];
        mowerCurrentSquare.setMowerParked(true);

        //Disable mower if it is placed on Square that disallows traversing
        if (mowerCurrentSquare.getType() == SquareKind.GOPHER_EMPTY ||
        mowerCurrentSquare.getType() == SquareKind.GOPHER_GRASS ||
        mowerCurrentSquare.getType() == SquareKind.FENCE ||
        mowerCurrentSquare.getType() == SquareKind.MOWER){
            mowerCurrentSquare.disableMower(m);
            mowerCurrentSquare.setMowerParked(false);
            return false;
        }
        else{
            mowerCurrentSquare.cut(m);
            currentNumberCut = this.updateCurrentNumberCut();
            mowerCurrentSquare.setType(SquareKind.MOWER);
            return true;
        }
    }
    
    /**
     * Removes and cleans up occupation information for Squares which were occupied by Mowers that have since crashed for other reasons.
     * @param wreckX X coordinate representing a Square on grid
     * @param wreckY Y coordinate representing a Square on grid
     */
    public void removeMowerWreck(int wreckX, int wreckY){
        Square wreckSquare = theGrid[wreckX+1][wreckY+1];
        wreckSquare.setMowerParked(false);
        if (wreckSquare.isRechargePad()){
            wreckSquare.setType(SquareKind.RECHARGE);
        }
        else{
            wreckSquare.setType(SquareKind.EMPTY);
        }
    }

    /**
     * Removes and cleans up occupation information for Squares which were occupied by Mowers that have since moved away.
     * @param oldX X coordinate representing a Square on grid which was once occupied by a mower
     * @param oldY Y coordinate representing a Square on grid which was once occupied by a mower
     */
    public void pickupMower(int oldX, int oldY){
        Square mowerWasHere = theGrid[oldX+1][oldY+1];
        mowerWasHere.setMowerParked(false);
        if (mowerWasHere.isRechargePad()) {
            mowerWasHere.setType(SquareKind.RECHARGE);
        }
        else{
            mowerWasHere.setType(SquareKind.EMPTY);
        }
    }


    /**
     * Provides information of surrounding 8 Squares when a scan is initiated from a Square in the center.
     * @param x X coordinate representing Square from which to scan
     * @param y Y coordinate representing Square from which to scan
     * @return Comma separated string containing details about the surrounding 8 Squares in order of N, NE, E, SE, S, SW, W, NW
     */
    public String cScanFrom(int x, int y){
        //Grid coordinates of the central square
        int scanCenterXGrid = x+1;
        int scanCenterYGrid = y+1;
        ArrayList<Square> answer = new ArrayList<>();

        //Get the eight surrounding Squares
        answer.add(theGrid[scanCenterXGrid][scanCenterYGrid+1]); //N
        answer.add(theGrid[scanCenterXGrid+1][scanCenterYGrid+1]); //NE
        answer.add(theGrid[scanCenterXGrid+1][scanCenterYGrid]); //E
        answer.add(theGrid[scanCenterXGrid+1][scanCenterYGrid-1]); //SE
        answer.add(theGrid[scanCenterXGrid][scanCenterYGrid-1]); //S
        answer.add(theGrid[scanCenterXGrid-1][scanCenterYGrid-1]); //SW
        answer.add(theGrid[scanCenterXGrid-1][scanCenterYGrid]); //W
        answer.add(theGrid[scanCenterXGrid-1][scanCenterYGrid+1]); //NW
        String answerString = "";

        //Get the kind of each of the surrounding Squares
        for (int j = 0; j < answer.size(); j++){
            Square theSquare = answer.get(j);
            answerString = answerString + theSquare.getSquareKind() + ",";
        }
        answerString = answerString.substring(0, answerString.length()-1);
        return answerString;
    }

    /**
     * Provides line scan in current facing direction
     * @param x X coordinate representing Square from which to scan
     * @param y Y coordinate representing Square from which to scan
     * @param facing Direction to line scan toward
     * @return Comma separated string containing details about the N number of squares in order starting with closest one in front of scanning mower
     */
    public String lScanFrom(int x, int y, Direction facing) {
        //Grid coordinates of the central square
        int scanCenterXGrid = x + 1;
        int scanCenterYGrid = y + 1;
        ArrayList<String> answer = new ArrayList<>();

        //Return scan results based on current direction
        switch (facing) {
            case NORTH:
                int northCounter = 0;
                while (true) {
                    northCounter++;
                    Square s = theGrid[scanCenterXGrid][scanCenterYGrid + northCounter];
                    if (s.getType() != SquareKind.FENCE) {
                        answer.add(s.getSquareKind());
                    } else {
                        answer.add(s.getSquareKind());
                        break;
                    }
                }
                break;
            case NORTHEAST:
                int northeastCounter = 0;
                while (true) {
                    northeastCounter++;
                    Square s = theGrid[scanCenterXGrid + northeastCounter][scanCenterYGrid + northeastCounter];
                    if (s.getType() != SquareKind.FENCE) {
                        answer.add(s.getSquareKind());
                    } else {
                        answer.add(s.getSquareKind());
                        break;
                    }
                }
                break;
            case EAST:
                int eastCounter = 0;
                while (true) {
                    eastCounter++;
                    Square s = theGrid[scanCenterXGrid + eastCounter][scanCenterYGrid];
                    if (s.getType() != SquareKind.FENCE) {
                        answer.add(s.getSquareKind());
                    } else {
                        answer.add(s.getSquareKind());
                        break;
                    }
                }
                break;
            case SOUTHEAST:
                int southeastCounter = 0;
                while (true) {
                    southeastCounter++;
                    Square s = theGrid[scanCenterXGrid + southeastCounter][scanCenterYGrid - southeastCounter];
                    if (s.getType() != SquareKind.FENCE) {
                        answer.add(s.getSquareKind());
                    } else {
                        answer.add(s.getSquareKind());
                        break;
                    }
                }
                break;
            case SOUTH:
                int southCounter = 0;
                while (true) {
                    southCounter++;
                    Square s = theGrid[scanCenterXGrid][scanCenterYGrid - southCounter];
                    if (s.getType() != SquareKind.FENCE) {
                        answer.add(s.getSquareKind());
                    } else {
                        answer.add(s.getSquareKind());
                        break;
                    }
                }
                break;
            case SOUTHWEST:
                int southwestCounter = 0;
                while (true) {
                    southwestCounter++;
                    Square s = theGrid[scanCenterXGrid - southwestCounter][scanCenterYGrid - southwestCounter];
                    if (s.getType() != SquareKind.FENCE) {
                        answer.add(s.getSquareKind());
                    } else {
                        answer.add(s.getSquareKind());
                        break;
                    }
                }
                break;
            case WEST:
                int westCounter = 0;
                while (true) {
                    westCounter++;
                    Square s = theGrid[scanCenterXGrid - westCounter][scanCenterYGrid];
                    if (s.getType() != SquareKind.FENCE) {
                        answer.add(s.getSquareKind());
                    } else {
                        answer.add(s.getSquareKind());
                        break;
                    }
                }
                break;
            case NORTHWEST:
                int northwestCounter = 0;
                while (true) {
                    northwestCounter++;
                    Square s = theGrid[scanCenterXGrid - northwestCounter][scanCenterYGrid + northwestCounter];
                    if (s.getType() != SquareKind.FENCE) {
                        answer.add(s.getSquareKind());
                    } else {
                        answer.add(s.getSquareKind());
                        break;
                    }
                }
                break;
            default:
                break;

        }

        String finalAnswer = "";
        for (int i = 0; i < answer.size(); i++){
            finalAnswer = finalAnswer + answer.get(i) + ",";
        }
        finalAnswer = finalAnswer.substring(0, finalAnswer.length()-1);
        return finalAnswer;
    }

    /**
     * Traverses through the grid and tallies all the grass Squares that have been cut.
     * @return Latest count of cut grass squares
     */
    private int updateCurrentNumberCut(){
        int counter = 0;
        for (int i = 0; i < (width+2); i++){
            for (int j = 0; j < (height+2); j++){
                Square theSquare = theGrid[i][j];
//                if (theSquare.getClass().getName().equalsIgnoreCase("grass")){
                if (theSquare.getType() == SquareKind.EMPTY || theSquare.getType() == SquareKind.MOWER || theSquare.getType() == SquareKind.RECHARGE){
                    counter++;
                }
            }
        }
        return counter;
    }

    /**
     * Increments the turn number for this lawn.
     */
    public void incrementTurn(){
        currentTurnNumber = currentTurnNumber + 1;
        currentNumberCut = this.updateCurrentNumberCut();
    }

    /**
     * Signifies if the current number of completed turns has exceeded the par number (max number) of allowed turns for this lawn.
     * @return Whether number of completed turns has exceeded the par number of turns
     */
    public boolean exceededTurns(){
        boolean answer = true;
        if (currentTurnNumber <= MAX_PAR){
            answer = false;
        }
        return answer;
    }

    /**
     * Marks a square at a particular location XY as a recharge pad
     * @param x X coordinate of a square to be marked as recharge pad
     * @param y Y coordinate of a square to be marked as recharge pad
     */
    public void specifyRechargePads(int x, int y){
        Square sq = theGrid[x+1][y+1];
        sq.setRechargePad(true);
        sq.setType(SquareKind.RECHARGE);
    }

    public boolean isLocationRechargingPad(int x, int y){
        Square sq = theGrid[x+1][y+1];
        return sq.isRechargePad();
    }

    //Provide getters if the simulator system needs this info
    public Square getSquareAt(int x, int y){
        return theGrid[x+1][y+1];
    }

    public int getCurrentNumberCut() {
        currentNumberCut = this.updateCurrentNumberCut();
        return currentNumberCut;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getCurrentTurnNumber() {
        return currentTurnNumber;
    }

    public int getGopherPeriod() {
        return gopherPeriod;
    }

    public int getTotalSquare() {
    	int totalSq = width * height;
    	return totalSq;
    }

    public int getGrassesRemaining(){
        int answer = this.START_NUMBER_UNCUT - this.getCurrentNumberCut();
        return answer;
    }

    
}
