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

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.websvc.api.webservices.WebServicesView;
import org.netbeans.modules.websvc.spi.webservices.WebServicesViewFactory;
import org.netbeans.modules.websvc.spi.webservices.WebServicesViewImpl;
import org.netbeans.modules.websvc.spi.webservices.WebServicesViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author Lukas Jungmann
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.spi.webservices.WebServicesViewProvider.class)
public class CustomWebServicesViewProvider implements WebServicesViewProvider {
    
    private Map<FileObject, WebServicesView> cache = new HashMap<FileObject, WebServicesView>();
    
    /** Creates a new instance of CustomJAXWSViewProvider */
    public CustomWebServicesViewProvider() {
    }
    
    public WebServicesView findWebServicesView(FileObject file) {
        if (file.getExt().equals("ws")) {
            WebServicesView em  =  cache.get(file.getParent());
            if (em == null) {
                em = WebServicesViewFactory.createWebServicesView(new CustomWebServicesViewImpl(file));
                cache.put(file.getParent(), em);
            }
            return em;
        }
        return null;
    }
    
    private static final class CustomWebServicesViewImpl implements WebServicesViewImpl {
        
        private FileObject fo;
        
        CustomWebServicesViewImpl(FileObject fo) {
            this.fo = fo;
        }
        
        public Node createWebServicesView(FileObject srcRoot) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
