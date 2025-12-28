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

package org.netbeans.modules.java.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CodeStyle.BracePlacement;
import org.netbeans.api.java.source.CodeStyle.BracesGenerationStyle;
import org.netbeans.api.java.source.CodeStyle.InsertionPoint;
import org.netbeans.api.java.source.CodeStyle.WrapStyle;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.java.source.save.Reformatter;
import static org.netbeans.modules.java.ui.FmtOptions.*;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.netbeans.modules.options.editor.spi.PreviewProvider;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Dusan Balek
 */
public class CategorySupport implements ActionListener, ChangeListener, ListDataListener, TableModelListener, DocumentListener, PreviewProvider, PreferencesCustomizer {

    public static final String OPTION_ID = "org.netbeans.modules.java.ui.FormatingOptions.ID";

    private static final int LOAD = 0;
    private static final int STORE = 1;
    private static final int ADD_LISTENERS = 2;

    private static final ComboItem  bracePlacement[] = new ComboItem[] {
            new ComboItem( BracePlacement.SAME_LINE.name(), "LBL_bp_SAME_LINE" ), // NOI18N
            new ComboItem( BracePlacement.NEW_LINE.name(), "LBL_bp_NEW_LINE" ), // NOI18N
            new ComboItem( BracePlacement.NEW_LINE_HALF_INDENTED.name(), "LBL_bp_NEW_LINE_HALF_INDENTED" ), // NOI18N
            new ComboItem( BracePlacement.NEW_LINE_INDENTED.name(), "LBL_bp_NEW_LINE_INDENTED" ) // NOI18N
        };
    private static final ComboItem  bracesGeneration[] = new ComboItem[] {
            new ComboItem( BracesGenerationStyle.GENERATE.name(), "LBL_bg_GENERATE" ), // NOI18N
            new ComboItem( BracesGenerationStyle.LEAVE_ALONE.name(), "LBL_bg_LEAVE_ALONE" ), // NOI18N
            new ComboItem( BracesGenerationStyle.ELIMINATE.name(), "LBL_bg_ELIMINATE" ) // NOI18N       
        };

    private static final ComboItem  wrap[] = new ComboItem[] {
            new ComboItem( WrapStyle.WRAP_ALWAYS.name(), "LBL_wrp_WRAP_ALWAYS" ), // NOI18N
            new ComboItem( WrapStyle.WRAP_IF_LONG.name(), "LBL_wrp_WRAP_IF_LONG" ), // NOI18N
            new ComboItem( WrapStyle.WRAP_NEVER.name(), "LBL_wrp_WRAP_NEVER" ) // NOI18N
        };

    private static final ComboItem  insertionPoint[] = new ComboItem[] {
            new ComboItem( InsertionPoint.LAST_IN_CATEGORY.name(), "LBL_ip_LAST_IN_CATEGORY" ), // NOI18N
            new ComboItem( InsertionPoint.FIRST_IN_CATEGORY.name(), "LBL_ip_FIRST_IN_CATEGORY" ), // NOI18N
            new ComboItem( InsertionPoint.ORDERED_IN_CATEGORY.name(), "LBL_ip_ORDERED_IN_CATEGORY" ), // NOI18N
            new ComboItem( InsertionPoint.CARET_LOCATION.name(), "LBL_ip_CARET_LOCATION" ) // NOI18N
        };

    // do not increase throughput; otherwise sources/sourceIndex below must be synchronized somehow.
    private static final RequestProcessor REFORMAT_RP = new RequestProcessor("Java Format Previewer");

    protected final String previewText;
//        private String forcedOptions[][];

//        private boolean changed = false;
//        private boolean loaded = false;
    private final String id;
    protected final JPanel panel;
    private final List<JComponent> components = new LinkedList<JComponent>();                
    private JEditorPane previewPane;

    protected final Preferences preferences;
    protected final Preferences previewPrefs;

    /* package private */ AtomicBoolean pendingRefresh = new AtomicBoolean(false);

    protected CategorySupport(Preferences preferences, String id, JPanel panel, String previewText, String[]... forcedOptions) {
        this.preferences = preferences;
        this.id = id;
        this.panel = panel;
        this.previewText = previewText != null ? previewText : NbBundle.getMessage(CategorySupport.class, "SAMPLE_Default"); //NOI18N

        // Scan the panel for its components
        scan(panel, components);

        // Initialize the preview preferences
        Preferences forcedPrefs = new PreviewPreferences();
        for (String[] option : forcedOptions) {
            forcedPrefs.put( option[0], option[1]);
        }
        this.previewPrefs = new ProxyPreferences(forcedPrefs, preferences);

        // Load and hook up all the components
        loadFrom(preferences);
        addListeners();
    }

