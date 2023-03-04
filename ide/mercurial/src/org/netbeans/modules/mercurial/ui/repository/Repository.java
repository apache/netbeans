/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.mercurial.ui.repository;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.config.HgConfigFiles;
import org.netbeans.modules.mercurial.ui.repository.HgURL.Scheme;
import org.netbeans.modules.mercurial.util.HgRepositoryContextCache;
import org.netbeans.modules.versioning.util.DialogBoundsPreserver;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Tomas Stupka
 * @author Marian Petras
 */
public class Repository implements ActionListener, FocusListener, ItemListener {
    
    public static final int FLAG_URL_ENABLED            = 4;
    public static final int FLAG_ACCEPT_REVISION        = 8;
    public static final int FLAG_SHOW_HINTS             = 32;    
    public static final int FLAG_SHOW_PROXY             = 64;    
    
    private static final String LOCAL_URL_HELP          = "file:/repository_path";              // NOI18N
    private static final String HTTP_URL_HELP           = "http://hostname/repository_path";      // NOI18N
    private static final String HTTPS_URL_HELP          = "https://hostname/repository_path";     // NOI18N
    private static final String STATIC_HTTP_URL_HELP    = "static-http://hostname/repository_path";       // NOI18N
    private static final String SSH_URL_HELP            = "ssh://hostname/repository_path";   // NOI18N   
               
    private RepositoryPanel repositoryPanel;
    private boolean valid = true;
    private List<ChangeListener> listeners;
    private final ChangeEvent changeEvent = new ChangeEvent(this);
    
    private RepositoryConnection repositoryConnection;
    private HgURL url;

    public static final String PROP_VALID = "valid";                                                    // NOI18N

    private String message;            
    private int modeMask;
    private Dimension maxNeededSize;
    private boolean bPushPull;
    private static int HG_PUSH_PULL_VERT_PADDING = 30;
    
    private JTextComponent urlComboEditor;
    private Document urlDoc, usernameDoc, passwordDoc, tunnelCmdDoc;
    private boolean urlBeingSelectedFromPopup = false;
    private final File root;
    private Map<String, String> storedPaths = Collections.<String, String>emptyMap();

    public Repository(int modeMask, String titleLabel, boolean bPushPull) {
        this(modeMask, titleLabel, bPushPull, null);
    }

    public Repository(int modeMask, String titleLabel, boolean bPushPull, File repositoryRoot) {
        
        this.modeMask = modeMask;
        this.root = repositoryRoot;
        
        initPanel();
        
        repositoryPanel.titleLabel.setText(titleLabel);
                                        
        repositoryPanel.urlComboBox.setEnabled(isSet(FLAG_URL_ENABLED));        
        repositoryPanel.urlComboBox.setRenderer(new UrlRenderer());
        repositoryPanel.tunnelHelpLabel.setVisible(isSet(FLAG_SHOW_HINTS));
        repositoryPanel.tipLabel.setVisible(isSet(FLAG_SHOW_HINTS));
        
        //repositoryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));

        // retrieve the dialog size for the largest configuration
        if(bPushPull)
            updateVisibility("foo:"); // NOI18N
        else
            updateVisibility("https:"); // NOI18N            
        maxNeededSize = repositoryPanel.getPreferredSize();

        repositoryPanel.savePasswordCheckBox.setSelected(HgModuleConfig.getDefault().getSavePassword());
        repositoryPanel.schedulePostInitRoutine(new Runnable() {
                    public void run() {
                        refreshUrlHistory();
                    }
        });
    }
    
    public void actionPerformed(ActionEvent e) {
        assert e.getSource() == repositoryPanel.proxySettingsButton;

        onProxyConfiguration();
    }
    
    private void onProxyConfiguration() {
        OptionsDisplayer.getDefault().open("General");              // NOI18N
    }    
    
