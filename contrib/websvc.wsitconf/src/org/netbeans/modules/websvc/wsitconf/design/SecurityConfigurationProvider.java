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

package org.netbeans.modules.websvc.wsitconf.design;


import java.util.Hashtable;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.design.configuration.WSConfiguration;
import org.netbeans.modules.websvc.design.configuration.WSConfigurationProvider;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;

/**
 *
 * @author rico
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.design.configuration.WSConfigurationProvider.class)
public class SecurityConfigurationProvider implements WSConfigurationProvider {
    
    private Hashtable<String, WSConfiguration> configProviders = new Hashtable<String, WSConfiguration>();
    
    /** Creates a new instance of SecurityConfigurationProvider */
    public SecurityConfigurationProvider() {
    }

    public synchronized WSConfiguration getWSConfiguration(Service service, FileObject implementationFile) {
        String key = "";
        if ((implementationFile == null) || (service == null)) {
            return null;
        }
        key += service.getLocalWsdlFile();
        key += implementationFile.getPath();
        if (!configProviders.containsKey(key)) {
            configProviders.put(key, new SecurityConfiguration(service, implementationFile));
        }
        implementationFile.addFileChangeListener(new FCListener());
        return configProviders.get(key);
    }
    
    private class FCListener implements FileChangeListener {

        public void fileDeleted(FileEvent fe) {
            if (fe == null) return;
            FileObject f = fe.getFile();
            if (f == null) return;
            
            for (String s : configProviders.keySet()) {
                if (s.contains(f.getPath())) {
                    configProviders.remove(s);
                    break;
                }
            }
        }

        public void fileRenamed(FileRenameEvent fe) {}
        public void fileFolderCreated(FileEvent fe) {}
        public void fileDataCreated(FileEvent fe) {}
        public void fileChanged(FileEvent fe) {}
        public void fileAttributeChanged(FileAttributeEvent fe) {}
    }
    
}
