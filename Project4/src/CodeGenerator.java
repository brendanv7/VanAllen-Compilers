import javafx.util.Pair;

import java.util.ArrayList;

public class CodeGenerator {

    public static ArrayList<String> image;
    private static ArrayList<StaticVarEntry> staticVars;
    private static int heapIndex;
    private static int codeIndex;
    private static int staticVarCount;
    private static int scope;
    private static boolean inAssignment;

    public static void generateCode(Tree ast, int programNum) {
        // Initialize the image to contain 00 for every byte.
        image = new ArrayList<>();
        for(int i=0; i < 256; i++) {
            image.add("00");
        }

        staticVars = new ArrayList<>();
        codeIndex = 0;
        heapIndex = 255;
        staticVarCount = 0;
        scope = 0;
        inAssignment = false;

        System.out.println("CODE GEN -- Beginning code gen for program "+ programNum + "...");

        // Add true and false to the heap for reference
        addToHeap("true");
        addToHeap("false");

        codeGen(ast.root);

        // Increment code index to separate code and static vars
        codeIndex++;

        System.out.println("CODE GEN -- Backpatching...");

        int staticStartIndex = codeIndex;

        for(StaticVarEntry s : staticVars) {
            String hex = Integer.toHexString(codeIndex).toUpperCase();
            codeIndex += 2;
            if(hex.length() == 1) {
                hex = "0" + hex;
            }
            s.setPermAddress(hex);
            System.out.println("CODE GEN -- Replacing [ " + s.tempAddress + " ] with [ " + s.permAddress + " ]");
            for(int i = 0; i<staticStartIndex; i++) {
                String temp = image.get(i);
                if(temp.equals(s.tempAddress)) {
                    image.set(i, s.permAddress);
                    image.set(i+1, "00");
                }
            }
        }

        System.out.println("CODE GEN -- Backpatching complete");

        printCode();
    }

    private static void codeGen(Tree.Node current) {
        System.out.println("CODE GEN -- Generating " + current.data.data + " on line " + current.data.lineNum);
        for(Tree.Node n : current.children) {
            switch (n.data.data) {
                case "<Block>" : {
                    scope++;
                    codeGen(n);
                    break;
                }

                case "<Print Statement>" : {
                    codeGen(n);
                    break;
                }

                case "<Assignment Statement>" : {
                    inAssignment = true;
                    codeGen(n);
                    if(n.children.size() > 0) {
                        String type = SemanticAnalyzer.findSymbol(n.children.get(0).data.data, scope).type;
                        if(type.equals("<int>")) {

                        }
                    }
                    break;
                }

                case "<Variable Declaration>" : {
                    if(n.children.size() > 0) {
                        int varScope = SemanticAnalyzer.findSymbol(n.children.get(1).data.data, scope).scope;
                        staticVars.add(new StaticVarEntry(n.children.get(1).data.data, staticVarCount, varScope));
                        image.set(codeIndex, "A9");
                        image.set(codeIndex+1, "00");
                        image.set(codeIndex+2, "8D");
                        image.set(codeIndex+3, "T"+staticVarCount);
                        image.set(codeIndex+4, "XX");
                        codeIndex += 5;
                    }
                    break;
                }

                case "<While Statement>" : {

                    break;
                }

                case "<If Statement>" : {

                    break;
                }

                case "<true>" : {
                    if(inAssignment) {
                        int
                    }
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
                    try {
                        int digit = Integer.parseInt(s);
                        if(inAssignment) {
                            String hex = Integer.toHexString(digit);
                            hex = "0" + hex;
                            image.set(codeIndex, "A9");
                            image.set(codeIndex+1, hex);
                            image.set(codeIndex+2, "8D");
                            String addr = findStaticVar(n.parent.children.get(0).data.data, scope).tempAddress;
                            image.set(codeIndex+3, addr);
                            image.set(codeIndex+4, "XX");
                            codeIndex += 5;
                        }
                    }
                    catch (NumberFormatException e) {
                        // Since s wasn't a number, we know its an identifier
                    }
                }
            }
        }
    }

    private static StaticVarEntry findStaticVar(String var, int scope) {
        for(StaticVarEntry s : staticVars) {
            if(s.id.equals(var)) {
                return s;
            }
        }
        return null;
    }

    private static void addToHeap(String data) {
        char[] temp = data.toCharArray();
        heapIndex -= temp.length;
        for(char c : temp) {
            String hex = Integer.toHexString((int) c).toUpperCase();
            image.set(heapIndex, hex);
            heapIndex++;
        }
        heapIndex -= temp.length+1;
        System.out.println("CODE GEN -- Added string [ " + data + " ] to heap at address " + (heapIndex+1));
    }

    public static void printCode() {
        for(int i=0; i<image.size(); i++) {
            if(i % 8 == 0) {
                System.out.println();
            }
            System.out.print(image.get(i) + " ");
        }
        System.out.println();
    }
}
