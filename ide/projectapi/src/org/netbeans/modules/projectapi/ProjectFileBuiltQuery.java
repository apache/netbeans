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

package org.netbeans.modules.projectapi;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.FileBuiltQuery;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.filesystems.FileObject;

/**
 * Delegates {@link FileBuiltQuery} to implementations in project lookup.
 * @author Jesse Glick
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.queries.FileBuiltQueryImplementation.class)
public class ProjectFileBuiltQuery implements FileBuiltQueryImplementation {

    /** Default constructor for lookup. */
    public ProjectFileBuiltQuery() {}
    
    public FileBuiltQuery.Status getStatus(FileObject file) {
        Project p = FileOwnerQuery.getOwner(file);
        if (p != null) {
            FileBuiltQueryImplementation fbqi = p.getLookup().lookup(FileBuiltQueryImplementation.class);
            if (fbqi != null) {
                return fbqi.getStatus(file);
            }
        }
        return null;
    }
    
}
