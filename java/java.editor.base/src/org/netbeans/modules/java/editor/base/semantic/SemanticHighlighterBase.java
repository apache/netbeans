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
package org.netbeans.modules.java.editor.base.semantic;

import com.sun.source.tree.CaseLabelTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExportsTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.OpensTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.PatternCaseLabelTree;
import com.sun.source.tree.ProvidesTree;
import com.sun.source.tree.RequiresTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.UsesTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaParserResultTask;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.modules.java.editor.base.imports.UnusedImports;
import org.netbeans.modules.java.editor.base.semantic.ColoringAttributes.Coloring;
import org.netbeans.modules.java.editor.base.semantic.UnusedDetector.UnusedDescription;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbPreferences;
import org.openide.util.Pair;


/**
 *
 * @author Jan Lahoda
 */
@SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
public abstract class SemanticHighlighterBase extends JavaParserResultTask<Result> {

    public static final String JAVA_INLINE_HINT_PARAMETER_NAME = "javaInlineHintParameterName"; //NOI18N
    public static final String JAVA_INLINE_HINT_CHAINED_TYPES = "javaInlineHintChainedTypes"; //NOI18N
    public static final String JAVA_INLINE_HINT_VAR_TYPE = "javaInlineHintVarType"; //NOI18N

    private final AtomicBoolean cancel = new AtomicBoolean();

    protected SemanticHighlighterBase() {
        super(Phase.RESOLVED, TaskIndexingMode.ALLOWED_DURING_SCAN);
    }

    @Override
    public void run(Result result, SchedulerEvent event) {

        CompilationInfo info = CompilationInfo.get(result);
        
        if (info == null) {
            return ;
        }
        
        cancel.set(false);
        
        final Document doc = result.getSnapshot().getSource().getDocument(false);
        
        if (!verifyDocument(doc)) return;

        process(info, doc);
    }

    private static boolean verifyDocument(final Document doc) {
        if (doc == null) {
            Logger.getLogger(SemanticHighlighterBase.class.getName()).log(Level.FINE, "SemanticHighlighter: Cannot get document!");
            return false;
        }

        final boolean[] tokenSequenceNull =  new boolean[1];
        doc.render(() -> {
            tokenSequenceNull[0] = (TokenHierarchy.get(doc).tokenSequence() == null);
        });
        return !tokenSequenceNull[0];
    }
    
