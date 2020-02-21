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
package org.netbeans.modules.cnd.refactoring.ui;

import java.text.MessageFormat;
import java.util.List;
import java.util.Vector;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.refactoring.api.ChangeParametersRefactoring;
import org.netbeans.modules.cnd.refactoring.api.CsmContext;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
* (based on Java version)
 * 
 */
public class ChangeParametersUI implements RefactoringUI {
    
    private final CsmObject selectedElement;
    private final CsmContext editorContext;
    private ChangeParametersPanel panel;
    private final ChangeParametersRefactoring refactoring;
    
    /** Creates a new instance of ChangeMethodSignatureRefactoring */
    private ChangeParametersUI(CsmObject selectedElement, CsmContext editorContext) {
        this.refactoring = new ChangeParametersRefactoring(selectedElement, editorContext);
        this.selectedElement = selectedElement;
        this.editorContext = editorContext;
    }
    
    public static ChangeParametersUI create(CsmObject selectedElement, CsmContext editorContext) {
        return new ChangeParametersUI(selectedElement, editorContext);
    }
    
    @Override
    public String getDescription() {
        String msg = NbBundle.getMessage(ChangeParametersUI.class, 
                                        "DSC_ChangeParsRootNode"); // NOI18N
        String name = CsmRefactoringUtils.getSimpleText(selectedElement);
        boolean isConstructor = CsmKindUtilities.isConstructor(selectedElement);
        return new MessageFormat(msg).format(new Object[] { 
            name,
            NbBundle.getMessage(ChangeParametersUI.class, "DSC_ChangeParsRootNode" + (isConstructor ? "Constr" : "Method")),
            panel.genDeclarationString()
       });
    }
    
    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new ChangeParametersPanel(selectedElement, editorContext, parent);
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
        ChangeParametersRefactoring.ParameterInfo[] paramList = new ChangeParametersRefactoring.ParameterInfo[data.size()];
        int counter = 0;
        Problem problem = null;
        for (List<Object> row : data) {
            int origIndex = ((Integer) row.get(ChangeParametersPanel.PARAM_ORIG_INDEX)).intValue();
            CharSequence name = (CharSequence) row.get(ChangeParametersPanel.PARAM_NAME);
            CharSequence type = (CharSequence) row.get(ChangeParametersPanel.PARAM_TYPE);
            CharSequence defaultVal = (CharSequence) row.get(ChangeParametersPanel.PARAM_VALUE);
            paramList[counter++] = new ChangeParametersRefactoring.ParameterInfo(origIndex, name, type, defaultVal);
        }
        CsmVisibility visibility = panel.getModifier();
        refactoring.setParameterInfo(paramList);
        refactoring.setVisibility(visibility);
        refactoring.setUseDefaultValueOnlyInFunctionDeclaration(panel.isUseDefaultValueOnlyInFunctionDeclaration());
        if (checkOnly) {
            problem = refactoring.fastCheckParameters();
        } else {
            problem = refactoring.checkParameters();
        }
        return problem;
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(ChangeParametersUI.class, "LBL_ChangeMethodSignature");
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
        return new HelpCtx("ChangeFunctionParameters"); //NOI18N
    }
}
