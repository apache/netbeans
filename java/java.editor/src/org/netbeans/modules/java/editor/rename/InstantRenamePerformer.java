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
package org.netbeans.modules.java.editor.rename;

import com.sun.source.tree.BreakTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.ElementScanner6;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.StyleConstants;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.MarkBlock;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.MutablePositionRegion;
import org.netbeans.modules.editor.java.ComputeOffAWT;
import org.netbeans.modules.editor.java.ComputeOffAWT.Worker;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.java.editor.base.javadoc.JavadocImports;
import org.netbeans.modules.java.editor.base.semantic.FindLocalUsagesQuery;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.ProgressListener;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor;
import org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor.Context;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakSet;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Lahoda
 */
public class InstantRenamePerformer implements DocumentListener, KeyListener {
    
    private static final Logger LOG = Logger.getLogger(InstantRenamePerformer.class.getName());
    private static final Set<InstantRenamePerformer> registry = Collections.synchronizedSet(new WeakSet<InstantRenamePerformer>());

    private SyncDocumentRegion region;
    private int span;
    private Document doc;
    private JTextComponent target;
    
    private AttributeSet attribs = null;
    private AttributeSet attribsLeft = null;
    private AttributeSet attribsRight = null;
    private AttributeSet attribsMiddle = null;
    private AttributeSet attribsAll = null;

    private AttributeSet attribsSlave = null;
    private AttributeSet attribsSlaveLeft = null;
    private AttributeSet attribsSlaveRight = null;
    private AttributeSet attribsSlaveMiddle = null;
    private AttributeSet attribsSlaveAll = null;
    
    /** Creates a new instance of InstantRenamePerformer */
    private InstantRenamePerformer(JTextComponent target, Set<Token> highlights, int caretOffset) throws BadLocationException {
        this.target = target;
        doc = target.getDocument();

        MutablePositionRegion mainRegion = null;
        List<MutablePositionRegion> regions = new ArrayList<MutablePositionRegion>();

        for (Token h : highlights) {
            // type parameter name is represented as ident -> ignore surrounding <> in rename
            int delta = h.id() == JavadocTokenId.IDENT && h.text().charAt(0) == '<' && h.text().charAt(h.length() - 1) == '>' ? 1 : 0;
            Position start = NbDocument.createPosition(doc, h.offset(null) + delta, Bias.Backward);
            Position end = NbDocument.createPosition(doc, h.offset(null) + h.length() - delta, Bias.Forward);
            MutablePositionRegion current = new MutablePositionRegion(start, end);
            
            if (isIn(current, caretOffset)) {
                mainRegion = current;
            } else {
                regions.add(current);
            }
        }

        if (mainRegion == null) {
            throw new IllegalArgumentException("No highlight contains the caret.");
        }

        regions.add(0, mainRegion);

        region = new SyncDocumentRegion(doc, regions);

        if (doc instanceof BaseDocument) {
            BaseDocument bdoc = ((BaseDocument) doc);
            bdoc.setPostModificationDocumentListener(this);

            UndoableEdit undo = new CancelInstantRenameUndoableEdit(this);
            for (UndoableEditListener l : bdoc.getUndoableEditListeners()) {
                l.undoableEditHappened(new UndoableEditEvent(doc, undo));
            }
        }

        target.addKeyListener(this);

        target.putClientProperty(InstantRenamePerformer.class, this);
        target.putClientProperty("NetBeansEditor.navigateBoundaries", mainRegion); // NOI18N
	
        requestRepaint();
        
        target.select(mainRegion.getStartOffset(), mainRegion.getEndOffset());
        
        span = region.getFirstRegionLength();
        
        registry.add(this);
        sendUndoableEdit(doc, CloneableEditorSupport.BEGIN_COMMIT_GROUP);
    }
    
