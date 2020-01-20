
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Stack;
import java.util.*;

/**
 * Service class that provides the mower with a strategy and decision on what the next action should be and where to steer
 */
public class MowerStrategy {

    //Class variables
    private static Direction currentDirection;
    private static boolean isDisabled;
    private static ArrayList<String> lastScanResults = new ArrayList<>();
    private static int LscanDirection;
    private static Stack<Action> previousActionsStack = new Stack<>();
    private static int currentMowerID;
    
    private HashMap<String, String> map;
    private HashMap<String, String> charger_location;
    private int mowerRelative_x;
    private int mowerRelative_y;
    private int mowerAbsolute_x;
    private int mowerAbsolute_y;
    private int mower_x;
    private int mower_y;
    private int energyTracker;
    private int fullEnergyAtCreation;
    
//    private Action desiredAction;
    private Direction desiredDirection;

    /**
     * Default constructor
     */
    public MowerStrategy(int fullEnergyAtCreation){
        this.fullEnergyAtCreation = fullEnergyAtCreation;
    }

    public Action NextAction(){
        
        Action answer;
        answer = Action.PASS;
        String object;
        int mowerThinksTurn;
        Double energy = new Double(energyTracker);
        
        mowerThinksTurn = previousActionsStack.size();
        if (mowerThinksTurn == 0){
            answer = Action.CSCAN;
            return answer;//CSCAN first
        }
        else if (energy/fullEnergyAtCreation <= 0.3){
            if (previousActionsStack.peek() == Action.LSCAN){
                answer = Action.CSCAN;
                return answer;
            }
            else if (previousActionsStack.peek() == Action.CSCAN){
                desiredDirection = NextDirection();
                if (desiredDirection == currentDirection){
                    answer = Action.MOVE;
                    return answer;
                }
                else {
                    answer = Action.STEER;
                    return answer;
                }
            }
            else if(previousActionsStack.peek() == Action.STEER){
                answer = Action.MOVE;
                return answer;
            }
            else if(previousActionsStack.peek() == Action.MOVE){
                answer = Action.CSCAN;
                return answer;
            }
        }
        else{
            if (previousActionsStack.peek() == Action.LSCAN){
                answer = Action.CSCAN;
                return answer;
            }
            else if (previousActionsStack.peek() == Action.CSCAN){
                desiredDirection = NextDirection();
                if (desiredDirection == currentDirection){
                    answer = Action.MOVE;
                    return answer;
                }
                else {
                    answer = Action.STEER;
                    return answer;
                }
            }
            else if(previousActionsStack.peek() == Action.STEER){
                answer = Action.MOVE;
                return answer;
            }
            else if(previousActionsStack.peek() == Action.MOVE){
                desiredDirection = NextDirection();
                answer = Action.CSCAN;
                return answer;
            }
        }
        return answer;

    }
    

