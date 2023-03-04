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

import java.awt.dnd.DropTargetDragEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseMetaDataTransfer;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.NewComponentDrop;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.project.ClassSource;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.nbform.project.ClassSourceResolver;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;

/**
 * Result of DB connection drop.
 *
 * @author Jan Stola
 */
public class DBConnectionDrop implements NewComponentDrop {
    /** Dropped connection. */
    private DatabaseMetaDataTransfer.Connection connection;
    /** Determines whether to drag just the binding or also a component. */
    private boolean bindingOnly;
    /** Form model. */
    FormModel model;
    /** Enclosing project. */
    Project project;
    /** Determines whether the assistant context messages were initialized. */
    boolean assistantInitialized;

    /**
     * Creates new <code>DBConnectionDrop</code>.
     *
     * @param model form model.
     * @param connection dropped connection.
     */
    public DBConnectionDrop(FormModel model, DatabaseMetaDataTransfer.Connection connection) {
        this.model = model;
        this.connection = connection;
    }

    /**
     * Returns <code>EntityManager</code> palette item.
     *
     * @param dtde corresponding drop target drag event.
     * @return <code>EntityManager</code> palette item.
     */
    @Override
    public PaletteItem getPaletteItem(DropTargetDragEvent dtde) {
        PaletteItem pItem = new PaletteItem(new ClassSource("javax.persistence.EntityManager", // NOI18N
                new ClassSourceResolver.LibraryEntry(LibraryManager.getDefault().getLibrary("eclipselink"))), // NOI18N
                null);
        pItem.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/j2ee/resources/EntityManager.png", false).getImage()); // NOI18N
        return pItem;
    }

    /**
     * Post-processing after placement of the dragged connection.
     *
     * @param componentId ID of the corresponding component.
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
            PersistenceUnit unit = J2EEUtils.initPersistenceUnit(persistenceXML, connection.getDatabaseConnection());

            // Initializes project's classpath
            J2EEUtils.updateProjectForUnit(formFile, unit, connection.getJDBCDriver());

            RADComponent entityManager = model.getMetaComponent(componentId);
            entityManager.getPropertyByName("persistenceUnit").setValue(unit.getName()); // NOI18N
            J2EEUtils.renameComponent(entityManager, true, unit.getName() + "EntityManager", "entityManager"); // NOI18N
        } catch (IOException ioex) {
            Logger.getLogger(DBConnectionDrop.class.getName()).log(Level.INFO, null, ioex);
        } catch (InvalidPersistenceXmlException ipxex) {
            Logger.getLogger(DBConnectionDrop.class.getName()).log(Level.INFO, null, ipxex);
        } catch (IllegalAccessException iaex) {
            Logger.getLogger(DBConnectionDrop.class.getName()).log(Level.INFO, null, iaex);
        } catch (InvocationTargetException itex) {
            Logger.getLogger(DBConnectionDrop.class.getName()).log(Level.INFO, null, itex);
        }
    }

    /**
     * Ensures existence of an entity manager "bean" that corresponds to the given persistence unit.
     *
     * @param unit persistence unit.
     * @return RAD component encapsulating entity manager corresponding to the given persistence unit.
     * @throws Exception when something goes wrong.
     */
    protected RADComponent initEntityManagerBean(PersistenceUnit unit) throws Exception {
        String puName = unit.getName();
        RADComponent entityManager = J2EEUtils.findEntityManager(model, puName);
        if (entityManager == null) {
            entityManager = J2EEUtils.createEntityManager(model, puName);
        }
        return entityManager;
    }

    /**
     * Sets <code>bindingOnly</code> property.
     *
     * @param bindingOnly new value of the property.
     */
    void setBindingOnly(boolean bindingOnly) {
        this.bindingOnly = bindingOnly;
    }

    /**
     * Returns value of <code>bindingOnly</code> property.
     *
     * @return value of <code>bindingOnly</code> property.
     */
    boolean isBindingOnly() {
        return bindingOnly;
    }

}
