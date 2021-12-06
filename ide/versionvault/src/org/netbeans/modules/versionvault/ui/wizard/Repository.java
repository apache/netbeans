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

package org.netbeans.modules.versionvault.ui.wizard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.versionvault.RepositoryFile;
import org.netbeans.modules.versionvault.Clearcase;
import org.netbeans.modules.versionvault.client.LsVobCommand;
import org.netbeans.modules.versionvault.client.PWVCommand;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Tomas Stupka, Ramin Moazeni
 */
public class Repository implements ActionListener, DocumentListener, FocusListener, ItemListener {
    
    public final static int FLAG_URL_EDITABLE           = 2;
    public final static int FLAG_URL_ENABLED            = 4;
    public final static int FLAG_ACCEPT_REVISION        = 8;
    public final static int FLAG_SHOW_REMOVE            = 16;
    public final static int FLAG_SHOW_HINTS             = 32;    
    public final static int FLAG_SHOW_PROXY             = 64;    
    
    private final static String LOCAL_URL_HELP          = "file:///vob_path/";              // NOI18N
 
    private RepositoryPanel repositoryPanel;
    private RepositoryFile repositoryFile;
    private boolean valid = true;
    private List<PropertyChangeListener> listeners;
    
    public static final String PROP_VALID = "valid";                                                    // NOI18N
    public static boolean SNAP_TRUE = false; 
    private String message;  
    private String snapshotViewPath;
    private int modeMask;
    private Dimension maxNeededSize;
    
    public Repository(String titleLabel) {
        this(0, titleLabel);
    }
            
    public Repository(int modeMask, String titleLabel) {
        
        this.modeMask = modeMask;
      
        initPanel();
        
        repositoryPanel.titleLabel.setText(titleLabel);
                                        
        repositoryPanel.urlComboBox.setEditable(false);
        repositoryPanel.urlComboBox.setEnabled(isSet(FLAG_URL_ENABLED));
        repositoryPanel.urlComboBox2.setEditable(false);
        repositoryPanel.urlComboBox2.setEnabled(isSet(FLAG_URL_ENABLED));
        
        List<String> result = new ArrayList();
        String dynamicView = null;
        
        LsVobCommand lsvob = new LsVobCommand();
        Clearcase.getInstance().getClient().exec(lsvob, true);
        result = lsvob.getMountedVobs();

        repositoryPanel.urlComboBox.addItem("Select...");
        for (int i = 0; i < result.size(); i++) {
            repositoryPanel.urlComboBox.addItem(result.get(i));
        }

        PWVCommand pwv = new PWVCommand();
        Clearcase.getInstance().getClient().exec(pwv, true);
        dynamicView = pwv.getStartedDynamicView();
      
        repositoryPanel.urlComboBox2.addItem("Select...");
        
        if (dynamicView != null) 
            repositoryPanel.urlComboBox2.addItem(dynamicView);
        else
            repositoryPanel.urlComboBox2.addItem("** NONE **");
        
        repositoryPanel.tipLabel.setVisible(isSet(FLAG_SHOW_HINTS));
        repositoryPanel.removeButton.setVisible(isSet(FLAG_SHOW_REMOVE));        
        
        // retrieve the dialog size for the largest configuration
        updateVisibility("svn+");                                                                       // NOI18N
        maxNeededSize = repositoryPanel.getPreferredSize();
    }
    
    public void actionPerformed(ActionEvent e) {
        String repositoryPath = null;
        
        validateUserInput();
         if(e.getSource() == repositoryPanel.urlComboBox) {

            try {
                repositoryPath = getUrlString();
            } catch (Exception ex) {
                return; // should not happen
            }
            repositoryFile = new RepositoryFile(repositoryPath);

        } else if (e.getSource() == repositoryPanel.urlComboBox2) {
            validateUserInput();
        }

    }
     public RepositoryFile getRepositoryFile() {
        return repositoryFile;
    }
     
     public boolean validateUserInput() {
        final String selectString = "Select...";
        final String undefinedString = "** NONE **";
        
        setValid(true, null);
        
        String text = repositoryPanel.urlComboBox.getSelectedItem().toString();
        
        if (text.equals(selectString)) {
            setValid(false, (org.openide.util.NbBundle.getMessage(Repository.class, "BK2017"))); // NOI18N
            return false;
        }
       
        text = repositoryPanel.urlComboBox2.getSelectedItem().toString();
        if (text.equals(selectString)) {
            setValid(false,(org.openide.util.NbBundle.getMessage(Repository.class, "BK2016"))); // NOI18N
            return false;
        }
        
        text = repositoryPanel.urlComboBox2.getSelectedItem().toString();
        if (text.trim().equals(undefinedString)) {
            setValid(false,(org.openide.util.NbBundle.getMessage(Repository.class, "BK2023"))); // NOI18N
            return false;
        }
        
        return valid;
    }
    
    private void initPanel() {        
        repositoryPanel = new RepositoryPanel();   
        repositoryPanel.removeButton.addActionListener(this);        
        repositoryPanel.urlComboBox.addActionListener(this);
        repositoryPanel.urlComboBox2.addActionListener(this);
        getUrlComboEditor().getDocument().addDocumentListener(this);
        repositoryPanel.urlComboBox.addItemListener(this);
        repositoryPanel.urlComboBox2.addItemListener(this);       
        
        onSelectedRepositoryChange();
        
    }
    public void setSnapshotViewPath (String snapshotViewPath) {
        this.snapshotViewPath = snapshotViewPath;
    }
    public String getSnapshotViewPath () {
        return snapshotViewPath;
    }
    private JTextComponent getUrlComboEditor() {
        Component editor = repositoryPanel.urlComboBox.getEditor().getEditorComponent();
        JTextComponent textEditor = (JTextComponent) editor;
        return textEditor;
    }     
    
