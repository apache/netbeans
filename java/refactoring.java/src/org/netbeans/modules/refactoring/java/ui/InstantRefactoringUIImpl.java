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

package org.netbeans.modules.refactoring.java.ui;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.DocSourcePositions;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import static org.netbeans.api.java.lexer.JavaTokenId.BLOCK_COMMENT;
import static org.netbeans.api.java.lexer.JavaTokenId.JAVADOC_COMMENT;
import static org.netbeans.api.java.lexer.JavaTokenId.LINE_COMMENT;
import static org.netbeans.api.java.lexer.JavaTokenId.WHITESPACE;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.DocTreePathHandle;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.MarkBlock;
import org.netbeans.lib.editor.util.swing.MutablePositionRegion;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.modules.refactoring.java.RefactoringModule;
import org.netbeans.modules.refactoring.java.plugins.FindLocalUsagesQuery;
import static org.netbeans.modules.refactoring.java.plugins.FindLocalUsagesQuery.createRegion;
import org.netbeans.modules.refactoring.java.ui.instant.InstantOption;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.filesystems.FileObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Ralph Benjamin Ruijs
 */
public final class InstantRefactoringUIImpl implements InstantRefactoringUI {
    
    private String newName;
//    private final Set<MutablePositionRegion> overloads;
    private final String oldName;
    private final RenameRefactoring refactoring;
    private final TreePathHandle tph;
    private final DocTreePathHandle dtph;
    private final ArrayList<InstantOption> options;
    private InstantOption searchComments/*, renameProp, test, testMethod*/;
    private final Set<MutablePositionRegion> usages;
    private final Set<MutablePositionRegion> comments;

    private InstantRefactoringUIImpl(Set<MutablePositionRegion> usages, Set<MutablePositionRegion> comments, String oldName, FileObject file, TreePathHandle tph) throws BadLocationException {
        this(usages, comments, oldName, file != null? new RenameRefactoring(Lookups.fixed(tph, file)) :
                new RenameRefactoring(Lookups.singleton(tph)), tph, null);
    }
    
    private InstantRefactoringUIImpl(Set<MutablePositionRegion> usages, Set<MutablePositionRegion> comments, String oldName, DocTreePathHandle dtph) throws BadLocationException {
        this(usages, comments, oldName, new RenameRefactoring(Lookups.singleton(dtph)), null, dtph);
    }

    private InstantRefactoringUIImpl(Set<MutablePositionRegion> usages, Set<MutablePositionRegion> comments, /*Set<MutablePositionRegion> overloads, */String oldName, RenameRefactoring refactoring, TreePathHandle tph, DocTreePathHandle dtph) throws BadLocationException {
        this.refactoring = refactoring;
        this.tph = tph;
        this.dtph = dtph;
        this.usages = usages;
        this.comments = comments;
//        this.overloads = overloads;
        this.oldName = oldName;
        options = new ArrayList<>(3);
        options.add(searchComments = new InstantOption(NbBundle.getMessage(RenamePanel.class, "LBL_RenameComments").replace("&", ""),
                null,
                RefactoringModule.getOption("searchInComments.rename", true)));
        updateInput(oldName);
    }

    @Override
    public KeyStroke getKeyStroke() {
        Object value = RefactoringActionsFactory.renameAction().getValue(ContextAwareAction.ACCELERATOR_KEY);
        return value instanceof KeyStroke ? (KeyStroke) value : null;
    }

    @Override
    public void updateInput(CharSequence text) {
        newName = text.toString();
        refactoring.setNewName(newName);
        refactoring.setSearchInComments(searchComments.selected());
        JavaRenameProperties properties = refactoring.getContext().lookup(JavaRenameProperties.class);
        if (properties==null) {
            properties = new JavaRenameProperties();
            refactoring.getContext().add(properties);
        }
        properties.setIsRenameGettersSetters(false);
        properties.setIsRenameTestClass(false);
        properties.setIsRenameTestClassMethod(false);
    }

    @Override
    public List<InstantOption> getOptions() {
        return options;
    }

    public Set<MutablePositionRegion> getRegions() {
        HashSet<MutablePositionRegion> regions = new HashSet<>(usages);
        if(searchComments.selected()) {
            regions.addAll(comments);
        }
        return regions;
    }

