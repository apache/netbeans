/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
