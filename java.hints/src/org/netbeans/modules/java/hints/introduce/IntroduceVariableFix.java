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

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
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
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 * Fix to create a local variable
 * @author sdedic
 */
final class IntroduceVariableFix extends IntroduceFixBase implements Fix {

    static TreePath findAddPosition(CompilationInfo info, TreePath original, Set<? extends TreePath> candidates, int[] outPosition) {
        TreePath statement = original;
        for (TreePath p : candidates) {
            Tree leaf = p.getLeaf();
            int leafStart = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), leaf);
            int stPathStart = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), statement.getLeaf());
            if (leafStart < stPathStart) {
                statement = p;
            }
        }
        List<TreePath> allCandidates = new LinkedList<TreePath>();
        allCandidates.add(original);
        allCandidates.addAll(candidates);
        while (statement != null && statement.getParentPath() != null && !TreeUtils.isParentOf(statement.getParentPath(), allCandidates)) {
            statement = statement.getParentPath();
        }
        statement = TreeUtils.findStatementInBlock(statement);
        if (statement == null) {
            //XXX: well....
            return null;
        }
        if (statement.getParentPath() == null) {
            return null; //XXX: log
        }
        if (statement.getParentPath().getLeaf().getKind() == Tree.Kind.LAMBDA_EXPRESSION) {
            // if the lambda was a BLOCK one, the search would terminate at the block.
            // so the lambda is an expression with a single tree, so the 'statement index' will be 0
            outPosition[0] = 0;
            return statement;
        }
        StatementTree statementTree = (StatementTree) statement.getLeaf();
        int index = IntroduceHint.getStatements(statement).indexOf(statementTree);
        if (index == (-1)) {
            //really strange...
            return null;
        }
        outPosition[0] = index;
        return statement;
    }
    private final String guessedName;
    private final TreePathHandle targetHandle;

    public IntroduceVariableFix(TreePathHandle handle, JavaSource js, String guessedName, int numDuplicates, IntroduceKind kind, 
            TreePathHandle methodHandle, int offset) {
        super(js, handle, numDuplicates, offset);
        this.guessedName = guessedName;
        this.targetHandle = methodHandle;
    }

    @Override
    public String toString() {
        return "[IntroduceFix:" + guessedName + ":" + duplicatesCount + ":" + IntroduceKind.CREATE_VARIABLE + "]"; // NOI18N
    }

    public String getKeyExt() {
        return "IntroduceVariable"; //NOI18N
    }

    public String getText() {
        return NbBundle.getMessage(IntroduceHint.class, "FIX_" + getKeyExt()); //NOI18N
    }

    public ChangeInfo implement() throws IOException, BadLocationException {
        JButton btnOk = new JButton(NbBundle.getMessage(IntroduceHint.class, "LBL_Ok"));
        JButton btnCancel = new JButton(NbBundle.getMessage(IntroduceHint.class, "LBL_Cancel"));
        IntroduceFieldPanel panel = new IntroduceFieldPanel(guessedName, null, duplicatesCount, 
                true, handle.getKind() == Tree.Kind.VARIABLE, 
                IntroduceFieldPanel.VARIABLE, 
                "introduceVariable", btnOk);
        String caption = NbBundle.getMessage(IntroduceHint.class, "CAP_" + getKeyExt()); //NOI18N
        DialogDescriptor dd = new DialogDescriptor(panel, caption, true, new Object[]{btnOk, btnCancel}, btnOk, DialogDescriptor.DEFAULT_ALIGN, null, null);
        FieldValidator val = new FieldValidator(js, null);
        panel.setNotifier(dd.createNotificationLineSupport());
        panel.setValidator(val);
        panel.setTarget(targetHandle);
        if (DialogDisplayer.getDefault().notify(dd) != btnOk) {
            return null; //cancel
        }
        final boolean refactor = panel.isRefactorExisting();
        final String name = panel.getFieldName();
        final boolean replaceAll = panel.isReplaceAll();
        final boolean declareFinal = panel.isDeclareFinal();
        final MemberSearchResult search = val.getLastResult();
        js.runModificationTask(new Task<WorkingCopy>() {
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
                if (!Utilities.isValidType(tm)) {
                    return; // TODO... 
                }
                Element targetEl = null;
                TreePath targetPath = null;
                if (targetHandle != null) {
                    targetPath = targetHandle.resolve(parameter);
                    if (targetPath == null) {
                        return;
                    }
                    targetPath = TreeUtils.findClass(targetPath);
                    if (targetPath == null) {
                        return;
                    }
                    targetEl = parameter.getTrees().getElement(targetPath);
                    if (targetEl == null || !(targetEl.getKind().isClass() || targetEl.getKind().isInterface())) {
                        return;
                    }
                }
                Tree original = resolved.getLeaf();
                boolean variableRewrite = original.getKind() == Tree.Kind.VARIABLE;
                ExpressionTree expression = !variableRewrite ? (ExpressionTree) resolved.getLeaf() : ((VariableTree) original).getInitializer();
                ModifiersTree mods;
                final TreeMaker make = parameter.getTreeMaker();
                boolean expressionStatement = resolved.getParentPath().getLeaf().getKind() == Tree.Kind.EXPRESSION_STATEMENT;
                TreePath method = TreeUtils.findMethod(resolved);
                if (method == null) {
                    return; //TODO...
                }
                TreePath statement;
                int index;
                if (replaceAll) {
                    Set<TreePath> candidates = SourceUtils.computeDuplicates(parameter, resolved, method, new AtomicBoolean());
                    for (TreePath p : candidates) {
                        Tree leaf = p.getLeaf();
                        parameter.rewrite(leaf, make.Identifier(name));
                    }
                    int[] out = new int[1];
                    statement = IntroduceVariableFix.findAddPosition(parameter, resolved, candidates, out);
                    if (statement == null) {
                        return;
                    }
                    index = out[0];
                } else {
                    int[] out = new int[1];
                    statement = IntroduceVariableFix.findAddPosition(parameter, resolved, Collections.<TreePath>emptySet(), out);
                    if (statement == null) {
                        return;
                    }
                    index = out[0];
                }
                // handle lambda of expression BodyKind
                List<StatementTree> nueStatements;
                GeneratorUtilities.get(parameter).importComments(IntroduceHint.getStatementOrBlock(statement).getLeaf(), parameter.getCompilationUnit());
                mods = make.Modifiers(declareFinal ? EnumSet.of(Modifier.FINAL) : EnumSet.noneOf(Modifier.class));
                VariableTree newVariable = make.Variable(mods, name, make.Type(tm), expression);
                nueStatements = new ArrayList<>();
                nueStatements.add(make.asReplacementOf(newVariable, resolved.getLeaf(), true));
                if (expressionStatement) {
                    Utilities.replaceStatements(parameter, statement, null, nueStatements);
                } else {
                    Utilities.insertStatement(parameter, statement, nueStatements, null);
                }
                
                if (!expressionStatement) {
                    Tree origParent = resolved.getParentPath().getLeaf();
                    Tree newParent = parameter.getTreeUtilities().translate(origParent, Collections.singletonMap(resolved.getLeaf(), 
                            make.asNew(make.Identifier(name))));
                    parameter.rewrite(origParent, newParent);
                }
                
                if (refactor) {
                    new ReferenceTransformer(parameter, ElementKind.LOCAL_VARIABLE, 
                            search, name, targetEl).scan(statement.getParentPath(), null);
                }
            }
        }).commit();
        return null;
    }
}
