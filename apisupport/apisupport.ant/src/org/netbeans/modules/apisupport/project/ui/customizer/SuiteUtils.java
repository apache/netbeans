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

package org.netbeans.modules.apisupport.project.ui.customizer;

import org.netbeans.modules.apisupport.project.ModuleDependency;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleProjectGenerator;
import org.netbeans.modules.apisupport.project.NbModuleType;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbCollections;

/**
 * Utility methods for miscellaneous suite module operations like moving its
 * subModules between individual suites, removing subModules, adding and other
 * handy methods.<br>
 * Note that some of the methods may acquire {@link ProjectManager#mutex} read
 * or write access. See javadoc to individual methods.
 *
 *
 * @author Martin Krauskopf
 */
public final class SuiteUtils {
    
    // XXX also match "${dir}/somedir/${anotherdir}"
    private static final String ANT_PURE_PROPERTY_REFERENCE_REGEXP = "\\$\\{\\p{Graph}+\\}"; // NOI18N
    
    private static final String PRIVATE_PLATFORM_PROPERTIES = "nbproject/private/platform-private.properties"; // NOI18N
    
    static final String MODULES_PROPERTY = "modules"; // NOI18N
    
    private final @NonNull SuiteProperties suiteProps;
    
    private SuiteUtils(final SuiteProperties suiteProps) {
        assert suiteProps != null;
        this.suiteProps = suiteProps;
    }
    
