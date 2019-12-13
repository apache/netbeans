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
package org.netbeans.modules.maven.modelcache;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.execution.DefaultMavenExecutionResult;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.maven.M2AuxilaryConfigImpl;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.Mutex.Action;
import org.openide.util.NbBundle;

/**
 * externalize the creation of MavenProject instances outside of NbMavenProjectImpl
 * and be able to access it without a project at hand
 * @author mkleint
 */
public final class MavenProjectCache {
    
    private static final Logger LOG = Logger.getLogger(MavenProjectCache.class.getName());
    private static final String CONTEXT_EXECUTION_RESULT = "NB_Execution_Result";
    private static final String CONTEXT_PARTICIPANTS = "NB_AbstractParticipant_Present";
    
    private static final Set<String> PARTICIPANT_WHITELIST = new HashSet<String>(Arrays.asList(new String[] {
        "org.sonatype.nexus.maven.staging.deploy.DeployLifecycleParticipant"
    }));
    
    //File is referenced during lifetime of the Project. FileObject cannot be used as with rename it changes value@!!!
    private static final Map<File, WeakReference<MavenProject>> file2Project = new WeakHashMap<File, WeakReference<MavenProject>>();
    private static final Map<File, Mutex> file2Mutex = new WeakHashMap<File, Mutex>();
    
    public static void clearMavenProject(final File pomFile) {
        Mutex mutex = getMutex(pomFile);
        mutex.writeAccess(new Action<MavenProject>() {
            @Override
            public MavenProject run() {
                file2Project.remove(pomFile);
                return null;
            }
        });
    }
    
    /**
     * returns a MavenProject instance for given folder, if folder contains a pom.xml always returns an instance, if not returns null
     * @param pomFile
     * @param reload consult the cache and return the cached value if true, otherwise force a reload.
     * @return 
     */
    public static MavenProject getMavenProject(final File pomFile, final boolean reload) {
        Mutex mutex = getMutex(pomFile);
        MavenProject mp = mutex.writeAccess(new Action<MavenProject>() {
            @Override
            public MavenProject run() {
                if (!reload) {
                    WeakReference<MavenProject> ref = file2Project.get(pomFile);
                    if (ref != null) {
                        MavenProject mp = ref.get();
                        if (mp != null) {
                            return mp;
                        }
                    }
                }
                MavenProject mp = loadOriginalMavenProject(pomFile);
                file2Project.put(pomFile, new WeakReference<MavenProject>(mp));
                return mp;
            }
        });

        return mp;
    }
    
    public static MavenExecutionResult getExecutionResult(MavenProject project) {
        return (MavenExecutionResult) project.getContextValue(CONTEXT_EXECUTION_RESULT);
    }
    
    public static boolean unknownBuildParticipantObserved(MavenProject project) {
        return project.getContextValue(CONTEXT_PARTICIPANTS) != null;
    }
    
    /**
     * list of class names of build participants in the project, null when none are present.
     * @param project
     * @return 
     */
    public static Collection<String> getUnknownBuildParticipantsClassNames(MavenProject project) {
        return (Collection<String>) project.getContextValue(CONTEXT_PARTICIPANTS);
    }
    
