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

package org.netbeans.modules.web.core;

import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author den
 */
public class TestWebModuleImplementation implements WebModuleImplementation{
    
    private TestWebModuleImplementation(){
    }
    
    public static TestWebModuleImplementation getInstance(){
        return INSTANCE;
    }

    @Override
    public FileObject getDocumentBase() {
        return null;
    }

    @Override
    public String getContextPath() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getJ2eePlatformVersion() {
        return profile.toString();
    }

    @Override
    public FileObject getWebInf() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FileObject getDeploymentDescriptor() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FileObject[] getJavaSources() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MetadataModel<WebAppMetadata> getMetadataModel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    void setJeeProfile( Profile profile){
        this.profile = profile;
    }
    
    private static final TestWebModuleImplementation INSTANCE = new TestWebModuleImplementation();
    private Profile profile;
    
}
