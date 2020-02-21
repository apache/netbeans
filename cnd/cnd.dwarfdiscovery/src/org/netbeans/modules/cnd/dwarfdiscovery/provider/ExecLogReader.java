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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.RemoteSyncSupport;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.utils.ImportUtils;
import org.netbeans.modules.cnd.discovery.api.DiscoveryUtils;
import org.netbeans.modules.cnd.discovery.api.DriverFactory;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.ItemProperties.LanguageKind;
import org.netbeans.modules.cnd.discovery.api.Progress;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.dwarfdump.source.Artifacts;
import org.netbeans.modules.cnd.dwarfdump.source.CompileLineOrigin;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.MIMESupport;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Utilities;

/**
 *
 */
public final class ExecLogReader {
    private static final String CYG_DRIVE = "/cygdrive/"; // NOI18N

    private final String root;
    private final FileObject logFileObject;
    private List<SourceFileProperties> result;
    private List<String> buildArtifacts;
    private Map<LanguageKind,Map<String,Integer>> buildTools;
    private final ProjectProxy project;
    private final PathMap pathMapper;
    private final RelocatablePathMapper localMapper;
    private final FileSystem fileSystem;
    private final RelocatablePathMapper.FS fs;
    private final CompilerSettings compilerSettings;
    private final Set<String> C_NAMES;
    private final Set<String> CPP_NAMES;
    private final Set<String> FORTRAN_NAMES;
    private final Set<String> LIBRARIES_NAMES;
    private int logType = 0; // 0 - not inited, 1 - exec log, 2 - json file

    public ExecLogReader(FileObject logFileObject, String root, ProjectProxy project, RelocatablePathMapper relocatablePathMapper, FileSystem fileSystem) {
        this.logFileObject = logFileObject;
        this.project = project;
        this.pathMapper = getPathMapper(project);
        this.localMapper = relocatablePathMapper;
        this.fileSystem = fileSystem;
        fs = new FSImpl(fileSystem);
        this.compilerSettings = new CompilerSettings(project);
        C_NAMES = DiscoveryUtils.getCompilerNames(project, PredefinedToolKind.CCompiler);
        CPP_NAMES = DiscoveryUtils.getCompilerNames(project, PredefinedToolKind.CCCompiler);
        FORTRAN_NAMES = DiscoveryUtils.getCompilerNames(project, PredefinedToolKind.FortranCompiler);
        LIBRARIES_NAMES = new HashSet<String>();
        LIBRARIES_NAMES.add("ld"); //NOI18N
        LIBRARIES_NAMES.add("ar"); //NOI18N
        if (project != null && root.isEmpty()) {
            String sourceRoot = project.getSourceRoot();
            if (sourceRoot != null && sourceRoot.length() > 1) {
                root = sourceRoot;
            }
        }
        if (root.isEmpty()) {
            String sourceRoot = PathUtilities.getDirName(logFileObject.getPath());
            if (sourceRoot != null && sourceRoot.length() > 1) {
                root = sourceRoot;
            }
        }
        if (root.length() > 0) {
            this.root = CndFileUtils.normalizeAbsolutePath(fileSystem, root);
        } else {
            this.root = root;
        }
    }

    private PathMap getPathMapper(ProjectProxy project) {
        if (project != null) {
            Project p = project.getProject();
            if (p != null) {
                return RemoteSyncSupport.getPathMap(p);
            }
        }
        return null;
    }

