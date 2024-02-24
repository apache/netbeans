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

package org.netbeans.modules.ant.freeform;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.ErrorManager;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 * Handles <code>&lt;export&gt;</code> elements in project.xml
 * @author Jesse Glick
 */
final class ArtifactProvider implements AntArtifactProvider {
    
    private final FreeformProject project;
    
    public ArtifactProvider(FreeformProject project) {
        this.project = project;
    }

    public AntArtifact[] getBuildArtifacts() {
        Element data = project.getPrimaryConfigurationData();
        List<AntArtifact> artifacts = new ArrayList<AntArtifact>();
        Set<String> ids = new HashSet<String>();
        HashMap<String,FreeformArtifact> uniqueArtifacts = new HashMap<String,FreeformArtifact>();
        for (Element export : XMLUtil.findSubElements(data)) {
            if (!export.getLocalName().equals("export")) { // NOI18N
                continue;
            }
            FreeformArtifact artifact = new FreeformArtifact(export);
            
            String artifactKey = artifact.getType() + artifact.getTargetName() + artifact.getScriptLocation().getAbsolutePath();
            FreeformArtifact alreadyHasArtifact = uniqueArtifacts.get(artifactKey);
            if (alreadyHasArtifact != null) {
                alreadyHasArtifact.addLocation(readArtifactLocation(export, project.evaluator()));
                continue;
            } else {
                artifact.addLocation(readArtifactLocation(export, project.evaluator()));
                uniqueArtifacts.put(artifactKey, artifact);
            }
            
            String id = artifact.preferredId();
            if (!ids.add(id)) {
                // Need to uniquify it.
                int counter = 2;
                while (true) {
                    String possibleId = id + counter;
                    if (ids.add(possibleId)) {
                        id = possibleId;
                        break;
                    }
                    counter++;
                }
            }
            artifact.configureId(id);
            artifacts.add(artifact);
        }
        return artifacts.toArray(new AntArtifact[0]);
    }
    
    public static URI readArtifactLocation(Element export, PropertyEvaluator eval) {
        Element locEl = XMLUtil.findElement(export, "location", FreeformProjectType.NS_GENERAL); // NOI18N
        assert locEl != null;
        String loc = XMLUtil.findText(locEl);
        assert loc != null;
        String locationResolved = eval.evaluate(loc);
        if (locationResolved == null) {
            return URI.create("file:/UNDEFINED"); // NOI18N
        }
        File locF = new File(locationResolved);
        if (locF.isAbsolute()) {
            return Utilities.toURI(locF);
        } else {
            // Project-relative path.
            try {
                return new URI(null, null, locationResolved.replace(File.separatorChar, '/'), null);
            } catch (URISyntaxException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                return URI.create("file:/BROKEN"); // NOI18N
            }
        }
    }

    private final class FreeformArtifact extends AntArtifact {
        
        private final Element export;
        private String id = null;
        private final Set<URI> locations = new LinkedHashSet<URI>();
        
        public FreeformArtifact(Element export) {
            this.export = export;
        }
        
        public String preferredId() {
            return getTargetName();
        }
        
        public void configureId(String id) {
            assert this.id == null;
            this.id = id;
        }

        public String getType() {
            Element typeEl = XMLUtil.findElement(export, "type", FreeformProjectType.NS_GENERAL); // NOI18N
            assert typeEl != null;
            String type = XMLUtil.findText(typeEl);
            assert type != null;
            return type;
        }

        public String getTargetName() {
            Element targetEl = XMLUtil.findElement(export, "build-target", FreeformProjectType.NS_GENERAL); // NOI18N
            assert targetEl != null;
            String target = XMLUtil.findText(targetEl);
            assert target != null;
            return target;
        }

        public String getCleanTargetName() {
            Element targetEl = XMLUtil.findElement(export, "clean-target", FreeformProjectType.NS_GENERAL); // NOI18N
            if (targetEl != null) {
                String target = XMLUtil.findText(targetEl);
                assert target != null;
                return target;
            } else {
                // Guess based on configured target for 'clean' command, if any.
                String target = null;
                Element genldata = project.getPrimaryConfigurationData();
                Element actionsEl = XMLUtil.findElement(genldata, "ide-actions", FreeformProjectType.NS_GENERAL); // NOI18N
                if (actionsEl != null) {
                    for (Element actionEl : XMLUtil.findSubElements(actionsEl)) {
                        if (actionEl.getAttribute("name").equals("clean")) { // NOI18N
                            for (Element actionTargetEl : XMLUtil.findSubElements(actionEl)) {
                                if (!actionTargetEl.getLocalName().equals("target")) { // NOI18N
                                    continue;
                                }
                                String possibleTarget = XMLUtil.findText(actionTargetEl);
                                assert possibleTarget != null;
                                if (target == null) {
                                    // OK, probably use it (unless there is another target for this command).
                                    target = possibleTarget;
                                } else {
                                    // Oops! >1 target not supported for AntArtifact.
                                    target = null;
                                    break;
                                }
                            }
                            // We found the clean command, use that target if we got it.
                            break;
                        }
                    }
                }
                if (target == null) {
                    // Guess!
                    target = "clean"; // NOI18N
                }
                return target;
            }
        }

        public File getScriptLocation() {
            String loc = null;
            Element scriptEl = XMLUtil.findElement(export, "script", FreeformProjectType.NS_GENERAL); // NOI18N
            if (scriptEl != null) {
                String script = XMLUtil.findText(scriptEl);
                assert script != null;
                loc = project.evaluator().evaluate(script);
            }
            if (loc == null) {
                // Not configured, or eval failed.
                loc = "build.xml"; // NOI18N
            }
            return project.helper().resolveFile(loc);
        }

        public Project getProject() {
            return project;
        }

        public String getID() {
            assert id != null;
            return id;
        }

        public URI[] getArtifactLocations() {
            return locations.toArray(new URI[0]);
        }
        
        private void addLocation(URI u) {
            locations.add(u);
        }
        
        public String toString() {
            return "FreeformArtifact[" + project + ":" + id + "]"; // NOI18N
        }

    }

}
