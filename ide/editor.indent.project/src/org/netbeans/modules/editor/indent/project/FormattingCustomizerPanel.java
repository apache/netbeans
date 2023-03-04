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

package org.netbeans.modules.editor.indent.project;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.project.api.Customizers;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.editor.settings.storage.api.EditorSettingsStorage;
import org.netbeans.modules.editor.settings.storage.spi.TypedValue;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.netbeans.modules.options.indentation.CustomizerSelector;
import org.netbeans.modules.options.indentation.FormattingPanel;
import org.netbeans.modules.options.indentation.FormattingPanelController;
import org.netbeans.modules.options.indentation.ProxyPreferences;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Mutex.ExceptionAction;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public final class FormattingCustomizerPanel extends javax.swing.JPanel implements ActionListener, HelpCtx.Provider {
    
    // ------------------------------------------------------------------------
    // ProjectCustomizer.CompositeCategoryProvider implementation
    // ------------------------------------------------------------------------

    /**
     * Creates an instance of the 'Formatting' category in the project properties dialog.
     * This method is meant to be used from XML layers by modules that wish to add
     * the 'Formatting' category to their project type's properties dialog.
     *
     * <p>The method recognizes 'allowedMimeTypes' XML layer attribute, which should
     * contain the comma separated list of mime types, which formatting settings
     * customizers should be made available for the project. If the attribute is
     * not specified all registered customizers are shown. If the attribute specifies
     * an empty list only the 'All Languages' customizer is shown.
     *
     * @param attrs The map of <code>FileObject</code> attributes
     *
     * @return A new 'Formatting' category provider.
     * @since 1.0
     * @deprecated Use {@link Customizers#createFormattingCategoryProvider(java.util.Map) } instead.
     */
    @Deprecated
    public static ProjectCustomizer.CompositeCategoryProvider createCategoryProvider(Map attrs) {
        return Customizers.createFormattingCategoryProvider(attrs);
    }

    public static class Factory implements ProjectCustomizer.CompositeCategoryProvider {
 
        private static final String CATEGORY_FORMATTING = "Formatting"; // NOI18N
        private final String allowedMimeTypes;

        public Factory() {
            this(null);
        }

        public Factory(String allowedMimeTypes) {
            this.allowedMimeTypes = allowedMimeTypes;
        }

        public ProjectCustomizer.Category createCategory(Lookup context) {
            return context.lookup(Project.class) == null ? null : ProjectCustomizer.Category.create(
                    CATEGORY_FORMATTING, 
                    NbBundle.getMessage(Factory.class, "LBL_CategoryFormatting"), //NOI18N
                    null);
        }

        public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
            FormattingCustomizerPanel customizerPanel = new FormattingCustomizerPanel(context, allowedMimeTypes);
            category.setStoreListener(customizerPanel);
            return customizerPanel;
        }
    } // End of Factory class
    
    // ------------------------------------------------------------------------
    // ActionListener implementation
    // ------------------------------------------------------------------------

    // this is called when OK button is clicked to store the controlled preferences
    public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            public @Override void run() {
                if (DEFAULT_PROFILE.equals(pf.getPreferences("").parent().get(USED_PROFILE, DEFAULT_PROFILE))) { //NOI18N
                    // no per-project formatting settings
                    Preferences p = ProjectUtils.getPreferences(pf.getProject(), IndentUtils.class, true);
                    try {
                        removeAllKidsAndKeys(p);
                    } catch (BackingStoreException bse) {
                        LOG.log(Level.WARNING, null, bse);
                    }
                } else {
                    pf.applyChanges();

                    // Find mimeTypes that do not have a customizer
                    Set<String> mimeTypes = new HashSet<String>(EditorSettings.getDefault().getAllMimeTypes());
                    mimeTypes.removeAll(selector.getMimeTypes());

                    // and make sure that they do NOT override basic settings from All Languages
                    Preferences p = ProjectUtils.getPreferences(pf.getProject(), IndentUtils.class, true);
                    for(String mimeType : mimeTypes) {
                        try {
                            p.node(mimeType).removeNode();
                        } catch (BackingStoreException bse) {
                            LOG.log(Level.WARNING, null, bse);
                        }
                    }
                }

                // XXX: just use whatever value, it's ignored anyway, this is here in order
                // to fire property change events on documents, which are then intercepted by
                // the new view hierarchy (DocumentView)
                JTextComponent lastFocused = EditorRegistry.lastFocusedComponent();
                if (lastFocused != null) {
                    lastFocused.getDocument().putProperty(SimpleValueNames.TEXT_LINE_WRAP, ""); //NOI18N
                    lastFocused.getDocument().putProperty(SimpleValueNames.TAB_SIZE, ""); //NOI18N
                    lastFocused.getDocument().putProperty(SimpleValueNames.TEXT_LIMIT_WIDTH, ""); //NOI18N
                }
                for(JTextComponent jtc : EditorRegistry.componentList()) {
                    if (lastFocused == null || lastFocused != jtc) {
                        jtc.getDocument().putProperty(SimpleValueNames.TEXT_LINE_WRAP, ""); //NOI18N
                        jtc.getDocument().putProperty(SimpleValueNames.TAB_SIZE, ""); //NOI18N
                        jtc.getDocument().putProperty(SimpleValueNames.TEXT_LIMIT_WIDTH, ""); //NOI18N
                    }
                }
            }
        });
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx( FormattingCustomizerPanel.class );
    }

    // ------------------------------------------------------------------------
    // private implementation
    // ------------------------------------------------------------------------
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        group = new javax.swing.ButtonGroup();
        globalButton = new javax.swing.JRadioButton();
        editGlobalButton = new javax.swing.JButton();
        projectButton = new javax.swing.JRadioButton();
        loadButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        customizerPanel = new javax.swing.JPanel();

        group.add(globalButton);
        org.openide.awt.Mnemonics.setLocalizedText(globalButton, org.openide.util.NbBundle.getMessage(FormattingCustomizerPanel.class, "LBL_FormattingCustomizer_Global")); // NOI18N
        globalButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                globalButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(editGlobalButton, org.openide.util.NbBundle.getMessage(FormattingCustomizerPanel.class, "LBL_FormattingCustomizer_EditGlobal")); // NOI18N
        editGlobalButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editGlobalButtonActionPerformed(evt);
            }
        });

        group.add(projectButton);
        org.openide.awt.Mnemonics.setLocalizedText(projectButton, org.openide.util.NbBundle.getMessage(FormattingCustomizerPanel.class, "LBL_FormattingCustomizer_Project")); // NOI18N
        projectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(loadButton, org.openide.util.NbBundle.getMessage(FormattingCustomizerPanel.class, "LBL_ForamttingCustomizer_Load")); // NOI18N
        loadButton.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButtonActionPerformed(evt);
            }
        });

        jScrollPane1.setBorder(null);

        customizerPanel.setLayout(new java.awt.BorderLayout());
        jScrollPane1.setViewportView(customizerPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(projectButton, javax.swing.GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE)
                    .addComponent(globalButton, javax.swing.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE))
                .addGap(14, 14, 14)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(editGlobalButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(loadButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(globalButton)
                    .addComponent(editGlobalButton))
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectButton)
                    .addComponent(loadButton))
                .addGap(12, 12, 12)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE))
        );

        globalButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormattingCustomizerPanel.class, "FormattingCustomizerPanel.globalButton.AccessibleContext.accessibleDescription")); // NOI18N
        editGlobalButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormattingCustomizerPanel.class, "FormattingCustomizerPanel.editGlobalButton.AccessibleContext.accessibleDescription")); // NOI18N
        projectButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormattingCustomizerPanel.class, "FormattingCustomizerPanel.projectButton.AccessibleContext.accessibleDescription")); // NOI18N
        loadButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormattingCustomizerPanel.class, "FormattingCustomizerPanel.loadButton.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void globalButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_globalButtonActionPerformed

    NotifyDescriptor d = new NotifyDescriptor.Confirmation(
            NbBundle.getMessage(FormattingCustomizerPanel.class, "MSG_use_global_settings_confirmation"), //NOI18N
            NbBundle.getMessage(FormattingCustomizerPanel.class, "MSG_use_global_settings_confirmation_title"), //NOI18N
            NotifyDescriptor.OK_CANCEL_OPTION
    );

    if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
        pf.getPreferences("").parent().put(USED_PROFILE, DEFAULT_PROFILE); //NOI18N
        loadButton.setEnabled(false);
        setEnabled(jScrollPane1, false);
    } else {
        projectButton.setSelected(true);
    }

}//GEN-LAST:event_globalButtonActionPerformed

