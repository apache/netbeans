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
package org.netbeans.modules.properties.formatter;

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.netbeans.modules.options.editor.spi.PreviewProvider;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author phrebejk
 *
 */
public class FmtOptions {

    private static final Logger LOGGER = Logger.getLogger(FmtOptions.class.getName());

    public static final String expandTabToSpaces = SimpleValueNames.EXPAND_TABS;
    public static final String tabSize = SimpleValueNames.TAB_SIZE;
    public static final String spacesPerTab = SimpleValueNames.SPACES_PER_TAB;
    public static final String indentSize = SimpleValueNames.INDENT_SHIFT_WIDTH;
    public static final String rightMargin = SimpleValueNames.TEXT_LIMIT_WIDTH;
    public static final String useProjectEncoding = "useProjectEncoding"; //NOI18N


    private FmtOptions() {}

    public static int getDefaultAsInt(String key) {
        return Integer.parseInt(defaults.get(key));
    }

    public static boolean getDefaultAsBoolean(String key) {
        return Boolean.parseBoolean(defaults.get(key));
    }

    public static String getDefaultAsString(String key) {
        return defaults.get(key);
    }

    // Private section ---------------------------------------------------------

    private static final String TRUE = "true";      // NOI18N
    private static final String FALSE = "false";    // NOI18N


    private static Map<String,String> defaults;

    static {
        createDefaults();
    }

    private static void createDefaults() {
        String defaultValues[][] = {
            { expandTabToSpaces, TRUE}, //NOI18N
            { tabSize, "8"}, //NOI18N
            { indentSize, "4"}, //NOI18N
            { useProjectEncoding, "true"}, //NOI18N
            { rightMargin, "80"}, //NOI18N
        };

        defaults = new HashMap<>();

        for (java.lang.String[] strings : defaultValues) {
            defaults.put(strings[0], strings[1]);
        }

    }

     // Support section ---------------------------------------------------------

    public static class CategorySupport implements ActionListener, DocumentListener, PreviewProvider, PreferencesCustomizer {

        public static final String OPTION_ID = "org.netbeans.modules.properties.formatter.FormatingOptions.ID";

        private static final int LOAD = 0;
        private static final int STORE = 1;
        private static final int ADD_LISTENERS = 2;

        private final String previewText;
        private final String id;
        protected final JPanel panel;
        private final List<JComponent> components = new LinkedList<>();
        private JEditorPane previewPane;

        private final Preferences preferences;
        private final Preferences previewPrefs;

        protected CategorySupport(Preferences preferences, String id,
                JPanel panel, String previewText, String[]... forcedOptions) {
            this.preferences = preferences;
            this.id = id;
            this.panel = panel;
            this.previewText = previewText != null ? previewText : NbBundle.getMessage(FmtOptions.class, "SAMPLE_Default"); //NOI18N

            // Scan the panel for its components
            scan(panel, components);

            // Initialize the preview preferences
            Preferences forcedPrefs = new PreviewPreferences();
            for (String[] option : forcedOptions) {
                forcedPrefs.put( option[0], option[1]);
            }
            this.previewPrefs = new ProxyPreferences(preferences, forcedPrefs);

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

        public void notifyChanged() {
//            if (loaded)
//                return;
            storeTo(preferences);
            refreshPreview();
        }

        // ActionListener implementation ---------------------------------------

        @Override
        public void actionPerformed(ActionEvent e) {
            notifyChanged();
        }

        // DocumentListener implementation -------------------------------------

        @Override
        public void insertUpdate(DocumentEvent e) {
            notifyChanged();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            notifyChanged();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            notifyChanged();
        }

        // PreviewProvider methods -----------------------------------------------------

        @Override
        public JComponent getPreviewComponent() {
            if (previewPane == null) {
                previewPane = new JEditorPane();
                previewPane.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtOptions.class, "AN_Preview")); //NOI18N
                previewPane.setEditorKit(CloneableEditorSupport.getEditorKit("text/x-properties"));
                previewPane.setEditable(false);
            }
            return previewPane;
        }

        @Override
        public void refreshPreview() {
            JEditorPane pane = (JEditorPane) getPreviewComponent();
            try {
                int rm = previewPrefs.getInt(rightMargin, getDefaultAsInt(rightMargin));
                pane.putClientProperty("TextLimitLine", rm); //NOI18N
            }
            catch( NumberFormatException e ) {
                // Ignore it
            }

            Rectangle visibleRectangle = pane.getVisibleRect();
            pane.setText(previewText);
            pane.setIgnoreRepaint(true);

            final Document doc = pane.getDocument();
            final AtomicLockDocument ald = LineDocumentUtils.as(doc, AtomicLockDocument.class);
            if (ald != null) {
                final Reformat reformat = Reformat.get(doc);
                reformat.lock();
                try {
                    ald.runAtomic(() -> {
                        try {
                            reformat.reformat(0, doc.getLength());
                        } catch (BadLocationException ble) {
                            LOGGER.log(Level.WARNING, null, ble);
                        }
                    });
                } finally {
                    reformat.unlock();
                }
            } else {
                LOGGER.warning(String.format("Can't format %s; it's not BaseDocument.", doc)); //NOI18N
            }
            pane.setIgnoreRepaint(false);
            pane.scrollRectToVisible(visibleRectangle);
            pane.repaint(100);

        }