    private void initPanel() {
        repositoryPanel = new RepositoryPanel();

        urlComboEditor = (JTextComponent) repositoryPanel.urlComboBox
                                          .getEditor().getEditorComponent();
        urlDoc = urlComboEditor.getDocument();
        usernameDoc = repositoryPanel.userTextField.getDocument();
        passwordDoc = repositoryPanel.userPasswordField.getDocument();
        tunnelCmdDoc = repositoryPanel.tunnelCommandTextField.getDocument();

        DocumentListener documentListener = new DocumentChangeHandler();
        urlDoc.addDocumentListener(documentListener);
        passwordDoc.addDocumentListener(documentListener);
        usernameDoc.addDocumentListener(documentListener);
        tunnelCmdDoc.addDocumentListener(documentListener);

        repositoryPanel.savePasswordCheckBox.addItemListener(this);
        repositoryPanel.urlComboBox.addItemListener(this);

        repositoryPanel.proxySettingsButton.addActionListener(this);

        repositoryPanel.userPasswordField.addFocusListener(this);

        tweakComboBoxEditor();
    }

    private void tweakComboBoxEditor() {
        final ComboBoxEditor origEditor = repositoryPanel.urlComboBox.getEditor();

        if (origEditor.getClass() == UrlComboBoxEditor.class) {
            /* attempt to tweak the combo-box multiple times */
            assert false;
            return;
        }

        repositoryPanel.urlComboBox.setEditor(new UrlComboBoxEditor(origEditor));
    }

    /**
     * Customized combo-box editor for displaying/modification of URL
     * of a Mercurial repository.
     * It is customized in the following aspects:
     * <ul>
     *     <li>When a RepositoryConnection is selected, displays its URL
     *         without user data (name and password).</li>
     *     <li>If a {@code RepositoryConnection} is set via method
     *         {@code setItem}, it holds a reference to it until another item
     *         is set via method {@code setItem()} or until the user modifies
     *         the text. This allows method {@code getItem()} to return
     *         the same item ({@code RepositoryConnection}).
     *         The allows the combo-box to correctly detect whether the item
     *         has been changed (since the last call of {@code setItem()}
     *         or not.</li>
     * </ul>
     */
    private final class UrlComboBoxEditor implements ComboBoxEditor,
                                                         DocumentListener {

        private final ComboBoxEditor origEditor;
        private Reference<RepositoryConnection> repoConnRef;

        private UrlComboBoxEditor(ComboBoxEditor originalEditor) {
            this.origEditor = originalEditor;
            ((JTextComponent) originalEditor.getEditorComponent())
                    .getDocument().addDocumentListener(this);
        }

        public void setItem(Object anObject) {
            urlBeingSelectedFromPopup = true;
            try {
                setItemImpl(anObject);
            } finally {
                urlBeingSelectedFromPopup = false;
            }
        }

        private void setItemImpl(Object anObject) {
            assert urlBeingSelectedFromPopup;

            if (anObject instanceof RepositoryConnection) {
                RepositoryConnection repoConn = (RepositoryConnection) anObject;
                repoConnRef = new WeakReference<RepositoryConnection>(repoConn);
                origEditor.setItem(repoConn.getUrl().toUrlStringWithoutUserInfo());
            } else {
                clearRepoConnRef();
                origEditor.setItem(anObject);
            }
        }

        public Component getEditorComponent() {
            return origEditor.getEditorComponent();
        }
        public Object getItem() {
            RepositoryConnection repoConn = getRepoConn();
            if (repoConn != null) {
                return repoConn;
            }

            return origEditor.getItem();
        }
        public void selectAll() {
            origEditor.selectAll();
        }
        public void addActionListener(ActionListener l) {
            origEditor.addActionListener(l);
        }
        public void removeActionListener(ActionListener l) {
            origEditor.removeActionListener(l);
        }

        public void insertUpdate(DocumentEvent e) {
            textChanged();
        }
        public void removeUpdate(DocumentEvent e) {
            textChanged();
        }
        public void changedUpdate(DocumentEvent e) {
            textChanged();
        }
        private void textChanged() {
            if (urlBeingSelectedFromPopup) {
                return;
            }
            clearRepoConnRef();
        }

        private RepositoryConnection getRepoConn() {
            if (repoConnRef != null) {
                RepositoryConnection repoConn = repoConnRef.get();
                if (repoConn != null) {
                    return repoConn;
                }
            }
            return null;
        }

        private void clearRepoConnRef() {
            if (repoConnRef != null) {
                repoConnRef.clear();
            }
        }

    }
    
