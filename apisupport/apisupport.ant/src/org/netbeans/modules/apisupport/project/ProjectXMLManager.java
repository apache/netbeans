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
package org.netbeans.modules.apisupport.project;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectType;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex.ExceptionAction;
import org.openide.util.MutexException;
import org.openide.util.NbBundle.Messages;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.modules.apisupport.project.universe.NonexistentModuleEntry;
import org.netbeans.modules.apisupport.project.universe.TestModuleDependency;
import org.openide.filesystems.FileSystem;
import org.openide.util.Mutex;
import org.openide.util.Parameters;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import static org.netbeans.modules.apisupport.project.Bundle.*;

/**
 * Convenience class for managing project's <em>project.xml</em> file. You
 * should explicitly enclose a <em>complete</em> operation within write access
 * to prevent race conditions. Use {@link ProjectManager#saveProject} to apply
 * changes <em>physically</em>.
 */
public final class ProjectXMLManager {

    private static final Logger LOG = Logger.getLogger(ProjectXMLManager.class.getName());

    /** Equal to AntProjectHelper.PROJECT_NS which is package private. */
    // XXX is there a better way? (impact of imposibility to use ProjectGenerator)
    private static final String PROJECT_NS =
            "http://www.netbeans.org/ns/project/1"; // NOI18N
    // elements constants
    private static final String BINARY_ORIGIN = "binary-origin"; // NOI18N
    static final String BUILD_PREREQUISITE = "build-prerequisite"; // NOI18N
    private static final String CLASS_PATH_BINARY_ORIGIN = "binary-origin"; //NOI18N
    private static final String CLASS_PATH_EXTENSION = "class-path-extension"; // NOI18N
    private static final String CLASS_PATH_RUNTIME_PATH = "runtime-relative-path"; //NOI18N
    static final String CODE_NAME_BASE = "code-name-base"; // NOI18N
    static final String COMPILE_DEPENDENCY = "compile-dependency"; // NOI18N
    private static final String DATA = "data"; // NOI18N
    static final String DEPENDENCY = "dependency"; // NOI18N
    private static final String EXTRA_COMPILATION_UNIT = "extra-compilation-unit"; // NOI18N
    private static final String FRIEND = "friend"; // NOI18N
    private static final String FRIEND_PACKAGES = "friend-packages"; // NOI18N
    private static final String IMPLEMENTATION_VERSION = "implementation-version"; // NOI18N
    static final String MODULE_DEPENDENCIES = "module-dependencies"; // NOI18N
    private static final String PACKAGE = "package"; // NOI18N
    private static final String PUBLIC_PACKAGES = "public-packages"; // NOI18N
    private static final String RELEASE_VERSION = "release-version"; // NOI18N
    static final String RUN_DEPENDENCY = "run-dependency"; // NOI18N
    private static final String SPECIFICATION_VERSION = "specification-version"; // NOI18N
    private static final String STANDALONE = "standalone"; // NOI18N
    private static final String SUBPACKAGES = "subpackages"; // NOI18N
    private static final String SUITE_COMPONENT = "suite-component"; // NOI18N
    private static final String TEST_DEPENDENCIES = "test-dependencies"; // NOI18N
    private static final String TEST_TYPE_NAME = "name"; // NOI18N
    private static final String TEST_DEPENDENCY = "test-dependency"; // NOI18N
    private static final String TEST_DEPENDENCY_CNB = "code-name-base"; // NOI18N
    private static final String TEST_DEPENDENCY_RECURSIVE = "recursive"; // NOI18N
    private static final String TEST_DEPENDENCY_COMPILE = "compile-dependency"; // NOI18N
    private static final String TEST_DEPENDENCY_TEST = "test"; // NOI18N
    private static final String TEST_TYPE = "test-type"; //NOI18N
    private static final String[] ORDER = {
        CODE_NAME_BASE,
        SUITE_COMPONENT,
        STANDALONE,
        MODULE_DEPENDENCIES,
        TEST_DEPENDENCIES,
        PUBLIC_PACKAGES,
        FRIEND_PACKAGES,
        CLASS_PATH_EXTENSION,
        EXTRA_COMPILATION_UNIT,
    };
    private final NbModuleProject project;
    private NbPlatform customPlaf;
    private String cnb;
    private SortedSet<ModuleDependency> directDeps;
    private ManifestManager.PackageExport[] publicPackages;
    private Map<String, String> cpExtensions;
    private String[] friends;    // cached confData element for easy access with getConfData
    private Element confData;

    /** Creates a new instance of {@link ProjectXMLManager}. */
    public ProjectXMLManager(final @NonNull NbModuleProject project) {
        Parameters.notNull("project", project);
        this.project = project;
    }

    /**
     * Utility mehtod for getting the {@link ProjectXMLManager instance}
     * associated with a project in the given directory.
     *
     * @throws IOException if the project under a given <code>projectDir</code>
     *         was recognized but could not be loaded (see {@link ProjectManager#findProject}).
     */
    public static ProjectXMLManager getInstance(final File projectDir) throws IOException {
        FileObject dir = FileUtil.toFileObject(projectDir);
        if (dir == null) {
            throw new IOException("no project dir " + projectDir);
        }
        NbModuleProject p = (NbModuleProject) ProjectManager.getDefault().findProject(dir);
        if (p == null) {
            throw new IOException("no project in " + projectDir);
        }
        return new ProjectXMLManager(p);
    }

