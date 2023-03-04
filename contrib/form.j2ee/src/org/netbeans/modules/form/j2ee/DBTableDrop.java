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
import org.netbeans.modules.form.RADVisualContainer;
import org.netbeans.modules.form.assistant.AssistantMessages;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.project.ClassPathUtils;
import org.netbeans.modules.form.project.ClassSource;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.nbform.project.ClassSourceResolver;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Result of database table DnD.
 *
 * @author Jan Stola
 */
public class DBTableDrop extends DBConnectionDrop {
    /** Dropped table. */
    private DatabaseMetaDataTransfer.Table table;

    /**
     * Creates new <code>DBTableDrop</code>.
     *
     * @param model form model.
     * @param table dropped table.
     */
    public DBTableDrop(FormModel model, DatabaseMetaDataTransfer.Table table) {
        super(model, null);
        this.table = table;
    }

    /**
     * Returns <code>JTable</code> palette item.
     *
     * @param dtde corresponding drop target drag event.
     * @return <code>JTable</code> palette item.
     */
    @Override
    public PaletteItem getPaletteItem(DropTargetDragEvent dtde) {
        if (!assistantInitialized) {
            initAssistant();
        }
        PaletteItem pItem;
        if (!J2EEUtils.hasPrimaryKey(table.getDatabaseConnection(), table.getTableName())) {
            FormEditor.getAssistantModel(model).setContext("tableWithoutPK"); // NOI18N
            return null;
        }
        if (FormJavaSource.isInDefaultPackage(model)) {
            // 97982: default package
            FormEditor.getAssistantModel(model).setContext("tableDefaultPackage"); // NOI18N
            return null;
        }
        setBindingOnly(dtde.getDropAction() == DnDConstants.ACTION_MOVE);
        if (isBindingOnly()) {
            FormEditor.getAssistantModel(model).setContext("tableDropBinding", "tableDropComponent"); // NOI18N
            pItem = new PaletteItem(new ClassSource("javax.persistence.EntityManager", // NOI18N
                        new ClassSourceResolver.LibraryEntry(LibraryManager.getDefault().getLibrary("eclipselink"))), // NOI18N
                        null);
            pItem.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/j2ee/resources/binding.gif", false).getImage()); // NOI18N
        } else {
            pItem = new PaletteItem(new ClassSource("javax.swing.JTable"), null); // NOI18N
        }
        return pItem;
    }

    /**
     * Registers assistant messages related to DB table DnD.
     */
    private void initAssistant() {
        ResourceBundle bundle = NbBundle.getBundle(DBColumnDrop.class);
        String dropBindingMsg = bundle.getString("MSG_TableDropBinding"); // NOI18N
        String dropComponentMsg = bundle.getString("MSG_TableDropComponent"); // NOI18N
        String tableWithoutPKMsg = bundle.getString("MSG_TableWithoutPK"); // NOI18N
        String tableDefaultPackageMsg = bundle.getString("MSG_TableDefaultPackage"); // NOI18N
        AssistantMessages messages = AssistantMessages.getDefault();
        messages.setMessages("tableDropBinding", dropBindingMsg); // NOI18N
        messages.setMessages("tableDropComponent", dropComponentMsg); // NOI18N
        messages.setMessages("tableWithoutPK", tableWithoutPKMsg); // NOI18N
        messages.setMessages("tableDefaultPackage", tableDefaultPackageMsg);
        assistantInitialized = true;
    }

