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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.java.hints.spi.ErrorRule.Data;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/**
 *
 * @author Tomas Zezula
 */
public class NotInitializedVariable implements ErrorRule<Void> {
    
    private static final String DIAGNOSTIC_KEY = "compiler.err.var.might.not.have.been.initialized";
    private volatile boolean canceled;
    
    public NotInitializedVariable () {        
    }

    public Set<String> getCodes() {
        return Collections.<String>singleton(DIAGNOSTIC_KEY);
    }

    public List<Fix> run(CompilationInfo compilationInfo, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        assert DIAGNOSTIC_KEY.equals(diagnosticKey);
        final List<Fix> result = new ArrayList<Fix> ();        
        if (!canceled) {
            final Trees t = compilationInfo.getTrees();
            final Element e = t.getElement(treePath);            
            if (!canceled && e != null && e.getKind() == ElementKind.LOCAL_VARIABLE) {
                TreePath declaration = t.getPath(e);
                if (!canceled && declaration != null) {
                    result.add(new NIVFix(compilationInfo.getFileObject(),e.getSimpleName().toString(),TreePathHandle.create(declaration, compilationInfo)));
                }
            }            
        }
        return Collections.unmodifiableList(result);
    }

    public String getId() {
        return "NotInitializedVariable";    //NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(NotInitializedVariable.class, "LBL_NotInitializedVariable");
    }

    public void cancel() {
        this.canceled = true;
    }
    
    
    private static class NIVFix implements Fix {
        
        private final FileObject file;
        private final String variableName;
        private final TreePathHandle variable;
        
        public NIVFix (final FileObject file, final String variableName, final TreePathHandle variable) {
            assert file != null;
            assert variableName != null;
            assert variable != null;
            this.file = file;
            this.variableName = variableName;
            this.variable = variable;
        }

        public String getText() {
            return NbBundle.getMessage(NotInitializedVariable.class, "LBL_NotInitializedVariable_fix",variableName); //NOI18N
        }

        public ChangeInfo implement() throws Exception {
            final JavaSource js = JavaSource.forFileObject(this.file);
            if (js != null) {
                js.runModificationTask(new Task<WorkingCopy>() {
                    public void run(final WorkingCopy wc) throws Exception {
                         wc.toPhase(Phase.RESOLVED);
                        TreePath tp = variable.resolve(wc);                        
                        if (tp == null)
                            return;                        
                        VariableTree vt = (VariableTree) tp.getLeaf();
                        ExpressionTree init = vt.getInitializer();
                        if (init != null) {
                            return;
                        }
                        Element decl = wc.getTrees().getElement(tp);
                        if (decl == null) {
                            return;
                        }
                        TypeMirror type = decl.asType();
                        TypeKind kind = type.getKind();
                        Object value;
                        if (kind.isPrimitive()) {
                            if (kind == TypeKind.BOOLEAN) {
                                value = false;
                            }
                            else {
                                value = 0;
                            }
                        }
                        else {
                            value = null;
                        }
                        ExpressionTree newInit = wc.getTreeMaker().Literal(value);
                        VariableTree newVt = wc.getTreeMaker().Variable(
                                vt.getModifiers(),
                                vt.getName(),
                                vt.getType(),
                                newInit);
                        wc.rewrite(vt, newVt);
                    }
                }).commit();
            }
            return null;
        }
        
    }

}
