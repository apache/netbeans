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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.hints.pom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.Document;
import org.netbeans.api.project.Project;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.hints.pom.spi.Configuration;
import org.netbeans.modules.maven.hints.pom.spi.POMErrorFixProvider;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Profile;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.text.Line;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class OverrideDependencyManagementError implements POMErrorFixProvider {
    private final Configuration configuration;

    public OverrideDependencyManagementError() {
        configuration = new Configuration("OverrideDependencyManagementError", //NOI18N
                NbBundle.getMessage(OverrideDependencyManagementError.class, "TIT_OverrideDependencyManagementError"),
                NbBundle.getMessage(OverrideDependencyManagementError.class, "DESC_OverrideDependencyManagementError"),
                true, Configuration.HintSeverity.WARNING);
    }


    @Override
    public List<ErrorDescription> getErrorsForDocument(POMModel model, Project prj) {
        assert model != null;
        List<ErrorDescription> toRet = new ArrayList<ErrorDescription>();
        if (prj == null) {
            return toRet;
        }
        Map<String, String> managed = collectManaged(prj);
        if (managed.isEmpty()) {
            return toRet;
        }

        checkDependencyList(model.getProject().getDependencies(), model, toRet, managed);
        List<Profile> profiles = model.getProject().getProfiles();
        if (profiles != null) {
            for (Profile prof : profiles) {
                checkDependencyList(prof.getDependencies(), model, toRet, managed);
            }
        }
        return toRet;

    }

    private void checkDependencyList(List<Dependency> deps, final POMModel model, List<ErrorDescription> toRet, Map<String, String> managed) {
        if (deps != null) {
            for (final Dependency dep : deps) {
                String ver = dep.getVersion();
                if (ver != null) {
                    String art = dep.getArtifactId();
                    String gr = dep.getGroupId();
                    String key = gr + ":" + art; //NOI18N
                    if (managed.keySet().contains(key)) {
                        final String managedver = managed.get(key);
                        Document doc = model.getBaseDocument();
                        final Line[] line = new Line[1];
                        doc.render(new Runnable() {
                            @Override
                            public void run() {
                                int position = dep.findChildElementPosition(model.getPOMQNames().VERSION.getQName());
                                line[0] = NbEditorUtilities.getLine(model.getBaseDocument(), position, false);
                            }
                        });
                        toRet.add(ErrorDescriptionFactory.createErrorDescription(
                            configuration.getSeverity(configuration.getPreferences()).toEditorSeverity(),
                            NbBundle.getMessage(OverrideDependencyManagementError.class, "TXT_OverrideDependencyManagementError", managedver),
                            Collections.<Fix>singletonList(new OverrideFix(dep)),
                            doc, line[0].getLineNumber() + 1));
                    }
                }
            }
        }
    }


    @Override
    public JComponent getCustomizer(Preferences preferences) {
        return null;
    }

    @Override
    public String getSavedValue(JComponent customizer, String key) {
        return null;
    }

    @Override
    public void cancel() { }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    private Map<String, String> collectManaged(Project prj) {
        NbMavenProject project = prj.getLookup().lookup(NbMavenProject.class);
        @SuppressWarnings("unchecked")
        HashMap<String, String> toRet = new HashMap<String, String>();
        if (project == null) { //#154462
            return toRet;
        }
        org.apache.maven.model.DependencyManagement dm = project.getMavenProject().getDependencyManagement();
        if (dm != null) {
            @SuppressWarnings("unchecked")
            List<org.apache.maven.model.Dependency> plugins = dm.getDependencies();
            for (org.apache.maven.model.Dependency dep : plugins) {
                toRet.put(dep.getGroupId() + ":" + dep.getArtifactId(), dep.getVersion()); //NOI18N
            }
        }
        return toRet;
    }

    private static class OverrideFix implements Fix, Runnable {
        private final Dependency dependency;

        OverrideFix(Dependency dep) {
            dependency = dep;
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(OverrideDependencyManagementError.class, "TEXT_OverrideDependencyFix");
        }
        
        @Override
        public void run() {
            dependency.setVersion(null);
        }

        @Override
        public ChangeInfo implement() throws Exception {
            ChangeInfo info = new ChangeInfo();
            POMModel mdl = dependency.getModel();
            if (!mdl.getState().equals(Model.State.VALID)) {
                return info;
            }
            PomModelUtils.implementInTransaction(mdl, this);
            return info;
        }

    }

}
