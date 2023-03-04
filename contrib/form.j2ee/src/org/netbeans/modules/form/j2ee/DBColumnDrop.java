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
package org.netbeans.modules.form.j2ee;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import org.netbeans.api.db.explorer.DatabaseMetaDataTransfer;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.form.BindingDesignSupport;
import org.netbeans.modules.form.BindingProperty;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.FormJavaSource;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.MetaBinding;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.assistant.AssistantMessages;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.project.ClassPathUtils;
import org.netbeans.modules.form.project.ClassSource;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.nbform.project.ClassSourceResolver;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Result of database column DnD.
 *
 * @author Jan Stola
 */
public class DBColumnDrop extends DBConnectionDrop {
    /** Dropped column. */
    private DatabaseMetaDataTransfer.Column column;
    
    /**
     * Creates new <code>DBColumnDrop</code>.
     *
     * @param model form model.
     * @param column dropped column.
     */
    public DBColumnDrop(FormModel model, DatabaseMetaDataTransfer.Column column) {
        super(model, null);
        this.column = column;
    }

    /**
     * Returns <code>JTextField</code> palette item.
     *
     * @param dtde corresponding drop target drag event.
     * @return <code>JTextField</code> palette item.
     */
    @Override
    public PaletteItem getPaletteItem(DropTargetDragEvent dtde) {
        PaletteItem pItem;
        if (!assistantInitialized) {
            initAssistant();
        }
        if (!J2EEUtils.hasPrimaryKey(column.getDatabaseConnection(), column.getTableName())) {
            FormEditor.getAssistantModel(model).setContext("tableWithoutPK"); // NOI18N
            return null;
        }
        if (FormJavaSource.isInDefaultPackage(model)) {
            // 97982: default package
            FormEditor.getAssistantModel(model).setContext("columnDefaultPackage"); // NOI18N
            return null;
        }
        setBindingOnly(dtde.getDropAction() == DnDConstants.ACTION_MOVE);
        if (isBindingOnly()) {
            FormEditor.getAssistantModel(model).setContext("columnDropBinding", "columnDropComponent"); // NOI18N
            pItem = new PaletteItem(new ClassSource("javax.persistence.EntityManager", // NOI18N
                        new ClassSourceResolver.LibraryEntry(LibraryManager.getDefault().getLibrary("eclipselink"))), // NOI18N
                        null);
            pItem.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/j2ee/resources/binding.gif", false).getImage()); // NOI18N
        } else {
            pItem = new PaletteItem(new ClassSource("javax.swing.JTextField"), null); // NOI18N
        }
        return pItem;
    }

    /**
     * Registers assistant messages related to DB column DnD.
     */
    private void initAssistant() {
        ResourceBundle bundle = NbBundle.getBundle(DBColumnDrop.class);
        String dropBindingMsg = bundle.getString("MSG_ColumnDropBinding"); // NOI18N
        String dropComponentMsg = bundle.getString("MSG_ColumnDropComponent"); // NOI18N
        String tableWithoutPKMsg = bundle.getString("MSG_TableWithoutPK"); // NOI18N
        String columnDefaultPackageMsg = bundle.getString("MSG_ColumnDefaultPackage"); // NOI18N
        AssistantMessages messages = AssistantMessages.getDefault();
        messages.setMessages("columnDropBinding", dropBindingMsg); // NOI18N
        messages.setMessages("columnDropComponent", dropComponentMsg); // NOI18N
        messages.setMessages("tableWithoutPK", tableWithoutPKMsg); // NOI18N
        messages.setMessages("columnDefaultPackage", columnDefaultPackageMsg);
        assistantInitialized = true;
    }

