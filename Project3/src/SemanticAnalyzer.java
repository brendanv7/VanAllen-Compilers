import java.util.ArrayList;

public class SemanticAnalyzer {
    public static ArrayList<HashTableRecord> symbolTable;

    public static Tree analyze(Tree ast, int programNum) {
        System.out.println("SEMANTIC -- Analyzing program " + programNum + "...");
        System.out.println();

        symbolTable = new ArrayList<>();
        buildSymbolTable(ast);
        printSymbolTable();

        return null;
    }

    private static void buildSymbolTable(Tree ast) {
        Tree.Node current = ast.root;
        int scope = 0;

        traverseTree(current, scope);
    }

    public static void traverseTree(Tree.Node current, int scope) {
        boolean newScope = false;
        if(current.data.data.equals("<Block>")) {
            scope++;
            newScope = true;
        }
        else if(current.data.data.equals("<Variable Declaration>")) {
            HashTableRecord h = new HashTableRecord(current.children.get(1).data, current.children.get(0).data.data, scope);
            symbolTable.add(h);
        }

        // Recursively go through all of this node's children
        for (int i = 0; i < current.children.size(); i++) {
            traverseTree(current.children.get(i), scope);
        }

        if(newScope) {
            scope--;
        }
    }

    private static void printSymbolTable() {
        System.out.println("ID\tType\t\tScope\tLine\tPosition");
        for (HashTableRecord h : symbolTable) {
            if(h.type.equals("<int>")){
                System.out.println(h.data.data + "\t" + h.type + "\t\t" + h.scope + "\t" + h.data.lineNum + "\t" + h.data.position);
            } else {
                System.out.println(h.data.data + "\t" + h.type + "\t" + h.scope + "\t" + h.data.lineNum + "\t" + h.data.position);
            }
        }
    }


}
