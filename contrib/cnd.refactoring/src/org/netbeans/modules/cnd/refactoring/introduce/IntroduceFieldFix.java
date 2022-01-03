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

import java.io.IOException;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 */
final class IntroduceFieldFix implements Fix {
    private final String guessedName;
    private final CsmObject handle;
    private final CsmFile csmFile;
    private final int numDuplicates;
    private final int[] initilizeIn;
    private final boolean statik;
    private final boolean allowFinalInCurrentMethod;

    public IntroduceFieldFix(CsmObject handle, CsmFile csmFile, String guessedName, int numDuplicates, int[] initilizeIn, boolean statik, boolean allowFinalInCurrentMethod) {
        this.handle = handle;
        this.csmFile = csmFile;
        this.guessedName = guessedName;
        this.numDuplicates = numDuplicates;
        this.initilizeIn = initilizeIn;
        this.statik = statik;
        this.allowFinalInCurrentMethod = allowFinalInCurrentMethod;
    }

    @Override
    public String getText() {
        return NbBundle.getMessage(IntroduceFieldFix.class, "FIX_IntroduceField");
    }

    @Override
    public String toString() {
        return "[IntroduceField:" + guessedName + ":" + numDuplicates + ":" + statik + ":" + allowFinalInCurrentMethod + ":" + Arrays.toString(initilizeIn) + "]"; // NOI18N
    }

    @Override
    public ChangeInfo implement() throws IOException, BadLocationException {
        JButton btnOk = new JButton(NbBundle.getMessage(IntroduceFieldFix.class, "LBL_Ok"));
        btnOk.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(IntroduceFieldFix.class, "AD_IntrHint_OK"));
        JButton btnCancel = new JButton(NbBundle.getMessage(IntroduceFieldFix.class, "LBL_Cancel"));
        btnCancel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(IntroduceFieldFix.class, "AD_IntrHint_Cancel"));
        IntroduceFieldPanel panel = new IntroduceFieldPanel(guessedName, initilizeIn, numDuplicates, allowFinalInCurrentMethod, btnOk);
        String caption = NbBundle.getMessage(IntroduceFieldFix.class, "CAP_IntroduceField");
        DialogDescriptor dd = new DialogDescriptor(panel, caption, true, new Object[]{btnOk, btnCancel}, btnOk, DialogDescriptor.DEFAULT_ALIGN, null, null);
        if (DialogDisplayer.getDefault().notify(dd) != btnOk) {
            return null; //cancel
        }
        return null;
    }

}
