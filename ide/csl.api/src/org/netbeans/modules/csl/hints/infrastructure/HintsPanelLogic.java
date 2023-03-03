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

package org.netbeans.modules.csl.hints.infrastructure;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.Rule;

import org.netbeans.modules.csl.api.Rule.UserConfigurableRule;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;


/** Contains all important listeners and logic of the Hints Panel.
 *
 * @author Petr Hrebejk
 */
class HintsPanelLogic implements MouseListener, KeyListener, TreeSelectionListener, ChangeListener, ActionListener {

    private static final Logger LOG = Logger.getLogger(HintsPanelLogic.class.getName());
    
    private Map<UserConfigurableRule,ModifiedPreferences> changes;
    
    private static final Map<HintSeverity,Integer> severity2index;
    
    private static final String DESCRIPTION_HEADER = 
        "<html><head>" + // NOI18N
        //"<link rel=\"StyleSheet\" href=\"nbdocs://org.netbeans.modules.usersguide/org/netbeans/modules/usersguide/ide.css\" type=\"text/css\">" // NOI18N
        //"<link rel=\"StyleSheet\" href=\"nbresloc:/org/netbeans/modules/csl/hints/infrastructure/ide.css\" type=\"text/css\">" + // NOI18N
        "</head><body>"; // NOI18N

    private static final String DESCRIPTION_FOOTER = "</body></html>"; // NOI18N
    
    
    static {
        severity2index = new HashMap<HintSeverity, Integer>();
        severity2index.put( HintSeverity.ERROR, 0  );
        severity2index.put( HintSeverity.WARNING, 1  );
        severity2index.put( HintSeverity.CURRENT_LINE_WARNING, 2  );
        severity2index.put( HintSeverity.INFO, 3  );
    }
    
    private JTree errorTree;
    private JComboBox severityComboBox;
    private JCheckBox tasklistCheckBox;
    private JPanel customizerPanel;
    private JEditorPane descriptionTextArea;
    private GsfHintsManager manager;
    private DefaultTreeModel errModel;
    
    HintsPanelLogic(GsfHintsManager manager) {
        this.manager = manager;
        changes = new HashMap<UserConfigurableRule, ModifiedPreferences>();        
    }
    
    void connect( JTree errorTree, DefaultTreeModel errModel, JComboBox severityComboBox, 
                  JCheckBox tasklistCheckBox, JPanel customizerPanel,
                  JEditorPane descriptionTextArea) {
        
        this.errorTree = errorTree;
        this.errModel = errModel;
        this.severityComboBox = severityComboBox;
        this.tasklistCheckBox = tasklistCheckBox;
        this.customizerPanel = customizerPanel;
        this.descriptionTextArea = descriptionTextArea;        
        
        valueChanged( null );
        
        errorTree.addKeyListener(this);
        errorTree.addMouseListener(this);
        errorTree.getSelectionModel().addTreeSelectionListener(this);
            
        severityComboBox.addActionListener(this);
        tasklistCheckBox.addChangeListener(this);
        
    }
    
    void disconnect() {
        
        errorTree.removeKeyListener(this);
        errorTree.removeMouseListener(this);
        errorTree.getSelectionModel().removeTreeSelectionListener(this);
            
        severityComboBox.removeActionListener(this);
        tasklistCheckBox.removeChangeListener(this);
                
        componentsSetEnabled( false );
    }
    
    synchronized void applyChanges() {
        for (Map.Entry<UserConfigurableRule, ModifiedPreferences> entry : changes.entrySet()) {
            UserConfigurableRule hint = entry.getKey();
            ModifiedPreferences mn = entry.getValue();
            mn.store(HintsSettings.getPreferences(manager, hint, HintsSettings.getCurrentProfileId()));            
        }
        
        updateHints();
    }


