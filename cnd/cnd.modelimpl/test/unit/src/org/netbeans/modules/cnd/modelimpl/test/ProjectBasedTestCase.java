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

package org.netbeans.modules.cnd.modelimpl.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.debug.CndTraceFlags;
import org.netbeans.modules.cnd.modelimpl.csm.core.LibraryManager;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.platform.CndIndexer;
import org.netbeans.modules.cnd.modelimpl.trace.TestModelHelper;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelBase;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelFileFilter;

/**
 * IMPORTANT NOTE:
 * If This class is not compiled with the notification about not resolved
 * BaseTestCase class => cnd/core tests are not compiled
 *
 * To solve this problem compile or run tests for cnd/core
 */

/**
 * test case for working with projects
 * test has possibility to copy project data into working test dir to prevent changes
 * in source folders when test makes any changes in content of files
 * 
 */
public abstract class ProjectBasedTestCase extends ModelBasedTestCase {

    private final Map<String, TestModelHelper> projectHelpers = new HashMap<>();
    private final Map<String, List<String>>    sysIncludes = new HashMap<>();
    private final Map<String, List<String>>    usrIncludes = new HashMap<>();
    private final Map<String, List<String>> projectDependencies = new HashMap<>();

    protected PrintWriter outputWriter  = null;
    
    protected PrintWriter logWriter = null;
    
    private final boolean performInWorkDir;
    private File workDirBasedProject = null;
    
    /**
     * Creates a new instance of CompletionBaseTestCase
     */
    public ProjectBasedTestCase(String testName) {
        this(testName, false);
    }
    
    /**
     * if test performs any modifications in data files or create new files
     * => pass performInWorkDir as 'true' to create local copy of project in work dir
     */
    public ProjectBasedTestCase(String testName, boolean performInWorkDir) {
        super(testName);
        this.performInWorkDir = performInWorkDir;
    }

    protected TraceModelFileFilter getTraceModelFileFilter() {
        return null;
    }

    protected final List<String> getSysIncludes(String prjPath) {
        return getProjectPaths(this.sysIncludes, prjPath);
    }

    protected void setSysIncludes(String prjPath, List<String> sysIncludes) {
        this.sysIncludes.put(prjPath, sysIncludes);
    }

    protected final List<String> getUsrIncludes(String prjPath) {
        return getProjectPaths(this.usrIncludes, prjPath);
    }

    protected void setLibProjectsPaths(String prjPath, List<String> dependentProjects) {
        this.projectDependencies.put(prjPath, dependentProjects);
    }

    protected List<String> getLibProjectsPaths(String prjPath) {
        return getProjectPaths(this.projectDependencies, prjPath);
    }

    protected void setUsrIncludes(String prjPath, List<String> usrIncludes) {
        this.usrIncludes.put(prjPath, usrIncludes);
    }
    
//    protected final void initDocumentSettings() {
//        String methodName = ProjectBasedTestCase.class.getName() + ".getIdentifierAcceptor";
//        Preferences prefs;
//        prefs = MimeLookup.getLookup(MIMENames.CPLUSPLUS_MIME_TYPE).lookup(Preferences.class);
//        prefs.put(EditorPreferencesKeys.IDENTIFIER_ACCEPTOR, methodName);
//        prefs = MimeLookup.getLookup(MIMENames.HEADER_MIME_TYPE).lookup(Preferences.class);
//        prefs.put(EditorPreferencesKeys.IDENTIFIER_ACCEPTOR, methodName);
//        prefs = MimeLookup.getLookup(MIMENames.C_MIME_TYPE).lookup(Preferences.class);
//        prefs.put(EditorPreferencesKeys.IDENTIFIER_ACCEPTOR, methodName);
//    }

//    public static Acceptor getIdentifierAcceptor() {
//        return AcceptorFactory.JAVA_IDENTIFIER;
//    }

    protected boolean needRepository() {
        return false;
    }
    
