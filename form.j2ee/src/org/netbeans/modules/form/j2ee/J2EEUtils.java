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

import com.sun.source.tree.*;
import java.awt.Component;
import java.awt.EventQueue;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.form.FormDataObject;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormProperty;
import org.netbeans.modules.form.FormUtils;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.codestructure.CodeStructure;
import org.netbeans.modules.form.project.ClassPathUtils;
import org.netbeans.modules.form.project.ClassSource;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.entity.generator.EntitiesFromDBGenerator;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Attributes;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Basic;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Id;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.JoinColumn;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.ManyToOne;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceMetadata;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.dd.common.Property;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation.CollectionType;
import org.netbeans.modules.j2ee.persistence.entitygenerator.EntityRelation.FetchType;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceLocationProvider;
import org.netbeans.modules.nbform.project.ClassSourceResolver;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 * Utility methods.
 *
 * @author Jan Stola
 */
public class J2EEUtils {
    /** Determines whether related entities should be generated.  */
    public static final boolean TABLE_CLOSURE = true;
    
    // Utility class - no need to instantiate
    private J2EEUtils() {
    }

    /**
     * Returns persistence unit that corresponds to given database URL.
     *
     * @param persistence persistence context.
     * @param dbURL database URL.
     * @return persistence unit that corresponds to given database URL.
     */
    public static PersistenceUnit findPersistenceUnit(Persistence persistence, String dbURL) {
        for (PersistenceUnit unit : persistence.getPersistenceUnit()) {
            Provider provider = ProviderUtil.getProvider(unit);
            String unitURL = ProviderUtil.getProperty(unit, provider.getJdbcUrl()).getValue();
            if (dbURL.equals(unitURL)) {
                return unit;
            }
        }
        return null;
    }

    /**
     * Creates persistence unit according to given DB <code>connection</code>.
     *
     * @param project target project for the new persistence unit.
     * @param connection database connection.
     * @throws IOException when something goes wrong.
     * @throws InvalidPersistenceXmlException
     * @return persistence unit that corresponds to given DB <code>connection</code>.
     */
    public static PersistenceUnit createPersistenceUnit(Project project, DatabaseConnection connection) throws IOException, InvalidPersistenceXmlException {
        FileObject persistenceXML = ProviderUtil.getDDFile(project);
        Persistence persistence = PersistenceMetadata.getDefault().getRoot(persistenceXML);
        String dbURL = connection.getDatabaseURL();
        
        // Determine name of the PU
        String dbName = dbURL.substring(dbURL.lastIndexOf('/')+1);
        String puName = dbName + "PU"; // NOI18N
        PersistenceUnit unit = findPersistenceUnit(persistence, puName);
        int count = 0;
        while (unit != null) {
           count++;
           puName = dbName + "PU" + count; // NOI18N
           unit = findPersistenceUnit(persistence, puName);
        }

        // Determine the provider
        Provider provider;
        if (persistence.getPersistenceUnit().length > 0) {
            // Use the same provider as the existing persistence unit
            provider = ProviderUtil.getProvider(persistence.getPersistenceUnit(0));
        } else {
            // The first persistence unit - use EclipseLink provider
            // (it is delivered as a part of NetBeans J2EE support)
            provider = ProviderUtil.ECLIPSELINK_PROVIDER;
        }

        unit = ProviderUtil.buildPersistenceUnit(puName, provider, connection, persistence.getVersion());
        unit.setTransactionType("RESOURCE_LOCAL"); // NOI18N

        // TopLink(Eclipselink may too, TODO: verify)/Derby combination doesn't like empty username and password,
        // but we can use dummy (app/app) values in this case, see issue 121427.
        if ((nullOrEmpty(connection.getUser()) || nullOrEmpty(connection.getPassword()))
                && (ProviderUtil.TOPLINK_PROVIDER1_0.equals(provider) || ProviderUtil.ECLIPSELINK_PROVIDER.equals(provider) || ProviderUtil.ECLIPSELINK_PROVIDER2_0.equals(provider))
                && connection.getDriverClass().startsWith("org.apache.derby.jdbc.")) { // NOI18N
            String userPropName = provider.getJdbcUsername();
            String passwdPropName = provider.getJdbcPassword();
            for (Property prop : unit.getProperties().getProperty2()) {
                String propName = prop.getName();
                if ((userPropName.equals(propName) || passwdPropName.equals(propName)) && nullOrEmpty(prop.getValue())) {
                    prop.setValue("app"); // NOI18N
                }
            }
        }

        // Java Embedded DB, see issue 121391
        if ("org.apache.derby.jdbc.EmbeddedDriver".equals(connection.getDriverClass())) { // NOI18N
            // Make sure tables are created if they do not exist
            ProviderUtil.setTableGeneration(unit, Provider.TABLE_GENERATION_CREATE, provider);
            // Make sure the DB is created if it does not exist
            if (!dbURL.contains(";create=true")) { // NOI18N
                if (!dbURL.endsWith(";")) { // NOI18N
                    dbURL += ";"; // NOI18N
                }
                dbURL += "create=true"; // NOI18N
                Property prop = ProviderUtil.getProperty(unit, provider.getJdbcUrl());
                if (prop != null) {
                    prop.setValue(dbURL);
                }
            }
        }
        ProviderUtil.addPersistenceUnit(unit, project);

        return unit;
    }

