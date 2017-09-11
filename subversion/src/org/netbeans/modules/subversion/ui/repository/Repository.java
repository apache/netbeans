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

package org.netbeans.modules.subversion.ui.repository;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.client.SvnClientFactory;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * @author Tomas Stupka
 */
public class Repository implements ActionListener, DocumentListener, ItemListener {
    
    public final static int FLAG_URL_EDITABLE           = 2;
    public final static int FLAG_URL_ENABLED            = 4;
    public final static int FLAG_ACCEPT_REVISION        = 8;
    public final static int FLAG_SHOW_REMOVE            = 16;
    public final static int FLAG_SHOW_HINTS             = 32;    
    public final static int FLAG_SHOW_PROXY             = 64;    

    private static final String FILE_PANEL        = "file-panel";       //NOI18N
    private static final String HTTP_PANEL        = "http-panel";       //NOI18N
    private static final String SSH_PANEL         = "ssh-panel";        //NOI18N
    private static final String INVALID_URL_PANEL = "invalid-url-panel";//NOI18N
    private static final String INVALID_SVN_URL = "invalid svn url:";   //NOI18N
    
    private String currentConnPanelType;

    private ConnectionType currentPanel;
    private RepositoryPanel repositoryPanel;
    private boolean valid = true;
    private List<PropertyChangeListener> listeners;
    
    private RepositoryConnection editedRC;
    
    public static final String PROP_VALID = "valid";                                                    // NOI18N

    private String message;            
    private int modeMask;
    private Dimension maxNeededSize;
    private ConnectionType http;
    private ConnectionType file;
    private ConnectionType svnSSH;
    private ConnectionType invalidUrlPanel;
    
    public Repository(String titleLabel) {
        this(0, titleLabel);
    }
            
    public Repository(int modeMask, String titleLabel) {
        
        this.modeMask = modeMask;
        
        initPanel();
        
        repositoryPanel.titleLabel.setText(titleLabel);
                                        
        repositoryPanel.urlComboBox.setEditable(isSet(FLAG_URL_EDITABLE));
        repositoryPanel.urlComboBox.setEnabled(isSet(FLAG_URL_ENABLED));
        
        repositoryPanel.tipLabel.setVisible(isSet(FLAG_SHOW_HINTS));
        repositoryPanel.removeButton.setVisible(isSet(FLAG_SHOW_REMOVE));        
        repositoryPanel.removeButton.addActionListener(this);

        // retrieve the dialog size for the largest configuration
        maxNeededSize = repositoryPanel.getPreferredSize();
        
        refreshUrlHistory();
    }
    
