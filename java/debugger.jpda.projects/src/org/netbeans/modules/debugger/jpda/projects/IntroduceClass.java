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

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.openide.filesystems.FileObject;

/**
 * Refactoring inspired by IntroduceMethodFix, which creates a virtual class from a code snippet.
 * 
 * @author martin
 */
class IntroduceClass {

    private static final Logger LOG = Logger.getLogger(IntroduceClass.class.getName());
    
    private String snippetCode;
    private final int codeOffset;
    private String methodBodyCode;
    private String methodInvokeCode;
    private long classGeneratePosition;
    private final boolean staticContext;
    
    IntroduceClass(String snippetCode, int codeOffset, boolean staticContext) {
        this.snippetCode = snippetCode;
        this.codeOffset = codeOffset;
        this.staticContext = staticContext;
    }
    
    boolean computeIntroduceMethod(TreePathHandle h, CompilationInfo info, TreePath treePath, Tree tree) {
        TreePath block = treePath;//h.resolve(info);
        TreePath method = findMethod(block);
        if (method == null) {
            TreePath parentPath = treePath.getParentPath();
            if (parentPath == null) {
                return false;
            }
            method = parentPath;
        }
        CompilationUnitTree compilationUnit = info.getCompilationUnit();
        SourcePositions sourcePositions = info.getTrees().getSourcePositions();
        long endPosition = sourcePositions.getEndPosition(compilationUnit, method.getLeaf());
        if (TreeUtilities.CLASS_TREE_KINDS.contains(method.getLeaf().getKind())) {
            // We're in a class, generate before it's end:
            this.classGeneratePosition = endPosition - 1;
        } else {
            // Can generate an inner class after this endPosition
            this.classGeneratePosition = endPosition + 1;
        }
        List<? extends StatementTree> blockStatements = getStatements(block);
        StatementTree lastStatement = blockStatements.isEmpty() ? null : blockStatements.get(blockStatements.size() - 1);
        ScanLocalVars scanner = new ScanLocalVars(info, lastStatement);
        scanner.scan(block, null);
        Set<TypeMirror> exceptions = new HashSet<>();
        String returnType = scanner.getReturnType();
        if (returnType == null || !scanner.hasReturns()) {
            TypeMirror type = scanner.getReturnTypeMirror();
            if (type == null) {
                Element lastStatementElement;
                if (lastStatement != null && (lastStatementElement = getElement(info, new TreePath(treePath, lastStatement))) != null) {
                    type = lastStatementElement.asType();
                    if (TypeKind.EXECUTABLE.equals(type.getKind())) {
                        ExecutableType eType = (ExecutableType) type;
                        type = eType.getReturnType();
                        // Check that it ends with a semicolon:
                        long lsEnd = sourcePositions.getEndPosition(compilationUnit, lastStatement);
                        if (lsEnd < 0) {
                            lsEnd = this.snippetCode.length() - 1;
                        } else {
                            lsEnd -= codeOffset;
                        }
                        if (';' != this.snippetCode.charAt((int) lsEnd)) {
                            this.snippetCode = new StringBuilder(this.snippetCode)
                                    .insert((int) lsEnd + 1, ";")
                                    .toString();
                        }
                    }
                }
            }
            if (type != null && !TypeKind.VOID.equals(type.getKind())) {
                returnType = type.toString();
                // Prepend a return statement:
                long lsBegin = sourcePositions.getStartPosition(compilationUnit, lastStatement);
                // Make it relative to the beginning of the code snippet:
                lsBegin -= codeOffset;
                this.snippetCode = new StringBuilder(this.snippetCode)
                        .insert((int) lsBegin, "return ")
                        .append(';')
                        .toString();
            }
            if (returnType == null) {
                returnType = info.getTypes().getNoType(TypeKind.VOID).toString();
            }
        }
        for (StatementTree s : blockStatements) {
            TreePath path = new TreePath(treePath, s);
            exceptions.addAll(info.getTreeUtilities().getUncaughtExceptions(path));
        }
        Set<VariableElement> referencedVariables = scanner.getReferencedVariables();
        StringBuilder declaration = new StringBuilder(returnType);
        declaration.append(" invoke(");
        boolean isFirst = true;
        for (VariableElement var : referencedVariables) {
            if (!isFirst) {
                declaration.append(", ");
            }
            declaration.append(var.asType().toString());
            declaration.append(" ");
            declaration.append(var.getSimpleName().toString());
            isFirst = false;
        }
        declaration.append(") ");
        if (!exceptions.isEmpty()) {
            declaration.append("throws ");
            isFirst = true;
            for (TypeMirror exc : exceptions) {
                if (!isFirst) {
                    declaration.append(", ");
                }
                declaration.append(exc.toString());
                isFirst = false;
            }
        }
        declaration.append("{\n");
        declaration.append(snippetCode);
        declaration.append("\n}");
        this.methodBodyCode = declaration.toString();
        
        StringBuilder methodInvode = new StringBuilder("invoke(");
        isFirst = true;
        for (VariableElement var : referencedVariables) {
            if (!isFirst) {
                methodInvode.append(", ");
            }
            methodInvode.append(var.getSimpleName().toString());
            isFirst = false;
        }
        methodInvode.append(");");
        this.methodInvokeCode = methodInvode.toString();
        
        return true;
    }
    
