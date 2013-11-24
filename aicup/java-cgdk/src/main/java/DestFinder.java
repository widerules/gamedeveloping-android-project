import model.*;

import java.util.LinkedList;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: lizard
 * Date: 24.11.13
 * Time: 4:15
 * To change this template use File | Settings | File Templates.
 */
public class DestFinder {
    private World world;

    public DestFinder(World world) {
        this.world = world;
    }

    public Node getSafePlace(Trooper self, World world, Game game, Move move){
        LinkedList<Trooper> enemies = getEnemies(world);
        if (enemies.isEmpty()){
            return null;
        }

        TreeSet<Node> opened = new TreeSet<>();
        LinkedList<Node> closed = new LinkedList<>();

        opened.add(new Node(self.getX(), self.getY(), 0, null));

        Node node = getNode(10, opened, closed, enemies, world, self.getStance());


        return node;
    }

    private boolean isCellFree(int x, int y){
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

    public Node getNode(int depth, TreeSet<Node> opened,LinkedList<Node> closed, LinkedList<Trooper> enemies, World world, TrooperStance stance){
        Node node = null;//new Node(self.getX(), self.getY(),10, null);

        while(true){
            node = opened.pollFirst();
            closed.add(node);
            if (isSafe(node.x, node.y, enemies, world, stance)){
                return node;
            }

            if (node.x>0 && isCellFree(node.x-1,node.y)){
                Node newNode = new Node(node.x-1, node.y, depth,node);
                if (!closed.contains(newNode)){
                    opened.add(newNode);
                }
            }

            if (node.x<29 && isCellFree(node.x+1,node.y)) {
                Node newNode = new Node(node.x+1, node.y, depth,node);

                if (!closed.contains(newNode)){
                    opened.add(newNode);
                }
            }

            if (node.y>0 && isCellFree(node.x,node.y-1)){
                Node newNode = new Node(node.x, node.y-1, depth ,node);
                if (!closed.contains(newNode)){
                    opened.add(newNode);
                }
            }

            if (node.y<19 && isCellFree(node.x,node.y+1)){
                Node newNode = new Node(node.x, node.y+1, depth,node);
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



    public boolean isSafe(int x, int y, LinkedList<Trooper> enemies, World world, TrooperStance stance){
        for (Trooper t : enemies){
            if (world.isVisible(t.getVisionRange(), t.getX(), t.getY(), t.getStance(),
                    x, y, stance)){
                return false;
            }
        }

        return true;
    }


    public LinkedList<Trooper> getEnemies(World world){
        LinkedList<Trooper> ret = new LinkedList<>();
        for (Trooper t : world.getTroopers()){
            if (!t.isTeammate()){
                ret.add(t);
            }
        }
        return ret;
    }
}
