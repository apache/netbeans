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
package org.netbeans.modules.cnd.toolchain.ui.compiler;

import java.awt.Component;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.remote.ui.RemoteFileChooserUtil;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.toolchain.support.CompilerDefinition;
import org.netbeans.modules.cnd.utils.ui.ListEditorPanel;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Panel used to manage predefined Include Paths and Macro Definitions of the compiler
 */
public class PredefinedPanel extends javax.swing.JPanel {

    private static final RequestProcessor RP = new RequestProcessor("Reset Compiler Settings", 2); // NOI18N
    private IncludesPanel includesPanel;
    private DefinitionsPanel definitionsPanel;
    private AbstractCompiler compiler;
    private final ParserSettingsPanel parserSettingsPanel;
    private ExecutionEnvironment env;

    private boolean settingsReseted = false;

    /** Creates new form PredefinedPanel */
    public PredefinedPanel(AbstractCompiler compiler, ParserSettingsPanel parserSettingsPanel, ExecutionEnvironment env) {
        initComponents();
        this.compiler = compiler;
        this.parserSettingsPanel = parserSettingsPanel;
        this.env = env;
        updatePanels(false);

        resetButton.getAccessibleContext().setAccessibleDescription(getString("RESET_BUTTON_AD"));
    }

    private void updatePanels(final boolean reset) {
        RP.post(new Runnable(){
            @Override
            public void run() {
                if (reset) {
                    compiler.resetCompilerDefinitions();
                }
                final List<String> includesList = compiler.getSystemIncludeDirectories();
                final List<String> definesList = compiler.getSystemPreprocessorSymbols();

                SwingUtilities.invokeLater(new Runnable(){
                    @Override
                    public void run() {
                        if (includesPanel != null) {
                            includes.remove(includesPanel);
                        }
                        includes.add(includesPanel = new IncludesPanel(includesList, env));
                        
                        if (definesList instanceof CompilerDefinition) {
                            ((CompilerDefinition)definesList).sort();
                        } else {
                            Collections.sort(definesList, new Comparator<String>() {
                                @Override
                                public int compare(String s1, String s2) {
                                    return s1.compareToIgnoreCase(s2);
                                }
                            });
                        }
                        if (definitionsPanel != null) {
                            macros.remove(definitionsPanel);
                        }
                        macros.add(definitionsPanel = new DefinitionsPanel(definesList));
                        if (reset) {
                            //parserSettingsPanel.fireFilesPropertiesChanged();
                            parserSettingsPanel.setModified(true);
                            settingsReseted = true;
                        }
                        validate();
                        repaint();
                    }
                });
            }
        });
    }

    public boolean save() {
        boolean wasChanges = settingsReseted;
        settingsReseted = false;
        if (includesPanel != null && definitionsPanel != null) {
            List<String> tmpIncludes = includesPanel.getListData();
            wasChanges |= compiler.setSystemIncludeDirectories(tmpIncludes);
            
            List<String> definitions = definitionsPanel.getListData();
            wasChanges |= compiler.setSystemPreprocessorSymbols(definitions);
        }
        return wasChanges;
    }

    public void update() {
        if (CodeAssistancePanelController.TRACE_CODEASSIST) {
            System.err.println("update for PredefinedPanel " + compiler.getName());
        }
        updatePanels(false);
    }

