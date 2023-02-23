/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.debugger.jpda.projects;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.UnionType;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;

import org.netbeans.spi.debugger.jpda.EditorContext;
import org.openide.util.Exceptions;

/**
 * This class tries to match the AST expression to bytecode. The result
 * of this match is provided as an
 * {@link org.netbeans.spi.debugger.jpda.EditorContext.ExpressionTree} object.
 * 
 * @author Martin Entlicher
 */
class AST2Bytecode {

    /** Creates a new instance of AST2Bytecode */
    private AST2Bytecode() {
    }
    
    static EditorContext.Operation[] matchSourceTree2Bytecode(
            CompilationUnitTree cu, CompilationController ci,
            List<Tree> treeNodes, ExpressionScanner.ExpressionsInfo info,
            byte[] bytecodes, int[] indexes, byte[] constantPoolBytes,
            ASTOperationCreationDelegate opCreationDelegate,
            Map<Tree, EditorContext.Operation> nodeOperations) {
        
        Trees trees = ci.getTrees();
        Types types = ci.getTypes();
        SourcePositions sp = trees.getSourcePositions();
        //List<Tree> treeNodes = linearizeTree(expTrees);
        if (treeNodes == null) {
            return null;
        }
        if (indexes == null) {
            return null;
        }
        int length = treeNodes.size();
        List<EditorContext.Operation> operations = new ArrayList<EditorContext.Operation>(length);
        LineMap lineMap = cu.getLineMap();
        int indexesIndex = 0;
        int from = indexes[indexesIndex];
        int to = indexes[indexesIndex + 1];
        ConstantPool constantPool;
        if (constantPoolBytes != null) {
            constantPool = ConstantPool.parse(constantPoolBytes, ci.getFileObject().toString());
        } else {
            constantPool = null;
        }
        for (int treeIndex = 0; treeIndex < length; treeIndex++) {
            Tree node = treeNodes.get(treeIndex);
            Tree.Kind kind = node.getKind();
            EditorContext.Operation op = null;
            if (kind == Tree.Kind.METHOD_INVOCATION || kind == Tree.Kind.NEW_CLASS) {
                
                int opcode;
                do {
                    do {
                        if (from < 0 || from >= bytecodes.length) {
                            // Check for bad bytecode or bad parsing...
                            return null;
                        }
                        opcode = bytecodes[from] & 0xFF;
                        if (isMethodCall(opcode)) {
                            break;
                        }
                        from += getInstrSize(opcode, bytecodes, from);
                    } while (from < to);
                    if (from < to) {
                        break;
                    }
                    if ((indexesIndex + 2) < indexes.length) {
                        indexesIndex += 2;
                        from = indexes[indexesIndex];
                        to = indexes[indexesIndex + 1];
                    } else {
                        break;
                    }
                } while (true);
                if (from < to) { // We have the method call
                    TreePath nodePath = trees.getPath(cu, node);
                    if (nodePath != null && !ci.getTreeUtilities().isSynthetic(nodePath)) {
                        // Find if 'node' is a native method:
                        Element methodElement = ci.getTrees().getElement(nodePath);
                        boolean isNativeMethod = (methodElement != null) && methodElement.getModifiers().contains(Modifier.NATIVE);
                        //System.err.println("Method "+methodElement+" is native = "+isNativeMethod);
                        String methodNameInBytecode = null;
                        String methodDescriptorInBytecode = null;
                        if (constantPool != null) {
                            int constantPoolIndex = ((bytecodes[from+1] & 0xFF) << 8) + (bytecodes[from+2] & 0xFF);
                            try {
                                methodNameInBytecode = constantPool.getMethodName(constantPoolIndex);
                                methodDescriptorInBytecode = constantPool.getMethodDescriptor(constantPoolIndex);
                            } catch (IndexOutOfBoundsException ioobex) {
                                ioobex = Exceptions.attachMessage(ioobex, "While matching "+treeNodes+". Please attach the code where this happens to http://www.netbeans.org/issues/show_bug.cgi?id=161839");
                                Exceptions.printStackTrace(ioobex);
                            }
                        }
                        int pos = (int) sp.getStartPosition(cu, node);
                        if (pos == Diagnostic.NOPOS) {
                            // Bad positions => give it up.
                            return null;
                        }
                        EditorContext.Position startPosition =
                                opCreationDelegate.createPosition(
                                        pos,
                                        (int) lineMap.getLineNumber(pos),
                                        (int) lineMap.getColumnNumber(pos)
                                );
                        pos = (int) sp.getEndPosition(cu, node);
                        if (pos == Diagnostic.NOPOS) {
                            // Bad positions => give it up.
                            return null;
                        }
                        EditorContext.Position endPosition =
                                opCreationDelegate.createPosition(
                                        pos,
                                        (int) lineMap.getLineNumber(pos),
                                        (int) lineMap.getColumnNumber(pos)
                                );
                        Tree identifier;
                        String methodName;
                        String methodClassType;
                        boolean getStartPosFromMethodLength = false;
                        if (kind == Tree.Kind.NEW_CLASS) {
                            identifier = ((NewClassTree) node).getIdentifier();
                            methodName = "<init>";
                            TreePath iPath = TreePath.getPath(cu, identifier);
                            if (iPath == null) {
                                return null; // No path to the identifier...
                            }
                            TypeMirror type = trees.getTypeMirror(iPath);
                            if (type == null || type.getKind() == TypeKind.ERROR) {
                                // There are errors, give it up.
                                return null;
                            }
                            assert type.getKind() == TypeKind.DECLARED;
                            TypeElement te = (TypeElement) types.asElement(type);
                            methodClassType = ElementUtilities.getBinaryName(te);
                        } else {
                            //identifier = ((MemberSelectTree) ((MethodInvocationTree) node).getMethodSelect()).getIdentifier();
                            identifier = ((MethodInvocationTree) node).getMethodSelect();
                            if (identifier.getKind() == Tree.Kind.IDENTIFIER) {
                                methodName = ((IdentifierTree) identifier).getName().toString();
                                TreePath iPath = TreePath.getPath(cu, identifier);
                                if (iPath == null) {
                                    return null; // No path to the identifier...
                                }
                                TypeElement te = trees.getScope(iPath).getEnclosingClass();
                                if (te == null) {
                                    // No enclosing class? Some error, give it up.
                                    return null;
                                }
                                methodClassType = ElementUtilities.getBinaryName(te);
                            } else {
                                methodName = ((MemberSelectTree) identifier).getIdentifier().toString();
                                getStartPosFromMethodLength = true;
                                ExpressionTree exp = ((MemberSelectTree) identifier).getExpression();
                                TreePath expPath = TreePath.getPath(cu, exp);
                                if (expPath == null) {
                                    return null; // No path to the expression...
                                }
                                TypeMirror type = trees.getTypeMirror(expPath);
                                if (type == null || type.getKind() == TypeKind.ERROR) {
                                    // There are errors, give it up.
                                    return null;
                                }
                                String array = "";
                                while (type.getKind() == TypeKind.ARRAY) {
                                    type = ((ArrayType) type).getComponentType();
                                    array += "[]";
                                }
                                TypeKind k = type.getKind();
                                if (k == TypeKind.DECLARED) {
                                    TypeElement te = (TypeElement) types.asElement(type);
                                    methodClassType = ElementUtilities.getBinaryName(te)+array;
                                } else if (k == TypeKind.TYPEVAR) {
                                    TypeElement te;
                                    TypeParameterElement tpe = (TypeParameterElement) types.asElement(type);
                                    List<? extends TypeMirror> exts = tpe.getBounds();
                                    if (exts.size() == 1) {
                                        type = exts.get(0);
                                        if (type.getKind() == TypeKind.DECLARED) {
                                            te = (TypeElement) types.asElement(type);
                                        } else {
                                            return null; // Unsupported
                                        }
                                    } else {
                                        return null; // Unsupported
                                    }
                                    methodClassType = ElementUtilities.getBinaryName(te)+array;
                                } else if (k == TypeKind.UNION) {
                                    UnionType ut = (UnionType) type;
                                    type = getUnionType(types, ut.getAlternatives());
                                    if (type == null) {
                                        // No union super type found.
                                        Exceptions.printStackTrace(new IllegalStateException("No union type found from "+ut+", alternatives = "+ut.getAlternatives()));
                                        return null;
                                    }
                                    TypeElement te = (TypeElement) types.asElement(type);
                                    methodClassType = ElementUtilities.getBinaryName(te)+array;
                                } else if (type instanceof PrimitiveType) {
                                    methodClassType = type.toString()+array;
                                } else {
                                    Exceptions.printStackTrace(new IllegalStateException("Unexpected type "+type+" of kind "+type.getKind()+" in "+treeNodes));
                                    return null;
                                }
                            }
                        }
                        if (methodNameInBytecode != null) {
                            if (!methodNameInBytecode.equals(methodName)) {
                                // There are some extra methods in the bytecode, we have to skip them
                                int next = from;
                                next += getInstrSize(opcode, bytecodes, next);
                                do {
                                    while (next < to) {
                                        //System.err.println("AST2Bytecode: skipped method in bytecode '"+methodNameInBytecode+"'");
                                        opcode = bytecodes[next] & 0xFF;
                                        if (isMethodCall(opcode)) {
                                            int constantPoolIndex = ((bytecodes[next+1] & 0xFF) << 8) + (bytecodes[next+2] & 0xFF);
                                            try {
                                                methodNameInBytecode = constantPool.getMethodName(constantPoolIndex);
                                                methodDescriptorInBytecode = constantPool.getMethodDescriptor(constantPoolIndex);
                                            } catch (IndexOutOfBoundsException ioobex) {
                                                ioobex = Exceptions.attachMessage(ioobex, "While matching "+treeNodes+". Please attach the code where this happens to http://www.netbeans.org/issues/show_bug.cgi?id=161839");
                                                Exceptions.printStackTrace(ioobex);
                                                break;
                                            }
                                            if (methodNameInBytecode.equals(methodName)) {
                                                break;
                                            } else {
                                                //System.err.println("AST2Bytecode: skipped method in bytecode '"+methodNameInBytecode+"'");
                                                methodDescriptorInBytecode = null;
                                            }
                                        }
                                        next += getInstrSize(opcode, bytecodes, next);
                                    }
                                    if (next < to) {
                                        // Method found
                                        from = next;
                                        break;
                                    }
                                    if ((indexesIndex + 2) < indexes.length) {
                                        indexesIndex += 2;
                                        from = indexes[indexesIndex];
                                        to = indexes[indexesIndex + 1];
                                    } else {
                                        break;
                                    }
                                } while (true);
                            }
                        }
                        //System.err.println("AST2Bytecode: methodNameInBytecode = '"+methodNameInBytecode+"', methodNameInSource = '"+methodName+"'");
                        pos = (int) sp.getEndPosition(cu, identifier);
                        if (pos == Diagnostic.NOPOS) {
                            // Bad positions => give it up.
                            return null;
                        }
                        EditorContext.Position methodEndPosition =
                                opCreationDelegate.createPosition(
                                        pos,
                                        (int) lineMap.getLineNumber(pos),
                                        (int) lineMap.getColumnNumber(pos)
                                );
                        if (getStartPosFromMethodLength) {
                            pos = pos - methodName.length();
                        } else {
                            pos = (int) sp.getStartPosition(cu, identifier);
                            if (pos == Diagnostic.NOPOS) {
                                // Bad positions => give it up.
                                return null;
                            }
                        }
                        EditorContext.Position methodStartPosition =
                                opCreationDelegate.createPosition(
                                        pos,
                                        (int) lineMap.getLineNumber(pos),
                                        (int) lineMap.getColumnNumber(pos)
                                );
                        /*
                        EditorContext.Operation op =
                                opCreationDelegate.createOperation(
                                        startPosition,
                                        endPosition,
                                        from
                                );
                         */
                        op = opCreationDelegate.createMethodOperation(
                                        startPosition,
                                        endPosition,
                                        methodStartPosition,
                                        methodEndPosition,
                                        methodName,
                                        methodClassType,
                                        from,
                                        isNativeMethod
                                );
                        try {
                            java.lang.reflect.Field methodDescriptorField = EditorContext.Operation.class.getDeclaredField("methodDescriptor");
                            methodDescriptorField.setAccessible(true);
                            methodDescriptorField.set(op, methodDescriptorInBytecode);
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        //treeNodes.get(treeIndex).setCodeIndex(from);
                        operations.add(op);
                    }
                    from += getInstrSize(opcode, bytecodes, from);
                } else {
                    return null; // Mismatch
                }
            }
            if (op != null) {
                nodeOperations.put(node, op);
            }
        }
        // Check the rest of the bytecode for method calls:
        if (constantPool == null) {
            // Do not do that if we can control method names
            // - there can be extra utility calls in the bytecode
            do {
                while (from < to) {
                    int opcode = bytecodes[(int) from] & 0xFF;
                    if (isMethodCall(opcode)) {
                        return null; // Mismatch
                    }
                    from += getInstrSize(opcode, bytecodes, from);
                }
                if ((indexesIndex + 2) < indexes.length) {
                    indexesIndex += 2;
                    from = indexes[indexesIndex];
                    to = indexes[indexesIndex + 1];
                } else {
                    break;
                }
            } while (true);
        }
        /*
        // Assign next operations:
        for (int treeIndex = 0; treeIndex < length; treeIndex++) {
            Tree node = treeNodes.get(treeIndex);
            Set<Tree> nextNodes = info.getNextExpressions(node);
            if (nextNodes != null) {
                EditorContext.Operation op = nodeOperations.get(node);
                if (op == null) {
                    for (int backIndex = treeIndex - 1; backIndex >= 0; backIndex--) {
                        node = treeNodes.get(backIndex);
                        op = nodeOperations.get(node);
                        if (op != null) break;
                    }
                }
                if (op != null) {
                    assignNext(op, opCreationDelegate, info, nodeOperations,
                               nextNodes, treeNodes);
                }
            }
        }
         */
        return operations.toArray(new EditorContext.Operation[] {});
    }
    
    private static TypeMirror getUnionType(Types types, List<? extends TypeMirror> tms) {
        int n = tms.size();
        List<TypeMirror>[] superTypes = new List[n];
        for (int i = 0; i < n; i++) {
            superTypes[i] = new ArrayList<TypeMirror>();
            TypeMirror tm = tms.get(i);
            TypeElement te = (TypeElement) types.asElement(tm);
            superTypes[i].add(te.getSuperclass());
        }
        TypeMirror unionType;
        while ((unionType = findUnion(types, superTypes)) == null) {
            boolean noSuper = true;
            for (int i = 0; i < n; i++) {
                TypeMirror lastTM = superTypes[i].get(superTypes[i].size() - 1);
                TypeElement te = (TypeElement) types.asElement(lastTM);
                if (te == null) {
                    continue;
                }
                TypeMirror superclass = te.getSuperclass();
                if (superclass != null) {
                    superTypes[i].add(superclass);
                    noSuper = false;
                }
            }
            if (noSuper) {
                break;
            }
        }
        return unionType;
    }
    
    private static TypeMirror findUnion(Types types, List<TypeMirror>[] sTypes) {
        for (TypeMirror tm : sTypes[0]) {
            if (isInTypes(types, tm, sTypes, 1)) {
                return tm;
            }
        }
        return null;
    }
    
    private static boolean isInTypes(Types types, TypeMirror tm, List<TypeMirror>[] sTypes, int from) {
        if (from >= sTypes.length) {
            return true;
        }
        for (TypeMirror testTM : sTypes[from]) {
            if (types.isSameType(tm, testTM)) {
                return isInTypes(types, tm, sTypes, from + 1);
            }
        }
        return false;
    }
    
    /*private static void assignNext(EditorContext.Operation op,
                                   OperationCreationDelegate opCreationDelegate,
                                   ExpressionScanner.ExpressionsInfo info,
                                   Map<Tree, EditorContext.Operation> nodeOperations,
                                   Set<Tree> nextNodes,
                                   List<Tree> allNodes) {
        for (Tree t : nextNodes) {
            EditorContext.Operation nextOp = nodeOperations.get(t);
            if (nextOp != null) {
                opCreationDelegate.addNextOperationTo(op, nextOp);
            } else {
                Set<Tree> nextNextNodes = info.getNextExpressions(t);
                if (nextNextNodes == null) {
                    boolean check = false;
                    for (int treeIndex = 0; treeIndex < allNodes.size(); treeIndex++) {
                        Tree node = allNodes.get(treeIndex);
                        if (check) {
                            nextNextNodes = info.getNextExpressions(node);
                            if (nextNextNodes != null) break;
                        } else {
                            if (t == node) {
                                check = true;
                            }
                        }
                    }
                } else {
                    assignNext(op, opCreationDelegate, info, nodeOperations, nextNextNodes, allNodes);
                }
            }
        }
    }*/
    
    private static boolean isMethodCall(int opcode) {
        return opcode >= 182 && opcode <= 185;
    }
    
    private static int getInstrSize(int opcode, byte[] bytecodes, long codeIndex) {
        if (opcode <= 15) return 1; // nop - dconst_1
        if (opcode == 16) return 2; // bipush <byte>
        if (opcode == 17) return 3; // sipush <byte1> <byte2>
        if (opcode == 18) return 2; // ldc <index>
        if (opcode <= 20) return 3; // ldc_w <byte1> <byte2>, ldc2_w <byte1> <byte2>
        if (opcode <= 25) return 2; // iload <index>, lload <index>, fload <index>, dload <index>, aload <index>
        if (opcode <= 53) return 1; // <x>load_<n>, <x>aload
        if (opcode <= 58) return 2; // <x>store <index>
        if (opcode <= 86) return 1; // <x>store_<n>, <x>astore
        if (opcode <= 94) return 1; // pop*, dup*
        if (opcode <= 131) return 1; // swap, <x>add, <x>sub, <x>mul, <x>div, <x>rem, <x>neg, *sh*, <x>and, <x>or, <x>xor
        if (opcode <= 132) return 3; // iinc <index> <const>
        if (opcode <= 147) return 1; // <x>2<x>
        if (opcode <= 152) return 1; // <x>cmp<x>
        if (opcode <= 168) return 3; // if<cond> <branchbyte1> <branchbyte2>, goto <branchbyte1> <branchbyte2>, jsr <branchbyte1> <branchbyte2>
        if (opcode <= 169) return 2; // ret <index>
        if (opcode == 170) return tableswitchSize(bytecodes, codeIndex);
        if (opcode == 171) return lookupswitchSize(bytecodes, codeIndex);
        if (opcode <= 177) return 1; // <x>return
        if (opcode <= 184) return 3; // <get/put><static/field> <byte1> <byte2>, invokevirtual, invokespecial, invokestatic
        if (opcode == 185) return 5; // invokeinterface
        if (opcode == 186) return 5; // invokedynamic
        if (opcode == 187) return 3; // new <byte1> <byte2>
        if (opcode == 188) return 2; // newarray <atype>
        if (opcode == 189) return 3; // anewarray <byte1> <byte2>
        if (opcode <= 191) return 1; // arraylength, athrow
        if (opcode <= 193) return 3; // checkcast <byte1> <byte2>, instanceof <byte1> <byte2>
        if (opcode <= 195) return 1; // monitorenter, monitorexit
        if (opcode == 196) return wideSize(bytecodes, codeIndex);
        if (opcode == 197) return 4; // multianewarray
        if (opcode <= 199) return 3; // ifnull/ifnonnull <byte1> <byte2>
        if (opcode <= 201) return 5; // goto_w/jsr_w 4x<byte>
        return 1; // reserved opcodes
    }
    
    private static int tableswitchSize(byte[] bytecodes, long codeIndex) {
        int padding = 4 - ((int) codeIndex % 4); // Following byte begins at an address that is a multiple of 4
        int pos = (int) codeIndex + padding;
        pos += 4; // default
        int low = readInt(bytecodes, pos);
        pos += 4; // low
        int high = readInt(bytecodes, pos);
        pos += 4; // high
        pos += (high - low + 1) << 2; // high - low + 1 32-bit offsets
        return pos - (int) codeIndex;
    }
    
    private static int lookupswitchSize(byte[] bytecodes, long codeIndex) {
        int padding = 4 - ((int) codeIndex % 4); // Following byte begins at an address that is a multiple of 4
        int pos = (int) codeIndex + padding;
        pos += 4; // default
        int npairs = readInt(bytecodes, pos);
        pos += 4; // npairs
        pos += npairs << 3; // npairs 2x32-bit numbers (pairs)
        return pos - (int) codeIndex;
    }
    
    private static int wideSize(byte[] bytecodes, long codeIndex) {
        int opcode = bytecodes[(int) codeIndex + 1] & 0xFF;
        if (opcode == 132) { // iinc
            return 6;
        } else {
            return 4;
        }
    }
    
    private static int readUnsignedShort(byte[] bytecodes, int pos) {
        return ((bytecodes[pos] & 0xFF) << 8) | (bytecodes[pos + 1] & 0xFF);
    }
    
    private static int readInt(byte[] bytecodes, int pos) {
        return  (bytecodes[pos    ] & 0xFF) << 24 |
                (bytecodes[pos + 1] & 0xFF) << 16 |
                (bytecodes[pos + 2] & 0xFF) << 8  |
                (bytecodes[pos + 3] & 0xFF);
    }

}
