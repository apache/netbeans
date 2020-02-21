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
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import org.netbeans.modules.cnd.makeproject.api.ui.wizard.WizardConstants;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.makeproject.ConfigurationDescriptorProviderImpl;
import org.netbeans.modules.cnd.makeproject.MakeActionProviderImpl;
import org.netbeans.modules.cnd.makeproject.MakeProjectTypeImpl;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectOptions;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.test.CndBaseTestCase;
import org.netbeans.modules.cnd.test.CndTestIOProvider;
import org.netbeans.modules.cnd.test.CndTestIOProvider.Listener;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.windows.IOProvider;

public class MakeSampleProjectIteratorTest extends CndBaseTestCase {
    private CompilerSet SunStudioSet = null;
    private CompilerSet GNUSet = null;
    private CompilerSet MinGWSet = null;
    private CompilerSet CygwinSet = null;

    private List<CompilerSet> allAvailableCompilerSets = null;
    private List<CompilerSet> SunStudioCompilerSet = null;
    private List<CompilerSet> GNUCompilerSet = null;
    private String[] defaultConfs = new String[] {"Debug", "Release"};
    private Logger executionLogger;
    private Logger baseExecutionLogger;

    public MakeSampleProjectIteratorTest(String name) {
        super(name);
    }

    @Before @Override
    public void setUp() throws Exception {
        super.setUp();
        List<CompilerSet> sets = CompilerSetManager.get(ExecutionEnvironmentFactory.getLocal()).getCompilerSets();
        for (CompilerSet set : sets) {
            if (set.getName().equals("OracleDeveloperStudio")) {
                SunStudioSet = set;
            }
            if (set.getName().equals("GNU")) {
                GNUSet = set;
            }
            if (set.getName().equals("MinGW")) {
                MinGWSet = set;
            }
            if (set.getName().equals("Cygwin_4.x")) {
                CygwinSet = set;
            }
            if (set.getName().equals("Cygwin") && CygwinSet == null) {
                CygwinSet = set;
            }
        }

        allAvailableCompilerSets = new ArrayList<>();
        allAvailableCompilerSets.add(SunStudioSet);
        allAvailableCompilerSets.add(GNUSet);
        allAvailableCompilerSets.add(MinGWSet);
        allAvailableCompilerSets.add(CygwinSet);

        SunStudioCompilerSet = new ArrayList<>();
        SunStudioCompilerSet.add(SunStudioSet);

        GNUCompilerSet = new ArrayList<>();
        GNUCompilerSet.add(GNUSet);
        executionLogger = Logger.getLogger("nativeexecution.support.logger");
        executionLogger.setLevel(Level.FINEST);
        baseExecutionLogger = Logger.getLogger("org.netbeans.api.extexecution.base.BaseExecutionService");
        baseExecutionLogger.setLevel(Level.FINEST);
    }

    @Test
    public void testArguments() throws IOException, InterruptedException, InvocationTargetException {
        testSample(allAvailableCompilerSets, "Arguments", defaultConfs, "", 20);
    }

    @Test
    public void testInputOutput() throws IOException, InterruptedException, InvocationTargetException {
        testSample(allAvailableCompilerSets, "InputOutput", defaultConfs, "", 20);
    }

    @Test
    public void testWelcome() throws IOException, InterruptedException, InvocationTargetException {
        testSample(allAvailableCompilerSets, "Welcome", defaultConfs, "", 20);
    }

    @Test
    public void testQuote() throws IOException, InterruptedException, InvocationTargetException {
        testSample(allAvailableCompilerSets, "Quote", defaultConfs, "", 20);
    }

    @Test
    public void testSubProjects() throws IOException, InterruptedException, InvocationTargetException {
        testSample(allAvailableCompilerSets, "SubProjects", defaultConfs, "", 20);
    }

