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
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.PrintWriter;
import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmModelListener;
import org.netbeans.modules.cnd.api.model.CsmModelState;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmStandaloneFileProvider;
import org.netbeans.modules.cnd.api.project.DefaultSystemSettings;
import org.netbeans.modules.cnd.api.project.IncludePath;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItem.LanguageFlavor;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectItemsListener;
import org.netbeans.modules.cnd.api.project.NativeProjectRegistry;
import org.netbeans.modules.cnd.api.project.NativeProjectSupport;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.trace.NativeProjectProvider;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.CndLanguageStandards;
import org.netbeans.modules.cnd.utils.CndLanguageStandards.CndLanguageStandard;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Pair;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.services.CsmStandaloneFileProvider.class)
public class CsmStandaloneFileProviderImpl extends CsmStandaloneFileProvider {

    private static final boolean TRACE = Boolean.getBoolean("cnd.standalone.trace"); //NOI18N
    private static final class Lock{}
    private static final Lock lock = new Lock();
    private static final Set<String> toBeRmoved = new HashSet<>();
    // equals to CompileExecSupport.PROP_LANG_STANDARD
    private static final String PROP_LANG_STANDARD = "standard"; // NOI18N

    private final CsmModelListener listener = new CsmModelListener() {

        @Override
        public void projectOpened(CsmProject project) {
        }

        @Override
        public void projectClosed(CsmProject project) {
        }

        @Override
        public void modelChanged(CsmChangeEvent e) {
            for (CsmFile file : e.getNewFiles()) {
                clean(file);
            }
            for (CsmFile file : e.getRemovedFiles()) {
                onFileRemove(file);
            }
        }
    };

    private final CsmProgressListener progressListener = new CsmProgressAdapter() {

        @Override
        public void projectLoaded(CsmProject project) {
            clean((ProjectBase) project);
        }

        @Override
        public void projectParsingFinished(CsmProject project) {
        }
    };

    public CsmStandaloneFileProviderImpl() {
        CsmListeners.getDefault().addModelListener(listener);
        CsmListeners.getDefault().addProgressListener(progressListener);
    }

    static CsmStandaloneFileProviderImpl getDefaultImpl() {
        return (CsmStandaloneFileProviderImpl) CsmStandaloneFileProvider.getDefault();
    }

    @Override
    public CsmFile getCsmFile(FileObject fo) {
        if (fo == null || ! fo.isValid() || ! CsmUtilities.isCsmSuitable(fo)) {  // #194431 Path should be absolute: Templates/cFiles/CSimpleTest.c
            return null;
        }
        CsmModelState modelState = CsmModelAccessor.getModelState();
        if (modelState != CsmModelState.ON) {
            if (TRACE) {
                trace("model is %s, no extra work for %s", modelState, fo.getPath());  //NOI18N
            }
            return null;
        }
// The check CsmUtilities.isCsmSuitable() above filters out templates
// The check below disallows code assistance for full remote standalone files
//        File file = CndFileUtils.toFile(fo);
//        // the file can be null, for example, when we edit templates
//        // TODO: check with full remote
//        if (file == null) {
//            return null;
//        }
        String absPath = CndFileUtils.normalizePath(fo);
        ProjectBase project = null;
        //synchronized (this) {
            // findFile is expensive - don't call it twice!
            CsmFile csmFile = ModelImpl.instance().findFile(FSPath.toFSPath(fo), true, false);
            if (csmFile != null) {
                if (TRACE) {trace("returns file %s", csmFile);} //NOI18N
                return csmFile;
            }
            synchronized (lock) {
                if (toBeRmoved.contains(absPath)){
                    return null;
                }
                NativeProject platformProject = NativeProjectImpl.getNativeProjectImpl(fo);
                if (platformProject != null) {
                    project = ModelImpl.instance().addProject(platformProject, absPath, true);
                    if (TRACE) {trace("added project %s", project);} //NOI18N
                    // could be null when model is swithing off
                    if (project != null) {
                        project.ensureFilesCreated();
                    }
                }
            }
        //}
        if (project != null && project.isValid()) {
            try {
                CsmFile out = project.getFile(absPath, false);
                if (TRACE) {trace("RETURNS STANALONE FILE %s", out);} //NOI18N
                return out;
            } catch (BufferUnderflowException ex) {
                // FIXUP: IZ#148840
                DiagnosticExceptoins.register(ex);
            } catch (IllegalStateException ex) {
                // project can be closed
                DiagnosticExceptoins.register(ex);
            }
        }
        return null;
    }

