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

package org.netbeans.modules.apisupport.project.ui.platform;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.apisupport.project.queries.GlobalSourceForBinaryImpl;
import org.netbeans.modules.apisupport.project.ui.ModuleUISettings;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.modules.apisupport.project.universe.SourceRootsProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Represents <em>Sources</em> tab in the NetBeans platforms customizer.
 *
 * @author Martin Krauskopf
 */
public final class NbPlatformCustomizerSources extends JPanel {
    
    private SourceRootsProvider srcRP;
    private PlatformComponentFactory.SourceRootsModel model;
    private final ListListener listListener;
    
    /** Creates new form NbPlatformCustomizerModules */
    public NbPlatformCustomizerSources() {
        initComponents();
        initAccessibility();
        this.listListener = new ListListener() {
            void listChanged() {
                updateEnabled();
            }
        };
        updateEnabled();
    }
    
    public void addNotify() {
        super.addNotify();
        sourceList.addListSelectionListener(listListener);
        sourceList.getModel().addListDataListener(listListener);
    }
    
    public void removeNotify() {
        sourceList.removeListSelectionListener(listListener);
        sourceList.getModel().removeListDataListener(listListener);
        super.removeNotify();
    }
    
    public void setSourceRootsProvider(SourceRootsProvider srp) {
        this.srcRP = srp;
        this.model = new PlatformComponentFactory.SourceRootsModel(srp);
        sourceList.setModel(model);
    }
    
    private void updateEnabled() {
        // update buttons enability appropriately
        removeButton.setEnabled(sourceList.getModel().getSize() > 0 && sourceList.getSelectedIndex() != -1);
        moveUpButton.setEnabled(sourceList.getSelectionModel().getMinSelectionIndex() > 0);
        moveDownButton.setEnabled(srcRP != null &&
                sourceList.getSelectionModel().getMaxSelectionIndex() < srcRP.getSourceRoots().length - 1);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        sourceLabel = new javax.swing.JLabel();
        sourceSP = new javax.swing.JScrollPane();
        sourceList = new javax.swing.JList();
        buttonPanel = new javax.swing.JPanel();
        addFolderButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        moveUpButton = new javax.swing.JButton();
        moveDownButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        sourceLabel.setLabelFor(sourceList);
        org.openide.awt.Mnemonics.setLocalizedText(sourceLabel, org.openide.util.NbBundle.getMessage(NbPlatformCustomizerSources.class, "LBL_PlatformSources"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(sourceLabel, gridBagConstraints);

        sourceList.setCellRenderer(PlatformComponentFactory.getURLListRenderer());
        sourceSP.setViewportView(sourceList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(sourceSP, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 0));
        org.openide.awt.Mnemonics.setLocalizedText(addFolderButton, org.openide.util.NbBundle.getMessage(NbPlatformCustomizerSources.class, "CTL_AddZipOrFolder"));
        addFolderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addZipOrFolder(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        buttonPanel.add(addFolderButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(NbPlatformCustomizerSources.class, "CTL_Remove"));
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeFolder(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 12, 0);
        buttonPanel.add(removeButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(moveUpButton, org.openide.util.NbBundle.getMessage(NbPlatformCustomizerSources.class, "CTL_MoveUp"));
        moveUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUp(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 0);
        buttonPanel.add(moveUpButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(moveDownButton, org.openide.util.NbBundle.getMessage(NbPlatformCustomizerSources.class, "CTL_MoveDown"));
        moveDownButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDown(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        buttonPanel.add(moveDownButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        add(buttonPanel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    private void moveDown(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDown
        int[] selIndices = sourceList.getSelectedIndices();
        model.moveSourceRootsDown(selIndices);
        for (int i = 0; i < selIndices.length; i++) {
            selIndices[i] = ++selIndices[i];
        }
        sourceList.setSelectedIndices(selIndices);
    }//GEN-LAST:event_moveDown
    
    private void moveUp(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUp
        int[] selIndices = sourceList.getSelectedIndices();
        model.moveSourceRootsUp(selIndices);
        for (int i = 0; i < selIndices.length; i++) {
            selIndices[i] = --selIndices[i];
        }
        sourceList.setSelectedIndices(selIndices);
    }//GEN-LAST:event_moveUp
    
    private void removeFolder(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeFolder
        Object[] selVals = sourceList.getSelectedValues();
        int toSelect = sourceList.getSelectedIndex() - 1;
        URL[] selURLs = new URL[selVals.length];
        System.arraycopy(selVals, 0, selURLs, 0, selVals.length);
        model.removeSourceRoot(selURLs);
        sourceList.setSelectedIndex(toSelect);
    }//GEN-LAST:event_removeFolder
    
    private void addZipOrFolder(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addZipOrFolder
        JFileChooser chooser = new JFileChooser(ModuleUISettings.getDefault().getLastUsedNbPlatformLocation());
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File f)  {
                return f.isDirectory() || isValidNbSourceRoot(f);
            }
            public String getDescription() {
                return getMessage("CTL_SourcesTab");
            }
        });
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = FileUtil.normalizeFile(chooser.getSelectedFile());
            if (!file.exists() || (file.isFile() && !isValidNbSourceRoot(file))) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        getMessage("MSG_NotValidNBSrcZIP")));
            } else {
                URL newUrl = FileUtil.urlForArchiveOrDir(file);
                if (model.containsRoot(newUrl)) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        getMessage("MSG_ExistingNBSrcZIP")));
                } else {
                    ModuleUISettings.getDefault().setLastUsedNbPlatformLocation(file.getParentFile().getAbsolutePath());
                    model.addSourceRoot(newUrl);
                    sourceList.setSelectedValue(newUrl, true);
                }
            }
        }
    }//GEN-LAST:event_addZipOrFolder
    
    private static boolean isValidNbSourceRoot(final File nbSrcRoot) {
        boolean isValid = false;
        String lcName = nbSrcRoot.getName().toLowerCase(Locale.US);
        if (lcName.endsWith(".jar") || lcName.endsWith(".zip")) { // NOI18N
            try {
                isValid = GlobalSourceForBinaryImpl.NetBeansSourcesParser.getInstance(nbSrcRoot) != null;
            } catch (IOException ex) {
                // isValid = false
            }
        }
        return isValid;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addFolderButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton moveDownButton;
    private javax.swing.JButton moveUpButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JLabel sourceLabel;
    private javax.swing.JList sourceList;
    private javax.swing.JScrollPane sourceSP;
    // End of variables declaration//GEN-END:variables
    
    private void initAccessibility() {
        addFolderButton.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_addFolderButton"));
        sourceList.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_sourceList"));
        moveDownButton.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_moveDownButton"));
        moveUpButton.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_moveUpButton"));
        removeButton.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_removeButton"));
    }
    
    private String getMessage(String key) {
        return NbBundle.getMessage(NbPlatformCustomizerSources.class, key);
    }
    
    static abstract class ListListener implements ListDataListener, ListSelectionListener {
        
        public void intervalAdded(final ListDataEvent e) {
            listChanged();
        }
        
        public void intervalRemoved(final ListDataEvent e) {
            listChanged();
        }
        
        public void contentsChanged(final ListDataEvent e) {
            listChanged();
        }
        
        public void valueChanged(final ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                listChanged();
            }
        }
        
        abstract void listChanged();
        
    }
    
}
