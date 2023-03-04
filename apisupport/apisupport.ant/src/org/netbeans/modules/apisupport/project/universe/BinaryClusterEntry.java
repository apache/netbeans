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
import java.net.URL;
import java.util.Set;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.openide.modules.Dependency;

/**
 * Entry for module in external binary cluster.
 * @author Richard Michalsky
 */
final class BinaryClusterEntry  extends AbstractBinaryEntry {
    private URL[] javadocRoots;
    private URL[] sourceRoots;

    public BinaryClusterEntry(String cnb, File jar, File[] exts, File clusterDir,
            String releaseVersion, String specVersion, String[] providedTokens,
            ManifestManager.PackageExport[] publicPackages, String[] friends,
            boolean deprecated, Set<Dependency> moduleDependencies,
            URL[] sourceRoots, URL[] javadocRoots) {
        super(cnb, jar, exts, clusterDir, releaseVersion, specVersion, providedTokens,
                publicPackages, friends, deprecated, moduleDependencies);
        this.javadocRoots = javadocRoots != null ? javadocRoots : new URL[0];
        this.sourceRoots = sourceRoots != null ? sourceRoots : new URL[0];
    }

    public File getSourceLocation() {
        return null;    // TODO C.P may actually return something meaningful?
    }

    public URL[] getSourceRoots() {
        return sourceRoots;
    }

    public URL[] getJavadocRoots() {
        return javadocRoots;
    }

    public String toString() {
//        File source = getSourceLocation();
        return "BinaryClusterEntry[" + getJarLocation() + /*(source != null ? "," + source : "") +*/ "]"; // NOI18N
    }
}
