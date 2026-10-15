/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.project;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.queries.AccessibilityQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 * Delegates {@link AccessibilityQueryImplementation2} to the project which
 * owns the affected source folder.
 * @author Tomas Zezula
 */
@ServiceProvider(service = AccessibilityQueryImplementation2.class, position=100)
public final class ProjectAccessibilityQuery2 implements AccessibilityQueryImplementation2 {

    public ProjectAccessibilityQuery2() {}

    @CheckForNull
    @Override
    public Result isPubliclyAccessible(@NonNull final FileObject pkg) {
        final Project project = FileOwnerQuery.getOwner(pkg);
        if (project != null) {
            AccessibilityQueryImplementation2 aqi = project.getLookup().lookup(AccessibilityQueryImplementation2.class);
            if (aqi != null) {
                return aqi.isPubliclyAccessible(pkg);
            }
        }
        return null;
    }
}
