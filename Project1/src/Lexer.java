import java.util.ArrayList;
import java.util.Scanner;

public class Lexer {

    public static void main(String args[]) {
        int lineNum = 0;
        int position = 0;
        int program = 1;
        boolean error = false;
        ArrayList<String> tokens = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        String input;
        while (scanner.hasNext()) {
            lineNum++;
            input = scanner.nextLine();

            input = input.trim(); // Remove leading and trailing spaces and tabs

            char[] inputChars = input.toCharArray(); // To analyze char by char.

            for (int i = 0; i < inputChars.length; i++) {
                position++;
                char c = inputChars[i];
                if
            }
        }

        // lineNum can only be 0 if there was no input
        if(lineNum == 0) {
            System.out.println("LEXER -- ERROR! No input found.");
            error = true;
        }

        if(error) {
            System.out.println("LEXER -- Error found. Ending lex.");
        }
    }

    private void setRegexes(){

    }
}
