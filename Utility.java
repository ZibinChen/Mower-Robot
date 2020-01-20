import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

public final class Utility {
 
    public static HashMap<Direction, Integer> xDIR_MAP = new HashMap<>();
    public static HashMap<Direction, Integer> yDIR_MAP = new HashMap<>();
    
    public static final int[] SCAN_LIST = {0, 1, 2, 3, 4, 5, 6, 7};
    
    public static final Direction[] ORIENT_LIST = {Direction.NORTH,Direction.NORTHEAST,Direction.EAST,Direction.SOUTHEAST,Direction.SOUTH,Direction.SOUTHWEST,Direction.WEST,Direction.NORTHWEST};

    public static void createUtility(){
        xDIR_MAP.put(Direction.NORTH, 0);
        xDIR_MAP.put(Direction.NORTHEAST, 1);
        xDIR_MAP.put(Direction.EAST, 1);
        xDIR_MAP.put(Direction.SOUTHEAST, 1);
        xDIR_MAP.put(Direction.SOUTH, 0);
        xDIR_MAP.put(Direction.SOUTHWEST, -1);
        xDIR_MAP.put(Direction.WEST, -1);
        xDIR_MAP.put(Direction.NORTHWEST, -1);
        
        yDIR_MAP.put(Direction.NORTH, 1);
        yDIR_MAP.put(Direction.NORTHEAST, 1);
        yDIR_MAP.put(Direction.EAST, 0);
        yDIR_MAP.put(Direction.SOUTHEAST, -1);
        yDIR_MAP.put(Direction.SOUTH, -1);
        yDIR_MAP.put(Direction.SOUTHWEST, -1);
        yDIR_MAP.put(Direction.WEST, 0);
        yDIR_MAP.put(Direction.NORTHWEST, 1);
    }


    
    public static int dir_Utility(Direction currentDirection){
        
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

}
