
/**
 * A utility class used for printing the final report and printing lawn renders for debugging
 */
public class OutputReportPrinter {

    //Class variables
    private Lawn lawn;
    private int[] mowerStateX;
    private int[] mowerStateY;
    private boolean[] mowerStateRunning;
    private Direction[] mowerStateDirection;

    /**
     * Constructor of a printing instance.
     * @param lawn The lawn to be printed
     * @param mowerStateX List of all mowers' X coordiantes
     * @param mowerStateY List of all mowers' Y coordinates
     * @param mowerStateRunning List of all mowers' crashed or running status
     * @param mowerStateDirection List of all mowers' directions
     */
    public OutputReportPrinter(Lawn lawn, int[] mowerStateX, int[] mowerStateY, boolean[] mowerStateRunning, Direction[] mowerStateDirection) {
        this.lawn = lawn;
        this.mowerStateX = mowerStateX;
        this.mowerStateY = mowerStateY;
        this.mowerStateRunning = mowerStateRunning;
        this.mowerStateDirection = mowerStateDirection;
    }

    /**
     * Generates the final report output of the system that contains information about total number of squares,
     * original number of grass squares, number of grass squares that were cut, and number of completed turns.
     * @return Comma separated string of the 4 pieces of information
     */
    public String generateFinalReport(){
        String answer = "";

        //Total number of squares on the lawn
        int totalNumSquares = lawn.getHeight()*lawn.getWidth();
        answer = answer + totalNumSquares + ",";

        //The original number of squares containing grass on the lawn
        answer = answer + lawn.START_NUMBER_UNCUT + ",";

        //The number of grass squares cut by the mowers
        answer = answer + lawn.getCurrentNumberCut() + ",";

        //The total number of fully completed turns for the simulation run
        answer = answer + lawn.getCurrentTurnNumber();

        return answer;
    }

    /**
     * Uses a modified version of the professor's code to generate and print the state of the lawn and objects on the lawn for debugging use.
     */
    public void printSimulationState() {
        int i, j;
        int charWidth = 2 * lawn.getWidth() + 2;

        // display the rows of the lawn from top to bottom
        for (j = lawn.getHeight() - 1; j >= 0; j--) {
            renderHorizontalBar(charWidth);

            // display the Y-direction identifier
            System.out.print(j);

            // display the contents of each square on this row
            for (i = 0; i < lawn.getWidth(); i++) {
                System.out.print("|");

                // the mower overrides all other contents
                if (i == mowerStateX[0] && j == mowerStateY[0] && mowerStateRunning[0] == true) {
                    System.out.print("0");

                } else if (mowerStateRunning.length > 1 && mowerStateRunning[1] == true && i == mowerStateX[1] && j == mowerStateY[1]) {
                    System.out.print("1");

                } else {
                    Square theSq = lawn.getSquareAt(i, j);
                    switch (theSq.getSquareKind()) {
                        case "empty":
                            System.out.print(" ");
                            break;
                        case "recharge":
                            System.out.print("R");
                            break;
                        case "grass":
                            System.out.print("g");
                            break;
                        case "gopher_empty":
                            System.out.print("E");
                            break;
                        case "gopher_grass":
                            System.out.print("G");
                            break;
                        default:
                            break;
                    }
                }
            }
            System.out.println("|");
        }
        renderHorizontalBar(charWidth);

        // display the column X-direction identifiers
        System.out.print(" ");
        for (i = 0; i < lawn.getWidth(); i++) {
            System.out.print(" " + i);
        }
        System.out.println("");

        // display the mower's directions
        for(int k = 0; k < mowerStateRunning.length; k++) {
            if (mowerStateRunning[k] == false) { continue; }
            System.out.println("dir m" + String.valueOf(k) + ": " + mowerStateDirection[k]);
        }
        System.out.println("Turn number: " + lawn.getCurrentTurnNumber());
        System.out.println("");
    }

    private void renderHorizontalBar(int size) {
        System.out.print(" ");
        for (int k = 0; k < size; k++) {
            System.out.print("-");
        }
        System.out.println("");
    }
}
