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
package org.netbeans.modules.cnd.modeldiscovery.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager.PackageConfiguration;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager.PkgConfig;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager.ResolvedPath;
import org.netbeans.modules.cnd.toolchain.support.ToolchainUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.Path;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 */
public class PkgConfigImpl implements PkgConfig {

    private static final boolean TRACE = false;

    private final HashMap<String, PackageConfigurationImpl> configurations = new HashMap<String, PackageConfigurationImpl>();
    private Map<String, List<Pair>> seachBase;
    private String drivePrefix;
    private final ExecutionEnvironment env;

    public PkgConfigImpl(ExecutionEnvironment env, MakeConfiguration conf) {
        this.env = env;
        initPackages(conf);
    }

    private boolean isWindows() {
        try {
            return HostInfoUtils.getHostInfo(env).getOSFamily()== HostInfo.OSFamily.WINDOWS;
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return false;
        }
    }
    private boolean isLinux() {
        try {
            return HostInfoUtils.getHostInfo(env).getOSFamily()== HostInfo.OSFamily.LINUX;
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return false;
        }
    }
    
    private List<String> envPaths(String folder, String... foders){
        List<String> res = new ArrayList<String>();
        String pkg_config;
        if (isWindows()) {
            pkg_config = "pkg-config.exe"; // NOI18N
        } else {
            pkg_config = "pkg-config"; // NOI18N
        }
        ExitStatus status = ProcessUtils.execute(env, pkg_config, new String[]{"--variable", "pc_path", "pkg-config"}); // NOI18N
        if (status.isOK()) {
            addPaths(res, status.getOutputString());
        }
        res.add(folder);
        Collections.addAll(res, foders);
        addPaths(res, HostInfoProvider.getEnv(env).get("PKG_CONFIG_PATH")); // NOI18N
        return res;
    }

    private void addPaths(List<String> res, String additionalPaths) {
        if (additionalPaths != null && additionalPaths.length() > 0) {
            StringTokenizer st;
            if (isWindows()){
                st = new StringTokenizer(additionalPaths, ";"); // NOI18N
            } else {
                st = new StringTokenizer(additionalPaths, ":"); // NOI18N
            }
            while(st.hasMoreTokens()) {
                res.add(st.nextToken());
            }
        }
    }
    
