import model.CellType;
import model.Direction;
import model.Trooper;
import model.World;

import java.util.LinkedList;
import java.util.TreeSet;

/**
 * @author : dkim
 *         Date: 11.11.13
 *         Time: 19:15
 */
public class PathFinder {
    private World world;

    public PathFinder(World world) {
        this.world = world;
    }

    private boolean isCellFree(int x, int y){
//        world.getCellVisibilities();

//        System.out.println("x: "+x+" y:"+y);
        if (world.getCells()[x][y].equals(CellType.FREE)){
            for (Trooper t : world.getTroopers()) {
                if (t.getX() == x && t.getY() == y) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }


    //    int[][] heuristic = new int[30][20];//в угоду производительности
    int[] heuristic = new int[600]; //30*20

    private int[] getH(int destX, int destY){

        for (int x = 0;x<30;x++){
            for (int y = 0; y<20; y++){
                heuristic[x+30*y] = Math.abs(x-destX) + Math.abs(y-destY);
            }
        }
        return heuristic;
    }

//    private int[][] getH(int destX, int destY){
//
//        for (int x = 0;x<30;x++){
//            for (int y = 0; y<20; y++){
//                heuristic[x][y] = Math.abs(x-destX) + Math.abs(y-destY);
//            }
//        }
//        return heuristic;
//    }

    public Direction getDirection(Trooper self, World world, int destX, int destY, double precision){
//        TreeSet<Node> opened = new TreeSet<>();
//        TreeSet<Node> closed = new TreeSet<>();

        TreeSet<Node> opened = new TreeSet<>();
        LinkedList<Node> closed = new LinkedList<>();

        getH(destX, destY);

        opened.add(new Node(self.getX(), self.getY(), 0, null));

        CellType[][] cellTypes = world.getCells();

        System.out.println("heading to: "+destX +" "+destY+ " from: "+self.getX() + " "+self.getY());

        Node finalNode = getNext(opened, closed, self, cellTypes, destX ,destY, 0, precision);

        System.out.println("finalNode: "+finalNode.x+" " +finalNode.y);
        System.out.println("opened: "+opened.size());
        System.out.println("closed: "+closed.size());

        Node pre = finalNode;
        while (finalNode.getParent()!=null){
            pre = finalNode;
            finalNode = finalNode.getParent();
        }

        if (pre.x>finalNode.x){
            return Direction.EAST;
        }
        else if (pre.x<finalNode.x){
            return Direction.WEST;
        }
        else if (pre.y>finalNode.y){
            return Direction.SOUTH;
        }
        else if (pre.y<finalNode.y){
            return Direction.NORTH;
        }
        else{
//            System.out.println("HOLD!!!");
            return Direction.CURRENT_POINT;
        }
    }

    int tmpCurrentIdx = 0;
    public Node getNext(TreeSet<Node> opened, LinkedList<Node> closed, Trooper self, CellType[][] cellTypes , int destX, int destY, int depth, double precision){
        Node node = null;//new Node(self.getX(), self.getY(),10, null);

        while(true){
            node = opened.pollFirst();
            closed.add(node);
//            System.out.println("rez: "+Math.abs(node.x - destX)+" "+Math.abs(node.y - destY) + " precision: "+precision);
//            if ( (Math.abs(node.x - destX)+ Math.abs(node.y - destY)<precision)){
            if ( Math.sqrt((node.x - destX)*(node.x - destX) + (node.y - destY)*(node.y - destY))<=precision){
                return node;
            }

            tmpCurrentIdx = node.x+node.y*30;

            if (node.x>0 && isCellFree(node.x-1,node.y)){
                Node newNode = new Node(node.x-1, node.y, depth + heuristic[tmpCurrentIdx-1],node);
                if (!closed.contains(newNode)){
                    opened.add(newNode);
                }
            }

            if (node.x<29 && isCellFree(node.x+1,node.y)) {
                Node newNode = new Node(node.x+1, node.y, depth + heuristic[tmpCurrentIdx+1],node);

                if (!closed.contains(newNode)){
                    opened.add(newNode);
                }
            }

            if (node.y>0 && isCellFree(node.x,node.y-1)){
                Node newNode = new Node(node.x, node.y-1, depth + heuristic[tmpCurrentIdx-30],node);
                if (!closed.contains(newNode)){
                    opened.add(newNode);
                }
            }

            if (node.y<19 && isCellFree(node.x,node.y+1)){
                Node newNode = new Node(node.x, node.y+1, depth + heuristic[tmpCurrentIdx+30],node);
                if (!closed.contains(newNode)){
                    opened.add(newNode);
                }
            }

            if (opened.isEmpty()){
                System.out.println("node:" + node.toString());
                return node;
            }
            depth+=10;

        }
    }
}
