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
package org.netbeans.modules.java.hints.introduce;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.JButton;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.matching.Matcher;
import org.netbeans.api.java.source.matching.Occurrence;
import org.netbeans.api.java.source.matching.Pattern;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Union2;

/**
 * Refactored from IntroduceFix originally by lahvac
 *
 * @author sdedic
 */
public final class IntroduceMethodFix extends IntroduceFixBase implements Fix {
    
    private static ScanStatement createAndRunScanner(CompilationInfo info, TreePath method, StatementTree from, StatementTree to, AtomicBoolean cancel) {
        Map<TypeMirror, TreePathHandle> typeVar2Def = new HashMap<TypeMirror, TreePathHandle>();
        List<TreePathHandle> typeVars = new LinkedList<TreePathHandle>();
        IntroduceHint.prepareTypeVars(method, info, typeVar2Def, typeVars);
        Flow.FlowResult flow = Flow.assignmentsForUse(info, method, cancel);
        if (flow == null || cancel.get()) {
            return null;
        }
        Map<Tree, Iterable<? extends TreePath>> assignmentsForUse = flow.getAssignmentsForUse();
        ScanStatement scanner = new ScanStatement(info, from, to, typeVar2Def, assignmentsForUse, cancel);
        Element methodEl = info.getTrees().getElement(method);
        if (methodEl != null && (methodEl.getKind() == ElementKind.METHOD || methodEl.getKind() == ElementKind.CONSTRUCTOR)) {
            ExecutableElement ee = (ExecutableElement) methodEl;
            scanner.localVariables.addAll(ee.getParameters());
        }
        scanner.scan(method, null);
        return scanner;
    }

