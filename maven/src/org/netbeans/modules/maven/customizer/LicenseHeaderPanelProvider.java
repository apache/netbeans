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
package org.netbeans.modules.maven.customizer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.TemplateAttrProvider;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.Lookup;
import static org.netbeans.modules.maven.customizer.Bundle.*;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.support.ant.ui.CustomizerUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Milos Kleint
 */
@ProjectCustomizer.CompositeCategoryProvider.Registration(projectType = "org-netbeans-modules-maven", position = 620)
public class LicenseHeaderPanelProvider implements ProjectCustomizer.CompositeCategoryProvider {

    @Override
    @Messages("TIT_Headers=License Headers")
    public Category createCategory(Lookup context) {
        return ProjectCustomizer.Category.create(
                ModelHandle2.PANEL_HEADERS,
                TIT_Headers(),
                null);
    }

    @Override
    public JComponent createComponent(Category category, Lookup context) {
        ModelHandle2 handle = context.lookup(ModelHandle2.class);
        Project prj = context.lookup(Project.class);
        return CustomizerUtilities.createLicenseHeaderCustomizerPanel(category, new Impl(handle, prj, prj.getLookup().lookup(AuxiliaryProperties.class)));
    }

    private final class Impl implements CustomizerUtilities.LicensePanelContentHandler {

        final ModelHandle2 handle;
        final AuxiliaryProperties props;
        private final Project project;
        private final ModelOperation<POMModel> operation;
        private String licenseContent;
        private String licensePath;

        public Impl(final ModelHandle2 handle, Project prj, AuxiliaryProperties props) {
            this.handle = handle;
            this.props = props;
            this.project = prj;
            operation = new ModelOperation<POMModel>() {
                @Override
                public void performOperation(POMModel model) {
                    if (licenseContent == null) {
                        return;
                    }
                    try {
                        ExpressionEvaluator createEvaluator = PluginPropertyUtils.createEvaluator(project);
                        Object evaluate = createEvaluator.evaluate(licensePath);
                        if(evaluate != null) {
                            String eval = evaluate.toString();

                            File file = FileUtilities.resolveFilePath(handle.getProject().getBasedir(), eval);
                            FileObject fo;
                            if (!file.exists()) {
                                fo = FileUtil.createData(file);
                            } else {
                                fo = FileUtil.toFileObject(file);
                            }
                            if (fo.isData()) {
                                OutputStream out = fo.getOutputStream();
                                try {
                                    FileUtil.copy(new ByteArrayInputStream(licenseContent.getBytes()), out);
                                } finally {
                                    out.close();
                                }
                            }
                        } else {
                            Logger.getLogger(LicenseHeaderPanelProvider.class.getName()).log(Level.WARNING, "Encountered problems evaluating license path: {0}", licensePath);
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (ExpressionEvaluationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            };  
            licensePath = getProjectLicenseLocation();
        }

        @Override
        public String getProjectLicenseLocation() {
            return props.get(Constants.HINT_LICENSE_PATH, true);
        }

        @Override
        public String getGlobalLicenseName() {
            String s =  props.get(Constants.HINT_LICENSE, true);
            if (s == null) {
                s = TemplateAttrProvider.findLicenseByMavenProjectContent(project.getLookup().lookup(NbMavenProject.class).getMavenProject());
            }
            return s;
        }

        @Override
        public FileObject resolveProjectLocation(String path) {
            if ("".equals(path)) {
                return null;
            }
            try {
                String eval = PluginPropertyUtils.createEvaluator(project).evaluate(path).toString();
                FileObject toRet = FileUtil.toFileObject(FileUtilities.resolveFilePath(handle.getProject().getBasedir(), eval));
                if (toRet != null && toRet.isFolder()) {
                    toRet = null;
                }
                return toRet;
            } catch (ExpressionEvaluationException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }

        @Override
        public void setProjectLicenseLocation(String newLocation) {
            licensePath = newLocation;
            handle.setRawAuxiliaryProperty(Constants.HINT_LICENSE_PATH, newLocation, true);
            if (newLocation != null) {
                handle.setRawAuxiliaryProperty(Constants.HINT_LICENSE, null, true);
            }
        }

        @Override
        public void setGlobalLicenseName(String newName) {
            handle.setRawAuxiliaryProperty(Constants.HINT_LICENSE, newName, true);
        }

        @Override
        public String getDefaultProjectLicenseLocation() {
            return "${project.basedir}/licenseheader.txt";
        }

        @Override
        public void setProjectLicenseContent(String text) {
            //TODO a fake model operation that is only meant to provide us with way to save
            // the license file at the end of the customizer.
            if (text == null) {
                handle.removePOMModification(operation);
                licenseContent = null;
            } else {
                handle.addPOMModification(operation);
                licenseContent = text;
            }
        }
    }
}