    public void insertUpdate(DocumentEvent e) {
        textChanged(e);
    }

    public void removeUpdate(DocumentEvent e) {
        textChanged(e);
    }

    public void changedUpdate(DocumentEvent e) { 
        textChanged(e);
    }

    private void textChanged(final DocumentEvent e) {
        // repost later to AWT otherwise it can deadlock because
        // the document is locked while firing event and we try
        // synchronously access its content from selected repository
        Runnable awt = new Runnable() {
            public void run() {
              if (e.getDocument() == ((JTextComponent) repositoryPanel.urlComboBox.getEditor().getEditorComponent()).getDocument()) {
                    onSelectedRepositoryChange();
              }
            }
        };
        SwingUtilities.invokeLater(awt);
        
    }
    
    /**    
     * Always updates UI fields visibility.
     */
    private void onSelectedRepositoryChange() {
        setValid(true, ""); 
        String urlString = "";                                                                         // NOI18N         
        try {
            urlString = getUrlString();
        } catch (InterruptedException ex) {
            return; // should not happen
        }
               
        message = "";                                                                                   // NOI18N
        updateVisibility();
    }            

    private void updateVisibility() {
        try {
            updateVisibility(getUrlString());
        } catch (InterruptedException ex) {
            return;
        }        
    }   
    
    /** Shows proper fields depending on Clearcase connection method. */
    private void updateVisibility(String selectedUrlString) {

        if(selectedUrlString.startsWith("file:")) {                      // NOI18N
            repositoryPanel.tipLabel.setText(LOCAL_URL_HELP);
        } else {
            repositoryPanel.tipLabel.setText(LOCAL_URL_HELP);
        }
    }
    
    /**
     * Load selected root from Swing structures (from arbitrary thread).
     * @return null on failure
     */
    private String getUrlString() throws InterruptedException {        
        if(!repositoryPanel.urlComboBox.isEditable()) {
            Object selection = repositoryPanel.urlComboBox.getSelectedItem();
            if(selection != null) {
                return selection.toString().trim();    
            }
            return "";    
        } else {
            final String[] svnUrl = new String[1];
            try {
                Runnable awt = new Runnable() {
                    public void run() {
                        svnUrl[0] = (String) repositoryPanel.urlComboBox.getEditor().getItem().toString().trim();
                    }
                };
                if (SwingUtilities.isEventDispatchThread()) {
                    awt.run();
                } else {
                    SwingUtilities.invokeAndWait(awt);
                }
                return svnUrl[0].trim();
            } catch (InvocationTargetException e) {
                ErrorManager err = ErrorManager.getDefault();
                err.notify(e);
            }
            return null;            
        }
    }
    
    public RepositoryPanel getPanel() {
        return repositoryPanel;
    }
    
    public boolean isValid() {
        return valid;
    }

    private void setValid(boolean valid, String message) {
        boolean oldValue = this.valid;
        this.message = message;
        this.valid = valid;
        fireValidPropertyChanged(oldValue, valid);
    }

    private void fireValidPropertyChanged(boolean oldValue, boolean valid) {
        if(listeners==null) {
            return;
        }
        for (Iterator it = listeners.iterator();  it.hasNext();) {
            PropertyChangeListener l = (PropertyChangeListener) it.next();
            l.propertyChange(new PropertyChangeEvent(this, PROP_VALID, new Boolean(oldValue), new Boolean(valid)));
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        if(listeners==null) {
            listeners = new ArrayList<PropertyChangeListener>();
        }
        listeners.add(l);     
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        if(listeners==null) {
            return;
        }
        listeners.remove(l);
    }

    public String getMessage() {
        return message;
    }

    public void focusGained(FocusEvent focusEvent) {
      
    }

    public void focusLost(FocusEvent focusEvent) {
        validateUserInput();
    }    
    
    public void itemStateChanged(ItemEvent evt) {
        if(evt.getStateChange() == ItemEvent.SELECTED) {
          validateUserInput();
        } else if(evt.getStateChange() == ItemEvent.DESELECTED) {
            validateUserInput();
            updateVisibility();  
        }       
    }
    
    public boolean show(String title, HelpCtx helpCtx, boolean setMaxNeddedSize) {
        RepositoryDialogPanel rdp = new RepositoryDialogPanel();
        rdp.panel.setLayout(new BorderLayout());
        JPanel p = getPanel();
        if(setMaxNeddedSize) {
            p.setPreferredSize(maxNeededSize);
        }        
        rdp.panel.add(p, BorderLayout.NORTH);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(rdp, title); // NOI18N        
        showDialog(dialogDescriptor, helpCtx);
        return dialogDescriptor.getValue() == DialogDescriptor.OK_OPTION;
    }
    
    public Object show(String title, HelpCtx helpCtx, Object[] options) {
        RepositoryDialogPanel rdp = new RepositoryDialogPanel();
        rdp.panel.setLayout(new BorderLayout());
        rdp.panel.add(getPanel(), BorderLayout.NORTH);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(rdp, title); // NOI18N        
        if(options!= null) {
            dialogDescriptor.setOptions(options); // NOI18N
        }        
        showDialog(dialogDescriptor, helpCtx);
        return dialogDescriptor.getValue();
    }
    
    private void showDialog(DialogDescriptor dialogDescriptor, HelpCtx helpCtx) {
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(helpCtx);        

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);        
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(Repository.class, "ACSD_RepositoryPanel"));
        dialog.getAccessibleContext().setAccessibleName(NbBundle.getMessage(Repository.class, "ACSN_RepositoryPanel"));
        dialog.setVisible(true);
    }

    private boolean isSet(int flag) {
        return (modeMask & flag) != 0;
    }
}
