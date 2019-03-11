import java.util.ArrayList;
import java.util.List;

/**
 * A simple tree created for this compiler to be used
 * as a concrete syntax tree.
 */
public class Tree {
    public Node root;
    public Node currentNode;

    public Tree() {
        root = null;
        root.children = new ArrayList<Node>();
        currentNode = null;
    }

    public void addNode(String data) {
        Node n = new Node(data);
        if (root == null)
            root = n;
        else {
            n.parent = currentNode;
            currentNode.children.add(n);
        }
        currentNode = n;
    }

    public class Node {
        String data;
        Node parent;
        ArrayList<Node> children;

        Node(String name) {
            this.data = data;
        }


    }
}