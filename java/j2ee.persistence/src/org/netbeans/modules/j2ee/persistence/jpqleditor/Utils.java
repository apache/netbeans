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
package org.netbeans.modules.j2ee.persistence.jpqleditor;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.j2ee.persistence.api.PersistenceEnvironment;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.editor.JPAEditorUtil;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibrarySupport;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author sp153251
 */
public class Utils {

    /**
     * test persistence unit and produce properties to sunstitute ee environment
     * with se environment (to execut jpql on se level)
     *
     * @param pe
     * @param pu
     * @param props - map will be filled with properties
     * @return return possible problems (missed paths etc.)
     */
    public static List<String> substitutePersistenceProperties(PersistenceEnvironment pe, PersistenceUnit pu, DatabaseConnection dbconn, Map<String, String> props) {
        final boolean containerManaged = Util.isSupportedJavaEEVersion(pe.getProject());
        final Provider provider = ProviderUtil.getProvider(pu.getProvider(), pe.getProject());
        ArrayList<String> problems = new ArrayList<>();
        if (containerManaged) {
            props.put("javax.persistence.provider", provider.getProviderClass());//NOI18N
            props.put("javax.persistence.transactionType", "RESOURCE_LOCAL");//NOI18N
            if (dbconn != null) {
                props.put(provider.getJdbcUrl(), dbconn.getDatabaseURL());
                props.put(provider.getJdbcDriver(), dbconn.getDriverClass());
                props.put(provider.getJdbcUsername(), dbconn.getUser());
                props.put(provider.getJdbcPassword(), dbconn.getPassword());
            } 
        }
        return problems;
    }

    /**
     * create URLs from project base to use as classpath later
     * also adds jdbc and provider f necessary and if available
     * @param pe
     * @param localResourcesURLList
     * @return possible problems (missed paths etc.)
     */
    public static List<String> collectClassPathURLs(PersistenceEnvironment pe, PersistenceUnit pu, DatabaseConnection dbconn, List<URL> localResourcesURLList) {
        final boolean containerManaged = Util.isSupportedJavaEEVersion(pe.getProject());
        final Provider provider = ProviderUtil.getProvider(pu.getProvider(), pe.getProject());
        ArrayList<String> problems = new ArrayList<>();
        // Construct custom classpath here.
        List<URL> projectURLs = pe.getProjectClassPath(pe.getLocation());
        int sources_count = 0;
        for(URL url:projectURLs) {
            if("file".equals(url.getProtocol())) {
                try {
                    if(new java.io.File(url.toURI().getPath()).exists()) {
                        sources_count++;
                        break;
                    }
                } catch (URISyntaxException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        if(provider == null) {
            //we have no valid provider, either no provider tag or no server to get default provider
            problems.add(NbBundle.getMessage(Utils.class, "NoValidProvider"));//NOI18N
        } else if(sources_count == 0) {
            //we have no valid classpath entries from a project, it may be because it wasn't build at least once
            problems.add(NbBundle.getMessage(Utils.class, "NoValidClasspath"));//NOI18N
            //no need to continue in this case
        } else if (pe.getLocation() == null || pe.getLocation().getFileObject("persistence.xml")==null) {
            problems.add(NbBundle.getMessage(Utils.class, "NoValidPersistenceXml"));//NOI18N
        } else {
            localResourcesURLList.addAll(projectURLs);
            localResourcesURLList.add(pe.getLocation().getParent().toURL());
            localResourcesURLList.add(pe.getLocation().toURL());
            localResourcesURLList.add(pe.getLocation().getFileObject("persistence.xml").toURL());
            SourceGroup[] sgs = ProjectUtils.getSources(pe.getProject()).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            FileObject sourceRoot = sgs[0].getRootFolder();
            ClassPathProvider cpProv = pe.getProject().getLookup().lookup(ClassPathProvider.class);
            ClassPath cp = cpProv.findClassPath(sourceRoot, ClassPath.EXECUTE);
            if(cp == null){
                cp = cpProv.findClassPath(sourceRoot, ClassPath.COMPILE);
            }
            if (containerManaged) {
                String providerClassName = provider.getProviderClass();
                String resourceName = providerClassName.replace('.', '/') + ".class"; // NOI18N
                if (cp != null) {
                    FileObject fob = cp.findResource(resourceName); // NOI18N
                    if (fob == null) {
                        Library library = PersistenceLibrarySupport.getLibrary(provider);
                        if (library != null) {
                            localResourcesURLList.addAll(library.getContent("classpath"));//NOI18N
                        } else {
                            problems.add(NbBundle.getMessage(Utils.class, "ProviderAbsent"));//NOI18N
                        }
                    }
                }
            }
            if (dbconn != null) {
                //autoadd driver classpath
                String driverClassName = dbconn.getDriverClass();
                String resourceName = driverClassName.replace('.', '/') + ".class"; // NOI18N
                if (cp != null) {
                    FileObject fob = cp.findResource(resourceName); // NOI18N
                    if (fob == null) {
                        JDBCDriver[] driver = JDBCDriverManager.getDefault().getDrivers(driverClassName);
                        if (driver != null && driver.length > 0) {
                            localResourcesURLList.addAll(Arrays.asList(driver[0].getURLs()));
                        } else {
                            problems.add(NbBundle.getMessage(Utils.class, "DriverAbsent"));//NOI18N
                        }
                    }
                }
            } else {
                HashMap<String,String> props = JPAEditorUtil.findDatabaseConnectionProperties(pu, pe.getProject());
                if(props == null) {
                    problems.add(NbBundle.getMessage(Utils.class, "DatabaseDataAbsent"));//NOI18N                    
                } else {
                    problems.add(NbBundle.getMessage(Utils.class, "DatabaseConnectionAbsent", props.get(JPAEditorUtil.JDBCURLKEY), props.get(JPAEditorUtil.JDBCDRIVERKEY), props.get(JPAEditorUtil.JDBCUSERKEY)));//NOI18N
                }
            }
        }
        return problems;
    }
}
