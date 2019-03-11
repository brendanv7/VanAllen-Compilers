import java.util.ArrayList;
import java.util.Scanner;

/**
 * A lexical analyzer for the class language of
 * CMPT 432-Compilers with Alan Labouseur.
 *
 * @author Brendan Van Allen
 * @version Spring 2019
 */
public class Lexer {
    private static int lineNum;
    private static int position;
    private static int program;
    private static int errors;

    public static void main(String args[]) {
        ArrayList<Token> tokens = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        String input;
        Token token = null;
        String keyword;
        boolean programComplete = true; // True to start so first print statement runs
        boolean tokenFound = false;
        boolean inComment = false;
        boolean inString = false;
        lineNum = 0;
        program = 1;
        errors = 0;

        while (scanner.hasNext()) {
            // Check for start of new program
            if (programComplete) {
                System.out.println("LEXER -- Lexing program " + program + "...");
                programComplete = false;
            }

            position = 0; // Reset position to beginning of line

            lineNum++;
            input = scanner.nextLine();
            char[] inputChars = input.toCharArray(); // Separating input into chars helps simplify operations

            for (int i = 0; i < inputChars.length; i++) {
                position++;
                char c = inputChars[i]; // Next character to analyze

                // Check for comment first, because a program with comments only is treated as no input
                if (c == '/') {
                    if (i + 1 < inputChars.length && inputChars[i+1] == '*') {
                        inComment = true;
                        // We know next character is '*', so we can skip it
                        i++;
                    }
                }

                // We don't want to/have time for lexing in comments
                if (!inComment) {
                    // Skip whitespace to next character
                    if (Character.isWhitespace(c))
                        continue;

                    // Everything inside a string is taken literally
                    if(inString && c != '"') {
                        token = new Token("CHAR", Character.toString(c), lineNum, position);
                        tokenFound = true;
                    } else {

                        // Sorry for switch/if statements when you recommended against it, Patterns are hard.
                        switch (c) {
                            // Keyword(s): boolean
                            case 'b': {
                                if (i + 6 < inputChars.length && (keyword = new String(inputChars, i, 7)).equals("boolean")) {
                                    // Found a keyword, add the token
                                    token = new Token("BOOL", keyword, lineNum, position);
                                    // Skip to end of keyword
                                    i += 6;
                                } else {
                                    // Not the keyword, so just add the char token
                                    token = new Token("CHAR", Character.toString(c), lineNum, position);
                                }
                                tokenFound = true;
                                break;
                            }

                            // Keyword(s): false
                            case 'f': {
                                if (i + 4 < inputChars.length && (keyword = new String(inputChars, i, 5)).equals("false")) {
                                    token = new Token("BOOL_VAL", keyword, lineNum, position);
                                    i += 4;
                                } else {
                                    token = new Token("CHAR", Character.toString(c), lineNum, position);
                                }
                                tokenFound = true;
                                break;
                            }

                            // Keyword(s): int, if
                            case 'i': {
                                // 'i' can start two keywords, so check both, starting with the longest
                                if (i + 2 < inputChars.length && (keyword = new String(inputChars, i, 3)).equals("int")) {
                                    token = new Token("INT", keyword, lineNum, position);
                                    i += 2;
                                } else if (i + 1 < inputChars.length && (keyword = new String(inputChars, i, 2)).equals("if")) {
                                    token = new Token("IF", keyword, lineNum, position);
                                    i += 1;
                                } else {
                                    token = new Token("CHAR", Character.toString(c), lineNum, position);
                                }
                                tokenFound = true;
                                break;
                            }

                            // Keyword(s): print
                            case 'p': {
                                if (i + 4 < inputChars.length && (keyword = new String(inputChars, i, 5)).equals("print")) {
                                    token = new Token("PRINT", keyword, lineNum, position);
                                    i += 4;
                                } else {
                                    token = new Token("CHAR", Character.toString(c), lineNum, position);
                                }
                                tokenFound = true;
                                break;
                            }

                            // Keyword(s): string
                            case 's': {
                                if (i + 5 < inputChars.length && (keyword = new String(inputChars, i, 6)).equals("string")) {
                                    token = new Token("STRING", keyword, lineNum, position);
                                    i += 5;
                                } else {
                                    token = new Token("CHAR", Character.toString(c), lineNum, position);
                                }
                                tokenFound = true;
                                break;
                            }

                            // Keyword(s): true
                            case 't': {
                                if (i + 3 < inputChars.length && (keyword = new String(inputChars, i, 4)).equals("true")) {
                                    token = new Token("BOOL_VAL", keyword, lineNum, position);
                                    i += 3;
                                } else {
                                    token = new Token("CHAR", Character.toString(c), lineNum, position);
                                }
                                tokenFound = true;
                                break;
                            }

                            // Keyword(s): while
                            case 'w': {
                                if (i + 4 < inputChars.length && (keyword = new String(inputChars, i, 5)).equals("while")) {
                                    token = new Token("WHILE", keyword, lineNum, position);
                                    i += 4;
                                } else {
                                    token = new Token("CHAR", Character.toString(c), lineNum, position);
                                }
                                tokenFound = true;
                                break;
                            }

                            case '{': {
                                token = new Token("L_BRACE", Character.toString(c), lineNum, position);
                                tokenFound = true;
                                break;
                            }

                            case '}': {
                                token = new Token("R_BRACE", Character.toString(c), lineNum, position);
                                tokenFound = true;
                                break;
                            }

                            case '(': {
                                token = new Token("L_PAREN", Character.toString(c), lineNum, position);
                                tokenFound = true;
                                break;
                            }

                            case ')': {
                                token = new Token("R_PAREN", Character.toString(c), lineNum, position);
                                tokenFound = true;
                                break;
                            }

                            case '"': {
                                token = new Token("QUOTE", Character.toString(c), lineNum, position);
                                tokenFound = true;
                                inString = !inString; // If we weren't in a string, we are now, and vice versa
                                break;
                            }

                            case '+': {
                                token = new Token("INT_OP", Character.toString(c), lineNum, position);
                                tokenFound = true;
                                break;
                            }

                            case '=': {
                                if (i + 1 < inputChars.length && inputChars[i + 1] == '=') {
                                    token = new Token("BOOL_OP", "==", lineNum, position);
                                    i += 1;
                                } else {
                                    token = new Token("ASSIGN_OP", Character.toString(c), lineNum, position);
                                }
                                tokenFound = true;
                                break;
                            }

                            case '!': {
                                if (i + 1 < inputChars.length && inputChars[i + 1] == '=') {
                                    token = new Token("BOOL_OP", "!=", lineNum, position);
                                    tokenFound = true;
                                    i += 1;
                                } else {
                                    errors++;
                                    printError("Unrecognized token [ " + Character.toString(c) + " ]");
                                }
                                break;
                            }

                            case '$': {
                                token = new Token("EOP", Character.toString(c), lineNum, position);
                                tokenFound = true;
                                programComplete = true;
                                break;
                            }

                            // If none of the above cases matched, c must be a char, digit, or something not in our language
                            default: {
                                // Only lowercase letters in our language
                                if (Character.isLetter(c) && Character.isLowerCase(c)) {
                                    token = new Token("CHAR", Character.toString(c), lineNum, position);
                                    tokenFound = true;
                                } else if (Character.isDigit(c)) {
                                    token = new Token("DIGIT", Character.toString(c), lineNum, position);
                                    tokenFound = true;
                                } else {
                                    // Token was not matched to anything, and must not be defined in our language
                                    errors++;
                                    printError("Unrecognized token [ " + Character.toString(c) + " ]");
                                }
                                break;
                            }

                        }
                    }
                    if (tokenFound) {
                        tokens.add(token);
                        printToken(token);
                        tokenFound = false;
                        position = i+1; // Regroup position with i now that we've printed the last token
                    }
                }
                // inComment = true
                else {
                    // See if the next character can get us out of the comment
                    if (c == '*' && i+1 < inputChars.length && inputChars[i+1] == '/') {
                        // We're now free to keep lexing
                        inComment = false;
                        i++;
                    }
                }
            }

            // Reached end of line, check for unterminated string
            if (inString) {
                errors++;
                printError("Unterminated string");
                inString = false; // Reset since string cannot continue onto next line
            }

            if (programComplete) {
                // Provide warning if program ended while in a comment.
                if (inComment) {
                    inComment = false;
                    printWarning("Program ended in open comment. Consider closing it.");
                }
                printEndOfProgram();

                errors = 0;

                program++;
            }
        }

        // Provide warning if program ended while in a comment.
        if (inComment) {
            printWarning("Program ended in open comment. Consider closing it");
        }

        // No input is an error.
        if (errors == 0 && tokens.size() == 0) {
            printError("No input found");
        }
        // Provide warning if last EOP character is omitted, and add it.
        else if (!tokens.get(tokens.size() - 1).data.equals("$")) {
            token = new Token("EOP", "$", lineNum, position);
            printWarning("No EOP character found and end-of-file has been reached.");
            printEndOfProgram();
        }
    }

    private static void printToken(Token token) {
        System.out.println("LEXER -- "+token.type+" [ "+token.data+" ] found at ("+token.lineNum+":"+token.position+")");
    }

    private static void printError(String message) {
        System.out.println("LEXER -- ERROR: "+message+" at ("+lineNum+":"+position+")");
    }

    private static void printWarning(String message) {
        System.out.println("LEXER -- WARNING: "+message+" at ("+lineNum+":"+position+")");
    }

    private static void printEndOfProgram() {
        if (errors == 0)
            // Lex has passed
            System.out.println("LEXER -- Lexing complete for program " + program + ". No errors found.\n");
        else
            // Lex has failed
            System.out.println("LEXER -- Lexing complete for program " + program + ". Lex failed with "+errors+" error(s).\n");
    }
}
