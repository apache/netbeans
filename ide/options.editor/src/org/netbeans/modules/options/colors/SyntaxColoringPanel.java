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

package org.netbeans.modules.options.colors;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.editor.settings.storage.api.FontColorSettingsFactory;
import org.netbeans.modules.options.colors.ColorModel.Preview;
import org.netbeans.modules.options.colors.spi.FontsColorsController;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ColorComboBox;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author  Jan Jancura
 */
@OptionsPanelController.Keywords(keywords={"#KW_SyntaxColoringPanel"}, location=OptionsDisplayer.FONTSANDCOLORS, tabTitle= "#Syntax_coloring_tab.displayName")
public class SyntaxColoringPanel extends JPanel implements ActionListener, 
    PropertyChangeListener, FontsColorsController, ItemListener {
    
    
    private Preview             preview;
    private Task                selectTask;
    private ColorModel          colorModel = null;
    private String              currentLanguage;
    private String              currentProfile;
    /** cache Map (String (profile name) > Map (String (language name) > List (AttributeSet))). */
    private Map<String, Map<String, List<AttributeSet>>> profiles = new HashMap<>();
    /** Map (String (profile name) > Set (String (language name))) of names of changed languages. */
    private Map<String, Set<String>> toBeSaved = new HashMap<>();
    private boolean listen = false;
    private boolean changed = false;
    private static Logger log = Logger.getLogger(SyntaxColoringPanel.class.getName ());


    /** Creates new form SyntaxColoringPanel1 */
    public SyntaxColoringPanel () {
        initComponents ();
        setName(loc("Syntax_coloring_tab")); //NOI18N
        // 1) init components
        cbLanguage.getAccessibleContext ().setAccessibleName (loc ("AN_Languages"));
        cbLanguage.getAccessibleContext ().setAccessibleDescription (loc ("AD_Languages"));
        lCategories.getAccessibleContext ().setAccessibleName (loc ("AN_Categories"));
        lCategories.getAccessibleContext ().setAccessibleDescription (loc ("AD_Categories"));
        bFont.getAccessibleContext ().setAccessibleName (loc ("AN_Font"));
        bFont.getAccessibleContext ().setAccessibleDescription (loc ("AD_Font"));
        cbForeground.getAccessibleContext ().setAccessibleName (loc ("AN_Foreground_Chooser"));
        cbForeground.getAccessibleContext ().setAccessibleDescription (loc ("AD_Foreground_Chooser"));
        cbBackground.getAccessibleContext ().setAccessibleName (loc ("AN_Background_Chooser"));
        cbBackground.getAccessibleContext ().setAccessibleDescription (loc ("AD_Background_Chooser"));
        cbEffects.getAccessibleContext ().setAccessibleName (loc ("AN_Efects_Color_Chooser"));
        cbEffects.getAccessibleContext ().setAccessibleDescription (loc ("AD_Efects_Color_Chooser"));
        cbEffectColor.getAccessibleContext ().setAccessibleName (loc ("AN_Efects_Color"));
        cbEffectColor.getAccessibleContext ().setAccessibleDescription (loc ("AD_Efects_Color"));
        cbLanguage.addActionListener (this);
        lCategories.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        lCategories.setVisibleRowCount (3);
        lCategories.setCellRenderer (new CategoryRenderer ());
        lCategories.addListSelectionListener ((ListSelectionEvent e) -> {
            if (!listen) return;
            selectTask.schedule (200);
        });
        lCategories.setSelectedIndex(0);
        tfFont.setEditable (false);
        bFont.addActionListener (this);
        bFont.setMargin (new Insets (0, 0, 0, 0));
        cbForeground.addItemListener(this);
        cbBackground.addItemListener(this);
        
        cbEffects.addItem (loc ("CTL_Effects_None"));
        cbEffects.addItem (loc ("CTL_Effects_Underlined"));
        cbEffects.addItem (loc ("CTL_Effects_Wave_Underlined"));
        cbEffects.addItem (loc ("CTL_Effects_Strike_Through"));
        cbEffects.getAccessibleContext ().setAccessibleName (loc ("AN_Effects"));
        cbEffects.getAccessibleContext ().setAccessibleDescription (loc ("AD_Effects"));
        cbEffects.addActionListener (this);
        cbEffectColor.addItemListener(this);
        
        loc(bFont, "CTL_Font_button");
        loc(lBackground, "CTL_Background_label");
        loc(lCategory, "CTL_Category");
        loc(lEffectColor, "CTL_Effects_color");
        loc(lEffects, "CTL_Effects_label");
        loc(lFont, "CTL_Font");
        loc(lForeground, "CTL_Foreground_label");
        loc(lLanguage, "CTL_Languages");
        loc(lPreview, "CTL_Preview");

        selectTask = new RequestProcessor ("SyntaxColoringPanel1").create (
            new Runnable () {
            @Override
                public void run () {
                    if(EventQueue.isDispatchThread()) {
                        refreshUI ();
                        if (!blink) {
                            return;
                        }
                        startBlinking ();
                    } else {
                        EventQueue.invokeLater(this);
                    }
                }
            }
        );

        spPreview.getVerticalScrollBar().setUnitIncrement(10);
        spPreview.getHorizontalScrollBar().setUnitIncrement(10);
    }
    
    private void updateLanguageCombobox() {
        if (!listen) {
            return;
        }
        synchronized (cbLanguage) {
            setCurrentLanguage((String) cbLanguage.getSelectedItem());
        }
        fireChanged();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lLanguage = new javax.swing.JLabel();
        lCategory = new javax.swing.JLabel();
        spCategories = new javax.swing.JScrollPane();
        lCategories = new javax.swing.JList<>();
        lPreview = new javax.swing.JLabel();
        spPreview = new javax.swing.JScrollPane();
        pPreview = new javax.swing.JPanel();
        lFont = new javax.swing.JLabel();
        lForeground = new javax.swing.JLabel();
        lBackground = new javax.swing.JLabel();
        lEffects = new javax.swing.JLabel();
        lEffectColor = new javax.swing.JLabel();
        cbForeground = new org.openide.awt.ColorComboBox();
        cbBackground = new ColorComboBox();
        cbEffects = new javax.swing.JComboBox<>();
        cbEffectColor = new ColorComboBox();
        tfFont = new javax.swing.JTextField();
        bFont = new javax.swing.JButton();

        lLanguage.setLabelFor(cbLanguage);
        lLanguage.setText("Language:");

        lCategory.setLabelFor(lCategories);
        lCategory.setText("Category:");

        spCategories.setViewportView(lCategories);

        lPreview.setText("Preview:");

        spPreview.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        spPreview.setAutoscrolls(true);

        pPreview.setAutoscrolls(true);
        pPreview.setLayout(new java.awt.BorderLayout());
        spPreview.setViewportView(pPreview);

        lFont.setLabelFor(bFont);
        lFont.setText("Font:");

        lForeground.setLabelFor(cbForeground);
        lForeground.setText("Foreground:");

        lBackground.setLabelFor(cbBackground);
        lBackground.setText("Background:");

        lEffects.setLabelFor(cbEffects);
        lEffects.setText("Effects:");

        lEffectColor.setLabelFor(cbEffectColor);
        lEffectColor.setText("Effect Color:");

        bFont.setText("...");
        bFont.setMargin(new java.awt.Insets(2, 2, 2, 2));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(spPreview, javax.swing.GroupLayout.DEFAULT_SIZE, 425, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lLanguage)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbLanguage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lCategory)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(spCategories, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                        .addGap(20, 20, 20)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lEffectColor)
                            .addComponent(lForeground)
                            .addComponent(lFont)
                            .addComponent(lEffects)
                            .addComponent(lBackground))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(tfFont, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bFont))
                            .addComponent(cbForeground, 0, 97, Short.MAX_VALUE)
                            .addComponent(cbBackground, 0, 97, Short.MAX_VALUE)
                            .addComponent(cbEffects, 0, 97, Short.MAX_VALUE)
                            .addComponent(cbEffectColor, 0, 97, Short.MAX_VALUE)))
                    .addComponent(lPreview))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lLanguage)
                    .addComponent(cbLanguage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lCategory)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(spCategories, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lPreview))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lFont)
                            .addComponent(tfFont, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(bFont))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lForeground)
                            .addComponent(cbForeground, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lBackground)
                            .addComponent(cbBackground, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lEffects)
                            .addComponent(cbEffects, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lEffectColor)
                            .addComponent(cbEffectColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spPreview, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bFont;
    private javax.swing.JComboBox cbBackground;
    private javax.swing.JComboBox cbEffectColor;
    private javax.swing.JComboBox<String> cbEffects;
    private javax.swing.JComboBox cbForeground;
    private final javax.swing.JComboBox<String> cbLanguage = new javax.swing.JComboBox<>();
    private javax.swing.JLabel lBackground;
    private javax.swing.JList<AttributeSet> lCategories;
    private javax.swing.JLabel lCategory;
    private javax.swing.JLabel lEffectColor;
    private javax.swing.JLabel lEffects;
    private javax.swing.JLabel lFont;
    private javax.swing.JLabel lForeground;
    private javax.swing.JLabel lLanguage;
    private javax.swing.JLabel lPreview;
    private javax.swing.JPanel pPreview;
    private javax.swing.JScrollPane spCategories;
    private javax.swing.JScrollPane spPreview;
    private javax.swing.JTextField tfFont;
    // End of variables declaration//GEN-END:variables
    

    @Override
    public void actionPerformed (ActionEvent evt) {
        if (!listen) return;
	if (evt.getSource () == cbEffects) {
            if (cbEffects.getSelectedIndex () == 0) {
                cbEffectColor.setSelectedItem(null);
            } else if (cbEffectColor.getSelectedItem() == null) {
                cbEffectColor.setSelectedIndex(0);
            }
	    cbEffectColor.setEnabled (cbEffects.getSelectedIndex () > 0);
            updateData ();
	} else if (evt.getSource () == cbLanguage) {
            updateLanguageCombobox();
	} else if (evt.getSource () == bFont) {
            PropertyEditor pe = PropertyEditorManager.findEditor (Font.class);
            AttributeSet category = getCurrentCategory ();
            if (category == null) {
                return;
            }
            Font f = getFont (category);
            pe.setValue (f);
            DialogDescriptor dd = new DialogDescriptor (
                pe.getCustomEditor (),
                loc ("CTL_Font_Chooser")                          // NOI18N
            );
            dd.setOptions (new Object[] {
                DialogDescriptor.OK_OPTION, 
                loc ("CTL_Font_Inherited"),                          // NOI18N
                DialogDescriptor.CANCEL_OPTION
            });
            Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
            dialog.setSize(460, 380);
            dialog.setVisible (true);
            if (dd.getValue () == DialogDescriptor.OK_OPTION) {
                f = (Font) pe.getValue ();
                category = modifyFont (category, f);
                replaceCurrrentCategory (category);
                setToBeSaved (currentProfile, currentLanguage);
                refreshUI (); // refresh font viewer
            } else if (dd.getValue ().equals (loc ("CTL_Font_Inherited"))) {
                String fontName = (String) getDefault (currentLanguage, category, StyleConstants.FontFamily);
                int style = 0;
                if (Boolean.TRUE.equals(getDefault (currentLanguage, category, StyleConstants.Bold))) {
                    style += Font.BOLD;
                }
                if (Boolean.TRUE.equals(getDefault (currentLanguage, category, StyleConstants.Italic))) {
                    style += Font.ITALIC;
                }
                Integer size = (Integer) getDefault (currentLanguage, category, StyleConstants.FontSize);
                
                assert(size != null);
                f = new Font (fontName, style,  size);
                category = modifyFont (category, f);
                replaceCurrrentCategory (category);
                setToBeSaved (currentProfile, currentLanguage);
                refreshUI (); // refresh font viewer
            }
        } else if (evt.getSource () instanceof JComboBox) {
            updateData ();
        }
        fireChanged();
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!listen || evt.getPropertyName() == null ) {
            return;
        }
        
        if (Preview.PROP_CURRENT_ELEMENT.equals(evt.getPropertyName())) {
            String currentCategory = (String) evt.getNewValue();
            List<AttributeSet> categories = getCategories(currentProfile, currentLanguage);
            if (currentLanguage.equals(ColorModel.ALL_LANGUAGES)) {
                String converted = (String) convertALC.get(currentCategory);
                if (converted != null) {
                    currentCategory = converted;
                }
            }

            for (int i = 0; i < categories.size(); i++) {
                AttributeSet as = categories.get(i);
                if (currentCategory.equals(as.getAttribute(StyleConstants.NameAttribute))) {
                    blink = false;
                    lCategories.setSelectedIndex(i);
                    lCategories.ensureIndexIsVisible(i);
                    blink = true;
                    break;
                }
            }
        }
    }
    private final Task updatecbLanguage = new RequestProcessor("SyntaxColoringPanel2").create(new Runnable() {

        @Override
        public void run() {
            final List<String> languages = new ArrayList<>(colorModel.getLanguages());
            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    languages.remove("text/x-all-languages");
                    languages.sort(new LanguagesComparator());
                    Iterator<String> it = languages.iterator();
                    synchronized (cbLanguage) {
                        Object lastLanguage = cbLanguage.getSelectedItem();
                        cbLanguage.removeAllItems();
                        while (it.hasNext()) {
                            cbLanguage.addItem(it.next());
                        }
                        listen = true;
                        if (lastLanguage != null) {
                            cbLanguage.setSelectedItem(lastLanguage);
                        } else {
                            cbLanguage.setSelectedIndex(0);
                        }
                        updateLanguageCombobox();
                    }
                }
            });
        }
    });
    
    @Override
    public void update (final ColorModel colorModel) {
        this.colorModel = colorModel;
        currentProfile = colorModel.getCurrentProfile ();
        currentLanguage = ColorModel.ALL_LANGUAGES;
        if (preview != null) 
            preview.removePropertyChangeListener 
                (Preview.PROP_CURRENT_ELEMENT, this);
        Component component = colorModel.getSyntaxColoringPreviewComponent 
            (currentLanguage);
        preview = (Preview) component;
        pPreview.removeAll ();
        pPreview.add ("Center", component);
        preview.addPropertyChangeListener 
            (Preview.PROP_CURRENT_ELEMENT, this);
        listen = false;
        updatecbLanguage.schedule(20);
        changed = false;
    }
    
    @Override
    public void cancel () {
        toBeSaved = new HashMap<>();
        profiles = new HashMap<>();
        changed = false;
    }
    
    @Override
    public void applyChanges() {
        if (colorModel == null) return;
        for(Map.Entry<String, Set<String>> entry : toBeSaved.entrySet()) {
            String profile = entry.getKey();
            Set<String> toBeSavedLanguages = entry.getValue();
            Map<String, List<AttributeSet>> schemeMap = profiles.get(profile);
            for(String languageName : toBeSavedLanguages) {
                colorModel.setCategories(
                    profile,
                    languageName,
                    schemeMap.get(languageName)
                );
            }
        }
        toBeSaved = new HashMap<>();
        profiles = new HashMap<>();
        changed = false;
    }
    
    @Override
    public boolean isChanged () {
        return changed;
    }
    
    @Override
    public void setCurrentProfile (String currentProfile) {
        String oldProfile = this.currentProfile;
        this.currentProfile = currentProfile;
        if (!colorModel.getProfiles().contains(currentProfile) && !profiles.containsKey(currentProfile)) {
            cloneScheme(oldProfile, currentProfile);
        }
        List<AttributeSet> categories = getCategories (currentProfile, currentLanguage);
        lCategories.setListData (categories.toArray(new AttributeSet[0]));
        blink = false;
        lCategories.setSelectedIndex (0);
        blink = true;
        refreshUI ();
        fireChanged();
    }

    @Override
    public void deleteProfile (String profile) {
        Map<String, List<AttributeSet>> m = new HashMap<> ();
        boolean custom = colorModel.isCustomProfile (profile);
        for (String language : colorModel.getLanguages ()) {
            if (custom) {
                m.put (language, null);
            } else {
                m.put (language, getDefaults (profile, language));
            }
        }
        profiles.put (profile, m);
        toBeSaved.put (profile, new HashSet<> (colorModel.getLanguages ()));
        if (!custom) {
            refreshUI ();
        }
        fireChanged();
    }
    
    @Override
    public JComponent getComponent() {
        return this;
    }
        
    // other methods ...........................................................
    
    private void cloneScheme(String oldScheme, String newScheme) {
        Map<String, List<AttributeSet>> m = new HashMap<>();
        for(String language : colorModel.getLanguages()) {
            List<AttributeSet> v = getCategories(oldScheme, language);
            List<AttributeSet> newV = new ArrayList<>();
            for (AttributeSet attributeSet : v) {
                newV.add(new SimpleAttributeSet (attributeSet));
            }
            m.put(language, newV);
            setToBeSaved(newScheme, language);
        }
        profiles.put(newScheme, m);
    }
    
    Collection<AttributeSet> getAllLanguages() {
        return getCategories(currentProfile, ColorModel.ALL_LANGUAGES);
    }
    
    Collection<AttributeSet> getSyntaxColorings() {
        return getCategories(currentProfile, currentLanguage);
    }
    
    private void setCurrentLanguage (String language) {
        if (language == null) {
            return;
        }
	currentLanguage = language;
        
        // setup categories list
        blink = false;
        lCategories.setListData (getCategories (currentProfile, currentLanguage).toArray(new AttributeSet[0]));
        lCategories.setSelectedIndex (0);
        blink = true;
        refreshUI ();
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (SyntaxColoringPanel.class, key);
    }
    
    private static void loc (Component c, String key) {
        if (c instanceof AbstractButton)
            Mnemonics.setLocalizedText (
                (AbstractButton) c, 
                loc (key)
            );
        else
            Mnemonics.setLocalizedText (
                (JLabel) c, 
                loc (key)
            );
    }

    /**
     * Called on user change.
     * Updates data structures and preview panel.
     */
    private void updateData () {
        int i = lCategories.getSelectedIndex ();
        if (i < 0) return;
        
        AttributeSet category = getCurrentCategory ();
        Color underline = null, 
              wave = null, 
              strikethrough = null;
        if (cbEffects.getSelectedIndex () == 1)
            underline = ((ColorComboBox)cbEffectColor).getSelectedColor();
        if (cbEffects.getSelectedIndex () == 2)
            wave = ((ColorComboBox)cbEffectColor).getSelectedColor();
        if (cbEffects.getSelectedIndex () == 3)
            strikethrough = ((ColorComboBox)cbEffectColor).getSelectedColor();
        
        SimpleAttributeSet c = category != null ? new SimpleAttributeSet(category) : new SimpleAttributeSet();
        
        Color color = ColorComboBoxSupport.getSelectedColor( (ColorComboBox)cbBackground );
        if (color != null) {
            c.addAttribute(StyleConstants.Background, color);
        } else {
            c.removeAttribute(StyleConstants.Background);
        }
        
        color = ColorComboBoxSupport.getSelectedColor( (ColorComboBox)cbForeground );
        if (color != null) {
            c.addAttribute(StyleConstants.Foreground, color);
        } else {
            c.removeAttribute(StyleConstants.Foreground);
        }
        
        if (underline != null) {
            c.addAttribute(StyleConstants.Underline, underline);
        } else {
            c.removeAttribute(StyleConstants.Underline);
        }
        
        if (strikethrough != null) {
            c.addAttribute(StyleConstants.StrikeThrough, strikethrough);
        } else {
            c.removeAttribute(StyleConstants.StrikeThrough);
        }
        
        if (wave != null) {
            c.addAttribute(EditorStyleConstants.WaveUnderlineColor, wave);
        } else {
            c.removeAttribute(EditorStyleConstants.WaveUnderlineColor);
        }
        
        replaceCurrrentCategory(c);
        setToBeSaved(currentProfile, currentLanguage);
        updatePreview();
    }

    private boolean                 blink = true;
    private int                     blinkSequence = 0;
    private RequestProcessor.Task   task = new RequestProcessor 
        ("SyntaxColoringPanel").create (new Runnable () {
        @Override
        public void run () {
            if (EventQueue.isDispatchThread()) {
                updatePreview ();
                if (blinkSequence == 0) {
                    return;
                }
                blinkSequence --;
                task.schedule (250);
            } else {
                EventQueue.invokeLater(this);
            }
        }
    });
    
    private void startBlinking () {
        blinkSequence = 5;
        task.schedule (0);
    }
    
    private void updatePreview () {
        Collection<AttributeSet> syntaxColorings = getSyntaxColorings();
        Collection<AttributeSet> allLanguages = getAllLanguages();
        if ((blinkSequence % 2) == 1) {
            if (ColorModel.ALL_LANGUAGES.equals(currentLanguage)) {
                allLanguages = invertCategory(allLanguages, getCurrentCategory());
            } else {
                syntaxColorings = invertCategory(syntaxColorings, getCurrentCategory());
            }
        }
        preview.setParameters (
            currentLanguage,
            allLanguages,
            Collections.<AttributeSet>emptySet(),
            syntaxColorings
        );
    }
    
    private Collection<AttributeSet> invertCategory (Collection<AttributeSet> c, AttributeSet category) {
        if (category == null) return c;
        ArrayList<AttributeSet> result = new ArrayList<> (c);
        int i = result.indexOf (category);
        SimpleAttributeSet as = new SimpleAttributeSet (category);
        Color highlight = (Color) getValue (currentLanguage, category, StyleConstants.Background);
        if (highlight == null) return result;
        Color newColor = new Color (
            255 - highlight.getRed (),
            255 - highlight.getGreen (),
            255 - highlight.getBlue ()
        );
        as.addAttribute (
            StyleConstants.Underline,
            newColor
        );
        result.set (i, as);
        return result;
    }
    
    /**
     * Called when current category, profile or language has been changed.
     * Updates all ui components.
     */
    private void refreshUI () {
        listen = false;
        AttributeSet category = getCurrentCategory ();
        if (category == null) {
            // no category selected > disable all elements
            tfFont.setText ("");
            bFont.setEnabled (false);
            cbEffects.setEnabled (false);
            cbForeground.setEnabled (false);
            ((org.openide.awt.ColorComboBox)cbForeground).setSelectedColor( null );
            cbBackground.setEnabled (false);
            cbBackground.setSelectedItem (new ColorValue (null, null));
            cbEffectColor.setEnabled (false);
            cbEffectColor.setSelectedItem (new ColorValue (null, null));
            updatePreview ();
            return;
        }
        bFont.setEnabled (true);
        cbEffects.setEnabled (true);
        cbForeground.setEnabled (true);
        cbBackground.setEnabled (true);
        
        // set defaults
        Color inheritedForeground = (Color) getDefault 
            (currentLanguage, category, StyleConstants.Foreground);
        if (inheritedForeground == null) inheritedForeground = Color.black;
        ColorComboBoxSupport.setInheritedColor ((ColorComboBox)cbForeground, inheritedForeground);
        Color inheritedBackground = (Color) getDefault 
            (currentLanguage, category, StyleConstants.Background);
        if (inheritedBackground == null) inheritedBackground = Color.white;
        ColorComboBoxSupport.setInheritedColor ((ColorComboBox)cbBackground, inheritedBackground);
        
        String font = fontToString (category);
        tfFont.setText (font);
        ColorComboBoxSupport.setSelectedColor( (ColorComboBox)cbForeground, (Color) category.getAttribute (StyleConstants.Foreground));
        ColorComboBoxSupport.setSelectedColor( (ColorComboBox)cbBackground, (Color) category.getAttribute (StyleConstants.Background));
        
        if (category.getAttribute (StyleConstants.Underline) != null) {
            cbEffects.setSelectedIndex (1);
            cbEffectColor.setEnabled (true);
            ((ColorComboBox)cbEffectColor).setSelectedColor((Color) category.getAttribute (StyleConstants.Underline));
        } else if (category.getAttribute (EditorStyleConstants.WaveUnderlineColor) != null) {
            cbEffects.setSelectedIndex (2);
            cbEffectColor.setEnabled (true);
            ((ColorComboBox)cbEffectColor).setSelectedColor((Color) category.getAttribute (EditorStyleConstants.WaveUnderlineColor));
        } else if (category.getAttribute (StyleConstants.StrikeThrough) != null) {
            cbEffects.setSelectedIndex (3);
            cbEffectColor.setEnabled (true);
            ((ColorComboBox)cbEffectColor).setSelectedColor((Color) category.getAttribute (StyleConstants.StrikeThrough));
        } else if (getDefault (currentLanguage, category, StyleConstants.Underline) != null) {
            cbEffects.setSelectedIndex (1);
            cbEffectColor.setEnabled (true);
            ((ColorComboBox)cbEffectColor).setSelectedColor((Color) getDefault (currentLanguage, category, StyleConstants.Underline));
        } else if (getDefault (currentLanguage, category, EditorStyleConstants.WaveUnderlineColor) != null) {
            cbEffects.setSelectedIndex (2);
            cbEffectColor.setEnabled (true);
            ((ColorComboBox)cbEffectColor).setSelectedColor((Color) getDefault (currentLanguage, category, EditorStyleConstants.WaveUnderlineColor));
        } else if (getDefault (currentLanguage, category, StyleConstants.StrikeThrough) != null) {
            cbEffects.setSelectedIndex (3);
            cbEffectColor.setEnabled (true);
            ((ColorComboBox)cbEffectColor).setSelectedColor((Color) getDefault (currentLanguage, category, StyleConstants.StrikeThrough));
        } else {
            cbEffects.setSelectedIndex (0);
            cbEffectColor.setEnabled (false);
            cbEffectColor.setSelectedIndex( -1 );
        }
        updatePreview ();
        listen = true;
    }
    
    private void setToBeSaved(String currentProfile, String currentLanguage) {
        toBeSaved.computeIfAbsent(currentProfile, k -> new HashSet<>())
                 .add(currentLanguage);
    }
    
    private void fireChanged() {
        boolean isChanged = false;
        for(Map.Entry<String, Set<String>> entry : toBeSaved.entrySet()) {
            String profile = entry.getKey();
            Set<String> toBeSavedLanguages = entry.getValue();
            Map<String, List<AttributeSet>> schemeMap = profiles.get(profile);
            for(String languageName : toBeSavedLanguages) {
                String[] mimePath = getMimePath(languageName);
                if (mimePath != null) { // this is a valid language
                    FontColorSettingsFactory fcs = EditorSettings.getDefault().getFontColorSettings(mimePath);
                    Collection<AttributeSet> allFontColors = fcs.getAllFontColors(profile);
                    Map<String, AttributeSet> savedSyntax = toMap(allFontColors);
                    List<AttributeSet> attributeSet = schemeMap.get(languageName);
                    Map<String, AttributeSet> currentSyntax = toMap(attributeSet);
                    if (savedSyntax != null && currentSyntax != null) {
                        if (savedSyntax.size() >= currentSyntax.size()) {
                            isChanged |= checkMaps(languageName, savedSyntax, currentSyntax);
                        } else {
                            isChanged |= checkMaps(languageName, currentSyntax, savedSyntax);
                        }
                    } else if (savedSyntax != null && currentSyntax == null) {
                        isChanged = true;
                    }
                    if (isChanged) { // no need to iterate further
                        changed = true;
                        return;
                    }
                }
            }
        }
        changed = isChanged;
    }
    
    private boolean checkMaps(String languageName, Map<String, AttributeSet> savedMap, Map<String, AttributeSet> currentMap) {
        boolean isChanged = false;
        for (String name : savedMap.keySet()) {
            if (currentMap.containsKey(name)) {
                AttributeSet currentAS = currentMap.get(name);
                AttributeSet savedAS = savedMap.get(name);
                isChanged |= isFontChanged(languageName, currentAS, savedAS)
                        || (Color) currentAS.getAttribute(StyleConstants.Foreground) != (Color) savedAS.getAttribute(StyleConstants.Foreground)
                        || (Color) currentAS.getAttribute(StyleConstants.Background) != (Color) savedAS.getAttribute(StyleConstants.Background)
                        || (Color) currentAS.getAttribute(StyleConstants.Underline) != (Color) savedAS.getAttribute(StyleConstants.Underline)
                        || (Color) currentAS.getAttribute(StyleConstants.StrikeThrough) != (Color) savedAS.getAttribute(StyleConstants.StrikeThrough)
                        || (Color) currentAS.getAttribute(EditorStyleConstants.WaveUnderlineColor) != (Color) savedAS.getAttribute(EditorStyleConstants.WaveUnderlineColor);
                if(isChanged) { // no need to iterate further
                    return true;
                }
            }
        }
        return isChanged;
    }
    
    private boolean isFontChanged(String language, AttributeSet currentAS, AttributeSet savedAS) {
        String name = (String) getValue(language, currentAS, StyleConstants.FontFamily);
        assert (name != null);
        Integer size = (Integer) getValue(language, currentAS, StyleConstants.FontSize);
        assert (size != null);
        Boolean bold = (Boolean) getValue(language, currentAS, StyleConstants.Bold);
        if (bold == null) {
            bold = Boolean.FALSE;
        }
        Boolean italic = (Boolean) getValue(language, currentAS, StyleConstants.Italic);
        if (italic == null) {
            italic = Boolean.FALSE;
        }
        int style = bold ? Font.BOLD : Font.PLAIN;
        if (italic) {
            style += Font.ITALIC;
        }
        Font currentFont = new Font(name, style, size);
        
        name = (String) getValue(language, savedAS, StyleConstants.FontFamily);
        assert (name != null);
        size = (Integer) getValue(language, savedAS, StyleConstants.FontSize);
        assert (size != null);
        bold = (Boolean) getValue(language, savedAS, StyleConstants.Bold);
        if (bold == null) {
            bold = Boolean.FALSE;
        }
        italic = (Boolean) getValue(language, savedAS, StyleConstants.Italic);
        if (italic == null) {
            italic = Boolean.FALSE;
        }
        style = bold ? Font.BOLD : Font.PLAIN;
        if (italic) {
            style += Font.ITALIC;
        }
        Font savedFont = new Font(name, style, size);
        return !currentFont.equals(savedFont);
    }
    
    // start copied from ColorModel
    private String [] getMimePath(String language) {
        if (language.equals(ColorModel.ALL_LANGUAGES)) {
            return new String[0];
        } else {
            String mimeType = getLanguageToMimeTypeMap().get(language);
            if(mimeType == null) {
                log.log(Level.WARNING, "Invalid language ''{0}''", language); //NOI18N
                return null;
            }
            return new String [] { mimeType };
        }
    }
    
    private Map<String, String> languageToMimeType;
    private Map<String, String> getLanguageToMimeTypeMap() {
        if (languageToMimeType == null) {
            languageToMimeType = new HashMap<>();
            Set<String> mimeTypes = EditorSettings.getDefault().getMimeTypes();
            for(String mimeType : mimeTypes) {
                String name = EditorSettings.getDefault().getLanguageName (mimeType);
                if (name.equals (mimeType))
                    continue;
                languageToMimeType.put (
                    name,
                    mimeType
                );
            }
            languageToMimeType.put(
                    ColorModel.ALL_LANGUAGES,
                    "Defaults" //NOI18N
                    );
        }
        return languageToMimeType;
    }
    
    private Map<String, AttributeSet> toMap(Collection<AttributeSet> categories) {
        if (categories == null) return null;
        Map<String, AttributeSet> result = new HashMap<>();
        for(AttributeSet as : categories) {
            result.put((String) as.getAttribute(StyleConstants.NameAttribute), as);
        }
        return result;
    }
    // end copied from ColorModel
    
    private List<AttributeSet> getCategories(String profile, String language) {
        if (colorModel == null) {
            return null;
        }
        Map<String, List<AttributeSet>> p = profiles.computeIfAbsent(profile, k -> new HashMap<String, List<AttributeSet>>());
        return p.computeIfAbsent(language, k -> {
            List<AttributeSet> l = new ArrayList<>();
            Collection<AttributeSet> c = colorModel.getCategories(profile, language);
            if (c != null) {
                l.addAll(c);
            }
            l.sort(new CategoryComparator());
            return l;
        });
    }

    private Map<String, Map<String, List<AttributeSet>>> defaults = new HashMap<>();
    /**
     * Returns original colors for given profile.
     */
    private List<AttributeSet> getDefaults(String profile, String language) {
        Map<String, List<AttributeSet>> m = defaults.computeIfAbsent(profile, k -> new HashMap<>());
        return new ArrayList<>(m.computeIfAbsent(language, k -> {
            List<AttributeSet> v = new ArrayList<>(colorModel.getDefaults(profile, language));
            v.sort(new CategoryComparator());
            return v;
        }));
    }
    
    private AttributeSet getCurrentCategory () {
        int i = lCategories.getSelectedIndex ();
        List<AttributeSet> c = getCategories(currentProfile, currentLanguage);
        return i >= 0 && i < c.size() ? (AttributeSet) c.get(i) : null;
    }
    
    private void replaceCurrrentCategory (AttributeSet newValues) {
        int i = lCategories.getSelectedIndex ();
        getCategories (currentProfile, currentLanguage).set (i, newValues);
    }
    
    private AttributeSet getCategory (
        String profile, 
        String language, 
        String name
    ) {
        List v = getCategories (profile, language);
        Iterator it = v.iterator ();
        while (it.hasNext ()) {
            AttributeSet c = (AttributeSet) it.next ();
            if (c.getAttribute (StyleConstants.NameAttribute).equals (name)) 
                return c;
        }
        return null;
    }
    
    private Object getValue (String language, AttributeSet category, Object key) {
        if (category.isDefined (key))
            return category.getAttribute (key);
        return getDefault (language, category, key);
    }
    
    private Object getDefault (String language, AttributeSet category, Object key) {
	String name = (String) category.getAttribute (EditorStyleConstants.Default);
	if (name == null) name = "default";

	// 1) search current language
	if (!name.equals (category.getAttribute (StyleConstants.NameAttribute))
        ) {
            AttributeSet defaultAS = getCategory 
                (currentProfile, language, name);
            if (defaultAS != null)
                return getValue (language, defaultAS, key);
	}
	
	// 2) search default language
        if (!language.equals (ColorModel.ALL_LANGUAGES)) {
            AttributeSet defaultAS = getCategory 
                (currentProfile, ColorModel.ALL_LANGUAGES, name);
            if (defaultAS != null)
                return getValue (ColorModel.ALL_LANGUAGES, defaultAS, key);
        }
        
        // CompositeFCS will fill default values if there are not.
        if (key == StyleConstants.FontFamily || key == StyleConstants.FontSize) {
            FontColorSettings fcs = MimeLookup.getLookup(MimePath.EMPTY).lookup(FontColorSettings.class);
            return fcs.getFontColors(FontColorNames.DEFAULT_COLORING).getAttribute(key);
        }
        else
            return null;
    }
    
    private Font getFont (AttributeSet category) {
        String name = (String) getValue (currentLanguage, category, StyleConstants.FontFamily);
        assert(name != null);
        Integer size = (Integer) getValue (currentLanguage, category, StyleConstants.FontSize);
        assert(size != null);
        Boolean bold = (Boolean) getValue (currentLanguage, category, StyleConstants.Bold);
        if (bold == null) bold = Boolean.FALSE;
        Boolean italic = (Boolean) getValue (currentLanguage, category, StyleConstants.Italic);
        if (italic == null) italic = Boolean.FALSE;
        int style = bold ? Font.BOLD : Font.PLAIN;
        if (italic) style += Font.ITALIC;
        return new Font (name, style, size);
    }
    
    private AttributeSet modifyFont (AttributeSet category, Font f) {
        String fontName = f.getName();
        Integer fontSize = f.getSize();
        Boolean bold = f.isBold();
        Boolean italic = f.isItalic();
        boolean isDefault = "default".equals (
            category.getAttribute (StyleConstants.NameAttribute)
        );
        if (fontName.equals(getDefault(currentLanguage, category, StyleConstants.FontFamily)) && !isDefault) {
            fontName = null;
        }
        if (fontSize.equals(getDefault(currentLanguage, category, StyleConstants.FontSize)) && !isDefault) {
            fontSize = null;
        }
        if (bold.equals(getDefault(currentLanguage, category, StyleConstants.Bold))) {
            bold = null;
        } else if (bold.equals(Boolean.FALSE) && getDefault(currentLanguage, category, StyleConstants.Bold) == null) {
            bold = null;
        }
        if (italic.equals(getDefault(currentLanguage, category, StyleConstants.Italic))) {
            italic = null;
        } else if (italic.equals(Boolean.FALSE) && getDefault(currentLanguage, category, StyleConstants.Italic) == null) {
            italic = null;
        }
        SimpleAttributeSet c = new SimpleAttributeSet(category);
        if (fontName != null) {
            c.addAttribute(
                    StyleConstants.FontFamily,
                    fontName
            );
        } else {
            c.removeAttribute(StyleConstants.FontFamily);
        }
        if (fontSize != null) {
            c.addAttribute(
                    StyleConstants.FontSize,
                    fontSize
            );
        } else {
            c.removeAttribute(StyleConstants.FontSize);
        }
        if (bold != null) {
            c.addAttribute(
                    StyleConstants.Bold,
                    bold
            );
        } else {
            c.removeAttribute(StyleConstants.Bold);
        }
        if (italic != null) {
            c.addAttribute(
                    StyleConstants.Italic,
                    italic
            );
        } else {
            c.removeAttribute(StyleConstants.Italic);
        }
        
        return c;
    }
    
    private String fontToString (AttributeSet category) {
        if ("default".equals(category.getAttribute (StyleConstants.NameAttribute))) {
            StringBuilder sb = new StringBuilder ();
            sb.append (getValue (currentLanguage, category, StyleConstants.FontFamily));
            sb.append (' ');
            sb.append (getValue (currentLanguage, category, StyleConstants.FontSize));
            Boolean bold = (Boolean) getValue (currentLanguage, category, StyleConstants.Bold);
            if (bold != null && bold)
                sb.append (' ').append (loc ("CTL_Bold"));                // NOI18N
            Boolean italic = (Boolean) getValue (currentLanguage, category, StyleConstants.Italic);
            if (italic != null && italic)
                sb.append (' ').append (loc ("CTL_Italic"));              // NOI18N
            return sb.toString ();
        }
        boolean def = false;
        StringBuilder sb = new StringBuilder();
        if (category.getAttribute (StyleConstants.FontFamily) != null)
            sb.append ('+').append (category.getAttribute (StyleConstants.FontFamily));
        else
            def = true;
        if (category.getAttribute (StyleConstants.FontSize) != null)
            sb.append ('+').append (category.getAttribute (StyleConstants.FontSize));
        else
            def = true;
        if (Boolean.TRUE.equals (category.getAttribute (StyleConstants.Bold)))
            sb.append ('+').append (loc ("CTL_Bold"));                // NOI18N
        if (Boolean.FALSE.equals (category.getAttribute (StyleConstants.Bold)))
            sb.append ('-').append (loc ("CTL_Bold"));                // NOI18N
        if (Boolean.TRUE.equals (category.getAttribute (StyleConstants.Italic)))
            sb.append ('+').append (loc ("CTL_Italic"));              // NOI18N
        if (Boolean.FALSE.equals (category.getAttribute (StyleConstants.Italic)))
            sb.append ('-').append (loc ("CTL_Italic"));              // NOI18N
        
        if (def) {
            sb.insert (0, loc ("CTL_Inherited"));                     // NOI18N
            return sb.toString ();
        } else {
            String result = sb.toString ();
            return result.replace ('+', ' ');
        }
    }
    
    private static final Map<String, String> convertALC = new HashMap<>();
    
    static {
        convertALC.put("character", "char"); //NOI18N
        convertALC.put("errors", "error"); //NOI18N
        convertALC.put("literal", "keyword"); //NOI18N
        convertALC.put("keyword-directive", "keyword"); //NOI18N
    }

    @Override
    public void itemStateChanged( ItemEvent e ) {
        if( e.getStateChange() == ItemEvent.DESELECTED || !listen )
            return;
        updateData();
        fireChanged();
    }
    
    private static final class LanguagesComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            if (o1.equals(ColorModel.ALL_LANGUAGES))
                return o2.equals(ColorModel.ALL_LANGUAGES) ? 0 : -1;
            if (o2.equals(ColorModel.ALL_LANGUAGES))
                return 1;
            return o1.compareTo(o2);
        }
    }
}
