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
package org.netbeans.modules.java.editor.base.semantic;

import com.sun.source.tree.BreakTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.Document;

import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaParserResultTask;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.editor.base.javadoc.JavadocImports;
import org.netbeans.modules.java.editor.base.options.MarkOccurencesSettingsNames;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;

/**
 *
 * @author Jan Lahoda
 */
public abstract class MarkOccurrencesHighlighterBase extends JavaParserResultTask {

    protected MarkOccurrencesHighlighterBase() {
        super(Phase.RESOLVED, TaskIndexingMode.ALLOWED_DURING_SCAN);
    }

    @Override
    public void run (Result parseResult, SchedulerEvent event) {
        resume();

        CompilationInfo info = CompilationInfo.get(parseResult);

        if (info == null) {
            return ;
        }

        Document doc = parseResult.getSnapshot().getSource().getDocument(false);
        
        process(info, doc, event);
    }

    protected abstract void process(CompilationInfo info, Document doc, SchedulerEvent event);
    
    private boolean isIn(CompilationUnitTree cu, SourcePositions sp, Tree tree, int position) {
        return sp.getStartPosition(cu, tree) <= position && position <= sp.getEndPosition(cu, tree);
    }

    private boolean isIn(int caretPosition, Token span) {
//        System.err.println("caretPosition = " + caretPosition );
//        System.err.println("span[0]= " + span[0]);
//        System.err.println("span[1]= " + span[1]);
        if (span == null)
            return false;

        return span.offset(null) <= caretPosition && caretPosition <= span.offset(null) + span.length();
    }

