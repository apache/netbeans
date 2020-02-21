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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.services.CsmCompilationUnit;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContentSignature;
import org.netbeans.modules.cnd.modelimpl.content.project.GraphContainer;
import org.netbeans.modules.cnd.modelimpl.content.project.GraphContainer.ParentFiles;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;

/**
 * Reparse dependant files.
 */
public final class DeepReparsingUtils {
    private static final Logger LOG = Logger.getLogger("DeepReparsingUtils"); // NOI18N
    private static final boolean TRACE = LOG.isLoggable(Level.FINE);
    
    private DeepReparsingUtils() {
    }

    /**
     * Reparse one file when fileImpl content changed. It could be
     * File->Document, Document->Document, Document->File, File->File
     */
    private static void reparseOnlyOneFile(ProjectBase project, FileImpl fileImpl) {
        checkFileState(fileImpl, "reparseOnlyOneFile"); // NOI18N
        if (TRACE) {
            LOG.log(Level.INFO, "reparseOnlyOneFile {0}", fileImpl.getAbsolutePath());
        }
        project.markAsParsingPreprocStates(fileImpl);
        fileImpl.markReparseNeeded(false);
        ParserQueue.instance().addToBeParsedNext(fileImpl);
    }

    /**
     * Reparse one file when fileImpl content changed as result of Undo operation.
     */
    static void reparseOnUndoEditedFile(ProjectBase project, FileImpl fileImpl) {
        checkFileState(fileImpl, "reparseOnUndoEditedFile"); // NOI18N
        if (TRACE) {
            LOG.log(Level.INFO, "reparseOnUndoEditedFile {0}", fileImpl.getAbsolutePath());
        }
        reparseOnlyOneFile(project, fileImpl);
    }

    /**
     * Reparse one file when fileImpl content changed as result of typing in editor.
     */
    static void reparseOnEditingFile(ProjectBase project, FileImpl fileImpl) {
        checkFileState(fileImpl, "reparseOnEditingFile"); // NOI18N 
        if (TRACE) {
            LOG.log(Level.INFO, "reparseOnEditingFile {0}", fileImpl.getAbsolutePath());
        }
        reparseOnlyOneFile(project, fileImpl);
    }

    /**
     * Reparse including/included files at fileImpl content changed.
     */
    public static void tryPartialReparseOnChangedFile(final ProjectBase changedFileProject, final FileImpl fileImpl) {
        checkFileState(fileImpl, "changedFileProject"); // NOI18N
        if (TRACE) {
            LOG.log(Level.INFO, "tryPartialReparseOnChangedFile {0}", fileImpl.getAbsolutePath());
        }
        changedFileProject.markAsParsingPreprocStates(fileImpl);
        fileImpl.markReparseNeeded(true);
        ParserQueue.instance().addForPartialReparse(fileImpl);
    }

    static boolean finishPartialReparse(FileImpl fileImpl, FileContentSignature lastFileBasedSignature, FileContentSignature newSignature) {
        FileContentSignature.ComparisonResult compareResult = FileContentSignature.compare(newSignature, lastFileBasedSignature);
        if (compareResult == FileContentSignature.ComparisonResult.SAME) {
            if (TRACE) {
                LOG.log(Level.INFO, "partial reparseOnChangedFile was enough for {0}", fileImpl.getAbsolutePath());
            }
            return true;
        } else if (fileImpl.isSourceFile() && compareResult == FileContentSignature.ComparisonResult.FILE_LOCAL_CHANGE) {
            if (TRACE) {
                LOG.log(Level.INFO, "partial reparseOnChangedFile was enough for changed src {0}", fileImpl.getAbsolutePath());
            }
            return true;
        } else if (TRACE) {
            LOG.log(Level.INFO, "partial reparseOnChangedFile results in changed signature for {0}:\n{1}", 
                    new Object[] { fileImpl.getAbsolutePath(), FileContentSignature.testDifference(newSignature, lastFileBasedSignature)}
                    );
        }
        // signature have changed => full reparse is needed
        DeepReparsingUtils.reparseOnChangedFileImpl(fileImpl.getProjectImpl(true), fileImpl, false, lastFileBasedSignature);
        return false;
    }

    static void fullReparseOnChangedFile(final ProjectBase changedFileProject, final FileImpl fileImpl) {
        reparseOnChangedFileImpl(changedFileProject, fileImpl, false, null);
    }

