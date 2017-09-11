/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
