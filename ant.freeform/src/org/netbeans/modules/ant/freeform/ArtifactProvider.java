/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        return artifacts.toArray(new AntArtifact[artifacts.size()]);
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
            return locations.toArray(new URI[locations.size()]);
        }
        
        private void addLocation(URI u) {
            locations.add(u);
        }
        
        public String toString() {
            return "FreeformArtifact[" + project + ":" + id + "]"; // NOI18N
        }

    }

}
