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

package org.netbeans.modules.maven.hints.pom;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;
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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.netbeans.modules.maven.hints.pom.spi.Configuration;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

import org.netbeans.modules.maven.hints.pom.spi.POMErrorFixBase;
import org.netbeans.modules.maven.hints.pom.spi.POMErrorFixProvider;


/** Contains all important listeners and logic of the Hints Panel.
 *
 * @author Petr Hrebejk
 */
class HintsPanelLogic implements MouseListener, KeyListener, TreeSelectionListener, ChangeListener, ActionListener {

    private final Map<POMErrorFixBase, ModifiedPreferences> changes;
    
    private static final Map<Configuration.HintSeverity, Integer> severity2index;
    
    private static final String DESCRIPTION_HEADER = 
        "<html><head>" + // NOI18N
        //"<link rel=\"StyleSheet\" href=\"nbdocs://org.netbeans.modules.usersguide/org/netbeans/modules/usersguide/ide.css\" type=\"text/css\">" // NOI18N
        //"<link rel=\"StyleSheet\" href=\"nbresloc:/org/netbeans/modules/java/hints/resources/ide.css\" type=\"text/css\">" + // NOI18N
        "</head><body>"; // NOI18N

    private static final String DESCRIPTION_FOOTER = "</body></html>"; // NOI18N
    
    
    static {
        severity2index = new HashMap<Configuration.HintSeverity, Integer>();
        severity2index.put( Configuration.HintSeverity.ERROR, 0  );
        severity2index.put( Configuration.HintSeverity.WARNING, 1  );
    }
    
    private JTree errorTree;
    private JComboBox severityComboBox;
    private JCheckBox tasklistCheckBox;
    private JPanel customizerPanel;
    private JEditorPane descriptionTextArea;
    
    HintsPanelLogic() {
        changes = new HashMap<POMErrorFixBase, ModifiedPreferences>();
    }
    
    void connect( JTree errorTree, JComboBox severityComboBox, 
                  JCheckBox tasklistCheckBox, JPanel customizerPanel,
                  JEditorPane descriptionTextArea) {
        
        this.errorTree = errorTree;
        this.severityComboBox = severityComboBox;
        this.tasklistCheckBox = tasklistCheckBox;
        this.customizerPanel = customizerPanel;
        this.descriptionTextArea = descriptionTextArea;        

        errorTree.addKeyListener(this);
        errorTree.addMouseListener(this);
        errorTree.getSelectionModel().addTreeSelectionListener(this);
            
        severityComboBox.addActionListener(this);
        tasklistCheckBox.addChangeListener(this);

        valueChanged(null);

    }
    
    void disconnect() {
        
        errorTree.removeKeyListener(this);
        errorTree.removeMouseListener(this);
        errorTree.getSelectionModel().removeTreeSelectionListener(this);
            
        severityComboBox.removeActionListener(this);
        tasklistCheckBox.removeChangeListener(this);
                
        componentsSetEnabled( false );
        for (POMErrorFixBase hint : changes.keySet()) {
            if (hint instanceof POMErrorFixProvider) {
                ((POMErrorFixProvider) hint).cancel();
            }
            Configuration config = hint.getConfiguration();
            if (config != null) {
                config.resetSavedValues();
            }
        }
        changes.clear();
    }
    
    synchronized void applyChanges() {
        for (Map.Entry<POMErrorFixBase, ModifiedPreferences> entry : changes.entrySet()) {
            POMErrorFixBase hint = entry.getKey();
            ModifiedPreferences mn = entry.getValue();
            mn.store(hint.getConfiguration().getPreferences());
        }
    }
    
