/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