    private static void reparseOnChangedFileImpl(final ProjectBase changedFileProject, final FileImpl changedFile, boolean contentChanged, FileContentSignature lastFileBasedSignature) {
        if (TRACE) {
            LOG.log(Level.INFO, "full reparseOnChangedFile {0}", changedFile.getAbsolutePath());
        }
        // content of file was changed => invalidate cache
        if (contentChanged) {
            APTDriver.invalidateAPT(changedFile.getBuffer());
        }
        boolean scheduleParsing = true; 
        ParentFiles top = changedFileProject.getGraph().getTopParentFiles(changedFile);
        Set<CsmFile> cuStartFiles = top.getCompilationUnits();
        Set<CsmFile> parents = top.getParentFiles();
        if (cuStartFiles.size() > 0) {
            changedFile.clearStateCache();
            GraphContainer.CoherenceFiles coherence = changedFileProject.getGraph().getCoherenceFiles(changedFile);
            Set<CsmFile> coherenceFiles = coherence.getCoherenceFiles();
            Set<CsmFile> affectedFiles = coherenceFiles;
            if (lastFileBasedSignature != null) {
                GraphContainer.CoherenceFiles oldCoherence = lastFileBasedSignature.getCoherenceFiles();
                Set<CsmFile> oldCoherenceFiles = oldCoherence.getCoherenceFiles();
                oldCoherenceFiles.removeAll(coherenceFiles);
                for (CsmFile file : oldCoherenceFiles) {
                    FileImpl impl = (FileImpl)file;
                    ParentFiles orphanCandidateTop = changedFileProject.getGraph().getTopParentFiles(impl);
                    Set<CsmFile> parentOrphanFiles = orphanCandidateTop.getParentFiles();
                    if (parentOrphanFiles.size() == 1 && parentOrphanFiles.contains(impl)) {
                        // header is orphan now
//                        invalidateFileAndPreprocState(changedFileProject, impl);
                        changedFileProject.markAsParsingPreprocStates(impl);
                        if (scheduleParsing) {
                            ParserQueue.instance().add(impl, changedFileProject.getPreprocHandlersForParse(impl, Interrupter.DUMMY), ParserQueue.Position.HEAD);
                        }
                    } else {
                        // header probably changed context add them to affected files collection
                        Set<CsmFile> startFilesForRemainingDetached = orphanCandidateTop.getCompilationUnits();
                        GraphContainer.CoherenceFiles coherenceOfDetached = changedFileProject.getGraph().getCoherenceFiles(impl);
                        Set<CsmFile> coherenceOfDetachedFiles = coherenceOfDetached.getCoherenceFiles();
                        cuStartFiles.addAll(startFilesForRemainingDetached);
                        affectedFiles.addAll(coherenceOfDetachedFiles);
                    }
                }
            }
            // update affected files
            updateStartFilesWithBestStartFiles(affectedFiles, cuStartFiles);
            for (CsmFile file : affectedFiles) {
                if (cuStartFiles.contains(file)) {
                    ((FileImpl)file).clearStateCache();
                } else if (parents.contains(file)) {
                    ((FileImpl)file).clearStateCache();
                    invalidateFileAndPreprocState(changedFileProject, file);
                } else {
                    invalidateFileAndPreprocState(changedFileProject, file);
                }
            }
            if (scheduleParsing) {
                // coherence already invalidated, pass empty set
                addToReparse(changedFileProject, cuStartFiles, new HashSet<CsmFile>(0), false);
            }
        } else {
            if (scheduleParsing) {
                ParserQueue.instance().add(changedFile, changedFileProject.getPreprocHandlersForParse(changedFile, Interrupter.DUMMY), ParserQueue.Position.HEAD);
            }
        }
    }

