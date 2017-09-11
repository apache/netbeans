/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
