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

package org.netbeans.spi.project.support.ant;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.project.ant.AntBasedProjectFactorySingleton;
import org.openide.util.BaseUtilities;

/**
 * A basic AntArtifact implementation.
 * @see AntProjectHelper#createSimpleAntArtifact
 * @author Jesse Glick
 */
final class SimpleAntArtifact extends AntArtifact {

    private final AntProjectHelper h;
    private final String type;
    private final String locationProperty;
    private final PropertyEvaluator eval;
    private final String targetName;
    private final String cleanTargetName;
    private final String buildScriptProperty;
    
    /**
     * @see AntProjectHelper#createSimpleAntArtifact
     */
    public SimpleAntArtifact(AntProjectHelper helper, String type, String locationProperty, 
            PropertyEvaluator eval, String targetName, String cleanTargetName, String buildScriptProperty) {
        this.h = helper;
        this.type = type;
        this.locationProperty = locationProperty;
        this.eval = eval;
        this.targetName = targetName;
        this.cleanTargetName = cleanTargetName;
        this.buildScriptProperty = buildScriptProperty;
    }
    
    private URI getArtifactLocation0() {
        String locationResolved = eval.getProperty(locationProperty);
        if (locationResolved == null) {
            return URI.create("file:/UNDEFINED"); // NOI18N
        }
        File locF = new File(locationResolved);
        if (locF.isAbsolute()) {
            return BaseUtilities.toURI(locF);
        } else {
            // Project-relative path.
            try {
                return new URI(null, null, locationResolved.replace(File.separatorChar, '/'), null);
            } catch (URISyntaxException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, null, e);
                return URI.create("file:/BROKEN"); // NOI18N
            }
        }
    }
    
    @Override
    public URI[] getArtifactLocations() {
        return new URI[]{getArtifactLocation0()};
    }
    
    public String getCleanTargetName() {
        return cleanTargetName;
    }
    
    public File getScriptLocation() {
        String path = null;
        if (buildScriptProperty != null) {
            path = eval.getProperty(buildScriptProperty);
        }
        if (path == null) {
            path = GeneratedFilesHelper.BUILD_XML_PATH;
        }
        return h.resolveFile(path);
    }
    
    public String getTargetName() {
        return targetName;
    }
    
    public String getType() {
        return type;
    }
    
    @Override
    public Project getProject() {
        return AntBasedProjectFactorySingleton.getProjectFor(h);
    }
    
    @Override
    public String toString() {
        return "SimpleAntArtifact[helper=" + h + ",type=" + type + ",locationProperty=" + locationProperty + // NOI18N
            ",targetName=" + targetName + ",cleanTargetName=" + cleanTargetName + ",buildScriptProperty=" + buildScriptProperty + "]"; // NOI18N
    }
    
}
