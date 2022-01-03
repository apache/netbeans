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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.discovery.api.DiscoveryUtils;
import org.netbeans.modules.cnd.discovery.api.DriverFactory;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.dwarfdiscovery.provider.BaseDwarfProvider.GrepEntry;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnitInterface;
import org.netbeans.modules.cnd.dwarfdump.source.SourceFile;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfMacinfoEntry;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfMacinfoTable;
import org.netbeans.modules.cnd.dwarfdump.dwarf.DwarfStatementList;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.MACINFO;
import org.netbeans.modules.cnd.dwarfdump.section.FileEntry;
import org.netbeans.modules.cnd.dwarfdump.source.Artifacts;
import org.netbeans.modules.cnd.dwarfdump.source.CompileLineOrigin;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.util.LinkSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 */
public class DwarfSource extends RelocatableImpl implements SourceFileProperties {
    public static final Logger LOG = Logger.getLogger(DwarfSource.class.getName());
    private static final boolean CUT_LOCALHOST_NET_ADRESS = Boolean.getBoolean("cnd.dwarfdiscovery.cut.localhost.net.adress"); // NOI18N
    private static final boolean ourGatherMacros = true;
    private static final boolean ourGatherIncludes = true;
    private static final String CYG_DRIVE_UNIX = "/cygdrive/"; // NOI18N
    private static final String CYG_DRIVE_WIN = "\\cygdrive\\"; // NOI18N
    private static final String CYGWIN_PATH = ":/cygwin"; // NOI18N
    private String cygwinPath;

    private String sourceName;
    private final ItemProperties.LanguageKind language;
    private ItemProperties.LanguageStandard standard;
    private List<String> systemIncludes;
    private boolean haveSystemIncludes;
    private Map<String, String> userMacros;
    private List<String> undefinedMacros;
    private Map<String, String> systemMacros;
    private boolean haveSystemMacros;
    private final Map<String,GrepEntry> grepBase;
    private String compilerName;
    private final CompileLineStorage storage;
    private int handler = -1;
    private final CompilerSettings compilerSettings;
    private String importantFlags;

    DwarfSource(CompilationUnitInterface cu, ItemProperties.LanguageKind lang, ItemProperties.LanguageStandard standard, CompilerSettings compilerSettings, Map<String,GrepEntry> grepBase, CompileLineStorage storage) throws IOException{
        this(lang, standard, grepBase, storage, compilerSettings);
        initCompilerSettings(compilerSettings, lang);
        initSourceSettings(cu, lang);
    }

    public DwarfSource(LanguageKind language, LanguageStandard standard, Map<String, GrepEntry> grepBase, CompileLineStorage storage, CompilerSettings compilerSettings) {
        this.language = language;
        this.standard = standard;
        this.grepBase = grepBase;
        this.storage = storage;
        this.compilerSettings = compilerSettings;
    }

    static DwarfSource relocateDerivedSourceFile(DwarfSource derived, String originalFile) {
        DwarfSource original = new DwarfSource(derived.language, derived.standard, derived.grepBase, derived.storage, derived.compilerSettings);
        original.compilePath = derived.compilePath;
        original.fullName = originalFile;
        original.userIncludes = new ArrayList(derived.userIncludes);
        original.userFiles = new ArrayList(derived.userFiles);
        original.includedFiles = new HashSet(derived.includedFiles);
        original.sourceName = PathUtilities.getBaseName(originalFile);
        original.systemIncludes = new ArrayList(derived.systemIncludes);
        original.haveSystemIncludes = derived.haveSystemIncludes;
        original.userMacros = new HashMap(derived.userMacros);
        original.undefinedMacros = new ArrayList(derived.undefinedMacros);
        original.systemMacros = new HashMap(derived.systemMacros);
        original.haveSystemMacros = derived.haveSystemMacros;
        original.compilerName = derived.compilerName;
        original.handler = derived.handler;
        original.importantFlags = derived.importantFlags;
        return original;
    }

    private void countFileName(CompilationUnitInterface cu) throws IOException {
        fullName = cu.getSourceFileAbsolutePath();
        fullName = fixFileName(fullName);
        //File file = new File(fullName);
        fullName = compilerSettings.getNormalizedPath(fullName);
        fullName = linkSupport(fullName);
        if (fullName != null && compilerSettings.isWindows() && compilerSettings.isLocalFileSystem()) {
            fullName = fullName.replace('/', '\\');
        }
        fullName = PathCache.getString(fullName);
        if (DwarfSource.LOG.isLoggable(Level.FINE)) {
            DwarfSource.LOG.log(Level.FINE, "Compilation unit full name:{0}", fullName); // NOI18N
        }
    }

