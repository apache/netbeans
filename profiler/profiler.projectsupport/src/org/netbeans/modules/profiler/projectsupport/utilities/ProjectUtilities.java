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
package org.netbeans.modules.profiler.projectsupport.utilities;

import java.io.InputStream;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Bachorik
 */
public class ProjectUtilities {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------
    private static final Logger LOGGER = Logger.getLogger(ProjectUtilities.class.getName());
    //~ Methods ------------------------------------------------------------------------------------------------------------------
    /**
     * @return The current main project or null if no project is main.
     */
    public static Project getMainProject() {
        return OpenProjects.getDefault().getMainProject();
    }

    public static Project[] getOpenedProjects() {
        Set<Project> projects = new HashSet<>();
        for (Project project : OpenProjects.getDefault().getOpenProjects()) // #256930
            if (!project.getClass().getName().equals("org.netbeans.modules.project.ui.LazyProject")) // NOI18N
                projects.add(project);
        return projects.toArray(new Project[0]);
//        return OpenProjects.getDefault().getOpenProjects();
    }

    public static boolean hasAction(Project project, String actionName) {
        ActionProvider ap = project.getLookup().lookup(ActionProvider.class);

        if (ap == null) {
            return false; // return false if no ActionProvider available
        }

        String[] actions = ap.getSupportedActions();

        for (int i = 0; i < actions.length; i++) {
            if ((actions[i] != null) && actionName.equals(actions[i])) {
                return true;
            }
        }

        return false;
    }

    @NbBundle.Messages({
        "ProjectUtilities_FailedCreateOutputFolderMsg=Failed to create build output folder: {0}"
    })
    public static FileObject getOrCreateBuildFolder(Project project, String buildDirProp) {
        FileObject buildDir = FileUtil.toFileObject(PropertyUtils.resolveFile(FileUtil.toFile(project.getProjectDirectory()),
                buildDirProp));

        if (buildDir == null) {
            try {
                // TODO: if buildDirProp is absolute, relativize via PropertyUtils
                buildDir = FileUtil.createFolder(project.getProjectDirectory(), buildDirProp);
            } catch (IOException e) {
                Bundle.ProjectUtilities_FailedCreateOutputFolderMsg(e.getMessage());

                ErrorManager.getDefault().notify(ErrorManager.ERROR, e);

                return null;
            }
        }

        return buildDir;
    }

    public static Properties getProjectProperties(final Project project) {
        final Properties props = new Properties();
        final FileObject propFile = project.getProjectDirectory().getFileObject("nbproject/project.properties"); // NOI18N
        if (propFile != null) {
            ProjectManager.mutex().readAccess(new Runnable() {

                public void run() {
                    InputStream in = null;
                    try {
                        in = propFile.getInputStream();
                        props.load(in);
                    } catch (IOException ex) {
                        LOGGER.finest("Could not load properties file: " + propFile.getPath()); // NOI18N
                    } finally {
                        if (in != null) {
                            try {
                                 in.close();
                            } catch (IOException ex) {
                                // ignore
                            }
                        }
                    }
                }
            });
        }
        return props;
    }

    public static String getProjectBuildScript(final Project project) {
        final FileObject buildFile = project.getProjectDirectory().getFileObject("build.xml"); //NOI18N
        RandomAccessFile file = null;
        byte[] data = null;

        try {
            file = new RandomAccessFile(FileUtil.toFile(buildFile), "r");
            data = new byte[(int) buildFile.getSize()];
            file.readFully(data);
        } catch (FileNotFoundException e2) {
            ProfilerLogger.log(e2);

            return null;
        } catch (IOException e2) {
            ProfilerLogger.log(e2);

            return null;
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e2) {
                    ProfilerLogger.log(e2);
                }
            }
        }

        try {
            return new String(data, "UTF-8" //NOI18N
                    ); // According to Issue 65557, build.xml uses UTF-8, not default encoding!
        } catch (UnsupportedEncodingException ex) {
            ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);

            return null;
        }
    }

