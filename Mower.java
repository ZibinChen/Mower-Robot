import java.util.ArrayList;
import java.util.Stack;
import java.util.*;

/**
 * A class representing a mower on a lawn
 */
public class Mower {

    //Class variables
    private int mowerID;
    private Direction currentDirection;
    private int LscanDirection;
    private boolean isDisabled;
    private ArrayList<String> lastScanResults = new ArrayList<>();
    private ArrayList<String> lastLineScanResults = new ArrayList<>();
    private Stack<Action> previousActionsStack = new Stack<>();
    private int strategy;
    private MowerStrategy ms;
    private int energyTracker;
    private int fullEnergyAtCreation;
    
    private int mowerRelative_x;
    private int mowerRelative_y;
    private int mowerAbsolute_x;
    private int mowerAbsolute_y;
    private HashMap<String, String> local_map;
    private HashMap<String, String> global_map;
    private HashMap<String, String> battery_global_map;
    
    public int get_mower_relative_x() {
        return mowerRelative_x;
    }

    public int get_mower_relative__y() {
        return mowerRelative_y;
    }

    public int get_mower_absolute_x() {
        return mowerAbsolute_x;
    }

    public int get_mower_absolute_y() {
        return mowerAbsolute_y;
    }
    
    public Direction get_mower_direction() {
        return currentDirection;
    }
    
    public HashMap<String, String> get_mower_global_map() {
        return global_map;
    }
    
    public void set_mower_global_map(HashMap<String, String> map) {
        this.global_map = map;
    }
    
    public void set_battery_global_map(HashMap<String, String> map) {
        this.battery_global_map = map;
    }
    
    /**
     * Constructor that sets Mower ID, x and y coordinate, initial direction, and mower's strategy
     * @param mowerID Unique identifier associated to each mower
     * @param currentDirection Direction that the mower is currently facing
     * @param strategy Algorithm the mower should use to mow the lawn
     */
    public Mower(int mowerID, Direction currentDirection, int strategy, int energyAtCreation){
        this.mowerID = mowerID;
        this.currentDirection = currentDirection;
        this.strategy = strategy;
        this.energyTracker = energyAtCreation;
        this.fullEnergyAtCreation = energyAtCreation;
        this.isDisabled = false;
        
        this.mowerRelative_x = 0;
        this.mowerRelative_y = 0;
        this.mowerAbsolute_x = -1;
        this.mowerAbsolute_y = -1;
        this.LscanDirection = 0;
        this.local_map = new HashMap<>();
        this.global_map = new HashMap<>();
        
        this.battery_global_map = new HashMap<>();
        this.battery_global_map.put(coordinator_to_string(0, 0), "charger");
        Utility.createUtility();
        //Use a strategy service class to handle different types of strategies such as randomly choosing actions, intelligently choosing actions, etc.
        //Can be extensible for future where multiple strategies need to be supported or different mowers need to incorporate different strategies.
        //TODO: Add MowerStrategy class to UML Class Diagram
        ms = new MowerStrategy(energyAtCreation);
    }

    /**
     * The mower will output an action representing its desired action each time this method is called.
     * @return One discrete desired action of this mower
     */
    public Action desiredNextAction(){
        Action answer = null;

        //Provide strategy service all the latest information this mower knows
        ms.setCurrentDirection(currentDirection);
        ms.setDisabled(isDisabled);
        ms.setLastScanResults(lastScanResults);
        ms.setPreviousActionsStack(previousActionsStack);
        ms.setCurrentMowerID(mowerID);
        ms.set_mower_relative_x(mowerRelative_x);
        ms.set_mower_relative_y(mowerRelative_y);
        ms.set_mower_absolute_x(mowerAbsolute_x);
        ms.set_mower_absolute_x(mowerAbsolute_y);
        ms.set_energyTracker(energyTracker);
        ms.set_charger_location(battery_global_map);
        ms.set_LscanDirection(LscanDirection);
        
        if((mowerAbsolute_x > -1)&(mowerAbsolute_y > -1)){
            ms.set_mower_x(mowerAbsolute_x);
            ms.set_mower_y(mowerAbsolute_y); 
            ms.set_map(global_map);
        }
        else{
            ms.set_mower_x(mowerRelative_x);
            ms.set_mower_y(mowerRelative_y);
            ms.set_map(local_map);
        }
        

        //Depending on mower strategy, request the strategy service to output an action using desired strategy
        if (strategy == 0){
            answer = ms.chooseRandomAction();
        }
        else if (strategy == 1){
            answer = ms.NextAction();
            if (answer == Action.MOVE){
                if (ms.reachedRecharging()){
                    energyTracker = fullEnergyAtCreation;
                }
                else {
                    energyTracker = energyTracker - 2;
                }
            }
            else if (answer == Action.STEER || answer == Action.CSCAN){
                energyTracker = energyTracker - 1;
            }
            else if (answer == Action.LSCAN){
                energyTracker = energyTracker - 3;

        }
//            answer = ms.chooseRandomAction();
        }
        previousActionsStack.add(answer);
        return answer;
    }