    /**
     * Post-processing after placement of the dragged table.
     *
     * @param componentId ID of the table (in fact, it is ID of the inserted
     * component e.g. the enclosing scroll pane).
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
            PersistenceUnit unit = J2EEUtils.initPersistenceUnit(persistenceXML, table.getDatabaseConnection());

            // Initializes project's classpath
            J2EEUtils.updateProjectForUnit(formFile, unit, table.getJDBCDriver());

            // Obtain description of entity mappings
            PersistenceScope scope = PersistenceScope.getPersistenceScope(formFile);
            MetadataModel<EntityMappingsMetadata> mappings = scope.getEntityMappingsModel(unit.getName());
            
            // Find entity that corresponds to the dragged table
            String[] entityInfo = J2EEUtils.findEntity(mappings, table.getTableName());
            
            // Create a new entity (if there isn't one that corresponds to the dragged table)
            if (entityInfo == null) {
                // Generates a Java class for the entity
                J2EEUtils.createEntity(formFile.getParent(), scope, unit, table.getDatabaseConnection(), table.getTableName(), null);

                mappings = scope.getEntityMappingsModel(unit.getName());
                entityInfo = J2EEUtils.findEntity(mappings, table.getTableName());
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

            RADComponent queryBean = createQueryBean(model, entityManager, entityInfo[0]);

            // Create a meta-component for the collection of entities
            RADComponent resultList = createResultListBean(model, queryBean, entityInfo);

            Class beanClass = javax.swing.JTable.class;
            if (isBindingOnly()) {
                if (droppedOverId == null) return;
                RADComponent comp = model.getMetaComponent(droppedOverId);
                if (javax.swing.JScrollPane.class.isAssignableFrom(comp.getBeanClass())) {
                    if (comp instanceof RADVisualContainer) {
                        RADVisualContainer cont = (RADVisualContainer)comp;
                        if (cont.getSubComponents().length > 0) {
                            comp = cont.getSubComponent(0);
                            droppedOverId = comp.getId();
                        }
                    }
                }
                // PENDING subclasses
                beanClass = comp.getBeanClass();
                if (!javax.swing.JTable.class.equals(beanClass)
                    && !javax.swing.JList.class.equals(beanClass)
                    && !javax.swing.JComboBox.class.equals(beanClass)) return;
            }
            if (beanClass.equals(javax.swing.JTable.class)) {
                // Bind the table component to the result list
                bindTableComponent(isBindingOnly() ? droppedOverId : componentId,
                    resultList, mappings, entityInfo);
            } else {
                // JList and JComboBox
                bindListComponent(droppedOverId, resultList);
            }
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
        }
    }

    /**
     * Creates query RAD component.
     *
     * @param model form model.
     * @param entityManager entity manager used for creation of the query.
     * @param entityName name of the entity whose instances will be in the result of the query.
     * @throws Exception when something goes wrong.
     * @return query RAD component.
     */
    public static RADComponent createQueryBean(FormModel model, RADComponent entityManager, String entityName) throws Exception {
        RADComponent query = new RADComponent();
        FileObject formFile = FormEditor.getFormDataObject(model).getFormFile();
        Class<?> queryClass = ClassPathUtils.loadClass("javax.persistence.Query", formFile); // NOI18N

        query.initialize(model);
        query.initInstance(queryClass);

        char c = entityName.toLowerCase().charAt(0);
        String q = "SELECT " + c + " FROM " + entityName + " " + c;  // NOI18N
        query.getPropertyByName("query").setValue(q); // NOI18N
        query.getPropertyByName("entityManager").setValue(entityManager); // NOI18N
        query.setStoredName(c + entityName.substring(1) + "Query"); // NOI18N

        model.addComponent(query, null, true);
        return query;
    }

    /**
     * Creates query result list RAD component.
     *
     * @param model form model.
     * @param query query used to obtain the list of entities.
     * @param entityInfo information about the entity whose instances will be in the result list.
     * @throws Exception when something goes wrong.
     * @return query result list RAD component.
     */
    public static RADComponent createResultListBean(FormModel model, RADComponent query, String[] entityInfo) throws Exception {
        // Create a meta-component for the collection of entities
        RADComponent resultList = new RADComponent();
        resultList.setAuxValue("JavaCodeGenerator_TypeParameters", '<' + entityInfo[1] + '>'); // NOI18N
        resultList.initialize(model);
        resultList.initInstance(java.util.List.class);

        char c = entityInfo[0].toLowerCase().charAt(0);
        resultList.getPropertyByName("query").setValue(query); // NOI18N
        resultList.setStoredName(c + entityInfo[0].substring(1) + "List"); // NOI18N

        model.addComponent(resultList, null, true);
        return resultList;
    }

    /**
     * Binds table component to the result list.
     *
     * @param tableID ID of the table to bind.
     * @param resultList RAD component representing the result list to bind to table.
     * @param scope persistence scope.
     * @param entity persistence entity.
     */
    private void bindTableComponent(String tableID, RADComponent resultList, MetadataModel<EntityMappingsMetadata> mappings, String[] entityInfo) throws Exception {
        RADComponent table = model.getMetaComponent(tableID);
        if (javax.swing.JScrollPane.class.isAssignableFrom(table.getBeanClass())) {
            table = ((RADVisualContainer)table).getSubComponent(0);
        }

        // Bind the elements property
        BindingProperty prop = table.getBindingProperty("elements"); // NOI18N
        MetaBinding binding = new MetaBinding(resultList, null, table, "elements"); // NOI18N

        List<String> propertyNames = J2EEUtils.propertiesForColumns(mappings, entityInfo[0], null);
        FileObject formFile = FormEditor.getFormDataObject(model).getPrimaryFile();
        List<String> propertyTypes = J2EEUtils.typesOfProperties(formFile, entityInfo[1], propertyNames);
        Iterator<String> typeIter = propertyTypes.iterator();
        for (String column : propertyNames) {
            BindingDesignSupport bindingSupport = FormEditor.getBindingSupport(model);
            MetaBinding subBinding = binding.addSubBinding(bindingSupport.elWrap(column), null);
            String clazz = typeIter.next();
            if (clazz != null) {
                subBinding.setParameter(MetaBinding.TABLE_COLUMN_CLASS_PARAMETER, clazz);
            }
        }

        prop.setValue(binding);
    }

    /**
     * Binds list or combobox component to the result list.
     *
     * @param listID ID of the component to bind.
     * @param resultList RAD component representing the result list to bind to table.
     */
    private void bindListComponent(String listID, RADComponent resultList) throws Exception {
        RADComponent list = model.getMetaComponent(listID);
        if (javax.swing.JScrollPane.class.isAssignableFrom(list.getBeanClass())) {
            list = ((RADVisualContainer)list).getSubComponent(0);
        }

        // Bind the elements property
        BindingProperty prop = list.getBindingProperty("elements"); // NOI18N
        MetaBinding binding = new MetaBinding(resultList, null, list, "elements"); // NOI18N
        // should we create some display expression (e.g. primary key)?
        prop.setValue(binding);
    }
        
}
