import java.util.ArrayList;

public class CodeGenerator {

    public static ArrayList<String> image;
    private static ArrayList<StaticVarEntry> staticVars;
    private static int heapIndex;
    private static int codeIndex;
    private static int staticVarCount;
    private static int scope;
    private static boolean inAssignment;
    private static boolean inPrint;
    private static int errors;

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
        inPrint = false;
        errors = 0;

        System.out.println("CODE GEN -- Beginning code gen for program "+ programNum + "...");

        // Add true and false to the heap for reference
        addToHeap("true");
        addToHeap("false");

        codeGen(ast.root);

        // Increment code index to separate code and static vars
        codeIndex++;

        if (codeIndex + staticVarCount >= heapIndex) {
            System.out.println("CODE GEN -- ERROR: Memory overflow. Please shorten the program.");
            errors++;
        }

        if(errors == 0) {
            System.out.println("CODE GEN -- Backpatching...");

            int staticStartIndex = codeIndex;

            for (StaticVarEntry s : staticVars) {
                String hex = Integer.toHexString(codeIndex).toUpperCase();
                codeIndex++;
                if (hex.length() == 1) {
                    hex = "0" + hex;
                }
                s.setPermAddress(hex);
                System.out.println("CODE GEN -- Replacing [ " + s.tempAddress + " ] with [ " + s.permAddress + " ]");
                for (int i = 0; i < staticStartIndex; i++) {
                    String temp = image.get(i);
                    if (temp.equals(s.tempAddress)) {
                        image.set(i, s.permAddress);
                        image.set(i + 1, "00");
                    }
                }
            }

            System.out.println("CODE GEN -- Backpatching complete");

            printCode();
        } else {
            System.out.println("CODE GEN -- Code gen failed with " + errors + " error(s).");
        }
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
                    inPrint = true;
                    codeGen(n);
                    image.set(codeIndex, "FF");
                    codeIndex++;
                    inPrint = false;
                    break;
                }

                case "<Assignment Statement>" : {
                    inAssignment = true;
                    codeGen(n);
                    inAssignment = false;
                    break;
                }

                case "<Variable Declaration>" : {
                    System.out.println("CODE GEN -- Generating <VariableDeclaration>");
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
                        codeIndex += 2;
                        storeToStatic(n);
                    } else if (inPrint) {
                        image.set(codeIndex, "A0");
                        image.set(codeIndex+1, "FB");
                        image.set(codeIndex+2, "A2");
                        image.set(codeIndex+3, "02");
                        codeIndex += 4;
                    }
                    break;
                }

                case "<false>" : {
                    if(inAssignment) {
                        image.set(codeIndex, "A9");
                        image.set(codeIndex+1, "F5"); // Address of 'false' in heap memory
                        codeIndex += 2;
                        storeToStatic(n);
                    } else if (inPrint) {
                        image.set(codeIndex, "A0");
                        image.set(codeIndex+1, "F5");
                        image.set(codeIndex+2, "A2");
                        image.set(codeIndex+3, "02");
                        codeIndex += 4;
                    }
                    break;
                }

                case "<!=>" : {
                    generateEquality(n);
                    if(inAssignment) {
                        image.set(codeIndex, "A9");
                        image.set(codeIndex+1, "FB");
                        image.set(codeIndex+2, "D0");
                        image.set(codeIndex+3, "02");
                        image.set(codeIndex+4, "A9");
                        image.set(codeIndex+5, "F5");
                        codeIndex += 6;
                        storeToStatic(n);
                    } else if (inPrint) {
                        image.set(codeIndex, "A0");
                        image.set(codeIndex+1, "FB");
                        image.set(codeIndex+2, "D0");
                        image.set(codeIndex+3, "02");
                        image.set(codeIndex+4, "A0");
                        image.set(codeIndex+5, "F5");
                        image.set(codeIndex+6, "A2");
                        image.set(codeIndex+7, "02");
                        codeIndex += 8;
                    }
                    break;
                }

                case "<==>" : {
                    generateEquality(n);
                    if(inAssignment) {
                        image.set(codeIndex, "A9");
                        image.set(codeIndex+1, "F5");
                        image.set(codeIndex+2, "D0");
                        image.set(codeIndex+3, "02");
                        image.set(codeIndex+4, "A9");
                        image.set(codeIndex+5, "FB");
                        codeIndex += 6;
                        storeToStatic(n);
                    } else if (inPrint) {
                        image.set(codeIndex, "A0");
                        image.set(codeIndex+1, "F5");
                        image.set(codeIndex+2, "D0");
                        image.set(codeIndex+3, "02");
                        image.set(codeIndex+4, "A0");
                        image.set(codeIndex+5, "FB");
                        image.set(codeIndex+6, "A2");
                        image.set(codeIndex+7, "02");
                        codeIndex += 8;
                    }
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
                    System.out.println("CODE GEN -- Generating <Addition>");
                    generateAddition(n, 0);
                    if(inAssignment) {
                        storeToStatic(n);
                    } else if(inPrint) {
                        image.set(codeIndex, "8D");
                        image.set(codeIndex+1, "00");
                        image.set(codeIndex+2, "00");
                        image.set(codeIndex+3, "AC");
                        image.set(codeIndex+4, "00");
                        image.set(codeIndex+5, "00");
                        image.set(codeIndex+6, "A2");
                        image.set(codeIndex+7, "01");
                        codeIndex += 8;
                    }
                    break;
                }

                default : {
                    // Remove < > to parse easier
                    String leftChild = n.data.data.substring(1, n.data.data.length()-1);
                    if(leftChild.substring(0,1).equals("\"")) {
                        if(inAssignment) {
                            addToHeap(leftChild.substring(1, leftChild.length()-1));
                            String heapAddr = Integer.toHexString(heapIndex+1).toUpperCase();
                            image.set(codeIndex, "A9");
                            image.set(codeIndex+1, heapAddr);
                            codeIndex += 2;
                            storeToStatic(n);
                        } else if (inPrint) {
                            addToHeap(leftChild.substring(1, leftChild.length()-1));
                            String heapAddr = Integer.toHexString(heapIndex+1).toUpperCase();
                            image.set(codeIndex, "A0");
                            image.set(codeIndex+1, heapAddr);
                            image.set(codeIndex+2, "A2");
                            image.set(codeIndex+3, "02");
                            codeIndex += 4;
                        }
                    } else {
                        try {
                            int digit = Integer.parseInt(leftChild);
                            if (inAssignment) {
                                String hex = "0" + Integer.toHexString(digit);
                                image.set(codeIndex, "A9");
                                image.set(codeIndex+1, hex);
                                codeIndex += 2;
                                storeToStatic(n);
                            } else if (inPrint) {
                                String hex = "0" + Integer.toHexString(digit);
                                image.set(codeIndex, "A0");
                                image.set(codeIndex+1, hex);
                                image.set(codeIndex+2, "A2");
                                image.set(codeIndex+3, "01");
                                codeIndex += 4;
                            }
                        } catch (NumberFormatException e) {
                            if (inAssignment && n.parent.children.indexOf(n) == 1) {
                                String addr = findStaticVar(n.parent.children.get(1).data.data, scope).tempAddress;
                                image.set(codeIndex, "AD");
                                image.set(codeIndex+1, addr);
                                image.set(codeIndex+2, "XX");
                                codeIndex += 3;
                                storeToStatic(n);
                            } else if (inPrint) {
                                String addr = findStaticVar(n.data.data, scope).tempAddress;
                                image.set(codeIndex, "AC");
                                image.set(codeIndex+1, addr);
                                image.set(codeIndex+2, "XX");
                                image.set(codeIndex+3, "A2");
                                if(n.data.type.equals("<int>")) {
                                    image.set(codeIndex+4, "01");
                                } else {
                                    image.set(codeIndex+4, "02");
                                }
                                codeIndex += 5;
                            }
                        }
                    }
                }
            }
        }
    }

    private static void storeToStatic(Tree.Node n) {
        image.set(codeIndex, "8D");
        String addr = findStaticVar(n.parent.children.get(0).data.data, scope).tempAddress;
        image.set(codeIndex+1, addr);
        image.set(codeIndex+2, "XX");
        codeIndex += 3;
    }

    private static void generateEquality(Tree.Node current) {
        if(current.data.data.equals("<==>") || current.data.data.equals("<!=>")) {
            String leftChild = current.children.get(0).data.data.substring(1, current.children.get(0).data.data.length()-1);
            if(leftChild.substring(0,1).equals("\"")) {
                addToHeap(leftChild.substring(1, leftChild.length()-1));
                String heapAddr = Integer.toHexString(heapIndex+1).toUpperCase();
                image.set(codeIndex, "A2");
                image.set(codeIndex+1, heapAddr);
                codeIndex += 2;
            } else {
                try {
                    int digit = Integer.parseInt(leftChild);
                    String hex = "0" + Integer.toHexString(digit);
                    image.set(codeIndex, "A2");
                    image.set(codeIndex+1, hex);
                    codeIndex += 2;
                } catch (NumberFormatException e) {
                    if(leftChild.length() == 1) {
                        // Id
                        String staticAddr = findStaticVar("<"+leftChild+">", scope).tempAddress;
                        image.set(codeIndex, "AE");
                        image.set(codeIndex+1, staticAddr);
                        image.set(codeIndex+2, "XX");
                        codeIndex += 3;
                    } else if (leftChild.equals("true")) {
                        image.set(codeIndex, "A2");
                        image.set(codeIndex+1, "FB");
                        codeIndex += 2;
                    } else if (leftChild.equals("false")) {
                        image.set(codeIndex, "A2");
                        image.set(codeIndex+1, "F5");
                        codeIndex += 2;
                    } else if (leftChild.equals("Addition")) {
                        generateAddition(current.children.get(0), 0);
                    } else if (leftChild.equals("==") || leftChild.equals("!=")) {
                        System.out.println("CODE GEN -- ERROR: Nested boolean expressions are not supported, sorry.");
                        errors++;
                    }
                }
            }

            String rightChild = current.children.get(1).data.data.substring(1, current.children.get(1).data.data.length()-1);
            if(rightChild.substring(0,1).equals("\"")) {
                addToHeap(rightChild.substring(1, rightChild.length()-1));
                String heapAddr = Integer.toHexString(heapIndex+1).toUpperCase();
                image.set(codeIndex, "A9");
                image.set(codeIndex+1, heapAddr);
                codeIndex += 2;
            } else {
                try {
                    int digit = Integer.parseInt(rightChild);
                    String hex = "0" + Integer.toHexString(digit);
                    image.set(codeIndex, "A9");
                    image.set(codeIndex+1, hex);
                    codeIndex += 2;
                } catch (NumberFormatException e) {
                    if(rightChild.length() == 1) {
                        // Id
                        String staticAddr = findStaticVar("<"+rightChild+">", scope).tempAddress;
                        image.set(codeIndex, "AD");
                        image.set(codeIndex+1, staticAddr);
                        image.set(codeIndex+2, "XX");
                        codeIndex += 3;
                    } else if (rightChild.equals("true")) {
                        image.set(codeIndex, "A9");
                        image.set(codeIndex+1, "FB");
                        codeIndex += 2;
                    } else if (rightChild.equals("false")){
                        image.set(codeIndex, "A9");
                        image.set(codeIndex+1, "F5");
                        codeIndex += 2;
                    } else if (rightChild.equals("Addition")) {
                        generateAddition(current.children.get(1), 0);
                    } else if (rightChild.equals("==") || rightChild.equals("!=")) {
                        System.out.println("CODE GEN -- ERROR: Nested boolean expressions are not supported, sorry.");
                        errors++;
                    }
                }
            }

            image.set(codeIndex, "8D");
            image.set(codeIndex+1, "00");
            image.set(codeIndex+2, "00");
            image.set(codeIndex+3, "EC");
            image.set(codeIndex+4, "00");
            image.set(codeIndex+5, "00");
            codeIndex += 6;
        }
    }

    private static void generateAddition(Tree.Node current, int level) {
        if(current.data.data.equals("<Addition>")) {
            generateAddition(current.children.get(1), level+1);
            int digit = Integer.parseInt(current.children.get(0).data.data.substring(1, current.children.get(0).data.data.length()-1));
            String hex = "0" + Integer.toHexString(digit);
            image.set(codeIndex, "A9");
            image.set(codeIndex+1, hex);
            image.set(codeIndex+2, "6D");
            image.set(codeIndex+3, "00");
            image.set(codeIndex+4, "00");
            codeIndex += 5;
            if(level > 0) {
                image.set(codeIndex, "8D");
                image.set(codeIndex+1, "00");
                image.set(codeIndex+2, "00");
                codeIndex += 3;
            }
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
                // Id
                String staticAddr = findStaticVar(current.data.data, scope).tempAddress;
                image.set(codeIndex, "AD");
                image.set(codeIndex+1, staticAddr);
                image.set(codeIndex+2, "XX");
                image.set(codeIndex+3, "8D");
                image.set(codeIndex+4, "00");
                image.set(codeIndex+5, "00");
                codeIndex += 6;
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