    @Test
    public void testPi() throws IOException, InterruptedException, InvocationTargetException {
        if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
            testSample(SunStudioCompilerSet, "Pi", new String[] {"Serial", "Pthreads", "Pthreads_safe", "Pthread_Hot", "OpenMP"}, "", 20);
            testSample(GNUCompilerSet, "Pi", new String[] {"Serial"}, "", 20);
        }
        else {
            testSample(allAvailableCompilerSets, "Pi", new String[] {"Serial"}, "", 20);
        }
    }

    @Test
    public void testFreeway() throws IOException, InterruptedException, InvocationTargetException {
        if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS || Utilities.getOperatingSystem() == Utilities.OS_LINUX) {
            testSample(allAvailableCompilerSets, "Freeway", defaultConfs, "", 30);
        }
    }

    @Test
    public void testFractal() throws IOException, InterruptedException, InvocationTargetException {
        testSample(allAvailableCompilerSets, "Fractal", new String[] {"FastBuild", "Debug", "PerformanceDebug", "DianogsableRelease", "Release", "PerformanceRelease"}, "", 20);
    }

    @Test
    public void testLexYacc() throws IOException, InterruptedException, InvocationTargetException {
        if (!Utilities.isWindows()) {
            testSample(allAvailableCompilerSets, "LexYacc", defaultConfs, "", 20);
        }
    }

    @Test
    public void testMP() throws IOException, InterruptedException, InvocationTargetException {
        if (!Utilities.isWindows()) {
            testSample(allAvailableCompilerSets, "MP", new String[] {"Debug", "Debug_mp", "Release", "Release_mp"}, "", 20);
        }
    }

    @Test
    public void testHello() throws IOException, InterruptedException, InvocationTargetException {
        if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
            testSample(SunStudioCompilerSet, "Hello", defaultConfs, "", 20);
        }
    }

