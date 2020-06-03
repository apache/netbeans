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
package org.netbeans.modules.cnd.makeproject.ui.customizer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import javax.swing.JComponent;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectHelper;
import org.netbeans.modules.cnd.makeproject.api.ui.configurations.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.ui.configurations.LicenseHeadersPanel;
import org.netbeans.modules.cnd.makeproject.ui.configurations.LicensePanelContentHandler;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 */
public class LicenseCustomizerNode extends CustomizerNode implements MakeContext.Savable {

    private JComponent licencePropPanel;
    private LicensePanelContentHandlerImpl handler;

    public LicenseCustomizerNode(String name, String displayName, CustomizerNode[] children, Lookup lookup) {
        super(name, displayName, children, lookup);
    }
    
    @Override
    public JComponent getPanel(Configuration configuration) {
        if (licencePropPanel == null) {
            MakeContext context = lookup.lookup(MakeContext.class);
            MakeProject project = (MakeProject) context.getProject();
            Properties projectProperties = project.getProjectProperties(true);
            String licenseName = projectProperties.getProperty(MakeProjectHelper.PROJECT_LICENSE_NAME_PROPERTY);
            String licensePath = projectProperties.getProperty(MakeProjectHelper.PROJECT_LICENSE_PATH_PROPERTY);
            handler =  new LicensePanelContentHandlerImpl(lookup, licensePath, licenseName);
            Category category = Category.create(getName(), getDisplayName(), null);
            licencePropPanel = new LicenseHeadersPanel(category, handler);
            getContext().registerSavable(this);

        }
        return licencePropPanel;
    }

    @Override
    public CustomizerStyle customizerStyle() {
        return CustomizerStyle.PANEL;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("ProjectProperties"); // NOI18N
    }

    @Override
    public void save() {
        MakeContext context = lookup.lookup(MakeContext.class);
        MakeProject project = (MakeProject) context.getProject();
        Properties projectProperties = project.getProjectProperties(true);
        
        String globalLicenseName = handler.getGlobalLicenseName();
        if (globalLicenseName == null || globalLicenseName.isEmpty()) {
            projectProperties.remove(MakeProjectHelper.PROJECT_LICENSE_NAME_PROPERTY);
        } else {
            projectProperties.put(MakeProjectHelper.PROJECT_LICENSE_NAME_PROPERTY, globalLicenseName);
        }
        
        String projectLicenseLocation = handler.getProjectLicenseLocation();
        if (projectLicenseLocation == null || projectLicenseLocation.isEmpty()) {
            projectProperties.remove(MakeProjectHelper.PROJECT_LICENSE_PATH_PROPERTY);
        } else {
            projectProperties.put(MakeProjectHelper.PROJECT_LICENSE_PATH_PROPERTY, projectLicenseLocation);
        }
        String projectLicenseContent = handler.getProjectLicenseContent();
        if (projectLicenseContent != null) {
            String path = projectLicenseLocation;
            assert path != null; //path needs to exist once we have content?
            FSPath file = project.getHelper().resolveFSPath(path);
            FileObject fo = file.getFileObject();
            if (fo == null) {
                try {
                    fo = FileUtil.createData(file.getFileSystem().getRoot(), file.getPath());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (fo != null) {
                try (OutputStream out = fo.getOutputStream()) {
                    FileUtil.copy(new ByteArrayInputStream(projectLicenseContent.getBytes()), out);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        project.saveProjectProperties(projectProperties, true);
    }
    
    private static class LicensePanelContentHandlerImpl implements LicensePanelContentHandler {

        private String licensePath;
        private String licenseName;
        private String licenseText;
        private final MakeContext context;

        public LicensePanelContentHandlerImpl(Lookup lookup, String licensePath, String licenseName) {
            context = lookup.lookup(MakeContext.class);
            this.licensePath = licensePath;
            this.licenseName = licenseName;
        }

        @Override
        public String getProjectLicenseLocation() {
            return licensePath;
        }

        @Override
        public String getGlobalLicenseName() {
            return licenseName;
        }

        @Override
        public FileObject resolveProjectLocation(@NonNull String path) {
            final MakeProject project = (MakeProject) context.getProject();
            return project.getHelper().resolveFileObject(path);
        }

        @Override
        public void setProjectLicenseLocation(@NullAllowed String newLocation) {
            licensePath = newLocation;
        }

        @Override
        public void setGlobalLicenseName(@NullAllowed String newName) {
            licenseName = newName;
        }

        @Override
        public String getDefaultProjectLicenseLocation() {
            return "./nbproject/licenseheader.txt"; // NOI18N
        }

        @Override
        public void setProjectLicenseContent(@NullAllowed String text) {
            licenseText = text;
        }

        public String getProjectLicenseContent() {
            return licenseText;
        }
    }
}
