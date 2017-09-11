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
package org.netbeans.modules.hibernate.service.spi;

import java.net.MalformedURLException;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.hibernate.service.*;
import org.netbeans.modules.hibernate.service.api.HibernateEnvironment;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.HibernateException;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.hibernate.cfg.model.HibernateConfiguration;
import org.netbeans.modules.hibernate.cfg.model.SessionFactory;
import org.netbeans.modules.hibernate.loaders.cfg.HibernateCfgDataObject;
import org.netbeans.modules.hibernate.loaders.mapping.HibernateMappingDataObject;
import org.netbeans.modules.hibernate.mapping.model.MyClass;
import org.netbeans.modules.hibernate.util.CustomClassLoader;
import org.netbeans.modules.hibernate.util.HibernateUtil;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 * This class provides the Hibernate service for NetBeans projects.
 * This class abstracts the services provided by Hibernate and
 * wrapps them around to provide a more meaningful and high level services.
 *
 * @author Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 */
@ProjectServiceProvider(service=HibernateEnvironment.class, projectType={
    "org-netbeans-modules-maven",
    "org-netbeans-modules-java-j2seproject",
    "org-netbeans-modules-web-project"
}, projectTypes=@LookupProvider.Registration.ProjectType(id="org-netbeans-modules-ant-freeform", position=700))
public class HibernateEnvironmentImpl implements HibernateEnvironment {

    /** Handle to the current project to which this HibernateEnvironment is bound*/
    private Project project;
    private Logger logger = Logger.getLogger(HibernateEnvironmentImpl.class.getName());
    private WeakReference<CustomClassLoader> loaderRef;

    /**
     * Creates a new hibernate environment for this NetBeans project.
     *
     * @param project NB project.
     */
    public HibernateEnvironmentImpl(Project project) {
        this.project = project;
    }

    /**
     * Tries to load the JDBC driver read from the configuration.The classpath
     * used to load the driver class includes the project classpath.
     * 
     * @param config the Hibernate Configuration
     * @return true if JDBC driver class can be loaded, else false.
     */
    public boolean canLoadDBDriver(HibernateConfiguration config) {
        String dbDriver = HibernateUtil.getDbConnectionDetails(config,
                "hibernate.connection.driver_class"); //NOI18N

        if (dbDriver == null || "".equals(dbDriver)) {
            dbDriver = HibernateUtil.getDbConnectionDetails(config,
                    "connection.driver_class"); //NOI18N

        }

        if (dbDriver == null || "".equals(dbDriver)) {
            logger.info("dbDriver class could not be found from the config.");
            return false;
        }
        JDBCDriver jdbcDriver = null;

        DatabaseConnection dbConnection = null;
        try {
            dbConnection = HibernateUtil.getDBConnection(config);
        } catch (DatabaseException ex) {

        }
        if(dbConnection != null) {
            jdbcDriver = dbConnection.getJDBCDriver();
        }
        try {
//            CustomClassLoader ccl = new CustomClassLoader(
//                    getProjectClassPath(
//                    getAllHibernateConfigFileObjects().get(0)).toArray(new URL[]{}),
//                    this.getClass().getClassLoader());

            List<URL> urls = getProjectClassPath();
            if(jdbcDriver != null) {
                urls.addAll(Arrays.asList(jdbcDriver.getURLs()));
            }
            ClassLoader ccl = getProjectClassLoader(urls.toArray(new URL[]{}));
            ccl.loadClass(dbDriver);
            logger.info("dbDriver loaded.");
            return true;
        } catch (Exception e) {
            // Ignore, will fall through.
        }
        logger.info("Could not load dbDriver class. CNFE");
        return false;
    }

