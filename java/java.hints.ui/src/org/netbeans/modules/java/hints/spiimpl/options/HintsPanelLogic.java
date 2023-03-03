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

package org.netbeans.modules.java.hints.spiimpl.options;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata.Options;
import org.netbeans.modules.java.hints.spiimpl.options.DepScanningSettings.DependencyTracking;
import org.netbeans.modules.java.hints.spiimpl.options.HintsPanel.State;
import org.netbeans.modules.java.hints.spiimpl.refactoring.Configuration;
import org.netbeans.modules.java.hints.spiimpl.refactoring.Utilities;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.editor.hints.settings.FileHintPreferences;
import org.netbeans.spi.java.hints.Hint.Kind;
import org.openide.awt.Mnemonics;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/** Contains all important listeners and logic of the Hints Panel.
 *
 * @author Petr Hrebejk
 */
public class HintsPanelLogic implements MouseListener, KeyListener, TreeSelectionListener, ChangeListener, ActionListener, ItemListener {

    private Map<HintMetadata,ModifiedPreferences> changes = new HashMap<HintMetadata, ModifiedPreferences>();
    private DependencyTracking depScn = null;
    
    private static final Map<Severity,Integer> severity2index;
    private static final Map<Integer,Severity> index2Severity;
    private static final Map<DependencyTracking,Integer> deptracking2index;
    
    private static final String DESCRIPTION_HEADER = 
        "<html><head>" + // NOI18N
        //"<link rel=\"StyleSheet\" href=\"nbdocs://org.netbeans.modules.usersguide/org/netbeans/modules/usersguide/ide.css\" type=\"text/css\">" // NOI18N
        //"<link rel=\"StyleSheet\" href=\"nbresloc:/org/netbeans/modules/java/hints/resources/ide.css\" type=\"text/css\">" + // NOI18N
        "</head><body>"; // NOI18N

    private static final String DESCRIPTION_FOOTER = "</body></html>"; // NOI18N
    
    
    static {
        severity2index = new EnumMap<Severity, Integer>(Severity.class);
        severity2index.put( Severity.ERROR, 0  );
        severity2index.put( Severity.VERIFIER, 1  );
        severity2index.put( Severity.HINT, 2  );
        severity2index.put( Severity.WARNING, 1  );
        index2Severity = new HashMap<Integer, Severity>();
        index2Severity.put(0, Severity.ERROR);
        index2Severity.put(1, Severity.VERIFIER);
        index2Severity.put(2, Severity.HINT);
        deptracking2index = new EnumMap<DepScanningSettings.DependencyTracking, Integer>(DepScanningSettings.DependencyTracking.class);
        deptracking2index.put(DependencyTracking.ENABLED, 0);
        deptracking2index.put(DependencyTracking.ENABLED_WITHIN_PROJECT, 1);
        deptracking2index.put(DependencyTracking.ENABLED_WITHIN_ROOT, 2);
    }
    
    private JTree errorTree;
    DefaultTreeModel errorTreeModel;
    private JLabel severityLabel;
    private JComboBox severityComboBox;
    private JCheckBox tasklistCheckBox;
    private JPanel customizerPanel;
    private JEditorPane descriptionTextArea;
    private DefaultComboBoxModel<String> defModel = new DefaultComboBoxModel<>();
    private DefaultComboBoxModel<String> depScanningModel = new DefaultComboBoxModel<>();
    private String defLabel = NbBundle.getMessage(HintsPanel.class, "CTL_ShowAs_Label"); //NOI18N
    private String depScanningLabel = NbBundle.getMessage(HintsPanel.class, "CTL_Scope_Label"); //NOI18N
    private String depScanningDescription = NbBundle.getMessage(HintsPanel.class, "CTL_Scope_Desc"); //NOI18N
    private JComboBox configCombo;
//    private String currentProfileId = HintsSettings.getCurrentProfileId();
    private JButton editScript;
    private HintsSettings originalSettings;
            WritableSettings writableSettings;
    private boolean direct;
    
    HintsPanelLogic() {
        defModel.addElement(NbBundle.getMessage(HintsPanel.class, "CTL_AsError")); //NOI18N
        defModel.addElement(NbBundle.getMessage(HintsPanel.class, "CTL_AsWarning")); //NOI18N
        defModel.addElement(NbBundle.getMessage(HintsPanel.class, "CTL_WarningOnCurrentLine")); //NOI18N

        depScanningModel.addElement(NbBundle.getMessage(HintsPanel.class, "CTL_AllProjects")); //NOI18N
        depScanningModel.addElement(NbBundle.getMessage(HintsPanel.class, "CTL_Project")); //NOI18N
        depScanningModel.addElement(NbBundle.getMessage(HintsPanel.class, "CTL_SrcRoot")); //NOI18N
    }
    
