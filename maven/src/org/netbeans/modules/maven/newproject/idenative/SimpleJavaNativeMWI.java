/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.newproject.idenative;

import org.netbeans.modules.maven.spi.newproject.CreateProjectBuilder;
import java.io.File;
import java.util.Collections;
import java.util.List;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.archetype.ArchetypeWizards;
import org.netbeans.modules.maven.api.archetype.ProjectInfo;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Project;
import org.netbeans.modules.maven.model.pom.Properties;
import static org.netbeans.modules.maven.newproject.idenative.Bundle.LBL_Maven_Quickstart_Archetype;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author mkleint
 */
@TemplateRegistration(folder=ArchetypeWizards.TEMPLATE_FOLDER, position=100, displayName="#LBL_Maven_Quickstart_Archetype", iconBase="org/netbeans/modules/maven/resources/jaricon.png", description="quickstart.html")
@Messages("LBL_Maven_Quickstart_Archetype=Java Application")
public class SimpleJavaNativeMWI extends IDENativeMavenWizardIterator {

    public SimpleJavaNativeMWI() {
        super(LBL_Maven_Quickstart_Archetype(), "org.apache.maven.archetypes:maven-archetype-quickstart:1.1", "jar");
    }

    @Override
    protected CreateProjectBuilder createBuilder(File projFile, ProjectInfo vi, ProgressHandle handle) {
        return super.createBuilder(projFile, vi, handle).setAdditionalNonPomWork(new CreateProjectBuilder.AdditionalChangeHandle() {
            @Override
            public Runnable createAdditionalChange(final CreateProjectBuilder.Context context) {
                return new Runnable() {

                    @Override
                    public void run() {
                        File src = new File(context.getProjectDirectory(), "src" + File.separator + "main" + File.separator + "java");
                        src.mkdirs();
                        if (context.getPackageName() != null) {
                            String path = context.getPackageName().replace(".", File.separator);
                            new File(src, path).mkdirs();
                        }
                    }
                };
            }
        }).setAdditionalOperations(new CreateProjectBuilder.PomOperationsHandle() {
            //#230984 use source 1.7 by default, unless parent paroject defines something, in that case, just inherit
            @Override
            public List<ModelOperation<POMModel>> createPomOperations(final CreateProjectBuilder.Context context) {
                return Collections.<ModelOperation<POMModel>>singletonList(new ModelOperation<POMModel>() {

                    @Override
                    public void performOperation(POMModel model) {
                        MavenProject mp = context.getParent();
                        boolean setLevel = true;
                        if (mp != null) {
                            String source = PluginPropertyUtils.getPluginProperty(mp, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, "source", "compile", "maven.compiler.source");
                            String target = PluginPropertyUtils.getPluginProperty(mp, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, "target", "compile", "maven.compiler.target");
                            if (target != null || source != null) {
                                setLevel = false;
                            }
                        }
                        if (setLevel) {
                            Project root = model.getProject();
                            if (root != null) {
                                Properties props = root.getProperties();
                                if (props == null) {
                                    props = model.getFactory().createProperties();
                                    root.setProperties(props);
                                }
                                String version = JavaPlatformManager.getDefault().getDefaultPlatform().getSpecification().getVersion().toString();
                                props.setProperty("maven.compiler.source", version);
                                props.setProperty("maven.compiler.target", version);
                            }
                        }
                    }
                });
            }
        });
    }
    
}
