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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.util.Utilities;

/**
 * Provide the module JAR as an exported artifact that other projects could import.
 */
public final class AntArtifactProviderImpl implements AntArtifactProvider {
    
    private final NbModuleProject project;
    private final PropertyEvaluator eval;
    private final AntProjectHelper helper;
    
    public AntArtifactProviderImpl(NbModuleProject project, AntProjectHelper helper, PropertyEvaluator eval) {
        this.project = project;
        this.eval = eval;
        this.helper = helper;
    }
    
    public AntArtifact[] getBuildArtifacts() {
        return new AntArtifact[] {
            new NbmAntArtifact(),
        };
    }
    
    private final class NbmAntArtifact extends AntArtifact {
        
        public NbmAntArtifact() {}

        public String getID() {
            return "module"; // NOI18N
        }

        public File getScriptLocation() {
            return helper.resolveFile("build.xml"); // NOI18N
        }

        public String getType() {
            return JavaProjectConstants.ARTIFACT_TYPE_JAR;
        }

        public URI[] getArtifactLocations() {
            String jarloc = eval.evaluate("${cluster}/${module.jar}"); // NOI18N
            File jar = helper.resolveFile(jarloc); // probably absolute anyway, now
            String reldir = PropertyUtils.relativizeFile(project.getProjectDirectoryFile(), jar);
            if (reldir != null) {
                try {
                    return new URI[] {new URI(null, null, reldir, null)};
                } catch (URISyntaxException e) {
                    throw new AssertionError(e);
                }
            } else {
                return new URI[] {Utilities.toURI(jar)};
            }
            // XXX should it add in class path extensions?
        }
        
        public String getTargetName() {
            return "netbeans"; // NOI18N
        }

        public String getCleanTargetName() {
            return "clean"; // NOI18N
        }

        public Project getProject() {
            return project;
        }

    }
    
}
