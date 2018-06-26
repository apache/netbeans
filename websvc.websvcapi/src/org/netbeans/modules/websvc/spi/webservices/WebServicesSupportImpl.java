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

package org.netbeans.modules.websvc.spi.webservices;

import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;

import org.openide.filesystems.FileObject;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean;
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
    public List/*WsCompileEditorSupport.ServiceSettings*/ getServices();
    
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