    public Direction NextDirection() {
        Double energy = new Double(energyTracker);
        
        int new_direction = Utility.dir_Utility(currentDirection);
        int score = 0;
        
        int offsetX_1 = Utility.xDIR_MAP.get(currentDirection);
        int offsetY_1 = Utility.yDIR_MAP.get(currentDirection);
        int checkX_1 = mowerRelative_x + offsetX_1;
        int checkY_1 = mowerRelative_y + offsetY_1;
        int kk = new_direction;
        String scan_object_1 = lastScanResults.get(kk);
        
        int grass_distance_1 = 0;
        if (scan_object_1.equals("grass")){
            grass_distance_1 = 1;
        }
        
        int gopher_distance_1 = 100000;
        for (Map.Entry<String, String> me : map.entrySet()) {
            
            String coord = me.getKey();
            String object = me.getValue();
            int[] coord_int = coordinator_to_int(coord);
            
            int x_dis = checkX_1 - coord_int[0];
            int y_dis = checkY_1 - coord_int[1];
            
            int ind_gopher = 0;
            if (object.equals("gopher_grass")||object.equals("gopher_empty")||object.equals("gopher")) {
                
                ind_gopher = Math.min(Math.abs(x_dis),Math.abs(y_dis)); //measure gopher distance.
                
            }
            
            if (ind_gopher < gopher_distance_1){
                gopher_distance_1 = ind_gopher;
            }
            
        }
        
        score = grass_distance_1 + gopher_distance_1;
        
        if (energy/fullEnergyAtCreation <= 0.3){
            
            int recharge_score = 100000;
            for (Map.Entry<String, String> me : map.entrySet()) {
                
                String coord = me.getKey();
                String object = me.getValue();
                int[] coord_int = coordinator_to_int(coord);
                
                int x_dis = checkX_1 - coord_int[0];
                int y_dis = checkY_1 - coord_int[1];
                
                int ind_recharge = 100000;
                if (object.equals("recharge")) {
                    
                    ind_recharge = Math.min(Math.abs(x_dis),Math.abs(y_dis));
                    
                }
                
                if (ind_recharge < recharge_score){
                    recharge_score = ind_recharge;
                }
                
            }
            
            score = - recharge_score;
            
        }
        
        for (int k: Utility.SCAN_LIST) {
            int score_k = 0;
            Direction lookThisWay = Utility.ORIENT_LIST[k];
            int offsetX = Utility.xDIR_MAP.get(lookThisWay);
            int offsetY = Utility.yDIR_MAP.get(lookThisWay);
            int checkX = mowerRelative_x + offsetX;
            int checkY = mowerRelative_y + offsetY;
            String scan_object = lastScanResults.get(k);
            
            if (scan_object.equals("gopher_grass")||scan_object.equals("gopher_empty")||scan_object.equals("fence")||scan_object.equals("mower")){
                continue;
            }
            
            int grass_distance = 0;
            if (scan_object.equals("grass")){
                grass_distance = 1;
            }
            
            int gopher_distance = 100000;
            for (Map.Entry<String, String> me : map.entrySet()) {
                
                String coord = me.getKey();
                String object = me.getValue();
                int[] coord_int = coordinator_to_int(coord);
                
                int x_dis = checkX - coord_int[0];
                int y_dis = checkY - coord_int[1];
                
                int ind_gopher = 0;
                if (object.equals("gopher_grass")||object.equals("gopher_empty")||object.equals("gopher")) {
                    
                    ind_gopher = Math.min(Math.abs(x_dis),Math.abs(y_dis)); //measure gopher distance.
                    
                }
                
                if (ind_gopher < gopher_distance){
                    gopher_distance = ind_gopher;
                }
                
            }

            
            
            score_k = grass_distance + gopher_distance;
            
            if (energy/fullEnergyAtCreation <= 0.3){
                
                int recharge_score = 100000;
                for (Map.Entry<String, String> me : map.entrySet()) {
                    
                    String coord = me.getKey();
                    String object = me.getValue();
                    int[] coord_int = coordinator_to_int(coord);
                    
                    int x_dis = checkX - coord_int[0];
                    int y_dis = checkY - coord_int[1];
                    
                    int ind_recharge = 100000;
                    if (object.equals("recharge")) {
                        
                        ind_recharge = Math.min(Math.abs(x_dis),Math.abs(y_dis));
                        
                    }
                    
                    if (ind_recharge < recharge_score){
                        recharge_score = ind_recharge;
                    }
                    
                }
                
                score_k = gopher_distance - recharge_score;
    
            }
            
            if (score < score_k){
                score = score_k;
                new_direction = k;
            }
            
            
        }

        return Utility.ORIENT_LIST[new_direction];
        
    }
            
    /**
     * Get an action selected based on randomness.
     * @return Action that was selected randomly
     */
    public Action chooseRandomAction(){
        Action answer;
        Random rand = new Random();
        int randomIntegerAction = rand.nextInt(100);
        if (randomIntegerAction < 10) {
            answer = Action.PASS;
        }
        else if (randomIntegerAction < 25) {
            answer = Action.LSCAN;
        }
        else if (randomIntegerAction < 45) {
            answer = Action.CSCAN;
        }
        else if (randomIntegerAction < 60) {
            answer = Action.STEER;
            desiredDirection = this.chooseRandomDirection();
        }
        else {
            answer = Action.MOVE;
        }
//        desiredAction = answer;
        return answer;
    }