    /**
     * Checks for direct database connection establishment using the database access
     * details from the given HibernateConfiguration metadata.
     *
     * @param config HibernateConfiguration (schema2beans) object.
     * @return true if can connect to database, false otherwise.
     */
    public boolean canDirectlyConnectToDB(HibernateConfiguration config) {
        //
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader ccl = null;
        try {
            List<URL> urls = getProjectClassPath();
            //try first to open a connection, see #205183, it will open connection if registred in ide
            DatabaseConnection dbconn = HibernateUtil.getDBConnection(config);
            if (dbconn != null) {
                dbconn.getJDBCConnection();
                JDBCDriver driver = dbconn.getJDBCDriver();
                if(driver != null) {
                    urls.addAll(Arrays.asList(driver.getURLs()));
                }
            }
            //
            ccl = getProjectClassLoader(urls.toArray(new URL[]{}));
            Thread.currentThread().setContextClassLoader(ccl);
            HibernateUtil.getDirectDBConnection(config);
            logger.info("Direct Database connection established.");
            Thread.currentThread().setContextClassLoader(originalClassLoader);
            return true;
        } catch (Exception e) {
            logger.log(Level.INFO, "Cannot establish direct database connection", e);
        } finally {
            if(ccl != null) {
                Thread.currentThread().setContextClassLoader(originalClassLoader);
            }
        }
        return false;
    }


    public FileObject getSourceLocation() {
        SourceGroup[] sourceGroups = HibernateUtil.getSourceGroups(project);
        if (sourceGroups != null && sourceGroups.length != 0) {
            return sourceGroups[0].getRootFolder();
        }
        return null;
    }

    /**
     * Returns all tables found in the configurations present in this project.
     * 
     * @return list of table names.
     */
    public List<String> getAllDatabaseTablesForProject() {
        return getAllDatabaseTables(
                (HibernateUtil.getAllHibernateConfigurations(project)).toArray(new HibernateConfiguration[]{}));
    }

    public List<String> getDatabaseTables(FileObject mappingFile) {
        List<String> databaseTables = new ArrayList<String>();
        try {
            List<HibernateConfiguration> configurations = getHibernateConfigurationForMappingFile(mappingFile);
            if (configurations.size() == 0) {
                //This mapping file does not belong to any configuration file.
                return databaseTables;
            }
            databaseTables.addAll(HibernateUtil.getAllDatabaseTables(configurations.toArray(new HibernateConfiguration[]{})));
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        } catch (HibernateException ex) {
            Exceptions.printStackTrace(ex);
        }
        return databaseTables;
    }

    /**
     * Returns the list of 'HibernateConfiguration' (schema2beans bean) for 
     * the current project.
     * 
     * @return list of HibernateConfiguration(s).
     */
    public List<HibernateConfiguration> getAllHibernateConfigurationsFromProject() {
        return HibernateUtil.getAllHibernateConfigurations(project);
    }

    /**
     * Returns configuration fileobjects if any contained in this project.
     * @return list of FileObjects for configuration files if found in this project, otherwise empty list.
     */
    public List<FileObject> getAllHibernateConfigFileObjects() {
        return HibernateUtil.getAllHibernateConfigFileObjects(project);
    }

    /**
     * Returns configuration fileobjects if any contained under the source root in this project.
     * @return list of FileObjects for configuration files if found in this project, otherwise empty list.
     */
    public List<FileObject> getDefaultHibernateConfigFileObjects() {
        return HibernateUtil.getDefaultHibernateConfigFileObjects(project);
    }

    /**
     * Returns all mapping files defined under this project.
     * 
     * @return List of FileObjects for mapping files.
     */
    public List<FileObject> getAllHibernateMappingFileObjects() {
        return HibernateUtil.getAllHibernateMappingFileObjects(project);
    }

    /**
     * Returns relaive source paths of all mapping files present in this project.
     * 
     * @return List of FileObjects for mapping files.
     */
    public List<String> getAllHibernateMappings() {
        return HibernateUtil.getAllHibernateMappingsRelativeToSourcePath(project);
    }

    /**
     * Returns all reverse engineering files defined under this project.
     * 
     * @return List of FileObjects for reverse engg. files.
     */
    public List<FileObject> getAllHibernateReverseEnggFileObjects() {
        return HibernateUtil.getAllHibernateReverseEnggFileObjects(project);
    }