    public static void invokeInstantRename(JTextComponent target) {
        try {
            final int caret = target.getCaretPosition();
            String ident = Utilities.getIdentifier(Utilities.getDocument(target), caret);
            
            if (ident == null) {
                Utilities.setStatusBoldText(target, NbBundle.getMessage(InstantRenamePerformer.class, "WARN_CannotPerformHere"));
                return;
            }
            
            DataObject od = (DataObject) target.getDocument().getProperty(Document.StreamDescriptionProperty);
            JavaSource js = od != null ? JavaSource.forFileObject(od.getPrimaryFile()) : null;

            if (js == null) {
                Utilities.setStatusBoldText(target, NbBundle.getMessage(InstantRenamePerformer.class, "WARN_CannotPerformHere"));
                return ;
            }
            
            final boolean[] wasResolved = new boolean[1];

            Set<Token> changePoints = ComputeOffAWT.computeOffAWT(new Worker<Set<Token>>() {
                @Override
                public Set<Token> process(CompilationInfo info) {
                    try {
                        return computeChangePoints(info, caret, wasResolved);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                        return null;
                    }
                }
            }, "Instant Rename", js, Phase.RESOLVED);
            
            if (wasResolved[0]) {
                if (changePoints != null) {
                    doInstantRename(changePoints, target, caret, ident);
                } else {
                    doFullRename(od.getCookie(EditorCookie.class), od.getNodeDelegate());
                }
            } else {
                Utilities.setStatusBoldText(target, NbBundle.getMessage(InstantRenamePerformer.class, "WARN_CannotPerformHere"));
            }
        } catch (BadLocationException e) {
            Exceptions.printStackTrace(e);
        }
    }
    private static void doFullRename(EditorCookie ec, Node n) {
        
        InstanceContent ic = new InstanceContent();
        ic.add(ec);
        ic.add(n);
        Lookup actionContext = new AbstractLookup(ic);
        
        Action a = RefactoringActionsFactory.renameAction().createContextAwareInstance(actionContext);
        a.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
    }
    
    private static void doInstantRename(Set<Token> changePoints, JTextComponent target, int caret, String ident) throws BadLocationException {
        InstantRenamePerformer.performInstantRename(target, changePoints, caret);
    }
    
