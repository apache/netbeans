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
package org.netbeans.modules.gradle.editor.hints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.GradleReport;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.project.LookupProvider;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Converts Gradle project's problems that have script / line information into editor Hints. This does not use usual Parsing API
 * 'kolecko' approach as gradle does not (yet) use any parsing machinery, but instead watches for opened projects, and their
 * reloads - and sets hints based on the {@link GradleReport}s found in the project.
 * 
 * @author sdedic
 */
public class GradleHintsProvider {
    private static final Logger LOG = Logger.getLogger(GradleHintsProvider.class.getName());
    
    /**
     * Layer for the hints - these come from the gradle script processing itself.
     */
    private static final String LAYER_LOADING = "gradle-processing"; // NOI18N
    
    private final Project gradleProject;
    private final PropertyChangeListener listener;
    
    public GradleHintsProvider(Project gradleProject) {
        this.gradleProject = gradleProject;

        listener = (PropertyChangeEvent evt) -> {
            // force initialization of the shared initializer
            if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                updateProjectProblems();
            }
        };
        NbGradleProject.addPropertyChangeListener(gradleProject, listener);
    }
    
    Map<LineDocument, List<GradleReport>> openReportDocuments(boolean reportNonLocations) {
        GradleBaseProject gbp = GradleBaseProject.get(gradleProject);
        if (gbp == null) {
            return null;
        }
        Set<GradleReport> reports = gbp.getProblems();
        Map<String, LineDocument> openedDocs = new HashMap<>();
        Map<LineDocument, List<GradleReport>> documentReports = new HashMap<>();
        NbGradleProject gp = NbGradleProject.get(gradleProject);
        File scriptF = gp.getGradleFiles().getBuildScript();
        for (GradleReport r : reports) {
            String l;
            int line;
            if (r.getLocation() == null || r.getLine() < 1 || scriptF == null) {
                if (!reportNonLocations) {
                    continue;
                }
                l = scriptF.getPath();
            } else {
                l = r.getLocation();
                line = r.getLine();
            }
            LineDocument doc = null;
            if (!openedDocs.containsKey(l)) {
                FileObject f = FileUtil.toFileObject(new File(l));
                if (f != null) {
                    EditorCookie cake = f.getLookup().lookup(EditorCookie.class);
                    try {
                        Document d = cake.openDocument();
                        if (d instanceof LineDocument) {
                            doc = (LineDocument)d;
                        }
                    } catch (IOException ex) {
                        LOG.log(Level.INFO, "Could not open project file: {0}", l);
                    }
                }
                openedDocs.put(l, doc);
            } else {
                doc = openedDocs.get(l);
            }
            
            if (doc != null) {
                documentReports.computeIfAbsent(doc, (x) -> new ArrayList<>()).add(r);
            }
        }
        return documentReports;
    }
    
    void updateProjectProblems() {
        Map<LineDocument, List<GradleReport>> documentReports = openReportDocuments(false);
        if (documentReports == null) {
            return;
        }
        List<ErrorDescription> hints = new ArrayList<>();
        for (Map.Entry<LineDocument, List<GradleReport>> it : documentReports.entrySet()) {
            LineDocument doc = it.getKey();

            doc.render(() -> {
                for (GradleReport r : it.getValue()) {
                    hints.add(ErrorDescriptionFactory.createErrorDescription(Severity.ERROR, r.formatReportForHintOrProblem(false, null), doc, r.getLine()));
                }
            });
            HintsController.setErrors(doc, LAYER_LOADING, hints);
        }
    }

    @LookupProvider.Registration(projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
    public static class F implements LookupProvider {

        @Override
        public Lookup createAdditionalLookup(Lookup baseContext) {
            NbGradleProject p = baseContext.lookup(NbGradleProject.class);
            if (p == null) {
                return Lookup.EMPTY;
            }
            return Lookups.fixed(new GradleHintsProvider(baseContext.lookup(Project.class)));
        }
    }
}
