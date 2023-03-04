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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.TestEntry;
import org.netbeans.spi.project.FileOwnerQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * Associated built module files with their owning project.
 * @author Jesse Glick
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.project.FileOwnerQueryImplementation.class, position=50)
public final class UpdateTrackingFileOwnerQuery implements FileOwnerQueryImplementation {
    
    /** Default constructor for lookup. */
    public UpdateTrackingFileOwnerQuery() {}

    public Project getOwner(URI file) {
        if (!ModuleList.existKnownEntries()) {
            return null; // #65700
        }
        if (file.getScheme().equals("file")) { // NOI18N
            return getOwner(Utilities.toFile(file));
        } else {
            return null;
        }
    }

    public Project getOwner(FileObject file) {
        if (!ModuleList.existKnownEntries()) {
            return null; // #65700
        }
        File f = FileUtil.toFile(file);
        if (f != null) {
            return getOwner(f);
        } else {
            return null;
        }
    }
    
    private Project getOwner(File file) {
        for (ModuleEntry entry : ModuleList.getKnownEntries(file)) {
            File sourcedir = entry.getSourceLocation();
            if (sourcedir != null) {
                FileObject sourcedirFO = FileUtil.toFileObject(sourcedir);
                if (sourcedirFO != null) {
                    try {
                        Project p = ProjectManager.getDefault().findProject(sourcedirFO);
                        if (p != null) {
                            return p;
                        }
                    } catch (IOException e) {
                        Util.err.notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            }
        }
        // may be tests.jar in tests distribution  
        TestEntry test = TestEntry.get(file);
        if (test != null) {
            Project p = test.getProject() ;
            if (p != null) {
                return p;
            }
        }
        return null;
    }
    
}