    static Set<Token> computeChangePoints(final CompilationInfo info, final int caret, final boolean[] wasResolved) throws IOException {
        final Document doc = info.getDocument();
        
        if (doc == null)
            return null;
        
        final int[] adjustedCaret = new int[] {caret};
        final boolean[] insideJavadoc = {false};
        
        doc.render(new Runnable() {
            @Override
            public void run() {
                TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(info.getTokenHierarchy(), caret);
                
                ts.move(caret);
                
                if (ts.moveNext() && ts.token()!=null) {
                    if (ts.token().id() == JavaTokenId.IDENTIFIER) {
                        adjustedCaret[0] = ts.offset() + ts.token().length() / 2 + 1;
                    } else if (ts.token().id() == JavaTokenId.JAVADOC_COMMENT) {
                        TokenSequence<JavadocTokenId> jdts = ts.embedded(JavadocTokenId.language());
                        if (jdts != null && JavadocImports.isInsideReference(jdts, caret)) {
                            jdts.move(caret);
                            if (jdts.moveNext() && jdts.token().id() == JavadocTokenId.IDENT) {
                                adjustedCaret[0] = jdts.offset();
                                insideJavadoc[0] = true;
                            }
                        } else if (jdts != null && JavadocImports.isInsideParamName(jdts, caret)) {
                            jdts.move(caret);
                            if (jdts.moveNext()) {
                                adjustedCaret[0] = jdts.offset();
                                insideJavadoc[0] = true;
                            }
                        }
                    }
                }
            }
        });
        
        TreePath path = insideJavadoc[0]? null: info.getTreeUtilities().pathFor(adjustedCaret[0]);
        
        //correction for int something[]:
        if (path != null && path.getParentPath() != null) {
            Kind leafKind = path.getLeaf().getKind();
            Kind parentKind = path.getParentPath().getLeaf().getKind();
            
            if (leafKind == Kind.ARRAY_TYPE && parentKind == Kind.VARIABLE) {
                long typeEnd = info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), path.getLeaf());
                long variableEnd = info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), path.getLeaf());
                
                if (typeEnd == variableEnd) {
                    path = path.getParentPath();
                }
            }
        }
        
        Element el = insideJavadoc[0]
                ? JavadocImports.findReferencedElement(info, adjustedCaret[0])
                : info.getTrees().getElement(path);
        
        if (el == null && path != null) {
            if (path != null && EnumSet.of(Kind.LABELED_STATEMENT, Kind.BREAK, Kind.CONTINUE).contains(path.getLeaf().getKind())) {
                Token<JavaTokenId> span = org.netbeans.modules.java.editor.base.semantic.Utilities.findIdentifierSpan(info, doc, path);
                if (span != null && span.offset(null) <= adjustedCaret[0] && adjustedCaret[0] <= span.offset(null) + span.length()) {
                    if (path.getLeaf().getKind() != Kind.LABELED_STATEMENT) {
                        Tree tgt = info.getTreeUtilities().getBreakContinueTargetTree(path);
                        path = tgt != null ? info.getTrees().getPath(info.getCompilationUnit(), tgt) : null;
                    }
                    if (path != null) {
                        wasResolved[0] = true;
                        return collectLabels(info, doc, path);
                    }
                }
            }            
            wasResolved[0] = false;
            return null;
        }
        
        //#89736: if the caret is not in the resolved element's name, no rename:
        final Token name = insideJavadoc[0]
                ? JavadocImports.findNameTokenOfReferencedElement(info, adjustedCaret[0])
                : org.netbeans.modules.java.editor.base.semantic.Utilities.getToken(info, doc, path);
        
        if (name == null)
            return null;
        
        doc.render(new Runnable() {
            @Override
            public void run() {
                wasResolved[0] = name.offset(null) <= caret && caret <= (name.offset(null) + name.length());
            }
        });
        
        if (!wasResolved[0])
            return null;
        
        if (insideJavadoc[0] && el == null) {
            return Collections.singleton(name);
        }
        
        if (el.getKind() == ElementKind.CONSTRUCTOR) {
            //for constructor, work over the enclosing class:
            el = el.getEnclosingElement();
        }
        
        if (allowInstantRename(info, el, info.getElementUtilities())) {
            final Set<Token> points = new HashSet<Token>(new FindLocalUsagesQuery(true).findUsages(el, info, doc));
            
            if (el.getKind().isClass()) {
                //rename also the constructors:
                for (ExecutableElement c : ElementFilter.constructorsIn(el.getEnclosedElements())) {
                    TreePath t = info.getTrees().getPath(c);
                    
                    if (t != null) {
                        Token token = org.netbeans.modules.java.editor.base.semantic.Utilities.getToken(info, doc, t);
                        
                        if (token != null) {
                            points.add(token);
                        }
                    }
                }
            }
            
            final boolean[] overlapsWithGuardedBlocks = new boolean[1];
            
            doc.render(new Runnable() {
                @Override
                public void run() {
                    overlapsWithGuardedBlocks[0] = overlapsWithGuardedBlocks(doc, points);
                }
            });
            
            if (overlapsWithGuardedBlocks[0]) {
                return null;
            }
            
            return points;
        } else if (insideJavadoc[0]) {
            // java refactoring does not support javadoc
            wasResolved[0] = false;
        }
        
        return null;
    }

    private static Set<Token> collectLabels(final CompilationInfo info, final Document document, final TreePath labeledStatement) {
        final Set<Token> result = new LinkedHashSet<Token>();
        if (labeledStatement.getLeaf().getKind() == Kind.LABELED_STATEMENT) {
            result.add(org.netbeans.modules.java.editor.base.semantic.Utilities.findIdentifierSpan(info, document, labeledStatement));
            final Name label = ((LabeledStatementTree)labeledStatement.getLeaf()).getLabel();
            new ErrorAwareTreePathScanner <Void, Void>() {
                @Override
                public Void visitBreak(BreakTree node, Void p) {
                    if (node.getLabel() != null && label.contentEquals(node.getLabel())) {
                        result.add(org.netbeans.modules.java.editor.base.semantic.Utilities.findIdentifierSpan(info, document, getCurrentPath()));
                    }
                    return super.visitBreak(node, p);
                }
                @Override
                public Void visitContinue(ContinueTree node, Void p) {
                    if (node.getLabel() != null && label.contentEquals(node.getLabel())) {
                        result.add(org.netbeans.modules.java.editor.base.semantic.Utilities.findIdentifierSpan(info, document, getCurrentPath()));
                    }
                    return super.visitContinue(node, p);
                }
            }.scan(labeledStatement, null);
        }
        return result;
    }

    private static boolean allowInstantRename(CompilationInfo info, Element e, ElementUtilities eu) {
        if(e.getKind() == ElementKind.FIELD) {
            VariableElement variableElement = (VariableElement) e;
            TypeElement typeElement = eu.enclosingTypeElement(e);
            
            boolean isProperty = false;
            try {
                CodeStyle codeStyle = CodeStyle.getDefault(info.getDocument());
                isProperty = eu.hasGetter(typeElement, variableElement, codeStyle);
                isProperty = isProperty || (!variableElement.getModifiers().contains(Modifier.FINAL) &&
                                    eu.hasSetter(typeElement, variableElement, codeStyle));
            } catch (IOException ex) {
            }
            if(isProperty) {
                return false;
            }
        }
        if (info.getElementUtilities().getLinkedRecordElements(e).size() > 1) {
            return false;
        }
        if (org.netbeans.modules.java.editor.base.semantic.Utilities.isPrivateElement(e)) {
            return true;
        }
        
        if (isInaccessibleOutsideOuterClass(e, eu)) {
            return true;
        }
        
        //#92160: check for local classes:
        if (e.getKind() == ElementKind.CLASS) {//only classes can be local
            Element enclosing = e.getEnclosingElement();
            final ElementKind enclosingKind = enclosing.getKind();

            //#150352: parent is annonymous class
            if (enclosingKind == ElementKind.CLASS) {
                final Set<ElementKind> fm = EnumSet.of(ElementKind.METHOD, ElementKind.FIELD);
                if (enclosing.getSimpleName().length() == 0 || fm.contains(enclosing.getEnclosingElement().getKind())) {
                    return true;
                }
            }


            return LOCAL_CLASS_PARENTS.contains(enclosingKind);
        }

        if (e.getKind() == ElementKind.TYPE_PARAMETER) {
            return true;
        }
        
        return false;
    }

    /**
     * computes accessibility of members of nested classes
     * @param e member
     * @return {@code true} if the member cannot be accessed outside the outer class
     * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=169377">169377</a>
     */
    private static boolean isInaccessibleOutsideOuterClass(Element e, ElementUtilities eu) {
        Element enclosing = e.getEnclosingElement();
        boolean isStatic = e.getModifiers().contains(Modifier.STATIC);
        ElementKind kind = e.getKind();
        if (isStatic || kind.isClass() || kind.isInterface() || kind.isField()) {
            // static declaration of nested class, interface, enum, ann type, method, field
            // or inner class
            return isAnyEncloserPrivate(e);
        } else if (enclosing != null && kind == ElementKind.METHOD) {
            // final is enum, ann type and some classes
            ElementKind enclosingKind = enclosing.getKind();
            boolean isEnclosingFinal = enclosing.getModifiers().contains(Modifier.FINAL)
                    // ann type is not final even if it cannot be subclassed
                    || enclosingKind == ElementKind.ANNOTATION_TYPE;
            return isAnyEncloserPrivate(e) && !eu.overridesMethod((ExecutableElement) e) && !eu.implementsMethod((ExecutableElement)e) &&
                    (isEnclosingFinal || !isOverriddenInsideOutermostEnclosingClass((ExecutableElement)e, eu));
        }
        return false;
    }

    private static boolean isAnyEncloserPrivate(Element e) {
        Element enclosing = e.getEnclosingElement();
        while (enclosing != null && (enclosing.getKind().isClass() || enclosing.getKind().isInterface())) {
            boolean isPrivateClass = enclosing.getModifiers().contains(Modifier.PRIVATE);
            if (isPrivateClass) {
                return true;
            }
            enclosing = enclosing.getEnclosingElement();
        }
        return false;
    }
    
    private static boolean isOverriddenInsideOutermostEnclosingClass(final ExecutableElement ee, final ElementUtilities eu) {
        final boolean[] ret = new boolean[] {false};
        new ElementScanner6<Void, Void>() {
            @Override
            public Void visitType(TypeElement te, Void p) {
                if (ret[0])
                    return null;
                if (te != ee.getEnclosingElement() && eu.getImplementationOf(ee, te) != null && !isAnyEncloserPrivate(te))
                    ret[0] = true;
                return super.visitType(te, p);
            }            
        }.scan(eu.outermostTypeElement(ee));
        return ret[0];
    }
    
    private static boolean overlapsWithGuardedBlocks(Document doc, Set<Token> highlights) {
        if (!(doc instanceof GuardedDocument))
            return false;
        
        GuardedDocument gd = (GuardedDocument) doc;
        MarkBlock current = gd.getGuardedBlockChain().getChain();
        
        while (current != null) {
            for (Token h : highlights) {
                if ((current.compare(h.offset(null), h.offset(null) + h.length()) & MarkBlock.OVERLAP) != 0) {
                    return true;
                }
            }
            
            current = current.getNext();
        }
        
        return false;
    }
    
    private static final Set<ElementKind> LOCAL_CLASS_PARENTS = EnumSet.of(ElementKind.CONSTRUCTOR, ElementKind.INSTANCE_INIT, ElementKind.METHOD, ElementKind.STATIC_INIT);
    
    
    public static void performInstantRename(JTextComponent target, Set<Token> highlights, int caretOffset) throws BadLocationException {
        new InstantRenamePerformer(target, highlights, caretOffset);
    }

    private static boolean isIn(MutablePositionRegion region, int caretOffset) {
	return region.getStartOffset() <= caretOffset && caretOffset <= region.getEndOffset();
    }
    
    private volatile boolean inSync;
    
    @Override
    public synchronized void insertUpdate(DocumentEvent e) {
	if (inSync)
	    return ;
	
        //check for modifications outside the first region:
        if (e.getOffset() < region.getFirstRegionStartOffset() || (e.getOffset() + e.getLength()) > region.getFirstRegionEndOffset()) {
            release();
            return;
        }
        
        inSync = true;
        region.sync(0);
        span = region.getFirstRegionLength();
        inSync = false;
        
        requestRepaint();
    }

    @Override
    public synchronized void removeUpdate(DocumentEvent e) {
	if (inSync)
	    return ;
	
        if (e.getLength() == 1) {
            if ((e.getOffset() < region.getFirstRegionStartOffset() || e.getOffset() > region.getFirstRegionEndOffset())) {
                release();
                return;
            }

            if (e.getOffset() == region.getFirstRegionStartOffset() && region.getFirstRegionLength() > 0 && region.getFirstRegionLength() == span) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "e.getOffset()={0}", e.getOffset ());
                    LOG.log(Level.FINE, "region.getFirstRegionStartOffset()={0}", region.getFirstRegionStartOffset ());
                    LOG.log(Level.FINE, "region.getFirstRegionEndOffset()={0}", region.getFirstRegionEndOffset ());
                    LOG.log(Level.FINE, "span= {0}", span);
                }
                release();
                return;
            }
            
            if (e.getOffset() == region.getFirstRegionEndOffset() && region.getFirstRegionLength() > 0 && region.getFirstRegionLength() == span) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "e.getOffset()={0}", e.getOffset ());
                    LOG.log(Level.FINE, "region.getFirstRegionStartOffset()={0}", region.getFirstRegionStartOffset ());
                    LOG.log(Level.FINE, "region.getFirstRegionEndOffset()={0}", region.getFirstRegionEndOffset ());
                    LOG.log(Level.FINE, "span= {0}", span);
                }

                release();
                return;
            }
            if (e.getOffset() == region.getFirstRegionEndOffset() && e.getOffset() == region.getFirstRegionStartOffset() && region.getFirstRegionLength() == 0 && region.getFirstRegionLength() == span) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "e.getOffset()={0}", e.getOffset ());
                    LOG.log(Level.FINE, "region.getFirstRegionStartOffset()={0}", region.getFirstRegionStartOffset ());
                    LOG.log(Level.FINE, "region.getFirstRegionEndOffset()={0}", region.getFirstRegionEndOffset ());
                    LOG.log(Level.FINE, "span= {0}", span);
                }
                
               
                release();
                return;
            }
        } else {
            //selection/multiple characters removed:
            int removeSpan = e.getLength() + region.getFirstRegionLength();
            
            if (span < removeSpan) {
                release();
                return;
            }
        }
        
        //#89997: do not sync the regions for the "remove" part of replace selection,
        //as the consequent insert may use incorrect offset, and the regions will be synced
        //after the insert anyway.
        if (doc.getProperty(BaseKit.DOC_REPLACE_SELECTION_PROPERTY) != null) {
            return ;
        }
        
        inSync = true;
        region.sync(0);
        span = region.getFirstRegionLength();
        inSync = false;
        
        requestRepaint();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    public void caretUpdate(CaretEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public synchronized void keyPressed(KeyEvent e) {
        if (   (e.getKeyCode() == KeyEvent.VK_ESCAPE && e.getModifiers() == 0) ||
               (e.getKeyCode() == KeyEvent.VK_ENTER  && e.getModifiers() == 0)
        ) {
            release();
            e.consume();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    private synchronized void release() {
        if (target == null) {
            //already released
            return ;
        }
        sendUndoableEdit(doc, CloneableEditorSupport.END_COMMIT_GROUP);
        target.putClientProperty("NetBeansEditor.navigateBoundaries", null); // NOI18N
        target.putClientProperty(InstantRenamePerformer.class, null);
        if (doc instanceof BaseDocument) {
            ((BaseDocument) doc).setPostModificationDocumentListener(null);
        }
        target.removeKeyListener(this);
        target = null;

        region = null;
        attribs = null;
        
        requestRepaint();

        doc = null;
    }

    
    private void requestRepaint() {
        if (region == null) {
            OffsetsBag bag = getHighlightsBag(doc);
            bag.clear();
        } else {
            // Compute attributes
            if (attribs == null) {
                // read the attributes for the master region
                attribs = getSyncedTextBlocksHighlight("synchronized-text-blocks-ext"); //NOI18N
                Color foreground = (Color) attribs.getAttribute(StyleConstants.Foreground);
                Color background = (Color) attribs.getAttribute(StyleConstants.Background);
                attribsLeft = createAttribs(
                        StyleConstants.Background, background,
                        EditorStyleConstants.LeftBorderLineColor, foreground, 
                        EditorStyleConstants.TopBorderLineColor, foreground, 
                        EditorStyleConstants.BottomBorderLineColor, foreground
                );
                attribsRight = createAttribs(
                        StyleConstants.Background, background,
                        EditorStyleConstants.RightBorderLineColor, foreground, 
                        EditorStyleConstants.TopBorderLineColor, foreground, 
                        EditorStyleConstants.BottomBorderLineColor, foreground
                );
                attribsMiddle = createAttribs(
                        StyleConstants.Background, background,
                        EditorStyleConstants.TopBorderLineColor, foreground, 
                        EditorStyleConstants.BottomBorderLineColor, foreground
                );
                attribsAll = createAttribs(
                        StyleConstants.Background, background,
                        EditorStyleConstants.LeftBorderLineColor, foreground, 
                        EditorStyleConstants.RightBorderLineColor, foreground,
                        EditorStyleConstants.TopBorderLineColor, foreground, 
                        EditorStyleConstants.BottomBorderLineColor, foreground
                );

                // read the attributes for the slave regions
                attribsSlave = getSyncedTextBlocksHighlight("synchronized-text-blocks-ext-slave"); //NOI18N
                Color slaveForeground = (Color) attribsSlave.getAttribute(StyleConstants.Foreground);
                Color slaveBackground = (Color) attribsSlave.getAttribute(StyleConstants.Background);
                attribsSlaveLeft = createAttribs(
                        StyleConstants.Background, slaveBackground,
                        EditorStyleConstants.LeftBorderLineColor, slaveForeground, 
                        EditorStyleConstants.TopBorderLineColor, slaveForeground, 
                        EditorStyleConstants.BottomBorderLineColor, slaveForeground
                );
                attribsSlaveRight = createAttribs(
                        StyleConstants.Background, slaveBackground,
                        EditorStyleConstants.RightBorderLineColor, slaveForeground, 
                        EditorStyleConstants.TopBorderLineColor, slaveForeground, 
                        EditorStyleConstants.BottomBorderLineColor, slaveForeground
                );
                attribsSlaveMiddle = createAttribs(
                        StyleConstants.Background, slaveBackground,
                        EditorStyleConstants.TopBorderLineColor, slaveForeground, 
                        EditorStyleConstants.BottomBorderLineColor, slaveForeground
                );
                attribsSlaveAll = createAttribs(
                        StyleConstants.Background, slaveBackground,
                        EditorStyleConstants.LeftBorderLineColor, slaveForeground, 
                        EditorStyleConstants.RightBorderLineColor, slaveForeground,
                        EditorStyleConstants.TopBorderLineColor, slaveForeground, 
                        EditorStyleConstants.BottomBorderLineColor, slaveForeground
                );
            }
            
            OffsetsBag nue = new OffsetsBag(doc);
            for(int i = 0; i < region.getRegionCount(); i++) {
                int startOffset = region.getRegion(i).getStartOffset();
                int endOffset = region.getRegion(i).getEndOffset();
                int size = region.getRegion(i).getLength();
                if (size == 1) {
                    nue.addHighlight(startOffset, endOffset, i == 0 ? attribsAll : attribsSlaveAll);
                } else if (size > 1) {
                    nue.addHighlight(startOffset, startOffset + 1, i == 0 ? attribsLeft : attribsSlaveLeft);
                    nue.addHighlight(endOffset - 1, endOffset, i == 0 ? attribsRight : attribsSlaveRight);
                    if (size > 2) {
                        nue.addHighlight(startOffset + 1, endOffset - 1, i == 0 ? attribsMiddle : attribsSlaveMiddle);
                    }
                }
            }
            
            OffsetsBag bag = getHighlightsBag(doc);
            bag.setHighlights(nue);
        }
    }
    
//    private static final AttributeSet defaultSyncedTextBlocksHighlight = AttributesUtilities.createImmutable(StyleConstants.Background, new Color(138, 191, 236));
    private static final AttributeSet defaultSyncedTextBlocksHighlight = AttributesUtilities.createImmutable(StyleConstants.Foreground, Color.red);
    
    private static AttributeSet getSyncedTextBlocksHighlight(String name) {
        FontColorSettings fcs = MimeLookup.getLookup(MimePath.EMPTY).lookup(FontColorSettings.class);
        AttributeSet as = fcs != null ? fcs.getFontColors(name) : null;
        return as == null ? defaultSyncedTextBlocksHighlight : as;
    }
    
    private static AttributeSet createAttribs(Object... keyValuePairs) {
        assert keyValuePairs.length % 2 == 0 : "There must be even number of prameters. " +
            "They are key-value pairs of attributes that will be inserted into the set.";

        List<Object> list = new ArrayList<Object>();
        
        for(int i = keyValuePairs.length / 2 - 1; i >= 0 ; i--) {
            Object attrKey = keyValuePairs[2 * i];
            Object attrValue = keyValuePairs[2 * i + 1];

            if (attrKey != null && attrValue != null) {
                list.add(attrKey);
                list.add(attrValue);
            }
        }
        
        return AttributesUtilities.createImmutable(list.toArray());
    }
    
    public static OffsetsBag getHighlightsBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(InstantRenamePerformer.class);
        
        if (bag == null) {
            doc.putProperty(InstantRenamePerformer.class, bag = new OffsetsBag(doc));
            
            Object stream = doc.getProperty(Document.StreamDescriptionProperty);
            
            if (stream instanceof DataObject) {
                Logger.getLogger("TIMER").log(Level.FINE, "Instant Rename Highlights Bag", new Object[] {((DataObject) stream).getPrimaryFile(), bag}); //NOI18N
            }
        }
        
        return bag;
    }
    
    private static void sendUndoableEdit(Document d, UndoableEdit ue) {
        if(d instanceof AbstractDocument) {
            UndoableEditListener[] uels = ((AbstractDocument)d).getUndoableEditListeners();
            UndoableEditEvent ev = new UndoableEditEvent(d, ue);
            for(UndoableEditListener uel : uels) {
                uel.undoableEditHappened(ev);
            }
        }
    }

    private static class CancelInstantRenameUndoableEdit extends AbstractUndoableEdit {

        private final Reference<InstantRenamePerformer> performer;

        public CancelInstantRenameUndoableEdit(InstantRenamePerformer performer) {
            this.performer = new WeakReference<InstantRenamePerformer>(performer);
        }

        @Override public boolean isSignificant() {
            return false;
        }

        @Override public void undo() throws CannotUndoException {
            InstantRenamePerformer perf = performer.get();

            if (perf != null) {
                perf.release();
            }
        }
    }
    
    @ServiceProvider(service=RefactoringPluginFactory.class, position=95)
    public static class AllRefactoringsPluginFactory implements RefactoringPluginFactory {

        @Override
        public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
            return new RefactoringPluginImpl();
        }

        private static final class RefactoringPluginImpl implements RefactoringPlugin {

            @Override
            public Problem preCheck() {
                return null;
            }

            @Override
            public Problem checkParameters() {
                return null;
            }

            @Override
            public Problem fastCheckParameters() {
                return null;
            }

            @Override
            public void cancelRequest() {}

            @Override
            public Problem prepare(RefactoringElementsBag refactoringElements) {
                refactoringElements.getSession().addProgressListener(new ProgressListener() {
                    @Override
                    public void start(ProgressEvent event) {
                        final InstantRenamePerformer[] performers = registry.toArray(new InstantRenamePerformer[0]);
                        for (InstantRenamePerformer p : performers) {
                            p.inSync = true;
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                for (InstantRenamePerformer p : performers) {
                                    p.release();
                                }
                            }
                        });
                    }
                    @Override
                    public void step(ProgressEvent event) {}
                    @Override
                    public void stop(ProgressEvent event) {}
                });

                return null;
            }

        }

    }
    
    public static class RenameDeletedTextInterceptor implements DeletedTextInterceptor {
        
        @Override
        public boolean beforeRemove(Context context) throws BadLocationException {
            Object getObject = context.getComponent().getClientProperty(InstantRenamePerformer.class);
            if (getObject instanceof InstantRenamePerformer) {
                InstantRenamePerformer instantRenamePerformer = (InstantRenamePerformer)getObject;
                MutablePositionRegion region = instantRenamePerformer.region.getRegion(0);
                return ((context.isBackwardDelete() && region.getStartOffset() == context.getOffset()) || (!context.isBackwardDelete() && region.getEndOffset() == context.getOffset()));
            } else {
                return false;
            }
        }
        @Override
        public void remove(Context context) throws BadLocationException {            
        }

        @Override
        public void afterRemove(Context context) throws BadLocationException {
        }

        @Override
        public void cancelled(Context context) {
        }

        @MimeRegistrations({
            @MimeRegistration(mimeType = JavaKit.JAVA_MIME_TYPE, service = DeletedTextInterceptor.Factory.class)
        })
        public static class Factory implements DeletedTextInterceptor.Factory {

            @Override
            public DeletedTextInterceptor createDeletedTextInterceptor(MimePath mimePath) {
                return new RenameDeletedTextInterceptor();
            }
        }
    }
}
