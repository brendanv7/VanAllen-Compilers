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

        buildSymbolTable(ast);
        scopeCheck(ast);

        // Reset the tree so null is returned and compilation does not continue
        if (errors > 0) {
            ast = null;
            System.out.println("SEMANTIC -- Semantic analysis failed with " + errors + " error(s) and " + warnings + " warning(s)\n");
        } else {
            System.out.println("SEMANTIC -- Semantic analysis completed successfully with " + warnings + " warning(s).\n");
            System.out.println("Symbol table for program " + programNum + ":");
            printSymbolTable();
            System.out.println();
        }

        return ast;
    }

    private static void scopeCheck(Tree ast) {
        Tree.Node current = ast.root;
        int scope = 0;

        scopeCheck(current, scope);
    }

    private static void scopeCheck(Tree.Node current, int scope) {
        for(Tree.Node n : current.children) {
            switch (n.data.data) {
                case "<Block>" : {
                    scope++;
                    scopeCheck(n, scope);
                    break;
                }

                case "<Print Statement>" : {
                    scopeCheck(n, scope);
                    break;
                }

                case "<Assignment Statement>" : {
                    if(n.children.size() > 0) {
                        HashTableRecord h = findSymbol(n.children.get(0).data.data, scope);
                        if (h != null) {
                            h.isInit = true;
                        }
                        scopeCheck(n, scope);
                    }
                    break;
                }

                case "<Variable Declaration>" : {
                    // Do nothing since symbol table is already built
                    break;
                }

                case "<While Statement>" : {
                    scopeCheck(n, scope);
                    break;
                }

                case "<If Statement>" : {
                    scopeCheck(n, scope);
                    break;
                }

                case "<true>" : {
                    // Do nothing
                    break;
                }

                case "<false>" : {
                    // Do nothing
                    break;
                }

                case "<!=>" : {
                    // Do nothing
                    break;
                }

                case "<==>" : {
                    // Do nothing
                    break;
                }

                case "<int>" : {
                    // Do nothing
                    break;
                }

                case "<string>" : {
                    // Do nothing
                    break;
                }

                case "<boolean>" : {
                    // Do nothing
                    break;
                }

                case "<Addition>" : {
                    //do nothing
                    break;
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

    private static void typeCheck(Tree ast) {
        Tree.Node current = ast.root;
        int scope = 0;

        typeCheck(current, scope);
    }

    private static void typeCheck(Tree.Node current, int scope) {
        for(Tree.Node n : current.children) {
            switch (n.data.data) {
                case "<Block>" : {
                    scope++;
                    typeCheck(n, scope);
                    break;
                }

                case "<Print Statement>" : {
                    typeCheck(n, scope);
                    break;
                }

                case "<Assignment Statement>" : {
                    if(n.children.size() > 0) {
                        HashTableRecord h = findSymbol(n.children.get(0).data.data, scope);
                        if (h != null) {
                            String type = h.type;
                            if (typeMatch(type, n.children.get(1), scope)) {

                            }
                        }
                        typeCheck(n, scope);
                    }
                    break;
                }

                case "<Variable Declaration>" : {
                    // Do nothing since symbol table is already built
                    break;
                }

                case "<While Statement>" : {
                    scopeCheck(n, scope);
                    break;
                }

                case "<If Statement>" : {
                    scopeCheck(n, scope);
                    break;
                }

                case "<true>" : {
                    // Do nothing
                    break;
                }

                case "<false>" : {
                    // Do nothing
                    break;
                }

                case "<!=>" : {
                    // Do nothing
                    break;
                }

                case "<==>" : {
                    // Do nothing
                    break;
                }

                case "<int>" : {
                    // Do nothing
                    break;
                }

                case "<string>" : {
                    // Do nothing
                    break;
                }

                case "<boolean>" : {
                    // Do nothing
                    break;
                }

                case "<Addition>" : {
                    //do nothing
                    break;
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

    private static boolean typeMatch(String type, Tree.Node expr, int scope) {
        switch (expr.data.data) {
            case "<Addition>" : {
                if(type.equals("<int>")) {
                    return typeMatch(type, expr.children.get(1));
                } else {
                    return false;
                }
                break;
            }

            case "<true>" : {
                return type.equals("<boolean>");
                break;
            }

            case "<false>" : {
                return type.equals("<boolean>");
                break;
            }

            case "<!=>" : {
                return type.equals("<boolean>");
                break;
            }

            case "<==>" : {
                return type.equals("<boolean>");
                break;
            }

            default : {
                // Remove < > to parse easier
                String s = expr.data.data.substring(1, expr.data.data.length()-1);
                if(s.substring(0,1).equals("\"")) {
                    return type.equals("<string>");
                } else {
                    try {
                        Integer.parseInt(s);
                        return type.equals("<int>");
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

    private static void buildSymbolTable(Tree ast) {
        Tree.Node current = ast.root;
        int scope = -1;

        scopeTree = new ScopeTree();

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
        while(scope >= 0) {
            for (HashTableRecord h : symbolTable) {
                if (symbol.equals(h.data.data) && scope == h.scope) {
                    return h;
                }
            }
            ScopeTree.Node parent = scopeTree.findScope(scope);
            if(parent != null) {
                parent = parent.parentScope;
                if (parent == null) {
                    scope = -1;
                } else {
                    scope = parent.scope;
                }
            }
        }
        return null;
    }
}
