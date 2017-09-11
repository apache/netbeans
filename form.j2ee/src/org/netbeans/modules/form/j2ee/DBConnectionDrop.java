/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
