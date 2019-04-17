public class SemanticAnalyzer {
    int scope = 0;

    public Tree analyze(Tree cst, int programNum) {
        System.out.println("SEMANTIC -- Analyzing program " + programNum + "...");

        createAST(cst);

        return null;
    }

    public Tree createAST(Tree cst) {
        Tree.Node current = cst.root;
        Tree ast = new Tree();

        // Root node should always be Program
        if(!current.data.equals("<Program>")) {
            ast = null;
        } else {
            // First child of program should always be Block
            current = current.children.get(0);
            if(!current.data.equals("<Block>")) {
                ast = null;
            } else {
                // Root of AST will always be a Block
                ast.addNode(current.data);
                buildAST(ast, current);
            }
        }

        return ast;
    }

    private Tree buildAST (Tree ast, Tree.Node current) {
        for(Tree.Node child : current.children) {

            switch (child.data) {
                case "<Block>" : {
                    ast.addNode(child.data);
                }
            }

        }

        return null;
    }
}
