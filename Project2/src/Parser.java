import java.util.ArrayList;

/**
 * A syntax parser for the class language of
 * CMPT 432-Compilers with Alan Labouseur.
 *
 * @author Brendan Van Allen
 * @version Spring 2019
 */
public class Parser {
    private static int program;
    private static Token currentToken;
    private static Tree cst;

    public static Tree parse(ArrayList<Token> tokens, int programNum) {
        program = programNum;
        cst = new Tree();

        parseProgram();

        return null;
    }

    private static void matchAndConsume(String expectedToken) {
        if(currentToken.type.equals(expectedToken)) {
            cst.addNode("["+ currentToken.data + "]");
        } else {
            System.out.println("PARSER -- ERROR: Expected [" + expectedToken + "], " +
                    "found [" + currentToken.type + "] with value '" + currentToken.data + "' " +
                    "at (" + currentToken.lineNum+":"+currentToken.position+")");
        }
    }

    private static void resetParent() {
        if(cst.currentNode != null && cst.currentNode.parent != null)
            cst.currentNode = cst.currentNode.parent;
    }

    private static void parseProgram() {
        cst.addNode("<Program>");
        parseBlock();
    }

    private static void parseBlock() {
        cst.addNode("<Block>");
        matchAndConsume("L_BRACE");
        parseStatementList();
        matchAndConsume("R_BRACE");
        resetParent();
    }

    private static void parseStatementList() {
        cst.addNode("<StatementList>");
        matchAndConsume("L_BRACE");
        parseStatementList();
        matchAndConsume("R_BRACE");
        resetParent();
    }

    private static void parseStatement() {

    }

    private static void parsePrintStatement() {

    }

    private static void parseAssignmentStatement() {

    }

    private static void parseVarDecl() {

    }

    private static void parseWhileStatement() {

    }

    private static void parseIfStatement() {

    }

    private static void parseExpr() {

    }

    private static void parseIntExpr() {

    }

    private static void parseStringExpr() {

    }

    private static void parseId() {

    }

    private static void parseCharList() {

    }

    private static void printError(String message) {

    }

}
