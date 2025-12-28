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
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.swing.JButton;
import javax.swing.text.BadLocationException;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 * Fix that introduces a field or constant based on an expression. Refactored from IntroduceHint originally by Lahvac
 *
 * @author sdedic
 */
class IntroduceFieldFix extends IntroduceFixBase implements Fix {
    protected final String guessedName;
    protected final boolean permitDuplicates;
    private final int[] initilizeIn;
    private final boolean statik;
    private final boolean allowFinalInCurrentMethod;
    private final TreePathHandle targetHandle;
    
    /**
     * Initializes the fix
     * @param handle handle for the refactored expression
     * @param js the target source
     * @param guessedName proposed new field's name
     * @param numDuplicates number of duplicates found
     * @param initilizeIn possible initialization styles.
     * @param statik true, if the field must be static
     * @param allowFinalInCurrentMethod false, if the variable may not be declared final
     * @param offset caret offset
     */
    public IntroduceFieldFix(TreePathHandle handle, Source source, String guessedName,
            int numDuplicates, int[] initilizeIn, boolean statik, boolean allowFinalInCurrentMethod, int offset, TreePathHandle target) {
        this(handle, source, guessedName, numDuplicates, initilizeIn, statik, allowFinalInCurrentMethod, offset, false, target);
    }
    
    public IntroduceFieldFix(TreePathHandle handle, Source source, String guessedName,
            int numDuplicates, int[] initilizeIn, boolean statik, boolean allowFinalInCurrentMethod, int offset, boolean allowDuplicates, TreePathHandle target) {
        super(source, handle, numDuplicates, offset);
        this.guessedName = guessedName;
        this.initilizeIn = initilizeIn;
        this.statik = statik;
        this.allowFinalInCurrentMethod = allowFinalInCurrentMethod;
        this.permitDuplicates = allowDuplicates;
        this.targetHandle = target;
    }
    
    @Override
    public String getText() {
        return NbBundle.getMessage(IntroduceHint.class, "FIX_IntroduceField");
    }

    @Override
    public String toString() {
        return "[IntroduceField:" + guessedName + ":" + duplicatesCount + ":" + statik + ":" + allowFinalInCurrentMethod + ":" + Arrays.toString(initilizeIn) + "]"; // NOI18N
    }
    
    protected IntroduceFieldPanel createPanel(JButton btnOk) {
        return new IntroduceFieldPanel(
                guessedName, initilizeIn, 
                permitDuplicates ? duplicatesCount : 1,
                allowFinalInCurrentMethod, 
                handle.getKind() == Tree.Kind.VARIABLE, 
                IntroduceFieldPanel.FIELD, "introduceField", btnOk);
    }
    
    protected String getCaption() {
        return NbBundle.getMessage(IntroduceHint.class, "CAP_IntroduceField");
    }
    
    protected TreePath findTargetClass(WorkingCopy copy, TreePath resolved) {
        TreePath pathToClass = resolved;
        while (pathToClass != null && !TreeUtilities.CLASS_TREE_KINDS.contains(pathToClass.getLeaf().getKind())) {
            pathToClass = pathToClass.getParentPath();
        }
        return pathToClass;
    }

    private TreePath findOutermostClass(WorkingCopy copy, TreePath resolved) {
        TreePath pathToClass = resolved;
        while (pathToClass != null && (!TreeUtilities.CLASS_TREE_KINDS.contains(pathToClass.getLeaf().getKind()) || pathToClass.getParentPath().getLeaf().getKind() != Tree.Kind.COMPILATION_UNIT)) {
            pathToClass = pathToClass.getParentPath();
        }
        return pathToClass;
    }