    /** Were there any changes in the settings
     */
    boolean isChanged() {
        boolean isChanged = false;
        for (Map.Entry<POMErrorFixBase, ModifiedPreferences> entry : changes.entrySet()) {
            POMErrorFixBase hint = entry.getKey();
            Preferences prefs = entry.getValue();
            try {
                for (String key : prefs.keys()) {
                    String current = prefs.get(key, null);
                    String saved;
                    if(key.equals(Configuration.ENABLED_KEY)) {
                        saved = Boolean.toString((Boolean)hint.getConfiguration().getSavedValue(key));
                    } else if(key.equals(Configuration.SEVERITY_KEY)) {
                        saved = ((Configuration.HintSeverity)hint.getConfiguration().getSavedValue(key)).toPreferenceString();
                    } else { // the key refers to a property handled by a custom customizer
                        saved = ((POMErrorFixProvider) hint).getSavedValue(hint.getCustomizer(prefs), key);
                    }
                    isChanged |= current == null ? saved != null : !current.equals(saved);
                    if (isChanged) { // no need to iterate further
                        return true;
                    }
                }
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return isChanged;
    }
    
    synchronized Preferences getCurrentPrefernces( POMErrorFixBase hint ) {
        Preferences node = changes.get(hint);
        return node == null ? hint.getConfiguration().getPreferences() : node;
    }
    
    synchronized Preferences getPreferences4Modification( POMErrorFixBase hint ) {
        Preferences node = changes.get(hint);        
        if ( node == null ) {
            node = new ModifiedPreferences( hint.getConfiguration().getPreferences() );
            changes.put( hint, (ModifiedPreferences)node);
        }        
        return node;                
    }
    
    
    
    static Object getUserObject( TreePath path ) {
        if( path == null )
            return null;
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
            if ( o instanceof POMErrorFixBase ) {
                POMErrorFixBase hint = (POMErrorFixBase)o;
                if ( hint.getConfiguration().isEnabled(getCurrentPrefernces(hint)) ) {
                    return true;
                }
            }
        }
        return false;
    }
    
    // MouseListener implementation --------------------------------------------
    
    @Override
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

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}
    
