import java.util.ArrayList;
import java.util.Random;

/**
 * A class representing a gopher on a lawn
 */
public class Gopher {

    //Class variables
    private int gopherID;
    private int currentX;
    private int currentY;
    private int desiredX;
    private int desiredY;

    public Gopher(int gopherID, int currentX, int currentY){
        this.gopherID = gopherID;
        this.currentX = currentX;
        this.currentY = currentY;
    }

    /**
     * Takes in the current state (location XY and whether running) of all mowers in the simulation and calculates the
     * next square this gopher wants to move to. The desired squares to move to is read from desiredX and desiredY
     * @param mowerStateX Current X locations of all mowers
     * @param mowerStateY Current Y locations of all mowers
     * @param mowerStateRunning Current status whether mower is still in simulation
     */
    public void desiredNextSquare(int[] mowerStateX, int[] mowerStateY, boolean[] mowerStateRunning){
        int targetMowerID = gopherSearch(mowerStateX, mowerStateY, mowerStateRunning);
        if (targetMowerID < 0){
            desiredX = currentX;
            desiredY = currentY;
        }
        else {
            int targetMowerX = mowerStateX[targetMowerID];
            int targetMowerY = mowerStateY[targetMowerID];
            if (targetMowerX < currentX) {
                desiredX = currentX - 1;
            } else if (targetMowerX > currentX) {
                desiredX = currentX + 1;
            } else {
                desiredX = currentX;
            }

            if (targetMowerY < currentY) {
                desiredY = currentY - 1;
            } else if (targetMowerY > currentY) {
                desiredY = currentY + 1;
            } else {
                desiredY = currentY;
            }
        }
    }

    /**
     * Internal method to calculate euclidean distance between this gopher and all mowers and returns the ID of
     * the closest mower
     * @param mowerStateX Current X locations of all mowers
     * @param mowerStateY Current Y locations of all mowers
     * @param mowerStateRunning Current status whether mower is still in simulation
     * @return ID of the mower this gopher wants to target right now
     */
    private int gopherSearch(int[] mowerStateX, int[] mowerStateY, boolean[] mowerStateRunning){
        ArrayList<Integer> currentChosenMowerID = new ArrayList<>();
        double currentMinimumDistance = 1000000.0;
        for (int i = 0; i < mowerStateRunning.length; i++) {
            if (mowerStateRunning[i]) {
                int diffX = currentX - mowerStateX[i];
                int diffY = currentY - mowerStateY[i];
                int sumOfDiffSq = (diffX * diffX) + (diffY * diffY);
                double distance = Math.sqrt(sumOfDiffSq);
                if (distance <= currentMinimumDistance) {
                    currentChosenMowerID.add(i);
                    currentMinimumDistance = distance;
                }
            }
        }

        if (currentChosenMowerID.size() == 1){
            return currentChosenMowerID.get(0);
        }
        else if (currentChosenMowerID.size() > 1){
            Random r = new Random();
            int chosenOne = r.nextInt(currentChosenMowerID.size());
            return currentChosenMowerID.get(chosenOne);
        }
        else{
            return -1;
        }
    }

    //Getters and setters

    public int getCurrentX() {
        return currentX;
    }

    public void setCurrentX(int currentX) {
        this.currentX = currentX;
    }

    public int getCurrentY() {
        return currentY;
    }

    public void setCurrentY(int currentY) {
        this.currentY = currentY;
    }

    public int getDesiredX() {
        return desiredX;
    }


    public int getDesiredY() {
        return desiredY;
    }

}
