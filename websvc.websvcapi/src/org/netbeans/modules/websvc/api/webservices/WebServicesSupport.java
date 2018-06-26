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

package org.netbeans.modules.websvc.api.webservices;

import java.util.Iterator;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.websvc.webservices.WebServicesSupportAccessor;
import org.netbeans.modules.websvc.spi.webservices.*;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/** WebServicesSupport should be used to manipulate a projects representation
 *  of a web service implementation.
 * <p>
 * A client may obtain a WebServicesSupport instance using
 * <code>WebServicesSupport.getWebServicesSupport(fileObject)</code> static
 * method, for any FileObject in the project directory structure.
 *
 * @author Peter Williams
 */
public final class WebServicesSupport {
    
    private WebServicesSupportImpl impl;
    private static final Lookup.Result implementations =
    Lookup.getDefault().lookup(new Lookup.Template(WebServicesSupportProvider.class));
    
    static  {
        WebServicesSupportAccessor.DEFAULT = new WebServicesSupportAccessor() {
            public WebServicesSupport createWebServicesSupport(WebServicesSupportImpl spiWebServicesSupport) {
                return new WebServicesSupport(spiWebServicesSupport);
            }
            
            public WebServicesSupportImpl getWebServicesSupportImpl(WebServicesSupport wss) {
                return wss == null ? null : wss.impl;
            }
        };
    }
    
    private WebServicesSupport(WebServicesSupportImpl impl) {
        if (impl == null)
            throw new IllegalArgumentException();
        this.impl = impl;
    }
    
    /** Find the WebServicesSupport for given file or null if the file does not belong
     * to any module support web services.
     */
    public static WebServicesSupport getWebServicesSupport(FileObject f) {
        if (f == null) {
            throw new NullPointerException("Passed null to WebServicesSupport.getWebServicesSupport(FileObject)"); // NOI18N
        }
        Iterator it = implementations.allInstances().iterator();
        while (it.hasNext()) {
            WebServicesSupportProvider impl = (WebServicesSupportProvider)it.next();
            WebServicesSupport wss = impl.findWebServicesSupport(f);
            if (wss != null) {
                return wss;
            }
        }
        return null;
    }
    
    // Delegated methods from WebServicesSupportImpl
    
    public void addServiceImpl(String serviceName, FileObject configFile, boolean fromWSDL) {
        impl.addServiceImpl(serviceName, configFile, fromWSDL);
    }
    
    public void addServiceEntriesToDD(String serviceName, String serviceEndpointInterface, String servantClassName){
        impl.addServiceEntriesToDD(serviceName, serviceEndpointInterface, servantClassName);
    }
    
    public FileObject getWebservicesDD() {
        return impl.getWebservicesDD();
    }
    
    public FileObject getWsDDFolder() {
        return impl.getWsDDFolder();
    }
    
    public String getArchiveDDFolderName() {
        return impl.getArchiveDDFolderName();
    }
    
    public String getImplementationBean(String linkName) {
        return impl.getImplementationBean(linkName);
    }
    
    public void removeServiceEntry(String linkName) {
        impl.removeServiceEntry(linkName);
    }
    
    public void removeProjectEntries(String serviceName){
        impl.removeProjectEntries(serviceName);
    }
    
    public AntProjectHelper getAntProjectHelper() {
        return impl.getAntProjectHelper();
    }
    
    public String  generateImplementationBean(String name, FileObject pkg, Project project, String delegateData) throws java.io.IOException {
        return impl.generateImplementationBean(name, pkg, project, delegateData);
    }
    
    public void addServiceImplLinkEntry(ServiceImplBean serviceImplBean, String wsName) {
        impl.addServiceImplLinkEntry(serviceImplBean, wsName);
    }
    
    public ReferenceHelper getReferenceHelper(){
        return impl.getReferenceHelper();
    }
    
    public List/*WsCompileEditorSupport.ServiceSettings*/ getServices() {
        return impl.getServices();
    }
    
    public void addInfrastructure(String implBeanClass, FileObject pkg) {
        impl.addInfrastructure(implBeanClass, pkg);
    }
    
    public boolean isFromWSDL(String serviceName) {
        return impl.isFromWSDL(serviceName);
    }
    
    public void addServiceImpl(String serviceName, FileObject configFile, boolean fromWSDL, String[] wscompileFeatures) {
        impl.addServiceImpl(serviceName, configFile, fromWSDL,wscompileFeatures);
    }
    
    public boolean isBroken(Project  project) {
        return (getWebServicesSupport(project.getProjectDirectory()) == null && !getServices().isEmpty());
    }
    
    public void showBrokenAlert(Project  project) {
        ProjectInformation pi = ProjectUtils.getInformation(project);
        String projectName = null;
        if(pi !=null) projectName = pi.getDisplayName();
        NotifyDescriptor alert = new NotifyDescriptor.Message(
                NbBundle.getMessage(WebServicesSupport.class, 
                "ERR_NoJaxrpcPluginFound", projectName), NotifyDescriptor.WARNING_MESSAGE);
        DialogDisplayer.getDefault().notifyLater(alert);
    }
}