    @Override
    public ChangeInfo implement() throws IOException, BadLocationException, ParseException {
        JButton btnOk = new JButton(NbBundle.getMessage(IntroduceHint.class, "LBL_Ok"));
        btnOk.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(IntroduceHint.class, "AD_IntrHint_OK"));
        JButton btnCancel = new JButton(NbBundle.getMessage(IntroduceHint.class, "LBL_Cancel"));
        btnCancel.setDefaultCapable(false);
        btnCancel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(IntroduceHint.class, "AD_IntrHint_Cancel"));
        IntroduceFieldPanel panel = createPanel(btnOk);
        FieldValidator fv = new FieldValidator(source, null, this.handle);
        if (targetIsInterface) {
            panel.setAllowAccess(false);
        }
        DialogDescriptor dd = new DialogDescriptor(panel, getCaption(), true, new Object[]{btnOk, btnCancel}, btnOk, DialogDescriptor.DEFAULT_ALIGN, null, null);
        panel.setNotifier(dd.createNotificationLineSupport());
        panel.setTarget(targetHandle);
        panel.setValidator(fv);

        if (DialogDisplayer.getDefault().notify(dd) != btnOk) {
            return null; //cancel
        }
        ModificationResult.runModificationTask(Collections.singleton(source), new Worker(panel.getFieldName(), permitDuplicates && panel.isReplaceAll(),
                panel.isDeclareFinal(), panel.getAccess(), panel.getInitializeIn(), fv.getLastResult(), panel.isRefactorExisting())).commit();
        return null;
    }

    @Override
    public ModificationResult getModificationResult() throws ParseException {
        return ModificationResult.runModificationTask(Collections.singleton(source), new Worker(guessedName, permitDuplicates, false, EnumSet.of(Modifier.PRIVATE), IntroduceFieldPanel.INIT_FIELD, null, false));
    }

    /**
     * The actual modification. Some javac related data are recorded in fields, inner class prevents
     * unintentional leak if someone keeps a reference to the Fix
     */
    protected final class Worker extends UserTask {
        final String name;
        final boolean replaceAll;
        final boolean declareFinal;
        final Set<Modifier> access;
        final int initializeIn;
        final boolean refactorExisting;
        final MemberSearchResult searchResult;

        private ClassTree nueClass;
        private TreePath toRemoveFromParent;
        private boolean variableRewrite;
        private boolean expressionStatementRewrite;

        public Worker(String name, boolean replaceAll, boolean declareFinal, Set<Modifier> access, int initializeIn, MemberSearchResult searchResult, boolean refactorExisting) {
            this.name = name;
            this.replaceAll = replaceAll;
            this.declareFinal = declareFinal;
            this.access = access;
            this.initializeIn = initializeIn;
            this.refactorExisting = refactorExisting;
            this.searchResult = searchResult;
        }

        private boolean initializeFromMethod(WorkingCopy parameter, TreePath resolved, 
                ExpressionTree expression, String name, TypeMirror tm) {
            TreeMaker make = parameter.getTreeMaker();
            TreePath statementPath = resolved;
            statementPath = TreeUtils.findStatement(statementPath);
            if (statementPath == null) {
                //XXX: well....
                return false;
            }
            ExpressionStatementTree assignment = make.ExpressionStatement(make.Assignment(make.Identifier(name), expression));
            StatementTree statement = (StatementTree) statementPath.getLeaf();
            insertStatement(parameter, statementPath.getParentPath(), statement, assignment, true);
            return true;
        }

        private boolean initializeConstructors(WorkingCopy parameter, 
                TreePath method, TreePath pathToClass, ExpressionTree expression, String name) {
            TreeMaker make = parameter.getTreeMaker();
            for (TreePath constructor : TreeUtils.findConstructors(parameter, method)) {
                //check for syntetic constructor:
                if (parameter.getTreeUtilities().isSynthetic(constructor)) {
                    List<StatementTree> nueStatements = new LinkedList<>();
                    ExpressionTree reference = make.Identifier(name);
                    Element clazz = parameter.getTrees().getElement(pathToClass);
                    ModifiersTree constrMods = (clazz == null || clazz.getKind() != ElementKind.ENUM) ? make.Modifiers(EnumSet.of(Modifier.PUBLIC)) : make.Modifiers(Collections.EMPTY_SET);
                    nueStatements.add(make.ExpressionStatement(make.Assignment(reference, expression)));
                    BlockTree nueBlock = make.Block(nueStatements, false);
                    MethodTree nueConstr = make.Method(constrMods, "<init>", null, Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), nueBlock, null); //NOI18N
                    nueClass = IntroduceHint.INSERT_CLASS_MEMBER.insertClassMember(parameter, nueClass, nueConstr, offset);
                    nueClass = make.removeClassMember(nueClass, constructor.getLeaf());
                    return true;
                }

                boolean hasParameterOfTheSameName = false;
                MethodTree constr = (MethodTree) constructor.getLeaf();
                if (constr.getBody() == null) {
                    continue;
                }
                for (VariableTree p : constr.getParameters()) {
                    if (name.equals(p.getName().toString())) {
                        hasParameterOfTheSameName = true;
                        break;
                    }
                }
                ExpressionTree reference = hasParameterOfTheSameName ? make.MemberSelect(make.Identifier("this"), name) : make.Identifier(name); // NOI18N
                ExpressionStatementTree assignment = make.ExpressionStatement(make.Assignment(reference, expression));
                // insert as the 2nd statement, after potential super call.
                insertStatement(parameter, new TreePath(constructor, constr.getBody()), null, 
                        assignment, constructor.getLeaf() == method.getLeaf());
            }
            return true;
        }

        /**
         * Adds or rewrites assignment or expression usage in the body of 'method'
         * @param parameter working copy
         * @param method 
         * @param target
         * @param assignment 
         */
        private void insertStatement(WorkingCopy parameter, 
                TreePath target, Tree anchor, StatementTree assignment, boolean replace) {
            if ((variableRewrite || expressionStatementRewrite) && replace) {
                parameter.rewrite(toRemoveFromParent.getLeaf(), assignment);
                toRemoveFromParent = null;
            } else {
                Utilities.insertStatement(parameter, target, anchor, Collections.singletonList(assignment), null, 0);
            }
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            WorkingCopy parameter = WorkingCopy.get(resultIterator.getParserResult());
            parameter.toPhase(JavaSource.Phase.RESOLVED);
            TreePath resolved = handle.resolve(parameter);
            if (resolved == null) {
                return; //TODO...
            }
            TypeMirror tm = IntroduceHint.resolveType(parameter, resolved);
            if (tm == null) {
                return; //TODO...
            }
            tm = Utilities.convertIfAnonymous(Utilities.resolveTypeForDeclaration(parameter, tm));
            TreePath pathToClass = findTargetClass(parameter, resolved);
            if (pathToClass == null) {
                return; //TODO...
            }
            Tree original = resolved.getLeaf();
            variableRewrite = original.getKind() == Tree.Kind.VARIABLE;
            final ExpressionTree expression;
            final TreePath matchPath;
            if (variableRewrite) {
                expression = ((VariableTree) original).getInitializer();
                matchPath = new TreePath(resolved, expression);
            } else {
                expression = (ExpressionTree) resolved.getLeaf();
                matchPath = resolved;
            }
            Set<Modifier> mods = declareFinal ? EnumSet.of(Modifier.FINAL) : EnumSet.noneOf(Modifier.class);
            if (statik) {
                mods.add(Modifier.STATIC);
            }
            mods.addAll(access);
            final TreeMaker make = parameter.getTreeMaker();
            boolean isAnyOccurenceStatic = false;
            Set<Tree> allNewUses = Collections.newSetFromMap(new IdentityHashMap<>());
            allNewUses.add(resolved.getLeaf());
            Collection<TreePath> duplicates = new ArrayList<>();
            if (replaceAll) {
                for (TreePath p : SourceUtils.computeDuplicates(parameter, resolved, findOutermostClass(parameter, resolved), new AtomicBoolean())) {
                    if (variableRewrite) {
                        IntroduceHint.removeFromParent(parameter, p);
                    } else {
                        parameter.rewrite(p.getLeaf(), make.Identifier(name));
                        allNewUses.add(p.getLeaf());
                    }
                    Scope occurenceScope = parameter.getTrees().getScope(p);
                    if (parameter.getTreeUtilities().isStaticContext(occurenceScope)) {
                        isAnyOccurenceStatic = true;
                    }
                    duplicates.add(p);
                }
            }
            if (!statik && isAnyOccurenceStatic) {
                mods.add(Modifier.STATIC);
            }
            pathToClass = IntroduceHint.findTargetClassWithDuplicates(pathToClass, duplicates);
            ModifiersTree modsTree = make.Modifiers(mods);
            Tree parentTree = resolved.getParentPath().getLeaf();
            VariableTree field;
            expressionStatementRewrite = parentTree.getKind() == Tree.Kind.EXPRESSION_STATEMENT;
            if (!variableRewrite) {
                Tree varType = make.Type(tm);
                field = make.Variable(modsTree, name, varType, initializeIn == IntroduceFieldPanel.INIT_FIELD ? expression : null);
                parameter.tag(varType, TYPE_TAG);
                if (!expressionStatementRewrite) {
                    Tree nueParent = parameter.getTreeUtilities().translate(parentTree, Collections.singletonMap(resolved.getLeaf(), make.Identifier(name)));
                    parameter.rewrite(parentTree, nueParent);
                    toRemoveFromParent = null;
                } else {
                    toRemoveFromParent = resolved.getParentPath();
                }
            } else {
                Tree originalVarType = ((VariableTree) original).getType();
                field = make.Variable(modsTree, name, originalVarType, initializeIn == IntroduceFieldPanel.INIT_FIELD ? expression : null);
                parameter.tag(originalVarType, TYPE_TAG);
                toRemoveFromParent = resolved;
            }
            nueClass = IntroduceHint.insertField(parameter, (ClassTree) pathToClass.getLeaf(), field, allNewUses, offset);
            TreePath method = TreeUtils.findMethod(resolved);
            // for INIT_FIELD, we want the rewrite below to be processed.
            if ((initializeIn & (IntroduceFieldPanel.INIT_METHOD | IntroduceFieldPanel.INIT_CONSTRUCTORS)) > 0) {
                if (method == null) {
                    return; //TODO...
                }
                switch (initializeIn) {
                    case IntroduceFieldPanel.INIT_METHOD: {
                        if (!initializeFromMethod(parameter, resolved, expression, name, tm)) {
                            return;
                        }
                        break;
                    }

                    case IntroduceFieldPanel.INIT_CONSTRUCTORS: {
                        if (!initializeConstructors(parameter, method, pathToClass, expression, name)) {
                            return;
                        }
                    }
                }
            }
            if (toRemoveFromParent != null) {
                IntroduceHint.removeFromParent(parameter, toRemoveFromParent);
            }
            if (refactorExisting) {
                new ReferenceTransformer(parameter, ElementKind.FIELD, searchResult, name, 
                    parameter.getTrees().getElement(pathToClass)).scan(pathToClass, null);
            }
            parameter.rewrite(pathToClass.getLeaf(), nueClass);
        }
    }
}
