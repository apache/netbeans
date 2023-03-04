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

package org.netbeans.modules.websvc.jaxws;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSSupportFactory;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSSupportImpl;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSSupportProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Lukas Jungmann
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.jaxws.spi.JAXWSSupportProvider.class)
public class CustomJAXWSSupportProvider implements JAXWSSupportProvider {
    
   private Map<FileObject, JAXWSSupport> cache = new HashMap<FileObject, JAXWSSupport>();
   
    /** Creates a new instance of TestProjectJAXWSSupportProvider */
    public CustomJAXWSSupportProvider() {
    }
    
    public JAXWSSupport findJAXWSSupport(FileObject file) {
        if (file.getExt().equals ("ws")) {
            JAXWSSupport em  =  cache.get(file.getParent());
            if (em == null) {
                em = JAXWSSupportFactory.createJAXWSSupport(new CustomJAXWSSupport(file.getParent()));
                cache.put(file.getParent(), em);
            }
            return em;
        }
        return null;
    }
    
    private static class CustomJAXWSSupport implements JAXWSSupportImpl {
        
        private FileObject fo;
        
        CustomJAXWSSupport(FileObject fo) {
            this.fo = fo;
        }
        
        public void addService(String serviceName, String serviceImpl,
                boolean isJsr109) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public String addService(String name, String serviceImpl, String wsdlUrl,
                String serviceName, String portName,
                String packageName, boolean isJsr109, boolean useProvider) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public List getServices() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void removeService(String serviceName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void serviceFromJavaRemoved(String serviceName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public String getServiceImpl(String serviceName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public boolean isFromWSDL(String serviceName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public FileObject getWsdlFolder(boolean create) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public FileObject getLocalWsdlFolderForService(String serviceName,
                boolean createFolder) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public FileObject getBindingsFolderForService(String serviceName,
                boolean createFolder) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public AntProjectHelper getAntProjectHelper() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public URL getCatalog() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public String getWsdlLocation(String serviceName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void removeNonJsr109Entries(String serviceName) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public FileObject getDeploymentDescriptorFolder() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public MetadataModel<WebservicesMetadata> getWebservicesMetadataModel() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
