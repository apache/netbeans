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

package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.RemoteSyncSupport;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.utils.CndFileVisibilityQuery;
import org.netbeans.modules.cnd.discovery.api.DiscoveryUtils;
import org.netbeans.modules.cnd.discovery.api.DriverFactory;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.ItemProperties.LanguageKind;
import org.netbeans.modules.cnd.discovery.api.Progress;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.dwarfdump.source.Artifacts;
import org.netbeans.modules.cnd.dwarfdump.source.CompileLineOrigin;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager.PackageConfiguration;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager.PkgConfig;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.MIMESupport;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Utilities;

/**
 *
 */
public class MakeLogReader {
    private String workingDir;
    private String guessWorkingDir;
    private String baseWorkingDir;
    private final String root;
    private final FileObject logFileObject;
    private List<SourceFileProperties> result;
    private List<String> buildArtifacts;
    private Map<LanguageKind,Map<String,Integer>> buildTools;
    private final PathMap pathMapper;
    private final ProjectProxy project;
    private final CompilerSettings compilerSettings;
    private final RelocatablePathMapper localMapper;
    private final FileSystem fileSystem;
    private final RelocatablePathMapper.FS fs;
    private final Map<String,String> alreadyConverted = new HashMap<String,String>();
    private final Set<String> C_NAMES;
    private final Set<String> CPP_NAMES;
    private final Set<String> FORTRAN_NAMES;
    private boolean isWindows = false;

    public MakeLogReader(FileObject logFileObject, String root, ProjectProxy project, RelocatablePathMapper relocatablePathMapper, FileSystem fileSystem) {
        if (root.length()>0) {
            this.root = CndFileUtils.normalizeAbsolutePath(fileSystem, root);
        } else {
            this.root = root;
        }
        this.logFileObject = logFileObject;
        this.project = project;
        this.pathMapper = getPathMapper(project);
        this.compilerSettings = new CompilerSettings(project);
        this.localMapper = relocatablePathMapper;
        this.fileSystem = fileSystem;
        fs = new FSImpl(fileSystem);
        C_NAMES = DiscoveryUtils.getCompilerNames(project, PredefinedToolKind.CCompiler);
        CPP_NAMES = DiscoveryUtils.getCompilerNames(project, PredefinedToolKind.CCCompiler);
        FORTRAN_NAMES = DiscoveryUtils.getCompilerNames(project, PredefinedToolKind.FortranCompiler);
    }

    private String convertPath(String path){
        if (isPathAbsolute(path)) {
            String originalPath = path;
            String converted = alreadyConverted.get(path);
            if (converted != null) {
                return converted;
            }
            if(pathMapper != null) {
                String local = pathMapper.getLocalPath(path);
                if (local != null) {
                    path = local;
                }
            }
            if (localMapper != null && fileSystem != null) {
                FileObject fo = fileSystem.findResource(path);
                if (fo == null || !fo.isValid()) {
                    RelocatablePathMapper.ResolvedPath resolvedPath = localMapper.getPath(path);
                    if (resolvedPath == null) {
                        if (root != null) {
                            if (localMapper.discover(fs, root, path)) {
                                resolvedPath = localMapper.getPath(path);
                                fo = fileSystem.findResource(resolvedPath.getPath());
                                if (fo != null && fo.isValid()) {
                                    path = fo.getPath();
                                }
                            }
                        }
                    } else {
                        fo = fileSystem.findResource(resolvedPath.getPath());
                        if (fo != null && fo.isValid()) {
                            path = fo.getPath();
                        }
                    }
                } else {
                    RelocatablePathMapper.ResolvedPath resolvedPath = localMapper.getPath(fo.getPath());
                    if (resolvedPath == null) {
                        if (root != null) {
                            RelocatablePathMapper.FS fs = new FSImpl(fileSystem);
                            if (localMapper.discover(fs, root, path)) {
                                resolvedPath = localMapper.getPath(path);
                                fo = fileSystem.findResource(resolvedPath.getPath());
                                if (fo != null && fo.isValid()) {
                                    path = fo.getPath();
                                }
                            }
                        }
                    } else {
                        path = fo.getPath();
                        fo = fileSystem.findResource(resolvedPath.getPath());
                        if (fo != null && fo.isValid()) {
                            path = fo.getPath();
                        }
                    }
                }
            }
            alreadyConverted.put(originalPath, path);
        }
        return path;
    }

    private PathMap getPathMapper(ProjectProxy project) {
        if (project != null) {
            Project p = project.getProject();
            if (p != null) {
                // it won't now return null for local environment
                return RemoteSyncSupport.getPathMap(p);
            }
        }
        return null;
    }

    private ExecutionEnvironment getExecutionEnvironment(MakeConfiguration conf) {
        ExecutionEnvironment env = null;
        if (conf != null) {
            env = conf.getDevelopmentHost().getExecutionEnvironment();
        }
        if (env == null) {
            env = ExecutionEnvironmentFactory.getLocal();
        }
        return env;
    }

    private MakeConfiguration getConfiguration(ProjectProxy project) {
        if (project != null && project.getProject() != null) {
            ConfigurationDescriptorProvider pdp = project.getProject().getLookup().lookup(ConfigurationDescriptorProvider.class);
            if (pdp != null && pdp.gotDescriptor()) {
                MakeConfigurationDescriptor confDescr = pdp.getConfigurationDescriptor();
                if (confDescr != null) {
                    return confDescr.getActiveConfiguration();
                }
            }
        }
        return null;
    }

