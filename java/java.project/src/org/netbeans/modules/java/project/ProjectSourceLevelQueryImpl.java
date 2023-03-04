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
package org.netbeans.modules.java.project;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;

/**
 * Finds project owning given file, SourceLevelQueryImplementation impl in its
 * lookup and delegates question to it.
 * @author David Konecny
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.queries.SourceLevelQueryImplementation.class, position=100)
@SuppressWarnings("deprecation")
public class ProjectSourceLevelQueryImpl implements org.netbeans.spi.java.queries.SourceLevelQueryImplementation {

    /** Default constructor for lookup. */
    public ProjectSourceLevelQueryImpl() {}

    public String getSourceLevel(org.openide.filesystems.FileObject javaFile) {
        Project project = FileOwnerQuery.getOwner(javaFile);
        if (project != null) {
            org.netbeans.spi.java.queries.SourceLevelQueryImplementation slq = project.getLookup().lookup(org.netbeans.spi.java.queries.SourceLevelQueryImplementation.class);
            if (slq != null) {
                return slq.getSourceLevel(javaFile);
            }
        }
        return null;
    }
    
}