    /**
     * The mower will output a direction representing its desired steering direction each time this method is called.
     * @return One discrete desired direction of this mower
     */
    public Direction desiredNextDirection() {
        //Get the mower strategy service's calculated direction
        return ms.getDesiredDirection();
    }

    /**
     * Ingest the results from a scan action which is a should contain information about the area surrounding the mower.
     * @param eightScannedSquares String containing information about the surrounding 8 squares around this mower
     */
    public void receiveScanData(String eightScannedSquares){
        lastScanResults.clear();
        String[] tokens = eightScannedSquares.split(",");
        for (int i = 0; i < tokens.length; i++){
            lastScanResults.add(tokens[i]);
        }
        if (previousActionsStack.peek() == Action.LSCAN){
            lastLineScanResults = lastScanResults;
            lastScanResults.clear();
        }
    }

    /**
     * Informs the mower that it has been disabled only for notifying the mower of this status change. Does not influence mower behavior.
     */
    public void setDisabled(){
        isDisabled = true;
    }

    /**
     * Informs the mower that its direction has been successfully changed to a new direction on the last steer action.
     * @param newDirection Direction that the mower has been successfully changed to
     */
    public void setCurrentDirection(Direction newDirection){
        currentDirection = newDirection;
    }
    
    
    public void move() {
        // update the mower's local location
        
        String coordinator_1 = coordinator_to_string(mowerRelative_x, mowerRelative_y);
        local_map.put(coordinator_1, "empty");
        
        mowerRelative_x += Utility.xDIR_MAP.get(currentDirection);
        mowerRelative_y += Utility.yDIR_MAP.get(currentDirection);
        
        if (isDisabled = false){
            String coordinator_2 = coordinator_to_string(mowerRelative_x, mowerRelative_y);
            local_map.put(coordinator_2, "mower");
        }
        
        if ((mowerAbsolute_x > -1)&(mowerAbsolute_y > -1)){
            
            String coordinator_3 = coordinator_to_string(mowerAbsolute_x, mowerAbsolute_y);
            global_map.put(coordinator_3, "empty");
            mowerAbsolute_x += Utility.xDIR_MAP.get(currentDirection);
            mowerAbsolute_y += Utility.yDIR_MAP.get(currentDirection);
            if (isDisabled = false){
                String coordinator_4 = coordinator_to_string(mowerAbsolute_x, mowerAbsolute_y);
                global_map.put(coordinator_4, "mower");
            }
        }
    }
    
    // get cscan values, update map
    public void cscan() {
        
        for (int k: Utility.SCAN_LIST) {
            
            Direction lookThisWay = Utility.ORIENT_LIST[k];
            int offsetX = Utility.xDIR_MAP.get(lookThisWay);
            int offsetY = Utility.yDIR_MAP.get(lookThisWay);
            int checkX = mowerRelative_x + offsetX;
            int checkY = mowerRelative_y + offsetY;
            
            String coordinator = coordinator_to_string(checkX, checkY);
            
            local_map.put(coordinator, lastScanResults.get(k));
            
            if ((mowerAbsolute_x > -1)&(mowerAbsolute_y > -1)){
                int checkX_1 = mowerAbsolute_x + offsetX;
                int checkY_1 = mowerAbsolute_y + offsetY;
                String coordinator_1 = coordinator_to_string(checkX_1, checkY_1);
                
                global_map.put(coordinator_1, lastScanResults.get(k));
            }
            
        }
    }
    
    
    // get lscan values, update map
    public void lscan() {

        for (int k = 0; k < lastLineScanResults.size(); k++) {
            
            int offsetX = Utility.xDIR_MAP.get(LscanDirection);
            int offsetY = Utility.yDIR_MAP.get(LscanDirection);
            int checkX = mowerRelative_x + (k+1)*offsetX;
            int checkY = mowerRelative_y + (k+1)*offsetY;
            
            String coordinator = coordinator_to_string(checkX, checkY);
            
            local_map.put(coordinator, lastLineScanResults.get(k));
            
            if ((mowerAbsolute_x > -1)&(mowerAbsolute_y > -1)){
                int checkX_1 = mowerAbsolute_x + (k+1)*offsetX;
                int checkY_1 = mowerAbsolute_y + (k+1)*offsetY;
                String coordinator_1 = coordinator_to_string(checkX_1, checkY_1);
                global_map.put(coordinator_1, lastLineScanResults.get(k));
            }
            
        }
        LscanDirection = Math.max(LscanDirection+2, 6);
    }
    
    // integer
    public String coordinator_to_string(int x, int y){
        return Integer.toString(x) + ',' + Integer.toString(y);
    }

}