    public void refreshUrlHistory() {
        repositoryPanel.urlComboBox.setModel(
                new DefaultComboBoxModel(createPresetComboEntries()));
        
        urlComboEditor.selectAll();
    }

    private static final Set<String> SKIPPED_PATHS = new HashSet<String>(Arrays.asList(HgConfigFiles.HG_DEFAULT_PULL, 
            HgConfigFiles.HG_DEFAULT_PULL_VALUE, 
            HgConfigFiles.HG_DEFAULT_PUSH, 
            HgConfigFiles.HG_DEFAULT_PUSH_VALUE));
    
    private Vector<?> createPresetComboEntries() {
        assert repositoryPanel.urlComboBox.isEditable();

        Vector<Object> result;

        List<RepositoryConnection> recentUrls = HgModuleConfig.getDefault().getRecentUrls();
        Scheme[] schemes = HgURL.Scheme.values();

        // acquire stored paths
        List<String> pathNames = Collections.<String>emptyList();
        storedPaths = Collections.<String, String>emptyMap();
        if (root != null) {
            Map<String, String> paths = HgRepositoryContextCache.getInstance().getPathValues(root);
            for (Iterator<Map.Entry<String, String>> it = paths.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, String> e = it.next();
                String storedUrl = e.getValue();
                boolean remove = true;
                if (!storedUrl.isEmpty()) {
                    try {
                        remove = false;
                        HgURL hgUrl = new HgURL(storedUrl);
                    } catch (URISyntaxException ex) {
                        // maybe it's a relative path
                        File f = FileUtil.normalizeFile(new File(root, storedUrl));
                        if (f.isDirectory()) {
                            // directory exists, it's ok path
                            e.setValue(f.getAbsolutePath());
                        } else {
                            Mercurial.LOG.log(Level.INFO, "Repository: Unknown stored path {0}:{1}", new Object[] { e.getKey(), storedUrl }); //NOI18N
                            // we do not need such entry
                            remove = true;
                        }
                    }
                }
                if (remove) {
                    it.remove();
                }
            }
            pathNames = new ArrayList<String>(paths.keySet());
            for (String skippedPath : SKIPPED_PATHS) {
                // add the default paths as URLs into the recent list
                String predefinedUrl = paths.get(skippedPath);
                if (predefinedUrl != null) {
                    try {
                        RepositoryConnection rc = new RepositoryConnection(predefinedUrl);
                        if (!recentUrls.contains(rc)) {
                            recentUrls.add(rc);
                        }
                    } catch (URISyntaxException ex) {
                        // 
                    }
                }
                pathNames.remove(skippedPath);
            }
            Collections.sort(pathNames);
            storedPaths = paths;
        }

        result = new Vector<Object>(recentUrls.size() + schemes.length + pathNames.size());
        result.addAll(pathNames);
        result.addAll(recentUrls);
        for (Scheme scheme : schemes) {
            result.add(createURIPrefixForScheme(scheme));
        }

        return result;
    }

    private static String createURIPrefixForScheme(Scheme scheme) {
        if (scheme == Scheme.FILE) {
            return scheme + ":/";                                       //NOI18N
        } else {
            return scheme + "://";                                      //NOI18N
        }
    }

    private final class DocumentChangeHandler implements DocumentListener {

        DocumentChangeHandler() { }

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
            assert EventQueue.isDispatchThread();

            Document modifiedDocument = e.getDocument();

            assert modifiedDocument != null;
            assert (modifiedDocument == urlDoc) || !urlBeingSelectedFromPopup;

