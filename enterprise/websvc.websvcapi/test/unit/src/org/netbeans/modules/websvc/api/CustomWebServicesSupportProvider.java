/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
