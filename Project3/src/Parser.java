import java.util.ArrayList;

/**
 * A syntax parser for the class language of
 * CMPT 432-Compilers with Alan Labouseur.
 *
 * @author Brendan Van Allen
 * @version Spring 2019
 */
public class Parser {
    private static ArrayList<Token> tokens;
    private static int index;
    private static Token currentToken;
    private static Tree cst;
    private static int errors;
    public static Tree ast;

    public static Tree parse(ArrayList<Token> tokenList, int programNum) {
        tokens = tokenList;
        index = 0;
        currentToken = tokens.get(index);
        cst = new Tree();
        ast = new Tree();
        errors = 0;

        System.out.println("PARSER -- Parsing program " + programNum + "...");
        System.out.println("PARSER -- parse()");
        parseProgram();

        // Reset the tree so null is returned and compilation does not continue
        if (errors > 0) {
            cst = null;
            System.out.println("PARSER -- Parse failed with " + errors + " error(s)");
        } else {
            System.out.println("PARSER -- Parse completed successfully.\n");
        }

        return cst;
    }

    /*
     * Determines if the current token being parsed is what
     * is expected from the production rules. If it is, we
     * add it to the CST and move on to the next token. If
     * it is not, then we have found a parse error.
     */
    private static void matchAndConsume(String expectedToken) {
        // We don't need to recover after first error, so make sure we haven't found
        if (errors == 0) {
            if (currentToken.type.equals(expectedToken)) {
                cst.addLeafNode("[" + currentToken.data + "]");
                index++;
                if(index < tokens.size())
                    currentToken = tokens.get(index);
            } else {
                System.out.println("PARSER -- ERROR: Expected [" + expectedToken + "], " +
                        "found [" + currentToken.type + "] with value '" + currentToken.data + "' " +
                        "at (" + currentToken.lineNum + ":" + currentToken.position + ")");
                errors++;
            }
        }
    }

    /*
     * programNum ::== Block $
     */
    private static void parseProgram() {
        System.out.println("PARSER -- parseProgram()");
        cst.addNode("<Program>");
        parseBlock();
        matchAndConsume("EOP");
    }

    /*
     * Block ::== { StatementList }
     */
    private static void parseBlock() {
        if (errors == 0) {
            System.out.println("PARSER -- parseBlock()");
            cst.addNode("<Block>");
            ast.addNode("<Block>");
            matchAndConsume("L_BRACE");
            parseStatementList();
            matchAndConsume("R_BRACE");
            cst.resetParent();
        }
    }

    /*
     * StatementList ::== Statement StatementList
     *               ::== ε
     */
    private static void parseStatementList() {
        if (errors == 0) {
            System.out.println("PARSER -- parseStatementList()");
            cst.addNode("<StatementList>");
            switch (currentToken.type) {
                case "PRINT": {
                    parseStatement();
                    parseStatementList();
                    break;
                }

                case "CHAR": {
                    parseStatement();
                    parseStatementList();
                    break;
                }

                case "INT": {
                    parseStatement();
                    parseStatementList();
                    break;
                }

                case "STRING": {
                    parseStatement();
                    parseStatementList();
                    break;
                }

                case "BOOL": {
                    parseStatement();
                    parseStatementList();
                    break;
                }

                case "WHILE": {
                    parseStatement();
                    parseStatementList();
                    break;
                }

                case "IF": {
                    parseStatement();
                    parseStatementList();
                    break;
                }

                case "L_BRACE": {
                    parseStatement();
                    parseStatementList();
                    break;
                }

                default:
                    // Epsilon production
            }

            cst.resetParent();
        }
    }

    /*
     * Statement ::== PrintStatement
     *           ::== AssignmentStatement
     *           ::== VarDecl
     *           ::== WhileStatement
     *           ::== IfStatement
     *           ::== Block
     */
    private static void parseStatement() {
        if (errors == 0) {
            System.out.println("PARSER -- parseStatement()");
            cst.addNode("<Statement>");
            switch (currentToken.type) {
                case "PRINT": {
                    parsePrintStatement();
                    break;
                }

                case "CHAR": {
                    parseAssignmentStatement();
                    break;
                }

                case "INT": {
                    parseVarDecl();
                    break;
                }

                case "STRING": {
                    parseVarDecl();
                    break;
                }

                case "BOOL": {
                    parseVarDecl();
                    break;
                }

                case "WHILE": {
                    parseWhileStatement();
                    break;
                }

                case "IF": {
                    parseIfStatement();
                    break;
                }

                case "L_BRACE": {
                    parseBlock();
                    break;
                }

                default: {
                    String expectedTokens = "PRINT, CHAR, INT, STRING, BOOL, WHILE, IF, L_BRACE";
                    System.out.println("PARSER -- ERROR: Expected token in [" + expectedTokens + "] " +
                            "found [" + currentToken.type + "] with value '" + currentToken.data + "' " +
                            "at (" + currentToken.lineNum + ":" + currentToken.position + ")");
                }
            }

            cst.resetParent();
        }
    }