    public static boolean nullOrEmpty(String s) {
        return (s == null) || "".equals(s); // NOI18N
    }

    /**
     * Returns names of persistence units in the specified project.
     *
     * @param project project to scan for persistence units.
     * @return names of persistence units in the specified project.
     */
    public static String[] getPersistenceUnitNames(Project project) {
        FileObject persistenceXML;
        try {
            persistenceXML = J2EEUtils.getPersistenceXML(project, false);
        } catch (InvalidPersistenceXmlException ipxex) {
            Logger.getLogger(J2EEUtils.class.getName()).log(Level.INFO, ipxex.getMessage(), ipxex);
            return new String[0];
        }
        if (persistenceXML == null) return new String[0];
        Persistence persistence;
        try {
             persistence = PersistenceMetadata.getDefault().getRoot(persistenceXML);
        } catch (IOException ioex) {
            Logger.getLogger(J2EEUtils.class.getName()).log(Level.INFO, ioex.getMessage(), ioex);
            return new String[0];
        }
        PersistenceUnit[] unit = persistence.getPersistenceUnit();
        String[] names = new String[unit.length];
        for (int i=0; i<unit.length; i++) {
            names[i] = unit[i].getName();
        }
        return names;
    }

    public static void addEntityToUnit(String entityClass, PersistenceUnit unit, Project project) {
        boolean added = false;
        for (String clazz : unit.getClass2()) {
            if (entityClass.equals(clazz)) {
                added = true;
                break;
            }
        }
        if (!added) {
            try {
                ProviderUtil.addManagedClass(unit, entityClass, ProviderUtil.getPUDataObject(project));
            } catch (InvalidPersistenceXmlException ipxex) {
                Logger.getLogger(J2EEUtils.class.getName()).log(Level.INFO, ipxex.getMessage(), ipxex);
            }
        }
    }