    private void runImpl(Progress progress, CompileLineStorage storage) {
        if (DwarfSource.LOG.isLoggable(Level.FINE)) {
            DwarfSource.LOG.log(Level.FINE, "LogReader is run for {0}", logFileObject); //NOI18N
        }
        Pattern pattern = Pattern.compile(";|\\|\\||&&"); // ;, ||, && //NOI18N
        result = new ArrayList<SourceFileProperties>();
        buildArtifacts = new ArrayList<String>();
        buildTools = new HashMap<LanguageKind,Map<String,Integer>>();
        buildTools.put(LanguageKind.C, new HashMap<String,Integer>());
        buildTools.put(LanguageKind.CPP, new HashMap<String,Integer>());
        buildTools.put(LanguageKind.Fortran, new HashMap<String,Integer>());
        buildTools.put(LanguageKind.Unknown, new HashMap<String,Integer>());
        if (logFileObject != null && logFileObject.isValid() && logFileObject.canRead()) {
            try {
                MakeConfiguration conf = getConfiguration(this.project);
                ExecutionEnvironment executionEnvironment = getExecutionEnvironment(conf);
                try {
                    HostInfo hostInfo = HostInfoUtils.getHostInfo(executionEnvironment);
                    if (hostInfo.getOSFamily() == HostInfo.OSFamily.WINDOWS) {
                        isWindows = true;
                    }
                } catch (CancellationException ex) {
                    ex.printStackTrace(System.err);
                }
                PkgConfig pkgConfig = PkgConfigManager.getDefault().getPkgConfig(executionEnvironment, conf);
                BufferedReader in = new BufferedReader(new InputStreamReader(logFileObject.getInputStream()));
                long length = logFileObject.getSize();
                long read = 0;
                int done = 0;
                if (length <= 0){
                    progress = null;
                }
                if (progress != null) {
                    progress.start(100);
                }
                int nFoundFiles = 0;
                try {
                    while(true){
                        if (isStoped.cancelled()) {
                            break;
                        }
                        String line = in.readLine();
                        if (line == null){
                            break;
                        }
                        read += line.length()+1;
                        line = line.trim();
                        while (line.endsWith("\\")) { // NOI18N
                            String oneMoreLine = in.readLine();
                            if (oneMoreLine == null) {
                                break;
                            }
                            line = line.substring(0, line.length() - 1) + " " + oneMoreLine.trim(); //NOI18N
                        }
                        line = trimBackApostropheCalls(line, pkgConfig);
                        String[] cmds = pattern.split(line);
                        for (int i = 0; i < cmds.length; i++) {
                            if (parseLine(cmds[i].trim(), storage)){
                                nFoundFiles++;
                            }
                        }
                        if (read*100/length > done && done < 100){
                            done++;
                            if (progress != null) {
                                progress.increment(null);
                            }
                        }
                    }
                } finally {
                    if (progress != null) {
                        progress.done();
                    }
                }
                if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                    DwarfSource.LOG.log(Level.FINE, "Files found: {0}", nFoundFiles); //NOI18N
                    DwarfSource.LOG.log(Level.FINE, "Files included in result: {0}", result.size()); //NOI18N
                }
                in.close();
            } catch (IOException ex) {
                 DwarfSource.LOG.log(Level.INFO, "Cannot read file "+logFileObject, ex); // NOI18N
            }
        }
    }

    public List<SourceFileProperties> getResults(Progress progress, Interrupter isStoped, CompileLineStorage storage) {
        if (result == null) {
            run(isStoped, progress, storage);
        }
        return result;
    }
    public List<String> getArtifacts(Progress progress, Interrupter isStoped, CompileLineStorage storage) {
        if (buildArtifacts == null) {
            run(isStoped, progress, storage);
        }
        return buildArtifacts;
    }
    
    public Map<LanguageKind,Map<String,Integer>> getTools(Progress progress, Interrupter isStoped, CompileLineStorage storage) {
        if (buildTools == null) {
            run(isStoped, progress, storage);
        }
        return buildTools;
    }

    private Interrupter isStoped;
    private void run(Interrupter isStoped, Progress progress, CompileLineStorage storage) {
        this.isStoped = isStoped;
        setWorkingDir(root);
        runImpl(progress, storage);
        if (subFolders != null) {
            subFolders.clear();
            subFolders = null;
            findBase.clear();
            findBase = null;
        }
        this.isStoped = null;
    }


    private final ArrayList<List<String>> makeStack = new ArrayList<List<String>>();

    private int getMakeLevel(String line){
        int i1 = line.indexOf('[');
        if (i1 > 0){
            int i2 = line.indexOf(']');
            if (i2 > i1) {
                String s = line.substring(i1+1, i2);
                try {
                    int res = Integer.parseInt(s);
                    return res;
                } catch (NumberFormatException ex) {

                }
            }
        }
        return -1;
    }

    private void enterMakeStack(String dir, int level){
        if (level < 0) {
            return;
        }
        for(int i = makeStack.size(); i <= level; i++) {
            makeStack.add(new ArrayList<String>());
        }
        List<String> list = makeStack.get(level);
        list.add(dir);
    }

    private boolean leaveMakeStack(String dir, int level) {
        if (level < 0) {
            return false;
        }
        if (makeStack.size() <= level) {
            return false;
        }
        List<String> list = makeStack.get(level);
        for(String s : list) {
            if (s.equals(dir)) {
                list.remove(s);
                return true;
            }
        }
        return false;
    }

    private List<String> getMakeTop(int level){
        ArrayList<String> res = new ArrayList<String>();
        for(int i = Math.min(makeStack.size(), level-1); i >=0; i--){
            List<String> list = makeStack.get(i);
            if (list.size() > 0) {
                if (res.isEmpty()) {
                    res.addAll(list);
                } else {
                    if (list.size() > 1) {
                        res.addAll(list);
                    }
                }
            }
        }
        return res;
    }

    private static final String CURRENT_DIRECTORY = "Current working directory"; //NOI18N
    private static final String ENTERING_DIRECTORY = "Entering directory"; //NOI18N
    private static final String LEAVING_DIRECTORY = "Leaving directory"; //NOI18N
    private static final Pattern MAKE_DIRECTORY = Pattern.compile(".*make(?:\\.exe)?(?:\\[([0-9]+)\\])?: .*`([^']*)'$"); //NOI18N
    private boolean isEntered;
    private final Stack<Integer> relativesLevel = new Stack<Integer>();
    private final Stack<String> relativesTo = new Stack<String>();

    private void popPath() {
        if (relativesTo.size() > 1) {
            relativesTo.pop();
        }
    }

    private String peekPath() {
        if (relativesTo.size() > 1) {
            return relativesTo.peek();
        }
        return root;
    }

    private void popLevel() {
        if (relativesLevel.size() > 1) {
            relativesLevel.pop();
        }
    }

    private Integer peekLevel() {
        if (relativesLevel.size() > 1) {
            return relativesLevel.peek();
        }
        return 0;
    }

    private String convertWindowsRelativePath(String path) {
        if (Utilities.isWindows()) {
            if (path.startsWith("/") || path.startsWith("\\")) { // NOI18N
                if (path.length() > 3 && (path.charAt(2) == '/' || path.charAt(2) == '\\') && Character.isLetter(path.charAt(1))) {
                    // MinGW path:
                    //make[1]: Entering directory `/c/Test/qlife-qt4-0.9/build'
                    path = ""+path.charAt(1)+":"+path.substring(2); // NOI18N
                } else if (path.startsWith("/cygdrive/")) { // NOI18N
                    path = path.substring("/cygdrive/".length()); // NOI18N
                    path = "" + path.charAt(0) + ':' + path.substring(1); // NOI18N
                } else {
                    if (root.length()>1 && root.charAt(1)== ':') {
                        path = root.substring(0,2)+path;
                    }
                }

            }
        }
        return path;
    }

    private boolean checkDirectoryChange(String line) {
        String workDir = null, message = null;

        if (line.startsWith(CURRENT_DIRECTORY)) {
            workDir = convertPath(line.substring(CURRENT_DIRECTORY.length() + 1).trim());
            workDir = convertWindowsRelativePath(workDir);
            if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                message = "**>> by [" + CURRENT_DIRECTORY + "] "; //NOI18N
            }
        } else if (line.contains(ENTERING_DIRECTORY)) {
            String dirMessage = line.substring(line.indexOf(ENTERING_DIRECTORY) + ENTERING_DIRECTORY.length() + 1).trim();
            workDir = convertPath(dirMessage.replaceAll("`|'|\"", "")); //NOI18N
            if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                message = "**>> by [" + ENTERING_DIRECTORY + "] "; //NOI18N
            }
            workDir = convertWindowsRelativePath(workDir);
            baseWorkingDir = workDir;
            enterMakeStack(workDir, getMakeLevel(line));
        } else if (line.contains(LEAVING_DIRECTORY)) {
            String dirMessage = line.substring(line.indexOf(LEAVING_DIRECTORY) + LEAVING_DIRECTORY.length() + 1).trim();
            workDir = convertPath(dirMessage.replaceAll("`|'|\"", "")); //NOI18N
            workDir = convertWindowsRelativePath(workDir);
            if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                message = "**>> by [" + LEAVING_DIRECTORY + "] "; //NOI18N
            }
            int level = getMakeLevel(line);
            if (leaveMakeStack(workDir, level)){
                List<String> paths = getMakeTop(level);
                if (paths.size()== 1) {
                    baseWorkingDir = paths.get(0);
                } else {
                    // TODO: make is performed in several threads
                    // algorithm should have guessing to select needed top of stack
                    //System.err.println("");
                }
            } else {
                // This is root or error
                //System.err.println("");
            }
        } else if (line.startsWith(LABEL_CD)) {
            int end = line.indexOf(MAKE_DELIMITER);
            workDir = convertPath((end == -1 ? line : line.substring(0, end)).substring(LABEL_CD.length()).trim());
            if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                message = "**>> by [ " + LABEL_CD + "] "; //NOI18N
            }
            if (workDir.startsWith("/")){ // NOI18N
                workDir = convertWindowsRelativePath(workDir);
                baseWorkingDir = workDir;
            }
        } else if (line.startsWith("/") && !line.contains(" ")) {  //NOI18N
            workDir = convertPath(line.trim());
            workDir = convertWindowsRelativePath(workDir);
            if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                message = "**>> by [just path string] "; //NOI18N
            }
        } else if (line.contains("make") && line.length() < 2000) { //NOI18N
            Matcher m = MAKE_DIRECTORY.matcher(line);
            boolean found = m.find();
            if (found && m.start() == 0) {
                String levelString = m.group(1);
                int level = levelString == null ? 0 : Integer.parseInt(levelString);
                int baseLavel = peekLevel();
                workDir = m.group(2);
                workDir = convertPath(workDir);
                workDir = convertWindowsRelativePath(workDir);
                if (level > baseLavel) {
                    isEntered = true;
                    relativesLevel.push(level);
                    isEntered = true;
                } else if (level == baseLavel) {
                    isEntered = !this.isEntered;
                } else {
                    isEntered = false;
                    popLevel();
                }
                if (isEntered) {
                    relativesTo.push(workDir);
                } else {
                    popPath();
                    workDir = peekPath();
                }
            }
        }

        if (workDir == null || workDir.length() == 0) {
            return false;
        }

        if (Utilities.isWindows() && CndFileUtils.isLocalFileSystem(fileSystem) && workDir.startsWith("/cygdrive/") && workDir.length()>11){ // NOI18N
            workDir = ""+workDir.charAt(10)+":"+workDir.substring(11); // NOI18N
        }

        if (workDir.charAt(0) == '/' || workDir.charAt(0) == '\\' || (workDir.length() > 1 && workDir.charAt(1) == ':')) {
            if ((fs.exists(workDir))) {
                if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                    DwarfSource.LOG.log(Level.FINE,message);
                }
                setWorkingDir(workDir);
                return true;
            } else {
                String netFile = fixNetHost(workDir);
                if (netFile != null) {
                    setWorkingDir(netFile);
                }
            }
        }
        String dir = workingDir + CndFileUtils.getFileSeparatorChar(fileSystem) + workDir;
        if (fs.exists(dir)) {
            if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                DwarfSource.LOG.log(Level.FINE,message);
            }
            setWorkingDir(dir);
            return true;
        }
        if (Utilities.isWindows() && CndFileUtils.isLocalFileSystem(fileSystem) && workDir.length()>3 &&
            workDir.charAt(0)=='/' &&
            workDir.charAt(2)=='/'){
            String d = ""+workDir.charAt(1)+":"+workDir.substring(2); // NOI18N
            if (fs.exists(d)) {
                if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                    DwarfSource.LOG.log(Level.FINE,message);
                }
                setWorkingDir(d);
                return true;
            }
        }
        if (baseWorkingDir != null) {
            dir = baseWorkingDir + CndFileUtils.getFileSeparatorChar(fileSystem) + workDir;
            if (fs.exists(dir)) {
                if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                    DwarfSource.LOG.log(Level.FINE,message);
                }
                setWorkingDir(dir);
                return true;
            }
        }
        return false;
    }

    private String fixNetHost(String dir) {
        if (root.startsWith("/net/")) { // NOI18N
            int i = root.indexOf('/', 5);
            if (i > 0) {
                String localPath = root.substring(i);
                String prefix = root.substring(0,i);
                if (dir.startsWith(localPath)) {
                    String netFile = prefix + dir;
                    if (fs.exists(netFile)) {
                        return netFile;
                    }
                }
            }
        }
        return null;
    }

    /*package-local*/ enum CompilerType {
        CPP, C, FORTRAN, UNKNOWN;
    };

    /*package-local*/ static class LineInfo {
        public String compileLine;
        public String compiler;
        public CompilerType compilerType = CompilerType.UNKNOWN;

        LineInfo(String line) {
            compileLine = line;
        }

        ItemProperties.LanguageKind getLanguage() {
            switch (compilerType) {
                case C:
                    return ItemProperties.LanguageKind.C;
                case CPP:
                    return ItemProperties.LanguageKind.CPP;
                case FORTRAN:
                    return ItemProperties.LanguageKind.Fortran;
                case UNKNOWN:
                default:
                    return ItemProperties.LanguageKind.Unknown;
            }
        }
    }

    private static final String LABEL_CD        = "cd "; //NOI18N
    private static final String MAKE_DELIMITER  = ";"; //NOI18N

    private String[] findCompiler(String line, Set<String> patterns, boolean checkExe){
        for(String pattern : patterns)    {
            int[] find = find(line, pattern);
            if (find != null) {
                return new String[]{pattern,line.substring(find[2])};
            }
            if (checkExe) {
                find = find(line, pattern+".exe"); //NOI18N
                if (find != null) {
                    return new String[]{pattern,line.substring(find[2])};
                }
            }
        }
        return null;
    }

    private int[] find(String line, String pattern) {
        int fromIndex = 0;
        while(true) {
            int start = line.indexOf(pattern, fromIndex);
            if (start < 0) {
                return null;
            }
            fromIndex = start + 1;
            char prev = ' ';
            if (start > 0) {
                prev = line.charAt(start-1);
            }
            if (prev == ' ' || prev == '\t' || prev == '/' || prev == '\\' || prev == '"') {
                if (start + pattern.length() >= line.length()) {
                    continue;
                }
                char next = line.charAt(start+pattern.length());
                if (next == ' ' || next == '\t') {
                    int binaryStart = start;
                    if (prev == '/' || prev == '\\') {
                        char first = prev;
                        for(int i = start - 2; i >= 0; i--) {
                            char c = line.charAt(i);
                            if (c == ' ' || c == '\t') {
                                break;
                            }
                            binaryStart = i;
                            first = c;
                        }
                        if (first == '-') {
                            continue;
                        }
                    }
                    int end = start + pattern.length();
                    return new int[]{start,end, binaryStart};
                } else if (next == '"' && prev == '"') {  //NOI18N
                    int end = start + pattern.length();
                    return new int[]{start,end, end+1};
                }
            }
        }
    }

    /*package-local*/ LineInfo testCompilerInvocation(String line) {
        LineInfo li = new LineInfo(line);
        String[] compiler = findCompiler(line, C_NAMES, isWindows);
        if (compiler != null) {
            li.compilerType = CompilerType.C;
            li.compiler = compiler[0];
            li.compileLine = compiler[1];
        } else {
            compiler = findCompiler(line, CPP_NAMES, isWindows);
            if (compiler != null) {
                li.compilerType = CompilerType.CPP;
                li.compiler = compiler[0];
                li.compileLine = compiler[1];
            } else {
                compiler = findCompiler(line, FORTRAN_NAMES, isWindows);
                if (compiler != null) {
                    li.compilerType = CompilerType.FORTRAN;
                    li.compiler = compiler[0];
                    li.compileLine = compiler[1];
                }
            }
        }
        return li;
    }

    private void setWorkingDir(String workingDir) {
        if (DwarfSource.LOG.isLoggable(Level.FINE)) {
            DwarfSource.LOG.log(Level.FINE, "**>> new working dir: {0}", workingDir);
        }
        this.workingDir = CndFileUtils.normalizeAbsolutePath(fileSystem, workingDir);
    }

    private void setGuessWorkingDir(String workingDir) {
        if (DwarfSource.LOG.isLoggable(Level.FINE)) {
            DwarfSource.LOG.log(Level.FINE, "**>> alternative guess working dir: {0}", workingDir);
        }
        this.guessWorkingDir = CndFileUtils.normalizeAbsolutePath(fileSystem, workingDir);
    }

    private boolean parseLine(String line, CompileLineStorage storage){
       if (checkDirectoryChange(line)) {
           return false;
       }
       if (workingDir == null) {
           return false;
       }
       //if (!workingDir.startsWith(root)){
       //    return false;
       //}
       LineInfo li = testCompilerInvocation(line);
       if (li.compilerType != CompilerType.UNKNOWN) {
           gatherLine(li, storage);
           return true;
       }
       return false;
    }

    private static final String PKG_CONFIG_PATTERN = "pkg-config "; //NOI18N
    private static final String ECHO_PATTERN = "echo "; //NOI18N
    private static final String CYGPATH_PATTERN = "cygpath "; //NOI18N
    /*package-local*/ static String trimBackApostropheCalls(String line, PkgConfig pkgConfig) {
        int i = line.indexOf('`'); //NOI18N
        if (line.lastIndexOf('`') == i) {  //NOI18N // do not trim unclosed `quotes`
            return line;
        }
        if (i < 0 || i == line.length() - 1) {
            return line;
        } else {
            StringBuilder out = new StringBuilder();
            if (i > 0) {
                out.append(line.substring(0, i));
            }
            line = line.substring(i+1);
            int j = line.indexOf('`'); //NOI18N
            if (j < 0) {
                return line;
            }
            String pkg = line.substring(0,j);
            if (pkg.startsWith(PKG_CONFIG_PATTERN)) { //NOI18N
                pkg = pkg.substring(PKG_CONFIG_PATTERN.length());
                StringTokenizer st = new StringTokenizer(pkg);
                boolean readFlags = false;
                String findPkg = null;
                while(st.hasMoreTokens()) {
                    String aPkg = st.nextToken();
                    if (aPkg.equals("--cflags")) { //NOI18N
                        readFlags = true;
                        continue;
                    }
                    if (aPkg.startsWith("-")) { //NOI18N
                        readFlags = false;
                        continue;
                    }
                    findPkg = aPkg;
                }
                if (readFlags && pkgConfig != null && findPkg != null) {
                    PackageConfiguration pc = pkgConfig.getPkgConfig(findPkg);
                    if (pc != null) {
                        for(String p : pc.getIncludePaths()){
                            out.append(" -I").append(p); //NOI18N
                        }
                        for(String p : pc.getMacros()){
                            out.append(" -D").append(p); //NOI18N
                        }
                        out.append(" "); //NOI18N
                    }
                }
            } else if (pkg.startsWith(CYGPATH_PATTERN)) {
                pkg = pkg.substring(CYGPATH_PATTERN.length());
                int start = 0;
                for(int i1 = 0; i1 < pkg.length(); i1++) {
                    char c = pkg.charAt(i1);
                    if (c == ' ' || c == '\t') {
                        start = i1;
                        if (i1 + 1 < pkg.length() && pkg.charAt(i1 + 1) != '-') {
                            break;
                        }
                    }
                }
                pkg = pkg.substring(start).trim();
                if (pkg.startsWith("'") && pkg.endsWith("'")) { //NOI18N
                    out.append(pkg.substring(1, pkg.length()-1));
                } else {
                    out.append(pkg);
                }
            } else if (pkg.startsWith(ECHO_PATTERN)) {
                pkg = pkg.substring(ECHO_PATTERN.length());
                if (pkg.startsWith("'") && pkg.endsWith("'")) { //NOI18N
                    out.append(pkg.substring(1, pkg.length()-1));
                } else {
                    StringTokenizer st = new StringTokenizer(pkg);
                    if (st.hasMoreTokens()) {
                        out.append(st.nextToken());
                    }
                }
            } else if (pkg.contains(ECHO_PATTERN)) {
                pkg = pkg.substring(pkg.indexOf(ECHO_PATTERN)+ECHO_PATTERN.length());
                if (pkg.startsWith("'") && pkg.endsWith("'")) { //NOI18N
                    out.append(pkg.substring(1, pkg.length()-1)); //NOI18N
                } else {
                    StringTokenizer st = new StringTokenizer(pkg);
                    if (st.hasMoreTokens()) {
                        out.append(st.nextToken());
                    }
                }
            }
            out.append(line.substring(j+1));
            return trimBackApostropheCalls(out.toString(), pkgConfig);
        }
    }

    // boost
    // #./b2 -a -d+2
    // prints:
    // gcc.compile.c++ bin.v2/libs/graph/build/gcc-4.5.2/release/threading-multi/read_graphviz_new.o
    //
    //     "g++"  -ftemplate-depth-128 -O3 -finline-functions -Wno-inline -Wall -pthreads -fPIC  -DBOOST_ALL_NO_LIB=1 -DBOOST_GRAPH_DYN_LINK=1 -DNDEBUG  -I"." -I"libs/graph/src" -c -o "bin.v2/libs/graph/build/gcc-4.5.2/release/threading-multi/read_graphviz_new.o" "libs/graph/src/read_graphviz_new.cpp"

    private void gatherLine(LineInfo li, CompileLineStorage storage) {
        String line = li.compileLine;
        Artifacts artifacts = compilerSettings.getDriver().gatherCompilerLine(line, CompileLineOrigin.BuildLog, li.compilerType == CompilerType.CPP);
        for(String what : artifacts.getInput()) {
            if (what == null){
                continue;
            }
            if (what.endsWith(".s") || what.endsWith(".S")) {  //NOI18N
                // It seems assembler file was compiled by C compiler.
                // Exclude assembler files from C/C++ code model.
                continue;
            }
            String file;
            boolean isRelative = true;
            if (isPathAbsolute(what)){
                what = convertWindowsRelativePath(what);
                isRelative = false;
                file = what;
            } else {
                file = workingDir+"/"+what;  //NOI18N
            }
            List<String> userIncludesCached = new ArrayList<String>(artifacts.getUserIncludes().size());
            for(String s : artifacts.getUserIncludes()){
                s = convertWindowsRelativePath(s);
                userIncludesCached.add(PathCache.getString(s));
            }
            List<String> userFilesCached = new ArrayList<String>(artifacts.getUserFiles().size());
            for(String s : artifacts.getUserFiles()){
                userFilesCached.add(PathCache.getString(s));
            }
            Map<String, String> userMacrosCached = new HashMap<String, String>(artifacts.getUserMacros().size());
            for(Map.Entry<String,String> e : artifacts.getUserMacros().entrySet()){
                if (e.getValue() == null) {
                    userMacrosCached.put(PathCache.getString(e.getKey()), null);
                } else {
                    userMacrosCached.put(PathCache.getString(e.getKey()), PathCache.getString(e.getValue()));
                }
            }
            if (fs.exists(file) /*&& isData*/) {
                if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                    DwarfSource.LOG.log(Level.FINE, "**** Gotcha: {0}", file);
                }
                result.add(new CommandLineSource(li, artifacts, workingDir, convertSymbolicLink(what), userIncludesCached, userFilesCached, userMacrosCached, storage));
                continue;
            }
            if (!isRelative) {
                file = convertPath(what);
                if (!file.equals(what)) {
                    what = file;
                    if (fs.exists(file) /*&& isData*/) {
                        if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                            DwarfSource.LOG.log(Level.FINE, "**** Gotcha: {0}", file);
                        }
                        result.add(new CommandLineSource(li, artifacts, workingDir, convertSymbolicLink(what), userIncludesCached, userFilesCached, userMacrosCached, storage));
                        continue;
                    }
                }
            }

            if (guessWorkingDir != null && !what.startsWith("/")) { //NOI18N
                String f = guessWorkingDir+"/"+what;  //NOI18N
                if (fs.exists(f)) {
                    if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                        DwarfSource.LOG.log(Level.FINE, "**** Gotcha guess: {0}", f);
                    }
                    result.add(new CommandLineSource(li, artifacts, guessWorkingDir, convertSymbolicLink(what), userIncludesCached, userFilesCached, userMacrosCached, storage));
                    continue;
                }
            }
            if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                DwarfSource.LOG.log(Level.FINE, "**** Not found {0}", file); //NOI18N
            }
            if (!what.startsWith("/") && artifacts.getUserIncludes().size()+artifacts.getUserMacros().size() > 0){  //NOI18N
                List<String> res = findFiles(what);
                if (res == null || res.isEmpty()) {
                    if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                        DwarfSource.LOG.log(Level.FINE, "** And there is no such file under root");
                    }
                } else {
                    if (res.size() == 1) {
                        result.add(new CommandLineSource(li, artifacts, res.get(0), convertSymbolicLink(what), userIncludesCached, userFilesCached, userMacrosCached, storage));
                        if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                            DwarfSource.LOG.log(Level.FINE, "** Gotcha: {0}{1}{2}", new Object[]{res.get(0), "/", what});
                        }
                        // kinda adventure but it works
                        setGuessWorkingDir(res.get(0));
                        continue;
                    } else {
                        if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                            DwarfSource.LOG.log(Level.FINE, "**There are several candidates and I'm not clever enough yet to find correct one.");
                        }
                    }
                }
                if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                    DwarfSource.LOG.log(Level.FINE, "{0} [{1}]", new Object[]{line.length() > 120 ? line.substring(0,117) + ">>>" : line, what}); //NOI18N
                }
            }
        }
        if (artifacts.getOutput() != null) {
            String what = artifacts.getOutput();
            String baseName = CndPathUtilities.getBaseName(what);
            if (!(baseName.endsWith(".exe") || !baseName.contains("."))) { //NOI18N
                return;
            }
            String file;
            boolean isRelative = true;
            if (isPathAbsolute(what)){
                what = convertWindowsRelativePath(what);
                isRelative = false;
                file = what;
            } else {
                file = workingDir+"/"+what;  //NOI18N
            }
            if (fs.exists(file) /*&& isData*/) {
                if (!buildArtifacts.contains(file)) {
                    buildArtifacts.add(file);
                }
            } else if (!isRelative) {
                file = convertPath(what);
                if (!file.equals(what)) {
                    if (fs.exists(file) /*&& isData*/) {
                        if (!buildArtifacts.contains(file)) {
                            buildArtifacts.add(file);
                        }
                    }
                }
            }
        }
    }

    private String convertSymbolicLink(String fullName) {
        if (project.resolveSymbolicLinks()) {
            String resolvedLink = DiscoveryUtils.resolveSymbolicLink(fileSystem, fullName);
            if (resolvedLink != null) {
                fullName = resolvedLink;
            }
        }
        return fullName;
    }

    //copy of CndPathUtilities.isPathAbsolute(CharSequence)
    // except checking on windows
    private boolean isPathAbsolute(CharSequence path) {
        if (path == null || path.length() == 0) {
            return false;
        } else if (path.charAt(0) == '/') {
            return true;
        } else if (path.charAt(0) == '\\') {
            return true;
        } else if (CharSequenceUtils.indexOf(path, ':') == 1) {
            if (path.length()==2) {
                return false;
            } else if (path.charAt(2) == '\\' || path.charAt(2) == '/') {
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    static ItemProperties.LanguageKind detectLanguage(LineInfo li, Artifacts artifacts, String sourcePath) {
        ItemProperties.LanguageKind language = li.getLanguage();
        if (artifacts.getLanguageArtifacts().contains("c")) { // NOI18N
            language = ItemProperties.LanguageKind.C;
        } else if (artifacts.getLanguageArtifacts().contains("c++")) { // NOI18N
            language = ItemProperties.LanguageKind.CPP;
        } else {
            if (language == LanguageKind.Unknown || "cl".equals(li.compiler)) { // NOI18N
                String mime =MIMESupport.getKnownSourceFileMIMETypeByExtension(sourcePath);
                if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mime)) {
                    if (li.getLanguage() != ItemProperties.LanguageKind.CPP) {
                        language = ItemProperties.LanguageKind.CPP;
                    }
                } else if (MIMENames.C_MIME_TYPE.equals(mime)) {
                    if (li.getLanguage() != ItemProperties.LanguageKind.C) {
                        language = ItemProperties.LanguageKind.C;
                    }
                }
            } else if (language == LanguageKind.C && !li.compiler.equals("cc")) { // NOI18N
                String mime =MIMESupport.getKnownSourceFileMIMETypeByExtension(sourcePath);
                if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mime)) {
                    language = ItemProperties.LanguageKind.CPP;
                }
            }
        }
        return language;
    }

    class CommandLineSource extends RelocatableImpl implements SourceFileProperties {

        private String sourceName;
        private final String compiler;
        private final ItemProperties.LanguageKind language;
        private ItemProperties.LanguageStandard standard = LanguageStandard.Unknown;
        private final List<String> systemIncludes = Collections.<String>emptyList();
        private final Map<String, String> userMacros;
        private final List<String> undefinedMacros;
        private final Map<String, String> systemMacros = Collections.<String, String>emptyMap();
        private final CompileLineStorage storage;
        private int handler = -1;
        private final String importantFlags;

        CommandLineSource(LineInfo li, Artifacts artifacts, String compilePath, String sourcePath,
                List<String> userIncludes, List<String> userFiles, Map<String, String> userMacros, CompileLineStorage storage) {
            language = detectLanguage(li, artifacts, sourcePath);
            standard = DriverFactory.getLanguageStandard(standard, artifacts);
            this.compiler = li.compiler;
            this.compilePath =compilePath;
            sourceName = sourcePath;
            if (CndPathUtilities.isPathAbsolute(sourceName)){
                fullName = sourceName;
                sourceName = DiscoveryUtils.getRelativePath(compilePath, sourceName);
            } else {
                fullName = compilePath+"/"+sourceName; //NOI18N
            }
            fullName = convertSymbolicLink(fullName);
            fullName = CndFileUtils.normalizeAbsolutePath(fileSystem, fullName);
            fullName = PathCache.getString(fullName);
            this.userIncludes = userIncludes;
            this.userFiles = userFiles;
            this.userMacros = userMacros;
            this.undefinedMacros = artifacts.getUserUndefinedMacros();
            this.storage = storage;
            if (storage != null) {
                handler = storage.putCompileLine(normalizeCompileLine(li.compileLine));
            }
            this.importantFlags = DriverFactory.importantFlagsToString(artifacts);;
        }

        private String normalizeCompileLine(String line) {
            List<String> args = MakeLogReader.this.compilerSettings.getDriver().splitCommandLine(line, CompileLineOrigin.BuildLog);
            StringBuilder buf = new StringBuilder();
            for (String s : args) {
                if (buf.length() > 0) {
                    buf.append(' ');
                }
                boolean isQuote = false;
                if (s.startsWith("'") && s.endsWith("'") || // NOI18N
                        s.startsWith("\"") && s.endsWith("\"")) { // NOI18N
                    if (s.length() >= 2) {
                        s = s.substring(1, s.length() - 1);
                        isQuote = true;
                    }
                }
                if (s.startsWith("-D")) { // NOI18N
                    String macro = s.substring(2);
                    macro = DriverFactory.removeQuotes(macro);
                    int i = macro.indexOf('=');
                    if (i > 0) {
                        String value = macro.substring(i + 1).trim();
                        value = DriverFactory.normalizeDefineOption(value, CompileLineOrigin.BuildLog, isQuote);
                        String key = DriverFactory.removeEscape(macro.substring(0, i));
                        s = "-D"+key+"="+value; // NOI18N
                    } else {
                        String key = DriverFactory.removeEscape(macro);
                        s = "-D"+key; // NOI18N
                    }
                }
                String s2 = CndPathUtilities.quoteIfNecessary(s);
                if (s.equals(s2)) {
                    if (s.indexOf('"') > 0) { // NOI18N
                        int j = s.indexOf("\\\""); // NOI18N
                        if (j < 0) {
                            s = s.replace("\"", "\\\""); // NOI18N
                        }
                    }
                } else {
                    s = s2;
                }
                buf.append(s);
            }
            
            return buf.toString();
        }
        
        @Override
        public String getCompilePath() {
            return compilePath;
        }

        @Override
        public String getItemPath() {
            return fullName;
        }


        @Override
        public String getCompileLine() {
            if (storage != null && handler != -1) {
                return storage.getCompileLine(handler);
            }
            return null;
        }

        @Override
        public String getItemName() {
            return sourceName;
        }

        @Override
        public List<String> getUserInludePaths() {
            return userIncludes;
        }

        @Override
        public List<String> getUserInludeFiles() {
            return userFiles;
        }


        @Override
        public List<String> getSystemInludePaths() {
            return systemIncludes;
        }

        public Set<String> getIncludedFiles() {
            return includedFiles;
        }

        @Override
        public Map<String, String> getUserMacros() {
            return userMacros;
        }

        @Override
        public List<String> getUndefinedMacros() {
            return undefinedMacros;
        }

        @Override
        public Map<String, String> getSystemMacros() {
            return systemMacros;
        }

        @Override
        public ItemProperties.LanguageKind getLanguageKind() {
            return language;
        }

        @Override
        public String getCompilerName() {
            return compiler;
        }

        @Override
        public LanguageStandard getLanguageStandard() {
            return standard;
        }

        @Override
        public String getImportantFlags() {
            return importantFlags;
        }
    }

    private List<String> getFiles(String name){
        getSubfolders();
        return findBase.get(name);
    }

    private List<String> findFiles(String relativePath) {
        relativePath = relativePath.replace('\\', '/');
        int i = relativePath.lastIndexOf('/');
        String name;
        String relativeFolder = null;
        if (i > 0) {
            name = relativePath.substring(i+1);
            relativeFolder = relativePath.substring(0,i);
        } else {
            name = relativePath;
        }
        String subFolder = null;
        if (relativeFolder != null) {
            int j = relativeFolder.lastIndexOf("../"); // NOI18N
            if (j >= 0) {
                subFolder = relativePath.substring(j+2);
            }
        }
        List<String> files = getFiles(name);
        if (files != null) {
            List<String> res = new ArrayList<String>(files.size());
            for(String s : files) {
                if (relativeFolder == null) {
                    res.add(s);
                    if (res.size() > 1) {
                        return res;
                    }
                } else {
                    if (subFolder == null) {
                        String path = s;
                        if (path.endsWith(relativeFolder) && path.length() > relativeFolder.length() + 1) {
                            path = path.substring(0,path.length()-relativeFolder.length()-1);
                            res.add(path);
                            if (res.size() > 1) {
                                return res;
                            }
                        }
                    } else {
                        for(String sub : getSubfolders()) {
                            String pathCandidate = normalizeFile(sub + "/" + relativePath); // NOI18N
                            int j = pathCandidate.lastIndexOf('/');
                            if (j > 0) {
                                 pathCandidate = pathCandidate.substring(0,j);
                                if (subFolders.contains(pathCandidate)){
                                    res.add(sub);
                                    if (res.size() > 1) {
                                        return res;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return res;
        }
        return null;
    }

    private String normalizeFile(String path) {
        path = path.replace("/./", "/"); // NOI18N
        while (true) {
            int i = path.indexOf("/../"); // NOI18N
            if (i < 0) {
                break;
            }
            int prev = -1;
            for (int j = i - 1; j >= 0; j--) {
                if (path.charAt(j) == '/') {
                    prev = j;
                    break;
                }
            }
            if (prev == -1) {
                break;
            }
            path = path.substring(0, prev)+path.substring(i+3);
        }
        return path;
    }

    private Set<String> getSubfolders(){
        if (subFolders == null){
            subFolders = new HashSet<String>();
            findBase = new HashMap<String,List<String>>();
            FileObject rootFO = fileSystem.findResource(root);
            gatherSubFolders(rootFO, new LinkedList<String>());
        }
        return subFolders;
    }
    private HashSet<String> subFolders;
    private Map<String,List<String>> findBase;

    private void gatherSubFolders(FileObject d, LinkedList<String> antiLoop){
        if (d != null && d.isValid() && d.isFolder() && d.canRead()){
            if (isStoped.cancelled()) {
                return;
            }
            if (CndPathUtilities.isIgnoredFolder(d.getNameExt())){
                return;
            }
            String canPath;
            try {
                canPath = CndFileUtils.getCanonicalPath(d);
            } catch (IOException ex) {
                return;
            }
            if (!antiLoop.contains(canPath)){
                antiLoop.addLast(canPath);
                subFolders.add(d.getPath().replace('\\', '/'));
                FileObject[] ff = d.getChildren();
                if (ff != null) {
                    for (int i = 0; i < ff.length; i++) {
                        if (isStoped.cancelled()) {
                            break;
                        }
                        if (ff[i].isFolder()) {
                            gatherSubFolders(ff[i], antiLoop);
                        } else if (ff[i].isData()) {
                            if (CndFileVisibilityQuery.getDefault().isIgnored(ff[i].getNameExt())) {
                                continue;
                            }
                            List<String> l = findBase.get(ff[i].getNameExt());
                            if (l==null){
                                l = new ArrayList<String>();
                                findBase.put(ff[i].getNameExt(),l);
                            }
                            l.add(d.getPath().replace('\\', '/'));
                        }
                    }
                }
                antiLoop.removeLast();
            }
        }
    }

}
