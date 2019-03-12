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
        currentNode = null;
    }

    public void addNode(String data) {
        currentNode = addLeafNode(data);
    }

    public Node addLeafNode(String data) {
        Node n = new Node(data);
        if (root == null)
            root = n;
        else {
            n.parent = currentNode;
            currentNode.children.add(n);
        }
        return n;
    }

    public void printTree() {
        Node current = this.root;
        int level = 0;
        int childNum = 0;

        printTree(current, level);
        System.out.println();
    }

    public void printTree(Node current, int treeLevel) {
        int level = treeLevel;

        // Print current node with proper level
        current.printNode(level);

        level++;
        // Recursively print all of this node's children
        for (int i = 0; i < current.children.size(); i++) {
            printTree(current.children.get(i), level);
        }
    }

    public class Node {
        String data;
        Node parent;
        ArrayList<Node> children;

        Node(String data) {
            this.data = data;
            parent = null;
            children = new ArrayList<>();
        }

        void printNode(int level) {
            String dashes = "";
            for(int j=0;j<level;j++) {
                dashes += "-";
            }
            System.out.println(dashes + this.data);
        }
    }
}