    public void setModuleType(NbModuleType moduleType) {
        Element _confData = getConfData();
        Document doc = _confData.getOwnerDocument();

        Element standaloneEl = findElement(_confData, ProjectXMLManager.STANDALONE);
        if (standaloneEl != null && moduleType == NbModuleType.STANDALONE) {
            // nothing needs to be done - standalone is already set
            return;
        }

        Element suiteCompEl = findElement(_confData, ProjectXMLManager.SUITE_COMPONENT);
        if (suiteCompEl != null && moduleType == NbModuleType.SUITE_COMPONENT) {
            // nothing needs to be done - suiteCompEl is already set
            return;
        }

        if (suiteCompEl == null && standaloneEl == null && moduleType == NbModuleType.NETBEANS_ORG) {
            // nothing needs to be done - nb.org modules don't have any element
            return;
        }

        // Ok, we get here. So clean up....
        if (suiteCompEl != null) {
            _confData.removeChild(suiteCompEl);
        }
        if (standaloneEl != null) {
            _confData.removeChild(standaloneEl);
        }

        // ....and create element for new module type.
        Element newModuleType = createTypeElement(doc, moduleType);
        if (newModuleType != null) {
            _confData.insertBefore(newModuleType, findModuleDependencies(_confData));
        }
        project.putPrimaryConfigurationData(_confData);
    }

    /**
     * Returns direct module dependencies using default module's platform. See
     * {@link #getDirectDependencies(NbPlatform)} for more details to which
     * this method delegates.
     */
    public SortedSet<ModuleDependency> getDirectDependencies() throws IOException {
        return getDirectDependencies(null);
    }

    /**
     * Returns sorted direct module dependencies using {@link
     * ModuleDependency#CNB_COMPARATOR} allowing to pass a custom platform.
     * Since no two modules with the same code name base may be set as a
     * dependency. Also this is ordering used in the <em>project.xml</em>.
     */
    public SortedSet<ModuleDependency> getDirectDependencies(final NbPlatform customPlaf) throws IOException {
        if (this.customPlaf == customPlaf && this.directDeps != null) {
            return this.directDeps;
        }
        this.customPlaf = customPlaf;
        SortedSet<ModuleDependency> _directDeps = new TreeSet<ModuleDependency>(ModuleDependency.CNB_COMPARATOR);
        Element moduleDependencies = findModuleDependencies(getConfData());
        assert moduleDependencies != null : "Cannot find <module-dependencies> for: " + project;
        File prjDirF = project.getProjectDirectoryFile();
        ModuleList ml;
        if (customPlaf != null) {
            ml = ModuleList.getModuleList(prjDirF, customPlaf.getDestDir());
        } else {
            ml = ModuleList.getModuleList(prjDirF);
        }
        for (Element depEl : XMLUtil.findSubElements(moduleDependencies)) {
            Element cnbEl = findElement(depEl, ProjectXMLManager.CODE_NAME_BASE);
            String _cnb = XMLUtil.findText(cnbEl);
            ModuleDependency depToAdd = getModuleDependency(_cnb, ml, depEl);
            if (depToAdd == null) {
                continue;
            }
            if (!_directDeps.add(depToAdd)) {
                throw new IOException("#175879: corrupted metadata in " + project + "; duplicate dep found: " + depToAdd);
            }
        }
        this.directDeps = Collections.unmodifiableSortedSet(_directDeps);
        return this.directDeps;
    }

    private ModuleDependency getModuleDependency(String cnb, ModuleList ml, Element depEl) {
        ModuleEntry me = ml.getEntry(cnb);
        if (me == null) {
            // XXX might be e.g. shown in nb.errorForreground and "disabled"
            Util.err.log(ErrorManager.WARNING,
                    "Detected dependency on module which cannot be found in " + // NOI18N
                    "the current module's universe (platform, suite): " + cnb); // NOI18N
            me = new NonexistentModuleEntry(cnb);
        }
        String relVer = null;
        String specVer = null;
        boolean implDep = false;
        Element runDepEl = findElement(depEl, ProjectXMLManager.RUN_DEPENDENCY);
        if (runDepEl != null) {
            Element relVerEl = findElement(runDepEl, ProjectXMLManager.RELEASE_VERSION);
            if (relVerEl != null) {
                relVer = XMLUtil.findText(relVerEl);
            }
            Element specVerEl = findElement(runDepEl, ProjectXMLManager.SPECIFICATION_VERSION);
            if (specVerEl != null) {
                specVer = XMLUtil.findText(specVerEl);
            }
            implDep = findElement(runDepEl, ProjectXMLManager.IMPLEMENTATION_VERSION) != null;
        }
        Element compDepEl = findElement(depEl, ProjectXMLManager.COMPILE_DEPENDENCY);
        ModuleDependency newDep = new ModuleDependency(
                me, relVer, specVer, compDepEl != null, implDep);
        newDep.buildPrerequisite = findElement(depEl, ProjectXMLManager.BUILD_PREREQUISITE) != null;
        newDep.runDependency = runDepEl != null;
        return newDep;
    }

    public ModuleDependency getModuleDependency(String cnb) throws IOException {
        return getModuleDependency(cnb, null);
    }

    public ModuleDependency getModuleDependency(String cnb, final NbPlatform customPlaf) throws IOException {
        this.customPlaf = customPlaf;
        Element moduleDependencies = findModuleDependencies(getConfData());
        assert moduleDependencies != null : "Cannot find <module-dependencies> for: " + project;
        File prjDirF = project.getProjectDirectoryFile();
        ModuleList ml;
        if (customPlaf != null) {
            ml = ModuleList.getModuleList(prjDirF, customPlaf.getDestDir());
        } else {
            ml = ModuleList.getModuleList(prjDirF);
        }
        for (Element dep : XMLUtil.findSubElements(moduleDependencies)) {
            Element cnbEl = findElement(dep, ProjectXMLManager.CODE_NAME_BASE);
            String depCnb = XMLUtil.findText(cnbEl);
            if (depCnb.equals(cnb)) {
                return getModuleDependency(cnb, ml, dep);
            }
        }
        return null;
    }

    /** Remove given dependency from the configuration data. */
    public void removeDependency(String cnbToRemove) {
        Element _confData = getConfData();
        Element moduleDependencies = findModuleDependencies(_confData);
        for (Element dep : XMLUtil.findSubElements(moduleDependencies)) {
            Element cnbEl = findElement(dep, ProjectXMLManager.CODE_NAME_BASE);
            String _cnb = XMLUtil.findText(cnbEl);
            if (cnbToRemove.equals(_cnb)) {
                moduleDependencies.removeChild(dep);
            }
        }
        project.putPrimaryConfigurationData(_confData);
        directDeps = null;
    }

