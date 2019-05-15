import java.util.ArrayList;

public class CodeGenerator {

    public static ArrayList<String> image;
    private static ArrayList<StaticVarEntry> staticVars;
    private static ArrayList<JumpTableEntry> jumpTable;
    private static int heapIndex;
    private static int codeIndex;
    private static int staticVarCount;
    private static int jumpsCount;
    private static int scope;
    private static boolean inAssignment;
    private static boolean inPrint;
    private static boolean inIf;
    private static boolean inWhile;
    private static int errors;
    private static int jumps;
    private static int jumpStart;

    public static void generateCode(Tree ast, int programNum) {
        // Initialize the image to contain 00 for every byte.
        image = new ArrayList<>();
        for(int i=0; i < 256; i++) {
            image.add("00");
        }

        staticVars = new ArrayList<>();
        jumpTable = new ArrayList<>();
        codeIndex = 0;
        heapIndex = 255;
        staticVarCount = 0;
        jumpsCount = 0;
        scope = 0;
        inAssignment = false;
        inPrint = false;
        inIf = false;
        errors = 0;
        jumps = 0;
        jumpStart = 0;

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

            for (JumpTableEntry j : jumpTable) {
                String hex = Integer.toHexString(j.shift).toUpperCase();
                if (hex.length() == 1) {
                    hex = "0" + hex;
                }
                System.out.println("CODE GEN -- Replacing [ " + j.id + " ] with [ " + hex + " ]");
                for (int i = 0; i < staticStartIndex; i++) {
                    String temp = image.get(i);
                    if (temp.equals(j.id)) {
                        image.set(i, hex);
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
                    inWhile = true;
                    System.out.println("CODE GEN -- Generating <While Statement>");
                    int topIndex = codeIndex;
                    int jumpIndex = jumpsCount;
                    codeGen(n);
                    image.set(codeIndex, "A9");
                    image.set(codeIndex+1, "00");
                    image.set(codeIndex+2, "8D");
                    image.set(codeIndex+3, "T"+staticVarCount);
                    image.set(codeIndex+4, "XX");
                    image.set(codeIndex+5, "A2");
                    image.set(codeIndex+6, "01");
                    image.set(codeIndex+7, "EC");
                    image.set(codeIndex+8, "T"+staticVarCount);
                    image.set(codeIndex+9, "XX");
                    image.set(codeIndex+10, "D0");
                    staticVars.add(new StaticVarEntry("Copy", staticVarCount, scope));
                    String jumpToTop = Integer.toHexString(256 - (codeIndex+12) + topIndex).toUpperCase();
                    if(jumpToTop.length() == 1) {
                        jumpToTop = "0" + jumpToTop;
                    }
                    image.set(codeIndex+11, jumpToTop);
                    codeIndex += 12;
                    jumps = codeIndex - jumpStart;
                    jumpTable.add(new JumpTableEntry(jumpsCount, jumps));
                    jumpsCount++;
                    inWhile = false;
                    break;
                }

                case "<If Statement>" : {
                    inIf = true;
                    System.out.println("CODE GEN -- Generating <If Statement>");
                    int jumpIndex = jumpsCount;
                    codeGen(n);
                    jumps = codeIndex - jumpStart;
                    jumpTable.add(new JumpTableEntry(jumpsCount, jumps));
                    inIf = false;
                    jumpsCount++;
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
                    } else if (inIf) {
                        image.set(codeIndex, "A2");
                        image.set(codeIndex+1, "01");
                        image.set(codeIndex+2, "A9");
                        image.set(codeIndex+3, "01");
                        image.set(codeIndex+4, "8D");
                        image.set(codeIndex+5, "00");
                        image.set(codeIndex+6, "00");
                        image.set(codeIndex+7, "EC");
                        image.set(codeIndex+8, "00");
                        image.set(codeIndex+9, "00");
                        image.set(codeIndex+10, "D0");
                        image.set(codeIndex+11, "J"+jumpsCount);
                        codeIndex += 12;
                        jumpStart = codeIndex;
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
                    } else if (inIf) {
                        image.set(codeIndex, "A2");
                        image.set(codeIndex+1, "01");
                        image.set(codeIndex+2, "A9");
                        image.set(codeIndex+3, "02");
                        image.set(codeIndex+4, "8D");
                        image.set(codeIndex+5, "00");
                        image.set(codeIndex+6, "00");
                        image.set(codeIndex+7, "EC");
                        image.set(codeIndex+8, "00");
                        image.set(codeIndex+9, "00");
                        image.set(codeIndex+10, "D0");
                        image.set(codeIndex+11, "J"+jumpsCount);
                        codeIndex += 12;
                        jumpStart = codeIndex;
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
                    } else if (inIf) {
                        image.set(codeIndex, "A2");
                        image.set(codeIndex+1, "F5");
                        image.set(codeIndex+2, "D0");
                        image.set(codeIndex+3, "02");
                        image.set(codeIndex+4, "A2");
                        image.set(codeIndex+5, "FB");
                        image.set(codeIndex+6, "A9");
                        image.set(codeIndex+7, "F5");
                        image.set(codeIndex+8, "8D");
                        image.set(codeIndex+9, "00");
                        image.set(codeIndex+10, "00");
                        image.set(codeIndex+11, "EC");
                        image.set(codeIndex+12, "00");
                        image.set(codeIndex+13, "00");
                        image.set(codeIndex+14, "D0");
                        image.set(codeIndex+15, "J"+jumpsCount);
                        codeIndex += 16;
                        jumpStart = codeIndex;
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
                    } else if (inIf) {
                        image.set(codeIndex, "D0");
                        image.set(codeIndex+1, "J"+jumpsCount);
                        codeIndex += 2;
                        jumpStart = codeIndex;
                    } else if (inWhile) {
                        image.set(codeIndex, "D0");
                        image.set(codeIndex+1, "J"+jumpsCount);
                        codeIndex += 2;
                        jumpStart = codeIndex;
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
                                String addr = findStaticVar(n.parent.children.get(1).data.data).tempAddress;
                                image.set(codeIndex, "AD");
                                image.set(codeIndex+1, addr);
                                image.set(codeIndex+2, "XX");
                                codeIndex += 3;
                                storeToStatic(n);
                            } else if (inPrint) {
                                String addr = findStaticVar(n.data.data).tempAddress;
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
        String addr = findStaticVar(n.parent.children.get(0).data.data).tempAddress;
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
                if (inWhile) {
                    image.set(codeIndex, "A9");
                    image.set(codeIndex+1, heapAddr);
                    image.set(codeIndex+2, "8D");
                    image.set(codeIndex+3, "T"+(staticVarCount+1));
                    image.set(codeIndex+4, "XX");
                    codeIndex += 5;
                    staticVars.add(new StaticVarEntry("Copy", staticVarCount+1, scope));
                } else {
                    image.set(codeIndex, "A2");
                    image.set(codeIndex + 1, heapAddr);
                    codeIndex += 2;
                }
            } else {
                try {
                    int digit = Integer.parseInt(leftChild);
                    String hex = "0" + Integer.toHexString(digit);
                    if (inWhile) {
                        image.set(codeIndex, "A9");
                        image.set(codeIndex+1, hex);
                        image.set(codeIndex+2, "8D");
                        image.set(codeIndex+3, "T"+(staticVarCount+1));
                        image.set(codeIndex+4, "XX");
                        codeIndex += 5;
                        staticVars.add(new StaticVarEntry("Copy", staticVarCount+1, scope));
                    } else {
                        image.set(codeIndex, "A2");
                        image.set(codeIndex + 1, hex);
                        codeIndex += 2;
                    }
                } catch (NumberFormatException e) {
                    if(leftChild.length() == 1) {
                        // Id
                        String staticAddr = findStaticVar("<"+leftChild+">").tempAddress;
                        if (inWhile) {
                            image.set(codeIndex, "AD");
                            image.set(codeIndex+1, staticAddr);
                            image.set(codeIndex+2, "XX");
                            image.set(codeIndex+3, "8D");
                            image.set(codeIndex+4, "T"+(staticVarCount+1));
                            image.set(codeIndex+5, "XX");
                            codeIndex += 6;
                            staticVars.add(new StaticVarEntry("Copy", staticVarCount+1, scope));
                        } else {
                            image.set(codeIndex, "AE");
                            image.set(codeIndex + 1, staticAddr);
                            image.set(codeIndex + 2, "XX");
                            codeIndex += 3;
                        }
                    } else if (leftChild.equals("true")) {
                        if (inWhile) {
                            image.set(codeIndex, "A9");
                            image.set(codeIndex+1, "FB");
                            image.set(codeIndex+2, "8D");
                            image.set(codeIndex+3, "T"+(staticVarCount+1));
                            image.set(codeIndex+4, "XX");
                            codeIndex += 5;
                            staticVars.add(new StaticVarEntry("Copy", staticVarCount+1, scope));
                        } else {
                            image.set(codeIndex, "A2");
                            image.set(codeIndex + 1, "FB");
                            codeIndex += 2;
                        }
                    } else if (leftChild.equals("false")) {
                        if (inWhile) {
                            image.set(codeIndex, "A9");
                            image.set(codeIndex+1, "F5");
                            image.set(codeIndex+2, "8D");
                            image.set(codeIndex+3, "T"+(staticVarCount+1));
                            image.set(codeIndex+4, "XX");
                            codeIndex += 5;
                            staticVars.add(new StaticVarEntry("Copy", staticVarCount+1, scope));
                        } else {
                            image.set(codeIndex, "A2");
                            image.set(codeIndex + 1, "F5");
                            codeIndex += 2;
                        }
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
                    if (inWhile) {
                        image.set(codeIndex, "A9");
                        image.set(codeIndex+1, hex);
                        image.set(codeIndex+2, "8D");
                        image.set(codeIndex+3, "T"+staticVarCount);
                        image.set(codeIndex+4, "XX");
                        codeIndex += 5;
                        staticVars.add(new StaticVarEntry("Copy", staticVarCount, scope));
                    } else {
                        image.set(codeIndex, "A9");
                        image.set(codeIndex + 1, hex);
                        codeIndex += 2;
                    }
                } catch (NumberFormatException e) {
                    if(rightChild.length() == 1) {
                        // Id
                        String staticAddr = findStaticVar("<"+rightChild+">").tempAddress;
                        if (inWhile) {
                            image.set(codeIndex, "AD");
                            image.set(codeIndex+1, staticAddr);
                            image.set(codeIndex+2, "XX");
                            image.set(codeIndex+3, "8D");
                            image.set(codeIndex+4, "T"+staticVarCount);
                            image.set(codeIndex+5, "XX");
                            codeIndex += 6;
                            staticVars.add(new StaticVarEntry("Copy", staticVarCount, scope));
                        } else {
                            image.set(codeIndex, "AD");
                            image.set(codeIndex + 1, staticAddr);
                            image.set(codeIndex + 2, "XX");
                            codeIndex += 3;
                        }
                    } else if (rightChild.equals("true")) {
                        if (inWhile) {
                            image.set(codeIndex, "A9");
                            image.set(codeIndex+1,"FB");
                            image.set(codeIndex+2, "8D");
                            image.set(codeIndex+3, "T"+staticVarCount);
                            image.set(codeIndex+4, "XX");
                            codeIndex += 5;
                            staticVars.add(new StaticVarEntry("Copy", staticVarCount, scope));
                        } else {
                            image.set(codeIndex, "A9");
                            image.set(codeIndex + 1, "FB");
                            codeIndex += 2;
                        }
                    } else if (rightChild.equals("false")){
                        if (inWhile) {
                            image.set(codeIndex, "A9");
                            image.set(codeIndex+1, "F5");
                            image.set(codeIndex+2, "8D");
                            image.set(codeIndex+3, "T"+staticVarCount);
                            image.set(codeIndex+4, "XX");
                            staticVars.add(new StaticVarEntry("Copy", staticVarCount, scope));
                            codeIndex += 5;
                        } else {
                            image.set(codeIndex, "A9");
                            image.set(codeIndex + 1, "F5");
                            codeIndex += 2;
                        }
                    } else if (rightChild.equals("Addition")) {
                        generateAddition(current.children.get(1), 0);
                        if (inWhile) {
                            image.set(codeIndex, "8D");
                            image.set(codeIndex+1, "T"+staticVarCount);
                            image.set(codeIndex+2, "XX");
                            staticVars.add(new StaticVarEntry("Copy", staticVarCount, scope));
                            codeIndex += 3;
                        }
                    } else if (rightChild.equals("==") || rightChild.equals("!=")) {
                        System.out.println("CODE GEN -- ERROR: Nested boolean expressions are not supported, sorry.");
                        errors++;
                    }
                }
            }

            if (inWhile) {
                image.set(codeIndex, "AE");
                image.set(codeIndex+1, "T"+(staticVarCount+1));
                image.set(codeIndex+2, "XX");
                image.set(codeIndex+3, "EC");
                image.set(codeIndex+4, "T"+staticVarCount);
                image.set(codeIndex+5, "XX");
                codeIndex += 6;
            } else {
                image.set(codeIndex, "8D");
                image.set(codeIndex + 1, "00");
                image.set(codeIndex + 2, "00");
                image.set(codeIndex + 3, "EC");
                image.set(codeIndex + 4, "00");
                image.set(codeIndex + 5, "00");
                codeIndex += 6;
            }
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
                String staticAddr = findStaticVar(current.data.data).tempAddress;
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

    private static StaticVarEntry findStaticVar(String var) {
        for(StaticVarEntry s : staticVars) {
            if(s.id.equals(var)) {
                return s;
            }
        }
        return null;
    }

    private static int findJumpEntry(int jumpCount) {
        for(int i=0; i<jumpTable.size(); i++) {
            if(jumpTable.get(i).id == "J"+jumpCount) {
                return i;
            }
        }
        return -1;
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