    protected List<int[]> processImpl(CompilationInfo info, Preferences node, Document doc, int caretPosition) {
        caretPosition = info.getSnapshot().getEmbeddedOffset(caretPosition);

        TokenSequence<JavaTokenId> cts = info.getTokenHierarchy().tokenSequence(JavaTokenId.language());

        if (cts != null) {
            cts.move(caretPosition);

            if (cts.moveNext() && cts.token().id() == JavaTokenId.IDENTIFIER && cts.offset() == caretPosition) {
                caretPosition++;
            }
        }

        CompilationUnitTree cu = info.getCompilationUnit();
        TreeUtilities tu = info.getTreeUtilities();
        TreePath tp = tu.pathFor(caretPosition);
        if (tp.getParentPath() != null && tp.getParentPath().getLeaf().getKind() == Kind.ANNOTATED_TYPE) {
            tp = tp.getParentPath();
        }
        TreePath typePath = findTypePath(tp);

        if (isCancelled())
            return null;

        //detect caret inside the return type or throws clause:
        if (typePath != null && typePath.getParentPath().getLeaf().getKind() == Kind.METHOD) {
            //hopefully found something, check:
            TreePath declPath = typePath.getParentPath();
            MethodTree decl = (MethodTree) declPath.getLeaf();
            Tree type = decl.getReturnType();

            if (   node.getBoolean(MarkOccurencesSettingsNames.EXIT, true)
                && isIn(cu, info.getTrees().getSourcePositions(), type, caretPosition)) {
                MethodExitDetector med = new MethodExitDetector();

                setExitDetector(med);

                try {
                    return med.process(info, doc, declPath, null);
                } finally {
                    setExitDetector(null);
                }
            }

            for (Tree exc : decl.getThrows()) {
                if (   node.getBoolean(MarkOccurencesSettingsNames.EXCEPTIONS, true)
                    && isIn(cu, info.getTrees().getSourcePositions(), exc, caretPosition)) {
                    MethodExitDetector med = new MethodExitDetector();

                    setExitDetector(med);

                    try {
                        return med.process(info, doc, declPath, Collections.singletonList(exc));
                    } finally {
                        setExitDetector(null);
                    }
                }
            }
        }

        if (isCancelled())
            return null;

        if (node.getBoolean(MarkOccurencesSettingsNames.EXCEPTIONS, true)) {
            //detect caret inside catch:
            if (typePath != null && ((typePath.getParentPath().getLeaf().getKind() == Kind.UNION_TYPE
                    && typePath.getParentPath().getParentPath().getLeaf().getKind() == Kind.VARIABLE
                    && typePath.getParentPath().getParentPath().getParentPath().getLeaf().getKind() == Kind.CATCH)
                    || (typePath.getParentPath().getLeaf().getKind() == Kind.VARIABLE
                    && typePath.getParentPath().getParentPath().getLeaf().getKind() == Kind.CATCH))) {
                    MethodExitDetector med = new MethodExitDetector();

                    setExitDetector(med);

                    try {
                        TreePath tryPath = tu.getPathElementOfKind(Kind.TRY, typePath);
                        if (tryPath != null) {
                            return med.process(info, doc, new TreePath(tryPath, ((TryTree)tryPath.getLeaf()).getBlock()), Collections.singletonList(typePath.getLeaf()));
                        }
                    } finally {
                        setExitDetector(null);
                    }
            }
        }

        if (isCancelled())
            return null;
        
        if (node.getBoolean(MarkOccurencesSettingsNames.IMPLEMENTS, true) || node.getBoolean(MarkOccurencesSettingsNames.OVERRIDES, true)) {
            //detect caret inside the extends/implements clause:
            if (typePath != null && TreeUtilities.CLASS_TREE_KINDS.contains(typePath.getParentPath().getLeaf().getKind())) {
                ClassTree ctree = (ClassTree) typePath.getParentPath().getLeaf();
                int bodyStart = Utilities.findBodyStart(info, ctree, cu, info.getTrees().getSourcePositions(), doc);

                boolean isExtends = ctree.getExtendsClause() == typePath.getLeaf();
                boolean isImplements = false;

                for (Tree t : ctree.getImplementsClause()) {
                    if (t == typePath.getLeaf()) {
                        isImplements = true;
                        break;
                    }
                }

                if (   (isExtends && node.getBoolean(MarkOccurencesSettingsNames.OVERRIDES, true))
                    || (isImplements && node.getBoolean(MarkOccurencesSettingsNames.IMPLEMENTS, true))) {
                    Element superType = info.getTrees().getElement(typePath);
                    Element thisType  = info.getTrees().getElement(typePath.getParentPath());

                    if (isClass(superType) && isClass(thisType))
                        return detectMethodsForClass(info, doc, typePath.getParentPath(), (TypeElement) superType, (TypeElement) thisType);
                }
            }

            if (isCancelled())
                return null;

            TokenSequence<JavaTokenId> ts = info.getTokenHierarchy().tokenSequence(JavaTokenId.language());

            if (ts != null && TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
                int bodyStart = Utilities.findBodyStart(info, tp.getLeaf(), cu, info.getTrees().getSourcePositions(), doc);

                if (caretPosition < bodyStart) {
                    ts.move(caretPosition);

                    if (ts.moveNext()) {
                        if (node.getBoolean(MarkOccurencesSettingsNames.OVERRIDES, true) && ts.token().id() == JavaTokenId.EXTENDS) {
                            Tree superClass = ((ClassTree) tp.getLeaf()).getExtendsClause();

                            if (superClass != null) {
                                Element superType = info.getTrees().getElement(new TreePath(tp, superClass));
                                Element thisType  = info.getTrees().getElement(tp);

                                if (isClass(superType) && isClass(thisType))
                                    return detectMethodsForClass(info, doc, tp, (TypeElement) superType, (TypeElement) thisType);
                            }
                        }

                        if (node.getBoolean(MarkOccurencesSettingsNames.IMPLEMENTS, true) && ts.token().id() == JavaTokenId.IMPLEMENTS) {
                            List<? extends Tree> superClasses = ((ClassTree) tp.getLeaf()).getImplementsClause();

                            if (superClasses != null) {
                                List<TypeElement> superTypes = new ArrayList<TypeElement>();

                                for (Tree superTypeTree : superClasses) {
                                    if (superTypeTree != null) {
                                        Element superType = info.getTrees().getElement(new TreePath(tp, superTypeTree));

                                        if (isClass(superType))
                                            superTypes.add((TypeElement) superType);
                                    }
                                }

                                Element thisType  = info.getTrees().getElement(tp);

                                if (!superTypes.isEmpty() && isClass(thisType))
                                    return detectMethodsForClass(info, doc, tp, superTypes, (TypeElement) thisType);
                            }

                        }
                    }
                }
            }
        }

        if (isCancelled())
            return null;

        Tree tree =tp.getLeaf();

        if (node.getBoolean(MarkOccurencesSettingsNames.BREAK_CONTINUE, true)) {
            if (tree.getKind() == Kind.BREAK || tree.getKind() == Kind.CONTINUE) {
                return detectBreakOrContinueTarget(info, doc, tp, caretPosition);
            } else if (tree.getKind() == Kind.LABELED_STATEMENT) {
                int[] span = Utilities.findIdentifierSpan(tp, info, doc);
                if (span[0] <= caretPosition && caretPosition <= span[1]) {
                    List<int[]> ret = detectLabel(info, doc, tp);
                    ret.add(span);
                    return ret;
                }
            }
        }

        Element el;

        el = JavadocImports.findReferencedElement(info, caretPosition);
        boolean insideJavadoc = el != null;

        if (isCancelled()) {
            return null;
        }

        //variable declaration:
        if (!insideJavadoc) {
            if (tp.getParentPath() != null && tp.getParentPath().getLeaf().getKind() == Kind.NEW_CLASS) {
                TreePath c = new TreePath(tp.getParentPath(), ((NewClassTree) tp.getParentPath().getLeaf()).getIdentifier());
                if (isIn(caretPosition, Utilities.findIdentifierSpan(info, doc, c))) {
                    el = info.getTrees().getElement(tp.getParentPath());
                } else {
                    el = info.getTrees().getElement(tp);
                }
            } else {
                el = info.getTrees().getElement(tp);
            }
        }

        if (   el != null
                && el.getKind() != ElementKind.MODULE
                && el.asType().getKind() != TypeKind.OTHER
                && (!TreeUtilities.CLASS_TREE_KINDS.contains(tree.getKind()) || isIn(caretPosition, Utilities.findIdentifierSpan(info, doc, tp)))
                && !Utilities.isNonCtorKeyword(tree)
                && (!(tree.getKind() == Kind.METHOD) || isIn(caretPosition, Utilities.findIdentifierSpan(info, doc, tp)))
                && isEnabled(node, el)
                || (insideJavadoc && isEnabled(node, el))) {
            FindLocalUsagesQuery fluq = new FindLocalUsagesQuery();

            setLocalUsages(fluq);

            try {
                List<int[]> bag = new ArrayList<int[]>();
                for (Token t : fluq.findUsages(el, info, doc)) {
                    // type parameter name is represented as ident -> ignore surrounding <> in rename
                    int delta = t.id() == JavadocTokenId.IDENT && t.text().charAt(0) == '<' && t.text().charAt(t.length() - 1) == '>' ? 1 : 0;
                    bag.add(new int[] {t.offset(null) + delta, t.offset(null) + t.length() - delta});
                }

                return bag;
            } finally {
                setLocalUsages(null);
            }
        }

        if (tp.getParentPath() != null && tp.getParentPath().getLeaf().getKind() == Kind.IMPORT) {
            ImportTree it = (ImportTree) tp.getParentPath().getLeaf();
            if (it.isStatic() && tp.getLeaf().getKind() == Kind.MEMBER_SELECT) {
                MemberSelectTree mst = (MemberSelectTree) tp.getLeaf();
                if (!"*".contentEquals(mst.getIdentifier())) {
                    List<int[]> bag = new ArrayList<int[]>();
                    Token<JavaTokenId> tok = Utilities.getToken(info, doc, tp);
                    if (tok != null)
                        bag.add(new int[] {tok.offset(null), tok.offset(null) + tok.length()});
                    el = info.getTrees().getElement(new TreePath(tp, mst.getExpression()));
                    if (el != null) {
                        FindLocalUsagesQuery fluq = new FindLocalUsagesQuery();
                        setLocalUsages(fluq);
                        try {
                            for (Element element : el.getEnclosedElements()) {
                                if (element.getModifiers().contains(Modifier.STATIC)) {
                                    for (Token t : fluq.findUsages(element, info, doc)) {
                                        bag.add(new int[] {t.offset(null), t.offset(null) + t.length()});
                                    }
                                }
                            }
                            return bag;
                        } finally {
                            setLocalUsages(null);
                        }
                    }
                }
            }
        }
        
        return null;
    }

