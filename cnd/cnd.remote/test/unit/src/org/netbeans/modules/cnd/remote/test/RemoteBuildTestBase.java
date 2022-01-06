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

package org.netbeans.modules.cnd.remote.test;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.SuppressWarnings;
import org.netbeans.api.project.ProjectManager;
//import org.netbeans.modules.cnd.api.model.CsmFile;
//import org.netbeans.modules.cnd.api.model.CsmInclude;
//import org.netbeans.modules.cnd.api.model.CsmModel;
//import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
//import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.builds.MakeExecSupport;
import org.netbeans.modules.cnd.makeproject.ConfigurationDescriptorProviderImpl;
import org.netbeans.modules.cnd.makeproject.MakeProjectImpl;
import org.netbeans.modules.cnd.makeproject.MakeProjectTypeImpl;
import org.netbeans.modules.cnd.makeproject.NativeProjectProvider;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.makeproject.api.configurations.CompilerSet2Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.DevelopmentHostConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.WizardConstants;
import org.netbeans.modules.cnd.makeproject.ui.wizards.MakeSampleProjectIterator;
//import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.remote.server.RemoteServerRecord;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.nativeexecution.test.RcFile;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.TemplateWizard;
import org.openide.nodes.Node;

// NOTE: Some tests commented out since Apache NetBeans does not use the
// donated cnd.api.model, cnd.indexing, cnd.modelimpl and cnd.repository modules.

/**
 * A common base class for tests that build remote project
 */
public class RemoteBuildTestBase extends RemoteTestBase {

    private static final boolean trace = Boolean.getBoolean("cnd.test.remote.code.model.trace");
    static {
        System.setProperty("apt.trace.resolver", "true");
        if (trace) {
            System.setProperty("org.netbeans.modules.cnd.test.CndTestIOProvider.traceout","true"); // NOI18N
        }
    }

    public RemoteBuildTestBase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    public RemoteBuildTestBase(String testName) {
        super(testName);
    }

    protected int getSampleBuildTimeout() throws Exception {
        int result = 180;
        RcFile rcFile = NativeExecutionTestSupport.getRcFile();
        String timeout = rcFile.get("remote", "sample.build.timeout");
        if (timeout != null) {
            result = Integer.parseInt(timeout);
        }
        return result;
    }

