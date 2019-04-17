import java.util.ArrayList;
import java.util.HashMap;

public class SemanticAnalyzer {
    public static ArrayList<HashTableRecord> symbolTable;
    public static ScopeTree scopeTree;
    private static int errors;
    private static int warnings;

    public static Tree analyze(Tree ast, int programNum) {
        errors = 0;
        warnings = 0;
        symbolTable = new ArrayList<>();

        System.out.println("SEMANTIC -- Analyzing program " + programNum + "...");
        System.out.println();

        buildSymbolTable(ast);

        System.out.println("AST for program " + programNum + ":");
        ast.printTree();
        printSymbolTable();

        return null;
    }

    private static void scopeCheck(Tree ast) {
        Tree.Node current = ast.root;
        int scope = -1;

        scopeCheck(current, scope);
    }

    private static void scopeCheck(Tree.Node current, int scope) {
        for(Tree.Node n : current.children) {
            switch (n.data.data) {
                case "<Block>" : {
                    scope++;
                    scopeCheck(n, scope);
                }

                case "<Print Statement>" : {
                    scopeCheck(n, scope);
                }

                case "<Assignment Statement" : {
                    HashTableRecord h = findSymbol(n.children.get(0).data.data, scope);
                    if(h != null) {
                        h.isInit = true;
                    }
                    scopeCheck(n, scope);
                }

                case "<Variable Declaration>" : {
                    // Do nothing since symbol table is already built
                }

                case "<While Statement>" : {
                    scopeCheck(n, scope);
                }

                case "<If Statement>" : {
                    scopeCheck(n, scope);
                }

                case "<true>" : {
                    // Do nothing
                }

                case "<false>" : {
                    // Do nothing
                }

                case "<!=>" : {
                    // Do nothing
                }

                case "<==>" : {
                    // Do nothing
                }

                case "<int>" : {
                    // Do nothing
                }

                case "<string>" : {
                    // Do nothing
                }

                case "<boolean>" : {
                    // Do nothing
                }

                default : {
                    // Remove < > to parse easier
                    String s = n.data.data.substring(1, n.data.data.length()-1);
                    if(s.substring(0,1).equals("\"")) {
                        // Ignore strings
                    } else {
                        try {
                            Integer.parseInt(s);
                        }
                        catch (NumberFormatException e) {
                            // Since s wasn't a number, we know its an identifier
                            s = "<" + s + ">";
                            HashTableRecord h = findSymbol(s, scope);
                            if (h == null) {
                                errors++;
                                System.out.println("SEMANTIC -- ERROR: Undeclared identifier " + s +
                                        " at (" + n.data.lineNum + ":" + n.data.position+ ")");
                            } else {
                                // If the identifier is not part of an assignment statement, than it is being used.
                                if(!current.data.data.equals("<Assignment Statement>")) {
                                    h.isUsed = true;
                                    // Provide warning if this identifier wasn't initialized
                                    if(!h.isInit) {
                                        warnings++;
                                        System.out.println("SEMANTIC -- WARNING: Identifier " + n.data.data + " is used but never initialized. " +
                                                "at (" + current.data.lineNum + ":" + current.data.position+ ")");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void buildSymbolTable(Tree ast) {
        Tree.Node current = ast.root;
        int scope = 0;

        scopeTree = new ScopeTree();
        scopeTree.openScope(0);

        buildSymbolTable(current, scope);
    }

    private static void buildSymbolTable(Tree.Node current, int scope) {
        boolean newScope = false;
        if(current.data.data.equals("<Block>")) {
            scope++;
            newScope = true;
            scopeTree.openScope(scope);
        }
        else if(current.data.data.equals("<Variable Declaration>")) {
            Token id = current.children.get(1).data;
            String type = current.children.get(0).data.data;

            // Check if this identifier is already used in this scope
            if(isDuplicateId(id.data, scope)) {
                errors++;
                System.out.println("SEMANTIC -- ERROR: Identifier " + id.data + " is already defined in this scope. " +
                        "at (" + current.data.lineNum + ":" + current.data.position+ ")");
            } else {
                HashTableRecord h = new HashTableRecord(id, type, scope);
                symbolTable.add(h);
            }
        }

        // Recursively go through all of this node's children
        for (int i = 0; i < current.children.size(); i++) {
            buildSymbolTable(current.children.get(i), scope);
        }

        if(newScope) {
            scope--;
            scopeTree.closeScope();
        }
    }

    private static boolean isDuplicateId(String id, int scope) {
        for (HashTableRecord h : symbolTable) {
            if(id.equals(h.data.data) && scope == h.scope) {
                return true;
            }
        }
        return false;
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

    private static HashTableRecord findSymbol(String symbol, int scope) {
        while(scope > 0) {
            for (HashTableRecord h : symbolTable) {
                if (symbol.equals(h.data.data) && scope == h.scope) {
                    return h;
                }
            }
            ScopeTree.Node parent = scopeTree.findScope(scope).parentScope;
            if(parent == null) {
                scope = -1;
            } else {
                scope = parent.scope;
            }
        }
        return null;
    }
}
