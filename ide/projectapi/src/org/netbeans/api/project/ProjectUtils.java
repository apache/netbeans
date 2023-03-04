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

package org.netbeans.api.project;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.projectapi.AuxiliaryConfigBasedPreferencesProvider;
import org.netbeans.modules.projectapi.AuxiliaryConfigImpl;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.CacheDirectoryProvider;
import org.netbeans.spi.project.DependencyProjectProvider;
import org.netbeans.spi.project.ParentProjectProvider;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.ProjectContainerProvider;
import org.netbeans.spi.project.ProjectInformationProvider;
import org.netbeans.spi.project.RootProjectProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Mutex.ExceptionAction;
import org.openide.util.MutexException;
import org.openide.util.Parameters;

/**
 * Utility methods to get information about {@link Project}s.
 * @author Jesse Glick
 */
public class ProjectUtils {

    private ProjectUtils() {}

    private static final Logger LOG = Logger.getLogger(ProjectUtils.class.getName());
    
    /**
     * Returns the active configuration for the given project. Returns {@code null},
     * if the project does not support configurations at all.
     * 
     * @param p the project
     * @return the active configuration, or {@code null} if configurations are not supported.
     * @since 1.89
     */
    public ProjectConfiguration getActiveConfiguration(@NonNull Project p) {
        ProjectConfigurationProvider pcp = p.getLookup().lookup(ProjectConfigurationProvider.class);
        if (pcp == null) {
            return null;
        }
        return ProjectManager.mutex().readAccess(() -> pcp.getActiveConfiguration());
    }
    
