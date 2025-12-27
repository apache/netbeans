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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.swing.JButton;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationInfo;
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
final class IntroduceExpressionBasedMethodFix extends IntroduceFixBase implements Fix {

    static List<TargetDescription> computeViableTargets(final CompilationInfo info, TreePath commonParent, Iterable<? extends Tree> toInclude, Iterable<? extends Occurrence> duplicates, 
            AtomicBoolean cancel, AtomicBoolean allIfaces) {
        List<TargetDescription> targets = new ArrayList<>();
        TreePath acceptableParent = commonParent;
        boolean allInterfaces = true;
        // for each enclosing class-like Tree (not interface !) define a target. Check if all duplicates fall inside the target
        // and if so, mark it as 'duplicatesAcceptable', because all duplicate sites sees that class as an enclosing scope.
        while (acceptableParent != null) {
            if (cancel.get()) {
                return null;
            }
            if (TreeUtilities.CLASS_TREE_KINDS.contains(acceptableParent.getLeaf().getKind())) {
                boolean duplicatesAcceptable = true;
                DUPLICATES_ACCEPTABLE:
                for (Occurrence duplicate : duplicates) {
                    for (Tree t : duplicate.getOccurrenceRoot()) {
                        if (t == acceptableParent.getLeaf()) {
                            continue DUPLICATES_ACCEPTABLE;
                        }
                    }
                    duplicatesAcceptable = false;
                    break;
                }
                Element el = info.getTrees().getElement(acceptableParent);
                if (el != null) {
                    boolean isIface = el.getKind().isInterface();
                    if (el.getKind().isClass() || isIface) {
                        TargetDescription td = TargetDescription.create(info, (TypeElement) el, 
                                acceptableParent, duplicatesAcceptable, isIface);
                        if (td.type != null && td.type.resolve(info) != null) {
                            targets.add(td);
                        }
                        allInterfaces &= isIface;
                    }
                }
            }
            acceptableParent = acceptableParent.getParentPath();
        }
        // sort the targets from in top-down order 
        Collections.reverse(targets);
        InstanceRefFinder finder = new InstanceRefFinder(info, commonParent);
        for (Tree include : toInclude) {
            finder.process(new TreePath(commonParent, include));
        }
        Set<Element> usedMembers = finder.getUsedMembers();
        Set<? extends Element> requiredEnclosing = finder.getRequiredInstances();
        // starting with the outermost scope, go to first enclosing class that sees all the usedMembers
        for (Iterator<TargetDescription> it = targets.iterator(); it.hasNext() && (!usedMembers.isEmpty() || !requiredEnclosing.isEmpty());) {
            TargetDescription td = it.next();
            TypeElement type = td.type.resolve(info);
            if (type == null) {
                it.remove();
                continue;
            }
            usedMembers.removeAll(info.getElements().getAllMembers(type));
            requiredEnclosing.remove(type);
            if (!usedMembers.isEmpty() || !requiredEnclosing.isEmpty()) {
                it.remove();
                continue;
            } 
            allInterfaces &= type.getKind() == ElementKind.INTERFACE;
        }
        if (targets.isEmpty()) {
            TreePath clazz = TreeUtils.findClass(commonParent);
            Element el = info.getTrees().getElement(clazz);
            if (el == null || (!el.getKind().isClass() && !el.getKind().isInterface())) {
                return null;
            }
            allInterfaces = el.getKind().isInterface();
            targets.add(TargetDescription.create(info, (TypeElement) el, clazz, true, el.getKind().isInterface()));
        }
        allIfaces.set(allInterfaces);
        return targets;
    }

    private final TypeMirrorHandle     returnType;
    private final List<TreePathHandle> parameters;
    private final Set<TypeMirrorHandle> thrownTypes;
    private final List<TreePathHandle> typeVars;
    private final Collection<TargetDescription> targets;