    private static final Set<Kind> TYPE_PATH_ELEMENT = EnumSet.of(Kind.IDENTIFIER, Kind.PRIMITIVE_TYPE, Kind.PARAMETERIZED_TYPE, Kind.MEMBER_SELECT, Kind.ARRAY_TYPE);

    private static TreePath findTypePath(TreePath tp) {
        if (!TYPE_PATH_ELEMENT.contains(tp.getLeaf().getKind()))
            return null;

        while (TYPE_PATH_ELEMENT.contains(tp.getParentPath().getLeaf().getKind())) {
            tp = tp.getParentPath();
        }

        return tp;
    }

    private static boolean isClass(Element el) {
        return el != null && (el.getKind().isClass() || el.getKind().isInterface());
    }

    private static boolean isEnabled(Preferences node, Element el) {
        switch (el.getKind()) {
            case ANNOTATION_TYPE:
            case CLASS:
            case ENUM:
            case INTERFACE:
            case TYPE_PARAMETER: //???
                return node.getBoolean(MarkOccurencesSettingsNames.TYPES, true);
            case CONSTRUCTOR:
            case METHOD:
                return node.getBoolean(MarkOccurencesSettingsNames.METHODS, true);
            case ENUM_CONSTANT:
                return node.getBoolean(MarkOccurencesSettingsNames.CONSTANTS, true);
            case FIELD:
                if (el.getModifiers().containsAll(EnumSet.of(Modifier.STATIC, Modifier.FINAL))) {
                    return node.getBoolean(MarkOccurencesSettingsNames.CONSTANTS, true);
                } else {
                    return node.getBoolean(MarkOccurencesSettingsNames.FIELDS, true);
                }
            case LOCAL_VARIABLE:
            case RESOURCE_VARIABLE:
            case PARAMETER:
            case EXCEPTION_PARAMETER:
                return node.getBoolean(MarkOccurencesSettingsNames.LOCAL_VARIABLES, true);
            case MODULE:
            case PACKAGE:
                return false; //never mark occurrence modules and packages
            default:
                Logger.getLogger(MarkOccurrencesHighlighterBase.class.getName()).log(Level.INFO, "Unknown element type: {0}.", el.getKind());
                return true;
        }
    }

