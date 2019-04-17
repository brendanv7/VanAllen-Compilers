import java.util.ArrayList;
import java.util.Scanner;

/**
 * A Java-implemented compiler for the class language of
 * CMPT 432 - Compilers with Alan Labouseur.
 *
 * @author Brendan Van Allen
 * @version Spring 2019
 */
public class Compiler {

    public static void main(String args[]) {
        Scanner scanner = new Scanner(System.in);
        int program = 0;
        int lineNum = 0;
        while(scanner.hasNext()) {
            program++;
            System.out.println("Compiling program " + program + "...\n");
            ArrayList<Token> tokens = Lexer.lex(scanner, program);
            if(tokens != null) {
                Tree cst = Parser.parse(tokens, program);
                if(cst != null) {
                    System.out.println("CST for program " + program + ":");
                    cst.printTree();
                    System.out.println();

                    Tree ast = Parser.ast;
                    System.out.println("AST for program " + program + ":");
                    ast.printTree();

                    // Semantic
                    SemanticAnalyzer.analyze(ast, program);
                    ////////

                    System.out.println();
                    System.out.println("Compilation complete (for now) for program " + program + ".\n");
                } else {
                    System.out.println("Compilation stopped for program " + program + " due to Parse error(s).\n");
                }
            } else {
                // Don't parse, error
                System.out.println("Compilation stopped for program " + program + " due to Lexical error(s).\n");
            }

            System.out.println("-------------------------------------------\n");
        }

        // One last check to make sure there was input
        if(program == 0) {
            System.out.println("ERROR: Empty file.");
        }

        scanner.close();
    }
}
