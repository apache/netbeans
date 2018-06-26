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