    private static Set<JavaTokenId> IGNORE_TOKES = EnumSet.of(
            JavaTokenId.WHITESPACE, JavaTokenId.BLOCK_COMMENT, JavaTokenId.LINE_COMMENT);
    
    public static InstantRefactoringUI create(final JavaSource js, final int caret) {
        InstantRefactoringUI ui;
        ui = ComputeOffAWT.computeOffAWT(new ComputeOffAWT.Worker<InstantRefactoringUI>() {
            @Override
            public InstantRefactoringUI process(final CompilationInfo info) {
                try {
                    //<editor-fold defaultstate="collapsed" desc="PreCheck">
                    final Document doc = info.getDocument();
                    final DocSourcePositions docSourcePositions = (DocSourcePositions) info.getTrees().getSourcePositions();
                    
                    if (doc == null) {
                        return null;
                    }
                    
                    //</editor-fold>
                    
                    TreePath path[] = {null};
                    final DocTreePath[] docPath = {null};
                    
                    
                    final int[] adjustedCaret = new int[]{caret};

                    //<editor-fold defaultstate="collapsed" desc="InitPath">
                    doc.render(new Runnable() {
                        @Override
                        public void run() {
                            TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(info.getTokenHierarchy(), caret);
                            ts.move(caret);

                            if (ts.moveNext() && ts.token() != null) {
                                if (ts.token().id() == JavaTokenId.IDENTIFIER) {
                                    adjustedCaret[0] = ts.offset() + ts.token().length() / 2 + 1;
                                } else if (ts.token().id() == JavaTokenId.JAVADOC_COMMENT) {
                                    int offsetBehindJavadoc = ts.offset() + ts.token().length();
                                    while (ts.moveNext()) {
                                        TokenId tid = ts.token().id();
                                        if (tid == JavaTokenId.BLOCK_COMMENT) {
                                            if ("/**/".contentEquals(ts.token().text())) { // NOI18N
                                                // see #147533
                                                return;
                                            }
                                        } else if (tid == JavaTokenId.JAVADOC_COMMENT) {
                                            if (ts.token().partType() == PartType.COMPLETE) {
                                                return;
                                            }
                                        } else if (!IGNORE_TOKES.contains(tid)) {
                                            offsetBehindJavadoc = ts.offset();
                                            // it is magic for TreeUtilities.pathFor
                                            ++offsetBehindJavadoc;
                                            break;
                                        }
                                    }
                                    TreePath path = info.getTreeUtilities().pathFor(offsetBehindJavadoc);
                                    while (!TreeUtilities.CLASS_TREE_KINDS.contains(path.getLeaf().getKind()) && path.getLeaf().getKind() != Tree.Kind.METHOD && path.getLeaf().getKind() != Tree.Kind.VARIABLE && path.getLeaf().getKind() != Tree.Kind.COMPILATION_UNIT) {
                                        path = path.getParentPath();
                                        if (path == null) {
                                            break;
                                        }
                                    }
                                    if (path != null) {
                                        DocCommentTree docComment = ((DocTrees) info.getTrees()).getDocCommentTree(path);
                                        DocTreePath docTreePath = info.getTreeUtilities().pathFor(new DocTreePath(path, docComment), caret);
                                        long start = docSourcePositions.getStartPosition(info.getCompilationUnit(), docComment, docTreePath.getLeaf());
                                        long end = docSourcePositions.getEndPosition(info.getCompilationUnit(), docComment, docTreePath.getLeaf());
                                        adjustedCaret[0] = (int) (start + ((end - start) / 2) + 1);
                                        docPath[0] = docTreePath;
                                    }
                                }
                            }
                        }
                    });

                    path[0] = docPath[0] != null ? docPath[0].getTreePath() : info.getTreeUtilities().pathFor(adjustedCaret[0]);
                    //</editor-fold>

                    //<editor-fold defaultstate="collapsed" desc="CorrectPathForArray">
                    //correction for int something[]:
                    if (path[0] != null && path[0].getParentPath() != null) {
                        Tree.Kind leafKind = path[0].getLeaf().getKind();
                        Tree.Kind parentKind = path[0].getParentPath().getLeaf().getKind();

                        if (leafKind == Tree.Kind.ARRAY_TYPE && parentKind == Tree.Kind.VARIABLE) {
                            long typeEnd = docSourcePositions.getEndPosition(info.getCompilationUnit(), path[0].getLeaf());
                            long variableEnd = docSourcePositions.getEndPosition(info.getCompilationUnit(), path[0].getLeaf());

                            if (typeEnd == variableEnd) {
                                path[0] = path[0].getParentPath();
                            }
                        }
                    }
                    //</editor-fold>

                    //<editor-fold defaultstate="collapsed" desc="computeLabel">
                    Element el = docPath[0] != null
                            ? ((DocTrees) info.getTrees()).getElement(docPath[0])
                            : info.getTrees().getElement(path[0]);

                    if (el == null || path[0] == null) {
                        if (path[0] != null) {
                            Set<MutablePositionRegion> labelPoints = computeLabelChangePoints(path, info, adjustedCaret, doc);
                            if (labelPoints != null) {
                                return new InstantRefactoringUIImpl(labelPoints, Collections.EMPTY_SET, labelPoints.iterator().next().getText(doc).toString(), (FileObject) null, TreePathHandle.create(path[0], info));
                            }
                        }
                        return null;
                    }
                    //</editor-fold>

                    long start = docPath[0] != null
                            ? docSourcePositions.getStartPosition(info.getCompilationUnit(), docPath[0].getDocComment(), docPath[0].getLeaf())
                            : docSourcePositions.getStartPosition(info.getCompilationUnit(), path[0].getLeaf());

                    long end = docPath[0] != null
                            ? docSourcePositions.getEndPosition(info.getCompilationUnit(), docPath[0].getDocComment(), docPath[0].getLeaf())
                            : docSourcePositions.getEndPosition(info.getCompilationUnit(), path[0].getLeaf());

                    if (!(start <= caret && caret <= end)) {
                        return null;
                    }
                    
                    
                    TreePathHandle tph = null;
                    FileObject file = null;
                    if(docPath[0] == null) {
                        tph = TreePathHandle.create(path[0], info);
                        Element selected = info.getTrees().getElement(path[0]);
                        if(selected instanceof TypeElement && !((TypeElement) selected).getNestingKind().isNested()) {
                            ElementHandle<TypeElement> handle = ElementHandle.create((TypeElement) selected);
                            file = SourceUtils.getFile(handle, info.getClasspathInfo());
                        }
                    }

                    if (el.getKind() == ElementKind.CONSTRUCTOR) {
                        //for constructor, work over the enclosing class:
                        el = el.getEnclosingElement();
                    }
                    final FindLocalUsagesQuery findLocalUsagesQuery = new FindLocalUsagesQuery();

                    findLocalUsagesQuery.findUsages(el, info, doc, true);

                    Set<MutablePositionRegion> usages = new HashSet<>(findLocalUsagesQuery.getUsages());
                    Set<MutablePositionRegion> comments = new HashSet<>(findLocalUsagesQuery.getComments());
                    
                    if (el.getKind().isClass()) {
                        //rename also the constructors:
                        for (ExecutableElement c : ElementFilter.constructorsIn(el.getEnclosedElements())) {
                            TreePath t = info.getTrees().getPath(c);

                            if (t != null) {
                                int[] span = info.getTreeUtilities().findNameSpan((MethodTree) t.getLeaf());
                                if (span != null) {
                                    try {
                                        usages.add(createRegion(doc, span[0], span[1]));
                                    } catch (BadLocationException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }
                            }
                        }
                    }
                    // Filter out guardedblocks
                    usages = removeOverlapsWithGuardedBlocks(doc, usages);
                    comments = removeOverlapsWithGuardedBlocks(doc, comments);
                    
                    InstantRefactoringUI ui;
                    if(docPath[0] == null) {
                        ui = new InstantRefactoringUIImpl(usages, comments, el.getSimpleName().toString(), file, tph);
                    } else {
                        DocTreePathHandle dtph;
                        dtph = DocTreePathHandle.create(docPath[0], info);
                        ui = new InstantRefactoringUIImpl(usages, comments, el.getSimpleName().toString(), dtph);
                    }
                    return ui;
                    
                } catch (IOException | BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                    return null;
                }
            }
        }, "Instant Rename", js, JavaSource.Phase.RESOLVED);
        return ui;
    }
    
    @Override
    public Set<MutablePositionRegion> optionChanged(InstantOption instantOption) {
        if(instantOption == searchComments) {
            RefactoringModule.setOption("searchInComments.rename", searchComments.selected());
            HashSet<MutablePositionRegion> regions = new HashSet<>(usages);
            if(searchComments.selected()) {
                regions.addAll(comments);
            }
            return regions;
        } else {
            return null;
        }
    }

    @Override
    public RefactoringUI getRefactoringUI() {
        final RenameRefactoringUI renameRefactoringUI = new RenameRefactoringUI(this.refactoring, this.oldName, this.newName, this.tph, this.dtph);
        return renameRefactoringUI;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(RenamePanel.class, "LBL_Rename");
    }

    @Override
    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }
    