    // KeyListener implementation ----------------------------------------------

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER ) {

            if ( e.getSource() instanceof JTree ) {
                JTree tree = (JTree) e.getSource();
                TreePath path = tree.getSelectionPath();

                if ( toggle( path )) {
                    e.consume();
                }
            }
        }
    }
    
    // TreeSelectionListener implementation ------------------------------------
    
    @Override
    public void valueChanged(TreeSelectionEvent ex) {            
        Object o = getUserObject(errorTree.getSelectionPath());
        
        if ( o instanceof POMErrorFixBase ) {
            POMErrorFixBase hint = (POMErrorFixBase) o;
            
            // Enable components
            componentsSetEnabled(true);
            
            // Set proper values to the components
            
            Preferences p = getCurrentPrefernces(hint);
            
            Configuration.HintSeverity severity = hint.getConfiguration().getSeverity(p);
            if (severity != null) {
                severityComboBox.setSelectedIndex(severity2index.get(severity));
                severityComboBox.setEnabled(true);
            } else {
                severityComboBox.setSelectedIndex(severity2index.get(Configuration.HintSeverity.ERROR));
                severityComboBox.setEnabled(false);
            }
            
//XXX            boolean toTasklist = HintsSettings.isShowInTaskList(hint, p);
//            tasklistCheckBox.setSelected(toTasklist);
            
            String description = hint.getConfiguration().getDescription();
            descriptionTextArea.setText( description == null ? "" : wrapDescription(description)); // NOI18N
                                    
            // Optionally show the customizer
            customizerPanel.removeAll();
            JComponent c = hint.getCustomizer(ex == null ? 
                getCurrentPrefernces(hint) :
                getPreferences4Modification(hint));
            
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
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if( errorTree.getSelectionPath() == null )
            return;
        
        Object o = getUserObject(errorTree.getSelectionPath());
        
        if ( o instanceof POMErrorFixBase ) {
            POMErrorFixBase hint = (POMErrorFixBase) o;
            Preferences p = getPreferences4Modification(hint);
            
            if(hint.getConfiguration().getSeverity(p) != null && severityComboBox.equals( e.getSource() ) )
                hint.getConfiguration().setSeverity(p, index2severity(severityComboBox.getSelectedIndex()));
        }
    }

   
    // ChangeListener implementation -------------------------------------------
    
    @Override
    public void stateChanged(ChangeEvent e) {
        // System.out.println("Task list box changed ");
    }
   
    // Private methods ---------------------------------------------------------

    private String wrapDescription( String description ) {
        return new StringBuffer( DESCRIPTION_HEADER ).append(description).append(DESCRIPTION_FOOTER).toString();        
    }
    
    private Configuration.HintSeverity index2severity( int index ) {
        for( Map.Entry<Configuration.HintSeverity,Integer> e : severity2index.entrySet()) {
            if ( e.getValue() == index ) {
                return e.getKey();
            }
        }
        throw new IllegalStateException( "Unknown severity");
    }
       
    private boolean toggle( TreePath treePath ) {

        if( treePath == null )
            return false;

        Object o = getUserObject(treePath);

        DefaultTreeModel model = (DefaultTreeModel) errorTree.getModel();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();


        if ( o instanceof POMErrorFixBase ) {
            POMErrorFixBase hint = (POMErrorFixBase)o;
            boolean value = !hint.getConfiguration().isEnabled(getCurrentPrefernces(hint));
            Preferences mn = getPreferences4Modification(hint);
            hint.getConfiguration().setEnabled(mn, value);
            model.nodeChanged(node);
            model.nodeChanged(node.getParent());
        }
        else if ( o instanceof FileObject ) {
            boolean value = !isSelected(node);
                                   
            for( int i = 0; i < node.getChildCount(); i++ ) {
                DefaultMutableTreeNode ch = (DefaultMutableTreeNode) node.getChildAt(i);                
                Object cho = ch.getUserObject();
                if ( cho instanceof POMErrorFixBase ) {
                    POMErrorFixBase hint = (POMErrorFixBase)cho;
                    boolean cv = hint.getConfiguration().isEnabled(getCurrentPrefernces(hint));
                    if ( cv != value ) {                    
                        Preferences mn = getPreferences4Modification(hint);
                        hint.getConfiguration().setEnabled(mn, value);
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
            severityComboBox.setSelectedIndex(severity2index.get(Configuration.HintSeverity.WARNING));
//XXX            tasklistCheckBox.setSelected(HintsSettings.IN_TASK_LIST_DEFAULT);
            descriptionTextArea.setText(""); // NOI18N
        }
        
        severityComboBox.setEnabled(enabled);
        tasklistCheckBox.setEnabled(enabled);
        descriptionTextArea.setEnabled(enabled);
    }
        
    // Inner classes -----------------------------------------------------------
           
    private static class ModifiedPreferences extends AbstractPreferences {
        
        private final Map<String,Object> map = new HashMap<String, Object>();

        public ModifiedPreferences( Preferences node ) {
            super(null, ""); // NOI18N
            try {                
                for (java.lang.String key : node.keys()) {
                    put(key, node.get(key, null));
                }
            }
            catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
             
        
        public void store( Preferences target ) {
            
            try {
                for (String key : keys()) {
                    target.put(key, get(key, null));
                }
            }
            catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }

        }
        
        @Override
        protected void putSpi(String key, String value) {
            map.put(key, value);            
        }

        @Override
        protected String getSpi(String key) {
            return (String)map.get(key);                    
        }

        @Override
        protected void removeSpi(String key) {
            map.remove(key);
        }

        @Override
        protected void removeNodeSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected String[] keysSpi() throws BackingStoreException {
            String array[] = new String[map.keySet().size()];
            return map.keySet().toArray( array );
        }

        @Override
        protected String[] childrenNamesSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected AbstractPreferences childSpi(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void syncSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void flushSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }

   
}
