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

package org.netbeans.modules.websvc.spi.webservices;

import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;

import org.openide.filesystems.FileObject;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean;
import org.netbeans.modules.websvc.api.webservices.WsCompileEditorSupport;
import org.netbeans.spi.project.support.ant.ReferenceHelper;

/**
 *
 * @author Peter Williams
 */
public interface WebServicesSupportImpl {

    /*
     * Add web service related entries to the project.properties and project.xml files
     */
    public void addServiceImpl(String serviceName, FileObject configFile, boolean fromWSDL, String[] wscompileFeatures);
    
    /*
     * Add web service related entries to the project.properties and project.xml files
     */
    public void addServiceImpl(String serviceName, FileObject configFile, boolean fromWSDL);
    
    /**
     * Add web service entries to the module's deployment descriptor
     */
    public void addServiceEntriesToDD(String serviceName, String serviceEndpointInterface, String serviceEndpoint);
    
    /**
     * Get the FileObject of the webservices.xml file.
     */
    public FileObject getWebservicesDD();
    
    /**
     *  Returns the directory that contains webservices.xml in the project
     */
    public FileObject getWsDDFolder();
    
    /**
     * Returns the name of the directory that contains the webservices.xml in
     * the archive
     */
    public String getArchiveDDFolderName();
    
    /**
     * Returns the name of the implementation bean class
     * given the servlet-link or ejb-link name
     */
    public String getImplementationBean(String linkName);
    
    /**
     *  Given the servlet-link or ejb-link, remove the servlet or
     *  ejb entry in the module's deployment descriptor.
     */
    public void removeServiceEntry(String linkName);
    
    /**
     * Remove the web service entries from the project properties
     * project.xml files
     */
    public void removeProjectEntries(String serviceName);
    
    /**
     * Get the AntProjectHelper from the project
     */
    public AntProjectHelper getAntProjectHelper();
    
    /**
     * Generate the implementation bean class and return the class name
     */
    public String generateImplementationBean(String wsName, FileObject pkg, Project project, String delegateData)throws java.io.IOException;
    
    /**
     *  Add the servlet link or ejb link in the webservices.xml entry
     */
    public void addServiceImplLinkEntry(ServiceImplBean serviceImplBean, String wsName);
    
    /**
     * Get the ReferenceHelper from the project
     */
    public ReferenceHelper getReferenceHelper();
    
    /**
     * Get the list of services and their wscompile settings.
     */
    public List<WsCompileEditorSupport.ServiceSettings> getServices();
    
    /**
     * Add infrastructure methods and fields (if any) that should be present
     * in the implementation bean class
     */
    public void addInfrastructure(String implBeanClass, FileObject pkg);
    
    /**
     * Determine if the web service was created from WSDL
     */
    public boolean isFromWSDL(String serviceName);
    
    /**
     * Provide classpath where annotations define webservices
     */
    public ClassPath getClassPath();
    
}