    /**
     * Updates project classpath with the TopLink library.
     *
     * @param fileInProject file in the project whose classpath should be updated.
     * @return <code>true</code> if the classpath has been updated,
     * returns <code>false</code> otherwise.
     */
    public static boolean updateProjectForTopLink(FileObject fileInProject) {
        try {
            ClassPath classPath = ClassPath.getClassPath(fileInProject, ClassPath.EXECUTE);
            FileObject fob = classPath.findResource("oracle/toplink/essentials/ejb/cmp3/EntityManagerFactoryProvider.class"); // NOI18N
            if(fob == null){
                fob =classPath.findResource("oracle/toplink/essentials/PersistenceProvider.class");//alternative
            }
            if (fob == null) {
                Library lib = LibraryManager.getDefault().getLibrary("toplink");
                if(lib !=null ){
                    ClassSource cs = new ClassSource("", // class name is not needed // NOI18N
                            new ClassSourceResolver.LibraryEntry(lib)); // NOI18N
                    return ClassPathUtils.updateProject(fileInProject, cs);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(J2EEUtils.class.getName()).log(Level.INFO, ex.getMessage(), ex);
        }
        return false;
    }
    /**
     * Updates project classpath with the EclipseLink library.
     *
     * @param fileInProject file in the project whose classpath should be updated.
     * @return <code>true</code> if the classpath has been updated,
     * returns <code>false</code> otherwise.
     */
    public static boolean updateProjectForEclipseLink(FileObject fileInProject) {
        try {
            ClassPath classPath = ClassPath.getClassPath(fileInProject, ClassPath.EXECUTE);
            FileObject fob = classPath.findResource("org/eclipse/persistence/jpa/PersistenceProvider.class"); // NOI18N
            if (fob == null) {
                ClassSource cs = new ClassSource("", // class name is not needed // NOI18N
                        new ClassSourceResolver.LibraryEntry(LibraryManager.getDefault().getLibrary("eclipselink"))); // NOI18N
                return ClassPathUtils.updateProject(fileInProject, cs);
            }
        } catch (IOException ex) {
            Logger.getLogger(J2EEUtils.class.getName()).log(Level.INFO, ex.getMessage(), ex);
        }
        return false;
    }
    
    /**
     * Updates project classpath with the JARs specified by the <code>urls</code>.
     * The classpath is not updated if the given reference class (<code>refClassName</code>)
     * is on the classpath.
     *
     * @param urls URLs of the JARs that should be added to the classpath.
     * @param refClassName name of the class that determines wheter the classpath
     * should be updated or not.
     * @param fileInProject file in the project whose classpath should be updated.
     * @return <code>true</code> if the classpath has been updated,
     * returns <code>false</code> otherwise.
     */
    public static boolean updateProjectWithJARs(URL[] urls, String refClassName, FileObject fileInProject) {
        try {
            ClassPath classPath = ClassPath.getClassPath(fileInProject, ClassPath.EXECUTE);
            String resourceName = refClassName.replace('.', '/') + ".class"; // NOI18N
            FileObject fob = classPath.findResource(resourceName); // NOI18N
            if (fob == null) {
                List<ClassSource.Entry> cpEntries = new ArrayList<ClassSource.Entry>(urls.length);
                for (URL url : urls) {
                    FileObject jar = URLMapper.findFileObject(url);
                    if (jar != null) {
                        cpEntries.add(new ClassSourceResolver.JarEntry(FileUtil.toFile(jar)));
                    }
                }
                return ClassPathUtils.updateProject(fileInProject, new ClassSource("", cpEntries)); // NOI18N
            }
        } catch (IOException ex) {
            Logger.getLogger(J2EEUtils.class.getName()).log(Level.INFO, ex.getMessage(), ex);
        }
        return false;
    }

    /**
     * Returns persistence descriptor for the given project.
     *
     * @param project project where the persistence descriptor should be found.
     * @param create determines whether the persistence descriptor should be created
     * (if there is not one already)
     * @throws InvalidPersistenceXmlException
     * @return persistence descriptor for the given project.
     */
    public static FileObject getPersistenceXML(Project project, boolean create) throws InvalidPersistenceXmlException {
        FileObject persistenceXML = ProviderUtil.getDDFile(project);
        if ((persistenceXML == null) && create) {
            // Forces creation of persistence.xml
            ProviderUtil.getPUDataObject(project); 
            persistenceXML = ProviderUtil.getDDFile(project);
        }
        return persistenceXML;
    }

    /**
     * Returns entity manager RAD component that corresponds to the specified persistence unit.
     *
     * @param model form model where the RAD component should be found.
     * @param puName name of the persistence unit.
     * @return entity manager RAD component that corresponds to the specified persistence unit
     * or <code>null</code> if such entity manager is not in the given form model.
     */
    public static RADComponent findEntityManager(FormModel model, String puName) {
        for (RADComponent metacomp : model.getAllComponents()) {
            if ("javax.persistence.EntityManager".equals(metacomp.getBeanClass().getName())) {  // NOI18N
                try {
                    FormProperty prop = (FormProperty)metacomp.getPropertyByName("persistenceUnit"); // NOI18N
                    Object name = prop.getRealValue();
                    if (puName.equals(name)) {
                        return metacomp;
                    }
                } catch (Exception ex) {
                    Logger.getLogger(J2EEUtils.class.getName()).log(Level.INFO, ex.getMessage(), ex);
                }
            }
        }
        return null;
    }
    
    /**
     * Creates entity manager RAD component that corresponds to the specified persistence unit.
     *
     * @param model form model where the RAD component should be created.
     * @param puName name of the persistence unit.
     * @return entity manager RAD component that corresponds to the specified persistence unit.
     * @throws Exception when something goes wrong ;-).
     */
    public static RADComponent createEntityManager(FormModel model, String puName) throws Exception {
        assert EventQueue.isDispatchThread();
        FileObject formFile = FormEditor.getFormDataObject(model).getFormFile();
        Class<?> emClass = ClassPathUtils.loadClass("javax.persistence.EntityManager", formFile); // NOI18N
        RADComponent entityManager = new RADComponent();
        entityManager.initialize(model);
        entityManager.initInstance(emClass);
        entityManager.getPropertyByName("persistenceUnit").setValue(puName); // NOI18N
        renameComponent(entityManager, false, puName + "EntityManager", "entityManager"); // NOI18N
        model.addComponent(entityManager, null, true);
        return entityManager;
    }

    /**
     * Returns entity in the specified persistence unit that corresponds to the given table.
     *
     * @param mappings entity mapping information.
     * @param tableName name of the table for the searched entity.
     * @throws IOException when something goes wrong
     * @return entity in the specified persistence unit that corresponds to the given table
     * or <code>null</code> if such an entity doesn't exist.
     */
    public static String[] findEntity(MetadataModel<EntityMappingsMetadata> mappings, final String tableName) throws IOException {
        String[] entity = null;
        try {
            entity = mappings.runReadActionWhenReady(new MetadataModelAction<EntityMappingsMetadata, String[]>() {
                @Override
                public String[] run(EntityMappingsMetadata metadata) {
                    Entity[] entity = metadata.getRoot().getEntity();
                    for (int i=0; i<entity.length; i++) {
                        String name = entity[i].getTable().getName();
                        name = unquote(name);
                        if (tableName.equals(name)) {
                            return new String[] {entity[i].getName(), entity[i].getClass2()};
                        }
                    }
                    return null;
                }
            }).get();
        } catch (InterruptedException iex) {
            Logger.getLogger(J2EEUtils.class.getName()).log(Level.INFO, iex.getMessage(), iex);
        } catch (ExecutionException eex) {
            Logger.getLogger(J2EEUtils.class.getName()).log(Level.INFO, eex.getMessage(), eex);
        }
        return entity;
    }

    public static String unquote(String name) {
        while (name.startsWith("\"") && name.endsWith("\"")) { // NOI18N
            name = name.substring(1, name.length()-1);
        }
        return name;
    }

    /**
     * Generates entity classes for given tables.
     *
     * @param project project where the classes should be generated.
     * @param location source location.
     * @param packageName name of the package where the classes should be generated.
     * @param tableNames names of the tables.
     * @param dbconn connection to the DB with the tables.
     * @param unit persistence unit to add the generated classes into
     * @return entity class that corresponds to the specified table and names
     * of the entity classes for related tables (if <code>relatedTableNames</code>
     * parameter was non-<code>null</code>).
     */
    private static String[] generateEntityClass(final Project project, SourceGroup location, String packageName, DatabaseConnection dbconn, List<String> tableNames, PersistenceUnit unit) {
        try {
            boolean regenTablesAttrs = "org.apache.derby.jdbc.EmbeddedDriver".equals(dbconn.getDriverClass()); // NOI18N
            EntitiesFromDBGenerator generator = new EntitiesFromDBGenerator(tableNames, true, true, regenTablesAttrs,
                    FetchType.DEFAULT, CollectionType.LIST,
                    packageName, location, dbconn, project, unit);
            // PENDING
            final Set<FileObject> entities = generator.generate(AggregateProgressFactory.createProgressContributor("PENDING"));

            String[] result = new String[entities.size()];
            int count = 0;
            for (FileObject fob : entities) {
                result[count++] = packageName + '.' + fob.getName();
            }
            return result;
        } catch (SQLException sex) {
            Logger.getLogger(J2EEUtils.class.getName()).log(Level.INFO, null, sex);
        } catch (IOException ioex) {
            Logger.getLogger(J2EEUtils.class.getName()).log(Level.INFO, null, ioex);
        }
        return null;
    }

    /**
     * Makes sure that the given database connection is established.
     *
     * @param connection connection that should be established.
     * @return established connection, may return <code>null</code> if the user cancel
     * the connection dialog.
     */
    public static Connection establishConnection(DatabaseConnection connection) {
        Connection con = connection.getJDBCConnection();
        if (con == null) { // connection not established yet
            ConnectionManager.getDefault().showConnectionDialog(connection);
            con = connection.getJDBCConnection();
        }
        return con;
    }

    /**
     * Updates project's classpath if necessary.
     *
     * @param fileInProject file in the project whose classpath should be updated.
     * @param unit persistence unit to be used in the project.
     * @param driver JDBC driver to be used in the project.
     */
    public static void updateProjectForUnit(FileObject fileInProject, PersistenceUnit unit, JDBCDriver driver) {
        // Make sure that TopLink/EclipseLink JAR files are on the classpath
        Provider provider = ProviderUtil.getProvider(unit);
        if (provider!=null && ProviderUtil.ECLIPSELINK_PROVIDER.getProviderClass().equals(provider.getProviderClass())) {
            updateProjectForEclipseLink(fileInProject);
        } else if (ProviderUtil.TOPLINK_PROVIDER1_0.equals(provider)) {
            updateProjectForTopLink(fileInProject);
        }

        // Make sure that DB driver classes are on the classpath
        updateProjectWithJARs(driver.getURLs(), driver.getClassName(), fileInProject);
    }

    /**
     * Initializes persistence unit and persistence descriptor.
     *
     * @param persistenceXML persistence descriptor.
     * @param connection DB connection that specifies parameters of the persistence unit.
     * @return persistence unit that corresponds to the given DB connection.
     * @throws IOException if there is a problem with creation of the persistence unit.
     * @throws InvalidPersistenceXmlException
     */
    public static PersistenceUnit initPersistenceUnit(FileObject persistenceXML, DatabaseConnection connection) throws IOException, InvalidPersistenceXmlException {
        Project project = FileOwnerQuery.getOwner(persistenceXML);
        
        // Make sure the database connection is established
        J2EEUtils.establishConnection(connection);

        // Make sure there is a persistence unit that corresponds to our DB connection
        Persistence persistence = PersistenceMetadata.getDefault().getRoot(persistenceXML);
        PersistenceUnit unit = J2EEUtils.findPersistenceUnit(persistence, connection.getDatabaseURL());
        if (unit == null) {
            unit = J2EEUtils.createPersistenceUnit(project, connection);
        }
        return unit;
    }

    /**
     * Creates entity that corresponds to the specified table (accessible via given DB connection).
     * Possibly creates also entities for related tables.
     *
     * @param dir directory where the entity should be created.
     * @param scope persistence scope where the entity should be created.
     * @param unit persistence unit where the entity should be created.
     * @param connection connection through which the table is accessible.
     * @param tableName name of the table.
     * @param relatedTableNames names of related tables whose entity classes should be added
     * into the peristence unit.
     * @throws IOException when something goes wrong.
     */
    public static void createEntity(FileObject dir, PersistenceScope scope, PersistenceUnit unit, DatabaseConnection connection, String tableName, String[] relatedTableNames) throws IOException {
        Project project = FileOwnerQuery.getOwner(dir);
        String packageName = scope.getClassPath().getResourceName(dir, '.', false);

        SourceGroup[] groups = getJavaSourceGroups(project);
        SourceGroup location = groups[0];
        for (int i=0; i<groups.length; i++) {
            boolean contains;
            try {
                contains = groups[i].contains(dir);
            } catch (IllegalArgumentException iaex) {
                contains = false;
            }
            if (contains) {
                location = groups[i];
                break;
            }
        }
        List<String> tableNames = new LinkedList<String>();
        tableNames.add(tableName);
        if (relatedTableNames != null) {
            List<String> relatedTables = Arrays.asList(relatedTableNames);
            if (relatedTables.contains(tableName)) {
                tableNames.remove(tableName);
            }
            tableNames.addAll(relatedTables);
        }
        J2EEUtils.generateEntityClass(project, location, packageName, connection, tableNames, unit);
        // PENDING ugly workaround for the fact that the generated entity is not immediately
        // in the model - will be removed as soon as the corresponding issue is fixed
        try {
            outer: for (int i=0; i<30; i++) {
                MetadataModel<EntityMappingsMetadata> mappings = scope.getEntityMappingsModel(unit.getName());
                for (String table : tableNames) {
                    String[] entityInfo = J2EEUtils.findEntity(mappings, table);
                    if (entityInfo == null) {
                        Thread.sleep(1000);
                        continue outer;
                    }
                }
                break;
            }
        } catch (InterruptedException iex) {
            Logger.getLogger(J2EEUtils.class.getName()).log(Level.INFO, iex.getMessage(), iex);
        }
    }

    public static String fieldToProperty(String fieldName) {
        char first = fieldName.charAt(0);
        if (fieldName.length() > 1) {
            char second = fieldName.charAt(1);
            String suffix = fieldName.substring(1);
            if (Character.isLowerCase(second)) {
                first = Character.toLowerCase(first);
            } else {
                first = Character.toUpperCase(first);
            }
            return first + suffix;
        } else {
            return Character.toString(Character.toLowerCase(first));
        }
    }

    /**
     * Renames metacomponent.
     * 
     * @param comp component to rename.
     * @param inModel determines whether the component is already in the model.
     * @param name suggested new names for the component.
     */
    public static void renameComponent(RADComponent comp, boolean inModel, String... name) {
        String oldName = comp.getName();
        FormModel formModel = comp.getFormModel();
        int index = 0;
        while (!Utilities.isJavaIdentifier(name[index])) index++;
        String prefix = name[index];
        String newName;
        CodeStructure codeStructure = formModel.getCodeStructure();
        if (codeStructure.isVariableNameReserved(prefix) && !prefix.equals(oldName)) {
            index = 0;
            while (codeStructure.isVariableNameReserved(prefix+index) && !prefix.equals(oldName)) index++;
            newName = prefix + index;
        } else {
            newName = prefix;
        }
        if (inModel) {
            comp.setName(newName);
        } else {
            comp.setStoredName(newName);
        }
    }
    
    public static boolean hasPrimaryKey(DatabaseConnection connection, String tableName) {
        Connection con = connection.getJDBCConnection();
        boolean hasPK = false;
        try {
            ResultSet rs = con.getMetaData().getPrimaryKeys(con.getCatalog(), connection.getSchema(), tableName);
            hasPK = rs.next();
            rs.close();
        } catch (Exception ex) {
            Logger.getLogger(J2EEUtils.class.getName()).log(Level.INFO, ex.getMessage(), ex);
        }
        return hasPK;
    }

    /**
     * Finds out tables in the DB represented by the given DB connection. 
     * 
     * @param connection DB connection to search for tables.
     * @return list of names of the tables.
     */
    public static List<DBColumnInfo> tableNamesForConnection(DatabaseConnection connection) {
        Connection con = connection.getJDBCConnection();
        List<DBColumnInfo> tables = new LinkedList<DBColumnInfo>();
        try {
            ResultSet rs = con.getMetaData().getTables(con.getCatalog(), connection.getSchema(), "%",  new String[] {"TABLE"}); // NOI18N
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME"); // NOI18N
                boolean hasPK = hasPrimaryKey(connection, tableName);
                tables.add(new DBColumnInfo(tableName, hasPK, hasPK ? null : NbBundle.getMessage(J2EEUtils.class, "MSG_NO_PK"))); // NOI18N
            }
            rs.close();
        } catch (Exception ex) {
            Logger.getLogger(J2EEUtils.class.getName()).log(Level.INFO, ex.getMessage(), ex);
        }
        return tables;
    }

    public static class DBColumnInfo {
        private String name;
        private boolean valid;
        private String message;
        
        DBColumnInfo(String name, boolean valid, String message) {
            this.name = name;
            this.valid = valid;
            this.message = message;
        }

        public String getName() {
            return name;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
        
        public static ListCellRenderer getRenderer() {
            return new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof DBColumnInfo) {
                        DBColumnInfo column = (DBColumnInfo)value;
                        String label = column.getName() + (column.isValid() ? "" : " (" + column.getMessage() + ")"); // NOI18N
                        setText(label);
                        setEnabled(column.isValid());
                    }
                    return this;
                }
            };
        }
    }

    /**
     * Makes the entity observable (e.g. adds property change support).
     * 
     * @param entity entity to make observable.
     */
    public static void makeEntityObservable(FileObject fileInProject, String[] entityInfo, MetadataModel<EntityMappingsMetadata> mappings) {
        if (entityInfo == null) return;
        ClassPath cp = ClassPath.getClassPath(fileInProject, ClassPath.SOURCE);
        String resName = entityInfo[1].replace('.', '/') + ".java"; // NOI18N
        FileObject entity = cp.findResource(resName);
        if (entity == null) return;
        final List<String> properties;
        try {
            properties = propertiesForColumns(mappings, entityInfo[0], null);
        } catch (IOException ioex) {
            return;
        }
        JavaSource source = JavaSource.forFileObject(entity);
        final boolean[] alreadyUpdated = new boolean[1];
        try {
            // PENDING merge into one task once it will be possible
            source.runModificationTask(new CancellableTask<WorkingCopy>() {

                @Override
                public void run(WorkingCopy wc) throws Exception {
                    wc.toPhase(JavaSource.Phase.RESOLVED);
                    CompilationUnitTree cu = wc.getCompilationUnit();
                    ClassTree clazz = null;
                    for (Tree typeDecl : cu.getTypeDecls()) {
                        if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                            ClassTree candidate = (ClassTree) typeDecl;
                            if (candidate.getModifiers().getFlags().contains(javax.lang.model.element.Modifier.PUBLIC)) {
                                clazz = candidate;
                                break;
                            }
                        }
                    }
                    
                    for (Tree member : clazz.getMembers()) {
                        if (Tree.Kind.VARIABLE == member.getKind()) {
                            VariableTree variable = (VariableTree)member;
                            String type = variable.getType().toString();
                            if (type.endsWith("PropertyChangeSupport")) { // NOI18N
                                alreadyUpdated[0] = true;
                            }
                        }
                    }
                    
                    TreeMaker make = wc.getTreeMaker();
                    ClassTree modifiedClass = clazz;
                    
                    if (!alreadyUpdated[0]) {
                        // changeSupport field
                        TypeElement transientElement = wc.getElements().getTypeElement("javax.persistence.Transient"); // NOI18N
                        TypeMirror transientMirror = transientElement.asType();
                        Tree transientType = make.Type(transientMirror);
                        AnnotationTree transientTree = make.Annotation(transientType, Collections.EMPTY_LIST);
                        ModifiersTree modifiers = make.Modifiers(Modifier.PRIVATE, Collections.singletonList(transientTree));
                        TypeElement changeSupportElement = wc.getElements().getTypeElement("java.beans.PropertyChangeSupport"); // NOI18N
                        TypeMirror changeSupportMirror = changeSupportElement.asType();
                        Tree changeSupportType = make.Type(changeSupportMirror);
                        NewClassTree changeSupportConstructor = make.NewClass(null, Collections.EMPTY_LIST, make.QualIdent(changeSupportElement), Collections.singletonList(make.Identifier("this")), null);
                        VariableTree changeSupport = make.Variable(modifiers, "changeSupport", changeSupportType, changeSupportConstructor); // NOI18N
                        modifiedClass = make.insertClassMember(clazz, 0, changeSupport);
                    }

                    // property change notification
                    for (Tree clMember : modifiedClass.getMembers()) {
                        if (clMember.getKind() == Tree.Kind.METHOD) {
                            MethodTree method = (MethodTree)clMember;
                            String methodName = method.getName().toString();
                            if (methodName.startsWith("set") && (methodName.length() > 3) && (Character.isUpperCase(methodName.charAt(3))) && (method.getParameters().size() == 1)) { // NOI18N
                                String propName = methodName.substring(3);
                                if ((propName.length() == 1) || (Character.isLowerCase(propName.charAt(1)))) {
                                    propName = Character.toLowerCase(propName.charAt(0)) + propName.substring(1);
                                }
                                if (!properties.contains(propName)) continue;
                                BlockTree block = method.getBody();
                                if (block.getStatements().size() != 1) continue;
                                StatementTree statement = block.getStatements().get(0);
                                if (statement.getKind() != Tree.Kind.EXPRESSION_STATEMENT) continue;
                                ExpressionTree expression = ((ExpressionStatementTree)statement).getExpression();
                                if (expression.getKind() != Tree.Kind.ASSIGNMENT) continue;
                                AssignmentTree assignment = (AssignmentTree)expression;
                                String parName = assignment.getExpression().toString();
                                VariableTree parameter = method.getParameters().get(0);
                                if (!parameter.getName().toString().equals(parName)) continue;
                                ExpressionTree persistentVariable = assignment.getVariable();

                                // <Type> old<PropertyName> = this.<propertyName>
                                String parameterName = parameter.getName().toString();
                                String oldParameterName = "old" + Character.toUpperCase(parameterName.charAt(0)) + parameterName.substring(1); // NOI18N
                                Tree parameterTree = parameter.getType();
                                VariableTree oldParameter = make.Variable(make.Modifiers(Collections.EMPTY_SET), oldParameterName, parameterTree, persistentVariable);
                                BlockTree newBlock = make.insertBlockStatement(block, 0, oldParameter);

                                // changeSupport.firePropertyChange("<propertyName>", old<PropertyName>, <propertyName>);
                                MemberSelectTree fireMethod = make.MemberSelect(make.Identifier("changeSupport"), "firePropertyChange"); // NOI18N
                                List<ExpressionTree> fireArgs = new LinkedList<ExpressionTree>();
                                fireArgs.add(make.Literal(propName));
                                fireArgs.add(make.Identifier(oldParameterName));
                                fireArgs.add(make.Identifier(parameterName));
                                MethodInvocationTree notification = make.MethodInvocation(Collections.EMPTY_LIST, fireMethod, fireArgs);
                                newBlock = make.addBlockStatement(newBlock, make.ExpressionStatement(notification));
                                wc.rewrite(block, newBlock);
                            }
                        }
                    }
                    wc.rewrite(clazz, modifiedClass);
                }

                @Override
                public void cancel() {
                }

            }).commit();
            if (alreadyUpdated[0]) return;
            source.runModificationTask(new CancellableTask<WorkingCopy>() {

                @Override
                public void run(WorkingCopy wc) throws Exception {
                    wc.toPhase(JavaSource.Phase.RESOLVED);
                    CompilationUnitTree cu = wc.getCompilationUnit();
                    ClassTree clazz = null;
                    for (Tree typeDecl : cu.getTypeDecls()) {
                        if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                            ClassTree candidate = (ClassTree) typeDecl;
                            if (candidate.getModifiers().getFlags().contains(javax.lang.model.element.Modifier.PUBLIC)) {
                                clazz = candidate;
                                break;
                            }
                        }
                    }
                    TreeMaker make = wc.getTreeMaker();

                    // addPropertyChange method
                    ModifiersTree parMods = make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST);
                    TypeElement changeListenerElement = wc.getElements().getTypeElement("java.beans.PropertyChangeListener"); // NOI18N
                    VariableTree par = make.Variable(parMods, "listener", make.QualIdent(changeListenerElement), null); // NOI18N
                    TypeElement changeSupportElement = wc.getElements().getTypeElement("java.beans.PropertyChangeSupport"); // NOI18N
                    VariableTree changeSupport = make.Variable(parMods, "changeSupport", make.QualIdent(changeSupportElement), null); // NOI18N
                    MemberSelectTree addCall = make.MemberSelect(make.Identifier(changeSupport.getName()), "addPropertyChangeListener"); // NOI18N
                    MethodInvocationTree addInvocation = make.MethodInvocation(Collections.EMPTY_LIST, addCall, Collections.singletonList(make.Identifier(par.getName())));
                    MethodTree addMethod = make.Method(
                        make.Modifiers(Modifier.PUBLIC, Collections.EMPTY_LIST),
                        "addPropertyChangeListener", // NOI18N
                        make.PrimitiveType(TypeKind.VOID),
                        Collections.EMPTY_LIST,
                        Collections.singletonList(par),
                        Collections.EMPTY_LIST,
                        make.Block(Collections.singletonList(make.ExpressionStatement(addInvocation)), false),
                        null
                    );
                    ClassTree modifiedClass = make.addClassMember(clazz, addMethod);
                    wc.rewrite(clazz, modifiedClass);
                }

                @Override
                public void cancel() {
                }

            }).commit();
            source.runModificationTask(new CancellableTask<WorkingCopy>() {

                @Override
                public void run(WorkingCopy wc) throws Exception {
                    wc.toPhase(JavaSource.Phase.RESOLVED);
                    CompilationUnitTree cu = wc.getCompilationUnit();
                    ClassTree clazz = null;
                    for (Tree typeDecl : cu.getTypeDecls()) {
                        if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                            ClassTree candidate = (ClassTree) typeDecl;
                            if (candidate.getModifiers().getFlags().contains(javax.lang.model.element.Modifier.PUBLIC)) {
                                clazz = candidate;
                                break;
                            }
                        }
                    }
                    TreeMaker make = wc.getTreeMaker();

                    // removePropertyChange method
                    ModifiersTree parMods = make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST);
                    TypeElement changeListenerElement = wc.getElements().getTypeElement("java.beans.PropertyChangeListener"); // NOI18N
                    VariableTree par = make.Variable(parMods, "listener", make.QualIdent(changeListenerElement), null); // NOI18N
                    TypeElement changeSupportElement = wc.getElements().getTypeElement("java.beans.PropertyChangeSupport"); // NOI18N
                    VariableTree changeSupport = make.Variable(parMods, "changeSupport", make.QualIdent(changeSupportElement), null); // NOI18N
                    MemberSelectTree removeCall = make.MemberSelect(make.Identifier(changeSupport.getName()), "removePropertyChangeListener"); // NOI18N
                    MethodInvocationTree removeInvocation = make.MethodInvocation(Collections.EMPTY_LIST, removeCall, Collections.singletonList(make.Identifier(par.getName())));
                    MethodTree removeMethod = make.Method(
                        make.Modifiers(Modifier.PUBLIC, Collections.EMPTY_LIST),
                        "removePropertyChangeListener", // NOI18N
                        make.PrimitiveType(TypeKind.VOID),
                        Collections.EMPTY_LIST,
                        Collections.singletonList(par),
                        Collections.EMPTY_LIST,
                        make.Block(Collections.singletonList(make.ExpressionStatement(removeInvocation)), false),
                        null
                    );
                    ClassTree modifiedClass = make.addClassMember(clazz, removeMethod);
                    wc.rewrite(clazz, modifiedClass);
                }

                @Override
                public void cancel() {
                }

            }).commit();
        } catch (IOException ioex) {
            Logger.getLogger(J2EEUtils.class.getName()).log(Level.INFO, ioex.getMessage(), ioex);
        }
    }

    public static List<String> typesOfProperties(FileObject fileInProject, String entityClass, final List<String> propertyNames) {
        ClassPath cp = ClassPath.getClassPath(fileInProject, ClassPath.SOURCE);
        String resourceName = entityClass.replace('.', '/') + ".java"; // NOI18N
        FileObject entity = cp.findResource(resourceName);
        final List<String> types = new LinkedList<String>();
        if (entity == null) return types;
        JavaSource source = JavaSource.forFileObject(entity);
        try {
            source.runUserActionTask(new CancellableTask<CompilationController>() {
                @Override
                public void run(CompilationController cc) throws Exception {
                    cc.toPhase(JavaSource.Phase.RESOLVED);
                    CompilationUnitTree cu = cc.getCompilationUnit();
                    ClassTree clazz = null;
                    for (Tree typeDecl : cu.getTypeDecls()) {
                        if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                            ClassTree candidate = (ClassTree) typeDecl;
                            if (candidate.getModifiers().getFlags().contains(javax.lang.model.element.Modifier.PUBLIC)) {
                                clazz = candidate;
                                break;
                            }
                        }
                    }
                    Map<String, String> variables = new HashMap<String, String>();
                    Map<String, String> methods = new HashMap<String, String>();
                    Element classElement = cc.getTrees().getElement(cc.getTrees().getPath(cu, clazz));
                    for (VariableElement variable : ElementFilter.fieldsIn(classElement.getEnclosedElements())) {
                        String name = variable.getSimpleName().toString();
                        String type = variable.asType().toString();
                        variables.put(name, type);
                    }
                    for (ExecutableElement method : ElementFilter.methodsIn(classElement.getEnclosedElements())) {
                        String type = method.getReturnType().toString();
                        String name = method.getSimpleName().toString();
                        if (name.startsWith("get")) { // NOI18N
                            name = name.substring(3);
                        } else if (name.startsWith("is") && type.equals("boolean")) { // NOI18N
                            name = name.substring(2);
                        } else {
                            name = null;
                        }
                        if ((name != null) && (name.length() > 0)) {
                            if ((name.length() == 1) || Character.isLowerCase(name.charAt(1))) {
                                name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
                            }
                            methods.put(name, type);
                        }                        
                    }
                    for (String name : propertyNames) {
                        String type = methods.get(name);
                        if (type == null) {
                            type = variables.get(name);
                        }
                        if (type != null) {
                            type = FormUtils.autobox(type);
                            if (type.startsWith("java.lang.")) { // NOI18N
                                type = type.substring(10);
                            }
                            type += ".class"; // NOI18N
                        }
                        types.add(type);
                    }
                }

                @Override
                public void cancel() {
                }
            }, true);
        } catch (IOException ioex) {
            Logger.getLogger(J2EEUtils.class.getName()).log(Level.INFO, ioex.getMessage(), ioex);
        }
        return types;
    }

    /**
     * Determines properties of the entity that will be displayed as table columns.
     * 
     * @param mappings information about entity mappings.
     * @param entityName of the entity.
     * @return list of property names.
     * @throws IOException when something goes wrong.
     */
    public static List<String> propertiesForColumns(MetadataModel<EntityMappingsMetadata> mappings,
            final String entityName, final List<String> columns) throws IOException {
        List<String> properties = Collections.emptyList();
        try {
            properties = mappings.runReadActionWhenReady(new MetadataModelAction<EntityMappingsMetadata, List<String>>() {
                @Override
                public List<String> run(EntityMappingsMetadata metadata) {
                    Entity[] entities = metadata.getRoot().getEntity();
                    Entity entity = null;
                    for (int i=0; i<entities.length; i++) {
                        if (entityName.equals(entities[i].getName())) {
                            entity = entities[i];
                            break;
                        }
                    }
                    if (entity == null) {
                        return Collections.emptyList();
                    }
                    boolean all = (columns == null);
                    List<String> props = new LinkedList<String>();
                    Map<String,String> columnToProperty = all ? null : new HashMap<String,String>();
                    Attributes attrs = entity.getAttributes();
                    for (Id id : attrs.getId()) {
                        String propName = J2EEUtils.fieldToProperty(id.getName());
                        if (all) {
                            props.add(propName);
                        } else {
                            String columnName = id.getColumn().getName();
                            columnName = unquote(columnName);
                            columnToProperty.put(columnName, propName);
                        }
                    }
                    for (Basic basic : attrs.getBasic()) {
                        String propName = J2EEUtils.fieldToProperty(basic.getName());
                        if ("<error>".equals(propName)) { // NOI18N
                            continue;
                        }
                        if (all) {
                            props.add(propName);
                        } else {
                            String columnName = basic.getColumn().getName();
                            columnName = unquote(columnName);
                            columnToProperty.put(columnName, propName);                        
                        }
                    }
                    for (ManyToOne manyToOne : attrs.getManyToOne()) {
                        String propName = J2EEUtils.fieldToProperty(manyToOne.getName());
                        if ("<error>".equals(propName)) { // NOI18N
                            continue;
                        }
                        if (all) {
                            props.add(propName);
                        } else {
                            JoinColumn[] joinColumn = manyToOne.getJoinColumn();
                            String columnName;
                            if (joinColumn.length == 0) {
                                columnName = manyToOne.getName().toUpperCase() + "_ID"; // NOI18N
                            } else {
                                columnName = manyToOne.getJoinColumn(0).getName();
                            }
                            columnName = unquote(columnName);
                            columnToProperty.put(columnName, propName);                        
                        }
                    }
                    if (!all) {
                        for (String column : columns) {
                            String propName = columnToProperty.get(column);
                            if (propName == null) {
                                Logger.getLogger(J2EEUtils.class.getName()).log(
                                    Level.INFO, "WARNING: Cannot find property for column {0}", column); // NOI18N
                            } else {
                                props.add(propName);
                            }
                        }
                    }
                    return props;
                }
            }).get();
        } catch (InterruptedException iex) {
            Logger.getLogger(J2EEUtils.class.getName()).log(Level.INFO, iex.getMessage(), iex);
        } catch (ExecutionException eex) {
            Logger.getLogger(J2EEUtils.class.getName()).log(Level.INFO, eex.getMessage(), eex);
        }
        return properties;
    }

    private static SourceGroup[] getJavaSourceGroups(Project project) {
        Parameters.notNull("project", project); //NOI18N
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        return sourceGroups;
    }

    static boolean supportsJPA(FormModel formModel) {
        PersistenceLocationProvider provider = null;
        FormDataObject fdo = FormEditor.getFormDataObject(formModel);
        if (fdo != null) {
            Project project = FileOwnerQuery.getOwner(fdo.getPrimaryFile());
            if (project != null) {
                provider = project.getLookup().lookup(PersistenceLocationProvider.class);
            }
        }
        return (provider != null);
    }
}
