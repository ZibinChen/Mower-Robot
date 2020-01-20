
/**
 * Class representing a square on the lawn.
 */
public class Square {

    //Class variables
    private int x;
    private int y;
    private boolean isMowerParked = false;
    private SquareKind type;
    private boolean isRechargePad = false;

    /**
     * Constructor that sets defaults for attributes
     */
    public Square(){
        this.x = 0;
        this.y = 0;
        type = SquareKind.EMPTY;
    }

    /**
     * Constructor that sets the X and Y coordinates of the square.
     * @param xCoordinate X location of this square
     * @param yCoordinate Y location of this square
     */
    public Square(int xCoordinate, int yCoordinate){
        this.x = xCoordinate;
        this.y = yCoordinate;
        type = SquareKind.EMPTY;
    }

    public Square(int xCoordinate, int yCoordinate, String type){
        this.x = xCoordinate;
        this.y = yCoordinate;
        this.type = SquareKind.valueOf(type.toUpperCase());
    }

    /**
     * Method that disables any mower that arrives on this Square.
     * @param m Mower that could be disabled by a subclass of Square
     */
    public void disableMower(Mower m){
        if (type == SquareKind.GOPHER_EMPTY ||
                type == SquareKind.GOPHER_GRASS ||
                type == SquareKind.FENCE ||
                type == SquareKind.MOWER){
            m.setDisabled();
        }
    }

    /**
     * Some children classes of Square could be cut. Square itself is empty with nothing to cut and will simply return.
     * @param m Mower that will be doing the cutting
     */
    public void cut(Mower m){
        if (type == SquareKind.GRASS){
            type = SquareKind.EMPTY;
        }
    }

    /**
     * Returns the contents of the square which can vary based on subclasses of Square. Square itself is empty.
     * @return The contents of this square
     */
    public String getSquareKind(){
        return type.name().toLowerCase();
    }

    /**
     * Allows setting whether a mower is parked on this instance of square.
     * @param isParked Status of whether something is parked on this square or not
     */
    public void setMowerParked(boolean isParked){
        isMowerParked = isParked;
    }

    //Getters and Setters
    public SquareKind getType() {
        return type;
    }

    public void setType(SquareKind newType) {
        type = newType;
    }

    public boolean isRechargePad() {
        return isRechargePad;
    }

    public void setRechargePad(boolean rechargePad) {
        isRechargePad = rechargePad;
    }
}
