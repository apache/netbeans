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
package org.netbeans.modules.java.hints.introduce;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.matching.Matcher;
import org.netbeans.api.java.source.matching.Occurrence;
import org.netbeans.api.java.source.matching.Pattern;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.api.java.source.support.SelectionAwareJavaSourceTaskFactory;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.modules.java.hints.errors.CreateElementUtilities;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Union2;


/**
 *
 * @author Jan Lahoda
 */
public class IntroduceHint implements CancellableTask<CompilationInfo> {

    private AtomicBoolean cancel = new AtomicBoolean();

    public IntroduceHint() {
    }

    private static final Set<TypeKind> NOT_ACCEPTED_TYPES = EnumSet.of(TypeKind.ERROR, TypeKind.NONE, TypeKind.OTHER, TypeKind.VOID, TypeKind.EXECUTABLE);

    static TreePath validateSelection(CompilationInfo ci, int start, int end) {
        return validateSelection(ci, start, end, NOT_ACCEPTED_TYPES);
    }

    public static TreePath validateSelection(CompilationInfo ci, int start, int end, Set<TypeKind> ignoredTypes) {
        int[] span = TreeUtils.ignoreWhitespaces(ci, Math.min(start, end), Math.max(start, end));

        start = span[0];
        end   = span[1];
        
        TreePath tp = ci.getTreeUtilities().pathFor((start + end) / 2 + 1);

        for ( ; tp != null; tp = tp.getParentPath()) {
            Tree leaf = tp.getLeaf();

            if (   !ExpressionTree.class.isAssignableFrom(leaf.getKind().asInterface())
                && (leaf.getKind() != Kind.VARIABLE || ((VariableTree) leaf).getInitializer() == null))
               continue;

            long treeStart = ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), leaf);
            long treeEnd   = ci.getTrees().getSourcePositions().getEndPosition(ci.getCompilationUnit(), leaf);

            if (treeStart != start || treeEnd != end) {
                continue;
            }

            TypeMirror type = ci.getTrees().getTypeMirror(tp);

            if (type != null && type.getKind() == TypeKind.ERROR) {
                type = ci.getTrees().getOriginalType((ErrorType) type);
            }

            if (type == null || ignoredTypes.contains(type.getKind()))
                continue;

            if(tp.getLeaf().getKind() == Kind.ASSIGNMENT)
                continue;

            if (tp.getLeaf().getKind() == Kind.ANNOTATION)
                continue;

            if (!TreeUtils.isInsideClass(tp))
                return null;

            TreePath candidate = tp;

            tp = tp.getParentPath();

            while (tp != null) {
                switch (tp.getLeaf().getKind()) {
                    case VARIABLE:
                        VariableTree vt = (VariableTree) tp.getLeaf();
                        if (vt.getInitializer() == leaf) {
                            return candidate;
                        } else {
                            return null;
                        }
                    case NEW_CLASS:
                        NewClassTree nct = (NewClassTree) tp.getLeaf();
                        
                        if (nct.getIdentifier().equals(candidate.getLeaf())) { //avoid disabling hint ie inside of anonymous class higher in treepath
                            for (Tree p : nct.getArguments()) {
                                if (p == leaf) {
                                    return candidate;
                                }
                            }

                            return null;
                        }
                }

                leaf = tp.getLeaf();
                tp = tp.getParentPath();
            }

