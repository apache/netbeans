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

package org.netbeans.modules.java.testrunner;

import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;

/**
 *
 * @author Marian Petras
 */
public final class OutputUtils {

    static final Action[] NO_ACTIONS = new Action[0];
    
    private OutputUtils() {
    }

    public static JavaPlatform getActivePlatform(final String activePlatformId) {
        final JavaPlatformManager pm = JavaPlatformManager.getDefault();
        if (activePlatformId == null) {
            return pm.getDefaultPlatform();
        } else {
            JavaPlatform[] installedPlatforms = pm.getInstalledPlatforms();
            for (JavaPlatform p : installedPlatforms) {
                String name = p.getSpecification().getName();
                String antName = p.getProperties().get("platform.ant.name"); // NOI18N
                if (name != null && name.equals("j2se") && antName != null && antName.equals(activePlatformId)) { // NOI18N
                    return p;
                }
            }
            return null;
        }
    }

    /**
     * Returns {@code ActionProvider} that is associated with a project
     * containing the specified {@code fileObject}.
     *
     * @param fileObject the file object.
     * @return an {@code ActionProvider}, or {@code null} if there is no
     *         known project containing the {@code fileObject}.
     *
     * @see ActionProvider
     * @see FileOwnerQuery#getOwner(org.openide.filesystems.FileObject)
     */
    public static ActionProvider getActionProvider(FileObject fileObject) {
        Project owner = FileOwnerQuery.getOwner(fileObject);
        if(owner == null) { // #183586
            return null;
        }
        return owner.getLookup().lookup(ActionProvider.class);
    }

}