private void projectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectButtonActionPerformed

    pf.getPreferences("").parent().put(USED_PROFILE, PROJECT_PROFILE); //NOI18N
    loadButton.setEnabled(true);
    setEnabled(jScrollPane1, true);

    if (copyOnFork) {
        copyOnFork = false;

        // copy global settings
        EditorSettingsStorage<String, TypedValue> storage = EditorSettingsStorage.<String, TypedValue>get("Preferences"); //NOI18N
        for(String mimeType : selector.getMimeTypes()) {
            Map<String, TypedValue> mimePathLocalPrefs;
            try {
                mimePathLocalPrefs = storage.load(MimePath.parse(mimeType), null, false);
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, null, ioe);
                continue;
            }

            Preferences projectPrefs = pf.getPreferences(mimeType);
            
            // XXX: we should somehow be able to determine __all__ the formatting settings
            // for each mime type, but there can be different sets of settings for different
            // mime types (eg. all java, ruby and C++ have different formatting settings).
            // The only way is to stash all formatting settings under one common Preferences node
            // as it is in projects. The problem is that MimeLookup's Preferences implementation
            // does not support subnodes.
            // So, we at least copy the basic setting
            boolean copied = false;
            copied |= copyValueIfExists(mimePathLocalPrefs, projectPrefs, SimpleValueNames.EXPAND_TABS);
            copied |= copyValueIfExists(mimePathLocalPrefs, projectPrefs, SimpleValueNames.INDENT_SHIFT_WIDTH);
            copied |= copyValueIfExists(mimePathLocalPrefs, projectPrefs, SimpleValueNames.SPACES_PER_TAB);
            copied |= copyValueIfExists(mimePathLocalPrefs, projectPrefs, SimpleValueNames.TAB_SIZE);
            copied |= copyValueIfExists(mimePathLocalPrefs, projectPrefs, SimpleValueNames.TEXT_LIMIT_WIDTH);
            copied |= copyValueIfExists(mimePathLocalPrefs, projectPrefs, SimpleValueNames.TEXT_LINE_WRAP);

            if (mimeType.length() > 0 && copied) {
                projectPrefs.putBoolean(FormattingPanelController.OVERRIDE_GLOBAL_FORMATTING_OPTIONS, true);
            }
        }
    }
}//GEN-LAST:event_projectButtonActionPerformed

