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

package org.netbeans.modules.cnd.discovery.project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.api.project.IncludePath;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.discovery.projectimport.DoubleFile;
import org.netbeans.modules.nativeexecution.api.util.Path;
import org.netbeans.modules.cnd.discovery.projectimport.ImportProject;
import org.netbeans.modules.cnd.indexing.impl.TextIndexStorageManager;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.MakeProjectTypeImpl;
import org.netbeans.modules.cnd.makeproject.api.wizards.BuildSupport;
import org.netbeans.modules.cnd.makeproject.api.wizards.PreBuildSupport;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.WizardConstants;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.test.ModelBasedTestCase;
import org.netbeans.modules.cnd.repository.support.RepositoryTestUtils;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * To speed up tests:
 * create $HOME/cnd-test-files-storage
 * with all gz files, i.e. 
 *  cmake-2.6.4.tar.gz
 *  DiscoveryTestApplication.tar.gz
 *  pkg-config-0.25.tar.gz
 *  qlife-qt4-0.9.tar.gz
 *  sqlite-autoconf-3071700.tar.gz
 */
public abstract class MakeProjectTestBase extends ModelBasedTestCase { //extends NbTestCase
    protected static final String LOG_POSTFIX = ".discoveryLog";
    private static final boolean TRACE = false;
    private final Logger logger1;

    public MakeProjectTestBase(String name) {
        super(name);
        if (TRACE) {
            System.setProperty("cnd.discovery.trace.projectimport", "true"); // NOI18N
            System.setProperty("org.netbeans.modules.cnd.test.CndTestIOProvider.traceout","true"); // NOI18N
        }
        //System.setProperty("org.netbeans.modules.cnd.makeproject.api.runprofiles", "true"); // NOI18N
//        System.setProperty("cnd.modelimpl.assert.notfound", "true");
        System.setProperty("cnd.mode.unittest", "true");
        System.setProperty("org.netbeans.modules.cnd.apt.level","OFF"); // NOI18N
        //System.setProperty("cnd.modelimpl.timing","true"); // NOI18N
        System.setProperty("parser.report.include.failures","true"); // NOI18N
        //System.setProperty("cnd.modelimpl.timing.per.file.flat","true"); // NOI18N
        //System.setProperty("cnd.dump.native.file.item.paths","true"); // NOI18N
        logger1 = Logger.getLogger("org.netbeans.modules.editor.settings.storage.Utils");
        logger1.setLevel(Level.SEVERE);
        //System.setProperty("org.netbeans.modules.cnd.apt.level","WARNING"); // NOI18N
        //Logger.getLogger("org.netbeans.modules.cnd.apt").setLevel(Level.WARNING);
        //MockServices.setServices(MakeProjectType.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        //MockServices.setServices(MakeProjectType.class);
        MakeOptions.getInstance().setFixUnresolvedInclude(false);
        startupModel();
    }

    @Override
    protected List<Class<?>> getServices() {
        List<Class<?>> list = new ArrayList<>();
        list.add(MakeProjectTypeImpl.class);
        list.addAll(super.getServices());
        return list;
    }
 
    @Override
    protected void setUpMime() {
        // setting up MIME breaks other services
        super.setUpMime();
    }

    private void startupModel() {
        ModelImpl model = (ModelImpl) CsmModelAccessor.getModel();
        model.startup();
        RepositoryTestUtils.deleteDefaultCacheLocation();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        shutdownModel();
    }

    private void shutdownModel() {
        ModelImpl model = (ModelImpl) CsmModelAccessor.getModel();
        model.shutdown();
        RepositoryTestUtils.deleteDefaultCacheLocation();
        TextIndexStorageManager.shutdown();
    }

    protected File detectConfigure(String path){
        File configure = new File(path, "configure");
        if (configure.exists()) {
            return configure;
        }
        configure = new File(path, "CMakeLists.txt");
        if (configure.exists()) {
            return configure;
        }
        File base = new File(path);
        File[] files = base.listFiles();
        if (files != null){
            for(File file : files) {
                if (file.getAbsolutePath().endsWith(".pro")){
                    return file;
                }
            }
        }
        if (new File(path, "configure").exists()) {
            return new File(path, "configure");
        }
        if (files != null){
            for(File file : files) {
                if (file.isDirectory()) {
                    File res = new File(file,"configure");
                    if (res.exists()) {
                        return res;
                    }
                }
            }
        }
        return new File(path, "configure");
    }
    
