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

import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.refactoring.api.CsmContext;
import org.netbeans.modules.cnd.refactoring.api.InlineRefactoring;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 */
public class InlineUI implements RefactoringUI {
    
    private InlinePanel panel;
    private final transient InlineRefactoring refactoring;
    private final CsmObject object;
    private final CsmContext context;
    
    private InlineUI(CsmObject object, CsmContext context) {
        this.object = object;
        this.context = context;
        this.refactoring = new InlineRefactoring(object, context);
    }
    
    public static InlineUI create(CsmObject object, CsmContext context) {
        return new InlineUI(object, context);
    }
    
    @Override
    public boolean isQuery() {
        return false;
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(InlineUI.class, "LBL_InlineRefactoring");
    }
    
    @Override
    public String getDescription() {
        return NbBundle.getMessage(InlineUI.class, "DSC_InlineRefactoring");
    }
    
    @Override
    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }
    
    @Override
    public InlinePanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new InlinePanel();
        }
        return panel;
    }
    
    @Override
    public boolean hasParameters() {
        return true;
    }
    
    @Override
    public Problem setParameters() {
        return setParameters(false);
    }
    
    @Override
    public Problem checkParameters() {
        return setParameters(true);
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("EncapsulateFields"); //NOI18N
    }
    
    private Problem setParameters(boolean checkOnly) {
        @SuppressWarnings("unchecked")
        Problem problem = null;
        refactoring.setApplyPlace(panel.getWhere());
        if (checkOnly) {
            problem = refactoring.fastCheckParameters();
        } else {
            problem = refactoring.checkParameters();
        }
        return problem;
    }
    
}