private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButtonActionPerformed
    JFileChooser chooser = ProjectChooser.projectChooser();
    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        File f = FileUtil.normalizeFile(chooser.getSelectedFile());
        FileObject fo = FileUtil.toFileObject(f);
        if (fo != null) {
            Object ret;
            
            try {
                final Project prjFrom = ProjectManager.getDefault().findProject(fo);
                if (prjFrom == pf.getProject()) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(FormattingCustomizerPanel.class, "MSG_CodeStyle_Import_Forbidden_From_The_Same_Project"), //NOI18N
                        NotifyDescriptor.Message.PLAIN_MESSAGE));

                    return;
                }

                ret = ProjectManager.mutex().readAccess(new ExceptionAction<Object>() {
                    public Object run() throws Exception {
                        Preferences fromPrjPrefs = ProjectUtils.getPreferences(prjFrom, IndentUtils.class, true);

                        if (!fromPrjPrefs.nodeExists(CODE_STYLE_PROFILE) ||
                            fromPrjPrefs.node(CODE_STYLE_PROFILE).get(USED_PROFILE, null) == null
                        ) {
                            return NbBundle.getMessage(FormattingCustomizerPanel.class, "MSG_No_CodeStyle_Info_To_Import"); //NOI18N
                        }

                        ProjectPreferencesFactory newPrefsFactory = new ProjectPreferencesFactory(pf.getProject());
                        Preferences toPrjPrefs = newPrefsFactory.projectPrefs;

                        removeAllKidsAndKeys(toPrjPrefs);
                        deepCopy(fromPrjPrefs, toPrjPrefs);

                        // XXX: detect somehow if the basic options are overriden in fromPrjPrefs
                        // and set the flag accordingly in toPrjPrefs
                        
                        //dump(fromPrjPrefs, "fromPrjPrefs");
                        //dump(toPrjPrefs, "toPrjPrefs");

                        return newPrefsFactory;
                    }
                });

            } catch (Exception e) {
                LOG.log(Level.INFO, null, e);
                ret = e;
            }

            if (ret instanceof ProjectPreferencesFactory) {
                String selectedMimeType = selector.getSelectedMimeType();
                PreferencesCustomizer c = selector.getSelectedCustomizer();
                String selectedCustomizerId = c != null ? c.getId() : null;

                pf.destroy();
                pf = (ProjectPreferencesFactory) ret;
                selector = new CustomizerSelector(pf, false, allowedMimeTypes);
                panel.setSelector(selector);

                if (selectedMimeType != null) {
                    selector.setSelectedMimeType(selectedMimeType);
                }
                if (selectedCustomizerId != null) {
                    selector.setSelectedCustomizer(selectedCustomizerId);
                }

                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(FormattingCustomizerPanel.class, "MSG_CodeStyle_Import_Successful"), //NOI18N
                    NotifyDescriptor.Message.PLAIN_MESSAGE));

            } else if (ret instanceof Exception) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(FormattingCustomizerPanel.class, "MSG_CodeStyle_Import_Failed"), //NOI18N
                    NotifyDescriptor.Message.WARNING_MESSAGE));
                
            } else {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    (String) ret,
                    NotifyDescriptor.Message.PLAIN_MESSAGE));
            }
        }
    }
}//GEN-LAST:event_loadButtonActionPerformed

    private void dump(Preferences prefs, String prefsId) throws BackingStoreException {
        for(String key : prefs.keys()) {
            System.out.println(prefsId + ", " + prefs.absolutePath() + "/" + key + "=" + prefs.get(key, null));
        }
        for(String child : prefs.childrenNames()) {
            dump(prefs.node(child), prefsId);
        }
    }

