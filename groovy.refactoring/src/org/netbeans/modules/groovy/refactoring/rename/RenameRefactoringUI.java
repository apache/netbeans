/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.refactoring.rename;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.groovy.refactoring.findusages.model.RefactoringElement;
import static org.netbeans.modules.groovy.refactoring.rename.Bundle.*;
import org.netbeans.modules.groovy.refactoring.ui.RenamePanel;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUIBypass;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Martin Janicek
 */
public class RenameRefactoringUI implements RefactoringUI, RefactoringUIBypass {

    private final RenameRefactoring refactoring;
    private final String name;
    private RenamePanel panel;


    public RenameRefactoringUI(RefactoringElement element) {
        name = element.getName();
        Collection<Object> lookupContent = new ArrayList<Object>();
        lookupContent.add(element);
        refactoring = new RenameRefactoring(Lookups.fixed(lookupContent.toArray()));
        refactoring.getContext().add(UI.Constants.REQUEST_PREVIEW);
    }

    @Override
    @Messages("LBL_Rename=Rename")
    public String getName() {
        return LBL_Rename();
    }

    @Override
    @Messages("LBL_Rename_Descr=Groovy Elements Rename")
    public String getDescription() {
        return LBL_Rename_Descr();
    }

    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new RenamePanel(name, parent, LBL_Rename(), true, true); //NOI18N
        }

        return panel;
    }

    @Override
    public Problem setParameters() {
        refactoring.setNewName(panel.getNameValue());
        return refactoring.checkParameters();
    }

    @Override
    public Problem checkParameters() {
        if (!panel.isUpdateReferences()) {
            return null;
        }
        refactoring.setNewName(panel.getNameValue());
        return refactoring.checkParameters();
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.groovy.refactoring.rename.RenameRefactoringUI");
    }

    @Override
    public boolean isRefactoringBypassRequired() {
        return false;
    }

    @Override
    public void doRefactoringBypass() throws IOException {
    }

}