    private void initPackages(MakeConfiguration conf) {
        if (isWindows()){
            // at first find pkg-config.exe in paths
            String baseDirectory = getPkgConfihPath(conf);
            if (baseDirectory == null) {
                CompilerSet set = null;
                for(CompilerSet cs : CompilerSetManager.get(ExecutionEnvironmentFactory.getLocal()).getCompilerSets()) {
                    if (cs.getCompilerFlavor().isCygwinCompiler()) {
                        set = cs;
                        break;
                    }
                }
                if (set != null){
                    baseDirectory = set.getDirectory();
                    //"C:\cygwin\bin"
                    if (baseDirectory != null && baseDirectory.endsWith("bin")){ // NOI18N
                        drivePrefix = baseDirectory.substring(0, baseDirectory.length()-4);
                        baseDirectory = baseDirectory.substring(0, baseDirectory.length()-3)+"lib/pkgconfig/"; // NOI18N
                    }
                }
                if (baseDirectory == null) {
                    drivePrefix = "c:/cygwin"; // NOI18N
                    baseDirectory = "c:/cygwin/lib/pkgconfig/"; // NOI18N
                }
            } else {
                String suffix = "/lib/pkgconfig/"; // NOI18N
                if (baseDirectory.endsWith(suffix)){
                    drivePrefix = baseDirectory.substring(0, baseDirectory.length()-suffix.length());
                }
            }
            initPackages(envPaths(baseDirectory), true); // NOI18N
        } else {
                //initPackages("/net/elif/export1/sside/as204739/pkgconfig/"); // NOI18N     
            if (isLinux()) {
                HostInfo hostinfo = null;
                try {
                    if (HostInfoUtils.isHostInfoAvailable(env)) {
                        hostinfo = HostInfoUtils.getHostInfo(env);
                    }                
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (CancellationException ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (hostinfo != null && hostinfo.getOS().getBitness() == HostInfo.Bitness._64) {
                    initPackages(envPaths("/usr/lib64/pkgconfig", "/usr/share/pkgconfig"), false); // NOI18N
                } else {
                    initPackages(envPaths("/usr/lib/pkgconfig", "/usr/share/pkgconfig"), false); // NOI18N
                } 
            } else {
                initPackages(envPaths("/usr/lib/pkgconfig", "/usr/share/pkgconfig"), false); // NOI18N
            }
        }
    }

    private String getPkgConfihPath(MakeConfiguration conf){
        List<String> buildPaths = null;
        if (conf != null) {
            CompilerSet cs = conf.getCompilerSet().getCompilerSet();
            if (cs != null) {
                Map<String, String> envMap = new HashMap<String, String>();
                org.openide.util.Pair<String, String> toolCollectionPath = ToolchainUtilities.modifyPathEnvVariable(env, envMap, cs, ""); // NOI18N
                String[] split = toolCollectionPath.second().split(";"); // NOI18N
                buildPaths = new ArrayList<String>(split.length);
                for(String s : split) {
                    buildPaths.add(s);
                }
            }
        }
        if (buildPaths == null) {
            buildPaths = Path.getPath();
        }
        for(String path : buildPaths){
            File file = new File(path+File.separator+"pkg-config.exe"); // NOI18N
            if (file.exists()) {
                path = path.replace('\\', '/'); // NOI18N
                if (path.endsWith("/")){ // NOI18N
                    path = path.substring(0, path.length()-1);
                }
                int i = path.lastIndexOf('/'); // NOI18N
                if (i > 0){
                    path = path.substring(0, i + 1 )+"lib/pkgconfig/"; // NOI18N
                    return path;
                }
                return null;
            }
        }
        return null;
    }

    private void initPackages(List<String> folders, boolean isWindows) {
        FileSystemProvider.warmup(FileSystemProvider.WarmupMode.FILES_CONTENT, env, folders, null);
        Set<FileObject> done = new HashSet<FileObject>();
        for(String folder:folders) {
            FileObject file = RemoteFileUtil.getFileObject(RemoteFileUtil.normalizeAbsolutePath(folder, env), env);
            if (file == null) {
                continue;
            }
            if (done.contains(file)) {
                continue;
            }
            done.add(file);
            if (file.isValid() && file.isFolder() && file.canRead()) {
                for (FileObject fpc : file.getChildren()) {
                    String name = fpc.getNameExt();
                    if (name.endsWith(".pc") && fpc.canRead() && fpc.isData()) { // NOI18N
                        String pkgName = name.substring(0, name.length()-3);
                        PackageConfigurationImpl pc = new PackageConfigurationImpl(pkgName);
                        readConfig(fpc, pc,  isWindows);
                        if (TRACE) {
                            System.err.println("read "+name+"\n"+pc.toString());
                        }
                        configurations.put(pkgName, pc);
                    }
                }
            }
        }
    }

    @Override
    public PackageConfiguration getPkgConfig(String pkg) {
        return getConfig(pkg);
    }

    @Override
    public List<PackageConfiguration> getAvaliablePkgConfigs() {
        return new ArrayList<PackageConfiguration>(configurations.values());
    }

    @Override
    public Collection<ResolvedPath> getResolvedPath(String include) {
        Map<String, List<Pair>> map = getLibraryItems();
        List<Pair> pairs = map.get(include);
        if (pairs != null && pairs.size() > 0){
            ArrayList<ResolvedPath> res = new ArrayList<ResolvedPath>(pairs.size());
            for(Pair p : pairs){
                res.add(new ResolvedPathImpl(p.path, p.configurations));
            }
            return res;
        }
        return null;
    }

    /*package-local*/ void trace(){
        List<String> sort = new ArrayList<String>(configurations.keySet());
        Collections.sort(sort);
        for(String pkg: sort){
            traceConfig(pkg, false);
        }
        Map<String, List<Pair>> res = getLibraryItems();
        System.out.println("Known includes size: "+res.size()); // NOI18N
        sort = new ArrayList<String>(res.keySet());
        Collections.sort(sort);
        for(String key: sort){
            List<Pair> pairs = res.get(key);
            if (pairs != null) {
                for(Pair value : pairs) {
                    StringBuilder buf = new StringBuilder();
                    for(PackageConfiguration pc : value.configurations){
                        if (buf.length()>0){
                            buf.append(", "); // NOI18N
                        }
                        buf.append(pc.getName());
                    }
                    System.out.println(key+"\t"+value.path+"\t["+buf.toString()+"]"); // NOI18N
                }
            }
        }

    }

    /*package-local*/ void traceConfig(String pkg, boolean recursive){
        traceConfig(pkg, recursive, new HashSet<String>(), "");

    }
    private void traceConfig(String pkg, boolean recursive, Set<String> visited, String tab){
        if (visited.contains(pkg)) {
            return;
        }
        visited.add(pkg);
        PackageConfigurationImpl pc = configurations.get(pkg);
        if (pc != null){
            System.out.println(tab+"Package definition"); // NOI18N
            System.out.println(tab+"Name:     "+pkg); // NOI18N
            System.out.println(tab+"Requires: "+pc.requires); // NOI18N
            System.out.println(tab+"Macros:   "+pc.macros); // NOI18N
            System.out.println(tab+"Paths:    "+pc.paths); // NOI18N
            if (recursive) {
                for(String p : pc.requires){
                    traceConfig(p, recursive, visited, tab+"    "); // NOI18N
                }
            }
        } else {
            System.out.println("Not found package definition "+pkg); // NOI18N
        }
    }

    /*package-local*/ void traceRecursiveConfig(String pkg){
        PackageConfiguration pc = getConfig(pkg);
        if (pc != null){
            System.out.println("Recursive package definition"); // NOI18N
            System.out.println("Name:    "+pkg); // NOI18N
            System.out.println("Package: "+pkg); // NOI18N
            System.out.println("Macros:  "+pc.getMacros()); // NOI18N
            System.out.println("Paths:   "+pc.getIncludePaths()); // NOI18N
        }
    }

    private PackageConfiguration getConfig(String pkg){
        PackageConfigurationImpl master = new PackageConfigurationImpl(pkg);
        getConfig(master, configurations.get(pkg));
        return master;
    }

    private void getConfig(PackageConfigurationImpl master, PackageConfigurationImpl pc){
        if (pc != null) {
            for(String m : pc.macros){
                if (!master.macros.contains(m)){
                    master.macros.add(m);
                }
            }
            for(String p : pc.paths){
                if (!master.paths.contains(p)){
                    master.paths.add(p);
                }
            }
            for(String require : pc.requires){
                getConfig(master, configurations.get(require));
            }
        }
    }

    private synchronized Map<String, List<Pair>> getLibraryItems(){
        Map<String, List<Pair>> res = seachBase;
        if (res == null) {
            res = _getLibraryItems();
            seachBase = res;
        }
        return res;
    }
    private Map<String, List<Pair>> _getLibraryItems(){
        Map<String, Set<PackageConfiguration>> map = new HashMap<String, Set<PackageConfiguration>>();
        for(String pkg : configurations.keySet()){
            PackageConfigurationImpl pc = configurations.get(pkg);
            if (pc != null){
                for (String p : pc.paths){
                    if (drivePrefix != null) {
                        if (p.substring(drivePrefix.length()).equals("/usr/include") || p.substring(drivePrefix.length()).equals("/usr/sfw/include")){ // NOI18N
                            continue;
                        }
                    } else {
                        if (p.equals("/usr/include") || p.equals("/usr/sfw/include")){ // NOI18N
                            continue;
                        }
                    }
                    Set<PackageConfiguration> set = map.get(p);
                    if (set == null){
                        set = new HashSet<PackageConfiguration>();
                        map.put(p, set);
                    }
                    set.add(pc);
                }
            }
        }
        Map<String, List<Pair>> res = new HashMap<String, List<Pair>>();
        for (Map.Entry<String, Set<PackageConfiguration>> entry : map.entrySet()) {
            Pair pair = new Pair(entry.getKey(), entry.getValue());
            if (isWindows()){
                if (entry.getKey().length() < 2 || entry.getKey().charAt(1) != ':') {
                    if (TRACE) {
                        System.err.println("ignore relative path "+entry.getKey());
                    }
                    continue;
                }
            } else {
                if (!entry.getKey().startsWith("/")) { // NOI18N
                    if (TRACE) {
                        System.err.println("ignore relative path "+entry.getKey());
                    }
                    continue;
                }
            }
            String normalizedPath = RemoteFileUtil.normalizeAbsolutePath(entry.getKey(), env);
            FileObject dir = RemoteFileUtil.getFileObject(normalizedPath, env);
            addLibraryItem(res, pair, "", dir, 0); // NOI18N
            if (TRACE) {
                System.err.println("init search base for "+entry.getKey());
            }
        }
        return res;
    }

    private void addLibraryItem(Map<String, List<Pair>> res, Pair pkg, String prefix, FileObject dir, int loop){
        if (dir == null) {
            return;
        }
        if (loop>2) {
            return;
        }
        if (dir.isFolder() && dir.canRead()){
            for(FileObject f : dir.getChildren()){
                if (f.canRead()) {
                    if (f.isFolder()) {
                        if (loop == 0) {
                            addLibraryItem(res, pkg, f.getNameExt(), f, loop+1);// NOI18N
                        } else {
                            addLibraryItem(res, pkg, prefix+"/"+f.getNameExt(), f, loop+1); // NOI18N
                        }
                    } else if (f.isData()) {
                        String key;
                        if (prefix.length()==0) {
                            key = f.getNameExt();
                        } else {
                            key = prefix+"/"+f.getNameExt(); // NOI18N
                        }
                        List<Pair> list = res.get(key);
                        if (list == null){
                            list = new ArrayList<Pair>(1);
                            res.put(key, list);
                        }
                        if (!list.contains(pkg)){
                            list.add(pkg);
                            //if (list.size() > 1) {
                            //    System.out.println("Name conflict '"+key+"' in packages '"+pkg+"' and '"+list.get(0)+"'"); // NOI18N
                            //}
                        }
                    }
                }
            }
        }
    }

//prefix=/usr
//prefix=${pcfiledir}/../..
//exec_prefix=${prefix}
//libdir=${exec_prefix}/lib
//includedir=${prefix}/include
//target=x11
//
//gtk_binary_version=2.4.0
//gtk_host=i386-pc-solaris2.10
//
//Name: GTK+
//Description: GIMP Tool Kit (${target} target)
//Version: 2.4.9
//Requires: gdk-${target}-2.0 atk
//0123456789
//Libs: -L${libdir} -lgtk-${target}-2.0
//Cflags: -I${includedir}/gtk-2.0

    private void readConfig(FileObject file, PackageConfigurationImpl pc, boolean isWindows) {
        try {
            String rootName = null;
            String rootValue = null;
            Map<String, String> vars = new HashMap<String, String>();
            if (file.getParent() != null) {
                vars.put("pcfiledir", file.getParent().getPath()); // NOI18N
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(file.getInputStream()));
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();
                if (line.startsWith("#")) { // NOI18N
                    continue;
                }
                int sharp = line.indexOf('#'); // NOI18N
                if (sharp > 0) {
                    line = line.substring(0,sharp).trim();
                }
                if (line.startsWith("Requires:")){ // NOI18N
                    String value = line.substring(9).trim();
                    value = expandMacros(value,vars);
                    StringTokenizer st = new StringTokenizer(value, " ,"); // NOI18N
                    while(st.hasMoreTokens()) {
                        String s = st.nextToken();
                        if (s.startsWith("<") || s.startsWith(">") || s.startsWith("=")|| Character.isDigit(s.charAt(0))){ // NOI18N
                            continue;
                        }
                        pc.requires.add(s);
                    }
                } else if (line.startsWith("Requires.private:")){ // NOI18N
                    if (true){
                        // It seems the pkg-config has a bug. It shouln't take into account "Requires.private" for --cflags option.
                        // See discussion: http://lists.freedesktop.org/archives/pkg-config/2009-February/000410.html
                        String value = line.substring(17).trim();
                        value = expandMacros(value,vars);
                        StringTokenizer st = new StringTokenizer(value, " ,"); // NOI18N
                        while(st.hasMoreTokens()) {
                            String s = st.nextToken();
                            if (s.startsWith("<") || s.startsWith(">") || s.startsWith("=")|| Character.isDigit(s.charAt(0))){ // NOI18N
                                continue;
                            }
                            pc.requires.add(s);
                        }
                    }
                } else if (line.startsWith("Version:")){ // NOI18N
                    pc.version = line.substring(8).trim();
                } else if (line.startsWith("Description:")){ // NOI18N
                    pc.displayName = line.substring(12).trim();
                } else if (line.startsWith("Libs:")){ // NOI18N
                    String value = line.substring(5).trim();
                    value = expandMacros(value,vars);
                    pc.libs = value;
                } else if (line.startsWith("Cflags:")){ // NOI18N
                    String value = line.substring(7).trim();
                    value = expandMacros(value,vars);
                    StringTokenizer st = new StringTokenizer(value, " "); // NOI18N
                    while(st.hasMoreTokens()) {
                        String v = st.nextToken();
                        if (v.startsWith("-I")){ // NOI18N
                            v = v.substring(2);
                            if (isWindows) {
                                if (v.length()>2 && v.charAt(1) == ':') {
                                    if (rootName != null && v.startsWith(rootName)) {
                                        if (rootValue != null) {
                                            v = rootValue+v.substring(rootName.length());
                                        } else if (drivePrefix != null) {
                                            v = drivePrefix+v.substring(rootName.length());
                                        }
                                    }
                                } else {
                                    if (rootValue != null) {
                                        if (v.startsWith(rootName)) {
                                            v = rootValue+v.substring(rootName.length());
                                        } else {
                                            v = rootValue+v;
                                        }
                                    } else if (drivePrefix != null) {
                                        v = drivePrefix+v;
                                    }
                                    if (v.indexOf("/usr/lib/") > 0) { // NOI18N
                                        v = v.replace("/usr/lib/", "/lib/"); // NOI18N
                                    }
                                }
                                if (TRACE) {
                                    if (!new File(v).exists()) {
                                        System.err.println("Not found path: "+v); // NOI18N
                                        System.err.println("\tValue: "+value); // NOI18N
                                        System.err.println("\tRoot Path: "+rootValue); // NOI18N
                                        System.err.println("\tRoot Name: "+rootName); // NOI18N
                                    }
                                }
                            }
                            pc.paths.add(v);
                        } else if (v.startsWith("-D")){ // NOI18N
                            pc.macros.add(v.substring(2));
                        }
                    }
                } else if (line.indexOf('=')>0){ // NOI18N
                    int i = line.indexOf('='); // NOI18N
                    String name = line.substring(0, i).trim();
                    String value = line.substring(i+1).trim();
                    if (isWindows && name.equals("prefix")) { // NOI18N
                        rootName = value;
                        rootValue = fixPrefixPath(value, file);
                    }
                    vars.put(name, expandMacros(value, vars));
                }
            }
            in.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace(System.err);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    private String fixPrefixPath(String value, FileObject file){
        //prefix=c:/devel/target/e1cabcfbab6c7ee30ed3ffc781169bba
        StringTokenizer st = new StringTokenizer(value, "\\/"); // NOI18N
        while(st.hasMoreTokens()){
            String s = st.nextToken();
            if (s.length() == 32) {
                boolean isHashCode = true;
                for(int i = 0; i < 32; i++){
                    char c = s.charAt(i);
                    switch(c){
                        case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9': // NOI18N
                        case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': // NOI18N
                        case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': // NOI18N
                            continue;
                        default:
                            isHashCode = false;
                            break;
                    }
                }
                if (isHashCode) {
                    file = file.getParent();
                    if (file != null) {
                        file = file.getParent();
                    }
                    if (file != null) {
                        file = file.getParent();
                    }
                    if (file != null) {
                        return file.getPath();
                    }
                }
            }
        }
        if (value.startsWith("/")) { // NOI18N
            file = file.getParent();
            if (file != null) {
                file = file.getParent();
            }
            if (file != null) {
                file = file.getParent();
            }
            if (file != null) {
                FileObject fileObject = file.getFileObject(value.substring(1));
                if (fileObject != null && fileObject.isValid() && fileObject.isFolder()) {
                    return fileObject.getPath();
                }
                return file.getPath();
            }
        }
        return null;
    }

    private String expandMacros(String value, Map<String, String> vars){
        if (value.indexOf("${")>=0) { // NOI18N
            while(value.indexOf("${")>=0) { // NOI18N
                int i = value.indexOf("${"); // NOI18N
                int j = value.indexOf('}'); // NOI18N
                if (j < i) {
                    break;
                }
                String macro = value.substring(i+2, j);
                String v = vars.get(macro);
                if (v == null || v.indexOf("${")>=0) { // NOI18N
                    break;
                }
                value = value.substring(0,i)+v+value.substring(j+1);
            }
        }
        return value;
    }

    /*package-local*/ static class PackageConfigurationImpl implements PackageConfiguration {
        List<String> requires = new ArrayList<String>();
        List<String> macros = new ArrayList<String>();
        List<String> paths = new ArrayList<String>();
        String libs = "";
        private final String name;
        private String version;
        private String displayName;
        private PackageConfigurationImpl(String name){
            this.name = name;
        }

        @Override
        public Collection<String> getIncludePaths() {
            return new ArrayList<String>(paths);
        }

        @Override
        public Collection<String> getMacros() {
            return new ArrayList<String>(macros);
        }

        @Override
        public String getLibs() {
            return libs;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDisplayName() {
            if (displayName != null) {
                return displayName;
            }
            return name;
        }

        @Override
        public String getVersion() {
            return version;
        }

        @Override
        public String toString() {
            return name+" "+paths+" "+macros; // NOI18N
        }

    }

    /*package-local*/ class ResolvedPathImpl implements ResolvedPath {
        private final String path;
        private final Set<PackageConfiguration> packages;
        private ResolvedPathImpl(String path, Set<PackageConfiguration> packages){
            this.path = path;
            this.packages = packages;
        }

        @Override
        public String getIncludePath() {
            return path;
        }

        @Override
        public Collection<PackageConfiguration> getPackages() {
            List<PackageConfiguration> res = new ArrayList<PackageConfiguration>(packages.size());
            for(PackageConfiguration pc : packages){
                res.add(getPkgConfig(pc.getName()));
            }
            return res;
        }
    }

    private static class Pair {
        private String path;
        private Set<PackageConfiguration> configurations;
        private Pair(String path, Set<PackageConfiguration> configurations){
            this.path = path;
            this.configurations = configurations;
        }
    }
}
