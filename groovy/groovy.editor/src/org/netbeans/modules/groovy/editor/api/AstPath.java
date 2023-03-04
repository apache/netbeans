/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.groovy.editor.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;
import javax.swing.text.BadLocationException;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.netbeans.editor.BaseDocument;
import org.openide.util.Exceptions;

/**
 * This represents a path in a Groovy AST.
 *
 * @author Tor Norbye
 * @author Martin Adamek
 */
public class AstPath implements Iterable<ASTNode> {
    
    private ArrayList<ASTNode> path = new ArrayList<ASTNode>(30);

    private int lineNumber = -1;

    private int columnNumber = -1;

    public AstPath() {
        super();
    }

    public AstPath(ASTNode root, int caretOffset, BaseDocument document) {
        this(root, caretOffset, document, false);
    }
    
    /**
     * Constructs AST path up to the specified position. Path starts at root, and 
     * leads up to and including the node that contains the `caretOffset'. If `dot` 
     * is false, the path leads to the deepest node that spans the offset. If true,
     * terminates at outer expression for method call, property access. The behaviour is different
     * just for the last character of an expression, as the boundary is common for several nodes
     * in the AST hierarchy; has no effects for other offsets. dot=true is suitable 
     * for working with the result type/value, not the symbol itself.
     * <p>
     * <b>Note: this method is implementation detail and is not intended to be called from
     * other modules.</b>
     * @param root root of the path
     * @param caretOffset offset that identifies the leaf node
     * @param document the document
     * @param dot if true, terminate the returned path at outer expression node.
     * @since 1.80
     */
    public AstPath(ASTNode root, int caretOffset, BaseDocument document, boolean dot) {
        try {
            // make sure offset is not higher than document length, see #138353
            int length = document.getLength();
            int offset = length == 0 ? 0 : caretOffset + 1;
            if (length > 0 && offset >= length) {
                offset = length - 1;
            }
            Scanner scanner = new Scanner(document.getText(0, offset));
            int line = 0;
            String lineText = "";
            while (scanner.hasNextLine()) {
                lineText = scanner.nextLine();
                line++;
            }
            int column = lineText.length();

            this.lineNumber = line;
            this.columnNumber = column;

            findPathTo(root, line, column, dot);
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
    }

    /**
     * Initialize a node path to the given caretOffset
     */
    public AstPath(ASTNode root, int line, int column) {
        this.lineNumber = line;
        this.columnNumber = column;

        findPathTo(root, line, column, false);
    }

    /**
     * Find the path to the given node in the AST
     */
    @SuppressWarnings("unchecked")
    public AstPath(ASTNode node, ASTNode target) {
        if (!find(node, target)) {
            path.clear();
        } else {
            // Reverse the list such that node is on top
            // When I get time rewrite the find method to build the list that way in the first place
            Collections.reverse(path);
        }
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void descend(ASTNode node) {
        path.add(node);
    }

    public void ascend() {
        path.remove(path.size() - 1);
    }

    /**
     * Find the position closest to the given offset in the AST. Place the path from the leaf up to the path in the
     * passed in path list.
     */
    @SuppressWarnings("unchecked")
    private ASTNode findPathTo(ASTNode node, int line, int column, boolean outermost) {
        
        assert node != null : "ASTNode should not be null";
        assert node instanceof ModuleNode : "ASTNode must be a ModuleNode";
        assert line >=0 : "line number was negative: " + line + " on the ModuleNode node with main class name: " + ((ModuleNode)node).getMainClassName();
        assert column >=0 : "column number was negative: " + column;
        
        path.addAll(find(node, line, column, outermost));

        // in scripts ClassNode is not in path, let's add it
        // find class that has same name as the file
        if (path.isEmpty() || !(path.get(0) instanceof ClassNode)) {
            ModuleNode moduleNode = (ModuleNode) node;
            String name = moduleNode.getContext().getName();
            int index = name.lastIndexOf(".groovy"); // NOI18N
            if (index != -1) {
                name = name.substring(0, index);
            }
            index = name.lastIndexOf('.');
            if (index != -1) {
                name = name.substring(index + 1);
            }
            for (Object object : moduleNode.getClasses()) {
                ClassNode classNode = (ClassNode) object;
                if (name.equals(classNode.getNameWithoutPackage())) {
                    path.add(0, classNode);
                    break;
                }
            }
        }

        // let's fix script class - run method
        // FIXME this should be more accurate - package
        // and imports are not in the method ;)
        if (!path.isEmpty() && (path.get(0) instanceof ClassNode)) {
            ClassNode clazz = (ClassNode) path.get(0);
            if (clazz.isScript()
                    && (path.size() == 1 || path.get(1) instanceof Expression || path.get(1) instanceof Statement)) {

                MethodNode method = clazz.getMethod("run", new Parameter[]{}); // NOI18N
                if (method != null) {
                    if (method.getCode() != null && (path.size() <= 1 || method.getCode() != path.get(1))) {
                        path.add(1, method.getCode());
                    }
                    path.add(1, method);
                }
            }
        }

        path.add(0, node);

        ASTNode result = path.get(path.size() - 1);

        return result;
    }

    @SuppressWarnings("unchecked")
    private List<ASTNode> find(ASTNode node, int line, int column, boolean outermost) {
        
        assert line >=0 : "line number was negative: " + line;
        assert column >=0 : "column number was negative: " + column;
        assert node != null : "ASTNode should not be null";
        assert node instanceof ModuleNode : "ASTNode must be a ModuleNode";
        
        ModuleNode moduleNode = (ModuleNode) node;
        PathFinderVisitor pathFinder = new PathFinderVisitor(moduleNode.getContext(), line, column, outermost);

        for (ClassNode classNode : moduleNode.getClasses()) {
            pathFinder.visitClass(classNode);
        }

        for (MethodNode methodNode : moduleNode.getMethods()) {
            pathFinder.visitMethod(methodNode);
        }

        return pathFinder.getPath();
    }

    /**
     * Find the path to the given node in the AST
     */
    @SuppressWarnings("unchecked")
    public boolean find(ASTNode node, ASTNode target) {
        if (node == target) {
            return true;
        }

        List<ASTNode> children = ASTUtils.children(node);

        for (ASTNode child : children) {
            boolean found = find(child, target);

            if (found) {
                path.add(child);

                return found;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Path(");
        sb.append(path.size());
        sb.append(")=[");

        for (ASTNode n : path) {
            String name = n.getClass().getName();
            name = name.substring(name.lastIndexOf('.') + 1);
            sb.append(name);
            sb.append("\n");
        }

        sb.append("]");

        return sb.toString();
    }

    public ASTNode leaf() {
        if (path.isEmpty()) {
            return null;
        } else {
            return path.get(path.size() - 1);
        }
    }

    public ASTNode leafParent() {
        if (path.size() < 2) {
            return null;
        } else {
            return path.get(path.size() - 2);
        }
    }

    public ASTNode leafGrandParent() {
        if (path.size() < 3) {
            return null;
        } else {
            return path.get(path.size() - 3);
        }
    }

    public ASTNode root() {
        if (path.isEmpty()) {
            return null;
        } else {
            return path.get(0);
        }
    }

    /** Return an iterator that returns the elements from the leaf back up to the root */
    @Override
    public Iterator<ASTNode> iterator() {
        return new LeafToRootIterator(path);
    }

    /** REturn an iterator that starts at the root and walks down to the leaf */
    public ListIterator<ASTNode> rootToLeaf() {
        return path.listIterator();
    }

    /** Return an iterator that walks from the leaf back up to the root */
    public ListIterator<ASTNode> leafToRoot() {
        return new LeafToRootIterator(path);
    }

    private static class LeafToRootIterator implements ListIterator<ASTNode> {
        private final ListIterator<ASTNode> it;

        private LeafToRootIterator(ArrayList<ASTNode> path) {
            it = path.listIterator(path.size());
        }

        @Override
        public boolean hasNext() {
            return it.hasPrevious();
        }

        @Override
        public ASTNode next() {
            return it.previous();
        }

        @Override
        public boolean hasPrevious() {
            return it.hasNext();
        }

        @Override
        public ASTNode previous() {
            return it.next();
        }

        @Override
        public int nextIndex() {
            return it.previousIndex();
        }

        @Override
        public int previousIndex() {
            return it.nextIndex();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void set(ASTNode arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void add(ASTNode arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

}