    /**
     * Use this for removing more than one dependencies. It's faster then
     * iterating and using <code>removeDependency</code> for every entry.
     */
    public void removeDependencies(Collection<ModuleDependency> depsToDelete) {
        Set<String> cnbsToDelete = new HashSet<String>(depsToDelete.size());
        for (ModuleDependency dep : depsToDelete) {
            cnbsToDelete.add(dep.getModuleEntry().getCodeNameBase());
        }
        removeDependenciesByCNB(cnbsToDelete);
        directDeps = null;
    }

    /**
     * Use this for removing more than one dependencies. It's faster then
     * iterating and using <code>removeDependency</code> for every entry.
     */
    public void removeDependenciesByCNB(Collection<String> cnbsToDelete) {
        Element _confData = getConfData();
        Element moduleDependencies = findModuleDependencies(_confData);
        for (Element dep : XMLUtil.findSubElements(moduleDependencies)) {
            Element cnbEl = findElement(dep, ProjectXMLManager.CODE_NAME_BASE);
            String _cnb = XMLUtil.findText(cnbEl);
            if (cnbsToDelete.remove(_cnb)) {
                moduleDependencies.removeChild(dep);
            }
            if (cnbsToDelete.size() == 0) {
                break; // everything was deleted
            }
        }
        if (cnbsToDelete.size() != 0) {
            Util.err.log(ErrorManager.WARNING,
                    "Some modules weren't deleted: " + cnbsToDelete); // NOI18N
        }
        project.putPrimaryConfigurationData(_confData);
        directDeps = null;
    }

    public void editDependency(ModuleDependency origDep, ModuleDependency newDep) {
        Element _confData = getConfData();
        Element moduleDependencies = findModuleDependencies(_confData);
        List<Element> currentDeps = XMLUtil.findSubElements(moduleDependencies);
        for (Iterator<Element> it = currentDeps.iterator(); it.hasNext();) {
            Element dep = it.next();
            Element cnbEl = findElement(dep, ProjectXMLManager.CODE_NAME_BASE);
            String _cnb = XMLUtil.findText(cnbEl);
            if (_cnb.equals(origDep.getModuleEntry().getCodeNameBase())) {
                moduleDependencies.removeChild(dep);
                Element nextDep = it.hasNext() ? it.next() : null;
                createModuleDependencyElement(moduleDependencies, newDep, nextDep);
                break;
            }
        }
        project.putPrimaryConfigurationData(_confData);
        directDeps = null;
    }

    /**
     * Adds given dependency.
     * Checks for dependency cycles, see {@link #replaceDependencies(java.util.Set)} for details.
     */
    public void addDependency(ModuleDependency md) throws IOException, CyclicDependencyException {
        addDependencies(Collections.singleton(md));
    }

    /**
     * Adds given modules as module-dependencies for the project.
     * Checks for dependency cycles, see {@link #replaceDependencies(java.util.Set)} for details.
     */
    public void addDependencies(final Set<ModuleDependency> toAdd) throws IOException, CyclicDependencyException {
        SortedSet<ModuleDependency> deps = new TreeSet<ModuleDependency>(getDirectDependencies());
        if (deps.addAll(toAdd)) {
            replaceDependencies(deps);
        }
    }

    /**
     * Checks if <code>candidates</code> dependencies introduce dependency cycle.
     * In such case returns localized message about which dependency causes the cycle.
     * @param candidates Module dependencies about to be added. May be empty but not <code>null</code>.
     * @return Localized warning message about introduced dependency cycle,
     * <code>null</code> otherwise.
     */
    @Messages({"# {0} - candidate project name", "# {1} - this project name", "MSG_cyclic_dep=Adding project {0} as dependency to {1} would introduce cyclic dependency! Dependency was not added."})
    public String getDependencyCycleWarning(final Set<ModuleDependency> candidates) {
        for (ModuleDependency md : candidates) {
            File srcLoc = md.getModuleEntry().getSourceLocation();
            if (srcLoc == null) {
                continue;
            }
            FileObject srcLocFO = FileUtil.toFileObject(srcLoc);
            if (srcLocFO == null) {
                continue;
            }
            Project candidate;
            try {
                candidate = ProjectManager.getDefault().findProject(srcLocFO);
            } catch (IOException x) {
                continue;
            }
            if (candidate == null) {
                continue;
            }
            boolean cyclicDep = ProjectUtils.hasSubprojectCycles(project, candidate);
            if (cyclicDep) {
                if (ProjectUtils.hasSubprojectCycles(project, null)) {
                    LOG.log(Level.WARNING, "Starting out with subproject cycles in {0} before even changing them", project);
                    return null;
                } else {
                    String c = ProjectUtils.getInformation(candidate).getDisplayName();
                    String m = ProjectUtils.getInformation(project).getDisplayName();
                    return MSG_cyclic_dep(c, m);
                }
            }
        }
        return null;
    }

    /**
     * Exception thrown when dependency cycle is created when storing
     * new dependencies via project XML manager.
     * {@link #getLocalizedMessage()} return warning message about which project caused the cycle.
     */
    public static class CyclicDependencyException extends Exception {
        private CyclicDependencyException(String message) {
            super(message);
        }
    }

    /**
     * Replaces all original dependencies with the given <code>newDeps</code>.
     *
     * Checks for dependency cycles, throws {@link CyclicDependencyException} if new dependencies
     * introduce dependency cycle and leaves current dependencies untouched.
     */
    public void replaceDependencies(final Set<ModuleDependency> newDeps) throws CyclicDependencyException {
        Set<ModuleDependency> addedDeps = new HashSet<ModuleDependency>(newDeps);
        try {
            SortedSet<ModuleDependency> currentDeps = getDirectDependencies();
            addedDeps.removeAll(currentDeps);
            String warning = getDependencyCycleWarning(addedDeps);
            if (warning != null) {
                throw new CyclicDependencyException(warning);
            }
        } catch (IOException x) { // getDirectDependencies
            LOG.log(Level.INFO, null, x); // and skip check
        }

        Element _confData = getConfData();
        Document doc = _confData.getOwnerDocument();
        Element moduleDependencies = findModuleDependencies(_confData);
        _confData.removeChild(moduleDependencies);
        moduleDependencies = createModuleElement(doc, ProjectXMLManager.MODULE_DEPENDENCIES);
        XMLUtil.appendChildElement(_confData, moduleDependencies, ORDER);
        SortedSet<ModuleDependency> sortedDeps = new TreeSet<ModuleDependency>(newDeps);
        for (ModuleDependency md : sortedDeps) {
            createModuleDependencyElement(moduleDependencies, md, null);
        }
        project.putPrimaryConfigurationData(_confData);
        this.directDeps = sortedDeps;
    }

