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
package org.netbeans.modules.cnd.completion.includes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.project.IncludePath;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.FileObjectFilter;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.MIMESupport;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.ExtensionList;
import org.openide.util.Exceptions;

/**
 *
 *
 */
public class CsmIncludeCompletionQuery {

    private static final Collection<String> EXCLUDED_DIR_NAMES = Arrays.asList(new String[]{
                "CVS", ".hg", "nbproject", "SCCS", "SunWS_cache"}); // NOI18N
    private Map<String, CsmIncludeCompletionItem> results;
    private final CsmFile file;

    public CsmIncludeCompletionQuery(CsmFile file) {
        this.file = file;
    }

    public Collection<CsmIncludeCompletionItem> query(BaseDocument doc, String childSubDir, int substitutionOffset, int substitutionDelta, Boolean usrInclude, boolean showAll) {
        results = new HashMap<String, CsmIncludeCompletionItem>(100);
        CsmFile docFile = this.file;
        if (docFile == null) {
            docFile = CsmUtilities.getCsmFile(doc, false, false);
        }
        FileObject baseFile = null;
        if (docFile != null) {
            baseFile = docFile.getFileObject();
        }
        if (baseFile == null) {
            baseFile = CsmUtilities.getFileObject(doc);
        }
        if (baseFile == null) {
            baseFile = NbEditorUtilities.getFileObject(doc);
        }
        if (baseFile == null || !baseFile.isValid()) {
            // IZ#123039: NPE
            return Collections.<CsmIncludeCompletionItem>emptyList();
        }
        FileSystem docFileSystem;
        try {
            docFileSystem = baseFile.getFileSystem();
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
            return results.values();
        }
        Collection<IncludePath> usrPaths = Collections.<IncludePath>emptyList();
        Collection<IncludePath> sysPaths = Collections.<IncludePath>emptyList();
        if (CndPathUtilities.isPathAbsolute(childSubDir)) {
            // special handling for absolute paths...
            addFolderItems(new IncludePath(FSPath.toFSPath(docFileSystem.getRoot())),
                    "",
                    childSubDir, true, (usrInclude != null ? usrInclude : false),
                    true, substitutionOffset, substitutionDelta);
            return results.values();
        }
        if (docFile != null) {
            usrPaths = getFileIncludes(docFile, false);
            sysPaths = getFileIncludes(docFile, true);
        }
        FileObject usrDir = baseFile.getParent();
        if (usrDir != null && usrDir.isValid()) {
            if (usrInclude == null || usrInclude == Boolean.TRUE) {
                addFolderItems(new IncludePath(FSPath.toFSPath(usrDir)), ".", childSubDir, false, false, true, substitutionOffset, substitutionDelta); // NOI18N
                if (showAll) {
                    for (IncludePath usrPath : usrPaths) {
                        addFolderItems(usrPath, usrPath.getFSPath().getPath(), childSubDir, false, false, true, substitutionOffset, substitutionDelta);
                    }
                    for (IncludePath sysPath : sysPaths) {
                        addFolderItems(sysPath, sysPath.getFSPath().getPath(), childSubDir, false, true, false, substitutionOffset, substitutionDelta);
                    }
                }
                if (usrDir.getParent() != null) {
                    addParentFolder(substitutionOffset, substitutionDelta, childSubDir, false);
                }
            } else {
                for (IncludePath sysPath : sysPaths) {
                    addFolderItems(sysPath, sysPath.getFSPath().getPath(), childSubDir, false, true, false, substitutionOffset, substitutionDelta);
                }
                if (showAll) {
                    for (IncludePath usrPath : usrPaths) {
                        addFolderItems(usrPath, usrPath.getFSPath().getPath(), childSubDir, false, false, true, substitutionOffset, substitutionDelta);
                    }
                    addFolderItems(new IncludePath(FSPath.toFSPath(usrDir)),".", childSubDir, false, false, true, substitutionOffset, substitutionDelta); // NOI18N
                    if (usrDir.getParent() != null) {
                        addParentFolder(substitutionOffset, substitutionDelta, childSubDir, true);
                    }
                }
            }
        }
        return results.values();
    }

