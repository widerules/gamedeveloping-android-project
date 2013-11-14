import model.*;

import java.util.LinkedList;
import java.util.TreeSet;

public class ViewAnalyzer {
    private World world;

    public ViewAnalyzer(World world) {
        this.world = world;
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


    public void analyze(Trooper self, World world, Game game, Move move){

        TreeSet<Node> teamVision = new TreeSet<>();
        for(Trooper t: world.getTroopers()){
            if (t.isTeammate() && t.getHitpoints()>0){
                teamVision.addAll(getVision(t));
            }
        }

        System.out.println("TeamVision Size: "+teamVision.size());
    }

    public LinkedList<Node> getVision(Trooper self){
        TreeSet<Node> opened =new TreeSet<>();
        LinkedList<Node> checked =new LinkedList<>();
        LinkedList<Node> finalSet =new LinkedList<>();

        opened.add(new Node(self.getX(), self.getY(), 0, null));

        while (!opened.isEmpty()){
            Node node = opened.pollFirst();
            if (self.getDistanceTo(node.x, node.y)<self.getVisionRange()){
                finalSet.add(node);
            }else{
                checked.add(node);
            }

            if (node.x>0 && isCellFree(node.x-1,node.y)
                    && self.getDistanceTo(node.x-1, node.y)<self.getVisionRange()){
                Node newNode = new Node(node.x-1, node.y, 0,node);
                if (!checked.contains(newNode) && !finalSet.contains(newNode)){
                    opened.add(newNode);
                }
            }

            if (node.x<29 && isCellFree(node.x+1,node.y)
                    &&self.getDistanceTo(node.x+1, node.y)<self.getVisionRange()) {
                Node newNode = new Node(node.x+1, node.y, 0,node);
                if (!checked.contains(newNode) && !finalSet.contains(newNode)){
                    opened.add(newNode);
                }
            }

            if (node.y>0 && isCellFree(node.x,node.y-1)
                    &&self.getDistanceTo(node.x, node.y-1)<self.getVisionRange()){
                Node newNode = new Node(node.x, node.y-1, 0,node);
                if (!checked.contains(newNode) && !finalSet.contains(newNode)){
                    opened.add(newNode);
                }
            }

            if (node.y<19 && isCellFree(node.x,node.y+1)
                    &&self.getDistanceTo(node.x, node.y+1)<self.getVisionRange()){
                Node newNode = new Node(node.x, node.y+1, 0,node);
                if (!checked.contains(newNode) && !finalSet.contains(newNode)){
                    opened.add(newNode);
                }
            }
        }

        return finalSet;

    }
}