    public void removeClassPathExtensions() {
        Element _confData = getConfData();
        NodeList nl = _confData.getElementsByTagNameNS(NbModuleProject.NAMESPACE_SHARED,
                ProjectXMLManager.CLASS_PATH_EXTENSION);
        int len = nl.getLength();
        for (int i = 0; i < len; i++) {
            _confData.removeChild(nl.item(0));
        }
        cpExtensions = Collections.emptyMap();
        project.putPrimaryConfigurationData(_confData);
    }

    /**
     * Removes test dependency under type <code>testType</code>, indentified
     * by <code>cnbToRemove</code>. Does not remove whole test type even if
     * removed test dependency was the last one.
     */
    public boolean removeTestDependency(String testType, String cnbToRemove) {
        boolean wasRemoved = false;
        Element _confData = getConfData();
        Element testModuleDependenciesEl = findTestDependenciesElement(_confData);
        Element testTypeRemoveEl = null;
        for (Element type : XMLUtil.findSubElements(testModuleDependenciesEl)) {
            Element nameEl = findElement(type, TEST_TYPE_NAME);
            String nameOfType = XMLUtil.findText(nameEl);
            if (testType.equals(nameOfType)) {
                testTypeRemoveEl = type;
            }
        }
        //found such a test type
        if (testTypeRemoveEl != null) {
            for (Element el : XMLUtil.findSubElements(testTypeRemoveEl)) {
                Element cnbEl = findElement(el, TEST_DEPENDENCY_CNB);
                if (cnbEl == null) {
                    continue;   //name node, continue
                }
                String _cnb = XMLUtil.findText(cnbEl);
                if (cnbToRemove.equals(_cnb)) {
                    // found test dependency with desired CNB
                    testTypeRemoveEl.removeChild(el);
                    wasRemoved = true;
                    project.putPrimaryConfigurationData(_confData);
                }
            }
        }
        return wasRemoved;
    }