    protected FileObject instantiateSample(String name, final File destdir) throws IOException, InterruptedException, InvocationTargetException {
        if(destdir.exists()) {
            assertTrue("Can not remove directory " + destdir.getAbsolutePath(), removeDirectoryContent(destdir));
        }
        final FileObject templateFO = FileUtil.getConfigFile("Templates/Project/Samples/Native/" + name);
        assertNotNull("FileObject for " + name + " sample not found", templateFO);
        final DataObject templateDO = DataObject.find(templateFO);
        assertNotNull("DataObject for " + name + " sample not found", templateDO);
        final AtomicReference<IOException> exRef = new AtomicReference<>();
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                MakeSampleProjectIterator projectCreator = new MakeSampleProjectIterator();
                TemplateWizard wiz = new TemplateWizard();
                wiz.setTemplate(templateDO);
                projectCreator.initialize(wiz);
                WizardConstants.PROPERTY_NAME.put(wiz, destdir.getName());
                ExecutionEnvironment ee = ExecutionEnvironmentFactory.getLocal();
                WizardConstants.PROPERTY_PROJECT_FOLDER.put(wiz, 
                    new FSPath(FileSystemProvider.getFileSystem(ee), RemoteFileUtil.normalizeAbsolutePath(destdir.getAbsolutePath(), ee)));
                try {
                    projectCreator.instantiate();
                } catch (IOException ex) {
                    exRef.set(ex);
                }
            }
        });
        if (exRef.get() != null) {
            throw exRef.get();
        }
        return CndFileUtils.toFileObject(destdir);
    }

    @Override
    protected List<Class<?>> getServices() {
        List<Class<?>> list = new ArrayList<>();
        list.add(MakeProjectTypeImpl.class);
        list.addAll(super.getServices());
        return list;
    }

    protected void setupHost() throws Exception {
        setupHost((String) null);
    }

    protected void setSyncFactory(String remoteSyncFactoryID) {
        ServerRecord record = ServerList.get(getTestExecutionEnvironment());
        assertNotNull(record);
        RemoteSyncFactory syncFactory = RemoteSyncFactory.fromID(remoteSyncFactoryID);
        assertNotNull(syncFactory);
        ((RemoteServerRecord) record).setSyncFactory(syncFactory);
    }

    protected void setupHost(String remoteSyncFactoryID) throws Exception {
        ExecutionEnvironment env = getTestExecutionEnvironment();
        setupHost(env);
        RemoteSyncFactory syncFactory = null;
        if (remoteSyncFactoryID != null) {
            syncFactory = RemoteSyncFactory.fromID(remoteSyncFactoryID);
        }
        if (syncFactory == null) {
            syncFactory = RemoteSyncFactory.getDefault();
        }
        RemoteServerRecord rec = (RemoteServerRecord) ServerList.addServer(env, env.getDisplayName(), syncFactory, true, true);
        rec.setSyncFactory(syncFactory);
        assertNotNull("Null ServerRecord for " + env, rec);
        clearRemoteSyncRoot();
    }

    protected MakeProject prepareSampleProject(Sync sync, Toolchain toolchain, String sampleName,  String projectDirBase)
            throws IllegalArgumentException, IOException, Exception, InterruptedException, InvocationTargetException {
        setupHost();
        setSyncFactory(sync.ID);
        assertEquals("Wrong sync factory:", sync.ID, ServerList.get(getTestExecutionEnvironment()).getSyncFactory().getID());
        setDefaultCompilerSet(toolchain.ID);
        assertEquals("Wrong tools collection", toolchain.ID, CompilerSetManager.get(getTestExecutionEnvironment()).getDefaultCompilerSet().getName());
        String prjDirBase = ((projectDirBase == null) ? sampleName : projectDirBase) + "_" + sync.ID;
        FileObject projectDirFO = prepareSampleProject(sampleName, prjDirBase);
        MakeProject makeProject = (MakeProject) ProjectManager.getDefault().findProject(projectDirFO);
        return makeProject;
    }

    @SuppressWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    protected FileObject prepareSampleProject(String sampleName, String projectDirShortName) throws IOException, InterruptedException, InvocationTargetException {
        // reusing directories makes debugging much more difficult, so we add host name
        projectDirShortName += "_" + getTestHostName();
        //File projectDir = new File(getWorkDir(), projectDirShortName);
        File projectDir = File.createTempFile(projectDirShortName + "_", "", getWorkDir());
        projectDir.delete();
        FileObject projectDirFO = instantiateSample(sampleName, projectDir);
        ConfigurationDescriptorProvider descriptorProvider = new ConfigurationDescriptorProviderImpl(projectDirFO);
        MakeConfigurationDescriptor descriptor = descriptorProvider.getConfigurationDescriptor();
        descriptor.save(); // make sure all necessary configuration files in nbproject/ are written
        File makefile = new File(projectDir, "Makefile");
        FileObject makefileFileObject = CndFileUtils.toFileObject(makefile);
        assertTrue("makefileFileObject == null", makefileFileObject != null);
        assertTrue("makefileFileObject is invalid", makefileFileObject.isValid());
        DataObject dObj = null;
        try {
            dObj = DataObject.find(makefileFileObject);
        } catch (DataObjectNotFoundException ex) {
        }
        assertTrue("DataObjectNotFoundException", dObj != null);
        Node node = dObj.getNodeDelegate();
        assertTrue("node == null", node != null);
        MakeExecSupport ses = node.getLookup().lookup(MakeExecSupport.class);
        assertTrue("ses == null", ses != null);
        return projectDirFO;
    }

    protected void setDefaultCompilerSet(String name) {
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        ServerRecord record = ServerList.get(execEnv);
        assertNotNull(record);
        final CompilerSetManager csm = CompilerSetManager.get(execEnv);
        for (CompilerSet cset : csm.getCompilerSets()) {
            if (cset.getName().equals(name)) {
                csm.setDefault(cset);
                break;
            }
        }
    }

    protected void changeProjectHost(MakeProject project, ExecutionEnvironment execEnv) {
        // the code below is copypasted from  org.netbeans.modules.cnd.makeproject.ui.RemoteDevelopmentAction
        ConfigurationDescriptorProvider configurationDescriptorProvider = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        assertNotNull("ConfigurationDescriptorProvider shouldn't be null", configurationDescriptorProvider);
        MakeConfigurationDescriptor configurationDescriptor = configurationDescriptorProvider.getConfigurationDescriptor();
        MakeConfiguration mconf = configurationDescriptor.getActiveConfiguration();
        // the below wiill throw NPE, the above woin't
        // MakeConfiguration mconf = project.getActiveConfiguration();
        ServerRecord record = ServerList.get(execEnv);
        assertTrue("Host " + execEnv, record.isSetUp());
        DevelopmentHostConfiguration dhc = new DevelopmentHostConfiguration(execEnv);
        mconf.setDevelopmentHost(dhc);
        CompilerSet2Configuration oldCS = mconf.getCompilerSet();
        if (oldCS.isDefaultCompilerSet()) {
            mconf.setCompilerSet(new CompilerSet2Configuration(dhc));
        } else {
            String oldCSName = oldCS.getName();
            CompilerSetManager csm = CompilerSetManager.get(dhc.getExecutionEnvironment());
            CompilerSet newCS = csm.getCompilerSet(oldCSName);
            // if not found => use default from new host
            newCS = (newCS == null) ? csm.getDefaultCompilerSet() : newCS;
            mconf.setCompilerSet(new CompilerSet2Configuration(dhc, newCS));
        }
//                    PlatformConfiguration platformConfiguration = mconf.getPlatform();
//                    platformConfiguration.propertyChange(new PropertyChangeEvent(
//                            jmi, DevelopmentHostConfiguration.PROP_DEV_HOST, oldDhc, dhc));
            //FIXUP: please send PropertyChangeEvent to MakeConfiguration listeners
            //when you do this changes
            //see cnd.tha.THAMainProjectAction which should use huck to get these changes
            NativeProjectProvider npp = project.getLookup().lookup(NativeProjectProvider.class);
            npp.propertyChange(new PropertyChangeEvent(this, ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE, null, mconf));
            //ConfigurationDescriptorProvider configurationDescriptorProvider = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
            //ConfigurationDescriptor configurationDescriptor = configurationDescriptorProvider.getConfigurationDescriptor();
            configurationDescriptor.setModified();
    }


    /** Allow descendants making additional actions */
    protected void postCopyProject(File origBase, File copiedBase, String projectName) throws Exception {
    }

    protected MakeProject openProject(String projectName, final ExecutionEnvironment execEnv, Sync sync, Toolchain toolchain) throws IOException, Exception, IllegalArgumentException {
        File origBase = getDataFile(projectName).getParentFile();
        File tempFile = File.createTempFile(origBase.getName(), "copy");
        tempFile.delete();
        File copiedBase = new File(new File(getWorkDir(), getTestHostName()), tempFile.getName());
        removeDirectoryContent(copiedBase);
        copyDirectory(origBase, copiedBase);
        postCopyProject(origBase, copiedBase, projectName);
        File projectDirFile = new File(copiedBase, projectName);
        // call this only before opening project!
        changeProjectHost(projectDirFile, execEnv);
        setupHost(sync.ID);
        setSyncFactory(sync.ID);
        assertEquals("Wrong sync factory:", sync.ID, ServerList.get(execEnv).getSyncFactory().getID());
        setDefaultCompilerSet(toolchain.ID);
        assertEquals("Wrong tools collection", toolchain.ID, CompilerSetManager.get(execEnv).getDefaultCompilerSet().getName());
        assertTrue(projectDirFile.exists());
        FileObject projectDirFO = CndFileUtils.toFileObject(projectDirFile);
        projectDirFO.refresh(true);
        MakeProject makeProject = (MakeProject) ProjectManager.getDefault().findProject(projectDirFO);
        ensureMakefilesWritten(makeProject);
        //changeProjectHost(makeProject, execEnv);
        assertNotNull("project is null", makeProject);
        return makeProject;
    }
    
    protected void ensureMakefilesWritten(MakeProject makeProject) {
        File nbproject = new File(FileUtil.toFile(makeProject.getProjectDirectory()), "nbproject");
        File publicConfFile = new File(nbproject, "configurations.xml");
        publicConfFile.setLastModified(publicConfFile.lastModified() + 1000*60*60); // this forces makefile regeneration for managed projects
        FileUtil.toFileObject(publicConfFile).refresh(true);
        ((MakeProjectImpl)makeProject).save();
    }

    private void changeProjectHost(File projectDir, ExecutionEnvironment env) throws Exception {
        File nbproject = new File(projectDir, "nbproject");
        assertTrue("file does not exist: " + nbproject.getAbsolutePath(), nbproject.exists());
        File publicConfFile = new File(nbproject, "configurations.xml");
        assertTrue(publicConfFile.exists());
        File privateConfFile = new File(new File(nbproject, "private"), "configurations.xml");
        boolean changed = changeProjectHostImpl(publicConfFile, env);
        if (privateConfFile.exists()) {
            changed |= changeProjectHostImpl(privateConfFile, env);            
        }
        assertTrue("Can not change development host for " + projectDir.getAbsolutePath(), changed);
    }
    
    private boolean changeProjectHostImpl(File confFile, ExecutionEnvironment env) throws Exception {
        String text = readFile(confFile);
        String openTag = "<developmentServer>";
        String closeTag = "</developmentServer>";
        int start = text.indexOf(openTag);
        start += openTag.length();
        if(start >= 0) {
            int end = text.indexOf(closeTag);
            if (end >= 0) {
                StringBuilder newText = new StringBuilder();
                newText.append(text.substring(0, start));
                newText.append(ExecutionEnvironmentFactory.toUniqueID(env));
                newText.append(text.substring(end));
                writeFile(confFile, newText);
            }
            return true;
        }
        return false;
    }

    protected void buildSample(Sync sync, Toolchain toolchain, String sampleName, String projectDirBase, int count) throws Exception {
        int timeout = getSampleBuildTimeout();
        buildSample(sync, toolchain, sampleName, projectDirBase, count, timeout, timeout);
    }

    protected interface ProjectProcessor {
        void processProject(MakeProject project) throws Exception;
    }

    protected void buildSample(Sync sync, Toolchain toolchain, String sampleName, String projectDirBase,
            int count, int firstTimeout, int subsequentTimeout) throws Exception {
        buildSample(sync, toolchain, sampleName, projectDirBase, count, firstTimeout, subsequentTimeout, null);
    }

    protected void buildSample(Sync sync, Toolchain toolchain, String sampleName, String projectDirBase,
            int count, int firstTimeout, int subsequentTimeout, ProjectProcessor projectProcessor) throws Exception {
        MakeProject makeProject = prepareSampleProject(sync, toolchain, sampleName, projectDirBase);
        if (projectProcessor != null) {
            projectProcessor.processProject(makeProject);
        }
        for (int i = 0; i < count; i++) {
            if (count > 0) {
                System.err.printf("BUILDING %s, PASS %d\n", sampleName, i);
            }
            buildProject(makeProject, ActionProvider.COMMAND_BUILD, firstTimeout, TimeUnit.SECONDS);
        }
    }

