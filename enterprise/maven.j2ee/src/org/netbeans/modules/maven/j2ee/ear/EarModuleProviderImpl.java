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
package org.netbeans.modules.maven.j2ee.ear;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener.Artifact;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.spi.ejbjar.EarImplementation2;
import org.netbeans.modules.j2ee.spi.ejbjar.EarProvider;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarFactory;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.j2ee.execution.ExecutionChecker;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * provider for ear specific functionality
 * @author  Milos Kleint
 */
@ProjectServiceProvider(
    service = {
        EarModuleProviderImpl.class,
        EarProvider.class,
        J2eeModuleProvider.class,
        J2eeApplicationProvider.class
    },
    projectType = {
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_EAR
    }
)
public class EarModuleProviderImpl extends J2eeApplicationProvider implements EarProvider  {
    
    private EarImpl earimpl;
    private Project project;
    private String serverInstanceID;
    private J2eeModule j2eemodule;
    
    private final DeployOnSaveSupport deployOnSaveSupport = new DeployOnSaveSupportProxy();

    
    public EarModuleProviderImpl(Project proj) {
        project = proj;
        earimpl = new EarImpl(project, this);
    }
    
    public EarImplementation2 getEarImplementation() {
        return earimpl;
    }

    @Override
    public boolean isOnlyCompileOnSaveEnabled() {
        return RunUtils.isCompileOnSaveEnabled(project) && !MavenProjectSupport.isDeployOnSave(project);
    }
    
    @Override
    public Ear findEar(FileObject file) {
        Project proj = FileOwnerQuery.getOwner(file);
        if (proj != null) {
            proj = proj.getLookup().lookup(Project.class);
        }
        if (proj != null && project == proj) {
            if (earimpl != null && earimpl.isValid()) {
                return EjbJarFactory.createEar((EarImplementation2) earimpl);
            }
        }
        return null;
    }

    /**
     * Returns the provider for the child module specified by given URI.
     * 
     * @param uri the child module URI within the J2EE application.
     * @return J2eeModuleProvider object
     */
    @Override
    public J2eeModuleProvider getChildModuleProvider(String uri) {
//        System.out.println("!!!give me module with uri=" + uri);
        return null;
    }

    /**
     * Returns list of providers of every child J2EE module of this J2EE app.
     * 
     * @return array of J2eeModuleProvider objects.
     */
    @Override
    public J2eeModuleProvider[] getChildModuleProviders() {
        List<J2eeModuleProvider> provs = new ArrayList<J2eeModuleProvider>();
        for (Project prj : earimpl.getProjects()) {
            if(prj.getProjectDirectory().equals(project.getProjectDirectory())) {
                Logger.getLogger(EarModuleProviderImpl.class.getName()).log(Level.WARNING, "EarImpl.getProjects() for project {0} returns itself as a child project!", project.getProjectDirectory());
                continue;
            }
            J2eeModuleProvider prv = prj.getLookup().lookup(J2eeModuleProvider.class);
            if (prv != null) {
                provs.add(prv);
            }
        }
        return provs.toArray(new J2eeModuleProvider[0]);
    }

    @Override
    public synchronized J2eeModule getJ2eeModule() {
        if (j2eemodule == null) {
            j2eemodule = J2eeModuleFactory.createJ2eeApplication(earimpl);
        }
        return j2eemodule; 
    }


    @Override
    public ModuleChangeReporter getModuleChangeReporter() {
        return earimpl;
    }

    public EarImpl getEarImpl() {
        return earimpl;
    }

    /**
     * Returns source deployment configuration file path for the given deployment 
     * configuration file name. 
     * 
     * @param name file name of the deployement configuration file.
     * @return non-null absolute path to the deployment configuration file.
     */
    public File getDeploymentConfigurationFile(String name) {
        if (name == null) {
            return null;
        }
        String path = getConfigSupport().getContentRelativePath(name);
        if (path == null) {
            path = name;
        }
//        System.out.println("EMPI: getDeploymentConfigFile=" + name);
        return earimpl.getDDFile(path);
    }



    /**
     * Finds source deployment configuration file object for the given deployment 
     * configuration file name.  
     * 
     * @param name file name of the deployement configuration file.
     * @return FileObject of the configuration descriptor file; null if the file does not exists.
     */
    public FileObject findDeploymentConfigurationFile(String name) {
        File fil = getDeploymentConfigurationFile(name);
        if (fil != null) {
            return FileUtil.toFileObject(fil);
        }
        return null;
    }

    @Override
    public void setServerInstanceID(String newID) {
       String oldID = null;
        if (serverInstanceID != null) {
            oldID = MavenProjectSupport.obtainServerID(serverInstanceID);
        }
        serverInstanceID = newID;
        fireServerChange(oldID, getServerID());  
    }
    