    @Override
    public void cancel() {
        cancel.set(true);
    }
    

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }
        
    protected abstract boolean process(CompilationInfo info, final Document doc);
    
    protected boolean process(CompilationInfo info, final Document doc, ErrorDescriptionSetter setter) {
        return process(info, doc, Settings.getDefault(), setter);
    }

    protected boolean process(CompilationInfo info, final Document doc, Settings settings, ErrorDescriptionSetter setter) {
        DetectorVisitor v = new DetectorVisitor(info, doc, settings, cancel);
        
        Map<Token, Coloring> newColoring = new IdentityHashMap<>();

        CompilationUnitTree cu = info.getCompilationUnit();
        
        v.scan(cu, null);
        
        if (cancel.get())
            return true;
        
        boolean computeUnusedImports = "text/x-java".equals(FileUtil.getMIMEType(info.getFileObject()));
        
        List<Pair<int[], Coloring>> extraColoring = computeUnusedImports ? new ArrayList<>(v.extraColoring) : v.extraColoring;

        if (computeUnusedImports) {
            Collection<TreePath> unusedImports = UnusedImports.process(info, cancel);

            if (unusedImports == null) return true;
            
            Coloring unused = collection2Coloring(Arrays.asList(ColoringAttributes.UNUSED));

            for (TreePath tree : unusedImports) {
                if (cancel.get()) {
                    return true;
                }

                //XXX: finish
                extraColoring.add(Pair.of(new int[] {
                    (int) info.getTrees().getSourcePositions().getStartPosition(cu, tree.getLeaf()),
                    (int) info.getTrees().getSourcePositions().getEndPosition(cu, tree.getLeaf())
                }, unused));
            }
        }
        
        Map<Element, List<UnusedDescription>> element2Unused = UnusedDetector.findUnused(info, () -> cancel.get()) //XXX: unnecessarily ugly
                                                                             .stream()
                                                                             .collect(Collectors.groupingBy(ud -> ud.unusedElement()));
        for (Map.Entry<Element, List<Use>> entry : v.type2Uses.entrySet()) {
            if (cancel.get())
                return true;
            
            Element decl = entry.getKey();
            List<Use> uses = entry.getValue();
            
            for (Use u : uses) {
                if (u.spec == null)
                    continue;
                
                if (u.declaration) {
                    if (element2Unused.containsKey(decl)) {
                        u.spec.add(ColoringAttributes.UNUSED);
                    }
                }
                
                Coloring c = collection2Coloring(u.spec);
                
                List<Token> tl = v.tree2Tokens.get(u.tree.getLeaf());
                
                if (tl != null) {
                    for (Token t : tl) {
                        newColoring.put(t, c);
                    }
                }
            }
        }
        
        Coloring kwc = collection2Coloring(EnumSet.of(ColoringAttributes.KEYWORD));
        for (Token kw : v.contextKeywords) {
            newColoring.put(kw, kwc);
        }
        
        if (cancel.get())
            return true;
        
        if (computeUnusedImports) {
            Map<int[], String> preTextWithSpans = new HashMap<>();
            v.preText.forEach((pos, text) -> preTextWithSpans.put(new int[] {pos, pos + 1}, text));
            setter.setHighlights(doc, extraColoring, preTextWithSpans);
        }

        setter.setColorings(doc, newColoring);

        return false;
    }
    
    private static Coloring collection2Coloring(Collection<ColoringAttributes> attr) {
        Coloring c = ColoringAttributes.empty();
        
        for (ColoringAttributes a : attr) {
            c = ColoringAttributes.add(c, a);
        }
        
        return c;
    }
    
    private static final Set<ElementKind> LOCAL_VARIABLES = EnumSet.of(
            ElementKind.LOCAL_VARIABLE, ElementKind.RESOURCE_VARIABLE,
            ElementKind.EXCEPTION_PARAMETER, ElementKind.BINDING_VARIABLE);

    private static boolean isLocalVariableClosure(Element el) {
        return el.getKind() == ElementKind.PARAMETER ||
               LOCAL_VARIABLES.contains(el.getKind());
    }

    private record Use(boolean declaration, TreePath tree, Collection<ColoringAttributes> spec) {
        @Override
        public String toString() {
            return "Use: " + spec;
        }
    }
    
    private static class DetectorVisitor extends CancellableTreePathScanner<Void, Void> {
        
        private final CompilationInfo info;
        private final Settings settings;
        private Map<Element, List<Use>> type2Uses;        
        private Map<Tree, List<Token>> tree2Tokens;
        private List<Token> contextKeywords;
        private List<Pair<int[], Coloring>> extraColoring;
        private Map<Integer, String> preText;
        private final TokenList tl;
        private long memberSelectBypass = -1;        
        private final SourcePositions sourcePositions;
        private ExecutableElement recursionDetector;
        
        private DetectorVisitor(CompilationInfo info, Document doc, Settings settings, AtomicBoolean cancel) {
            super(cancel);
            
            this.info = info;
            this.settings = settings;
            type2Uses = new HashMap<>();
            tree2Tokens = new IdentityHashMap<>();
            contextKeywords = new ArrayList<>();
            extraColoring = new ArrayList<>();
            preText = new HashMap<>();

            tl = new TokenList(info, doc, cancel);
            
            this.sourcePositions = info.getTrees().getSourcePositions();
        }
        
        private void firstIdentifier(String name) {
            firstIdentifier(getCurrentPath(), name);
        }

        private void firstIdentifier(TreePath path, String name) {
            tl.firstIdentifier(path, name, tree2Tokens);
        }
        
        private Token firstIdentifierToken(String... names) {
            for (String name : names) {
                Token t = tl.firstIdentifier(getCurrentPath(), name);
                if (t != null) {
                    return t;
                }
            }
            return null;
        }
        
        @Override
        public Void visitMemberSelect(MemberSelectTree tree, Void p) {
            if (info.getTreeUtilities().isSynthetic(getCurrentPath()))
                return null;

            long memberSelectBypassLoc = memberSelectBypass;
            
            memberSelectBypass = -1;
            
            Element el = info.getTrees().getElement(getCurrentPath());
            
            if (el != null && el.getKind() == ElementKind.MODULE) {
                //Xxx
                handlePossibleIdentifier(getCurrentPath(), false);
                tl.moduleNameHere(tree, tree2Tokens);
                return null;
            }

            super.visitMemberSelect(tree, p);
            
            tl.moveToEnd(tree.getExpression());
            
            if (memberSelectBypassLoc != (-1)) {
                tl.moveToOffset(memberSelectBypassLoc);
            }
            
            handlePossibleIdentifier(getCurrentPath(), false);
            firstIdentifier(tree.getIdentifier().toString());
            addParameterInlineHint(tree);
            return null;
        }
        
        private void addModifiers(Element decl, Collection<ColoringAttributes> c) {
            if (decl.getModifiers().contains(Modifier.STATIC)) {
                c.add(ColoringAttributes.STATIC);
            }
            
            if (decl.getModifiers().contains(Modifier.ABSTRACT) && !decl.getKind().isInterface()) {
                c.add(ColoringAttributes.ABSTRACT);
            }
            
            boolean accessModifier = false;

            if (decl.getModifiers().contains(Modifier.PUBLIC)) {
                c.add(ColoringAttributes.PUBLIC);
                accessModifier = true;
            }
            
            if (decl.getModifiers().contains(Modifier.PROTECTED)) {
                c.add(ColoringAttributes.PROTECTED);
                accessModifier = true;
            }
            
            if (decl.getModifiers().contains(Modifier.PRIVATE)) {
                c.add(ColoringAttributes.PRIVATE);
                accessModifier = true;
            }
            
            if (!accessModifier && !isLocalVariableClosure(decl)) {
                c.add(ColoringAttributes.PACKAGE_PRIVATE);
            }
            
            if (info.getElements().isDeprecated(decl)) {
                c.add(ColoringAttributes.DEPRECATED);
            }
        }
        
        private Collection<ColoringAttributes> getMethodColoring(ExecutableElement mdecl) {
            Collection<ColoringAttributes> c = new ArrayList<>();
            
            addModifiers(mdecl, c);
            
            if (mdecl.getKind() == ElementKind.CONSTRUCTOR) {
                c.add(ColoringAttributes.CONSTRUCTOR);
            } else
                c.add(ColoringAttributes.METHOD);
            
            return c;
        }
        
        private Collection<ColoringAttributes> getVariableColoring(Element decl) {
            Collection<ColoringAttributes> c = new ArrayList<>();
            
            addModifiers(decl, c);
            
            if (decl.getKind().isField() || decl.getKind() == ElementKind.RECORD_COMPONENT) {
                if (decl.getKind().isField()) {
                    c.add(ColoringAttributes.FIELD);
                } else {
                    c.add(ColoringAttributes.RECORD_COMPONENT);
                }
                
                return c;
            }
            
            if (LOCAL_VARIABLES.contains(decl.getKind())) {
                c.add(ColoringAttributes.LOCAL_VARIABLE);
                
                return c;
            }
            
            if (decl.getKind() == ElementKind.PARAMETER) {
                c.add(ColoringAttributes.PARAMETER);
                
                return c;
            }
            
            assert false;
            
            return null;
        }

        private static final Set<Kind> LITERALS = EnumSet.of(Kind.BOOLEAN_LITERAL, Kind.CHAR_LITERAL, Kind.DOUBLE_LITERAL, Kind.FLOAT_LITERAL, Kind.INT_LITERAL, Kind.LONG_LITERAL, Kind.STRING_LITERAL);

        private void handlePossibleIdentifier(TreePath expr, boolean declaration) {
            handlePossibleIdentifier(expr, declaration, null);
        }
        
        private void handlePossibleIdentifier(TreePath expr, boolean declaration, Element decl) {
            if (Utilities.isKeyword(expr.getLeaf())) {
                //ignore keywords:
                return ;
            }

            if (expr.getLeaf().getKind() == Kind.PRIMITIVE_TYPE) {
                //ignore primitive types:
                return ;
            }

            if (LITERALS.contains(expr.getLeaf().getKind())) {
                //ignore literals:
                return ;
            }

            decl = decl == null ? Utilities.toRecordComponent(info.getTrees().getElement(expr)) : decl;

            ElementKind declKind = decl != null ? decl.getKind() : null;
            boolean isDeclType = decl != null &&
                                 (declKind.isClass() || declKind.isInterface());
            TreePath currentPath = getCurrentPath();
            TreePath parent = currentPath.getParentPath();

            //for new <type>(), highlight <type> as a constructor:
            if (isDeclType &&
                parent.getLeaf().getKind() == Kind.NEW_CLASS) {
		decl = info.getTrees().getElement(parent);
	    }

            if (isDeclType &&
                (parent.getLeaf().getKind() == Kind.PARAMETERIZED_TYPE &&
                  ((ParameterizedTypeTree) parent.getLeaf()).getType() == currentPath.getLeaf() &&
                  parent.getParentPath().getLeaf().getKind() == Kind.NEW_CLASS)) {
		decl = info.getTrees().getElement(parent.getParentPath());
	    }

            if (decl == null) {
                return ;
            }

            isDeclType = decl.getKind().isClass() || decl.getKind().isInterface();
            Collection<ColoringAttributes> c = null;

            if (decl.getKind().isField() || isLocalVariableClosure(decl) || decl.getKind() == ElementKind.RECORD_COMPONENT) {
                c = getVariableColoring(decl);
            }
            
            if (decl instanceof ExecutableElement exec) {
                c = getMethodColoring(exec);
            }
            
            if (decl.getKind() == ElementKind.MODULE) {
                c = new ArrayList<>();
                c.add(ColoringAttributes.MODULE);
            }

            if (isDeclType) {
                c = new ArrayList<>();
                addModifiers(decl, c);
                
                switch (decl.getKind()) {
                    case CLASS -> c.add(ColoringAttributes.CLASS);
                    case INTERFACE -> c.add(ColoringAttributes.INTERFACE);
                    case ANNOTATION_TYPE -> c.add(ColoringAttributes.ANNOTATION_TYPE);
                    case ENUM -> c.add(ColoringAttributes.ENUM);
                    case RECORD -> c.add(ColoringAttributes.RECORD);
                }
            }                       
            
            if (declaration) {
                if (c == null) {
                    c = new ArrayList<>();
                }
                c.add(ColoringAttributes.DECLARATION);
            }
            
            if (c != null) {
                if (decl.getKind() == ElementKind.CONSTRUCTOR && !declaration) {
                    if (info.getElements().isDeprecated(decl.getEnclosingElement())) {
                        c.add(ColoringAttributes.DEPRECATED);
                    }
                }
                addUse(decl, declaration, expr, c);
            }
        }
        
        private void addUse(Element decl, boolean declaration, TreePath t, Collection<ColoringAttributes> c) {
            type2Uses.computeIfAbsent(decl, k -> new ArrayList<>())
                     .add(new Use(declaration, t, c));
        }

        @Override
        public Void visitCompilationUnit(CompilationUnitTree tree, Void p) {
	    //ignore package X.Y.Z;:
	    //scan(tree.getPackageDecl(), p);
            tl.moveBefore(tree.getImports());
	    scan(tree.getImports(), p);
            tl.moveBefore(tree.getPackageAnnotations());
	    scan(tree.getPackageAnnotations(), p);
            tl.moveToEnd(tree.getImports());
	    scan(tree.getTypeDecls(), p);
	    return null;
        }

        @Override
        public Void visitModule(ModuleTree tree, Void p) {
            tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));
            scan(tree.getAnnotations(), p);
            tl.moveToEnd(tree.getAnnotations());
            if (tree.getModuleType() == ModuleTree.ModuleKind.OPEN) {
                Token t = firstIdentifierToken("open"); //NOI18N
                if (t != null) {
                    contextKeywords.add(t);
                }
                tl.moveNext();
            }
            Token t = firstIdentifierToken("module"); //NOI18N
            if (t != null) {
                contextKeywords.add(t);
            }
            Element e = info.getTrees().getElement(getCurrentPath());
            if (e != null && e.getKind() == ElementKind.MODULE) {
                handlePossibleIdentifier(new TreePath(getCurrentPath(), tree.getName()), true, e);
                tl.moduleNameHere(tree.getName(), tree2Tokens);
            }
            scan(tree.getDirectives(), p);
            return null;
        }

        @Override
        public Void visitExports(ExportsTree tree, Void p) {
            tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));
            Token t = firstIdentifierToken("exports"); //NOI18N
            if (t != null) {
                contextKeywords.add(t);
            }
            scan(tree.getPackageName(), p);
            tl.moveToOffset(sourcePositions.getEndPosition(info.getCompilationUnit(), tree.getPackageName()));
            t = firstIdentifierToken("to"); //NOI18N
            if (t != null) {
                contextKeywords.add(t);
            }
            return scan(tree.getModuleNames(), p);
        }

        @Override
        public Void visitOpens(OpensTree tree, Void p) {
            tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));
            Token t = firstIdentifierToken("opens"); //NOI18N
            if (t != null) {
                contextKeywords.add(t);
            }
            scan(tree.getPackageName(), p);
            tl.moveToOffset(sourcePositions.getEndPosition(info.getCompilationUnit(), tree.getPackageName()));
            t = firstIdentifierToken("to"); //NOI18N
            if (t != null) {
                contextKeywords.add(t);
            }
            return scan(tree.getModuleNames(), p);
        }

        @Override
        public Void visitProvides(ProvidesTree tree, Void p) {
            tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));
            Token t = firstIdentifierToken("provides"); //NOI18N
            if (t != null) {
                contextKeywords.add(t);
            }
            scan(tree.getServiceName(), p);
            tl.moveToOffset(sourcePositions.getEndPosition(info.getCompilationUnit(), tree.getServiceName()));
            t = firstIdentifierToken("with"); //NOI18N
            if (t != null) {
                contextKeywords.add(t);
            }
            return scan(tree.getImplementationNames(), p);
        }

        @Override
        public Void visitRequires(RequiresTree tree, Void p) {
            tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));
            Token t = firstIdentifierToken("requires"); //NOI18N
            if (t != null) {
                contextKeywords.add(t);
                tl.moveNext();
                if (tree.isStatic() && tree.isTransitive()) {
                    t = firstIdentifierToken("static", "transitive"); //NOI18N
                    if (t != null) {
                        contextKeywords.add(t);
                    }
                    tl.moveNext();
                    t = firstIdentifierToken("static", "transitive"); //NOI18N
                    if (t != null) {
                        contextKeywords.add(t);
                    }
                } else if (tree.isStatic()) {
                    t = firstIdentifierToken("static"); //NOI18N
                    if (t != null) {
                        contextKeywords.add(t);
                    }
                } else if (tree.isTransitive()) {
                    t = firstIdentifierToken("transitive"); //NOI18N
                    if (t != null) {
                        contextKeywords.add(t);
                    }
                }
            }
            return super.visitRequires(tree, p);
        }

        @Override
        public Void visitCase(CaseTree node, Void p) {
            int restartIndex = tl.index();
            tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), node));
            List<? extends CaseLabelTree> labels = node.getLabels();
            for (CaseLabelTree labelTree : labels) {
                if (labelTree.getKind() == Tree.Kind.PATTERN_CASE_LABEL) {
                    PatternCaseLabelTree patternLabel = (PatternCaseLabelTree) labelTree;
                    tl.moveToOffset(sourcePositions.getEndPosition(info.getCompilationUnit(), patternLabel.getPattern()));
                    tl.moveNext();
                    if (tl.currentToken() != null && TokenUtilities.equals(tl.currentToken().text(), "when")) {      //NOI18N
                        contextKeywords.add(tl.currentToken());
                    }
                }
            }
            tl.resetToIndex(restartIndex);
            return super.visitCase(node, p);
        }

        @Override
        public Void visitUses(UsesTree tree, Void p) {
            tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));
            Token t = firstIdentifierToken("uses"); //NOI18N
            if (t != null) {
                contextKeywords.add(t);
            }
            return super.visitUses(tree, p);
        }
                
        @Override
        public Void visitMethodInvocation(MethodInvocationTree tree, Void p) {
            int startTokenIndex = tl.index();
            Tree possibleIdent = tree.getMethodSelect();
            
            if (possibleIdent.getKind() == Kind.IDENTIFIER) {
                //handle "this" and "super" constructors:
                String ident = ((IdentifierTree) possibleIdent).getName().toString();
                
                if ("super".equals(ident) || "this".equals(ident)) { //NOI18N
                    Element resolved = info.getTrees().getElement(getCurrentPath());
                    
                    addUse(resolved, false, null, null);
                }
            }
            
            List<? extends Tree> ta = tree.getTypeArguments();
            long afterTypeArguments = ta.isEmpty() ? -1 : info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), ta.get(ta.size() - 1));
            
            switch (tree.getMethodSelect().getKind()) {
                case IDENTIFIER:
                case MEMBER_SELECT:
                    memberSelectBypass = afterTypeArguments;
                    scan(tree.getMethodSelect(), p);
                    memberSelectBypass = -1;
                    break;
                default:
                    //todo: log
                    scan(tree.getMethodSelect(), p);
            }

            //the type arguments are before the last identifier in the select, so we should return there:
            //not very efficient, though:
            tl.moveBefore(tree.getTypeArguments());
            
            scan(tree.getTypeArguments(), null);
            
            scan(tree.getArguments(), p);
            
            addParameterInlineHint(tree);

            Tree parent = getCurrentPath().getParentPath().getLeaf();
            Tree parentParent = getCurrentPath().getParentPath().getParentPath().getLeaf();

            if (parent.getKind() != Kind.MEMBER_SELECT ||
                parentParent.getKind() != Kind.METHOD_INVOCATION ||
                ((MemberSelectTree) parent).getExpression() != tree) {
                int afterInvocation = tl.index();
                tl.resetToIndex(startTokenIndex);
                addChainedTypes(getCurrentPath());
                tl.resetToIndex(afterInvocation);
            }

            return null;
        }

        private void addChainedTypes(TreePath current) {
            if (!settings.javaInlineHintChainedTypes) {
                return;
            }
            List<TreePath> chain = new ArrayList<>(); //TODO: avoid creating an instance if possible!
            OUTER: while (true) {
                chain.add(current);
                switch (current.getLeaf().getKind()) {
                    case METHOD_INVOCATION:
                        MethodInvocationTree mit = (MethodInvocationTree) current.getLeaf();
                        if (mit.getMethodSelect().getKind() == Kind.MEMBER_SELECT) {
                            current = new TreePath(new TreePath(current, mit.getMethodSelect()), ((MemberSelectTree) mit.getMethodSelect()).getExpression());
                            break;
                        }
                        break OUTER;
                    default:
                        break OUTER;
                }
            }
            Collections.reverse(chain);
            List<Pair<String, Integer>> typeToPosition = new ArrayList<>();
            List<Pair<String, Integer>> forcedTypeToPosition = new ArrayList<>();
            for (TreePath tp : chain) {
                long end = info.getTrees().getSourcePositions().getEndPosition(tp.getCompilationUnit(), tp.getLeaf());
                tl.moveToOffset(end);
                Token t = tl.currentToken();
                if (t != null && (t.id() == JavaTokenId.COMMA || t.id() == JavaTokenId.SEMICOLON)) {
                    tl.moveNext();
                    t = tl.currentToken();
                } else if (t != null && t.id() == JavaTokenId.RPAREN) {
                    while (t != null && t.id() == JavaTokenId.RPAREN) {
                        tl.moveNext();
                        t = tl.currentToken();
                    }
                    if (t != null && (t.id() == JavaTokenId.COMMA || t.id() == JavaTokenId.SEMICOLON)) {
                        tl.moveNext();
                        t = tl.currentToken();
                    }
                }
                int pos;
                if (t != null && t.id() == JavaTokenId.WHITESPACE && (pos = t.text().toString().indexOf("\n")) != -1) {
                    TypeMirror type = info.getTrees().getTypeMirror(tp);
                    String typeName;
                    if (type.getKind().isPrimitive() || type.getKind() == TypeKind.DECLARED) {
                        typeName = info.getTypeUtilities().getTypeName(type).toString();
                    } else {
                        typeName = "";
                    }
                    int preTextPos = tl.offset() + pos;
                    if (typeToPosition.isEmpty() || !typeName.equals(typeToPosition.get(typeToPosition.size() - 1).first()) || preText.containsKey(preTextPos)) {
                        typeToPosition.add(Pair.of(typeName, preTextPos));
                    }
                    if (preText.containsKey(preTextPos)) {
                        forcedTypeToPosition.add(Pair.of(typeName, preTextPos));
                    }
                }
            }
            if (typeToPosition.size() >= 2) {
                for (Pair<String, Integer> typeAndPosition : typeToPosition) {
                    preText.compute(typeAndPosition.second(),
                                    (p, n) -> (n == null ? " " : ";" ) + " " + typeAndPosition.first());
                }
            } else {
                for (Pair<String, Integer> typeAndPosition : forcedTypeToPosition) {
                    preText.compute(typeAndPosition.second(),
                                    (p, n) -> (n == null ? " " : n + ";" ) + " " + typeAndPosition.first());
                }
            }
        }

        @Override
        public Void visitExpressionStatement(ExpressionStatementTree node, Void p) {
            return super.visitExpressionStatement(node, p);
        }

        @Override
        public Void visitIdentifier(IdentifierTree tree, Void p) {
            if (info.getTreeUtilities().isSynthetic(getCurrentPath()))
                return null;

            tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));
            
            if (memberSelectBypass != (-1)) {
                tl.moveToOffset(memberSelectBypass);
                memberSelectBypass = -1;
            }
            
            tl.identifierHere(tree, tree2Tokens);
            
            handlePossibleIdentifier(getCurrentPath(), false);
            addParameterInlineHint(tree);
            super.visitIdentifier(tree, null);
            return null;
        }

        @Override
        public Void visitMethod(MethodTree tree, Void p) {
            if (info.getTreeUtilities().isSynthetic(getCurrentPath())) {
                return super.visitMethod(tree, p);
            }

            //#170338: constructor without modifiers:
            tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));

            handlePossibleIdentifier(getCurrentPath(), true);
            
            Element el = info.getTrees().getElement(getCurrentPath());
            
            scan(tree.getModifiers(), null);
            tl.moveToEnd(tree.getModifiers());
            scan(tree.getTypeParameters(), null);
            tl.moveToEnd(tree.getTypeParameters());
            scan(tree.getReturnType(), p);
            tl.moveToEnd(tree.getReturnType());
            
            String name;
            
            if (tree.getReturnType() != null) {
                //method:
                name = tree.getName().toString();
            } else {
                //constructor:
                TreePath tp = getCurrentPath();
                
                while (tp != null && !TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
                    tp = tp.getParentPath();
                }
                
                if (tp != null && TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
                    name = ((ClassTree) tp.getLeaf()).getSimpleName().toString();
                } else {
                    name = null;
                }
            }
            
            if (name != null) {
                firstIdentifier(name);
            }
            
            scan(tree.getParameters(), null);
            scan(tree.getThrows(), null);
            scan(tree.getDefaultValue(), null);

            recursionDetector = (el != null && el.getKind() == ElementKind.METHOD) ? (ExecutableElement) el : null;
            
            scan(tree.getBody(), null);

            recursionDetector = null;
        
            return null;
        }

        @Override
        public Void visitVariable(VariableTree tree, Void p) {
            if (info.getTreeUtilities().isSynthetic(getCurrentPath()))
                return null;

            tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));

            handlePossibleIdentifier(getCurrentPath(), true);
            
            scan(tree.getModifiers(), null);
            
            tl.moveToEnd(tree.getModifiers());
            
            scan(tree.getType(), null);
            
            int[] span = info.getTreeUtilities().findNameSpan(tree);
            if (span != null)
                tl.moveToOffset(span[0]);
            else
                tl.moveToEnd(tree.getType());
            
            firstIdentifier(tree.getName().toString());
            
            tl.moveNext();
            
            if (info.getTreeUtilities().isVarType(getCurrentPath()) && settings.javaInlineHintVarType) {
                int afterName = tl.offset();
                TypeMirror type = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), tree.getType()));

                this.preText.put(afterName, " : " + info.getTypeUtilities().getTypeName(type));
            }

            scan(tree.getInitializer(), p);
            
            return null;
        }
        
        @Override
        public Void visitNewClass(NewClassTree tree, Void p) {
            TreePath tp;
            Tree ident = tree.getIdentifier();
            
            if (ident.getKind() == Kind.PARAMETERIZED_TYPE) {
                tp = new TreePath(new TreePath(getCurrentPath(), ident), ((ParameterizedTypeTree) ident).getType());
            } else {
                tp = new TreePath(getCurrentPath(), ident);
            }
            
            Element clazz = info.getTrees().getElement(tp);
            
            if (clazz != null) {
                addUse(clazz, false, null, null);
            }
	    
            scan(tree.getEnclosingExpression(), null);
            scan(tree.getIdentifier(), null);
            scan(tree.getTypeArguments(), null);
            scan(tree.getArguments(), p);
            scan(tree.getClassBody(), null);
            
            return null;
        }

        @Override
        public Void visitClass(ClassTree tree, Void p) {
            tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));
            
            handlePossibleIdentifier(getCurrentPath(), true);
            
            scan(tree.getModifiers(), null);
            
            tl.moveToEnd(tree.getModifiers());
            boolean record = false;
            Token recordToken = tl.firstIdentifier(getCurrentPath(), "record");
            if (recordToken != null) {
                contextKeywords.add(recordToken);
                tl.moveNext();
                record = true;
            }
            firstIdentifier(tree.getSimpleName().toString());

            //XXX:????
            scan(tree.getTypeParameters(), null);
            if (record) {
                scan(tree.getMembers().stream().filter(m -> isRecordComponent(m)).toList(), null);
            }
            scan(tree.getExtendsClause(), null);
            scan(tree.getImplementsClause(), null);
            try {
                List<? extends Tree> permitList = tree.getPermitsClause();
                if (permitList != null && !permitList.isEmpty()) {
                    tl.moveNext();
                    Token t = firstIdentifierToken("permits");// NOI18N
                    if (tl != null) {
                        contextKeywords.add(t);
                        scan(permitList, null);
                    }
                }
            } catch (NullPointerException ex) {
                //Do nothing
            }
            ExecutableElement prevRecursionDetector = recursionDetector;

            recursionDetector = null;
            
            if (record) {
                scan(tree.getMembers().stream().filter(m -> !isRecordComponent(m)).toList(), null);
            } else {
                scan(tree.getMembers(), null);
            }

            recursionDetector = prevRecursionDetector;
            
            //XXX: end ???
            
            return null;
        }
        
        private boolean isRecordComponent(Tree member) {
            Element el = info.getTrees().getElement(new TreePath(getCurrentPath(), member));
            return el != null && Utilities.toRecordComponent(el).getKind() == ElementKind.RECORD_COMPONENT;
        }

        @Override
        public Void visitMemberReference(MemberReferenceTree node, Void p) {
            scan(node.getQualifierExpression(), p);
            tl.moveToEnd(node.getQualifierExpression());
            scan(node.getTypeArguments(), null);
            tl.moveToEnd(node.getTypeArguments());
            handlePossibleIdentifier(getCurrentPath(), false);
            firstIdentifier(node.getName().toString());
            return null;
        }

        private static final Coloring UNINDENTED_TEXT_BLOCK =
                ColoringAttributes.add(ColoringAttributes.empty(), ColoringAttributes.UNINDENTED_TEXT_BLOCK);

        @Override
        public Void visitLiteral(LiteralTree node, Void p) {
            int startPos = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), node);
            tl.moveToOffset(startPos);
            Token t = tl.currentToken();
            if (t != null && t.id() == JavaTokenId.MULTILINE_STRING_LITERAL && t.partType() == PartType.COMPLETE) {
                String tokenText = t.text().toString();
                String[] lines = tokenText.split("\n");
                int indent = Arrays.stream(lines, 1, lines.length)
                                   .filter(l -> !l.isBlank())
                                   .mapToInt(this::leadingIndent)
                                   .min()
                                   .orElse(0);
                int pos = startPos + lines[0].length() + 1;
                for (int i = 1; i < lines.length; i++) {
                    String line = lines[i];
                    if (i == lines.length - 1) {
                        line = line.substring(0, line.length() - 3);
                    }
                    String strippendLine = line.replaceAll("[\t ]+$", "");
                    int indentedStart = pos + indent;
                    int indentedEnd = pos + strippendLine.length();
                    if (indentedEnd > indentedStart)
                        extraColoring.add(Pair.of(new int[] {indentedStart, indentedEnd}, UNINDENTED_TEXT_BLOCK));
                    pos += line.length() + 1;
                }
            }

            addParameterInlineHint(node);
            return super.visitLiteral(node, p);
        }

        @Override
        public Void scan(Tree tree, Void p) {
            if (tree != null && tree.getKind() == Kind.YIELD) {
                tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));
                Token t = firstIdentifierToken("yield"); //NOI18N
                if (t != null) {
                    contextKeywords.add(t);
                }
            } else if (tree != null && tree.getKind() == Kind.MODIFIERS) {
               visitModifier(tree);
            }
            return super.scan(tree, p);
        }

        private void visitModifier(Tree tree) {
            tl.moveToOffset(sourcePositions.getStartPosition(info.getCompilationUnit(), tree));
            Token t = null;
            if (tree.toString().contains("non-sealed")) {// NOI18N
                Token firstIdentifier = tl.firstIdentifier(getCurrentPath(), "non");// NOI18N
                if (firstIdentifier != null) {
                    contextKeywords.add(firstIdentifier);
                }
                tl.moveNext();
                tl.moveNext();
                if (TokenUtilities.textEquals(tl.currentToken().text(), "sealed")) {// NOI18N
                    contextKeywords.add(tl.currentToken());
                }
            } else if (tree.toString().contains("sealed")) {// NOI18N
                t = firstIdentifierToken("sealed"); //NOI18N
                if (t != null) {
                    contextKeywords.add(t);
                }
            }
        }

        private int leadingIndent(String line) {
            int indent = 0;

            for (int i = 0; i < line.length(); i++) { //TODO: code points
                if (Character.isWhitespace(line.charAt(i)))
                    indent++;
                else
                    break;
            }

            return indent;
        }

        private void addParameterInlineHint(Tree tree) {
            if (!settings.javaInlineHintParameterName) {
                return;
            }
            TreePath pp = getCurrentPath().getParentPath();
            Tree leaf = pp.getLeaf();
            if (leaf != null &&
                (leaf.getKind() == Kind.METHOD_INVOCATION || leaf.getKind() == Kind.NEW_CLASS)) {
                int pos = -1;
                if (leaf.getKind() == Kind.METHOD_INVOCATION) {
                    pos = MethodInvocationTree.class.cast(leaf).getArguments().indexOf(tree);
                } else if (leaf.getKind() == Kind.NEW_CLASS) {
                    pos = NewClassTree.class.cast(leaf).getArguments().indexOf(tree);
                }
                if (pos != (-1)) {
                    Element invoked = info.getTrees().getElement(pp);
                    if (invoked != null && (invoked.getKind() == ElementKind.METHOD || invoked.getKind() == ElementKind.CONSTRUCTOR)) {
                        long start = sourcePositions.getStartPosition(info.getCompilationUnit(), tree);
                        ExecutableElement invokedMethod = (ExecutableElement) invoked;
                        pos = Math.min(pos, invokedMethod.getParameters().size() - 1);
                        if (pos != (-1)) {
                            boolean shouldBeAdded = true;
                            if (tree.getKind() == Kind.IDENTIFIER &&
                                    invokedMethod.getParameters().get(pos).getSimpleName().equals(
                                            IdentifierTree.class.cast(tree).getName())) {
                                shouldBeAdded = false;
                            }
                            if (shouldBeAdded) {
                                preText.put((int) start,
                                            invokedMethod.getParameters().get(pos).getSimpleName() + ":");
                            }
                        }
                    }
                }
            }
        }
    }

    public static interface ErrorDescriptionSetter {
        
        public void setHighlights(Document doc, Collection<Pair<int[], Coloring>> highlights, Map<int[], String> preText);
        public void setColorings(Document doc, Map<Token, Coloring> colorings);
    }

    public record Settings(boolean javaInlineHintParameterName, boolean javaInlineHintChainedTypes, boolean javaInlineHintVarType) {

        private static final Map<String, Boolean> DEFAULT_VALUES = Map.of(
                JAVA_INLINE_HINT_PARAMETER_NAME, true,
                JAVA_INLINE_HINT_CHAINED_TYPES, false,
                JAVA_INLINE_HINT_VAR_TYPE, false
        );

        public static Settings getDefault() {
            Preferences preferences = NbPreferences.root().node("/org/netbeans/modules/java/editor/InlineHints/default");
            return new Settings(preferences.getBoolean(JAVA_INLINE_HINT_PARAMETER_NAME, DEFAULT_VALUES.get(JAVA_INLINE_HINT_PARAMETER_NAME)),
                                preferences.getBoolean(JAVA_INLINE_HINT_CHAINED_TYPES, DEFAULT_VALUES.get(JAVA_INLINE_HINT_CHAINED_TYPES)),
                                preferences.getBoolean(JAVA_INLINE_HINT_VAR_TYPE, DEFAULT_VALUES.get(JAVA_INLINE_HINT_VAR_TYPE)));
        }

    }
}
