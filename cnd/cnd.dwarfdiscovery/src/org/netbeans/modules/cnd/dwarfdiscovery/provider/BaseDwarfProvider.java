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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.netbeans.modules.cnd.discovery.api.ApplicableImpl;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface.Position;
import org.netbeans.modules.cnd.discovery.api.DiscoveryUtils;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty;
import org.netbeans.modules.cnd.discovery.api.ProviderPropertyType;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.dwarfdiscovery.RemoteJavaExecution;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnitInterface;
import org.netbeans.modules.cnd.dwarfdump.source.SourceFile;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.LANG;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;
import org.netbeans.modules.cnd.dwarfdump.reader.ElfReader.SharedLibraries;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;

/**
 *
 */
public abstract class BaseDwarfProvider extends BaseProvider {
    
    protected final ProviderProperty<FileSystem> BYNARY_FILESYSTEM_PROPERTY;
    private Map<String,GrepEntry> grepBase = new ConcurrentHashMap<String, GrepEntry>();

    public BaseDwarfProvider() {
        BYNARY_FILESYSTEM_PROPERTY = new ProviderProperty<FileSystem>(){
            private FileSystem fs;
            @Override
            public String getName() {
                return ""; // NOI18N
            }
            @Override
            public String getDescription() {
                return ""; // NOI18N
            }
            @Override
            public FileSystem getValue() {
                return fs;
            }
            @Override
            public void setValue(FileSystem value) {
                fs = value;
            }
            @Override
            public ProviderPropertyType<FileSystem> getPropertyType() {
                return ProviderPropertyType.BinaryFileSystemPropertyType;
            }
        };
    }
    
    @Override
    protected void after() {
        grepBase.clear();
        grepBase = new ConcurrentHashMap<String, GrepEntry>();
    }

