/**
 * @author : dkim
 *         Date: 11.11.13
 *         Time: 19:12
 */
public class Node implements Comparable<Node>{
    public int x;
    public int y;
    public int cost;
    Node parent;

    public Node(int x, int y, int cost, Node parent) {
        this.x = x;
        this.y = y;
        this.cost = cost;
        this.parent = parent;
    }



    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    @Override
    public int compareTo(Node o) {
        if((this.x == o.x)&&(this.y == o.y)){
            return 0;
        }
        return this.cost < o.cost ? -1 : 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        if (x != node.x) return false;
        if (y != node.y) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 100 * result + y;
        return result;
    }

    @Override
    public String toString() {
        return "Node{" +
                "x=" + x +
                ", y=" + y +
                ", cost=" + cost +
                ", parent=" + parent +
                '}';
    }
}