       @NbBundle.Messages({
        "TXT_RuntimeException=RuntimeException occurred in Apache Maven embedder while loading",
        "TXT_RuntimeExceptionLong=RuntimeException occurred in Apache Maven embedder while loading the project. \n"
            + "This is preventing the project model from loading properly. \n"
            + "Please file a bug report with details about your project and the IDE's log file.\n\n"
    })
    private static @NonNull MavenProject loadOriginalMavenProject(final File pomFile) {
        long startLoading = System.currentTimeMillis();
        MavenEmbedder projectEmbedder = EmbedderFactory.getProjectEmbedder();
        MavenProject newproject = null;
        //TODO have independent from M2AuxiliaryConfigImpl
        FileObject projectDir = FileUtil.toFileObject(pomFile.getParentFile());
        if (projectDir == null || !projectDir.isValid()) {
            return getFallbackProject(pomFile);
        }
        AuxiliaryConfiguration aux = new M2AuxilaryConfigImpl(projectDir, false);
        ActiveConfigurationProvider config = new ActiveConfigurationProvider(projectDir, aux);
        M2Configuration active = config.getActiveConfiguration();
        MavenExecutionResult res = null;
        try {
            List<String> mavenConfigOpts = Collections.emptyList();
            for (FileObject root = projectDir; root != null; root = root.getParent()) {
                FileObject mavenConfig = root.getFileObject(".mvn/maven.config");
                if (mavenConfig != null && mavenConfig.isData()) {
                    mavenConfigOpts = Arrays.asList(mavenConfig.asText().split("\\s+"));
                    break;
                }
            }
            final MavenExecutionRequest req = projectEmbedder.createMavenExecutionRequest();
            req.addActiveProfiles(active.getActivatedProfiles());
            BiConsumer<String, String> addActiveProfiles = (opt, prefix) -> req.addActiveProfiles(Arrays.asList(opt.substring(prefix.length()).split(",")));
            Iterator<String> optIt = mavenConfigOpts.iterator();
            while (optIt.hasNext()) {
                String opt = optIt.next();
                // Could try to write/integrate a more general option parser here,
                // but some options like -amd anyway violate GNU style.
                if (opt.equals("-P") || opt.equals("--activate-profiles")) {
                    addActiveProfiles.accept(optIt.next(), "");
                } else if (opt.startsWith("-P")) {
                    addActiveProfiles.accept(opt, "-P");
                } else if (opt.startsWith("--activate-profiles=")) {
                    addActiveProfiles.accept(opt, "--activate-profiles=");
                }
            }

            req.setPom(pomFile);
            req.setNoSnapshotUpdates(true);
            req.setUpdateSnapshots(false);
            //MEVENIDE-634 i'm wondering if this fixes the issue
            req.setInteractiveMode(false);
            // recursive == false is important to avoid checking all submodules for extensions
            // that will not be used in current pom anyway..
            // #135070
            req.setRecursive(false);
            req.setOffline(true);
            //#238800 important to merge, not replace
            Properties uprops = req.getUserProperties();
            BiConsumer<String, String> setProperty = (opt, prefix) -> {
                String value = opt.substring(prefix.length());
                int equals = value.indexOf('=');
                if (equals != -1) {
                    uprops.setProperty(value.substring(0, equals), value.substring(equals + 1));
                } else {
                    uprops.setProperty(value, "true");
                }
            };
            optIt = mavenConfigOpts.iterator();
            while (optIt.hasNext()) {
                String opt = optIt.next();
                if (opt.equals("-D") || opt.equals("--define")) {
                    setProperty.accept(optIt.next(), "");
                } else if (opt.startsWith("-D")) {
                    setProperty.accept(opt, "-D");
                } else if (opt.startsWith("--define=")) {
                    setProperty.accept(opt, "--define=");
                }
            }
            uprops.putAll(createUserPropsForProjectLoading(active.getProperties()));
            req.setUserProperties(uprops);
            res = projectEmbedder.readProjectWithDependencies(req, true);
            newproject = res.getProject();
            
            //#204898
            if (newproject != null) {
                ClassLoader projectRealm = newproject.getClassRealm();
                if (projectRealm != null) {
                    ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(projectRealm);
                    try {
                        //boolean execute = EnableParticipantsBuildAction.isEnabled(aux);
                        List<AbstractMavenLifecycleParticipant> lookup = projectEmbedder.getPlexus().lookupList(AbstractMavenLifecycleParticipant.class);
                        if (lookup.size() > 0) { //just in case..
//                            if (execute) {
//                                LOG.info("Executing External Build Participants...");
//                                MavenSession session = new MavenSession( projectEmbedder.getPlexus(), newproject.getProjectBuildingRequest().getRepositorySession(), req, res );
//                                session.setCurrentProject(newproject);
//                                session.setProjects(Collections.singletonList(newproject));
//                                projectEmbedder.setUpLegacySupport();
//                                projectEmbedder.getPlexus().lookup(LegacySupport.class).setSession(session);
//                                
//                                for (AbstractMavenLifecycleParticipant part : lookup) {
//                                    try {
//                                        Thread.currentThread().setContextClassLoader( part.getClass().getClassLoader() );
//                                        part.afterSessionStart(session);
//                                        part.afterProjectsRead(session);
//                                    } catch (MavenExecutionException ex) {
//                                        Exceptions.printStackTrace(ex);
//                                    }
//                                }
//                            } else {
                                List<String> parts = new ArrayList<String>();
                                for (AbstractMavenLifecycleParticipant part : lookup) {
                                    String name = part.getClass().getName();
                                    if (PARTICIPANT_WHITELIST.contains(name)) {
                                        //#204898 create a whitelist of known not harmful participants that can be just ignored
                                        continue;
                                    }
                                    parts.add(name);
                                }
                                if (parts.size() > 0) {
                                    newproject.setContextValue(CONTEXT_PARTICIPANTS, parts);
                                }
//                            }
                        }
                    } catch (ComponentLookupException e) {
                        // this is just silly, lookupList should return an empty list!
                    } finally {
                        Thread.currentThread().setContextClassLoader(originalClassLoader);
                    }
                }
            }
        } catch (RuntimeException | IOException exc) {
            //guard against exceptions that are not processed by the embedder
            //#136184 NumberFormatException
            LOG.log(Level.INFO, "Runtime exception thrown while loading maven project at " + pomFile, exc); //NOI18N
            res = new DefaultMavenExecutionResult();
            res.addException(exc);
        } finally {
            if (newproject == null) {
                newproject = getFallbackProject(pomFile);
            }
            //#215159 clear the project building request, it references multiple Maven Models via the RepositorySession cache
            //is not used in maven itself, most likely used by m2e only..
            newproject.setProjectBuildingRequest(null);
            //TODO some exceptions in result contain various model caches as well..
            newproject.setContextValue(CONTEXT_EXECUTION_RESULT, res);
            long endLoading = System.currentTimeMillis();
            LOG.log(Level.FINE, "Loaded project in {0} msec at {1}", new Object[] {endLoading - startLoading, pomFile.getPath()});
            if (LOG.isLoggable(Level.FINE) && SwingUtilities.isEventDispatchThread()) {
                LOG.log(Level.FINE, "Project " + pomFile.getPath() + " loaded in AWT event dispatching thread!", new RuntimeException());
            }
        }
        return newproject;
    }
    @NbBundle.Messages({
        "LBL_Incomplete_Project_Name=<partially loaded Maven project>",
        "LBL_Incomplete_Project_Desc=Partially loaded Maven project; try building it."
    })
    public static MavenProject getFallbackProject(File projectFile) throws AssertionError {
        MavenProject newproject = new MavenProject();
        newproject.setGroupId("error");
        newproject.setArtifactId("error");
        newproject.setVersion("0");
        newproject.setPackaging("pom");
        newproject.setName(Bundle.LBL_Incomplete_Project_Name());
        newproject.setDescription(Bundle.LBL_Incomplete_Project_Desc());
        newproject.setFile(projectFile);
        return newproject;
    }
    
    public static boolean isFallbackproject(MavenProject prj) {
        return "error".equals(prj.getGroupId()) && "error".equals(prj.getArtifactId()) && Bundle.LBL_Incomplete_Project_Name().equals(prj.getName());
    }
    
    public static Properties createUserPropsForProjectLoading(Map<String, String> activeConfiguration) {
        Properties props = new Properties();
        if (activeConfiguration != null) {
            props.putAll(activeConfiguration);
        }
        return props;
    }
    
    
    private static Mutex getMutex(File pomFile) {
        synchronized (file2Mutex) {
            Mutex mutex = file2Mutex.get(pomFile);
            if (mutex != null) {
                return mutex;
            }
            mutex = new Mutex();
            file2Mutex.put(pomFile, mutex);
            return mutex;
        }
    }
    
}
