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
package org.netbeans.modules.hibernate.service.api;

import java.sql.SQLException;
import org.netbeans.modules.hibernate.service.*;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hibernate.cfg.model.HibernateConfiguration;
import org.netbeans.modules.hibernate.spi.hibernate.HibernateFileLocationProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * This inteface defines Hibernate services for NetBeans projects.
 *
 * @author Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 */
public interface HibernateEnvironment extends HibernateFileLocationProvider {

    /**
     * Returns the list of names of Java classes (POJOs) that are defined in
     * this configuration through mapping files or directly using annotation.
     *
     * @param configFileObject the configuration FileObject.
     * @return List of Strings with names of the Java classes.
     */
    public Map<FileObject, List<String>> getAllPOJONamesFromConfiguration(FileObject configFileObject);

    /**
     * Returns list of annotated POJO (FQN) classnames (String) found in this 
     * Hibernate configuration.
     * 
     * @param configurationFO hibernate configuration FleObject.
     * @return List of classnames (FQN) (String) of all annotated POJO classes found in this configuration or an empty list.
     */
    public List<String> getAnnotatedPOJOClassNames(FileObject configurationFO);

    /**
     * Registers the selected DB Driver with the project.
     * @param driver the driver classname.
     * @param primaryFile a file in the project. Used to extend the classpath.
     * @return true if sucessfully registered the given driver or false, if there's problem with registering.
     */
    public boolean registerDBDriver(String driver, FileObject primaryFile);
    
    /**
     * Registers Hibernate Library in this project.
     *
     * @return true if the library is registered, false if the library is already registered or
     * registration fails for some reason.
     */
    boolean addHibernateLibraryToProject(FileObject fileInProject);

    /**
     * Connects to the DB using supplied HibernateConfigurations and gets the list of
     * all table names.
     *
     * @param configurations vararg of Hibernate Configurations.
     * @return array list of strings of table names.
     */
    List<String> getAllDatabaseTables(HibernateConfiguration... configurations);

    /**
     * Connects to the DB using supplied HibernateConfigurations and gets the list of
     * all table names. This method should be called from event thread.
     *
     * @param configuration Hibernate Configurations.
     * @return array list of strings of table names.
     */
    List<String> getAllDatabaseTablesOnEventThread(FileObject configurationFO) throws DataObjectNotFoundException, DatabaseException, SQLException;
    
    /**
     * Returns all tables found in the configurations present in this project.
     *
     * @return list of table names.
     */
    List<String> getAllDatabaseTablesForProject();

    /**
     * Returns configuration fileobjects if any contained in this project.
     * @return list of FileObjects for configuration files if found in this project, otherwise empty list.
     */
    List<FileObject> getAllHibernateConfigFileObjects();
    
    /**
     * Returns configuration fileobjects if any contained under the source root in this project.
     * @return list of FileObjects for configuration files if found in this project, otherwise empty list.
     */
    List<FileObject> getDefaultHibernateConfigFileObjects();
    

    /**
     * Returns the list of 'HibernateConfiguration' (schema2beans bean) for
     * the current project.
     *
     * @return list of HibernateConfiguration(s).
     */
    List<HibernateConfiguration> getAllHibernateConfigurationsFromProject();

    /**
     * Returns all mapping files defined under this project.
     *
     * @return List of FileObjects for mapping files.
     */
    List<FileObject> getAllHibernateMappingFileObjects();

    /**
     * Returns relaive source paths of all mapping files present in this project.
     *
     * @return List of FileObjects for mapping files.
     */
    List<String> getAllHibernateMappings();

    /**
     * Returns all mappings registered with this HibernateConfiguration.
     *
     * @param hibernateConfiguration hibernate configuration.
     * @return list of mapping files.
     */
    List<String> getAllHibernateMappingsFromConfiguration(HibernateConfiguration hibernateConfiguration);

    /**
     * Returns all reverse engineering files defined under this project.
     *
     * @return List of FileObjects for reverse engg. files.
     */
    List<FileObject> getAllHibernateReverseEnggFileObjects();

    /**
     * Returns the table column names for the given table.
     *
     * @param tableName the table whose column names are needed.
     * @return the list of column names.
     */
    List<TableColumn> getColumnsForTable(String tableName, FileObject mappingFileObject);

    List<String> getDatabaseTables(FileObject mappingFile);

    FileObject getSourceLocation();
    
    /**
     * Prepares and returns a custom classloader for this project.
     * The classloader is capable of loading project classes and resources.
     * 
     * @param classpaths, custom classpaths that are registered along with project based classpath.
     * @return classloader which is a URLClassLoader instance.
     */
    ClassLoader getProjectClassLoader(URL[] classpaths);

    /**
     * Returns the NetBeans project to which this HibernateEnvironment instance is bound.
     *
     * @return NetBeans project.
     */
    Project getProject();

    /**
     * Returns the project classpath including project build paths.
     * Can be used to set classpath for custom classloader.
     *
     * @param projectFile file in current project.
     * @return List of java.io.File objects representing each entry on the classpath.
     */
    List<URL> getProjectClassPath(FileObject projectFile);
    
    /**
     * Returns the project classpath including project build paths.
     * Can be used to set classpath for custom classloader.
     * 
     * @return List of java.io.File objects representing each entry on the classpath.
     */
    List<URL> getProjectClassPath();

    /**
     * Tries to load the JDBC driver read from the configuration.The classpath
     * used to load the driver class includes the project classpath.
     * 
     * @param config the Hibernate Configuration
     * @return true if JDBC driver class can be loaded, else false.
     */
    boolean canLoadDBDriver(HibernateConfiguration config);

    /**
     * Checks for direct database connection establishment using the database access
     * details from the given HibernateConfiguration metadata.
     *
     * @param config HibernateConfiguration (schema2beans) object.
     * @return true if can connect to database, false otherwise.
     */
    boolean canDirectlyConnectToDB(HibernateConfiguration config);
}
