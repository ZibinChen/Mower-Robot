import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;

/**
 * Utility class that opens the input file name and reads the information specific to this scenario file into data structure within the system.
 */
public class ScenarioFileParser {

    //Class variables (mostly from input file standards)
    private boolean isVerbose;
    private String fileName;
    private int lawnWidth;
    private int lawnHeight;
    private int numMowers;
    private ArrayList<String> mowerConfigs;
    static int maxMowerEnergy;
    private int numGophers;
    private ArrayList<String> gopherConfigs;
    private int gopherPeriod;
    static int maxTurns;


    /**
     * Constructor (overloaded) - allowing for verbose prints
     * @param fileName Name (or full path) of file containing the scenario information.
     * @param isVerbose Flag enable printing of additional debug information.
     */
    public ScenarioFileParser(String fileName, boolean isVerbose){
        this.fileName = fileName;
        lawnWidth = 0;
        lawnHeight = 0;
        numMowers = 0;
        mowerConfigs = new ArrayList<>();
        maxMowerEnergy = 0;
        numGophers = 0;
        gopherConfigs = new ArrayList<>();
        gopherPeriod = 1;
        maxTurns = 0;
        this.isVerbose = isVerbose;

        this.parseScenario();
    }


    /**
     * Executes the parse command.
     * Reading in the file and sets instance variables to the values within the scenario file.
     */
    private void parseScenario(){
        String delimiter = ",";
        try {
            Scanner takeCommand = new Scanner(new File(fileName));
            String[] tokens;

            // read in the lawn information
            tokens = takeCommand.nextLine().split(delimiter);
            lawnWidth = Integer.parseInt(tokens[0]);
            tokens = takeCommand.nextLine().split(delimiter);
            lawnHeight = Integer.parseInt(tokens[0]);

            // read in number of mowers
            tokens = takeCommand.nextLine().split(delimiter);
            numMowers = Integer.parseInt(tokens[0]);
            if (isVerbose){
                System.out.println("lawnWidth :: " + lawnWidth);
                System.out.println("lawnHeight :: " + lawnHeight);
                System.out.println("numMowers :: " + numMowers);
            }

            // read in data about each mower
            for (int i = 0; i < numMowers; i++){
                String aMowerConfig = takeCommand.nextLine();
                mowerConfigs.add(aMowerConfig);
            }
            if (isVerbose){
                System.out.println("MowerConfigs :: " + mowerConfigs);
            }

            // read in energy capacity for each mower
            tokens = takeCommand.nextLine().split(delimiter);
            maxMowerEnergy = Integer.parseInt(tokens[0]);
            if (isVerbose){
                System.out.println("maxMowerEnergy :: " + maxMowerEnergy);
            }

            // read in number of gophers
            tokens = takeCommand.nextLine().split(delimiter);
            numGophers = Integer.parseInt(tokens[0]);
            if (isVerbose){
                System.out.println("numGophers :: " + numGophers);
            }

            // read in data about each gopher
            for (int i = 0; i < numGophers; i++){
                String aGopher = takeCommand.nextLine();
                gopherConfigs.add(aGopher);
            }
            if (isVerbose){
                System.out.println("gopherConfigs :: " + gopherConfigs);
            }

            // read in data about gopher period
            tokens = takeCommand.nextLine().split(delimiter);
            gopherPeriod = Integer.parseInt(tokens[0]);
            if (isVerbose){
                System.out.println("gopherPeriod :: " + gopherPeriod);
            }

            // read in data about max turns
            tokens = takeCommand.nextLine().split(delimiter);
            maxTurns = Integer.parseInt(tokens[0]);
            if (isVerbose){
                System.out.println("maxTurns :: " + maxTurns);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println();
        }
    }

    /**
     * Instantiates an instance of Lawn class.
     * @return The instantiated lawn serves as a critical data structure tracking state and provides functionality and data to rest of the system.
     */
    public Lawn loadAndCreateLawn(){
        //Instantiate an instance of Lawn class to represent one lawn
        Lawn theLawn = new Lawn(lawnWidth, lawnHeight, numMowers, numGophers, gopherPeriod, maxTurns, maxMowerEnergy, isVerbose);
        //Provide the lawn instance information about craters and let the lawn class populate its squares
        theLawn.populateSquaresOnLawn(gopherConfigs);
        return theLawn;
    }

    /**
     * Creates a list of instantiated mowers to represent the one or the many mowers in a scenario.
     * @return List of instantiated mower objects indexed to each mower's unique ID.
     */
    public ArrayList<Mower> loadAndCreateMowers(){
        ArrayList<Mower> mowers = new ArrayList<>();
        int numMowers = mowerConfigs.size();
        for (int i = 0; i < numMowers; i++){
            //Get mower data from the file parser
            String aMowerConfig = mowerConfigs.get(i);
            String[] tokens = aMowerConfig.split(",");
            Direction mowerDirection = Enum.valueOf(Direction.class, tokens[2].toUpperCase());
            int mowerStrategy = Integer.parseInt(tokens[3]);
            //Instantiate an instance of the Mower class to represent each mower
            Mower aMower = new Mower(i, mowerDirection, mowerStrategy, maxMowerEnergy);
            //Add the newly instantiated mower to the list of mowers
            mowers.add(aMower);
        }
        return mowers;
    }

    /**
     * Creates a list of instantiated gophers to represent the one or the many gophers in a scenario.
     * @return List of instantiated gophers objects indexed to each gopherss's unique ID.
     */
    public ArrayList<Gopher> loadAndCreateGophers(){
        ArrayList<Gopher> gophers = new ArrayList<>();
        int numGophers = gopherConfigs.size();
        for (int i = 0; i < numGophers; i++){
            //Get gopher data from the file parser
            String aGopherConfig = gopherConfigs.get(i);
            String[] tokens = aGopherConfig.split(",");
            int gX = Integer.parseInt(tokens[0]);
            int gY = Integer.parseInt(tokens[1]);
            //Instantiate an instance of the Gopher class to represent each gopher
            Gopher g = new Gopher(i, gX, gY);
            //Add the newly instantiated mower to the list of mowers
            gophers.add(g);
        }
        return gophers;

    }

    /**
     * Creates a list of string that can be used to track mower state. This state information is intended for the simulator (God).
     * The Mower objects themselves can use this information to cheat, so this data must be encapsulated in the Simulator away from Mower.
     * @return List of string containing mower state information indexed to each mower's unique ID.
     */
    public ArrayList<String> loadAndCreateMowerStateTracker(){
        ArrayList<String> mowerStateTracker = new ArrayList<>();
        int numMowers = mowerConfigs.size();
        for (int i = 0; i < numMowers; i++) {
            String aMowerConfig = mowerConfigs.get(i);
            aMowerConfig = aMowerConfig + ",true";
            mowerStateTracker.add(aMowerConfig);
        }
        return mowerStateTracker;
    }

    /**
     * Get Maximum Turns
     * @return
     */
    public int getMaxTurn() {
        return maxTurns;
    }

    /**
     * Get Maximum mower energy
     * @return
     */
    public int getMaxEnergy() {
        return maxMowerEnergy;
    }
}
