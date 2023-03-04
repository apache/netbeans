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

package org.netbeans.installer.infra.build.ant.registries;

import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.installer.infra.lib.registries.ManagerException;
import org.netbeans.installer.infra.lib.registries.impl.RegistriesManagerImpl;

/**
 *
 * @author ks152834
 */
public class AddPackage extends Task {
    private File root;
    private File archive;
    private String parentUid;
    private String parentVersion;
    private String parentPlatforms;
    
    public void setRoot(final File root) {
        this.root = root;
    }

    public void setArchive(final File archive) {
        this.archive = archive;
    }

    public void setUid(final String parentUid) {
        this.parentUid = parentUid;
    }

    public void setVersion(final String parentVersion) {
        this.parentVersion = parentVersion;
    }

    public void setPlatforms(final String parentPlatforms) {
        this.parentPlatforms = parentPlatforms;
    }
    
    @Override
    public void execute() throws BuildException {
        try {
            new RegistriesManagerImpl().addPackage(
                    root, 
                    archive, 
                    parentUid, 
                    parentVersion, 
                    parentPlatforms);
        } catch (ManagerException e) {
            throw new BuildException(e);
        }
    }
}
