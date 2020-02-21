/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