    private boolean canceled;
    private MethodExitDetector exitDetector;
    private FindLocalUsagesQuery localUsages;

    private final synchronized void setExitDetector(MethodExitDetector detector) {
        this.exitDetector = detector;
    }

    private final synchronized void setLocalUsages(FindLocalUsagesQuery localUsages) {
        this.localUsages = localUsages;
    }

    public final synchronized void cancel() {
        canceled = true;

        if (exitDetector != null) {
            exitDetector.cancel();
        }
        if (localUsages != null) {
            localUsages.cancel();
        }
    }

    protected final synchronized boolean isCancelled() {
        return canceled;
    }

    protected final synchronized void resume() {
        canceled = false;
    }

    private List<int[]> detectMethodsForClass(CompilationInfo info, Document document, TreePath clazz, TypeElement superType, TypeElement thisType) {
        return detectMethodsForClass(info, document, clazz, Collections.singletonList(superType), thisType);
    }

    private List<int[]> detectMethodsForClass(CompilationInfo info, Document document, TreePath clazz, List<TypeElement> superTypes, TypeElement thisType) {
        List<int[]> highlights = new ArrayList<int[]>();
        ClassTree clazzTree = (ClassTree) clazz.getLeaf();
        TypeElement jlObject = info.getElements().getTypeElement("java.lang.Object");

        OUTER: for (Tree member: clazzTree.getMembers()) {
            if (isCancelled()) {
                return null;
            }

            if (member.getKind() == Kind.METHOD) {
                TreePath path = new TreePath(clazz, member);
                Element el = info.getTrees().getElement(path);

                if (el.getKind() == ElementKind.METHOD) {
                    for (TypeElement superType : superTypes) {
                        for (ExecutableElement ee : ElementFilter.methodsIn(info.getElements().getAllMembers(superType))) {
                            if (info.getElements().overrides((ExecutableElement) el, ee, thisType) && (superType.getKind().isClass() || !ee.getEnclosingElement().equals(jlObject))) {
                                Token t = Utilities.getToken(info, document, path);

                                if (t != null) {
                                    highlights.add(new int[] {t.offset(null), t.offset(null) + t.length()});
                                }
                                continue OUTER;
                            }
                        }
                    }
                }
            }
        }

        return highlights;
    }

