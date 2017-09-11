/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.introduce;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
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
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;
import javax.swing.JButton;
import javax.swing.text.BadLocationException;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.errors.Utilities;
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
    private final int[] initilizeIn;
    private final boolean statik;
    private final boolean allowFinalInCurrentMethod;
    private final boolean permitDuplicates;
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
    public IntroduceFieldFix(TreePathHandle handle, JavaSource js, String guessedName, 
            int numDuplicates, int[] initilizeIn, boolean statik, boolean allowFinalInCurrentMethod, int offset, TreePathHandle target) {
        this(handle, js, guessedName, numDuplicates, initilizeIn, statik, allowFinalInCurrentMethod, offset, false, target);
    }
    
    public IntroduceFieldFix(TreePathHandle handle, JavaSource js, String guessedName, 
            int numDuplicates, int[] initilizeIn, boolean statik, boolean allowFinalInCurrentMethod, int offset, boolean allowDuplicates, TreePathHandle target) {
        super(js, handle, numDuplicates, offset);
        this.guessedName = guessedName;
        this.initilizeIn = initilizeIn;
        this.statik = statik;
        this.allowFinalInCurrentMethod = allowFinalInCurrentMethod;
        this.permitDuplicates = allowDuplicates;
        this.targetHandle = target;
    }
    
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

    public ChangeInfo implement() throws IOException, BadLocationException {
        JButton btnOk = new JButton(NbBundle.getMessage(IntroduceHint.class, "LBL_Ok"));
        btnOk.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(IntroduceHint.class, "AD_IntrHint_OK"));
        JButton btnCancel = new JButton(NbBundle.getMessage(IntroduceHint.class, "LBL_Cancel"));
        btnCancel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(IntroduceHint.class, "AD_IntrHint_Cancel"));
        IntroduceFieldPanel panel = createPanel(btnOk);
        FieldValidator fv = new FieldValidator(js, null);
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
        js.runModificationTask(new Worker(panel.getFieldName(), permitDuplicates && panel.isReplaceAll(),
                panel.isDeclareFinal(), panel.getAccess(), panel.getInitializeIn(), 
                fv.getLastResult(),
                panel.isRefactorExisting())).commit();
        return null;
    }
    
    /**
     * The actual modification. Some javac related data are recorded in fields, inner class prevents
     * unintentional leak if someone keeps a reference to the Fix
     */
    final class Worker implements Task<WorkingCopy> {
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
            BlockTree statements = (BlockTree) statementPath.getParentPath().getLeaf();
            StatementTree statement = (StatementTree) statementPath.getLeaf();
            int index = statements.getStatements().indexOf(statement);
            if (index == (-1)) {
                //really strange...
                return false;
            }
            insertStatement(parameter, statementPath.getParentPath(), statement, assignment, true);
            return true;
        }

        private boolean initializeConstructors(WorkingCopy parameter, 
                TreePath method, TreePath pathToClass, ExpressionTree expression, String name) {
            TreeMaker make = parameter.getTreeMaker();
            for (TreePath constructor : TreeUtils.findConstructors(parameter, method)) {
                //check for syntetic constructor:
                if (parameter.getTreeUtilities().isSynthetic(constructor)) {
                    List<StatementTree> nueStatements = new LinkedList<StatementTree>();
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

        public void run(WorkingCopy parameter) throws Exception {
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
            Set<Tree> allNewUses = Collections.newSetFromMap(new IdentityHashMap<Tree, Boolean>());
            allNewUses.add(resolved.getLeaf());
            Collection<TreePath> duplicates = new ArrayList<TreePath>();
            if (replaceAll) {
                for (TreePath p : SourceUtils.computeDuplicates(parameter, resolved, new TreePath(parameter.getCompilationUnit()), new AtomicBoolean())) {
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
                field = make.Variable(modsTree, name, make.Type(tm), initializeIn == IntroduceFieldPanel.INIT_FIELD ? expression : null);
                if (!expressionStatementRewrite) {
                    Tree nueParent = parameter.getTreeUtilities().translate(parentTree, Collections.singletonMap(resolved.getLeaf(), make.Identifier(name)));
                    parameter.rewrite(parentTree, nueParent);
                    toRemoveFromParent = null;
                } else {
                    toRemoveFromParent = resolved.getParentPath();
                }
            } else {
                VariableTree originalVar = (VariableTree) original;
                field = make.Variable(modsTree, name, originalVar.getType(), initializeIn == IntroduceFieldPanel.INIT_FIELD ? expression : null);
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
