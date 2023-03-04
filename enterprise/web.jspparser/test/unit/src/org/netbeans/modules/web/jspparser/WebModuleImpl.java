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

package org.netbeans.modules.web.jspparser;

import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation;
import org.openide.filesystems.FileObject;

/**
 * Dummy implementation of WebModule for testing memory leaks.
 * @author Tomas Mysik
 */
public class WebModuleImpl implements WebModuleImplementation {
    private final FileObject documentRoot;

    public WebModuleImpl(FileObject documentRoot) {
        this.documentRoot = documentRoot;
    }

    public FileObject getDocumentBase() {
        return documentRoot;
    }

    public String getContextPath() {
        return null;
    }

    public String getJ2eePlatformVersion() {
        return WebModule.JAVA_EE_5_LEVEL;
    }

    public FileObject getWebInf() {
        return documentRoot.getFileObject("WEB-INF");
    }

    public FileObject getDeploymentDescriptor() {
        return null;
    }

    public FileObject[] getJavaSources() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MetadataModel<WebAppMetadata> getMetadataModel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