    private void addFolderItems(IncludePath parentFolder, String parentFolderPresentation,
            String childSubDir, boolean highPriority, boolean system, boolean filtered, int substitutionOffset, int substitutionDelta) {
        FileObject parentFO = parentFolder.getFSPath().getFileObject();
        if (parentFO != null) {
            FileObject dir;
            if (parentFolder.isFramework()) {
                dir = parentFO.getFileObject(childSubDir.replace("/", ".framework/Headers/")); //NOI18N
            } else {
                dir = parentFO.getFileObject(childSubDir);
            }
            if (dir != null && dir.isValid()) {
                FileObject[] list = filtered ? listFiles(dir, new HeadersFileFilter()) : listFiles(dir, new DefFileFilter());
                if (list != null) {
                    String relFileName;
                    for (FileObject curFile : list) {
                        relFileName = curFile.getNameExt();
                        if (parentFolder.isFramework() && curFile.isFolder()) {
                            if (relFileName.endsWith(".framework")) { //NOI18N
                                relFileName = relFileName.substring(0, relFileName.lastIndexOf('.'));
                            }
                        }
                        CsmIncludeCompletionItem item = CsmIncludeCompletionItem.createItem(
                                substitutionOffset, substitutionDelta, relFileName, parentFolderPresentation, childSubDir,
                                system, highPriority, curFile.isFolder(), true);
                        if (!results.containsKey(relFileName)) {
                            results.put(relFileName, item);
                        }
                    }
                }
            }
        }
    }
    
    private FileObject[] listFiles(FileObject parent, FileObjectFilter filter) {
        FileObject[] children = parent.getChildren();
        if (children == null || children.length == 0) {
            return null;
        }
        List<FileObject> result = new ArrayList<FileObject>(children.length);
        for (FileObject child : children) {
            if (filter.accept(child)) {
                result.add(child);
            }
        }
        return result.toArray(new FileObject[result.size()]);
    }

    private void addParentFolder(int substitutionOffset, int substitutionDelta, String childSubDir, boolean system) {
        // IZ#128044: Completion in #include should switch to 2-nd mode if there are no files in the list
        // doesn't append ".." item for empty lists
        if (!results.isEmpty()) {
            CsmIncludeCompletionItem item = CsmIncludeCompletionItem.createItem(
                    substitutionOffset, substitutionDelta, "..", ".", childSubDir, system, false, true, false); // NOI18N
            results.put("..", item);//NOI18N
        }
    }

    private Collection<IncludePath> getFileIncludes(CsmFile file, boolean system) {        
        CsmFileInfoQuery query = CsmFileInfoQuery.getDefault();
        return system ? query.getSystemIncludePaths(file) : query.getUserIncludePaths(file);
    }

    private static final class DefFileFilter implements FileObjectFilter {
        @Override
        public boolean accept(FileObject fileObject) {
            return !specialFile(fileObject);
        }
    }

    private static final class HeadersFileFilter implements FileObjectFilter {

        private final ExtensionList exts;

        protected HeadersFileFilter() {
            exts = new ExtensionList();
            for (String ext : MIMEExtensions.get(MIMENames.HEADER_MIME_TYPE).getValues()) {
                exts.addExtension(ext);
            }
        }

        @Override
        public boolean accept(FileObject pathname) {
            return !specialFile(pathname) &&
                    (exts.isRegistered(pathname.getNameExt()) || pathname.isFolder() || isHeaderFileWoExt(pathname));
        }
    }

    private static boolean isHeaderFileWoExt(FileObject pathname) {
        if (FileUtil.getExtension(pathname.getNameExt()).length() == 0) {
            return MIMENames.HEADER_MIME_TYPE.equals(MIMESupport.getSourceFileMIMEType(pathname));
        }
        return false;
    }

    private static boolean specialFile(FileObject file) {
        String name = file.getNameExt();
        if (name.startsWith(".")) { // NOI18N
            return true;
        } else if (name.endsWith("~")) { // NOI18N
            return true;
        } else if (file.isFolder()) {
            if (EXCLUDED_DIR_NAMES.contains(name)) {
                return true;
            }
        }
        return false;
    }
}
