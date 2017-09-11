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

package org.netbeans.modules.git.options;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.git.Annotator.LabelVariable;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.versioning.util.VCSOptionsKeywordsProvider;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

final class GitOptionsPanelController extends OptionsPanelController implements VCSOptionsKeywordsProvider,
        ActionListener, DocumentListener {
    
    private GitOptionsPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;
    
    public GitOptionsPanelController() { }

    @Override
    public void update() {
        load();
        changed = false;
    }
    
    @Override
    public void applyChanges() {
        if (!validateFields()) return;
        store();        
        changed = false;
    }
    
    @Override
    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }
    
    @Override
    public boolean isValid() {
        return true;
    }
    
    @Override
    public boolean isChanged() {
        return changed;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.git.options.GitOptionsPanelController"); //NOI18N
    }
    
    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }            

    @Override
    public boolean acceptKeywords (List<String> keywords) {
        Set<String> allKeywords = new HashSet<String>(panel.getKeywords());
        allKeywords.retainAll(keywords);
        return !allKeywords.isEmpty();
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        if (e.getSource() == panel.btnAddVariable) {
            onAddClick();
        } else {
            changed();
        }
    }

    @Override
    public void insertUpdate (DocumentEvent e) {
        changed();
    }

    @Override
    public void removeUpdate (DocumentEvent e) {
        changed();
    }

    @Override
    public void changedUpdate (DocumentEvent e) {
    }

    private Boolean validateFields() {
        
        return true;
    }

    private GitOptionsPanel getPanel() {
        if (panel == null) {
            panel = new GitOptionsPanel();
            panel.btnAddVariable.addActionListener(this);
            panel.cbIgnoreNotSharableFiles.addActionListener(this);
            panel.cbOpenOutputWindow.addActionListener(this);
            panel.excludeNewFiles.addActionListener(this);
            panel.signOffCheckBox.addActionListener(this);
            panel.txtProjectAnnotation.setText(GitModuleConfig.getDefault().getProjectAnnotationFormat());
            panel.txtProjectAnnotation.getDocument().addDocumentListener(this);
        }
        return panel;
    }
    
    private void changed () {
        fireChanged();
        if (!changed) {
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }
    
    private void load () {
        getPanel();
        panel.cbOpenOutputWindow.setSelected(GitModuleConfig.getDefault().getAutoOpenOutput());
        panel.excludeNewFiles.setSelected(GitModuleConfig.getDefault().getExludeNewFiles());
        panel.signOffCheckBox.setSelected(GitModuleConfig.getDefault().getSignOff());
        panel.cbIgnoreNotSharableFiles.setSelected(GitModuleConfig.getDefault().getAutoIgnoreFiles());
        panel.txtProjectAnnotation.setText(GitModuleConfig.getDefault().getProjectAnnotationFormat());
    }

    private void store () {
        getPanel();
        GitModuleConfig.getDefault().setAutoOpenOutput(panel.cbOpenOutputWindow.isSelected());
        GitModuleConfig.getDefault().setExcludeNewFiles(panel.excludeNewFiles.isSelected());
        GitModuleConfig.getDefault().setSignOff(panel.signOffCheckBox.isSelected());
        GitModuleConfig.getDefault().setAutoIgnoreFiles(panel.cbIgnoreNotSharableFiles.isSelected());
        GitModuleConfig.getDefault().setProjectAnnotationFormat(panel.txtProjectAnnotation.getText());
        Git.getInstance().getVCSAnnotator().refreshFormat();
    }
    
    private void fireChanged() {
        changed = GitModuleConfig.getDefault().getAutoOpenOutput() != panel.cbOpenOutputWindow.isSelected()
                || GitModuleConfig.getDefault().getExludeNewFiles() != panel.excludeNewFiles.isSelected()
                || GitModuleConfig.getDefault().getSignOff() != panel.signOffCheckBox.isSelected()
                || GitModuleConfig.getDefault().getAutoIgnoreFiles() != panel.cbIgnoreNotSharableFiles.isSelected()
                || !GitModuleConfig.getDefault().getProjectAnnotationFormat().equals(panel.txtProjectAnnotation.getText());
    }

    @NbBundle.Messages({
        "GitOptionsPanel.labelVariables.title=Add Variable",
        "GitOptionsPanel.labelVariables.acsd=Select a variable to add to the annotation pattern"
    })
    private void onAddClick () {
        getPanel();
        LabelsPanel labelsPanel = new LabelsPanel();
        LabelVariable[] variables = Git.getInstance().getVCSAnnotator().getProjectVariables();
        labelsPanel.labelsList.setListData(variables);

        String title = Bundle.GitOptionsPanel_labelVariables_title();
        String acsd = Bundle.GitOptionsPanel_labelVariables_acsd();

        DialogDescriptor dialogDescriptor = new DialogDescriptor(labelsPanel, title, true,
                DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, null);

        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.getAccessibleContext().setAccessibleDescription(acsd);

        labelsPanel.labelsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked (MouseEvent e) {
                if (e.getClickCount() == 2) {
                    dialog.setVisible(false);
                }
            }
        });

        dialog.setVisible(true);

        if (DialogDescriptor.OK_OPTION == dialogDescriptor.getValue()) {

            Object[] selection = (Object[]) labelsPanel.labelsList.getSelectedValues();

            String variable = ""; // NOI18N
            for (Object o : selection) {
                variable += ((LabelVariable) o).getPattern(); // NOI18N
            }

            String annotation = panel.txtProjectAnnotation.getText();

            int pos = panel.txtProjectAnnotation.getCaretPosition();
            if (pos < 0) {
                pos = annotation.length();
            }

            StringBuilder sb = new StringBuilder(annotation.length() + variable.length());
            sb.append(annotation.substring(0, pos));
            sb.append(variable);
            if (pos < annotation.length()) {
                sb.append(annotation.substring(pos, annotation.length()));
            }
            panel.txtProjectAnnotation.setText(sb.toString());
            panel.txtProjectAnnotation.requestFocus();
            panel.txtProjectAnnotation.setCaretPosition(pos + variable.length());
        }
    }
 
}
