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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hibernate.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.queries.BinaryForSourceQuery.Result;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.SourceGroupModifier;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.dbschema.SchemaElement;
import org.netbeans.modules.hibernate.cfg.model.HibernateConfiguration;
import org.netbeans.modules.hibernate.cfg.model.SessionFactory;
import org.netbeans.modules.hibernate.loaders.cfg.HibernateCfgDataLoader;
import org.netbeans.modules.hibernate.loaders.cfg.HibernateCfgDataObject;
import org.netbeans.modules.hibernate.loaders.mapping.HibernateMappingDataLoader;
import org.netbeans.modules.hibernate.loaders.reveng.HibernateRevengDataLoader;
import org.netbeans.modules.hibernate.service.TableColumn;
import org.netbeans.modules.hibernate.wizards.support.DBSchemaManager;
import org.netbeans.modules.hibernate.wizards.support.DBSchemaTableProvider;
import org.netbeans.modules.hibernate.wizards.support.EmptyTableProvider;
import org.netbeans.modules.hibernate.wizards.support.Table;
import org.netbeans.modules.hibernate.wizards.support.TableProvider;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;

/**
 * This class provides utility methods using Hibernate API to query database
 * based on the configurations setup in the project.
 *
 * @author Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 */
public class HibernateUtil {
    private static final String REGEXP_XML_FILE = ".+\\.xml"; // NOI18N
    private static final String REGEXP_CFG_XML_FILE = ".+\\.cfg\\.xml"; // NOI18N

    private static Logger logger = Logger.getLogger(HibernateUtil.class.getName());