    void connect( final JTree errorTree, DefaultTreeModel errorTreeModel, JLabel severityLabel, JComboBox severityComboBox,
                  JCheckBox tasklistCheckBox, JPanel customizerPanel,
                  JEditorPane descriptionTextArea, final JComboBox configCombo, JButton editScript,
                  HintsSettings settings, boolean direct) {
        
        this.errorTree = errorTree;
        this.errorTreeModel = errorTreeModel;
        this.severityLabel = severityLabel;
        this.severityComboBox = severityComboBox;
        this.tasklistCheckBox = tasklistCheckBox;
        this.customizerPanel = customizerPanel;
        this.descriptionTextArea = descriptionTextArea;        
        this.configCombo = configCombo;
        this.editScript = editScript;
        this.direct = direct;
        
        if (configCombo.getSelectedItem() !=null) {
            originalSettings = ((Configuration) configCombo.getSelectedItem()).getSettings();
        } else if (settings != null) {
            originalSettings = settings;
        } else {
            originalSettings = HintsSettings.getGlobalSettings();
        }
        
        writableSettings = new WritableSettings(originalSettings, direct);
        
        valueChanged( null );
        
        errorTree.addKeyListener(this);
        errorTree.addMouseListener(this);
        errorTree.getSelectionModel().addTreeSelectionListener(this);
            
        this.configCombo.addItemListener(this);
        severityComboBox.addActionListener(this);
        tasklistCheckBox.addChangeListener(this);
        
    }
    
    void disconnect() {
        
        errorTree.removeKeyListener(this);
        errorTree.removeMouseListener(this);
        errorTree.getSelectionModel().removeTreeSelectionListener(this);
            
        severityComboBox.removeActionListener(this);
        tasklistCheckBox.removeChangeListener(this);
        configCombo.removeItemListener(this);
        
        componentsSetEnabled( false );
    }
    
//    String getCurrentProfileId() {
//        return currentProfileId;
//    }

    synchronized void setOverlayPreferences(HintsSettings settings, boolean direct) {
        applyChanges();
        this.originalSettings = settings != null ? settings : HintsSettings.getGlobalSettings();
        this.writableSettings = new WritableSettings(originalSettings, direct);
        valueChanged(null);
        errorTree.repaint();
    }
    
    synchronized HintsSettings getOverlayPreferences() {
        return originalSettings;
    }
    
    synchronized void applyChanges() {
	boolean containsChanges = writableSettings.isModified();
        writableSettings.commit();
	if (containsChanges) {
            FileHintPreferences.fireChange();
	}
        if (depScn != null)
            DepScanningSettings.setDependencyTracking(depScn);
    }
    
    /** Were there any changes in the settings
     */
    boolean isChanged() {
        return writableSettings.isModified() || (depScn != null && depScn != DepScanningSettings.getDependencyTracking());
    }
    
//    synchronized Preferences getCurrentPrefernces(HintMetadata hm) {
//        Preferences node = changes.get(hm);
//        return node == null ? settings.getHintPreferences(hm) : node;
//    }
//    
//    synchronized Preferences getPreferences4Modification(HintMetadata hm) {
//        Preferences node = changes.get(hm);
//        if ( node == null ) {
//            node = new ModifiedPreferences(settings.getHintPreferences(hm));
//            changes.put(hm, (ModifiedPreferences)node);
//        }        
//        return node;                
//    }
    
