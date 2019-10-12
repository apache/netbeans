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
package org.netbeans.modules.payara.jakartaee.db;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Java EE module for Payara features tests.
 * <p/>
 * @author Tomas Kraus
 */
public class HK2TestEEModuleImpl implements J2eeModuleImplementation2 {
    
    private final FileObject appRoot;
    private final File srcDir;
    private final File configDir;
    private final J2eeModule.Type moduleType;
    private final String moduleVersion;

    /** Creates a new instance of TestJ2eeModule
     * @param appRoot Application root directory.
     * @param moduleType Java EE module type.
     * @param moduleVersion Java EE version.
     */
    public HK2TestEEModuleImpl(
            final FileObject appRoot, final J2eeModule.Type moduleType,
            final String moduleVersion
    ) {
        this.appRoot = appRoot;
        this.srcDir = new File(FileUtil.toFile(appRoot), "src");
        this.configDir = new File(srcDir, "conf");
        this.moduleType = moduleType;
        this.moduleVersion = moduleVersion;
    }

    @Override
    public FileObject getArchive() {
        return null;
    }
    
    @Override
    public Iterator<J2eeModule.RootedEntry> getArchiveContents() {
        return Collections.<J2eeModule.RootedEntry>emptySet().iterator();
    }
    
    @Override
    public FileObject getContentDirectory() {
        return appRoot;
    }
    
    @Override
    public J2eeModule.Type getModuleType() {
        return moduleType;
    }
    
    @Override
    public String getModuleVersion() {
        return moduleVersion;
    }
    
    @Override
    public String getUrl() {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Override
    public File getResourceDirectory() {
        return new File(FileUtil.toFile(appRoot), "setup");
    }

    @Override
    public File getDeploymentConfigurationFile(String name) {
        return new File(configDir, name);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Override
    public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
        throw new UnsupportedOperationException("Not implemented.");
    }
}