    /**
     * This methods gets all database tables from the supplied Hibernate Configurations.
     * Note : This class uses a deprecated method, that will be replaced in future.
     *
     * @param configurations hibernate configrations used to connect to the DB.
     * @return list of strings of table names.
     * @throws java.sql.SQLException
     */
    public static List<String> getAllDatabaseTables(HibernateConfiguration... configurations)
            throws java.sql.SQLException {
        List<String> allTables = new ArrayList<String>();
        for (HibernateConfiguration configuration : configurations) {
            try {
                DatabaseConnection dbConnection = getDBConnection(configuration);
                if (dbConnection != null) {
                    java.sql.Connection jdbcConnection = dbConnection.getJDBCConnection();
                    if (jdbcConnection != null) {
                        java.sql.DatabaseMetaData dbMetadata = jdbcConnection.getMetaData();
                        java.sql.ResultSet rsSchema = dbMetadata.getSchemas();
                        if (rsSchema.next()) {
                            do {
                                java.sql.ResultSet rs = dbMetadata.getTables(null, rsSchema.getString("TABLE_SCHEM"), null, new String[]{"TABLE"}); //NOI18N
                                while (rs.next()) {
                                    allTables.add(rs.getString("TABLE_NAME")); //NOI18N
                                }
                            } while (rsSchema.next());
                        } else {
                            // Getting tables from default schema.
                            java.sql.ResultSet rs = dbMetadata.getTables(null, dbConnection.getSchema(), null, new String[]{"TABLE"}); //NOI18N
                            while (rs.next()) {
                                allTables.add(rs.getString("TABLE_NAME")); //NOI18N
                            }
                        }
                    } else {
                        // JDBC Connection could not be established.
                        //TODO Handle this situation gracefully, probably by displaying message to user.
                        //throw new DatabaseException("JDBC Connection cannot be established.");
                    }
                } else {
                    // DBConnection could not be established.
                    //TODO Handle this situation gracefully, probably by displaying message to user.
                    //throw new DatabaseException("JDBC Connection cannot be established.");
                }
            } catch (DatabaseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return allTables;
    }
    
    /**
     * This methods gets all database tables from the supplied Hibernate Configurations.
     *
     * @param configurations hibernate configrations used to connect to the DB.
     * @return list of strings of table names.
     * @throws java.sql.SQLException
     */
    public static List<String> getAllDatabaseTablesOnEventThread(FileObject configurationFO) 
        throws SQLException, DatabaseException, DataObjectNotFoundException{
        List<String> databaseTables = new ArrayList<String>();
        DBSchemaManager dbSchemaManager = new DBSchemaManager();
        DatabaseConnection dbConnection = getDBConnection(((HibernateCfgDataObject)DataObject.find(configurationFO)).getHibernateConfiguration());
        SchemaElement schemaElement=null;
        if (dbConnection!=null) {
            schemaElement = dbSchemaManager.getSchemaElement(dbConnection);
        }
        TableProvider tableProvider = null;
        if(schemaElement != null) {
            tableProvider = new DBSchemaTableProvider(schemaElement);
        } else {
            tableProvider = new EmptyTableProvider();
        }
        for(Table table : tableProvider.getTables()) {
            databaseTables.add(table.getName());
        }
        return databaseTables;
    }

    /**
     * Constructs HibernateConfiguration (schema2beans) objects for each of the cfg 
     * file under this project. 
     * 
     * @param project the project for which HibernateConfigurations need to be constructed.
     * @return list of HibernateConfiguration objects or an empty list of none found.
     */
    public static List<HibernateConfiguration> getAllHibernateConfigurations(Project project) {
        List<HibernateConfiguration> configFiles = new ArrayList<HibernateConfiguration>();
        for (FileObject fo : getAllHibernateConfigFileObjects(project)) {
            try {
                configFiles.add(((HibernateCfgDataObject) DataObject.find(fo)).getHibernateConfiguration());
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return configFiles;
    }

    /**
     * Seaches cfg FileObjects under the given projects and returns them.
     * 
     * @param project the project for which HIbernate configuration files need to be searched.
     * @return list of HibernateConfiguration FileObjects or an empty list of none found.
     */
    public static List<FileObject> getAllHibernateConfigFileObjects(Project project) {
        return searchSourceFiles(project, REGEXP_XML_FILE, HibernateCfgDataLoader.REQUIRED_MIME);
    }

    /**
     * Seaches cfg FileObjects under the sourceroots for given projects and returns them.
     * 
     * @param project the project for which Hibernate configuration files need to be searched.
     * @return list of HibernateConfiguration FileObjects or an empty list of none found.
     */
    public static List<FileObject> getDefaultHibernateConfigFileObjects(Project project) {
        return searchSourceFiles(project, REGEXP_CFG_XML_FILE, null);
    }

    /**
     * Seaches mapping files under the given project and returns the list of 
     * FileObjects if found.
     * 
     * @param project the project for whcih the mapping files are to be found.
     * @return list of FileObjects of actual mapping files.
     */
    public static List<FileObject> getAllHibernateMappingFileObjects(Project project) {
        return searchSourceFiles(project, REGEXP_XML_FILE, HibernateMappingDataLoader.REQUIRED_MIME);
    }

    private static FileObject createBuildFolder(File buildFile) {
        FileObject buildFO = null;
        logger.info("Build folder does not exist. Creating it.");

        try {
            buildFO = FileUtil.createFolder(buildFile);
        } catch (IOException ioe) {
            logger.log(Level.INFO, "Cannot create build folder", ioe);
        }
        return buildFO;
    }

    /**
     * @return {@link JavaProjectConstants#SOURCES_TYPE_RESOURCES} if exists, {@link JavaProjectConstants#SOURCES_TYPE_JAVA} otherwise.
     */
    public static SourceGroup[] getSourceGroups(Project project) {
        Sources projectSources = ProjectUtils.getSources(project);
        // first, try to get resources
        SourceGroup[] resources = projectSources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
        if (resources.length > 0) {
            return resources;
        }
        // try to create it
        SourceGroup resourcesSourceGroup = SourceGroupModifier.createSourceGroup(
            project, JavaProjectConstants.SOURCES_TYPE_RESOURCES, JavaProjectConstants.SOURCES_HINT_MAIN);
        if (resourcesSourceGroup != null) {
            return new SourceGroup[] {resourcesSourceGroup};
        }
        // fallback to java sources
        return projectSources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
    }

    /**
     * Get the 1st source group.
     * @see #getSourceGroups(Project)
     */
    public static SourceGroup getFirstSourceGroup(Project project) {
        // well, we should always get some sources
        return getSourceGroups(project)[0];
    }

    /**
     * Searches for the given Java File Object in project sources.
     * @param className the FQN java classname to be searched.
     * @param project the project.
     * @return Java FileObject if found, null if not found.
     */
    public static FileObject findJavaFileObjectInProject(String className, Project project) {
//        for (SourceGroup sourceGroup : getSourceGroups(project)) {
//            FileObject root = sourceGroup.getRootFolder();
        className = className.replace('.', File.separatorChar);
        className = className + ".java"; //NOI18N
//            FileObject clazzFO = root.getFileObject(className);
//            if(clazzFO != null) {
//                logger.info("Found Java FileObject " + clazzFO + ". Returning.");
//                return clazzFO;
//            }
//        }
        GlobalPathRegistry globalPathRegistry = GlobalPathRegistry.getDefault();
        FileObject clazzFO = globalPathRegistry.findResource(className);
        return clazzFO;
    }

    /**
     * Returns the project classpath including project build paths.
     * Can be used to set classpath for custom classloader.
     * 
     * @param projectFile file in current project.
     * @return List of java.net.URL objects representing each entry on the classpath.
     */
    public static List<URL> getProjectClassPathEntries(FileObject projectFile) {
        List<URL> projectClassPathEntries = new ArrayList<URL>();
        ClassPath cp = ClassPath.getClassPath(projectFile, ClassPath.EXECUTE);

        for (ClassPath.Entry cpEntry : cp.entries()) {
            projectClassPathEntries.add(cpEntry.getURL());
        }

        return projectClassPathEntries;
    }

    /**
     * Returns the project classpath including project build paths.
     * Can be used to set classpath for custom classloader.
     * 
     * @param project the current project.
     * @return List of java.net.URL objects representing each entry on the classpath.
     */
    public static List<URL> getProjectClassPath(Project project) {
        List<URL> projectClassPathEntries = new ArrayList<URL>();
        for (SourceGroup sourceGroup : getSourceGroups(project)) {
            if (sourceGroup == null) {
                continue;
            }
            ClassPath cp = ClassPath.getClassPath(sourceGroup.getRootFolder(), ClassPath.COMPILE);

            for (ClassPath.Entry cpEntry : cp.entries()) {
                projectClassPathEntries.add(cpEntry.getURL());
            }
        }

        return projectClassPathEntries;
    }

    /**
     * Returns the build directory set for this project.
     * @param projectFile a file in the project.
     * @param project the project.
     * @return build directory FileObject, or null if not found.
     */
    public static FileObject getBuildFO(Project project) {
        FileObject buildFO = null;
        try {
            BinaryForSourceQueryImplementation binaryForSourceQueryImpl = project.getLookup().lookup(BinaryForSourceQueryImplementation.class);

            if (binaryForSourceQueryImpl == null) {
                // Web projects do not have this in the lookup.
                logger.info("BinaryForSourceQueryImpl is null. trying reflection.");
                // The following is a hack because of #140802.
                Method getEvaluatorMethod = null;
                try {
                    getEvaluatorMethod = project.getClass().getDeclaredMethod("evaluator", new Class[]{});
                } catch (NoSuchMethodException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (SecurityException ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (getEvaluatorMethod != null) {
                    try {
                        PropertyEvaluator propEvaluator = (PropertyEvaluator) getEvaluatorMethod.invoke(project, new Object[]{});
                        String buildDir = propEvaluator.getProperty("build.classes.dir");
                        if (buildDir != null) {
                            buildFO = project.getProjectDirectory().getFileObject(buildDir);
                            if (buildFO == null) {
                                File buildFile = new File(FileUtil.toFile(project.getProjectDirectory()), buildDir);
                                buildFO = createBuildFolder(buildFile);
                            }
                        }
                    } catch (IllegalAccessException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IllegalArgumentException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (java.lang.reflect.InvocationTargetException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    logger.info("No method named 'evaluator() found in project : " + project);
                }

            } else {
                SourceGroup[] sourceGroup = HibernateUtil.getSourceGroups(project);
                Result result = BinaryForSourceQuery.findBinaryRoots(
                        sourceGroup[0].getRootFolder().getURL());
                URL buildURL = result.getRoots()[0];
                logger.info("Got build URL from the project sources : " + buildURL);
                File buildFile = new File(buildURL.getPath());
                buildFO = FileUtil.toFileObject(buildFile);
                if (buildFO == null) {
                    buildFO = createBuildFolder(buildFile);
                }
            }
            
            if(buildFO == null) { // For freeform projects.
                SourceGroup[] sourceGroup = HibernateUtil.getSourceGroups(project);
                Result result = BinaryForSourceQuery.findBinaryRoots(
                        sourceGroup[0].getRootFolder().getURL());
                URL buildURL = result.getRoots()[0];
                logger.info("Got build URL from the project sources : " + buildURL);
                File buildFile = new File(buildURL.getPath());
                buildFO = FileUtil.toFileObject(buildFile);
                if (buildFO == null) {
                    buildFO = createBuildFolder(buildFile);
                }
            }
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }
        logger.info("Build Folder = " + buildFO);
        return buildFO;
    }

    /**
     * Seaches mapping files under the given project and returns the list of 
     * mapping files relative to the source path. This method is intendeed to be 
     * used in code completion of mapping files in config files.
     * 
     * @param project the project for whcih the mapping files are to be found.
     * @return list of relative paths of actual mapping files.
     */
    public static List<String> getAllHibernateMappingsRelativeToSourcePath(Project project) {
        List<FileObject> files = searchSourceFiles(project, REGEXP_XML_FILE, HibernateMappingDataLoader.REQUIRED_MIME);
        List<String> mappingFiles = new ArrayList<String>(files.size());

        SourceGroup[] javaSourceGroup = getSourceGroups(project);
        for (FileObject fo : files) {
            for (SourceGroup sourceGroup : javaSourceGroup) {
                FileObject root = sourceGroup.getRootFolder();
                String relativePath = FileUtil.getRelativePath(root, fo);
                if (relativePath != null) {
                    assert relativePath.length() > 0;
                    mappingFiles.add(relativePath);
                    break;
                }
            }
        }
        return mappingFiles;
    }

    public static List<FileObject> getAllHibernateReverseEnggFileObjects(Project project) {
        return searchSourceFiles(project, REGEXP_XML_FILE, HibernateRevengDataLoader.REQUIRED_MIME);
    }

    /**
     * Returns Column information for the given table defined under the given 
     * configuration.
     * 
     * @param tableName the tablename.
     * @param hibernateConfiguration the database configuration to be used.
     * @return list of TableColumn objects.
     */
    public static List<TableColumn> getColumnsForTable(String tableName, HibernateConfiguration hibernateConfiguration) {
        List<TableColumn> columnNames = new ArrayList<TableColumn>();

        try {
            java.sql.Connection connection = getJDBCConnection(hibernateConfiguration);
            if (connection != null) {
                java.sql.Statement stmt = connection.createStatement();
                java.sql.ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName); //NOI18N
                java.sql.ResultSetMetaData rsMetadata = rs.getMetaData();
                java.sql.DatabaseMetaData dbMetadata = connection.getMetaData();
                java.sql.ResultSet rsDBMetadata = dbMetadata.getPrimaryKeys(null, null, tableName);
                List<String> primaryColumns = new ArrayList<String>();
                while (rsDBMetadata.next()) {
                    primaryColumns.add(rsDBMetadata.getString("COLUMN_NAME")); //NOI18N
                }
                for (int i = 1; i <= rsMetadata.getColumnCount(); i++) {
                    TableColumn tableColumn = new TableColumn();
                    tableColumn.setColumnName(rsMetadata.getColumnName(i));
                    if (primaryColumns.contains(tableColumn.getColumnName())) {
                        tableColumn.setPrimaryKey(true);
                    }
                    columnNames.add(tableColumn);
                }
            } else {
                //TODO Cannot connect to the database. 
                // Need to handle this gracefully and display the error message.
                // throw new DatabaseException("Cannot connect to the database");
            }
        } catch (DatabaseException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SQLException sQLException) {
            Exceptions.printStackTrace(sQLException);
        }

        return columnNames;
    }

    public static String getDbConnectionDetails(HibernateConfiguration configuration, String property) {
        SessionFactory fact = configuration.getSessionFactory();
        if(fact == null) {
            logger.log(Level.WARNING, "Hibernate Configuration xml is missing <session-factory>, applying temporary fix, please check your xml.");
            configuration.setSessionFactory( new SessionFactory());
            fact = configuration.getSessionFactory();
            if(fact == null) {
                logger.log(Level.SEVERE, "Hibernate configuration xml is missing <session-factory>, xml must be fixed!");
                return "";
            }
        }
        int count = 0;
        for (String val : fact.getProperty2()) {
            String propName = fact.getAttributeValue(SessionFactory.PROPERTY2, count++, "name");  //NOI18N
            if (propName.equals(property)) {
                return val;
            }
        }

        return ""; //NOI18N
    }

    public static DatabaseConnection getDBConnection(HibernateConfiguration configuration)
            throws DatabaseException {
        try {

            String driverClassName = getDbConnectionDetails(configuration, "hibernate.connection.driver_class"); //NOI18N
            String driverURL = getDbConnectionDetails(configuration, "hibernate.connection.url"); //NOI18N
            String username = getDbConnectionDetails(configuration, "hibernate.connection.username"); //NOI18N
            String password = getDbConnectionDetails(configuration, "hibernate.connection.password"); //NOI18N
            //Hibernate allows abbrivated properties
            if (driverClassName == null) {
                driverClassName = getDbConnectionDetails(configuration, "connection.driver_class"); //NOI18N
            }
            if (driverURL == null) {
                driverURL = getDbConnectionDetails(configuration, "connection.url"); //NOI18N
            }
            if (username == null) {
                username = getDbConnectionDetails(configuration, "connection.username"); //NOI18N
            }
            if (password == null) {
                password = getDbConnectionDetails(configuration, "connection.password"); //NOI18N
            }

            // Try to get the pre-existing connection, if there's one already setup.
            DatabaseConnection[] dbConnections = ConnectionManager.getDefault().getConnections();
            for (DatabaseConnection dbConn : dbConnections) {
                if (dbConn.getDatabaseURL().equals(driverURL) &&
                        dbConn.getUser().equals(username)) {
                    logger.info("Found pre-existing database connection.");
                    return checkAndConnect(dbConn);
                }
            }
            JDBCDriver[] drivers = JDBCDriverManager.getDefault().getDrivers(driverClassName);
            //TODO check the driver here... it might not be loaded and driver[0] might result in AIOOB exception
            // The following is an annoying work around till #129633 is fixed.
            if (drivers.length == 0) {
                //throw new DatabaseException("Unable to load the driver : " + driverClassName);
                return null;
            }
            final DatabaseConnection dbConnection = DatabaseConnection.create(drivers[0], driverURL, username, null, password, true);
            ConnectionManager.getDefault().addConnection(dbConnection);

            return checkAndConnect(dbConnection);
        } catch (DatabaseException ex) {
            Exceptions.printStackTrace(ex);
            throw ex;
        }
    }

    private static DatabaseConnection checkAndConnect(final DatabaseConnection dbConnection) {
        if (dbConnection.getJDBCConnection() == null) {
            logger.info("Database Connection not established, connecting..");
            return Mutex.EVENT.readAccess(new Mutex.Action<DatabaseConnection>() {

                public DatabaseConnection run() {
                    ConnectionManager.getDefault().showConnectionDialog(dbConnection);
                    return dbConnection;
                }
            });

        } else {
            logger.info("Database Connection is pre-established. Returning the conneciton.");
            return dbConnection;
        }
    }

    public static Connection getDirectDBConnection(HibernateConfiguration configuration)
            throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {

            String driverClassName = getDbConnectionDetails(configuration, "hibernate.connection.driver_class"); //NOI18N
            String driverURL = getDbConnectionDetails(configuration, "hibernate.connection.url"); //NOI18N
            String username = getDbConnectionDetails(configuration, "hibernate.connection.username"); //NOI18N
            String password = getDbConnectionDetails(configuration, "hibernate.connection.password"); //NOI18N
            //Hibernate allows abbrivated properties
            if (driverClassName == null) {
                driverClassName = getDbConnectionDetails(configuration, "connection.driver_class"); //NOI18N
            }
            if (driverURL == null) {
                driverURL = getDbConnectionDetails(configuration, "connection.url"); //NOI18N
            }
            if (username == null) {
                username = getDbConnectionDetails(configuration, "connection.username"); //NOI18N
            }
            if (password == null) {
                password = getDbConnectionDetails(configuration, "connection.password"); //NOI18N
            }

            Class driverClass = Thread.currentThread().getContextClassLoader().loadClass(driverClassName);
            java.sql.Driver driver = (java.sql.Driver) driverClass.newInstance();
            // Establish the connection
            java.util.Properties info = new java.util.Properties();
            info.setProperty("user", username);
            info.setProperty("password", password);
            return driver.connect(driverURL, info);
    }

    public static String getRelativeSourcePath(FileObject file, FileObject sourceRoot) {
        String relativePath = "";
        try {
            String absolutePath = file.getPath();
            String sourceRootPath = sourceRoot.getPath();
            int index = absolutePath.indexOf(sourceRootPath);
            if (index == -1) {
                // file is not under the source root - constructing relativePath
                relativePath = constructRelativePath(absolutePath, sourceRootPath);
                if (relativePath==null) {
                    return "";
                }
                return relativePath;
            }
            relativePath = absolutePath.substring(index + sourceRootPath.length() + 1);
        } catch (Exception e) {
            logger.info("exception while parsing relative path");
            Exceptions.printStackTrace(e);
        }
        return relativePath;
    }

    private static Connection getJDBCConnection(HibernateConfiguration hibernateConfiguration) throws DatabaseException {
        if (getDBConnection(hibernateConfiguration) == null) {
            return null;
        }
        return getDBConnection(hibernateConfiguration).getJDBCConnection();
    }

    public static List<SourceGroup> getJavaSourceGroups(Project project) {
        assert project != null;
        SourceGroup[] sourceGroups = getSourceGroups(project);
        Set<SourceGroup> testGroups = getTestSourceGroups(sourceGroups);
        List<SourceGroup> result = new ArrayList<SourceGroup>();
        for (SourceGroup sourceGroup : sourceGroups) {
            if (!testGroups.contains(sourceGroup)) {
                result.add(sourceGroup);
            }
        }
        return result;
    }

    private static Set<SourceGroup> getTestSourceGroups(SourceGroup[] sourceGroups) {
        Map<FileObject, SourceGroup> foldersToSourceGroupsMap = createFoldersToSourceGroupsMap(sourceGroups);
        Set<SourceGroup> testGroups = new HashSet<SourceGroup>();
        for (SourceGroup sourceGroup : sourceGroups) {
            testGroups.addAll(getTestTargets(sourceGroup, foldersToSourceGroupsMap));
        }
        return testGroups;
    }

    private static Map<FileObject, SourceGroup> createFoldersToSourceGroupsMap(final SourceGroup[] sourceGroups) {
        if (sourceGroups.length == 0) {
            return Collections.emptyMap();
        }
        Map<FileObject, SourceGroup> result = new HashMap<FileObject, SourceGroup>(2 * sourceGroups.length);
        for (SourceGroup sourceGroup : sourceGroups) {
            result.put(sourceGroup.getRootFolder(), sourceGroup);
        }
        return result;
    }

    private static List<SourceGroup> getTestTargets(SourceGroup sourceGroup, Map<FileObject, SourceGroup> foldersToSourceGroupsMap) {
        final URL[] rootURLs = UnitTestForSourceQuery.findUnitTests(sourceGroup.getRootFolder());
        if (rootURLs.length == 0) {
            return Collections.emptyList();
        }
        List<FileObject> sourceRoots = getFileObjects(rootURLs);
        List<SourceGroup> result = new ArrayList<SourceGroup>(sourceRoots.size());
        for (FileObject sourceRoot : sourceRoots) {
            SourceGroup srcGroup = foldersToSourceGroupsMap.get(sourceRoot);
            if (srcGroup != null) {
                result.add(srcGroup);
            }
        }
        return result;
    }

    private static List<FileObject> getFileObjects(URL[] urls) {
        List<FileObject> result = new ArrayList<FileObject>(urls.length);
        for (URL url : urls) {
            FileObject sourceRoot = URLMapper.findFileObject(url);
            if (sourceRoot != null) {
                result.add(sourceRoot);
            } else {
                logger.log(Level.SEVERE, "No FileObject found for the following URL: " + url + " the test(s) will be skipped");
            }
        }
        return result;
    }

    /**
     * @see org.netbeans.modules.jumpto.file.FileSearchAction.Worker#getFileNames(String text)
     */
    private static List<FileObject> searchSourceFiles(Project project, String regExp, String mimeType) {
        // search index is enough because config files have to be underneath source folders
        List<FileObject> files = new LinkedList<FileObject>();

        Collection<? extends FileObject> roots = createFoldersToSourceGroupsMap(getSourceGroups(project)).keySet();
        try {
            QuerySupport q = QuerySupport.forRoots(
                    "org-netbeans-modules-jumpto-file-FileIndexer", // org.netbeans.modules.jumpto.file.FileIndexer.ID
                    1,                                              // org.netbeans.modules.jumpto.file.FileIndexer.VERSION
                    roots.toArray(new FileObject [roots.size()]));
            Collection<? extends IndexResult> results = q.query(
                    "ci-file-name",                                 // org.netbeans.modules.jumpto.file.FileIndexer.FIELD_CASE_INSENSITIVE_NAME
                    regExp,
                    QuerySupport.Kind.CASE_INSENSITIVE_REGEXP);
            for (IndexResult r : results) {
                FileObject file = r.getFile();
                if (file == null || !file.isValid()) {
                    // the file has been deleted in the meantime
                    continue;
                }
                if (mimeType == null
                        || mimeType.equals(FileUtil.getMIMEType(file, mimeType))) {
                    files.add(file);
                }
            }
        } catch (PatternSyntaxException pse) {
            assert false;
            return Collections.<FileObject>emptyList();
        } catch (IOException ioe) {
            logger.log(Level.WARNING, null, ioe);
            return Collections.<FileObject>emptyList();
        }

        // possible improvement (or fix ;)
        //  if scan is in progress (IndexingManager.isIndexing()) then it would be possible to traverse filesystem (but see issue #158261)

        return files;
    }

    /**
     * Returns the relative path from src to abs.
     *
     * @param      abs path to the target file   src path to the source directory.
     * @return     The relative path from src to abs or null.
     */
    private static String constructRelativePath(String abs, String src) {
        String[] field1 = abs.split("/");
        String[] field2 = src.split("/");
        StringBuilder result = null;
        int length = (field1.length>field2.length)?(field2.length):(field1.length);
        int numSameTokens = 0;
        boolean empty = true;
        for (int i=0; i<length; i++) {
            if ( field1[i].equals(field2[i]) ) {
                numSameTokens++;
                if (field1[i].length()>0) {
                    empty = false;
                }
            } else {
                break;
            }
        }
        if (empty) {
            return null;
        }
        result= new StringBuilder("");
        for (int i=numSameTokens;i<field2.length ;i++) {
            result.append("../");
        }
        for (int i=numSameTokens;i<field1.length ;i++) {
            result.append(field1[i]+"/");
        }
        if (result.charAt(result.length()-1) == '/' ) {
            result.deleteCharAt(result.length()-1);
        }
        return result.toString();
    }
}