    private void clean(ProjectBase projectOpened) {
        if (projectOpened.getPlatformProject() instanceof NativeProjectImpl) {
            return;
        }
        if (TRACE) {trace("checking project %s", projectOpened.toString());} //NOI18N
        for (CsmProject dummy : ModelImpl.instance().projects()) {
            if (dummy.getPlatformProject() instanceof NativeProjectImpl) {
                for (CsmFile file : dummy.getAllFiles()) {
                    if (TRACE) {trace("\nchecking file %s", file.getAbsolutePath());} //NOI18N
                    if (projectOpened.getFile(((FileImpl) file).getAbsolutePath(), false) != null) {
                        scheduleProjectRemoval(dummy);
                        continue;
                    }
                }
            }
        }
    }

    /** Is called when a file is added to model */
    private void clean(CsmFile file) {
        if (!(file.getProject().getPlatformProject() instanceof NativeProjectImpl)) {
            notifyClosed(file);
        }
    }

    void onFileRemove(CsmFile file) {
        FileObject fo = CsmUtilities.getFileObject(file);
        if (fo != null && isOpen(fo)) {
            this.getCsmFile(fo);
        }
    }

    private boolean isOpen(FileObject fo) {
        try {
            DataObject dao = DataObject.find(fo);
            if (dao != null) {
                return CsmUtilities.findOpenedEditor(dao) != null;
            }
        } catch (DataObjectNotFoundException ex) {
            // we don't need to report this exception;
            // probably the file is just removed by user
        }
        return false;
    }

    @Override
    public void notifyClosed(CsmFile csmFile) {
        //if (TRACE) {trace("checking file %s", csmFile.toString());} //NOI18N
        String closedFilePath = csmFile.getAbsolutePath().toString();
        synchronized (lock) {
            for (CsmProject csmProject : ModelImpl.instance().projects()) {
                Object platformProject = csmProject.getPlatformProject();
                if (platformProject instanceof NativeProjectImpl) {
                    NativeProjectImpl nativeProject = (NativeProjectImpl) platformProject;
                    if (nativeProject.getProjectRoot().equals(closedFilePath)) {
                        for (CsmFile csmf : csmProject.getAllFiles()) {
                            FileObject fo = ((FileImpl) csmf).getFileObject();
                            DataObject dao = NativeProjectProvider.getDataObject(fo);
                            if (dao != null) {
                                NativeFileItemSet set = dao.getLookup().lookup(NativeFileItemSet.class);
                                if (set != null) {
                                    set.remove(nativeProject.findFileItem(fo));
                                }
                            }
                        }
                        scheduleProjectRemoval(csmProject);
                    }
                }
            }
        }
    }

    @Override
    public boolean isStandalone(CsmFile file) {
        if (file instanceof FileImpl) {
            NativeFileItem nfi = ((FileImpl) file).getNativeFileItem();
            if (nfi instanceof CsmStandaloneFileProviderImpl.NativeFileItemImpl) {
                return true;
            }
        }
        return false;
    }

    private void scheduleProjectRemoval(final CsmProject project) {
        if(!project.isValid()) {
            return;
        }
        final Object nativeProject = project.getPlatformProject();
        if (!(nativeProject instanceof NativeProject)) {
            return;
        }
        final String root = ((NativeProject)nativeProject).getProjectRoot();
        if (TRACE) {trace("schedulling removal %s", project.toString());} //NOI18N
        synchronized (lock) {
            toBeRmoved.add(root);
        }
        ModelImpl.instance().enqueueModelTask(new Runnable() {
            @Override
            public void run() {
                if (project.isValid()) {
                    if (TRACE) {trace("removing %s", project.toString());} //NOI18N
                    ProjectBase projectBase = (ProjectBase) project;
                    ModelImpl.instance().closeProjectBase(projectBase, false);
                    synchronized (lock) {
                        toBeRmoved.remove(root);
                    }
                    if (TRACE) {trace("removed %s", project.toString());} //NOI18N
                }
            }
        }, "Standalone project removal."); //NOI18N
    }

