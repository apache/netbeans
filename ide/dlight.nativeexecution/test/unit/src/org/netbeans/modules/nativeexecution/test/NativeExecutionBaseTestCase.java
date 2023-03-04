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
package org.netbeans.modules.nativeexecution.test;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory.MacroExpander;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ShellScriptRunner;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.netbeans.modules.nativeexecution.support.MiscUtils;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NotImplementedException;

public class NativeExecutionBaseTestCase extends NbTestCase {
    static {
        // Setting netbeans.dirs makes installedFileLocator work properly
        System.setProperty("netbeans.dirs", NbClustersInfoProvider.getClusters());
        System.setProperty("remote.user.password.keep_in_memory", "true"); // NOI18N
        System.setProperty("cnd.mode.unittest", "true");        
    }

    protected static class TestLogHandler extends Handler {

        protected final Logger log;

        @Deprecated
        public TestLogHandler(Logger log) {
            this.log = log;
        }
        
        public static void attach(Logger log) {
            log.addHandler(new TestLogHandler((log)));
        }
    
        
        @Override
        public void publish(LogRecord record) {
            // Log if parent cannot log the message ONLY.
            if (!log.getParent().isLoggable(record.getLevel())) {
                String message;
                Object[] params = record.getParameters();
                if (params == null || params.length == 0) {
                    message = record.getMessage();
                } else {
                    message =  MessageFormat.format(record.getMessage(), record.getParameters());
                }
                System.err.printf("%s: %s\n", record.getLevel(), message); // NOI18N
                if (record.getThrown() != null) {
                    record.getThrown().printStackTrace(System.err);
                }
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

    }

    protected static class DumpingFileChangeListener implements FileChangeListener {
        public static final String FILE_ATTRIBUTE_CHANGED = "fileAttributeChanged";
        public static final String FILE_CHANGED = "fileChanged";
        public static final String FILE_DATA_CREATED = "fileDataCreated";
        public static final String FILE_DELETED = "fileDeleted";
        public static final String FILE_FOLDER_CREATED = "fileFolderCreated";
        public static final String FILE_RENAMED = "fileRenamed";

        private final String listenerName;
        private final String prefixToStrip;
        private final PrintStream out;
        private final boolean checkExpected;

        public DumpingFileChangeListener(String name, String prefixToStrip, PrintStream out, boolean checkExpected) {
            this.listenerName = name;
            this.prefixToStrip = prefixToStrip;
            this.out = out;
            this.checkExpected = checkExpected;
        }

        protected void register(String eventKind, FileEvent fe) {
            String src = stripPrefix(((FileObject) fe.getSource()).getPath());
            String obj = stripPrefix(fe.getFile().getPath());
            String exp = checkExpected ? ("exp=" + Boolean.toString(fe.isExpected())) : "";
            String extra = "";
            if (fe instanceof FileRenameEvent) {
                FileRenameEvent fre = (FileRenameEvent) fe;
                extra = "oldName="+fre.getName()+" oldExt="+fre.getExt();
            }
            out.printf("%-20s: %-20s SRC %-20s OBJ %-20s %s %s\n", listenerName, eventKind, src, obj, exp, extra);
        }

        private String stripPrefix(String path) {
            if (path.startsWith(prefixToStrip)) {
                path = path.substring(prefixToStrip.length());
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
            }
            if (path.length() == 0) {
                path = ".";
            }
            return path;
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            register(FILE_ATTRIBUTE_CHANGED, fe);
        }

        @Override
        public void fileChanged(FileEvent fe) {
            register(FILE_CHANGED, fe);
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            register(FILE_DATA_CREATED, fe);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            register(FILE_DELETED, fe);
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            register(FILE_FOLDER_CREATED, fe);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            register(FILE_RENAMED, fe);
        }
    }
    
    static {
        TestLogHandler.attach(org.netbeans.modules.nativeexecution.support.Logger.getInstance());
 
        // the 3 lines below contain a workaround for some WinXP tests failure
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        tmpDir = FileUtil.normalizeFile(tmpDir.getAbsoluteFile());
        System.setProperty("java.io.tmpdir", tmpDir.getAbsolutePath());
        
        Logger fsLogger = Logger.getLogger("org.netbeans.modules.masterfs.watcher.Watcher");
        fsLogger.setLevel(Level.WARNING);
    }

    private final ExecutionEnvironment testExecutionEnvironment;
    private String remoteTmpDir;
    private Level oldLevel = null;
    
    public NativeExecutionBaseTestCase(String name) {
        super(name);
        System.setProperty("nativeexecution.mode.unittest", "true");
        testExecutionEnvironment = null;
        setupUserDir();
    }

    /**
     * A special constructor for use with NativeExecutionBaseTestSuite
     * @param name
     * @param testExecutionEnvironment
     */
    /*protected - feel free to make it public in the case you REALLY need this */
    protected NativeExecutionBaseTestCase(String name, ExecutionEnvironment testExecutionEnvironment) {
        super(name);
        System.setProperty("nativeexecution.mode.unittest", "true");
        this.testExecutionEnvironment = testExecutionEnvironment;
        assertNotNull(testExecutionEnvironment);
        setupUserDir();        
    }

    @Override
    protected void setUp() throws Exception {
        setLoggers(true);
        setupProperties();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        setLoggers(false);
    }
    
    private void setLoggers(boolean setup) {
        if (setup) {
            if (NativeExecutionTestSupport.getBoolean("execution", "logging.finest")
                 || NativeExecutionTestSupport.getBoolean("execution", getClass().getName() + ".logging.finest")) {        
                oldLevel = org.netbeans.modules.nativeexecution.support.Logger.getInstance().getLevel();
                org.netbeans.modules.nativeexecution.support.Logger.getInstance().setLevel(Level.ALL);
            }
        } else {
            if (oldLevel != null) {
                org.netbeans.modules.nativeexecution.support.Logger.getInstance().setLevel(oldLevel);
            }
        }
    }

    private void setupUserDir() {
        Logger.getLogger("org.netbeans.modules.editor.settings.storage.keybindings.KeyMapsStorage").setLevel(Level.SEVERE);
        File userDir = getUserDir();
        userDir.mkdirs();
        System.setProperty("netbeans.user", userDir.getAbsolutePath());
    }

    protected File getUserDir() {
        Logger.getLogger("org.netbeans.modules.editor.settings.storage.keybindings.KeyMapsStorage").setLevel(Level.SEVERE);
        File dataDir = getDataDir();
        File dataDirParent = dataDir.getParentFile();
        File userDir = new File(dataDirParent, "userdir");
        return userDir;
    }

    private void setupProperties() throws IOException, FormatException {
        RcFile rcFile = NativeExecutionTestSupport.getRcFile();
        String section = getClass().getSimpleName() + ".properties";
        Collection<String> keys = rcFile.getKeys(section);
        for (String key : keys) {
            String value = rcFile.get(section, key);
            System.setProperty(key, value);
        }
    }    

    @Override
    protected int timeOut() {
        return 500000;
    }

    /**
     * Gets execution environment this test was created with.
     * @return
     */
    protected ExecutionEnvironment getTestExecutionEnvironment() {
        return testExecutionEnvironment;
    }

    protected RcFile getLocalRcFile() throws IOException, RcFile.FormatException {
        return NativeExecutionTestSupport.getRcFile();
    }

    protected RcFile getRemoteRcFile() 
            throws IOException, RcFile.FormatException, ConnectException,
            ConnectionManager.CancellationException, InterruptedException, ExecutionException {
        return NativeExecutionTestSupport.getRemoteRcFile(getTestExecutionEnvironment());
    }

    protected String getTestHostName() {
        ExecutionEnvironment env = getTestExecutionEnvironment();
        return (env == null) ? null : env.getHost();
    }

    @Override
    public String getName() {
        String name = super.getName();
        ExecutionEnvironment env = getTestExecutionEnvironment();
        if (env == null) {
            return name;
        } else {
            return String.format("%s [%s]", name, env);
        }
    }
    
    private static boolean ignoreRandomFailures() {
        return Boolean.getBoolean("ignore.random.failures");
    }
    
    private static boolean randomFailsOnly() {
        return Boolean.getBoolean("random.failures.only");
    }
    
    private boolean isRandomFail() {
        if (getClass().isAnnotationPresent(RandomlyFails.class)) {
            return true;
        }
        try {
            if (getClass().getMethod(super.getName()).isAnnotationPresent(RandomlyFails.class)) {
                return true;
            }
        } catch (NoSuchMethodException x) {
            // Specially named methods; let it pass.
        }
        return false;
    }

    @Override
    public boolean canRun() {
        boolean res = true;
        // Random Failures Only mode
        if (randomFailsOnly()) {
            return isRandomFail();
        }
        
        res = res && super.canRun();
        if (!res) {
            return false;
        }
        // Our own check for random failures
        if (ignoreRandomFailures() && getTestExecutionEnvironment() != null) {
            if (isRandomFail()) {
                System.err.println("Skipping " + getClass().getName() + "." + getName());
                return false;
            }
        }
        return res;
    }

    protected String runCommand(String command, String... args) throws Exception {
        return runCommand(getTestExecutionEnvironment(), command, args);
    }

    protected String runCommand(ExecutionEnvironment env, String command, String... args) throws Exception {
        ProcessUtils.ExitStatus res = ProcessUtils.execute(env, command, args);
        assertTrue("Command failed:" + command + ' ' + stringArrayToString(args), res.isOK());
        return res.getOutputString();
    }

    protected String runCommandInDir(String dir, String command, String... args) throws Exception {
        return runCommandInDir(getTestExecutionEnvironment(), dir, command, args);
        
    }
    
    protected String runCommandInDir(ExecutionEnvironment env, String dir, String command, String... args) throws Exception {
        ProcessUtils.ExitStatus res = ProcessUtils.executeInDir(dir, env, command, args);
        assertTrue("Command \"" + command + ' ' + stringArrayToString(args) +
                "\" in dir " + dir + " failed", res.isOK());
        return res.getOutputString();
    }
    
    /**
     * Creates a directory structure described by parameters
     *
     * @param env execution environment
     * @param baseDir base directory; of not exists, it is created ; if exists,
     * the content is removed
     * @param creationData array of strings, a string per file; a string should
     * have format below:
     * "- plain-filen-name" for plain file
     * "d directory-name" for directory
     * "l link-target link-name" for link
     * "R file-or-dir-name" for removal
     * "T file-name" for touch
     * "M dir-or-file-name new-name" for move
     * @throws Exception
     */
    public static void createDirStructure(ExecutionEnvironment env, String baseDir, String[] creationData) throws Exception {
        createDirStructure(env, baseDir, creationData, true);
    }
    public static void createDirStructure(ExecutionEnvironment env, String baseDir, String[] creationData, boolean cleanOld) throws Exception {
        if (baseDir == null || baseDir.length() == 0 || baseDir.equals("/")) {
            throw new IllegalArgumentException("Illegal base dir: " + baseDir);
        }
        if (HostInfoUtils.getHostInfo(env).getOSFamily() == HostInfo.OSFamily.WINDOWS) {
            baseDir = WindowsSupport.getInstance().convertToCygwinPath(baseDir);
        }
        StringBuilder script = new StringBuilder();
        try {
            script.append("mkdir -p \"").append(baseDir).append("\";\n");
            script.append("cd \"").append(baseDir).append("\";\n");
            if (cleanOld) {
                script.append("rm -rf *").append(";\n");
            }
            Set<String> checkedPaths = new HashSet<>();
            for (String data : creationData) {
                if (data.length() < 3 || data.charAt(1) != ' ') {
                    throw new IllegalArgumentException("wrong format: " + data);
                }
                String[] parts = data.split(" ");
                String path = parts[1];
                int slashPos = path.lastIndexOf('/');
                if (slashPos > 0) {
                    String dir = path.substring(0, slashPos);
                    if (!checkedPaths.contains(dir)) {
                        checkedPaths.add(dir);
                        script.append("mkdir -p \"").append(dir).append("\";\n");
                    }
                }
                switch (data.charAt(0)) {
                    case '-':
                        script.append("touch \"").append(path).append("\";\n");
                        break;
                    case 'd':
                        script.append("mkdir -p \"").append(path).append("\";\n");
                        break;
                    case 'l':
                        String link = parts[2];
                        script.append("ln -s \"").append(path).append("\" \"").append(link).append("\";\n");
                        break;
                    case 'R':
                        script.append("rm -rf \"").append(path).append("\";\n");
                        break;
                    case 'T':
                        script.append("touch \"").append(path).append("\";\n");
                        break;
                    case 'M':
                        String dst = parts[2];
                        script.append("mv \"").append(path).append("\" \"").append(dst).append("\";\n");
                        break;
                    default:
                        throw new IllegalArgumentException("Unexpected 1-st char: " + data);
                }
            }
        } catch (Throwable thr) {
            throw new IllegalArgumentException("Error creating script", thr);
        }
        ProcessUtils.ExitStatus res = ProcessUtils.execute(env, "sh", "-c", script.toString());
        if (res.exitCode != 0) {
            assertTrue("script failed at " + env.getDisplayName() + " rc=" + res.exitCode + " err=" + res.getErrorString(), false);
        } else if (res.getErrorString() != null && res.getErrorString().length() > 0) {
            assertTrue("script failed at " + env.getDisplayName() + " rc=" + res.exitCode + " err=" + res.getErrorString(), false);
        }
    }
    
    private String stringArrayToString(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            sb.append(' ').append(arg);
        }
        return sb.toString();
    }
    