    /**
     * Gets suite components from the same suite which have set a given suite
     * component as a dependency.
     */
    public static NbModuleProject[] getDependentModules(final NbModuleProject suiteComponent) throws IOException {
        final String cnb = suiteComponent.getCodeNameBase();
        try {
            return ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<NbModuleProject[]>(){
                public NbModuleProject[] run() throws Exception {
                    Set<NbModuleProject> result = new HashSet<NbModuleProject>();
                    SuiteProject suite = SuiteUtils.findSuite(suiteComponent);
                    if (suite == null) { // #88303
                        Util.err.log(ErrorManager.WARNING,
                                "Cannot find suite for the given suitecomponent (" + suiteComponent + ')'); // NOI18N
                    } else {
                        for (NbModuleProject p : SuiteUtils.getSubProjects(suite)) {
                            for (ModuleDependency dep : new ProjectXMLManager(p).getDirectDependencies()) {
                                if (dep.getModuleEntry().getCodeNameBase().equals(cnb)) {
                                    result.add(p);
                                    break;
                                }
                            }
                        }
                    }
                    return result.toArray(new NbModuleProject[0]);
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
    }
    
    /**
     * Reads needed information from the given {@link SuiteProperties} and
     * appropriately replace its all modules with new ones.
     * <p>Acquires write access.</p>
     */
    public static void replaceSubModules(final SuiteProperties suiteProps) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws Exception {
                    SuiteUtils utils = new SuiteUtils(suiteProps);
                    Set<NbModuleProject> currentModules = suiteProps.getSubModules();
                    Set<NbModuleProject> origSubModules = suiteProps.getOrigSubModules();
                    
                    // remove removed modules
                    for (NbModuleProject origModule : origSubModules) {
                        if (!currentModules.contains(origModule)) {
                            Util.err.log("Removing module: " + origModule); // NOI18N
                            removeModule(origModule, suiteProps);
                        }
                    }
                    
                    // add new modules
                    for (NbModuleProject currentModule : currentModules) {
                        if (SuiteUtils.contains(suiteProps.getProject(), currentModule)) {
                            Util.err.log("Module \"" + currentModule + "\" or a module with the same CNB is already contained in the suite."); // NOI18N
                            continue;
                        }
                        utils.addModule(currentModule);
                    }
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
    }
    
    /**
     * Adds the given module to the given suite if it is not already contained
     * there. If the module is already suite component of another suite it will
     * be appropriatelly removed from it (i.e moved from module's current suite
     * to the given suite).
     * <p>Acquires write access.</p>
     */
    public static void addModule(final SuiteProject suite, final NbModuleProject project) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws Exception {
                    final SuiteProperties suiteProps = new SuiteProperties(suite, suite.getHelper(),
                            suite.getEvaluator(), getSubProjects(suite));
                    if (!SuiteUtils.contains(suite, project)) {
                        SuiteUtils utils = new SuiteUtils(suiteProps);
                        utils.addModule(project);
                        suiteProps.storeProperties();
                    } else {
                        Util.err.log("Module \"" + project + "\" or a module with the same CNB is already contained in the suite."); // NOI18N
                    }
                    ProjectManager.getDefault().saveProject(suite);
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
    }
    
    /**
     * Removes module from its current suite if the given module is a suite
     * component and also remove all dependencies on this module from the suite
     * components in the same suite.
     * <p>Acquires write access.</p>
     */
    public static void removeModuleFromSuiteWithDependencies(final NbModuleProject suiteComponent) throws IOException {
        // #164220: ISE "Should not acquire Children.MUTEX..." when removing project from suite and its suite.properties file node is expanded;
        // FS.runAtomicAction should probably never be run within ProjectManager.mutex().writeAccess
        suiteComponent.getProjectDirectory().getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                suiteComponent.setRunInAtomicAction(true);
                try {
                    ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {

                        public Void run() throws Exception {
                            try {
                                NbModuleProject[] modules = SuiteUtils.getDependentModules(suiteComponent);
                                // remove all dependencies on the being removed suite component
                                String cnb = suiteComponent.getCodeNameBase();
                                for (int j = 0; j < modules.length; j++) {
                                    ProjectXMLManager pxm = new ProjectXMLManager(modules[j]);
                                    pxm.removeDependency(cnb);
                                    ProjectManager.getDefault().saveProject(modules[j]);
                                }
                            } catch (IOException x) {
                                Logger.getLogger(SuiteUtils.class.getName()).log(Level.INFO, null, x);
                                // #137021: suite may have broken platform dependency, so just continue
                            }
                            // finally remove suite component itself
                            SuiteUtils.removeModuleFromSuite(suiteComponent);
                            return null;
                        }
                    });
                } catch (MutexException e) {
                    throw (IOException) e.getException();
                } finally {
                    suiteComponent.setRunInAtomicAction(false);
                }
            }
        });
    }
    
    /**
     * Removes module from its current suite if the given module is a suite
     * component. Does nothing otherwise.
     * <p>Acquires write access.</p>
     */
    public static void removeModuleFromSuite(final NbModuleProject suiteComponent) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws Exception {
                    SuiteProject suite = SuiteUtils.findSuite(suiteComponent);
                    if (suite != null) {
                        // detach module from its current suite
                        SuiteProperties suiteProps = new SuiteProperties(suite, suite.getHelper(),
                                suite.getEvaluator(), getSubProjects(suite));
                        removeModule(suiteComponent, suiteProps);
                        suiteProps.storeProperties();
                        ProjectManager.getDefault().saveProject(suite);
                    } else {
                        removeModule(suiteComponent, null);
                    }
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
    }
    
    private void addModule(final NbModuleProject project) throws IOException, IllegalArgumentException {
        // TODO - in case of removing from s1 and adding to s2, custom code switching module owner
        // directly is more appropriate. Between the calls, project metadata are in inconsistent state.
        SuiteUtils.removeModuleFromSuite(project);
        // attach it to the new suite
        attachSubModuleToSuite(project);
    }
    
    /**
     * Detach the given <code>subModule</code> from the suite. This actually
     * means deleting its <em>nbproject/suite.properties</em> and eventually
     * <em>nbproject/private/suite-private.properties</em> if it exists from
     * <code>subModule</code>'s base directory. Also set the
     * <code>subModule</code>'s type to standalone. Then it accordingly set the
     * <code>suite</code>'s properties (see {@link #removeFromProperties})
     * for details).
     * <p>
     * Also saves <code>subModule</code> using {@link ProjectManager#saveProject}.
     * </p>
     */
    private static void removeModule(final NbModuleProject subModule, final SuiteProperties/*or null*/ suiteProps) {
        // #152279: used also for cleanup of standalone modules with suite.properties
        // assert subModule.getModuleType() == NbModuleType.SUITE_COMPONENT : "Not a suite component: " + subModule;
        try {
            if (!Boolean.TRUE.equals(temporaryChange.get())) {
            subModule.getProjectDirectory().getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    try {
                        subModule.setRunInAtomicAction(true);
                        // remove both suite properties files
                        FileObject subModuleDir = subModule.getProjectDirectory();
                        FileObject fo = subModuleDir.getFileObject(
                                "nbproject/suite.properties"); // NOI18N
                        if (fo != null) {
                            // XXX this is a bit dangerous. Surely would be better to delete just the relevant
                            // property from it, then delete it iff it is empty. (Would require that
                            // NbModuleProjectGenerator.createSuiteProperties accept an existing file.)
                            fo.delete();
                        }
                        fo = subModuleDir.getFileObject(
                                "nbproject/private/suite-private.properties"); // NOI18N
                        if (fo != null) {
                            fo.delete();
                        }

                        if (suiteProps != null) {
                            // copy suite's platform.properties to the module (needed by standalone module)
                            FileObject plafPropsFO = suiteProps.getProject().getProjectDirectory().
                                    getFileObject("nbproject/platform.properties"); // NOI18N
                            FileObject subModuleNbProject = subModuleDir.getFileObject("nbproject"); // NOI18N
                            if (subModuleNbProject != null && subModuleNbProject.getFileObject("platform.properties") == null) { // NOI18N
                                FileUtil.copyFile(plafPropsFO, subModuleNbProject, "platform"); // NOI18N
                            }
                        }
                        EditableProperties props = subModule.getHelper().getProperties(PRIVATE_PLATFORM_PROPERTIES);
                        if (props.getProperty("user.properties.file") == null) { // NOI18N
                            String nbuser = System.getProperty("netbeans.user"); // NOI18N
                            if (nbuser != null) {
                                props.setProperty("user.properties.file", new File(nbuser, "build.properties").getAbsolutePath()); // NOI18N
                                subModule.getHelper().putProperties(PRIVATE_PLATFORM_PROPERTIES, props);
                            } else {
                                Util.err.log("netbeans.user system property is not defined. Skipping " + PRIVATE_PLATFORM_PROPERTIES + " creation."); // NOI18N
                            }
                        }

                        SuiteUtils.setNbModuleType(subModule, NbModuleType.STANDALONE);
                        // save subModule
                        ProjectManager.getDefault().saveProject(subModule);
                    } finally {
                        subModule.setRunInAtomicAction(false);
                    }
                }
            });
            }

            // now clean up the suite
            if (suiteProps != null) {
                removeFromProperties(subModule, suiteProps);
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    /**
     * Adjust <em>modules</em> property together with removing appropriate
     * other properties from <code>projectProps</code> and
     * <code>privateProps</code>.
     *
     * @return wheter something has changed or not
     */
    private static boolean removeFromProperties(NbModuleProject moduleToRemove, SuiteProperties suiteProps) {
        String modulesProp = suiteProps.getProperty(MODULES_PROPERTY);
        boolean removed = false;
        if (modulesProp != null) {
            List<String> pieces = new ArrayList<String>(Arrays.asList(PropertyUtils.tokenizePath(modulesProp)));
            for (Iterator<String> piecesIt = pieces.iterator(); piecesIt.hasNext(); ) {
                String unevaluated = piecesIt.next();
                String evaluated = suiteProps.getEvaluator().evaluate(unevaluated);
                if (evaluated == null) {
                    Util.err.log("Cannot evaluate " + unevaluated + " property."); // NOI18N
                    continue;
                }
                if (moduleToRemove.getProjectDirectory() !=
                        suiteProps.getHelper().resolveFileObject(evaluated)) {
                    continue;
                }
                piecesIt.remove();
                String[] newModulesProp = getAntProperty(pieces);
                suiteProps.getModulesListModel().removeModules(Collections.singletonList(moduleToRemove));
                suiteProps.setProperty(MODULES_PROPERTY, newModulesProp);
                removed = true;
                // if the value is pure reference also tries to remove that
                // reference which is nice to have. Otherwise just do nothing.
                if (unevaluated.matches(ANT_PURE_PROPERTY_REFERENCE_REGEXP)) {
                    String key = unevaluated.substring(2, unevaluated.length() - 1);
                    suiteProps.removeProperty(key);
                    suiteProps.removePrivateProperty(key);
                }
                break;
            }
        }
        if (!removed) {
            Util.err.log("Removing of " + moduleToRemove + " was unsuccessful."); // NOI18N
        }
        return removed;
    }
    
    private void attachSubModuleToSuite(NbModuleProject subModule) throws IOException {
        // adjust suite project's properties
        File projectDirF = FileUtil.toFile(subModule.getProjectDirectory());
        File suiteDirF = suiteProps.getProjectDirectoryFile();
        String projectPropKey = generatePropertyKey(subModule);
        String rel = PropertyUtils.relativizeFile(suiteDirF, projectDirF);
        //mkleint: removed CollocationQuery.areCollocated() reference
        // when AlwaysRelativeCQI gets removed the condition resolves to false more frequently.
        // that might not be desirable.
        if (rel != null) {
            suiteProps.setProperty(projectPropKey,
                    rel);
        } else {
            suiteProps.setPrivateProperty(projectPropKey, projectDirF.getAbsolutePath());
        }
        String origModules = suiteProps.getProperty(MODULES_PROPERTY);
        StringBuilder modules = new StringBuilder(origModules == null ? "" : origModules);
        if (modules.length() > 0) {
            modules.append(':');
        }
        modules.append("${").append(projectPropKey).append('}'); // NOI18N
        suiteProps.setProperty(MODULES_PROPERTY, modules.toString().split("(?<=:)", -1)); // NOI18N
        
        // adjust subModule's properties
        NbModuleProjectGenerator.createSuiteProperties(subModule.getProjectDirectory(), suiteDirF);
        setNbModuleType(subModule, NbModuleType.SUITE_COMPONENT);
        ProjectManager.getDefault().saveProject(subModule);
    }
    
    /** Generates unique property key suitable for a given modules. */
    private String generatePropertyKey(final Project subModule) {
        String key = "project." + ProjectUtils.getInformation(subModule).getName(); // NOI18N
        String modules = suiteProps.getProperty(MODULES_PROPERTY);
        String[] keys = modules != null ? modules.split("(?<=:)", -1) : new String[0]; // NOI18N
        int index = 0;
        while (Arrays.binarySearch(keys, "${" + key + "}") >= 0) { // NOI18N
            key += "_" + ++index; // NOI18N
        }
        return key;
    }
    
    private static void setNbModuleType(NbModuleProject module, NbModuleType type) throws IOException {
        ProjectXMLManager pxm = new ProjectXMLManager((module));
        pxm.setModuleType(type);
        module.refreshLookup(); // #160604: add SuiteProvider to lookup
    }
    
    public static String[] getAntProperty(final Collection<String> pieces) {
        List<String> l = new ArrayList<String>();
        for (Iterator<String> it = pieces.iterator(); it.hasNext();) {
            String piece = it.next() + (it.hasNext() ? ":" : ""); // NOI18N
            l.add(piece);
        }
        return l.toArray(new String[0]);
    }
    
    /**
     * Returns whether a given directory contains regular <em>suite</em>. Note
     * it returns <code>false</code> for suite components.
     *
     * @return <code>true</code> if a given directory contains regular
     *         <em>suite</em>; <code>false</code> otherwise.
     */
    public static boolean isSuite(final File maybeSuiteDir) {
        boolean isSuite = false;
        try {
            FileObject dirFO = FileUtil.toFileObject(maybeSuiteDir);
            if (dirFO != null) {
                Project maybeSuite = ProjectManager.getDefault().findProject(dirFO);
                if (maybeSuite != null) {
                    isSuite = maybeSuiteDir.equals(getSuiteDirectory(maybeSuite));
                }
            }
        } catch (IOException e) {
            // leave it false
        }
        return isSuite;
    }
    
    /**
     * Returns suite for the given suite component. May return
     * <code>null</code>.
     * <p>Acquires read access.</p>
     */
    public static SuiteProject findSuite(final Project suiteComponent) throws IOException {
        try {
            return ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<SuiteProject>(){
                public SuiteProject run() throws Exception {
                    Project suite = null;
                    File suiteDir = SuiteUtils.getSuiteDirectory(suiteComponent);
                    if (suiteDir != null) {
                        FileObject fo = FileUtil.toFileObject(suiteDir);
                        if (fo == null) {
                            Util.err.log(ErrorManager.WARNING, "Module in the \"" + // NOI18N
                                    FileUtil.toFile(suiteComponent.getProjectDirectory()).getAbsolutePath() +
                                    "\" directory claims to be a subcomponent of a suite in the \"" + // NOI18N
                                    suiteDir.getAbsolutePath() + "\" which does not exist however."); // NOI18N
                        } else {
                            suite = ProjectManager.getDefault().findProject(fo);
                        }
                    }
                    return suite instanceof SuiteProject ? (SuiteProject) suite : /* #80786 */null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
    }
    
    /**
     * Returns whether a given suite already contains a given project or a
     * project with the same code name base.
     */
    public static boolean contains(final SuiteProject suite, final NbModuleProject project) {
        Set<NbModuleProject> subModules = getSubProjects(suite);
        if (subModules.contains(project)) {
            return true;
        }
        for (Iterator it = subModules.iterator(); it.hasNext();) {
            NbModuleProject p = (NbModuleProject) it.next();
            if (p.getCodeNameBase().equals(project.getCodeNameBase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Utility method to acquire modules contains within a given suite. Just
     * delegates to {@link SubprojectProvider#getSubprojects()}.
     */
    public static Set<NbModuleProject> getSubProjects(final Project suite) {
        assert suite != null;
        SubprojectProvider spp = suite.getLookup().lookup(SubprojectProvider.class);
        return NbCollections.checkedSetByFilter(spp.getSubprojects(), NbModuleProject.class, true);
    }
    
    /**
     * Convenient method for getting a suite directory from a given project
     * which should contain an instance of {@link SuiteProvider} in its lookup.
     * @return either suite directory or <code>null</code>
     */
    public static File getSuiteDirectory(final Project project) {
        File suiteDir = null;
        SuiteProvider sp = project.getLookup().lookup(SuiteProvider.class);
        if (sp != null) {
            suiteDir = sp.getSuiteDirectory();
        }
        return suiteDir;
    }
    
    /**
     * Returns {@link #getSuiteDirectory}'s absolute path.
     * @return path or <code>null</code>
     */
    public static String getSuiteDirectoryPath(final Project project) {
        File suiteDir = getSuiteDirectory(project);
        return suiteDir != null ? suiteDir.getAbsolutePath() : null;
    }

    private static final ThreadLocal<Boolean> temporaryChange = new ThreadLocal<Boolean>();
    
    public static <T> T moving (Callable<T> callable) throws IOException {
        Boolean temp = temporaryChange.get();
        try {
            temporaryChange.set(true);
            return callable.call();
        } catch (Exception ex) {
            if (ex instanceof IOException) {
                throw (IOException) ex;
            } else {
                throw new IOException(ex);
            }
        } finally {
            temporaryChange.set(temp);
        }
    }
    
}