    protected void addListeners() {
        scan(ADD_LISTENERS, null);
    }

    protected void loadFrom(Preferences preferences) {
//            loaded = true;
        scan(LOAD, preferences);
//            loaded = false;
    }
//
//        public void applyChanges() {
//            storeTo(preferences);
//        }
//
    protected void storeTo(Preferences p) {
        scan(STORE, p);
    }

    protected void notifyChanged() {
//            if (loaded)
//                return;
        storeTo(preferences);
        // give other listeners a chance to refresh their data, too
        SwingUtilities.invokeLater(new Runnable() { public void run() { 
            refreshPreview(); 
        } });
    }

    protected void loadListData(final JList list, final String optionID, final Preferences p) {
    }

    protected void storeListData(final JList list, final String optionID, final Preferences node) {            
    }

    protected void loadTableData(final JTable table, final String optionID, final Preferences p) {
    }

    protected void storeTableData(final JTable table, final String optionID, final Preferences node) {            
    }

    // ActionListener implementation ---------------------------------------

    public void actionPerformed(ActionEvent e) {
        notifyChanged();
    }

    // ChangeListener implementation ---------------------------------------

    @Override
    public void stateChanged(ChangeEvent e) {
        notifyChanged();
    }

    // ListDataListener implementation -----------------------------------

    @Override
    public void contentsChanged(ListDataEvent e) {
    }

    @Override
    public void intervalAdded(ListDataEvent e) {
        notifyChanged();
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
    }

    // TableModelListener implementation -----------------------------------

    @Override
    public void tableChanged(TableModelEvent e) {
        notifyChanged();
    }

    // DocumentListener implementation -------------------------------------

    public void insertUpdate(DocumentEvent e) {
        notifyChanged();
    }

    public void removeUpdate(DocumentEvent e) {
        notifyChanged();
    }

    public void changedUpdate(DocumentEvent e) {
        notifyChanged();
    }

    // PreviewProvider methods -----------------------------------------------------

