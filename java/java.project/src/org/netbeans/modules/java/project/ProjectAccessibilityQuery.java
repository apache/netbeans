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
import org.netbeans.spi.java.queries.AccessibilityQueryImplementation;
import org.openide.filesystems.FileObject;

/**
 * Delegates {@link AccessibilityQueryImplementation} to the project which
 * owns the affected source folder.
 * @author Jesse Glick
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.queries.AccessibilityQueryImplementation.class)
public class ProjectAccessibilityQuery implements AccessibilityQueryImplementation {

    /** Default constructor for lookup. */
    public ProjectAccessibilityQuery() {}

    public Boolean isPubliclyAccessible(FileObject pkg) {
        Project project = FileOwnerQuery.getOwner(pkg);
        if (project != null) {
            AccessibilityQueryImplementation aqi = project.getLookup().lookup(AccessibilityQueryImplementation.class);
            if (aqi != null) {
                return aqi.isPubliclyAccessible(pkg);
            }
        }
        return null;
    }
    
}