    public void selectUrl (final SVNUrl url, final boolean force) {
        Mutex.EVENT.readAccess(new Mutex.Action<Void>() {
            @Override
            public Void run () {
                DefaultComboBoxModel dcbm = (DefaultComboBoxModel) repositoryPanel.urlComboBox.getModel();
                int idx = dcbm.getIndexOf(url.toString());
                if(idx > -1) {
                    dcbm.setSelectedItem(url.toString());    
                } else if(force) {
                    RepositoryConnection rc = new RepositoryConnection(url.toString());
                    dcbm.addElement(rc);
                    dcbm.setSelectedItem(rc);
                }
                return null;
            }
        });
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == repositoryPanel.removeButton) {
            onRemoveClick();
        }
    }

    private void onRemoveClick() {
        RepositoryConnection rc = getSelectedRCIntern();
        if (rc != null) {
            remove(rc);
        }
    }

    private void initPanel() {        
        repositoryPanel = new RepositoryPanel();
        repositoryPanel.connPanel.add(
                (http = new ConnectionType.Http(this)).getPanel(),
                HTTP_PANEL);
        repositoryPanel.connPanel.add(
                (file = new ConnectionType.FileUrl(this)).getPanel(),
                FILE_PANEL);

        if(SvnClientFactory.isSvnKit()) {
            svnSSH = new ConnectionType.SvnSSHSvnKit(this);
        } else {
            svnSSH = new ConnectionType.SvnSSHCli(this);
        }

        repositoryPanel.connPanel.add(svnSSH.getPanel(), SSH_PANEL);

        repositoryPanel.connPanel.add(
                (invalidUrlPanel = new ConnectionType.InvalidUrl(this)).getPanel(),
                INVALID_URL_PANEL);

        svnSSH.showHints(isSet(FLAG_SHOW_HINTS));

        // fill url field with a default value
        String selectedUrlString = "file://"; //NOI18N
        ((JTextComponent) repositoryPanel.urlComboBox.getEditor().getEditorComponent()).setText(selectedUrlString); //NOI18N
        updateVisibility(FILE_PANEL, selectedUrlString);
        
        repositoryPanel.urlComboBox.addActionListener(this);
        getUrlComboEditor().getDocument().addDocumentListener(this);
        repositoryPanel.urlComboBox.addItemListener(this);
        repositoryPanel.urlComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected && comp instanceof JComponent) {
                    String tt = null;
                    if (comp.getPreferredSize().getWidth() > repositoryPanel.urlComboBox.getSize().getWidth() && value instanceof RepositoryConnection) {
                        tt = ((RepositoryConnection) value).getUrl();
                    }
                    ((JComponent) comp).setToolTipText(tt);
                }
                return comp;
            }
        });
        
        onSelectedRepositoryChange();
    }

    /**
     * Performs its body outside of AWT. Runs asynchronously if called in AWT
     */
    public final void refreshUrlHistory() {
        repositoryPanel.urlComboBox.setEnabled(false);
        Runnable notInAWT = new Runnable() {
            @Override
            public void run() {
                List<RepositoryConnection> recentUrls = SvnModuleConfig.getDefault().getRecentUrls();
                final Set<RepositoryConnection> recentRoots = new LinkedHashSet<RepositoryConnection>();
                recentRoots.addAll(recentUrls);
                addProjects(recentRoots);
                if (repositoryPanel.urlComboBox.isEditable()) {
                    // templates for supported connection methods
                    recentRoots.add(new RepositoryConnection("file:///"));      // NOI18N
                    recentRoots.add(new RepositoryConnection("http://"));       // NOI18N
                    recentRoots.add(new RepositoryConnection("https://"));      // NOI18N
                    recentRoots.add(new RepositoryConnection("svn://"));        // NOI18N
                    recentRoots.add(new RepositoryConnection("svn+ssh://"));    // NOI18N
                }

                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        ComboBoxModel rootsModel = new RepositoryModel(new Vector<RepositoryConnection>(recentRoots));
                        repositoryPanel.urlComboBox.setModel(rootsModel);
                        if (recentRoots.size() > 0) {
                            repositoryPanel.urlComboBox.setSelectedIndex(0);
                            onSelectedRepositoryChange();
                            currentPanel.refresh(getSelectedRCIntern());
                        }
                        repositoryPanel.urlComboBox.setEnabled(isSet(FLAG_URL_ENABLED));

                        if (repositoryPanel.urlComboBox.isEditable()) {
                            JTextComponent textEditor = getUrlComboEditor();
                            textEditor.selectAll();
                        }
                        updateVisibility();
                    }
                });
            }

            private void addProjects (final Set<RepositoryConnection> recentRoots) {
                for (Project p : OpenProjects.getDefault().getOpenProjects()) {
                    File projectFolder = FileUtil.toFile(p.getProjectDirectory());
                    if (projectFolder != null && SvnUtils.isManaged(projectFolder)) {
                        try {
                            SVNUrl repositoryUrl = SvnUtils.getRepositoryRootUrl(projectFolder);
                            if (repositoryUrl != null) {
                                RepositoryConnection rc = new RepositoryConnection(repositoryUrl.toString());
                                if (!recentRoots.contains(rc)) {
                                    recentRoots.add(rc);
                                }
                            }
                        } catch (SVNClientException ex) {
                            Logger.getLogger(Repository.class.getName()).log(Level.FINE, null, ex);
                        }
                    }
                }
            }
        };
        if (EventQueue.isDispatchThread()) {
            Subversion.getInstance().getRequestProcessor().post(notInAWT);
        } else {
            notInAWT.run();
        }
    }

    public void storeRecentUrls() {
        final List<RepositoryConnection> recentUrls = getRecentUrls();
        Subversion.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                SvnModuleConfig.getDefault().setRecentUrls(recentUrls);
            }
        });
    }
    
    public boolean isChanged() {
        List<RepositoryConnection> connections = getRecentUrls();
        List<RepositoryConnection> storedConnections = SvnModuleConfig.getDefault().getRecentUrls();        
        return !SvnUtils.equals(connections, storedConnections);
    }
    
    private List<RepositoryConnection> getRecentUrls() {
        ComboBoxModel model = repositoryPanel.urlComboBox.getModel();
        List<RepositoryConnection> ret = new ArrayList<RepositoryConnection>(model.getSize());
        for (int i = 0; i < model.getSize(); i++) {
            ret.add((RepositoryConnection)model.getElementAt(i));
        }
        return ret;
    }
    
    private JTextComponent getUrlComboEditor() {
        Component editor = repositoryPanel.urlComboBox.getEditor().getEditorComponent();
        JTextComponent textEditor = (JTextComponent) editor;
        return textEditor;
    }     
    
    public void setEditable(boolean editable) {
        repositoryPanel.urlComboBox.setEditable(editable);
        currentPanel.setEditable(editable);
    }
    
    public void storeConfigValues() {
        currentPanel.storeConfigValues();
    }
    
    @Override
    public void insertUpdate(DocumentEvent e) {
        textChanged(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        textChanged(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) { 
        textChanged(e);
    }

    private void textChanged(final DocumentEvent e) {     
        Runnable awt = new Runnable() {
            @Override
            public void run() {
                if (e.getDocument() == ((JTextComponent) repositoryPanel.urlComboBox.getEditor().getEditorComponent()).getDocument()) {
                    onSelectedRepositoryChange();
                } 
                validateSvnUrl();
            }
        };
        SwingUtilities.invokeLater(awt);
    }
            
    /**
     * Fast url syntax check. It can invalidate the whole step
     */
    void validateSvnUrl() {
        boolean valid = true;

        RepositoryConnection rc = null; 
        try {
            rc = getSelectedRCIntern();
            // check for a valid svnurl - file:/// returns file://, which is invalid, so this is kind of a double-check
            // 1. rc.getSvnUrl()
            // 2. new SVNUrl(string)
            new SVNUrl(rc.getSvnUrl().toString());
            if(!isSet(FLAG_ACCEPT_REVISION) && !rc.getSvnRevision().equals(SVNRevision.HEAD)) 
            {
                message = NbBundle.getMessage(Repository.class, "MSG_Repository_OnlyHEADRevision");
                valid = false;
            } else {
                // check for a valid svnrevision
                rc.getSvnRevision();
            }
        } catch (Exception ex) {             
            message = translateMessage(ex.getLocalizedMessage());
            valid = false;
        }        
        
        if(valid) {            
            valid = rc != null && !rc.getUrl().equals("");
            if(!currentPanel.isValid(rc)) {
                valid = false;
            }
        }
        
        setValid(valid, message);
        repositoryPanel.removeButton.setEnabled(rc != null && rc.getUrl().length() > 0);
    }
    
    /**    
     * Always updates UI fields visibility.
     */
    private void onSelectedRepositoryChange() {
        setValid(true, "");                                                                            // NOI18N     
        String urlString = "";                                                                         // NOI18N         
        try {
            urlString = getUrlString();
        } catch (InterruptedException ex) {
            return; // should not happen
            }
                
        if(urlString != null) {
                       
            RepositoryConnection editedrc = getEditedRC();
            editedrc.setUrl(urlString);
            
            DefaultComboBoxModel dcbm = (DefaultComboBoxModel) repositoryPanel.urlComboBox.getModel();                
            int idx = dcbm.getIndexOf(editedrc);       
            if(idx > -1) {
                //dcbm.setSelectedItem(urlString);                                                
                currentPanel.refresh((RepositoryConnection)dcbm.getElementAt(idx));
            } 
            currentPanel.onSelectedRepositoryChange(urlString);
            currentPanel.fillRC(editedrc);
            
        }
        message = "";                                                                                   // NOI18N
        updateVisibility();
    }            

    private RepositoryConnection getEditedRC() {
        if(editedRC == null) {
            editedRC = new RepositoryConnection("");
        }
        return editedRC;
    }

    private void updateVisibility() {
        String selectedUrlString;
        try {
            selectedUrlString = getUrlString();
        } catch (InterruptedException ex) {
            return;
        }
        repositoryPanel.urlComboBox.setToolTipText(selectedUrlString);

        String connPanelType;

        if(selectedUrlString.startsWith("http:")) {                             // NOI18N
            connPanelType = HTTP_PANEL;
        } else if(selectedUrlString.startsWith("https:")) {                     // NOI18N
            connPanelType = HTTP_PANEL;
        } else if(selectedUrlString.startsWith("svn:")) {                       // NOI18N
            connPanelType = HTTP_PANEL;
        } else if(selectedUrlString.startsWith("svn+")) {                       // NOI18N
            connPanelType = SSH_PANEL;
        } else if(selectedUrlString.startsWith("file:")) {                      // NOI18N
            connPanelType = FILE_PANEL;
        } else {
            connPanelType = INVALID_URL_PANEL;
        }

        updateVisibility(connPanelType, selectedUrlString);
    }

    private void updateVisibility(String connPanelTypeId,
                                  String selectedUrlString) {

        if (connPanelTypeId == HTTP_PANEL) {
            currentPanel = http;
        } else if (connPanelTypeId == SSH_PANEL) {
            currentPanel = svnSSH;
        } else if (connPanelTypeId == FILE_PANEL) {
            currentPanel = file;
        } else if (connPanelTypeId == INVALID_URL_PANEL) {
            currentPanel = invalidUrlPanel;
        } else {
            assert false;
        }

        if (connPanelTypeId != currentConnPanelType) {
            ((CardLayout) repositoryPanel.connPanel.getLayout()).show(repositoryPanel.connPanel, connPanelTypeId);
            currentConnPanelType = connPanelTypeId;
        }
        repositoryPanel.tipLabel.setText(currentPanel.getTip(selectedUrlString));
        currentPanel.updateVisibility(selectedUrlString);
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
                    @Override
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
                Subversion.LOG.log(Level.SEVERE, null, e);
            }
            return null;            
        }
    }

    public RepositoryConnection getSelectedRC() {
        RepositoryConnection rc = getSelectedRCIntern();
        return rc;
    }

    RepositoryConnection getSelectedRCIntern() {
        String urlString;
        try {
            urlString = getUrlString();            
        }
        catch (InterruptedException ex) {
            // should not happen
            Subversion.LOG.log(Level.SEVERE, null, ex);
            return null;
        }
        
        DefaultComboBoxModel dcbm = (DefaultComboBoxModel) repositoryPanel.urlComboBox.getModel();                
        int idx = dcbm.getIndexOf(urlString);        
        
        if(idx > -1) {
            return (RepositoryConnection) dcbm.getElementAt(idx);
        }        
        return getEditedRC();        
    }
    
    public RepositoryPanel getPanel() {
        return repositoryPanel;
    }
    
    public boolean isValid() {
        return valid;
    }

    void setValid(boolean valid, String message) {
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
            l.propertyChange(new PropertyChangeEvent(this, PROP_VALID, oldValue, valid));
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
    
    public void remove(RepositoryConnection toRemove) {
        RepositoryModel model = (RepositoryModel) repositoryPanel.urlComboBox.getModel();        
        model.removeElement(toRemove);        
    }

    @Override
    public void itemStateChanged(ItemEvent evt) {
        if(evt.getStateChange() == ItemEvent.SELECTED) {
            RepositoryConnection rc = (RepositoryConnection) evt.getItem();
            if (rc != null) {
                currentPanel.refresh(rc);
                updateVisibility();  
                editedRC = new RepositoryConnection(rc);
            }
        } else if(evt.getStateChange() == ItemEvent.DESELECTED) {
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
    
    public Object show(String title, HelpCtx helpCtx, Object[] options, Object initialValue) {
        RepositoryDialogPanel rdp = new RepositoryDialogPanel();
        rdp.panel.setLayout(new BorderLayout());
        rdp.panel.add(getPanel(), BorderLayout.NORTH);
        DialogDescriptor dialogDescriptor = null;
        if(options != null) {
            dialogDescriptor = new DialogDescriptor(rdp, title, // NOI18N
                    true, options, initialValue,
                    DialogDescriptor.DEFAULT_ALIGN, helpCtx, null);
        } else {
            dialogDescriptor = new DialogDescriptor(rdp, title); // NOI18N
        }
        showDialog(dialogDescriptor, helpCtx);
        return dialogDescriptor.getValue();
    }

    public Object show(String title, HelpCtx helpCtx, Object[] options) {
        return show(title, helpCtx, options, null);
    }
    
    private void showDialog(DialogDescriptor dialogDescriptor, HelpCtx helpCtx) {
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(helpCtx);        

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);        
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(Repository.class, "ACSD_RepositoryPanel"));
        dialog.getAccessibleContext().setAccessibleName(NbBundle.getMessage(Repository.class, "ACSN_RepositoryPanel"));
        dialog.setVisible(true);
    }

    final boolean isSet(int flag) {
        return (modeMask & flag) != 0;
    }
    
    public class RepositoryModel  extends DefaultComboBoxModel {

        public RepositoryModel(Vector v) {
            super(v);
        }

        @Override
        public void setSelectedItem(Object obj) {
            if(obj instanceof String) {
                int idx = getIndexOf(obj);
                if(idx > -1) {
                    obj = getElementAt(idx);
                } else {
                    obj = createNewRepositoryConnection((String) obj);                   
                }                
            }            
            super.setSelectedItem(obj);
        }

        @Override
        public int getIndexOf(Object obj) {
            if(obj instanceof String) {
                obj = createNewRepositoryConnection((String)obj);                
            }
            return super.getIndexOf(obj);
        }

        @Override
        public void addElement(Object obj) {
            if(obj instanceof String) {
                obj = createNewRepositoryConnection((String)obj);                
            }
            super.addElement(obj);
        }

        @Override
        public void insertElementAt(Object obj,int index) {
            if(obj instanceof String) {
                String str = (String) obj;
                RepositoryConnection rc = null;
                try {
                    rc = (RepositoryConnection) getElementAt(index);                    
                } catch (ArrayIndexOutOfBoundsException e) {
                }
                if(rc != null) {
                    rc.setUrl(str);
                    obj = rc;
                }                
                obj = createNewRepositoryConnection(str);
            } 
            super.insertElementAt(obj, index);
        }         

        @Override
        public void removeElement(Object obj) {
            int index = getIndexOf(obj);
            if ( index != -1 ) {
                removeElementAt(index);
            }
        }
        
        private RepositoryConnection createNewRepositoryConnection(String url) {
            if(editedRC != null) {
                editedRC.setUrl(url);
                return new RepositoryConnection(editedRC);
            } else {
                return new RepositoryConnection(url);
            }
        }
    }

    private String translateMessage (String message) {
        message = message.toLowerCase();
        int pos = message.indexOf(INVALID_SVN_URL);
        if (pos != -1) {
            message = message.substring(INVALID_SVN_URL.length());
            message = NbBundle.getMessage(Repository.class, "MSG_Repository_InvalidSvnUrl", message);
        }
        return message;
    }
}