    static boolean isSupportedLog(FileObject file) {
        if (file == null || !file.isValid() || !file.isData() || !file.canRead()) {
            return false;
        }
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(file.getInputStream()));            
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (line.startsWith("called:")) { //NOI18N
                    return true;
                } else if (line.trim().startsWith("[") || line.trim().startsWith("{")) { //NOI18N
                    return true;
                } else {
                    return false;
                }
            }
        } catch (IOException ex) {
            return false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                }
            }
        }
        return false;
    }
    
    // Exec log format
    //called: /opt/solstudio12.2/bin/cc
    //        /var/tmp/as204739-cnd-test-downloads/pkg-config-0.25/popt
    //        /opt/solstudio12.2/bin/cc
    //        -DHAVE_CONFIG_H
    //        -I.
    //        -I..
    //        -g
    //        -c
    //        findme.c
    //        -o
    //        findme.o
    //
    // json format
    //[
    //{
    //  "directory": "/export/home/alsimon/projects/cmake-2.6.4/Example/Hello",
    //  "command": "/usr/bin/g++    -g3 -gdwarf-2   -o CMakeFiles/Hello.dir/hello.o -c /export/home/alsimon/projects/cmake-2.6.4/Example/Hello/hello.cxx",
    //  "file": "/export/home/alsimon/projects/cmake-2.6.4/Example/Hello/hello.cxx"
    //},
    //{
    //  "directory": "/export/home/alsimon/projects/cmake-2.6.4/Example/Demo",
    //  "command": "/usr/bin/g++    -g3 -gdwarf-2 -I/export/home/alsimon/projects/cmake-2.6.4/Example/Hello    -o CMakeFiles/helloDemo.dir/demo.o -c /export/home/alsimon/projects/cmake-2.6.4/Example/Demo/demo.cxx",
    //  "file": "/export/home/alsimon/projects/cmake-2.6.4/Example/Demo/demo.cxx"
    //},
    //{
    //  "directory": "/export/home/alsimon/projects/cmake-2.6.4/Example/Demo",
    //  "command": "/usr/bin/g++    -g3 -gdwarf-2 -I/export/home/alsimon/projects/cmake-2.6.4/Example/Hello    -o CMakeFiles/helloDemo.dir/demo_b.o -c /export/home/alsimon/projects/cmake-2.6.4/Example/Demo/demo_b.cxx",
    //  "file": "/export/home/alsimon/projects/cmake-2.6.4/Example/Demo/demo_b.cxx"
    //}
    //]
    private void run(Progress progress, Interrupter isStoped, CompileLineStorage storage) {
        result = new ArrayList<SourceFileProperties>();
        buildArtifacts = new ArrayList<String>();
        buildTools = new HashMap<LanguageKind,Map<String,Integer>>();
        buildTools.put(LanguageKind.C, new HashMap<String,Integer>());
        buildTools.put(LanguageKind.CPP, new HashMap<String,Integer>());
        buildTools.put(LanguageKind.Fortran, new HashMap<String,Integer>());
        buildTools.put(LanguageKind.Unknown, new HashMap<String,Integer>());
        if (logFileObject != null && logFileObject.isValid() && logFileObject.canRead()) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(logFileObject.getInputStream()));            
                long length = logFileObject.getSize();
                long read = 0;
                int done = 0;
                if (length <= 0) {
                    progress = null;
                }
                if (progress != null) {
                    progress.start(100);
                }
                try {
                    String tool = null;
                    List<String> params = new ArrayList<String>();
                    String directory = null;
                    String command = null;
                    String cu = null;
                    int count = 0;
                    while (true) {
                        count++;
                        if (isStoped.cancelled()) {
                            break;
                        }
                        String line = in.readLine();
                        if (line == null) {
                            break;
                        }
                        if (logType == 0) {
                            if (line.startsWith("called:")) { //NOI18N
                                logType = 1;
                            } else if (line.trim().startsWith("[") || line.trim().startsWith("{")) { //NOI18N
                                logType = 2;
                            }
                        }
                        read += line.length() + 1;
                        if (read * 100 / length > done && done < 100) {
                            done++;
                            if (progress != null) {
                                progress.increment(null);
                            }
                        }
                        if (logType == 1) {
                            if (line.startsWith("called:")) { //NOI18N
                                tool = line.substring(7).trim();
                                continue;
                            }
                            if (line.startsWith("\t")) { //NOI18N
                                params.add(line.substring(1).trim());
                                continue;
                            } else if (line.startsWith("\\t")) { //NOI18N
                                params.add(line.substring(2).trim());
                                continue;
                            }
                            if (line.length() == 0) {
                                // create new result entry
                                try {
                                    if (tool == null) {
                                        DwarfSource.LOG.log(Level.INFO, "Exec log file '"+logFileObject.getPath()+"' is corrupted near line "+count+".");
                                    } else {
                                        addSources(tool, params, storage);
                                    }
                                } catch (Throwable ex) {
                                    // ExecSource constructor can throw IllegalArgumentException for non source exec
                                    DwarfSource.LOG.log(Level.INFO, "Tool:" + tool, ex);
                                    for (String p : params) {
                                        DwarfSource.LOG.log(Level.INFO, "\t{0}", p); //NOI18N
                                    }
                                }
                                tool = null;
                                params = new ArrayList<String>();
                                continue;
                            }
                        } else if (logType == 2) {
                            line = line.trim();
                            if (line.startsWith("[") || line.startsWith("]")) { // NOI18N
                                continue;
                            }
                            if (line.startsWith("{")) { // NOI18N
                                continue;
                            }
                            if (line.startsWith("}")) { // NOI18N
                                if (directory != null && command != null && cu != null) {
                                    // create new result entry
                                    try {
                                        addSources(directory, command, cu, storage);
                                    } catch (Throwable ex) {
                                        // ExecSource constructor can throw IllegalArgumentException for non source exec
                                        DwarfSource.LOG.log(Level.INFO, "directory:" + directory + "\ncommand:" + command + "\nfile:" + logFileObject, ex); // NOI18N
                                    }
                                }
                                directory = null;
                                command = null;
                                cu = null;
                                continue;
                            }
                            String pattern = "\"directory\":"; // NOI18N
                            if (line.startsWith(pattern)) {
                                directory = line.substring(pattern.length() + 1).trim();
                            }
                            pattern = "\"command\":"; // NOI18N
                            if (line.startsWith(pattern)) {
                                command = line.substring(pattern.length() + 1).trim();
                            }
                            pattern = "\"file\":"; // NOI18N
                            if (line.startsWith(pattern)) {
                                cu = line.substring(pattern.length() + 1).trim();
                            }
                        }
                    }
                } finally {
                    if (progress != null) {
                        progress.done();
                    }
                }
                in.close();
            } catch (IOException ex) {
                DwarfSource.LOG.log(Level.INFO, "Cannot read file " + logFileObject, ex); // NOI18N
            }
        }
    }

    public List<SourceFileProperties> getResults(Progress progress, Interrupter isStoped, CompileLineStorage storage) {
        if (result == null) {
            run(progress, isStoped, storage);
        }
        return result;
    }

    public List<String> getArtifacts(Progress progress, Interrupter isStoped, CompileLineStorage storage) {
        if (buildArtifacts == null) {
            run(progress, isStoped, storage);
        }
        return buildArtifacts;
    }

    public Map<LanguageKind,Map<String,Integer>> getTools(Progress progress, Interrupter isStoped, CompileLineStorage storage) {
        if (buildTools == null) {
            run(progress, isStoped, storage);
        }
        return buildTools;
    }

    private String removeQuotes(String s) {
        if (s.endsWith(",")) { // NOI18N
            s = s.substring(0, s.length() - 1);
        }
        return DiscoveryUtils.removeQuotes(s);
    }

    private void addSources(String directory, String command, String cu, CompileLineStorage storage) {
        directory = removeQuotes(directory);
        command = removeQuotes(command);
        List<String> parseArgs = ImportUtils.parseArgs(command);
        if (parseArgs.isEmpty()) {
            throw new IllegalArgumentException("Wrong entry"); //NOI18N
        }
        Iterator<String> iterator = parseArgs.iterator();
        String tool = iterator.next();
        tool = tool.replace('\\', '/'); //NOI18N
        String compiler;
        ItemProperties.LanguageKind language;
        if (tool.lastIndexOf('/') > 0) {
            //NOI18N
            compiler = tool.substring(tool.lastIndexOf('/') + 1); //NOI18N
        } else {
            compiler = tool;
        }
        if (compiler.endsWith(".exe")) { // NOI18N
            compiler = compiler.substring(0, compiler.lastIndexOf('.')); //NOI18N
        }
        if (C_NAMES.contains(compiler)) {
            language = ItemProperties.LanguageKind.C;
        } else if (CPP_NAMES.contains(compiler)) {
            language = ItemProperties.LanguageKind.CPP;
        } else if (FORTRAN_NAMES.contains(compiler)) {
            language = ItemProperties.LanguageKind.Fortran;
        } else {
            language = ItemProperties.LanguageKind.Unknown;
        }
        cu = removeQuotes(cu);
        boolean added = addSource(compiler, language, iterator, directory, storage, cu);
        if (added) {
            // register compiler
            Map<String, Integer> compilerCount = buildTools.get(language);
            Integer count = compilerCount.get(tool);
            if (count == null) {
                compilerCount.put(tool, 1);
            } else {
                compilerCount.put(tool, count+1);
            }
        }
    }

    private void addSources(String tool, List<String> args, CompileLineStorage storage) {
        String compiler;
        ItemProperties.LanguageKind language;
        String compilePath = null;
        int lastPathSeparator = tool.replace('\\', '/').lastIndexOf('/'); //NOI18N
        if (lastPathSeparator >= 0) {
            compiler = tool.substring(lastPathSeparator + 1);
        } else {
            compiler = tool;
        }
        if (compiler.endsWith(".exe")) { //NOI18N
            compiler = compiler.substring(0, compiler.length()-4);
        }
        if (C_NAMES.contains(compiler)) {
            language = ItemProperties.LanguageKind.C;
        } else if (CPP_NAMES.contains(compiler)) {
            language = ItemProperties.LanguageKind.CPP;
        } else if (FORTRAN_NAMES.contains(compiler)) {
            language = ItemProperties.LanguageKind.Fortran;
        } else if (LIBRARIES_NAMES.contains(compiler)) {
            processLibrary(compiler, args, storage);
            return;
        } else {
            language = ItemProperties.LanguageKind.Unknown;
        }
        if (args.size() > 0) {
            compilePath = args.get(0);
        }
        Iterator<String> iterator = args.iterator();
        if (iterator.hasNext()) {
            // skip path
            iterator.next();
        }
        if (iterator.hasNext()) {
            // skip tool
            iterator.next();
        }
        boolean added = addSource(compiler, language, iterator, compilePath, storage, null);
        if (added) {
            // register compiler
            Map<String, Integer> compilerCount = buildTools.get(language);
            Integer count = compilerCount.get(tool);
            if (count == null) {
                compilerCount.put(tool, 1);
            } else {
                compilerCount.put(tool, count+1);
            }
        }
    }

    private String convertCygwinPath(String path) {
        if (Utilities.isWindows()) {
            if (path.startsWith(CYG_DRIVE) && path.length() >= CYG_DRIVE.length() + 2 && path.charAt(CYG_DRIVE.length() + 1) == '/') {
                path = path.substring(CYG_DRIVE.length());
                path = "" + Character.toUpperCase(path.charAt(0)) + ':' + path.substring(1); // NOI18N
            }
        }
        return path;
    }

    private boolean addSource(String compiler, ItemProperties.LanguageKind language, Iterator<String> iterator, String compilePath, CompileLineStorage storage, String cu) {
        boolean retValue = false;
        if (CndPathUtilities.isPathAbsolute(compilePath) && pathMapper != null) {
            String mapped = pathMapper.getLocalPath(compilePath);
            if (mapped != null) {
                compilePath = mapped;
                if (Utilities.isWindows()) {
                    compilePath = compilePath.replace('\\', '/'); // NOI18N
                }
            }
        }
        compilePath = convertCygwinPath(compilePath);
        List<String> args = new ArrayList<String>();
        while (iterator.hasNext()) {
            String next = iterator.next();
            if (next.startsWith("@")) { //NOI18N
                final String relPath = next.substring(1);
                String filePath;
                if (CndPathUtilities.isPathAbsolute(relPath)) {
                    filePath = relPath;
                } else {
                    filePath = compilePath + "/" + relPath; //NOI18N
                }
                FileObject fo = fileSystem.findResource(filePath);
                if (fo != null && fo.isValid()) {
                    List<String> lines;
                    try {
                        lines = fo.asLines();
                        if (lines != null && lines.size() > 0) {
                            next = lines.get(0).trim();
                            List<String> additional = compilerSettings.getDriver().splitCommandLine(next, CompileLineOrigin.DwarfCompileLine);
                            for (String option : additional) {
                                if (option.startsWith("'") && option.endsWith("'") || // NOI18N
                                option.startsWith("\"") && option.endsWith("\"")) { // NOI18N
                                    if (option.length() >= 2) {
                                        option = option.substring(1, option.length() - 1);
                                    }
                                }
                                args.add(option);
                            }
                        }
                    } catch (IOException ex) {
                    }
                    continue;
                }
            }
            args.add(next);
        }
        Artifacts artifacts = compilerSettings.getDriver().gatherCompilerLine(args.listIterator(), CompileLineOrigin.ExecLog, language == ItemProperties.LanguageKind.CPP);
        if (cu != null) {
            artifacts.getInput().clear();
            artifacts.getInput().add(cu);
        }
        for (String what : artifacts.getInput()) {
            if (what == null) {
                continue;
            }
            if (what.endsWith(".s") || what.endsWith(".S")) { //NOI18N
                // It seems assembler file was compiled by C compiler.
                // Exclude assembler files from C/C++ code model.
                continue;
            }
            String fullName;
            String sourceName;
            List<String> userIncludes = new ArrayList<String>(artifacts.getUserIncludes().size());
            for (String s : artifacts.getUserIncludes()) {
                if (CndPathUtilities.isPathAbsolute(s) && pathMapper != null) {
                    // NOI18N
                    String mapped = pathMapper.getLocalPath(s);
                    if (mapped != null) {
                        s = mapped;
                        if (Utilities.isWindows()) {
                            s = s.replace('\\', '/'); // NOI18N
                        }
                    }
                }
                s = convertCygwinPath(s);
                userIncludes.add(PathCache.getString(s));
            }
            List<String> userFiles = new ArrayList<String>(artifacts.getUserFiles().size());
            userFiles.addAll(artifacts.getUserFiles());
            Map<String, String> userMacros = new HashMap<String, String>(artifacts.getUserMacros().size());
            for (Map.Entry<String, String> e : artifacts.getUserMacros().entrySet()) {
                if (e.getValue() == null) {
                    userMacros.put(PathCache.getString(e.getKey()), null);
                } else {
                    userMacros.put(PathCache.getString(e.getKey()), PathCache.getString(e.getValue()));
                }
            }
            if (CndPathUtilities.isPathAbsolute(what)) {
                //NOI18N
                if (pathMapper != null) {
                    String mapped = pathMapper.getLocalPath(what);
                    if (mapped != null) {
                        what = mapped;
                        if (Utilities.isWindows()) {
                            what = what.replace('\\', '/');
                        }
                    }
                }
                what = convertCygwinPath(what);
                fullName = what;
                sourceName = DiscoveryUtils.getRelativePath(compilePath, what);
            } else {
                fullName = compilePath + "/" + what; //NOI18N
                sourceName = what;
            }
            //FileObject f = fileSystem.findResource(fullName);
            //if (f != null && f.isValid() && f.isData()) {
            fullName = PathCache.getString(fullName);
            if (artifacts.getLanguageArtifacts().contains("c")) { // NOI18N
                language = ItemProperties.LanguageKind.C;
            } else if (artifacts.getLanguageArtifacts().contains("c++")) { // NOI18N
                language = ItemProperties.LanguageKind.CPP;
            } else {
                if (language == ItemProperties.LanguageKind.Unknown) {
                    String mime = MIMESupport.getKnownSourceFileMIMETypeByExtension(fullName);
                    if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mime)) {
                        language = ItemProperties.LanguageKind.CPP;
                    } else if (MIMENames.C_MIME_TYPE.equals(mime)) {
                        language = ItemProperties.LanguageKind.C;
                    }
                } else if (language == ItemProperties.LanguageKind.C && !compiler.equals("cc")) { // NOI18N
                    // GNU driver detect language by mime type
                    String mime = MIMESupport.getKnownSourceFileMIMETypeByExtension(fullName);
                    if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mime)) {
                        language = ItemProperties.LanguageKind.CPP;
                    }
                }
            }
            ExecSource res = new ExecSource(storage);
            res.compilePath = compilePath;
            res.compiler = compiler;
            res.sourceName = sourceName;
            //
            if (project.resolveSymbolicLinks()) {
                String resolvedLink = DiscoveryUtils.resolveSymbolicLink(fileSystem, fullName);
                if (resolvedLink != null) {
                    fullName = resolvedLink;
                }
            }
            fullName = compilerSettings.normalizePath(fullName);
            //
            res.fullName = fullName;
            res.language = language;
            res.userIncludes = userIncludes;
            res.userFiles = userFiles;
            res.userMacros = userMacros;
            res.undefinedMacros = artifacts.getUserUndefinedMacros();
            res.importantFlags = DriverFactory.importantFlagsToString(artifacts);
            res.standard = DriverFactory.getLanguageStandard(res.standard, artifacts);
            if (storage != null) {
                StringBuilder buf = new StringBuilder();
                for (String s : args) {
                    if (buf.length() > 0) {
                        buf.append(' ');
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
                res.handler = storage.putCompileLine(buf.toString());
            }
            result.add(res);
            retValue = true;
            //} else {
            //    continue;
            //}
        }
        return retValue;
    }

    private FileObject convertPath(String path) {
        FileObject fo = fileSystem.findResource(path);
        if (localMapper != null) {
            if (fo == null || !fo.isValid()) {
                RelocatablePathMapper.ResolvedPath resolvedPath = localMapper.getPath(path);
                if (resolvedPath == null) {
                    if (root != null) {
                        if (localMapper.discover(fs, root, path)) {
                            resolvedPath = localMapper.getPath(path);
                            fo = fileSystem.findResource(resolvedPath.getPath());
                        }
                    }
                } else {
                    fo = fileSystem.findResource(resolvedPath.getPath());
                }
            }
        }
        return fo;
    }

    private void processLibrary(String tool, List<String> args, CompileLineStorage storage) {
        //TODO: get library name
        if ("ar".equals(tool)) { // NOI18N
        } else if ("ld".equals(tool)) { // NOI18N
            // executable or dynamic library
            //called: /usr/ccs/bin/ld
            //        /var/tmp/alsimon-cnd-test-downloads/pkg-config-0.25/glib-1.2.10/gmodule
            //        /usr/ccs/bin/ld
            //        -zld32=-S/tmp/lib_link.1359732141.24769.01/libldstab_ws.so
            //        /opt/solarisstudio12.3/prod/lib/crti.o
            //        testgmodule.o
            //        ./.libs/libgmodule.a
            //        ../.libs/libglib.a
            //        -o
            //        testgmodule
            //        -Y
            //        P,/opt/solarisstudio12.3/prod/lib:/usr/ccs/lib:/lib:/usr/lib
            //        -Qy
            //        -lc
            //        /opt/solarisstudio12.3/prod/lib/crtn.o
            Iterator<String> iterator = args.iterator();
            if (!iterator.hasNext()) {
                return;
            }
            String compilePath = iterator.next();
            if (pathMapper != null) {
                String anCompilePath = pathMapper.getLocalPath(compilePath);
                if (anCompilePath != null) {
                    compilePath = anCompilePath;
                }
            }
            if (!iterator.hasNext()) {
                return;
            }
            // skip tool
            iterator.next();
            String binary = null;
            while (iterator.hasNext()) {
                String option = iterator.next();
                if ("-o".equals(option)) { // NOI18N
                    if (iterator.hasNext()) {
                        binary = iterator.next();
                        break;
                    }
                }
            }
            if (binary != null) {
                String fullName;
                if (CndPathUtilities.isPathAbsolute(binary)) {
                    //NOI18N
                    if (pathMapper != null) {
                        String mapped = pathMapper.getLocalPath(binary);
                        if (mapped != null) {
                            binary = mapped;
                        }
                    }
                    fullName = binary;
                } else {
                    fullName = compilePath + "/" + binary; //NOI18N
                }
                FileObject f = fileSystem.findResource(fullName);
                if (f == null) {
                    // probably it is just created binary. Try to refresh folder.
                    FileObject folder = fileSystem.findResource(compilePath);
                    if (folder != null && folder.isValid() && folder.isFolder()) {
                        if (!refresedFolders.contains(folder)) {
                            folder.refresh();
                            refresedFolders.add(folder);
                            f = fileSystem.findResource(fullName);
                        }
                    }
                }
                if (f != null && f.isValid() && f.isData()) {
                    buildArtifacts.add(fullName);
                } else {
                    f = convertPath(fullName);
                    if (f != null && f.isValid() && f.isData()) {
                        buildArtifacts.add(fullName);
                    }
                }
            }
        }
    }
    
    private Set<FileObject> refresedFolders = new HashSet<FileObject>();
    
    private static final class ExecSource extends RelocatableImpl implements SourceFileProperties {

        private String sourceName;
        private String compiler;
        private ItemProperties.LanguageKind language;
        private ItemProperties.LanguageStandard standard = ItemProperties.LanguageStandard.Unknown;
        private final List<String> systemIncludes = Collections.<String>emptyList();
        private Map<String, String> userMacros;
        private List<String> undefinedMacros;
        private final Map<String, String> systemMacros = Collections.<String, String>emptyMap();
        private final CompileLineStorage storage;
        private int handler = -1;
        private String importantFlags;

        private ExecSource(CompileLineStorage storage) {
            this.storage = storage;
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
            return storage.getCompileLine(handler);
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
        public ItemProperties.LanguageStandard getLanguageStandard() {
            return standard;
        }

        @Override
        public String getImportantFlags() {
            return importantFlags;
        }
    }
}
