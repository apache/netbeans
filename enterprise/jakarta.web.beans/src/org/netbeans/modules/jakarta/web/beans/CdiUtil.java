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
package org.netbeans.modules.jakarta.web.beans;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.SourceGroupModifier;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.modules.jakarta.web.beans.xml.BeansAttributes;
import org.netbeans.modules.jakarta.web.beans.xml.WebBeansModel;
import org.netbeans.modules.jakarta.web.beans.xml.WebBeansModelFactory;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/**
 * @author ads
 * beware, non-static methods may behave differently in different projects(by type) if provider is overrided and registred to appropriate project type
 * known subclasses #WebCdiUtil
 */
@ProjectServiceProvider(service=CdiUtil.class, projectType = {
    "org-netbeans-modules-java-j2seproject", "org-netbeans-modules-maven/jar"})
public class CdiUtil {

    private static final Logger LOG = Logger.getLogger("org.netbeans.ui.metrics.cdi");   // NOI18N
    
    public static final String BEANS = "beans";                          // NOI18N
    public static final String BEANS_XML = BEANS+".xml";                 // NOI18N
    private static final String META_INF = "META-INF";                   // NOI18N
    public static final String WEB_INF = "WEB-INF";                      // NOI18N
    
    public CdiUtil(Project project){
        myProject = new WeakReference<>( project );
        myMessages = new CopyOnWriteArraySet<>();
    }
    
    public void log(String message , Class<?> clazz, Object[] params){
        log(message, clazz, params , false );
    }
    
    
    public void log(String message , Class<?> clazz, Object[] params, boolean once){
        if (!once) {
            if (myMessages.contains(message)) {
                return;
            }
            else {
                myMessages.add(message);
            }
        }
        
        LogRecord logRecord = new LogRecord(Level.INFO, message);
        logRecord.setLoggerName(LOG.getName());
        logRecord.setResourceBundle(NbBundle.getBundle(clazz));
        logRecord.setResourceBundleName(clazz.getPackage().getName() + ".Bundle"); // NOI18N
        if (params != null) {
            logRecord.setParameters(params);
        }
        LOG.log(logRecord);
    }
    
    /**
     * check if cdi is enabled in supplied project, general implementation
     * @param project
     * @return 
     */
    public static boolean isCdiEnabled(Project project){
        return (getBeansXmlExists(project)!=null) || isCdi11OrLater(project);
    }
    
    private static FileObject getBeansXmlExists(Project project){
        Collection<FileObject> beansTargetFolder = getBeansTargetFolder(project, false);
        for (FileObject fileObject : beansTargetFolder) {
            if ( fileObject != null && fileObject.getFileObject(BEANS_XML)!=null){
                return fileObject.getFileObject(BEANS_XML);
            }
        }
        return null;
    }
    
    private  FileObject getBeansXmlExists(){
        Collection<FileObject> beansTargetFolder = getBeansTargetFolder(false);
        for (FileObject fileObject : beansTargetFolder) {
            if ( fileObject != null && fileObject.getFileObject(BEANS_XML)!=null){
                return fileObject.getFileObject(BEANS_XML);
            }
        }
        return null;
    }
    
    /**
     *  check if cdi is enabled in the project where CdiUtil is registered as a service
     * @return ch
     */
    public boolean isCdiEnabled(){
        Project project = getProject();
        if ( project == null ){
            return false;
        }
        Collection<FileObject> beansTargetFolder = getBeansTargetFolder(false);
        for (FileObject fileObject : beansTargetFolder) {
            if ( fileObject != null && fileObject.getFileObject(BEANS_XML)!=null){
                return true;
            }
        }
        // #229078 - since CDI 1.1 beans.xml is optional in case of 'implicit bean archive'
        if (isCdi11OrLater()) {
            return true;
        }
        return false;
    }

    /**
     * Avoid static methods usage as much as possible, use isCdi11OrLater() instead
     * @param p
     * @return 
     */
    public static boolean isCdi11OrLater(Project p) {
        if(! (hasResource(p, "javax/enterprise/inject/spi/AfterTypeDiscovery.class")
                || hasResource(p, "jakarta/enterprise/inject/spi/AfterTypeDiscovery.class"))) {
            return false;
        } else {
            FileObject beans = getBeansXmlExists(p);
            if(beans == null) {
                return true;//no beans.xml and ee7 environment, default cdi 1.1 behavior
            }
            WebBeansModel model = WebBeansModelFactory.getInstance().getModel(getModelSource(beans, true));
            if (model == null) {
                return false;//???
            }

            String attribute = model.getRootComponent().getAttribute(BeansAttributes.VERSION);
            if(attribute == null || attribute.equals("1.0")) {
                return false;//no version attribute in cdi1.0 or equal to "1.0" in cdi 1.1.
            }
            return true;
        }
    }
    
