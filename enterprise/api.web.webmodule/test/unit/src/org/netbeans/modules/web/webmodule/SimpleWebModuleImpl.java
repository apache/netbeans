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
package org.netbeans.modules.web.webmodule;

import java.beans.PropertyChangeListener;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation2;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Andrei Badea
 */
public class SimpleWebModuleImpl implements WebModuleImplementation2 {

    public String getContextPath() {
        return null;
    }

    public FileObject getDocumentBase() {
        return null;
    }

    public Profile getJ2eeProfile() {
        return null;
    }

    public FileObject getDeploymentDescriptor() {
        return null;
    }

    public FileObject getWebInf() {
        return null;
    }

    public FileObject[] getJavaSources() {
        return new FileObject[0];
    }

    public MetadataModel<WebAppMetadata> getMetadataModel() {
        return null;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }
}
