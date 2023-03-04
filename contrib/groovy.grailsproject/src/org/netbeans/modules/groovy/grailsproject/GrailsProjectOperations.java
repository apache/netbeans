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

package org.netbeans.modules.groovy.grailsproject;

import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.openide.filesystems.FileObject;

/**
 *
 * @author schmidtm
 */
public class GrailsProjectOperations implements DeleteOperationImplementation {

    private final GrailsProject project;

    public GrailsProjectOperations(GrailsProject project) {
        this.project = project;
    }

    public void notifyDeleting() throws IOException {
        return;
    }

    public void notifyDeleted() throws IOException {
        project.getProjectState().notifyDeleted();
    }
    
    public List<FileObject> getMetadataFiles() {
        // we don't write any metadata, if some will be added in future, add them here
        return Collections.emptyList();
    }

    public List<FileObject> getDataFiles() {
        return Arrays.asList(project.getProjectDirectory().getChildren());
    }

}