            if (modifiedDocument == urlDoc) {
                onUrlChange();
            } else if (modifiedDocument == usernameDoc) {
                onUsernameChange();
            } else if (modifiedDocument == passwordDoc) {
                onPasswordChange();
            } else if (modifiedDocument == tunnelCmdDoc) {
                onTunnelCommandChange();
            }
        }
            
    }

    /**
     * Fast url syntax check. It can invalidate the whole step
     */
    private void quickValidateUrl() {
        String toValidate = getUrlString();
        String errMsg;
        if (storedPaths.containsValue(toValidate)) {
            // path is among stored paths from config file, it was validated in combo urls setup
            errMsg = null;
        } else {
            errMsg = HgURL.validateQuickly(toValidate);
        }
        if (errMsg == null) {
            setValid();
        } else {
            setValid(false, errMsg);
        }        
    }
    
    /**    
     * Always updates UI fields visibility.
     */
    private void onUrlChange() {
        if (!urlBeingSelectedFromPopup) {
            repositoryConnection = null;
            url = null;

            repositoryPanel.userTextField.setText(null);
            repositoryPanel.userPasswordField.setText(null);
            repositoryPanel.tunnelCommandTextField.setText(null);
            repositoryPanel.savePasswordCheckBox.setSelected(false);
        }
        quickValidateUrl();
        updateVisibility();
    }            

    private void updateVisibility() {
        updateVisibility(getUrlString());
    }   
    
    /** Shows proper fields depending on Mercurial connection method. */
    private void updateVisibility(String selectedUrlString) {

        boolean authFields = false;
        boolean proxyFields = false;
        boolean sshFields = false;
        repositoryPanel.chooseFolderButton.setVisible(false);
        if(selectedUrlString.startsWith("http:")) {                             // NOI18N
            repositoryPanel.tipLabel.setText(HTTP_URL_HELP);
            authFields = true;
            proxyFields = true;
        } else if(selectedUrlString.startsWith("https:")) {                     // NOI18N
            repositoryPanel.tipLabel.setText(HTTPS_URL_HELP);
            authFields = true;
            proxyFields = true;
        } else if(selectedUrlString.startsWith("static-http:")) {                       // NOI18N
            repositoryPanel.tipLabel.setText(STATIC_HTTP_URL_HELP);
            authFields = true;
            proxyFields = true;
        } else if(selectedUrlString.startsWith("ssh")) {                        // NOI18N
            repositoryPanel.tipLabel.setText(getSVNTunnelTip(selectedUrlString));
            sshFields = true;
        } else if(selectedUrlString.startsWith("file:")) {                      // NOI18N
            repositoryPanel.tipLabel.setText(LOCAL_URL_HELP);
            repositoryPanel.chooseFolderButton.setVisible(true);
        } else {
            repositoryPanel.tipLabel.setText(NbBundle.getMessage(Repository.class, "MSG_Repository_Url_Help", new Object [] { // NOI18N
                LOCAL_URL_HELP, HTTP_URL_HELP, HTTPS_URL_HELP, STATIC_HTTP_URL_HELP, SSH_URL_HELP
                //LOCAL_URL_HELP, HTTP_URL_HELP, STATIC_HTTP_URL_HELP, SSH_URL_HELP
            }));
        }

        repositoryPanel.userPasswordField.setVisible(authFields);
        repositoryPanel.passwordLabel.setVisible(authFields);          
        repositoryPanel.userTextField.setVisible(authFields);          
        repositoryPanel.leaveBlankLabel.setVisible(authFields);        
        repositoryPanel.userLabel.setVisible(authFields);             
        //repositoryPanel.savePasswordCheckBox.setVisible(authFields);
        repositoryPanel.savePasswordCheckBox.setVisible(false);
        repositoryPanel.proxySettingsButton.setVisible(proxyFields && ((modeMask & FLAG_SHOW_PROXY) != 0));        
        //repositoryPanel.tunnelCommandTextField.setVisible(sshFields);        
        //repositoryPanel.tunnelCommandLabel.setVisible(sshFields);        
        //repositoryPanel.tunnelLabel.setVisible(sshFields);        
        //repositoryPanel.tunnelHelpLabel.setVisible(sshFields);       
        repositoryPanel.savePasswordCheckBox.setVisible(false);             
        repositoryPanel.tunnelCommandTextField.setVisible(false);        
        repositoryPanel.tunnelCommandLabel.setVisible(false);        
        repositoryPanel.tunnelLabel.setVisible(false);        
        repositoryPanel.tunnelHelpLabel.setVisible(false);       
    }

    public void setEditable(boolean editable) {
        assert EventQueue.isDispatchThread();

        repositoryPanel.urlComboBox.setEnabled(editable && isSet(FLAG_URL_ENABLED));
        repositoryPanel.userTextField.setEnabled(editable && valid);
        repositoryPanel.userPasswordField.setEnabled(editable && valid);
        repositoryPanel.savePasswordCheckBox.setEnabled(editable && valid);
        repositoryPanel.tunnelCommandTextField.setEnabled(editable && valid);
        repositoryPanel.proxySettingsButton.setEnabled(editable && valid);
    }

    private String getSVNTunnelTip(String urlString) {
        //String tunnelName = getTunnelName(urlString);
        //return MessageFormat.format(SSH_URL_HELP, tunnelName).trim();
        return SSH_URL_HELP;
    }
            
    /**
     * Load selected root from Swing structures (from arbitrary thread).
     * @return null on failure
     */
    public String getUrlString() {
        String value = urlComboEditor.getText().trim();
        String storedUrl = storedPaths.get(value);
        if (storedUrl != null) {
            value = storedUrl;
        }
        return value;
    }

    private String getUsername() {
        return repositoryPanel.userTextField.getText().trim();
    }

    private char[] getPassword() {
        return repositoryPanel.userPasswordField.getPassword();
    }

    private String getExternalCommand() {
        return repositoryPanel.tunnelCommandTextField.getText();
    }

    private boolean isSavePassword() {
        return repositoryPanel.savePasswordCheckBox.isSelected();
    }

    public HgURL getUrl() throws URISyntaxException {
        prepareUrl();
        assert (url != null);
        return url;
    }

    public RepositoryConnection getRepositoryConnection() {
        prepareRepositoryConnection();
        assert (repositoryConnection != null);
        return repositoryConnection;
    }
    
    private void prepareUrl() throws URISyntaxException {
        if (url != null) {
            return;
        }

        String urlString = getUrlString();
        String username = getUsername();

        if (username.length() == 0) {
            url = new HgURL(urlString);
        } else {
            url = new HgURL(urlString, username, getPassword());
        }
    }

    private void prepareRepositoryConnection() {
        if (repositoryConnection != null) {
            return;
        }
        String extCommand = getExternalCommand();
        boolean savePassword = isSavePassword();
        repositoryConnection = new RepositoryConnection(url, extCommand, savePassword);
    }

    private void onUsernameChange() {
        repositoryConnection = null;
        url = null;
    }
    
    private void onPasswordChange() {        
        repositoryConnection = null;
        url = null;
    }

    private void onTunnelCommandChange() {
        repositoryConnection = null;
    }

    private void onSavePasswordChange() {
        repositoryConnection = null;
    }

    public RepositoryPanel getPanel() {
        return repositoryPanel;
    }
    
    public boolean isValid() {
        return valid;
    }

    private void setValid() {
        setValid(true, "");                                             //NOI18N
    }

    public void setInvalid() {
        setValid(false, "");
    }

    private void setValid(boolean valid, String message) {
        if ((valid == this.valid) && message.equals(this.message)) {
            return;
        }

        if (valid != this.valid) {
            repositoryPanel.proxySettingsButton.setEnabled(valid);
            repositoryPanel.userPasswordField.setEnabled(valid);
            repositoryPanel.userTextField.setEnabled(valid);
            //repositoryPanel.savePasswordCheckBox.setEnabled(valid);
        }

        this.valid = valid;
        this.message = message;

        fireStateChanged();
    }

    private void fireStateChanged() {
        if ((listeners != null) && !listeners.isEmpty()) {
            for (ChangeListener l : listeners) {
                l.stateChanged(changeEvent);
            }
        }
    }

    public void addChangeListener(ChangeListener l) {
        if(listeners==null) {
            listeners = new ArrayList<ChangeListener>(4);
        }
        listeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        if(listeners==null) {
            return;
        }
        listeners.remove(l);
    }

    public String getMessage() {
        return message;
    }

    public void focusGained(FocusEvent focusEvent) {
        if(focusEvent.getSource()==repositoryPanel.userPasswordField) {
            repositoryPanel.userPasswordField.selectAll();
        }
    }

    public void focusLost(FocusEvent focusEvent) {
        // do nothing
    }    
    
    public void itemStateChanged(ItemEvent evt) {
        Object source = evt.getSource();

        if (source == repositoryPanel.urlComboBox) {
            if(evt.getStateChange() == ItemEvent.SELECTED) {
                comboBoxItemSelected(evt.getItem());
            }
        } else if (source == repositoryPanel.savePasswordCheckBox) {
            onSavePasswordChange();
        } else {
            assert false;
        }
    }

    private void comboBoxItemSelected(Object selectedItem) {
        if (selectedItem.getClass() == String.class) {
            urlPrefixSelected();
        } else if (selectedItem instanceof RepositoryConnection) {
            repositoryConnectionSelected((RepositoryConnection) selectedItem);
        } else {
            assert false;
        }
    }

    private void urlPrefixSelected() {
        repositoryPanel.userTextField.setText(null);
        repositoryPanel.userPasswordField.setText(null);
        repositoryPanel.tunnelCommandTextField.setText(null);
        repositoryPanel.savePasswordCheckBox.setSelected(false);

        url = null;
        repositoryConnection = null;
    }
    
    private void repositoryConnectionSelected(RepositoryConnection rc) {
        repositoryPanel.userTextField.setText(rc.getUsername());
        repositoryPanel.userPasswordField.setText(null);
        repositoryPanel.tunnelCommandTextField.setText(rc.getExternalCommand());           
        repositoryPanel.savePasswordCheckBox.setSelected(rc.isSavePassword());

        url = rc.getUrl();
        repositoryConnection = rc;
    } 

    public void setTipVisible(Boolean flag) {        
        repositoryPanel.tipLabel.setVisible(flag);
    } 

    public boolean show(String title, HelpCtx helpCtx, boolean setMaxNeddedSize) {
        RepositoryDialogPanel corectPanel = new RepositoryDialogPanel();
        corectPanel.panel.setLayout(new BorderLayout());
        JPanel p = getPanel();
        if(setMaxNeddedSize) {
            if(bPushPull){
                maxNeededSize.setSize(maxNeededSize.width, maxNeededSize.height + HG_PUSH_PULL_VERT_PADDING);
            }
            p.setPreferredSize(maxNeededSize);
        }        
        corectPanel.panel.add(p, BorderLayout.NORTH);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(corectPanel, title); // NOI18N        
        showDialog(dialogDescriptor, helpCtx, null);
        return dialogDescriptor.getValue() == DialogDescriptor.OK_OPTION;
    }
    
    public Object show(String title, HelpCtx helpCtx, Object[] options, boolean setMaxNeededSize, String name) {
        RepositoryDialogPanel corectPanel = new RepositoryDialogPanel();
        corectPanel.panel.setLayout(new BorderLayout());
        corectPanel.panel.add(getPanel(), BorderLayout.NORTH);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(corectPanel, title); // NOI18N        
        JPanel p = getPanel();
        if(setMaxNeededSize) {
            if(bPushPull){
                maxNeededSize.setSize(maxNeededSize.width, maxNeededSize.height + HG_PUSH_PULL_VERT_PADDING);
            }
            p.setPreferredSize(maxNeededSize);
        }        
        if(options!= null) {
            dialogDescriptor.setOptions(options); // NOI18N
        }        
        showDialog(dialogDescriptor, helpCtx, name);
        return dialogDescriptor.getValue();
    }
    
    private void showDialog(DialogDescriptor dialogDescriptor, HelpCtx helpCtx, String name) {
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(helpCtx);        

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);        
        if (name != null) {
            dialog.addWindowListener(new DialogBoundsPreserver(HgModuleConfig.getDefault().getPreferences(), name)); // NOI18N
        }
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(Repository.class, "ACSD_RepositoryPanel"));

        dialog.setVisible(true);
    }

    private boolean isSet(int flag) {
        return (modeMask & flag) != 0;
    }

    private class UrlRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof String) {
                String url = storedPaths.get((String) value);
                if (url != null) {
                    value = value + " (" + url + ")"; //NOI18N
                }
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }

    }
    
}