    private static Set<MutablePositionRegion> removeOverlapsWithGuardedBlocks(Document doc, Set<MutablePositionRegion> points) {
        if (!(doc instanceof GuardedDocument))
            return points;
        
        GuardedDocument gd = (GuardedDocument) doc;
        MarkBlock current = gd.getGuardedBlockChain().getChain();
        
        while (current != null && !points.isEmpty()) {
            Iterator<MutablePositionRegion> iterator = points.iterator();
            while(iterator.hasNext()) {
                MutablePositionRegion region = iterator.next();
                if ((current.compare(region.getStartOffset(), region.getEndOffset()) & MarkBlock.OVERLAP) != 0) {
                    iterator.remove();
                }
            }
            
            current = current.getNext();
        }
        
        return points;
    }

    private static Set<MutablePositionRegion> computeLabelChangePoints(TreePath[] path, final CompilationInfo info, final int[] adjustedCaret, final Document doc) throws IllegalArgumentException {
        final Tree tree = path[0].getLeaf();
        int[] nameSpan = null;
        switch(tree.getKind()) {
            case LABELED_STATEMENT:
                nameSpan = info.getTreeUtilities().findNameSpan((LabeledStatementTree)tree);
                break;
            case BREAK:
                nameSpan = info.getTreeUtilities().findNameSpan((BreakTree) tree);
                break;
            case CONTINUE:
                nameSpan = info.getTreeUtilities().findNameSpan((ContinueTree) tree);
                break;
        }
        if (nameSpan != null && nameSpan[0] <= adjustedCaret[0] && adjustedCaret[0] <= nameSpan[1]) {
            if (path[0].getLeaf().getKind() != Tree.Kind.LABELED_STATEMENT) {
                Tree tgt = info.getTreeUtilities().getBreakContinueTargetTree(path[0]);
                path[0] = tgt != null ? info.getTrees().getPath(info.getCompilationUnit(), tgt) : null;
            }
            if (path[0] != null) {
                TreePath labeledStatement = path[0];
                final Set<MutablePositionRegion> result = new LinkedHashSet<>();
                if (labeledStatement.getLeaf().getKind() == Tree.Kind.LABELED_STATEMENT) {
                    int[] span = info.getTreeUtilities().findNameSpan((LabeledStatementTree)path[0].getLeaf());
                    if(span != null) {
                        try {
                            result.add(createRegion(doc, span[0], span[1]));
                        } catch(BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    final Name label = ((LabeledStatementTree)labeledStatement.getLeaf()).getLabel();
                    new ErrorAwareTreePathScanner <Void, Void>() {
                        @Override
                        public Void visitBreak(BreakTree node, Void p) {
                            if (node.getLabel() != null && label.contentEquals(node.getLabel())) {
                                int[] span = info.getTreeUtilities().findNameSpan((BreakTree) node);
                                if(span != null) {
                                    try {
                                        result.add(createRegion(doc, span[0], span[1]));
                                    } catch (BadLocationException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }
                            }
                            return super.visitBreak(node, p);
                        }
                        @Override
                        public Void visitContinue(ContinueTree node, Void p) {
                            if (node.getLabel() != null && label.contentEquals(node.getLabel())) {
                                int[] span = info.getTreeUtilities().findNameSpan((ContinueTree) node);
                                if(span != null) {
                                    try {
                                        result.add(createRegion(doc, span[0], span[1]));
                                    } catch (BadLocationException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }
                            }
                            return super.visitContinue(node, p);
                        }
                    }.scan(labeledStatement, null);
                }
                return result;
            }
        }
        return null;
    }
}