//    public static java.util.List<SimpleFilter> getProjectDefaultInstrFilters(Project project) {
//        java.util.List<SimpleFilter> v = new ArrayList<SimpleFilter>();
//
//        if (ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA).length > 0) {
//            v.add(FILTER_PROJECT_ONLY);
//        }
//
//        if (hasSubprojects(project)) {
//            v.add(FILTER_PROJECT_SUBPROJECTS_ONLY);
//        }
//
//        return v;
//    }

    public static ClientUtils.SourceCodeSelection[] getProjectDefaultRoots(Project project, String[][] projectPackagesDescr) {
        computeProjectPackages(project, true, projectPackagesDescr);

        ClientUtils.SourceCodeSelection[] ret = new ClientUtils.SourceCodeSelection[projectPackagesDescr[1].length];

        for (int i = 0; i < projectPackagesDescr[1].length; i++) {
            if ("".equals(projectPackagesDescr[1][i])) { //NOI18N
                ret[i] = new ClientUtils.SourceCodeSelection("", "", ""); //NOI18N
            } else {
                ret[i] = new ClientUtils.SourceCodeSelection(projectPackagesDescr[1][i] + ".**", "", ""); //NOI18N
            }
        }

        return ret;
    }

    public static Project getProjectForBuildScript(String fileName) {
        FileObject projectFO = FileUtil.toFileObject(new File(fileName));

        while (projectFO != null) {
            try {
                if (projectFO.isFolder()) {
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("Trying: " + projectFO); //NOI18N
                    }

                    Project p = ProjectManager.getDefault().findProject(projectFO);

                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("Got: " + ((p != null) ? getProjectName(p) : null)); //NOI18N
                    }

                    if (p != null) {
                        return p;
                    }
                }

                projectFO = projectFO.getParent();
            } catch (IOException e) {
                ProfilerLogger.severe("Got: IOException : " + e.getMessage()); //NOI18N
            }
        }

        return null;
    }

    public static Icon getProjectIcon(Lookup.Provider project) {
        ProjectInformation info = project.getLookup().lookup(ProjectInformation.class);

        if (info == null) {
            return new ImageIcon();
        } else {
            return info.getIcon();
        }
    }

    public static String getProjectName(Lookup.Provider project) {
        ProjectInformation info = project.getLookup().lookup(ProjectInformation.class);

        return (info != null) ? info.getDisplayName() : "UNKNOWN";
    }

    /**
     * Provides a list of source roots for the given project.
     *
     * @param project The project
     * @return an array of FileObjects that are the source roots for this project
     */
    public static FileObject[] getSourceRoots(final Lookup.Provider project) {
        return getSourceRoots(project, true);
    }

    /**
     * Provides a list of source roots for the given project.
     *
     * @param project The project
     * @param traverse Include subprojects
     * @return an array of FileObjects that are the source roots for this project
     */
    public static FileObject[] getSourceRoots(final Lookup.Provider project, final boolean traverse) {
        Set<FileObject> set = new HashSet<FileObject>();
        Set<Lookup.Provider> projects = new HashSet<Lookup.Provider>();

        projects.add(project);
        getSourceRoots(project, traverse, projects, set);

        return set.toArray(new FileObject[0]);
    }

    public static void fetchSubprojects(final Project project, final Set<Project> projects) {
        // process possible subprojects
        //mkleint: see subprojectprovider for official contract, see #210465
        SubprojectProvider spp = project.getLookup().lookup(SubprojectProvider.class);

        if (spp != null) {
            for (Project p : spp.getSubprojects()) {
                if (projects.add(p)) {
                    fetchSubprojects(p, projects);
                }
            }
        }
    }

