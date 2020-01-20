import java.util.ArrayList;

/**
 * Service class that checks and processes the mower actions.
 */
public class ActionProcessor {

    //Class variables
    private Lawn lawn;
    private ArrayList<Mower> mowers;
    private int[] mowerStateX;
    private int[] mowerStateY;
    private Direction[] mowerStateDirection;
    private boolean[] mowerStateRunning;
    private int[] mowerStateEnergy;
    private ArrayList<Gopher> gophers;

    /**
     * Constructor for this service.
     * @param lawn The lawn on which simulation is performed
     * @param mowers The mowers participating in the simulation
     * @param mowerStateX State information for all mowers (X coordiantes)
     * @param mowerStateY State information for all mowers (Y coordinates)
     * @param mowerStateDirection State information for all mowers (mower's direction)
     * @param mowerStateRunning State information for all mowers (mower's crashed / not crashed status)
     */
    public ActionProcessor(Lawn lawn, ArrayList<Mower> mowers, int[] mowerStateX, int[] mowerStateY, Direction[] mowerStateDirection, boolean[] mowerStateRunning, int[] mowerStateEnergy, ArrayList<Gopher> gophers) {
        this.lawn = lawn;
        this.mowers = mowers;
        this.mowerStateX = mowerStateX;
        this.mowerStateY = mowerStateY;
        this.mowerStateDirection = mowerStateDirection;
        this.mowerStateRunning = mowerStateRunning;
        this.mowerStateEnergy = mowerStateEnergy;
        this.gophers = gophers;
    }

    /**
     * Parse mower's requested action checking for validity. If invalid, mower state is updated to crashed status.
     * If requested action is valid, performs processing and updates state depending on the specific action.
     * @param mowerID Unique identifier of the mower making the action request
     * @param mowerRequestedAction The specific action that the mower is requesting to be processed
     */
    public Mower handleMowerAction(int mowerID, Action mowerRequestedAction){
        Mower currentMower = mowers.get(mowerID);
        int mowerX = mowerStateX[mowerID];
        int mowerY = mowerStateY[mowerID];
        Direction mowerDirection = mowerStateDirection[mowerID];
        if (lawn.isLocationRechargingPad(mowerX, mowerY)){
            mowerStateEnergy[mowerID] = lawn.MAX_MOWER_ENERGY;
            System.out.println("m" + mowerID + ": recharged (remaining energy: " + mowerStateEnergy[mowerID] + ")");
        }

        //Handle scenario where mower requested MOVE
        if (mowerRequestedAction == Action.MOVE){
            handleMowerMove(mowerID, currentMower, mowerX, mowerY);
        }
        //Handle scenario where mower requested STEER
        else if (mowerRequestedAction == Action.STEER){
            handleMowerSteer(mowerID, currentMower);
        }
        //Handle scenario where mower requested CSCAN
        else if (mowerRequestedAction == Action.CSCAN){
            //Request scan data from the lawn object
            String scanResults = lawn.cScanFrom(mowerX, mowerY);
            //Send resulting scan data to mower
            currentMower.receiveScanData(scanResults);
            currentMower.cscan();
            //Print output required at completion of each action
            String actionLineOne = "m" + mowerID + ",cscan";
            System.out.println(actionLineOne);
            System.out.println(scanResults);
            mowerStateEnergy[mowerID] = mowerStateEnergy[mowerID] - 1;
            System.out.println("m" + mowerID + " ok (remaining energy: " + mowerStateEnergy[mowerID] + ")");
        }
        //Handle scenario where mower requested LSCAN
        else if (mowerRequestedAction == Action.LSCAN){
            if (mowerStateEnergy[mowerID] >=3 ) {
                String scanResults = lawn.lScanFrom(mowerX, mowerY, mowerDirection);
                currentMower.receiveScanData(scanResults);
                currentMower.lscan();
                String actionLineOne = "m" + mowerID + ",lscan";
                System.out.println(actionLineOne);
                System.out.println(scanResults);
                mowerStateEnergy[mowerID] = mowerStateEnergy[mowerID] - 3;
                System.out.println("m" + mowerID + " ok (remaining energy: " + mowerStateEnergy[mowerID] + ")");
            }
            else{
                mowerStateRunning[mowerID] = false;
                System.out.println("out of energy");
            }
        }
        else if (mowerRequestedAction == Action.PASS){
            //Print output required at completion of each action
            String actionLineOne = "m" + mowerID + ",pass";
            System.out.println(actionLineOne);
            System.out.println("m" + mowerID + " ok (remaining energy: " + mowerStateEnergy[mowerID] + ")");
        }
        else{
            //Performed an invalid action and crash (disable) the mower
            mowerStateRunning[mowerID] = false;
            currentMower.setDisabled();
            String actionLineOne = "m" + mowerID;
            System.out.println(actionLineOne);
//            lawn.removeMowerWreck(mowerX, mowerY);
            System.out.println("m" + mowerID + ": crash");
        }
        return currentMower;
    }

