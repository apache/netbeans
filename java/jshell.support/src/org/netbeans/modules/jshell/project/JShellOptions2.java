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
package org.netbeans.modules.jshell.project;

import org.netbeans.modules.jshell.launch.PropertyNames;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.TextAction;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.api.java.source.ui.TypeElementFinder;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.java.j2seproject.api.J2SECategoryExtensionProvider;
import org.netbeans.modules.java.j2seproject.api.J2SECategoryExtensionProvider.ConfigChangeListener;
import org.netbeans.modules.jshell.project.RunOptionsModel.LoaderPolicy;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import static org.netbeans.modules.jshell.launch.PropertyNames.*;

/**
 *
 * @author sdedic
 */
public class JShellOptions2 extends javax.swing.JPanel implements ItemListener {
    private final Project project;
    private J2SECategoryExtensionProvider.ConfigChangeListener listener;
    private ChangeListener  changeListener;
    private Map<String, String> changedOptions = new HashMap<>();
    private List<JComponent> hideControls = new ArrayList<>();
    private ElementHandle<TypeElement> targetClass;
    private String oldText;
    private ClasspathInfo cpi;
    
    /**
     * Creates new form JShellOptions
     */
    public JShellOptions2(Project project) {
        this.project = project;
        initComponents();
        
        DefaultComboBoxModel<LoaderPolicy> mdl = new DefaultComboBoxModel<>();
        mdl.addElement(LoaderPolicy.SYSTEM);
        mdl.addElement(LoaderPolicy.CLASS);
        mdl.addElement(LoaderPolicy.EVAL);
        
        loaderSelect.setModel(mdl);
        loaderSelect.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                RunOptionsModel.LoaderPolicy pol = (RunOptionsModel.LoaderPolicy)value;
                if (pol == null) {
                    pol = RunOptionsModel.LoaderPolicy.SYSTEM;
                }
                setText(NbBundle.getMessage(JShellOptions2.class, "JShellOptions.loader." + pol.name().toLowerCase()));
                return this;
            }
        });
        
        source.addActionListener(this::classNameChanged);
        source.addFocusListener(new FocusAdapter() {
           @Override
            public void focusLost(FocusEvent e) {
                if (e.getComponent() == source) {
                    classNameChanged(null);
                }
            }
        });
        loaderSelect.addItemListener(this);
        cbMember.setRenderer(new MemberRenderer());
        
        source.getActionMap().put("type-browse", new BrowseAction());
        source.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_DOWN_MASK), "type-browse");

        enableDisable();
    }
    
    public Project getProject() {
        return project;
    }
    
    public class BrowseAction extends TextAction {
        public BrowseAction() {
            super("type-browse");
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            btnBrowseClassActionPerformed(e);
        }
    }
    
    private void classNameChanged(ActionEvent ac) {
        final String s = source.getText().trim();
        if (s.equals(oldText)) {
            return;
        }
        if (loaderSelect.getSelectedItem() == LoaderPolicy.SYSTEM) {
            return;
        }
        
        class UT extends UserTask implements ClasspathInfo.Provider {

            @Override
            public ClasspathInfo getClasspathInfo() {
                return JShellOptions2.this.getClasspathInfo();
            }
            
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                CompilationInfo cc = CompilationInfo.get(resultIterator.getParserResult());
                TypeElement tel = cc.getElements().getTypeElement(s);
                if (tel != null) {
                    targetClass = ElementHandle.create(tel);
                    return;
                } else {
                    targetClass = null;
                }
            }
        }
        try {
            ParserManager.parse("text/x-java", new UT());
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        this.message = null;
        if (targetClass == null) {
            this.message = Bundle.ERR_ClassNameInvalid();
        } else {
            changedOptions.put(PropertyNames.JSHELL_CLASSNAME, s);
        }
        updateMembers();
        oldText = s;
        if (disableUpdates) {
            return;
        }
        storeChanges();
    }
    
    private String message;
    
    public boolean isPanelValid() {
        return message == null;
    }
    
    public String getErrorMessage() {
        return message;
    }
    
    public Map<String, String> getChangedOptions() {
        return changedOptions;
    }
    
    public void setConfigChangeListener(ChangeListener l) {
        this.changeListener = l;
    }
    
    public void setConfigChangeListener(ConfigChangeListener l) {
        this.listener = l;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        checkEnable = new javax.swing.JCheckBox();
        optsPanel = new javax.swing.JPanel();
        lblLoaderSelect = new javax.swing.JLabel();
        loaderSelect = new javax.swing.JComboBox();
        lblSource = new javax.swing.JLabel();
        source = new javax.swing.JTextField();
        lblMember = new javax.swing.JLabel();
        btnBrowseClass = new javax.swing.JButton();
        cbMember = new javax.swing.JComboBox();
        cSwingExecutor = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(checkEnable, org.openide.util.NbBundle.getMessage(JShellOptions2.class, "JShellOptions2.checkEnable.text")); // NOI18N
        checkEnable.setBorder(null);
        checkEnable.setLabel(org.openide.util.NbBundle.getMessage(JShellOptions2.class, "CHECK_EnableJShell")); // NOI18N
        checkEnable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkEnableActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblLoaderSelect, org.openide.util.NbBundle.getMessage(JShellOptions2.class, "JShellOptions2.lblLoaderSelect.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblSource, org.openide.util.NbBundle.getMessage(JShellOptions2.class, "JShellOptions2.lblSource.text")); // NOI18N
        lblSource.setEnabled(false);

        source.setText(org.openide.util.NbBundle.getMessage(JShellOptions2.class, "JShellOptions2.source.text")); // NOI18N
        source.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(lblMember, org.openide.util.NbBundle.getMessage(JShellOptions2.class, "JShellOptions2.lblMember.text")); // NOI18N
        lblMember.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(btnBrowseClass, org.openide.util.NbBundle.getMessage(JShellOptions2.class, "JShellOptions2.btnBrowseClass.text")); // NOI18N
        btnBrowseClass.setEnabled(false);
        btnBrowseClass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseClassActionPerformed(evt);
            }
        });

        cbMember.setEnabled(false);
        cbMember.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbMemberActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cSwingExecutor, org.openide.util.NbBundle.getMessage(JShellOptions2.class, "JShellOptions2.cSwingExecutor.text")); // NOI18N
        cSwingExecutor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cSwingExecutorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout optsPanelLayout = new javax.swing.GroupLayout(optsPanel);
        optsPanel.setLayout(optsPanelLayout);
        optsPanelLayout.setHorizontalGroup(
            optsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optsPanelLayout.createSequentialGroup()
                .addGroup(optsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(optsPanelLayout.createSequentialGroup()
                        .addComponent(lblLoaderSelect)
                        .addGap(4, 4, 4)
                        .addComponent(loaderSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(optsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblSource, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblMember, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(optsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbMember, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, optsPanelLayout.createSequentialGroup()
                                .addComponent(source, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnBrowseClass))))
                    .addComponent(cSwingExecutor))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        optsPanelLayout.setVerticalGroup(
            optsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optsPanelLayout.createSequentialGroup()
                .addGroup(optsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblLoaderSelect)
                    .addComponent(loaderSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(source, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSource)
                    .addComponent(btnBrowseClass))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(optsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblMember)
                    .addComponent(cbMember, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cSwingExecutor)
                .addGap(27, 27, 27))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(checkEnable)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(optsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkEnable)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(optsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void checkEnableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkEnableActionPerformed
        boolean enable = checkEnable.isSelected();
        enableDisable();
        if (!disableUpdates) {
            if (enable) {
                changedOptions.put(JSHELL_ENABLED, Boolean.TRUE.toString());
            } else {
                changedOptions.put(JSHELL_ENABLED, null);
            }
            storeChanges();
        }
    }//GEN-LAST:event_checkEnableActionPerformed

    public ClasspathInfo getClasspathInfo() {
        if (cpi != null) {
            return cpi;
        }
        FileObject fileObject = project.getProjectDirectory();
        SourceGroup[] grp = org.netbeans.api.project.ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        List<FileObject> roots = new ArrayList<>(grp.length);
        for (SourceGroup sg : grp) {
            roots.add(sg.getRootFolder());
        }
        return cpi = ClasspathInfo.create(
                ClassPath.getClassPath(fileObject, ClassPath.BOOT), // JDK classes
                ClassPath.EMPTY,
                ClassPathSupport.createClassPath(roots.toArray(FileObject[]::new))
        );
    }
    
    private void btnBrowseClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseClassActionPerformed
        final ClasspathInfo cpi = getClasspathInfo();
        String current = this.source.getText();
        ElementHandle<TypeElement> handle = TypeElementFinder.find(cpi, current, new TypeElementFinder.Customizer() {

            @Override
            public Set<ElementHandle<TypeElement>> query(ClasspathInfo classpathInfo, String textForQuery, ClassIndex.NameKind nameKind, Set<ClassIndex.SearchScope> searchScopes) {
                searchScopes.retainAll(Arrays.asList(ClassIndex.SearchScope.SOURCE));
                return cpi.getClassIndex().getDeclaredTypes(textForQuery, nameKind, searchScopes);
            }

            @Override
            public boolean accept(ElementHandle<TypeElement> typeHandle) {
                return true;
            }
        });
        if (handle == null) {
            return;
        }
        targetClass = handle;
        source.setText(handle.getQualifiedName());
        classNameChanged(null);
    }//GEN-LAST:event_btnBrowseClassActionPerformed

    private void cbMemberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbMemberActionPerformed
        Object o = cbMember.getSelectedItem();
        if (o != null && !disableUpdates) {
            MemberDescription md = (MemberDescription)o;
            if (md.kind == ElementKind.METHOD) {
                changedOptions.put(PropertyNames.JSHELL_FROM_FIELD, null);
                changedOptions.put(PropertyNames.JSHELL_FROM_METHOD, md.name);
            } else {
                changedOptions.put(PropertyNames.JSHELL_FROM_METHOD, null);
                changedOptions.put(PropertyNames.JSHELL_FROM_FIELD, md.name);
            }
            message = null;
            storeChanges();
        }
    }//GEN-LAST:event_cbMemberActionPerformed

    private void cSwingExecutorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cSwingExecutorActionPerformed
        if (disableUpdates) {
            return;
        }
        if (cSwingExecutor.isSelected()) {
            changedOptions.put(PropertyNames.JSHELL_EXECUTOR, PropertyNames.EXECUTOR_CLASS_SWING);
        } else {
            changedOptions.put(PropertyNames.JSHELL_EXECUTOR, null);
        }
        storeChanges();
    }//GEN-LAST:event_cSwingExecutorActionPerformed

    @NbBundle.Messages({
        "ERR_ClassNameInvalid=Invalid reference class name",
        "ERR_NoSuitableMembers=The selected class has no suitable mebers"
    })
    private void updateMembers() {
        if (loaderSelect.getSelectedItem() != LoaderPolicy.EVAL) {
            cbMember.getEditor().setItem(null);
            cbMember.setEnabled(false);
            return;
        }
        MemberDescription old = (MemberDescription)cbMember.getSelectedItem();
        final List<MemberDescription> descs = new ArrayList<>();
        if (targetClass != null) {
            try {
                ParserManager.parse("text/x-java", new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        CompilationInfo cc = CompilationInfo.get(resultIterator.getParserResult());
                        TypeElement clazz = targetClass.resolve(cc);
                        if (clazz == null) {
                            return;
                        }
                        for (Element e : clazz.getEnclosedElements()) {
                            ElementKind k = e.getKind();
                            if (k != ElementKind.FIELD) {
                                if (k != ElementKind.METHOD ||
                                    !((ExecutableElement)e).getParameters().isEmpty()) {
                                    // ignore non-fields, non-methods and methods with arguments
                                    continue;
                                }
                            }
                            if (clazz.getKind().isClass() && !e.getModifiers().contains(Modifier.STATIC)) {
                                continue;
                            }
                            String n = e.getSimpleName().toString();
                            String typeString = cc.getTypeUtilities().getTypeName(e.asType()).toString();
                            descs.add(new MemberDescription(k, n, e.getModifiers(), typeString));
                        }

                    }
                });
            } catch (ParseException ex) {
            }
            if (descs.isEmpty()) {
                this.message = Bundle.ERR_NoSuitableMembers();
            }
        }
        DefaultComboBoxModel mdl = new DefaultComboBoxModel(descs.toArray(MemberDescription[]::new));
        cbMember.setModel(mdl);
        cbMember.setSelectedItem(old);
        if (cbMember.getItemCount() == 0) {
            cbMember.setEnabled(false);
            return;
        } else {
            cbMember.setEnabled(true);
        }
        if (cbMember.getSelectedItem() == null || !cbMember.getSelectedItem().equals(old)) {
            cbMember.setSelectedIndex(0);
            if (!disableUpdates) {
                cbMemberActionPerformed(null);
            }
        }
    }
    
    private static class MemberRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel l = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            MemberDescription md = (MemberDescription)value;
            if (md == null) {
                return l;
            }
            l.setIcon(ElementIcons.getElementIcon(md.kind, md.modifiers));
            String n = md.name;
            if (md.kind == ElementKind.METHOD) {
                n += "()"; // NOI18N
            }
            l.setText(md.name);
            
            return l;
        }
    }
    
    private static class MemberDescription {
        private ElementKind kind;
        private Set<Modifier>   modifiers;
        private String      name;
        private String      typeString;

        public MemberDescription(ElementKind kind, String name, Set<Modifier> modifiers, String typeString) {
            this.kind = kind;
            this.name = name;
            this.typeString = typeString;
            this.modifiers = modifiers;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MemberDescription other = (MemberDescription) obj;
            if (!Objects.equals(this.name, other.name)) {
                return false;
            }
            if (this.kind != other.kind) {
                return false;
            }
            return true;
        }
    }
    
    private void storeChanges() {
        if (listener != null) {
            listener.propertiesChanged(changedOptions);
        } 
        if (changeListener != null) {
            changeListener.stateChanged(new ChangeEvent(this));
        }
    }
    
    public void readOptions(Map<String, String> opts) {
        disableUpdates = true;
        
        RunOptionsModel.LoaderPolicy pol;
        boolean enabled = Boolean.parseBoolean(opts.getOrDefault(JSHELL_ENABLED, Boolean.FALSE.toString()));
        checkEnable.setSelected(enabled);
        String polString = opts.getOrDefault(JSHELL_CLASS_LOADING, RunOptionsModel.LoaderPolicy.SYSTEM.name()).toUpperCase();
        try {
            pol = RunOptionsModel.LoaderPolicy.valueOf(polString);
        } catch (IllegalArgumentException ex) {
            // expected
            pol = RunOptionsModel.LoaderPolicy.SYSTEM;
        }
        
        String cn = opts.get(JSHELL_CLASSNAME);
        String f = opts.get(JSHELL_FROM_FIELD);
        String m = opts.get(JSHELL_FROM_METHOD);
        
        if (cn == null) {
            pol = RunOptionsModel.LoaderPolicy.SYSTEM;
        } else if (f == null && m == null) {
            pol = RunOptionsModel.LoaderPolicy.CLASS;
        }
        this.message = null;
        setPolicy(pol);
        setClassName(cn);
        setMethodOrFieldName(cn, f, m);
        
        cSwingExecutor.setSelected(PropertyNames.EXECUTOR_CLASS_SWING.equals(opts.get(PropertyNames.JSHELL_EXECUTOR)));
        
        disableUpdates = false;
        enableDisable();
    }
    
    private void enableDisable() {
        optsPanel.setVisible(checkEnable.isSelected());
        if (checkEnable.isSelected()) {
            Object o = loaderSelect.getSelectedItem();
            boolean b = o != null && o != LoaderPolicy.SYSTEM;
            source.setEnabled(b);
            lblSource.setEnabled(b);
            btnBrowseClass.setEnabled(b);

            lblMember.setEnabled(o == LoaderPolicy.EVAL);
            cbMember.setEnabled(o == LoaderPolicy.EVAL);
        }
        invalidate();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        enableDisable();
        LoaderPolicy pol;
        
        if (disableUpdates) {
            return;
        }
        Object o = loaderSelect.getSelectedItem();
        if (o == null) {
            o = LoaderPolicy.SYSTEM;
        }
        pol = (LoaderPolicy)o;
        changedOptions.put(JSHELL_CLASS_LOADING, o.toString().toLowerCase());
        switch ((LoaderPolicy)o) {
            case SYSTEM:
                changedOptions.remove(PropertyNames.JSHELL_CLASSNAME);
                // fall through
            case CLASS:
                changedOptions.remove(PropertyNames.JSHELL_FROM_FIELD);
                changedOptions.remove(PropertyNames.JSHELL_FROM_METHOD);
        }
        this.oldText = null;
        classNameChanged(null);
        storeChanges();
    }
    
    public void setPolicy(RunOptionsModel.LoaderPolicy policy) {
        disableUpdates = true;
        loaderSelect.setSelectedItem(policy);
        disableUpdates = false;
    }
    
    public void setClassName(String name) {
        disableUpdates = true;
        source.setText(name);
        oldText = null;
        classNameChanged(null);
        disableUpdates = false;
    }
    
    public void setMethodOrFieldName(String clazz, String method, String field) {
        disableUpdates = true;
        updateMembers();
        for (int i = 0; i < cbMember.getItemCount(); i++) {
            Object o = cbMember.getItemAt(i);
            MemberDescription desc = (MemberDescription)o;
            if (method != null) {
                if (desc.kind == ElementKind.METHOD && desc.name.equals(method)) {
                    cbMember.setSelectedIndex(i);
                    break;
                }
            } else if (field != null) {
                if (desc.kind == ElementKind.FIELD && desc.name.equals(method)) {
                    cbMember.setSelectedIndex(i);
                    break;
                }
            }
        }
        disableUpdates = false;
    }
    
    public void setEnabled(boolean enabled) {
        disableUpdates = true;
        checkEnable.setSelected(enabled);
        disableUpdates = true;
    }
    
    /**
     * If true, will not update config based on UI events.
     */
    private boolean disableUpdates = false;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBrowseClass;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox cSwingExecutor;
    private javax.swing.JComboBox cbMember;
    private javax.swing.JCheckBox checkEnable;
    private javax.swing.JLabel lblLoaderSelect;
    private javax.swing.JLabel lblMember;
    private javax.swing.JLabel lblSource;
    private javax.swing.JComboBox loaderSelect;
    private javax.swing.JPanel optsPanel;
    private javax.swing.JTextField source;
    // End of variables declaration//GEN-END:variables
}