    private List<int[]> detectBreakOrContinueTarget(CompilationInfo info, Document document, TreePath breakOrContinue, int caretPosition) {
        List<int[]> result = new ArrayList<int[]>();
        Tree target = info.getTreeUtilities().getBreakContinueTargetTree(breakOrContinue);

        if (target == null)
            return null;
        
        TokenSequence<JavaTokenId> ts = info.getTokenHierarchy().tokenSequence(JavaTokenId.language());
        
        ts.move((int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), target));

        if (ts.moveNext()) {
            result.add(new int[] {ts.offset(), ts.offset() + ts.token().length()});
        }
        StatementTree targetStatementTree = null;
        ExpressionTree targetExpressionTree = null;
        if (target instanceof StatementTree) {
            targetStatementTree = target.getKind() == Kind.LABELED_STATEMENT ?  ((LabeledStatementTree) target).getStatement() : (StatementTree) target;
        } else if (target instanceof ExpressionTree) {
            targetExpressionTree = (ExpressionTree) target;
        }
        Tree block = null;
        
        if (targetStatementTree != null) {
            switch (targetStatementTree.getKind()) {
                case SWITCH:
                    block = targetStatementTree;
                    break;
                case WHILE_LOOP:
                    if (((WhileLoopTree) targetStatementTree).getStatement().getKind() == Kind.BLOCK)
                        block = ((WhileLoopTree) targetStatementTree).getStatement();
                    break;
                case FOR_LOOP:
                    if (((ForLoopTree) targetStatementTree).getStatement().getKind() == Kind.BLOCK)
                        block = ((ForLoopTree) targetStatementTree).getStatement();
                    break;
                case ENHANCED_FOR_LOOP:
                    if (((EnhancedForLoopTree) targetStatementTree).getStatement().getKind() == Kind.BLOCK)
                        block = ((EnhancedForLoopTree) targetStatementTree).getStatement();
                    break;
                case DO_WHILE_LOOP:
                    if (((DoWhileLoopTree) targetStatementTree).getStatement().getKind() == Kind.BLOCK)
                        block = ((DoWhileLoopTree) targetStatementTree).getStatement();
                    break;
            }
        // No need to check for version of JDK, as targetExpressionTree can only be non-null in case of JDK-12 or higher
        } else if (targetExpressionTree != null) {
            block = targetExpressionTree;
        }

        if (block != null) {
            ts.move((int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), block));

            if (ts.movePrevious() && ts.token().id() == JavaTokenId.RBRACE) {
                result.add(new int[] {ts.offset(), ts.offset() + ts.token().length()});
            }
        }
        
        if (target.getKind() == Kind.LABELED_STATEMENT && isIn(caretPosition, Utilities.findIdentifierSpan(info, document, breakOrContinue))) {
            result.addAll(detectLabel(info, document, info.getTrees().getPath(info.getCompilationUnit(), target)));
        }

        return result;
    }
    
    private List<int[]> detectLabel(final CompilationInfo info, final Document document, final TreePath labeledStatement) {
        final List<int[]> result = new ArrayList<int[]>();
        if (labeledStatement.getLeaf().getKind() == Kind.LABELED_STATEMENT) {
            final Name label = ((LabeledStatementTree)labeledStatement.getLeaf()).getLabel();
            new ErrorAwareTreePathScanner <Void, Void>() {
                @Override
                public Void visitBreak(BreakTree node, Void p) {
                    if (node.getLabel() != null && label.contentEquals(node.getLabel())) {
                        result.add(Utilities.findIdentifierSpan(getCurrentPath(), info, document));
                    }
                    return super.visitBreak(node, p);
                }
                @Override
                public Void visitContinue(ContinueTree node, Void p) {
                    if (node.getLabel() != null && label.contentEquals(node.getLabel())) {
                        result.add(Utilities.findIdentifierSpan(getCurrentPath(), info, document));
                    }
                    return super.visitContinue(node, p);
                }
            }.scan(labeledStatement, null);
        }
        return result;
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }

}

