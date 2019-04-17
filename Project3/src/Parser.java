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
    private static int scope;

    public static Tree parse(ArrayList<Token> tokenList, int programNum) {
        tokens = tokenList;
        index = 0;
        currentToken = tokens.get(index);
        cst = new Tree();
        ast = new Tree();
        errors = 0;
        scope = 0;

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
                cst.addLeafNode(currentToken);
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
        cst.addNode(new Token("","<Program>", 0, 0));
        parseBlock();
        matchAndConsume("EOP");
    }

    /*
     * Block ::== { StatementList }
     */
    private static void parseBlock() {
        if (errors == 0) {
            System.out.println("PARSER -- parseBlock()");
            cst.addNode(new Token("","<Block>", 0, 0));
            ast.addNode(new Token("","<Block>", 0, 0));
            matchAndConsume("L_BRACE");
            parseStatementList();
            matchAndConsume("R_BRACE");
            cst.resetParent();
            ast.resetParent();
        }
    }

    /*
     * StatementList ::== Statement StatementList
     *               ::== ε
     */
    private static void parseStatementList() {
        if (errors == 0) {
            System.out.println("PARSER -- parseStatementList()");
            cst.addNode(new Token("","<StatementList>", 0, 0));
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
            cst.addNode(new Token("","<Statement>", 0, 0));
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
            cst.addNode(new Token("","<PrintStatement>", 0, 0));
            ast.addNode(new Token("","<Print Statement>", 0, 0));
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
            cst.addNode(new Token("","<AssignmentStatement>", 0, 0));
            ast.addNode(new Token("","<Assignment Statement>", 0, 0));
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
            cst.addNode(new Token("","<VarDecl>", 0, 0));
            ast.addNode(new Token("","<Variable Declaration>", 0, 0));
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
            cst.addNode(new Token("","<WhileStatement>", 0, 0));
            ast.addNode(new Token("","<While Statement>", 0, 0));
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
            cst.addNode(new Token("","<IfStatement>", 0, 0));
            ast.addNode(new Token("","<If Statement>", 0, 0));
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
            cst.addNode(new Token("","<Expr>", 0, 0));
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
            cst.addNode(new Token("","<IntExpr>", 0, 0));
            Token first = currentToken;
            matchAndConsume("DIGIT");
            if (currentToken.type.equals("INT_OP")) {
                ast.addNode(new Token("","<Addition>", 0, 0));
                parent = ast.currentNode;
                ast.addNode(new Token("", "<"+first.data+">", first.lineNum, first.position));
                ast.resetParent();
                matchAndConsume("INT_OP");
                parseExpr();
            } else {
                ast.addNode(new Token("", "<"+first.data+">", first.lineNum, first.position));
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
            cst.addNode(new Token("", "<StringExpr>", 0, 0));
            Token start = currentToken;
            matchAndConsume("QUOTE");
            String s = parseCharList();
            matchAndConsume("QUOTE");
            ast.addNode(new Token("","<\""+s+"\">", start.lineNum, start.position));
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
            cst.addNode(new Token("", "<BooleanExpr>", 0, 0));
            switch (currentToken.type) {
                case "L_PAREN": {
                    matchAndConsume("L_PAREN");
                    Tree.Node child = parseExpr();
                    ast.addNode(new Token("", "<"+currentToken.data+">", currentToken.lineNum, currentToken.position));
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
                    ast.addNode(new Token("", "<"+currentToken.data+">", currentToken.lineNum, currentToken.position));
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
            cst.addNode(new Token("", "<Id>", 0, 0));
            ast.addNode(new Token( "", "<"+currentToken.data+">", currentToken.lineNum, currentToken.position));
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
            cst.addNode(new Token("", "<CharList>", 0, 0));
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
                    ast.addNode(new Token("", "<"+currentToken.data+">", currentToken.lineNum, currentToken.position));
                    ast.resetParent();
                    matchAndConsume("INT");
                    break;
                }

                case "STRING": {
                    ast.addNode(new Token("", "<"+currentToken.data+">", currentToken.lineNum, currentToken.position));
                    ast.resetParent();
                    matchAndConsume("STRING");
                    break;
                }

                case "BOOL": {
                    ast.addNode(new Token("", "<"+currentToken.data+">", currentToken.lineNum, currentToken.position));
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
