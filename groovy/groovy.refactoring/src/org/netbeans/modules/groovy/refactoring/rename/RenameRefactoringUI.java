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