    protected String runScript(String script) throws Exception {
        return runScript(getTestExecutionEnvironment(), script);
    }

    protected String runScript(ExecutionEnvironment env, String script) throws Exception {
        final StringBuilder output = new StringBuilder();        
        ShellScriptRunner scriptRunner = new ShellScriptRunner(env, script, new LineProcessor() {
            @Override
            public void processLine(String line) {
                output.append(line).append('\n');
                //System.err.println(line);
            }
            @Override
            public void reset() {}
            @Override
            public void close() {}
        });
        int rc = scriptRunner.execute();
        assertEquals("Error running script", 0, rc);
        return output.toString();
    }
    
    protected boolean canRead(ExecutionEnvironment env, String path) throws Exception {
        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
        npb.setExecutable("test").setArguments("-r", path);
        return ProcessUtils.execute(npb).isOK();
    }

    protected boolean canWrite(ExecutionEnvironment env, String path) throws Exception {
        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
        npb.setExecutable("test").setArguments("-w", path);
        return ProcessUtils.execute(npb).isOK();
    }

    protected boolean canExecute(ExecutionEnvironment env, String path) throws Exception {
        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
        npb.setExecutable("test").setArguments("-x", path);
        return ProcessUtils.execute(npb).isOK();
    }

    public static void writeFile(File file, CharSequence content) throws IOException {
        Writer writer = new FileWriter(file);
        try {
            writer.write(content.toString());
        } finally {
            writer.close();
        }
    }
    
