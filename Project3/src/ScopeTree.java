import java.util.ArrayList;

public class ScopeTree {
    Node root;
    Node currentNode;

    public ScopeTree() {
        root = null;
        currentNode = null;
    }

    public void openScope(int scope) {
        if(root == null) {
            root = new Node(scope, currentNode);
            currentNode = root;
        } else {
            Node child = new Node(scope, currentNode);
            currentNode.children.add(child);
            currentNode = new Node(scope, currentNode);
        }
    }

    public void closeScope() {
        if(currentNode != null && currentNode.parentScope != null) {
            currentNode = currentNode.parentScope;
        }
    }

    public Node findScope(int scope) {
        currentNode = root;
        if(currentNode != null) {
            return findScope(currentNode, scope);
        }
        return null;
    }

    private Node findScope(Node current, int scope) {
        if(current.scope == scope) {
            return currentNode;
        } else if (current.children.size() > 0) {
            for (Node child : current.children) {
                findScope(child, scope);
            }
        }
        return null;
    }

    public class Node {
        int scope;
        Node parentScope;
        ArrayList<Node> children;

        public Node(int scope, Node parentScope) {
            this.scope = scope;
            this.parentScope = parentScope;
            children = new ArrayList<>();
        }
    }
}