    /**
     * For the scenario where the mower wants to use a very conservative strategy (i.e. when there are multiple mowers around), a very conservative strategy where each move is preceded by a scan to significantly reduce the probability of mowers colliding.
     * @return Action that was selected (scanning every other action) and the action based on the scan results
     */
    public Action conservativeNextAction(){
//        return Action.PASS;
        Action answer;
        int mowerThinksTurn;
        if (currentMowerID % 2 == 0) {
            mowerThinksTurn = previousActionsStack.size();
        }
        else {
            mowerThinksTurn = previousActionsStack.size() + 1;
        }
        //First action should always be Scan
        if (mowerThinksTurn == 0){
            answer = Action.CSCAN;
//            desiredAction = answer;
            return answer;
        }

        //Shift so that only odd mowers scan a second time
        if (((mowerThinksTurn == 1)) && (currentMowerID % 2 == 1)){
            answer = Action.CSCAN;
//            desiredAction = answer;
            return answer;
        }

        //Then scan on every other action (on even actions so 0th, 2nd, 4th, 6th, etc.)
        if ((mowerThinksTurn % 2) == 0){
            answer = Action.CSCAN;
//            desiredAction = answer;
            return answer;
        }
        //Then act on the odd actions (either move if already facing correct direction or steer if obsticle ahead)
        else if ((mowerThinksTurn % 2) == 1 && previousActionsStack.peek() == Action.CSCAN){
            ArrayList<Integer> scanScoring = new ArrayList<>();
            for (int i = 0; i < lastScanResults.size(); i++){
                scanScoring.add(0);
            }
            for (int i = 0; i < lastScanResults.size(); i++){
                String squareKind = lastScanResults.get(i);
                if (squareKind.equalsIgnoreCase("grass")){
                    scanScoring.set(i, 10);
                }
                else if (squareKind.equalsIgnoreCase("empty")){
                    scanScoring.set(i, 0);
                }
                else if (squareKind.equalsIgnoreCase("crater")){
                    scanScoring.set(i, -5);
                }
                else if (squareKind.equalsIgnoreCase("fence")){
                    scanScoring.set(i, -7);
                }
                else if (squareKind.equalsIgnoreCase("mower")){
                    scanScoring.set(i, -10);
                }
                else {
                    scanScoring.set(i, 0);
                }
            }

            int currentDirectionIndex = convertDirectionToIndex(currentDirection);
            int currentFacingScore = scanScoring.get(currentDirectionIndex);

            //Square in front of current direction is grass (and traversable), then go ahead and move to it
            if (currentFacingScore > 0){
                answer = Action.MOVE;
//                desiredAction = answer;
                return answer;
            }
            //Square in front of current direction is not traversable, need to steer but be smart about steering
            else if (currentFacingScore < 0){
                answer = Action.STEER;
//                desiredAction = answer;
                desiredDirection = conservativeNextDirection(currentFacingScore, scanScoring);
                if (desiredDirection == null){
                    //No place to steer, return pass instead
                    answer = Action.PASS;
//                    desiredAction = answer;
                }
                return answer;
            }
            //Square in front of current direction is traversable but not grass, can move to it if really need to but look around for better option
            else {

                //Check the 8 squares around for the highest score
                int maxScore = Collections.max(scanScoring);
                int maxScoreIndex = scanScoring.indexOf(maxScore);

                //If current empty is the highest score (we are surrounded), then move to it
                if (maxScore == 0 && maxScoreIndex == currentDirectionIndex){
                    answer = Action.MOVE;
//                    desiredAction = answer;
                    return answer;
                }
                else {
                    answer = Action.STEER;
                    desiredDirection = conservativeNextDirection(currentFacingScore, scanScoring);
//                    desiredAction = answer;
                    return answer;
                }
            }
        }

        //If no information, default fail into safe state of ppass
        else{
            return Action.PASS;
        }
    }

