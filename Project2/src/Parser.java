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
    private static int program;
    private static int index;
    private static Token currentToken;
    private static Tree cst;
    private static int errors;

    public static Tree parse(ArrayList<Token> tokenList, int programNum) {
        tokens = tokenList;
        program = programNum;
        index = 0;
        currentToken = tokens.get(index);
        cst = new Tree();
        errors = 0;

        System.out.println("PARSER -- Parsing program " + program + "...");
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

    private static void resetParent() {
        if(cst.currentNode != null && cst.currentNode.parent != null)
            cst.currentNode = cst.currentNode.parent;
    }

    private static void parseProgram() {
        System.out.println("PARSER -- parseProgram()");
        cst.addNode("<Program>");
        parseBlock();
        matchAndConsume("EOP");
    }

    private static void parseBlock() {
        if (errors == 0) {
            System.out.println("PARSER -- parseBlock()");
            cst.addNode("<Block>");
            matchAndConsume("L_BRACE");
            parseStatementList();
            matchAndConsume("R_BRACE");
            resetParent();
        }
    }

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

            resetParent();
        }
    }

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

            resetParent();
        }
    }

    private static void parsePrintStatement() {
        if (errors == 0) {
            System.out.println("PARSER -- parsePrintStatement()");
            cst.addNode("<PrintStatement>");
            matchAndConsume("PRINT");
            matchAndConsume("L_PAREN");
            parseExpr();
            matchAndConsume("R_PAREN");
            resetParent();
        }
    }

    private static void parseAssignmentStatement() {
        if (errors == 0) {
            System.out.println("PARSER -- parseAssignmentStatement()");
            cst.addNode("<AssignmentStatement>");
            parseId();
            matchAndConsume("ASSIGN_OP");
            parseExpr();
            resetParent();
        }
    }

    private static void parseVarDecl() {
        if (errors == 0) {
            System.out.println("PARSER -- parseVarDecl()");
            cst.addNode("<VarDecl>");
            parseType();
            parseId();
            resetParent();
        }
    }

    private static void parseWhileStatement() {
        if (errors == 0) {
            System.out.println("PARSER -- parseWhileStatement()");
            cst.addNode("<WhileStatement>");
            matchAndConsume("WHILE");
            parseBooleanExpr();
            parseBlock();
            resetParent();
        }
    }

    private static void parseIfStatement() {
        if (errors == 0) {
            System.out.println("PARSER -- parseIfStatement()");
            cst.addNode("<IfStatement>");
            matchAndConsume("IF");
            parseBooleanExpr();
            parseBlock();
            resetParent();
        }
    }

    private static void parseExpr() {
        if (errors == 0) {
            System.out.println("PARSER -- parseExpr()");
            cst.addNode("<Expr>");
            switch (currentToken.type) {
                case "DIGIT": {
                    parseIntExpr();
                    break;
                }

                case "QUOTE": {
                    parseStringExpr();
                    break;
                }

                case "L_PAREN": {
                    parseBooleanExpr();
                    break;
                }

                case "BOOL_VAL": {
                    parseBooleanExpr();
                    break;
                }

                case "CHAR": {
                    parseId();
                    break;
                }

                default: {
                    String expectedTokens = "DIGIT, QUOTE, L_PAREN, BOOL_VAL, CHAR";
                    System.out.println("PARSER -- ERROR: Expected token in [" + expectedTokens + "] " +
                            "found [" + currentToken.type + "] with value '" + currentToken.data + "' " +
                            "at (" + currentToken.lineNum + ":" + currentToken.position + ")");
                }
            }

            resetParent();
        }
    }

    private static void parseIntExpr() {
        if (errors == 0) {
            System.out.println("PARSER -- parseIntExpr()");
            cst.addNode("<IntExpr>");
            matchAndConsume("DIGIT");
            if (currentToken.type.equals("INT_OP")) {
                matchAndConsume("INT_OP");
                parseExpr();
            }
            resetParent();
        }
    }

    private static void parseStringExpr() {
        if (errors == 0) {
            System.out.println("PARSER -- parseStringExpr()");
            cst.addNode("<StringExpr>");
            matchAndConsume("QUOTE");
            parseCharList();
            matchAndConsume("QUOTE");
            resetParent();
        }
    }

    private static void parseBooleanExpr() {
        if (errors == 0) {
            System.out.println("PARSER -- parseBooleanExpr()");
            cst.addNode("<BooleanExpr>");
            switch (currentToken.type) {
                case "L_PAREN": {
                    matchAndConsume("L_PAREN");
                    parseExpr();
                    matchAndConsume("BOOL_OP");
                    parseExpr();
                    matchAndConsume("R_PAREN");
                    break;
                }

                case "BOOL_VAL": {
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

            resetParent();
        }
    }

    private static void parseId() {
        if (errors == 0) {
            System.out.println("PARSER -- parseId()");
            cst.addNode("<Id>");
            matchAndConsume("CHAR");
            resetParent();
        }
    }

    private static void parseCharList() {
        if (errors == 0) {
            System.out.println("PARSER -- parseCharList()");
            cst.addNode("<CharList>");
            switch (currentToken.type) {
                case "CHAR": {
                    matchAndConsume("CHAR");
                    parseCharList();
                    break;
                }

                case "SPACE": {
                    matchAndConsume("SPACE");
                    parseCharList();
                    break;
                }

                default:
                    // Epsilon production
            }
        }
    }

    private static void parseType() {
        if (errors == 0) {
            switch (currentToken.type) {
                case "INT": {
                    matchAndConsume("INT");
                    break;
                }

                case "STRING": {
                    matchAndConsume("STRING");
                    break;
                }

                case "BOOL": {
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