//    public static SimpleFilter computeProjectOnlyInstrumentationFilter(Project project, SimpleFilter predefinedInstrFilter,
//            String[][] projectPackagesDescr) {
//        // TODO: projectPackagesDescr[1] should only contain packages from subprojects, currently contains also toplevel project packages
//        if (FILTER_PROJECT_ONLY.equals(predefinedInstrFilter)) {
//            return new SimpleFilter(PROFILE_PROJECT_CLASSES_STRING, SimpleFilter.SIMPLE_FILTER_INCLUSIVE, computeProjectOnlyInstrumentationFilter(project, false, projectPackagesDescr));
//        } else if (FILTER_PROJECT_SUBPROJECTS_ONLY.equals(predefinedInstrFilter)) {
//            return new SimpleFilter(PROFILE_PROJECT_SUBPROJECT_CLASSES_STRING, SimpleFilter.SIMPLE_FILTER_INCLUSIVE, computeProjectOnlyInstrumentationFilter(project, true, projectPackagesDescr));
//        }
//
//        return null;
//    }

    public static String computeProjectOnlyInstrumentationFilter(Project project, boolean useSubprojects,
            String[][] projectPackagesDescr) {
        if (!useSubprojects) {
            computeProjectPackages(project, false, projectPackagesDescr);

            StringBuilder projectPackages = new StringBuilder();

            for (int i = 0; i < projectPackagesDescr[0].length; i++) {
                projectPackages.append("".equals(projectPackagesDescr[0][i]) ? getDefaultPackageClassNames(project)
                        : (projectPackagesDescr[0][i] + ". ")); //NOI18N
            }

            return projectPackages.toString().trim();
        } else {
            computeProjectPackages(project, true, projectPackagesDescr);

            StringBuilder projectPackages = new StringBuilder();

            for (int i = 0; i < projectPackagesDescr[1].length; i++) {
                projectPackages.append("".equals(projectPackagesDescr[1][i]) ? getDefaultPackageClassNames(project)
                        : (projectPackagesDescr[1][i] + ". ")); //NOI18N // TODO: default packages need to be processed also for subprojects!!!
            }

            return projectPackages.toString().trim();
        }
    }

