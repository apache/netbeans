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

package org.netbeans.tests.j2eeserver.devmodule;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.xml.sax.SAXException;
import org.netbeans.modules.j2ee.metadata.model.api.SimpleMetadataModelImpl;

/**
 *
 * @author  sherold
 */
public class TestJ2eeModuleImpl implements J2eeModuleImplementation2 {
    
    private final FileObject webAppRoot;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private final MetadataModel<WebAppMetadata> webAppMetadata;

    /** Creates a new instance of TestJ2eeModule */
    public TestJ2eeModuleImpl(FileObject webAppRoot) throws IOException, SAXException {
        this.webAppRoot = webAppRoot;
        webAppMetadata = MetadataModelFactory.createMetadataModel(new SimpleMetadataModelImpl<WebAppMetadata>());
    }

    public FileObject getArchive() {
        return null;
    }
    
    @Override public Iterator<J2eeModule.RootedEntry> getArchiveContents() {
        return Collections.<J2eeModule.RootedEntry>emptySet().iterator();
    }
    
    public FileObject getContentDirectory() {
        return webAppRoot;
    }
    
    public J2eeModule.Type getModuleType() {
        return J2eeModule.Type.EJB;
    }
    
    public String getModuleVersion() {
        return J2eeModule.JAVA_EE_5;
    }
    
    public String getUrl() {
        return null;
    }
    
    public void setUrl(String url) {
        // noop
    }

    public File getResourceDirectory() {
        return new File(FileUtil.toFile(webAppRoot), "resources");
    }

    public File getDeploymentConfigurationFile(String name) {
        if (name.equals(J2eeModule.WEB_XML)) {
            return new File(FileUtil.toFile(webAppRoot), name);
        } else {
            return null;
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
    
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
        if (type == WebAppMetadata.class) {
            return (MetadataModel<T>) webAppMetadata;
        } else {
            return null;
        }
    }
}
