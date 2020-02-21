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
package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmIncludeResolver;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmIncludeHierarchyResolver;
import org.netbeans.modules.cnd.api.project.IncludePath;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelimpl.content.project.GraphContainer;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;

/**
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.services.CsmIncludeResolver.class)
public final class IncludeResolverImpl extends CsmIncludeResolver {
    private final Map<String,String[]> standardCHeaders;
    private final Map<String,String[]> standardCppHeaders;
    private static final String[] EMPTY = new String[]{};
    
    public IncludeResolverImpl() {
        standardCppHeaders = loadHeadesInfo("CND/headers/CPP"); // NOI18N
        standardCHeaders = loadHeadesInfo("CND/headers/C"); // NOI18N
    }
    
    private Map<String,String[]> loadHeadesInfo(String root) {
        Map<String,String[]>standardHeaders = new HashMap<>();
        FileObject folder = FileUtil.getConfigFile(root);
        if (folder != null && folder.isFolder()) {
            for (FileObject file : folder.getChildren()) {
                String key = file.getNameExt();
                String define = (String) file.getAttribute("define"); // NOI18N
                if (define != null) {
                    standardHeaders.put(key, define.split(";")); // NOI18N
                } else {
                    standardHeaders.put(key, EMPTY);
                }
            }
        }
        return standardHeaders;
    }

    @Override
    public String getIncludeDirective(CsmFile currentFile, CsmObject item) {
        if (CsmKindUtilities.isOffsetable(item)) {
            CsmFile file = ((CsmOffsetable) item).getContainingFile();
            if (file != null) {
                if (file.equals(currentFile) || file.isHeaderFile()) {
                    return getIncludeDirectiveByFile(currentFile, item).replace('\\', '/'); // NOI18N;
                } else if (file.isSourceFile() && CsmKindUtilities.isGlobalVariable(item)) {
                    Collection<CsmOffsetableDeclaration> decls = file.getProject().findDeclarations(((CsmVariable) item).getUniqueName() + " (EXTERN)"); // NOI18N
                    if (!decls.isEmpty()) {
                        return getIncludeDirectiveByFile(currentFile, decls.iterator().next()).replace('\\', '/'); // NOI18N;
                    }
                }
            } else {
                System.err.println("can not find for item " + item); // NOI18N;
            }
        } else if (!CsmKindUtilities.isNamespace(item)) {
            System.err.println("not yet handled object " + item); // NOI18N;
        }
        return ""; // NOI18N
    }

    // Says is header standard or not
    private String[] getStandardCHeader(List<IncludePath> sysIncsPaths, CsmFile header) {
        final String path = header.getAbsolutePath().toString();
        String bestSystemPath = getRelativePath(sysIncsPaths, path);
        return standardCHeaders.get(path.substring(bestSystemPath.length() + 1));
    }

    private String[] getStandardCppHeader(List<IncludePath> sysIncsPaths, CsmFile header) {
        final String path = header.getAbsolutePath().toString();
        String bestSystemPath = getRelativePath(sysIncsPaths, path);
        return standardCppHeaders.get(path.substring(bestSystemPath.length() + 1));
    }
    
    // Returns standard header if it exists
    private void getStandardHeaderIfExists(CsmFile currentFile, List<IncludePath> sysIncsPaths, CsmFile file, HashSet<CsmFile> scannedFiles, String name, Result result) {
        if (!file.isValid() || scannedFiles.contains(file) || !isSystemHeader(currentFile, file)) {
            return;
        }
        scannedFiles.add(file);
        String[] keys = getStandardCppHeader(sysIncsPaths, file);
        if (keys != null) {
            if (name != null && keys.length > 0) {
                for(String key : keys) {
                    if (key.equals(name)) {
                        result.bestCpp = file;
                        if (result.bestC != null) {
                            return;
                        } else {
                            break;
                        }
                    }
                }
            }
            if (result.first == null) {
                result.first = file;
            }
        }
        String[] keysC = getStandardCHeader(sysIncsPaths, file);
        if (keysC != null) {
            if (name != null && keysC.length > 0) {
                for(String key : keysC) {
                    if (key.equals(name)) {
                        result.bestC = file;
                        if (result.bestCpp != null) {
                            return;
                        } else {
                            break;
                        }
                    }
                }
            }
            if (result.first == null) {
                result.first = file;
            }
        }
        CsmIncludeHierarchyResolver ihr = CsmIncludeHierarchyResolver.getDefault();
        Collection<CsmFile> files = ihr.getAllFiles(file);
        for (CsmFile f : files) {
            getStandardHeaderIfExists(currentFile, sysIncsPaths, f, scannedFiles, name, result);
            if (result.bestCpp != null && result.bestC != null) {
                return;
            }
        }
    }

    // Generates "#include *" string for item
    private String getIncludeDirectiveByFile(CsmFile currentFile, CsmObject item) {
        if (CsmKindUtilities.isOffsetable(item)) {
            if (currentFile instanceof FileImpl) {
                NativeFileItem nativeFile = ((FileImpl) currentFile).getNativeFileItem();
                CsmFile incFile = ((CsmOffsetable) item).getContainingFile();
                String incFilePath = incFile.getAbsolutePath().toString();

                StringBuilder includeDirective = new StringBuilder("#include "); // NOI18N

                if (nativeFile != null) {
                    if (isSystemHeader(currentFile, ((CsmOffsetable) item).getContainingFile())) {
                        // check is this file included into standard header
                        HashSet<CsmFile> scannedFiles = new HashSet<>();
                        String name = null;
                        if (CsmKindUtilities.isNamedElement(item)) {
                            name = ((CsmNamedElement)item).getName().toString();
                        }
                        Result result = new Result();
                        Pair<NativeFileItem.Language, NativeFileItem.LanguageFlavor> fileLanguageFlavor = FileInfoQueryImpl.getDefault().getFileLanguageFlavor((FileImpl) currentFile);
                        NativeFileItem.Language lang = fileLanguageFlavor.first();
                        List<IncludePath> systemIncludePaths;
                        if (nativeFile.getLanguage() == NativeFileItem.Language.C || nativeFile.getLanguage() == NativeFileItem.Language.CPP) {
                            systemIncludePaths = nativeFile.getSystemIncludePaths();
                        } else {
                            NativeFileItem nativeIndexer = null;
                            NativeProject nativeProject = nativeFile.getNativeProject();
                            for(NativeFileItem std : nativeProject.getStandardHeadersIndexers()) {
                                if (std.getLanguage() == lang) {
                                    nativeIndexer = std;
                                }
                            }
                            if (nativeIndexer != null) {
                                systemIncludePaths = nativeIndexer.getSystemIncludePaths();
                            } else {
                                systemIncludePaths = nativeProject.getSystemIncludePaths();
                            }
                        }
                        getStandardHeaderIfExists(currentFile, systemIncludePaths, ((CsmOffsetable) item).getContainingFile(), scannedFiles, name, result);
                        if (lang == NativeFileItem.Language.CPP) {
                            if (result.bestCpp != null) {
                                incFilePath = result.bestCpp.getAbsolutePath().toString();
                            } else if (result.bestC != null) {
                                incFilePath = result.bestC.getAbsolutePath().toString();
                            } else if (result.first != null) {
                                incFilePath = result.first.getAbsolutePath().toString();
                            }
                        } else {
                            if (result.bestC != null) {
                                incFilePath = result.bestC.getAbsolutePath().toString();
                            } else if (result.bestCpp != null) {
                                incFilePath = result.bestCpp.getAbsolutePath().toString();
                            } else if (result.first != null) {
                                incFilePath = result.first.getAbsolutePath().toString();
                            }
                        }
                        String bestSystemPath = getRelativePath(systemIncludePaths, incFilePath);
                        if (!bestSystemPath.equals("")) { // NOI18N
                            includeDirective.append("<"); // NOI18N
                            includeDirective.append(CndPathUtilities.toRelativePath(bestSystemPath, incFilePath));
                            includeDirective.append(">"); // NOI18N
                            return includeDirective.toString();
                        }
                    } else {
                        includeDirective.append("\""); // NOI18N
                        String projectPath = currentFile.getAbsolutePath().toString().substring(0,
                                currentFile.getAbsolutePath().length() - currentFile.getName().length() - 1);
                        if (!incFilePath.startsWith(projectPath)) {
                            projectPath = ""; // NOI18N
                        }
                        String bestUserPath = getRelativePath(nativeFile.getUserIncludePaths(), incFilePath);
                        if (bestUserPath.length() < projectPath.length()) {
                            includeDirective.append(CndPathUtilities.toRelativePath(projectPath, incFilePath));
                        } else {
                            includeDirective.append(CndPathUtilities.toRelativePath(bestUserPath, incFilePath));
                        }
                        if (!bestUserPath.equals("") || !projectPath.equals("")) // NOI18N
                        {
                            includeDirective.append("\""); // NOI18N
                            return includeDirective.toString();
                        }
                    }
                } else {
                    String projectPath = currentFile.getAbsolutePath().toString().substring(0, currentFile.getAbsolutePath().length() - currentFile.getName().length());
                    if (incFilePath.startsWith(projectPath)) {
                        includeDirective.append("\""); // NOI18N
                        includeDirective.append(incFilePath.substring(projectPath.length()));
                        includeDirective.append("\""); // NOI18N
                        return includeDirective.toString();
                    }
                }
            } else {
                System.err.println("not handled file instance " + currentFile);
            }
        } else if (!CsmKindUtilities.isNamespace(item)) {
            System.err.println("not yet handled object " + item);
        }
        return ""; // NOI18N
    }

    @Override
    public String getLocalIncludeDerectiveByFilePath(FSPath path, CsmObject item) {
        if (CsmKindUtilities.isOffsetable(item)) {
            CsmFile incFile = ((CsmOffsetable) item).getContainingFile();
            if (incFile != null) {
                if (incFile.isHeaderFile()) {
                    return getLocalIncludeDerectiveByHeaderFilePath(path, item).replace('\\', '/'); // NOI18N;
                } else if (incFile.isSourceFile() && CsmKindUtilities.isGlobalVariable(item)) {
                    Collection<CsmOffsetableDeclaration> decls = incFile.getProject().findDeclarations(((CsmVariable) item).getUniqueName() + " (EXTERN)"); // NOI18N
                    if (!decls.isEmpty()) {
                        return getLocalIncludeDerectiveByHeaderFilePath(path, decls.iterator().next()).replace('\\', '/'); // NOI18N;
                    }
                } else if (incFile.isSourceFile() && CsmKindUtilities.isFunctionDefinition(item)) {
                    return getLocalIncludeDerectiveByHeaderFilePath(path, ((CsmFunction) item).getDeclaration()).replace('\\', '/'); // NOI18N;
                }
            } else {
                System.err.println("can not find for item " + item); // NOI18N;
            }
        } else if (!CsmKindUtilities.isNamespace(item)) {
            System.err.println("not yet handled object " + item); // NOI18N;
        }
        return ""; // NOI18N
    }

    private String getLocalIncludeDerectiveByHeaderFilePath(FSPath path, CsmObject item) {
        CsmFile incFile = ((CsmOffsetable) item).getContainingFile();
        if(incFile.isHeaderFile()) {
            String incFilePath = incFile.getAbsolutePath().toString();

            StringBuilder includeDirective = new StringBuilder("#include "); // NOI18N
            includeDirective.append("\""); // NOI18N
            String projectPath = path.getPath();
            if (!incFilePath.startsWith(projectPath)) {
                projectPath = ""; // NOI18N
            }
            includeDirective.append(CndPathUtilities.toRelativePath(projectPath, incFilePath));
            if (!projectPath.equals("")) // NOI18N
            {
                includeDirective.append("\""); // NOI18N
                return includeDirective.toString();
            }
        }
        return "";
    }

    
    // Returns relative path for file from list of paths
    private String getRelativePath(List<IncludePath> paths, String filePath) {
        String goodPath = ""; // NOI18N
        for (IncludePath fsPath : paths) {
            String path = fsPath.getFSPath().getPath();
            if (filePath.startsWith(path)) {
                if (goodPath.length() < path.length()) {
                    goodPath = path;
                }
            }
        }
        return goodPath;
    }

    @Override
    public boolean isObjectVisible(CsmFile currentFile, CsmObject item) {
        if (CsmKindUtilities.isOffsetable(item)) {
            CsmFile file = ((CsmOffsetable) item).getContainingFile();
            if (file != null) {
                if (!file.equals(currentFile)) {
                    if (file.isHeaderFile()) {
                        if (((ProjectBase) currentFile.getProject()).getGraphStorage().isFileIncluded(currentFile, file)) {
                            return true;
                        }
                    //HashSet<CsmFile> scannedfiles = new HashSet<CsmFile>();
                    //if (isFileVisibleInIncludeFiles(currentFile.getIncludes(), file, scannedfiles)) {
                    //    return true;
                    //}
                    } else if (file.isSourceFile() && CsmKindUtilities.isGlobalVariable(item)) {
                        HashSet<CsmProject> scannedprojects = new HashSet<>();
                        if (isVariableVisible(currentFile, file.getProject(), (CsmVariable) item, scannedprojects)) {
                            return true;
                        }
                    }
                } else {
                    return true;
                }
            } else {
                return false;
            }
        } else if (CsmKindUtilities.isFile(item)) {
            CsmFile file = (CsmFile) item;
            if (!file.equals(currentFile)) {
                // TODO: think if should check only for headers
                if (file.isHeaderFile()) {
                    if (((ProjectBase) currentFile.getProject()).getGraphStorage().isFileIncluded(currentFile, file)) {
                        return true;
                    }
                }
            } else {
                return true;
            }
        } else if (!CsmKindUtilities.isNamespace(item)) {
            System.err.println("not yet handled object " + item);
        }
        return false;
    }

    @Override
    public CsmFile getCloseTopParentFile(CsmFile file) {
        if (file.isHeaderFile()) {
            String name = file.getName().toString();
            if (name.indexOf('.') > 0) {
                name = name.substring(0, name.lastIndexOf('.'));
            }
            GraphContainer.ParentFiles topParentFiles = ((ProjectBase) file.getProject()).getGraphStorage().getTopParentFiles(file);
            for(CsmUID<CsmFile> uid : topParentFiles.getCompilationUnitsUids()) {
                String aName = PathUtilities.getBaseName(UIDUtilities.getName(uid).toString());
                if (aName.indexOf('.') > 0) {
                    aName = aName.substring(0, aName.lastIndexOf('.'));
                }
                if (name.equals(aName)) {
                    return UIDCsmConverter.UIDtoFile(uid);
                }
            }
        }
        return null;
    }

    // Says is variable visible in current file
    private boolean isVariableVisible(CsmFile currentFile, CsmProject project, CsmVariable var, HashSet<CsmProject> scannedProjects) {
        if (scannedProjects.contains(project)) {
            return false;
        }
        scannedProjects.add(project);
        if (isVariableDeclarationsVisible(currentFile, project.findDeclarations(var.getUniqueName() + " (EXTERN)"))) { // NOI18N
            return true;
        }
        if (isVariableDeclarationsVisible(currentFile, project.findDeclarations(var.getUniqueName()))) {
            return true;
        }
        for (CsmProject lib : project.getLibraries()) {
            if (isVariableVisible(currentFile, lib, var, scannedProjects)) {
                return true;
            }
        }
        return false;
    }

    // Says is at least one of variable declarations visible in current file
    private boolean isVariableDeclarationsVisible(CsmFile currentFile, Collection<CsmOffsetableDeclaration> decls) {
        for (CsmOffsetableDeclaration decl : decls) {
            if(decl.getContainingFile().equals(currentFile)) {
                return true;
            }
            if (((ProjectBase)currentFile.getProject()).getGraphStorage().isFileIncluded(currentFile, decl.getContainingFile())){
                return true;
            }
            //HashSet<CsmFile> scannedFiles = new HashSet<CsmFile>();
            //if (isFileVisibleInIncludeFiles(currentFile.getIncludes(), decl.getContainingFile(), scannedFiles)) {
            //    return true;
            //}
        }
        return false;
    }

    // Says is file visible in includes
//    private boolean isFileVisibleInIncludeFiles(Collection<CsmInclude> includes, CsmFile file, HashSet<CsmFile> scannedFiles) {
//        for (CsmInclude inc : includes) {
//            CsmFile incFile = inc.getIncludeFile();
//            if (incFile != null) {
//                if (!scannedFiles.contains(incFile)) {
//                    scannedFiles.add(incFile);
//                    if (file.equals(incFile)) {
//                        return true;
//                    }
//                    if (isFileVisibleInIncludeFiles(incFile.getIncludes(), file, scannedFiles)) {
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }

    private boolean isSystemHeader(CsmFile currentFile, CsmFile header) {
        return !(currentFile.getProject().equals(header.getProject()));
    }
    
    private static final class Result {
        private CsmFile first;
        private CsmFile bestCpp;
        private CsmFile bestC;
    }
}
