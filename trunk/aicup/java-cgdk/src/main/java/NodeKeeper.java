import java.util.ArrayList;

/**
 * @author : dkim
 *         Date: 12.11.13
 *         Time: 20:32
 */
public class NodeKeeper{
    private ArrayList<Node> nodes = new ArrayList<>(601);

    private static NodeKeeper nk = null;

    private NodeKeeper() {
        for (int y = 0; y<20; y++){
            for (int x = 0; x<30; x++){
                nodes.add(new Node(x, y, 0, null));
            }
        }
    }

    public static NodeKeeper getInstance(){
        if (nk==null){
            nk = new NodeKeeper();
        }
        return nk;
    }




    public Node getNode(int x, int y){
        return nodes.get(y*30+x);
    }
}
