/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