    private void initCompilerSettings(CompilerSettings compilerSettings, ItemProperties.LanguageKind lang){
        List<String> list = compilerSettings.getSystemIncludePaths(lang);
       if (list != null){
           systemIncludes = new ArrayList<String>(list);
           //if (FULL_TRACE) {
           //    System.out.println("System Include Paths:"); // NOI18N
           //    for (String s : list) {
           //        System.out.println("\t"+s); // NOI18N
           //    }
           //}
           if (compilerSettings.isWindows()) {
               if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                    DwarfSource.LOG.log(Level.FINE, "CompileFlavor:{0}", compilerSettings.getCompileFlavor()); // NOI18N
               }
               if (compilerSettings.getCompileFlavor() != null && compilerSettings.getCompileFlavor().isCygwinCompiler()) {
                   cygwinPath = compilerSettings.getCygwinDrive();
                   if (cygwinPath == null) {
                       for(String path:list){
                           int i = path.toLowerCase().indexOf(CYGWIN_PATH);
                           if (i > 0) {
                                cygwinPath = "" + Character.toUpperCase(path.charAt(0)) + CYGWIN_PATH; // NOI18N
                                for(i = i + CYGWIN_PATH.length();i < path.length();i++){
                                    char c = path.charAt(i);
                                    if (c == '\\'){
                                        break;
                                    }
                                    cygwinPath+=""+c;
                                }
                                if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                                    DwarfSource.LOG.log(Level.FINE, "Detect cygwinPath:{0}", cygwinPath); // NOI18N
                                }
                                break;
                           }
                       }
                   }
               }
            }
        } else {
            systemIncludes = new ArrayList<String>();
        }
        haveSystemIncludes = systemIncludes.size() > 0;
        Map<String, String> map = compilerSettings.getSystemMacroDefinitions(lang);
        if (map != null){
            systemMacros = new HashMap<String,String>(map);
        } else {
            systemMacros = new HashMap<String,String>();
        }
        haveSystemMacros = systemMacros.size() > 0;
    }

    @Override
    public String getCompilePath() {
        return compilePath;
    }

    @Override
    public String getCompileLine() {
        if (storage != null && handler != -1) {
            return storage.getCompileLine(handler);
        }
        return null;
    }

    @Override
    public String getItemPath() {
        return fullName;
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
        if (false && ConfigurationDescriptorProvider.VCS_WRITE) {
            return Collections.emptySet();
        }
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
    public LanguageStandard getLanguageStandard() {
        return standard;
    }

    @Override
    public String getCompilerName() {
        return compilerName;
    }

    @Override
    public String getImportantFlags() {
        return importantFlags;
    }

    private String fixFileName(String fileName) {
        if (fileName == null){
            return fileName;
        }
        if (compilerSettings.isWindows() && compilerSettings.isLocalFileSystem()) {
            //replace /cygdrive/<something> prefix with <something>:/ prefix:
            if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                DwarfSource.LOG.log(Level.FINE, "Try to fix win name:{0}", fileName); // NOI18N
            }
            if (fileName.startsWith(CYG_DRIVE_UNIX)) {
                fileName = fileName.substring(CYG_DRIVE_UNIX.length()); // NOI18N
                fileName = "" + Character.toUpperCase(fileName.charAt(0)) + ':' + fileName.substring(1); // NOI18N
                fileName = fileName.replace('\\', '/');
                if (cygwinPath == null) {
                    cygwinPath = "" + Character.toUpperCase(fileName.charAt(0)) + CYGWIN_PATH;
                    if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                        DwarfSource.LOG.log(Level.FINE, "Set cygwinPath:{0}", cygwinPath); // NOI18N
                    }
                }
            } else {
                int i = fileName.indexOf(CYG_DRIVE_WIN);
                if (i > 0) {
                    //replace D:\cygdrive\c\<something> prefix with <something>:\ prefix:
                    if (cygwinPath == null) {
                        cygwinPath = "" + Character.toUpperCase(fileName.charAt(0)) + CYGWIN_PATH; // NOI18N
                        if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                            DwarfSource.LOG.log(Level.FINE, "Set cygwinPath:{0}", cygwinPath); // NOI18N
                        }
                    }
                    fileName = fileName.substring(i+CYG_DRIVE_UNIX.length());
                    fileName = "" + Character.toUpperCase(fileName.charAt(0)) + ':' + fileName.substring(1); // NOI18N
                    fileName = fileName.replace('\\', '/');
                }
            }
            if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                DwarfSource.LOG.log(Level.FINE, "\t{0}", fileName); // NOI18N
            }
        } else if (CUT_LOCALHOST_NET_ADRESS && Utilities.isUnix()) {
            if (fileName.startsWith("/net/")){ // NOI18N
                try {
                    InetAddress addr = InetAddress.getLocalHost();
                    String host = addr.getHostName();
                    if (host != null && host.length()>0) {
                        String u = "/net/"+host+"/"; // NOI18N
                        if (fileName.startsWith(u)){
                            fileName = fileName.substring(u.length()-1);
                        }
                    }
                } catch (UnknownHostException ex) {
                }
            }
        }
        return fileName;
    }

    private String linkSupport(String name){
        if (compilerSettings.isWindows() &&  compilerSettings.isLocalFileSystem()) {
            if (!new File(name).exists()){
                String link = name+".lnk"; // NOI18N
                if (new File(link).exists()){
                    String resolve = LinkSupport.getOriginalFile(link);
                    if (resolve != null){
                        name = resolve;
                    }
                } else {
                    StringTokenizer st = new StringTokenizer(name,"\\/"); // NOI18N
                    StringBuilder buf = new StringBuilder();
                    while(st.hasMoreTokens()){
                        String token = st.nextToken();
                        if (buf.length()>0){
                            buf.append('\\');
                        }
                        buf.append(token);
                        if (token.length()>0 && token.charAt(token.length()-1) != ':'){
                            String path = buf.toString();
                            if (!new File(path).exists()){
                                link = path+".lnk"; // NOI18N
                                if (new File(link).exists()){
                                    String resolve = LinkSupport.getOriginalFile(link);
                                    if (resolve != null){
                                        buf = new StringBuilder(resolve);
                                    } else {
                                        return name;
                                    }
                                } else {
                                    return name;
                                }
                            }
                        }
                    }
                    name = buf.toString();
                }
            }
        }
        return name;
    }


    static String extractCompilerName(CompilationUnitInterface cui, ItemProperties.LanguageKind lang) throws IOException {
        String compilerName = null;
        if (cui instanceof CompilationUnit) {
            CompilationUnit cu = (CompilationUnit) cui;
            if (cu.getCompileOptions() == null) {
                compilerName = cu.getProducer();
            } else {
                String compileOptions = cu.getCompileOptions();
                int startIndex = compileOptions.indexOf("R="); // NOI18N
                if (startIndex >=0 ) {
                    int endIndex = compileOptions.indexOf(";", startIndex); // NOI18N
                    if (endIndex >= 0) {
                        compilerName = PathCache.getString(compileOptions.substring(startIndex+2, endIndex));
                    }
                }
                if (compilerName == null) {
                    if (lang == ItemProperties.LanguageKind.CPP) {
                        compilerName = PathCache.getString("CC"); // NOI18N
                    } else if (lang == ItemProperties.LanguageKind.C) {
                        compilerName = PathCache.getString("cc"); // NOI18N
                    } else if (lang == ItemProperties.LanguageKind.Fortran) {
                        compilerName = PathCache.getString("fortran"); // NOI18N
                    } else {
                        compilerName = PathCache.getString("unknown"); // NOI18N
                    }

                }
            }
        }
        return compilerName;
    }

    static boolean isSunStudioCompiler(CompilationUnitInterface cu) throws IOException {
        if (cu instanceof CompilationUnit) {
            return ((CompilationUnit)cu).getCompileOptions() != null;
        } else {
            return cu.getCommandLine() != null && !cu.getCommandLine().isEmpty();
        }
    }

    private void initSourceSettings(CompilationUnitInterface cu, ItemProperties.LanguageKind lang) throws IOException{
        userIncludes = new ArrayList<String>();
        userFiles = new ArrayList<String>();
        userMacros = new HashMap<String,String>();
        undefinedMacros = new ArrayList<String>();
        includedFiles = new HashSet<String>();
        countFileName(cu);
        compilerName = PathCache.getString(extractCompilerName(cu, lang));
        compilePath = PathCache.getString(fixFileName(cu.getCompilationDir()));
        sourceName = PathCache.getString(cu.getSourceFileName());

        if (compilePath == null && sourceName.lastIndexOf('/')>0) {
            int i = sourceName.lastIndexOf('/');
            compilePath = sourceName.substring(0,i);
            sourceName = sourceName.substring(i+1);
        } else {
            if (sourceName.startsWith("/")) { // NOI18N
                sourceName = DiscoveryUtils.getRelativePath(compilePath, sourceName);
            }
            if (compilePath == null) {
                if (fullName != null && fullName.lastIndexOf('/')>0) {
                    int i = fullName.lastIndexOf('/');
                    compilePath = fullName.substring(0,i);
                } else {
                    compilePath = ""; // NOI18N
                }
            }
        }
    }

    public void process(CompilationUnitInterface cu) throws IOException{
        String line = cu.getCommandLine();
        if (line != null && line.length()>0){
            if (storage != null) {
                List<String> args = compilerSettings.getDriver().splitCommandLine(line, CompileLineOrigin.DwarfCompileLine);
                //List<String> args = ImportUtils.parseArgs(line);
                StringBuilder buf = new StringBuilder();
                for (int i = 0; i < args.size(); i++) {
                    if (buf.length() > 0) {
                        buf.append(' ');// NOI18N
                    }
                    String s = args.get(i);
                    if (s.startsWith("-D")) {// NOI18N
                        int j = s.indexOf("=");// NOI18N
                        if (j > 0) {
                            String key = s.substring(0, j+1);
                            key = DiscoveryUtils.removeEscape(key);
                            String value = s.substring(j+1);
                            if (value.startsWith("'") && value.endsWith("'") && value.length() > 1) {// NOI18N
                                value = value.substring(1, value.length()-1);
                                value = DiscoveryUtils.removeEscape(value);
                                s = key+value;
                            } else {
                                s = key+value;
                            }
                        } else {
                            s = DiscoveryUtils.removeEscape(s);
                        }
                    }
                    String s2 = CndPathUtilities.quoteIfNecessary(s);
                    if (s.equals(s2)) {
                        if (s.indexOf('"') > 0) {// NOI18N
                            int j = s.indexOf("\\\"");// NOI18N
                            if (j < 0) {
                                s = s.replace("\"", "\\\"");// NOI18N
                            }
                        }
                    } else {
                        s = s2;
                    }
                    buf.append(s);
                }

                handler = storage.putCompileLine(buf.toString());
            }
            gatherLine(line);
            if (cu instanceof CompilationUnit) {
                gatherIncludedFiles((CompilationUnit)cu);
            }
        } else {
            if (cu instanceof CompilationUnit) {
                gatherMacros((CompilationUnit)cu);
                gatherIncludes((CompilationUnit)cu);
                if (compilerName != null && compilerName.indexOf(" -") > 0) { // NOI18N
                    // Since 4.7 GNU write flags in the producer attribute
                    // Example:
                    // #g++ main.cpp -o .object/debug/main.o -g3 -Wall -std=c++11 -c -I./.object/debug -DDEBUG   -I./include -I./   -O3  -ldl
                    // DW_AT_producer [DW_FORM_string] GNU C++ 4.8.2 -mtune=generic -march=pentium4 -g3 -O3 -std=c++11
                    // importantFlags = "-mtune=generic -march=pentium4 -O3 -std=c++11"
                    if (language == LanguageKind.C) {
                        Artifacts artifacts = compilerSettings.getDriver().gatherCompilerLine(compilerName, CompileLineOrigin.DwarfCompileLine, false);
                        if (!artifacts.getImportantFlags().isEmpty()) {
                            importantFlags = DriverFactory.importantFlagsToString(artifacts);
                        }
                    } else if (language == LanguageKind.CPP) {
                        Artifacts artifacts = compilerSettings.getDriver().gatherCompilerLine(compilerName, CompileLineOrigin.DwarfCompileLine, true);
                        if (!artifacts.getImportantFlags().isEmpty()) {
                            importantFlags = DriverFactory.importantFlagsToString(artifacts);
                        }
                        standard = DriverFactory.getLanguageStandard(standard, artifacts);
                    }
                }
            } else if (cu instanceof SourceFile) {
                processPseudoDwarf((SourceFile) cu);
            }
            if (importantFlags == null || importantFlags.isEmpty()) {
                switch(standard) {
                    case C89:
                        importantFlags = "-std=c89";// NOI18N
                        break;
                    case C99:
                        importantFlags = "-std=c99";// NOI18N
                        break;
                }
            }
        }
    }

    private void processPseudoDwarf(SourceFile sf) {
        final String line = sf.getDwarfDump();
        if (line == null || line.isEmpty()) {
            return;
        }
        DwarfStatementList emulateDwarf = new DwarfStatementList(0);
        List<String> dirs = emulateDwarf.getIncludeDirectories();
        List<FileEntry> files = emulateDwarf.getFileEntries();
        List<String> options = compilerSettings.getDriver().splitCommandLine(line, CompileLineOrigin.DwarfCompileLine);
        for(String option : options) {
            if (option.startsWith("-d")) { // NOI18N
                //buf.append(" -d").append("'").append(dir).append("'"); // NOI18N
                dirs.add(DiscoveryUtils.removeQuotes(option.substring(2)));
            } else if (option.startsWith("-f:")) { // NOI18N
                //buf.append(" -f:").append(""+fileEntry.dirIndex).append(":'").append(fileEntry.fileName).append("'"); // NOI18N
                String s = option.substring(3);
                int i = s.indexOf(':'); // NOI18N
                if (i <= 0) {
                    // broken format
                    return;
                }
                int dirNumber = Integer.parseInt(s.substring(0,i));
                String name = DiscoveryUtils.removeQuotes(s.substring(i+1));
                files.add(new FileEntry(name, dirNumber, -1, -1));
            } else if (option.startsWith("-include")) { // NOI18N
                //buf.append(" -include").append("'").append(includedSource).append("'"); // NOI18N
                String s = DiscoveryUtils.removeQuotes(option.substring(8));
                undefinedMacros.add(PathCache.getString(s));
            } else if (option.startsWith("-D")) { // NOI18N
                //buf.append(" -D").append(def.substring(0,i)).append("='").append(def.substring(i+1).trim()).append("'"); // NOI18N
                String s = option.substring(2);
                int i = s.indexOf('='); // NOI18N
                String name;
                String value;
                if (i >= 0) {
                    name = s.substring(0,i);
                    value = DiscoveryUtils.removeQuotes(s.substring(i+1));
                } else {
                    name = s;
                    value = null;
                }
                if (haveSystemMacros && systemMacros.containsKey(name)){
                    String sysValue = systemMacros.get(name);
                    if (equalValues(sysValue, value)) {
                        continue;
                    }
                }
                userMacros.put(PathCache.getString(name), PathCache.getString(value));
            } else if (option.startsWith("-U")) { // NOI18N
                //buf.append(" -D").append(def.substring(0,i)); // NOI18N
                String s = option.substring(2);
                undefinedMacros.add(PathCache.getString(s));
            }
        }
        for (Iterator<String> it = emulateDwarf.getIncludeDirectories().iterator(); it.hasNext();) {
            addpath(it.next());
        }
        List<String> list = grepSourceFile(fullName).includes;
        for(String path : list){
            cutFolderPrefix(path, emulateDwarf);
        }
        for(String path : emulateDwarf.getFilePaths()){
            processPath(path, emulateDwarf, true);
        }
        if (DwarfSource.LOG.isLoggable(Level.FINE)) {
            DwarfSource.LOG.log(Level.FINE, "Include paths:{0}", userIncludes); // NOI18N
        }
    }

    private void addUserIncludePath(String path){
        if (!userIncludes.contains(path)) {
            userIncludes.add(path);
        }
    }

    private void gatherLine(String line) {
        // /set/c++/bin/5.9/intel-S2/prod/bin/CC -c -g -DHELLO=75 -Idist  main.cc -Qoption ccfe -prefix -Qoption ccfe .XAKABILBpivFlIc.
        if (DwarfSource.LOG.isLoggable(Level.FINE)) {
            DwarfSource.LOG.log(Level.FINE, "Process command line {0}", line); // NOI18N
        }
        Artifacts artifacts = compilerSettings.getDriver().gatherCompilerLine(line, CompileLineOrigin.DwarfCompileLine, this.language == LanguageKind.CPP);
        for(String s : artifacts.getUserIncludes()) {
            String include = PathCache.getString(s);
            addUserIncludePath(include);
        }
        userFiles.addAll(artifacts.getUserFiles());
        for(String s : artifacts.getUserUndefinedMacros()) {
            undefinedMacros.add(PathCache.getString(s));
        }
        for(Map.Entry<String, String> entry : artifacts.getUserMacros().entrySet()) {
            userMacros.put(PathCache.getString(entry.getKey()), entry.getValue());
        }
        importantFlags = DriverFactory.importantFlagsToString(artifacts);
        standard = DriverFactory.getLanguageStandard(standard, artifacts);
    }

    private String fixCygwinPath(String path){
        if (cygwinPath != null) {
            if (path.startsWith("/usr/lib/")){// NOI18N
                path = cygwinPath+path.substring(4);
            } else if (path.startsWith("/usr")) { // NOI18N
                path = cygwinPath+path;
            }
        }
        if (path.startsWith(CYG_DRIVE_UNIX)){
            path = fixFileName(path);
        }
        if (compilerSettings.isWindows()) {
            path = path.replace('\\', '/');
        }
        return path;
    }

    private boolean isSystemPath(String path){
        path = fixCygwinPath(path);
        path = normalizePath(path);
        if (path.startsWith("/") || // NOI18N
                path.length()>2 && path.charAt(1)==':'){
            HashSet<String> bits = new HashSet<String>();
            for (String cp : systemIncludes){
                if (path.equals(cp)) {
                    return true;
                }
                for(String sub : grepSystemFolder(cp).includes){
                    bits.add(sub);
                }
            }
            for (String cp : systemIncludes){
                for(String sub : bits) {
                    if (path.startsWith(cp)) {
                        if (path.substring(cp.length()).startsWith(sub)){
                            return true;
                        }
                    }
                }
            }
            //if (path.startsWith("/usr")) {
            //    System.err.println("Detectes as user include"+path);
            //}
        }
        return false;
    }

    private void addpath(String path){
        if (haveSystemIncludes) {
            if (!isSystemPath(path)){
                path = fixCygwinPath(path);
                path = normalizePath(path);
                addUserIncludePath(PathCache.getString(path));
                if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                    DwarfSource.LOG.log(Level.FINE, "\tuser:{0}", path); // NOI18N
                }
            }
        } else {
            if (path.startsWith("/usr")) { // NOI18N
                path = fixCygwinPath(path);
                path = normalizePath(path);
                path = PathCache.getString(path);
                if (!systemIncludes.contains(path)) {
                    systemIncludes.add(path);
                }
                if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                    DwarfSource.LOG.log(Level.FINE, "\tsystem:{0}", path); // NOI18N
                }
            } else {
                path = fixCygwinPath(path);
                path = normalizePath(path);
                addUserIncludePath(PathCache.getString(path));
                if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                    DwarfSource.LOG.log(Level.FINE, "\tuser:{0}", path); // NOI18N
                }
            }
        }
    }

    private String normalizePath(String path){
        if (path.startsWith("/") || // NOI18N
                path.length()>2 && path.charAt(1)==':') {
            return compilerSettings.getNormalizedPath(path);
        }
        return path;
    }

    private void gatherIncludes(final CompilationUnit cu) throws IOException {
        if (!ourGatherIncludes) {
            return;
        }
        DwarfStatementList dwarfTable = cu.getStatementList();
        if (dwarfTable == null) {
            if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                DwarfSource.LOG.log(Level.FINE, "Include paths not found"); // NOI18N
            }
            return;
        }
        for (Iterator<String> it = dwarfTable.getIncludeDirectories().iterator(); it.hasNext();) {
            addpath(it.next());
        }
        List<String> list = grepSourceFile(fullName).includes;
        for(String path : list){
            cutFolderPrefix(path, dwarfTable);
        }
        List<String> dwarfIncludedFiles = dwarfTable.getFilePaths();
        DwarfMacinfoTable dwarfMacroTable = cu.getMacrosTable();
        if (dwarfMacroTable != null) {
            List<Integer> commandLineIncludedFiles = dwarfMacroTable.getCommandLineIncludedFiles();
            for(int i : commandLineIncludedFiles) {
                String includedSource = processPath(dwarfTable.getFilePath(i));
                if (!fullName.replace('\\', '/').equals(includedSource)) {
                    processPath(dwarfTable.getFilePath(i), dwarfTable, false);
                }
            }
        }
        for(String path : dwarfIncludedFiles){
            processPath(path, dwarfTable, true);
        }
        if (DwarfSource.LOG.isLoggable(Level.FINE)) {
            DwarfSource.LOG.log(Level.FINE, "Include paths:{0}", userIncludes); // NOI18N
        }
    }

    private void processPath(String path, DwarfStatementList dwarfTable, boolean isPath) {
        String includeFullName = processPath(path);
        if (isPath) {
            int i = includeFullName.lastIndexOf('/'); // NOI18N
            if (i > 0) {
                String userPath = includeFullName.substring(0, i);
                if (!isSystemPath(userPath)) {
                    for (String included : grepSourceFile(includeFullName).includes) {
                        cutFolderPrefix(included, dwarfTable);
                    }
                    addpath(userPath);
                }
            }
        } else {
            addpath(includeFullName);
        }
        includedFiles.add(PathCache.getString(includeFullName));
        if (DwarfSource.LOG.isLoggable(Level.FINE)) {
            DwarfSource.LOG.log(Level.FINE, "Included file:{0}", includeFullName); // NOI18N
        }
    }

    private String processPath(String path) {
        path = path.replace('\\', '/'); // NOI18N
        if (DwarfSource.LOG.isLoggable(Level.FINE)) {
            DwarfSource.LOG.log(Level.FINE, "Included file original:{0}", path); // NOI18N
        }
        String includeFullName;
        if (path.startsWith("./")) { // NOI18N
            includeFullName = compilePath + path.substring(1);
        } else if (path.startsWith("../")) { // NOI18N
            includeFullName = compilePath + "/" + path; // NOI18N
        } else if (!(path.startsWith("/") || path.length()>2 && path.charAt(1)==':')) { // NOI18N
            includeFullName = compilePath + "/" + path; // NOI18N
        } else {
            includeFullName = fixCygwinPath(path);
        }
        if (compilerSettings.isWindows()) {
            includeFullName = includeFullName.replace('\\', '/'); // NOI18N
        }
        includeFullName = normalizePath(includeFullName);
        return includeFullName;
    }

    private void cutFolderPrefix(String path, final DwarfStatementList dwarfTable) {
        if (compilerSettings.isWindows()) {
            path = path.replace('\\', '/'); // NOI18N
        }
        if (path.indexOf('/')>0){ // NOI18N
            int n = path.lastIndexOf('/'); // NOI18N
            String name = path.substring(n+1);
            String relativeDir = path.substring(0,n);
            String dir = "/"+relativeDir; // NOI18N
            List<String> paths = dwarfTable.getPathsForFile(name);
            if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                DwarfSource.LOG.log(Level.FINE, "Try to find new include paths for:{0} in folder {1}", new Object[]{name, dir}); // NOI18N
            }
            for(String dwarfPath : paths){
                if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                    DwarfSource.LOG.log(Level.FINE, "    candidate:{0}", dwarfPath); // NOI18N
                }
                if (dwarfPath.endsWith(dir)){
                    String found = dwarfPath.substring(0,dwarfPath.length()-dir.length());
                    found = fixCygwinPath(found);
                    found = normalizePath(found);
                    if (!userIncludes.contains(found)) {
                        if (haveSystemIncludes) {
                            boolean system = false;
                            if (found.startsWith("/") || // NOI18N
                                    found.length()>2 && found.charAt(1)==':'){
                                system = systemIncludes.contains(found);
                            }
                            if (!system){
                                if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                                    DwarfSource.LOG.log(Level.FINE, "    Find new include path:{0}", found); // NOI18N
                                }
                                addUserIncludePath(PathCache.getString(found));
                            }
                        } else {
                            if (!dwarfPath.startsWith("/usr")){ // NOI18N
                                if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                                    DwarfSource.LOG.log(Level.FINE, "    Find new include path:{0}", found); // NOI18N
                                }
                                addUserIncludePath(PathCache.getString(found));
                            }
                        }
                    }
                    break;
                } else if (dwarfPath.equals(relativeDir)){
                    String found = "."; // NOI18N
                    if (!userIncludes.contains(found)) {
                        if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                            DwarfSource.LOG.log(Level.FINE, "    Find new include path:{0}", found); // NOI18N
                        }
                        addUserIncludePath(PathCache.getString(found));
                    }
                    break;
                }
            }
        }
    }

    private void gatherIncludedFiles(final CompilationUnit cu) throws IOException {
        if (!ourGatherIncludes) {
            return;
        }
        DwarfStatementList dwarfTable = cu.getStatementList();
        if (dwarfTable == null) {
            return;
        }
        for(String path :dwarfTable.getFilePaths()){
            String includeFullName = path;
            if (path.startsWith("./")) { // NOI18N
                includeFullName = compilePath+path.substring(1);
            } else if (path.startsWith("../")) { // NOI18N
                includeFullName = compilePath + "/" + path; // NOI18N
            }
            includeFullName = normalizePath(includeFullName);
            includedFiles.add(PathCache.getString(includeFullName));
        }
    }

    private void gatherMacros(final CompilationUnit cu) throws IOException {
        if (!ourGatherMacros){
            return;
        }
        DwarfMacinfoTable dwarfTable = cu.getMacrosTable();
        if (dwarfTable == null) {
            if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                DwarfSource.LOG.log(Level.FINE, "Macros not found"); // NOI18N
            }
            return;
        }
        int firstMacroLine = grepSourceFile(fullName).firstMacroLine;
        List<DwarfMacinfoEntry> table = dwarfTable.getCommandLineMarcos();
        for (Iterator<DwarfMacinfoEntry> it = table.iterator(); it.hasNext();) {
            DwarfMacinfoEntry entry = it.next();
            if ((entry.type == MACINFO.DW_MACINFO_define ||
                 entry.type == MACINFO.DW_MACRO_define_indirect) &&
                 entry.definition != null) {
                String def = entry.definition;
                int i = def.indexOf(' ');
                String macro;
                String value = null;
                if (i>0){
                    macro = PathCache.getString(def.substring(0,i));
                    value = PathCache.getString(def.substring(i+1).trim());
                } else {
                    macro = PathCache.getString(def);
                }
                if (firstMacroLine == entry.lineNum) {
                    if (macro.equals(grepSourceFile(fullName).firstMacro)){
                        break;
                    }
                }
                if (haveSystemMacros && systemMacros.containsKey(macro)){
                    String sysValue = systemMacros.get(macro);
                    if (equalValues(sysValue, value)) {
                        continue;
                    }
                }
                userMacros.put(macro,value);
            } else if ((entry.type == MACINFO.DW_MACINFO_undef ||
                 entry.type == MACINFO.DW_MACRO_undef_indirect) &&
                 entry.definition != null) {
                String def = PathCache.getString(entry.definition);
                undefinedMacros.add(def);
            }
        }
        if (DwarfSource.LOG.isLoggable(Level.FINE)) {
            DwarfSource.LOG.log(Level.FINE, "Macros:{0}", userMacros); // NOI18N
        }
    }

    private boolean equalValues(String sysValue, String value) {
        // filter out system macros
        // For example gcc windows dwarf contains following system macros as user:
        // unix=1 __unix=1 __unix__=1 __CYGWIN__=1 __CYGWIN32__=1
        if (value == null || "1".equals(value)) { // NOI18N
            return sysValue == null || "1".equals(sysValue); // NOI18N
        }
        return value.equals(sysValue); // NOI18N
    }

    private GrepEntry grepSystemFolder(String path) {
        GrepEntry res = grepBase.get(path);
        if (res != null) {
            return res;
        }
        FileSystem fs = compilerSettings.getFileSystem();
        res = new GrepEntry();
        FileObject folder = fs.findResource(path);
        if (folder != null && folder.isValid() && folder.canRead() && folder.isFolder()) {
            FileObject[] ff = folder.getChildren();
            if (ff != null) {
                for(FileObject f: ff){
                    if (f != null && f.isValid() && f.canRead() && f.isData()){
                        List<String> l = grepSourceFile(f.getPath()).includes;
                        for (String i : l){
                            if (i.indexOf("..")>0 || i.startsWith("/") || i.indexOf(':')>0) { // NOI18N
                                continue;
                            }
                            if (i.indexOf('/')>0){ // NOI18N
                                int n = i.lastIndexOf('/'); // NOI18N
                                String relativeDir = i.substring(0,n);
                                String dir = "/"+relativeDir; // NOI18N
                                if (!res.includes.contains(dir)){
                                    res.includes.add(PathCache.getString(dir));
                                }
                            }
                        }
                    }
                }
            }
        }
        List<String> secondLevel = new ArrayList<String>();
        for(String sub : res.includes) {
            FileObject subFolder = fs.findResource(path+sub);
            if (subFolder != null && subFolder.isValid() && subFolder.canRead() && subFolder.isFolder()) {
                try {
                    String canonicalPath = CndFileUtils.getCanonicalPath(subFolder);
                    if (canonicalPath != null && canonicalPath.startsWith(path + sub)) {
                        for (String s : grepSystemFolder(path + sub).includes) {
                            secondLevel.add(s);
                        }
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        for(String s: secondLevel){
            if (!res.includes.contains(s)){
                res.includes.add(PathCache.getString(s));
            }
        }
        grepBase.put(PathCache.getString(path), res);
        return res;
    }

    private GrepEntry grepSourceFile(String fileName){
        FileSystem fs = compilerSettings.getFileSystem();
        GrepEntry res = grepBase.get(fileName);
        if (res != null) {
            return res;
        }
        res = new GrepEntry();
        String dirName = CndPathUtilities.getDirName(fileName);
        if (dirName != null) {
            GrepEntry dirInfo = grepBase.get(dirName);
            if (dirInfo != null && !dirInfo.exists) {
                grepBase.put(fileName,res);
                return res;
            }
        }
        FileObject file = fs.findResource(fileName);
        if (file == null || !file.isValid()) {
            if (dirName != null) {
                FileObject dir = fs.findResource(dirName);
                if (dir != null && dir.isValid()) {
                    GrepEntry dirInfo = new GrepEntry();
                    dirInfo.exists = true;
                    grepBase.put(dirName, dirInfo);
                } else {
                    GrepEntry dirInfo = new GrepEntry();
                    dirInfo.exists = false;
                    grepBase.put(dirName, dirInfo);
                }
            }
        }
        if (file != null && file.isValid() && file.canRead() && file.isData()){
            res.exists = true;
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(file.getInputStream()));
                int lineNo = 0;
                int size;
                String line;
                int first;
                fileLoop:while((line = in.readLine()) != null) {
                    lineNo++;
                    if ((size = line.length()) == 0) {
                        continue;
                    }
                    firstLoop:for(first = 0; first < size; first++) {
                        switch (line.charAt(first)) {
                            case ' ':
                            case '\t':
                                break;
                            case '#':
                                break firstLoop;
                            default:
                                continue fileLoop;
                        }
                    }
                    first++;
                    if (first >= size) {
                        continue;
                    }
                    secondLoop:for(; first < size; first++) {
                        switch (line.charAt(first)) {
                            case ' ':
                            case '\t':
                                break;
                            case 'i':
                                if (first + 1 < size && line.charAt(first + 1) != 'n') {
                                    // not "include" prefix
                                    continue fileLoop;
                                }
                                break secondLoop;
                            case 'd':
                                break secondLoop;
                            default:
                                continue fileLoop;
                        }
                    }
                    if (first >= size) {
                        continue;
                    }
                    line = line.substring(first);
                    if (line.startsWith("include")){ // NOI18N
                        line = line.substring(7).trim();
                        if (line.length()>2) {
                            if (line.startsWith("/*")) { // NOI18N
                                int i = line.indexOf("*/"); // NOI18N
                                if (i > 0) {
                                    line = line.substring(i+2).trim();
                                }
                            }
                            char c = line.charAt(0);
                            if (c == '"') {
                                if (line.indexOf('"',1)>0){
                                    res.includes.add(PathCache.getString(line.substring(1,line.indexOf('"',1))));
                                    if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                                        DwarfSource.LOG.log(Level.FINE, "find in source:{0}", line.substring(1,line.indexOf('"',1))); // NOI18N
                                    }
                                }
                            } else if (c == '<'){
                                if (line.indexOf('>')>0){
                                    res.includes.add(PathCache.getString(line.substring(1,line.indexOf('>'))));
                                    if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                                        DwarfSource.LOG.log(Level.FINE, "find in source:{0}", line.substring(1,line.indexOf('>'))); // NOI18N
                                    }
                                }
                            }
                        }
                    } else if (line.startsWith("define")){ // NOI18N
                        if (res.firstMacroLine == -1) {
                            line = line.substring(6).trim();
                            if (line.length()>0) {
                                if (line.startsWith("/*")) { // NOI18N
                                    int i = line.indexOf("*/"); // NOI18N
                                    if (i > 0) {
                                        line = line.substring(i+2).trim();
                                    }
                                }
                                StringTokenizer st = new StringTokenizer(line,"\t ("); // NOI18N
                                while(st.hasMoreTokens()) {
                                    res.firstMacroLine = lineNo;
                                    res.firstMacro = PathCache.getString(st.nextToken());
                                    break;
                                }
                            }
                        }
                    }
                }
                in.close();
            } catch (IOException ex) {
                DwarfSource.LOG.log(Level.INFO, "Cannot grep file: "+fileName, ex); // NOI18N
            }
        } else {
            if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                DwarfSource.LOG.log(Level.FINE, "Cannot grep file:{0}", fileName); // NOI18N
            }
        }
        res.includes.trimToSize();
        grepBase.put(fileName,res);
        return res;
    }
}