    /**
     * Get a randomly selected direction.
     * @return
     */
    private Direction chooseRandomDirection(){
        Random rand = new Random();
        int randomIntegerDirection = rand.nextInt(8);
        return convertIndexToDirection(randomIntegerDirection);
    }

    /**
     * Select a direction based on where the mower is currently facing and avoids obsticles based on the results of its most recent scan.
     * @param currentFacingScore Indicates the kind of square in the mower's current facing direction
     * @param scanScoring Results from scanning the surrounding 8 squares indexed by direction
     * @return
     */
    private Direction conservativeNextDirection(int currentFacingScore, ArrayList<Integer> scanScoring) {
        int answerIndex;
        int currentDirectionIndex = convertDirectionToIndex(currentDirection);
        ArrayList<Integer> allowedDirections = new ArrayList<>();

        //Facing another mower, most critical that we don't steer badly (allowed to steer two to right only)
        if (currentFacingScore <= -10){
            for (int i = 0; i < scanScoring.size(); i++){
                if (!( (i==(currentDirectionIndex%scanScoring.size())) ||
                        (i==(currentDirectionIndex-1)%scanScoring.size()) ||
                        (i==(currentDirectionIndex-2)%scanScoring.size()) ||
                        (i==(currentDirectionIndex+1)%scanScoring.size()) ||
                        (i==(currentDirectionIndex+2)%scanScoring.size())
                )) {
                    if (scanScoring.get(i) == 0) {
                        //Put the allowed square's direction into a list for random selection
                        allowedDirections.add(i);
                    }
                    else if (scanScoring.get(i) > 0){
                        //Put more copies of the allowed square's direction based on that square's priority scoring
                        for (int j = 0; j < scanScoring.get(i); j++) {
                            allowedDirections.add(i);
                        }
                    }
                    if ((i==((currentMowerID*4))%8) ||
                            (i==((currentMowerID*4)+1)%8) ||
                            (i==((currentMowerID*4)+2)%8) ||
                            (i==((currentMowerID*4)+3)%8)
                    ) {
                        if (scanScoring.get(i) >= 0){
                            allowedDirections.add(i);
                            allowedDirections.add(i);
                            allowedDirections.add(i);
                        }
                    }
                }
            }
        }
        //Facing a fence or crater or some other non-traversable square, chose direction from remaining allowed square
        else if (currentFacingScore <= 0){

            for (int i = 0; i < scanScoring.size(); i++){
                if (scanScoring.get(i) == 0) {
                    //Put the allowed square's direction into a list for random selection
                    allowedDirections.add(i);
                }
                else if (scanScoring.get(i) > 0){
                    //Put more copies of the allowed square's direction based on that square's priority scoring
                    for (int j = 0; j < scanScoring.get(i); j++) {
                        allowedDirections.add(i);
                    }
                }
            }
        }

        //Randomly select from the remaining allowed squares
        if (allowedDirections.size() > 0) {
            answerIndex = randomlySelectOneAllowedDirection(allowedDirections);
        }
        else {
            //No other square is safe
            answerIndex = -1;
        }

        if (answerIndex <= -1) {
            return null;
        }
        else {
            return convertIndexToDirection(answerIndex);
        }
    }

    /**
     * Chooses one of the directions from its input list randomly.
     * @param allowedDirections List of directions that are all allowed from which to choose one
     * @return A chosen direction's index
     */
    private int randomlySelectOneAllowedDirection(ArrayList<Integer> allowedDirections){
        Random rand = new Random();
        int randomlySelectedOneDirection = rand.nextInt(allowedDirections.size());
        return allowedDirections.get(randomlySelectedOneDirection);
    }