    public IntroduceExpressionBasedMethodFix(Source source, TreePathHandle expression, List<TreePathHandle> parameters, TypeMirrorHandle returnType, Set<TypeMirrorHandle> thrownTypes, int duplicatesCount, List<TreePathHandle> typeVars, int offset, Collection<TargetDescription> targets) {
        super(source, expression, duplicatesCount, offset);
        this.parameters = parameters;
        this.thrownTypes = thrownTypes;
        this.typeVars = typeVars;
        this.targets = targets;
        this.returnType = returnType;
    }

    public String getText() {
        return NbBundle.getMessage(IntroduceHint.class, "FIX_IntroduceMethod");
    }

    public String toString() {
        return "[IntroduceExpressionBasedMethodFix]"; // NOI18N
    }

    @NbBundle.Messages(value = {"MSG_ExpressionContainsLocalReferences=Could not move the expression that references local classes"})
    public ChangeInfo implement() throws Exception {
        JButton btnOk = new JButton(NbBundle.getMessage(IntroduceHint.class, "LBL_Ok"));
        JButton btnCancel = new JButton(NbBundle.getMessage(IntroduceHint.class, "LBL_Cancel"));
        btnCancel.setDefaultCapable(false);
        IntroduceMethodPanel panel = new IntroduceMethodPanel("method", duplicatesCount, targets, targetIsInterface); //NOI18N
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
        final String name = panel.getMethodName();
        final Set<Modifier> access = panel.getAccess();
        final boolean replaceOther = panel.getReplaceOther();
        final TargetDescription target = panel.getSelectedTarget();
        final MemberSearchResult searchResult = val.getResult();
        final boolean redoReferences = panel.isRefactorExisting();
        getModificationResult(name, target, replaceOther, access, redoReferences, searchResult).commit();
        return null;
    }

    @Override
    public ModificationResult getModificationResult() throws ParseException {
        ModificationResult result = null;
        int counter = 0;
        do {
            try {
                result = getModificationResult("method" + (counter != 0 ? String.valueOf(counter) : ""), targets.iterator().next(), true, EnumSet.of(Modifier.PRIVATE), false, null);
            } catch (Exception e) {
                counter++;
            }
        } while (result == null && counter < 10);
        return result;
    }

