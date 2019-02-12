import java.util.ArrayList;
import java.util.Scanner;

public class Lexer {
    private static int lineNum;
    private static int position;

    public static void main(String args[]) {
        int program = 1;
        boolean error = false;
        boolean programComplete = false;
        ArrayList<String[]> tokens = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        String input = null;
        String[] token = new String[2];
        String keyword;
        boolean tokenFound = false;
        lineNum = 0;
        while (scanner.hasNext()) {
            if(lineNum == 0)
                System.out.println("LEXER -- Lexing program "+program+"...");

            position = 0;

            lineNum++;
            input = scanner.nextLine();
            char[] inputChars = input.toCharArray(); // To analyze char by char.

            for (int i = 0; i < inputChars.length; i++) {
                position++;
                char c = inputChars[i];

                // Skip whitespace to next character
                if(Character.isWhitespace(c))
                    continue;

                switch (c) {
                    case 'b': {
                        if (i + 6 < inputChars.length) {
                            keyword = new String(inputChars, i, 7);
                            if (keyword.equals("boolean")) {
                                token = new String[]{"B_TYPE",keyword};
                                i += 6;
                            } else {
                                token = new String[]{"CHAR",Character.toString(c)};
                            }
                        } else {
                            token = new String[]{"CHAR",Character.toString(c)};
                        }
                        tokenFound = true;
                        break;
                    }

                    case 'f': {
                        if (i + 4 < inputChars.length) {
                            keyword = new String(inputChars, i, 5);
                            if (keyword.equals("false")) {
                                token = new String[]{"BOOL_VAL",keyword};
                                i += 4;
                            } else {
                                token = new String[]{"CHAR",Character.toString(c)};
                            }
                        } else {
                            token = new String[]{"CHAR",Character.toString(c)};
                        }
                        tokenFound = true;
                        break;
                    }

                    case 'i': {
                        if (i + 2 < inputChars.length) {
                            keyword = new String(inputChars, i, 3);
                            if (keyword.equals("int")) {
                                token = new String[]{"I_TYPE",keyword};
                                i += 2;
                            } else if(keyword.substring(0,2).equals("if")) {
                                token = new String[]{"IF",keyword.substring(0,2)};
                                i += 2;
                            } else {
                                token = new String[]{"CHAR",Character.toString(c)};
                            }
                        } else {
                            token = new String[]{"CHAR",Character.toString(c)};
                        }
                        tokenFound = true;
                        break;
                    }

                    case 'p': {
                        if (i + 4 < inputChars.length) {
                            keyword = new String(inputChars, i, 5);
                            if (keyword.equals("print")) {
                                token = new String[]{"PRINT",keyword};
                                i += 4;
                            } else {
                                token = new String[]{"CHAR",Character.toString(c)};
                            }
                        } else {
                            token = new String[]{"CHAR",Character.toString(c)};
                        }
                        tokenFound = true;
                        break;
                    }

                    case 's': {
                        if (i + 5 < inputChars.length) {
                            keyword = new String(inputChars, i, 6);
                            if (keyword.equals("string")) {
                                token = new String[]{"STRING",keyword};
                                i += 5;
                            } else {
                                token = new String[]{"CHAR",Character.toString(c)};
                            }
                        } else {
                            token = new String[]{"CHAR",Character.toString(c)};
                        }
                        tokenFound = true;
                        break;
                    }

                    case 't': {
                        if (i + 3 < inputChars.length) {
                            keyword = new String(inputChars, i, 4);
                            if (keyword.equals("true")) {
                                token = new String[]{"BOOL_VAL",keyword};
                                i += 4;
                            } else {
                                token = new String[]{"CHAR",Character.toString(c)};
                            }
                        } else {
                            token = new String[]{"CHAR",Character.toString(c)};
                        }
                        tokenFound = true;
                        break;
                    }

                    case 'w': {
                        if (i + 4 < inputChars.length) {
                            keyword = new String(inputChars, i, 5);
                            if (keyword.equals("while")) {
                                token = new String[]{"WHILE",keyword};
                                i += 4;
                            } else {
                                token = new String[]{"CHAR",Character.toString(c)};
                            }
                        } else {
                            token = new String[]{"CHAR",Character.toString(c)};
                        }
                        tokenFound = true;
                        break;
                    }

                    case '{': {
                        token = new String[]{"R_BRACE",Character.toString(c)};
                        tokenFound = true;
                        break;
                    }

                    case '}': {
                        token = new String[]{"L_BRACE",Character.toString(c)};
                        tokenFound = true;
                        break;
                    }

                    case '(': {
                        token = new String[]{"R_PAREN",Character.toString(c)};
                        tokenFound = true;
                        break;
                    }

                    case ')': {
                        token = new String[]{"L_PAREN",Character.toString(c)};
                        tokenFound = true;
                        break;
                    }

                    case '"': {
                        token = new String[]{"QUOTE",Character.toString(c)};
                        tokenFound = true;
                        break;
                    }

                    case '+': {
                        token = new String[]{"INT_OP",Character.toString(c)};
                        tokenFound = true;
                        break;
                    }

                    case '=': {
                        if (i+1 < inputChars.length && inputChars[i+1] == '=') {
                            token = new String[]{"BOOL_OP","=="};
                                i += 1;
                        } else {
                            token = new String[]{"ASSIGN_OP",Character.toString(c)};
                        }
                        tokenFound = true;
                        break;
                    }

                    case '!': {
                        if (i+1 < inputChars.length && inputChars[i+1] == '=') {
                            token = new String[]{"BOOL_OP","!="};
                            i += 1;
                        } else {
                            error = true;
                            printError("Unrecognized token [ "+Character.toString(c)+" ]");
                        }
                        tokenFound = true;
                        break;
                    }

                    case '$': {
                        token = new String[]{"EOP","$"};
                        tokenFound = true;
                        programComplete = true;
                        break;
                    }

                    case '/': {
                        token = new String[]{"EOP","$"};
                        tokenFound = true;
                        programComplete = true;
                        break;
                    }

                    default: {
                        if(Character.isLetter(c)) {
                            token = new String[]{"CHAR",Character.toString(c)};
                        } else if(Character.isDigit(c)) {
                            token = new String[]{"DIGIT",Character.toString(c)};
                        } else {
                            
                        }
                    }

                }
                if(tokenFound){
                    tokens.add(token);
                    printToken(token);
                    tokenFound = false;
                } else if(error) {
                    programComplete = true;
                    break;
                }
            }

            if(programComplete) {
                if (error) {
                    System.out.println("LEXER -- Error found. Exiting lex for program " + program + ".");
                } else {
                    System.out.println("LEXER -- Lexing complete for program " + program + ". No errors found.\n");
                }
                programComplete = false;
                lineNum = 0;
                program++;
            }
        }
        // No input is an error
        if(input == null) {
            printError("No input found.");
        }
    }

    private static void printToken(String[] token) {
        System.out.println("LEXER -- "+token[0]+" [ "+token[1]+" ] found at ("+lineNum+":"+position+")");
    }

    private static void printError(String message) {
        System.out.println("LEXER -- ERROR: "+message+" at ("+lineNum+":"+position+")");
    }
}
