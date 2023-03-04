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

package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.openide.modules.Dependency;

final class BinaryEntry extends AbstractBinaryEntry {
    private final File nbdestdir;
    
    public BinaryEntry(String cnb, File jar, File[] exts, @NonNull File nbdestdir, File clusterDir,
            String releaseVersion, String specVersion, String[] providedTokens,
            ManifestManager.PackageExport[] publicPackages, String[] friends,
            boolean deprecated, Set<Dependency> moduleDependencies) {
        super(cnb, jar, exts, clusterDir, releaseVersion, specVersion, providedTokens,
                publicPackages, friends, deprecated, moduleDependencies);
        this.nbdestdir = nbdestdir;
        if (nbdestdir == null)
            throw new NullPointerException("nbdestdir must not be null.");    // NOI18N
    }
    
    //private boolean recurring;
    public File getSourceLocation() {
        NbPlatform platform = NbPlatform.getPlatformByDestDir(nbdestdir, null);
            /*
            assert !recurring : jar;
            recurring = true;
            try {
             */
        return platform.getSourceLocationOfModule(getJarLocation());
            /*
            } finally {
                recurring = false;
            }
             */
    }
    
    public String toString() {
        File source = getSourceLocation();
        return "BinaryEntry[" + getJarLocation() + (source != null ? "," + source : "") + "]"; // NOI18N
    }
}
