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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.project.IncludePath;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.ItemProperties.LanguageKind;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager.PackageConfiguration;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager.PkgConfig;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager.ResolvedPath;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileSystem;
import org.openide.util.Utilities;

/**
 *
 */
public class ModelSource implements SourceFileProperties {
    private static final boolean TRACE_AMBIGUOUS = Boolean.getBoolean("cnd.modeldiscovery.trace.ambiguous"); // NOI18N
    private static final int MAX_DEPTH = 4;
    private static final Logger logger = Logger.getLogger("org.netbeans.modules.cnd.modeldiscovery.provider.SourceFileProperties"); // NOI18N
    {
        if (TRACE_AMBIGUOUS){logger.setLevel(Level.ALL);}
    }

    private final Item item;
    private final CsmFile file;
    private final Map<String,List<String>> searchBase;
    private final Map<String,Item> projectSearchBase;
    private final PkgConfig pkgConfig;
    private String itemPath;
    private List<String> userIncludePaths;
    private final Set<CharSequence> includedFiles = new HashSet<CharSequence>();
    private Map<String,String> userMacros;
    private final boolean preferLocal;
    private final FileSystem sourceFileSystem;
    
    public ModelSource(Item item, CsmFile file, Map<String,List<String>> searchBase, Map<String,Item> projectSearchBase, PkgConfig pkgConfig, boolean preferLocal){
        this.item = item;
        this.file = file;
        this.searchBase = searchBase;
        this.projectSearchBase = projectSearchBase;
        this.pkgConfig = pkgConfig;
        this.preferLocal = preferLocal;
        sourceFileSystem = item.getFSPath().getFileSystem();
    }

    public Set<CharSequence> getIncludedFiles() {
        if (userIncludePaths == null) {
            getUserInludePaths();
        }
        return includedFiles;
    }

    @Override
    public String getCompilePath() {
        return CndPathUtilities.getDirName(getItemPath());
    }
    
    @Override
    public String getItemPath() {
        if (itemPath == null) {
            itemPath = item.getAbsPath();
            itemPath = itemPath.replace('\\','/');
            itemPath = cutLocalRelative(itemPath);
            if (Utilities.isWindows() && CndFileUtils.isLocalFileSystem(sourceFileSystem)) {
                itemPath = itemPath.replace('/', File.separatorChar);
            }
        }
        return itemPath;
    }
    
    private static final String PATTERN = "/../"; // NOI18N
    public static String cutLocalRelative(String path){
        String pattern = PATTERN;
        while(true) {
            int i = path.indexOf(pattern);
            if (i < 0){
                break;
            }
            int k = -1;
            for (int j = i-1; j >= 0; j-- ){
                if ( path.charAt(j)=='/'){
                    k = j;
                    break;
                }
            }
            if (k<0) {
                break;
            }
            path = path.substring(0,k+1)+path.substring(i+pattern.length());
        }
        return path;
    }
    
    @Override
    public String getItemName() {
        return item.getName();
    }
    
    @Override
    public List<String> getUserInludePaths() {
        if (userIncludePaths == null) {
            List<String> includePaths = IncludePath.toStringList(item.getUserIncludePaths());
            Set<String> res = new LinkedHashSet<String>();
            for(String path : includePaths){
                path = getRelativepath(path);
                res.add(path);
            }
            analyzeUnresolved(res,file, 0);
            userIncludePaths = new ArrayList<String>(res);
        }
        return userIncludePaths;
    }
    
    @Override
    public List<String> getUserInludeFiles() {
        return Collections.emptyList();
    }
    
    private String getRelativepath(String path){
        if (Utilities.isWindows() && CndFileUtils.isLocalFileSystem(sourceFileSystem)) {
            path = path.replace('/', File.separatorChar);
        }
        path = CndPathUtilities.toRelativePath(getCompilePath(), path);
        path = CndPathUtilities.normalizeSlashes(path);
        return path;
    }
    