    /** Regenerate hints for the current file, if you change settings */
    private void updateHints() {
        JTextComponent pane = EditorRegistry.lastFocusedComponent();
        if (pane != null) {
            Document doc = pane.getDocument();
            final Source source = Source.create(doc);
            // see issue #212967; non-file Source appears for some reason.
            if (source != null && source.getFileObject() != null) {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        try {
                            ParserManager.parse(Collections.singleton(source), new UserTask() {
                                public @Override void run(ResultIterator resultIterator) throws Exception {
                                    GsfHintsManager.refreshHints(resultIterator);
                                }
                            });
                        } catch (ParseException ex) {
                            LOG.log(Level.WARNING, null, ex);
                        }
                    }
                });
            }
        }
    }
    
    /** Were there any changes in the settings
     */
    boolean isChanged() {
        boolean isChanged = false;
        for (Map.Entry<UserConfigurableRule, ModifiedPreferences> entry : changes.entrySet()) {
            UserConfigurableRule hint = entry.getKey();
            ModifiedPreferences mn = entry.getValue();

            Boolean currentEnabled = mn.getBoolean(HintsSettings.ENABLED_KEY, hint.getDefaultEnabled());
            Boolean savedEnabled = HintsSettings.isEnabled(manager, hint);
            isChanged |= currentEnabled != savedEnabled;
            if (isChanged) {
                return true;
            }

            String currentSeverity = mn.get(HintsSettings.SEVERITY_KEY, hint.getDefaultSeverity().toString());
            String savedSeverity = HintsSettings.getSeverity(manager, hint).toString();
            isChanged |= !currentSeverity.equals(savedSeverity);
            if (isChanged) {
                return true;
            }

            try {
                for (String key : mn.keys()) {
                    if(key.equals(HintsSettings.ENABLED_KEY) || key.equals(HintsSettings.SEVERITY_KEY)) {
                        continue;
                    }
                    String current = mn.get(key, null);
                    String saved = mn.getSavedValue(key);
                    isChanged |= current == null ? saved != null : !current.equals(saved);
                    if (isChanged) {
                        return true;
                    }
                }
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return false;
    }
    
    synchronized Preferences getCurrentPrefernces(UserConfigurableRule hint ) {
        Preferences node = changes.get(hint);
        return node == null ? HintsSettings.getPreferences(manager, hint, HintsSettings.getCurrentProfileId() ) : node;
    }
    
    synchronized Preferences getPreferences4Modification( UserConfigurableRule hint ) {        
        Preferences node = changes.get(hint);        
        if ( node == null ) {
            node = new ModifiedPreferences( HintsSettings.getPreferences(manager, hint, HintsSettings.getCurrentProfileId() ) );
            changes.put( hint, (ModifiedPreferences)node);
        }        
        return node;                
    }
    
    
    
    static Object getUserObject( TreePath path ) {
        if( path == null ) {
            return null;
        }
        DefaultMutableTreeNode tn = (DefaultMutableTreeNode)path.getLastPathComponent();
        return tn.getUserObject();
    }
    
    static Object getUserObject( DefaultMutableTreeNode node ) {
        return node.getUserObject();
    }
    
    boolean isSelected( DefaultMutableTreeNode node ) {
        for( int i = 0; i < node.getChildCount(); i++ ) {
            DefaultMutableTreeNode ch = (DefaultMutableTreeNode) node.getChildAt(i);
            Object o = ch.getUserObject();
            if ( o instanceof Rule ) {
                UserConfigurableRule hint = (UserConfigurableRule) o;
                if ( HintsSettings.isEnabled(manager, hint, getCurrentPrefernces(hint)) ) {
                    return true;
                }
            }
        }
        return false;
    }
    
    // MouseListener implementation --------------------------------------------
    
    public void mouseClicked(MouseEvent e) {
        Point p = e.getPoint();
        TreePath path = errorTree.getPathForLocation(e.getPoint().x, e.getPoint().y);
        if ( path != null ) {
            Rectangle r = errorTree.getPathBounds(path);
            if (r != null) {
                r.width = r.height;
                if ( r.contains(p)) {
                    toggle( path );
                }
            }
        }
    }

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) {}
    
    // KeyListener implementation ----------------------------------------------

    public void keyTyped(KeyEvent e) {}

    public void keyReleased(KeyEvent e) {}

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {

            if ( e.getSource() instanceof JTree ) {
                JTree tree = (JTree) e.getSource();
                TreePath path = tree.getSelectionPath();

                if ( toggle( path )) {
                    e.consume();
                }
            }
        }
    }
    
    private boolean ignoreControlChanges;
    
    // TreeSelectionListener implementation ------------------------------------
    
    public void valueChanged(TreeSelectionEvent ex) {            
        Object o = getUserObject(errorTree.getSelectionPath());
        
        if ( o instanceof UserConfigurableRule ) {
            UserConfigurableRule hint = (UserConfigurableRule) o;
            
            // Enable components
            componentsSetEnabled(true);
            
            // Set proper values to the components
            
            Preferences p = getCurrentPrefernces(hint);

            ignoreControlChanges = true;
            try {
                HintSeverity severity = HintsSettings.getSeverity(hint, p);
                severityComboBox.setSelectedIndex(severity2index.get(severity));

                boolean toTasklist = HintsSettings.isShowInTaskList(hint, p);
                tasklistCheckBox.setSelected(toTasklist);

                String description = hint.getDescription();
                descriptionTextArea.setText( description == null ? wrapDescription("") : wrapDescription(description)); // NOI18N
            } finally {                                    
                ignoreControlChanges = false;
            }
            // Optionally show the customizer
            customizerPanel.removeAll();
            JComponent c = hint.getCustomizer(getPreferences4Modification(hint));
            if ( c != null ) {               
                customizerPanel.add(c, BorderLayout.CENTER);
            }            
            customizerPanel.getParent().invalidate();
            ((JComponent)customizerPanel.getParent()).revalidate();
            customizerPanel.getParent().repaint();
        }
        else { // Category or nonsense selected.
            componentsSetEnabled(false);
        }
    }
    
    // ActionListener implementation -------------------------------------------
    
    public void actionPerformed(ActionEvent e) {
        if( errorTree.getSelectionPath() == null || ignoreControlChanges) {
            return;
        }
        
        Object o = getUserObject(errorTree.getSelectionPath());
        
        if ( o instanceof UserConfigurableRule ) {
            UserConfigurableRule hint = (UserConfigurableRule) o;
            Preferences p = getPreferences4Modification(hint);
            
            if( severityComboBox.equals( e.getSource() ) ) {
                HintsSettings.setSeverity(p, index2severity(severityComboBox.getSelectedIndex()));
            }            
        }
    }

   
    // ChangeListener implementation -------------------------------------------
    
    public void stateChanged(ChangeEvent e) {
        // System.out.println("Task list box changed ");
    }
   
    // Private methods ---------------------------------------------------------

    private String wrapDescription( String description ) {
        return new StringBuffer( DESCRIPTION_HEADER ).append(description).append(DESCRIPTION_FOOTER).toString();        
    }
    
    private HintSeverity index2severity( int index ) {
        for( Map.Entry<HintSeverity,Integer> e : severity2index.entrySet()) {
            if ( e.getValue() == index ) {
                return e.getKey();
            }
        }
        throw new IllegalStateException( "Unknown severity");
    }
       
    private boolean toggle( TreePath treePath ) {

        if( treePath == null ) {
            return false;
        }

        Object o = getUserObject(treePath);

        DefaultTreeModel model = errModel;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();


        if ( o instanceof UserConfigurableRule ) {
            UserConfigurableRule hint = (UserConfigurableRule)o;
            boolean value = HintsSettings.isEnabled(manager, hint,getCurrentPrefernces(hint));
            Preferences mn = getPreferences4Modification(hint);
            HintsSettings.setEnabled(mn, !value);
            model.nodeChanged(node);
            model.nodeChanged(node.getParent());
        }
        else if ( o instanceof FileObject ) {
            boolean value = !isSelected(node);
                                   
            for( int i = 0; i < node.getChildCount(); i++ ) {
                DefaultMutableTreeNode ch = (DefaultMutableTreeNode) node.getChildAt(i);                
                Object cho = ch.getUserObject();
                if ( cho instanceof UserConfigurableRule ) {
                    UserConfigurableRule hint = (UserConfigurableRule)cho;
                    boolean cv = HintsSettings.isEnabled(manager, hint,getCurrentPrefernces(hint));
                    if ( cv != value ) {                    
                        Preferences mn = getPreferences4Modification(hint);
                        HintsSettings.setEnabled(mn, value);
                        model.nodeChanged( ch );
                    }
                }
            }            
            model.nodeChanged(node);
        }

        return false;
    }
    
    private void componentsSetEnabled( boolean enabled ) {
        
        if ( !enabled ) {
            customizerPanel.removeAll();
            customizerPanel.getParent().invalidate();
            ((JComponent)customizerPanel.getParent()).revalidate();
            customizerPanel.getParent().repaint();
            severityComboBox.setSelectedIndex(severity2index.get(HintsSettings.SEVERITY_DEFAUT));
            tasklistCheckBox.setSelected(HintsSettings.IN_TASK_LIST_DEFAULT);
            descriptionTextArea.setText(wrapDescription("")); // NOI18N
        }
        
        severityComboBox.setEnabled(enabled);
        tasklistCheckBox.setEnabled(enabled);
        descriptionTextArea.setEnabled(enabled);
    }
        
    // Inner classes -----------------------------------------------------------
           
    private static class ModifiedPreferences extends AbstractPreferences {
        
        private Map<String,Object> map = new HashMap<String, Object>();
        private final Map<String,String> mapSaved = new HashMap<String,String>();
        private boolean modified;
        public ModifiedPreferences( Preferences node ) {
            super(null, ""); // NOI18N
            try {                
                for (java.lang.String key : node.keys()) {
                    put(key, node.get(key, null));
                }
                modified = false;
            }
            catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
             
        public boolean isModified() {
            return modified;
        }
        
        public void store( Preferences target ) {
            
            try {
                for (String key : keys()) {
                    target.put(key, get(key, null));
                }
                modified = false;
            }
            catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }

        }
        
        public String getSavedValue(String key) {
            return mapSaved.get(key);
        }
        
        protected void putSpi(String key, String value) {
            modified = true;
            map.put(key, value);            
            if(!mapSaved.containsKey(key)) {
                // The saved value for key is equal to the default value, which should be set by the Customizer's constructor
                mapSaved.put(key, value);
            }
        }

        protected String getSpi(String key) {
            return (String)map.get(key);                    
        }

        protected void removeSpi(String key) {
            modified = true;
            map.remove(key);
        }

        protected void removeNodeSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected String[] keysSpi() throws BackingStoreException {
            String array[] = new String[map.keySet().size()];
            return map.keySet().toArray( array );
        }

        protected String[] childrenNamesSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected AbstractPreferences childSpi(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected void syncSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected void flushSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }

   
}