    static void reparseOnEdit(Collection<FileImpl> toReparse, ProjectBase project, boolean scheduleParsing) {
        if (TRACE) {
            LOG.log(Level.INFO, "reparseOnEdit {0}", toString(toReparse));
        }        
        Set<CsmUID<CsmFile>> topParents = new HashSet<>();
        Set<CsmUID<CsmFile>> parents = new HashSet<>();
        Set<CsmUID<CsmFile>> coherence = new HashSet<>();
        for(FileImpl fileImpl: toReparse){
            ParentFiles top = project.getGraph().getTopParentFiles(fileImpl);
            Set<CsmUID<CsmFile>> units = top.getCompilationUnitsUids();
            if (units.size() > 0) {
                topParents.addAll(units);
                parents.addAll(top.getParentFilesUids());
                coherence.addAll(project.getGraph().getCoherenceFiles(fileImpl).getCoherenceFilesUids());
            } else {
                if (scheduleParsing) {
                    ParserQueue.instance().add(fileImpl, project.getPreprocHandlersForParse(fileImpl, Interrupter.DUMMY), ParserQueue.Position.HEAD);
                }
            }
        }
        if (topParents.size() > 0) {
            Set<CsmFile> topParentsImpl = new HashSet<>();
            for (CsmUID<CsmFile> file : coherence) {
                CsmFile fileImpl = UIDCsmConverter.UIDtoFile(file);
                if (fileImpl != null) {
                    updateStartFilesWithBestStartFiles(Collections.singleton(fileImpl), topParentsImpl);
                }
            }
            for (CsmUID<CsmFile> file : coherence) {
                FileImpl fileImpl = (FileImpl) UIDCsmConverter.UIDtoFile(file);
                if (fileImpl != null) {
                    if (topParents.contains(file)) {
                        topParentsImpl.add(fileImpl);
                        fileImpl.clearStateCache();
                    } else if (parents.contains(file)){
                        fileImpl.clearStateCache();
                        invalidateFileAndPreprocState(project, fileImpl);
                    } else {
                        invalidateFileAndPreprocState(project, fileImpl);
                    }
                }
            }
            if (scheduleParsing) {
                // coherence already invalidated, pass empty set
                addToReparse(project, topParentsImpl, new HashSet<CsmFile>(0), false);
            }
        }
    }

    private static void gatherCoherenceLibrary(Set<CsmFile> coherenceLibrary) {
        while (true) {
            Set<CsmFile> newCoherenceLibrary = new HashSet<>();
            for (CsmFile coherence : coherenceLibrary) {
                newCoherenceLibrary.add(coherence);
                ProjectBase coherenceProject = (ProjectBase) coherence.getProject();
                newCoherenceLibrary.addAll(coherenceProject.getGraph().getIncludedFiles(coherence));
            }
            if (newCoherenceLibrary.size() == coherenceLibrary.size()) {
                return;
            }
            coherenceLibrary.addAll(newCoherenceLibrary);
        }
    }

    /**
     * Reparse including/included files at file properties changed.
     */
    public static void reparseOnPropertyChanged(Collection<NativeFileItem> items, ProjectBase changedProject, boolean invalidateLibs) {
        if (TRACE) {
            LOG.log(Level.INFO, "reparseOnPropertyChanged {0}{1}", new Object[] {invalidateLibs ? "With Invalidating Libs " : "", toString(items)});
        }        
        try {
            ParserQueue.instance().onStartAddingProjectFiles(changedProject);
            Map<FileImpl, NativeFileItem> pairs = new HashMap<>();
            Set<CsmFile> cuStartFiles = new HashSet<>();
            Set<CsmFile> coherence = new HashSet<>();
            Set<CsmFile> coherenceLibrariesFiles = new HashSet<>();
            for (NativeFileItem item : items) {
                if (Utils.acceptNativeItem(item)) {
                    FileImpl file = changedProject.getFile(item.getAbsolutePath(), false);
                    if (file != null) {
                        file.clearStateCache();
                        pairs.put(file, item);
                        cuStartFiles.addAll(changedProject.getGraph().getTopParentFiles(file).getCompilationUnits());
                        coherence.addAll(changedProject.getGraph().getIncludedFiles(file));
                    }
                }
            }
            updateStartFilesWithBestStartFiles(coherence, cuStartFiles);
            for (CsmFile file : coherence) {
                if (!cuStartFiles.contains(file)) {
                    if (changedProject.equals(file.getProject())) {
                        invalidateFileAndPreprocState(changedProject, file);
                    } else {
                        coherenceLibrariesFiles.add(file);
                    }
                }
            }
            if (!TraceFlags.DEEP_REPARSING_OPTIMISTIC) {
                gatherCoherenceLibrary(coherenceLibrariesFiles);
                invalidateFileAndPreprocState(coherenceLibrariesFiles);
            }
            for (CsmFile parent : cuStartFiles) {
                FileImpl parentImpl = (FileImpl) parent;
                if (pairs.containsKey(parentImpl)) {
                    NativeFileItem item = pairs.get(parentImpl);
                    addToReparse(changedProject, item, parentImpl);
                } else {
                    addCompilationUnitToReparse(parentImpl, true);
                }
            }
            if (invalidateLibs) {
                if (TRACE) {
                    LOG.log(Level.INFO, "reparseOnPropertyChanged invalidates all libraries for {0}", changedProject);
                }
                // invalide libraries when asked but after deep reparsing activity
                // because this activity uses information about project dependency and library dependencies
                assert (changedProject instanceof ProjectImpl): "should be ProjectImpl: " + changedProject;
                LibraryManager.getInstance(changedProject.getUnitId()).onProjectPropertyChanged(changedProject);
            }
        } catch (Exception e) {
            DiagnosticExceptoins.register(e);
        } finally {
            ParserQueue.instance().onEndAddingProjectFiles(changedProject);
        }
    }

