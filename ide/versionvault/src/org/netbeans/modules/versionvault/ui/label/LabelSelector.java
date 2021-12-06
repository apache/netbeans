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

/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.netbeans.modules.versionvault.ui.label;

import java.awt.event.WindowEvent;
import org.openide.util.NbBundle;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import javax.swing.*;
import java.awt.Dialog;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.util.List;
import org.netbeans.modules.versionvault.Clearcase;
import org.netbeans.modules.versionvault.client.LsTypeCommand;
import org.netbeans.modules.versionvault.util.ProgressSupport;
import org.netbeans.modules.versioning.util.NoContentPanel;
import org.openide.util.Cancellable;
import org.openide.util.RequestProcessor;

/**
 * Provides chooser from list of strings.
 *
 * @author  Maros Sandor
 */
public class LabelSelector extends javax.swing.JPanel implements MouseListener {

    private final JList listValues;
    private NoContentPanel labelPanel;        
    
    public static String select(String title, String prompt, File... files) {
        if(files == null || files.length == 0) {
            return "";
        }
        LabelSelector selector = new LabelSelector();
        Mnemonics.setLocalizedText(selector.promptLabel, prompt);
        
        selector.listValues.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);        
        
        selector.labelPanel = new NoContentPanel(NbBundle.getMessage(LabelSelector.class, "LabelsListing_NoContent")); //NOI18N
        selector.listPanel.add(selector.labelPanel);
                        
        DialogDescriptor descriptor = new DialogDescriptor(selector, title);
        descriptor.setClosingOptions(null);
        descriptor.setHelpCtx(null);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(LabelSelector.class, "ACSD_LabelSelectorDialog"));  // NOI18N        
        
        final Cancellable cancellable = listLabels(selector, files[0].isFile() ? files[0].getParentFile() : files[0]);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                cancellable.cancel();
            }        
        });
        
        selector.putClientProperty(Dialog.class, dialog);
        selector.putClientProperty(DialogDescriptor.class, descriptor);
        dialog.setVisible(true);
        if (descriptor.getValue() != DialogDescriptor.OK_OPTION) return null;
        
        return (String) selector.listValues.getSelectedValue();
    }

    private static Cancellable listLabels(final LabelSelector selector, final File workingDir) {
        ProgressSupport ps = new ProgressSupport(new RequestProcessor("Clearcase - List Labels", 1), NbBundle.getMessage(LabelSelector.class, "Progress_Listing_Labels")) { //NOI18N
            @Override
            protected void perform() {
                LsTypeCommand cmd = new LsTypeCommand(workingDir, LsTypeCommand.Kind.Label);                
                Clearcase.getInstance().getClient().post(NbBundle.getMessage(LabelSelector.class, "Progress_Listing_Labels"), cmd).waitFinished(); //NOI18N
                selector.setChoices(cmd.getTypes());
            }
        };
        ps.start();
        return ps;
    }    
    
    private List<String> choices;    
    private void setChoices(List<String> strings) {
        choices = strings;        
        if(choices.size() > 0) {
            listValues.setModel(new AbstractListModel() {
                public int getSize() {
                    return choices.size();
                }

                public Object getElementAt(int index) {
                    return choices.get(index);
                }
            });
            listPanel.remove(labelPanel);
            JScrollPane scrollPane = new javax.swing.JScrollPane();        
            scrollPane.setViewportView(listValues);
            listPanel.add(scrollPane, java.awt.BorderLayout.CENTER);        
            listPanel.revalidate();
        } else {
            labelPanel.setLabel(NbBundle.getMessage(LabelSelector.class, "LabelsListing_NoLabels")); //NOI18N
        }
    }

    /** Creates new form StringSelector */
    public LabelSelector() {
        initComponents();
        listValues = new JList();
        listValues.addMouseListener(this);
    }
    
    public void mouseClicked(MouseEvent e) {
        if (!e.isPopupTrigger() && e.getClickCount() == 2) {
            Dialog dialog = (Dialog) getClientProperty(Dialog.class);
            DialogDescriptor descriptor = (DialogDescriptor) getClientProperty(DialogDescriptor.class);
            descriptor.setValue(DialogDescriptor.OK_OPTION);
            dialog.dispose();
        }
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        promptLabel = new javax.swing.JLabel();

        promptLabel.setText(org.openide.util.NbBundle.getMessage(LabelSelector.class, "LabelSelector.promptLabel.text")); // NOI18N

        listPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(listPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(promptLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 81, Short.MAX_VALUE)
                        .addGap(96, 96, 96))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(promptLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(listPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JPanel listPanel = new javax.swing.JPanel();
    private javax.swing.JLabel promptLabel;
    // End of variables declaration//GEN-END:variables
}
