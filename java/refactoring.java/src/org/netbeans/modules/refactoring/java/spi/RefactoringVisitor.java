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

package org.netbeans.modules.refactoring.java.spi;

import com.sun.source.doctree.AttributeTree;
import com.sun.source.doctree.AuthorTree;
import com.sun.source.doctree.CommentTree;
import com.sun.source.doctree.DeprecatedTree;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocRootTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.DocTreeVisitor;
import com.sun.source.doctree.EndElementTree;
import com.sun.source.doctree.EntityTree;
import com.sun.source.doctree.ErroneousTree;
import com.sun.source.doctree.HiddenTree;
import com.sun.source.doctree.IdentifierTree;
import com.sun.source.doctree.IndexTree;
import com.sun.source.doctree.InheritDocTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.LiteralTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ProvidesTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.SeeTree;
import com.sun.source.doctree.SerialDataTree;
import com.sun.source.doctree.SerialFieldTree;
import com.sun.source.doctree.SerialTree;
import com.sun.source.doctree.SinceTree;
import com.sun.source.doctree.StartElementTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.doctree.ThrowsTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import com.sun.source.doctree.UnknownInlineTagTree;
import com.sun.source.doctree.UsesTree;
import com.sun.source.doctree.ValueTree;
import com.sun.source.doctree.VersionTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.DocTreePathScanner;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.io.IOException;
import javax.lang.model.element.Element;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Becicka
 */
public class RefactoringVisitor extends ErrorAwareTreePathScanner<Tree, Element> implements DocTreeVisitor<DocTree, Element> {
    /**
     * 
     */
    protected WorkingCopy workingCopy;
    /**
     * 
     */
    protected TreeMaker make;
    
    private final DocTreePathScannerImpl docScanner;

    public RefactoringVisitor() {
        this(false);
    }
    
    /**
     * Creates a new Refactoring Visitor.
     * 
     * @param javadoc true if javadoc should be visited, false otherwise
     * @since 1.47
     */
    public RefactoringVisitor(boolean javadoc) {
        docScanner = javadoc ? new DocTreePathScannerImpl(this) : null;
    }
    