    /**
     * Post-processing after placement of the dragged column.
     *
     * @param componentId ID of the corresponding UI component.
     * @param droppedOverId ID of a component the new component has been dropped over.
     */
    @Override
    public void componentAdded(String componentId, String droppedOverId) {
        try {
            FileObject formFile = FormEditor.getFormDataObject(model).getFormFile();
            project = FileOwnerQuery.getOwner(formFile);

            // Make sure persistence.xml file exists
            FileObject persistenceXML = J2EEUtils.getPersistenceXML(project, true);
            
            // Initializes persistence unit and persistence descriptor
            PersistenceUnit unit = J2EEUtils.initPersistenceUnit(persistenceXML, column.getDatabaseConnection());

            // Initializes project's classpath
            J2EEUtils.updateProjectForUnit(formFile, unit, column.getJDBCDriver());

            // Obtain description of entity mappings
            PersistenceScope scope = PersistenceScope.getPersistenceScope(formFile);
            MetadataModel<EntityMappingsMetadata> mappings = scope.getEntityMappingsModel(unit.getName());

            // Find entity that corresponds to the dragged table
            String[] entityInfo = J2EEUtils.findEntity(mappings, column.getTableName());
            
            // Create a new entity (if there isn't one that corresponds to the dragged table)
            if (entityInfo == null) {
                // Generates a Java class for the entity
                J2EEUtils.createEntity(formFile.getParent(), scope, unit, column.getDatabaseConnection(), column.getTableName(), null);

                entityInfo = J2EEUtils.findEntity(mappings, column.getTableName());
            } else {
                // Add the entity into the persistence unit if it is not there already
                J2EEUtils.addEntityToUnit(entityInfo[1], unit, project);
            }
            
            J2EEUtils.makeEntityObservable(formFile, entityInfo, mappings);

            // Find (or create) entity manager "bean" for the persistence unit
            RADComponent entityManager;
            if (isBindingOnly()) {
                String unitName = unit.getName();
                entityManager = J2EEUtils.findEntityManager(model, unitName);
                if (entityManager == null) {
                    entityManager = model.getMetaComponent(componentId);
                    entityManager.getPropertyByName("persistenceUnit").setValue(unitName); // NOI18N
                    J2EEUtils.renameComponent(entityManager, true, unitName + "EntityManager", "entityManager"); // NOI18N
                } else {
                    // The entity manager was already there => remove the dragged one
                    model.removeComponent(model.getMetaComponent(componentId), true);
                }
            } else {
                entityManager = initEntityManagerBean(unit);
            }

            RADComponent control = null;
            String controlProperty = null;
            if (isBindingOnly()) {
                if (droppedOverId == null) return;
                control = model.getMetaComponent(droppedOverId);
                Class controlClass = control.getBeanClass();
                controlProperty = controlProperty(controlClass);
                if (controlProperty == null) {
                    return;
                }
            } else {
                control = model.getMetaComponent(componentId);
                controlProperty = "text"; // NOI18N
                control.getPropertyByName("columns").setValue(15); // NOI18N
            }
            
            List<String> l = Collections.singletonList(column.getColumnName());
            l = J2EEUtils.propertiesForColumns(mappings, entityInfo[0], l);
            if (l.isEmpty()) {
                // There is no property corresponding to the dragged column
                return;
            }
            String sourcePath = l.get(0);
            
            RADComponent metaTable = null;
            for (RADComponent component : model.getAllComponents()) {
                if ("java.util.List".equals(component.getBeanClass().getName())) { // NOI18N
                    Object value = component.getSyntheticProperty("typeParameters").getValue();
                    if (value instanceof String) {
                        int index = ((String)value).indexOf(entityInfo[1] + '>'); // PENDING improve this check
                        if (index != -1) {
                            for (RADComponent tableCand : model.getAllComponents()) {
                                if (javax.swing.JTable.class.isAssignableFrom(tableCand.getBeanClass())) {
                                    BindingProperty prop = tableCand.getBindingProperty("elements"); // NOI18N
                                    if (prop != null) {
                                        MetaBinding binding = prop.getValue();
                                        if (binding.getSource() == component) {
                                            metaTable = tableCand;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            BindingProperty prop;
            MetaBinding binding;
            BindingDesignSupport bindingSupport = FormEditor.getBindingSupport(model);
            if (metaTable == null) {
                RADComponent metaEntity = null;
                Class<?> entityClass = ClassPathUtils.loadClass(entityInfo[1], formFile); // NOI18N
                for (RADComponent component : model.getAllComponents()) {
                    if (entityClass.equals(component.getBeanClass())) {
                        metaEntity = component;
                    }
                }

                if (metaEntity == null) {
                    RADComponent metaQuery = new RADComponent();
                    Class<?> queryClass = ClassPathUtils.loadClass("javax.persistence.Query", formFile); // NOI18N
                    metaQuery.initialize(model);
                    metaQuery.initInstance(queryClass);
                    metaQuery.getPropertyByName("entityManager").setValue(entityManager); // NOI18N
                    char c = entityInfo[0].toLowerCase().charAt(0);
                    String query = "SELECT " + c + " FROM " + entityInfo[0] + " " + c; // NOI18N
                    metaQuery.getPropertyByName("query").setValue(query); // NOI18N
                    metaQuery.getPropertyByName("maxResults").setValue(1); // NOI18N
                    metaQuery.setStoredName(c + entityInfo[0].substring(1) + "Query"); // NOI18N
                    model.addComponent(metaQuery, null, true);

                    metaEntity = new RADComponent();
                    metaEntity.initialize(model);
                    metaEntity.initInstance(entityClass);
                    metaEntity.getPropertyByName("query").setValue(metaQuery); // NOI18N
                    model.addComponent(metaEntity, null, true);
                }

                prop = control.getBindingProperty(controlProperty); // NOI18N
                binding = new MetaBinding(metaEntity, bindingSupport.elWrap(sourcePath), control, controlProperty); // NOI18N
            } else {
                prop = control.getBindingProperty("enabled"); // NOI18N
                binding = new MetaBinding(metaTable, bindingSupport.elWrap("selectedElement != null"), control, "enabled"); // NOI18N
                prop.setValue(binding);
                prop = control.getBindingProperty(controlProperty);
                binding = new MetaBinding(metaTable, bindingSupport.elWrap("selectedElement." + sourcePath), control, controlProperty); // NOI18N
                if (controlProperty.equals("text") && JTextComponent.class.isAssignableFrom(control.getBeanClass())) { // NOI18N
                    binding.setIncompletePathValueSpecified(true);
                    prop.getSubProperty(BindingProperty.PROP_INCOMPLETE_VALUE).setValue(null);
                }
            }
            prop.setValue(binding);
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
        }
    }

    /**
     * Determines the property that should be bound.
     * 
     * @param controlClass class of the control that will be bound.
     * @return the name of the property that should be bound.
     */
    private static String controlProperty(Class controlClass) {
        String controlProperty = null;
        if (javax.swing.text.JTextComponent.class.isAssignableFrom(controlClass)) {
            controlProperty = "text"; // NOI18N
        } else if (javax.swing.JToggleButton.class.isAssignableFrom(controlClass)) {
            controlProperty = "selected"; // NOI18N
        } else if (javax.swing.JSlider.class.isAssignableFrom(controlClass)) {
            controlProperty = "value"; // NOI18N
        }
        return controlProperty;
    }

}