    /**
     * Sets the active configuration to a project. The configuration should have been previously obtained by 
     * {@link #getActiveConfiguration(org.netbeans.api.project.Project)} from the same project. The method
     * returns {@code false}, if the configuration could not be set: if the project does not support configurations
     * at all, or the project failed to switch the configurations. Since the active configuration setting is persisted,
     * the method throws {@link IOException} if the setting save fails.
     * 
     * @param <C> configuration type
     * @param p the project
     * @param cfg configuration or {@code null} for default configuration.
     * @return true, if the configuration was successfully set.
     * @throws IOException when the selected configuration cannot be persisted.
     * @since 1.89
     */
    public <C extends ProjectConfiguration> boolean setActiveConfiguration(@NonNull Project p, @NonNull C cfg) throws IOException {
        ProjectConfigurationProvider<C> pcp = p.getLookup().lookup(ProjectConfigurationProvider.class);
        if (pcp == null) {
            return false;
        }
        try {
            try {
                return (Boolean)ProjectManager.mutex().writeAccess(new ExceptionAction() {
                    @Override
                    public Object run() throws Exception {
                        if (!pcp.getConfigurations().contains(cfg)) {
                            return false;
                        }
                        pcp.setActiveConfiguration(cfg);
                        return cfg == null || cfg.equals(pcp.getActiveConfiguration());
                    }
                });
            } catch (MutexException ex) {
                throw ex.getException();
            }
        } catch (IOException ex) {
            throw ex;
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }
    
    /**
     * Get basic information about a project.
     * If the project has a {@link ProjectInformation} instance in its lookup,
     * that is used. Otherwise, a basic dummy implementation is returned.
     * @param p a project
     * @return some information about it
     * @see Project#getLookup
     */
    public static ProjectInformation getInformation(@NonNull Project p) {
        final ProjectInformationProvider pip = Lookup.getDefault().lookup(ProjectInformationProvider.class);
        if (pip == null) {
            throw new IllegalStateException("No ProjectInformationProvider found in global Lookup.");  //NOI18N
        }
        return pip.getProjectInformation(p);
    }
    
    /**
     * Get a list of sources for a project.
     * If the project has a {@link Sources} instance in its lookup,
     * that is used. Otherwise, a basic implementation is returned
     * using {@link GenericSources#genericOnly}.
     * @param p a project
     * @return a list of sources for it
     * @see Project#getLookup
     */
    public static Sources getSources(@NonNull Project p) {
        Parameters.notNull("p", p); //NOI18N
        Lookup l = p.getLookup();
        Sources s = l.lookup(Sources.class);
        if (s != null) {
            return s;
        } else {
            return GenericSources.genericOnly(p);
        }
    }
    
    /**
     * Check whether a project has, or might have, cycles in its subproject graph.
     * <p>
     * If the candidate parameter is null, this simply checks whether the master
     * project's current directed graph of (transitive) subprojects contains any
     * cycles. If the candidate is also passed, this checks whether the master
     * project's subproject graph would contain cycles if the candidate were added
     * as a (direct) subproject of the master project.
     * </p>
     * <p>
     * All cycles are reported even if they do not contain the master project.
     * </p>
     * <p>
     * If the master project already contains the candidate as a (direct) subproject,
     * the effect is as if the candidate were null.
     * </p>
     * <p>
     * Projects with no {@link SubprojectProvider} are considered to have no
     * subprojects, just as if the provider returned an empty set.
     * </p>
     * <p>
     * Acquires read access.
     * </p>
     * <p class="nonnormative">
     * Project types which let the user somehow configure subprojects in the GUI
     * (perhaps indirectly, e.g. via a classpath) should use this call to check
     * for possible cycles before adding new subprojects.
     * </p>
     * @param master a project to root the subproject graph from
     * @param candidate a potential direct subproject of the master project, or null
     * @return true if the master project currently has a cycle somewhere in its
     *         subproject graph, regardless of the candidate parameter, or if the
     *         candidate is not null and the master project does not currently have
     *         a cycle but would have one if the candidate were added as a subproject
     * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=43845">Issue #43845</a>
     */
    public static boolean hasSubprojectCycles(final Project master, final Project candidate) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Boolean>() {
            @Override
            public Boolean run() {
                return visit(new HashMap<Project,Boolean>(), master, master, candidate);
            }
        });
    }
    
    /**
     * Utility method for access to {@link DependencyProjectProvider}, a less vague variant of the {@link SubprojectProvider} for code
     * that wants to access project's dependencies that are also projects. Even when recursive, will only use DependencyProjectProvider on other projects.
     * Unlike some java level API this doesn't distinguish between compile, runtime, test level dependencies. 
     * 
     * @param root project where to start calculating dependencies
     * @param recursive true if entire dependency tree should be calculated, 
     *                some project implementation can return just direct dependency projects that themselves have dependency projects.
     *                Please note that false value does NOT guarantee that only direct dependency projects will be returned.
     * @return null if project doesn't have {@link DependencyProjectProvider} in it's lookup, or a set with projects, ordering not mandated
     * @since 1.56
     */
    public static Set<Project> getDependencyProjects(@NonNull Project root, boolean recursive) {
        DependencyProjectProvider prov = root.getLookup().lookup(DependencyProjectProvider.class);
        if (prov != null) {
            Set<Project> toRet = new HashSet<Project>();
            DependencyProjectProvider.Result res = prov.getDependencyProjects();
            toRet.addAll(res.getProjects());
            if (recursive && !res.isRecursive()) {
                for (Project p : res.getProjects()) {
                    Set<Project> subs = getDependencyProjects(p, recursive);
                    if (subs != null) {
                        toRet.addAll(subs);
                    }
                }
            }
            return toRet;
        }
        return null;
    }
    
    /**
     * Utility method for access to {@link ProjectContainerProvider}, a less vague variant of the {@link SubprojectProvider} for code
     * that wants to access projects that the current project serves as container for. Eg. in case of Maven based projects it means projects referenced by &lt;modules&gt;
     * pom.xml section.
     * Even when recursive, will only use ProjectContainerProvider on other projects.
     * 
     * @param root project where to start calculating contained projects
     * @param recursive true if entire container tree should be calculated, 
     *                some project implementation can return just direct subprojects that themselves contain projects.
     *                Please note that false value does NOT guarantee that only direct projects will be returned.
     * @return null if project doesn't have {@link ProjectContainerProvider} in it's lookup, or a set with projects, ordering not mandated
     * @since 1.56
     */
    public static Set<Project> getContainedProjects(@NonNull Project root, boolean recursive) {
        ProjectContainerProvider prov = root.getLookup().lookup(ProjectContainerProvider.class);
        if (prov != null) {
            Set<Project> toRet = new HashSet<Project>();
            ProjectContainerProvider.Result res = prov.getContainedProjects();
            toRet.addAll(res.getProjects());
            if (recursive && !res.isRecursive()) {
                for (Project p : res.getProjects()) {
                    Set<Project> subs = getContainedProjects(p, recursive);
                    if (subs != null) {
                        toRet.addAll(subs);
                    }
                }
            }
            return toRet;
        }
        return null;
    }


    /**
     * Utility method for {@link ParentProjectProvider}. If the given project
     * support {@link ParentProjectProvider} this method will return the immediate
     * parent of that project or <code>null</code> if that can not be determined
     * or the project has no parent. This method also returns <code>null</code>
     * if the given project has no {@link ParentProjectProvider} support.
     *
     * @param project a suspected child project
     * @return the immediate parent of the given project if known or <code>null</code>.
     * @since 1.79
     */
    public static Project parentOf(@NonNull Project project) {
        ParentProjectProvider pvd = project.getLookup().lookup(ParentProjectProvider.class);
        return pvd != null ? pvd.getPartentProject() : null;
    }

    /**
     * Utility method for {@link RootProjectProvider}. If the given project
     * support {@link RootProjectProvider} this method will return its farthest
     * parent.If the given project itself is root the it returns that. If the
     * the farthest parent cannot be determined the given project is considered
     * to be a root project and will be returned.
     *
     * @param project a suspected child project
     * @return the farthest parent of the given project if known or <code>this</code>.
     * @since 1.79
     */
    public static Project rootOf(@NonNull Project project) {
        RootProjectProvider pvd = project.getLookup().lookup(RootProjectProvider.class);
        return pvd != null ? pvd.getRootProject() : project;
    }

    /**
     * Return {@link Preferences} for the given project and given module.
     * 
     * <p class="nonnormative">
     * The preferences are stored in the project using either {@link AuxiliaryConfiguration}
     * or {@link AuxiliaryProperties}.
     * </p>
     * 
     * @param project project for which preferences should be returned
     * @param clazz module specification as in {@link org.openide.util.NbPreferences#forModule(java.lang.Class)}
     * @param shared whether the returned settings should be shared
     * @return {@link Preferences} for the given project
     * @since 1.16
     */
    public static Preferences getPreferences(@NonNull Project project, @NonNull Class clazz, boolean shared) {
        Parameters.notNull("project", project);
        Parameters.notNull("clazz", clazz);
        
        return AuxiliaryConfigBasedPreferencesProvider.getPreferences(project, clazz, shared);
    }
    
    /**
     * Do a DFS traversal checking for cycles.
     * @param encountered projects already encountered in the DFS
     * @param curr current node to visit
     * @param master the original master project (for use with candidate param)
     * @param candidate a candidate added subproject for master, or null
     */
    private static boolean visit(@NonNull Map<Project,Boolean> encountered, @NonNull Project curr, Project master, @NullAllowed Project candidate) {
        if (encountered.containsKey(curr)) {
            if (encountered.get(curr)) {
                return false;
            } else {
                LOG.log(Level.FINE, "Encountered cycle in {0} from {1} at {2} via {3}", new Object[] {master, candidate, curr, encountered});
                return true;
            }
        }
        encountered.put(curr, false);
        SubprojectProvider spp = curr.getLookup().lookup(SubprojectProvider.class);
        if (spp != null) {
            Set<? extends Project> subprojects = spp.getSubprojects();
            LOG.log(Level.FINEST, "Found subprojects {0} from {1}", new Object[] {subprojects, curr});
            for (Project child : subprojects) {
                if (visit(encountered, child, master, candidate)) {
                    return true;
                } else if (candidate == child) {
                    candidate = null;
                }
            }
        }
        if (candidate != null && curr == master) {
            if (visit(encountered, candidate, master, candidate)) {
                return true;
            }
        }
        assert !encountered.get(curr);
        encountered.put(curr, true);
        return false;
    }

    /**
     * Find a way of storing extra configuration in a project.
     * If the project's {@linkplain Project#getLookup lookup} does not provide an instance,
     * a fallback implementation is used.
     * <p class="nonnormative">
     * The current fallback implementation uses {@linkplain FileObject#setAttribute file attributes}
     * for "nonsharable" configuration, and a specially named file in the project directory
     * for "sharable" configuration. For compatibility purposes (in case a project adds an
     * {@link AuxiliaryConfiguration} instance to its lookup where before it had none),
     * the fallback storage is read (but not written) even if there is an instance in project lookup.
     * </p>
     * @param project a project
     * @return an auxiliary configuration handle
     * @since org.netbeans.modules.projectapi/1 1.17
     */
    public static AuxiliaryConfiguration getAuxiliaryConfiguration(@NonNull Project project) {
        Parameters.notNull("project", project);
        return new AuxiliaryConfigImpl(project);
    }

    /**
     * Gets a directory in which modules may store arbitrary extra unversioned files
     * associated with a project.
     * These could be caches of information found in sources, logs or snapshots
     * from activities associated with developing the project, etc.
     * <p>
     * If the project supplies a {@link CacheDirectoryProvider}, that will be used
     * for the parent directory. Otherwise an unspecified storage area will be used.
     * @param project a project
     * @param owner a class from the calling module (each module or package will get its own space)
     * @return a directory available for storing miscellaneous files
     * @throws IOException if no such directory could be created
     * @since org.netbeans.modules.projectapi/1 1.26
     */
    public static FileObject getCacheDirectory(@NonNull Project project, @NonNull Class<?> owner) throws IOException {
        FileObject d;
        CacheDirectoryProvider cdp = project.getLookup().lookup(CacheDirectoryProvider.class);
        if (cdp != null) {
            d = cdp.getCacheDirectory();
        } else {
            d = FileUtil.createFolder(FileUtil.getConfigRoot(),
                    String.format("Projects/extra/%s-%08x", getInformation(project).getName().replace('/', '_'), // NOI18N
                                  project.getProjectDirectory().getPath().hashCode()));
        }
        return FileUtil.createFolder(d, AuxiliaryConfigBasedPreferencesProvider.findCNBForClass(owner));
    }

}