    /**
     * Connects to the DB using supplied HibernateConfigurations and gets the list of
     * all table names.
     *
     * @param configurations vararg of Hibernate Configurations.
     * @return array list of strings of table names.
     */
    public List<String> getAllDatabaseTables(HibernateConfiguration... configurations) {
        try {
            return HibernateUtil.getAllDatabaseTables(configurations);
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        } catch (HibernateException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }

    /**
     * Connects to the DB using supplied HibernateConfigurations and gets the list of
     * all table names. This method should be called from event thread.
     *
     * @param configuration Hibernate Configurations.
     * @return array list of strings of table names.
     */
    public List<String> getAllDatabaseTablesOnEventThread(FileObject configurationFO)
        throws DataObjectNotFoundException, DatabaseException, SQLException{
        return HibernateUtil.getAllDatabaseTablesOnEventThread(configurationFO);
    }

    /**
     * Returns the table column names for the given table.
     * 
     * @param tableName the table whose column names are needed.
     * @return the list of column names.
     */
    public List<TableColumn> getColumnsForTable(String tableName, FileObject mappingFileObject) {
        List<TableColumn> columnNames = new ArrayList<TableColumn>();
        List<HibernateConfiguration> hibernateConfigurations =
                getHibernateConfigurationForMappingFile(mappingFileObject);
        if (hibernateConfigurations.size() == 0) {
            // This mapping fileis not (yet) mapped to any config file.
            return columnNames;
        } else {
            for (HibernateConfiguration hibernateConfiguration : hibernateConfigurations) {
                columnNames.addAll(HibernateUtil.getColumnsForTable(
                        tableName,
                        hibernateConfiguration));
            }
        }

        return columnNames;
    }

    /**
     * Registers Hibernate Library in this project.
     * 
     * @return true if the library is registered, false if the library is already registered or 
     * registration fails for some reason.
     */
    public boolean addHibernateLibraryToProject(FileObject fileInProject) {
        boolean addLibraryResult = false;
        try {
            LibraryManager libraryManager = LibraryManager.getDefault();
            Library hibernateLibrary = libraryManager.getLibrary("hibernate4-support");  //NOI18N
            Library ejb3PersistenceLibrary = libraryManager.getLibrary("jpa2-persistence");  //NOI18N

            Library[] librariesToBeRegistered = null;

            // Bugfix: 140811
            project.getProjectDirectory().getFileSystem().refresh(true);
            // Adding ejb3-persistence.jar if project classpath doesn't contain it            
            ClassPath cp = ClassPath.getClassPath(fileInProject, ClassPath.EXECUTE);
            if (cp == null) {
                logger.info("Cannot register libraries because cannot get the classpath for created file : " + fileInProject);
                return false;
            }
            if (!containsClass(cp, "javax.persistence.EntityManager")) { // NOI18N                
                librariesToBeRegistered = new Library[]{hibernateLibrary, ejb3PersistenceLibrary};
            } else {
                librariesToBeRegistered = new Library[]{hibernateLibrary};
            }

            addLibraryResult = ProjectClassPathModifier.addLibraries(librariesToBeRegistered, fileInProject, ClassPath.COMPILE);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            addLibraryResult = false;
        } catch (UnsupportedOperationException ex) {
            // PCPM not implemented for this project. May be a freeform project ?
            logger.info("Library registration not possible : " + project);
        }
        return addLibraryResult;
    }

    /**
     * Returns all mappings registered with this HibernateConfiguration.
     *
     * @param hibernateConfiguration hibernate configuration.
     * @return list of mapping files.
     */
    public List<String> getAllHibernateMappingsFromConfiguration(HibernateConfiguration hibernateConfiguration) {
        List<String> mappingsFromConfiguration = new ArrayList<String>();
        SessionFactory fact = hibernateConfiguration.getSessionFactory();
        List<String> mappingsFromJavaPackage = new ArrayList<String>();
        int count = 0;
        for (boolean val : fact.getMapping()) {
            String propName = fact.getAttributeValue(SessionFactory.MAPPING,
                    count, "resource"); //NOI18N
            if (propName != null) {
                mappingsFromConfiguration.add(propName);
            }
            propName = fact.getAttributeValue(SessionFactory.MAPPING,
                    count, "file"); //NOI18N
            if (propName != null) {
                mappingsFromConfiguration.add(propName);
            }
            propName = fact.getAttributeValue(SessionFactory.MAPPING,
                    count, "package"); //NOI18N
            if (propName != null) {
                mappingsFromJavaPackage.add(propName);
            }
            count++;
        }

        // Process mappings from Java Package(s).
        if (mappingsFromJavaPackage.size() != 0) {
            List<String> allMappingFilesRelativeToSourceRoot = HibernateUtil.getAllHibernateMappingsRelativeToSourcePath(project);
            for (String mappingRelativeToSourceRoot : allMappingFilesRelativeToSourceRoot) {
                for (String mappingFromPackage : mappingsFromJavaPackage) {
                    mappingFromPackage = mappingFromPackage.replace(".", "/");
                    if (mappingRelativeToSourceRoot.startsWith(mappingFromPackage)) {
                        mappingsFromConfiguration.add(mappingRelativeToSourceRoot);
                    }
                }
            }
        }
        return mappingsFromConfiguration;
    }

    private List<HibernateConfiguration> getHibernateConfigurationForMappingFile(FileObject mappingFileObject) {
        List<HibernateConfiguration> hibernateConfigurations = new ArrayList<HibernateConfiguration>();
        for (HibernateConfiguration config : getAllHibernateConfigurationsFromProject()) {
            for (String mappingFile : getAllHibernateMappingsFromConfiguration(config)) {
                if (!mappingFile.trim().equals("") && mappingFileObject.getPath().contains(mappingFile)) {
                    hibernateConfigurations.add(config);
                }
            }
        }
        return hibernateConfigurations;
    }

    /**
     * Returns the project classpath including project build paths.
     * Can be used to set classpath for custom classloader.
     * 
     * @param projectFile file in current project.
     * @return List of java.io.File objects representing each entry on the classpath.
     */
    public List<URL> getProjectClassPath(FileObject projectFile) {
        return HibernateUtil.getProjectClassPathEntries(projectFile);
    }

    /**
     * Returns the project classpath including project build paths.
     * Can be used to set classpath for custom classloader.
     * 
     * @return List of java.io.File objects representing each entry on the classpath.
     */
    public List<URL> getProjectClassPath() {
        return HibernateUtil.getProjectClassPath(project);
    }

    /**
     * Returns the NetBeans project to which this HibernateEnvironment instance is bound.
     *
     * @return NetBeans project.
     */
    public Project getProject() {
        return project;
    }

    /**
     * Returns list of annotated POJO (FQN) classnames (String) found in this 
     * Hibernate configuration.
     * 
     * @param configurationFO hibernate configuration FileObject.
     * @return List of classnames (FQN) (String) of all annotated POJO classes found in this configuration or an empty list.
     */
    public List<String> getAnnotatedPOJOClassNames(FileObject configurationFO) {
        List<String> annototedPOJOClassNameList = new ArrayList<String>();

        try {
            HibernateCfgDataObject configDO = (HibernateCfgDataObject) DataObject.find(configurationFO);
            HibernateConfiguration configuration = configDO.getHibernateConfiguration();
            SessionFactory fact = configuration.getSessionFactory();
            int count = 0;
            for (boolean val : fact.getMapping()) {
                String propName = fact.getAttributeValue(SessionFactory.MAPPING,
                        count++, "class"); //NOI18N
                if (propName != null) {
                    annototedPOJOClassNameList.add(propName);
                }
            }
        } catch (DataObjectNotFoundException dataObjectNotFoundException) {
            logger.log(Level.INFO, "DataObject not found during Annototated POJO procesing.", dataObjectNotFoundException);
        }
        return annototedPOJOClassNameList;
    }

    /**
     * Returns a map of mapping file objects and list of names of Java classes (POJOs) that are defined in 
     * that mapping file for this configuration.
     * 
     * @param configFileObject the configuration FileObject.
     * @return Map of mapping FileObject with List of POJO class names.
     */
    public Map<FileObject, List<String>> getAllPOJONamesFromConfiguration(FileObject configFileObject) {
        Map<FileObject, List<String>> mappingPOJOMap = new HashMap<FileObject, List<String>>();
        try {
            HibernateCfgDataObject hibernateCfgDO = (HibernateCfgDataObject) DataObject.find(configFileObject);
            for (String mappingFileName : getAllHibernateMappingsFromConfiguration(hibernateCfgDO.getHibernateConfiguration())) {
                for (FileObject mappingFO : getAllHibernateMappingFileObjects()) {
                    if (mappingFileName.contains(mappingFO.getName())) {
                        List<String> l = getPOJONameFromMapping(mappingFO);
                        mappingPOJOMap.put(mappingFO, l);
                    }
                }
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }

        return mappingPOJOMap;
    }

    private List<String> getPOJONameFromMapping(FileObject mappingFO) {
        List<String> pojoNamesList = new ArrayList<String>();
        try {
            HibernateMappingDataObject hibernateMappingDO = (HibernateMappingDataObject) DataObject.find(mappingFO);
            for (MyClass myClass : hibernateMappingDO.getHibernateMapping().getMyClass()) {
                String propName = myClass.getAttributeValue("name");
                pojoNamesList.add(propName);
            }

        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return pojoNamesList;
    }

    /**
     *@return true if the given classpath contains a class with the given name.
     */
    private boolean containsClass(ClassPath cp, String className) {
        String classRelativePath = className.replace('.', '/') + ".class"; //NOI18N

        return cp.findResource(classRelativePath) != null;
    }

    /**
     * Registers the selected DB Driver with the project.
     * @param driver the driver classname.
     * @param primaryFile a file in the project. Used to extend the classpath.
     * @return true if successfully registered the given driver or false, if there's problem with registering.
     */
    public boolean registerDBDriver(String driver, FileObject primaryFile) {
        boolean registeredDBDriver = false;

        JDBCDriver[] jdbcDrivers = JDBCDriverManager.getDefault().getDrivers(driver);
        List<URL> driverURLs = new ArrayList<URL>();
        for (JDBCDriver jdbcDriver : jdbcDrivers) {
            for (URL url : jdbcDriver.getURLs()) {
                java.io.File file = null;
                if (url.getProtocol().equals("nbinst")) { //NOI18N
                    try {
                        file = InstalledFileLocator.getDefault().locate(url.getFile().substring(1), null, false);
                        logger.info("Bundled DB Driver Jar : " + file);
                        if (file == null) {
                            continue;
                        }
                        url = file.toURI().toURL();
                    } catch (MalformedURLException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    logger.info("User provided DB Driver Jar : " + url);
                }
                if (FileUtil.isArchiveFile(url)) {
                    driverURLs.add(FileUtil.getArchiveRoot(url));
                } else {
                    driverURLs.add(url);
                }
            }
        }

        try {
                registeredDBDriver = ProjectClassPathModifier.addRoots(
                        driverURLs.toArray(new URL[]{}),
                        primaryFile,
                        ClassPath.COMPILE);

        } catch (UnsupportedOperationException e) {
            // PCPM is not defined for this project. May be freeform project ?
            logger.info("DB Driver registration not possible : " + project);
            registeredDBDriver = false;
        } catch (Exception e) {
            registeredDBDriver = false;
            logger.log(Level.INFO, "Problem in registering db driver.", e);
        }

        return registeredDBDriver;
    }

    /**
     * Prepares and returns a custom classloader for this project.
     * The classloader is capable of loading project classes and resources.
     * 
     * @param classpaths, custom classpaths that are registered along with project based classpath.
     * @return classloader which is a URLClassLoader instance.
     */
    @Override
    public ClassLoader getProjectClassLoader(URL[] classpaths) {
        CustomClassLoader customClassLoader = new CustomClassLoader(classpaths, getClass().getClassLoader());
        if(loaderRef !=null){
            CustomClassLoader cL = loaderRef.get();
            if(cL != null){
                URL[] oldUrls = cL.getURLs();
                classpaths = customClassLoader.getURLs();//reassign just in case if CustomClassLoader may modify assigned urls to have proper comparison below.
                if(Arrays.equals(oldUrls, classpaths)) {
                    return cL;
                }
            }
        }
        loaderRef = new WeakReference<CustomClassLoader>((CustomClassLoader) customClassLoader);
        return customClassLoader;
    }

}