    /*
     * PrintStatement ::== print ( Expr )
     */
    private static void parsePrintStatement() {
        if (errors == 0) {
            System.out.println("PARSER -- parsePrintStatement()");
            cst.addNode("<PrintStatement>");
            ast.addNode("<Print Statement>");
            matchAndConsume("PRINT");
            matchAndConsume("L_PAREN");
            parseExpr();
            matchAndConsume("R_PAREN");
            cst.resetParent();
            ast.resetParent();
        }
    }

    /*
     * AssignmentStatement ::== Id = Expr
     */
    private static void parseAssignmentStatement() {
        if (errors == 0) {
            System.out.println("PARSER -- parseAssignmentStatement()");
            cst.addNode("<AssignmentStatement>");
            ast.addNode("<Assignment Statement>");
            parseId();
            matchAndConsume("ASSIGN_OP");
            parseExpr();
            cst.resetParent();
            ast.resetParent();
        }
    }

    /*
     * VarDecl ::== type Id
     */
    private static void parseVarDecl() {
        if (errors == 0) {
            System.out.println("PARSER -- parseVarDecl()");
            cst.addNode("<VarDecl>");
            ast.addNode("<Variable Declaration");
            parseType();
            parseId();
            cst.resetParent();
            ast.resetParent();
        }
    }

    /*
     * WhileStatement ::== while BooleanExpr Block
     */
    private static void parseWhileStatement() {
        if (errors == 0) {
            System.out.println("PARSER -- parseWhileStatement()");
            cst.addNode("<WhileStatement>");
            ast.addNode("<While Statement>");
            matchAndConsume("WHILE");
            parseBooleanExpr();
            parseBlock();
            cst.resetParent();
            ast.resetParent();
        }
    }

    /*
     * IfStatement ::== if BooleanExpr Block
     */
    private static void parseIfStatement() {
        if (errors == 0) {
            System.out.println("PARSER -- parseIfStatement()");
            cst.addNode("<IfStatement>");
            ast.addNode("<If Statement>");
            matchAndConsume("IF");
            parseBooleanExpr();
            parseBlock();
            cst.resetParent();
            ast.resetParent();
        }
    }

    /*
     * Expr ::== IntExpr
     *      ::== StringExpr
     *      ::== BooleanExpr
     *      ::== Id
     */
    private static Tree.Node parseExpr() {
        Tree.Node parent = null;
        if (errors == 0) {
            System.out.println("PARSER -- parseExpr()");
            cst.addNode("<Expr>");
            String value = "";
            switch (currentToken.type) {
                case "DIGIT": {
                    parent = parseIntExpr();
                    break;
                }

                case "QUOTE": {
                    parent = parseStringExpr();
                    break;
                }

                case "L_PAREN": {
                    parent = parseBooleanExpr();
                    break;
                }

                case "BOOL_VAL": {
                    parent = parseBooleanExpr();
                    break;
                }

                case "CHAR": {
                    parent = parseId();
                    break;
                }

                default: {
                    String expectedTokens = "DIGIT, QUOTE, L_PAREN, BOOL_VAL, CHAR";
                    System.out.println("PARSER -- ERROR: Expected token in [" + expectedTokens + "] " +
                            "found [" + currentToken.type + "] with value '" + currentToken.data + "' " +
                            "at (" + currentToken.lineNum + ":" + currentToken.position + ")");
                }
            }

            cst.resetParent();
        }

        return parent;
    }

    /*
     * IntExpr ::== digit intop Expr
     *         ::== digit
     */
    private static Tree.Node parseIntExpr() {
        Tree.Node parent = null;
        if (errors == 0) {
            System.out.println("PARSER -- parseIntExpr()");
            cst.addNode("<IntExpr>");
            String first = currentToken.data;
            matchAndConsume("DIGIT");
            if (currentToken.type.equals("INT_OP")) {
                ast.addNode("<Addition>");
                parent = ast.currentNode;
                ast.addNode("<"+first+">");
                ast.resetParent();
                matchAndConsume("INT_OP");
                parseExpr();
            } else {
                ast.addNode("<"+first+">");
                parent = ast.currentNode;
                ast.resetParent();
            }
            cst.resetParent();
        }
        return parent;
    }

