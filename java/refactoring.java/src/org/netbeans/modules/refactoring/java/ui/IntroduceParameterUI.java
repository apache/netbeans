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
package org.netbeans.modules.refactoring.java.ui;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.EnumSet;
import java.util.Set;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.JEditorPane;
import javax.swing.event.ChangeListener;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.api.IntroduceParameterRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Becicka
 */
public class IntroduceParameterUI implements RefactoringUI, JavaRefactoringUIFactory {
    
    private TreePathHandle expression;
    private IntroduceParameterPanel panel;
    private IntroduceParameterRefactoring refactoring;
    private Lookup lookup;
    
    /** Creates a new instance of IntroduceParameterUI */
    private IntroduceParameterUI(TreePathHandle expression) {
        this.refactoring = new IntroduceParameterRefactoring(expression);
        this.expression = expression;
    }

    private IntroduceParameterUI(Lookup lookup) {
        this.lookup = lookup;
    }
    
    @Override
    public RefactoringUI create(CompilationInfo info, TreePathHandle[] handles, FileObject[] files, NonRecursiveFolder[] packages) {
        if(handles.length > 0) {
            TreePath path = handles[0].resolve(info);
            if(path != null && (path.getLeaf().getKind() == Tree.Kind.VARIABLE || path.getLeaf() instanceof ExpressionTree)) {
                return new IntroduceParameterUI(handles[0]);
            }
        }
        return null;
    }
    
    public static JavaRefactoringUIFactory factory(Lookup lookup) {
        return new IntroduceParameterUI(lookup);
    }
    
    @Override
    public String getDescription() {
        return NbBundle.getMessage(IntroduceParameterUI.class, 
                                        "DSC_IntroduceParameterRootNode", refactoring.getParameterName()); // NOI18N
    }
    
    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new IntroduceParameterPanel(expression, parent);
        }
        return panel;
    }
    
    @Override
    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    @Override
    public boolean isQuery() {
        return false;
    }
    
    private Problem setParameters(boolean checkOnly) {
        Problem problem = null;
        refactoring.setFinal(panel.isDeclareFinal());
        refactoring.setParameterName(panel.getParameterName());
        refactoring.setOverloadMethod(panel.isCompatible());
        refactoring.setReplaceAll(panel.isReplaceAll());
        refactoring.getContext().add(panel.getJavadoc());
        if (checkOnly) {
            problem = refactoring.fastCheckParameters();
        } else {
            problem = refactoring.checkParameters();
        }
        return problem;
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(IntroduceParameterUI.class, "LBL_IntroduceParameter");
    }
    
    @Override
    public Problem checkParameters() {
        return setParameters(true);
    }

    @Override
    public Problem setParameters() {
        return setParameters(false);
    }
    
    @Override
    public boolean hasParameters() {
        return true;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.refactoring.java.ui.IntroduceParameterUI"); // NOI18N
    }
}