    protected ExecutionEnvironment getEE(){
        return ExecutionEnvironmentFactory.getLocal();
    }
    
    protected void setupWizard(WizardDescriptor wizard) {
        
    }

    public void performTestProject(String URL, List<String> additionalScripts, boolean useSunCompilers, final String subFolder) throws Exception {
        Map<String, String> tools = findTools();
        final ExecutionEnvironment ee = getEE();
        if (ee == null) {
            System.err.println("REMOTE IS NOT SET UP CORRECTLY. Check ~/.cndtestrc");
            return;
        }        
        CompilerSetManager csm = CompilerSetManager.get(ee);
        while (csm.isPending()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                // skip
            }
        }
        CompilerSet def = csm.getDefaultCompilerSet();
        if (useSunCompilers) {
            if (def != null && def.getCompilerFlavor().isGnuCompiler()) {
                for(CompilerSet set : CompilerSetManager.get(ee).getCompilerSets()){
                    if (set.getCompilerFlavor().isSunStudioCompiler()) {
                        CompilerSetManager.get(ee).setDefault(set);
                        break;
                    }
                }
            }
        } else {
            if (def != null && def.getCompilerFlavor().isSunStudioCompiler()) {
                for(CompilerSet set : CompilerSetManager.get(ee).getCompilerSets()){
                    if (set.getCompilerFlavor().isGnuCompiler()) {
                        CompilerSetManager.get(ee).setDefault(set);
                        break;
                    }
                }
            }
        }
        def = CompilerSetManager.get(ee).getDefaultCompilerSet();
        final boolean isSUN = def != null ? def.getCompilerFlavor().isSunStudioCompiler() : false;
        if (tools == null) {
            assertTrue("Please install required tools.", false);
            System.err.println("Test did not run because required tools do not found");
            return;
        }
        try {
            String path = download(URL, additionalScripts, tools)+subFolder;
            MyWizardDescriptor wizard = new MyWizardDescriptor(path, ExecutionEnvironmentFactory.getLocal(), ee, def);
            wizard.init();
            setupWizard(wizard);
            ImportProject importer = new ImportProject(wizard);
            importer.setUILessMode();
            importer.create();
            OpenProjects.getDefault().open(new Project[]{importer.getProject()}, false);
            int i = 0;
            while(!importer.isFinished()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (i > 10 && !OpenProjects.getDefault().isProjectOpen(importer.getProject())){
                    break;
                }
                i++;
            }
            final ImportProject.State configureStep = importer.getState().get(ImportProject.Step.Configure);
            if (configureStep != null) {
                assertEquals("Failed configure", ImportProject.State.Successful, configureStep);
            }
            if (!ImportProject.State.Successful.equals(importer.getState().get(ImportProject.Step.Make))) {
                DoubleFile file = importer.getMakeLog();
                if (file != null) {
                    FileObject makeLog = importer.getMakeLog().getLocalFileObject();
                    if (makeLog != null && makeLog.isValid()) {
                        System.err.println("Build log:");
                        System.err.println(makeLog.asText());
                    }
                }
                assertEquals("Failed build", ImportProject.State.Successful, importer.getState().get(ImportProject.Step.Make));
            }
            CsmModel model = CsmModelAccessor.getModel();
            Project makeProject = importer.getProject();
            assertTrue("Not found model", model != null);
            assertTrue("Not found make project", makeProject != null);
            NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
            assertTrue("Not found native project", np != null);
            CsmProject csmProject = model.getProject(np);
            assertTrue("Not found model project", csmProject != null);
            csmProject.waitParse();
            perform(csmProject);
            OpenProjects.getDefault().close(new Project[]{makeProject});
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            assertTrue(ex.getMessage(), false);
        }
    }

    protected boolean findObjectFiles(String path){
        return findObjectFiles(new File(path));
    }

    private boolean findObjectFiles(File file){
        if (file.isDirectory()) {
            File[] ff = file.listFiles();
            if (ff != null) {
                for(File f : ff){
                    if (f.isDirectory()) {
                        boolean b = findObjectFiles(f);
                        if (b) {
                            return true;
                        }
                    } else if (f.isFile() && f.getName().endsWith(".o")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected List<String> requiredTools(){
        List<String> list = new ArrayList<>();
        list.add("wget");
        list.add("gzip");
        list.add("tar");
        list.add("rm");
        list.add("cp");
        return list;
    }

    protected Map<String, String> findTools(){
        Map<String, String> map = new HashMap<>();
        for(String t: requiredTools()){
            map.put(t, null);
        }
        if (findTools(map)){
            return map;
        }
        return null;
    }

    private boolean findTools(Map<String, String> map){
        if (map.isEmpty()) {
            return true;
        }
        for (String path : Path.getPath()) {
            for(Map.Entry<String, String> entry : map.entrySet()){
                if (entry.getValue() == null) {
                    String task = path+File.separatorChar+entry.getKey();
                    File tool = new File(task);
                    if (tool.exists() && tool.isFile()) {
                        entry.setValue(task);
                    } else if (Utilities.isWindows()) {
                        task = task+".exe";
                        tool = new File(task);
                        if (tool.exists() && tool.isFile()) {
                            entry.setValue(task);
                        }   
                    }
                }
            }
        }
        boolean res = true;
        for(Map.Entry<String, String> entry : map.entrySet()){
           if (entry.getValue() == null) {
               System.err.println("Not found required tool: "+entry.getKey());
               res =false;
           } else {
              if (TRACE) {
                   System.err.println("Found required tool: "+entry.getKey()+"="+entry.getValue());
              }
           }
        }
        return res;
    }

    protected void perform(CsmProject csmProject) throws Exception {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        csmProject.waitParse();
        Collection<CsmFile> col = csmProject.getAllFiles();
        if (TRACE) {
            System.err.println("Model has "+col.size()+" files");
        }
        boolean hasInvalidFiles = false;
        for (CsmFile file : col) {
            if (TRACE) {
                //System.err.println("\t"+file.getAbsolutePath());
            }
            boolean fileHasUnresolved = false;
            for(CsmInclude include : file.getIncludes()){
                if (include.getIncludeFile() == null) {
                    hasInvalidFiles = true;
                    fileHasUnresolved = true;
                    System.err.println("Not resolved include directive "+include.getIncludeName()+" in file "+file.getAbsolutePath());
                }
            }
            if (fileHasUnresolved) {
                NativeFileItem nativeFileItem = ((ProjectBase) file.getProject()).getNativeFileItem(UIDs.get(file));
                if (nativeFileItem != null) {
                    for(IncludePath path : nativeFileItem.getUserIncludePaths()) {
                        System.err.println("\tSearch path "+path.getFSPath().getPath());
                    }
                }
            }
        }
        assertNoExceptions();
        assertFalse("Model has unresolved include directives", hasInvalidFiles);
    }

    protected String download(String urlName, List<String> additionalScripts, Map<String, String> tools) throws IOException {
        String zipName = urlName.substring(urlName.lastIndexOf('/')+1);
        String tarName = zipName.substring(0, zipName.lastIndexOf('.'));
        String packageName = tarName.substring(0, tarName.lastIndexOf('.'));
        File fileDataPath = CndCoreTestUtils.getDownloadBase();
        String dataPath = fileDataPath.getAbsolutePath();
        File localFilesStorage = new File(System.getProperty("user.home"), "cnd-test-files-storage");
        File fileFromStorage = new File(localFilesStorage, zipName);
        File unzipedFileFromStorage = new File(localFilesStorage, tarName);

        File fileCreatedFolder = new File(fileDataPath, packageName);
        String createdFolder = fileCreatedFolder.getAbsolutePath();
        if (!fileCreatedFolder.exists()){
            fileCreatedFolder.mkdirs();
        } else {
            execute(tools, "rm", dataPath, "-rf", packageName);
            fileCreatedFolder.mkdirs();
        }
        if (fileCreatedFolder.list().length == 0){
            if (!new File(fileDataPath, tarName).exists()) {
                if (unzipedFileFromStorage.exists()) {
                    execute(tools, "cp", dataPath, unzipedFileFromStorage.getAbsolutePath(), dataPath);
                } else {
                    if (fileFromStorage.exists()) {
                        execute(tools, "cp", dataPath, fileFromStorage.getAbsolutePath(), dataPath);
                    } else {
                        if (urlName.startsWith("http")) {
                            execute(tools, "wget", dataPath, "-qN", urlName);
                        } else {
                            execute(tools, "cp", dataPath, urlName, dataPath);
                        }
                    }
                    execute(tools, "gzip", dataPath, "-d", zipName);
                }
            }
            execute(tools, "tar", dataPath, "xf", tarName);
            execAdditionalScripts(createdFolder, additionalScripts, tools);
        } else {
            final File configure = new File(fileCreatedFolder, "configure");
            final File makeFile = detectConfigure(createdFolder);
            if (!configure.exists()) {
                if (!makeFile.exists()){
                    execAdditionalScripts(createdFolder, additionalScripts, tools);
                }
            }
        }
        execute(tools, "rm", createdFolder, "-rf", "nbproject");
        return createdFolder;
    }

    private void execute(Map<String, String> tools, String cmd, String folder, String ... arguments){
        String command = tools.get(cmd);
        StringBuilder buf = new StringBuilder();
        for(String arg : arguments) {
            buf.append(' ');
            buf.append(arg);
        }
        if (TRACE) {
            System.err.println(folder+"#"+command+buf.toString());
        }
        NativeProcessBuilder ne = NativeProcessBuilder.newProcessBuilder(ExecutionEnvironmentFactory.getLocal())
        .setWorkingDirectory(folder)
        .setExecutable(command)
        .setArguments(arguments);
        ne.redirectError();
        waitExecution(ne);
    }

    private void execAdditionalScripts(String createdFolder, List<String> additionalScripts, Map<String, String> tools) throws IOException {
        if (additionalScripts != null) {
            for(String s: additionalScripts){
                int i = s.indexOf(' ');
                String command = s.substring(0,i);
                String arguments = s.substring(i+1);
                command = tools.get(command);
                if (TRACE) {
                    System.err.println(createdFolder+"#"+command+" "+arguments);
                }
                //NativeExecutor ne = new NativeExecutor(createdFolder, tools.get(command), arguments, new String[0], command, "run", false, false);
                NativeProcessBuilder ne = NativeProcessBuilder.newProcessBuilder(ExecutionEnvironmentFactory.getLocal())
                .setWorkingDirectory(createdFolder)
                .setCommandLine(command+" "+arguments);
                waitExecution(ne);
            }
        }
    }

    private void waitExecution(NativeProcessBuilder ne){
        try {
            NativeProcess process = ne.call();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            RequestProcessor RP = new RequestProcessor("command", 2);
            RP.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String line = reader.readLine();
                            if (line == null) {
                                break;
                            } else {
                                if (TRACE) {
                                  System.out.println(line);
                                }
                            }
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    } finally {
                        try {
                            reader.close();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            });
            final BufferedReader reader2 = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            RP.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String line = reader2.readLine();
                            if (line == null) {
                                break;
                            } else {
                                System.out.println(line);
                            }
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    } finally {
                        try {
                            reader2.close();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }

            });
            int rc = process.waitFor();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected static boolean hasLogs(File projectDir) {
        File configureLog = new File(projectDir, "configure" + LOG_POSTFIX);
        if (!configureLog.exists()) {
            return false;
        }
        File makeLog = new File(projectDir, "Makefile" + LOG_POSTFIX);
        return makeLog.exists();
    }

    protected static void hackConfigure(File file) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String firstLine = in.readLine();
            in.close();
            BufferedWriter out = Files.newBufferedWriter(file.toPath(), Charset.forName("UTF-8")); //NOI18N
            out.write(firstLine);
            out.newLine();
            out.write("cat \"" + file.getAbsolutePath() + LOG_POSTFIX + "\"");
            out.close();
        } catch (IOException e) {
        }
    }

    protected static void hackMakefile(File file) {
        try {
            BufferedWriter out = Files.newBufferedWriter(file.toPath(), Charset.forName("UTF-8")); //NOI18N
            out.write("all:");
            out.newLine();
            out.write("\tcat \"" + file.getAbsolutePath() + LOG_POSTFIX + "\"");
            out.close();
        } catch (IOException e) {
        }
    }
    
    private static final class MyWizardDescriptor extends WizardDescriptor {
        private final String path;
        private final ExecutionEnvironment fs;
        private final ExecutionEnvironment ee;
        private final CompilerSet def;
        private MyWizardDescriptor(String path, ExecutionEnvironment fs, ExecutionEnvironment ee, CompilerSet def) {
            this.path = path;
            this.fs = fs;
            this.ee = ee; //ExecutionEnvironmentFactory.getLocal();
            this.def = def; //CompilerSetManager.get(ee).getDefaultCompilerSet();
        }
        
        private void init() {
            WizardConstants.PROPERTY_SIMPLE_MODE.put(this, Boolean.TRUE);
            WizardConstants.PROPERTY_NATIVE_PROJ_DIR.put(this, path);
            final FSPath fsPath = new FSPath(FileSystemProvider.getFileSystem(fs), RemoteFileUtil.normalizeAbsolutePath(path, fs));
            WizardConstants.PROPERTY_NATIVE_PROJ_FO.put(this, fsPath.getFileObject());
            WizardConstants.PROPERTY_PROJECT_FOLDER.put(this, fsPath);
            WizardConstants.PROPERTY_TOOLCHAIN.put(this, def);
            WizardConstants.PROPERTY_USE_BUILD_ANALYZER.put(this, true);

            WizardConstants.PROPERTY_HOST_UID.put(this, ExecutionEnvironmentFactory.toUniqueID(ee));
            WizardConstants.PROPERTY_SOURCE_HOST_ENV.put(this, fs);
            WizardConstants.PROPERTY_TOOLCHAIN_DEFAULT.put(this, true);
            PreBuildSupport.PreBuildArtifact scriptArtifact = PreBuildSupport.findArtifactInFolder(fsPath.getFileObject(), fs, def);
            BuildSupport.BuildFile makeArtifact = null;
            if (scriptArtifact != null) {
                WizardConstants.PROPERTY_RUN_CONFIGURE.put(this, Boolean.TRUE);
                FileObject script = scriptArtifact.getScript();
                WizardConstants.PROPERTY_CONFIGURE_RUN_FOLDER.put(this, script.getParent().getPath());
                WizardConstants.PROPERTY_CONFIGURE_SCRIPT_PATH.put(this, script.getPath());
                String args = scriptArtifact.getArguments(ee, def, "");
                WizardConstants.PROPERTY_CONFIGURE_SCRIPT_ARGS.put(this, args);
                String command = scriptArtifact.getCommandLine(args, script.getParent().getPath());
                WizardConstants.PROPERTY_CONFIGURE_COMMAND.put(this, command);
                
                String makefile = script.getParent().getPath()+"/Makefile"; //NOI18N
                makefile = RemoteFileUtil.normalizeAbsolutePath(makefile, fs);
                makeArtifact = BuildSupport.scriptToBuildFile(makefile);
            }
            if (makeArtifact == null) {
                makeArtifact = BuildSupport.findBuildFileInFolder(fsPath.getFileObject(), fs, def);
            }
            if (makeArtifact != null) {
                WizardConstants.PROPERTY_RUN_REBUILD.put(this, Boolean.TRUE);
                WizardConstants.PROPERTY_USER_MAKEFILE_PATH.put(this, makeArtifact.getFile());
                WizardConstants.PROPERTY_WORKING_DIR.put(this, CndPathUtilities.getDirName(makeArtifact.getFile()));
                WizardConstants.PROPERTY_BUILD_COMMAND.put(this, makeArtifact.getBuildCommandLine(null, CndPathUtilities.getDirName(makeArtifact.getFile())));
                WizardConstants.PROPERTY_CLEAN_COMMAND.put(this, makeArtifact.getCleanCommandLine(null, CndPathUtilities.getDirName(makeArtifact.getFile())));
            }
        }
    }
}