    /*
     * StringExpr ::== " CharList "
     */
    private static Tree.Node parseStringExpr() {
        Tree.Node parent = null;
        if (errors == 0) {
            System.out.println("PARSER -- parseStringExpr()");
            cst.addNode("<StringExpr>");
            matchAndConsume("QUOTE");
            String s = parseCharList();
            matchAndConsume("QUOTE");
            ast.addNode("<\""+s+"\">");
            parent = ast.currentNode;
            ast.resetParent();
            cst.resetParent();
        }
        return parent;
    }

    /*
     * BooleanExpr ::== ( Expr boolop Expr )
     *             ::== boolval
     */
    private static Tree.Node parseBooleanExpr() {
        Tree.Node parent = null;
        if (errors == 0) {
            System.out.println("PARSER -- parseBooleanExpr()");
            cst.addNode("<BooleanExpr>");
            switch (currentToken.type) {
                case "L_PAREN": {
                    matchAndConsume("L_PAREN");
                    Tree.Node child = parseExpr();
                    ast.addNode("<"+currentToken.data+">");
                    parent = ast.currentNode;
                    parent.children.add(child);
                    child.parent.children.remove(child);
                    child.parent = parent;
                    matchAndConsume("BOOL_OP");
                    parseExpr();
                    matchAndConsume("R_PAREN");
                    break;
                }

                case "BOOL_VAL": {
                    ast.addNode("<"+currentToken.data+">");
                    parent = ast.currentNode;
                    ast.resetParent();
                    matchAndConsume("BOOL_VAL");
                    break;
                }

                default: {
                    String expectedTokens = "L_PAREN, BOOL_VAL";
                    System.out.println("PARSER -- ERROR: Expected token in [" + expectedTokens + "] " +
                            "found [" + currentToken.type + "] with value '" + currentToken.data + "' " +
                            "at (" + currentToken.lineNum + ":" + currentToken.position + ")");
                }
            }

            cst.resetParent();
        }

        return parent;
    }

    /*
     * Id ::== char
     */
    private static Tree.Node parseId() {
        Tree.Node parent = null;
        if (errors == 0) {
            System.out.println("PARSER -- parseId()");
            cst.addNode("<Id>");
            ast.addNode("<"+currentToken.data+">");
            parent = ast.currentNode;
            ast.resetParent();
            matchAndConsume("CHAR");
            cst.resetParent();
        }

        return parent;
    }

    /*
     * CharList ::== char CharList
     *          ::== space CharList
     *          ::== ε
     */
    private static String parseCharList() {
        String s = "";
        if (errors == 0) {
            System.out.println("PARSER -- parseCharList()");
            cst.addNode("<CharList>");
            switch (currentToken.type) {
                case "CHAR": {
                    s = currentToken.data;
                    matchAndConsume("CHAR");
                    s += parseCharList();
                    break;
                }

                case "SPACE": {
                    s = " ";
                    matchAndConsume("SPACE");
                    s += parseCharList();
                    break;
                }

                default:
                    // Epsilon production
            }
            
            cst.resetParent();
        }
        return s;
    }

    /*
     * type ::== int | string | boolean
     *
     * I know type is a not a non-terminal, but this
     * method helps the aesthetics of the code.
     */
    private static void parseType() {
        if (errors == 0) {
            switch (currentToken.type) {
                case "INT": {
                    ast.addNode("<"+currentToken.data+">");
                    ast.resetParent();
                    matchAndConsume("INT");
                    break;
                }

                case "STRING": {
                    ast.addNode("<"+currentToken.data+">");
                    ast.resetParent();
                    matchAndConsume("STRING");
                    break;
                }

                case "BOOL": {
                    ast.addNode("<"+currentToken.data+">");
                    ast.resetParent();
                    matchAndConsume("BOOL");
                    break;
                }

                default: {
                    String expectedTokens = "INT, STRING, BOOL";
                    System.out.println("PARSER -- ERROR: Expected token in [" + expectedTokens + "] " +
                            "found [" + currentToken.type + "] with value '" + currentToken.data + "' " +
                            "at (" + currentToken.lineNum + ":" + currentToken.position + ")");
                }
            }
        }
    }
}
