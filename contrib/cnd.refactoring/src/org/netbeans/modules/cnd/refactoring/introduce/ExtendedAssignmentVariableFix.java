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

import javax.swing.JButton;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.refactoring.actions.RefactoringKind;
import org.netbeans.modules.cnd.refactoring.hints.AssignmentVariableFix;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 */
public class ExtendedAssignmentVariableFix extends AssignmentVariableFix {
    private int numDuplicates;
    private final RefactoringKind kind;
    private String type;

    public ExtendedAssignmentVariableFix(CsmExpression expression, Document doc, FileObject fo) {
        super(expression, doc, fo);
        kind = RefactoringKind.CREATE_VARIABLE;
    }

    public String getKeyExt() {
        switch (kind) {
            case CREATE_CONSTANT:
                return "IntroduceConstant"; //NOI18N
            case CREATE_VARIABLE:
                return "IntroduceVariable"; //NOI18N
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public String getText() {
        return NbBundle.getMessage(ExtendedAssignmentVariableFix.class, "FIX_" + getKeyExt()); //NOI18N
    }

    @Override
    protected boolean isInstanceRename() {
        return false;
    }

    @Override
    protected String getType() {
        return type;
    }

    @Override
    protected String suggestName() {
        type = super.getType();
        String guessedName = super.suggestName();
        JButton btnOk = new JButton(NbBundle.getMessage(ExtendedAssignmentVariableFix.class, "LBL_Ok"));
        JButton btnCancel = new JButton(NbBundle.getMessage(ExtendedAssignmentVariableFix.class, "LBL_Cancel"));
        IntroduceVariablePanel panel = new IntroduceVariablePanel(numDuplicates, type, guessedName, kind == RefactoringKind.CREATE_CONSTANT, btnOk);
        String caption = NbBundle.getMessage(ExtendedAssignmentVariableFix.class, "CAP_" + getKeyExt()); //NOI18N
        DialogDescriptor dd = new DialogDescriptor(panel, caption, true, new Object[]{btnOk, btnCancel}, btnOk, DialogDescriptor.DEFAULT_ALIGN, null, null);
        if (DialogDisplayer.getDefault().notify(dd) != btnOk) {
            return null; //cancel
        }
        guessedName = panel.getVariableName();
        type = panel.getType();
        return guessedName;
    }
}