    /**
     * Reparse included files at file added.
     */
    public static void reparseOnAdded(FileObject addedFile, ProjectBase project) {
        reparseOnAdded(Collections.singleton(addedFile.getNameExt()), project);
    }

    /**
     * Reparse included files at file added.
     */
    static void reparseOnAdded(List<NativeFileItem> toReparse, ProjectBase project) {
        Set<String> names = new HashSet<>();
        for (NativeFileItem item : toReparse) {
            names.add(item.getName());
        }
        reparseOnAdded(names, project);
    }
    
    private static void reparseOnAdded(Set<String> names, ProjectBase project) {
        if (TRACE) {
            LOG.log(Level.INFO, "reparseOnAdded {0}", toString(names));
        }
        Set<CsmFile> resolved = new HashSet<>();
        for (CsmFile file : project.getAllFiles()) {
            findResolved(names, resolved, file);
        }
        if (resolved.size() > 0) {
            Set<CsmFile> top = new HashSet<>();
            Set<CsmFile> coherence = new HashSet<>();
            for (CsmFile file : resolved) {
                top.addAll(project.getGraph().getTopParentFiles(file).getCompilationUnits());
                coherence.add(file);
                coherence.addAll(project.getGraph().getIncludedFiles(file));
            }
            updateStartFilesWithBestStartFiles(coherence, top);
            addToReparse(project, top, coherence, true);
        }
    }

    private static void findResolved(final Set<String> names, final Set<CsmFile> resolved, final CsmFile file) {
        for (CsmInclude incl : file.getIncludes()) {
            String name = incl.getIncludeName().toString();
            int i = Math.max(name.lastIndexOf('\\'), name.lastIndexOf('/'));
            if (i > 0) {
                name = name.substring(i);
            }
            if (names.contains(name)) {
                resolved.add(file);
                break;
            }
        }
    }

    static void reparseOnRemoved(Collection<FileImpl> removedPhysically, Collection<FileImpl> removedAsExcluded, ProjectBase project) {
        if (TRACE) {
            LOG.log(Level.INFO, "reparseOnRemoved \nPHYSICAL:{0}\nEXCLUDED:{1}", new Object[] {toString(removedPhysically), toString(removedAsExcluded)});
        }
        CndFileUtils.clearFileExistenceCache();
        Set<CsmFile> topParents = new HashSet<>();
        Set<CsmFile> coherence = new HashSet<>();
        // physically removed can cause broken #includes => we need to reparse parent and coherence files
        for (FileImpl impl : removedPhysically) {
            if (impl != null) {
                topParents.addAll(project.getGraph().getTopParentFiles(impl).getCompilationUnits());
                coherence.addAll(project.getGraph().getCoherenceFiles(impl).getCoherenceFiles());
                project.getGraph().removeFile(impl);
                topParents.remove(impl);
                coherence.remove(impl);
            }
        }
        // excluded are just removed, because their model excluded from model
        // but physically they could be in place, no need to reparse those who includes them
        for (FileImpl impl : removedAsExcluded) {
            project.getGraph().removeFile(impl);
            topParents.remove(impl);
            coherence.remove(impl);
        }
        addToReparse(project, topParents, coherence, false);
    }

    private static void addToReparse(final ProjectBase changedFileProject, final Set<CsmFile> cuStartFiles, final Set<CsmFile> coherence, boolean invalidateCache) {
        for (CsmFile incl : coherence) {
            if (!cuStartFiles.contains(incl)) {
                if (incl.getProject() == changedFileProject) {
                    invalidateFileAndPreprocState(changedFileProject, incl);
                }
            }
        }
        if (!cuStartFiles.isEmpty()) {
            try {
                // send notifications
                ParserQueue.instance().onStartAddingProjectFiles(changedFileProject);
                for (CsmFile parent : cuStartFiles) {
                    FileImpl parentImpl = (FileImpl) parent;
                    addCompilationUnitToReparse(parentImpl, invalidateCache);
                }
            } catch (Exception e) {
                DiagnosticExceptoins.register(e);
            } finally {
                // send notifications
                ParserQueue.instance().onEndAddingProjectFiles(changedFileProject);
            }
        }
    }

