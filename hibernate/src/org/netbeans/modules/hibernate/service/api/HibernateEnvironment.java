/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