    @Override
    public String getServerInstanceID() {
        if (serverInstanceID != null && MavenProjectSupport.obtainServerID(serverInstanceID) != null) {
            return serverInstanceID;
        }
        return ExecutionChecker.DEV_NULL;
    }
    
    @Override
    public String getServerID() {
        if (serverInstanceID != null) {
            String serverID = MavenProjectSupport.obtainServerID(serverInstanceID);
            if (serverID != null) {
                return serverID;
            }
        }
        return ExecutionChecker.DEV_NULL;
    }
    
    @Override
    public FileObject[] getSourceRoots() {
       ProjectSourcesClassPathProvider cppImpl = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
        ClassPath cp = cppImpl.getProjectSourcesClassPath(ClassPath.SOURCE);
        NbMavenProject prj = project.getLookup().lookup(NbMavenProject.class);
        List<URL> resUris = new ArrayList<>();
        for (URI uri : prj.getResources(false)) {
            try {
                resUris.add(uri.toURL());
            } catch (MalformedURLException ex) {
//                Exceptions.printStackTrace(ex);
            }
        }
        Iterator<ClassPath.Entry> en = cp.entries().listIterator();
        List<FileObject> toRet = new ArrayList<>();
        int index = 0;
        while (en.hasNext()) {
            ClassPath.Entry ent = en.next();
            if (ent.getRoot() == null) continue;
            if (resUris.contains(ent.getURL())) {
                //put resources up front..
                toRet.add(index, ent.getRoot());
                index = index + 1;
            } else {
                toRet.add(ent.getRoot());
            }
        }
        return toRet.toArray(FileObject[]::new);
    }

    @Override
    public DeployOnSaveClassInterceptor getDeployOnSaveClassInterceptor() {
        return new DeployOnSaveClassInterceptor() {

            @Override
            public Artifact convert(Artifact original) {
                for (J2eeModuleProvider provider : getChildModuleProviders()) {
                    DeployOnSaveClassInterceptor interceptor = provider.getDeployOnSaveClassInterceptor();
                    if (interceptor != null) {
                        Artifact converted = interceptor.convert(original);
                        if (converted != original) {
                            return converted;
                        }
                    }
                }
                return original;
            }
        };
    }

    @Override
    public DeployOnSaveSupport getDeployOnSaveSupport() {
        return deployOnSaveSupport;
    }
    
    /**
     * This class is proxying events from child listeners.
     */
    private class DeployOnSaveSupportProxy implements ArtifactListener, DeployOnSaveSupport {

        private final List<ArtifactListener> listeners = new ArrayList<>();

        public DeployOnSaveSupportProxy() {
            super();
        }

        @Override
        public synchronized void addArtifactListener(ArtifactListener listener) {
            //copyOnSaveSupport.addArtifactListener(listener);

            boolean register = listeners.isEmpty();
            if (listener != null) {
                if(listener == DeployOnSaveSupportProxy.this) {
                    Logger.getLogger(EarModuleProviderImpl.class.getName()).log(Level.WARNING, "DeployOnSaveSupportProxy.addArtifactListener for project {0} was about to register itself as a listener!", project.getProjectDirectory());
                } else {                
                    listeners.add(listener);
                }
            }

            if (register) {
                for (J2eeModuleProvider provider : getChildModuleProviders()) {
                    DeployOnSaveSupport support = provider.getDeployOnSaveSupport();
                    if (support != null && support != this) { // how comes? issue #257106
                        support.addArtifactListener(this);
                    }
                }
            }
        }

        @Override
        public synchronized void removeArtifactListener(ArtifactListener listener) {
            //copyOnSaveSupport.removeArtifactListener(listener);

            if (listener != null) {
                listeners.remove(listener);
            }

            if (listeners.isEmpty()) {
                for (J2eeModuleProvider provider : getChildModuleProviders()) {
                    DeployOnSaveSupport support = provider.getDeployOnSaveSupport();
                    if (support != null && support != this) { // how comes? issue #257106
                        support.removeArtifactListener(this);
                    }
                }
            }
        }

        @Override
        public boolean containsIdeArtifacts() {
            for (J2eeModuleProvider provider : getChildModuleProviders()) {
                DeployOnSaveSupport support = provider.getDeployOnSaveSupport();
                if (support != null && support != this) { // how comes? issue #257106
                    if (support.containsIdeArtifacts()) {
                        return true;
                    }
                }
            }
            return false;
        }

        
        @Override
        public void artifactsUpdated(Iterable<Artifact> artifacts) {
            List<ArtifactListener> toFire = null;
            synchronized (this) {
                toFire = new ArrayList<ArtifactListener>(listeners);
            }
            for (ArtifactListener listener : toFire) {
                listener.artifactsUpdated(artifacts);
            }
        }
    }    
}
