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

package org.netbeans.modules.cnd.modelimpl.trace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.IncludePath;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectItemsListener;
import org.netbeans.modules.cnd.debug.DebugUtils;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.MIMESupport;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

/**
 * 
 */
public final class NativeProjectProvider {
    
    /** Creates a new instance of NativeProjectProvider */
    private NativeProjectProvider() {
    }

    public static NativeProject createProject(String projectRoot, List<File> files,
            List<String> libProjectsPaths,
	    List<String> sysIncludes, List<String> usrIncludes, List<String> sysIncludeHeaders, List<String> usrFiles,
	    List<String> sysMacros, List<String> usrMacros, List<String> undefinedMacros, boolean pathsRelCurFile) throws IOException {

        InstanceContent ic = new InstanceContent();
        NativeProjectImpl project = new NativeProjectImpl(projectRoot, libProjectsPaths,
		sysIncludes, usrIncludes, sysIncludeHeaders, usrFiles, sysMacros, usrMacros, undefinedMacros, pathsRelCurFile, ic);

        TraceProjectLookupProvider lkp = Lookup.getDefault().lookup(TraceProjectLookupProvider.class);
        if (lkp != null) {
            lkp.createLookup(ic, project);
        }

	project.addFiles(files);
	
        return project;
    }
    
    public static void fireAllFilesChanged(NativeProject nativeProject) {
	if( nativeProject instanceof NativeProjectImpl) {
	    ((NativeProjectImpl) nativeProject).fireAllFilesChanged();
	}
    }
    
    public static void setUserMacros(NativeProject nativeProject, List<String> usrMacros) {
	if( nativeProject instanceof NativeProjectImpl) {
	    ((NativeProjectImpl) nativeProject).usrMacros.clear();
            ((NativeProjectImpl) nativeProject).usrMacros.addAll(usrMacros);
	}
    }

    // XXX:FileObject conversion: remove
    public static NativeFileItem.Language getLanguage(File file, DataObject dobj) {
        CndUtils.assertNotNull(file, "null file"); //NOI18N
        FileObject fo = null;
        if (dobj != null) {
            fo = dobj.getPrimaryFile();
        }
        String mimeType;
        if (fo != null) {
            mimeType = MIMESupport.getSourceFileMIMEType(fo);
        } else {
            mimeType = MIMESupport.getSourceFileMIMEType(file);
        }
        return getLanguage(mimeType);
    }

    public static NativeFileItem.Language getLanguage(FileObject fo, DataObject dobj) {
        CndUtils.assertNotNull(fo, "null file object"); //NOI18N
        String mimeType = MIMESupport.getSourceFileMIMEType(fo);
        return getLanguage(mimeType);
    }