    synchronized DependencyTracking getCurrentDependencyTracking() {
        return depScn != null ? depScn : DepScanningSettings.getDependencyTracking();
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
    
    State isSelected( DefaultMutableTreeNode node ) {
        boolean hasEnabled = false;
        boolean hasDisabled = false;
        List<DefaultMutableTreeNode> todo = new LinkedList<>();

        todo.add(node);
        
        while (!todo.isEmpty()) {
            DefaultMutableTreeNode current = todo.remove(0);
            Object o = current.getUserObject();
            if ( o instanceof HintMetadata ) {
                HintMetadata hint = (HintMetadata)o;
                if (isEnabled(hint)) {
                    hasEnabled = true;
                } else {
                    hasDisabled = true;
                }
            } else if (o instanceof HintCategory) {
                for (int i = 0; i < current.getChildCount(); i++) {
                    todo.add((DefaultMutableTreeNode) current.getChildAt(i));
                }
            }
        }
        return hasEnabled ? hasDisabled ? State.OTHER : State.SELECTED : State.NOT_SELECTED;
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
    
    // TreeSelectionListener implementation ------------------------------------
    private boolean ignoreControlChanges;
    
    @Override
    public void valueChanged(TreeSelectionEvent ex) {            
        Object o = getUserObject(errorTree.getSelectionPath());
        
        editScript.setEnabled(false);
        ignoreControlChanges = true;
        try {
            if ( o instanceof HintMetadata ) {
                if (defModel != severityComboBox.getModel()) {
                    severityComboBox.setModel(defModel);
                    Mnemonics.setLocalizedText(severityLabel, defLabel);
                }

                HintMetadata hint = (HintMetadata) o;

                // Enable components
                componentsSetEnabled(true);

                editScript.setEnabled(hint.category.equals(Utilities.CUSTOM_CATEGORY));

                // Set proper values to the components
                if (hint.kind == Kind.ACTION) {
                    severityComboBox.setSelectedIndex(severity2index.get(Severity.HINT));
                    severityComboBox.setEnabled(false);
                } else {
                    Severity severity = writableSettings.getSeverity(hint);
                    if (severity != null) {
                        severityComboBox.setSelectedIndex(severity2index.get(severity));
                        severityComboBox.setEnabled(true);
                    } else {
                        severityComboBox.setSelectedIndex(severity2index.get(Severity.ERROR));
                        severityComboBox.setEnabled(false);
                    }
                }

                //TODO: tasklist checkbox
    //            boolean toTasklist = HintsSettings.isShowInTaskList(hint, p);
    //            tasklistCheckBox.setSelected(toTasklist);

                String description = hint.description;
                descriptionTextArea.setText( description == null ? "" : wrapDescription(description, hint)); // NOI18N

                // Optionally show the customizer
                customizerPanel.removeAll();
                JComponent c = hint.customizer != null ? hint.customizer.getCustomizer(/*TODO: will always create modified prefs*/writableSettings.getHintPreferences(hint)) : null;

                if ( c != null ) {               
                    customizerPanel.add(c, BorderLayout.CENTER);
                }            
                customizerPanel.getParent().invalidate();
                ((JComponent)customizerPanel.getParent()).revalidate();
                customizerPanel.getParent().repaint();
            }
            else if (o instanceof String) {
                DependencyTracking dt = getCurrentDependencyTracking();
                if (depScanningModel != severityComboBox.getModel()) {
                    severityComboBox.setModel(depScanningModel);
                    Mnemonics.setLocalizedText(severityLabel, depScanningLabel);
                }
                componentsSetEnabled(false);
                severityComboBox.setEnabled(true);
                descriptionTextArea.setEnabled(true);
                descriptionTextArea.setText(wrapDescription(depScanningDescription, null));
                descriptionTextArea.setCaretPosition(0);
                if (dt != DependencyTracking.DISABLED)
                    severityComboBox.setSelectedIndex(deptracking2index.get(dt));
            }
            else { // Category or nonsense selected.
                if (defModel != severityComboBox.getModel()) {
                    severityComboBox.setModel(defModel);
                    Mnemonics.setLocalizedText(severityLabel, defLabel);
                }
                componentsSetEnabled(false);
            }
        } finally {
            ignoreControlChanges = false;
        }
    }
    
    // ActionListener implementation -------------------------------------------
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if( errorTree.getSelectionPath() == null || !severityComboBox.equals(e.getSource()) 
                || ignoreControlChanges)
            return;
        
        Object o = getUserObject(errorTree.getSelectionPath());
        
        if ( o instanceof HintMetadata ) {
            HintMetadata hint = (HintMetadata) o;
            
            if(writableSettings.getSeverity(hint) != null)
                writableSettings.setSeverity(hint, index2severity(severityComboBox.getSelectedIndex()));            
        } else if (o instanceof String) {
            if (getCurrentDependencyTracking() != DependencyTracking.DISABLED)
                depScn = index2deptracking(severityComboBox.getSelectedIndex());
        }
    }

   
    // ChangeListener implementation -------------------------------------------
    
    @Override
    public void stateChanged(ChangeEvent e) {
        // System.out.println("Task list box changed ");
    }
   
    // Private methods ---------------------------------------------------------

    private String wrapDescription( String description, HintMetadata hint ) {
        return new StringBuffer( DESCRIPTION_HEADER ).append(description).append(getQueryWarning(hint)).append(DESCRIPTION_FOOTER).toString();        
    }
    
    public static String getQueryWarning(HintMetadata hint) {
        if (hint==null || !hint.options.contains(Options.QUERY)) {
            return "";
        }
        return NbBundle.getMessage(HintsPanelLogic.class, "NO_REFACTORING");
        
    }
    
    private Severity index2severity( int index ) {
        Severity s = index2Severity.get(index);

        if (s == null) {
            throw new IllegalStateException( "Unknown severity");
        }

        return s;
    }
       
    private DependencyTracking index2deptracking( int index ) {
        for( Map.Entry<DependencyTracking,Integer> e : deptracking2index.entrySet()) {
            if ( e.getValue() == index ) {
                return e.getKey();
            }
        }
        throw new IllegalStateException( "Unknown severity");
    }

    private boolean toggle( TreePath treePath ) {

        if( treePath == null )
            return false;
        
        if (! (errorTree.getCellRenderer() instanceof HintsPanel.CheckBoxRenderer)) {
            //no checkboxes, no toggle
            return false;
        }

        Object o = getUserObject(treePath);

        DefaultTreeModel model = errorTreeModel;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();


        if ( o instanceof HintMetadata ) {
            HintMetadata hint = (HintMetadata)o;
            boolean value = isEnabled(hint);
            writableSettings.setEnabled(hint, !value);
            model.nodeChanged(node);
            model.nodeChanged(node.getParent());
        }
        else if ( o instanceof HintCategory ) {
            boolean value = isSelected(node) == State.NOT_SELECTED;
            List<DefaultMutableTreeNode> todo = new LinkedList<>();

            todo.add(node);

            while (!todo.isEmpty()) {
                DefaultMutableTreeNode current = todo.remove(0);
                Object cho = current.getUserObject();
                if ( cho instanceof HintMetadata ) {
                    HintMetadata hint = (HintMetadata)cho;
                    boolean cv = isEnabled(hint);
                    if ( cv != value ) {                    
                        writableSettings.setEnabled(hint, value);
                        model.nodeChanged( current );
                    }
                } else if (o instanceof HintCategory) {
                    for (int i = 0; i < current.getChildCount(); i++) {
                        todo.add((DefaultMutableTreeNode) current.getChildAt(i));
                    }
                }
            }            
            model.nodeChanged(node);
        }
        else if (o instanceof String) {
            DependencyTracking value = getCurrentDependencyTracking();
            depScn = value != DependencyTracking.DISABLED ? DependencyTracking.DISABLED : index2deptracking(severityComboBox.getSelectedIndex());
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
            severityComboBox.setSelectedIndex(severity2index.get(Severity.VERIFIER));
            tasklistCheckBox.setSelected(false);
            descriptionTextArea.setText(""); // NOI18N
        }
        
        severityComboBox.setEnabled(enabled);
        tasklistCheckBox.setEnabled(enabled);
        descriptionTextArea.setEnabled(enabled);
    }

    @Override
    public void itemStateChanged(ItemEvent ie) {
        Object o = configCombo.getSelectedItem();
        if (o instanceof Configuration) {
            setOverlayPreferences(((Configuration) o).getSettings(), direct);
        }
    }

    public boolean isEnabled(HintMetadata hint) {
        return writableSettings.isEnabled(hint);
    }
    
    public static final class HintCategory {
        public final String codeName;
        public final String displayName;
        public final List<HintCategory> subCategories = new ArrayList<>();
        public final List<HintMetadata> hints = new ArrayList<>();

        public HintCategory(String codeName) {
            this.codeName = codeName;
            this.displayName = Utilities.categoryDisplayName(codeName);
        }

    }

    // Inner classes -----------------------------------------------------------
           
    private static class ModifiedPreferences extends AbstractPreferences {
        private static final String MODIFIED_HINT_SETTINGS_MARKER = "MODIFIED_HINT_SETTINGS";
        
        private Map<String,Object> map = new HashMap<String, Object>();
        private final Map<String,String> mapSaved = new HashMap<>();

        public ModifiedPreferences( Preferences node ) {
            super(FAKE_ROOT, MODIFIED_HINT_SETTINGS_MARKER); // NOI18N
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
        
        public String getSavedValue(String key) {
            return mapSaved.get(key);
        }
        
        @Override
        protected void putSpi(String key, String value) {
            map.put(key, value);            
            if(!mapSaved.containsKey(key)) {
                // The saved value for key is equal to the default value, which is set by the Customizer's constructor
                mapSaved.put(key, value);
            }
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

	boolean isEmpty() {
	    return map.isEmpty();
	}
    }
    
    private static final AbstractPreferences FAKE_ROOT = new AbstractPreferences(null, "") {
        @Override protected void putSpi(String key, String value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override protected String getSpi(String key) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override protected void removeSpi(String key) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override protected void removeNodeSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override protected String[] keysSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override protected String[] childrenNamesSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override protected AbstractPreferences childSpi(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override protected void syncSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        @Override protected void flushSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };
    
    static final class WritableSettings extends HintsSettings {
        private final HintsSettings delegate;
        private final boolean direct;
        private Map<HintMetadata,ModifiedHint> changes = new HashMap<HintMetadata, ModifiedHint>();

        public WritableSettings(HintsSettings delegate, boolean direct) {
            this.delegate = delegate;
            this.direct = direct;
        }

        @Override
        public boolean isEnabled(HintMetadata hint) {
            ModifiedHint modified = changes.get(hint);
            Boolean enabled = modified != null ? modified.enabledOverride : null;
            
            if (enabled != null) return enabled;
            else return delegate.isEnabled(hint);
        }

        private ModifiedHint forWriting(HintMetadata hint) {
            ModifiedHint result = changes.get(hint);
            
            if (result == null) {
                changes.put(hint, result = new ModifiedHint());
            }
            
            return result;
        }
        
        @Override
        public void setEnabled(HintMetadata hint, boolean value) {
            if (direct) delegate.setEnabled(hint, value);
            else forWriting(hint).enabledOverride = value;
        }

        @Override
        public Preferences getHintPreferences(HintMetadata hint) {
            if (direct) return delegate.getHintPreferences(hint);
            
            Preferences prefs = forWriting(hint).preferencesOverride;
            
            if (prefs == null) {
                //will always create the modified preferences
                prefs = forWriting(hint).preferencesOverride = new ModifiedPreferences(delegate.getHintPreferences(hint));
            }
            
            return prefs;
        }

        @Override
        public Severity getSeverity(HintMetadata hint) {
            ModifiedHint modified = changes.get(hint);
            Severity severity = modified != null ? modified.severityOverride : null;
            
            if (severity != null) return severity;
            else return delegate.getSeverity(hint);
        }

        @Override
        public void setSeverity(HintMetadata hint, Severity severity) {
            if (direct) delegate.setSeverity(hint, severity);
            else forWriting(hint).severityOverride = severity;
        }
        
        public boolean isModified() {
            boolean isChanged = false;
            for (Entry<HintMetadata, ModifiedHint> entry : changes.entrySet()) {
                HintMetadata hint = entry.getKey();
                ModifiedHint e = entry.getValue();
                if (e.enabledOverride != null) {
                    Boolean currentEnabled = e.enabledOverride;
                    Boolean savedEnabled = delegate.isEnabled(hint);
                    isChanged |= currentEnabled != savedEnabled;
                    if(isChanged) {
                        return true;
                    }
                }
                if (e.severityOverride != null) {
                    Severity currentSeverity = e.severityOverride;
                    Severity savedSeverity = delegate.getSeverity(hint);
                    isChanged |= currentSeverity.compareTo(savedSeverity) != 0;
                    if(isChanged) {
                        return true;
                    }
                }
                if (e.preferencesOverride != null && !e.preferencesOverride.isEmpty()) {
                    try {
                        for (String key : e.preferencesOverride.keys()) {
                            String current = e.preferencesOverride.get(key, null);
                            String saved = delegate.getHintPreferences(hint).get(key, null);
                            if(saved == null) {
                                // The saved value for key is equal to the default value, which should be set by the Customizer's constructor
                                saved = e.preferencesOverride.getSavedValue(key);
                            }
                            isChanged |= current == null ? saved != null : !current.equals(saved);
                            if (isChanged) {
                                return true;
                            }
                        }
                    } catch (BackingStoreException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            return false;
        }
        
        public void commit() {
            for (Entry<HintMetadata, ModifiedHint> e : changes.entrySet()) {
                if (e.getValue().preferencesOverride != null)
                    e.getValue().preferencesOverride.store(delegate.getHintPreferences(e.getKey()));
                if (e.getValue().enabledOverride != null)
                    delegate.setEnabled(e.getKey(), e.getValue().enabledOverride);
                if (e.getValue().severityOverride != null)
                    delegate.setSeverity(e.getKey(), e.getValue().severityOverride);
            }
            changes.clear();
        }

        private static final class ModifiedHint {
            private Boolean enabledOverride;
            private Severity severityOverride;
            private ModifiedPreferences preferencesOverride;
        }
    }

}
