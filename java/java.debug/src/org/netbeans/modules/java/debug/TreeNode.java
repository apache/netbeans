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
package org.netbeans.modules.java.debug;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.support.CancellableTreeScanner;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Lahoda
 */
public class TreeNode extends AbstractNode implements OffsetProvider {
    
    private TreePath tree;
    private CompilationInfo info;
    private boolean synthetic;
    
    public static Node getTree(CompilationInfo info, TreePath tree, AtomicBoolean cancel) {
        List<Node> result = new ArrayList<Node>();
        
        new FindChildrenTreeVisitor(info, cancel).scan(tree, result);
        
        return result.get(0);
    }

    public static @CheckForNull Node findNode(@NonNull Node parent, @NonNull TreePath tree) {
        List<Tree> trees = new LinkedList<Tree>();

        while (tree != null) {
            trees.add(tree.getLeaf());
            tree = tree.getParentPath();
        }

        if (trees.isEmpty()) return null;
        
        Collections.reverse(trees);
        Iterator<Tree> it = trees.iterator();

        it.next();

        return findNode(parent, it);
    }

    private static @NonNull Node findNode(@NonNull Node parent, @NonNull Iterator<Tree> trees) {
        if (!trees.hasNext()) return parent;

        Tree next = trees.next();

        for (Node child : parent.getChildren().getNodes(true)) {
            if (child.getLookup().lookup(Tree.class) == next) {
                return findNode(child, trees);
            }
        }

        return parent;
    }

    public TreeNode(CompilationInfo info, TreePath tree, List<Node> nodes) {
        super(nodes.isEmpty() ? Children.LEAF: new NodeChilren(nodes), Lookups.singleton(tree.getLeaf()));
        this.tree = tree;
        this.info = info;
        this.synthetic = info.getTreeUtilities().isSynthetic(tree);
        int start = (int) info.getTrees().getSourcePositions().getStartPosition(tree.getCompilationUnit(), tree.getLeaf());
        int end   = (int) info.getTrees().getSourcePositions().getEndPosition(tree.getCompilationUnit(), tree.getLeaf());
        String text;

        if (start >= 0 && end >= 0 && end > start) {
            text = info.getText().substring(start, end);
        } else {
            text = tree.getLeaf().toString();
        }

        setDisplayName(tree.getLeaf().getKind().toString() + ":" + text); //NOI18N
        setIconBaseWithExtension("org/netbeans/modules/java/debug/resources/tree.png"); //NOI18N
    }

    @Override
    public String getHtmlDisplayName() {
        if (synthetic) {
            return "<html><font color='#808080'>" + translate(getDisplayName()); //NOI18N
        }
        
        return null;
    }
            
    private static String[] c = new String[] {"&", "<", ">", "\""}; // NOI18N
    private static String[] tags = new String[] {"&amp;", "&lt;", "&gt;", "&quot;"}; // NOI18N
    
    private String translate(String input) {
        for (int cntr = 0; cntr < c.length; cntr++) {
            input = input.replace(c[cntr], tags[cntr]);
        }
        
        return input;
    }
    
    public int getStart() {
        return (int)info.getTrees().getSourcePositions().getStartPosition(tree.getCompilationUnit(), tree.getLeaf());
    }

    public int getEnd() {
        return (int)info.getTrees().getSourcePositions().getEndPosition(tree.getCompilationUnit(), tree.getLeaf());
    }

    public int getPreferredPosition() {
        return -1;
    }
    
    static Node nodeForElement(CompilationInfo info, Element el) {
        if (el != null) {
            return new ElementNode(info, el, Collections.<Node>emptyList());
        } else {
            return new NotFoundElementNode(NbBundle.getMessage(TreeNode.class, "Cannot_Resolve_Element"));
        }
    }
        
    static final class NodeChilren extends Children.Array {
        public NodeChilren(List<Node> nodes) {
            super(nodes);
        }
    }
    
