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

package org.netbeans.modules.cordova.project;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cordova.platforms.spi.Device;
import org.netbeans.modules.cordova.platforms.spi.MobilePlatform;
import org.netbeans.modules.cordova.platforms.api.PlatformManager;
import org.netbeans.modules.cordova.platforms.spi.PropertyProvider;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserImplementation;
import org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer;
import org.netbeans.modules.web.clientproject.spi.platform.RefreshOnSaveListener;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectConfiguration;
import org.openide.filesystems.FileObject;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;

/**
 *
 */
public class MobileConfigurationImpl implements ProjectConfiguration, PropertyProvider {

    private final Project project;
    //final private ClientProjectPlatformImpl platform;
    private final String name;
    private final String displayName;
    private final String type;
    private final EditableProperties props;
    private final FileObject file;

    private MobileConfigurationImpl(Project project, FileObject kid, String id, String displayName, String type, EditableProperties ep) {
        this.project = project;
        this.name = id;
        this.displayName = displayName;
        this.type = type;
        this.props = ep;
        this.file = kid;
    }
    
//    @Override
    public String getId() {
        return name;
    }

//    @Override
    public void save() {
        if (file == null) {
            return;
        }
        OutputStream os = null;
        try {
            os = file.getOutputStream();
            try {
                props.store(os);
            } finally {
                os.close();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                os.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public String getType() {
        return type;
    }
    
    public Device getDevice() {
        return PlatformManager.getPlatform(type).getDevice(name, props);
    }

    @Override
    public String getProperty(String prop) {
        return props.getProperty(prop);
    }
    
    @Override
    public String putProperty(String prop, String value) {
        return props.put(prop, value);
    }

    public static MobileConfigurationImpl create(Project project, String id) {
        FileObject configFile = project.getProjectDirectory().getFileObject("nbproject/configs/" + id + ".properties"); // NOI18N
        assert configFile != null : "missing configuration file for id: " + id;
        return create(project, configFile); 
    }

    public static MobileConfigurationImpl create(Project proj, FileObject configFile) {
        try {
            InputStream is = configFile.getInputStream();
            try {
                EditableProperties p = new EditableProperties(true);
                p.load(is);
                String id = configFile.getName();
                String label = p.getProperty("display.name"); // NOI18N
                String type = p.getProperty("type"); //NOI18N
                return new MobileConfigurationImpl(proj, configFile, id, label != null ? label : id, type, p);
            } finally {
                is.close();
            }
        } catch (IOException x) {
            throw new IllegalStateException(x);
        }
    }
    
    @Override
    public String getDisplayName() {
        return displayName;
    }


//    @Override
    public boolean canBeDeleted() {
        return true;
    }

//    @Override
    public void delete() {
        try {
            file.delete();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