    private static void addCompilationUnitToReparse(final FileImpl fileImpl, final boolean invalidateCache) {
        ProjectBase project = fileImpl.getProjectImpl(true);
        project.markAsParsingPreprocStates(fileImpl);
        fileImpl.markReparseNeeded(invalidateCache);
        ParserQueue.instance().add(fileImpl, fileImpl.getPreprocHandlersForParse(Interrupter.DUMMY), ParserQueue.Position.HEAD);
        if (TraceFlags.USE_DEEP_REPARSING_TRACE) {
            System.out.println("Add file to reparse " + fileImpl.getAbsolutePath() + " from " + project); // NOI18N
        }
    }

    private static void addToReparse(final ProjectBase project, final NativeFileItem nativeFile, final FileImpl file) {
        if (nativeFile.getFileObject() != null && nativeFile.getFileObject().isValid()) {
            file.markReparseNeeded(true);
            PreprocHandler.State state = project.setChangedFileState(nativeFile);
            if (state == null) {
                CndUtils.assertTrue(!file.isValid(), "setChangedFileState returned null for valid file ", file); //NOI18N
            } else {
                if (TraceFlags.USE_DEEP_REPARSING_TRACE) {
                    System.out.println("Add file to reparse " + file.getAbsolutePath() + " from " + project); // NOI18N
                }
                ParserQueue.instance().add(file, state, ParserQueue.Position.HEAD);
            }
        } else {
            assert false;
        }
    }

    private static void invalidateFileAndPreprocState(final ProjectBase changedFileProject, final CsmFile file) {
        FileImpl fileImpl = (FileImpl) file;
        ProjectBase fileProject = fileImpl.getProjectImpl(true);
        if (changedFileProject != null && fileProject != null) {
            if (changedFileProject != fileProject && fileProject.isArtificial()) {
                return;
            }
        }
        if (fileProject != null) {
            fileImpl.clearStateCache();
            fileProject.invalidatePreprocState(fileImpl.getAbsolutePath());
            fileImpl.markReparseNeeded(false);
            if (TraceFlags.USE_DEEP_REPARSING_TRACE) {
                System.out.println("Invalidate file to reparse " + file.getAbsolutePath() + " from " + fileProject); // NOI18N
            }
        }
    }

    private static void invalidateFileAndPreprocState(Set<CsmFile> coherenceLibrary) {
        for (CsmFile parent : coherenceLibrary) {
            CsmProject project = parent.getProject();
            if (project instanceof ProjectBase) {
                FileImpl parentImpl = (FileImpl) parent;
                parentImpl.clearStateCache();
                ((ProjectBase) project).invalidatePreprocState(parentImpl.getAbsolutePath());
                parentImpl.markReparseNeeded(false);
                if (TraceFlags.USE_DEEP_REPARSING_TRACE) {
                    System.out.println("Invalidate file to reparse " + parent.getAbsolutePath() + " from " + project); // NOI18N
                }
            }
        }
    }
    
    private static void updateStartFilesWithBestStartFiles(Set<CsmFile> coherence, Set<CsmFile> cuStartFiles) {
        for (CsmFile csmFile : coherence) {
            // append extra start files
            if (!cuStartFiles.contains(csmFile)) {
                Collection<CsmCompilationUnit> compilationUnits = CsmFileInfoQuery.getDefault().getCompilationUnits(csmFile, 0);
                for (CsmCompilationUnit cu : compilationUnits) {
                    CsmFile startFile = cu.getStartFile();
                    if (startFile != null) {
                        cuStartFiles.add(startFile);
                    }
                }
            }
        }
    }
    
    public static String toString(Collection<?> files) {
        StringBuilder out = new StringBuilder();
        for (Object elem : files) {
            if (elem instanceof FileImpl) {
                out.append(((FileImpl) elem).getAbsolutePath());
            } else if (elem instanceof NativeFileItem) {
                out.append(((NativeFileItem) elem).getAbsolutePath());
            } else {
                out.append(elem.toString());
            }
        }
        return out.toString();
    }    
    
    private static void checkFileState(FileImpl fileImpl, String msg) {
        if (fileImpl.getState() == FileImpl.State.INITIAL) {
            LOG.log(Level.INFO, "{0} for INITIAL {1}", new Object[]{msg, fileImpl.getAbsolutePath()});
        }
    }    
}