            return candidate;
        }

        return null;
    }

    public void run(CompilationInfo info) {
        cancel.set(false);

        FileObject file = info.getFileObject();
        int[] selection = SelectionAwareJavaSourceTaskFactory.getLastSelection(file);

        if (selection == null) {
            //nothing to do....
            HintsController.setErrors(info.getFileObject(), IntroduceHint.class.getName(), Collections.<ErrorDescription>emptyList());
        } else {
            HintsController.setErrors(info.getFileObject(), IntroduceHint.class.getName(), computeError(info, selection[0], selection[1], null, new EnumMap<IntroduceKind, String>(IntroduceKind.class), cancel));

            Document doc = info.getSnapshot().getSource().getDocument(false);

            if (doc != null) {
                PositionRefresherHelperImpl.setVersion(doc, selection[0], selection[1]);
            }
        }
    }

    public void cancel() {
        cancel.set(true);
    }
    
    private static void addExpressionFixes(CompilationInfo info, int start, int end, List<Fix> fixes, Map<IntroduceKind, Fix> fixesMap, AtomicBoolean cancel) {
        TreePath resolved = validateSelection(info, start, end);
        if (resolved == null) {
            return;
        }
        TypeMirror exprType = info.getTrees().getTypeMirror(resolved);
        if (!Utilities.isValidType(exprType)) {
            // do not attempt to introduce ill-typed variables, constant or parameters.
            return;
        }
        TreePathHandle h = TreePathHandle.create(resolved, info);
        TreePath method   = TreeUtils.findMethod(resolved);
        boolean variableRewrite = resolved.getLeaf().getKind() == Kind.VARIABLE;
        TreePath value = !variableRewrite ? resolved : new TreePath(resolved, ((VariableTree) resolved.getLeaf()).getInitializer());
        boolean isVariable = TreeUtils.findStatement(resolved) != null && method != null && !variableRewrite;
        Set<TreePath> duplicatesForVariable = isVariable ? SourceUtils.computeDuplicates(info, resolved, method, cancel) : null;
        Set<TreePath> duplicatesForConstant = /*isConstant ? */SourceUtils.computeDuplicates(info, resolved, new TreePath(info.getCompilationUnit()), cancel);// : null;

        Scope scope = info.getTrees().getScope(resolved);
        boolean statik = scope != null ? info.getTreeUtilities().isStaticContext(scope) : false;
        String guessedName = org.netbeans.modules.editor.java.Utilities.varNameSuggestion(resolved.getLeaf());
        if (guessedName == null) guessedName = "name"; // NOI18N
        Scope s = info.getTrees().getScope(resolved);
        CodeStyle cs = CodeStyle.getDefault(info.getFileObject());
        Fix variable = isVariable ? new IntroduceVariableFix(h, info.getSnapshot().getSource(),
                variableRewrite ? guessedName : Utilities.makeNameUnique(info, s, guessedName, cs.getLocalVarNamePrefix(), cs.getLocalVarNameSuffix()),
                duplicatesForVariable.size() + 1, IntroduceKind.CREATE_VARIABLE, TreePathHandle.create(method, info), end) : null;
        Fix constant = IntroduceConstantFix.createConstant(resolved, info, value, guessedName, duplicatesForConstant.size() + 1, end, variableRewrite, cancel);


        Fix parameter = isVariable ? new IntroduceParameterFix(h) : null;
        IntroduceFieldFix field = null;
        IntroduceFixBase methodFix = null;

        TreePath pathToClass = TreeUtils.findClass(resolved);
        if (method != null && !TreeUtils.isInAnnotationType(info, method)) {
            int[] initilizeIn = computeInitializeIn(info, resolved, duplicatesForConstant);

            if (statik) {
                initilizeIn[0] &= ~IntroduceFieldPanel.INIT_CONSTRUCTORS;
                initilizeIn[1] &= ~IntroduceFieldPanel.INIT_CONSTRUCTORS;
            }

            boolean allowFinalInCurrentMethod = false;

            if (TreeUtils.isConstructor(info, method)) {
                //how many constructors do we have in the target class?:
                allowFinalInCurrentMethod = TreeUtils.findConstructors(info, method).size() == 1;
            }

            if (resolved.getLeaf().getKind() == Kind.VARIABLE) {
                //the variable name would incorrectly clash with itself:
                guessedName = Utilities.guessName(info, resolved, resolved.getParentPath(), cs.getFieldNamePrefix(), cs.getFieldNameSuffix());
            } else if (!variableRewrite) {
                if (pathToClass != null) { //XXX: should actually produce two different names: one when replacing duplicates, one when not replacing them
                    guessedName = Utilities.makeNameUnique(info,
                                                           info.getTrees().getScope(pathToClass),
                                                           guessedName,
                                                           statik ? cs.getStaticFieldNamePrefix() : cs.getFieldNamePrefix(),
                                                           statik ? cs.getStaticFieldNameSuffix() : cs.getFieldNameSuffix());
                }
            }
            Element el = info.getTrees().getElement(pathToClass);
            if (pathToClass != null && el != null && (el.getKind().isClass() || el.getKind().isInterface())) {
                field = new IntroduceFieldFix(h, info.getSnapshot().getSource(), guessedName, duplicatesForConstant.size() + 1, initilizeIn,
                        statik, allowFinalInCurrentMethod, end, !variableRewrite, TreePathHandle.create(pathToClass, info));
            }

            if (!variableRewrite) {
                //introduce method based on expression:
                Map<TypeMirror, TreePathHandle> typeVar2Def = new HashMap<TypeMirror, TreePathHandle>();
                List<TreePathHandle> typeVars = new LinkedList<TreePathHandle>();

                prepareTypeVars(method, info, typeVar2Def, typeVars);

                ScanStatement scanner = new ScanStatement(info, resolved.getLeaf(), resolved.getLeaf(), typeVar2Def, Collections.<Tree, Iterable<? extends TreePath>>emptyMap(), cancel);
                Element methodEl = info.getTrees().getElement(method);
                if (methodEl != null && (methodEl.getKind() == ElementKind.METHOD || methodEl.getKind() == ElementKind.CONSTRUCTOR)) {
                    ExecutableElement ee = (ExecutableElement) methodEl;

                    scanner.localVariables.addAll(ee.getParameters());
                }

                scanner.scan(method, null);

                List<TreePathHandle> params = new LinkedList<TreePathHandle>();

                boolean error186980 = false;
                for (VariableElement ve : scanner.usedLocalVariables.keySet()) {
                    TreePath path = info.getTrees().getPath(ve);
                    if (path == null) {
                        error186980 = true;
                        Logger.getLogger(IntroduceHint.class.getName()).warning("Cannot get TreePath for local variable " + ve + "\nfile=" + info.getFileObject().getPath());
                    } else {
                        params.add(TreePathHandle.create(path, info));
                    }
                }

                if (!error186980) {
                    Set<TypeMirror> exceptions = new HashSet<TypeMirror>(info.getTreeUtilities().getUncaughtExceptions(resolved));

                    Set<TypeMirrorHandle> exceptionHandles = new HashSet<TypeMirrorHandle>();

                    for (TypeMirror tm : exceptions) {
                        exceptionHandles.add(TypeMirrorHandle.create(tm));
                    }

                    Pattern p = Pattern.createPatternWithRemappableVariables(resolved, scanner.usedLocalVariables.keySet(), true);
                    Collection<? extends Occurrence> duplicates = Matcher.create(info).setCancel(cancel).match(p);
                    int duplicatesCount = duplicates.size();

                    typeVars.retainAll(scanner.usedTypeVariables);

                    AtomicBoolean allIfaces = new AtomicBoolean();
                     List<TargetDescription> viableTargets = IntroduceExpressionBasedMethodFix.computeViableTargets(info, resolved.getParentPath(), 
                            Collections.singleton(resolved.getLeaf()), duplicates, cancel, allIfaces);
                    if (viableTargets != null && !viableTargets.isEmpty()) {
                        TypeMirror returnType = 
                                Utilities.convertIfAnonymous(Utilities.resolveCapturedType(info, 
                                        resolveType(info, resolved)));
                        if (Utilities.isValidType(returnType)) {
                            methodFix = new IntroduceExpressionBasedMethodFix(info.getSnapshot().getSource(), h, params, TypeMirrorHandle.create(returnType),
                                    exceptionHandles, duplicatesCount, typeVars, end, viableTargets);
                            methodFix.setTargetIsInterface(allIfaces.get());
                        }
                    }
                }
            }
        }
        if (fixesMap != null) {
            fixesMap.put(IntroduceKind.CREATE_VARIABLE, variable);
            fixesMap.put(IntroduceKind.CREATE_CONSTANT, constant);
            fixesMap.put(IntroduceKind.CREATE_FIELD, field);
            fixesMap.put(IntroduceKind.CREATE_METHOD, methodFix);
            fixesMap.put(IntroduceKind.CREATE_PARAMETER, parameter);
        }


        if (variable != null) {
            fixes.add(variable);
        }

        if (constant != null) {
            fixes.add(constant);
        }

        if (field != null) {
            fixes.add(field);
        }

        if (methodFix != null) {
            fixes.add(methodFix);
        }
        if (parameter != null) {
            fixes.add(parameter);
        } 
    }

    public static List<ErrorDescription> computeError(CompilationInfo info, int start, int end, Map<IntroduceKind, Fix> fixesMap, Map<IntroduceKind, String> errorMessage, AtomicBoolean cancel) {
        List<ErrorDescription> hints = new LinkedList<ErrorDescription>();
        List<Fix> fixes = new LinkedList<Fix>();

        addExpressionFixes(info, start, end, fixes, fixesMap, cancel);
        Fix introduceMethod = IntroduceMethodFix.computeIntroduceMethod(info, start, end, errorMessage, cancel);

        if (introduceMethod != null) {
            fixes.add(introduceMethod);
            if (fixesMap != null) {
                // TODO: replaces previous version of method fix, but retains it in fixes list ?
                fixesMap.put(IntroduceKind.CREATE_METHOD, introduceMethod);
            }
        }

        if (!fixes.isEmpty()) {
            int pos = CaretAwareJavaSourceTaskFactory.getLastPosition(info.getFileObject());
            String displayName = NbBundle.getMessage(IntroduceHint.class, "HINT_Introduce");

            hints.add(ErrorDescriptionFactory.createErrorDescription(Severity.HINT, displayName, fixes, info.getFileObject(), pos, pos));
        }

        return hints;
    }

    static final Set<ElementKind> LOCAL_VARIABLES = EnumSet.of(ElementKind.EXCEPTION_PARAMETER, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER, ElementKind.RESOURCE_VARIABLE);

    private static int[] computeInitializeIn(final CompilationInfo info, TreePath firstOccurrence, Set<TreePath> occurrences) {
        int[] result = new int[] {7, 7};
        boolean inOneMethod = true;
        Tree currentMethod = TreeUtils.findMethod(firstOccurrence).getLeaf();

        for (TreePath occurrence : occurrences) {
            TreePath method = TreeUtils.findMethod(occurrence);

            if (method == null || currentMethod != method.getLeaf()) {
                inOneMethod = false;
                break;
            }
        }

        class Result extends RuntimeException {
            @Override
            public synchronized Throwable fillInStackTrace() {
                return null;
            }

        }
        class ReferencesLocalVariable extends ErrorAwareTreePathScanner<Void, Void> {
            @Override
            public Void visitIdentifier(IdentifierTree node, Void p) {
                Element e = info.getTrees().getElement(getCurrentPath());

                if (e != null && LOCAL_VARIABLES.contains(e.getKind())) {
                    throw new Result();
                }

                return null;
            }
        }

        boolean referencesLocalvariables = false;

        try {
            new ReferencesLocalVariable().scan(firstOccurrence, null);
        } catch (Result r) {
            referencesLocalvariables = true;
        }

        if (!inOneMethod) {
            result[1] = IntroduceFieldPanel.INIT_FIELD | IntroduceFieldPanel.INIT_CONSTRUCTORS;
        }

        if (referencesLocalvariables) {
            result[0] = IntroduceFieldPanel.INIT_METHOD;
            result[1] = IntroduceFieldPanel.INIT_METHOD;
        }

        return result;
    }

    
    // used from IntroduceMethodFix, IntroduceExprBasedMethodFix
    static List<ExpressionTree> realArguments(final TreeMaker make, List<VariableElement> parameters) {
        List<ExpressionTree> realArguments = new LinkedList<ExpressionTree>();

        for (VariableElement p : parameters) {
            realArguments.add(make.Identifier(p.getSimpleName()));
        }

        return realArguments;
    }

    // MethodFix, ExpressionMethodFix
    static List<ExpressionTree> realArgumentsForTrees(final TreeMaker make, List<Union2<VariableElement, TreePath>> parameters) {
        List<ExpressionTree> realArguments = new LinkedList<ExpressionTree>();

        for (Union2<VariableElement, TreePath> p : parameters) {
            if (p.hasFirst()) {
                realArguments.add(make.Identifier(p.first().getSimpleName()));
            } else {
                realArguments.add((ExpressionTree) p.second().getLeaf());
            }
        }

        return realArguments;
    }

    // used by ExpressionBasedMethodFix, MethodFix
    /**
     * Crates method formal parameters, following code style conventions.
     * The trees in 'statements' will be rewritten to use the new identifiers.
     * 
     * @param copy working copy
     * @param parameters variables to turn into parameters
     * @param statements trees that should refer to parameters
     * @return 
     */
    static List<VariableTree> createVariables(WorkingCopy copy, List<VariableElement> parameters, 
            TreePath targetParent,
            List<TreePath> statements) {
        final TreeMaker make = copy.getTreeMaker();
        List<VariableTree> formalArguments = new LinkedList<VariableTree>();
        CodeStyle cs = CodeStyle.getDefault(copy.getFileObject());
        
        String prefix = cs.getParameterNamePrefix();
        String suffix = cs.getParameterNameSuffix(); 
        Map<VariableElement, CharSequence> renamedVariables = new HashMap<VariableElement, CharSequence>();
        Set<Name> changedNames = new HashSet<Name>();
        for (VariableElement p : parameters) {
            TypeMirror tm = p.asType();
            Tree type = make.Type(tm);
            Name formalArgName = p.getSimpleName();
            Set<Modifier> formalArgMods = EnumSet.noneOf(Modifier.class);
            
            if (p.getModifiers().contains(Modifier.FINAL)) {
                formalArgMods.add(Modifier.FINAL);
            }
            String strippedName = Utilities.stripVariableName(cs, p);
            CharSequence codeStyleName = Utilities.guessName(copy, strippedName, targetParent, prefix, suffix, p.getKind() == ElementKind.PARAMETER);
            if (!formalArgName.contentEquals(codeStyleName)) {
                renamedVariables.put(p, codeStyleName);
                changedNames.add(formalArgName);
            } else {
                codeStyleName = formalArgName;
            }
            formalArguments.add(make.Variable(make.Modifiers(formalArgMods), codeStyleName, type, null));
        }
        if (!changedNames.isEmpty()) {
            VariableRenamer renamer = new VariableRenamer(copy, renamedVariables, changedNames);
            for (TreePath stPath : statements) {
                renamer.scan(stPath, null);
            }
        }
        return formalArguments;
    }
    
    static class VariableRenamer extends ErrorAwareTreePathScanner {
        private final Map<VariableElement, CharSequence> renamedVars;
        private final Set<Name> changedNames;
        private final WorkingCopy info;

        public VariableRenamer(WorkingCopy info, Map<VariableElement, CharSequence> renamedVars, Set<Name> changedNames) {
            this.renamedVars = renamedVars;
            this.changedNames = changedNames;
            this.info = info;
        }

        @Override
        public Object visitIdentifier(IdentifierTree node, Object p) {
            if (changedNames.contains(node.getName())) {
                Element e = info.getTrees().getElement(getCurrentPath());
                CharSequence nn = renamedVars.get(e);
                if (nn != null) {
                    info.rewrite(node, info.getTreeMaker().Identifier(nn));
                }
            }
            return super.visitIdentifier(node, p);
        }
    }

    // MethodFix, ExpressionMethodFix
    static List<ExpressionTree> typeHandleToTree(WorkingCopy copy, Set<TypeMirrorHandle> thrownTypes) {
        final TreeMaker make = copy.getTreeMaker();
        List<ExpressionTree> thrown = new LinkedList<ExpressionTree>();

        for (TypeMirrorHandle h : thrownTypes) {
            TypeMirror t = h.resolve(copy);

            if (t == null) {
                return null;
            }

            thrown.add((ExpressionTree) make.Type(t));
        }

        return thrown;
    }

    static final OffsetsBag introduceBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(IntroduceHint.class);

        if (bag == null) {
            doc.putProperty(IntroduceHint.class, bag = new OffsetsBag(doc));
        }

        return bag;
    }

    // MethodFix, ExpressionMethodFix
    static List<VariableElement> resolveVariables(CompilationInfo info, Collection<? extends TreePathHandle> handles) {
        List<VariableElement> vars = new LinkedList<VariableElement>();

        for (TreePathHandle tph : handles) {
            vars.add((VariableElement) tph.resolveElement(info));
        }

        return vars;
    }

    // Used from IntroduceMethodFix + IntroduceExpressionBasedMethodFix (computed by IntroduceHint)
    static void prepareTypeVars(TreePath method, CompilationInfo info, Map<TypeMirror, TreePathHandle> typeVar2Def, List<TreePathHandle> typeVars) throws IllegalArgumentException {
        if (method.getLeaf().getKind() == Kind.METHOD) {
            MethodTree mt = (MethodTree) method.getLeaf();

            for (TypeParameterTree tv : mt.getTypeParameters()) {
                TreePath def = new TreePath(method, tv);
                TypeMirror type = info.getTrees().getTypeMirror(def);

                if (type != null && type.getKind() == TypeKind.TYPEVAR) {
                    TreePathHandle tph = TreePathHandle.create(def, info);

                    typeVar2Def.put(type, tph);
                    typeVars.add(tph);
                }
            }
        }
    }

    // used from IntroduceExpressionBasedMethodFix, IntroduceMethodFix
    static boolean needsStaticRelativeTo(CompilationInfo info, TreePath targetClass, TreePath occurrence) {
        while (occurrence != null && targetClass.getLeaf() != occurrence.getLeaf()) {
            switch (occurrence.getLeaf().getKind()) {
                case METHOD:
                    if (((MethodTree) occurrence.getLeaf()).getModifiers().getFlags().contains(Modifier.STATIC)) {
                        return true;
                    }
                    break;
                case BLOCK:
                    if (((BlockTree) occurrence.getLeaf()).isStatic()) {
                        return true;
                    }
                    break;
                case INTERFACE:
                    return true;
            }

            occurrence = occurrence.getParentPath();
        }

        return false;
    }

    // FieldFix, IntroduceFix / Constant
    static void removeFromParent(WorkingCopy parameter, TreePath what) throws IllegalAccessException {
        final TreeMaker make = parameter.getTreeMaker();
        Tree parentTree = what.getParentPath().getLeaf();
        Tree original = what.getLeaf();
        Tree newParent;

        switch (parentTree.getKind()) {
            case BLOCK:
                newParent = make.removeBlockStatement((BlockTree) parentTree, (StatementTree) original);
                break;
            case CASE:
                newParent = make.removeCaseStatement((CaseTree) parentTree, (StatementTree) original);
                break;
            case CLASS:
            case ENUM:
            case INTERFACE:
                newParent = make.removeClassMember((ClassTree)parentTree, original);
                break;
            default:
                throw new IllegalAccessException(parentTree.getKind().toString());
        }

        parameter.rewrite(parentTree, newParent);
    }
    
    public static TreePath getStatementOrBlock(TreePath firstLeaf) {
        switch (firstLeaf.getParentPath().getLeaf().getKind()) {
            case BLOCK:
            case CASE:
                return firstLeaf.getParentPath();
            default:
                return firstLeaf;
        }
    }
    
    //XXX: duplicate from CopyFinder:
    public static List<? extends StatementTree> getStatements(TreePath firstLeaf) {
        switch (firstLeaf.getParentPath().getLeaf().getKind()) {
            case BLOCK:
                return ((BlockTree) firstLeaf.getParentPath().getLeaf()).getStatements();
            case CASE:
                return ((CaseTree) firstLeaf.getParentPath().getLeaf()).getStatements();
            default:
                return Collections.singletonList((StatementTree) firstLeaf.getLeaf());
        }
    }

    // used by IntroduceFieldFix, IntroduceFix / constant
    private static Map<Tree, TreePath> createTree2TreePathMap(TreePath pathToClass) {
        Map<Tree, TreePath> classNormalization = new IdentityHashMap<Tree, TreePath>();
        TreePath temp = pathToClass;

        while (temp != null) {
            classNormalization.put(temp.getLeaf(), temp);
            temp = temp.getParentPath();
        }

        return classNormalization;
    }

    // used by IntroduceFieldFix, IntroduceFix / constant
    static TreePath findTargetClassWithDuplicates(TreePath pathToClass, Collection<TreePath> duplicates) {
        TreePath targetClassWithDuplicates = pathToClass;
        Map<Tree, TreePath> classNormalization = createTree2TreePathMap(pathToClass);

        for (TreePath p : duplicates) {
            while (p != null) {
                if (classNormalization.containsKey(p.getLeaf())) {
                    classNormalization = createTree2TreePathMap(targetClassWithDuplicates = p);
                    break;
                }
                p = p.getParentPath();
            }
        }

        assert targetClassWithDuplicates != null;

        while (targetClassWithDuplicates != null && !TreeUtilities.CLASS_TREE_KINDS.contains(targetClassWithDuplicates.getLeaf().getKind())) {
            targetClassWithDuplicates = targetClassWithDuplicates.getParentPath();
        }

        if (targetClassWithDuplicates == null) {
            //strange...
            targetClassWithDuplicates = pathToClass;
        }
        
        return targetClassWithDuplicates;
    }
    
    // used in IntroduceFieldFix, IntroduceFix / constant
    static ClassTree insertField(final WorkingCopy parameter, ClassTree clazz, VariableTree fieldToAdd, Set<Tree> allNewUses, int offset) {
        ClassTree nueClass = INSERT_CLASS_MEMBER.insertClassMember(parameter, clazz, fieldToAdd, offset);

        class Contains extends ErrorAwareTreeScanner<Boolean, Set<Tree>> {
            @Override public Boolean reduce(Boolean r1, Boolean r2) {
                return r1 == Boolean.TRUE || r2 == Boolean.TRUE;
            }
            @Override public Boolean scan(Tree tree, Set<Tree> searchFor) {
                if (tree != null && searchFor.contains(tree)) return true;
                return super.scan(tree, searchFor);
            }
        }

        int i = 0;
        int insertLocation = -1;
        boolean newFieldStatic = fieldToAdd.getModifiers().getFlags().contains(Modifier.STATIC);

        for (Tree member : nueClass.getMembers()) {
            i++;
            if (member.getKind() == Kind.VARIABLE) {
                VariableTree field = (VariableTree) member;

                if (   (field.getModifiers().getFlags().contains(Modifier.STATIC) ^ newFieldStatic)
                    || new Contains().scan(field.getInitializer(), allNewUses) != Boolean.TRUE) {
                    continue;
                }
            } else if (member.getKind() == Kind.BLOCK) {
                BlockTree block = (BlockTree) member;

                if (   (block.isStatic() ^ newFieldStatic)
                    || new Contains().scan(block, allNewUses) != Boolean.TRUE) {
                    continue;
                }
            } else if (member == fieldToAdd) {
                break;
            } else {
                continue;
            }

            insertLocation = i - 1;
            break;
        }
        
        // fallback; the individual hinds should have sent correct modifiers.
        if (clazz.getKind() == Tree.Kind.INTERFACE) {
            Set<Modifier> mod = fieldToAdd.getModifiers().getFlags();
            if (!mod.isEmpty()) {
                mod = EnumSet.copyOf(mod);
                EnumSet<Modifier> mods = EnumSet.of(Modifier.PRIVATE, Modifier.PROTECTED);
                if (mod.removeAll(EnumSet.of(Modifier.PRIVATE, Modifier.PROTECTED))) {
                    // offending modifier, plan further rewrite
                    mod.add(Modifier.PUBLIC);
                    ModifiersTree mtt = parameter.getTreeMaker().Modifiers(mod, fieldToAdd.getModifiers().getAnnotations());
                    parameter.rewrite(fieldToAdd.getModifiers(), mtt);
                }
            }
        }
        
        TreePath clazzPath = TreePath.getPath(parameter.getCompilationUnit(), clazz); //TODO: efficiency
        final Set<Element> used = Collections.newSetFromMap(new IdentityHashMap<Element, Boolean>());
        final boolean statik = fieldToAdd.getModifiers().getFlags().contains(Modifier.STATIC);
        
        new ErrorAwareTreePathScanner<Void, Void>() {
            @Override public Void visitIdentifier(IdentifierTree node, Void p) {
                handleCurrentPath();
                return super.visitIdentifier(node, p); //To change body of generated methods, choose Tools | Templates.
            }
            @Override public Void visitMemberSelect(MemberSelectTree node, Void p) {
                handleCurrentPath();
                return super.visitMemberSelect(node, p); //To change body of generated methods, choose Tools | Templates.
            }
            private void handleCurrentPath() {
                Element el = parameter.getTrees().getElement(getCurrentPath());
                
                if (el != null && el.getKind().isField() && el.getModifiers().contains(Modifier.STATIC) == statik) {
                    used.add(el);
                }
            }
        }.scan(new TreePath(clazzPath, fieldToAdd), null);
        
        List<? extends Tree> nueMembers = new ArrayList<Tree>(nueClass.getMembers());
        
        Collections.reverse(nueMembers);
        
        i = nueMembers.size() - 1;
        for (Tree member : nueMembers) {
            Element el = parameter.getTrees().getElement(new TreePath(clazzPath, member));
            
            if (el != null && used.contains(el)) {
                insertLocation = i;
                break;
            }
            
            i--;
            
            if (member == fieldToAdd || i < insertLocation)
                break;
        }

        if (insertLocation != (-1))
            nueClass = parameter.getTreeMaker().insertClassMember(clazz, insertLocation, fieldToAdd);

        return nueClass;
    }
    
    // used by IntroduceFix, IntroduceFieldFix, IntroduceExprBasedMethodFix
    static TypeMirror resolveType(CompilationInfo info, TreePath path) {
        TypeMirror tm = info.getTrees().getTypeMirror(path);
        
        if (tm != null && tm.getKind() == TypeKind.NULL) {
            List<? extends TypeMirror> targetType = CreateElementUtilities.resolveType(new HashSet<ElementKind>(), info, path.getParentPath(), path.getLeaf(), (int) info.getTrees().getSourcePositions().getStartPosition(path.getCompilationUnit(), path.getLeaf()), new TypeMirror[1], new int[1]);
            
            if (targetType != null && !targetType.isEmpty()) {
                tm = targetType.get(0);
            } else {
                TypeElement object = info.getElements().getTypeElement("java.lang.Object");
                tm = object != null ? object.asType() : null;
            }
        }
        if (!Utilities.isValidType(tm)) {
            return null;
        } else {
            return tm;
        }
    }

    static final AttributeSet DUPE = AttributesUtilities.createImmutable(StyleConstants.Background, Color.GRAY);

    // used by IntroduceMethod Fix / IntroduceExpressionBasedMethodFix; should be used by all, duplicates are
    // everywhere.
    static boolean shouldReplaceDuplicate(final Document doc, final int startOff, final int endOff) {
        introduceBag(doc).clear();
        introduceBag(doc).addHighlight(startOff, endOff, DUPE);

        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                JTextComponent c = EditorRegistry.lastFocusedComponent();

                if (c != null && c.getDocument() == doc) {
                    try {
                        Rectangle start = c.modelToView(startOff);
                        Rectangle end = c.modelToView(endOff);
                        int sx = Math.min(start.x, end.x);
                        int dx = Math.max(start.x + start.width, end.x + end.width);
                        int sy = Math.min(start.y, end.y);
                        int dy = Math.max(start.y + start.height, end.y + end.height);

                        c.scrollRectToVisible(new Rectangle(sx, sy, dx - sx, dy - sy));
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });

        String title = NbBundle.getMessage(IntroduceHint.class, "TTL_DuplicateMethodPiece");
        String message = NbBundle.getMessage(IntroduceHint.class, "MSG_DuplicateMethodPiece");

        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(message, title, NotifyDescriptor.YES_NO_OPTION);
        
        return DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION;
    }

    public static final class HLFImpl implements HighlightsLayerFactory {

        public HighlightsLayer[] createLayers(Context context) {
            return new HighlightsLayer[] {
                HighlightsLayer.create(IntroduceHint.class.getName(), ZOrder.TOP_RACK.forPosition(500), true, introduceBag(context.getDocument())),
            };
        }

    }
    
    static class InsertClassMember {
        public ClassTree insertClassMember(WorkingCopy wc, ClassTree clazz, Tree member, int offset) throws IllegalStateException {
            return GeneratorUtils.insertClassMember(wc, clazz, member, offset);
        }
    }
    
    static InsertClassMember INSERT_CLASS_MEMBER = new InsertClassMember();//just for tests, for achieve compatibility with original behaviour
}