    private static void trace(String pattern, Object... args) {
        assert TRACE : "Should not be called if TRACE is off!"; //NOI18N
        System.err.printf("### Standalone provider:  %s%n", String.format(pattern, args)); //NOI18N
    }

    private static CndLanguageStandard getStandardFromFileAttrs(FileObject fo) {
        if (fo == null) {
            return null;
        }
        String mimeType = FileUtil.getMIMEType(fo);
        if (mimeType == null) {
            return null;
        }
        return CndLanguageStandards.StringToLanguageStandard((String) fo.getAttribute(PROP_LANG_STANDARD));
    }
    
    /*package*/ static final class NativeProjectImpl implements NativeProject, ChangeListener, FileChangeListener {

        private final List<IncludePath> sysIncludes;
        private final List<IncludePath> usrIncludes;
        private final List<FSPath> sysIncludeHeaders;
        private final List<FSPath> usrFiles;
        private final List<String> sysMacros;
        private final List<String> usrMacros;
        private final List<NativeFileItemImpl> files = new ArrayList<>();
        private final FileObject projectRoot;
        private final FileSystem fileSystem;
        private final List<NativeProjectItemsListener> listeners = new ArrayList<>();

        private static final class Lock {}
        private final Object listenersLock = new Lock();

        static NativeProject getNativeProjectImpl(FileObject file) {
            DataObject dao = NativeProjectProvider.getDataObject(file);
            if (dao == null) {
                return null;
            }
            NativeFileItemSet set = dao.getLookup().lookup(NativeFileItemSet.class);
            if (set == null) {
                // it does not matter, what is there in the set! - see #185599, #185629
                return null;
            }
            NativeFileItem itemPrototype = null;
            if (!set.isEmpty()) {
                Iterator<NativeFileItem> iterator = set.getItems().iterator();
                Collection<NativeProject> openProjects = NativeProjectRegistry.getDefault().getOpenProjects();
                while(iterator.hasNext()) {
                    itemPrototype = iterator.next();
                    if (openProjects.contains(itemPrototype.getNativeProject())) {
                        return null;
                    }
                }
            }
            CsmModel model = ModelImpl.instance();
            List<IncludePath> sysIncludes = new ArrayList<>();
            List<IncludePath> usrIncludes = new ArrayList<>();
            List<FSPath> sysIncludeHeaders = new ArrayList<>();
            List<FSPath> usrFiles = new ArrayList<>();
            List<String> sysMacros = new ArrayList<>();
            List<String> usrMacros = new ArrayList<>();
            List<String> undefinedMacros = new ArrayList<>();
            NativeFileItem.Language lang;
            Pair<LanguageFlavor,MIMEExtensions> flavorExt;
            if (itemPrototype != null) {
                lang = itemPrototype.getLanguage();
                flavorExt = Pair.of(itemPrototype.getLanguageFlavor(), null);
            } else {
                lang = NativeProjectProvider.getLanguage(file, dao);
                flavorExt = getDefaultStandard(lang);
            }
            NativeProject prototype = null;
            for (CsmProject csmProject : model.projects()) {
                Object p = csmProject.getPlatformProject();
                if (p instanceof NativeProject) {
                    NativeProject project = (NativeProject)p;
                    if (file.getPath().startsWith(project.getProjectRoot())) {
                        prototype = project;
                        break;
                    }
                    for (String root : project.getSourceRoots()) {
                        if (file.getPath().startsWith(root)) {
                            prototype = project;
                            break;
                        }
                    }
                    if (prototype != null) {
                        break;
                    }
                }
            }
            if (prototype != null && ModelImpl.instance().isProjectDisabled(prototype)){
                return null;
            }

            FileSystem fs;
            try {
                fs = file.getFileSystem();
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
                fs = CndFileUtils.getLocalFileSystem();
            }
            NativeProjectImpl impl = new NativeProjectImpl(file, sysIncludes, usrIncludes, sysIncludeHeaders, usrFiles, sysMacros, usrMacros, undefinedMacros);
            LanguageFlavor resultingFlavor = flavorExt.first();
            CndLanguageStandard standard = getStandardFromFileAttrs(file);
            if (standard != null) {
                LanguageFlavor standardToFlavor = NativeProjectSupport.cndStandardToItemFlavor(standard);
                if (standardToFlavor != LanguageFlavor.UNKNOWN) {
                    resultingFlavor = standardToFlavor;
                }
            }
            if (itemPrototype != null) {
                sysIncludes.addAll(itemPrototype.getSystemIncludePaths());
                sysMacros.addAll(itemPrototype.getSystemMacroDefinitions());
                sysIncludeHeaders.addAll(itemPrototype.getSystemIncludeHeaders());
                usrIncludes.addAll(itemPrototype.getUserIncludePaths());
                usrFiles.addAll(itemPrototype.getIncludeFiles());
                usrMacros.addAll(itemPrototype.getUserMacroDefinitions());
            } else if (prototype != null) {
                sysIncludes.addAll(prototype.getSystemIncludePaths());
                sysMacros.addAll(prototype.getSystemMacroDefinitions());
                sysIncludeHeaders.addAll(prototype.getSystemIncludeHeaders());
                usrIncludes.addAll(prototype.getUserIncludePaths());
                usrFiles.addAll(prototype.getIncludeFiles());
                usrMacros.addAll(prototype.getUserMacroDefinitions());
            } else  {
                sysIncludes.addAll(IncludePath.toIncludePathList(fs, DefaultSystemSettings.getDefault().getSystemIncludes(lang, resultingFlavor, impl)));
                sysIncludeHeaders.addAll(CndFileUtils.toFSPathList(fs, DefaultSystemSettings.getDefault().getSystemIncludeHeaders(lang, resultingFlavor, impl)));
                sysMacros.addAll(DefaultSystemSettings.getDefault().getSystemMacros(lang, resultingFlavor, impl));
            }
            impl.checkPaths();
            impl.addFile(file, lang, resultingFlavor);
            set.add(impl.findFileItem(file));
            MIMEExtensions me = flavorExt.second();
            if (me != null) {
                me.addChangeListener(impl);
            }
            file.addFileChangeListener(impl);
            return impl;
        }

        private static Pair<LanguageFlavor,MIMEExtensions> getDefaultStandard(NativeFileItem.Language lang) {
            LanguageFlavor flavor = LanguageFlavor.UNKNOWN;
            MIMEExtensions me = null;
            switch(lang) {
                case C:
                    me = MIMEExtensions.get(MIMENames.C_MIME_TYPE);
                    break;
                case CPP:
                    me = MIMEExtensions.get(MIMENames.CPLUSPLUS_MIME_TYPE);
                    break;
                case C_HEADER:
                    me = MIMEExtensions.get(MIMENames.HEADER_MIME_TYPE);
                    break;
            }
            if (me != null) {
                CndLanguageStandard defaultStandard = me.getDefaultStandard();
                if (defaultStandard != null) {
                    flavor = NativeProjectSupport.cndStandardToItemFlavor(defaultStandard);
                }
            }
            if (LanguageFlavor.UNKNOWN.equals(flavor)) {
                String key = "cnd.standalone.default.flavor." + lang.name(); //NOI18N
                String defFlavorTxt = System.getProperty(key);
                if (defFlavorTxt != null) {
                    try {
                        flavor = LanguageFlavor.valueOf(defFlavorTxt);
                    } catch (IllegalArgumentException e) {
                        StringBuilder all = new StringBuilder();
                        for (LanguageFlavor lf : LanguageFlavor.values()) {
                            all.append(all.length() > 0 ? ',' : ' ').append(lf.name());
                        }
                        System.err.printf("Wrong parameter -J-D%s=%s. Should be one of %s%n", key, defFlavorTxt, all);
                    }
                }
            }
            return Pair.of(flavor, me);
        }

        private NativeProjectImpl(FileObject projectRoot,
                List<IncludePath> sysIncludes, List<IncludePath> usrIncludes,  List<FSPath> sysIncludeHeaders, List<FSPath> usrFiles,
                List<String> sysMacros, List<String> usrMacros, List<String> undefinedMacros) {

            this.projectRoot = projectRoot;
            this.fileSystem = getFileSystem(projectRoot);
            this.sysIncludes = sysIncludes;
            this.sysIncludeHeaders = sysIncludeHeaders;
            this.usrIncludes = usrIncludes;
            this.usrFiles = usrFiles;
            this.sysMacros = sysMacros;
            this.usrMacros = usrMacros;
        }

        private static FileSystem getFileSystem(FileObject fo) {
            try {
                return fo.getFileSystem();
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
                return CndFileUtils.getLocalFileSystem();
            }
        }

        private void checkPaths() {
            check(sysIncludes);
            check(usrIncludes);
        }

        private void check(List<IncludePath> list) {
            for(Iterator<IncludePath> it = list.iterator(); it.hasNext();){
                IncludePath path = it.next();
                if (!CndPathUtilities.isPathAbsolute(path.getFSPath().getPath())) {
                    CndUtils.assertTrueInConsole(false, "Can not convert "+path.getFSPath().getPath()); //NOI18N
                    it.remove();
                }
            }
        }

        private void addFile(FileObject file, NativeFileItem.Language lang, LanguageFlavor flavor) {
            NativeFileItemImpl item = new NativeFileItemImpl(file, this, lang, flavor);
            //TODO: put item in loockup of DataObject
            // registerItemInDataObject(dobj, item);
            this.files.add(item);
        }

        @Override
        public Lookup.Provider getProject() {
            return null;
        }

        @Override
        public List<String> getSourceRoots() {
            return Collections.<String>emptyList();
        }

        @Override
        public String getProjectRoot() {
            return this.projectRoot.getPath();
        }

        @Override
        public FileSystem getFileSystem() {
            try {
                return projectRoot.getFileSystem();
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
                return CndFileUtils.getLocalFileSystem();
            }
        }

        @Override
        public String getProjectDisplayName() {
            return getProjectRoot();
        }

        @Override
        public List<NativeFileItem> getAllFiles() {
            return Collections.<NativeFileItem>unmodifiableList(files);
        }

        @Override
        public List<NativeFileItem> getStandardHeadersIndexers() {
            return Collections.<NativeFileItem>emptyList();
        }

        @Override
        public void addProjectItemsListener(NativeProjectItemsListener listener) {
            synchronized (listenersLock) {
                listeners.add(listener);
            }
        }

        @Override
        public void removeProjectItemsListener(NativeProjectItemsListener listener) {
            synchronized (listenersLock) {
                listeners.remove(listener);
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            Object source = e.getSource();
            if (source instanceof MIMEExtensions) {
                if (files.size()>0) {
                    FileObject fo = files.get(0).getFileObject();
                    Object attribute = fo.getAttribute(PROP_LANG_STANDARD);
                    if (attribute instanceof String) {
                        return;
                    }
                }
                ArrayList<NativeProjectItemsListener> list = new ArrayList<>();
                synchronized (listenersLock) {
                    list.addAll(listeners);
                }
                MIMEExtensions me = (MIMEExtensions) source;
                CndLanguageStandard defaultStandard = me.getDefaultStandard();
                for(NativeFileItemImpl nfi : files) {
                    NativeFileItem.LanguageFlavor flavor = NativeProjectSupport.cndStandardToItemFlavor(defaultStandard);
                    if (flavor != LanguageFlavor.UNKNOWN) {
                        nfi.setLanguageFlavor(flavor);
                    }
                }
                for(NativeProjectItemsListener listener : list) {
                    listener.filesPropertiesChanged(this);
                }
            }
        }


        @Override
        public NativeFileItemImpl findFileItem(FileObject fileObject) {
            for (NativeFileItemImpl item : files) {
                if (item.getFileObject().equals(fileObject)) {
                    return item;
                }
            }
            return null;
        }

        @Override
        public List<IncludePath> getSystemIncludePaths() {
            return this.sysIncludes;
        }

        @Override
        public List<IncludePath> getUserIncludePaths() {
            return this.usrIncludes;
        }

        @Override
        public List<FSPath> getSystemIncludeHeaders() {
            return this.sysIncludeHeaders;
        }

        @Override
        public List<FSPath> getIncludeFiles() {
            return this.usrFiles;
        }

        @Override
        public List<String> getSystemMacroDefinitions() {
            return this.sysMacros;
        }

        @Override
        public List<String> getUserMacroDefinitions() {
            return this.usrMacros;
        }

        @Override
        public List<NativeProject> getDependences() {
            return Collections.<NativeProject>emptyList();
        }

        @Override
        public void runOnProjectReadiness(NamedRunnable task) {
            task.run();
        }

        @Override
        public void fireFilesPropertiesChanged() {
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
        }

        @Override
        public void fileChanged(FileEvent fe) {
        }

        @Override
        public void fileDeleted(FileEvent fe) {
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            String name = fe.getName();
            if (!PROP_LANG_STANDARD.equals(name)) {
                return;
            }
            FileObject fo = fe.getFile();
            if (fo == null) {
                return;
            }
            CndLanguageStandard standard = getStandardFromFileAttrs(fo);
            if (standard == null) {
                String mimeType = FileUtil.getMIMEType(fo);
                if (mimeType == null) {
                    return;
                }
                MIMEExtensions me = MIMEExtensions.get(mimeType);
                if (me == null) {
                    return;
                }
                // use standatd from me
                stateChanged(new ChangeEvent(me));
                return;
            }
            for(NativeFileItemImpl nfi : files) {
                NativeFileItem.LanguageFlavor flavor = NativeProjectSupport.cndStandardToItemFlavor(standard);
                if (flavor != LanguageFlavor.UNKNOWN) {
                    nfi.setLanguageFlavor(flavor);
                }
            }
            ArrayList<NativeProjectItemsListener> list = new ArrayList<>();
            synchronized (listenersLock) {
                list.addAll(listeners);
            }
            for(NativeProjectItemsListener listener : list) {
                listener.filesPropertiesChanged(this);
            }
        }

        @Override
        public final String toString() {
            return "SA " + projectRoot + ' ' + getClass().getName() + " @" + hashCode() + ":" + System.identityHashCode(this); // NOI18N
        }
    }

    private static final class NativeFileItemImpl implements NativeFileItem {

        private final FileObject fileObject;
        private final NativeProjectImpl project;
        private final NativeFileItem.Language lang;
        private NativeFileItem.LanguageFlavor flavor;

        public NativeFileItemImpl(FileObject file, NativeProjectImpl project, NativeFileItem.Language language, NativeFileItem.LanguageFlavor flavor) {
            this.project = project;
            this.fileObject = file;
            this.lang = language;
            this.flavor = flavor;
        }

        @Override
        public NativeProject getNativeProject() {
            return project;
        }

        @Override
        public FileObject getFileObject() {
            return fileObject;
        }

        @Override
        public String getAbsolutePath() {
            return CndFileUtils.normalizePath(fileObject);
            }

        @Override
        public String getName() {
            return fileObject.getNameExt();
        }

        @Override
        public List<IncludePath> getSystemIncludePaths() {
            List<IncludePath> result = project.getSystemIncludePaths();
            checkAbsoluteItemPath(result);
            return result;
        }

        @Override
        public List<IncludePath> getUserIncludePaths() {
            List<IncludePath> result = project.getUserIncludePaths();
            checkAbsoluteItemPath(result);
            return result;
        }

        @Override
        public List<FSPath> getSystemIncludeHeaders() {
            List<FSPath> result = project.getSystemIncludeHeaders();
            checkAbsolute(result);
            return result;
        }

        @Override
        public List<FSPath> getIncludeFiles() {
            return project.getIncludeFiles();
        }

        private void checkAbsolute(List<FSPath> orig) {
            for (FSPath path : orig) {
                CndUtils.assertAbsolutePathInConsole(path.getPath());
            }
        }

        private void checkAbsoluteItemPath(List<IncludePath> orig) {
            for (IncludePath path : orig) {
                CndUtils.assertAbsolutePathInConsole(path.getFSPath().getPath());
            }
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

        public void setLanguageFlavor(NativeFileItem.LanguageFlavor flavor) {
            this.flavor = flavor;
        }

        @Override
        public boolean isExcluded() {
            return false;
        }

        @Override
        public String toString() {
            return "SA " + fileObject + " " + System.identityHashCode(this) + " " + lang + " from project:" + project; // NOI18N
        }
    }

    public void dumpInfo(PrintWriter printOut) {
        printOut.printf("SAProvider %s has toBeRemoved=%d entries%n", this.getClass().getSimpleName(), toBeRmoved.size());// NOI18N
        int ind = 1;
        for (String str : toBeRmoved) {
            printOut.printf("[%d] %s%n", ind++, str);// NOI18N
        }
    }

}