    /**
     * 
     * @param workingCopy 
     * @throws org.netbeans.modules.refactoring.java.spi.ToPhaseException 
     */
    public void setWorkingCopy(WorkingCopy workingCopy) throws ToPhaseException {
        this.workingCopy = workingCopy;
        try {
            if (this.workingCopy.toPhase(JavaSource.Phase.RESOLVED) != JavaSource.Phase.RESOLVED) {
                throw new ToPhaseException();
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
        this.make = workingCopy.getTreeMaker();
    }
    
    /**
     * 
     * @param oldTree 
     * @param newTree 
     */
    protected void rewrite(Tree oldTree, Tree newTree) {
        workingCopy.rewrite(oldTree, newTree);
        TreePath current = getCurrentPath();
        if (current.getLeaf() == oldTree) {
            JavaRefactoringUtils.cacheTreePathInfo(current, workingCopy);
        } else {
            if (oldTree!=null) {
                TreePath tp = workingCopy.getTrees().getPath(current.getCompilationUnit(), oldTree);
                if(tp != null) {
                    JavaRefactoringUtils.cacheTreePathInfo(tp, workingCopy);
                }
            }
        }
    }
    
    /**
     * Replaces the original doctree <code>oldTree</code> with the new one -
     * <code>newTree</code> for a specific tree.
     * <p>
     * To create a new javadoc comment, use
     * <code>rewrite(tree, null, docCommentTree)</code>.
     * <p>
     * <code>tree</code> and <code>newTree</code> cannot be <code>null</code>.
     * If <code>oldTree</code> is null, <code>newTree</code> must be of kind
     * {@link com.sun.source.doctree.DocTree.Kind#DOC_COMMENT DOC_COMMENT}.
     * 
     * @param tree     the tree to which the doctrees belong.
     * @param oldTree  tree to be replaced, use tree already represented in
     *                 source code. <code>null</code> to create a new file.
     * @param newTree  new tree, either created by <code>TreeMaker</code>
     *                 or obtained from different place. <code>null</code>
     *                 values are not allowed.
     * @throws IllegalStateException if <code>toPhase()</code> method was not
     *         called before.
     * @since 1.47
     */
    protected void rewrite(@NonNull Tree tree, @NullAllowed DocTree oldTree, @NonNull DocTree newTree) {
        workingCopy.rewrite(tree, oldTree, newTree);
        TreePath current = getCurrentPath();
        if (current.getLeaf() == tree) {
            JavaRefactoringUtils.cacheTreePathInfo(current, workingCopy);
        } else {
            if (oldTree!=null) {
                TreePath tp = workingCopy.getTrees().getPath(current.getCompilationUnit(), tree);
                if(tp != null) {
                    JavaRefactoringUtils.cacheTreePathInfo(tp, workingCopy);
                }
            }
        }
    }

    @Override
    public Tree scan(Tree tree, Element p) {
        final TreePath currentPath = getCurrentPath();
        if(docScanner != null && tree != null && currentPath != null) {
            switch(tree.getKind()) {
                case METHOD:
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                case RECORD:
                case VARIABLE:
                    TreePath path = new TreePath(currentPath, tree);
                    scanJavadoc(path, p);
                default:
                    break;
            }
        }
        return super.scan(tree, p);
    }
    
    /**
     * Get the current path for the doctree node, as built up by the currently
     * active set of scan calls.
     *
     * @return the current DocTreePath, or null of javadoc was disabled
     * @see #RefactoringVisitor(boolean) 
     * @since 1.47
     */
    @CheckForNull
    public DocTreePath getCurrentDocPath() {
        return docScanner.getCurrentPath();
    }

    /**
     * @since 1.47
     */
    @Override
    public DocTree visitAttribute(AttributeTree node, Element p) {
        return docScanner.visitAttribute(node, p, null);
    }

    /**
     * @since 1.47
     */
    @Override
    public DocTree visitAuthor(AuthorTree node, Element p) {
        return docScanner.visitAuthor(node, p, null);
    }

    /**
     * @since 1.47
     */
    @Override
    public DocTree visitComment(CommentTree node, Element p) {
        return docScanner.visitComment(node, p, null);
    }

    /**
     * @since 1.47
     */
    @Override
    public DocTree visitDeprecated(DeprecatedTree node, Element p) {
        return docScanner.visitDeprecated(node, p, null);
    }

    /**
     * @since 1.47
     */
    @Override
    public DocTree visitDocComment(DocCommentTree node, Element p) {
        return docScanner.visitDocComment(node, p, null);
    }

    /**
     * @since 1.47
     */
    @Override
    public DocTree visitDocRoot(DocRootTree node, Element p) {
        return docScanner.visitDocRoot(node, p, null);
    }

    /**
     * @since 1.47
     */
    @Override
    public DocTree visitEndElement(EndElementTree node, Element p) {
        return docScanner.visitEndElement(node, p, null);
    }

    /**
     * @since 1.47
     */
    @Override
    public DocTree visitEntity(EntityTree node, Element p) {
        return docScanner.visitEntity(node, p, null);
    }

    /**
     * @since 1.47
     */
    @Override
    public DocTree visitErroneous(ErroneousTree node, Element p) {
        return docScanner.visitErroneous(node, p, null);
    }

    @Override
    public DocTree visitHidden(HiddenTree node, Element p) {
        return docScanner.visitHidden(node, p, null);
    }

    /**
     * @since 1.47
     */
    @Override
    public DocTree visitIdentifier(IdentifierTree node, Element p) {
        return docScanner.visitIdentifier(node, p, null);
    }

    @Override
    public DocTree visitIndex(IndexTree node, Element p) {
        return docScanner.visitIndex(node, p, null);
    }

    /**
     * @since 1.47
     */
    @Override
    public DocTree visitInheritDoc(InheritDocTree node, Element p) {
        return docScanner.visitInheritDoc(node, p, null);
    }

    /**
     * @since 1.47
     */
    @Override
    public DocTree visitLink(LinkTree node, Element p) {
        return docScanner.visitLink(node, p, null);
    }

    /**
     * @since 1.47
     */
    @Override
    public DocTree visitLiteral(LiteralTree node, Element p) {
        return docScanner.visitLiteral(node, p, null);
    }

    /**
     * @since 1.47
     */
    @Override
    public DocTree visitParam(ParamTree node, Element p) {
        return docScanner.visitParam(node, p, null);
    }

    /**
     * @since 1.62
     */
    @Override
    public DocTree visitProvides(ProvidesTree node, Element p) {
        return docScanner.visitProvides(node, p, null);
    }

    /**
     * @since 1.47
     */
    @Override
    public DocTree visitReference(ReferenceTree node, Element p) {
        return docScanner.visitReference(node, p, null);
    }

    /**
     * @since 1.47
     */
    @Override
    public DocTree visitReturn(ReturnTree node, Element p) {
        return docScanner.visitReturn(node, p, null);
    }

    /**
     * @since 1.47
     */
    @Override
    public DocTree visitSee(SeeTree node, Element p) {
        return docScanner.visitSee(node, p, null);
    }

    /**
     * @since 1.47
     */
    @Override
    public DocTree visitSerial(SerialTree node, Element p) {
        return docScanner.visitSerial(node, p, null);
    }

    /**
     * @since 1.47
     */
    @Override
    public DocTree visitSerialData(SerialDataTree node, Element p) {
        return docScanner.visitSerialData(node, p, null);
    }

    /**
     * @since 1.47
     */
    @Override
    public DocTree visitSerialField(SerialFieldTree node, Element p) {
        return docScanner.visitSerialField(node, p, null);
    }

    /**
     * @since 1.47
     */
    @Override
    public DocTree visitSince(SinceTree node, Element p) {
        return docScanner.visitSince(node, p, null);
    }

    /**
     * @since 1.47
     */
    @Override
    public DocTree visitStartElement(StartElementTree node, Element p) {
        return docScanner.visitStartElement(node, p, null);
    }

    /**
     * @since 1.47
     */
    @Override
    public DocTree visitText(TextTree node, Element p) {
        return docScanner.visitText(node, p, null);
    }

    /**
     * @since 1.47
     */
    @Override
    public DocTree visitThrows(ThrowsTree node, Element p) {
        return docScanner.visitThrows(node, p, null);
    }

    /**
     * @since 1.47
     */
    @Override
    public DocTree visitUnknownBlockTag(UnknownBlockTagTree node, Element p) {
        return docScanner.visitUnknownBlockTag(node, p, null);
    }

    /**
     * @since 1.47
     */
    @Override
    public DocTree visitUnknownInlineTag(UnknownInlineTagTree node, Element p) {
        return docScanner.visitUnknownInlineTag(node, p, null);
    }

    /**
     * @since 1.62
     */
    @Override
    public DocTree visitUses(UsesTree node, Element p) {
        return docScanner.visitUses(node, p, null);
    }

    /**
     * @since 1.47
     */
    @Override
    public DocTree visitValue(ValueTree node, Element p) {
        return docScanner.visitValue(node, p, null);
    }

    /**
     * @since 1.47
     */
    @Override
    public DocTree visitVersion(VersionTree node, Element p) {
        return docScanner.visitVersion(node, p, null);
    }

    
    /**
     * @since 1.47
     */
    @Override
    public DocTree visitOther(DocTree node, Element p) {
        return docScanner.visitOther(node, p, null);
    }

    private void scanJavadoc(TreePath path, Element p) {
        DocCommentTree docCommentTree = workingCopy.getDocTrees().getDocCommentTree(path);
        if(docCommentTree != null) {
            DocTreePath docTreePath = new DocTreePath(path, docCommentTree);
            docScanner.scan(docTreePath, p);
        }
    }

    private static class DocTreePathScannerImpl extends DocTreePathScanner<DocTree, Element> {

        private final RefactoringVisitor instance;

        public DocTreePathScannerImpl(RefactoringVisitor instance) {
            this.instance = instance;
        }

        @Override
        public DocTree visitAttribute(AttributeTree node, Element p) {
            return instance.visitAttribute(node, p);
        }

        @Override
        public DocTree visitAuthor(AuthorTree node, Element p) {
            return instance.visitAuthor(node, p);
        }

        @Override
        public DocTree visitComment(CommentTree node, Element p) {
            return instance.visitComment(node, p);
        }

        @Override
        public DocTree visitDeprecated(DeprecatedTree node, Element p) {
            return instance.visitDeprecated(node, p);
        }

        @Override
        public DocTree visitDocComment(DocCommentTree node, Element p) {
            return instance.visitDocComment(node, p);
        }

        @Override
        public DocTree visitDocRoot(DocRootTree node, Element p) {
            return instance.visitDocRoot(node, p);
        }

        @Override
        public DocTree visitEndElement(EndElementTree node, Element p) {
            return instance.visitEndElement(node, p);
        }

        @Override
        public DocTree visitEntity(EntityTree node, Element p) {
            return instance.visitEntity(node, p);
        }

        @Override
        public DocTree visitErroneous(ErroneousTree node, Element p) {
            return instance.visitErroneous(node, p);
        }

        @Override
        public DocTree visitHidden(HiddenTree node, Element p) {
            return instance.visitHidden(node, p);
        }

        @Override
        public DocTree visitIdentifier(IdentifierTree node, Element p) {
            return instance.visitIdentifier(node, p);
        }

        @Override
        public DocTree visitIndex(IndexTree node, Element p) {
            return instance.visitIndex(node, p);
        }

        @Override
        public DocTree visitInheritDoc(InheritDocTree node, Element p) {
            return instance.visitInheritDoc(node, p);
        }

        @Override
        public DocTree visitLink(LinkTree node, Element p) {
            return instance.visitLink(node, p);
        }

        @Override
        public DocTree visitLiteral(LiteralTree node, Element p) {
            return instance.visitLiteral(node, p);
        }

        @Override
        public DocTree visitParam(ParamTree node, Element p) {
            return instance.visitParam(node, p);
        }

        @Override
        public DocTree visitProvides(ProvidesTree node, Element p) {
            return instance.visitProvides(node, p);
        }

        @Override
        public DocTree visitReference(ReferenceTree node, Element p) {
            return instance.visitReference(node, p);
        }

        @Override
        public DocTree visitReturn(ReturnTree node, Element p) {
            return instance.visitReturn(node, p);
        }

        @Override
        public DocTree visitSee(SeeTree node, Element p) {
            return instance.visitSee(node, p);
        }

        @Override
        public DocTree visitSerial(SerialTree node, Element p) {
            return instance.visitSerial(node, p);
        }

        @Override
        public DocTree visitSerialData(SerialDataTree node, Element p) {
            return instance.visitSerialData(node, p);
        }

        @Override
        public DocTree visitSerialField(SerialFieldTree node, Element p) {
            return instance.visitSerialField(node, p);
        }

        @Override
        public DocTree visitSince(SinceTree node, Element p) {
            return instance.visitSince(node, p);
        }

        @Override
        public DocTree visitStartElement(StartElementTree node, Element p) {
            return instance.visitStartElement(node, p);
        }

        @Override
        public DocTree visitText(TextTree node, Element p) {
            return instance.visitText(node, p);
        }

        @Override
        public DocTree visitThrows(ThrowsTree node, Element p) {
            return instance.visitThrows(node, p);
        }

        @Override
        public DocTree visitUnknownBlockTag(UnknownBlockTagTree node, Element p) {
            return instance.visitUnknownBlockTag(node, p);
        }

        @Override
        public DocTree visitUnknownInlineTag(UnknownInlineTagTree node, Element p) {
            return instance.visitUnknownInlineTag(node, p);
        }

        @Override
        public DocTree visitUses(UsesTree node, Element p) {
            return instance.visitUses(node, p);
        }

        @Override
        public DocTree visitValue(ValueTree node, Element p) {
            return instance.visitValue(node, p);
        }

        @Override
        public DocTree visitVersion(VersionTree node, Element p) {
            return instance.visitVersion(node, p);
        }

        @Override
        public DocTree visitOther(DocTree node, Element p) {
            return instance.visitOther(node, p);
        }

        public DocTree visitAttribute(AttributeTree node, Element p, Void ignore) {
            return super.visitAttribute(node, p);
        }

        public DocTree visitAuthor(AuthorTree node, Element p, Void ignore) {
            return super.visitAuthor(node, p);
        }

        public DocTree visitComment(CommentTree node, Element p, Void ignore) {
            return super.visitComment(node, p);
        }

        public DocTree visitDeprecated(DeprecatedTree node, Element p, Void ignore) {
            return super.visitDeprecated(node, p);
        }

        public DocTree visitDocComment(DocCommentTree node, Element p, Void ignore) {
            return super.visitDocComment(node, p);
        }

        public DocTree visitDocRoot(DocRootTree node, Element p, Void ignore) {
            return super.visitDocRoot(node, p);
        }

        public DocTree visitEndElement(EndElementTree node, Element p, Void ignore) {
            return super.visitEndElement(node, p);
        }

        public DocTree visitEntity(EntityTree node, Element p, Void ignore) {
            return super.visitEntity(node, p);
        }

        public DocTree visitErroneous(ErroneousTree node, Element p, Void ignore) {
            return super.visitErroneous(node, p);
        }

        public DocTree visitHidden(HiddenTree node, Element p, Void ignore) {
            return super.visitHidden(node, p);
        }

        public DocTree visitIdentifier(IdentifierTree node, Element p, Void ignore) {
            return super.visitIdentifier(node, p);
        }

        public DocTree visitIndex(IndexTree node, Element p, Void ignore) {
            return super.visitIndex(node, p);
        }

        public DocTree visitInheritDoc(InheritDocTree node, Element p, Void ignore) {
            return super.visitInheritDoc(node, p);
        }

        public DocTree visitLink(LinkTree node, Element p, Void ignore) {
            return super.visitLink(node, p);
        }

        public DocTree visitLiteral(LiteralTree node, Element p, Void ignore) {
            return super.visitLiteral(node, p);
        }

        public DocTree visitParam(ParamTree node, Element p, Void ignore) {
            return super.visitParam(node, p);
        }

        public DocTree visitProvides(ProvidesTree node, Element p, Void ignore) {
            return super.visitProvides(node, p);
        }

        public DocTree visitReference(ReferenceTree node, Element p, Void ignore) {
            return super.visitReference(node, p);
        }

        public DocTree visitReturn(ReturnTree node, Element p, Void ignore) {
            return super.visitReturn(node, p);
        }

        public DocTree visitSee(SeeTree node, Element p, Void ignore) {
            return super.visitSee(node, p);
        }

        public DocTree visitSerial(SerialTree node, Element p, Void ignore) {
            return super.visitSerial(node, p);
        }

        public DocTree visitSerialData(SerialDataTree node, Element p, Void ignore) {
            return super.visitSerialData(node, p);
        }

        public DocTree visitSerialField(SerialFieldTree node, Element p, Void ignore) {
            return super.visitSerialField(node, p);
        }

        public DocTree visitSince(SinceTree node, Element p, Void ignore) {
            return super.visitSince(node, p);
        }

        public DocTree visitStartElement(StartElementTree node, Element p, Void ignore) {
            return super.visitStartElement(node, p);
        }

        public DocTree visitText(TextTree node, Element p, Void ignore) {
            return super.visitText(node, p);
        }

        public DocTree visitThrows(ThrowsTree node, Element p, Void ignore) {
            return super.visitThrows(node, p);
        }

        public DocTree visitUnknownBlockTag(UnknownBlockTagTree node, Element p, Void ignore) {
            return super.visitUnknownBlockTag(node, p);
        }

        public DocTree visitUnknownInlineTag(UnknownInlineTagTree node, Element p, Void ignore) {
            return super.visitUnknownInlineTag(node, p);
        }

        public DocTree visitUses(UsesTree node, Element p, Void ignore) {
            return super.visitUses(node, p);
        }

        public DocTree visitValue(ValueTree node, Element p, Void ignore) {
            return super.visitValue(node, p);
        }

        public DocTree visitVersion(VersionTree node, Element p, Void ignore) {
            return super.visitVersion(node, p);
        }

        public DocTree visitOther(DocTree node, Element p, Void ignore) {
            return super.visitOther(node, p);
        }
    }
}
