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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.hibernate.refactoring;

import java.io.IOException;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUIBypass;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * 
 */
final class RenameMappingFileRefactoringUI implements RefactoringUI, RefactoringUIBypass {
    private RenameRefactoring refactoring;
    private String originalName;
    private String oldName;
    private String dispOldName;
    private String newName;
    private RenameMappingFilePanel panel;
    private boolean newNameSpecified;
    private FileObject byPassFolder;

    RenameMappingFileRefactoringUI(FileObject jspFileObject, String newName) {
        refactoring = new RenameRefactoring(Lookups.singleton(jspFileObject));
        originalName = jspFileObject.getName();
        if (newName == null) {
            dispOldName = oldName = jspFileObject.getName();
        } else {
            dispOldName = oldName = newName;
            newNameSpecified = true;
        }
    }
    
    public String getName() {
        return NbBundle.getMessage(RenameMappingFilePanel.class, "LBL_Rename");
    }
    
    public String getDescription() {
        return NbBundle.getMessage(RenameMappingFilePanel.class, "DSC_RenameMappingFile", dispOldName, newName);
    }

    public boolean isQuery() {
        return false;
    }

    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new RenameMappingFilePanel(
                            parent,
                            oldName,
                            NbBundle.getMessage(RenameMappingFilePanel.class, "DSC_RenameMappingFile", originalName),
                            !newNameSpecified);
        }
        return panel;
    }
    
    public boolean hasParameters() {
        return true;
    }

    public Problem setParameters() {
        newName = panel.getNameValue();
        refactoring.setNewName(newName);
        return refactoring.checkParameters();
    }
    
    public Problem checkParameters() {
        newName = panel.getNameValue();
        refactoring.setNewName(newName);
        return refactoring.fastCheckParameters();
    }
    
    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    public boolean isRefactoringBypassRequired() {
        return !panel.isUpdateReferences();
    }
    
    public void doRefactoringBypass() throws IOException {
        DataObject dob = null;
        if (byPassFolder != null) {
            dob = DataFolder.findFolder(byPassFolder);
        } else {
            dob = DataObject.find(refactoring.getRefactoringSource().lookup(FileObject.class));
        }
        dob.rename(panel.getNameValue());
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
