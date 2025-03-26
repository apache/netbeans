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
        this.files = fs.toArray(new FileObject[0]);
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
