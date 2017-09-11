/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.bridge.exportdiff;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.versioning.util.ExportDiffSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Stupka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.versioning.util.ExportDiffSupport.ExportDiffProvider.class)
public class ExportDiffProviderImpl extends ExportDiffSupport.ExportDiffProvider implements DocumentListener, ChangeListener {

    private AttachPanel panel;
    private FileObject[] files;
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.bugtracking.exportdiff.AttachIssue");   // NOI18N

    public ExportDiffProviderImpl() {
    }

    @Override
    protected void setContext(File[] files) {
        List<FileObject> fs = new LinkedList<FileObject>();
        for (File file : files) {
            FileObject fo = FileUtil.toFileObject(file);
            if(fo != null) {
                fs.add(fo);
            }
        }
        this.files = fs.toArray(new FileObject[fs.size()]);
    }

    @Override
    public void handleDiffFile(File file) {
        LOG.log(Level.FINE, "handeDiff start for {0}", file); // NOI18N

        Issue issue = panel.getIssue();
        if (issue == null) {
            LOG.log(Level.FINE, " no issue set"); // NOI18N
            return;
        }
        
        issue.attachFile(file, panel.descriptionTextField.getText(), true);
        issue.open();

        LOG.log(Level.FINE, "handeDiff end for {0}", file); // NOI18N
    }

    @Override
    public JComponent createComponent() {
        assert files != null;
        panel = new AttachPanel(this, files.length > 0 ? files[0] : null);
        panel.descriptionTextField.getDocument().addDocumentListener(this);        
        return panel;
    }

    @Override
    public boolean isValid() {
        return !panel.descriptionTextField.getText().trim().equals("") &&       // NOI18N
                panel.getIssue() != null;
    }

    @Override public void insertUpdate(DocumentEvent e)  { fireDataChanged(); }
    @Override public void removeUpdate(DocumentEvent e)  { fireDataChanged(); }
    @Override public void changedUpdate(DocumentEvent e) { fireDataChanged(); }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireDataChanged();
    }
}