    protected static void writeFile(FileObject fo, CharSequence content) throws IOException {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(fo.getOutputStream()));
            bw.append(content);
        } finally {
            if (bw != null) {
                bw.close();
            }
        }
    }

    public static void writeFile(File file, List<? extends CharSequence> lines) throws IOException {
        Writer writer = new FileWriter(file);
        try {
            for (CharSequence line : lines) {
                writer.write(line.toString());
                writer.write('\n');
            }
        } finally {
            writer.close();
        }
    }

    public void sortFile(File file) throws IOException {
        List<String> lines = readFileLines(file);
        Collections.sort(lines);
        writeFile(file, lines);
    }
    
    private List<String> readFileLines(File file) throws IOException {
        BufferedReader r = new BufferedReader(new FileReader(file));
        try {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = r.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        } finally {
            if (r != null) { 
                try { 
                    r.close(); 
                } catch (IOException e) {}
            }
        }
    }

    public static String readStream(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        char buf[] = new char[4096];
        int cnt = 0;
        while ((cnt = reader.read(buf)) != -1) {
            String text = String.valueOf(buf, 0, cnt);
            sb.append(text);
        }
        reader.close();
        return sb.toString();
    }

    public static String readFile(File file) throws IOException {
        return readStream(new FileInputStream(file));
    }

    public static String readFile(FileObject fo) throws IOException {
        assertTrue("File " +  fo.getPath() + " does not exist", fo.isValid());
        return readStream(fo.getInputStream());
    }

    /**
     * Removes directory recursively
     * @param dir directory  to remove
     * @return true in the case the directory was removed successfully, otherwise false
     */
    public static boolean removeDirectory(File dir) {
        return removeDirectory(dir, true);
    }

    /**
     * Removes directory content (recursively)
     * @param dir directory  to remove
     * @return true in the case the directory content was removed successfully, otherwise false
     */
    public static boolean removeDirectoryContent(File dir) {
        return removeDirectory(dir, false);
    }

    /**
     * Removes directory recursively
     * @param dir directory  to remove
     * @return true in the case the directory was removed successfully, otherwise false
     */
    private static boolean removeDirectory(File dir, boolean removeItself) {
        boolean success = true;
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                if (!removeDirectory(new File(dir, children[i]), true)) {
                    success = false;
                }
            }
        }
        if (success && removeItself) {
            success = dir.delete();
        }
        return success;
    }
    public static File createTempFile(String prefix, String suffix, boolean directory) throws IOException {
        File tmpFile = File.createTempFile(prefix, suffix);
        if (directory) {
            if (!(tmpFile.delete())) {
                throw new IOException("Could not delete temp file: " + tmpFile.getAbsolutePath()); // NOI18N
            }
            if (!(tmpFile.mkdir())) {
                throw new IOException("Could not create temp directory: " + tmpFile.getAbsolutePath()); // NOI18N
            }
        }
        tmpFile.deleteOnExit();
        return tmpFile;
    }

    protected File getNetBeansDir() throws URISyntaxException {
        return getNetBeansPlatformDir().getParentFile();
    }

    protected File getNetBeansPlatformDir() throws URISyntaxException {
        File result = getIdeUtilJar(). // should be ${NBDIST}/platform/lib/org-openide-util.jar
                getParentFile().  // platform/lib
                getParentFile();  // platform
        return result;
    }

    protected File getIdeUtilJar() throws URISyntaxException  {
        return new File(Lookup.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    }

    protected void copyDirectory(File srcDir, File dstDir) throws IOException {
        assertTrue(srcDir.getPath() + " should exist and be a directory", srcDir.isDirectory());
        if (!dstDir.exists()) {
            dstDir.mkdirs();
        }
        assertTrue("Can't create directory " + dstDir.getAbsolutePath(), dstDir.exists());
        for (File child : srcDir.listFiles()) {
            File dst = new File(dstDir, child.getName());
            if (child.isDirectory()) {
                copyDirectory(child, dst);
            } else {
                Files.copy(child.toPath(), dst.toPath());
            }
        }
    }

    protected static void printFile(File file, String prefix, PrintStream out) throws Exception {
        BufferedReader rdr = new BufferedReader(new FileReader(file));
        try {
            String line;
            while ((line = rdr.readLine()) != null) {
                if (prefix == null) {
                    out.printf("%s\n", line);
                } else {
                    out.printf("%s: %s\n", prefix, line);
                }
            }
        } finally {
            if (rdr != null) {
                rdr.close();                
            }
        }
    }
    
    /** A convenience wrapper for Thread.sleep */
    protected static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected String mkTemp(ExecutionEnvironment execEnv, boolean directory) throws Exception {
        return NativeExecutionTestSupport.mkTemp(execEnv, directory);
    }

    protected String createRemoteTmpDir() throws Exception {
        String dir = getRemoteTmpDir();
        int rc = CommonTasksSupport.mkDir(getTestExecutionEnvironment(), dir, new PrintWriter(System.err)).get().intValue();
        assertEquals("Can not create directory " + dir, 0, rc);
        return dir;
    }

    protected void clearRemoteTmpDir() throws Exception {
        String dir = getRemoteTmpDir();
        int rc = CommonTasksSupport.rmDir(getTestExecutionEnvironment(), dir, true, new PrintWriter(System.err)).get().intValue();
        if (rc != 0) {
            System.err.printf("Can not delete directory %s\n", dir);
        }
    }

    private static final String POSTFIX = System.getProperty("cnd.remote.sync.root.postfix"); //NOI18N

    protected synchronized  String getRemoteTmpDir() {
        if (remoteTmpDir == null) {
            final ExecutionEnvironment local = ExecutionEnvironmentFactory.getLocal();
            MacroExpander expander = MacroExpanderFactory.getExpander(local);
            String id;
            try {
                id = expander.expandPredefinedMacros("${hostname}-${osname}-${platform}${_isa}"); // NOI18N
            } catch (ParseException ex) {
                id = local.getHost();
                Exceptions.printStackTrace(ex);
            }
            remoteTmpDir = "/tmp/" + id + "-" + System.getProperty("user.name") + "-" + getTestExecutionEnvironment().getUser();
            if (POSTFIX != null) {
                remoteTmpDir += '-' + POSTFIX;
            }
        }
        return remoteTmpDir;
    }
    
    protected static void threadsDump(String header, String footer) {
        NativeExecutionTestSupport.threadsDump(header, footer);
    }

    protected static boolean isDebugged() {
        return MiscUtils.isDebugged();
    }    
}
