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

package org.netbeans.api.project.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.ant.AntArtifactQueryImplementation;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * Find out how to create a certain build artifact by calling some Ant script.
 * @see AntArtifactQueryImplementation
 * @author Jesse Glick
 */
public class AntArtifactQuery {
    
    private AntArtifactQuery() {}
    
    /**
     * Try to find an Ant artifact object corresponding to a given file on disk.
     * The file need not currently exist for the query to succeed.
     * All registered {@link AntArtifactQueryImplementation} providers are asked.
     * @param file a file which might be built by some Ant target
     * @return an Ant artifact object describing it, or null if it is not recognized
     */
    public static AntArtifact findArtifactFromFile(File file) {
        if (!file.equals(FileUtil.normalizeFile(file))) {
            throw new IllegalArgumentException("Parameter file was not "+  // NOI18N
                "normalized. Was "+file+" instead of "+FileUtil.normalizeFile(file));  // NOI18N
        }
        for (AntArtifactQueryImplementation aaqi : Lookup.getDefault().lookupAll(AntArtifactQueryImplementation.class)) {
            AntArtifact artifact = aaqi.findArtifact(file);
            if (artifact != null) {
                return artifact;
            }
        }
        return null;
    }
    
    /**
     * Try to find a particular build artifact according to the Ant target producing it.
     * @param p a project (should have {@link AntArtifactProvider} in lookup
     *          in order for this query to work)
     * @param id a desired {@link AntArtifact#getID ID}
     * @return an artifact produced by that project with the specified target,
     *         or null if none such can be found
     */
    public static AntArtifact findArtifactByID(Project p, String id) {
        AntArtifactProvider prov = p.getLookup().lookup(AntArtifactProvider.class);
        if (prov == null) {
            return null;
        }
        for (AntArtifact artifact : prov.getBuildArtifacts()) {
            if (artifact.getID().equals(id)) {
                return artifact;
            }
        }
        return null;
    }
    
    /**
     * Try to find build artifacts of a certain type in a project.
     * @param p a project (should have {@link AntArtifactProvider} in lookup
     *          in order for this query to work)
     * @param type a desired {@link AntArtifact#getType artifact type}
     * @return all artifacts of the specified type produced by that project
     *         (may be an empty list)
     */
    public static AntArtifact[] findArtifactsByType(Project p, String type) {
        AntArtifactProvider prov = p.getLookup().lookup(AntArtifactProvider.class);
        if (prov == null) {
            return new AntArtifact[0];
        }
        AntArtifact[] artifacts = prov.getBuildArtifacts();
        List<AntArtifact> l = new ArrayList<AntArtifact>(artifacts.length);
        for (AntArtifact aa : artifacts) {
            if (aa.getType().equals(type)) {
                l.add(aa);
            }
        }
        return l.toArray(new AntArtifact[0]);
    }
    
}