    static Fix computeIntroduceMethod(CompilationInfo info, int start, int end, Map<IntroduceKind, String> errorMessage, AtomicBoolean cancel) {
        int[] statements = new int[2];
        TreePathHandle h = validateSelectionForIntroduceMethod(info, start, end, statements);
        if (h == null) {
            errorMessage.put(IntroduceKind.CREATE_METHOD, "ERR_Invalid_Selection");
            return null;
        }
        TreePath block = h.resolve(info);
        TreePath method = TreeUtils.findMethod(block, true);
        if (method == null) {
            errorMessage.put(IntroduceKind.CREATE_METHOD, "ERR_Invalid_Selection");
            return null;
        }
        if (method.getLeaf().getKind() == Tree.Kind.METHOD && ((MethodTree) method.getLeaf()).getParameters().contains(block.getLeaf())) {
            errorMessage.put(IntroduceKind.CREATE_METHOD, "ERR_Invalid_Selection");
            return null;
        }
        Element methodEl = info.getTrees().getElement(method);
        List<? extends StatementTree> parentStatements = IntroduceHint.getStatements(block);
        List<? extends StatementTree> statementsToWrap = parentStatements.subList(statements[0], statements[1] + 1);
        ScanStatement scanner = createAndRunScanner(info, method, statementsToWrap.get(0), statementsToWrap.get(statementsToWrap.size() - 1), cancel);
        if (scanner == null) {
            return null;
        }
        Set<TypeMirror> exceptions = new HashSet<TypeMirror>();
        int index = 0;
        TypeMirror methodReturnType = info.getTypes().getNoType(TypeKind.VOID);
        if (methodEl != null && (methodEl.getKind() == ElementKind.METHOD || methodEl.getKind() == ElementKind.CONSTRUCTOR)) {
            ExecutableElement ee = (ExecutableElement) methodEl;
            methodReturnType = ee.getReturnType();
        }
        List<TreePath> pathsOfStatementsToWrap = new LinkedList<TreePath>();
        for (StatementTree s : parentStatements) {
            TreePath path = new TreePath(block, s);
            if (index >= statements[0] && index <= statements[1]) {
                exceptions.addAll(info.getTreeUtilities().getUncaughtExceptions(path));
                pathsOfStatementsToWrap.add(path);
            }
            index++;
        }
        boolean exitsFromAllBranches = Utilities.exitsFromAllBranchers(info, new TreePath(block, statementsToWrap.get(statementsToWrap.size() - 1)));
        String exitsError = scanner.verifyExits(exitsFromAllBranches);
        if (exitsError != null) {
            errorMessage.put(IntroduceKind.CREATE_METHOD, exitsError);
            return null;
        }
        Map<VariableElement, Boolean> mergedVariableUse = new LinkedHashMap<VariableElement, Boolean>(scanner.usedLocalVariables);
        if (!scanner.usedAfterSelection.isEmpty()) {
            VariableElement el = scanner.usedAfterSelection.entrySet().iterator().next().getKey();
            
        }
        for (Map.Entry<VariableElement, Boolean> e : scanner.usedLocalVariables.entrySet()) {
            if (cancel.get()) {
                return null;
            }
            Boolean usedLocal = e.getValue();
            if (usedLocal == null && Flow.definitellyAssigned(info, e.getKey(), pathsOfStatementsToWrap, cancel)) {
                mergedVariableUse.put(e.getKey(), true);
            } else {
                Boolean def = scanner.usedAfterSelection.get(e.getKey());
                mergedVariableUse.put(e.getKey(), !(usedLocal == Boolean.FALSE) && (def != Boolean.FALSE));
            }
        }
        if (cancel.get()) {
            return null;
        }
        Set<VariableElement> additionalLocalVariables = new LinkedHashSet<VariableElement>();
        Set<VariableElement> paramsVariables = new LinkedHashSet<VariableElement>();
        for (Map.Entry<VariableElement, Boolean> e : mergedVariableUse.entrySet()) {
            if (e.getValue() == null ||  e.getValue()) {
                additionalLocalVariables.add(e.getKey());
            } else {
                paramsVariables.add(e.getKey());
                additionalLocalVariables.remove(e.getKey());
            }
        }
        List<TreePathHandle> params = new LinkedList<TreePathHandle>();
        for (VariableElement ve : paramsVariables) {
            params.add(TreePathHandle.create(info.getTrees().getPath(ve), info));
        }
        additionalLocalVariables.removeAll(paramsVariables);
        additionalLocalVariables.removeAll(scanner.selectionLocalVariables);
        List<TypeMirrorHandle> additionaLocalTypes = new LinkedList<TypeMirrorHandle>();
        List<String> additionaLocalNames = new LinkedList<String>();
        for (VariableElement ve : additionalLocalVariables) {
            TypeMirror vt = Utilities.resolveTypeForDeclaration(info, ve.asType());
            additionaLocalTypes.add(TypeMirrorHandle.create(vt));
            additionaLocalNames.add(ve.getSimpleName().toString());
        }
        List<TreePathHandle> exits = null;
        Tree lastStatement = statementsToWrap.get(statementsToWrap.size() - 1);
        if (parentStatements.get(parentStatements.size() - 1) == lastStatement) {
            TreePath search = block.getParentPath();
            Tree last = block.getLeaf();
            OUTTER:
            while (search != null) {
                switch (search.getLeaf().getKind()) {
                    case BLOCK:
                        List<? extends StatementTree> thisBlockStatements = ((BlockTree) search.getLeaf()).getStatements();
                        if (thisBlockStatements.get(thisBlockStatements.size() - 1) == last) {
                            break;
                        } else {
                            break OUTTER;
                        }
                    case IF:
                        break;
                    case METHOD:
                        Tree returnType = ((MethodTree) search.getLeaf()).getReturnType();
                        if (returnType == null || (returnType.getKind() == Tree.Kind.PRIMITIVE_TYPE && ((PrimitiveTypeTree) returnType).getPrimitiveTypeKind() == TypeKind.VOID)) {
                            exits = Collections.emptyList();
                        }
                        break OUTTER;
                    default:
                        break OUTTER;
                }
                last = search.getLeaf();
                search = search.getParentPath();
            }
        }
        if (exits == null) {
            exits = new LinkedList<TreePathHandle>();
            for (TreePath tp : scanner.selectionExits) {
                if (isInsideSameClass(tp, method)) {
                    exits.add(TreePathHandle.create(tp, info));
                }
            }
        }
        TypeMirror returnType;
        TreePathHandle returnAssignTo;
        boolean declareVariableForReturnValue;
        Pattern p = Pattern.createPatternWithRemappableVariables(pathsOfStatementsToWrap, scanner.usedLocalVariables.keySet(), true);
        Collection<? extends Occurrence> duplicates = Matcher.create(info).setCancel(cancel).match(p);
        int duplicatesCount = duplicates.size();
        if (!scanner.usedAfterSelection.isEmpty()) {
            VariableElement result = scanner.usedAfterSelection.keySet().iterator().next();
            returnType = Utilities.resolveTypeForDeclaration(info, result.asType());
            returnAssignTo = TreePathHandle.create(info.getTrees().getPath(result), info);
            declareVariableForReturnValue = scanner.selectionLocalVariables.contains(result);
        } else {
            if (!exits.isEmpty() && !exitsFromAllBranches) {
                returnType = info.getTypes().getPrimitiveType(TypeKind.BOOLEAN);
                returnAssignTo = null;
                declareVariableForReturnValue = false;
            } else {
                if (exitsFromAllBranches && scanner.hasReturns) {
                    returnType = methodReturnType;
                    returnAssignTo = null;
                    declareVariableForReturnValue = false;
                } else {
                    returnType = info.getTypes().getNoType(TypeKind.VOID);
                    returnAssignTo = null;
                    declareVariableForReturnValue = false;
                }
            }
        }
        Set<TypeMirrorHandle> exceptionHandles = new HashSet<TypeMirrorHandle>();
        for (TypeMirror tm : exceptions) {
            exceptionHandles.add(TypeMirrorHandle.create(tm));
        }
        AtomicBoolean allIfaces = new AtomicBoolean();
        List<TargetDescription> viableTargets = IntroduceExpressionBasedMethodFix.computeViableTargets(info, block, statementsToWrap, duplicates, cancel, allIfaces);
        IntroduceMethodFix imf = null;
        if (viableTargets != null && !viableTargets.isEmpty()) {
            imf = new IntroduceMethodFix(info.getSnapshot().getSource(), h, params, additionaLocalTypes, additionaLocalNames, TypeMirrorHandle.create(returnType), returnAssignTo, declareVariableForReturnValue, exceptionHandles, exits, exitsFromAllBranches, statements[0], statements[1],
                    duplicatesCount, scanner.getUsedTypeVars(), end, viableTargets);
            imf.setTargetIsInterface(allIfaces.get());
        }
        return imf;
    }

