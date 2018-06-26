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
