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

package org.netbeans.modules.apisupport.project.suite;

import org.netbeans.modules.apisupport.project.spi.BrandingSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Locale;
import java.util.StringTokenizer;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.spi.BrandingModel;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

public class SuiteBrandingModel extends BrandingModel {

    /** generated properties*/
    public static final String NAME_PROPERTY = "app.name";//NOI18N
    public static final String TITLE_PROPERTY = "app.title";//NOI18N
    public static final String ICON_LOCATION_PROPERTY = "app.icon";//NOI18N
    public static final String BRANDING_TOKEN_PROPERTY = "branding.token";//NOI18N
    public static final String BRANDING_DIR_PROPERTY = "branding.dir"; // NOI18N

    private final @NonNull SuiteProperties suiteProps;
    private boolean brandingChanged = false;

    public SuiteBrandingModel(@NonNull SuiteProperties suiteProps) {
        assert null != suiteProps;
        this.suiteProps = suiteProps;
        this.locale = Locale.getDefault();
    }

    @Override public void init() {
        super.init();
        brandingChanged = false;
    }

    @Override public void setBrandingEnabled(boolean brandingEnabled) {
        if (isBrandingEnabled() != brandingEnabled) {
            brandingChanged = true;
        }
        super.setBrandingEnabled(brandingEnabled);
    }

    @Override protected boolean isBrandingEnabledRefresh() {
        return suiteProps.getPlatformProperty(BRANDING_TOKEN_PROPERTY) != null || suiteProps.getProperty(BRANDING_TOKEN_PROPERTY) != null;
    }

    @Override public void setName(String name) {
        super.setName(name);
        if (isBrandingEnabled()) {
            suiteProps.setProperty(NAME_PROPERTY, "${" + BRANDING_TOKEN_PROPERTY + "}");
            suiteProps.removeProperty(BRANDING_TOKEN_PROPERTY);
            suiteProps.setPlatformProperty(BRANDING_TOKEN_PROPERTY, getName());
        }
    }

    @Override protected String getSimpleName() {
        Element nameEl = XMLUtil.findElement(suiteProps.getProject().getHelper().getPrimaryConfigurationData(true), "name", SuiteProjectType.NAMESPACE_SHARED); // NOI18N
        String text = (nameEl != null) ? XMLUtil.findText(nameEl) : null;
        return (text != null) ? text : "???"; // NOI18N
    }

    @Override protected String loadName() {
        String bt = suiteProps.getPlatformProperty(BRANDING_TOKEN_PROPERTY);
        return bt != null ? bt : suiteProps.getProperty(NAME_PROPERTY);
    }

    @Override public void setTitle(String title) {
        super.setTitle(title);
        if (isBrandingEnabled()) {
            suiteProps.setProperty(TITLE_PROPERTY, getTitle());
        }
    }

    @Override protected String loadTitle() {
        return suiteProps.getProperty(TITLE_PROPERTY);
    }

    @Override public void setIconSource(int size, URL url) {
        super.setIconSource(size, url);
        if (isBrandingEnabled()) {
            suiteProps.setProperty(ICON_LOCATION_PROPERTY, getIconLocation());
        }
    }

    @Override public Project getProject() {
        return suiteProps.getProject();
    }

    @Override protected File getProjectDirectoryFile() {
        return suiteProps.getProjectDirectoryFile();
    }

    @Override public void store() throws IOException {
        super.store();
        if (!isBrandingEnabled() && brandingChanged) { // #115737
            suiteProps.removeProperty(BRANDING_TOKEN_PROPERTY);
            suiteProps.setPlatformProperty(BRANDING_TOKEN_PROPERTY, null);
            suiteProps.removeProperty(NAME_PROPERTY);
            suiteProps.removeProperty(TITLE_PROPERTY);
            suiteProps.removeProperty(ICON_LOCATION_PROPERTY);
        }
    }

    @Override public void doSave() {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws IOException {
                    suiteProps.storeProperties();
                    return null;
                }
            });
        } catch (MutexException e) {
            Exceptions.printStackTrace(e);
        }
    }

    @Override protected BrandingSupport createBranding() throws IOException {
        SuiteProject suiteProject = suiteProps.getProject();
        String brandingPath = suiteProject.getEvaluator().getProperty(BRANDING_DIR_PROPERTY);
        if (brandingPath == null) { // #125160
            brandingPath = "branding"; // NOI18N
        }
        return new SuiteBrandingSupport(suiteProject, brandingPath, this.locale);
    }

    @Override public void reloadProperties() {
        suiteProps.reloadProperties();
    }

    @Override
    public void updateProjectInternationalizationLocales() {
        EditableProperties p = null;
        File projectProperties = null;
        try {
            projectProperties = new File(FileUtil.toFile(suiteProps.getProject().getProjectDirectory()), "nbproject" + File.separatorChar + "project.properties");
            p = getEditableProperties(projectProperties);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if(p != null && projectProperties != null) {
            if(p.getProperty("branding.locales") == null) {
                p.setProperty("branding.locales", this.locale.toString());
            } else {
                String localizationsStr = p.getProperty("branding.locales");
                StringTokenizer tokenizer = new StringTokenizer(localizationsStr, ",");
                boolean containsLocale = false;
                while (tokenizer.hasMoreElements()) {
                    if(this.locale.toString().equals(tokenizer.nextToken())) {
                        containsLocale = true;
                        break;
                    }
                }
                if(!containsLocale) {
                    p.setProperty("branding.locales", p.getProperty("branding.locales") + "," + this.locale.toString());
                }
            }
            try {
                storeEditableProperties(p, projectProperties);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    private static EditableProperties getEditableProperties(final File bundle) throws IOException {
        EditableProperties p = new EditableProperties(true);
        InputStream is = new FileInputStream(bundle);
        try {
            p.load(is);
        } finally {
            is.close();
        }
        return p;
    }
    
    private static void storeEditableProperties(final EditableProperties p, final File bundle) throws IOException {
        FileObject fo = FileUtil.toFileObject(bundle);
        OutputStream os = null == fo ? new FileOutputStream(bundle) : fo.getOutputStream();
        try {
            p.store(os);
        } finally {
            os.close();
        }
    }

}
