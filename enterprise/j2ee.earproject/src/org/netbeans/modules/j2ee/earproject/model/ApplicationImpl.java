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

package org.netbeans.modules.j2ee.earproject.model;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.Module;
import org.netbeans.modules.j2ee.dd.api.application.Web;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.Icon;
import org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.netbeans.modules.j2ee.dd.api.common.SecurityRole;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.ui.customizer.CustomizerRun;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.xml.sax.SAXParseException;

/**
 * Default implementation of {@link Application} for EAR project which <b>caches</b> application modules.
 * It should be used in {@link org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation#runReadAction(org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction)} only.
 * <p>
 * <b>This class is not thread safe so it is necessary to ensure that
 * is controlled by {@link org.netbeans.api.project.ProjectManager#mutex() mutex}.</b>
 * @author Tomas Mysik
 * @see #enterRunReadAction()
 * @see #leaveRunReadAction()
 */
public class ApplicationImpl implements Application {
    
    private final EarProject earProject;
    private List<Module> modules;
    private volatile boolean runReadActionRunning = false;
    
    
    /**
     * Create application for given EAR project.
     * @param earProject EAR project instance for which corresponding application is created.
     */
    public ApplicationImpl(EarProject earProject) {
        this.earProject = earProject;
    }

    protected void enterRunReadAction() {
        runReadActionRunning = true;
    }

    protected void leaveRunReadAction() {
        runReadActionRunning = false;
        clearModules();
    }

    /**
     * Clear all modules.
     * <p>
     * This method ensures that all callers will always have up to date modules.
     * @see ApplicationMetadataModelImpl#runReadAction(MetadataModelAction<ApplicationMetadata, R>)
     */
    private void clearModules() {
        modules = null;
    }
    
    /**
     * @see EarProjectProperties#addItemToAppDD(Application, VisualClassPathItem)
     */
    private List<Module> getModules() {
        if (!ProjectManager.mutex().isWriteAccess()
                || !runReadActionRunning) {
             throw new IllegalStateException("Cannot read modules outside runReadAction()");
        }
        if (modules != null) {
            return modules;
        }
        
        List<ClassPathSupport.Item> vcpis = EarProjectProperties.getJarContentAdditional(earProject);
        modules = new ArrayList<Module>(vcpis.size());
        for (ClassPathSupport.Item vcpi : vcpis) {
            addModuleFromVcpi(vcpi);
        }
        
        return modules;
    }
    
    private void addModuleFromVcpi(ClassPathSupport.Item vcpi) {
        Module mod = null;
        String path = EarProjectProperties.getCompletePathInArchive(earProject, vcpi);
        if (vcpi.getType() == ClassPathSupport.Item.TYPE_ARTIFACT) {
            mod = getModFromAntArtifact(vcpi.getArtifact(), path);
            // TODO: init clientModule/appClient
        } else if (vcpi.getType() == ClassPathSupport.Item.TYPE_JAR) {
           mod = getModFromFile(vcpi.getResolvedFile(), path);
        }
        Module prevMod = searchForModule(path);
        if (prevMod == null && mod != null) {
            modules.add(mod);
        }
    }
    
    private Module getModFromAntArtifact(AntArtifact aa, String path) {
        Project p = aa.getProject();
        Module mod = null;
        J2eeModuleProvider jmp = p.getLookup().lookup(J2eeModuleProvider.class);
        if (jmp != null) {
            String connector = null;
            String ejb = null;
            String car = null;
            Web web = null;
            
            jmp.setServerInstanceID(earProject.getServerInstanceID());
            J2eeModule jm = jmp.getJ2eeModule();
            if (jm != null) {
                earProject.getAppModule().addModuleProvider(jmp, path);
            } else {
                return null;
            }
            
            if (J2eeModule.Type.EJB.equals(jm.getType())) {
                ejb = path;
            } else if (J2eeModule.Type.WAR.equals(jm.getType())) {
                FileObject tmp = aa.getScriptFile();
                if (tmp != null) {
                    tmp = tmp.getParent().getFileObject("web/WEB-INF/web.xml"); // NOI18N
                }
                WebModule wm = null;
                if (tmp != null) {
                    wm = WebModule.getWebModule(tmp);
                }
                String contextPath = null;
                if (wm != null) {
                    contextPath = wm.getContextPath();
                } 
                if (contextPath == null) {
                    int endex = path.length() - 4;
                    if (endex < 1) {
                        endex = path.length();
                    }
                    contextPath = path.substring(0, endex);
                }
                web = new WebImpl(path, contextPath);
            } else if (J2eeModule.Type.RAR.equals(jm.getType())) {
                connector = path;
            } else if (J2eeModule.Type.CAR.equals(jm.getType())) {
                car = path;
            }
            mod = new ModuleImpl(connector, ejb, car, web);
        }
        return mod;
    }
    
