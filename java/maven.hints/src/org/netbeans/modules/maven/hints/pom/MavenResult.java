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
package org.netbeans.modules.maven.hints.pom;

import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.filesystems.FileObject;

/**
 * Result of POM 'parsing'. Contains {@link POMModel} build from the pom file or
 * editor contents.
 * 
 * @author sdedic
 */
public final class MavenResult extends Parser.Result {
    
    private final POMModel projectModel;
    private final FileObject pomFile;

    public MavenResult(POMModel projectModel, FileObject pomFile, Snapshot _snapshot) {
        super(_snapshot);
        this.projectModel = projectModel;
        this.pomFile = pomFile;
    }

    public FileObject getPomFile() {
        return pomFile;
    }

    public POMModel getProjectModel() {
        return projectModel;
    }

    @Override
    protected void invalidate() {
    }
    
}