    private static NativeFileItem.Language getLanguage(String mimeType) {
        if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mimeType)) {
            return NativeFileItem.Language.CPP;
        } else if (MIMENames.C_MIME_TYPE.equals(mimeType)) {
            return NativeFileItem.Language.C;
        } else if (MIMENames.FORTRAN_MIME_TYPE.equals(mimeType)) {
            return NativeFileItem.Language.FORTRAN;
        } else if (MIMENames.HEADER_MIME_TYPE.equals(mimeType)) {
            return NativeFileItem.Language.C_HEADER;
        }
        return NativeFileItem.Language.OTHER;
    }
    
    private static NativeFileItem.LanguageFlavor getLanguageFlavor(File file) {
        if (DebugUtils.getBoolean("cnd.language.flavor.cpp11", false)) { // NOI18N
            return NativeFileItem.LanguageFlavor.CPP11;
        }
        if (DebugUtils.getBoolean("cnd.language.flavor.cpp14", false)) { // NOI18N
            return NativeFileItem.LanguageFlavor.CPP14;
        }
        if (DebugUtils.getBoolean("cnd.language.flavor.cpp17", false)) { // NOI18N
            return NativeFileItem.LanguageFlavor.CPP17;
        }
        String cpp11Dirs = System.getProperty("cnd.tests.cpp11directories"); // NOI18N
        if (cpp11Dirs != null && !cpp11Dirs.isEmpty()) {
            String cpp11DirList[] = cpp11Dirs.split(";"); // NOI18N
            for (String cpp11Dir : cpp11DirList) {
                if (file.getAbsolutePath().contains(cpp11Dir)) {
                    return NativeFileItem.LanguageFlavor.CPP11;
                }
            }
        }
        String cpp14Dirs = System.getProperty("cnd.tests.cpp14directories"); // NOI18N
        if (cpp14Dirs != null && !cpp14Dirs.isEmpty()) {
            String cpp14DirList[] = cpp14Dirs.split(";"); // NOI18N
            for (String cpp14Dir : cpp14DirList) {
                if (file.getAbsolutePath().contains(cpp14Dir)) {
                    return NativeFileItem.LanguageFlavor.CPP14;
                }
            }
        }
        return NativeFileItem.LanguageFlavor.UNKNOWN;
    }

    public static DataObject getDataObject(FileObject fo) {
        DataObject dobj = null;
        if (fo != null) {
            try {
                dobj = DataObject.find(fo);
            } catch (DataObjectNotFoundException ex) {
                // skip;
            }
        }
        return dobj;
    }

    // XXX:FileObject conversion: remove
    public static DataObject getDataObject(File file) {
        CndUtils.assertNormalized(file);
        return getDataObject(CndFileUtils.toFileObject(file));
    }
    
    public static final class NativeProjectImpl implements NativeProject, Lookup.Provider {
	
	private final List<String> sysIncludes;
	private final List<String> usrIncludes;
        private final List<String> sysIncludeHeaders;
	private final List<String> usrFiles;
	private final List<String> sysMacros;
	private final List<String> usrMacros;
	    
        private final List<NativeFileItem> files  = new ArrayList<>();
	
        private final String projectRoot;
	private final boolean pathsRelCurFile;
	private final String name;
	private final List<NativeProjectItemsListener> listeners = new ArrayList<>();
        private final List<NativeProject> libProjects;

        private static final class Lock {}
        private final Object listenersLock = new Lock();
        
        private final Lookup lookup;

	private NativeProjectImpl(String projectRoot,
                List<String> libProjectsPaths,
		List<String> sysIncludes, List<String> usrIncludes, List<String>  sysIncludeHeaders, List<String> usrFiles,
		List<String> sysMacros, List<String> usrMacros, List<String> undefinedMacros,
		boolean pathsRelCurFile, InstanceContent ic) {

	    this.projectRoot = projectRoot;
            List<NativeProject> libs = new ArrayList<>();
            Collection<CsmProject> projects = CsmModelAccessor.getModel().projects();
            for (String libPath : libProjectsPaths) {
                boolean found = false;
                for (CsmProject csmProject : projects) {
                    Object platformProject = csmProject.getPlatformProject();
                    if (platformProject instanceof NativeProjectProvider.NativeProjectImpl) {
                        if (((NativeProjectProvider.NativeProjectImpl) platformProject).projectRoot.equals(libPath)) {
                            assert !found : "two projects for the same root " + libPath + libs + csmProject;
                            found = true;
                            libs.add((NativeProjectProvider.NativeProjectImpl) platformProject);
                        }
                    }
                }
                assert found : " not found project for " + libPath + " in " + projects;
            }
            this.libProjects = libs;

	    this.pathsRelCurFile = pathsRelCurFile;
	    
	    this.sysIncludes = createIncludes(sysIncludes);
	    this.usrIncludes = createIncludes(usrIncludes);
            this.sysIncludeHeaders = createIncludes(sysIncludeHeaders);
	    this.usrFiles = createIncludes(usrFiles);
	    this.sysMacros = new ArrayList<>(sysMacros);
	    this.usrMacros = new ArrayList<>(usrMacros);
            this.name = initName(projectRoot);
            this.lookup = (ic == null) ? Lookups.fixed() : new AbstractLookup(ic);
        }
        
        private String initName(String projectRoot) {
            String out = System.getProperty("cnd.modelimpl.tracemodel.project.name"); // NOI18N
            if (out == null) { 
                out = PathUtilities.getBaseName(projectRoot);
                String dir = PathUtilities.getDirName(projectRoot);
                if (dir != null) {
                    dir = PathUtilities.getBaseName(dir);
                }
                if (dir != null) {
                    out = dir + "_" + out; // NOI18N
                }
            }
            return out;
        }

        private List<String> createIncludes(List<String> src) {
	    if( pathsRelCurFile ) {
		return new ArrayList<>(src);
	    }
	    else {
		List<String> result = new ArrayList<>(src.size());
		for( String path : src ) {
		    File file = new File(path);
		    result.add(file.getAbsolutePath());
		}
		return result;
	    }
	}
	
	private void addFiles(List<File> files) throws IOException {
	    for( File file : files ) {
                final FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file.getAbsoluteFile()));
                if (fo == null) {
                    throw new IOException("no file object for " + file); // NOI18N
                } else {
                    addFile(fo);
                }
	    }
	}
	
        @Override
        public Lookup.Provider getProject() {
            return this;
        }

        @Override
        public Lookup getLookup() {
            return lookup;
        }        

        @Override
        public FileSystem getFileSystem() {
            return CndFileUtils.getLocalFileSystem();
        }
        
        @Override
        public List<String> getSourceRoots() {
            return Collections.<String>emptyList();
        }
                
        @Override
        public String getProjectRoot() {
            return this.projectRoot;
        }

        @Override
        public String getProjectDisplayName() {
            return name;
        }

        @Override
        public List<NativeFileItem> getAllFiles() {
            return Collections.unmodifiableList(files);
        }

        @Override
        public List<NativeFileItem> getStandardHeadersIndexers() {
            return Collections.<NativeFileItem>emptyList();
        }

        @Override
        public void addProjectItemsListener(NativeProjectItemsListener listener) {
            synchronized( listenersLock ) {
		listeners.add(listener);
	    }
        }

        @Override
        public void removeProjectItemsListener(NativeProjectItemsListener listener) {
            synchronized( listenersLock ) {
		listeners.remove(listener);
	    }
        }

	public void fireFileChanged(FileObject fo) {
            NativeFileItem item = findFileItem(fo);
	    List<NativeProjectItemsListener> listenersCopy;
	    synchronized( listenersLock ) {
		listenersCopy = new ArrayList<>(listeners);
	    }
            List<NativeFileItem> list = Collections.singletonList(item);            
	    for( NativeProjectItemsListener listener : listenersCopy ) {
		listener.filesPropertiesChanged(list);
	    }
        }

        public void fireFileAdded(FileObject fo) {
            NativeFileItem item = findFileItem(fo);
            if (item == null) {
                item = addFile(fo);
            }
	    List<NativeProjectItemsListener> listenersCopy;
	    synchronized( listenersLock ) {
		listenersCopy = new ArrayList<>(listeners);
	    }
            List<NativeFileItem> list = Collections.singletonList(item);
	    for( NativeProjectItemsListener listener : listenersCopy ) {
		listener.filesAdded(list);
	    }
        }

	private void fireAllFilesChanged() {
	    List<NativeProjectItemsListener> listenersCopy;
	    synchronized( listenersLock ) {
		listenersCopy = new ArrayList<>(listeners);
	    }
	    List<NativeFileItem> items = Collections.unmodifiableList(files);
	    for( NativeProjectItemsListener listener : listenersCopy ) {
		listener.filesPropertiesChanged(items);
	    }
	}

        @Override
        public NativeFileItem findFileItem(FileObject fileObject) {
            return findFileItem(CndFileUtils.normalizePath(fileObject));
        }

        private NativeFileItem findFileItem(String path) {
            for (NativeFileItem item : files) {
                if (item.getAbsolutePath().equalsIgnoreCase(path)) {
                    return item;
                }
            }
            return null;
        }

        @Override
        public List<IncludePath> getSystemIncludePaths() {
            return IncludePath.toIncludePathList(CndFileUtils.getLocalFileSystem(), this.sysIncludes);
        }

        @Override
        public List<IncludePath> getUserIncludePaths() {
            return IncludePath.toIncludePathList(CndFileUtils.getLocalFileSystem(), this.usrIncludes);
        }

        @Override
        public List<FSPath> getSystemIncludeHeaders() {
            return CndFileUtils.toFSPathList(CndFileUtils.getLocalFileSystem(), this.sysIncludeHeaders);
        }

        @Override
        public List<FSPath> getIncludeFiles() {
            return CndFileUtils.toFSPathList(CndFileUtils.getLocalFileSystem(), this.usrFiles);
        }

        @Override
        public List<String> getSystemMacroDefinitions() {
            return this.sysMacros;
        }

        @Override
        public List<String> getUserMacroDefinitions() {
            return this.usrMacros;
        }

	private NativeFileItem addFile(FileObject fo) {
            File file = FileUtil.toFile(fo);
            DataObject dobj = getDataObject(fo);
	    NativeFileItem.Language lang = getLanguage(fo, dobj);
        NativeFileItem.LanguageFlavor flavor = getLanguageFlavor(file);
	    NativeFileItem item = new NativeFileItemImpl(file, this, lang, flavor);
	    //TODO: put item in loockup of DataObject
            // registerItemInDataObject(dobj, item);
	    this.files.add(item);
            return item;
	}
	
        @Override
        public List<NativeProject> getDependences() {
            return libProjects;
        }

        @Override
        public void runOnProjectReadiness(NamedRunnable task) {
            task.run();
        }

        @Override
        public void fireFilesPropertiesChanged() {
        }
    }    
        
    /*package*/ static void registerItemInDataObject(DataObject obj, NativeFileItem item) {
        if (obj != null) {
            NativeFileItemSet set = obj.getLookup().lookup(NativeFileItemSet.class);
            if (set != null) {
                set.add(item);
                if (item instanceof NativeFileItemImpl) {
                    ((NativeFileItemImpl)item).lastDataObject = obj;
                }
            }
        }
    }
    
    private static final class NativeFileItemImpl implements NativeFileItem {
	
        private final File file;
        private final NativeProjectImpl project;
        private final NativeFileItem.Language lang;
        private final NativeFileItem.LanguageFlavor flavor;
        private DataObject lastDataObject; //keep data object, otherwise it will be recreated without association to NativeFileItem

        public NativeFileItemImpl(File file, NativeProjectImpl project, NativeFileItem.Language language, NativeFileItem.LanguageFlavor flavor) {
	    
            this.project = project;
            this.file = CndFileUtils.normalizeFile(file);
            this.lang = language;
            this.flavor = flavor;
        }
        
        @Override
        public NativeProject getNativeProject() {
            return project;
        }

        @Override
        public FileObject getFileObject() {
            return CndFileUtils.toFileObject(file); // XXX:FileObject conversion
        }
        
        @Override
        public String getAbsolutePath() {
            return file.getAbsolutePath();
        }

        @Override
        public String getName() {
            return file.getName();
        }

        @Override
        public List<IncludePath> getSystemIncludePaths() {
	    List<IncludePath> result = project.getSystemIncludePaths();
	    return project.pathsRelCurFile ? toAbsoluteItemPath(result) : result;
        }

        @Override
        public List<IncludePath> getUserIncludePaths() {
	    List<IncludePath> result = project.getUserIncludePaths();
            return project.pathsRelCurFile ? toAbsoluteItemPath(result) : result;
        }

        @Override
        public List<FSPath> getSystemIncludeHeaders() {
	    List<FSPath> result = project.getSystemIncludeHeaders();
	    return project.pathsRelCurFile ? toAbsolute(result) : result;
        }

        @Override
        public List<FSPath> getIncludeFiles() {
            return project.getIncludeFiles();
        }
	
	private List<FSPath> toAbsolute(List<FSPath> orig) {
	    File base = file.getParentFile();
	    List<FSPath> result = new ArrayList<>(orig.size());
	    for( FSPath path : orig ) {
		File pathFile = new File(path.getPath());
		if( pathFile.isAbsolute() ) {
		    result.add(path);
		}
		else {
		    pathFile = new File(base, path.getPath());
		    result.add(new FSPath(CndFileUtils.getLocalFileSystem(), pathFile.getAbsolutePath()));
		}
	    }
	    return result;
	}

        private List<IncludePath> toAbsoluteItemPath(List<IncludePath> orig) {
	    File base = file.getParentFile();
	    List<IncludePath> result = new ArrayList<>(orig.size());
	    for( IncludePath path : orig ) {
		File pathFile = new File(path.getFSPath().getPath());
		if( pathFile.isAbsolute() ) {
		    result.add(path);
		} else {
		    pathFile = new File(base, path.getFSPath().getPath());
		    result.add(new IncludePath(CndFileUtils.getLocalFileSystem(), pathFile.getAbsolutePath(), path.isFramework()));
		}
	    }
	    return result;
	}

        @Override
        public List<String> getSystemMacroDefinitions() {
            return project.getSystemMacroDefinitions();
        }

        @Override
        public List<String> getUserMacroDefinitions() {
            return project.getUserMacroDefinitions();
        }

        @Override
        public NativeFileItem.Language getLanguage() {
            return lang;
        }

        @Override
        public NativeFileItem.LanguageFlavor getLanguageFlavor() {
            return flavor;
        }

        @Override
        public boolean isExcluded() {
            return false;
        }

        @Override
        public String toString() {
            return file.getAbsolutePath();
        }

    }
}
