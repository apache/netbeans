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

package org.netbeans.modules.web.jsf.palette.items;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.persistence.api.EntityClassScope;
import org.netbeans.modules.j2ee.persistence.wizard.EntityClosure;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JsfTemplateUtils;
import org.netbeans.modules.web.jsf.JsfTemplateUtils.OpenTemplateAction;
import org.netbeans.modules.web.jsf.JsfTemplateUtils.TemplateType;
import org.netbeans.modules.web.jsf.api.editor.JSFBeanCache;
import org.netbeans.modules.web.jsf.api.facesmodel.JsfVersionUtils;
import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.jsfapi.api.JsfVersion;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class ManagedBeanCustomizer extends javax.swing.JPanel implements CancellableDialog {

    private static final long serialVersionUID = 1L;
    private static final RequestProcessor RP = new RequestProcessor();

    public static final String VIEW_TEMPLATE = "view.ftl"; // NOI18N
    public static final String EDIT_TEMPLATE = "edit.ftl"; // NOI18N
    public static final String TABLE_TEMPLATE = "table.ftl"; // NOI18N

    private Project project;
    private org.netbeans.modules.web.beans.MetaModelSupport metaModelSupport;
    private org.netbeans.modules.jakarta.web.beans.MetaModelSupport jakartaMetaModelSupport;
    private boolean collection;
    private boolean dummyBean = false;
    private Dialog dialog;
    private DialogDescriptor dialogDescriptor;
    private boolean cancelled = false;

    public ManagedBeanCustomizer(Project project, boolean collection, boolean enableReadOnly) {
        initComponents();
        scanningLabel.setVisible(SourceUtils.isScanInProgress());
        EntityClassScope scope = EntityClassScope.getEntityClassScope(project.getProjectDirectory());
        EntityClosure ec = EntityClosure.create(scope, project);
        entityBeanCombo.setModel(EntityClosure.getAsComboModel(ec));
        entityBeanCombo.getModel().addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                setScanningLabelVisible(SourceUtils.isScanInProgress());
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                setScanningLabelVisible(SourceUtils.isScanInProgress());
            }
        });
        this.project = project;
        JsfVersion projectJsfVersion = JsfVersionUtils.forProject(project);
        if(projectJsfVersion != null && projectJsfVersion.isAtLeast(JsfVersion.JSF_3_0)){
            this.jakartaMetaModelSupport = new org.netbeans.modules.jakarta.web.beans.MetaModelSupport(project);
        } else {
            this.metaModelSupport = new org.netbeans.modules.web.beans.MetaModelSupport(project);
        }
        
        this.collection = collection;
        readOnlyCheckBox.setVisible(enableReadOnly);
        hint.setVisible(false);
        Component comp = this.managedBeanCombo.getEditor().getEditorComponent();
        if (comp instanceof JTextField) {
            final JTextField field = (JTextField)comp;
            field.getDocument().addDocumentListener(new DocumentListener() {

                public void insertUpdate(DocumentEvent arg0) {
                    updateValidity(field.getText());
                }

                public void removeUpdate(DocumentEvent arg0) {
                    updateValidity(field.getText());
                }

                public void changedUpdate(DocumentEvent arg0) {
                    updateValidity(field.getText());
                }
            });
        }

        // templates comboBox
        for (JsfTemplateUtils.Template template : JsfTemplateUtils.getTemplates(JsfTemplateUtils.TemplateType.SNIPPETS)) {
            templatesStyleComboBox.addItem(template);
        }
        templatesStyleComboBox.setRenderer(new JsfTemplateUtils.TemplateCellRenderer());
    }

    private void updateValidity(String text) {
        dialogDescriptor.setValid(text.length() > 0 && entityBeanCombo.getSelectedItem() != null);
    }

    public String getBeanClass() {
        return (String)entityBeanCombo.getSelectedItem();
    }

    public String getManagedBeanProperty() {
        return (String)managedBeanCombo.getSelectedItem();
    }

    public boolean isReadOnly() {
        return readOnlyCheckBox.isSelected();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        entityBeanLabel = new javax.swing.JLabel();
        entityBeanCombo = new javax.swing.JComboBox();
        managedBeanLabel = new javax.swing.JLabel();
        managedBeanCombo = new javax.swing.JComboBox();
        readOnlyCheckBox = new javax.swing.JCheckBox();
        customizeTemplatesLabel = new javax.swing.JLabel();
        hint = new javax.swing.JLabel();
        scanningLabel = new javax.swing.JLabel();
        templatesStyleLabel = new javax.swing.JLabel();
        templatesStyleComboBox = new javax.swing.JComboBox();

        entityBeanLabel.setText(org.openide.util.NbBundle.getMessage(ManagedBeanCustomizer.class, "ManagedBeanCustomizer.entityBeanLabel.text")); // NOI18N

        entityBeanCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                entityBeanComboItemStateChanged(evt);
            }
        });

        managedBeanLabel.setText(org.openide.util.NbBundle.getMessage(ManagedBeanCustomizer.class, "ManagedBeanCustomizer.managedBeanLabel.text")); // NOI18N

        managedBeanCombo.setEditable(true);
        managedBeanCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                managedBeanComboItemStateChanged(evt);
            }
        });

        readOnlyCheckBox.setText(org.openide.util.NbBundle.getMessage(ManagedBeanCustomizer.class, "ManagedBeanCustomizer.readOnlyCheckBox.text")); // NOI18N

        customizeTemplatesLabel.setText(org.openide.util.NbBundle.getMessage(ManagedBeanCustomizer.class, "ManagedBeanCustomizer.customizeTemplatesLabel.text")); // NOI18N
        customizeTemplatesLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                customizeTemplatesLabelMouseClicked(evt);
            }
        });

        hint.setText(org.openide.util.NbBundle.getMessage(ManagedBeanCustomizer.class, "ManagedBeanCustomizer.hint.text")); // NOI18N

        scanningLabel.setFont(new java.awt.Font("Dialog", 2, 12)); // NOI18N
        scanningLabel.setForeground(new java.awt.Color(153, 153, 153));
        scanningLabel.setText(org.openide.util.NbBundle.getMessage(ManagedBeanCustomizer.class, "ManagedBeanCustomizer.scanningLabel.text")); // NOI18N

        templatesStyleLabel.setText(org.openide.util.NbBundle.getMessage(ManagedBeanCustomizer.class, "ManagedBeanCustomizer.templatesStyleLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(scanningLabel)
                        .addGap(0, 350, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(entityBeanLabel)
                            .addComponent(managedBeanLabel)
                            .addComponent(templatesStyleLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(templatesStyleComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(customizeTemplatesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(entityBeanCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(managedBeanCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(readOnlyCheckBox)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(hint, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(entityBeanLabel)
                    .addComponent(entityBeanCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(managedBeanLabel)
                    .addComponent(managedBeanCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hint)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(readOnlyCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 58, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(customizeTemplatesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(templatesStyleLabel)
                    .addComponent(templatesStyleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scanningLabel)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void entityBeanComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_entityBeanComboItemStateChanged
        final String entityClass = (String)entityBeanCombo.getModel().getSelectedItem();

        if (entityClass != null && entityClass.length() > 0) {
            if (collection) {
                hint.setText(NbBundle.getMessage(ManagedBeanCustomizer.class, "ManagedBeanCustomizer.listHint", entityClass));
            } else {
                hint.setText(NbBundle.getMessage(ManagedBeanCustomizer.class, "ManagedBeanCustomizer.instanceHint", entityClass));
            }
            hint.setVisible(true);
            RP.post(new Runnable() {
                @Override
                public void run() {
                    final List<String> props = getPropertyNames(project, entityClass, collection);
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            dummyBean = false;
                            if (props.isEmpty()) {
                                props.add(""); // NOI18N
                                props.add(NbBundle.getMessage(ManagedBeanCustomizer.class, "ManagedBeanCustomizer.notManagedBeanFound")); // NOI18N
                                dummyBean = true;
                            }
                            managedBeanCombo.setModel(new DefaultComboBoxModel(props.toArray(new String[0])));
                        }
                    });
                }
            } );
        } else {
            managedBeanCombo.setModel(new DefaultComboBoxModel());
            hint.setVisible(false);
        }
    }//GEN-LAST:event_entityBeanComboItemStateChanged

    private void managedBeanComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_managedBeanComboItemStateChanged
        final String entityClass = (String)managedBeanCombo.getModel().getSelectedItem();
        if (dummyBean && entityClass.equals(NbBundle.getMessage(ManagedBeanCustomizer.class, "ManagedBeanCustomizer.notManagedBeanFound"))) { // NOI18N
            managedBeanCombo.setSelectedIndex(0);
        }
    }//GEN-LAST:event_managedBeanComboItemStateChanged

    private void customizeTemplatesLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_customizeTemplatesLabelMouseClicked
        if (collection) {
            new OpenTemplateAction(this, NbBundle.getMessage(ManagedBeanCustomizer.class, "ManagedBeanCustomizer.tableTemplate"),
                    JsfTemplateUtils.getTemplatePath(TemplateType.SNIPPETS, getTemplatesStyle(), TABLE_TEMPLATE)).actionPerformed(null);
        } else {
            JPopupMenu menu = new JPopupMenu();
            String viewTemplatePath = JsfTemplateUtils.getTemplatePath(TemplateType.SNIPPETS, getTemplatesStyle(), VIEW_TEMPLATE);
            String editTemplatePath = JsfTemplateUtils.getTemplatePath(TemplateType.SNIPPETS, getTemplatesStyle(), EDIT_TEMPLATE);
            menu.add(new OpenTemplateAction(this, NbBundle.getMessage(ManagedBeanCustomizer.class, "ManagedBeanCustomizer.allTemplates"), viewTemplatePath, editTemplatePath));
            menu.add(new OpenTemplateAction(this, NbBundle.getMessage(ManagedBeanCustomizer.class, "ManagedBeanCustomizer.viewTemplate"), viewTemplatePath));
            menu.add(new OpenTemplateAction(this, NbBundle.getMessage(ManagedBeanCustomizer.class, "ManagedBeanCustomizer.editTemplate"), editTemplatePath));
            menu.show(customizeTemplatesLabel, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_customizeTemplatesLabelMouseClicked

    void setDialog(Dialog dlg, DialogDescriptor dd) {
        this.dialog = dlg;
        this.dialogDescriptor = dd;
        updateValidity("");
    }

    public void cancel() {
        cancelled = true;
        dialog.setVisible(false);
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public String getTemplatesStyle() {
        return ((JsfTemplateUtils.Template) templatesStyleComboBox.getSelectedItem()).getName();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel customizeTemplatesLabel;
    private javax.swing.JComboBox entityBeanCombo;
    private javax.swing.JLabel entityBeanLabel;
    private javax.swing.JLabel hint;
    private javax.swing.JComboBox managedBeanCombo;
    private javax.swing.JLabel managedBeanLabel;
    private javax.swing.JCheckBox readOnlyCheckBox;
    private javax.swing.JLabel scanningLabel;
    private javax.swing.JComboBox templatesStyleComboBox;
    private javax.swing.JLabel templatesStyleLabel;
    // End of variables declaration//GEN-END:variables


    public List<String> getPropertyNames(final Project project, final String entityClass, final boolean collection) {
        final List<String> res = new ArrayList<String>();
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        assert wm != null;
        List<FacesManagedBean> beans = JSFBeanCache.getBeans(project);
        for (FacesManagedBean b : beans) {
            res.addAll(getManagedBeanPropertyNames(project, b.getManagedBeanClass(), entityClass, b.getManagedBeanName(), collection));
        }
        try {
            //check web beans
            if(JsfVersionUtils.forProject(project).isAtLeast(JsfVersion.JSF_3_0)) {
               jakartaMetaModelSupport.getMetaModel().runReadAction(new MetadataModelAction<org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel, Void>() {
                @Override
                public Void run(org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel metadata) throws Exception {
                    for (Element bean : metadata.getNamedElements()) {
                        if (bean == null) {
                            continue;
                        }
                        String beanName = metadata.getName(bean);
                        String className = bean.asType().toString();
                        if ((beanName != null)) {
                            res.addAll(getManagedBeanPropertyNames(project, className, entityClass, beanName, collection));
                        }
                    }
                    return null;
                }
                }); 
            } else {
                metaModelSupport.getMetaModel().runReadAction(new MetadataModelAction<org.netbeans.modules.web.beans.api.model.WebBeansModel, Void>() {
                    @Override
                    public Void run(org.netbeans.modules.web.beans.api.model.WebBeansModel metadata) throws Exception {
                        for (Element bean : metadata.getNamedElements()) {
                            if (bean == null) {
                                continue;
                            }
                            String beanName = metadata.getName(bean);
                            String className = bean.asType().toString();
                            if ((beanName != null)) {
                                res.addAll(getManagedBeanPropertyNames(project, className, entityClass, beanName, collection));
                            }
                        }
                        return null;
                    }
                });
            }
        } catch (MetadataModelException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return res;
    }

    public List<String> getManagedBeanPropertyNames(Project project,
            final String managedBean, final String entityClassName,
            final String managedBeanName, final boolean collection) {
        final List<String> res = new ArrayList<String>();

        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sourceGroups.length == 0) {
            return res;
        }
        FileObject root = sourceGroups[0].getRootFolder();
        ClasspathInfo classpathInfo = ClasspathInfo.create(
                ClassPathSupport.createProxyClassPath(ClassPath.getClassPath(root, ClassPath.BOOT)),
                ClassPathSupport.createProxyClassPath(ClassPath.getClassPath(root, ClassPath.COMPILE)),
                ClassPathSupport.createProxyClassPath(ClassPath.getClassPath(root, ClassPath.SOURCE)));
        JavaSource js = JavaSource.create(classpathInfo);
        try {
            Future<Void> searchingTask = js.runWhenScanFinished(
                    new SearchTask(managedBean, entityClassName, managedBeanName, res, false),
                    true);
            if (searchingTask.isDone()) {
                return res;
            }
            js.runUserActionTask(new SearchTask(managedBean, entityClassName, managedBeanName, res, true), true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return res;
    }

    private void setScanningLabelVisible(final boolean visible) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                scanningLabel.setVisible(visible);
            }
        });
    }

    private class SearchTask implements Task<CompilationController> {

        private final String managedBean;
        private final String entityClassName;
        private final String managedBeanName;
        private final List<String> result;
        private boolean scanning;

        public SearchTask(String managedBean, String entityClassName, String managedBeanName,
                List<String> result, boolean scanning) {
            this.managedBean = managedBean;
            this.entityClassName = entityClassName;
            this.managedBeanName = managedBeanName;
            this.scanning = scanning;
            this.result = result;
        }

        @Override
        public void run(CompilationController cc) throws Exception {
            cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            TypeElement entityClassTypeElement = cc.getElements().getTypeElement(entityClassName);
            TypeElement beanTypeElement = cc.getElements().getTypeElement(managedBean);
            if (entityClassTypeElement != null && beanTypeElement != null) {
                TypeMirror entityClassType = entityClassTypeElement.asType();
                for (ExecutableElement el : ElementFilter.methodsIn(beanTypeElement.getEnclosedElements())) {
                    if (el.getParameters().size() > 0) {
                        continue;
                    }
                    if (el.getReturnType().getKind() != TypeKind.DECLARED) {
                        continue;
                    }
                    DeclaredType declaredReturnType = (DeclaredType)el.getReturnType();
                    Element returnElement = declaredReturnType.asElement();
                    TypeElement returnTypeElement;
                    if ((returnElement.getKind() == ElementKind.CLASS ||
                        returnElement.getKind() == ElementKind.INTERFACE) &&
                        (returnElement instanceof TypeElement) ) {
                        returnTypeElement = (TypeElement)returnElement;
                    } else {
                        continue;
                    }
                    TypeMirror returnTypeMirror;
                    TypeElement returnCollectionTypeElement = null;
                    if (declaredReturnType.getTypeArguments().size() > 0) {
                        returnCollectionTypeElement = returnTypeElement;
                        returnTypeMirror = declaredReturnType.getTypeArguments().get(0);
                    } else {
                        returnTypeMirror = returnTypeElement.asType();
                    }
                    if (collection) {
                        if (returnCollectionTypeElement == null) {
                            continue;
                        }
                        if (isCollection(returnCollectionTypeElement) &&
                                cc.getTypes().isAssignable(returnTypeMirror, entityClassType)) {
                            result.add(managedBeanName+"."+JpaControllerUtil.getPropNameFromMethod(el.getSimpleName().toString()));
                        }
                    } else {
                        if (entityClassType.equals(returnTypeMirror)) {
                            result.add(managedBeanName+"."+JpaControllerUtil.getPropNameFromMethod(el.getSimpleName().toString()));
                        }
                    }
                }
            }
            setScanningLabelVisible(SourceUtils.isScanInProgress());
        }

        private boolean isCollection(TypeElement type) {
            String collectionTypeClass = type.getQualifiedName().toString();
            Class collectionTypeAsClass = null;
            try {
                collectionTypeAsClass = Class.forName(collectionTypeClass);
            } catch (ClassNotFoundException cfne) {
                //let collectionTypeAsClass be null
            }
            return (collectionTypeAsClass != null && Collection.class.isAssignableFrom(collectionTypeAsClass));
        }
    }

}
