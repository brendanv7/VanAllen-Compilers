import java.util.ArrayList;
import java.util.Scanner;

/**
 * A Java-implementated compiler for the class language of
 * CMPT 432 - Compilers with Alan Labouseur.
 *
 * @author Brendan Van Allen
 * @version Spring 2019
 */
public class Compiler {

    public static void main(String args[]) {
        Scanner scanner = new Scanner(System.in);
        int program = 1;
        int lineNum = 0;
        while(scanner.hasNext()) {
            System.out.println("Compiling program " + program + "...\n");
            ArrayList<Token> tokens = Lexer.lex(scanner, program, lineNum);
            if(tokens != null) {
                Tree cst = Parser.parse(tokens, program);
                if(cst != null) {
                    System.out.println("CST for program " + program + ":");
                    cst.printTree();
                    // Semantic
                    System.out.println("Compilation complete (for now) for program " + program + ".\n");
                } else {
                    System.out.println("Compilation stopped for program " + program + " due to Parse error(s).\n");
                }
            } else {
                // Don't parse, error
                System.out.println("Compilation stopped for program " + program + " due to Lexical error(s).\n");
            }
            program++;
        }

        scanner.close();
    }

}