    @Override
    protected void setUp() throws Exception {
        CndUtils.clearLastAssertion();
        super.setUp();
        System.setProperty("cnd.modelimpl.persistent", needRepository() ? "true" : "false");
        //initDocumentSettings();
        super.clearWorkDir();
        
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        
        log("setUp preparing project.");
        File projectDir;
        if (performInWorkDir) {
            workDirBasedProject = new File(getWorkDir(), "project"); // NOI18N
            // copy data dir
            copyDirToWorkDir(getTestCaseDataDir(), workDirBasedProject, getTraceModelFileFilter());
            projectDir = workDirBasedProject; 
        } else {
            projectDir = getTestCaseDataDir();
        }
        File[] changedDirs = changeDefProjectDirBeforeParsingProjectIfNeeded(projectDir);
        FileObject fobjs[] = new FileObject[changedDirs.length];
        for (int i = 0; i < changedDirs.length; i++) {
            File file = changedDirs[i];
            TestModelHelper projectHelper = new TestModelHelper(i==0, getTraceModelFileFilter());
            String prjPath = file.getAbsolutePath();
            projectHelper.initParsedProject(prjPath, getSysIncludes(prjPath), getUsrIncludes(prjPath), getLibProjectsPaths(prjPath));
            projectHelpers.put(prjPath, projectHelper);
            fobjs[i] = FileUtil.toFileObject(file);
        }
        
        if (CndTraceFlags.USE_INDEXING_API) {
            System.setProperty("org.netbeans.modules.parsing.impl.indexing.LogContext$EventType.PATH.minutes", "1");
            System.setProperty("org.netbeans.modules.parsing.impl.indexing.LogContext$EventType.PATH.treshold", "32000");
            RepositoryUpdater.getDefault().start(false);
            Project prj = ProjectManager.getDefault().findProject(FileUtil.toFileObject(projectDir));
            if (prj != null) {
                OpenProjects.getDefault().open(new Project[] {prj}, false);
            }

            ClassPath classPath = ClassPathSupport.createClassPath(fobjs);
            GlobalPathRegistry.getDefault().register("org.netbeans.modules.cnd.makeproject/SOURCES", new ClassPath[]{classPath});
        }

        log("setUp finished preparing project.");
        log("Test "+getName()+  "started");
    }
    
    @ServiceProvider(service=MimeDataProvider.class)
    public static final class MimeDataProviderImpl implements MimeDataProvider {

        private static final Lookup L = Lookups.singleton(new CndIndexer.Factory());

        @Override
        public Lookup getLookup(MimePath mimePath) {
            if (MIMENames.isHeaderOrCppOrC(mimePath.getPath())) {
                return L;
            }
            return null;
        }
    }
    
    /**
     * change the folder if needed from test folder to subfolder
     * i.e. if test folder has several folders: for project and libs =>
     * change dir to subfolders corresponding to projects dirs
     * @param projectDir current project dir
     * @return folders that should be used as project directories
     */
    protected File[] changeDefProjectDirBeforeParsingProjectIfNeeded(File projectDir) {
        return new File[] {projectDir};
    }

    protected void checkDir(File srcDir) {
        assertTrue("Not existing directory" + srcDir, srcDir.exists());
        assertTrue("Not directory" + srcDir, srcDir.isDirectory());
    }

    protected boolean isDumpingPPState() {
        Iterator<TestModelHelper> iterator = projectHelpers.values().iterator();
        while (iterator.hasNext()) {
            TestModelHelper testModelHelper = iterator.next();
        }
        return false;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (CndTraceFlags.USE_INDEXING_API) {
            RepositoryUpdater.getDefault().stop(null);
        }
        
        Iterator<TestModelHelper> iterator = projectHelpers.values().iterator();
        while (iterator.hasNext()) {
            TestModelHelper testModelHelper = iterator.next();
            testModelHelper.shutdown(!iterator.hasNext());
        }
        if (outputWriter != null) {
            outputWriter.flush();
            outputWriter.close();
        }
        if (logWriter != null) {
            logWriter.flush();
            logWriter.close();
        }
        sysIncludes.clear();
        usrIncludes.clear();
        projectDependencies.clear();
        projectHelpers.clear();
        assertTrue("unexpected exception " + CndUtils.getLastAssertion(), CndUtils.getLastAssertion() == null);
    }

    @Override
    protected File getDataFile(String filename) {
        if (performInWorkDir) {
            return new File(workDirBasedProject, filename);
        } else {
            return super.getDataFile(filename);
        }
    }     
    
    protected CsmProject getProject() {
        for (TestModelHelper testModelHelper : projectHelpers.values()) {
            return testModelHelper.getProject();
        }
        assert false : "no initialized projects";
        return null;
    }

    protected CsmProject getProject(String name) {
        CsmProject out = null;
        for (TestModelHelper testModelHelper : projectHelpers.values()) {
            if (name.contentEquals(testModelHelper.getProjectName())) {
                CsmProject project = testModelHelper.getProject();
                assertTrue("two projects with the same name " + name + " " + out + " and " + project, out == null);
                out = project;
                // do not break to allow initialization of all names in TestModelHelpers
            }
        }
        return out;
    }

    protected CsmModel getModel() {
        for (TestModelHelper testModelHelper : projectHelpers.values()) {
            return testModelHelper.getModel();
        }
        assert false : "no initialized projects";
        return null;
    }

    protected void reopenProject(String name, boolean waitParse) {
        for (TestModelHelper testModelHelper : projectHelpers.values()) {
            if (name.contentEquals(testModelHelper.getProjectName())) {
                testModelHelper.reopenProject();
                if (waitParse) {
                    waitAllProjectsParsed();
                }
                return;
            }
        }
    }

