/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.payara.eecommon.api.config;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;

/**
 *
 * @author Peter Williams
 */
public class FolderListener implements FileChangeListener {

    public static FileChangeListener createListener(File key, FileObject folder, J2eeModule.Type type) {
        return new FolderListener(key, folder, type);
    }

    private final File configKey;
    private final String [] targets;
    
    private FolderListener(File key, FileObject folder, J2eeModule.Type type) {
        configKey = key;
        if(type == J2eeModule.Type.WAR) {
            targets = new String [] { "web.xml", "webservices.xml" };
        } else if(type == J2eeModule.Type.EJB) {
            targets = new String [] { "ejb-jar.xml", "webservices.xml" };
        } else if(type == J2eeModule.Type.EAR) {
            targets = new String [] { "application.xml" };
        } else if(type == J2eeModule.Type.CAR) {
            targets = new String [] { "application-client.xml" };
        } else {
            Logger.getLogger("payara-eecommon").log(Level.WARNING, "Unsupported module type: " + type);
            targets = new String [0];
        }
        
        folder.addFileChangeListener(this);
    }
    
    public void fileFolderCreated(FileEvent fe) {
    }

    public void fileDataCreated(FileEvent fe) {
        FileObject fo = fe.getFile();
        for(String target: targets) {
            if(target.equals(fo.getNameExt())) {
                PayaraConfiguration config = PayaraConfiguration.getConfiguration(configKey);
                if(config != null) {
                    config.addDescriptorListener(fo);
                }
            }
        }
    }
    
    public void fileChanged(FileEvent fe) {
    }

    public void fileDeleted(FileEvent fe) {
    }

    public void fileRenamed(FileRenameEvent fe) {
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
    }

}
