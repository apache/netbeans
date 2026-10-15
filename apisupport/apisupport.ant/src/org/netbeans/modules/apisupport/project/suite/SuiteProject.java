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

package org.netbeans.modules.apisupport.project.suite;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.ApisupportAntUtils;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.queries.FileEncodingQueryImpl;
import org.netbeans.modules.apisupport.project.queries.OSGiSourceForBinaryImpl;
import org.netbeans.modules.apisupport.project.queries.TemplateAttributesProvider;
import org.netbeans.modules.apisupport.project.spi.PlatformJarProvider;
import org.netbeans.modules.apisupport.project.ui.SuiteActions;
import org.netbeans.modules.apisupport.project.ui.SuiteLogicalView;
import org.netbeans.modules.apisupport.project.ui.SuiteOperations;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteCustomizer;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.modules.apisupport.project.universe.DestDirProvider;
import org.netbeans.modules.apisupport.project.universe.HarnessVersion;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.lookup.Lookups;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 * Represents one module suite project.
 * @author Jesse Glick
 */
@AntBasedProjectRegistration(
    type=SuiteProjectType.TYPE,
    iconResource="org/netbeans/modules/apisupport/project/suite/resources/suite.png", // NOI18N
    sharedName=SuiteProjectType.NAME_SHARED,
    sharedNamespace= SuiteProjectType.NAMESPACE_SHARED,
    privateName=SuiteProjectType.NAME_PRIVATE,
    privateNamespace= SuiteProjectType.NAMESPACE_PRIVATE
)
public final class SuiteProject implements Project {
    
    public static final String SUITE_ICON_PATH =
            "org/netbeans/modules/apisupport/project/suite/resources/suite.png"; // NOI18N

    private final AntProjectHelper helper;
    private Lookup lookup;
    private final PropertyEvaluator eval;
    private final GeneratedFilesHelper genFilesHelper;
    
    @SuppressWarnings("LeakingThisInConstructor")
    public SuiteProject(AntProjectHelper helper) throws IOException {
        this.helper = helper;
        eval = createEvaluator();
        genFilesHelper = new GeneratedFilesHelper(helper);
        Util.err.log("Loading suite project in " + getProjectDirectory());
        lookup = Lookups.fixed(
            this, 
            new Info(),
            helper.createAuxiliaryConfiguration(),
            helper.createAuxiliaryProperties(),
            helper.createCacheDirectoryProvider(),
            new SavedHook(),
            UILookupMergerSupport.createProjectOpenHookMerger(new OpenedHook()),
            helper.createSharabilityQuery(eval, new String[0], new String[] {"${suite.build.dir}", "${dist.dir}"}), // NOI18N
            new SuiteSubprojectProviderImpl(helper, eval),
            new SuiteProviderImpl(),
            new SuiteActions(this),
            new SuiteLogicalView(this),
            new SuiteCustomizer(this, helper, eval),
            new PrivilegedTemplatesImpl(),
            new SuiteOperations(this),
            new TemplateAttributesProvider(null, helper, false),
            new FileEncodingQueryImpl(),
            new PlatformJarProviderImpl(),
            new OSGiSourceForBinaryImpl(this));
        lookup = LookupProviderSupport.createCompositeLookup(lookup, "Projects/org-netbeans-modules-apisupport-project-suite/Lookup");
    }
    
