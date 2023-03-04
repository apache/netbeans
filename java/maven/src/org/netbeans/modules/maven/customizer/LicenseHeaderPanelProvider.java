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
