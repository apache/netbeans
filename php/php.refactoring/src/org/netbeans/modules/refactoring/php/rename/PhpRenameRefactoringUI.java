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
package org.netbeans.modules.refactoring.php.rename;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.php.findusages.WhereUsedSupport;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUIBypass;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Radek Matous
 */
public class PhpRenameRefactoringUI implements RefactoringUI, RefactoringUIBypass {

    private final AbstractRefactoring refactoring;
    private final String name;
    private final ElementKind kind;
    private RenamePanel panel;
    private final PhpElementKind phpKind;

    public PhpRenameRefactoringUI(WhereUsedSupport usage) {
        kind = usage.getElementKind();
        phpKind = usage.getPhpElementKind();
        name = getElementName(usage.getName(), kind);
        Collection<Object> lookupContent = new ArrayList<>();
        lookupContent.add(usage);
        this.refactoring = new RenameRefactoring(Lookups.fixed(lookupContent.toArray()));
        this.refactoring.getContext().add(UI.Constants.REQUEST_PREVIEW);
    }

    static String getElementName(final String name, final ElementKind kind) {
        String retval = name;
        if (kind.equals(ElementKind.VARIABLE) || kind.equals(ElementKind.FIELD)) {
            while (retval.length() > 1 && retval.startsWith("$")) { //NOI18N
                retval = retval.substring(1);
            }
        }
        return retval;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(PhpRenameRefactoringUI.class, "LBL_Rename"); //NOI18N
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(PhpRenameRefactoringUI.class, "LBL_Rename_Descr"); //NOI18N
    }

    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new RenamePanel(name, phpKind, parent, NbBundle.getMessage(RenamePanel.class, "LBL_Rename"), true, true); //NOI18N
        }

        return panel;
    }

    @Override
    public Problem setParameters() {
        String newName = panel.getNameValue();
        if (refactoring instanceof RenameRefactoring) {
            ((RenameRefactoring) refactoring).setNewName(newName);
            ((RenameRefactoring) refactoring).getContext().add(new RenameDeclarationFile(panel.renameDeclarationFile(), panel.lowerCaseFileName()));
        }
        return refactoring.checkParameters();
    }

    @Override
    public Problem checkParameters() {
        if (!panel.isUpdateReferences()) {
            return null;
        }
        boolean firstSet = false;
        if (refactoring instanceof RenameRefactoring) {
            final RenameRefactoring renameRefactoring = (RenameRefactoring) refactoring;
            firstSet = renameRefactoring.getNewName() == null;
            renameRefactoring.setNewName(panel.getNameValue());
            renameRefactoring.getContext().add(new RenameDeclarationFile(panel.renameDeclarationFile(), panel.lowerCaseFileName()));
        }
        if (firstSet) {
            return null;
        }
        return refactoring.checkParameters();
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public AbstractRefactoring getRefactoring() {
        return this.refactoring;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.refactoring.php.rename.PhpRenameRefactoringUI");
    }

    @Override
    public boolean isRefactoringBypassRequired() {
        return false; //TODO fix this
    }

    @Override
    public void doRefactoringBypass() throws IOException {
        //TODO implement
    }

    public static final class RenameDeclarationFile {
        private final boolean renameDeclarationFile;
        private final boolean lowerCaseFileName;

        public RenameDeclarationFile(boolean renameDeclarationFile, boolean lowerCaseFileName) {
            this.renameDeclarationFile = renameDeclarationFile;
            this.lowerCaseFileName = lowerCaseFileName;
        }

        public boolean renameDeclarationFile() {
            return renameDeclarationFile;
        }

        public String adjustName(String newName) {
            return lowerCaseFileName ? newName.toLowerCase() : newName;
        }

    }
}
