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

package org.netbeans.modules.spellchecker.options;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.spellchecker.ComponentPeer;
import org.netbeans.modules.spellchecker.DefaultLocaleQueryImplementation;
import org.netbeans.modules.spellchecker.DictionaryProviderImpl;
import org.netbeans.modules.spellchecker.options.DictionaryInstallerPanel.DictionaryDescription;
import org.netbeans.modules.spellchecker.spi.dictionary.DictionaryProvider;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Lahoda
 */
@OptionsPanelController.Keywords(keywords = {"#TITLE_InstallDictionary", "#KW_SpellcheckerOptions",
					    "#TITLE_OptionsPanel", "#LBL_Default_Locale_Panel"},
	location = OptionsDisplayer.EDITOR, tabTitle="#TITLE_OptionsPanel")
public class SpellcheckerOptionsPanel extends javax.swing.JPanel {
    
    private List<Locale> removedDictionaries = new ArrayList<Locale>();
    private List<DictionaryDescription> addedDictionaries = new ArrayList<DictionaryDescription>();

    private SpellcheckerOptionsPanelController c;
    private static final Icon errorIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/spellchecker/resources/error.gif"));
    
    /**
     * Creates new form SpellcheckerOptionsPanel
     */
    public SpellcheckerOptionsPanel(final SpellcheckerOptionsPanelController c) {
        initComponents();
        this.c = c;
        Color errorColor = UIManager.getColor("nb.errorForeground");
        
        if (errorColor == null) {
            errorColor = new Color(255, 0, 0);
        }
        
        errorText.setForeground(errorColor);
        
        JTextComponent editorComponent = (JTextComponent) defaultLocale.getEditor().getEditorComponent();
        final Document document = editorComponent.getDocument();
        
        document.addDocumentListener(new DocumentListener() {
            private void validate() {
                try {
                    String locale = document.getText(0, document.getLength());

                    setError(getErrorsForLocale(locale));
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            public void insertUpdate(DocumentEvent e) {
                validate();
            }
            public void removeUpdate(DocumentEvent e) {
                validate();
            }
            public void changedUpdate(DocumentEvent e) {}
        });
        List<String> cathegories = loadCategories ();
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String category : cathegories)
            model.addElement (category);
        lUseIn.setModel (model);
        lUseIn.setCellRenderer (new CheckBoxRenderrer ());
        lUseIn.addKeyListener (new KeyAdapter () {

            @Override
            public void keyTyped (KeyEvent e) {
                if (e.getKeyChar () == KeyEvent.VK_SPACE) {
                    int i = lUseIn.getSelectedIndex ();
                    if (i < 0) return;
                    String name = (String) lUseIn.getModel ().getElementAt (i);
                    if (name.charAt (0) == '+')
                        ((DefaultListModel) lUseIn.getModel ()).set (i, "-" + name.substring (1));
                    else
                        ((DefaultListModel) lUseIn.getModel ()).set (i, "+" + name.substring (1));
                    fireChanged();
                }
            }
        });
        lUseIn.addMouseListener (new MouseAdapter () {

            @Override
            public void mouseClicked (MouseEvent e) {
                int i = lUseIn.getSelectedIndex ();
                if (i < 0) return;
                String name = (String) lUseIn.getModel ().getElementAt (i);
                if (name.charAt (0) == '+')
                    ((DefaultListModel) lUseIn.getModel ()).set (i, "-" + name.substring (1));
                else
                    ((DefaultListModel) lUseIn.getModel ()).set (i, "+" + name.substring (1));
                fireChanged();
            }
        });
        
        installedLocalesList.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                enableDisableButtons();
            }
        });
        enableDisableButtons();
    }
    
    private void fireChanged() {
        List<String> savedCategories = loadCategories();
        ListModel model = lUseIn.getModel();
        List<String> currentCategories = new ArrayList<String>(model.getSize());
        for (int i = 0; i < model.getSize(); i++) {
            currentCategories.add((String) model.getElementAt(i));
        }
        boolean isChanged = !savedCategories.equals(currentCategories);
        
        List<Locale> savedLocales = new ArrayList<Locale>(Arrays.asList(DictionaryProviderImpl.getInstalledDictionariesLocales()));
        model = installedLocalesList.getModel();
        List<Locale> currentLocales = new ArrayList<Locale>(model.getSize());
        for (int i = 0; i < model.getSize(); i++) {
            currentLocales.add((Locale) model.getElementAt(i));
        }
        isChanged |= !savedLocales.equals(currentLocales);
        
        Object selectedItem = defaultLocale.getSelectedItem();
        Locale selectedLocale = null;
        if (selectedItem instanceof Locale) {
            selectedLocale = (Locale) selectedItem;
        }
        if (selectedItem instanceof String) {
            String[] parsedComponents = ((String) selectedItem).split("_");
            String[] components = new String[] {"", "", ""};
            
            System.arraycopy(parsedComponents, 0, components, 0, parsedComponents.length);
            
            selectedLocale = new Locale(components[0], components[1], components[2]);
        }
        if (selectedLocale != null) {
            isChanged |= !DefaultLocaleQueryImplementation.getDefaultLocale().equals(selectedLocale);
        }
        c.notifyChanged(isChanged);
    }

    private void setError(String error) {
        c.setValid(error == null);
        errorText.setText(error != null ? NbBundle.getMessage(SpellcheckerOptionsPanel.class, error) : "");
        errorText.setIcon(error != null ? errorIcon : null);
    }
    
    public void update() {
        updateUsedIn();
        removedDictionaries.clear();
        addedDictionaries.clear();

        updateLocales();
        
        defaultLocale.setSelectedItem(DefaultLocaleQueryImplementation.getDefaultLocale());
    }
    
    private void updateUsedIn() {
        List<String> categories = loadCategories();
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String category : categories) {
            model.addElement(category);
        }
        lUseIn.setModel(model);
    }

    private void updateLocales() {
        DefaultListModel<Locale> model = new DefaultListModel<>();
        List<Locale> locales = new ArrayList<>(Arrays.asList(DictionaryProviderImpl.getInstalledDictionariesLocales()));

        for (DictionaryDescription desc : addedDictionaries) {
            locales.add(desc.getLocale());
        }
        
        locales.removeAll(removedDictionaries);

        for (Locale l : locales) {
            model.addElement(l);
        }
        
        installedLocalesList.setModel(model);
    }
    
    public void commit() {
        //Add dictionaries:
        for (DictionaryDescription desc : addedDictionaries) {
            DictionaryInstallerPanel.doInstall(desc);
        }

        //Remove dictionaries:
        for (Locale remove : removedDictionaries) {
            DictionaryInstallerPanel.removeDictionary(remove);
        }
        
        Object selectedItem = defaultLocale.getSelectedItem();
        Locale selectedLocale = null;
        
        if (selectedItem instanceof Locale) {
            selectedLocale = (Locale) selectedItem;
        }
        
        if (selectedItem instanceof String) {
            String[] parsedComponents = ((String) selectedItem).split("_");
            String[] components = new String[] {"", "", ""};
            
            System.arraycopy(parsedComponents, 0, components, 0, parsedComponents.length);
            
            selectedLocale = new Locale(components[0], components[1], components[2]);
        }
        
        if (selectedLocale != null) {
            DefaultLocaleQueryImplementation.setDefaultLocale(selectedLocale);
        }

        for (DictionaryProvider p : Lookup.getDefault().lookupAll(DictionaryProvider.class)) {
            if (p instanceof DictionaryProviderImpl) {
                ((DictionaryProviderImpl) p).clearDictionaries();
            }
        }

        // save categories:
        FileObject root = FileUtil.getConfigFile ("Spellcheckers");
        if (root != null) {
            Set<String> hidden = new HashSet<String> ();
            ListModel model = lUseIn.getModel ();
            for (int i = 0; i < model.getSize (); i++) {
                String n = (String) model.getElementAt (i);
                if (n.charAt (0) == '-')
                    hidden.add (n.substring (1));
            }
            FileObject[] children = root.getChildren ();
            for (FileObject fileObject : children) {
                String name = null;
                try {
                    name = fileObject.getFileSystem ().getDecorator ().annotateName (fileObject.getName (), Collections.singleton (fileObject));
                } catch (FileStateInvalidException ex) {
                    name = fileObject.getName ();
                }
                try {
                    fileObject.setAttribute ("Hidden", Boolean.valueOf (hidden.contains (name)));
                } catch (IOException ex) {
                }
            }
        }
        for (JTextComponent component : EditorRegistry.componentList ()) {
            ComponentPeer componentPeer = (ComponentPeer) component.getClientProperty (ComponentPeer.class);
            if (componentPeer != null)
                componentPeer.reschedule ();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel4 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jScrollPane2 = new javax.swing.JScrollPane();
        lUseIn = new javax.swing.JList();
        jScrollPane1 = new javax.swing.JScrollPane();
        installedLocalesList = new javax.swing.JList();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        errorText = new javax.swing.JLabel();
        defaultLocale = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SpellcheckerOptionsPanel.class, "LBL_Use_in")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(SpellcheckerOptionsPanel.class, "SpellcheckerOptionsPanel.dictionariesListPanel.border.title")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(SpellcheckerOptionsPanel.class, "LBL_Default_Locale_Panel")); // NOI18N

        lUseIn.setVisibleRowCount(5);
        jScrollPane2.setViewportView(lUseIn);
        lUseIn.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SpellcheckerOptionsPanel.class, "lUseIn_ACSN")); // NOI18N
        lUseIn.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SpellcheckerOptionsPanel.class, "lUseIn_ACSD")); // NOI18N

        installedLocalesList.setModel(getInstalledDictionariesModel());
        installedLocalesList.setVisibleRowCount(4);
        jScrollPane1.setViewportView(installedLocalesList);
        installedLocalesList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SpellcheckerOptionsPanel.class, "installedLocalesList_ACSN")); // NOI18N
        installedLocalesList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SpellcheckerOptionsPanel.class, "installedLocalesList_ACSD")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(SpellcheckerOptionsPanel.class, "SpellcheckerOptionsPanel.addButton.text")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(SpellcheckerOptionsPanel.class, "SpellcheckerOptionsPanel.removeButton.text")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        defaultLocale.setEditable(true);
        defaultLocale.setModel(getLocaleModel());
        defaultLocale.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                defaultLocaleItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SpellcheckerOptionsPanel.class, "LBL_Default_Locale", new Object[] {})); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(142, 142, 142)
                .addComponent(errorText)
                .addContainerGap(285, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(defaultLocale, 0, 297, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2))
            .addComponent(jScrollPane2)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(addButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(removeButton)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 72, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4)
                    .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(defaultLocale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(errorText)
                .addContainerGap())
        );

        addButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SpellcheckerOptionsPanel.class, "SpellcheckerOptionsPanel.addButton.ACSN")); // NOI18N
        addButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SpellcheckerOptionsPanel.class, "SpellcheckerOptionsPanel.addButton.ACSD")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SpellcheckerOptionsPanel.class, "SpellcheckerOptionsPanel.removeButton.ACSN")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SpellcheckerOptionsPanel.class, "SpellcheckerOptionsPanel.removeButton.ACSD")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        for (Object o : installedLocalesList.getSelectedValues()) {
            removedDictionaries.add((Locale) o);
        }
        updateLocales();
        fireChanged();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        Collection<String> currentLocales = new HashSet<String>();
        ListModel locales = installedLocalesList.getModel();

        for (int c = 0; c < locales.getSize(); c++) {
            currentLocales.add(locales.getElementAt(c).toString());
        }

        JButton okButton = new JButton(NbBundle.getMessage(SpellcheckerOptionsPanel.class, "BTN_Add"));
        DictionaryInstallerPanel panel = new DictionaryInstallerPanel(okButton, currentLocales);
        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(SpellcheckerOptionsPanel.class, "LBL_AddDictionary"), true, new Object[] {okButton, DialogDescriptor.CANCEL_OPTION}, okButton, DialogDescriptor.DEFAULT_ALIGN, null, null);

        dd.setClosingOptions(null);
        panel.setNotifications(dd.createNotificationLineSupport());
        
        Dialog d = DialogDisplayer.getDefault().createDialog(dd);

        d.setVisible(true);

        if (dd.getValue() == okButton) {
            DictionaryDescription desc = panel.createDescription();

            addedDictionaries.add(desc);
            removedDictionaries.remove(desc.getLocale());
            updateLocales();
        }
        fireChanged();
    }//GEN-LAST:event_addButtonActionPerformed

    private void defaultLocaleItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_defaultLocaleItemStateChanged
        fireChanged();
    }//GEN-LAST:event_defaultLocaleItemStateChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JComboBox defaultLocale;
    private javax.swing.JLabel errorText;
    private javax.swing.JList installedLocalesList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JList lUseIn;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
    
    private ListModel getInstalledDictionariesModel() {
        DefaultListModel<Locale> dlm = new DefaultListModel<>();

        for (Locale l : DictionaryProviderImpl.getInstalledDictionariesLocales()) {
            dlm.addElement(l);
        }

        return dlm;
    }

    private ComboBoxModel getLocaleModel() {
        DefaultComboBoxModel<Locale> dlm = new DefaultComboBoxModel<>();
        List<Locale> locales = new ArrayList<>(Arrays.asList(Locale.getAvailableLocales()));
        
        locales.sort(new LocaleComparator());
        
        for (Locale l : locales) {
            dlm.addElement(l);
        }
        
        return dlm;
    }

    private static List<String> loadCategories () {
        //Repository.getDefault ().findResource ("Spellcheckers");
        List<String> result = new ArrayList<String> ();
        FileObject root = FileUtil.getConfigFile ("Spellcheckers");
        if (root != null) {
            FileObject[] children = root.getChildren ();
            for (FileObject fileObject : children) {
                String name = null;
                try {
                    name = fileObject.getFileSystem ().getDecorator ().annotateName (fileObject.getName (), Collections.singleton (fileObject));
                } catch (FileStateInvalidException ex) {
                    name = fileObject.getName ();
                }
                Boolean b = (Boolean) fileObject.getAttribute ("Hidden");
                if (b != null && b) {
                    result.add ("-" + name); // hidden
                } else {
                    result.add ("+" + name);
                }
            }
        }
        result.sort(CategoryComparator);
        return result;
    }

    public static String getErrorsForLocale(String locale) {
        if (locale.length() == 0) {
            return "ERR_LocaleIsEmpty";
        }

        String[] components = locale.split("_");

        if (components.length > 3) {
            return "ERR_InvalidLocale";
        }

        if (!Arrays.asList(Locale.getISOLanguages()).contains(components[0])) {
            return "ERR_UnknownLanguage";
        }

        if (components.length > 1) {
            if (!Arrays.asList(Locale.getISOCountries()).contains(components[1])) {
                return "ERR_UnknownCountry";
            }

            if (!Arrays.asList(Locale.getAvailableLocales()).contains(new Locale(components[0], components[1]))) {
                return "ERR_UnsupportedLocale";
            }
        }

        return null;
    }

    private void enableDisableButtons() {
        removeButton.setEnabled(installedLocalesList.getSelectedIndex() != (-1));
    }
    
    private static final Comparator<String> CategoryComparator = new Comparator<String> () {

        public int compare (String o1, String o2) {
            return o1.substring (1).compareTo (o2.substring (1));
        }
    };

    private static class LocaleComparator implements Comparator<Locale> {
        
        public int compare(Locale o1, Locale o2) {
            return o1.toString().compareTo(o2.toString());
        }
        
    }
}
