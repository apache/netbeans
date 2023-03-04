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
package org.netbeans.modules.java.debug;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.DocTreeScanner;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.debug.TreeNode.NodeChilren;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;

/**
 *
 * @author lahvac
 */
public class DocTreeNode extends AbstractNode implements OffsetProvider {

    private final CompilationInfo info;
    private final DocCommentTree docComment;
    private final DocTree tree;
    
    public DocTreeNode(CompilationInfo info, TreePath declaration, DocCommentTree docComment, DocTree tree) {
        super(new NodeChilren(children(info, declaration, docComment, tree)));
        this.info = info;
        this.docComment = docComment;
        this.tree = tree;
        setDisplayName(tree.getKind() + ":" + tree.toString());
    }
    
    private static List<Node> children(final CompilationInfo info, final TreePath declaration, final DocCommentTree docComment, final DocTree tree) {
        final List<Node> result = new ArrayList<Node>();
        
        tree.accept(new DocTreeScanner<Void, Void>() {
            @Override public Void scan(DocTree node, Void p) {
                result.add(new DocTreeNode(info, declaration, docComment, node));
                return null;
            }
            @Override
            public Void visitReference(ReferenceTree node, Void p) {
                DocTreePath currentPath = new DocTreePath(new DocTreePath(declaration, docComment), tree);
                result.add(TreeNode.nodeForElement(info, ((DocTrees) info.getTrees()).getElement(currentPath)));
                ExpressionTree classReference = info.getTreeUtilities().getReferenceClass(currentPath);
                if (classReference != null) {
                    result.add(TreeNode.getTree(info, new TreePath(declaration, classReference), /*TODO: cancel*/new AtomicBoolean()));
                }
                List<? extends Tree> methodParameters = info.getTreeUtilities().getReferenceParameters(currentPath);
                if (methodParameters != null) {
                    for (Tree param : methodParameters) {
                        result.add(TreeNode.getTree(info, new TreePath(declaration, param), /*TODO: cancel*/new AtomicBoolean()));
                    }
                }
                return super.visitReference(node, p);
            }
        }, null);
        
        return result;
    }

    @Override
    public int getStart() {
        return (int) ((DocTrees)info.getTrees()).getSourcePositions().getStartPosition(info.getCompilationUnit(), docComment, tree);
    }

    @Override
    public int getEnd() {
        return (int) ((DocTrees)info.getTrees()).getSourcePositions().getEndPosition(info.getCompilationUnit(), docComment, tree);
    }

    @Override
    public int getPreferredPosition() {
        return -1;
    }
    
}
