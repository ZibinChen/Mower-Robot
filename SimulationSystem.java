import java.util.ArrayList;

/**
 * Main controlling class of the simulation system that runs, manages, and monitors the simulation.
 */
public class SimulationSystem {

    //Class variables
    private Lawn lawn;
    private ArrayList<Mower> mowers;
    private ArrayList<Gopher> gophers;
    private int[] mowerStateX;
    private int[] mowerStateY;
    private Direction[] mowerStateDirection;
    private boolean[] mowerStateRunning;
    private int[] mowerStateEnergy;
    private boolean isVerbose;
    private boolean stopButtonPressed;
    private String nextObjectPolled;

    /**
     * Constructor of the simulation system.
     * @param lawn Lawn on which the simulation will run
     * @param ms List of Mowers which will operate on the lawn in the simulation
     * @param mowerStateTracker Tracker used by the simulation system to maintain state of each mower
     * @param verbose Flag to print additional debugging information
     */
    public SimulationSystem(Lawn lawn, ArrayList<Mower> ms, ArrayList<String> mowerStateTracker, ArrayList<Gopher> gs, boolean verbose){
        this.lawn = lawn;
        this.mowers = ms;
        this.gophers = gs;
        mowerStateX = new int[mowerStateTracker.size()];
        mowerStateY = new int[mowerStateTracker.size()];
        mowerStateDirection = new Direction[mowerStateTracker.size()];
        mowerStateRunning = new boolean[mowerStateTracker.size()];
        mowerStateEnergy = new int[mowerStateTracker.size()];
        isVerbose = verbose;
        //Break state tracker up four smaller trackers (one for each critical mower variable: x, y, direction, and disabled status)
        for (int i = 0; i < mowerStateTracker.size(); i++){
            String currentMowerState = mowerStateTracker.get(i);
            String[] tokens = currentMowerState.split(",");
            mowerStateX[i] = Integer.parseInt(tokens[0]);
            mowerStateY[i] = Integer.parseInt(tokens[1]);
            mowerStateDirection[i] = Enum.valueOf(Direction.class, tokens[2].toUpperCase());
            mowerStateRunning[i] = Boolean.parseBoolean(tokens[4]);
            mowerStateEnergy[i] = lawn.MAX_MOWER_ENERGY;
        }
        nextObjectPolled = "M0";
    }

    /**
     * Prepares the simulation for starting by aligning state and placing the mowers at their initial starting positions.
     */
    public void placeInitialMowers(){
        //Place mowers on their initial squares on the lawn
        for (int i = 0; i < mowers.size(); i++) {
            Mower aMower = mowers.get(i);
            lawn.placeMower(aMower, mowerStateX[i], mowerStateY[i]);
            lawn.specifyRechargePads(mowerStateX[i], mowerStateY[i]);
        }
    }

    /**
     * Runs the simulation continually incrementing each turn until one of the exit conditions has been met.
     * Exit conditions: all grass cut, all mowers crashed, or max turns exceeded.
     * @return String equivalent to what was printed in the final report
     */
    private String run(){
        //Run turns and check with Lawn if turn has exceeded max turn for that lawn
        while (lawn.exceededTurns() == false){
            if (isVerbose){
                OutputReportPrinter op = new OutputReportPrinter(lawn, mowerStateX, mowerStateY, mowerStateRunning, mowerStateDirection);
                op.printSimulationState();
            }

            //Stop if the lawn fully cut
            if (lawn.getCurrentNumberCut() == lawn.START_NUMBER_UNCUT){
                break;
            }

            //Stop if the mower running state array is all false meaning all mowers have crashed
            int numMowersStillRunning = 0;
            for (int i = 0; i < mowerStateRunning.length; i++){
                if (mowerStateRunning[i]){
                    numMowersStillRunning++;
                }
            }
            if (numMowersStillRunning == 0){
                break;
            }



            //Work through each mower on what they want to do
            int numMowers = mowers.size();
            for (int i = 0; i < numMowers; i++){
                int disabledMowerCounter = 0;
                boolean mowerRunning = mowerStateRunning[i];

                //Stop if stop button is pressed
                if (stopButtonPressed){
                    break;
                }

                //Only poll the mowers that are not crashed (still running)
                if (mowerRunning){

                    //Check if mower has run out of energy, if so, mark it as disabled and skip it
                    int mowerCurrentEnergy = mowerStateEnergy[i];
                    if (mowerCurrentEnergy <= 0){
                        mowerStateRunning[i] = false;
                        disabledMowerCounter = disabledMowerCounter + 1;
                        mowers.get(i).setDisabled();
                        System.out.println("m" + i + ": out of energy");
                    }
                    else {
                        this.simulateOneMowerStep(i);
                    }

                }
                else{
                    disabledMowerCounter = disabledMowerCounter + 1;
                }

                //Stop this loop if all mowers have crashed
                if (disabledMowerCounter >= numMowers){
                    break;
                }
            }

            //Stop if stop button is pressed
            if (stopButtonPressed){
                break;
            }

            //If period is gopher, then poll each gopher on what they want to do
            if ((lawn.getCurrentTurnNumber() % lawn.getGopherPeriod() == 0) && (lawn.getCurrentTurnNumber() > 0)){
                int numGophers = gophers.size();
                for (int i = 0; i < numGophers; i++){

                    //Stop if stop button is pressed
                    if (stopButtonPressed){
                        break;
                    }
                    this.simulateOneGopherStep(i);

                }
                //Stop if stop button is pressed
                if (stopButtonPressed){
                    break;
                }
            }

            //Increment turn
            lawn.incrementTurn();

        }

        //Exited the running while loop, generate final report
        OutputReportPrinter op = new OutputReportPrinter(lawn, mowerStateX, mowerStateY, mowerStateRunning, mowerStateDirection);
        String finalReport = op.generateFinalReport();
        System.out.println("Finished: " + finalReport);
        stopButtonPressed = true;
        return finalReport;
    }

