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

package org.netbeans.modules.java.hints;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.JComponent;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.modules.java.hints.errors.CreateElementUtilities;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 * @author Sam Halliday
 */
public class FieldForUnusedParam extends AbstractHint {

    private static final String ERROR = "<error>"; //TODO: there should ideally be an API method for this
    private static final String FINAL_FIELDS = "final-fields";

    public static boolean isFinalFields(Preferences p) {
        return p.getBoolean(FINAL_FIELDS, true);
    }

    static void setFinalFields(Preferences p, boolean selected) {
        p.putBoolean(FINAL_FIELDS, selected);
    }

    public FieldForUnusedParam() {
        super(true, true, HintSeverity.CURRENT_LINE_WARNING);
    }

    private final AtomicBoolean cancel = new AtomicBoolean();
    
    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.VARIABLE);
    }

    public List<ErrorDescription> run(CompilationInfo compilationInfo, TreePath treePath) {
        return run(compilationInfo, treePath, CaretAwareJavaSourceTaskFactory.getLastPosition(compilationInfo.getFileObject()));
    }
    
    List<ErrorDescription> run(final CompilationInfo info, TreePath treePath, int offset) {
        cancel.set(false);
        
        if (!getTreeKinds().contains(treePath.getLeaf().getKind())) {
            return null;
        }
        
        if (treePath.getParentPath() == null || treePath.getParentPath().getLeaf().getKind() != Kind.METHOD) {
            return null;
        }
        
        final Element el = info.getTrees().getElement(treePath);
        
        if (el == null || el.getKind() != ElementKind.PARAMETER) {
            return null;
        }
        
        MethodTree parent = (MethodTree) treePath.getParentPath().getLeaf();
        Element parentEl = info.getTrees().getElement(treePath.getParentPath());
        
        if (parentEl == null || parentEl.getKind() != ElementKind.CONSTRUCTOR || parent.getBody() == null) {
            return null;
        }

        VariableTree var = (VariableTree) treePath.getLeaf();

        if (var.getName().contentEquals(ERROR)) return null;
        
        boolean existing = false;
        
        for (VariableElement field : ElementFilter.fieldsIn(parentEl.getEnclosingElement().getEnclosedElements())) {
            if (cancel.get()) {
                return null;
            }
            
            if (field.getSimpleName().equals(el.getSimpleName())) {
                if (!info.getTypes().isAssignable(field.asType(), el.asType())) {
                    return null;
                }
                
                existing = true;
                break;
            }
        }
        
        @SuppressWarnings("serial")
        class Result extends RuntimeException {
            @Override
            public synchronized Throwable fillInStackTrace() {
                return this;
            }
        }
        
        boolean found = false;

        try {
            new CancellableTreePathScanner<Void, Void>(cancel) {
                @Override
                public Void visitIdentifier(IdentifierTree node, Void p) {
                    Element e = info.getTrees().getElement(getCurrentPath());

                    if (el.equals(e)) {
                        throw new Result();
                    }
                    return super.visitIdentifier(node, p);
                }
            }.scan(new TreePath(treePath.getParentPath(), parent.getBody()), null);
        } catch (Result r) {
            found = true;
        }
        
        if (cancel.get() || found) {
            return null;
        }

        final VariableTree vt = (VariableTree) treePath.getLeaf();
        final String name = vt.getName().toString();

        List<Fix> fix = Collections.<Fix>singletonList(new FixImpl(info.getJavaSource(), TreePathHandle.create(treePath, info), isFinalFields(getPreferences(null)), existing, name));
        String displayName = NbBundle.getMessage(FieldForUnusedParam.class, "ERR_UnusedParameter", name);
        ErrorDescription err = ErrorDescriptionFactory.createErrorDescription(Severity.HINT, displayName, fix, info.getFileObject(), offset, offset);

        return Collections.singletonList(err);
    }

    public String getId() {
        return FieldForUnusedParam.class.getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(FieldForUnusedParam.class, "DN_FieldUnusedParam");
    }

    @Override
    public JComponent getCustomizer(Preferences node) {
        JComponent customizer = new FieldForUnusedParamCustomizer(node);
        setFinalFields(node, isFinalFields(node));
        return customizer;
    }

    public void cancel() {
        cancel.set(true);
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(FieldForUnusedParam.class, "DSC_FieldUnusedParam");
    }

    static final class FixImpl implements Fix {

        private final JavaSource js;
        private final TreePathHandle tph;
        private final boolean finalFields;
        private final String fieldName;
        // used from tests
        final boolean existing;

        public FixImpl(JavaSource js, TreePathHandle tph, boolean finalFields, boolean existing, String fieldName) {
            this.js = js;
            this.tph = tph;
            this.finalFields = finalFields;
            this.existing = existing;
            this.fieldName = fieldName;
        }

        public String getText() {
            return existing ? NbBundle.getMessage(FieldForUnusedParam.class, "FIX_AssignToExisting", fieldName) : NbBundle.getMessage(FieldForUnusedParam.class, "FIX_CreateField", fieldName);
        }

        public ChangeInfo implement() throws Exception {
            js.runModificationTask(new Task<WorkingCopy>() {
                public void run(WorkingCopy wc) throws Exception {
                    wc.toPhase(Phase.PARSED);
                    
                    TreePath variable = tph.resolve(wc);
                    
                    if (variable == null) {
                        return ;
                    }
                    
                    VariableTree vt = (VariableTree) variable.getLeaf();
                    MethodTree   mt = (MethodTree) variable.getParentPath().getLeaf();
                    ClassTree    ct = (ClassTree) variable.getParentPath().getParentPath().getLeaf();
                    TreeMaker    make = wc.getTreeMaker();
                    Name         before = null;
                    int          index = 0;
                    
                    for (VariableTree p : mt.getParameters()) {
                        if (p == vt) {
                            if (mt.getParameters().size() > index + 1) {
                                before = mt.getParameters().get(index + 1).getName();
                            }
                            
                            break;
                        }
                        
                        index++;
                    }
                    
                    if (!existing) {
                        Set<Modifier> modifiers = EnumSet.of(Modifier.PRIVATE);
                        // we know the variable is not used in the ctor - no need to flow-analyze the body.
                        if (finalFields && CreateElementUtilities.canDeclareVariableFinal(wc, variable.getParentPath(), null)) {
                            modifiers.add(Modifier.FINAL);
                        }
                        VariableTree field = make.Variable(make.Modifiers(modifiers), vt.getName(), vt.getType(), null);
                        int insertPlace = -1;
                        
                        index = 0;
                        
                        for (Tree member : ct.getMembers()) {
                            if (member.getKind() == Kind.VARIABLE && ((VariableTree) member).getName().equals(before)) {
                                insertPlace = index;
                                break;
                            }
                            
                            index++;
                        }

                        wc.rewrite(ct, insertPlace != (-1) ? make.insertClassMember(ct, insertPlace, field) : GeneratorUtilities.get(wc).insertClassMember(ct, field));
                    }
                    
                    StatementTree assignment = make.ExpressionStatement(make.Assignment(make.MemberSelect(make.Identifier("this"), vt.getName()), make.Identifier(vt.getName()))); // NOI18N
                    
                    int insertPlace = -1;

                    index = 0;

                    for (StatementTree st : mt.getBody().getStatements()) {
                        if (st.getKind() == Kind.EXPRESSION_STATEMENT) {
                            ExpressionStatementTree est = (ExpressionStatementTree) st;
                            
                            if (est.getExpression().getKind() == Kind.ASSIGNMENT) {
                                AssignmentTree at = (AssignmentTree) est.getExpression();
                                
                                if (at.getVariable().getKind() == Kind.MEMBER_SELECT) {
                                    MemberSelectTree mst = (MemberSelectTree) at.getVariable();
                                    
                                    if (mst.getIdentifier().equals(before) && mst.getExpression().getKind() == Kind.IDENTIFIER && ((IdentifierTree) mst.getExpression()).getName().contentEquals("this")) { // NOI18N
                                        insertPlace = index;
                                        break;
                                    }
                                }
                            }
                        }

                        index++;
                    }
                    
                    wc.rewrite(mt.getBody(), insertPlace != (-1) ? make.insertBlockStatement(mt.getBody(), insertPlace, assignment) : make.addBlockStatement(mt.getBody(), assignment));
                }
            }).commit();
            
            return null;
        }
        
    }
    
}