    /**
     * Converts a direction to an index value for easier usage in the other methods. Index begins at 0 for NORTH and proceeds in clockwise fashion till 7 for NORTHWEST.
     * @param currentDirection Direction in enumeration
     * @return Index in int corresponding to the input direction
     */
    private int convertDirectionToIndex(Direction currentDirection){
        int answer = 0;
        switch (currentDirection) {
            case NORTH:
                answer = 0;
                break;
            case NORTHEAST:
                answer = 1;
                break;
            case EAST:
                answer = 2;
                break;
            case SOUTHEAST:
                answer = 3;
                break;
            case SOUTH:
                answer = 4;
                break;
            case SOUTHWEST:
                answer = 5;
                break;
            case WEST:
                answer = 6;
                break;
            case NORTHWEST:
                answer = 7;
                break;
        }
        return answer;
    }

    /**
     * Converts a direction index back to direction enum for easier usage in other methods. Index begins at 0 for NORTH and proceeds in clockwise fashion till 7 for NORTHWEST.
     * @param index Direction index to be converted
     * @return The direction in enumeration
     */
    private Direction convertIndexToDirection(int index){
        Direction answer = null;
        switch (index) {
            case 0:
                answer = Direction.NORTH;
                break;
            case 1:
                answer = Direction.NORTHEAST;
                break;
            case 2:
                answer = Direction.EAST;
                break;
            case 3:
                answer = Direction.SOUTHEAST;
                break;
            case 4:
                answer = Direction.SOUTH;
                break;
            case 5:
                answer = Direction.SOUTHWEST;
                break;
            case 6:
                answer = Direction.WEST;
                break;
            case 7:
                answer = Direction.NORTHWEST;
                break;
        }
        return answer;
    }

    public boolean reachedRecharging(){
        return false;
    }

    //Setters
    public void setCurrentDirection(Direction currentDirection) {
        MowerStrategy.currentDirection = currentDirection;
    }

    public void setDisabled(boolean disabled) {
        MowerStrategy.isDisabled = disabled;
    }

    public void setLastScanResults(ArrayList<String> lastScanResults) {
        MowerStrategy.lastScanResults = lastScanResults;
    }

    public void setPreviousActionsStack(Stack<Action> previousActionsStack) {
        MowerStrategy.previousActionsStack = previousActionsStack;
    }

    public void setCurrentMowerID(int currentMowerID) {
        MowerStrategy.currentMowerID = currentMowerID;
    }

    //Getters
//    public Action getDesiredAction() {
//        return desiredAction;
//    }
    public static int[] coordinator_to_int(String coordinator) {
        String[] string_array = coordinator.split(",");
        int[] int_array = new int[string_array.length];
        for(int i = 0; i < string_array.length; i++) {
            int_array[i] = Integer.parseInt(string_array[i]);
        }
        return int_array;
    }
            
    public Direction getDesiredDirection() {
        return desiredDirection;
    }
    
    public void set_mower_relative_x(int mowerRelative_x) {
        this.mowerRelative_x = mowerRelative_x;
    }
    
    public void set_mower_relative_y(int mowerRelative_y) {
        this.mowerRelative_y = mowerRelative_y;
    }
    
    public void set_mower_absolute_x(int mowerAbsolute_x) {
        this.mowerAbsolute_x = mowerAbsolute_x;;
    }
    
    public void set_mower_absolute_y(int mowerAbsolute_y) {
        this.mowerAbsolute_y = mowerAbsolute_y;;
    }
    
    public void set_mower_x(int mower_x) {
        this.mower_x = mower_x;;
    }
    
    public void set_mower_y(int mower_y) {
        this.mower_y = mower_y;;
    }
    
    public void set_map(HashMap<String, String> map) {
        this.map = map;
    }
    
    public void set_charger_location(HashMap<String, String> charger_location) {
        this.charger_location = charger_location;
    }

    public void set_energyTracker(int energyTracker) {
        this.energyTracker = energyTracker;
    }
    public void set_LscanDirection(int LscanDirection){
        this.LscanDirection = LscanDirection;
    }

}