    protected void reparseAllProjects() {
        Collection<CsmProject> projects = getModel().projects();
        int expectedNrProjects = projects.size();
        getModel().scheduleReparse(projects);
        for (int i = 0; i < 20; i++) {
            sleep(1000);
            projects = getModel().projects();
            if (projects.size() == expectedNrProjects) {
                //System.err.println("scheduleReparse took "+(i+1)+" sec");
                break;
            }
        }
        projects = getModel().projects();
        assertEquals("projects " + projects, expectedNrProjects, projects.size());
        waitAllProjectsParsed();
    }

    protected void closeProject(String name) {
        for (TestModelHelper testModelHelper : projectHelpers.values()) {
            if (name.contentEquals(testModelHelper.getProjectName())) {
                testModelHelper.resetProject();
                return;
            }
        }
        assertFalse("Project not found or getProject was not called for this name before: " + name, true);
    }
    
    protected int getOffset(File testSourceFile, int lineIndex, int colIndex) throws Exception {
        BaseDocument doc = getBaseDocument(testSourceFile);
        assert doc != null;
        int offset = CndCoreTestUtils.getDocumentOffset(doc, lineIndex, colIndex);  
        return offset;
    }

    private List<String> getProjectPaths(Map<String, List<String>> map, String prjPath) {
        List<String> dependentProjects = map.get(prjPath);
        if (dependentProjects == null) {
            return Collections.emptyList();
        }
        return dependentProjects;
    }

    protected void dumpModel() {
        CsmCacheManager.enter();
        try {        
            for (CsmProject prj : getModel().projects()) {
                new CsmTracer(System.err).dumpModel(prj);
                for (CsmProject lib : prj.getLibraries()) {
                    new CsmTracer(System.err).dumpModel(lib);
                }
            }
            LibraryManager.dumpInfo(new PrintWriter(System.err), true);
        } finally {
            CsmCacheManager.leave();
        }
    }

    protected void waitAllProjectsParsed() {
        sleep(1000);
        Collection<CsmProject> projects;
        projects = getModel().projects();
        for (CsmProject csmProject : projects) {
            TraceModelBase.waitProjectParsed(((ProjectBase) csmProject), true);
        }
    }
    
    protected void checkDifference(File workDir, File goldenDataFile, File output) throws Exception {
        if (!goldenDataFile.exists()) {
            fail("No golden file " + goldenDataFile.getAbsolutePath() + "\n to check with output file " + output.getAbsolutePath());
        }
        if (diffGoldenFiles(isDumpingPPState(), output, goldenDataFile, null)) {
            // copy golden
            File goldenCopyFile = new File(workDir, goldenDataFile.getName() + ".golden");
            CndCoreTestUtils.copyToWorkDir(goldenDataFile, goldenCopyFile); // NOI18N
            StringBuilder buf = new StringBuilder("OUTPUT Difference between diff " + output + " " + goldenCopyFile);
            File diffErrorFile = new File(output.getAbsolutePath() + ".diff");
            diffGoldenFiles(isDumpingPPState(), output, goldenDataFile, diffErrorFile);
            showDiff(diffErrorFile, buf);
            fail(buf.toString());
        }         
    }
    
    private void copyDirToWorkDir(File sourceDir, File toDir, TraceModelFileFilter filter) throws IOException {
        assert (sourceDir.isDirectory()) : sourceDir.getAbsolutePath() + " is not a directory" ;// NOI18N;
        assert (sourceDir.exists()) : sourceDir.getAbsolutePath() + " does not exist" ;// NOI18N;
        toDir.mkdirs();
        assert (toDir.isDirectory());
        File files[] = sourceDir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File curFile = files[i];
                File newFile = new File(toDir, curFile.getName());
                if (curFile.isDirectory()) {
                    copyDirToWorkDir(curFile, newFile, filter);
                } else {
                    boolean add = true;
                    if (filter != null) {
                        add = filter.isProjectFile(curFile.getName());
                    }
                    if (add) {
                        copyToWorkDir(curFile, newFile, filter);
                    }
                }
            }
        }
    }
    
    private void copyToWorkDir(File resource, File toFile, TraceModelFileFilter filter) throws IOException {
        CndCoreTestUtils.copyToFile(resource, toFile);
    }
    
    public static final class SimpleFileFilter implements TraceModelFileFilter {
        private final String pattern;
        private final String additional;
        
        public SimpleFileFilter(String name) {
            pattern = name.toLowerCase();
            additional = null;
        }

        public SimpleFileFilter(String first, String second) {
            pattern = first.toLowerCase();
            additional = second.toLowerCase();
        }

        public static String testNameToFileName(String testName){
            String simpleName = testName.substring(4);
            int separator = simpleName.indexOf('$');
            if (separator > 0) {
                simpleName = simpleName.substring(0, separator);
            }
            return simpleName;
        }
        
        @Override
        public boolean isProjectFile(String filename) {
            if (filename.toLowerCase().contains(pattern)) {
                return true;
            }
            if (additional != null) {
                return filename.toLowerCase().contains(additional);
            }
            return false;
        }
    }

}