    static boolean isInsideSameClass(TreePath one, TreePath two) {
        ClassTree oneClass = null;
        ClassTree twoClass = null;
        while (one.getLeaf().getKind() != Tree.Kind.COMPILATION_UNIT && one.getLeaf().getKind() != null) {
            Tree t = one.getLeaf();
            if (TreeUtilities.CLASS_TREE_KINDS.contains(t.getKind())) {
                oneClass = (ClassTree) t;
                break;
            }
            one = one.getParentPath();
        }
        while (two.getLeaf().getKind() != Tree.Kind.COMPILATION_UNIT && two.getLeaf().getKind() != null) {
            Tree t = two.getLeaf();
            if (TreeUtilities.CLASS_TREE_KINDS.contains(t.getKind())) {
                twoClass = (ClassTree) t;
                break;
            }
            two = two.getParentPath();
        }
        if (oneClass != null && oneClass.equals(twoClass)) {
            return true;
        }
        return false;
    }

    /**
     * Checks that the selection contains entire statements. First it tries to extend from the selection range up the
     * tree to find the exact matching (single) statement. If that does not work, the block that contains the selection
     * is searched to find the first and last statements fully covered by the selection. The first statement's TreePath is
     * returned; the first and last statement indexes within their parent block are returned in statementsSpan array
     * @param ci context
     * @param start start of selection 
     * @param end end of selection
     * @param statementsSpan out; indexes of first and last statement within their parent Block or Case.
     * @return TreePath for the first statement for valid selections, {@code null} otherwise
     */
    public static TreePathHandle validateSelectionForIntroduceMethod(CompilationInfo ci, int start, int end, int[] statementsSpan) {
        int[] span = TreeUtils.ignoreWhitespaces(ci, Math.min(start, end), Math.max(start, end));
        start = span[0];
        end = span[1];
        if (start >= end) {
            return null;
        }
        TreePath tp = ci.getTreeUtilities().pathFor((start + end) / 2 + 1);
        // finds and returns a TreePath to a statement, which precisely matches the non-whitespace area in the selection
        for (; tp != null; tp = tp.getParentPath()) {
            Tree leaf = tp.getLeaf();
            if (!StatementTree.class.isAssignableFrom(leaf.getKind().asInterface())) {
                continue;
            }
            long treeStart = ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), leaf);
            long treeEnd = ci.getTrees().getSourcePositions().getEndPosition(ci.getCompilationUnit(), leaf);
            if (treeStart != start || treeEnd != end) {
                continue;
            }
            List<? extends StatementTree> statements = IntroduceHint.getStatements(tp);
            statementsSpan[0] = statements.indexOf(tp.getLeaf());
            statementsSpan[1] = statementsSpan[0];
            return TreePathHandle.create(tp, ci);
        }
        TreePath tpStart = ci.getTreeUtilities().pathFor(start);
        TreePath tpEnd = ci.getTreeUtilities().pathFor(end);
        if (tpStart.getLeaf() != tpEnd.getLeaf() || (tpStart.getLeaf().getKind() != Tree.Kind.BLOCK && tpStart.getLeaf().getKind() != Tree.Kind.CASE)) {
            //??? not in the same block:
            return null;
        }
        int from = -1;
        int to = -1;
        List<? extends StatementTree> statements = tpStart.getLeaf().getKind() == Tree.Kind.BLOCK ? ((BlockTree) tpStart.getLeaf()).getStatements() : ((CaseTree) tpStart.getLeaf()).getStatements();
        int index = 0;
        for (StatementTree s : statements) {
            long sStart = ci.getTrees().getSourcePositions().getStartPosition(ci.getCompilationUnit(), s);
            if (sStart == start && from == (-1)) {
                from = index;
            }
            if (end < sStart && to == (-1)) {
                to = index - 1;
            }
            index++;
        }
        if (from == (-1)) {
            return null;
        }
        if (to == (-1)) {
            to = statements.size() - 1;
        }
        if (to < from) {
            return null;
        }
        statementsSpan[0] = from;
        statementsSpan[1] = to;
        return TreePathHandle.create(new TreePath(tpStart, statements.get(from)), ci);
    }
    private final List<TreePathHandle> parameters;
    private final List<TypeMirrorHandle> additionalLocalTypes;
    private final List<String> additionalLocalNames;
    private final TypeMirrorHandle returnType;
    private final TreePathHandle returnAssignTo;
    private final boolean declareVariableForReturnValue;
    private final Set<TypeMirrorHandle> thrownTypes;
    private final List<TreePathHandle> exits;
    private final boolean exitsFromAllBranches;
    private final int from;
    private final int to;
    private final List<TreePathHandle> typeVars;
    private final Collection<TargetDescription> targets;

    public IntroduceMethodFix(Source source, TreePathHandle parentBlock, List<TreePathHandle> parameters, List<TypeMirrorHandle> additionalLocalTypes, List<String> additionalLocalNames, TypeMirrorHandle returnType, TreePathHandle returnAssignTo, boolean declareVariableForReturnValue, Set<TypeMirrorHandle> thrownTypes, List<TreePathHandle> exists, boolean exitsFromAllBranches, int from, int to, int duplicatesCount, List<TreePathHandle> typeVars, int offset, Collection<TargetDescription> targets) {
        super(source, parentBlock, duplicatesCount, offset);
        this.parameters = parameters;
        this.additionalLocalTypes = additionalLocalTypes;
        this.additionalLocalNames = additionalLocalNames;
        this.returnType = returnType;
        this.returnAssignTo = returnAssignTo;
        this.declareVariableForReturnValue = declareVariableForReturnValue;
        this.thrownTypes = thrownTypes;
        this.exits = exists;
        this.exitsFromAllBranches = exitsFromAllBranches;
        this.from = from;
        this.to = to;
        this.typeVars = typeVars;
        this.targets = targets;
    }

    public String getText() {
        return NbBundle.getMessage(IntroduceHint.class, "FIX_IntroduceMethod");
    }

    public String toDebugString(CompilationInfo info) {
        return "[IntroduceMethod:" + from + ":" + to + "]"; // NOI18N
    }
    
    public ChangeInfo implement() throws Exception {
        JButton btnOk = new JButton(NbBundle.getMessage(IntroduceHint.class, "LBL_Ok"));
        JButton btnCancel = new JButton(NbBundle.getMessage(IntroduceHint.class, "LBL_Cancel"));
        IntroduceMethodPanel panel = new IntroduceMethodPanel("", duplicatesCount, targets, targetIsInterface); //NOI18N
        String caption = NbBundle.getMessage(IntroduceHint.class, "CAP_IntroduceMethod");
        DialogDescriptor dd = new DialogDescriptor(panel, caption, true, new Object[]{btnOk, btnCancel}, btnOk, DialogDescriptor.DEFAULT_ALIGN, null, null);
        NotificationLineSupport notifier = dd.createNotificationLineSupport();
        MethodValidator val = new MethodValidator(source, parameters, returnType);
        panel.setNotifier(notifier);
        panel.setValidator(val);
        panel.setOkButton(btnOk);
        if (DialogDisplayer.getDefault().notify(dd) != btnOk) {
            return null; //cancel
        }
        boolean redoReferences =  panel.isRefactorExisting();
        final String name = panel.getMethodName();
        final Set<Modifier> access = panel.getAccess();
        final boolean replaceOther = panel.getReplaceOther();
        final TargetDescription target = panel.getSelectedTarget();
        ModificationResult.runModificationTask(Collections.singleton(source), new TaskImpl(access, name, target, replaceOther, val.getResult(), redoReferences)).commit();
        return null;
    }

    @Override
    public ModificationResult getModificationResult() throws ParseException {
        ModificationResult result = null;
        int counter = 0;
        do {
            try {
                result = ModificationResult.runModificationTask(Collections.singleton(source), new TaskImpl(EnumSet.of(Modifier.PRIVATE), "method" + (counter != 0 ? String.valueOf(counter) : ""), targets.iterator().next(), true, null, false));
            } catch (Exception e) {
                counter++;
            }
        } while (result == null && counter < 10);
        return result;
    }

    static class OccurrencePositionComparator implements Comparator<Occurrence> {
        final CompilationUnitTree cut;
        final SourcePositions positions;

        public OccurrencePositionComparator(CompilationUnitTree cut, SourcePositions positions) {
            this.cut = cut;
            this.positions = positions;
        }

        @Override
        public int compare(Occurrence o1, Occurrence o2) {
            Tree r1 = o1.getOccurrenceRoot().getLeaf();
            Tree r2 = o2.getOccurrenceRoot().getLeaf();
            int p1 = (int)positions.getStartPosition(cut, r1);
            int p2 = (int)positions.getStartPosition(cut, r2);
            return p1 - p2;
        }
        
    }

    private class TaskImpl extends UserTask {
        private final Set<Modifier> access;
        private final String name;
        private final TargetDescription target;
        private final boolean replaceOther;

        WorkingCopy copy;
        TreeMaker   make;
        /**
         * Anchor statement, should correspond to the first statement in the selection.
         */
        TreePath firstStatement;
        /**
         * Variable that holds the outcome of the extracted code; null, if no return value is required
         */
        VariableElement outcomeVariable;
        /**
         * Statement list enclosing the original selection, the entire block/case/etc
         */
        List<? extends StatementTree> statements;
        
        List<TreePath> statementPaths;
        
        /**
         * Parameters of the extracted method
         */
        List<VariableElement> parameters;
        /**
         * Return type of the extracted method
         */
        TypeMirror returnType;
        /**
         * One of the exits from the extracted method. All exits have to be of the same type (ensured by the caller).
         */
        TreePath branchExit;
        /**
         * Tree for the return type of the extracted method. Used when generating the method and when
         * creating local variables to assign method's result to.
         */
        Tree returnTypeTree;
        /**
         * List of resolved exits
         */
        List<TreePath> resolvedExits;
        
        TreePath pathToClass;
        
        MemberSearchResult searchResult;
        
        boolean redoReferences;

        public TaskImpl(Set<Modifier> access, String name, TargetDescription target, boolean replaceOther, MemberSearchResult searchResult, boolean redoReferences) {
            this.access = access;
            this.name = name;
            this.target = target;
            this.replaceOther = replaceOther;
            this.searchResult = searchResult;
            this.redoReferences = redoReferences;
        }

        private void generateMethodContents(List<StatementTree> methodStatements) {
            Iterator<TypeMirrorHandle> additionalType = additionalLocalTypes.iterator();
            Iterator<String> additionalName = additionalLocalNames.iterator();
            while (additionalType.hasNext() && additionalName.hasNext()) {
                TypeMirror tm = additionalType.next().resolve(copy);
                if (tm == null) {
                    //XXX:
                    return;
                }
                Tree type = make.Type(tm);
                methodStatements.add(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), additionalName.next(), type, null));
            }
            if (from == to && statements.get(from).getKind() == Tree.Kind.BLOCK) {
                methodStatements.addAll(((BlockTree) statements.get(from)).getStatements());
            } else {
                methodStatements.addAll(statements.subList(from, to + 1));
            }
        }
        
        private MethodTree createMethodDefinition(boolean mustStatic) {
            // if all the statements are contained within a Block, get just the block, it will be processed recursively
            List<VariableTree> formalArguments = IntroduceHint.createVariables(copy, parameters, pathToClass, 
                    statementPaths.subList(from, to + 1));
            if (formalArguments == null) {
                return null; //XXX
            }
            List<ExpressionTree> thrown = IntroduceHint.typeHandleToTree(copy, thrownTypes);
            if (thrown == null) {
                return null; //XXX
            }
            List<TypeParameterTree> typeVars = new LinkedList<TypeParameterTree>();
            for (TreePathHandle tph : IntroduceMethodFix.this.typeVars) {
                typeVars.add((TypeParameterTree) tph.resolve(copy).getLeaf());
            }
            List<StatementTree> methodStatements = new ArrayList<StatementTree>();
            generateMethodContents(methodStatements);
            makeReturnsFromExtractedMethod(methodStatements);
            
            Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
            modifiers.addAll(access);
            
            if (target.iface) {
                modifiers.add(Modifier.DEFAULT);
            } else if (mustStatic) {
                modifiers.add(Modifier.STATIC);
            }
            ModifiersTree mods = make.Modifiers(modifiers);
            MethodTree method = make.Method(mods, name, returnTypeTree, typeVars, formalArguments, thrown, make.Block(methodStatements, false), null);
            copy.tag(returnTypeTree, TYPE_TAG);

            return method;
        }
        /**
         * True, if all exit points returns the computed value.
         */
        private boolean returnSingleValue;

        /**
         * Generates method invocation at the end of `nueStatements'.
         */
        private void generateMethodInvocation(List<StatementTree> nueStatements, List<ExpressionTree> realArguments,
                Occurrence desc) {
            boolean alreadyInvoked = false;
            ExpressionTree invocation = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.Identifier(name), realArguments);
            VariableElement remappedReturn = outcomeVariable;
            
            if (outcomeVariable != null) {
                ExpressionTree sel = null;
                
                if (desc != null) {
                    TreePath remappedTree = desc.getVariablesRemapToTrees().get(outcomeVariable);
                    VariableElement remappedElement = (VariableElement) desc.getVariablesRemapToElement().get(outcomeVariable);
                    
                    if (remappedElement != null) {
                        remappedReturn = remappedElement;
                    }
                    if (remappedTree != null) {
                        sel = (ExpressionTree)remappedTree.getLeaf();
                    }
                }
                if (sel == null) {
                    // also reached if des != null & remappedTree == null, uses fallback to returnAssignTo if remappedElement == null
                    sel = make.Identifier(remappedReturn.getSimpleName());
                }
                
                if (declareVariableForReturnValue) {
                    nueStatements.add(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), remappedReturn.getSimpleName(), returnTypeTree, invocation));
                    alreadyInvoked = true;
                } else if (!returnSingleValue) {
                    invocation = make.Assignment(sel, invocation);
                }
            }
            if (branchExit != null) {
                if (returnSingleValue) {
                    nueStatements.add(make.Return(invocation));
                } else {
                    StatementTree branch = null;
                    switch (branchExit.getLeaf().getKind()) {
                        case BREAK:
                            branch = make.Break(((BreakTree) branchExit.getLeaf()).getLabel());
                            break;
                        case CONTINUE:
                            branch = make.Continue(((ContinueTree) branchExit.getLeaf()).getLabel());
                            break;
                        case RETURN:
                            branch = make.Return(((ReturnTree) branchExit.getLeaf()).getExpression());
                            break;
                    }
                    if (remappedReturn != null || exitsFromAllBranches) {
                        nueStatements.add(make.ExpressionStatement(invocation));
                        nueStatements.add(branch);
                    } else {
                        nueStatements.add(make.If(make.Parenthesized(invocation), branch, null));
                    }
                }
                alreadyInvoked = true;
            }
            if (!alreadyInvoked) {
                nueStatements.add(make.ExpressionStatement(invocation));
            }
        }

        /**
         * Creates a return statement tree from the extracted method, depending on return type and/or branching
         */
        private ReturnTree makeExtractedReturn(boolean forceReturn) {
            if (outcomeVariable != null) {
                return make.Return(make.Identifier(outcomeVariable.getSimpleName()));
            } else if (forceReturn) {
                return make.Return(exitsFromAllBranches ? null : make.Literal(true));
            } else {
                return null;
            }
        }

        /**
         * Replaces former exit points by returns and/or adds a return with value at the end of the method
         */
        private void makeReturnsFromExtractedMethod(List<StatementTree> methodStatements) {
            if (returnSingleValue) {
                return;
            }
            if (resolvedExits != null) {
                for (TreePath resolved : resolvedExits) {
                    ReturnTree r = makeExtractedReturn(true);
                    GeneratorUtilities.get(copy).copyComments(resolved.getLeaf(), r, false);
                    GeneratorUtilities.get(copy).copyComments(resolved.getLeaf(), r, true);
                    copy.rewrite(resolved.getLeaf(), r);
                }
                // the default exit path, should return false
                if (outcomeVariable == null && !exitsFromAllBranches) {
                    methodStatements.add(make.Return(make.Literal(false)));
                }
            } else {
                ReturnTree ret = makeExtractedReturn(false);
                if (ret != null) {
                    methodStatements.add(ret);
                }
            }
        }

        /**
         * Resolves the handles, returns false if some handle does not resolve.
         */
        private boolean resolveAndInitialize() {
            firstStatement = handle.resolve(copy);
            returnType = IntroduceMethodFix.this.returnType.resolve(copy);
            if (firstStatement == null || returnType == null) {
                return false;
            }
            parameters = IntroduceHint.resolveVariables(copy, IntroduceMethodFix.this.parameters);
            if (IntroduceMethodFix.this.returnAssignTo != null) {
                outcomeVariable = (VariableElement) IntroduceMethodFix.this.returnAssignTo.resolveElement(copy);
                if (outcomeVariable == null) {
                    return false;
                }
            }
            if (exits != null && !exits.isEmpty()) {
                branchExit = exits.iterator().next().resolve(copy);
                if (branchExit == null) {
                    return false;
                }
                resolvedExits = new ArrayList<TreePath>(exits.size());
                for (TreePathHandle h : exits) {
                    TreePath resolved = h.resolve(copy);
                    if (resolved == null) {
                        return false;
                    }
                    if (resolvedExits.isEmpty()) {
                        branchExit = resolved;
                    }
                    resolvedExits.add(resolved);
                }
                
                returnSingleValue = exitsFromAllBranches && branchExit.getLeaf().getKind() == Tree.Kind.RETURN && outcomeVariable == null && returnType.getKind() != TypeKind.VOID;
            }
            // initialization
            make = copy.getTreeMaker();
            returnTypeTree = make.Type(returnType);
            statementPaths = Utilities.getStatementPaths(firstStatement);
            statements = IntroduceHint.getStatements(firstStatement);
            GeneratorUtilities.get(copy).importComments(firstStatement.getParentPath().getLeaf(), copy.getCompilationUnit());
            return true;
        }
        /**
         * List of replacements. Key is the parent Tree of the replaced portion; values contain start/end trees of
         * the match.
         */
        Map<Tree, List<Integer>> replacements = new HashMap<Tree, List<Integer>>();

        /**
         * Adds a replacement to the check list
         */
        void addReplacement(Tree parent, int start, int end) {
            List<Integer> rr = replacements.get(parent);
            if (rr == null) {
                rr = new ArrayList<Integer>();
                replacements.put(parent, rr);
            }
            rr.add(start); rr.add(end);
        }

        /**
         * Checks whether a duplicate intersects with some of the replacements already made
         */
        boolean isDuplicateValid(TreePath duplicateRoot) {
            TreePath parent = duplicateRoot.getParentPath();
            List<Integer> repls = replacements.get(parent.getLeaf());
            if (repls == null) {
                return true;
            }
            List<? extends StatementTree> stmts = IntroduceHint.getStatements(duplicateRoot);
            int o = stmts.indexOf(duplicateRoot.getLeaf());
            int l = repls.size();
            for (int idx = 0; idx < l; idx += 2) {
                if (o < repls.get(idx)) {
                    continue;
                }
                if (o <= repls.get(idx + 1)) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Translates original variables passed to the extracted method to the duplicate's variable space.
         */
        List<ExpressionTree> makeArgumentsForDuplicate(Occurrence desc) {
            List<Union2<VariableElement, TreePath>> dupeParameters = new LinkedList<Union2<VariableElement, TreePath>>();
            for (VariableElement ve : parameters) {
                if (desc.getVariablesRemapToTrees().containsKey(ve)) {
                    dupeParameters.add(Union2.<VariableElement, TreePath>createSecond(desc.getVariablesRemapToTrees().get(ve)));
                } else {
                    dupeParameters.add(Union2.<VariableElement, TreePath>createFirst(ve));
                }
            }
            List<ExpressionTree> dupeRealArguments = IntroduceHint.realArgumentsForTrees(make, dupeParameters);
            return dupeRealArguments;
        }

        public void run(ResultIterator resultIterator) throws Exception {
            WorkingCopy copy = WorkingCopy.get(resultIterator.getParserResult());
            copy.toPhase(JavaSource.Phase.RESOLVED);
            this.copy = copy;
            
            if (!resolveAndInitialize()) {
                return;
            }
            final Map<Tree, Tree> rewritten = new IdentityHashMap<Tree, Tree>(); 

            InstanceRefFinder finder = new InstanceRefFinder(copy, firstStatement);
            for (TreePath stp : statementPaths) {
                finder.process(stp);
            }
            if (finder.containsLocalReferences()) {
                NotifyDescriptor dd = new NotifyDescriptor.Message(Bundle.MSG_ExpressionContainsLocalReferences(), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(dd);
                return;
            }
            TypeElement targetType = target.type.resolve(copy);
            pathToClass = targetType != null ? copy.getTrees().getPath(targetType) : null;
            if (pathToClass == null) {
                pathToClass = TreeUtils.findClass(firstStatement);
            }
            assert pathToClass != null;

            boolean referencesInstances = finder.containsInstanceReferences();
            boolean isStatic = IntroduceHint.needsStaticRelativeTo(copy, pathToClass, firstStatement);
            
            // generate new version of the statement list, with the method invocation.
            List<StatementTree> nueStatements = new LinkedList<StatementTree>();
            generateMethodInvocation(nueStatements, IntroduceHint.realArguments(make, parameters), null);
            
            Utilities.replaceStatements(copy, firstStatement, statements.get(to), nueStatements);
            
            addReplacement(firstStatement.getParentPath().getLeaf(), from, to);
            if (replaceOther) {
                //handle duplicates
                Document doc = copy.getDocument();
                List<TreePath> statementsPaths = new LinkedList<TreePath>();
                for (StatementTree t : statements.subList(from, to + 1)) {
                    statementsPaths.add(new TreePath(firstStatement.getParentPath(), t));
                }
                Pattern p = Pattern.createPatternWithRemappableVariables(statementsPaths, parameters, true);
                List<? extends Occurrence> occurrences = new ArrayList<Occurrence>(Matcher.create(copy).setSearchRoot(pathToClass).setCancel(new AtomicBoolean()).match(p));
                occurrences.sort(new OccurrencePositionComparator(copy.getCompilationUnit(), copy.getTrees().getSourcePositions()));
                for (Occurrence desc :occurrences ) {
                    TreePath firstLeaf = desc.getOccurrenceRoot();
                    if (!isDuplicateValid(firstLeaf)) {
                        // the duplicate intersects with some replacement already made.
                        continue;
                    }
                    // FIXME - does this really work, in case of `case' contents ? Check on case X: stmt; and case X: { stmts; }
                    Tree mapped = copy.resolveRewriteTarget(firstLeaf.getParentPath().getLeaf());
                    TreePath mappedPath = new TreePath(new TreePath(firstLeaf.getParentPath().getParentPath(), 
                            mapped), firstLeaf.getLeaf());
                    List<? extends StatementTree> parentStatements = IntroduceHint.getStatements(mappedPath);
                    int dupeStart = parentStatements.indexOf(firstLeaf.getLeaf());
                    assert dupeStart > -1;
                    int dupeLast = dupeStart + statementsPaths.size() - 1;
                    StatementTree firstSt = (StatementTree)firstLeaf.getLeaf();
                    StatementTree lastSt = parentStatements.get(dupeLast);
                    final TreePath enclosingMethod = TreeUtils.findMethod(firstLeaf);
                    if (enclosingMethod == null) {
                        continue;
                    }
                    ScanStatement scanner = createAndRunScanner(copy, enclosingMethod, firstSt, lastSt, new AtomicBoolean(false));
                    boolean usedAfter = false;
                    if (!scanner.usedAfterSelection.isEmpty()) {
                        usedAfter = true;
                        if (scanner.usedAfterSelection.size() == 1 && outcomeVariable != null) {
                            VariableElement usedVar = scanner.usedAfterSelection.keySet().iterator().next();
                            Element remapped = desc.getVariablesRemapToElement().get(outcomeVariable);
                            if (remapped == null || remapped == usedVar) {
                                // the return value is used, no other escapes.
                                usedAfter = false;
                            }
                        }
                    }
                    int startOff = (int) copy.getTrees().getSourcePositions().getStartPosition(copy.getCompilationUnit(), firstSt);
                    int endOff = (int) copy.getTrees().getSourcePositions().getEndPosition(copy.getCompilationUnit(), lastSt);
                    
                    if (usedAfter || !GraphicsEnvironment.isHeadless() && !IntroduceHint.shouldReplaceDuplicate(doc, startOff, endOff)) {
                        continue;
                    }
                    List<StatementTree> newStatements = new LinkedList<StatementTree>();
                    generateMethodInvocation(newStatements, makeArgumentsForDuplicate(desc), desc);
                    Utilities.replaceStatements(copy, mappedPath, lastSt, newStatements);
                    addReplacement(firstLeaf.getParentPath().getLeaf(), dupeStart, dupeLast);
                    
                    isStatic |= IntroduceHint.needsStaticRelativeTo(copy, pathToClass, firstLeaf);
                }
                IntroduceHint.introduceBag(doc).clear();
                //handle duplicates end
            }
            isStatic &= !referencesInstances & target.canStatic;
            MethodTree method = createMethodDefinition(isStatic);
            ClassTree nueClass = IntroduceHint.INSERT_CLASS_MEMBER.insertClassMember(copy, (ClassTree) pathToClass.getLeaf(), method, offset);
            copy.rewrite(pathToClass.getLeaf(), nueClass);
            
            if (redoReferences) {
                new ReferenceTransformer(
                    copy, ElementKind.METHOD, 
                    searchResult,
                    name, 
                    targetType).scan(pathToClass, null);
            }
        }
    }
}
