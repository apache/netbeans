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

package org.netbeans.modules.projectimport.eclipse.core.spi;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.projectimport.eclipse.core.ProjectOpenHookImpl;
import org.netbeans.modules.projectimport.eclipse.core.UpgradableProject;
import org.netbeans.spi.project.LookupProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

// registered separately in j2se and web modules, otherwise could use @LookupProvider.Registration
public final class UpgradableProjectLookupProvider implements LookupProvider {

    public Lookup createAdditionalLookup(Lookup baseContext) {
        Project p = baseContext.lookup(Project.class);
        assert p != null;
        if (!isRegistered(p)) { // NOI18N
            // Shortcut, the normal case:
            return Lookup.EMPTY;
        } else {
            return Lookups.fixed(
                new UpgradableProject(p),
                new ProjectOpenHookImpl());
        }
    }

    /**
     * Quickly checks whether a project is registered with the importer.
     */
    public static boolean isRegistered(Project p) {
        return ProjectUtils.getPreferences(p, UpgradableProjectLookupProvider.class, true).get("project", null) != null;
    }

}
