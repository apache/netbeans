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
package org.netbeans.modules.cnd.refactoring.introduce;

import java.util.List;
import java.util.Vector;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.refactoring.api.CsmContext;
import org.netbeans.modules.cnd.refactoring.api.IntroduceMethodRefactoring;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 */
public class IntroduceMethodUI implements RefactoringUI {

    private final CsmObject selectedElement;
    private final CsmContext editorContext;
    private IntroduceMethodPanel panel;
    private final IntroduceMethodRefactoring refactoring;

    /** Creates a new instance of ChangeMethodSignatureRefactoring */
    private IntroduceMethodUI(CsmObject selectedElement, CsmContext editorContext) {
        this.refactoring = new IntroduceMethodRefactoring(selectedElement, editorContext);
        this.selectedElement = selectedElement;
        this.editorContext = editorContext;
    }

    public static IntroduceMethodUI create(CsmObject selectedElement, CsmContext info) {
        return new IntroduceMethodUI(selectedElement, info);
    }

    @Override
    public String getDescription() {
        switch(refactoring.getIntroduceMethodContext().getFunctionKind()) {
            case MethodDefinition:
            case MethodDeclarationDefinition:
                return NbBundle.getMessage(IntroduceMethodUI.class, "CAP_IntroduceMethodMethod", refactoring.getFunctionName()); // NOI18N
            case Function:
            default:
                return NbBundle.getMessage(IntroduceMethodUI.class, "CAP_IntroduceMethodFunction", refactoring.getFunctionName()); // NOI18N
        }
    }

    @Override
    public IntroduceMethodPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new IntroduceMethodPanel(refactoring, selectedElement, editorContext, parent);
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
        @SuppressWarnings("unchecked")
        Vector<Vector> data = panel.getTableModel().getDataVector();
        IntroduceMethodRefactoring.ParameterInfo[] paramList = new IntroduceMethodRefactoring.ParameterInfo[data.size()];
        int counter = 0;
        Problem problem = null;
        for (List<Object> row : data) {
            Boolean byRef = (Boolean) row.get(IntroduceMethodRefactoring.PARAM_BY_REF);
            CharSequence name = (CharSequence) row.get(IntroduceMethodRefactoring.PARAM_NAME);
            CharSequence type = (CharSequence) row.get(IntroduceMethodRefactoring.PARAM_TYPE);
            paramList[counter++] = new IntroduceMethodRefactoring.ParameterInfo(byRef, name, type);
        }
        refactoring.setParameterInfo(paramList);
        refactoring.setMethodDefinition(panel.getMethodDefinition());
        refactoring.setFunctionName(panel.getFunctionName());
        refactoring.setMethodCall(panel.getMethodCall());
        refactoring.setMethodDeclaration(panel.getMethodDeclarationString());
        refactoring.setDeclarationInsetOffset(panel.getInsertPoint());
        if (checkOnly) {
            problem = refactoring.fastCheckParameters();
        } else {
            problem = refactoring.checkParameters();
        }
        return problem;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(IntroduceMethodUI.class, "CAP_IntroduceMethod");
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
        return new HelpCtx("IntroduceMethod"); //NOI18N
    }
}