    protected ApplicableImpl sizeComilationUnit(ProjectProxy project, Collection<String> objFileNames, Set<String> dlls, boolean findMain){
        int res = 0;
        int sunStudio = 0;
        Dwarf dump = null;
        Position position = null;
        List<String> errors = new ArrayList<String>();
        List<String> searchPaths = new ArrayList<String>();
        TreeMap<String,AtomicInteger> realRoots = new TreeMap<String,AtomicInteger>();
        TreeMap<String,AtomicInteger> roots = new TreeMap<String,AtomicInteger>();
        int foundDebug = 0;
        Map<String, AtomicInteger> compilers = new HashMap<String, AtomicInteger>();
        for(String objFileName : objFileNames) {
            try{
                dump = new Dwarf(objFileName);
                Dwarf.CompilationUnitIterator iterator = dump.iteratorCompilationUnits();
                while (iterator.hasNext()) {
                    CompilationUnitInterface cu = iterator.next();
                    if (cu != null) {
                        if (cu.getSourceFileName() == null) {
                            continue;
                        }
                        String lang = cu.getSourceLanguage();
                        if (lang == null) {
                            continue;
                        }
                        foundDebug++;
                        String path = cu.getSourceFileAbsolutePath();
                        path = getCommpilerSettings().getNormalizedPath(path);
                        if (!CndFileUtils.isExistingFile(path)) {
                            String fileFinder = Dwarf.fileFinder(objFileName, path);
                            if (fileFinder != null) {
                                fileFinder = getCommpilerSettings().getNormalizedPath(fileFinder);
                                if (!CndFileUtils.isExistingFile(fileFinder)) {
                                    continue;
                                } else {
                                    path = fileFinder;
                                }
                            } else {
                                continue;
                            }
                        }
                        ItemProperties.LanguageKind language;
                        if (LANG.DW_LANG_C.toString().equals(lang) ||
                                LANG.DW_LANG_C89.toString().equals(lang) ||
                                LANG.DW_LANG_C99.toString().equals(lang)) {
                            language = ItemProperties.LanguageKind.C;
                            res++;
                        } else if (LANG.DW_LANG_C_plus_plus.toString().equals(lang)) {
                            language = ItemProperties.LanguageKind.CPP;
                            res++;
                        } else if (LANG.DW_LANG_Fortran77.toString().equals(lang) ||
                               LANG.DW_LANG_Fortran90.toString().equals(lang) ||
                               LANG.DW_LANG_Fortran95.toString().equals(lang)) {
                            language = ItemProperties.LanguageKind.Fortran;
                            res++;
                        } else {
                            continue;
                        }
                        incrementRoot(path, roots);
                        if (project.resolveSymbolicLinks()) {
                            String resolvedLink = DiscoveryUtils.resolveSymbolicLink(getSourceFileSystem(), path);
                            if (resolvedLink != null) {
                                incrementRoot(resolvedLink, roots);
                            }
                        }
                        String compilerName = DwarfSource.extractCompilerName(cu, language);
                        if (compilerName != null) {
                            AtomicInteger count = compilers.get(compilerName);
                            if (count == null) {
                                count = new AtomicInteger();
                                compilers.put(compilerName, count);
                            }
                            count.incrementAndGet();
                        }
                        if (DwarfSource.isSunStudioCompiler(cu)) {
                            sunStudio++;
                        }
                        if (findMain && position == null) {
                            if (cu.hasMain()) {
                                int line = cu.getMainLine();
                                if (line >0 ) {
                                    position = new MyPosition(path, line);
                                } else {
                                    position = new MyPosition(path, 1);
                                }
                            }
                        }
                    }
                }
                if (dlls != null) {
                    SharedLibraries pubNames = dump.readPubNames();
                    synchronized (dlls) {
                        for (String dll : pubNames.getDlls()) {
                            if (!dlls.contains(dll)) {
                                dlls.add(dll);
                            }
                        }
                        for(String s : pubNames.getPaths()) {
                            if (!searchPaths.contains(s)) {
                                searchPaths.add(s);
                            }
                        }
                    }
                }
            } catch (FileNotFoundException ex) {
                errors.add(NbBundle.getMessage(BaseDwarfProvider.class, "FileNotFoundException", objFileName));  // NOI18N
                if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                    DwarfSource.LOG.log(Level.FINE, "File not found {0}: {1}", new Object[]{objFileName, ex.getMessage()});  // NOI18N
                }
            } catch (WrongFileFormatException ex) {
                errors.add(NbBundle.getMessage(BaseDwarfProvider.class, "WrongFileFormatException", objFileName));  // NOI18N
                if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                    DwarfSource.LOG.log(Level.FINE, "Unsuported format of file {0}: {1}", new Object[]{objFileName, ex.getMessage()});  // NOI18N
                }
            } catch (IOException ex) {
                errors.add(NbBundle.getMessage(BaseDwarfProvider.class, "IOException", objFileName, ex.toString()));  // NOI18N
                DwarfSource.LOG.log(Level.INFO, "Exception in file " + objFileName, ex);  // NOI18N
            } catch (Throwable ex) {
                errors.add(NbBundle.getMessage(BaseDwarfProvider.class, "Exception", objFileName, ex.toString()));  // NOI18N
                DwarfSource.LOG.log(Level.INFO, "Exception in file " + objFileName, ex);  // NOI18N
            } finally {
                if (dump != null) {
                    dump.dispose();
                }
            }
        }
        int max = 0;
        String top = "";
        for(Map.Entry<String, AtomicInteger> entry : compilers.entrySet()){
            if (entry.getValue().get() > max) {
                max = entry.getValue().get();
                top = entry.getKey();
            }
        }
        ArrayList<String> dllResult = null;
        if (dlls != null) {
            dllResult = new ArrayList<String>(dlls);
        }
        ArrayList<String> pathsResult = null;
        if (dlls != null) {
            pathsResult = new ArrayList<String>(searchPaths);
        }
        String commonRoot = getRoot(roots);
        if (res > 0) {
            return new ApplicableImpl(true, errors, top, res, sunStudio > res/2, dllResult, pathsResult, commonRoot, position);
        } else {
            if (errors.isEmpty()) {
                if (foundDebug > 0) {
                    String notFoundRoot = getRoot(realRoots);
                    errors.add(NbBundle.getMessage(BaseDwarfProvider.class, "BadDebugInformation", notFoundRoot));  // NOI18N
                } else {
                    StringBuilder buf = new StringBuilder();
                    for(String s : objFileNames) {
                        if (buf.length()>0) {
                            buf.append(';');
                        }
                        buf.append(s);
                    }
                    errors.add(NbBundle.getMessage(BaseDwarfProvider.class, "NotFoundDebugInformation", buf.toString()));  // NOI18N
                }
            }
            return new ApplicableImpl(false, errors, top, res, sunStudio > res/2, dllResult, pathsResult, commonRoot, position);
        }
    }

    private void incrementRoot(String path, Map<String,AtomicInteger> roots) {
        path = path.replace('\\', '/');
        int i = path.lastIndexOf('/');
        if (i >= 0) {
            String folder = path.substring(0, i);
            AtomicInteger val = roots.get(folder);
            if (val == null) {
                val = new AtomicInteger();
                roots.put(folder, val);
            }
            val.incrementAndGet();
        }
    }
    
    private String getCommonPart(String path, String commonRoot) {
        String[] splitPath = path.split("/"); // NOI18N
        ArrayList<String> list1 = new ArrayList<String>();
        boolean isUnixPath = false;
        for (int i = 0; i < splitPath.length; i++) {
            if (!splitPath[i].isEmpty()) {
                list1.add(splitPath[i]);
            } else {
                if (i == 0) {
                    isUnixPath = true;
                }
            }
        }
        String[] splitRoot = commonRoot.split("/"); // NOI18N
        ArrayList<String> list2 = new ArrayList<String>();
        boolean isUnixRoot = false;
        for (int i = 0; i < splitRoot.length; i++) {
            if (!splitRoot[i].isEmpty()) {
                list2.add(splitRoot[i]);
            } else {
                if (i == 0) {
                    isUnixRoot = true;
                }
            }
        }
        if (isUnixPath != isUnixRoot) {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        if (isUnixPath) {
            buf.append('/');
        }
        for (int i = 0; i < Math.min(list1.size(), list2.size()); i++) {
            if (list1.get(i).equals(list2.get(i))) {
                if (i > 0) {
                    buf.append('/');
                }
                buf.append(list1.get(i));
            } else {
                break;
            }
        }
        return buf.toString();
    }

    private String getRoot(TreeMap<String,AtomicInteger> roots) {
        ArrayList<String> res = new ArrayList<String>();
        ArrayList<AtomicInteger> resCount = new ArrayList<AtomicInteger>();
        String current = null;
        AtomicInteger currentCount = null;
        for(Map.Entry<String,AtomicInteger> entry : roots.entrySet()) {
            if (current == null) {
                current = entry.getKey();
                currentCount = new AtomicInteger(entry.getValue().get());
                continue;
            }
            String s = getCommonPart(entry.getKey(), current);
            String[] split = s.split("/"); // NOI18N
            int length = (split.length > 0 && split[0].isEmpty()) ? split.length - 1 : split.length;
            if (length >= 2) {
                current = s;
                currentCount.addAndGet(entry.getValue().get());
            } else {
                res.add(current);
                resCount.add(currentCount);
                current = entry.getKey();
                currentCount = new AtomicInteger(entry.getValue().get());
            }
        }
        if (current != null) {
            res.add(current);
            resCount.add(currentCount);
        }
        TreeMap<String,AtomicInteger> newRoots = new TreeMap<String, AtomicInteger>();
        String bestRoot = null;
        int bestCount = 0;
        for(int i = 0; i < res.size(); i++) {
            newRoots.put(res.get(i), resCount.get(i));
            if (bestRoot == null) {
                bestRoot = res.get(i);
                bestCount = resCount.get(i).get();
            } else {
                if (bestCount < resCount.get(i).get()) {
                    bestRoot = res.get(i);
                    bestCount = resCount.get(i).get();
                }
            }
        }
        return bestRoot;
    }
    
    @Override
    protected List<SourceFileProperties> getSourceFileProperties(String objFileName, Map<String, SourceFileProperties> map, ProjectProxy project, Set<String> dlls,
            List<String> buildArtifacts, Map<ItemProperties.LanguageKind,Map<String,Integer>> buildTools, CompileLineStorage storage) {
        FileSystem fs = BYNARY_FILESYSTEM_PROPERTY.getValue();
        if (fs == null || CndFileUtils.isLocalFileSystem(fs)) {
            return getSourceFilePropertiesLocal(objFileName, map, project, dlls, buildArtifacts, buildTools, storage);
        } else {
            return getSourceFilePropertiesRemote(objFileName, map, project, dlls, buildArtifacts, buildTools, storage);
        }
    }

    private List<SourceFileProperties> getSourceFilePropertiesRemote(String objFileName, Map<String, SourceFileProperties> map, ProjectProxy project, Set<String> dlls,
            List<String> buildArtifacts, Map<ItemProperties.LanguageKind,Map<String,Integer>> buildTools, CompileLineStorage storage) {
        List<SourceFileProperties> list = new ArrayList<SourceFileProperties>();
        FileSystem fs = BYNARY_FILESYSTEM_PROPERTY.getValue();
        ExecutionEnvironment ee = FileSystemProvider.getExecutionEnvironment(fs);
        if (ConnectionManager.getInstance().isConnectedTo(ee)) {
            RemoteJavaExecution processor = new RemoteJavaExecution(fs);
            try {
                for (SourceFile cu : processor.getCompileLines(objFileName, true)) {
                    if (getStopInterrupter().cancelled()) {
                        break;
                    }
                    processUnit(cu, objFileName, storage, map, project, list);
                }
                if (dlls != null) {
                    SharedLibraries pubNames = processor.getDlls(objFileName);
                    synchronized(dlls) {
                        for(String dll : pubNames.getDlls()) {
                            dlls.add(dll);
                        }

                    }
                }
            } catch (IOException ex) {
                DwarfSource.LOG.log(Level.INFO, "Exception in file " + objFileName, ex);  // NOI18N
            }
        }
        return list;
    }
    
    
    private List<SourceFileProperties> getSourceFilePropertiesLocal(String objFileName, Map<String, SourceFileProperties> map, ProjectProxy project, Set<String> dlls,
            List<String> buildArtifacts, Map<ItemProperties.LanguageKind,Map<String,Integer>> buildTools, CompileLineStorage storage) {
        List<SourceFileProperties> list = new ArrayList<SourceFileProperties>();
        Dwarf dump = null;
        try {
            if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                DwarfSource.LOG.log(Level.FINE, "Process file {0}", objFileName);  // NOI18N
            }
            dump = new Dwarf(objFileName);
            Dwarf.CompilationUnitIterator iterator = dump.iteratorCompilationUnits();
            while (iterator.hasNext()) {
                CompilationUnitInterface cu = iterator.next();
                if (cu != null) {
                    if (getStopInterrupter().cancelled()) {
                        break;
                    }
                    processUnit(cu, objFileName, storage, map, project, list);
                }
            }
            if (dlls != null) {
                SharedLibraries pubNames = dump.readPubNames();
                synchronized(dlls) {
                    for(String dll : pubNames.getDlls()) {
                        dlls.add(dll);
                    }

                }
            }
        } catch (FileNotFoundException ex) {
            // Skip Exception
            if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                DwarfSource.LOG.log(Level.FINE, "File not found {0}: {1}", new Object[]{objFileName, ex.getMessage()});  // NOI18N
            }
        } catch (WrongFileFormatException ex) {
            if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                DwarfSource.LOG.log(Level.FINE, "Unsuported format of file {0}: {1}", new Object[]{objFileName, ex.getMessage()});  // NOI18N
            }
        } catch (IOException ex) {
            DwarfSource.LOG.log(Level.INFO, "Exception in file " + objFileName, ex);  // NOI18N
        } catch (Throwable ex) {
            DwarfSource.LOG.log(Level.INFO, "Exception in file " + objFileName, ex);  // NOI18N
        } finally {
            if (dump != null) {
                dump.dispose();
            }
        }
        return list;
    }

    private void processUnit(CompilationUnitInterface cu, String objFileName, CompileLineStorage storage, Map<String, SourceFileProperties> map, ProjectProxy project, List<SourceFileProperties> list) throws IOException {
        if (cu.getSourceFileName() == null) {
            if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                DwarfSource.LOG.log(Level.FINE, "Compilation unit has broken name in file {0}", objFileName);  // NOI18N
            }
            return;
        }
        String lang = cu.getSourceLanguage();
        if (lang == null) {
            if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                DwarfSource.LOG.log(Level.FINE, "Compilation unit has unresolved language in file {0}for {1}", new Object[]{objFileName, cu.getSourceFileName()});  // NOI18N
            }
            return;
        }
        DwarfSource source = null;
        if (LANG.DW_LANG_C.toString().equals(lang)) {
            source = new DwarfSource(cu, ItemProperties.LanguageKind.C, ItemProperties.LanguageStandard.C, getCommpilerSettings(), grepBase, storage);
        } else if (LANG.DW_LANG_C89.toString().equals(lang)) {
            source = new DwarfSource(cu, ItemProperties.LanguageKind.C, ItemProperties.LanguageStandard.C89, getCommpilerSettings(), grepBase, storage);
        } else if (LANG.DW_LANG_C99.toString().equals(lang)) {
            source = new DwarfSource(cu, ItemProperties.LanguageKind.C, ItemProperties.LanguageStandard.C99, getCommpilerSettings(), grepBase, storage);
        } else if (LANG.DW_LANG_C_plus_plus.toString().equals(lang)) {
            source = new DwarfSource(cu, ItemProperties.LanguageKind.CPP, ItemProperties.LanguageStandard.Unknown, getCommpilerSettings(), grepBase, storage);
        } else if (LANG.DW_LANG_Fortran77.toString().equals(lang)) {
            source = new DwarfSource(cu, ItemProperties.LanguageKind.Fortran, ItemProperties.LanguageStandard.F77, getCommpilerSettings(), grepBase, storage);
        } else if (LANG.DW_LANG_Fortran90.toString().equals(lang)) {
            source = new DwarfSource(cu, ItemProperties.LanguageKind.Fortran, ItemProperties.LanguageStandard.F90, getCommpilerSettings(), grepBase, storage);
        } else if (LANG.DW_LANG_Fortran95.toString().equals(lang)) {
            source = new DwarfSource(cu, ItemProperties.LanguageKind.Fortran, ItemProperties.LanguageStandard.F95, getCommpilerSettings(), grepBase, storage);
        } else {
            if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                DwarfSource.LOG.log(Level.FINE, "Unknown language: {0}", lang);  // NOI18N
            }
            // Ignore other languages
        }
        if (source != null) {
            if (source.getCompilePath() == null) {
                if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                    DwarfSource.LOG.log(Level.FINE, "Compilation unit has NULL compile path in file {0}", objFileName);  // NOI18N
                }
                return;
            }
            String name = source.getItemPath();
            source.process(cu);
            if (project.resolveSymbolicLinks()) {
                String resolvedLink = DiscoveryUtils.resolveSymbolicLink(getSourceFileSystem(), name);
                if (resolvedLink != null) {
                    DwarfSource original = DwarfSource.relocateDerivedSourceFile(source, resolvedLink);
                    list.add(original);
                    return;
                }
            }
            list.add(source);
        }
    }
    
    public static final class GrepEntry {
        ArrayList<String> includes = new ArrayList<String>();
        String firstMacro = null;
        int firstMacroLine = -1;
        boolean exists;
    }

    private static final class MyPosition implements Position {
        private final String path;
        private final int line;

        private MyPosition(String path, int line){
            this.path = path;
            this.line = line;
        }

        @Override
        public String getFilePath() {
            return path;
        }

        @Override
        public int getLine() {
            return line;
        }

        @Override
        public String toString() {
            return path+":"+line; //NOI18N
        }
    }
}