    public boolean isCdi11OrLater() {
        if (!(hasResource(getProject(), "javax/enterprise/inject/spi/AfterTypeDiscovery.class")
                || hasResource(getProject(), "jakarta/enterprise/inject/spi/AfterTypeDiscovery.class"))) {
            return false;
        } else {
            FileObject beans = getBeansXmlExists();
            if(beans == null) {
                return true;//no beans.xml and ee7 environment, default cdi 1.1 behavior
            }
            WebBeansModel model = WebBeansModelFactory.getInstance().getModel(getModelSource(beans, true));
            if (model == null || model.getRootComponent() == null) {
                return false;//empty? as in cdi1.0
            }

            String attribute = model.getRootComponent().getAttribute(BeansAttributes.XMLNS);
            String version = model.getRootComponent().getAttribute(BeansAttributes.VERSION);
            if(attribute != null && attribute.startsWith("http://java")) {//NOI18N
                return false;//only cdi1.0 use java.sun.com namespace, also default for future usage is cdi 1.1 (in case of corrupted beans without namespace)
            } else if ("1.0".equals(version)){//NOI18N
                return false;//we can fall back with version attribute if exists.
            }
            return true;
        }
    }
    
    private static ModelSource getModelSource( FileObject fileObject , 
            boolean isEditable )
    {
        try {
            return Utilities.createModelSource( fileObject,isEditable);
        } catch (CatalogModelException ex) {
            Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                ex.getMessage(), ex);   // NOI18N
        }
        return null;
    }
    
    private static boolean hasResource(Project project, String resource) {
        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sgs.length < 1) {
            return false;
        }
        FileObject sourceRoot = sgs[0].getRootFolder();
        ClassPath classPath = ClassPath.getClassPath(sourceRoot, ClassPath.COMPILE);
        if (classPath == null) {
            return false;
        }
        FileObject resourceFile = classPath.findResource(resource);
        if (resourceFile != null) {
            return true;
        }
        return false;
    }

    public Collection<FileObject> getBeansTargetFolder(boolean create) 
    {
        Project project = getProject();
        if ( project == null ){
            return Collections.emptyList();
        }
        return getBeansTargetFolder(project, create);
    }
    
    protected Project getProject(){
        return myProject.get();
    }
    
    /**
     * Enables CDI in the project and returns reference to the created beans.xml file if any.
     * @return reference to beans.xml if was created, {@code null} otherwise
     * @since 2.3
     */
    @CheckForNull
    public FileObject enableCdi() {
        Collection<FileObject> infs = getBeansTargetFolder(true);
        for (FileObject inf : infs) {
            if (inf != null) {
                FileObject beansXml = inf.getFileObject(CdiUtil.BEANS_XML);
                if (beansXml != null) {
                    return null;
                }
                try {
                    EjbJar ejbJar = EjbJar.getEjbJar(myProject.get().getProjectDirectory());
                    Profile profile = ejbJar != null ? ejbJar.getJ2eeProfile() : Profile.JAVA_EE_6_WEB;
                    LOG.log(Level.INFO, "Creating beans.xml file for project: {0}", myProject.get().getProjectDirectory());
                    return DDHelper.createBeansXml(profile, inf, CdiUtil.BEANS);
                } catch (IOException exception) {
                    Exceptions.printStackTrace(exception);
                }
                return null;
            }
        }
        return null;
    }
    
    public static Collection<FileObject> getBeansTargetFolder(Project project, 
            boolean create) 
    {
        Sources sources = ProjectUtils.getSources(project);
        Collection<FileObject> result = new ArrayList<>(2);
        SourceGroup[] sourceGroups = sources.getSourceGroups(
                    JavaProjectConstants.SOURCES_TYPE_RESOURCES );
        if (sourceGroups != null && sourceGroups.length > 0) {
            FileObject fileObject = getDefaultBeansTargetFolder(sourceGroups, false);
            if (fileObject != null) {
                result.add(fileObject);
            }
        }
        else {
            sourceGroups = sources.getSourceGroups(
                    JavaProjectConstants.SOURCES_TYPE_JAVA);
            FileObject fileObject = getDefaultBeansTargetFolder(sourceGroups, false);
            if ( fileObject != null ){
                result.add(fileObject);
            }
        }
        if ( result.isEmpty() && create ){
            SourceGroup resourcesSourceGroup = SourceGroupModifier.createSourceGroup(
                    project, JavaProjectConstants.SOURCES_TYPE_RESOURCES, 
                    JavaProjectConstants.SOURCES_HINT_MAIN);
            if ( resourcesSourceGroup != null ){
                sourceGroups = new SourceGroup[]{resourcesSourceGroup};
            }
            FileObject fileObject = getDefaultBeansTargetFolder(sourceGroups, true);
            result.add(fileObject);
        }
        return result;
    }
    
    private static FileObject getDefaultBeansTargetFolder( SourceGroup[] sourceGroups,
            boolean create )
    {
        if ( sourceGroups.length >0 ){
            FileObject metaInf = sourceGroups[0].getRootFolder().getFileObject( META_INF );
            if ( metaInf == null && create ){
                try {
                    metaInf = FileUtil.createFolder(
                        sourceGroups[0].getRootFolder(), META_INF);
                }
                catch( IOException e ){
                    Logger.getLogger( CdiUtil.class.getName() ).log( 
                            Level.WARNING, null, e );
                }
            }
            return metaInf;
        }
        return null;
    }
    
    private WeakReference<Project> myProject;
    private Set<String> myMessages;

}
