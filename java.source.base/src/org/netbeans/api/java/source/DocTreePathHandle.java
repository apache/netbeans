/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.api.java.source;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.DocTreePathScanner;
import com.sun.source.util.DocTreeScanner;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.tree.DCTree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.source.PositionRefProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 * Represents a handle for {@link TreePath} which can be kept and later resolved
 * by another javac. The Javac {@link Element}s are valid only in the single
 * {@link javax.tools.CompilationTask} or single run of the
 * {@link org.netbeans.api.java.source.CancellableTask}. If the client needs to
 * keep a reference to the {@link TreePath} and use it in the other
 * CancellableTask he has to serialize it into the {@link TreePathHandle}.
 * <div class="nonnormative">
 * <p>
 * Typical usage of TreePathHandle enclElIsCorrespondingEl:
 * </p>
 * <pre>
 * final TreePathHandle[] tpHandle = new TreePathHandle[1];
 * javaSource.runCompileControlTask(new CancellableTask<CompilationController>() {
 *     public void run(CompilationController compilationController) {
 *         parameter.toPhase(Phase.RESOLVED);
 *         CompilationUnitTree cu = compilationController.getTree ();
 *         TreePath treePath = getInterestingTreePath (cu);
 *         treePathHandle[0] = TreePathHandle.create (element, compilationController);
 *    }
 * },priority);
 *
 * otherJavaSource.runCompileControlTask(new CancellableTask<CompilationController>() {
 *     public void run(CompilationController compilationController) {
 *         parameter.toPhase(Phase.RESOLVED);
 *         TreePath treePath = treePathHanlde[0].resolve (compilationController);
 *         ....
 *    }
 * },priority);
 * </pre>
 * </div>
 *
 *
 * @author Jan Becicka
 * @author Ralph Benjamin Ruijs
 * @since 0.124
 */
public final class DocTreePathHandle {

    private static Logger log = Logger.getLogger(DocTreePathHandle.class.getName());
    private final Delegate delegate;

    private DocTreePathHandle(Delegate d) {
        if (d == null) {
            throw new IllegalArgumentException();
        }
        this.delegate = d;
    }

    /**
     * Resolves an {@link DocTreePath} from the {@link DocTreePathHandle}.
     *
     * @param compilationInfo representing the
     * {@link javax.tools.CompilationTask}
     * @return resolved subclass of {@link DocTreePath} or null if the doctree does
     * not exist on the classpath/sourcepath of
     * {@link javax.tools.CompilationTask}.
     * @throws IllegalArgumentException when this {@link DocTreePathHandle} is not
     * created for a source represented by the compilationInfo.
     */
    public DocTreePath resolve(final CompilationInfo compilationInfo) throws IllegalArgumentException {
        final DocTreePath result = this.delegate.resolve(compilationInfo);
        if (result == null) {
            Logger.getLogger(DocTreePathHandle.class.getName()).info("Cannot resolve: " + toString());
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DocTreePathHandle)) {
            return false;
        }

        if (delegate.getClass() != ((DocTreePathHandle) obj).delegate.getClass()) {
            return false;
        }