    /**
     * Process the move action of a mower updating simulation state accordingly based on mower's pre-move state (location and direction).
     * @param mowerID Unique identifier of the mower in question.
     * @param currentMower Mower instance that wants to do the moving
     * @param mowerX X coordiante of mower's current / old location
     * @param mowerY Y coordiante of mower's current / old location
     */
    private void handleMowerMove(int mowerID, Mower currentMower, int mowerX, int mowerY){
        //Save the mower's current / old location
        int prevMowerX = mowerX;
        int prevMowerY = mowerY;

        //Get the direction that the mower is currently facing (not from mower itself but from the state tracker)
        Direction mowerCurrentDirection = mowerStateDirection[mowerID];
        
        //Update mowerX and mowerY based on the direction mower is facing
        switch (mowerCurrentDirection){
            case NORTH:
                mowerY++;
                break;
            case NORTHEAST:
                mowerX++;
                mowerY++;
                break;
            case EAST:
                mowerX++;
                break;
            case SOUTHEAST:
                mowerX++;
                mowerY--;
                break;
            case SOUTH:
                mowerY--;
                break;
            case SOUTHWEST:
                mowerX--;
                mowerY--;
                break;
            case WEST:
                mowerX--;
                break;
            case NORTHWEST:
                mowerX--;
                mowerY++;
                break;
            default:
                //Mower Direction invalid, update state tracker to reflect mower has crash
                currentMower.setDisabled();
                mowerStateRunning[mowerID] = false;
//                lawn.removeMowerWreck(prevMowerX, prevMowerY);
        }
        currentMower.move();
        //Update lawn that mower is no longer at its previous old location
        lawn.pickupMower(prevMowerX, prevMowerY);

        //Place mower on new square on lawn (square will disable mower if applicable)
        boolean mowerSuccessfullyPlaced = lawn.placeMower(currentMower, mowerX, mowerY);

        //At new square, check for collision with other mowers
        boolean mowerNoCollision = checkNoCollision(mowerID, mowerX, mowerY);

        //Print first line about action to output
        String actionLineOne = "m" + mowerID + ",move";
        System.out.println(actionLineOne);

        //Check if mower has survived and print ok
        if (mowerSuccessfullyPlaced && mowerNoCollision && mowerStateEnergy[mowerID] >= 2){
            mowerStateX[mowerID] = mowerX;
            mowerStateY[mowerID] = mowerY;
            if (lawn.isLocationRechargingPad(mowerX, mowerY)){
                mowerStateEnergy[mowerID] = lawn.MAX_MOWER_ENERGY;
                System.out.println("m" + mowerID + ": ok and recharged (remaining energy: " + mowerStateEnergy[mowerID] + ")");
            }
            else{
                mowerStateEnergy[mowerID] = mowerStateEnergy[mowerID] - 2;
                System.out.println("m" + mowerID + ": ok (remaining energy: " + mowerStateEnergy[mowerID] + ")");
            }
        }
        //Print crash and update mower state if it has not survived and crashed
        else{
            mowerStateRunning[mowerID] = false;
            if (mowerStateEnergy[mowerID] < 2){
                System.out.println("m" + mowerID + ": out of energy");
            }
            else {
//                lawn.removeMowerWreck(prevMowerX, prevMowerY);
                System.out.println("m" + mowerID + ": crash");
            }
        }
    }

    /**
     * Checks whether a given mower's location is occupied by any other mower in the simulation.
     * @param movingMowerID The given mower at a given location
     * @param movingMowerX X coordinate of the location the given mower is occupying
     * @param movingMowerY Y coordinate of the location the given mower is occupying
     * @return True if mower is safe and no collision occurred
     */
    private boolean checkNoCollision(int movingMowerID, int movingMowerX, int movingMowerY){
        boolean answer = true;
        for (int i = 0; i < mowerStateRunning.length; i++){
            if (mowerStateRunning[i]) {
                int anotherMowerX = mowerStateX[i];
                int anotherMowerY = mowerStateY[i];
                //Check all other mowers except for current mower
                if (i != movingMowerID) {
                    //Only when X and Y both overlap between both mowers did a collision occur
                    if (anotherMowerX == movingMowerX && anotherMowerY == movingMowerY) {
                        //Remove the peer mower from simulation (this mower will be removed when method returns)
                        mowerStateRunning[i] = false;
                        lawn.removeMowerWreck(anotherMowerX, anotherMowerY);
                        mowers.get(i).setDisabled();
                        answer = false;
                        break;
                    }
                }
            }
        }
        return answer;
    }

