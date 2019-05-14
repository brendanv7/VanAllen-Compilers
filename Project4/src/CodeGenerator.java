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
    private static boolean inAddition;

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
        System.out.println("CODE GEN -- Generating " + current.data.data);
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
                        staticVarCount++;
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
                        image.set(codeIndex, "A9");
                        image.set(codeIndex+1, "FB"); // Address of 'true' in heap memory
                        image.set(codeIndex+2, "8D");
                        String addr = findStaticVar(n.parent.children.get(0).data.data, scope).tempAddress;
                        image.set(codeIndex+3, addr);
                        image.set(codeIndex+4, "XX");
                        codeIndex += 5;
                    }
                    break;
                }

                case "<false>" : {
                    if(inAssignment) {
                        image.set(codeIndex, "A9");
                        image.set(codeIndex+1, "F5"); // Address of 'false' in heap memory
                        image.set(codeIndex+2, "8D");
                        String addr = findStaticVar(n.parent.children.get(0).data.data, scope).tempAddress;
                        image.set(codeIndex+3, addr);
                        image.set(codeIndex+4, "XX");
                        codeIndex += 5;
                    }
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
                    inAddition = true;
                    codeGen(n);
                    if(inAssignment) {

                    }
                    break;
                }

                default : {
                    // Remove < > to parse easier
                    String s = n.data.data.substring(1, n.data.data.length()-1);
                    if(s.substring(0,1).equals("\"")) {
                        if(inAssignment) {
                            addToHeap(s.substring(1, s.length()-1));
                            String heapAddr = Integer.toHexString(heapIndex+1).toUpperCase();
                            image.set(codeIndex, "A9");
                            image.set(codeIndex+1, heapAddr); // Address of 'false' in heap memory
                            image.set(codeIndex+2, "8D");
                            String staticAddr = findStaticVar(n.parent.children.get(0).data.data, scope).tempAddress;
                            image.set(codeIndex+3, staticAddr);
                            image.set(codeIndex+4, "XX");
                            codeIndex += 5;
                        }

                    } else {
                        try {
                            int digit = Integer.parseInt(s);
                            if(inAddition) {

                            }
                            if (inAssignment) {
                                String hex = "0" + Integer.toHexString(digit);
                                image.set(codeIndex, "A9");
                                image.set(codeIndex + 1, hex);
                                image.set(codeIndex + 2, "8D");
                                String addr = findStaticVar(n.parent.children.get(0).data.data, scope).tempAddress;
                                image.set(codeIndex + 3, addr);
                                image.set(codeIndex + 4, "XX");
                                codeIndex += 5;
                            }
                        } catch (NumberFormatException e) {
                            // Since s wasn't a number, we know its an identifier
                        }
                    }
                }
            }
        }
    }

    private static void generateAddition(Tree.Node current, int scope) {
        if(current.data.data.equals("<Addition>")) {
            generateAddition(current.children.get(1), scope);
            int digit = Integer.parseInt(current.children.get(0).data.data.substring(1, current.children.get(0).data.data.length()-1));
            String hex = "0" + Integer.toHexString(digit);
            image.set(codeIndex, "A9");
            image.set(codeIndex+1, hex);
        } else {
            try {
                int digit = Integer.parseInt(current.data.data.substring(1, current.data.data.length()-1));
                String hex = "0" + Integer.toHexString(digit);
                image.set(codeIndex, "A9");
                image.set(codeIndex+1, hex);
                image.set(codeIndex+2, "8D");
                image.set(codeIndex+3, "00");
                image.set(codeIndex+4, "00");
                codeIndex += 5;

            }
            catch (NumberFormatException e) {
                // ID
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
        heapIndex -= temp.length+1; // Add 1 to include break
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
