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

package org.netbeans.modules.remote.impl.fileoperations.spi;

import java.io.*;
import java.util.ArrayList;
import org.netbeans.modules.remote.impl.fs.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import junit.framework.Test;
import org.netbeans.api.extexecution.ProcessBuilder;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ShellScriptRunner;
import org.netbeans.modules.nativeexecution.support.NativeTaskExecutorService;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.remote.impl.fileoperations.spi.FileOperationsProvider.FileOperations;
import org.netbeans.modules.remote.impl.fileoperations.spi.FileOperationsProvider.FileProxyO;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 */
public class FileOperationsTestCase extends RemoteFileTestBase {

    private String script;
    private String localScript;
    private String remoteDir;
    private String localDir;
    private String user;
    private String group;
    private FileOperations fileOperations;

    public static Test suite() {
        return RemoteApiTest.createSuite(FileOperationsTestCase.class);
    }

    public FileOperationsTestCase(String testName) {
        super(testName);
    }

    public FileOperationsTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (execEnv != null) {
            user = execEnv.getUser();
            if (HostInfoUtils.getHostInfo(execEnv).getOSFamily() == HostInfo.OSFamily.MACOSX) {
                group = "wheel"; // don't know the reason, but mac isn't supported, so it's mostly for my own convenien
            } else {
                group = execute("groups").split(" ")[0];
            }
            remoteDir = mkTempAndRefreshParent(true);
            ProcessUtils.execute(execEnv, "umask", "0002");
            script = getScript(remoteDir);
            localDir = mkTemp(ExecutionEnvironmentFactory.getLocal(), true);
            localScript = getScript(localDir);
        } else {
            user = "user_1563";
            group = "staff";
        }
        prepareDirectory();
        copyAgent();
        // To make sure that agent is delivered
        fileOperations = FileOperationsProvider.getDefault().getFileOperations(fs);
    }

    private String getScript(String dir) {
        return  "cd " + dir + "\n" +
                "echo \"123\" > just_a_file\n" +
                "echo \"123\" > \"file with a space\"\n" +
                "mkdir -p \"dir with a space\"\n" +
                "mkdir -p dir_1\n" +
                "ln -s just_a_file just_a_link\n" +
                "ln -s dir_1 link_to_dir\n" +
                "ln -s \"file with a space\" link_to_file_with_a_space\n" +
                "ln -s \"file with a space\" \"link with a space to file with a space\"\n" +
                "ln -s "+dir+"/dir_1 link_to_abs_dir\n" +
                "mkfifo fifo\n"+
                "cd dir_1\n"+
                "ln -s .. recursive_link\n" +
                "ln -s ../just_a_file back_link\n" +
                "cd ..\n"+
                "ln -s dir_1/back_link double_link\n" +
                "mkdir -p classes\n" +
                "cd classes\n" +
                "mkdir -p org\n" +
                "";
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (execEnv != null) {
            removeRemoteDirIfNotNull(remoteDir);
        }
        if (localDir != null) {
            CommonTasksSupport.rmDir(ExecutionEnvironmentFactory.getLocal(), localDir, true, new OutputStreamWriter(System.err)).get();
        }
    }

    private void prepareDirectory() throws Exception {
        ShellScriptRunner scriptRunner = new ShellScriptRunner(execEnv, script, new LineProcessor() {
            @Override
            public void processLine(String line) {
                System.err.println(line);
            }
            @Override
            public void reset() {}
            @Override
            public void close() {}
        });
        int rc = scriptRunner.execute();
        assertEquals("Error running script", 0, rc);
        scriptRunner = new ShellScriptRunner(ExecutionEnvironmentFactory.getLocal(), localScript, new LineProcessor() {
            @Override
            public void processLine(String line) {
                System.err.println(line);
            }
            @Override
            public void reset() {}
            @Override
            public void close() {}
        });
        rc = scriptRunner.execute();
        assertEquals("Error running local script", 0, rc);
    }

    @ForAllEnvironments
    public void testFileOperations() throws Exception {
        List<DirEntry> entries = RemoteFileSystemTransport.readDirectory(execEnv, remoteDir).getEntries();
        for(DirEntry entry : entries) {
            String name = entry.getName();
            String path = remoteDir+"/"+name;
            FileProxyO file = FileOperationsProvider.toFileProxy(path);
            assertTrue(fileOperations.exists(file));
            assertEquals(entry.canWrite(), fileOperations.canWrite(file));
            assertEquals(remoteDir, fileOperations.getDir(file));
            assertEquals(name, fileOperations.getName(file));
            assertEquals(path, fileOperations.getPath(file));
            assertEquals(fs.getRoot(), fileOperations.getRoot());
            if (!entry.isLink()) {
                assertEquals("sftp and fileOperations isFile differ for " + file.getPath(), entry.isPlainFile(), fileOperations.isFile(file));
                assertEquals("sftp and fileOperations isDirectory differ for " + file.getPath(), entry.isDirectory(), fileOperations.isDirectory(file));
            }
            File ioFile = new File(localDir+"/"+name);
            fileEquals(ioFile, file, false);
        }
    }
    
    @ForAllEnvironments
    public void testUnexisting() throws Exception {
        // test unexisting file
        String name = "unexisting";
        String path = remoteDir+"/"+name;
        FileProxyO file = FileOperationsProvider.toFileProxy(path);
        File ioFile = new File(localDir+"/"+name);
        fileEquals(ioFile, file, false);
    }

    @ForAllEnvironments
    public void testReadOnlyFile() throws Exception {
        // test unexisting file
        String name = "just_a_file";
        String path = remoteDir+"/"+name;
        FileProxyO file = FileOperationsProvider.toFileProxy(path);
        makeReadOnly(file, name);
        File ioFile = new File(localDir+"/"+name);
        ioFile.setReadOnly();
        fileEquals(ioFile, file, false);
    }

    @ForAllEnvironments
    public void testReadOnlyDir() throws Exception {
        // test unexisting file
        String path = remoteDir;
        FileProxyO file = FileOperationsProvider.toFileProxy(path);
        makeReadOnly(file, path);
        File ioFile = new File(localDir);
        ioFile.setReadOnly();
        fileEquals(ioFile, file, true);
    }
        
    @ForAllEnvironments
    public void testSelfDir() throws Exception {
        // test of self dir
        FileProxyO file = FileOperationsProvider.toFileProxy(remoteDir);
        File ioFile = new File(localDir);
        fileEquals(ioFile, file, true);
    }
    
    @ForAllEnvironments
    public void testRecursiveLink() throws Exception {
        // test of recursive link
        FileProxyO file = FileOperationsProvider.toFileProxy(remoteDir+"/"+"dir_1/recursive_link");
        File ioFile = new File(localDir+"/"+"dir_1/recursive_link");
        fileEquals(ioFile, file, false);
    }
        
    @ForAllEnvironments
    public void testProcessBuilder() throws Exception {
        // test of process builder
        FileProxyO file = FileOperationsProvider.toFileProxy(remoteDir);
        ProcessBuilder pb = fileOperations.createProcessBuilder(file);
        pb.setExecutable("ls");
        pb.setWorkingDirectory(remoteDir);
        final Process process = pb.call();
        Future<String> error = NativeTaskExecutorService.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return ProcessUtils.readProcessErrorLine(process);
                }
            }, "e"); // NOI18N
        Future<String> output = NativeTaskExecutorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return ProcessUtils.readProcessOutputLine(process);
            }
        }, "o"); // NOI18N
        fileOperations.list(file);
        listEquals(message(file, "list"), output.get().split("\n"), fileOperations.list(file));
    }

    @ForAllEnvironments
    public void testNormalizeUnixPath() throws Exception {
        if (!Utilities.isWindows()) {
            //assertEquals("/", fileOperations.normalizeUnixPath(FileOperationsProvider.toFileProxy("")));
            assertEquals("/", fileOperations.normalizeUnixPath(FileOperationsProvider.toFileProxy("/..")));
            assertEquals("/", fileOperations.normalizeUnixPath(FileOperationsProvider.toFileProxy("/../.")));
            assertEquals("/tmp", fileOperations.normalizeUnixPath(FileOperationsProvider.toFileProxy("/../../tmp")));
        }
    }

    @ForAllEnvironments
    public void testEquals() throws Exception {
        FileProxyO file1 = FileOperationsProvider.toFileProxy(remoteDir);
        FileProxyO file2 = FileOperationsProvider.toFileProxy(remoteDir);
        assertEquals(file1, file2);
        FileOperations fileOperations1 = FileOperationsProvider.getDefault().getFileOperations(fs);
        assertEquals(fileOperations, fileOperations1);
    }

    private void makeReadOnly(FileProxyO file, String name) throws IOException, InterruptedException, ExecutionException {
        ProcessBuilder pb = fileOperations.createProcessBuilder(file);
        pb.setExecutable("chmod");
        pb.setWorkingDirectory(remoteDir);
        List<String> list = new ArrayList<>();
        list.add("oag-w");
        list.add(name);
        pb.setArguments(list);
        final Process process = pb.call();
        Future<String> error = NativeTaskExecutorService.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return ProcessUtils.readProcessErrorLine(process);
                }
            }, "e"); // NOI18N
        Future<String> output = NativeTaskExecutorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return ProcessUtils.readProcessOutputLine(process);
            }
        }, "o"); // NOI18N
        process.waitFor();
        if (error.get().length()>0) {
            System.err.println(error.get());
        }
        if (output.get().length()>0) {
            System.err.println(output.get());
        }