    public @Override String toString() {
        return "SuiteProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    public Lookup getLookup() {
        return lookup;
    }
    
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }
    
    public File getProjectDirectoryFile() {
        return FileUtil.toFile(getProjectDirectory());
    }
    
    /** For unit tests purpose only. */
    public AntProjectHelper getHelper() {
        return helper;
    }
    
    /** For unit tests purpose only. */
    public PropertyEvaluator getEvaluator() {
        return eval;
    }

    public @NonNull File getTestUserDirLockFile() {
        String v = getEvaluator().evaluate("${test.user.dir}/lock");
        return getHelper().resolveFile(v != null ? v : "unknown");
    }
    
    /**
     * Get the platform selected for use with this suite.
     * @param fallback if true, fall back to the default platform if necessary
     * @return the current platform; or null if fallback is false and there is no
     *         platform specified, or an invalid platform is specified, or even if
     *         fallback is true but even the default platform is not available
     */
    public @CheckForNull NbPlatform getPlatform(boolean fallback) {
        NbPlatform p;
        // #65652: more reliable to use the dest dir, in case nbplatform.active is not set.
        String destdir = getEvaluator().getProperty(ModuleList.NETBEANS_DEST_DIR);
        if (destdir != null) {
            String harnessDir = getEvaluator().getProperty("harness.dir");
            p = NbPlatform.getPlatformByDestDir(getHelper().resolveFile(destdir), harnessDir != null ? getHelper().resolveFile(harnessDir) : null);
        } else {
            p = null;
        }
        if (fallback && (p == null || !p.isValid())) {
            p = NbPlatform.getDefaultPlatform();
        }
        return p;
    }

    private PropertyEvaluator createEvaluator() {
        PropertyProvider predefs = helper.getStockPropertyPreprovider();
        File dir = getProjectDirectoryFile();
        List<PropertyProvider> providers = new ArrayList<PropertyProvider>();
        providers.add(helper.getPropertyProvider("nbproject/private/platform-private.properties")); // NOI18N
        providers.add(helper.getPropertyProvider("nbproject/platform.properties")); // NOI18N
        PropertyEvaluator baseEval = PropertyUtils.sequentialPropertyEvaluator(predefs, providers.toArray(new PropertyProvider[0]));
        providers.add(new ApisupportAntUtils.UserPropertiesFileProvider(baseEval, dir));
        baseEval = PropertyUtils.sequentialPropertyEvaluator(predefs, providers.toArray(new PropertyProvider[0]));
        providers.add(new DestDirProvider(baseEval));
        providers.add(helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH));
        providers.add(helper.getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH));
        Map<String,String> fixedProps = new HashMap<String,String>();
        // synchronize with suite.xml
        fixedProps.put(SuiteProperties.ENABLED_CLUSTERS_PROPERTY, "");
        fixedProps.put(SuiteProperties.DISABLED_CLUSTERS_PROPERTY, "");
        fixedProps.put(SuiteProperties.DISABLED_MODULES_PROPERTY, "");
        fixedProps.put(SuiteBrandingModel.BRANDING_DIR_PROPERTY, "branding"); // NOI18N
        fixedProps.put("suite.build.dir", "build"); // NOI18N
        fixedProps.put("cluster", "${suite.build.dir}/cluster"); // NOI18N
        fixedProps.put("dist.dir", "dist"); // NOI18N
        fixedProps.put("test.user.dir", "${suite.build.dir}/testuserdir"); // NOI18N
        providers.add(PropertyUtils.fixedPropertyProvider(fixedProps));
        return PropertyUtils.sequentialPropertyEvaluator(predefs, providers.toArray(new PropertyProvider[0]));
    }
    
    private final class Info implements ProjectInformation, AntProjectListener {
        
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        
        Info() {
            helper.addAntProjectListener(this);
        }
        
        private String getSimpleName() {
            Element nameEl = XMLUtil.findElement(helper.getPrimaryConfigurationData(true), "name", SuiteProjectType.NAMESPACE_SHARED); // NOI18N
            String text = (nameEl != null) ? XMLUtil.findText(nameEl) : null;
            return (text != null) ? text : "???"; // NOI18N
        }
        
        public String getName() {
            return PropertyUtils.getUsablePropertyName(getSimpleName());
        }
        
        public String getDisplayName() {
            String appTitle = getEvaluator().getProperty("app.title"); // NOI18N
            if (appTitle != null) {
                return appTitle;
            } else {
                return getSimpleName();
            }
        }
        
        public Icon getIcon() {
            return ImageUtilities.loadImageIcon(SUITE_ICON_PATH, false);
        }
        
        public Project getProject() {
            return SuiteProject.this;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }
        
        public void configurationXmlChanged(AntProjectEvent ev) {
            fireNameChange();
        }
        
        public void propertiesChanged(AntProjectEvent ev) {
            fireNameChange();
        }
        
        private void fireNameChange() {
            pcs.firePropertyChange(ProjectInformation.PROP_NAME, null, getName());
            pcs.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME, null, getDisplayName());
        }
        
    }
    
    /** For access from tests. */
    public void open() {
        // XXX skip this in case nbplatform.active is not defined
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
            public Void run() {
                String path = "nbproject/private/platform-private.properties"; // NOI18N
                EditableProperties ep = helper.getProperties(path);
                File buildProperties = new File(System.getProperty("netbeans.user"), "build.properties"); // NOI18N
                ep.setProperty("user.properties.file", buildProperties.getAbsolutePath()); //NOI18N
                helper.putProperties(path, ep);
                try {
                    ProjectManager.getDefault().saveProject(SuiteProject.this);
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
                return null;
            }
        });
        // refresh build.xml and build-impl.xml
        try {
            refreshBuildScripts(true);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }

    public void refreshBuildScripts(boolean checkForProjectXmlModified) throws IOException {
        NbPlatform platform = getPlatform(true);
        if (platform == null) { // #169855
            return;
        }
        String buildImplPath =
                platform.getHarnessVersion().compareTo(HarnessVersion.V65) <= 0
                || eval.getProperty(SuiteProperties.CLUSTER_PATH_PROPERTY) == null
                ? "build-impl-65.xsl" : "build-impl.xsl";    // NOI18N
        genFilesHelper.refreshBuildScript(
                GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                SuiteProject.class.getResource("resources/" + buildImplPath),// NOI18N
                checkForProjectXmlModified);
        genFilesHelper.refreshBuildScript(
                "nbproject/platform.xml",
                SuiteProject.class.getResource("resources/platform.xsl"),
                checkForProjectXmlModified);
        genFilesHelper.refreshBuildScript(
                GeneratedFilesHelper.BUILD_XML_PATH,
                SuiteProject.class.getResource("resources/build.xsl"),// NOI18N
                checkForProjectXmlModified);
    }

    private final class OpenedHook extends ProjectOpenedHook {
        OpenedHook() {}
        public void projectOpened() {
            open();
        }
        protected void projectClosed() {
            try {
                ProjectManager.getDefault().saveProject(SuiteProject.this);
            } catch (IOException e) {
                Util.err.notify(e);
            }
        }
    }
    
    private final class SavedHook extends ProjectXmlSavedHook {
        
        SavedHook() {}
        
        protected void projectXmlSaved() throws IOException {
            // refresh build.xml and build-impl.xml
            refreshBuildScripts(false);
        }
        
    }
    
    private final class SuiteProviderImpl implements SuiteProvider {
        
        public File getSuiteDirectory() {
            return getProjectDirectoryFile();
        }

        public File getClusterDirectory() {
            String clusterName = getEvaluator().evaluate("${cluster}");
            return getHelper().resolveFile(clusterName).getAbsoluteFile();
        }
    }
    
    private static final class PrivilegedTemplatesImpl implements PrivilegedTemplates, RecommendedTemplates {
        
        private static final String[] PRIVILEGED_NAMES = new String[] {
            "Templates/Ant/Project.xml", // NOI18N
            "Templates/Other/properties.properties", // NOI18N
        };
        
        private static final String[] RECOMMENDED_TYPES = new String[] {
            "oasis-XML-catalogs",   // NOI18N
            "XML",                  // NOI18N
            "ant-script",           // NOI18N
            "simple-files",         // NOI18N
        };
        
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }
        
        public String[] getRecommendedTypes() {
            return RECOMMENDED_TYPES;
        }
    }

    private class PlatformJarProviderImpl implements PlatformJarProvider {

        @Override public Set<File> getPlatformJars() throws IOException {
            Set<File> jars = new HashSet<File>();
            for (ModuleEntry entry : ModuleList.findOrCreateModuleListFromSuite(getProjectDirectoryFile(), null).getAllEntries()) {
                jars.add(entry.getJarLocation());
            }
            return jars;
        }

    }
    
    public SuiteType getSuiteType() {
        if(this.helper.getProperties("nbproject/platform.properties").getProperty("branding.token") != null) {
            return SuiteType.APPLICATION;
        } else {
            return SuiteType.SUITE;
        }
    }
}