    private Module getModFromFile(File f, String path) {
        JarFile jar = null;
        Module mod = null;
        try {
            String connector = null;
            String ejb = null;
            String car = null;
            Web web = null;
            boolean found = false;
            
            jar = new JarFile(f);
            JarEntry ddf = jar.getJarEntry("META-INF/ejb-jar.xml"); // NOI18N
            if (ddf != null) {
                ejb = path;
                found = true;
            }
            ddf = jar.getJarEntry("META-INF/ra.xml"); // NOI18N
            if (ddf != null && !found) {
                connector = path;
                found = true;
            } else if (ddf != null && found) {
                return null; // two timing jar file.
            }
            ddf = jar.getJarEntry("META-INF/application-client.xml"); // NOI18N
            if (ddf != null && !found) {
                car = path;
                found = true;
            } else if (ddf != null && found) {
                return null; // two timing jar file.
            }
            ddf = jar.getJarEntry("WEB-INF/web.xml"); // NOI18N
            if (ddf != null && !found) {
                int endex = path.length() - 4;
                if (endex < 1) {
                    endex = path.length();
                }
                String contextPath = "/" + path.substring(0, endex); // NOI18N
                web = new WebImpl(path, contextPath);
                found = true;
            } else if (ddf != null && found) {
                return null; // two timing jar file.
            }
            
            ddf = jar.getJarEntry("META-INF/application.xml"); // NOI18N
            if (ddf != null) {
                return null;
            }
            mod = new ModuleImpl(connector, ejb, car, web);
            
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        } finally {
            try {
                if (jar != null) {
                    jar.close();
                }
            } catch (IOException ioe) {
                // there is little that we can do about this.
            }
        }
        return mod;
    }
    
    private Module searchForModule(String path) {
        assert path != null;
        
        for (Module m : getModules()) {
            String val = m.getEjb();
            if (path.equals(val)) {
                return m;
            }
            val = m.getConnector();
            if (path.equals(val)) {
                return m;
            }
            val = m.getJava();
            if (path.equals(val)) {
                return m;
            }
            Web w = m.getWeb();
            val = null;
            if (null != w) {
                val = w.getWebUri();
            }
            if (path.equals(val)) {
                return m;
            }
        }
        return null;
    }
    
    public String getDefaultDisplayName() {
        return ProjectUtils.getInformation(earProject).getDisplayName();
    }
    
    @Override
    public Object clone() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public BigDecimal getVersion() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public SAXParseException getError() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public int getStatus() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void setModule(int index, Module value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public Module getModule(int index) {
        if (index < 0 || index >= getModules().size()) {
            return null;
        }
        return getModules().get(index);
    }
    
    public int sizeModule() {
        return getModules().size();
    }
    
    public void setModule(Module[] value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public Module[] getModule() {
        return getModules().toArray(new Module[getModules().size()]);
    }
    
    public int addModule(Module value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public int removeModule(Module value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public Module newModule() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void setSecurityRole(int index, SecurityRole value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public SecurityRole getSecurityRole(int index) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public int sizeSecurityRole() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void setSecurityRole(SecurityRole[] value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public SecurityRole[] getSecurityRole() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public int addSecurityRole(SecurityRole value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public int removeSecurityRole(SecurityRole value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public SecurityRole newSecurityRole() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void setIcon(int index, Icon value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public Icon getIcon(int index) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public int sizeIcon() throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void setIcon(Icon[] value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public int addIcon(Icon value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public int removeIcon(Icon value) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public Icon newIcon() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void write(FileObject fo) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void merge(RootInterface root, int mode) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    protected final void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void setId(String value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public String getId() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public Object getValue(String propertyName) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void write(OutputStream os) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void setDescription(String locale, String description) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void setDescription(String description) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void setAllDescriptions(Map descriptions) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public String getDescription(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public String getDefaultDescription() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public Map getAllDescriptions() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void removeDescriptionForLocale(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void removeDescription() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void removeAllDescriptions() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void setDisplayName(String locale, String displayName) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void setDisplayName(String displayName) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void setAllDisplayNames(Map displayNames) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public String getDisplayName(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public Map getAllDisplayNames() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void removeDisplayNameForLocale(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void removeDisplayName() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void removeAllDisplayNames() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public CommonDDBean createBean(String beanName) throws ClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public CommonDDBean addBean(String beanName, String[] propertyNames, Object[] propertyValues,
            String keyProperty) throws ClassNotFoundException,
            NameAlreadyUsedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public CommonDDBean addBean(String beanName) throws ClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public CommonDDBean findBeanByName(String beanName, String propertyName, String value) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void setSmallIcon(String locale, String icon) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void setSmallIcon(String icon) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void setLargeIcon(String locale, String icon) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void setLargeIcon(String icon) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void setAllIcons(String[] locales, String[] smallIcons, String[] largeIcons) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void setIcon(Icon icon) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public String getSmallIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public String getSmallIcon() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public String getLargeIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public String getLargeIcon() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public Icon getDefaultIcon() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public Map getAllIcons() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void removeSmallIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void removeLargeIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void removeIcon(String locale) throws VersionNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void removeSmallIcon() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void removeLargeIcon() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void removeIcon() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    public void removeAllIcons() {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        final String newLine = System.getProperty("line.separator");

        sb.append(this.getClass().getName() + " Object {");
        sb.append(newLine);
        
        sb.append(" Name: ");
        sb.append(getDefaultDisplayName());
        sb.append(newLine);

        sb.append(" Number of modules: ");
        sb.append(getModules().size());
        sb.append(newLine);

        sb.append(" Modules: ");
        sb.append(getModules());
        sb.append(newLine);

        sb.append("}");
        return sb.toString();
    }
}
