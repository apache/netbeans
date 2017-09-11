/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.modelcache;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;
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
            final MavenExecutionRequest req = projectEmbedder.createMavenExecutionRequest();
            req.addActiveProfiles(active.getActivatedProfiles());

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
        } catch (RuntimeException exc) {
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