        return delegate.equalsHandle(((DocTreePathHandle) obj).delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    /**
     * returns {@link TreePathHandle} corresponding to this {@link DocTreePathHandle}
     *
     * @return {@link TreePathHandle}
     */
    public TreePathHandle getTreePathHandle() {
        return this.delegate.getTreePathHandle();
    }

    /**
     * Returns the {@link DocTree.Kind} of this DocTreePathHandle, it returns the kind
     * of the {@link DocTree} from which the handle was created.
     *
     * @return {@link Tree.Kind}
     */
    public DocTree.Kind getKind() {
        return this.delegate.getKind();
    }

    /**
     * Factory method for creating {@link DocTreePathHandle}.
     *
     * @param docTreePath for which the {@link DocTreePathHandle} should be created.
     * @param javac
     * @return a new {@link DocTreePathHandle}
     * @throws java.lang.IllegalArgumentException if arguments are not supported
     */
    public static DocTreePathHandle create(final DocTreePath docTreePath, CompilationInfo javac) throws IllegalArgumentException {
        Parameters.notNull("docTreePath", docTreePath);
        Parameters.notNull("javac", javac);

        TreePathHandle treePathHandle = TreePathHandle.create(docTreePath.getTreePath(), javac);
        if(treePathHandle.getFileObject() == null) {
            return null;
        }
        int position = (int) ((DCTree) docTreePath.getLeaf()).getSourcePosition((DCTree.DCDocComment)docTreePath.getDocComment());
        if (position == (-1)) {
            DocTree docTree = docTreePath.getLeaf();
            if(docTree == docTreePath.getDocComment()) {
                return new DocTreePathHandle(new DocCommentDelegate(treePathHandle));
            }
            int index = listChildren(docTreePath.getParentPath().getLeaf()).indexOf(docTree);
            assert index != (-1);
            return new DocTreePathHandle(new CountingDelegate(treePathHandle, index, docTreePath.getLeaf().getKind()));
        }
        Position pos = createPositionRef(treePathHandle.getFileObject(), position, Bias.Forward);
        return new DocTreePathHandle(new DocTreeDelegate(pos, new DocTreeDelegate.KindPath(docTreePath), treePathHandle));
    }

    private static boolean isSupported(Element el) {
        switch (el.getKind()) {
            case PACKAGE:
            case CLASS:
            case INTERFACE:
            case ENUM:
            case METHOD:
            case CONSTRUCTOR:
            case INSTANCE_INIT:
            case STATIC_INIT:
            case FIELD:
            case ANNOTATION_TYPE:
            case ENUM_CONSTANT:
                return true;
            default:
                return false;
        }
    }
    
        private static List<DocTree> listChildren(@NonNull DocTree t) {
        final List<DocTree> result = new LinkedList<DocTree>();

        t.accept(new DocTreeScanner<Void, Void>() {
            @Override
            public Void scan(DocTree node, Void p) {
                result.add(node);
                return null;
            }
        }, null);

        return result;
    }

    private static Position createPositionRef(FileObject file, int position, Position.Bias bias) {
        try {
            PositionRefProvider prp = PositionRefProvider.get(file);
            Position positionRef = prp != null ? prp.createPosition(position, bias) : null;
            if (positionRef != null) {
                return positionRef;
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        throw new IllegalStateException("Cannot create PositionRef for file " + file.getPath() + ". CloneableEditorSupport not found");
    }

    @Override
    public String toString() {
        return "DocTreePathHandle[delegate:" + delegate + "]";
    }

    static interface Delegate {

        public DocTreePath resolve(final CompilationInfo compilationInfo) throws IllegalArgumentException;

        public boolean equalsHandle(Delegate obj);

        public int hashCode();

        public DocTree.Kind getKind();

        public TreePathHandle getTreePathHandle();
    }

    private static final class DocTreeDelegate implements Delegate {

        private final Position position;
        private final KindPath kindPath;
        private final TreePathHandle treePathHandle;
        private final DocTree.Kind kind;

        private DocTreeDelegate(Position position, KindPath kindPath, TreePathHandle treePathHandle) {
            this.kindPath = kindPath;
            this.position = position;
            this.treePathHandle = treePathHandle;
            if (kindPath != null) {
                this.kind = kindPath.kindPath.get(0);
            } else {
                this.kind = null;
            }
        }
        
        /**
         * Resolves an {@link TreePath} from the {@link TreePathHandle}.
         *
         * @param javac representing the
         * {@link javax.tools.CompilationTask}
         * @return resolved subclass of {@link Element} or null if the element
         * does not exist on the classpath/sourcepath of
         * {@link javax.tools.CompilationTask}.
         * @throws IllegalArgumentException when this {@link TreePathHandle} is
         * not created for a source represented by the compilationInfo.
         */
        public DocTreePath resolve(final CompilationInfo javac) throws IllegalArgumentException {
            assert javac != null;
            TreePath treePath = treePathHandle.resolve(javac);
            if(treePath == null) {
                throw new IllegalArgumentException("treePathHandle.resolve(compilationInfo) returned null for treePathHandle " + treePathHandle);    //NOI18N
            }
            DocTreePath tp = null;
            DocCommentTree doc = javac.getDocTrees().getDocCommentTree(treePath);
            if (doc == null) {
                // no doc comment for the TreePath
                return null;
            }
            int pos = position.getOffset();
            tp = resolvePathForPos(javac, treePath, doc, pos + 1);
            if (tp != null) {
                return tp;
            }
            tp = resolvePathForPos(javac, treePath, doc, pos);
            return tp;
        }

        private DocTreePath resolvePathForPos(CompilationInfo javac, TreePath treePath, DocCommentTree doc, int pos) {
            DocTreePath tp = javac.getTreeUtilities().pathFor(treePath, doc, pos);
            while (tp != null) {
                KindPath kindPath1 = new KindPath(tp);
                kindPath.getList().remove(Tree.Kind.ERRONEOUS);
                if (kindPath1.equals(kindPath)) {
                    return tp;
                }
                tp = tp.getParentPath();
            }
            return null;
        }

        public boolean equalsHandle(Delegate obj) {
            DocTreeDelegate other = (DocTreeDelegate) obj;
            if (this.position.getOffset() != this.position.getOffset()) {
                return false;
            }
            return other.getTreePathHandle().equals(treePathHandle);
        }

        @Override
        public int hashCode() {
            if (this.position == null) {
                return 553 + treePathHandle.hashCode();
            }
            int hash = 7;
            hash = 79 * hash + this.position.getOffset();
            hash = 79 * hash + this.treePathHandle.hashCode();
            return hash;
        }
        
        /**
         * Returns the {@link Tree.Kind} of this TreePathHandle, it returns the
         * kind of the {@link Tree} from which the handle was created.
         *
         * @return {@link Tree.Kind}
         */
        public DocTree.Kind getKind() {
            return kind;
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "[kind:" + kind + ", treepathHandle:" + treePathHandle + "]";
        }

        @Override
        public TreePathHandle getTreePathHandle() {
            return treePathHandle;
        }

        static class KindPath {

            private ArrayList<DocTree.Kind> kindPath = new ArrayList();

            KindPath(DocTreePath treePath) {
                while (treePath != null) {
                    kindPath.add(treePath.getLeaf().getKind());
                    treePath = treePath.getParentPath();
                }
            }

            public int hashCode() {
                return kindPath.hashCode();
            }

            public boolean equals(Object object) {
                if (object instanceof KindPath) {
                    return kindPath.equals(((KindPath) object).kindPath);
                }
                return false;
            }

            public ArrayList<DocTree.Kind> getList() {
                return kindPath;
            }
        }
    }
    
    private static final class DocCommentDelegate implements Delegate {

        private final TreePathHandle parent;

        public DocCommentDelegate(TreePathHandle parent) {
            this.parent = parent;
        }

        public DocTreePath resolve(CompilationInfo javac) throws IllegalArgumentException {
            TreePath p = parent.resolve(javac);

            if (p == null) {
                return null;
            }
            
            DocCommentTree docCommentTree = javac.getDocTrees().getDocCommentTree(p);
            return new DocTreePath(p, docCommentTree);
        }

        public boolean equalsHandle(Delegate obj) {
            return this == obj;//XXX
        }

        @Override
        public TreePathHandle getTreePathHandle() {
            return parent;
        }

        @Override
        public DocTree.Kind getKind() {
            return DocTree.Kind.DOC_COMMENT;
        }
    }

    private static final class CountingDelegate implements Delegate {

        private final TreePathHandle parent;
        private final int index;
        private final DocTree.Kind kind;

        public CountingDelegate(TreePathHandle parent, int index, DocTree.Kind kind) {
            this.parent = parent;
            this.index = index;
            this.kind = kind;
        }

        public DocTreePath resolve(CompilationInfo javac) throws IllegalArgumentException {
            TreePath p = parent.resolve(javac);

            if (p == null) {
                return null;
            }
            
            DocCommentTree docCommentTree = javac.getDocTrees().getDocCommentTree(p);
            return getChild(docCommentTree, index);
        }

        public boolean equalsHandle(Delegate obj) {
            return this == obj;//XXX
        }

        public DocTree.Kind getKind() {
            return kind;
        }

        @Override
        public TreePathHandle getTreePathHandle() {
            return parent;
        }
    }

    private static DocTreePath getChild(@NonNull DocCommentTree t, final int index) {
        final DocTreePath[] result = new DocTreePath[1];

        t.accept(new DocTreePathScanner<DocTreePath, Void>() {
            int count = 0;
            @Override
            public DocTreePath scan(DocTree node, Void p) {
                if(index == count) {
                    result[0] = getCurrentPath();
                }
                return null;
            }
        }, null);

        return result[0];
    }
}
