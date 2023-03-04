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

package org.netbeans.modules.websvc.client;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.websvc.api.client.ClientStubDescriptor;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportFactory;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportImpl;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportProvider;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportFactory;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportImpl;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author Lukas Jungmann
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.spi.client.WebServicesClientSupportProvider.class)
public class CustomWebServicesClientSupportProvider implements WebServicesClientSupportProvider {
    
    private Map<FileObject, WebServicesClientSupport> cache = new HashMap<FileObject, WebServicesClientSupport>();
    private Map<FileObject, JAXWSClientSupport> cache2 = new HashMap<FileObject, JAXWSClientSupport>();
    
    /** Creates a new instance of CustomWebServicesSupportProvider */
    public CustomWebServicesClientSupportProvider() {
    }
    
    public WebServicesClientSupport findWebServicesClientSupport(FileObject file) {
        if (file.getExt().equals("ws") || file.getExt().equals("both")) {
            WebServicesClientSupport em  =  (WebServicesClientSupport) cache.get(file.getParent());
            if (em == null) {
                em = WebServicesClientSupportFactory.createWebServicesClientSupport(new CustomWebServicesClientSupportImpl(file));
                cache.put(file.getParent(), em);
            }
            return em;
        }
        return null;
    }
    
    public JAXWSClientSupport findJAXWSClientSupport(FileObject file) {
        if (file.getExt().equals("jaxws") || file.getExt().equals("both")) {
            JAXWSClientSupport em = cache2.get(file.getParent());
            if (em == null) {
                em = JAXWSClientSupportFactory.createJAXWSClientSupport(new CustomJAXWSClientSupportImpl(file));
                cache2.put(file.getParent(), em);
            }
            return em;
        }
        return null;
    }
    
    private static final class CustomWebServicesClientSupportImpl implements WebServicesClientSupportImpl {
        
        private FileObject fo;
        
        CustomWebServicesClientSupportImpl(FileObject fo) {
            this.fo = fo;
        }
        
        public void addServiceClient(String serviceName, String packageName,
                String sourceUrl, FileObject configFile,
                ClientStubDescriptor stubDescriptor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void addServiceClient(String serviceName, String packageName,
                String sourceUrl, FileObject configFile,
                ClientStubDescriptor stubDescriptor,
                String[] wscompileFeatures) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void addServiceClientReference(String serviceName,
                String fqServiceName,
                String relativeWsdlPath,
                String mappingPath,
                String[] portSEIInfo) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void removeServiceClient(String serviceName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public FileObject getWsdlFolder(boolean create) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public FileObject getDeploymentDescriptor() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public List getStubDescriptors() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public List getServiceClients() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public String getWsdlSource(String serviceName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void setWsdlSource(String serviceName, String wsdlSource) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void setProxyJVMOptions(String proxyHost, String proxyPort) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public String getServiceRefName(String serviceName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    private static final class CustomJAXWSClientSupportImpl implements JAXWSClientSupportImpl {
        
        private FileObject fo;
        
        CustomJAXWSClientSupportImpl(FileObject fo) {
            this.fo = fo;
        }
        
        public String addServiceClient(String clientName, String wsdlUrl,
                String packageName, boolean isJsr109) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public FileObject getWsdlFolder(boolean create) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public FileObject getLocalWsdlFolderForClient(String clientName,
                boolean createFolder) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public FileObject getBindingsFolderForClient(String clientName,
                boolean createFolder) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void removeServiceClient(String serviceName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public List getServiceClients() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public URL getCatalog() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public String getServiceRefName(Node clientNode) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

	public AntProjectHelper getAntProjectHelper() {
            throw new UnsupportedOperationException("Not supported yet.");
	}
    }
}