    /**
     * Runs the simulation one step and sets the next object to be polled in the next run.
     * @return String equivalent to what was printed in the final report but during each step
     */
    private String runOnce(){
        String currentObjectPolled = nextObjectPolled;

        if (isVerbose){
            System.out.println("Charles inside runOnce");
            System.out.println(currentObjectPolled);
            OutputReportPrinter op = new OutputReportPrinter(lawn, mowerStateX, mowerStateY, mowerStateRunning, mowerStateDirection);
            op.printSimulationState();
        }

        //Stop if the lawn fully cut
        if (lawn.getCurrentNumberCut() == lawn.START_NUMBER_UNCUT){
            stopButtonPressed = true;
            OutputReportPrinter op = new OutputReportPrinter(lawn, mowerStateX, mowerStateY, mowerStateRunning, mowerStateDirection);
            String finalReport = op.generateFinalReport();
            System.out.println("Finished: " + finalReport);
            return currentObjectPolled;
        }

        //Stop if the mower running state array is all false meaning all mowers have crashed
        int numMowersStillRunning = 0;
        for (int i = 0; i < mowerStateRunning.length; i++){
            if (mowerStateRunning[i]){
                numMowersStillRunning++;
            }
        }
        if (numMowersStillRunning == 0){
            stopButtonPressed = true;
            OutputReportPrinter op = new OutputReportPrinter(lawn, mowerStateX, mowerStateY, mowerStateRunning, mowerStateDirection);
            String finalReport = op.generateFinalReport();
            System.out.println("Finished: " + finalReport);
            return currentObjectPolled;
        }



        //Work through each mower on what they want to do
        int numMowers = mowers.size();

        String type = currentObjectPolled.substring(0,1);
        String IDStr = currentObjectPolled.substring(1,2);
        if (type.equalsIgnoreCase("M")){
            if (isVerbose){
                System.out.println("Run Once :: polling Mower :: " + currentObjectPolled);
            }
            int i = Integer.parseInt(IDStr);
            //Precalculate who to poll next
            if (i < numMowers-1) {
                nextObjectPolled = "M" + (i + 1);
            }
            else {
                if ((lawn.getCurrentTurnNumber() % lawn.getGopherPeriod() == 0) && (lawn.getCurrentTurnNumber() > 0)) {
                    nextObjectPolled = "G0";
                }
                else{
                    lawn.incrementTurn();
                    nextObjectPolled = "M0";
                }
            }

            boolean mowerRunning = mowerStateRunning[i];

            //Stop if stop button is pressed
            if (stopButtonPressed) {
                OutputReportPrinter op = new OutputReportPrinter(lawn, mowerStateX, mowerStateY, mowerStateRunning, mowerStateDirection);
                String finalReport = op.generateFinalReport();
                System.out.println("Stopped: " + finalReport);
                return currentObjectPolled;
            }

            //Only poll the mowers that are not crashed (still running)
            if (mowerRunning) {

                //Check if mower has run out of energy, if so, mark it as disabled and skip it
                int mowerCurrentEnergy = mowerStateEnergy[i];
                if (mowerCurrentEnergy <= 0) {
                    mowerStateRunning[i] = false;
                    mowers.get(i).setDisabled();
                    System.out.println("m" + i + ": out of energy");
                } else {
                    this.simulateOneMowerStep(i);
                }
            }

        }


        //Stop if stop button is pressed
        if (stopButtonPressed){
            OutputReportPrinter op = new OutputReportPrinter(lawn, mowerStateX, mowerStateY, mowerStateRunning, mowerStateDirection);
            String finalReport = op.generateFinalReport();
            System.out.println("Stopped: " + finalReport);
            return currentObjectPolled;
        }

        //If period is gopher, then poll each gopher on what they want to do
        if ((lawn.getCurrentTurnNumber() % lawn.getGopherPeriod() == 0) && (lawn.getCurrentTurnNumber() > 0)){
            int numGophers = gophers.size();

            type = currentObjectPolled.substring(0,1);
            IDStr = currentObjectPolled.substring(1,2);
            if (type.equalsIgnoreCase("G")) {
                if (isVerbose){
                    System.out.println("Run Once :: polling Gopher :: " + currentObjectPolled);
                }
                int i = Integer.parseInt(IDStr);
                if (i < gophers.size()-1) {
                    nextObjectPolled = "G" + (i + 1);
                }
                else {
                    nextObjectPolled = "M0";
                    lawn.incrementTurn();
                }
                this.simulateOneGopherStep(i);
            }


            //Stop if stop button is pressed
            if (stopButtonPressed){
                OutputReportPrinter op = new OutputReportPrinter(lawn, mowerStateX, mowerStateY, mowerStateRunning, mowerStateDirection);
                String finalReport = op.generateFinalReport();
                System.out.println("Stopped: " + finalReport);
                return currentObjectPolled;
            }
        }


        System.out.println(currentObjectPolled);
        return currentObjectPolled;
    }