    private ModificationResult getModificationResult(final String name, final TargetDescription target, final boolean replaceOther, final Set<Modifier> access, final boolean redoReferences, final MemberSearchResult searchResult) throws ParseException {
        return ModificationResult.runModificationTask(Collections.singleton(source), new UserTask() {
            public void run(ResultIterator resultIterator) throws Exception {
                WorkingCopy copy = WorkingCopy.get(resultIterator.getParserResult());
                copy.toPhase(JavaSource.Phase.RESOLVED);
                TreePath expression = IntroduceExpressionBasedMethodFix.this.handle.resolve(copy);
                InstanceRefFinder finder = new InstanceRefFinder(copy, expression);
                finder.process();
                if (finder.containsLocalReferences()) {
                    NotifyDescriptor dd = new NotifyDescriptor.Message(Bundle.MSG_ExpressionContainsLocalReferences(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notifyLater(dd);
                    return;
                }
                boolean referencesInstances = finder.containsInstanceReferences();
                TypeMirror returnType = expression != null ? IntroduceHint.resolveType(copy, expression) : null;
                if (expression == null || returnType == null) {
                    return; //TODO...
                }
                returnType = Utilities.convertIfAnonymous(Utilities.resolveTypeForDeclaration(copy, returnType), false);
                final TreeMaker make = copy.getTreeMaker();
                Tree returnTypeTree = make.Type(returnType);
                copy.tag(returnTypeTree, TYPE_TAG);
                List<VariableElement> parameters = IntroduceHint.resolveVariables(copy, IntroduceExpressionBasedMethodFix.this.parameters);
                List<ExpressionTree> realArguments = IntroduceHint.realArguments(make, parameters);
                ExpressionTree invocation = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.Identifier(name), realArguments);
                TypeElement targetType = target.type.resolve(copy);
                TreePath pathToClass = targetType != null ? copy.getTrees().getPath(targetType) : null;
                if (pathToClass == null) {
                    pathToClass = TreeUtils.findClass(expression);
                }
                assert pathToClass != null;
                List<VariableTree> formalArguments = IntroduceHint.createVariables(copy, parameters, pathToClass, 
                        Collections.singletonList(expression));
                if (formalArguments == null) {
                    return; //XXX
                }
                List<ExpressionTree> thrown = IntroduceHint.typeHandleToTree(copy, thrownTypes);
                if (thrownTypes == null) {
                    return; //XXX
                }
                List<StatementTree> methodStatements = new LinkedList<StatementTree>();
                methodStatements.add(make.Return((ExpressionTree) expression.getLeaf()));
                List<TypeParameterTree> typeVars = new LinkedList<TypeParameterTree>();
                for (TreePathHandle tph : IntroduceExpressionBasedMethodFix.this.typeVars) {
                    typeVars.add((TypeParameterTree) tph.resolve(copy).getLeaf());
                }
                boolean isStatic = !referencesInstances || IntroduceHint.needsStaticRelativeTo(copy, pathToClass, expression);
                Tree parentTree = expression.getParentPath().getLeaf();
                Tree nueParent = copy.getTreeUtilities().translate(parentTree, Collections.singletonMap(expression.getLeaf(), invocation));
                copy.rewrite(parentTree, nueParent);
                if (replaceOther) {
                    //handle duplicates
                    Document doc = copy.getDocument();
                    Pattern p = Pattern.createPatternWithRemappableVariables(expression, parameters, true);
                    for (Occurrence desc : Matcher.create(copy).setSearchRoot(pathToClass).setCancel(new AtomicBoolean()).match(p)) {
                        TreePath firstLeaf = desc.getOccurrenceRoot();
                        int startOff = (int) copy.getTrees().getSourcePositions().getStartPosition(copy.getCompilationUnit(), firstLeaf.getLeaf());
                        int endOff = (int) copy.getTrees().getSourcePositions().getEndPosition(copy.getCompilationUnit(), firstLeaf.getLeaf());
                        if (!GraphicsEnvironment.isHeadless() && !IntroduceHint.shouldReplaceDuplicate(doc, startOff, endOff)) {
                            continue;
                        }
                        //XXX:
                        List<Union2<VariableElement, TreePath>> dupeParameters = new LinkedList<Union2<VariableElement, TreePath>>();
                        for (VariableElement ve : parameters) {
                            if (desc.getVariablesRemapToTrees().containsKey(ve)) {
                                dupeParameters.add(Union2.<VariableElement, TreePath>createSecond(desc.getVariablesRemapToTrees().get(ve)));
                            } else {
                                dupeParameters.add(Union2.<VariableElement, TreePath>createFirst(ve));
                            }
                        }
                        List<ExpressionTree> dupeRealArguments = IntroduceHint.realArgumentsForTrees(make, dupeParameters);
                        ExpressionTree dupeInvocation = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.Identifier(name), dupeRealArguments);
                        copy.rewrite(firstLeaf.getLeaf(), dupeInvocation);
                        isStatic |= IntroduceHint.needsStaticRelativeTo(copy, pathToClass, firstLeaf);
                    }
                    IntroduceHint.introduceBag(doc).clear();
                    //handle duplicates end
                }
                Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
                
                if (target.iface) {
                    modifiers.add(Modifier.DEFAULT);
                } else if (isStatic && target.canStatic) {
                    modifiers.add(Modifier.STATIC);
                }
                modifiers.addAll(access);
                ModifiersTree mods = make.Modifiers(modifiers);
                MethodTree method = make.Method(mods, name, returnTypeTree, typeVars, formalArguments, thrown, make.Block(methodStatements, false), null);
                ClassTree nueClass = IntroduceHint.INSERT_CLASS_MEMBER.insertClassMember(copy, (ClassTree) pathToClass.getLeaf(), method, offset);
                copy.rewrite(pathToClass.getLeaf(), nueClass);
                
                if (redoReferences) {
                    new ReferenceTransformer(
                            copy, ElementKind.METHOD,  searchResult,
                            name,
                            targetType).scan(pathToClass, null);
                }
            }
        });
    }

}