    public JComponent getPreviewComponent() {
        if (previewPane == null) {
            previewPane = new JEditorPane();
            previewPane.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CategorySupport.class, "AN_Preview")); //NOI18N
            previewPane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CategorySupport.class, "AD_Preview")); //NOI18N
            previewPane.putClientProperty("HighlightsLayerIncludes", "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.SyntaxHighlighting$"); //NOI18N
            previewPane.setEditorKit(CloneableEditorSupport.getEditorKit("text/x-java"));
            previewPane.setEditable(false);
        }
        return previewPane;
    }

    public void refreshPreview() {
        if (pendingRefresh.getAndSet(true)) {
            return;
        }
        final JEditorPane jep = (JEditorPane) getPreviewComponent();

        jep.setIgnoreRepaint(true);
        REFORMAT_RP.post(new Runnable() {
            private String text;

            public void run() {
                if (SwingUtilities.isEventDispatchThread()) {
                    try {
                        int rm = previewPrefs.getInt(rightMargin, getDefaultAsInt(rightMargin));
                        jep.putClientProperty("TextLimitLine", rm); //NOI18N
                        jep.getDocument().putProperty(SimpleValueNames.TEXT_LINE_WRAP, ""); //NOI18N
                        jep.getDocument().putProperty(SimpleValueNames.TAB_SIZE, ""); //NOI18N
                        jep.getDocument().putProperty(SimpleValueNames.TEXT_LIMIT_WIDTH, ""); //NOI18N
                    }
                    catch( NumberFormatException e ) {
                        // Ignore it
                    }
                    jep.setIgnoreRepaint(true);
                    jep.setText(text);
                    jep.setIgnoreRepaint(false);
                    jep.scrollRectToVisible(new Rectangle(0,0,10,10) );
                    jep.repaint(100);
                    return;
                }
                try {
                    Class.forName(CodeStyle.class.getName(), true, CodeStyle.class.getClassLoader());
                } catch (ClassNotFoundException cnfe) {
                    // ignore
                }
                pendingRefresh.getAndSet(false);
                CodeStyle codeStyle = codeStyleProducer.create(previewPrefs);
                text = Reformatter.reformat(previewText, codeStyle);
                SwingUtilities.invokeLater(this);
            }
        }, 100);
    }

    // PreferencesCustomizer implementation --------------------------------

    public JComponent getComponent() {
        return panel;
    }

    public String getDisplayName() {
        return panel.getName();
    }

    public String getId() {
        return id;
    }

    public HelpCtx getHelpCtx() {
        return null;
    }

    // PreferencesCustomizer.Factory implementation ------------------------

    public static final class Factory implements PreferencesCustomizer.Factory {

        private final String id;
        private final Class<? extends JPanel> panelClass;
        private final String previewText;
        private final String[][] forcedOptions;

        public Factory(String id, Class<? extends JPanel> panelClass, String previewText, String[]... forcedOptions) {
            this.id = id;
            this.panelClass = panelClass;
            this.previewText = previewText;
            this.forcedOptions = forcedOptions;
        }

        public PreferencesCustomizer create(Preferences preferences) {
            try {
                CategorySupport categorySupport = new CategorySupport(preferences, id, panelClass.getDeclaredConstructor().newInstance(), previewText, forcedOptions);
                if (categorySupport.panel instanceof Runnable)
                    ((Runnable)categorySupport.panel).run();
                return categorySupport;
            } catch (Exception e) {
                return null;
            }
        }
    } // End of CategorySupport.Factory class

    // Private methods -----------------------------------------------------

    private void performOperation(int operation, JComponent jc, String optionID, Preferences p) {
        switch(operation) {
        case LOAD:
            loadData(jc, optionID, p);
            break;
        case STORE:
            storeData(jc, optionID, p);
            break;
        case ADD_LISTENERS:
            addListener(jc);
            break;
        }
    }

    private void scan(int what, Preferences p ) {
        for (JComponent jc : components) {
            Object o = jc.getClientProperty(OPTION_ID);
            if (o instanceof String) {
                performOperation(what, jc, (String)o, p);
            } else if (o instanceof String[]) {
                for(String oid : (String[])o) {
                    performOperation(what, jc, oid, p);
                }
            }
        }
    }

    private void scan(Container container, List<JComponent> components) {
        for (Component c : container.getComponents()) {
            if (c instanceof JComponent) {
                JComponent jc = (JComponent)c;
                Object o = jc.getClientProperty(OPTION_ID);
                if (o instanceof String || o instanceof String[])
                    components.add(jc);
            }                    
            if (c instanceof Container)
                scan((Container)c, components);
        }
    }

    /** Very smart method which tries to set the values in the components correctly
     */ 
    private void loadData( JComponent jc, String optionID, Preferences node ) {

        if ( jc instanceof JTextField ) {
            JTextField field = (JTextField)jc;                
            field.setText( node.get(optionID, getDefaultAsString(optionID)) );
        }
        else if ( jc instanceof JSpinner ) {
            JSpinner js = (JSpinner)jc;
            js.setValue(node.getInt(optionID, getDefaultAsInt(optionID)));
        }
        else if ( jc instanceof JToggleButton ) {
            JToggleButton toggle = (JToggleButton)jc;
            boolean df = getDefaultAsBoolean(optionID);
            toggle.setSelected( node.getBoolean(optionID, df));                
        } 
        else if ( jc instanceof JComboBox ) {
            JComboBox cb  = (JComboBox)jc;
            String value = node.get(optionID, getDefaultAsString(optionID) );
            ComboBoxModel model = createModel(value);
            cb.setModel(model);
            ComboItem item = whichItem(value, model);
            cb.setSelectedItem(item);
        }
        else if ( jc instanceof JList ) {
            loadListData((JList)jc, optionID, node);
        }
        else if ( jc instanceof JTable ) {
            loadTableData((JTable)jc, optionID, node);
        }
    }

    private void storeData( JComponent jc, String optionID, Preferences node ) {

        if ( jc instanceof JTextField ) {
            JTextField field = (JTextField)jc;

            String text = field.getText();

            // XXX test for numbers
            if ( isInteger(optionID) ) {
                try {
                    int i = Integer.parseInt(text);                        
                } catch (NumberFormatException e) {
                    return;
                }
            }

            // XXX: watch out, tabSize, spacesPerTab, indentSize and expandTabToSpaces
            // fall back on getGlopalXXX() values and not getDefaultAsXXX value,
            // which is why we must not remove them. Proper solution would be to
            // store formatting preferences to MimeLookup and not use NbPreferences.
            // The problem currently is that MimeLookup based Preferences do not support subnodes.
            if (!optionID.equals(tabSize) &&
                !optionID.equals(spacesPerTab) && !optionID.equals(indentSize) &&
                getDefaultAsString(optionID).equals(text)
            ) {
                node.remove(optionID);
            } else {
                node.put(optionID, text);
            }
        }
        else if ( jc instanceof JSpinner ) {
            JSpinner js = (JSpinner)jc;
            Object value = js.getValue();
            if (getDefaultAsInt(optionID) == ((Integer)value).intValue())
                node.remove(optionID);
            else
                node.putInt(optionID, ((Integer)value).intValue());
        }
        else if ( jc instanceof JToggleButton ) {
            JToggleButton toggle = (JToggleButton)jc;
            if (!optionID.equals(expandTabToSpaces) && getDefaultAsBoolean(optionID) == toggle.isSelected())
                node.remove(optionID);
            else
                node.putBoolean(optionID, toggle.isSelected());
        }
        else if ( jc instanceof JComboBox ) {
            JComboBox cb  = (JComboBox)jc;
            // Logger.global.info( cb.getSelectedItem() + " " + optionID);
            String value = ((ComboItem) cb.getSelectedItem()).value;
            if (getDefaultAsString(optionID).equals(value))
                node.remove(optionID);
            else
                node.put(optionID,value);
        }
        else if ( jc instanceof JList ) {
            storeListData((JList)jc, optionID, node);
        }
        else if ( jc instanceof JTable ) {
            storeTableData((JTable)jc, optionID, node);
        }
    }

    private void addListener( JComponent jc ) {
        if ( jc instanceof JTextField ) {
            JTextField field = (JTextField)jc;
            field.addActionListener(this);
            field.getDocument().addDocumentListener(this);
        }
        else if ( jc instanceof JSpinner ) {
            JSpinner spinner = (JSpinner)jc;
            spinner.addChangeListener(this);
        }
        else if ( jc instanceof JToggleButton ) {
            JToggleButton toggle = (JToggleButton)jc;
            toggle.addActionListener(this);
        }
        else if ( jc instanceof JComboBox) {
            JComboBox cb  = (JComboBox)jc;
            cb.addActionListener(this);
        }
        else if ( jc instanceof JList) {
            JList jl = (JList)jc;
            jl.getModel().addListDataListener(this);
        }
        else if ( jc instanceof JTable) {
            JTable jt = (JTable)jc;
            jt.getModel().addTableModelListener(this);
        }
    }


    private ComboBoxModel createModel( String value ) {

        // is it braces placement?            
        for (ComboItem comboItem : bracePlacement) {
            if ( value.equals( comboItem.value) ) {
                return new DefaultComboBoxModel( bracePlacement );
            }
        }

        // is it braces generation?
        for (ComboItem comboItem : bracesGeneration) {
            if ( value.equals( comboItem.value) ) {
                return new DefaultComboBoxModel( bracesGeneration );
            }
        }

        // is it wrap?
        for (ComboItem comboItem : wrap) {
            if ( value.equals( comboItem.value) ) {
                return new DefaultComboBoxModel( wrap );
            }
        }

        // is it insertion point?
        for (ComboItem comboItem : insertionPoint) {
            if ( value.equals( comboItem.value) ) {
                return new DefaultComboBoxModel( insertionPoint );
            }
        }

        return null;
    }

    private static ComboItem whichItem(String value, ComboBoxModel model) {

        for (int i = 0; i < model.getSize(); i++) {
            ComboItem item = (ComboItem)model.getElementAt(i);
            if ( value.equals(item.value)) {
                return item;
            }
        }    
        return null;
    }

    private static class ComboItem {

        String value;
        String displayName;

        public ComboItem(String value, String key) {
            this.value = value;
            this.displayName = NbBundle.getMessage(CategorySupport.class, key);
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    abstract static class DocumentCategorySupport extends CategorySupport {
        /**
         * Two Sources are used. While one Source document is displayed by the preview JEditorPane, 
         * the other source may be formatted in the RP. The editor then switch between those sources.
         */
        private final Source[] sources = new Source[2];
        
        /**
         * Index of the next Source to be used. Not synchronized, incremented only from the RP.
         */
        private int sourceIndex;
        
        public DocumentCategorySupport(Preferences preferences, String id, JPanel panel, String previewText, String[]... forcedOptions) {
            super(preferences, id, panel, previewText, forcedOptions);
        }
        
        private String getSourceName(int index) {
            if (index == 0) {
                return "org.netbeans.samples.ClassA"; // NOI18N
            } else {
                return "org.netbeans.samples" + (index + 1) + ".ClassA"; // NOI18N
            }
        }
        
        private Document reformatDocument(int index) {
            assert REFORMAT_RP.isRequestProcessorThread();
            try {
                Class.forName(CodeStyle.class.getName(), true, CodeStyle.class.getClassLoader());
            } catch (ClassNotFoundException cnfe) {
                // ignore
            }
            final CodeStyle codeStyle = codeStyleProducer.create(previewPrefs);
            final Document doc;
            try {
                Source s;
                if (sources[index] == null) {
                    FileObject fo = FileUtil.createMemoryFileSystem().getRoot().createData(getSourceName(index), "java"); //NOI18N
                    sources[index] = Source.create(fo);
                }
                s = sources[index];
                doc = s.getDocument(true);
                if (doc.getLength() > 0) {
                    doc.remove(0, doc.getLength());
                }
                doc.insertString(0, previewText, null);
                doc.putProperty(CodeStyle.class, codeStyle);
                
                reformatSource(doc, s);
                
                final Reformat reformat = Reformat.get(doc);
                reformat.lock();
                try {
                    if (doc instanceof BaseDocument) {
                        ((BaseDocument) doc).runAtomicAsUser(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    reformat.reformat(0, doc.getLength());
                                } catch (BadLocationException ble) {}
                            }
                        });
                    } else {
                        reformat.reformat(0, doc.getLength());
                    }
                } finally {
                    reformat.unlock();
                }
                DataObject dataObject = DataObject.find(s.getFileObject());
                SaveCookie sc = dataObject.getLookup().lookup(SaveCookie.class);
                if (sc != null)
                    sc.save();
                return doc;
            } catch (Exception ex) {}
            return null;
        }
        
        protected void doModification(ResultIterator iterator) throws Exception {}
        
        protected void reformatSource(Document d, Source s) throws ParseException, IOException {
            ModificationResult result = ModificationResult.runModificationTask(Collections.singleton(s), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    doModification(resultIterator);
                }
                
            });
            result.commit();
        }

        @Override
        public void refreshPreview() {
            if (pendingRefresh.getAndSet(true)) {
                return;
            }
            final JEditorPane jep = (JEditorPane) getPreviewComponent();
            int rm = previewPrefs.getInt(rightMargin, getDefaultAsInt(rightMargin));
            jep.putClientProperty("TextLimitLine", rm); //NOI18N
            jep.getDocument().putProperty(SimpleValueNames.TEXT_LINE_WRAP, ""); //NOI18N
            jep.getDocument().putProperty(SimpleValueNames.TAB_SIZE, ""); //NOI18N
            jep.getDocument().putProperty(SimpleValueNames.TEXT_LIMIT_WIDTH, ""); //NOI18N
            
            REFORMAT_RP.post(new Runnable() {
                private Document doc;
                
                public void run() {
                    if (SwingUtilities.isEventDispatchThread()) {
                        jep.setIgnoreRepaint(true);
                        if (doc != null) {
                            jep.setDocument(doc);
                        }
                        jep.scrollRectToVisible(new Rectangle(0, 0, 10, 10));
                        jep.repaint(100);
                        jep.setIgnoreRepaint(false);
                    } else {
                        pendingRefresh.getAndSet(false);
                        int index = DocumentCategorySupport.this.sourceIndex;
                        doc = reformatDocument(index);
                        sourceIndex = (sourceIndex + 1) % sources.length;
                        SwingUtilities.invokeLater(this);
                    }
                }
            }, 100);
        }
    }      

    private static class PreviewPreferences extends AbstractPreferences {
        
        private Map<String,Object> map = new HashMap<String, Object>();

        public PreviewPreferences() {
            super(null, ""); // NOI18N
        }
        
        protected void putSpi(String key, String value) {
            map.put(key, value);            
        }

        protected String getSpi(String key) {
            return (String)map.get(key);                    
        }

        protected void removeSpi(String key) {
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

    // read-only, no subnodes
    private static final class ProxyPreferences extends AbstractPreferences {
        
        private final Preferences[] delegates;

        public ProxyPreferences(Preferences... delegates) {
            super(null, ""); // NOI18N
            this.delegates = delegates;
        }
        
        protected void putSpi(String key, String value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected String getSpi(String key) {
            for(Preferences p : delegates) {
                String value = p.get(key, null);
                if (value != null) {
                    return value;
                }
            }
            return null;
        }

        protected void removeSpi(String key) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected void removeNodeSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected String[] keysSpi() throws BackingStoreException {
            Set<String> keys = new HashSet<String>();
            for(Preferences p : delegates) {
                keys.addAll(Arrays.asList(p.keys()));
            }
            return keys.toArray(new String[ 0 ]);
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
    } // End of ProxyPreferences class    
}
