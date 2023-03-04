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

package org.netbeans.modules.websvc.manager.test;

import java.io.File;

/**
 *
 * @author quynguyen
 */
public class SetupData {
    private File localWsdlFile = null;
    private File localCatalogFile = null;
    private File localOriginalWsdl = null;
    
    private File websvcHome = null;
    
    public SetupData() {
    }

    public File getLocalCatalogFile() {
        return localCatalogFile;
    }

    public void setLocalCatalogFile(File localCatalogFile) {
        this.localCatalogFile = localCatalogFile;
    }

    public File getLocalOriginalWsdl() {
        return localOriginalWsdl;
    }

    public void setLocalOriginalWsdl(File localOriginalWsdl) {
        this.localOriginalWsdl = localOriginalWsdl;
    }

    public File getLocalWsdlFile() {
        return localWsdlFile;
    }

    public void setLocalWsdlFile(File localWsdlFile) {
        this.localWsdlFile = localWsdlFile;
    }

    public File getWebsvcHome() {
        return websvcHome;
    }

    public void setWebsvcHome(File websvcHome) {
        this.websvcHome = websvcHome;
    }
    
    
}