        // PreferencesCustomizer implementation --------------------------------

        @Override
        public JComponent getComponent() {
            return panel;
        }

        @Override
        public String getDisplayName() {
            return panel.getName();
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }

        // PreferencesCustomizer.Factory implementation ------------------------

        public static final class Factory implements PreferencesCustomizer.Factory {

            private final String id;
            private final Class<? extends JPanel> panelClass;
            private final String previewText;
            private final String[][] forcedOptions;

            public Factory(String id, Class<? extends JPanel> panelClass,
                    String previewText, String[]... forcedOptions) {
                this.id = id;
                this.panelClass = panelClass;
                this.previewText = previewText;
                this.forcedOptions = forcedOptions;
            }

            @Override
            public PreferencesCustomizer create(Preferences preferences) {
                try {
                    return new CategorySupport(preferences, id, panelClass.getDeclaredConstructor().newInstance(), previewText, forcedOptions);
                } catch (RuntimeException | ReflectiveOperationException e) {
                    LOGGER.log(Level.WARNING, "Exception during creating formatter customiezer", e);
                    return null;
                }
            }
        } // End of CategorySupport.Factory class

        // Private methods -----------------------------------------------------

        private void performOperation(int operation, JComponent jc, String optionID, Preferences p) {
            switch (operation) {
                case LOAD:
                    loadData(jc, optionID, p);
                    break;
                case STORE:
                    storeData(jc, optionID, p);
                    break;
                case ADD_LISTENERS:
                    addListener(jc);
                    break;
                default:
                    LOGGER.log(Level.WARNING, "Unknown operation value {0}", operation);
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
            else if ( jc instanceof JCheckBox ) {
                JCheckBox checkBox = (JCheckBox)jc;
                boolean df = getDefaultAsBoolean(optionID);
                checkBox.setSelected( node.getBoolean(optionID, df));
            }

        }

        private void storeData( JComponent jc, String optionID, Preferences node ) {

            if ( jc instanceof JTextField ) {
                JTextField field = (JTextField)jc;

                String text = field.getText();

                // XXX test for numbers
                if ( isInteger(optionID) ) {
                    try {
                        Integer.parseInt(text);
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
            else if ( jc instanceof JCheckBox ) {
                JCheckBox checkBox = (JCheckBox)jc;
                if (!optionID.equals(expandTabToSpaces) && getDefaultAsBoolean(optionID) == checkBox.isSelected())
                    node.remove(optionID);
                else
                    node.putBoolean(optionID, checkBox.isSelected());
            }
            else if ( jc instanceof JComboBox) {
                JComboBox cb  = (JComboBox)jc;
                ComboItem comboItem = ((ComboItem) cb.getSelectedItem());
                String value = comboItem == null ? getDefaultAsString(optionID) : comboItem.value;

                if (getDefaultAsString(optionID).equals(value))
                    node.remove(optionID);
                else
                    node.put(optionID,value);
            }
        }

        private void addListener( JComponent jc ) {
            if ( jc instanceof JTextField ) {
                JTextField field = (JTextField)jc;
                field.addActionListener(this);
                field.getDocument().addDocumentListener(this);
            }
            else if ( jc instanceof JCheckBox ) {
                JCheckBox checkBox = (JCheckBox)jc;
                checkBox.addActionListener(this);
            }
            else if ( jc instanceof JComboBox) {
                JComboBox cb  = (JComboBox)jc;
                cb.addActionListener(this);
            }
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
                this.displayName = NbBundle.getMessage(FmtOptions.class, key);
            }

            @Override
            public String toString() {
                return displayName;
            }

        }
    }

    public static class PreviewPreferences extends AbstractPreferences {

        private final Map<String,Object> map = new HashMap<>();

        public PreviewPreferences() {
            super(null, ""); // NOI18N
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

    // read-only, no subnodes
    public static final class ProxyPreferences extends AbstractPreferences {

        private final Preferences[] delegates;

        public ProxyPreferences(Preferences... delegates) {
            super(null, ""); // NOI18N
            this.delegates = delegates;
        }

        @Override
        protected void putSpi(String key, String value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected String getSpi(String key) {
            for(Preferences p : delegates) {
                String value = p.get(key, null);
                if (value != null) {
                    return value;
                }
            }
            return null;
        }

        @Override
        protected void removeSpi(String key) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void removeNodeSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected String[] keysSpi() throws BackingStoreException {
            Set<String> keys = new HashSet<>();
            for(Preferences p : delegates) {
                keys.addAll(Arrays.asList(p.keys()));
            }
            return keys.toArray(new String[0]);
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
    } // End of ProxyPreferences class

    public static boolean isInteger(String optionID) {
        String value = defaults.get(optionID);

        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException numberFormatException) {
            return false;
        }
    }
}