//        String pathToRefresh = name;
//        if (pathToRefresh.startsWith("/")) {
//            String parent = PathUtilities.getDirName(pathToRefresh);
//            if (parent != null) {
//                pathToRefresh = parent;
//            }
//        } else {
//            pathToRefresh = file.getPath();
//        }
//        RemoteVcsSupportUtil.refreshFor(fs, pathToRefresh);
    }
    
    private void copyAgent() {
        try {
            String name = Agent.class.getName().replace('.', '/');
            FileObject classFile = FileUtil.createData(rootFO, remoteDir+"/classes/"+name+".class");
            OutputStream outputStream = classFile.getOutputStream();
            FileUtil.copy(Agent.class.getResourceAsStream("Agent.class"), outputStream);
            outputStream.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private Map<String, Object> runAgent(String path) {
        Agent agent = new Agent(execEnv, remoteDir+"/classes");
        return agent.execute(path);
    }
    
    private void fileEquals(File ioFile, FileProxyO file, boolean skipName) {
        try {
            RemoteFileSystemManager.getInstance().getFileSystem(execEnv).setInsideVCS(true);
            if (!Utilities.isWindows()) {
                assertEquals(message(ioFile, file, "exist"), ioFile.exists(), fileOperations.exists(file));
                if (!skipName) {
                    assertEquals(ioFile.getName(), fileOperations.getName(file));
                }
                absPathEquals(ioFile.getAbsolutePath(), fileOperations.getPath(file));
                assertEquals(message(ioFile, file, "canWrite"), ioFile.canWrite(), fileOperations.canWrite(file));
                assertEquals(message(ioFile, file, "isDirectory"), ioFile.isDirectory(), fileOperations.isDirectory(file));
                assertEquals(message(ioFile, file, "isFile"), ioFile.isFile(), fileOperations.isFile(file));
                listEquals(message(ioFile, file, "list"), ioFile.list(), fileOperations.list(file));
                absPathEquals(ioFile.getParent(), fileOperations.getDir(file));
                String normalized = fileOperations.normalizeUnixPath(file);
                absPathEquals(FileUtil.normalizePath(ioFile.getAbsolutePath()), normalized);
                assertEquals(normalized, fileOperations.normalizeUnixPath(FileOperationsProvider.toFileProxy(normalized)));
                if (fileOperations.isDirectory(file)) {
                    File ioFile2 = new File(ioFile, "test/..");
                    ioFile2 = FileUtil.normalizeFile(ioFile2);
                    assertEquals(ioFile2, ioFile);

                    FileProxyO file2 = FileOperationsProvider.toFileProxy(file.getPath() + "/test/..");
                    absPathEquals(ioFile2.getAbsolutePath(), fileOperations.normalizeUnixPath(file2));
                }
            }// else {
            Map<String, Object> agentResults = runAgent(file.getPath());
            assertNotNull(agentResults);
            //if (runAgent != null) {
            ioFile = new MyFile(user, agentResults);
            assertEquals(message(ioFile, file, "exist"), ioFile.exists(), fileOperations.exists(file));
            assertEquals(ioFile.getName(), fileOperations.getName(file));
            assertEquals(ioFile.getAbsolutePath(), fileOperations.getPath(file));
            assertEquals(message(ioFile, file, "canWrite"), ioFile.canWrite(), fileOperations.canWrite(file));
            assertEquals(message(ioFile, file, "isDirectory"), ioFile.isDirectory(), fileOperations.isDirectory(file));
            assertEquals(message(ioFile, file, "isFile"), ioFile.isFile(), fileOperations.isFile(file));
            listEquals(message(ioFile, file, "list"), ioFile.list(), fileOperations.list(file));
            assertEquals(ioFile.getParent(), fileOperations.getDir(file));
            //}
            //}
        } finally {
            RemoteFileSystemManager.getInstance().getFileSystem(execEnv).setInsideVCS(false);
        }
    }

    private String message(File ioFile, FileProxyO file, String method) {
        return new StringBuilder().append(method)
                .append("(")
                .append(ioFile.getAbsolutePath())
                .append(") # ")
                .append(method)
                .append("(")
                .append(file.getPath())
                .append(")")
                .toString();
    }

    private String message(FileProxyO file, String method) {
        return new StringBuilder().append(method)
                .append("(")
                .append(file.getPath())
                .append(") # ")
                .append(method)
                .append("(")
                .append(file.getPath())
                .append(")")
                .toString();
    }
    
    private void absPathEquals(String file, String fo) {
        assertEquals(file == null , fo == null);
        if (file != null) {
            if (file.length() <= localDir.length()) {
                return;
            }
            String fileName = file.substring(localDir.length());
            if (fileName.startsWith("/")) {
                file = remoteDir+fileName;
            } else {
                System.err.println("File:     >"+file+"<");
                System.err.println("File base:>"+localDir+"<");
                System.err.println("FO:       >"+fo+"<");
                System.err.println("FO   base:>"+remoteDir+"<");
                file = remoteDir+"/"+fileName;
            }
            assertEquals(file, fo);
        }
    }
    
    private void listEquals(String message, String[] file, String[] fo) {
        assertEquals(file == null , fo == null);
        if (file != null) {
            assertEquals(file.length, fo.length);
            loop:for (String file1 : file) {
                for (String fo1 : fo) {
                    if (file1.equals(fo1)) {
                        continue loop;
                    }
                }
                assertTrue(message, false);
            }
        }
    }

    private static final class MyFile extends File {
        private final Map<String, Object> map;
        
        private MyFile(String path, Map<String, Object> map) {
            super(path);
            this.map = map;
        }

        @Override
        public boolean canWrite() {
            return (Boolean)map.get("canWrite");
        }

        @Override
        public boolean exists() {
            return (Boolean)map.get("exists");
        }

        @Override
        public String getName() {
            return (String)map.get("getName");
        }

        @Override
        public String getAbsolutePath() {
            return (String)map.get("getAbsolutePath");
        }

        @Override
        public String getParent() {
            return (String)map.get("getParent");
        }

        @Override
        public boolean isDirectory() {
            return (Boolean)map.get("isDirectory");
        }

        @Override
        public boolean isFile() {
            return (Boolean)map.get("isFile");
        }

        @Override
        public String[] list() {
            return (String[])map.get("list");
        }
    }
}