    private static Element getElement(CompilationInfo info, TreePath path) {
        Element elm = info.getTrees().getElement(path);
        if (elm == null) {
            if (path.getLeaf() instanceof ExpressionStatementTree) {
                ExpressionStatementTree exp = (ExpressionStatementTree) path.getLeaf();
                path = new TreePath(path, exp.getExpression());
                elm = info.getTrees().getElement(path);
            }
        }
        return elm;
    }

    String getMethodInvoke() {
        return methodInvokeCode;
    }

    /**
     * Returns a path to the immediate enclosing method, lambda body or initializer block
     * @param path start of the search
     * @return path to the nearest enclosing executable or {@code null} in case of error.
     */
    static TreePath findMethod(TreePath path) {
        while (path != null) {
            Tree leaf = path.getLeaf();
            switch (leaf.getKind()) {
                case BLOCK:
                    if (path.getParentPath() != null && TreeUtilities.CLASS_TREE_KINDS.contains(path.getParentPath().getLeaf().getKind())) {
                        return path.getParentPath();
                    }
                    break;
                case METHOD:
                case LAMBDA_EXPRESSION:
                    return path;
                default:
                    break;
            }
            path = path.getParentPath();
        }
        return null;
    }
    
    private static List<? extends StatementTree> getStatements(TreePath firstLeaf) {
        Tree parentsLeaf = firstLeaf.getLeaf();
        List<? extends StatementTree> statements;
        switch (parentsLeaf.getKind()) {
            case BLOCK:
                statements = ((BlockTree) parentsLeaf).getStatements();
                break;
            case CASE:
                statements = ((CaseTree) parentsLeaf).getStatements();
                break;
            default:
                Tree first = firstLeaf.getLeaf();
                if (Tree.Kind.EXPRESSION_STATEMENT.equals(first.getKind())) {
                    statements = Collections.singletonList((StatementTree) firstLeaf.getLeaf());
                } else {
                    statements = Collections.emptyList();
                }
        }
        int s = statements.size();
        boolean haveOriginalStatements = true;
        while (s > 0 && ";".equals(statements.get(--s).toString().trim())) {
            if (haveOriginalStatements) {
                statements = new ArrayList<>(statements);
                haveOriginalStatements = false;
            }
            statements.remove(s);
        }
        return statements;
    }

    String computeIntroduceClass(String className, FileObject fo) throws IOException {
        String fileText = fo.asText();
        StringBuilder textBuilder = new StringBuilder(fileText);
        String classText = ((this.staticContext) ? "static " : "") + "class "+className+" {\n"+
                "public "+className+"() {}\n"+
                this.methodBodyCode+"\n}";
        textBuilder.insert((int) this.classGeneratePosition, classText);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Inserted class: '"+classText+"'");
            LOG.fine("Updated full file content:\n'"+textBuilder.toString()+"'");
        }
        return textBuilder.toString();
    }

}