    private static class FindChildrenTreeVisitor extends CancellableTreeScanner<Void, List<Node>> {
        
        private final CompilationInfo info;
        private TreePath currentPath;
        
        public FindChildrenTreeVisitor(CompilationInfo info, AtomicBoolean cancel) {
            super(cancel);
            this.info = info;
        }

        public Void scan(TreePath path, List<Node> d) {
            currentPath = path.getParentPath();

            scan(path.getLeaf(), d);

            return null;
        }

        @Override
        public Void scan(Tree tree, List<Node> d) {
            if (tree != null) {
                TreePath oldPath = currentPath;
                try {
                    List<Node> below = new ArrayList<Node>();
                    currentPath = new TreePath(currentPath, tree);

                    //???
                    addCorrespondingElement(currentPath, below);
                    addCorrespondingType(currentPath, below);
                    addCorrespondingComments(currentPath, below);
                    addCorrespondingJavadoc(currentPath, below);

                    super.scan(tree, below);

                    d.add(new TreeNode(info, currentPath, below));
                } finally {
                    currentPath = oldPath;
                }
            }
            return null;
        }
        
        private void addCorrespondingJavadoc(TreePath currentPath, List<Node> below) {
            DocCommentTree docCommentTree = ((DocTrees) info.getTrees()).getDocCommentTree(currentPath);
            
            if (docCommentTree != null) {
                below.add(new DocTreeNode(info, currentPath, docCommentTree, docCommentTree));
            } else {
                below.add(new NotFoundJavadocNode("<javadoc-not-found>"));
            }
        }

        private void addCorrespondingElement(TreePath currentPath, List<Node> below) {
            Element el = info.getTrees().getElement(currentPath);
            
            below.add(nodeForElement(info, el));
        }

        private void addCorrespondingType(TreePath currentPath, List<Node> below) {
            TypeMirror tm = info.getTrees().getTypeMirror(currentPath);
            
            if (tm != null) {
                below.add(new TypeNode(tm));
            } else {
                below.add(new NotFoundTypeNode(NbBundle.getMessage(TreeNode.class, "Cannot_Resolve_Type")));
            }
        }
        
        private void addCorrespondingComments(TreePath currentPath, List<Node> below) {
            below.add(new CommentsNode(NbBundle.getMessage(TreeNode.class, "NM_Preceding_Comments"), info.getTreeUtilities().getComments(currentPath.getLeaf(), true)));
            below.add(new CommentsNode(NbBundle.getMessage(TreeNode.class, "NM_Trailing_Comments"), info.getTreeUtilities().getComments(currentPath.getLeaf(), false)));
        }
    }
    
    private static class NotFoundJavadocNode extends AbstractNode {
        
        public NotFoundJavadocNode(String name) {
            super(Children.LEAF);
            setName(name);
            setDisplayName(name);
//            setIconBaseWithExtension("org/netbeans/modules/java/debug/resources/element.png"); //NOI18N
        }
        
    }
    
    private static class NotFoundElementNode extends AbstractNode {
        
        public NotFoundElementNode(String name) {
            super(Children.LEAF);
            setName(name);
            setDisplayName(name);
            setIconBaseWithExtension("org/netbeans/modules/java/debug/resources/element.png"); //NOI18N
        }
        
    }
    
    private static class TypeNode extends AbstractNode {
        
        public TypeNode(TypeMirror type) {
            super(Children.LEAF);
            setDisplayName(type.getKind().toString() + ":" + type.toString()); //NOI18N
            setIconBaseWithExtension("org/netbeans/modules/java/debug/resources/type.png"); //NOI18N
        }
        
    }
    
    private static class NotFoundTypeNode extends AbstractNode {
        
        public NotFoundTypeNode(String name) {
            super(Children.LEAF);
            setName(name);
            setDisplayName(name);
            setIconBaseWithExtension("org/netbeans/modules/java/debug/resources/type.png"); //NOI18N
        }
        
    }    
}