private void editGlobalButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editGlobalButtonActionPerformed
    OptionsDisplayer.getDefault().open(GLOBAL_OPTIONS_CATEGORY);
}//GEN-LAST:event_editGlobalButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel customizerPanel;
    private javax.swing.JButton editGlobalButton;
    private javax.swing.JRadioButton globalButton;
    private javax.swing.ButtonGroup group;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton loadButton;
    private javax.swing.JRadioButton projectButton;
    // End of variables declaration//GEN-END:variables

    private static final Logger LOG = Logger.getLogger(FormattingCustomizerPanel.class.getName());
    
    private static final String GLOBAL_OPTIONS_CATEGORY = "Editor/Formatting"; //NOI18N
    private static final String CODE_STYLE_PROFILE = "CodeStyle"; // NOI18N
    private static final String DEFAULT_PROFILE = "default"; // NOI18N
    private static final String PROJECT_PROFILE = "project"; // NOI18N
    private static final String USED_PROFILE = "usedProfile"; // NOI18N

    private final String allowedMimeTypes;
    private ProjectPreferencesFactory pf;
    private CustomizerSelector selector;
    private final FormattingPanel panel;
    private boolean copyOnFork;
    
    /** Creates new form CodeStyleCustomizerPanel */
    private FormattingCustomizerPanel(Lookup context, String allowedMimeTypes) {
        this.allowedMimeTypes = allowedMimeTypes;
        this.pf = new ProjectPreferencesFactory(context.lookup(Project.class));
        this.selector = new CustomizerSelector(pf, false, allowedMimeTypes);
        this.panel = new FormattingPanel();
        this.panel.setSelector(selector);

        initComponents();
        customizerPanel.add(panel, BorderLayout.CENTER);
        
        Preferences prefs = pf.getPreferences("").parent(); //NOI18N
        this.copyOnFork = prefs.get(USED_PROFILE, null) == null;
        String profile = prefs.get(USED_PROFILE, DEFAULT_PROFILE);
        if (DEFAULT_PROFILE.equals(profile)) {
            globalButton.setSelected(true);
            loadButton.setEnabled(false);
            setEnabled(jScrollPane1, false);
//            globalButton.doClick();
        } else {
//            projectButton.doClick();
            projectButton.setSelected(true);
            loadButton.setEnabled(true);
            setEnabled(jScrollPane1, true);
        }
    }
    
    private void setEnabled(Component component, boolean enabled) {
        component.setEnabled(enabled);
        if (component instanceof Container && !(component instanceof JSpinner)) {
            for (Component c : ((Container)component).getComponents()) {
                setEnabled(c, enabled);
            }
        }
    }

    private static boolean copyValueIfExists(Map<String, TypedValue> src, Preferences trg, String key) {
        TypedValue value = src.get(key);
        if (value != null) {
            // since project Preferences do not support javaType we can just use simple put
            trg.put(key, value.getValue());
            return true;
        } else {
            return false;
        }
    }

    private static void removeAllKidsAndKeys(Preferences prefs) throws BackingStoreException {
        for(String kid : prefs.childrenNames()) {
            // remove just the keys otherwise node listeners won't survive
            removeAllKidsAndKeys(prefs.node(kid));
        }
        for(String key : prefs.keys()) {
            prefs.remove(key);
        }
    }

    private static void deepCopy(Preferences from, Preferences to) throws BackingStoreException {
        for(String kid : from.childrenNames()) {
            Preferences fromKid = from.node(kid);
            Preferences toKid = to.node(kid);
            deepCopy(fromKid, toKid);
        }
        for(String key : from.keys()) {
            String value = from.get(key, null);
            if (value == null) continue;

            Class type = guessType(value);
            if (Integer.class == type) {
                to.putInt(key, from.getInt(key, -1));
            } else if (Long.class == type) {
                to.putLong(key, from.getLong(key, -1L));
            } else if (Float.class == type) {
                to.putFloat(key, from.getFloat(key, -1f));
            } else if (Double.class == type) {
                to.putDouble(key, from.getDouble(key, -1D));
            } else if (Boolean.class == type) {
                to.putBoolean(key, from.getBoolean(key, false));
            } else if (String.class == type) {
                to.put(key, value);
            } else /* byte [] */ {
                to.putByteArray(key, from.getByteArray(key, new byte [0]));
            }
        }
    }

    // XXX: this is here only to supprt deprecated Settings.class, when we are sure,
    // that no code uses Settings.class we will be able to remove this
    private static Class guessType(String value) {
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) { //NOI18N
            return Boolean.class;
        }

        try {
            Integer.parseInt(value);
            return Integer.class;
        } catch (NumberFormatException nfe) { /* ignore */ }

        try {
            Long.parseLong(value);
            return Long.class;
        } catch (NumberFormatException nfe) { /* ignore */ }

        try {
            Float.parseFloat(value);
            return Float.class;
        } catch (NumberFormatException nfe) { /* ignore */ }

        try {
            Double.parseDouble(value);
            return Double.class;
        } catch (NumberFormatException nfe) { /* ignore */ }

        // XXX: ignoring byte []
        return String.class;
    }
    
    private static final class ProjectPreferencesFactory implements CustomizerSelector.PreferencesFactory {

        public ProjectPreferencesFactory(Project project) {
            this.project = project;
            Preferences p = ProjectUtils.getPreferences(project, IndentUtils.class, true);
            projectPrefs = ProxyPreferences.getProxyPreferences(this, p);
        }

        public Project getProject() {
            return project;
        }

        public void destroy() {
            accessedMimeTypes.clear();
            projectPrefs.destroy();
            projectPrefs = null;
        }

        // --------------------------------------------------------------------
        // CustomizerSelector.PreferencesFactory implementation
        // --------------------------------------------------------------------

        public synchronized Preferences getPreferences(String mimeType) {
            assert projectPrefs != null;
            accessedMimeTypes.add(mimeType);
            return projectPrefs.node(mimeType).node(CODE_STYLE_PROFILE).node(PROJECT_PROFILE);
        }

        public synchronized void applyChanges() {
            for(String mimeType : accessedMimeTypes) {
                if (mimeType.length() == 0) {
                    continue;
                }

                ProxyPreferences pp = (ProxyPreferences) getPreferences(mimeType);
                if (null != pp.get(FormattingPanelController.OVERRIDE_GLOBAL_FORMATTING_OPTIONS, null)) {
                    // tabs-and-indents has been used and the basic options might have been changed
                    pp.silence();
                    if (!pp.getBoolean(FormattingPanelController.OVERRIDE_GLOBAL_FORMATTING_OPTIONS, false)) {
                        // remove the basic settings if a language is not overriding the 'all languages' values
                        pp.remove(SimpleValueNames.EXPAND_TABS);
                        pp.remove(SimpleValueNames.INDENT_SHIFT_WIDTH);
                        pp.remove(SimpleValueNames.SPACES_PER_TAB);
                        pp.remove(SimpleValueNames.TAB_SIZE);
                        pp.remove(SimpleValueNames.TEXT_LIMIT_WIDTH);
                        pp.remove(SimpleValueNames.TEXT_LINE_WRAP);
                    }
                    pp.remove(FormattingPanelController.OVERRIDE_GLOBAL_FORMATTING_OPTIONS);
                }
            }

            // flush the root prefs
            projectPrefs.silence();
            try {
                LOG.fine("Flushing root pp"); //NOI18N
                projectPrefs.flush();
            } catch (BackingStoreException ex) {
                LOG.log(Level.WARNING, "Can't flush project codestyle root preferences", ex); //NOI18N
            }

            destroy();
        }

        public boolean isKeyOverridenForMimeType(String key, String mimeType) {
            Preferences p = projectPrefs != null ? projectPrefs : ProjectUtils.getPreferences(project, IndentUtils.class, true);
            p = p.node(mimeType).node(CODE_STYLE_PROFILE).node(PROJECT_PROFILE);
            return p.get(key, null) != null;
        }

        // --------------------------------------------------------------------
        // private implementation
        // --------------------------------------------------------------------

        private final Project project;
        private final Set<String> accessedMimeTypes = new HashSet<String>();
        private ProxyPreferences projectPrefs;

    } // End of ProjectPreferencesFactory class
}