    private void analyzeUnresolved(Set<String> res, CsmFile what, int level){
        if (what == null) {
            return;
        }
        for (CsmInclude include : what.getIncludes()){
            CsmFile resolved = include.getIncludeFile();
            if (resolved == null){
                // unresolved include
                String path = guessPath(include);
                if (path != null) {
                    String fullPath = CndFileUtils.normalizeAbsolutePath(path + File.separatorChar + include.getIncludeName());
                    if (!file.getProject().isArtificial()) {
                        Item aItem = projectSearchBase.get(fullPath);
                        if (aItem != null) {
                            resolved = file.getProject().findFile(aItem, true, false);
                        }
                    } else {
                        resolved = file.getProject().findFile(fullPath, true, false);
                    }
                    path = getRelativepath(path);
                    res.add(path);
                    if (level < MAX_DEPTH && resolved != null) {
                        analyzeUnresolved(res, resolved, level+1);
                    }
                } else {
                    if (pkgConfig != null) {
                        Collection<ResolvedPath> listRP = pkgConfig.getResolvedPath(include.getIncludeName().toString());
                        if (listRP != null && !listRP.isEmpty()) {
                            ResolvedPath rp = listRP.iterator().next();
                            res.add(rp.getIncludePath());
                            for(PackageConfiguration pc : rp.getPackages()){
                                for(String p : pc.getIncludePaths()){
                                    if (!getSystemInludePaths().contains(p)){
                                        res.add(p);
                                    }
                                }
                                for(String p : pc.getMacros()){
                                    int i = p.indexOf('='); // NOI18N
                                    String macro;
                                    String value = null;
                                    if (i > 0){
                                        macro = p.substring(0, i);
                                        value = p.substring(i+1);
                                    } else {
                                        macro = p;
                                    }
                                    if (!getUserMacros().containsKey(macro)){
                                        getUserMacros().put(macro, value);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                boolean reResolve = false;
                if (preferLocal) {
                    boolean isSystem = false;
                    String resolvedPath = resolved.getAbsolutePath().toString();
                    for (String path : getSystemInludePaths()){
                        if (resolvedPath.startsWith(path)) {
                            isSystem = true;
                            break;
                        }
                    }
                    if (isSystem) {
                        String path = guessPath(include);
                        if (path != null && !resolvedPath.startsWith(path)){
                            if (TRACE_AMBIGUOUS) {
                                logger.log(Level.FINE, "Directive resolved in project on path: {0} instead {1}", new Object[]{path, resolvedPath}); // NOI18N
                            }
                            String fullPath = CndFileUtils.normalizeAbsolutePath(path+File.separatorChar+include.getIncludeName());
                            resolved = file.getProject().findFile(fullPath, true, false);
                            path = getRelativepath(path);
                            res.add(path);
                            reResolve = true;
                        }
                    }
                }
                if (!reResolve) {
                    includedFiles.add(resolved.getAbsolutePath());
                }
                if (level < MAX_DEPTH && resolved != null) {
                    analyzeUnresolved(res, resolved, level+1);
                }
            }
        }
    }
    
    private String guessPath(CsmInclude include){
        String name = include.getIncludeName().toString();
        String found = name.replace('\\','/');
        //String back = null;
        int i = found.lastIndexOf('/');
        if(i >= 0){
            String prefix = found.substring(0,i+1);
            found = found.substring(i+1);
            i = prefix.lastIndexOf("./"); // NOI18N
            if (i >= 0) {
                //back = prefix.substring(0,i+2);
                prefix = prefix.substring(i+2);
                if (prefix.length()==0) {
                    name = found;
                } else {
                    name = prefix+'/'+found;
                }
            }
        }
        List<String> result = searchBase.get(found);
        if (result != null && result.size()>0){
            int pos = -1;
            //TODO: resolve ambiguously
            for(int j = 0; j < result.size(); j++){
                if (result.get(j).endsWith(name)){
                    if (pos >= 0) {
                        if (TRACE_AMBIGUOUS) {
                            logger.log(Level.FINE, "Ambiguous name for item: {0}", getItemPath()); // NOI18N
                            logger.log(Level.FINE, "  name1: {0}", result.get(pos)); // NOI18N
                            logger.log(Level.FINE, "  name2: {0}", result.get(j)); // NOI18N
                        }
                    } else {
                        pos = j;
                    }
                }
            }
            if (pos >=0) {
                String path = result.get(pos);
                path = path.substring(0,path.length()-name.length()-1);
                return path;
            }
        }
        if (TRACE_AMBIGUOUS) {
            logger.log(Level.FINE, "Unresolved name for item: {0}", getItemPath()); // NOI18N
            logger.log(Level.FINE, "  from: {0}", include.getContainingFile().getAbsolutePath()); // NOI18N
            logger.log(Level.FINE, "  name: {0}", include.getIncludeName()); // NOI18N
            if (result != null && result.size()>0){
                for(int j = 0; j < result.size(); j++){
                    logger.log(Level.FINE, "  candidate: {0}", result.get(j)); // NOI18N
                }
            }
        }
        return null;
    }
    
    @Override
    public List<String> getSystemInludePaths() {
        return IncludePath.toStringList(item.getSystemIncludePaths());
    }
    
    @Override
    public Map<String, String> getUserMacros() {
        if (userMacros == null){
            userMacros = new HashMap<String,String>();
            List<String> macros = item.getUserMacroDefinitions();
            for(String macro : macros){
                int i = macro.indexOf('=');
                if (i>0){
                    userMacros.put(macro.substring(0,i).trim(),macro.substring(i+1).trim());
                } else {
                    userMacros.put(macro,null);
                }
            }
        }
        return userMacros;
    }

    @Override
    public List<String> getUndefinedMacros() {
        return new ArrayList<String>(item.getUndefinedMacros());
    }
    
    @Override
    public Map<String, String> getSystemMacros() {
        return null;
    }
    
    @Override
    public ItemProperties.LanguageKind getLanguageKind() {
        switch(item.getLanguage()) {
            case C:
                return LanguageKind.C;
            case CPP:
                return LanguageKind.CPP;
            case FORTRAN:
                return LanguageKind.Fortran;
        }
        return LanguageKind.Unknown;
    }

    @Override
    public String getCompilerName() {
        return null;
    }

    @Override
    public LanguageStandard getLanguageStandard() {
        switch(item.getLanguageFlavor()) {
            case C: return LanguageStandard.C;
            case C89: return LanguageStandard.C89;
            case C99: return LanguageStandard.C99;
            case C11: return LanguageStandard.C11;
            case CPP98: return LanguageStandard.CPP98;
            case CPP11: return LanguageStandard.CPP11;
            case CPP14: return LanguageStandard.CPP14;
            case CPP17: return LanguageStandard.CPP17;
            case DEFAULT: return LanguageStandard.Default;
            case F77: return LanguageStandard.F77;
            case F90: return LanguageStandard.F90;
            case F95: return LanguageStandard.F95;
            default: UNKNOWN: return LanguageStandard.Unknown;
        }
    }

    @Override
    public String getCompileLine() {
        return null;
    }

    @Override
    public String getImportantFlags() {
        return item.getImportantFlags();
    }
}

