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

package org.netbeans.modules.hudson.maven;

import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hudson.spi.HudsonSCM;
import org.netbeans.modules.hudson.ui.spi.ProjectHudsonJobCreatorFactory;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.openide.util.lookup.ServiceProvider;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;

@ServiceProvider(service=ProjectHudsonJobCreatorFactory.class, position=100)
public class JobCreator implements ProjectHudsonJobCreatorFactory {

    public ProjectHudsonJobCreator forProject(Project project) {
        final NbMavenProject prj = project.getLookup().lookup(NbMavenProject.class);
        if (prj == null) {
            return null;
        }
        return new ProjectHudsonJobCreator() {
            final HudsonSCM.Configuration scm = Helper.prepareSCM(prj.getMavenProject().getBasedir());
            
            public String jobName() {
                return prj.getMavenProject().getArtifactId();
            }

            public JComponent customizer() {
                return new JPanel();
            }

            public HudsonSCM.ConfigurationStatus status() {
                if (scm == null) {
                    return ProjectHudsonJobCreatorFactory.Helper.noSCMError();
                }
                HudsonSCM.ConfigurationStatus scmStatus = scm.problems();
                if (scmStatus != null) {
                    return scmStatus;
                } else {
                    return HudsonSCM.ConfigurationStatus.valid();
                }
            }

            public void addChangeListener(ChangeListener listener) {}
            public void removeChangeListener(ChangeListener listener) {}

            public Document configure() throws IOException {
                Document doc = XMLUtil.createDocument("maven2-moduleset", null, null, null); // NOI18N
                scm.configure(doc);
                Helper.addLogRotator(doc);
                doc.getDocumentElement().appendChild(doc.createElement("aggregatorStyleBuild")).appendChild(doc.createTextNode("true"));
                return doc;
            }

        };
    }

}
