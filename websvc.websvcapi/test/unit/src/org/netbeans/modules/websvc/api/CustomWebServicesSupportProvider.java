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

package org.netbeans.modules.websvc.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportFactory;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportImpl;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Lukas Jungmann
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.spi.webservices.WebServicesSupportProvider.class)
public class CustomWebServicesSupportProvider implements WebServicesSupportProvider {
    
    private Map<FileObject, WebServicesSupport> cache = new HashMap<FileObject, WebServicesSupport>();
    
    /** Creates a new instance of CustomWebServicesSupportProvider */
    public CustomWebServicesSupportProvider() {
    }
    
    public WebServicesSupport findWebServicesSupport(FileObject file) {
        if (file.getExt().equals("ws")) {
            WebServicesSupport em  =  cache.get(file.getParent());
            if (em == null) {
                em = WebServicesSupportFactory.createWebServicesSupport(new CustomWebServicesSupport(file));
                cache.put(file.getParent(), em);
            }
            return em;
        }
        return null;
    }
    
    private static final class CustomWebServicesSupport implements WebServicesSupportImpl {
        private FileObject fo;
        
        CustomWebServicesSupport(FileObject fo) {
            this.fo = fo;
        }
        
        public void addServiceImpl(String serviceName, FileObject configFile,
                boolean fromWSDL, String[] wscompileFeatures) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void addServiceImpl(String serviceName, FileObject configFile,
                boolean fromWSDL) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void addServiceEntriesToDD(String serviceName,
                String serviceEndpointInterface,
                String serviceEndpoint) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public FileObject getWebservicesDD() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public FileObject getWsDDFolder() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public String getArchiveDDFolderName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public String getImplementationBean(String linkName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void removeServiceEntry(String linkName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void removeProjectEntries(String serviceName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public AntProjectHelper getAntProjectHelper() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public String generateImplementationBean(String wsName, FileObject pkg,
                Project project,
                String delegateData) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void addServiceImplLinkEntry(ServiceImplBean serviceImplBean,
                String wsName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public ReferenceHelper getReferenceHelper() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public List getServices() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void addInfrastructure(String implBeanClass, FileObject pkg) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public boolean isFromWSDL(String serviceName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public ClassPath getClassPath() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
}