    public void updateCompiler(AbstractCompiler compiler, ExecutionEnvironment env) {
        this.compiler = compiler;
        this.env = env;
        updatePanels(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        includes = new javax.swing.JPanel();
        macros = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        resetButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        includes.setOpaque(false);
        includes.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        add(includes, gridBagConstraints);

        macros.setOpaque(false);
        macros.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(macros, gridBagConstraints);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/toolchain/ui/compiler/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("CODE_ASSISTANCE_COMMENT")); // NOI18N
        jLabel1.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jLabel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(resetButton, bundle.getString("RESET_BUTTON_TXT")); // NOI18N
        resetButton.setOpaque(false);
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(resetButton, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        // This can be undone now. No need to show confirmation dialog.
        updatePanels(true);
    }//GEN-LAST:event_resetButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel includes;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel macros;
    private javax.swing.JButton resetButton;
    // End of variables declaration//GEN-END:variables

    private static String getString(String s) {
        return NbBundle.getMessage(PredefinedPanel.class, s);
    }

    boolean isChanged() {
        boolean isChanged = settingsReseted;
        if (this.includesPanel != null) {
            isChanged |= !compiler.getSystemIncludeDirectories().equals(includesPanel.getListData());
        }
        if (this.definitionsPanel != null) {
            isChanged |= !compiler.getSystemPreprocessorSymbols().equals(definitionsPanel.getListData());
        }
        if (CodeAssistancePanelController.TRACE_CODEASSIST) {
            System.err.println("isChanged for PredefinedPanel " + compiler.getName() + " is " + isChanged); // NOI18N
        }
        return isChanged;
    }

    boolean isDataValid() {
        boolean isDataValid = true;
        if (this.includesPanel != null) {
            isDataValid &= this.includesPanel.isDataValid();
        }
        if (this.definitionsPanel != null) {
            isDataValid &= this.definitionsPanel.isDataValid();
        }
        if (CodeAssistancePanelController.TRACE_CODEASSIST) {
            System.err.println("isDataValid for PredefinedPanel " + compiler.getName() + " is " + isDataValid); // NOI18N
        }
        return isDataValid;
    }

    void cancel() {
        if (CodeAssistancePanelController.TRACE_CODEASSIST) {
            System.err.println("cancel for PredefinedPanel " + compiler.getName()); // NOI18N
        }
    }
    
    private static class IncludesPanel extends ListEditorPanel<String> {
        private CompilerDefinition defs;
        private final ExecutionEnvironment env;

        public IncludesPanel(List<String> objects, ExecutionEnvironment env) {
            super(objects);
            this.env = env;
            if (objects instanceof CompilerDefinition) {
                defs = (CompilerDefinition) objects;
                setCustomCellRenderer(new MyDefaultListCellRenderer(defs, "include")); // NOI18N
            }
            getDefaultButton().setVisible(false);
        }

        @Override
        public String addAction() {
            final String chooser_key = "IncludesPanel"; // NOI18N
            String seed = RemoteFileChooserUtil.getCurrentChooserFile(chooser_key, env);
            if (seed == null) {
                seed = System.getProperty("user.home"); // NOI18N
            }
            JFileChooser fileChooser = RemoteFileChooserUtil.createFileChooser(env, getString("SelectDirectoryTxt"), getString("SelectTxt"), // NOI18N
                                                      JFileChooser.DIRECTORIES_ONLY, null, seed, true); // NOI18N
            int ret = fileChooser.showOpenDialog(this);
            if (ret == JFileChooser.CANCEL_OPTION) {
                return null;
            }
            final File selectedFile = fileChooser.getSelectedFile();
            String itemPath = selectedFile.getPath();
            RemoteFileChooserUtil.setCurrentChooserFile(chooser_key, selectedFile.isFile() ? selectedFile.getParentFile().getPath() : itemPath, env);
            if (defs != null) {
                defs.setUserAdded(true, getListDataSize());
            }
            return itemPath;
        }

        @Override
        public String getListLabelText() {
            return getString("IncludeDirectoriesTxt"); // NOI18N
        }

        @Override
        public char getListLabelMnemonic() {
            return getString("IncludeDirectoriesMn").charAt(0); // NOI18N
        }

        @Override
        public String getAddButtonText() {
            return getString("AddButtonTxt"); // NOI18N
        }

        @Override
        public char getAddButtonMnemonics() {
            return getString("IAddButtonMn").charAt(0); // NOI18N
        }

        @Override
        public char getCopyButtonMnemonics() {
            return getString("ICopyButtonMn").charAt(0); // NOI18N
        }

        @Override
        public String copyAction(String o) {
            if (defs != null) {
                defs.setUserAdded(true, getListDataSize());
            }
            return o;
        }

        @Override
        public String getRenameButtonText() {
            return getString("EditButtonTxt"); // NOI18N
        }

        @Override
        public char getRenameButtonMnemonics() {
            return getString("EditButtonMn").charAt(0); // NOI18N
        }

        @Override
        public void editAction(String o, int i) {
            String s = o;

            NotifyDescriptor.InputLine notifyDescriptor = new NotifyDescriptor.InputLine(getString("EditDialogLabelDir"), getString("EditDialogTitle")); // NOI18N
            notifyDescriptor.setInputText(s);
            DialogDisplayer.getDefault().notify(notifyDescriptor);
            if (notifyDescriptor.getValue() != NotifyDescriptor.OK_OPTION) {
                return;
            }
            String newS = notifyDescriptor.getInputText().trim();
            if (defs != null) {
                defs.setUserAdded(true, i);
            }
            replaceElement(o, newS, i);
        }

        @Override
        public char getRemoveButtonMnemonics() {
            return getString("IRemoveButtonMn").charAt(0); // NOI18N
        }

        @Override
        public void removeAction(String o, int i) {
            if (defs != null) {
                defs.setUserAdded(false, i);
                for(int j = i; j < getListDataSize()- 1; j++)  {
                    boolean userAdded = defs.isUserAdded(j + 1);
                    defs.setUserAdded(userAdded, j);
                    defs.setUserAdded(false, j + 1);
                }
            }
        }

        @Override
        public char getUpButtonMnemonics() {
            return getString("IUpButtonMn").charAt(0); // NOI18N
        }

        @Override
        public void upAction(int from) {
            if (defs != null) {
                boolean fromValue = defs.isUserAdded(from);
                boolean toValue = defs.isUserAdded(from - 1);
                defs.setUserAdded(fromValue, from - 1);
                defs.setUserAdded(toValue, from);
            }
        }

        @Override
        public char getDownButtonMnemonics() {
            return getString("IDownButtonMn").charAt(0); // NOI18N
        }

        @Override
        public void downAction(int from) {
            if (defs != null) {
                boolean fromValue = defs.isUserAdded(from);
                boolean toValue = defs.isUserAdded(from + 1);
                defs.setUserAdded(fromValue, from + 1);
                defs.setUserAdded(toValue, from);
            }
        }        
    }

    private static class DefinitionsPanel extends ListEditorPanel<String> {
        private CompilerDefinition defs;

        public DefinitionsPanel(List<String> objects) {
            super(objects);
            if (objects instanceof CompilerDefinition) {
                defs = (CompilerDefinition) objects;
                setCustomCellRenderer(new MyDefaultListCellRenderer(defs, "macro")); // NOI18N
            }
            getDefaultButton().setVisible(false);
            getUpButton().setVisible(false);
            getDownButton().setVisible(false);
            getCopyButton().setVisible(false);
        }

        @Override
        public String addAction() {
            NotifyDescriptor.InputLine notifyDescriptor = new NotifyDescriptor.InputLine(getString("EditDialogLabelDef"), getString("AddDialogTitle")); // NOI18N
            DialogDisplayer.getDefault().notify(notifyDescriptor);
            if (notifyDescriptor.getValue() != NotifyDescriptor.OK_OPTION) {
                return null;
            }
            String def = notifyDescriptor.getInputText().trim();
            if (def.length() != 0) {
                if (defs != null) {
                    defs.setUserAdded(true, getListDataSize());
                }
                return def;
            } else {
                return null;
            }
        }

        @Override
        public String getListLabelText() {
            return getString("MacroDefinitionsTxt"); // NOI18N
        }

        @Override
        public char getListLabelMnemonic() {
            return getString("MacroDefinitionsMn").charAt(0); // NOI18N
        }

        @Override
        public String getAddButtonText() {
            return getString("AddButtonTxt"); // NOI18N
        }

        @Override
        public char getAddButtonMnemonics() {
            return getString("MAddButtonMn").charAt(0); // NOI18N
        }

        @Override
        public char getRenameButtonMnemonics() {
            return getString("MEditButtonMn").charAt(0); // NOI18N
        }

        @Override
        public String getRenameButtonText() {
            return getString("EditButtonTxt"); // NOI18N
        }

        @Override
        public void editAction(String o, int i) {
            String s = o;

            NotifyDescriptor.InputLine notifyDescriptor = new NotifyDescriptor.InputLine(getString("EditDialogLabelDef"), getString("EditDialogTitle")); // NOI18N
            notifyDescriptor.setInputText(s);
            DialogDisplayer.getDefault().notify(notifyDescriptor);
            if (notifyDescriptor.getValue() != NotifyDescriptor.OK_OPTION) {
                return;
            }
            String newS = notifyDescriptor.getInputText().trim();
            if (defs != null) {
                defs.setUserAdded(true, i);
            }
            replaceElement(o, newS, i);
        }

        @Override
        public char getRemoveButtonMnemonics() {
            return getString("MRemoveButtonMn").charAt(0); // NOI18N
        }

        @Override
        public void removeAction(String o, int i) {
            if (defs != null) {
                defs.setUserAdded(false, i);
                for(int j = i; j < getListDataSize()- 1; j++)  {
                    boolean userAdded = defs.isUserAdded(j + 1);
                    defs.setUserAdded(userAdded, j);
                    defs.setUserAdded(false, j + 1);
                }
            }
        }
    }

    private static final class MyDefaultListCellRenderer extends DefaultListCellRenderer {
        private final CompilerDefinition defs;
        private final String toolTipSuffix;
        
        MyDefaultListCellRenderer(CompilerDefinition defs, String toolTipSuffix) {
            this.defs = defs;
            this.toolTipSuffix=toolTipSuffix;
        }
        
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            boolean showToolTip = false;
            if (defs != null) {
                if (defs.isUserAdded(index)) {
                    label.setIcon(getLibraryIcon());
                    showToolTip = true;
                }
            }
            label.setText(value.toString());
            if (showToolTip) {
                String message = NbBundle.getMessage(PredefinedPanel.class, "UserAdded.tooltip.text."+toolTipSuffix, //NOI18N
                        value.toString());
                label.setToolTipText(message);
            } else {
                label.setToolTipText(null);
            }
            return label;
        }
        private ImageIcon getLibraryIcon() {
            return ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/toolchain/ui/compiler/key.png", false); //NOI18N
        }

    };
}