//    protected CsmProject getCsmProject(MakeProject makeProject) throws Exception {
//        NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
//        assertNotNull("Null NativeProject", np);
//        CsmModel model = CsmModelAccessor.getModel();
//        ((ModelImpl) model).enableProject(np);
//        CsmProject csmProject = model.getProject(makeProject);
//        return csmProject;
//    }
//    
//    protected void checkCodeModel(MakeProject makeProject) throws Exception {
//        CsmProject csmProject = getCsmProject(makeProject);
//        assertNotNull("Null CsmProject", csmProject);
//        csmProject.waitParse();
//        checkIncludes(csmProject, true);
//    }
//        
//    protected void checkIncludes(CsmFile csmFile, boolean recursive, Set<CsmFile> antiLoop) throws Exception {
//        if (!antiLoop.contains(csmFile)) {
//            antiLoop.add(csmFile);
//            trace("Checking %s\n", csmFile.getAbsolutePath());
//            for (CsmInclude incl : csmFile.getIncludes()) {
//                CsmFile includedFile = incl.getIncludeFile();
//                trace("\t%s -> %s\n", incl.getIncludeName(), includedFile);
//                assertNotNull("Unresolved include: " + incl.getIncludeName() + " in " + csmFile.getAbsolutePath(), includedFile);
//                if (recursive) {
//                    checkIncludes(includedFile, true, antiLoop);
//                }
//            }
//        }
//    }
//
//    protected void checkIncludes(CsmProject csmProject, boolean recursive) throws Exception {
//        for (CsmFile csmFile : csmProject.getAllFiles()) {
//            checkIncludes(csmFile, recursive, new HashSet<CsmFile>());
//        }
//    }
    
    protected void trace(String pattern, Object... args) {
        if (trace) {
            System.err.printf(pattern, args);
        }
    }    
}
