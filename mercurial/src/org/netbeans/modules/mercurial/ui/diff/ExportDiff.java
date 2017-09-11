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
package org.netbeans.modules.mercurial.ui.diff;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;
import java.io.File;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.versioning.util.ExportDiffSupport;

/**
 *
 * @author Padraig O'Briain
 */
public abstract class ExportDiff extends ExportDiffSupport {

    private ExportDiffPanel panel;
    private File fileToDiff;
    private AbstractExportDiffPanel aedp;
    private DocumentListener listener;
    private DialogDescriptor dd;
    
    
    /** Creates a new instance of ExportDiff */
    public ExportDiff(File repository, HgLogMessage repoRev, File [] roots, File fileToDiff) {
        super(roots == null ? new File[] {fileToDiff} : roots, HgModuleConfig.getDefault().getPreferences());
        this.fileToDiff = fileToDiff;

        panel = new ExportDiffPanel(repository, repoRev, roots, fileToDiff);
    } 
    
    public ExportDiff(File repository, HgLogMessage repoRev, File [] roots) {
        this(repository, repoRev, roots, null);
    }
    
    public ExportDiff(File repository, File [] roots) {
        this(repository, null, roots, null);
    }

    private void nameChange() {
        if (aedp.getOutputFileText().trim().length() > 0)
            dd.setValid(true);
        else
            dd.setValid(false);
    }

    public String getOutputFileName() {
        if (panel == null) return null;
        return panel.getOutputFileName().trim();
    }

    public String getSelectionRevision() {
        if (panel == null) return null;
        return panel.getSelectedRevisionCSetId();
    }

    @Override
    protected AbstractExportDiffPanel createSimpleDialog(String currentFilePath) {
        aedp = new ExportAsFilePanel();
        listener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { nameChange(); }
            public void removeUpdate(DocumentEvent e) { nameChange(); }
            public void changedUpdate(DocumentEvent e) { nameChange(); }
        };
        panel.setInsidePanel(aedp);

        if(fileToDiff != null){
            dd = new DialogDescriptor(panel, org.openide.util.NbBundle.getMessage(ExportDiff.class,
                "CTL_ExportFileDialog")); // NOI18N
        } else{
            dd = new DialogDescriptor(panel, org.openide.util.NbBundle.getMessage(ExportDiff.class,
                "CTL_ExportDialog")); // NOI18N
        }
        
        aedp.addOutputFileTextDocumentListener(listener);
        dd.setModal(true);
        dd.setHelpCtx(new HelpCtx(this.getClass()));
        dd.setValid(false);

        return aedp;
    }

    @Override
    protected void createComplexDialog(AbstractExportDiffPanel insidePanel) {
        panel.setInsidePanel(insidePanel);
        if(fileToDiff != null){
            dd = new DialogDescriptor(panel, org.openide.util.NbBundle.getMessage(ExportDiff.class,
                "CTL_ExportFileDialog")); // NOI18N
        } else{
            dd = new DialogDescriptor(panel, org.openide.util.NbBundle.getMessage(ExportDiff.class,
                "CTL_ExportDialog")); // NOI18N
        }
    }

    @Override
    protected DialogDescriptor getDialogDescriptor() {
        return dd;
    }
}