//    @Test
//    public void testHelloQtWorld() throws IOException, InterruptedException, InvocationTargetException {
//        if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
//            testSample(SunStudioCompilerSet, "HelloQtWorld", defaultConfs, "-j 1");
//        }
//        if (Utilities.getOperatingSystem() == Utilities.OS_LINUX) {
//            testSample(GNUCompilerSet, "HelloQtWorld", defaultConfs, "");
//        }
//    }

    //@Test
    //public void testProfilingDemo() throws IOException, InterruptedException, InvocationTargetException {
    //    if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS || Utilities.getOperatingSystem() == Utilities.OS_LINUX) {
    //        testSample(SunStudioCompilerSet, "ProfilingDemo", defaultConfs, "");
    //    }
    //}

    @Override
    protected List<Class<?>> getServices() {
        List<Class<?>> list = new ArrayList<>();
        list.add(MakeProjectTypeImpl.class);
        list.addAll(super.getServices());
        return list;
    }

    protected static Set<DataObject>  instantiateSample(String name, final File destdir) throws IOException, InterruptedException, InvocationTargetException {
        if(destdir.exists()) {
            assertTrue("Can not remove directory " + destdir.getAbsolutePath(), removeDirectoryContent(destdir));
        }
        final FileObject templateFO = FileUtil.getConfigFile("Templates/Project/Samples/Native/" + name);
        assertNotNull("FileObject for " + name + " sample not found", templateFO);
        final DataObject templateDO = DataObject.find(templateFO);
        assertNotNull("DataObject for " + name + " sample not found", templateDO);
        final AtomicReference<IOException> exRef = new AtomicReference<>();
        final AtomicReference<Set<DataObject>> setRef = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            MakeSampleProjectIterator projectCreator = new MakeSampleProjectIterator();
            TemplateWizard wiz = new TemplateWizard();
            wiz.setTemplate(templateDO);
            projectCreator.initialize(wiz);
            WizardConstants.PROPERTY_NAME.put(wiz, destdir.getName());
            ExecutionEnvironment ee = ExecutionEnvironmentFactory.getLocal();
            WizardConstants.PROPERTY_PROJECT_FOLDER.put(wiz, 
                    new FSPath(FileSystemProvider.getFileSystem(ee), RemoteFileUtil.normalizeAbsolutePath(destdir.getAbsolutePath(), ee)));
            try {
                setRef.set(projectCreator.instantiate());
            } catch (IOException ex) {
                exRef.set(ex);
            }
        });
        if (exRef.get() != null) {
            throw exRef.get();
        }
        return setRef.get();
    }

    public void testSample(List<CompilerSet> sets, String sample, String[] confs, String makeOptions, int timeout) throws IOException, InterruptedException, InvocationTargetException {
        // keep IOProvider in local variable to prevent weak lookup storage garbage collected.
        final IOProvider iop = IOProvider.getDefault();
        for (CompilerSet set : sets) {
            if (set != null) {
                for (String conf : confs) {
                    testSample(set, sample, conf, makeOptions, timeout);
                }
            }
        }
    }

    public void testSample(CompilerSet set, String sample, String conf, String makeOptions, int timeout) throws IOException, InterruptedException, InvocationTargetException {
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicInteger build_rc = new AtomicInteger(-1);

        CompilerSetManager.get(ExecutionEnvironmentFactory.getLocal()).setDefault(set);
        MakeProjectOptions.setDefaultMakeOptions(makeOptions);

        File workDir = getWorkDir();//new File("/tmp");
        File projectDir = new File(workDir, sample + set.getName() + conf);
        File mainProjectDir = null;
        FileObject mainProjectDirFO = null;
        Set<DataObject> projectDataObjects;

        projectDataObjects = instantiateSample(sample, projectDir);
        assertTrue(projectDataObjects.size()>0);

        for (DataObject projectDataObject : projectDataObjects) {
            FileObject projectDirFO = projectDataObject.getPrimaryFile();
            if (mainProjectDir == null) {
                mainProjectDirFO = projectDirFO;
                mainProjectDir = CndFileUtils.toFile(projectDirFO);
            }
            ConfigurationDescriptorProvider descriptorProvider = new ConfigurationDescriptorProviderImpl(projectDirFO);
            MakeConfigurationDescriptor descriptor = descriptorProvider.getConfigurationDescriptor();
            descriptor.getConfs().setActive(conf);
            descriptor.save(); // make sure all necessary configuration files in nbproject/ are written
        }

        final String successLine = "BUILD SUCCESSFUL";
        final String failureLine = "BUILD FAILED";

        final IOProvider iop = IOProvider.getDefault();
        assert iop instanceof CndTestIOProvider : "found " + iop.getClass();
        final StringBuilder buf = new StringBuilder();
        final Listener listener = new CndTestIOProvider.Listener() {
            @Override
            public void linePrinted(String line) {
                if(line != null) {
                    buf.append(line).append('\n');
                    if (line.trim().startsWith(successLine)) {
                        build_rc.set(0);
                        done.countDown();
                    }
                    else if (line.trim().startsWith(failureLine)) {
                        // message is:
                        // BUILD FAILED (exit value 1, total time: 326ms)
                        int rc = -1;
                        String[] tokens = line.split("[ ,]");
                        if (tokens.length > 4) {
                            try {
                                rc = Integer.parseInt(tokens[4]);
                            } catch(NumberFormatException nfe) {
                                nfe.printStackTrace(System.err);
                            }
                        }
                        build_rc.set(rc);
                        done.countDown();
                    }
                }
            }
        };
        ((CndTestIOProvider) iop).addListener(listener);
        MakeProject makeProject = (MakeProject) ProjectManager.getDefault().findProject(mainProjectDirFO);
        assertNotNull(makeProject);
        MakeActionProviderImpl makeActionProvider = new MakeActionProviderImpl(makeProject);
        makeActionProvider.invokeAction("build", Lookup.EMPTY);

//        File makefile = new File(mainProjectDir, "Makefile");
//        FileObject makefileFileObject = CndFileUtils.toFileObject(makefile);
//        assertTrue("makefileFileObject == null", makefileFileObject != null);
//        DataObject dObj = null;
//        try {
//            dObj = DataObject.find(makefileFileObject);
//        } catch (DataObjectNotFoundException ex) {
//        }
//        assertTrue("DataObjectNotFoundException", dObj != null);
//        Node node = dObj.getNodeDelegate();
//        assertTrue("node == null", node != null);
//
//        MakeExecSupport ses = node.getCookie(MakeExecSupport.class);
//        assertTrue("ses == null", ses != null);
//
//        MakeAction.execute(node, target, new MyExecutionListener(), null, null, null);
//
        try {
            done.await(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException ir) {
        }
        ((CndTestIOProvider) iop).removeListener(listener);
        String shell = null;
        try {
            shell = HostInfoUtils.getHostInfo(ExecutionEnvironmentFactory.getLocal()).getShell();
        } catch (Exception ex) {
        }
        Tool C = set.findTool(PredefinedToolKind.CCompiler);
        Tool CPP = set.findTool(PredefinedToolKind.CCCompiler);
        Tool MAKE = set.findTool(PredefinedToolKind.MakeTool);
        if (build_rc.intValue() == -1) {
            CndUtils.threadsDump();
        }
        assertTrue("build failed - rc = " + build_rc.intValue()+
                "\nBuildLog:\n"+buf.toString()+
                "\nTool Collection:"+set.getName()+
                "\n\tC:"+(C == null?"null":C.getPath())+
                "\n\tC++:"+(CPP == null?"null":CPP.getPath())+
                "\n\tmake:"+(MAKE == null?"null":MAKE.getPath())+
                "\n\tShell:"+shell,
                build_rc.intValue() == 0);
    }
}