    /**
     * Process the steer action of a mower by polling the mower for its desired direction.
     * Checks that the received direction is valid. If invalid, makes the mower as crashed.
     * If valid, updates the simulation state and informs the mower of its newly successfully set direction.
     * @param mowerID Unique identifier of mower requesting the steer to new direction
     * @param currentMower Mower object that contains the requested direction
     */
    private void handleMowerSteer(int mowerID, Mower currentMower){
        //Poll mower for its desired direction
        Direction mowerDesiredNewDirection = currentMower.desiredNextDirection();

        //Check that the received direction from mower is valid
        if (mowerDesiredNewDirection == Direction.NORTH ||
                mowerDesiredNewDirection == Direction.NORTHEAST ||
                mowerDesiredNewDirection == Direction.EAST ||
                mowerDesiredNewDirection == Direction.SOUTHEAST ||
                mowerDesiredNewDirection == Direction.SOUTH ||
                mowerDesiredNewDirection == Direction.SOUTHWEST ||
                mowerDesiredNewDirection == Direction.WEST ||
                mowerDesiredNewDirection == Direction.NORTHWEST){

            //Update this mower's state in the simulation data
            mowerStateDirection[mowerID] = mowerDesiredNewDirection;

            //Inform the mower of its newly set direction
            currentMower.setCurrentDirection(mowerDesiredNewDirection);

            //Print output required at completion of each action
            mowerStateEnergy[mowerID] = mowerStateEnergy[mowerID] - 1;
            String actionLineOne = "m" + mowerID + ",steer," + mowerDesiredNewDirection.name().toLowerCase();
            System.out.println(actionLineOne);
            System.out.println("m" + mowerID + " ok (remaining energy: " + mowerStateEnergy[mowerID] + ")");
        }
        else{
            //Received an invalid direction from mower, crash and remove it from simulation
            mowerStateRunning[mowerID] = false;
//            lawn.removeMowerWreck(mowerStateX[mowerID], mowerStateY[mowerID]);
            String actionLineOne = "m" + mowerID + ",steer,invalid direction";
            System.out.println(actionLineOne);
            System.out.println("m" + mowerID + ": crash");
            currentMower.setDisabled();
        }
    }

    /**
     * Parse gopher's requested move checking for validity. If invalid, keeps gopher at previous location.
     * If requested location is valid, performs processing and updates state on lawn reflecting gopher's new location.
     * @param gopherID The ID of gopher wanting to move
     * @param gopherDesiredX The gopher's new location X
     * @param gopherDesiredY The gopher's new location Y
     * @return The gopher instance which moved
     */
    public Gopher handleGopherMove(int gopherID, int gopherDesiredX, int gopherDesiredY){
        Gopher g = gophers.get(gopherID);
        boolean gopherSuccessfullyMoved = lawn.moveGopher(g.getCurrentX(), g.getCurrentY(), gopherDesiredX, gopherDesiredY);
        if (gopherSuccessfullyMoved){
            // Update gopher's current X and Y
            g.setCurrentX(gopherDesiredX);
            g.setCurrentY(gopherDesiredY);
            System.out.println("G" + gopherID + " moved ");
            // Check to see if this gopher got a mower
            for (int i = 0; i < mowerStateRunning.length; i++) {
                if (mowerStateRunning[i]) {
                    int mowerX = mowerStateX[i];
                    int mowerY = mowerStateY[i];
                    if (mowerX == gopherDesiredX && mowerY == gopherDesiredY) {
                        //Got one! Remove the mower from simulation (this mower will be removed when method returns)
                        mowerStateRunning[i] = false;
                        lawn.removeMowerWreck(mowerX, mowerY);
                        mowers.get(i).setDisabled();
                        System.out.println("    G" + gopherID + " got M" + i);
                        break;
                    }
                }
            }
        }
        return g;
    }

    //Getters to send updated state information out back to simulator system
    public Lawn getLawn() {
        return lawn;
    }

    public ArrayList<Mower> getMowers() {
        return mowers;
    }

    public int[] getMowerStateX() {
        return mowerStateX;
    }

    public int[] getMowerStateY() {
        return mowerStateY;
    }

    public Direction[] getMowerStateDirection() {
        return mowerStateDirection;
    }

    public boolean[] getMowerStateRunning() {
        return mowerStateRunning;
    }

    public int[] getMowerStateEnergy() {
        return mowerStateEnergy;
    }

    public ArrayList<Gopher> getGophers() { return gophers; }
}