    private Mower simulateOneMowerStep(int theCurrentMowerIndex) {
        Mower theCurrentMower = mowers.get(theCurrentMowerIndex);

        //Get the mower's desired action
        Action mowerRequestedAction = theCurrentMower.desiredNextAction();

        //Use Action Processor service class that validates and processes the action requested by each mower
        //Separating action processing into its own service class decouples action checking and action updates from this
        // simulation system class. In future, if new actions are added, needed code changes can be consolidated in action processor class.
        ActionProcessor ap = new ActionProcessor(lawn, mowers, mowerStateX, mowerStateY, mowerStateDirection, mowerStateRunning, mowerStateEnergy, gophers);

        //Handle the action requested by the mower
        Mower m = ap.handleMowerAction(theCurrentMowerIndex, mowerRequestedAction);

        //Update simulation state data based on results from handling mower action
        lawn = ap.getLawn();
        mowers = ap.getMowers();
        mowerStateX = ap.getMowerStateX();
        mowerStateY = ap.getMowerStateY();
        mowerStateDirection = ap.getMowerStateDirection();
        mowerStateRunning = ap.getMowerStateRunning();
        mowerStateEnergy = ap.getMowerStateEnergy();
        return m;
    }

    private Gopher simulateOneGopherStep(int theCurrentGopherIndex) {
        Gopher theCurrentGopher = gophers.get(theCurrentGopherIndex);

        theCurrentGopher.desiredNextSquare(mowerStateX, mowerStateY, mowerStateRunning);

        int gopherDesiredX = theCurrentGopher.getDesiredX();
        int gopherDesiredY = theCurrentGopher.getDesiredY();

        ActionProcessor ap = new ActionProcessor(lawn, mowers, mowerStateX, mowerStateY, mowerStateDirection, mowerStateRunning, mowerStateEnergy, gophers);

        //Handle the action requested by the gopher
        ap.handleGopherMove(theCurrentGopherIndex, gopherDesiredX, gopherDesiredY);

        //Update simulation state data based on results from handling mower action
        lawn = ap.getLawn();
        mowers = ap.getMowers();
        mowerStateX = ap.getMowerStateX();
        mowerStateY = ap.getMowerStateY();
        mowerStateDirection = ap.getMowerStateDirection();
        mowerStateRunning = ap.getMowerStateRunning();
        mowerStateEnergy = ap.getMowerStateEnergy();
        gophers = ap.getGophers();
        return theCurrentGopher;
    }

    /**
     * Interrupts a run (even in FastForward mode
     * UI call this method to stop the simulation immediately and print current state to console
     */
    public String stop(){
        stopButtonPressed = true;
        OutputReportPrinter op = new OutputReportPrinter(lawn, mowerStateX, mowerStateY, mowerStateRunning, mowerStateDirection);
        String finalReport = op.generateFinalReport();
        System.out.println("Stopped: " +  finalReport);
        return "Stopped";
    }

    /**
     * Tells simulation to run until completion
     * UI call this method when FF button is pressed
     */
    public String activateFastForward() {
        if (!stopButtonPressed) {
            return this.run();
        }
        else {
            return this.stop();
        }
    }

    /**
     * Tells simulation to run one increment
     * UI call this method when SingleStep button is pressed
     */
    public String pressIncrement(){
        if (!stopButtonPressed) {
            return this.runOnce();
        }
        else {
            return this.stop();
        }
    }

    public String getNextObjectPolled() {
        return nextObjectPolled;
    }

    public Lawn getLawn() {
        return lawn;
    }

    public int[] getMowerStateX() {
        return mowerStateX;
    }

    public int[] getMowerStateY() {
        return mowerStateY;
    }


    public boolean isVerbose() {
        return isVerbose;
    }

    public ArrayList<Mower> getMowers(){
        return mowers;
    }

    public ArrayList<Gopher> getGophers() {
        return gophers;
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
}