    /**
     * Adds new test dependency to <code>project.xml</code>. Currently only two test types are
     * supported - <code>UNIT</code> and <code>QA_FUNCTIONAL</code>. Test dependencies under 
     * test types are sorted by CNB.
     */
    public boolean addTestDependency(final String testType, final TestModuleDependency newTestDep) throws IOException {
        final String UNIT = TestModuleDependency.UNIT;
        final String QA_FUNCTIONAL = TestModuleDependency.QA_FUNCTIONAL;
        assert (UNIT.equals(testType) || QA_FUNCTIONAL.equals(testType)) : "Current impl.supports only " + QA_FUNCTIONAL +
                " or " + UNIT + " tests"; // NOI18N

        final ExceptionAction<Boolean> action = new Mutex.ExceptionAction<Boolean>() {
            public Boolean run() throws Exception {
                File projectDir = FileUtil.toFile(project.getProjectDirectory());
                ModuleList ml = ModuleList.getModuleList(projectDir);
                Map<String, Set<TestModuleDependency>> map = new HashMap<String, Set<TestModuleDependency>>(getTestDependencies(ml));
                Set<TestModuleDependency> testDependenciesSet = map.get(testType);
                if (testDependenciesSet == null) {
                    testDependenciesSet = new TreeSet<TestModuleDependency>();
                    map.put(testType, testDependenciesSet);
                } else {
                    // XXX necessary to clone?
                    testDependenciesSet = new TreeSet<TestModuleDependency>(testDependenciesSet);
                }
                if (!testDependenciesSet.add(newTestDep)) {
                    return false; //nothing new to add, dep is already there, finished
                }
                final Element confData = getConfData();
                final Document doc = confData.getOwnerDocument();
                Element testModuleDependenciesEl = findTestDependenciesElement(confData);
                if (testModuleDependenciesEl == null) {      // test dependencies element does not exist, create it
                    testModuleDependenciesEl = createModuleElement(doc, TEST_DEPENDENCIES);
                    XMLUtil.appendChildElement(confData, testModuleDependenciesEl, ORDER);
                }
                Element testTypeEl = null;
                //iterate through test types to determine if testType exist
                for (Element tt : XMLUtil.findSubElements(testModuleDependenciesEl)) {
                    Node nameNode = findElement(tt, "name"); // NOI18N
                    assert nameNode != null : "should be some child with name";
                    //Node nameNode = tt.getFirstChild();
                    //nameNode.getNodeName()
                    assert (TEST_TYPE_NAME.equals(nameNode.getLocalName())) : "name node should be first child, but was:" + nameNode.getLocalName() +
                    "or" + nameNode.getNodeName(); //NOI18N
                    //equals
                    if (nameNode.getTextContent().equals(testType)) {
                        testTypeEl = tt;
                    }
                }

                // #142594: silently switch to /3 schema
                final Element ttEl = testTypeEl;
                final Element tmdEl = testModuleDependenciesEl;
                final Set<TestModuleDependency> tdSet = testDependenciesSet;

                AuxiliaryConfiguration auxConf = project.getHelper().createAuxiliaryConfiguration();
                auxConf.removeConfigurationFragment(NbModuleProject.NAME_SHARED, NbModuleProject.NAMESPACE_SHARED_2, true);

                //? new or existing test type?
                if (ttEl == null) {
                    //this test type, does not exist, create it, and add new test dependency
                    Element newTestTypeEl = createNewTestTypeElement(doc, testType);
                    tmdEl.appendChild(newTestTypeEl);
                    createTestModuleDependencyElement(newTestTypeEl, newTestDep);
                    project.putPrimaryConfigurationData(confData);
                } else {
                    //testtype exists, refresh it
                    Node beforeWhat = ttEl.getNextSibling();
                    tmdEl.removeChild(ttEl);
                    Element refreshedTestTypeEl = createNewTestTypeElement(doc, testType);
                    if (beforeWhat == null) {
                        tmdEl.appendChild(refreshedTestTypeEl);
                    } else {
                        tmdEl.insertBefore(refreshedTestTypeEl, beforeWhat);
                    }
                    for (TestModuleDependency tmd : tdSet) {
                        createTestModuleDependencyElement(refreshedTestTypeEl, tmd);
                        project.putPrimaryConfigurationData(confData);
                    }
                }
                return true;
            }
        };

        final AtomicBoolean result = new AtomicBoolean();
        project.getProjectDirectory().getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                try {
                    project.setRunInAtomicAction(true);
                    result.set(ProjectManager.mutex().writeAccess(action));
                } catch (MutexException ex) {
                    throw (IOException) ex.getCause();
                } finally {
                    project.setRunInAtomicAction(false);
                }
            }
        });
        return result.get();
    }

    private Element createNewTestTypeElement(Document doc, String testTypeName) {
        Element newTestTypeEl = createModuleElement(doc, TEST_TYPE);
        Element nameOfTestTypeEl = createModuleElement(doc, TEST_TYPE_NAME, testTypeName);
        newTestTypeEl.appendChild(nameOfTestTypeEl);
        return newTestTypeEl;
    }

    private void createTestModuleDependencyElement(Element testTypeElement, TestModuleDependency tmd) {
        Document doc = testTypeElement.getOwnerDocument();
        Element tde = createModuleElement(doc, TEST_DEPENDENCY);
        testTypeElement.appendChild(tde);
        tde.appendChild(createModuleElement(doc, TEST_DEPENDENCY_CNB, tmd.getModule().getCodeNameBase()));
        if (tmd.isRecursive()) {
            tde.appendChild(createModuleElement(doc, TEST_DEPENDENCY_RECURSIVE));
        }
        if (tmd.isCompile()) {
            tde.appendChild(createModuleElement(doc, TEST_DEPENDENCY_COMPILE));
        }
        if (tmd.isTest()) {
            tde.appendChild(createModuleElement(doc, TEST_DEPENDENCY_TEST));
        }
    }

    /**
     * Gives a map from test type (e.g. <em>unit</em> or <em>qa-functional</em>)
     * to the set of {@link TestModuleDependency dependencies} belonging to it.
     */
    public @NonNull Map<String, Set<TestModuleDependency>> getTestDependencies(final ModuleList ml) {
        Element testDepsEl = findTestDependenciesElement(getConfData());

        Map<String, Set<TestModuleDependency>> testDeps = new HashMap<String, Set<TestModuleDependency>>();

        if (testDepsEl != null) {
            for (Element typeEl : XMLUtil.findSubElements(testDepsEl)) {
                Element testTypeEl = findElement(typeEl, TEST_TYPE_NAME);
                String testType = null;
                if (testTypeEl != null) {
                    testType = XMLUtil.findText(testTypeEl);
                }
                if (testType == null) {
                    testType = TestModuleDependency.UNIT; // default variant
                }
                Set<TestModuleDependency> directTestDeps = new TreeSet<TestModuleDependency>();
                for (Element depEl : XMLUtil.findSubElements(typeEl)) {
                    if (depEl.getTagName().equals(TEST_DEPENDENCY)) {
                        // parse test dep
                        Element cnbEl = findElement(depEl, TEST_DEPENDENCY_CNB);
                        boolean test = findElement(depEl, TEST_DEPENDENCY_TEST) != null;
                        String _cnb = null;
                        if (cnbEl != null) {
                            _cnb = XMLUtil.findText(cnbEl);
                        }
                        boolean recursive = findElement(depEl, TEST_DEPENDENCY_RECURSIVE) != null;
                        boolean compile = findElement(depEl, TEST_DEPENDENCY_COMPILE) != null;
                        if (_cnb != null) {
                            ModuleEntry me = ml.getEntry(_cnb);
                            if (me == null) {
                                me = new NonexistentModuleEntry(_cnb);
                            }
                                TestModuleDependency tmd = new TestModuleDependency(me, test, recursive, compile);
                                if (!directTestDeps.add(tmd)) {
                                    // testdependency already exists
                                    String path = project.getPathWithinNetBeansOrg();
                                    if (path == null) {
                                        path = project.getProjectDirectoryFile().getAbsolutePath();
                                    }
                                    String msg = "Invalid project.xml (" + path + "); testdependency " // NOI18N
                                            + tmd.getModule().getCodeNameBase() + " is duplicated!"; // NOI18N
                                    Util.err.log(ErrorManager.WARNING, msg);
                                }
                        }
                    }
                }
                testDeps.put(testType, directTestDeps);
            }
        }
        return testDeps;
    }

    /**
     * Replace existing classpath extensions with new values.
     * @param newValues &lt;key=runtime-path(String), value=binary-path(String)&gt;
     */
    public void replaceClassPathExtensions(final Map<String, String> newValues) {
        removeClassPathExtensions();
        if (newValues != null && newValues.size() > 0) {
            Element _confData = getConfData();
            Document doc = _confData.getOwnerDocument();
            for (Map.Entry<String,String> entry : newValues.entrySet()) {
                Element cpel = createModuleElement(doc, ProjectXMLManager.CLASS_PATH_EXTENSION);
                Element runtime = createModuleElement(doc, ProjectXMLManager.CLASS_PATH_RUNTIME_PATH, entry.getKey());
                cpel.appendChild(runtime);
                String binaryPath = entry.getValue();
                if (binaryPath != null) {
                    Element binary = createModuleElement(doc, ProjectXMLManager.CLASS_PATH_BINARY_ORIGIN, binaryPath);
                    cpel.appendChild(binary);
                }
                _confData.appendChild(cpel);
            }
            cpExtensions = new HashMap<String, String>(newValues);
            project.putPrimaryConfigurationData(_confData);
        }
    }

    /**
     * Replaces all original public packages with the given
     * <code>newPackages</code>. Also removes friend packages if there are any
     * since those two mutually exclusive. packages ending with .* will be translated to <subpackages> element
     */
    public void replacePublicPackages(Set<String> newPackages) {
        removePublicAndFriends();
        Element _confData = getConfData();
        Document doc = _confData.getOwnerDocument();
        Element publicPackagesEl = createModuleElement(doc, ProjectXMLManager.PUBLIC_PACKAGES);

        insertPublicOrFriend(publicPackagesEl);

        for (String pkg : newPackages) {
            if (pkg.endsWith(".*")) {
                publicPackagesEl.appendChild(
                    createModuleElement(doc, ProjectXMLManager.SUBPACKAGES, pkg.substring(0, pkg.length() - ".*".length())));
            } else {
                publicPackagesEl.appendChild(
                    createModuleElement(doc, ProjectXMLManager.PACKAGE, pkg));
            }
        }
        project.putPrimaryConfigurationData(_confData);
        publicPackages = null; // XXX cleaner would be to listen on changes in helper
    }

    /** Position public-packages or friend-packages according to XSD. */
    private void insertPublicOrFriend(Element packagesEl) {
        XMLUtil.appendChildElement(getConfData(), packagesEl, ORDER);
    }

    /**
     * Replaces all original friends with the given <code>friends</code> with
     * <code>packagesToExpose</code> as exposed packages to those friends. Also
     * removes public packages if there are any since those two are mutually
     * exclusive. packages ending with .* will be translated to <subpackages> element
     */
    public void replaceFriends(Set<String> friends, Set<String> packagesToExpose) {
        removePublicAndFriends();
        Element _confData = getConfData();
        Document doc = _confData.getOwnerDocument();
        Element friendPackages = createModuleElement(doc, ProjectXMLManager.FRIEND_PACKAGES);
        insertPublicOrFriend(friendPackages);

        for (String friend : friends) {
                friendPackages.appendChild(
                    createModuleElement(doc, ProjectXMLManager.FRIEND, friend));
        }
        for (String pkg : packagesToExpose) {
            if (pkg.endsWith(".*")) {
                friendPackages.appendChild(
                    createModuleElement(doc, ProjectXMLManager.SUBPACKAGES, pkg.substring(0, pkg.length() - ".*".length())));
            } else {
                friendPackages.appendChild(
                    createModuleElement(doc, ProjectXMLManager.PACKAGE, pkg));
            }
        }
        project.putPrimaryConfigurationData(_confData);
        publicPackages = null;
    }

    /**
     * Returns an array of {@link ManifestManager.PackageExport}s of all
     * exposed public packages. Method considers both <em>package</em> and
     * <em>subpackages</em> elements with the recursivity flag set
     * appropriately for returned entries.
     *
     * @return array of {@link ManifestManager.PackageExport}. May be empty but
     *         not <code>null</code>.
     */
    public ManifestManager.PackageExport[] getPublicPackages() {
        if (publicPackages == null) {
            publicPackages = ProjectXMLManager.findPublicPackages(getConfData());
        }
        return publicPackages;
    }

    /** Returns all friends or <code>null</code> if there are none. */
    public String[] getFriends() {
        if (friends == null) {
            friends = ProjectXMLManager.findFriends(getConfData());
        }
        return friends;
    }

    /**
     * Returns paths of all libraries bundled within a project this
     * <em>manager</em> manage. So the result should be an array of
     * <code>String</code>s each representing a relative path to the project's
     * external library (jar/zip).
     * @return an array of strings (may be empty)
     */
    public String[] getBinaryOrigins() {
        Set<String> origins = new LinkedHashSet<String>(getClassPathExtensions().values());
        origins.remove(null);
        return origins.toArray(new String[0]);
    }

    /**
     * Returns existing classpath extensions mapping.
     * Returned map is unmodifiable.
     * @return classpath extensions map &lt;key=runtime-path(String), value=binary-path(String or null)&gt;
     */
    public Map<String, String> getClassPathExtensions() {
        if (cpExtensions != null) {
            return Collections.unmodifiableMap(cpExtensions);
        }
        Map<String, String> cps = new HashMap<String, String>();
        for (Element cpExtEl : XMLUtil.findSubElements(getConfData())) {
            if (CLASS_PATH_EXTENSION.equals(cpExtEl.getTagName())) {
                Element binOrigEl = findElement(cpExtEl, BINARY_ORIGIN);
                Element runtimePathEl = findElement(cpExtEl, CLASS_PATH_RUNTIME_PATH);
                cps.put(XMLUtil.findText(runtimePathEl), binOrigEl != null ? XMLUtil.findText(binOrigEl) : null);
            }
        }
        return Collections.unmodifiableMap(cpExtensions = cps);
    }

    /** Returns code-name-base. */
    public String getCodeNameBase() {
        if (cnb == null) {
            Element cnbEl = findElement(getConfData(), ProjectXMLManager.CODE_NAME_BASE);
            cnb = XMLUtil.findText(cnbEl);
        }
        return cnb;
    }

    /** Package-private for unit tests only. */
    static void createModuleDependencyElement(
            Element moduleDependencies, ModuleDependency md, Element nextSibling) {

        Document doc = moduleDependencies.getOwnerDocument();
        Element modDepEl = createModuleElement(doc, ProjectXMLManager.DEPENDENCY);
        moduleDependencies.insertBefore(modDepEl, nextSibling);

        modDepEl.appendChild(createModuleElement(doc, ProjectXMLManager.CODE_NAME_BASE,
                md.getModuleEntry().getCodeNameBase()));
        if (md.buildPrerequisite) {
            modDepEl.appendChild(createModuleElement(doc, ProjectXMLManager.BUILD_PREREQUISITE));
        }
        if (md.hasCompileDependency()) {
            modDepEl.appendChild(createModuleElement(doc, ProjectXMLManager.COMPILE_DEPENDENCY));
        }
        if (!md.runDependency) {
            return;
        }

        Element runDepEl = createModuleElement(doc, ProjectXMLManager.RUN_DEPENDENCY);
        modDepEl.appendChild(runDepEl);

        String rv = md.getReleaseVersion();
        if (rv != null && !rv.trim().equals("")) {
            runDepEl.appendChild(createModuleElement(
                    doc, ProjectXMLManager.RELEASE_VERSION, rv));
        }
        if (md.hasImplementationDependency()) {
            runDepEl.appendChild(createModuleElement(
                    doc, ProjectXMLManager.IMPLEMENTATION_VERSION));
        } else {
            String sv = md.getSpecificationVersion();
            if (sv != null && !"".equals(sv)) { // NOI18N
                runDepEl.appendChild(createModuleElement(
                        doc, ProjectXMLManager.SPECIFICATION_VERSION, sv));
            }
        }
    }

    /** Removes public-packages and friend-packages elements. */
    private void removePublicAndFriends() {
        Element friendPackages = findFriendsElement(getConfData());
        if (friendPackages != null) {
            getConfData().removeChild(friendPackages);
        }
        Element _publicPackages = findPublicPackagesElement(getConfData());
        if (_publicPackages != null) {
            getConfData().removeChild(_publicPackages);
        }
    }

    private static Element findElement(Element parentEl, String elementName) {
        return XMLUtil.findElement(parentEl, elementName, NbModuleProject.NAMESPACE_SHARED);
    }

    /** Package-private for unit tests only. */
    static Element findModuleDependencies(Element parentEl) {
        return findElement(parentEl, ProjectXMLManager.MODULE_DEPENDENCIES);
    }

    private static Element findTestDependenciesElement(Element parentEl) {
        return findElement(parentEl, ProjectXMLManager.TEST_DEPENDENCIES);
    }

    private static Element findPublicPackagesElement(Element parentEl) {
        return findElement(parentEl, ProjectXMLManager.PUBLIC_PACKAGES);
    }

    private static Element findFriendsElement(Element parentEl) {
        return findElement(parentEl, ProjectXMLManager.FRIEND_PACKAGES);
    }

    private static Element createModuleElement(Document doc, String name) {
        return doc.createElementNS(NbModuleProject.NAMESPACE_SHARED, name);
    }

    private static Element createModuleElement(Document doc, String name, String innerText) {
        Element el = createModuleElement(doc, name);
        el.appendChild(doc.createTextNode(innerText));
        return el;
    }

    private static Element createSuiteElement(Document doc, String name) {
        return doc.createElementNS(SuiteProjectType.NAMESPACE_SHARED, name);
    }

    private static Element createSuiteElement(Document doc, String name, String innerText) {
        Element el = createSuiteElement(doc, name);
        el.appendChild(doc.createTextNode(innerText));
        return el;
    }

    /**
     * Find packages in public-packages or friend-packages section. Method
     * considers both <em>package</em> and <em>subpackages</em> elements with
     * the recursivity flag set appropriately for returned entries.
     */
    private static Set<ManifestManager.PackageExport> findAllPackages(Element parent) {
        Set<ManifestManager.PackageExport> packages = new HashSet<ManifestManager.PackageExport>();
        try {
            for (Element pkgEl : XMLUtil.findSubElements(parent)) {
                if (PACKAGE.equals(pkgEl.getTagName())) {
                    packages.add(new ManifestManager.PackageExport(XMLUtil.findText(pkgEl), false));
                } else if (SUBPACKAGES.equals(pkgEl.getTagName())) {
                    packages.add(new ManifestManager.PackageExport(XMLUtil.findText(pkgEl), true));
                }
            }
        } catch(IllegalArgumentException e) {
            LOG.log(Level.WARNING, "Error getting subelements, malformed xml");
            packages = new HashSet<ManifestManager.PackageExport>();
        }
        return packages;
    }

    /**
     * Utility method for finding public packages. Method considers both
     * <em>package</em> and <em>subpackages</em> elements with the recursivity
     * flag set appropriately for returned entries.
     *
     * @return array of {@link ManifestManager.PackageExport}. May be empty but
     *         not <code>null</code>.
     */
    public static ManifestManager.PackageExport[] findPublicPackages(final Element confData) {
        Element ppEl = findPublicPackagesElement(confData);
        Set<ManifestManager.PackageExport> pps = new HashSet<ManifestManager.PackageExport>();
        if (ppEl != null) {
            pps.addAll(findAllPackages(ppEl));
        }
        ppEl = findFriendsElement(confData);
        if (ppEl != null) {
            pps.addAll(findAllPackages(ppEl));
        }
        return pps.isEmpty() ? ManifestManager.EMPTY_EXPORTED_PACKAGES : pps.toArray(new ManifestManager.PackageExport[0]);
    }

    /** Utility method for finding friend. */
    public static String[] findFriends(final Element confData) {
        Element friendsEl = findFriendsElement(confData);
        if (friendsEl != null) {
            Set<String> friends = new TreeSet<String>();
            for (Element friendEl : XMLUtil.findSubElements(friendsEl)) {
                if (FRIEND.equals(friendEl.getTagName())) {
                    friends.add(XMLUtil.findText(friendEl));
                }
            }
            return friends.toArray(new String[0]);
        }
        return null;
    }

    /**
     * Generates a basic <em>project.xml</em> templates into the given
     * <code>projectXml</code> for <em>standalone</em> or <em>module in
     * suite</em> module.
     */
    static void generateEmptyModuleTemplate(FileObject projectXml, String cnb,
            NbModuleType moduleType, String... compileDepsCnbs) throws IOException {

        Document prjDoc = XMLUtil.createDocument("project", PROJECT_NS, null, null); // NOI18N

        // generate general project elements
        Element typeEl = prjDoc.createElementNS(PROJECT_NS, "type"); // NOI18N
        typeEl.appendChild(prjDoc.createTextNode(NbModuleProject.TYPE));
        prjDoc.getDocumentElement().appendChild(typeEl);
        Element confEl = prjDoc.createElementNS(PROJECT_NS, "configuration"); // NOI18N
        prjDoc.getDocumentElement().appendChild(confEl);

        // generate NB Module project type specific elements
        Element dataEl = createModuleElement(confEl.getOwnerDocument(), DATA);
        confEl.appendChild(dataEl);
        Document dataDoc = dataEl.getOwnerDocument();
        dataEl.appendChild(createModuleElement(dataDoc, CODE_NAME_BASE, cnb));
        Element moduleTypeEl = createTypeElement(dataDoc, moduleType);
        if (moduleTypeEl != null) {
            dataEl.appendChild(moduleTypeEl);
        }
        final Element deps = createModuleElement(dataDoc, MODULE_DEPENDENCIES);
        for (String depCnb : compileDepsCnbs) {
            Element modDepEl = createModuleElement(dataDoc, ProjectXMLManager.DEPENDENCY);
            modDepEl.appendChild(createModuleElement(dataDoc, ProjectXMLManager.CODE_NAME_BASE, depCnb));
            modDepEl.appendChild(createModuleElement(dataDoc, ProjectXMLManager.BUILD_PREREQUISITE));
            modDepEl.appendChild(createModuleElement(dataDoc, ProjectXMLManager.COMPILE_DEPENDENCY));
            deps.appendChild(modDepEl);
        }
        dataEl.appendChild(deps);
        dataEl.appendChild(createModuleElement(dataDoc, PUBLIC_PACKAGES));

        // store document to disk
        ProjectXMLManager.safelyWrite(projectXml, prjDoc);
    }

    /**
     * Create a library wrapper project.xml.
     *
     * @param publicPackages set of <code>String</code>s representing the packages
     * @param extensions &lt;key=runtime path(String), value=binary path (String)&gt;
     */
    static void generateLibraryModuleTemplate(FileObject projectXml, String cnb,
            NbModuleType moduleType, Set<String> publicPackages, Map<String,String> extensions) throws IOException {

        Document prjDoc = XMLUtil.createDocument("project", PROJECT_NS, null, null); // NOI18N

        // generate general project elements
        Element typeEl = prjDoc.createElementNS(PROJECT_NS, "type"); // NOI18N
        typeEl.appendChild(prjDoc.createTextNode(NbModuleProject.TYPE));
        prjDoc.getDocumentElement().appendChild(typeEl);
        Element confEl = prjDoc.createElementNS(PROJECT_NS, "configuration"); // NOI18N
        prjDoc.getDocumentElement().appendChild(confEl);

        // generate NB Module project type specific elements
        Element dataEl = createModuleElement(confEl.getOwnerDocument(), DATA);
        confEl.appendChild(dataEl);
        Document dataDoc = dataEl.getOwnerDocument();
        dataEl.appendChild(createModuleElement(dataDoc, CODE_NAME_BASE, cnb));
        Element moduleTypeEl = createTypeElement(dataDoc, moduleType);
        if (moduleTypeEl != null) {
            dataEl.appendChild(moduleTypeEl);
        }
        dataEl.appendChild(createModuleElement(dataDoc, MODULE_DEPENDENCIES));
        Element packages = createModuleElement(dataDoc, PUBLIC_PACKAGES);
        dataEl.appendChild(packages);
        for (String pkg : publicPackages) {
            packages.appendChild(createModuleElement(dataDoc, PACKAGE, pkg));
        }
        for (Map.Entry<String,String> entry : extensions.entrySet()) {
            Element cp = createModuleElement(dataDoc, CLASS_PATH_EXTENSION);
            dataEl.appendChild(cp);
            cp.appendChild(createModuleElement(dataDoc, CLASS_PATH_RUNTIME_PATH, entry.getKey()));
            cp.appendChild(createModuleElement(dataDoc, CLASS_PATH_BINARY_ORIGIN, entry.getValue()));
        }

        // store document to disk
        ProjectXMLManager.safelyWrite(projectXml, prjDoc);
    }

    private static Element createTypeElement(Document dataDoc, NbModuleType type) {
        Element result = null;
        if (type == NbModuleType.STANDALONE) {
            result = createModuleElement(dataDoc, STANDALONE);
        } else if (type == NbModuleType.SUITE_COMPONENT) {
            result = createModuleElement(dataDoc, SUITE_COMPONENT);
        }
        return result;
    }

    /**
     * Generates a basic <em>project.xml</em> templates into the given
     * <code>projectXml</code> for <em>Suite</em>.
     */
    public static void generateEmptySuiteTemplate(FileObject projectXml, String name) throws IOException {
        // XXX this method could be moved in a future (depends on how complex
        // suite's project.xml will be) to the .suite package dedicated class
        Document prjDoc = XMLUtil.createDocument("project", PROJECT_NS, null, null); // NOI18N

        // generate general project elements
        Element typeEl = prjDoc.createElementNS(PROJECT_NS, "type"); // NOI18N
        typeEl.appendChild(prjDoc.createTextNode(SuiteProjectType.TYPE));
        prjDoc.getDocumentElement().appendChild(typeEl);
        Element confEl = prjDoc.createElementNS(PROJECT_NS, "configuration"); // NOI18N
        prjDoc.getDocumentElement().appendChild(confEl);

        // generate NB Suite project type specific elements
        Element dataEl = createSuiteElement(confEl.getOwnerDocument(), DATA);
        confEl.appendChild(dataEl);
        Document dataDoc = dataEl.getOwnerDocument();
        dataEl.appendChild(createSuiteElement(dataDoc, "name", name)); // NOI18N

        // store document to disk
        ProjectXMLManager.safelyWrite(projectXml, prjDoc);
    }

    private static void safelyWrite(FileObject projectXml, Document prjDoc) throws IOException {
        OutputStream os = projectXml.getOutputStream();
        try {
            XMLUtil.write(prjDoc, os, "UTF-8"); // NOI18N
        } finally {
            os.close();
        }
    }

    private Element getConfData() {
        if (confData == null) {
            confData = project.getPrimaryConfigurationData();
        }
        return confData;
    }
}
