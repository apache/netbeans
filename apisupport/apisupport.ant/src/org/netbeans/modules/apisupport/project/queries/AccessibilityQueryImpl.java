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

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.SuppressWarnings;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.spi.java.queries.AccessibilityQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 * Says which module packages are accessible.
 * @author Jesse Glick
 */
public final class AccessibilityQueryImpl implements AccessibilityQueryImplementation {

    private final NbModuleProject project;

    public AccessibilityQueryImpl(NbModuleProject project) {
        this.project = project;
    }

    @SuppressWarnings("NP_BOOLEAN_RETURN_NULL")
      @Override public @CheckForNull Boolean isPubliclyAccessible(@NonNull FileObject pkg) {
        FileObject srcdir = project.getSourceDirectory();
        if (srcdir != null) {
            String path = FileUtil.getRelativePath(srcdir, pkg);
            if (path != null) {
                String name = path.replace('/', '.');
                Element config = project.getPrimaryConfigurationData();
                Element pubPkgs = XMLUtil.findElement(config, "public-packages", NbModuleProject.NAMESPACE_SHARED); // NOI18N
                if (pubPkgs == null) {
                    // Try <friend-packages> too.
                    pubPkgs = XMLUtil.findElement(config, "friend-packages", NbModuleProject.NAMESPACE_SHARED); // NOI18N
                }
                if (pubPkgs != null) {
                    for (Element pubPkg : XMLUtil.findSubElements(pubPkgs)) {
                        boolean sub = "subpackages".equals(pubPkg.getLocalName()); // NOI18N
                        String pubPkgS = XMLUtil.findText(pubPkg);
                        if (name.equals(pubPkgS) || (sub && name.startsWith(pubPkgS + '.'))) {
                            return true;
                        }
                    }
                    // Everything else assumed to *not* be public.
                    return false;
                } else {
                    Util.err.log(ErrorManager.WARNING, "Invalid project.xml for " + project);
                }
            }
        }
        return null;
    }

}
