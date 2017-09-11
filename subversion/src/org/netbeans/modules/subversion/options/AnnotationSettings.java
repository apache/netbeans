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
package org.netbeans.modules.subversion.options;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.netbeans.modules.subversion.SvnModuleConfig;
import java.util.regex.Pattern;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.subversion.ui.wizards.URLPatternWizard;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class AnnotationSettings implements ActionListener, TableModelListener, ListSelectionListener {
    
    private final AnnotationSettingsPanel panel; 
    private DialogDescriptor dialogDescriptor;
    private boolean valid;
    
    public AnnotationSettings() {
        
        panel = new AnnotationSettingsPanel();
        getModel().addTableModelListener(this); 
        getSelectionModel().addListSelectionListener(this);         
        panel.upButton.setEnabled(false);
        panel.downButton.setEnabled(false);            
        panel.removeButton.setEnabled(false);                    
        panel.editButton.setEnabled(false);                                    
        
        panel.upButton.addActionListener(this); 
        panel.downButton.addActionListener(this); 
        panel.newButton.addActionListener(this); 
        panel.removeButton.addActionListener(this); 
        panel.editButton.addActionListener(this); 
        panel.resetButton.addActionListener(this); 
        panel.wizardButton.addActionListener(this);     
            
        panel.warningLabel.setVisible(false);        
    }

    private void setValid(boolean valid) {
        if(!valid) {
            panel.warningLabel.setText(NbBundle.getMessage(AnnotationSettings.class, "MSG_MissingFolderVariable"));
        }        
        panel.warningLabel.setVisible(!valid);
        panel.upButton.setVisible(valid);
        panel.downButton.setVisible(valid);
        panel.newButton.setVisible(valid);
        panel.removeButton.setVisible(valid);
        panel.editButton.setVisible(valid);
        panel.resetButton.setVisible(valid);
        panel.wizardButton.setVisible(valid);            
        panel.expresionsTable.setVisible(valid);      
        panel.expressionsPane.setVisible(valid);      
        panel.tableLabel.setVisible(valid);      
    }
    
    JPanel getPanel() {
        return panel;
    }
        
    void show(boolean valid) {
        setValid(valid);
        
        String title = NbBundle.getMessage(SvnOptionsController.class, "CTL_ManageLabels");
        String accesibleDescription = NbBundle.getMessage(SvnOptionsController.class, "ACSD_ManageLabels");
        HelpCtx helpCtx = new HelpCtx(AnnotationSettings.class);
        
        dialogDescriptor = new DialogDescriptor(panel, title);
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(helpCtx);
        dialogDescriptor.setValid(valid);
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.getAccessibleContext().setAccessibleDescription(accesibleDescription);
        //dialog.setModal(false);
        dialog.setAlwaysOnTop(false);
        dialog.setVisible(true);                
    }
    
    void update() {
        reset(SvnModuleConfig.getDefault().getAnnotationExpresions());
    }
       
    boolean isChanged() {                
        List<AnnotationExpression> storedExpressions = SvnModuleConfig.getDefault().getAnnotationExpresions();
        List<AnnotationExpression> expressions = getAnnotationExpressions();                
        return !SvnUtils.equals(storedExpressions, expressions);
    }
    
    void applyChanges() {                                         
        List<AnnotationExpression> exps = getAnnotationExpressions();
        SvnModuleConfig.getDefault().setAnnotationExpresions(exps);        
    }    

    private List<AnnotationExpression> getAnnotationExpressions() {
        TableModel model = panel.expresionsTable.getModel();
        List<AnnotationExpression> exps = new ArrayList<AnnotationExpression>(model.getRowCount());
        for (int r = 0; r < model.getRowCount(); r++) {
            String urlExp = (String) model.getValueAt(r, 0);
            if (urlExp.trim().equals("")) {
                continue;
            }
            String annotationExp = (String) model.getValueAt(r, 1);
            exps.add(new AnnotationExpression(urlExp, annotationExp));
        }
        return exps;
    }    
    
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == panel.upButton) {
            onUpClick();
        } else if (evt.getSource() == panel.downButton) {
            onDownClick();
        } else if (evt.getSource() == panel.newButton) {
            onNewClick();
        } else  if (evt.getSource() == panel.removeButton) {
            onRemoveClick();
        } else if (evt.getSource() == panel.editButton) {
            onEditClick();
        } else if (evt.getSource() == panel.resetButton) {
            onResetClick();
        } else if (evt.getSource() == panel.wizardButton) {
            onWizardClick();
        } 
    }   
    
    private void onUpClick() {
        ListSelectionModel listSelectionModel = getSelectionModel();
        int r = listSelectionModel.getMinSelectionIndex();        
        if(r > 0) {
            DefaultTableModel model = getModel();
            int rNew = r - 1;
            model.moveRow(r, r, rNew) ;
            listSelectionModel.setSelectionInterval(rNew, rNew);
        }
    }
    
    private void onDownClick() {
        ListSelectionModel listSelectionModel = getSelectionModel();
        int r = listSelectionModel.getMinSelectionIndex();                
        DefaultTableModel model = getModel();
        if(r > -1 && r < model.getRowCount() - 1) {     
           int rNew = r + 1;
           model.moveRow(r, r, rNew) ;
           listSelectionModel.setSelectionInterval(rNew, rNew);
        }        
    }
    
    private void onNewClick() {
        EditAnnotationPanel editPanel = new EditAnnotationPanel();  
        String title = NbBundle.getMessage(AnnotationSettings.class, "AnnotationSettings.new.title");        
        if(showEdit(editPanel, title)) {
            addRow(editPanel.patternTextField.getText(), 
                   editPanel.folderTextField.getText());                        
        }                        
    }    
    
    private void addRow(String pattern, String folder) {
        int r = getSelectionModel().getMinSelectionIndex();
        if(r < 0) {
            getModel().addRow(      new String[] {pattern, folder});
        } else {
            getModel().insertRow(r, new String[] {pattern, folder});
        }
    }
    
    private void onRemoveClick() {        
        ListSelectionModel selectionModel = getSelectionModel();
        int r = selectionModel.getMinSelectionIndex();
        if(r > -1) {
            getModel().removeRow(r);
        }
        int size = getModel().getRowCount();
        if(size > 0) {            
            if (r > size - 1) {
                r = size - 1;
            } 
            selectionModel.setSelectionInterval(r, r);    
        }
    }

    private void onEditClick() {
        ListSelectionModel selectionModel = getSelectionModel();
        int r = selectionModel.getMinSelectionIndex();        
        if(r < 0) return;

        String pattern = (String) getModel().getValueAt(r, 0);
        String folder  = (String) getModel().getValueAt(r, 1);      

        EditAnnotationPanel editPanel = new EditAnnotationPanel();
        if(pattern != null) editPanel.patternTextField.setText(pattern);
        if(folder  != null) editPanel.folderTextField.setText(folder);
        
        String title = NbBundle.getMessage(AnnotationSettings.class, "AnnotationSettings.edit.title");        
        if(showEdit(editPanel, title)) {
            getModel().setValueAt(editPanel.patternTextField.getText(), r, 0);
            getModel().setValueAt(editPanel.folderTextField.getText(),  r, 1);                        
        }
    }
    
    private void onResetClick() {  
        reset(SvnModuleConfig.getDefault().getDefaultAnnotationExpresions());               
    }
    
    private void onWizardClick() {  
        URLPatternWizard wizard = new URLPatternWizard();
        if (!wizard.show()) return;
        String pattern = wizard.getPattern();
        String folder;
        if(wizard.useName()) {
            folder = wizard.getRepositoryFolder();
        } else {
            folder = "\\1";
        }        
        addRow(pattern, folder);    
    } 
    
    private void reset(List<AnnotationExpression> exps) {  
                
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DefaultTableModel model = getModel();
        model.setColumnCount(2);
        model.setRowCount(exps.size());
        int r = -1;
        for (Iterator<AnnotationExpression> it = exps.iterator(); it.hasNext();) {
            AnnotationExpression annotationExpression = it.next();                
            r++;
            model.setValueAt(annotationExpression.getUrlExp(),        r, 0);
            model.setValueAt(annotationExpression.getAnnotationExp(), r, 1);
        }        
    }
    
    private DefaultTableModel getModel() {              
        return (DefaultTableModel) panel.expresionsTable.getModel();
    }

    private ListSelectionModel getSelectionModel() {
        return panel.expresionsTable.getSelectionModel();        
    }    

    public void tableChanged(TableModelEvent evt) {
        if (evt.getType() == TableModelEvent.UPDATE) {
            validateTable(evt.getFirstRow(), evt.getColumn());
        }
    }

    private void validateTable(int r, int c) {
        
        if(r < 0 || c != 0) {
            return;
        }
        
        valid = true;     
        String pattern = (String) getModel().getValueAt(r, c);
        try {                         
            Pattern.compile(pattern);                                                       
        } catch (Exception e) {
            valid = false;
        }       
            
        if(valid) {
            panel.warningLabel.setVisible(false);                
        } else {
            String label = NbBundle.getMessage(AnnotationSettings.class, "AnnotationSettingsPanel.warningLabel.text", pattern);
            panel.warningLabel.setText(label);
            panel.warningLabel.setVisible(true);            
        }
        if(dialogDescriptor != null) {
            dialogDescriptor.setValid(valid);
        }
    }

    public void valueChanged(ListSelectionEvent evt) {
        ListSelectionModel selectionModel = getSelectionModel();
        int r = selectionModel.getMinSelectionIndex();        
        panel.upButton.setEnabled(r > 0);
        panel.downButton.setEnabled(r > -1 && r < getModel().getRowCount() - 1);            
        panel.removeButton.setEnabled(r > -1);                    
        panel.editButton.setEnabled(r > -1);                    
    }

    private boolean showEdit(final EditAnnotationPanel editPanel, String title) {                                        
        final DialogDescriptor dd = new DialogDescriptor(editPanel, title);
        dd.setModal(true);        
        dd.setValid(isValid(editPanel));
        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent arg0) {                
                validate();
            }
            public void removeUpdate(DocumentEvent arg0) {
                validate();                
            }
            public void changedUpdate(DocumentEvent arg0) {
                validate();                
            }   
            private void validate() {
                dd.setValid(isValid(editPanel));
            }
        };
        editPanel.patternTextField.getDocument().addDocumentListener(dl);
        editPanel.folderTextField.getDocument().addDocumentListener(dl);         
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);             
        dialog.setVisible(true);        
        return dd.getValue() == dd.OK_OPTION;        
    }            
    
    private boolean isValid(EditAnnotationPanel editPanel) {
        return !( editPanel.patternTextField.getText().trim().equals("") ||
                  editPanel.folderTextField.getText().trim().equals(""));
    }
            
}