//    public static boolean isIncludeSubprojects(SimpleFilter filter) {
//        return FILTER_PROJECT_SUBPROJECTS_ONLY.equals(filter);
//    }

    public static String getDefaultPackageClassNames(Project project) {
        Collection<String> classNames = getDefaultPackageClassNamesSet(project);
        StringBuilder classNamesBuf = new StringBuilder();

        for (String className : classNames) {
            classNamesBuf.append(className).append(" "); //NOI18N
        }

        return classNamesBuf.toString();
    }
    
    /**
     * Returns the JavaSource repository of a given project or global JavaSource if no project is provided
     */
    public static JavaSource getSources(Project project) {
        if (project == null) {
            return getSources((FileObject[]) null);
        } else {
            return getSources(ProjectUtilities.getSourceRoots(project, true));
        }

    }

    public static void computeProjectPackages(final Project project, boolean subprojects, String[][] storage) {
        if ((storage == null) || (storage.length != 2)) {
            throw new IllegalArgumentException("Storage must be a non-null String[2][] array"); // NOI18N
        }

        if (storage[0] == null || storage[0].length == 0) {
            Collection<String> packages1 = new ArrayList<String>();

            for (FileObject root : getSourceRoots(project, false)) {
                addSubpackages(packages1, "", root); //NOI18N
            }

            storage[0] = packages1.toArray(new String[0]);
        }

        if (subprojects && (storage[1] == null || storage[1].length == 0)) {
            FileObject[] srcRoots2 = getSourceRoots(project, true); // TODO: should be computed based on already known srcRoots1
            ArrayList<String> packages2 = new ArrayList<String>();

            for (FileObject root : srcRoots2) {
                addSubpackages(packages2, "", root); //NOI18N
            }

            storage[1] = packages2.toArray(new String[0]);
        }
    }

    /**
     * Will find
     * Copied from JUnit module implementation in 4.1 and modified
     */
    public static FileObject findTestForFile(final FileObject selectedFO) {
        if ((selectedFO == null) || !selectedFO.getExt().equalsIgnoreCase("java")) {
            return null; // NOI18N
        }

        ClassPath cp = ClassPath.getClassPath(selectedFO, ClassPath.SOURCE);

        if (cp == null) {
            return null;
        }

        FileObject packageRoot = cp.findOwnerRoot(selectedFO);

        if (packageRoot == null) {
            return null; // not a file in the source dirs - e.g. generated class in web app
        }

        URL[] testRoots = UnitTestForSourceQuery.findUnitTests(packageRoot);
        FileObject fileToOpen = null;

        for (int j = 0; j < testRoots.length; j++) {
            fileToOpen = findUnitTestInTestRoot(cp, selectedFO, testRoots[j]);

            if (fileToOpen != null) {
                return fileToOpen;
            }
        }

        return null;
    }

    public static void invokeAction(Project project, String s) {
        ActionProvider ap = project.getLookup().lookup(ActionProvider.class);

        if (ap == null) {
            return; // fail early
        }

        ap.invokeAction(s, Lookup.getDefault());
    }

        // Returns true if the project contains any Java sources (does not check subprojects!)
    public static boolean isJavaProject(Project project) {
        if (project == null) return false;
        
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

        return sourceGroups.length > 0;
    }
    
    private static void getSourceRoots(final Lookup.Provider project, final boolean traverse, Set<Lookup.Provider> projects, Set<FileObject> roots) {
        if (project instanceof Project) {
            final Sources sources = ProjectUtils.getSources((Project)project);

            for (SourceGroup sg : sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                roots.add(sg.getRootFolder());
            }

            if (traverse) {
                // process possible subprojects
                //mkleint: see subprojectprovider for official contract, maybe classpath should be checked instead? see #210465                
                SubprojectProvider spp = project.getLookup().lookup(SubprojectProvider.class);

                if (spp != null) {
                    for (Project p : spp.getSubprojects()) {
                        if (projects.add(p)) {
                            getSourceRoots(p, traverse, projects, roots);
                        }
                    }
                }
            }
        }
    }

    private static void addSubpackages(Collection<String> packages, String prefix, FileObject packageFO) {
        if (!packageFO.isFolder()) { // not a folder

            return;
        }

        FileObject[] children = packageFO.getChildren();

        // 1. check if there are java sources in this folder and if so, add to the list of packages
        if (!packages.contains(prefix)) { // already in there, skip this

            for (int i = 0; i < children.length; i++) {
                FileObject child = children[i];

                if (child.getExt().equals("java")) { //NOI18N
                    packages.add(prefix);

                    break;
                }
            }
        }

        // 2. recurse into subfolders
        for (int i = 0; i < children.length; i++) {
            FileObject child = children[i];

            if (child.isFolder()) {
                if ("".equals(prefix)) { //NOI18N
                    addSubpackages(packages, child.getName(), child);
                } else {
                    addSubpackages(packages, prefix + "." + child.getName(), child); //NOI18N
                }
            }
        }
    }

    public static boolean hasSubprojects(Project project) {
        if (project == null) return false;
        
        SubprojectProvider spp = project.getLookup().lookup(SubprojectProvider.class);

        if (spp == null) {
            return false;
        }

        return spp.getSubprojects().size() > 0;
    }

    /**
     * Copied from JUnit module implementation in 4.1 and modified
     */
    private static FileObject findUnitTestInTestRoot(ClassPath cp, FileObject selectedFO, URL testRoot) {
        ClassPath testClassPath = null;

        if (testRoot == null) { //no tests, use sources instead
            testClassPath = cp;
        } else {
            try {
                List<PathResourceImplementation> cpItems = new ArrayList<PathResourceImplementation>();
                cpItems.add(ClassPathSupport.createResource(testRoot));
                testClassPath = ClassPathSupport.createClassPath(cpItems);
            } catch (IllegalArgumentException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                testClassPath = cp;
            }
        }

        String testName = getTestName(cp, selectedFO);

        return testClassPath.findResource(testName + ".java"); // NOI18N
    }

    /**
     * Copied from JUnit module implementation in 4.1 and modified
     */
    private static String getTestName(ClassPath cp, FileObject selectedFO) {
        String resource = cp.getResourceName(selectedFO, '/', false); //NOI18N
        String testName = null;

        if (selectedFO.isFolder()) {
            //find Suite for package
            testName = convertPackage2SuiteName(resource);
        } else {
            // find Test for class
            testName = convertClass2TestName(resource);
        }

        return testName;
    }

    /**
     * Copied from JUnit module implementation in 4.1 and modified
     * Hardcoded test name prefix/suffix.
     */
    private static String convertClass2TestName(String classFileName) {
        if ((classFileName == null) || "".equals(classFileName)) {
            return ""; //NOI18N
        }

        int index = classFileName.lastIndexOf('/'); //NOI18N
        String pkg = (index > -1) ? classFileName.substring(0, index) : ""; // NOI18N
        String clazz = (index > -1) ? classFileName.substring(index + 1) : classFileName;
        clazz = clazz.substring(0, 1).toUpperCase() + clazz.substring(1);

        if (pkg.length() > 0) {
            pkg += "/"; // NOI18N
        }

        return pkg + clazz + "Test"; // NOI18N
    }

    /**
     * Copied from JUnit module implementation in 4.1 and modified
     * Hardcoded test name prefix/suffix.
     */
    private static String convertPackage2SuiteName(String packageFileName) {
        if ((packageFileName == null) || "".equals(packageFileName)) {
            return ""; //NOI18N
        }

        int index = packageFileName.lastIndexOf('/'); //NOI18N
        String pkg = (index > -1) ? packageFileName.substring(index + 1) : packageFileName;
        pkg = pkg.substring(0, 1).toUpperCase() + pkg.substring(1);

        return packageFileName + "/" + pkg + "Test"; // NOI18N
    }
    
    private static Collection<String> getDefaultPackageClassNamesSet(Project project) {
        final Collection<String> classNames = new ArrayList<String>();

        JavaSource js = getSources(project);
        final Set<ElementHandle<TypeElement>> types = getProjectTypes(project, js);

        for (ElementHandle<TypeElement> typeHandle : types) {
            int firstPkgSeparIndex = typeHandle.getQualifiedName().indexOf('.');

            if (firstPkgSeparIndex <= 0) {
                classNames.add(typeHandle.getQualifiedName().substring(firstPkgSeparIndex + 1));
            }
        }

        return classNames;
    }
    
    /**
     * Returns the JavaSource repository for given source roots
     */
    private static JavaSource getSources(FileObject[] roots) {
        //    findMainClasses(roots);
        // prepare the classpath based on the source roots
        ClassPath srcPath;
        ClassPath bootPath;

        ClassPath compilePath;

        if (roots == null || roots.length == 0) {
            srcPath = ClassPathSupport.createProxyClassPath(GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE).toArray(new ClassPath[0]));
            bootPath =
                    JavaPlatform.getDefault().getBootstrapLibraries();
            compilePath =
                    ClassPathSupport.createProxyClassPath(GlobalPathRegistry.getDefault().getPaths(ClassPath.COMPILE).toArray(new ClassPath[0]));
        } else {
            srcPath = ClassPathSupport.createClassPath(roots);
            bootPath =
                    ClassPath.getClassPath(roots[0], ClassPath.BOOT);
            compilePath =
                    ClassPath.getClassPath(roots[0], ClassPath.COMPILE);
        }

// create ClassPathInfo for JavaSources only -> (bootPath, classPath, sourcePath)
        final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, srcPath);

        // create the javasource repository for all the source files
        return JavaSource.create(cpInfo, Collections.<FileObject>emptyList());
    }
    
        /**
     * Returns all types (classes) defined on the given source roots
     */
    private static Set<ElementHandle<TypeElement>> getProjectTypes(FileObject[] roots, JavaSource js) {
        final Set<ClassIndex.SearchScope> scope = new HashSet<ClassIndex.SearchScope>();
        scope.add(ClassIndex.SearchScope.SOURCE);

        if (js != null) {
            return js.getClasspathInfo().getClassIndex().getDeclaredTypes("", ClassIndex.NameKind.CASE_INSENSITIVE_PREFIX, scope); // NOI18N
        }

        return null;
    }

    /**
     * Returns all types (classes) defined within a project
     */
    private static Set<ElementHandle<TypeElement>> getProjectTypes(Project project, JavaSource js) {
        return getProjectTypes(ProjectUtilities.getSourceRoots(project, true), js);
    }
}
