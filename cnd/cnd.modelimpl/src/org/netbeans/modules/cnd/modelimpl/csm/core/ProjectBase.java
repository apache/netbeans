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

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.NameAcceptor;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.api.project.IncludePath;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItem.Language;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectItemsAdapter;
import org.netbeans.modules.cnd.api.project.NativeProjectItemsListener;
import org.netbeans.modules.cnd.api.project.NativeProjectSupport;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTFileCacheEntry;
import org.netbeans.modules.cnd.apt.support.APTFileCacheManager;
import org.netbeans.modules.cnd.apt.support.APTFileSearch;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.api.PPIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTIncludePathStorage;
import org.netbeans.modules.cnd.apt.support.api.PPMacroMap;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler.State;
import org.netbeans.modules.cnd.apt.support.APTSystemStorage;
import org.netbeans.modules.cnd.apt.support.ClankDriver;
import org.netbeans.modules.cnd.apt.support.IncludeDirEntry;
import org.netbeans.modules.cnd.apt.support.api.StartEntry;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.debug.CndTraceFlags;
import org.netbeans.modules.cnd.debug.DebugUtils;
import org.netbeans.modules.cnd.indexing.api.CndTextIndex;
import org.netbeans.modules.cnd.modelimpl.cache.impl.WeakContainer;
import org.netbeans.modules.cnd.modelimpl.content.project.ClassifierContainer;
import org.netbeans.modules.cnd.modelimpl.content.project.DeclarationContainerProject;
import org.netbeans.modules.cnd.modelimpl.content.project.FileContainer;
import org.netbeans.modules.cnd.modelimpl.content.project.FileContainer.FileEntry;
import org.netbeans.modules.cnd.modelimpl.content.project.GraphContainer;
import org.netbeans.modules.cnd.modelimpl.content.project.IncludedFileContainer;
import org.netbeans.modules.cnd.modelimpl.content.project.IncludedFileContainer.Storage;
import org.netbeans.modules.cnd.modelimpl.content.project.ProjectComponent;
import org.netbeans.modules.cnd.modelimpl.csm.ClassEnumBase;
import org.netbeans.modules.cnd.modelimpl.csm.ForwardClass;
import org.netbeans.modules.cnd.modelimpl.csm.ForwardEnum;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionImplEx;
import org.netbeans.modules.cnd.modelimpl.csm.MutableDeclarationsContainer;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceImpl;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.Terminator;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.impl.services.FileInfoQueryImpl;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTTokenStreamProducer;
import org.netbeans.modules.cnd.modelimpl.platform.CsmEventDispatcher;
import org.netbeans.modules.cnd.modelimpl.platform.ModelSupport;
import org.netbeans.modules.cnd.modelimpl.repository.ClassifierContainerKey;
import org.netbeans.modules.cnd.modelimpl.repository.FileContainerKey;
import org.netbeans.modules.cnd.modelimpl.repository.GraphContainerKey;
import org.netbeans.modules.cnd.modelimpl.repository.KeyHolder;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.ProjectDeclarationContainerKey;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.ProjectNameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.trace.TraceUtils;
import org.netbeans.modules.cnd.modelimpl.uid.LazyCsmCollection;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDManager;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.api.UnitDescriptor;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.dlight.libs.common.InvalidFileObjectSupport;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Cancellable;
import org.openide.util.CharSequences;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.Parameters;

/**
 * Base class for CsmProject implementation
 */
public abstract class ProjectBase implements CsmProject, Persistent, SelfPersistent, CsmIdentifiable,
        CndFileSystemProvider.CndFileSystemProblemListener {

    /** Creates a new instance of CsmProjectImpl */
    protected ProjectBase(ModelImpl model, FileSystem fs, Object platformProject, CharSequence name, Key key) {
        namespaces = new ConcurrentHashMap<>();
        this.uniqueName = getUniqueName(fs, platformProject);
        RepositoryUtils.openUnit(key);
        unitId = key.getUnitId();
        setStatus(Status.Initial);
        this.name = ProjectNameCache.getManager().getString(name);
        this.fileSystem = fs;
        init(model, platformProject);
        sysAPTData = APTSystemStorage.getInstance();
        userPathStorage = new APTIncludePathStorage();
        declarationsSorageKey = new ProjectDeclarationContainerKey(unitId);
        weakDeclarationContainer = new WeakContainer<>(this, declarationsSorageKey);
        classifierStorageKey = new ClassifierContainerKey(unitId);
        weakClassifierContainer = new WeakContainer<>(this, classifierStorageKey);
        fileContainerKey = new FileContainerKey(unitId);
        weakFileContainer = new WeakContainer<>(this, fileContainerKey);
        graphStorageKey = new GraphContainerKey(unitId);
        weakGraphContainer = new WeakContainer<>(this, graphStorageKey);
        includedFileContainer = new IncludedFileContainer(this);
        initFields();
        libraryManager = LibraryManager.getInstance(getUnitId());
    }

    /*package*/final void initFields() {
        NamespaceImpl ns;
        synchronized (namespaceLock) {
            ns = NamespaceImpl.create(this, false);
        }
        assert ns != null;
        this.globalNamespaceUID = UIDCsmConverter.namespaceToUID(ns);
        DeclarationContainerProject declarationContainer = new DeclarationContainerProject(this);
        CndUtils.assertTrue(declarationsSorageKey.equals(declarationContainer.getKey()));
        weakDeclarationContainer.clear();
        ClassifierContainer classifierContainer = new ClassifierContainer(this);
        CndUtils.assertTrue(classifierStorageKey.equals(classifierContainer.getKey()));
        weakClassifierContainer.clear();
        FileContainer fileContainer = new FileContainer(this);
        CndUtils.assertTrue(fileContainerKey.equals(fileContainer.getKey()));
        weakFileContainer.clear();
        GraphContainer graphContainer = new GraphContainer(this);
        CndUtils.assertTrue(graphStorageKey.equals(graphContainer.getKey()));
        weakGraphContainer.clear();
        includedFileContainer.clear();
        FAKE_GLOBAL_NAMESPACE = NamespaceImpl.create(this, true);
    }

    private void init(ModelImpl model, Object platformProject) {
        this.model = model;
        assert (platformProject instanceof NativeProject) || (platformProject instanceof CharSequence);
        this.platformProject = platformProject;
        // remember in repository
        RepositoryUtils.hang(this);
        // create global namespace

        if (TraceFlags.CLOSE_AFTER_PARSE) {
            Terminator.create(this);
        }
    }

    private boolean checkConsistency(boolean restoring) {
        long time = TraceFlags.TIMING ? System.currentTimeMillis() : 0;
        if (getFileContainer() == FileContainer.empty()) {
            return false;
        }
        if (getDeclarationsSorage() == DeclarationContainerProject.empty()) {
            return false;
        }
        if (getGraph() == GraphContainer.empty()) {
            return false;
        }
        if (getGlobalNamespace() == FAKE_GLOBAL_NAMESPACE) {
            return false;
        }
        if (TraceFlags.CHECK_CONSISTENCY || CndUtils.isUnitTestMode()) {
            if (TraceFlags.TIMING) {
                System.err.printf("Consistency check took %d ms%n", System.currentTimeMillis() - time);
            }
            time = System.currentTimeMillis();
            checkFileContainerConsistency(restoring);
            if (TraceFlags.TIMING) {
                System.err.printf("File Container Consistency check took %d ms%n", System.currentTimeMillis() - time);
            }
        }
        return true;
    }

    private void checkFileContainerConsistency(boolean restoring) {
        if (this.isArtificial()) {
            return;
        }
        Set<FileImpl> allFileImpls = new HashSet<>(this.getAllFileImpls());
        Storage storageForSelf = this.includedFileContainer.getStorageForProject(this);
        if (storageForSelf != null) {
            for (Map.Entry<CharSequence, FileEntry> entry : storageForSelf.getInternalMap().entrySet()) {
                FileImpl file = getFileContainer().getFile(entry.getKey(), true);
                if (file == null || !allFileImpls.contains(file)) {
                    CndUtils.assertTrueInConsole(false, "no file enty for included file ", entry);
                }
            }
        }
        for (FileImpl fileImpl : allFileImpls) {
            checkFileEntryConsistency(fileImpl, restoring);
        }
    }

    private void checkFileEntryConsistency(FileImpl fileImpl, boolean restoring) {
        CharSequence fileKey = fileImpl.getAbsolutePath();
        FileImpl.State fileState = fileImpl.getState();
        FileEntry entry = getFileContainer().getEntry(fileKey);
        if (entry != null) {
            Object lock = entry.getLock();
            synchronized (lock) {
                List<PreprocessorStatePair> fcPairs = new ArrayList<>(entry.getStatePairs());
                if (fcPairs.isEmpty() && fileState != FileImpl.State.INITIAL) {
                    CndUtils.assertTrueInConsole(false, "no states for own file ", fileImpl);
                }
                boolean hasParsing = false;
                Boolean hasValid = fcPairs.isEmpty() ? Boolean.TRUE : null;
                // check own File Container and remember what we have found for checking own includes files
                // from includedFileContainer later
                for (PreprocessorStatePair fcPair : fcPairs) {
                    if (fileState == FileImpl.State.PARSED) {
                        CndUtils.assertTrueInConsole(fcPair.state.isValid(), "FC Should not contain invalid ", fcPair);
                        CndUtils.assertTrueInConsole(fcPair.pcState != FilePreprocessorConditionState.PARSING, "FC Should not contain PARSING ", fcPair);
                    }
                    if (fcPair.pcState == FilePreprocessorConditionState.PARSING) {
                        hasParsing = true;
                    }
                    if (hasValid == null) {
                        hasValid = fcPair.state.isValid();
                    }
                    CndUtils.assertTrueInConsole(hasValid == fcPair.state.isValid(), "FC Should not contain " + hasValid, fcPair);
                }
                CsmUID<CsmFile> testFileUID = entry.getTestFileUID();
                FileImpl testFileImpl = (FileImpl) UIDCsmConverter.UIDtoFile(testFileUID);
                if (!testFileImpl.equals(fileImpl)) {
                    CndUtils.assertTrueInConsole(false, "different files: " + fileImpl, testFileImpl);
                }
                // check if input file was included by 'this' project
                FileEntry includedFileEntry = this.includedFileContainer.getIncludedFileEntry(lock, this, fileKey);
                if (includedFileEntry != null) {
                    // it was included => check in which states it was included from 'this' project
                    List<PreprocessorStatePair> inclPairs = new ArrayList<>(includedFileEntry.getStatePairs());
                    if (inclPairs.isEmpty()) {
                        CndUtils.assertTrueInConsole(false, "no included states for included file ", fileImpl);
                    } else {
                        for (PreprocessorStatePair inclPair : inclPairs) {
                            if (fileState == FileImpl.State.PARSED) {
                                CndUtils.assertTrueInConsole(inclPair.state.isValid(), "Should not contain invalid ", inclPair);
                                CndUtils.assertTrueInConsole(inclPair.pcState != FilePreprocessorConditionState.PARSING, "Should not contain PARSING ", inclPair);
                            }
                            // check that any own include files are contributing in own FC container the same way
                            List<PreprocessorStatePair> statesToKeep = new ArrayList<>(4);
                            AtomicBoolean newStateFound = new AtomicBoolean();
                            ComparisonResult resultPP;
                            resultPP = fillStatesToKeepBasedOnPPState(inclPair.state, fcPairs, statesToKeep, newStateFound);
                            if (inclPair.state.isValid()) {
                                if (resultPP != ComparisonResult.KEEP_WITH_OTHERS) {
                                    CndUtils.assertTrueInConsole(false, "Should not contribute [" + restoring + "," + hasParsing + "] " + newStateFound + " " + resultPP + " pair into File Container (state based) " + inclPair + "vs.\n", fcPairs);
                                }
                            } else {
                                CndUtils.assertTrueInConsole(resultPP == ComparisonResult.DISCARD && !hasValid, "Should not contribute invalid [" + restoring + "," + hasParsing + "] " + newStateFound + " " + resultPP + " pair into File Container (state based) " + inclPair + "vs.\n", fcPairs);
                            }
                            // check that any own include files are contributing has not worse PC state in own FC container
                            ComparisonResult resultPC = fillStatesToKeepBasedOnPCState(inclPair.pcState, fcPairs, statesToKeep);
                            if (resultPC != ComparisonResult.DISCARD) {
                                CndUtils.assertTrueInConsole(inclPair.pcState == FilePreprocessorConditionState.PARSING && hasParsing, "Should not contribute [" + restoring + "," + hasParsing + "] " + newStateFound + " " + resultPC + " pair into File Container (PCState based) " + inclPair, fcPairs);
                            }
                        }
                    }
                }
            }
        } else {
            CndUtils.assertTrueInConsole(false, "no entry for ", fileKey);
        }
    }

    private void setStatus(Status newStatus) {
        //System.err.printf("CHANGING STATUS %s -> %s for %s (%s)\n", status, newStatus, name, getClass().getName());
        status = newStatus;
    }

    protected static ProjectBase readInstance(ModelImpl model, Key key, Object platformProject, CharSequence name) {

        long time = 0;
        if (TraceFlags.TIMING) {
            System.err.printf("Project %s: instantiating...%n", name);
            time = System.currentTimeMillis();
        }

        assert TraceFlags.PERSISTENT_REPOSITORY;
        RepositoryUtils.openUnit(key);
        Persistent o = RepositoryUtils.get(key);
        if (o != null) {
            assert o instanceof ProjectBase;
            ProjectBase impl = (ProjectBase) o;
            CharSequence aName = ProjectNameCache.getManager().getString(name);
            if (!impl.name.equals(aName)) {
                impl.setName(aName);
            }
            impl.init(model, platformProject);
            if (TraceFlags.TIMING) {
                time = System.currentTimeMillis() - time;
                System.err.printf("Project %s: loaded. %d ms%n", name, time);
            }
            UIDManager.instance().clearProjectCache(key);
            if (impl.checkConsistency(true)) {
                return impl;
            }
        }
        return null;
    }

    @Override
    public final CsmNamespace getGlobalNamespace() {
        return _getGlobalNamespace();
    }

    @Override
    public final CharSequence getName() {
        return name;
    }

    protected final void setName(CharSequence name) {
        this.name = name;
    }

    /**
     * Returns a string that uniquely identifies this project.
     * One should never rely on this name structure,
     * just use it as in unique identifier
     */
    public final CharSequence getUniqueName() {
        return uniqueName;
    }

    public static CharSequence getUniqueName(NativeProject platformProject) {
        return getUniqueName(platformProject.getFileSystem(), platformProject);
    }

    public final LibraryManager getLibraryManager() {
        return libraryManager;
    }

    @Override
    public String getDisplayName() {
        if (CndFileUtils.isLocalFileSystem(fileSystem)) {
            return name.toString();
        } else {
            return NbBundle.getMessage(getClass(), "ProjectDisplayName", name, fileSystem.getDisplayName());
        }
    }

    @Override
    public String getHtmlDisplayName() {
        if (CndFileUtils.isLocalFileSystem(fileSystem)) {
            return name.toString();
        } else {
            return NbBundle.getMessage(getClass(), "ProjectHtmlDisplayName", name, fileSystem.getDisplayName());
        }
    }

    public static CharSequence getRepositoryUnitName(FileSystem fs, CharSequence projectSourceRoot) {
        Parameters.notNull("FileSystem", fs); //NOI18N
        String result = projectSourceRoot.toString() + "/N/"; // NOI18N
        return ProjectNameCache.getManager().getString(result);
    }

    public static CharSequence getRepositoryUnitName(FileSystem fs, NativeProject nativeProject) {
        Parameters.notNull("FileSystem", fs); //NOI18N
        String result = ((NativeProject) nativeProject).getProjectRoot() + "/N/"; // NOI18N
        return ProjectNameCache.getManager().getString(result);
    }

    public static CharSequence getUniqueName(FileSystem fs, Object platformProject) {
        Parameters.notNull("FileSystem", fs); //NOI18N
        String postfix = CndFileUtils.isLocalFileSystem(fs) ? "" : fs.getDisplayName();
        String result;
        if (platformProject instanceof NativeProject) {
            result = ((NativeProject) platformProject).getProjectRoot() + "/N/" + postfix; // NOI18N
        } else if (platformProject instanceof CharSequence) {
            result = ((CharSequence)platformProject).toString() + "/L/" + postfix; // NOI18N
        } else if (platformProject == null) {
            throw new IllegalArgumentException("Incorrect platform project: null"); // NOI18N
        } else {
            throw new IllegalArgumentException("Incorrect platform project class: " + platformProject.getClass()); // NOI18N
        }
        return ProjectNameCache.getManager().getString(result);
    }

    /** Gets an object, which represents correspondent IDE project */
    @Override
    public final Object getPlatformProject() {
        assert (platformProject == null) || (platformProject instanceof NativeProject) || (platformProject instanceof CharSequence);
        return platformProject;
    }

    private UnitDescriptor getUnitDescriptor() {
         if (platformProject instanceof NativeProject) {
             return KeyUtilities.createUnitDescriptor((NativeProject) platformProject);
         } else if (platformProject instanceof CharSequence) {
             return KeyUtilities.createUnitDescriptor((CharSequence) platformProject, fileSystem);
         } else if (platformProject == null) {
             return null;
         } else {
             throw new IllegalArgumentException("Incorrect platform project class: " + platformProject.getClass()); // NOI18N
         }
     }

    /** Gets an object, which represents correspondent IDE project */
    protected final void setPlatformProject(CharSequence platformProject) {
        setPlatformProjectImpl(platformProject);
    }

    protected final void setPlatformProject(NativeProject platformProject) {
        setPlatformProjectImpl(platformProject);
    }

    private void setPlatformProjectImpl(Object platformProject) {
        assert (platformProject == null) || (platformProject instanceof NativeProject) || (platformProject instanceof CharSequence);
        CndUtils.assertTrue(this.platformProject == null);
        CndUtils.assertNotNull(platformProject, "Passing null project for ", this);
        this.platformProject = platformProject;
        checkUniqueNameConsistency();
    }

    private void checkUniqueNameConsistency() {
        if (CndUtils.isDebugMode()) {
            CharSequence expectedUniqueName = getUniqueName(fileSystem, platformProject);
            CharSequence defactoUniqueName = this.uniqueName;
            if (!defactoUniqueName.equals(expectedUniqueName)) {
                CndUtils.assertTrue(false,
                        "Existing project unique name differ: " + defactoUniqueName + " - expected " + expectedUniqueName); //NOI18N
            }
        }
    }

    /** Finds namespace by its qualified name */
    public final CsmNamespace findNamespace(CharSequence qualifiedName, boolean findInLibraries) {
        CsmNamespace result = findNamespace(qualifiedName);
        if (result == null && findInLibraries) {
            for (Iterator<CsmProject> it = getLibraries().iterator(); it.hasNext();) {
                CsmProject lib = it.next();
                result = lib.findNamespace(qualifiedName);
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

    /** Finds namespace by its qualified name */
    @Override
    public final CsmNamespace findNamespace(CharSequence qualifiedName) {
        CsmNamespace nsp = _getNamespace(qualifiedName);
        return nsp;
    }

    private static CharSequence getNestedNamespaceQualifiedName(CharSequence name, NamespaceImpl parent, boolean createForEmptyNames) {
        StringBuilder sb = new StringBuilder(name);
        if (parent != null) {
            if (name.length() == 0 && createForEmptyNames) {
                sb.append(parent.getNameForUnnamedElement());
            }
            if (!parent.isGlobal()) {
                sb.insert(0, APTUtils.SCOPE);
                sb.insert(0, parent.getQualifiedName());
            }
        }
        return sb;
    }

    /**
     * Use addNamespaceDefinition instead!
     *
     * @param parent
     * @param name
     * @return
     * @deprecated
     */
    @Deprecated
    public final NamespaceImpl findNamespaceCreateIfNeeded(NamespaceImpl parent, CharSequence name) {
        synchronized (namespaceLock) {
            CharSequence qualifiedName = ProjectBase.getNestedNamespaceQualifiedName(name, parent, true);
            NamespaceImpl nsp = _getNamespace(qualifiedName);
            if (nsp == null) {
                nsp = NamespaceImpl.create(this, parent, name, qualifiedName);
            }
            return nsp;
        }
    }

    public final CsmUID<CsmNamespace> addNamespaceDefinition(NamespaceImpl parent, CsmNamespaceDefinition nsDefinition) {
        synchronized (namespaceLock) {
            CharSequence qualifiedName = ProjectBase.getNestedNamespaceQualifiedName(nsDefinition.getName(), parent, true);
            NamespaceImpl nsp = _getNamespace(qualifiedName);
            if (nsp == null) {
                nsp = NamespaceImpl.create(this, parent, nsDefinition.getName(), qualifiedName);
            }
            nsp.addNamespaceDefinition(nsDefinition);
            return UIDCsmConverter.namespaceToUID(nsp);
        }
    }

    public final void removeNamespaceDefinition(CsmNamespaceDefinition nsDefinition) {
        synchronized (namespaceLock) {
            NamespaceImpl nsp = (NamespaceImpl) nsDefinition.getNamespace();
            if (nsp != null) {
                nsp.removeNamespaceDefinition(nsDefinition);
            }
        }
    }

    public final boolean holdsNamespaceLock() {
        return Thread.holdsLock(namespaceLock);
    }

    public final void registerNamespace(NamespaceImpl namespace) {
        assert holdsNamespaceLock() : "Modifications of namespace can be performed only under namespaceLock!";
        _registerNamespace(namespace);
    }

    public final void unregisterNamesace(NamespaceImpl namespace) {
        assert holdsNamespaceLock() : "Modifications of namespace can be performed only under namespaceLock!";
        _unregisterNamespace(namespace);
    }

    public final CsmClassifier findClassifier(CharSequence qualifiedName, boolean findInLibraries) {
        CsmClassifier result = findClassifier(qualifiedName);
        if (result == null && findInLibraries) {
            for (Iterator<CsmProject> it = getLibraries().iterator(); it.hasNext();) {
                CsmProject lib = it.next();
                result = lib.findClassifier(qualifiedName);
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public final CsmClassifier findClassifier(CharSequence qualifiedName) {
        CsmClassifier result = getClassifierSorage().getClassifier(qualifiedName);
        return result;
    }

    @Override
    public final Collection<CsmClassifier> findClassifiers(CharSequence qualifiedName) {
        CsmClassifier result = getClassifierSorage().getClassifier(qualifiedName);
        Collection<CsmClassifier> out = new ArrayList<>();
        //Collection<CsmClassifier> out = new LazyCsmCollection<CsmClassifier, CsmClassifier>(new ArrayList<CsmUID<CsmClassifier>>(), TraceFlags.SAFE_UID_ACCESS);
        if (result != null) {
            if (CsmKindUtilities.isBuiltIn(result)) {
                return Collections.<CsmClassifier>singletonList(result);
            }
            CharSequence[] allClassifiersUniqueNames = Utils.getAllClassifiersUniqueNames(result.getUniqueName());
            Collection<CsmClassifier> fwds = new ArrayList<>(1);
            for (CharSequence curUniqueName : allClassifiersUniqueNames) {
                Collection<? extends CsmDeclaration> decls = this.findDeclarations(curUniqueName);
                @SuppressWarnings("unchecked")
                Collection<CsmClassifier> classifiers = (Collection<CsmClassifier>) decls;
                for (CsmClassifier csmClassifier : classifiers) {
                    if (ForwardClass.isForwardClass(csmClassifier)) {
                        fwds.add(csmClassifier);
                    } else {
                        out.add(csmClassifier);
                    }
                }
            }
            // All forwards move at the end
            out.addAll(fwds);
        }
        return out;
    }

    @Override
    public final Collection<CsmInheritance> findInheritances(CharSequence name) {
        return getClassifierSorage().getInheritances(name);
    }

    @Override
    public final CsmDeclaration findDeclaration(CharSequence uniqueName) {
        return getDeclarationsSorage().getDeclaration(uniqueName);
    }

    @Override
    public final Collection<CsmOffsetableDeclaration> findDeclarations(CharSequence uniqueName) {
        return getDeclarationsSorage().findDeclarations(uniqueName);
    }

    public final Collection<CsmOffsetableDeclaration> findDeclarationsByPrefix(String uniquNamePrefix) {
        // To improve performance use char(255) instead real Character.MAX_VALUE
        char maxChar = 255; //Character.MAX_VALUE;
        return getDeclarationsSorage().getDeclarationsRange(uniquNamePrefix, uniquNamePrefix + maxChar); // NOI18N
    }

    public final Collection<CsmOffsetableDeclaration> findExternalDeclarations(CsmFile file) {
        return getDeclarationsSorage().findExternalDeclarations(file);
    }

    public final Collection<CsmFriend> findFriendDeclarations(CsmOffsetableDeclaration decl) {
        return getDeclarationsSorage().findFriends(decl);
    }

    public final boolean registerDeclaration(CsmOffsetableDeclaration decl) {

        if (!Utils.canRegisterDeclaration(decl)) {
            if (TraceFlags.TRACE_REGISTRATION) {
                traceRegistration("not registered decl " + decl + " UID " + UIDs.get(decl)); //NOI18N
            }
            return false;
        }

        if (CsmKindUtilities.isClass(decl) || CsmKindUtilities.isEnum(decl)) {

            ClassEnumBase<?> cls = (ClassEnumBase<?>) decl;
            CharSequence qname = cls.getQualifiedName();

            synchronized (classifierReplaceLock) {
                CsmClassifier old = getClassifierSorage().getClassifier(qname);
                if (old != null) {
                    // don't register if the new one is weaker
                    if (cls.shouldBeReplaced(old)) {
                        if (TraceFlags.TRACE_REGISTRATION) {
                            traceRegistration("not registered decl " + decl + " UID " + UIDs.get(decl)); //NOI18N
                        }
                        return false;
                    }
                    // remove the old one if the new one is stronger
                    if ((old instanceof ClassEnumBase<?>) && ((ClassEnumBase<?>) old).shouldBeReplaced(cls)) {
                        if (TraceFlags.TRACE_REGISTRATION) {
                            System.err.println("disposing old decl " + old + " UID " + UIDs.get(decl)); //NOI18N
                        }
                        ((ClassEnumBase<?>) old).dispose();
                    }
                }
                getDeclarationsSorage().putDeclaration(decl);
                getClassifierSorage().putClassifier((CsmClassifier) decl);
            }

        } else if (CsmKindUtilities.isTypedef(decl) || CsmKindUtilities.isTypeAlias(decl)) { // isClassifier(decl) or isTypedef(decl) ??
            getDeclarationsSorage().putDeclaration(decl);
            getClassifierSorage().putClassifier((CsmClassifier) decl);
        } else {
            // only classes, enums and typedefs are registered as classifiers;
            // even if you implement CsmClassifier, this doesn't mean you atomatically get there ;)
            getDeclarationsSorage().putDeclaration(decl);
        }

        if (TraceFlags.TRACE_REGISTRATION) {
            System.err.println("registered " + decl + " UID " + UIDs.get(decl)); //NOI18N
        }
        return true;
    }

    public final void unregisterDeclaration(CsmOffsetableDeclaration decl) {
        if (TraceFlags.TRACE_REGISTRATION) {
            traceRegistration("unregistered " + decl + " UID " + UIDs.get(decl)); //NOI18N
        }
        if (decl instanceof CsmClassifier) {
            getClassifierSorage().removeClassifier(decl);
        }
        getDeclarationsSorage().removeDeclaration(decl);
    }

    static final Logger WAIT_PARSE_LOGGER = Logger.getLogger("cnd.wait.parse"); // NOI18N

    private static void traceRegistration(String text) {
        assert TraceFlags.TRACE_REGISTRATION : "TraceFlags.TRACE_REGISTRATION should be checked *before* call !"; //NOI18N
        System.err.printf("registration: %s%n", text);
    }

    /** to be overridden */
    protected void addModifiedFile(FileImpl file) {
    }

    /** to be overridden */
    protected void removeModifiedFile(FileImpl file) {
    }

    @Override
    public final void waitParse() {
        if (WAIT_PARSE_LOGGER.isLoggable(Level.FINE)) {
            WAIT_PARSE_LOGGER.fine(String.format("##> waitParse %s %d", getName(), System.currentTimeMillis()));
        }
        try {
            boolean insideParser = ParserThreadManager.instance().isParserThread();
            if (insideParser) {
                new Throwable("project.waitParse should NEVER be called in parser thread !!!").printStackTrace(System.err); // NOI18N
            }
            if (insideParser) {
                return;
            }
            ensureFilesCreated();
            ensureChangedFilesEnqueued();
            model.waitModelTasks();
            waitParseImpl();
        } finally {
            if (WAIT_PARSE_LOGGER.isLoggable(Level.FINE)) {
                WAIT_PARSE_LOGGER.fine(String.format("##< waitParse %d", System.currentTimeMillis()));
            }
        }
    }

    private void waitParseImpl() {
        synchronized (waitParseLock) {
            while (ParserQueue.instance().hasPendingProjectRelatedWork(this, null)) {
                try {
                    //FIXUP - timeout is a workaround for #146436 hang on running unit tests
                    waitParseLock.wait(10000);
                } catch (InterruptedException ex) {
                    // do nothing
                }
            }
        }
    }

    protected void ensureChangedFilesEnqueued() {
    }

    /**
     * @param skipFile if null => check all files, otherwise skip checking
     * this file
     *
     */
    protected boolean hasChangedFiles(CsmFile skipFile) {
        return false;
    }

    protected boolean hasEditedFiles() {
        return false;
    }

    private Set<FileSystem> getIncludesFileSystems(NativeProject nativeProject) {
        Set<FileSystem> fileSystems = new HashSet<>();
        for (IncludePath fsPath : nativeProject.getSystemIncludePaths()) {
            fileSystems.add(fsPath.getFileSystem());
        }
        return fileSystems;
    }


    @Override
    public void problemOccurred(FSPath fsPath) {
        synchronized (fileSystemProblemsLock) {
            hasFileSystemProblems = true;
        }
    }

    @Override
    public void recovered(FileSystem fileSystem) {
        boolean prev;
        synchronized (fileSystemProblemsLock) {
            prev = hasFileSystemProblems;
            hasFileSystemProblems = false;
        }
        if (prev) {
            ModelImpl.instance().scheduleReparse(Collections.<CsmProject>singleton(this));
        }
    }

    public final void enableProjectListeners(boolean enable) {
        if (TraceFlags.MERGE_EVENTS) {
            if (platformProject instanceof NativeProject) {
                // should be in ProjectImpl, but leaving it here makes diff more clear
                CsmEventDispatcher.getInstance().enableListening(this, enable);
            }
        } else {
            synchronized (projectListenerLock) {
                if (projectListener != null) {
                    projectListener.enableListening(enable);
                }
            }
        }
    }

    protected final void registerProjectListeners() {
        if (platformProject instanceof NativeProject) {
            if (TraceFlags.MERGE_EVENTS) {
                // should be in ProjectImpl, but leaving it here makes diff more clear
                CsmEventDispatcher.getInstance().registerProject(this);
                NativeProject nativeProject = (NativeProject) platformProject;
                for (FileSystem fs : getIncludesFileSystems(nativeProject)) {
                    CndFileSystemProvider.addFileSystemProblemListener(this, fs);
                }
            } else {
                synchronized (projectListenerLock) {
                    if (platformProject instanceof NativeProject) {
                        if (projectListener == null) {
                            projectListener = new NativeProjectListenerImpl(getModel(), (NativeProject) platformProject, this);
                        }
                        NativeProject nativeProject = (NativeProject) platformProject;
                        nativeProject.addProjectItemsListener(projectListener);
                        for (FileSystem fs : getIncludesFileSystems(nativeProject)) {
                            CndFileSystemProvider.addFileSystemProblemListener(this, fs);
                        }
                    }
                }
            }
        }
    }

    protected final void unregisterProjectListeners() {
        if (platformProject instanceof NativeProject) {
            if (TraceFlags.MERGE_EVENTS) {
                // should be in ProjectImpl, but leaving it here makes diff more clear
                CsmEventDispatcher.getInstance().unregisterProject(this);
                NativeProject nativeProject = (NativeProject) platformProject;
                for (FileSystem fs : getIncludesFileSystems(nativeProject)) {
                    CndFileSystemProvider.removeFileSystemProblemListener(this, fs);
                }
            } else {
                synchronized (projectListenerLock) {
                    if (projectListener != null) {
                        if (platformProject instanceof NativeProject) {
                            NativeProject nativeProject = (NativeProject) platformProject;
                            nativeProject.removeProjectItemsListener(projectListener);
                            for (FileSystem fs : getIncludesFileSystems(nativeProject)) {
                                CndFileSystemProvider.removeFileSystemProblemListener(this, fs);
                            }
                        }
                        projectListener = null;
                    }
                }
            }
        }
    }

    /*package*/ final void scheduleReparse() {
        ensureFilesCreated();
        //DeepReparsingUtils.reparseOnEdit(this.getAllFileImpls(), this, true);
    }

    private final Object fileCreateLock = new Object();

    protected void ensureFilesCreated() {
        if (status == Status.Ready) {
            return;
        }
        boolean notify = false;
        try {
            synchronized (fileCreateLock) {
                if (status == Status.Initial || status == Status.Restored) {
                    try {
                        setStatus((status == Status.Initial) ? Status.AddingFiles : Status.Validating);
                        long time = 0;
                        if (TraceFlags.SUSPEND_PARSE_TIME != 0) {
                            System.err.println("suspend queue");
                            ParserQueue.instance().suspend();
                            if (TraceFlags.TIMING) {
                                time = System.currentTimeMillis();
                            }
                        }
                        ParserQueue.instance().onStartAddingProjectFiles(this);
                        notify = true;
                        registerProjectListeners();
                        NativeProject nativeProject = ModelSupport.getNativeProject(platformProject);
                        if (nativeProject != null) {
                            try {
                                ParserQueue.instance().suspend();
                                createProjectFilesIfNeed(nativeProject);
                            } finally {
                                ParserQueue.instance().resume();
                            }
                        }
                        if (TraceFlags.SUSPEND_PARSE_TIME != 0) {
                            if (TraceFlags.TIMING) {
                                time = System.currentTimeMillis() - time;
                                System.err.println("getting files from project system + put in queue took " + time + "ms");
                            }
                            System.err.println("sleep for " + TraceFlags.SUSPEND_PARSE_TIME + "sec before resuming queue");
                            sleep(TraceFlags.SUSPEND_PARSE_TIME * 1000);
                        }
                    } finally {
                        if (TraceFlags.SUSPEND_PARSE_TIME != 0) {
                            System.err.println("woke up after sleep");
                            ParserQueue.instance().resume();
                        }
                        setStatus(Status.Ready);
                    }
                }
            }
        } finally {
            if (notify) {
                ParserQueue.instance().onEndAddingProjectFiles(this);
            }
        }
    }

    private void sleep(int millisec) {
        try {
            Thread.sleep(millisec);
        } catch (InterruptedException ex) {
            // do nothing
        }
    }

    private void createProjectFilesIfNeed(NativeProject nativeProject) {

        if (TraceFlags.TIMING) {
            System.err.printf("%n%nGetting files from project system for %s...%n", getName());
        }
        if (TraceFlags.SUSPEND_PARSE_TIME != 0) {
            try {
                System.err.println("sleep for " + TraceFlags.SUSPEND_PARSE_TIME + "sec before getting files from project");
                Thread.sleep(TraceFlags.SUSPEND_PARSE_TIME * 1000);
                System.err.println("woke up after sleep");
            } catch (InterruptedException ex) {
                // do nothing
            }
        }
        long time = System.currentTimeMillis();
        final Set<NativeFileItem> removedFileItems = Collections.synchronizedSet(new HashSet<NativeFileItem>());
        final Set<NativeFileItem> readOnlyRemovedFilesSet = Collections.unmodifiableSet(removedFileItems);
        NativeProjectItemsListener projectItemListener = new NativeProjectItemsAdapter() {
            @Override
            public void filesRemoved(List<NativeFileItem> fileItems) {
                removedFileItems.addAll(fileItems);
            }

            @Override
            public void filesPropertiesChanged(List<NativeFileItem> fileItems) {
                for (NativeFileItem item : fileItems) {
                    if (item.isExcluded()) {
                        removedFileItems.add(item);
                    }
                }
            }

        };
        nativeProject.addProjectItemsListener(projectItemListener);
        List<NativeFileItem> sources = new ArrayList<>();
        List<NativeFileItem> headers = new ArrayList<>();
        for (NativeFileItem item : nativeProject.getAllFiles()) {
            if (!item.isExcluded()) {
                if (false) {
                    String file = System.getProperty("check.one.file.only"); // NOI18N
                    if (file != null && !file.contentEquals(item.getAbsolutePath())) {
                        continue;
                    }
                }
                switch (item.getLanguage()) {
                    case C:
                    case CPP:
                    case FORTRAN:
                        sources.add(item);
                        break;
                    case C_HEADER:
                        headers.add(item);
                        break;
                    default:
                        break;
                }
            } else {
                switch (item.getLanguage()) {
                    case C:
                    case CPP:
                    case FORTRAN:
                        removedFileItems.add(item);
                        break;
                    default:
                        break;
                }
            }
        }
        if (false/*part of AllFiles*/) sources.addAll(nativeProject.getStandardHeadersIndexers());
        if (TraceFlags.TIMING) {
            time = System.currentTimeMillis() - time;
            System.err.printf("Getting files from project system took  %d ms for %s%n", time, getName());
            System.err.printf("FILES COUNT for %s:%nSource files:\t%d%nHeader files:\t%d%nTotal files:\t%d%n",
                    getName(), sources.size(), headers.size(), sources.size() + headers.size());
            time = System.currentTimeMillis();
        }
        if (TraceFlags.SUSPEND_PARSE_TIME != 0) {
            try {
                System.err.println("sleep for " + TraceFlags.SUSPEND_PARSE_TIME + "sec after getting files from project");
                Thread.sleep(TraceFlags.SUSPEND_PARSE_TIME * 1000);
                System.err.println("woke up after sleep");
            } catch (InterruptedException ex) {
                // do nothing
            }
        }
        if (TraceFlags.DUMP_PROJECT_ON_OPEN) {
            ModelSupport.dumpNativeProject(nativeProject);
        }

        try {
            disposeLock.readLock().lock();

            if (TraceFlags.TIMING) {
                time = System.currentTimeMillis() - time;
                System.err.printf("Waited on disposeLock: %d ms for %s%n", time, getName());
                time = System.currentTimeMillis();
            }

            if (isDisposing()) {
                if (TraceFlags.TRACE_MODEL_STATE) {
                    System.err.printf("filling parser queue interrupted for %s%n", getName());
                }
                return;
            }

            boolean validator = false;
            if (status == Status.Validating) {
                validator = true;
            }
            if (status == Status.Validating && RepositoryUtils.getRepositoryErrorCount(this) > 0){
                System.err.println("Clean index for project \""+getUniqueName()+"\" because index was corrupted (was "+RepositoryUtils.getRepositoryErrorCount(this)+" errors)."); // NOI18N
                validator = false;
                reopenUnit();
            }

            getProjectRoots().fixFolder(nativeProject.getProjectRoot());
            for (String root : nativeProject.getSourceRoots()) {
                getProjectRoots().fixFolder(root);
            }
            getProjectRoots().addSources(sources);
            getProjectRoots().addSources(headers);
            getProjectRoots().addSources(removedFileItems);
            if (CndUtils.isDebugMode()) {
                checkConsistency(false);
            }
            CreateFilesWorker worker = new CreateFilesWorker(this, readOnlyRemovedFilesSet, validator);
            if (TraceFlags.SORT_PARSED_FILES) {
                Collections.sort(sources, NATIVE_FILE_ITEMS_COMPARATOR);
            }
            worker.createProjectFilesIfNeed(sources, true);
            if (status != Status.Validating  || RepositoryUtils.getRepositoryErrorCount(this) == 0){
                worker.createProjectFilesIfNeed(headers, false);
            }
            if (status == Status.Validating && RepositoryUtils.getRepositoryErrorCount(this) > 0){
                if (!TraceFlags.DEBUG_BROKEN_REPOSITORY) {
                    System.err.println("Clean index for project \""+getUniqueName()+"\" because index was corrupted (was "+RepositoryUtils.getRepositoryErrorCount(this)+" errors)."); // NOI18N
                }
                reopenUnit();
                // reset worker to create files without validator
                validator = false;
                worker = new CreateFilesWorker(this, readOnlyRemovedFilesSet, validator);
                worker.createProjectFilesIfNeed(sources, true);
                worker.createProjectFilesIfNeed(headers, false);
            }
            worker.checkLibraries();
            if (CndUtils.isDebugMode()) {
                checkConsistency(validator);
            }
            worker.finishProjectFilesCreation();
            if (CndUtils.isDebugMode()) {
                checkConsistency(false);
            }
        } finally {
            disposeLock.readLock().unlock();
            if (TraceFlags.TIMING) {
                time = System.currentTimeMillis() - time;
                System.err.printf("FILLING PARSER QUEUE took %d ms for %s%n", time, getName());
            }
        }
        nativeProject.removeProjectItemsListener(projectItemListener);
    // in fact if visitor used for parsing => visitor will parse all included files
    // recursively starting from current source file
    // so, when we visit headers, they should not be reparsed if already were parsed
    }

    private void reopenUnit() {
        setStatus(Status.Initial);
        ParserQueue.instance().clean(this);
        RepositoryUtils.closeUnit(this.getUnitId(), null, true);
        RepositoryUtils.openUnit(this);
        RepositoryUtils.hang(this);
        initFields();
    }

    private FileAndHandler preCreateIfNeed(NativeFileItem nativeFile){
        // file object can be invalid for not existing file (#194357)
        assert (nativeFile != null && nativeFile.getFileObject() != null);
        if (!Utils.acceptNativeItem(nativeFile)) {
            return null;
        }
        FileImpl.FileType fileType = Utils.getFileType(nativeFile);

        FileAndHandler fileAndHandler = createOrFindFileImpl(ModelSupport.createFileBuffer(nativeFile.getFileObject()), nativeFile, fileType);

        if (fileAndHandler.preprocHandler == null) {
            fileAndHandler.preprocHandler = createPreprocHandler(nativeFile);
        }
        return fileAndHandler;
    }

    /*package*/final FileImpl createIfNeed(NativeFileItem nativeFile,
            boolean validator, Collection<FileImpl> reparseOnEdit, Collection<NativeFileItem> reparseOnPropertyChanged) {

        FileAndHandler fileAndHandler = preCreateIfNeed(nativeFile);
        if (fileAndHandler == null) {
            return null;
        }
        FileImpl fileImpl = fileAndHandler.fileImpl;
        if (validator) {
            // fill up needed collections based on validation
            if (fileImpl.validate()) {
                if (fileImpl.isParsed()){
                    if (getGraph().getInLinksUids(fileImpl).isEmpty()) {
                        if (APTHandlersSupport.getCompilationUnitCRC(fileAndHandler.preprocHandler) != fileImpl.getLastParsedCompilationUnitCRC()) {
                            if (TraceFlags.TRACE_VALIDATION) {
                                System.err.printf("Validation: %s properties are changed %n", nativeFile.getAbsolutePath());
                            }
                            reparseOnPropertyChanged.add(nativeFile);
                        } else {
                            if (TraceFlags.TRACE_VALIDATION) {
                                System.err.printf("Validation: %s file is skipped as valid PARSED%n", nativeFile.getAbsolutePath());
                            }
                        }
                    } else {
                        if (TraceFlags.TRACE_VALIDATION) {
                            System.err.printf("Validation: %s file is skipped as non compilation unit PARSED%n", nativeFile.getAbsolutePath());
                        }
                    }
                } else {
                    if (TraceFlags.TRACE_VALIDATION) {
                        System.err.printf("Validation: %s file to be parsed, because of state %s%n", nativeFile.getAbsolutePath(), fileImpl.getState());
                    }
                    if (APTHandlersSupport.getCompilationUnitCRC(fileAndHandler.preprocHandler) != fileImpl.getLastParsedCompilationUnitCRC()) {
                        if (fileImpl.getState() == FileImpl.State.INITIAL){
                            fileAndHandler.preprocHandler = createPreprocHandler(nativeFile);
                            ParserQueue.instance().add(fileImpl, fileAndHandler.preprocHandler.getState(), ParserQueue.Position.TAIL);
                        } else {
                            if (TraceFlags.TRACE_VALIDATION) {
                                System.err.printf("Validation: %s properties are changed %n", nativeFile.getAbsolutePath());
                            }
                            reparseOnPropertyChanged.add(nativeFile);
                        }
                    } else {
                        ParserQueue.instance().add(fileImpl, fileAndHandler.preprocHandler.getState(), ParserQueue.Position.TAIL);
                    }
                }
            } else {
                if (TraceFlags.TRACE_VALIDATION) {
                    System.err.printf("Validation: file %s is changed%n", nativeFile.getAbsolutePath());
                }
                if (APTHandlersSupport.getCompilationUnitCRC(fileAndHandler.preprocHandler) != fileImpl.getLastParsedCompilationUnitCRC()) {
                    reparseOnPropertyChanged.add(nativeFile);
                } else {
                    reparseOnEdit.add(fileImpl);
                }
            }
        } else {
            // put directly into parser queue if needed
            boolean addToQueue = true;
            if (TraceFlags.PARSE_HEADERS_WITH_SOURCES) {
                addToQueue = fileImpl.isSourceFile();
            }
            if (addToQueue) {
                ParserQueue.instance().add(fileImpl, fileAndHandler.preprocHandler.getState(), ParserQueue.Position.TAIL);
            }
        }
        return fileImpl;
    }

    /**
     * Is called after project is added to model
     * and all listeners are notified
     */
    public final void onAddedToModel() {
        final boolean isRestored = status == Status.Restored;
        //System.err.printf("onAddedToModel isRestored=%b status=%s for %s (%s) \n", isRestored, status, name, getClass().getName());
        if (status == Status.Initial || status == Status.Restored) {
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    onAddedToModelImpl(isRestored);
                    synchronized (initializationTaskLock) {
                        initializationTask = null;
                    }
                }
            };
            String text = (status == Status.Initial) ? "Filling parser queue for " : "Validating files for ";	// NOI18N
            synchronized (initializationTaskLock) {
                initializationTask = ModelImpl.instance().enqueueModelTask(r, text + getName());
            }
        }
    }

    protected final Status getStatus() {
        return status;
    }

    boolean isValidating(){
        return status == Status.Validating;
    }

    private void onAddedToModelImpl(boolean isRestored) {
        if (TraceFlags.TRACE_182342_BUG) {
            new Exception("Restored: " + isRestored + " disposing: " + isDisposing()).printStackTrace(System.err); // NOI18N
        }
        if (isDisposing()) {
            return;
        }

        try {
            disposeLock.readLock().lock();
            if (isDisposing()) {
                return;
            }

            ensureFilesCreated();
            if (isDisposing()) {
                return;
            }

            // changed files are enqueued by edit start/edit end handlers
//            ensureChangedFilesEnqueued();
//            if (isDisposing()) {
//                return;
//            }
            Notificator.instance().flush();
        } finally {
            disposeLock.readLock().unlock();
        }

        if (isRestored) {
            FileImpl.incParseCount();
            ProgressSupport.instance().fireProjectLoaded(ProjectBase.this);
        }

        try {
            disposeLock.readLock().lock();
            if (isRestored && !isDisposing()) {
                // FIXUP for #109105 fix the reason instead!
                try {
                    // TODO: refactor this - remove waiting here!
                    // It was introduced in version 1.2.2.27.2.94.4.41
                    // when validation was introduced
                    waitParseImpl();
                    checkForRemoved();
                } catch (Exception e) {
                    DiagnosticExceptoins.register(e);
                }
            }
            if (isDisposing()) {
                return;
            }
            Notificator.instance().flush();
        } finally {
            disposeLock.readLock().unlock();
        }
    }

    /**
     * For the project that is restored from persistence,
     * is called when 1-st time parsed.
     * Checks whether there are files in code model, that are removed from the project system
     */
    public final void checkForRemoved() {
        CndUtils.assertTrueInConsole(ModelImpl.isModelRequestProcessorThread(), "should be called from model RP"); // NOI18N
        NativeProject nativeProject = (platformProject instanceof NativeProject) ? (NativeProject) platformProject : null;

        // we might just ask NativeProject to find file,
        // but it's too ineffective; so we have to create a set of project files paths
        Set<String> prjNotExcludedSourceFileItems = Collections.emptySet();
        Set<String> prjNotExcludedHeaderFileItems = Collections.emptySet();
        if (nativeProject != null) {
            prjNotExcludedSourceFileItems = new HashSet<>();
            prjNotExcludedHeaderFileItems = new HashSet<>();
            for (NativeFileItem item : nativeProject.getAllFiles()) {
                if (!item.isExcluded()) {
                    switch (item.getLanguage()) {
                        case C:
                        case CPP:
                        case FORTRAN:
                            prjNotExcludedSourceFileItems.add(item.getAbsolutePath());
                            //this would be a workaround for #116706 Code assistance do not recognize changes in file
                            //projectFiles.add(item.getFile().getCanonicalPath());
                            break;
                        case C_HEADER:
                            prjNotExcludedHeaderFileItems.add(item.getAbsolutePath());
                            break;
                        case OTHER:
                            break;
                        default: throw new AssertionError(item.getLanguage());
                    }
                }
            }
        }

        LinkedList<FileImpl> liveStartFiles = new LinkedList<>();
        Collection<FileImpl> prjFileImpls = getAllFileImpls();
        Map<FileImpl, Boolean> allFileImpls = new HashMap<>(prjFileImpls.size());
        boolean hasChanges = false;
        for (FileImpl file : prjFileImpls) {
            allFileImpls.put(file, null); // register file without any information
            FileObject fo = file.getFileObject();
            if (fo == null || !fo.isValid()) {
                // special marker for physically removed
                allFileImpls.put(file, Boolean.FALSE);
                hasChanges = true;
            } else if (nativeProject != null) {
                // all non-excluded project files are live
                if (prjNotExcludedSourceFileItems.contains(file.getAbsolutePath().toString())) {
                    liveStartFiles.addLast(file);
                    allFileImpls.put(file, Boolean.TRUE);
                } else if (prjNotExcludedHeaderFileItems.contains(file.getAbsolutePath().toString())) {
                    // TODO: for now we consider project to be clever
                    // so if header file is marked as not excluded it is really live
                    liveStartFiles.addLast(file);
                    allFileImpls.put(file, Boolean.TRUE);
                } else {
                    hasChanges = true;
                }
            } else {
                liveStartFiles.addLast(file);
                allFileImpls.put(file, Boolean.TRUE);
            }
        }

        if (!hasChanges) {
            if (CndUtils.isDebugMode() || CndUtils.isUnitTestMode()) {
                for (Map.Entry<FileImpl, Boolean> entry : allFileImpls.entrySet()) {
                    CndUtils.assertTrue(entry.getValue() == Boolean.TRUE);
                }
            }
            return;
        }
        // find live and dead files
        while (!liveStartFiles.isEmpty()) {
            // remove head
            FileImpl curLiveFile = liveStartFiles.removeFirst();
            // get directly included files
            for (CsmFile csmFile : getGraph().getOutLinks(curLiveFile)) {
                FileImpl includedFileImpl = (FileImpl) csmFile;
                if (includedFileImpl.getProjectUID().equals(this.getUID())) {
                    if (CndUtils.isDebugMode() || CndUtils.isUnitTestMode()) {
                        CndUtils.assertTrueInConsole(allFileImpls.containsKey(includedFileImpl),
                                "no record for: " + includedFileImpl, "\n\twhile checking out links for " + curLiveFile); // NOI18N
                    }
                    // check if file was already handled
                    Boolean result = allFileImpls.get(includedFileImpl);
                    if (result == null) {
                        // mark as live
                        allFileImpls.put(includedFileImpl, Boolean.TRUE);
                        // add new live file to the tail
                        liveStartFiles.addLast(includedFileImpl);
                    }
                }
            }
        }

        Set<FileImpl> removedPhysically = new HashSet<>();
        Set<FileImpl> removedOrAbsentInProject = new HashSet<>();
        for (Map.Entry<FileImpl, Boolean> entry : allFileImpls.entrySet()) {
            Boolean value = entry.getValue();
            FileImpl fileImpl = entry.getKey();
            if (value == null) {
                // check for absence of includes from dependent projects
                if (getGraph().getInLinksUids(fileImpl).isEmpty()) {
                    removedOrAbsentInProject.add(fileImpl);
                    if (TraceFlags.TRACE_VALIDATION) {
                        System.err.printf("Validation: removing (removed from project) %s%n", fileImpl.getAbsolutePath());//NOI18N
                    }
                }
            } else if (value == Boolean.FALSE) {
                removedPhysically.add(fileImpl);
                if (TraceFlags.TRACE_VALIDATION) {
                    System.err.printf("Validation: removing (physically deleted) %s%n", fileImpl.getAbsolutePath()); //NOI18N
                }
            }
        }
        if (!removedPhysically.isEmpty() || !removedOrAbsentInProject.isEmpty()) {
            onFileImplRemoved(removedPhysically, removedOrAbsentInProject);
        }
    }

    public final PreprocHandler createEmptyPreprocHandler(CharSequence absPath) {
        StartEntry startEntry = new StartEntry(getFileSystem(), FileContainer.getFileKey(absPath, true).toString(), getUIDKey());
        return APTHandlersSupport.createEmptyPreprocHandler(startEntry);
    }

    protected final PreprocHandler createPreprocHandler(NativeFileItem nativeFile) {
        assert (nativeFile != null);
        PPMacroMap macroMap = getMacroMap(nativeFile);
        PPIncludeHandler inclHandler = getIncludeHandler(nativeFile);
        NativeFileItem.LanguageFlavor languageFlavor = nativeFile.getLanguageFlavor();
        Language language = nativeFile.getLanguage();
        if (languageFlavor == NativeFileItem.LanguageFlavor.UNKNOWN) {
            languageFlavor = NativeProjectSupport.getDefaultLanguageFlavor(language);
        }
        return APTHandlersSupport.createPreprocHandler(macroMap, inclHandler, isSourceFile(nativeFile), language.toString(), languageFlavor.toString());
    }

    private PPIncludeHandler getIncludeHandler(NativeFileItem nativeFile) {
        if (!isSourceFile(nativeFile)) {
            nativeFile = DefaultFileItem.toDefault(nativeFile);
        }
        List<IncludePath> origUserIncludePaths = nativeFile.getUserIncludePaths();
        if (TraceFlags.DUMP_NATIVE_FILE_ITEM_USER_INCLUDE_PATHS) {
            System.err.println("Item "+nativeFile.getAbsolutePath());
            for(IncludePath path : origUserIncludePaths) {
                System.err.println("\tPath "+path.getFSPath().getPath() + (path.isFramework() ? " (framework directory)" : ""));
            }
        }
        List<IncludePath> origSysIncludePaths = nativeFile.getSystemIncludePaths();
        List<IncludeDirEntry> userIncludePaths = userPathStorage.get(origUserIncludePaths.toString(), origUserIncludePaths);
        List<IncludeDirEntry> sysIncludePaths = sysAPTData.getIncludes(origSysIncludePaths.toString(), origSysIncludePaths);
        List<FSPath> includeFileEntries = new ArrayList<>();
        for (FSPath systemIncludeHeader : nativeFile.getSystemIncludeHeaders()) {
            includeFileEntries.add(systemIncludeHeader);
        }
        for (FSPath includeFile : nativeFile.getIncludeFiles()) {
            includeFileEntries.add(includeFile);
        }
        String entryKey = FileContainer.getFileKey(nativeFile.getAbsolutePath(), true).toString();
        if (CndUtils.isDebugMode()) {
            FileSystem curPrjFS = getFileSystem();
            FileSystem nativeProjectFS = nativeFile.getNativeProject().getFileSystem();
            CndUtils.assertTrue(nativeProjectFS.equals(curPrjFS), "File systems differ: incoming=" + nativeProjectFS + ";cur=" + curPrjFS + //NOI18N
                    "; project=" + this +  "; file=" + nativeFile); //NOI18N
        }
        StartEntry startEntry = new StartEntry(getFileSystem(), entryKey, getUIDKey());
        APTFileSearch searcher = null;
        UnitDescriptor unitDescriptor = getUnitDescriptor();
        if (unitDescriptor != null) {
            searcher = APTFileSearch.get(KeyUtilities.createProjectKey(unitDescriptor));
        }
        return APTHandlersSupport.createIncludeHandler(startEntry, sysIncludePaths, userIncludePaths, includeFileEntries, searcher);
    }

    private PPMacroMap getMacroMap(NativeFileItem nativeFile) {
        if (!isSourceFile(nativeFile)) {
            nativeFile = DefaultFileItem.toDefault(nativeFile);
        }
        List<String> userMacros = nativeFile.getUserMacroDefinitions();
        List<String> sysMacros = nativeFile.getSystemMacroDefinitions();
        PPMacroMap map = APTHandlersSupport.createMacroMap(getSysMacroMap(sysMacros), userMacros);
        return map;
    }

    protected final boolean isSourceFile(NativeFileItem nativeFile) {
        FileImpl.FileType type = Utils.getFileType(nativeFile);
        return FileImpl.isSourceFileType(type);
    }

    private PPMacroMap getSysMacroMap(List<String> sysMacros) {
        //TODO: it's faster to use sysAPTData.getMacroMap(configID, sysMacros);
        // but we need this ID to get somehow... how?
        PPMacroMap map = sysAPTData.getMacroMap(sysMacros.toString(), sysMacros);
        return map;
    }

    /*package*/ final PreprocHandler getPreprocHandler(CharSequence absPath, PreprocessorStatePair statePair) {
        assert statePair != null;
        return createPreprocHandlerFromState(absPath, statePair.state);
    }

    public final PreprocHandler createPreprocHandlerFromState(CharSequence absPath, PreprocHandler.State state) {
        Collection<PreprocHandler> out = createPreprocHandlerFromStates(Collections.singleton(state), absPath, Interrupter.DUMMY);
        return out.iterator().next();
    }

    public final PreprocHandler.State getFirstValidPreprocState(CharSequence fileKey) {
        FileContainer.FileEntry entry = getFileContainer().getEntry(fileKey);
        if (entry == null) {
            return null;
        }
        Object lock = entry.getLock();
        Collection<ProjectBase> dependentProjects = getDependentProjects();
        synchronized (lock) {
            for (PreprocessorStatePair pair : entry.getStatePairs()) {
                StartEntry startEntry = APTHandlersSupport.extractStartEntry(pair.state);
                if (!ModelImpl.isClosedProject(startEntry.getStartFileProject())) {
                    return pair.state;
                }
            }
            Collection<FileEntry> includedFileEntries = getIncludedFileEntries(lock, fileKey, dependentProjects);
            for (FileEntry fileEntry : includedFileEntries) {
                // return the first with non empty states collection
                for (State state : fileEntry.getPrerocStates()) {
                    return state;
                }
            }
        }
        return null;
    }

    /*package-local*/ final Collection<PreprocessorStatePair> getPreprocessorStatePairs(FileImpl fileImpl) {
        CharSequence fileKey = FileContainer.getFileKey(fileImpl.getAbsolutePath(), false);
        FileContainer.FileEntry entry = getFileContainer().getEntry(fileKey);
        if (entry == null) {
            return Collections.emptyList();
        }
        Collection<ProjectBase> dependentProjects = getDependentProjects();
        Object lock = entry.getLock();
        Collection<PreprocessorStatePair> out;
        synchronized (lock) {
            Collection<PreprocessorStatePair> containerStatePairs = entry.getStatePairs();
            if (hasClosedStartEntry(lock, containerStatePairs)) {
                FileSystem fs = fileImpl.getFileSystem();
                // need to merge from dependent projects' storages
                Collection<FileEntry> includedFileEntries = getIncludedFileEntries(lock, fileKey, dependentProjects);
                FileEntry mergeEntry = FileContainer.createFileEntryForMerge(fs, fileKey);
                for (FileEntry fileEntry : includedFileEntries) {
                    for (PreprocessorStatePair pair : fileEntry.getStatePairs()) {
                        if (pair.pcState != FilePreprocessorConditionState.PARSING) {
                            updateFileEntryBasedOnIncludedStatePair(mergeEntry, pair, fileKey, fileImpl, null, null, new AtomicBoolean());
                        }
                    }
                }
                out = mergeEntry.getStatePairs();
            } else {
                out = containerStatePairs;
            }
        }
        return out;
    }

    public final Collection<PreprocHandler> getPreprocHandlersForParse(FileImpl fileImpl, Interrupter interrupter) {
        CharSequence fileKey = FileContainer.getFileKey(fileImpl.getAbsolutePath(), false);
        FileContainer.FileEntry entry = getFileContainer().getEntry(fileKey);
        if (entry == null) {
            return Collections.emptyList();
        }

        Collection<ProjectBase> dependentProjects = getDependentProjects();
        Collection<PreprocHandler.State> states = new HashSet<>();
        Object lock = entry.getLock();
        synchronized (lock) {
            for (PreprocessorStatePair pair : entry.getStatePairs()) {
                StartEntry startEntry = APTHandlersSupport.extractStartEntry(pair.state);
                if (!ModelImpl.isClosedProject(startEntry.getStartFileProject())) {
                    states.add(pair.state);
                }
            }
            // need to get all included knowledge
            Collection<FileEntry> includedFileEntries = getIncludedFileEntries(lock, fileKey, dependentProjects);
            for (FileEntry fileEntry : includedFileEntries) {
                for (PreprocessorStatePair pair : fileEntry.getStatePairs()) {
                    states.add(pair.state);
                }
            }
        }
        Collection<PreprocHandler> result = createPreprocHandlerFromStates(states, fileKey, interrupter);
        return result;
    }

    private boolean hasClosedStartEntry(Object lock, Collection<PreprocessorStatePair> containerStatePairs) {
        assert Thread.holdsLock(lock) : " must hold lock ";
        for (PreprocessorStatePair pair : containerStatePairs) {
            StartEntry startEntry = APTHandlersSupport.extractStartEntry(pair.state);
            if (ModelImpl.isClosedProject(startEntry.getStartFileProject())) {
                return true;
            }
        }
        return false;
    }

    /*package-local*/ final Collection<PreprocessorStatePair> getFileContainerStatePairsToDump(CharSequence absPath) {
        FileContainer.FileEntry entry = getFileContainer().getEntry(absPath);
        if (entry == null) {
            return Collections.emptyList();
        }
        synchronized (entry.getLock()) {
            return entry.getStatePairs();
        }
    }

    public final Collection<PreprocHandler> getFileContainerPreprocHandlersToDump(CharSequence absPath) {
        FileContainer.FileEntry entry = getFileContainer().getEntry(absPath);
        if (entry == null) {
            return Collections.emptyList();
        }

        Collection<PreprocHandler.State> states;
        synchronized (entry.getLock()) {
            states = entry.getPrerocStates();
        }
        return createPreprocHandlerFromStates(states, absPath, Interrupter.DUMMY);
    }

    private Collection<PreprocHandler> createPreprocHandlerFromStates(Collection<State> states, CharSequence absPath, Interrupter interrupter) {
        Collection<PreprocHandler> result = new ArrayList<>(states.size());
        for (PreprocHandler.State state : states) {
            PreprocHandler preprocHandler = createEmptyPreprocHandler(absPath);
            if (state != null) {
                if (state.isCleaned()) {
                    preprocHandler = restorePreprocHandler(absPath, preprocHandler, state, interrupter);
                } else {
                    if (TRACE_PP_STATE_OUT) {
                        System.err.println("copying state for " + absPath);
                    }
                    preprocHandler.setState(state);
                }
            }
            if (TRACE_PP_STATE_OUT) {
                System.err.printf("null state for %s, returning default one", absPath);
            }
            result.add(preprocHandler);
        }
        return result;
    }

    /**
     * This method for testing purpose only. Used from TraceModel
     */
    public final CsmFile testAPTParseFile(NativeFileItem item) {
        PreprocHandler preprocHandler = createPreprocHandler(item);
        return findFile(item.getAbsolutePath(), false, Utils.getFileType(item), preprocHandler, true, preprocHandler.getState(), item);
    }

    protected final PreprocHandler.State setChangedFileState(NativeFileItem nativeFile) {
        // TODO: do we need to change states in dependent projects' storages???
        FileContainer fileContainer = getFileContainer();
        FileContainer.FileEntry entry = fileContainer.getEntry(nativeFile.getAbsolutePath());
        if (entry == null) {
            return null;
        } else {
            PreprocHandler.State state = createPreprocHandler(nativeFile).getState();
            synchronized (entry.getLock()) {
                entry.invalidateStates();
                entry.setState(state, FilePreprocessorConditionState.PARSING);
            }
            fileContainer.put();
            return state;
        }
    }

    protected final void invalidatePreprocState(CharSequence absPath) {
        FileContainer fileContainer = getFileContainer();
        Object stateLock = fileContainer.getLock(absPath);
        Collection<ProjectBase> dependentProjects = getDependentProjects();
        synchronized (stateLock) {
            fileContainer.invalidatePreprocState(absPath);
            this.invalidateIncludedPreprocState(stateLock, this, absPath);
            for (ProjectBase projectBase : dependentProjects) {
                projectBase.invalidateIncludedPreprocState(stateLock, this, absPath);
            }
        }
        putContainers(dependentProjects, fileContainer);
    }

    private void putContainers(Collection<ProjectBase> dependentProjects, FileContainer fileContainer) {
        includedFileContainer.putStorage(this);
        for (ProjectBase prj : dependentProjects) {
            prj.includedFileContainer.putStorage(this);
        }
        fileContainer.put();
    }

    protected final void markAsParsingPreprocStates(FileImpl fileImpl) {
        CharSequence absPath = fileImpl.getAbsolutePath();
        // TODO: do we need to change states in dependent projects' storages???
        FileContainer fileContainer = getFileContainer();
        FileEntry fileEntry = fileContainer.getEntry(absPath);
        if (fileEntry == null) {
            CndUtils.assertTrueInConsole(!fileImpl.isValid(), "null entry for valid file ", fileImpl); //NOI18N
            return;
        }
        Object stateLock = fileEntry.getLock();
        Collection<ProjectBase> dependentProjects = getDependentProjects();
        synchronized (stateLock) {
            fileEntry.markAsParsingPreprocStates();
            Collection<FileEntry> entries = this.getIncludedFileEntries(stateLock, absPath, dependentProjects);
            for (FileEntry includedFileEntry : entries) {
                includedFileEntry.markAsParsingPreprocStates();
            }
        }
//        fileContainer.put();
    }
    /**
     * The method is for tracing/testing/debugging purposes only
     */
    public final void debugInvalidateFiles() {
        // TODO: do we need to change states in dependent projects' storages???
        Collection<FileImpl> allFileImpls = getAllFileImpls();
        for (FileImpl fileImpl : allFileImpls) {
            fileImpl.debugInvalidate();
        }
        getFileContainer().debugClearState();
        this.includedFileContainer.debugClearState();
        for (Iterator<CsmProject> it = getLibraries().iterator(); it.hasNext();) {
            ProjectBase lib = (ProjectBase) it.next();
            lib.debugInvalidateFiles();
        }
    }

    public final FileImpl prepareIncludedFile(ProjectBase startProject, CharSequence file, PreprocHandler preprocHandler) {
        assert preprocHandler != null : "null preprocHandler for " + file;
        if (isDisposing() || startProject.isDisposing()) {
            return null;
        }
        if (!CsmModelAccessor.isModelAlive()) {
            if (TraceFlags.TRACE_VALIDATION || TraceFlags.TRACE_MODEL_STATE) {
                System.err.printf("prepareIncludedFile: %s file [%s] is interrupted on closing model%n", file, this.getName());
            }
            return null;
        }
        FileImpl csmFile = findFile(file, true, FileImpl.FileType.HEADER_FILE, preprocHandler, false, null, null);
        return csmFile;
    }

    public final boolean checkIfFileWasIncludedBeforeWithBetterOrEqualContent(FileImpl preIncludedFile, PreprocHandler ppPreIncludeHandler) {
        // this method is called after prepareIncludedFile
        // do the best to reduce work to be done on include of passed file

        // for now we check if there is already fully included pcState
        // but also controlling macros can be checked if they were collected before
        
        CharSequence path = preIncludedFile.getAbsolutePath();
        FileContainer.FileEntry entry = getFileContainer().getEntry(path);
        if (entry == null) {
            // suspicious to have null after previously successful prepareIncludedFile
            // but might be when cancelling/interrupting
            entryNotFoundMessage(path);
            // can skip such file inclusion
            return true;
        }
        synchronized (entry.getLock()) {
            for (PreprocessorStatePair keptPair : entry.getStatePairs()) {
                if (keptPair.pcState.isAllIncluded() && keptPair.state.isCompileContext()) {
                    // can not contribute any new content, because all was already included before
                    return true;
                }
            }
        }
        return false;
    }
    
    private static final boolean TRACE_FILE = (TraceFlags.TRACE_FILE_NAME != null);
    public void postIncludeFile(ProjectBase startProject, FileImpl csmFile, CharSequence file, PreprocessorStatePair newStatePair, APTFileCacheEntry aptCacheEntry) {
        boolean thisProjectUpdateResult = false;
        boolean startProjectUpdateResult = false;
        try {
            if (isDisposing() || startProject.isDisposing()) {
                return;
            }
            FileContainer.FileEntry entry = getFileContainer().getEntry(csmFile.getAbsolutePath());
            if (entry == null) {
                entryNotFoundMessage(file);
                return;
            }
            synchronized (entry.getLock()) {
//                    Map<CsmUID<CsmProject>, Collection<PreprocessorStatePair>> includedStatesToDebug = startProject.getIncludedPreprocStatePairs(csmFile);
//                    Collection<PreprocessorStatePair> statePairsToDebug = entry.getStatePairs();
                // register included file and it's states in start project under current included file lock
                AtomicBoolean newStateFoundInStartProject = new AtomicBoolean();
                List<PreprocHandler.State> statesToParse = new ArrayList<>(4);
                startProjectUpdateResult = startProject.updateFileEntryForIncludedFile(entry, this, file, csmFile, newStatePair, newStateFoundInStartProject);

                // decide if parse is needed
                statesToParse.add(newStatePair.state);
                AtomicBoolean clean = new AtomicBoolean(false);
                AtomicBoolean newStateFoundInFileContainer = new AtomicBoolean();
                thisProjectUpdateResult = updateFileEntryBasedOnIncludedStatePair(entry, newStatePair, file, csmFile, clean, statesToParse, newStateFoundInFileContainer);
                if (thisProjectUpdateResult) {
                    // start project can be this project or another project, but
                    // we found the "best from the bests" for the current lib;
                    // it have to be considered as the best in start project lib storage as well
                    if (!startProjectUpdateResult) {
                        // except the situation when "best from the bests" collection
                        // contains PARSING-marked entry which is not comparable with newStatePair,
                        // so newStatePair was accepted as candidate as well, but in this case we
                        // expect that startProject have seen newStatePair
                        if (!newStateFoundInStartProject.get()) {
                            //CndUtils.assertTrueInConsole(false, " this project " + this + " thinks that new state for " + file + " is the best but start project does not take it " + startProject);
                        }
                    }
                }
                if (thisProjectUpdateResult) {
                    // TODO: think over, what if we aready changed entry,
                    // but now deny parsing, because base, but not this project, is disposing?!
                    if (!isDisposing() && !startProject.isDisposing()) {
                        assert !(APTTraceFlags.USE_CLANK && (aptCacheEntry != null)) : "aptCacheEntry must not be used in Clank Mode";
                        if (aptCacheEntry != null) {
                          csmFile.setAPTCacheEntry(newStatePair.state, aptCacheEntry, clean.get());
                        }
                        if (!TraceFlags.PARSE_HEADERS_WITH_SOURCES) {
                            // NOTE: we need to add to Parser Queue from sync block after our calculation has proven
                            // the need to be put in queue. Otherwise if we leave block and hold, 
                            // so that another thread after it's calculation decides to add it's the best states and do it before we resume,
                            // we can put already rejected states
                            if (APTTraceFlags.USE_CLANK) {
                                // PERF: prepare caches out of big Parser Queue sync block
                                for (int i = 0; i < statesToParse.size(); i++) {
                                    PreprocHandler.State ppState = statesToParse.get(i);
                                    PreprocHandler.State cacheReady = APTHandlersSupport.preparePreprocStateCachesIfPossible(ppState);
                                    statesToParse.set(i, cacheReady);
                                }
                            }
                            ParserQueue.instance().add(csmFile, statesToParse, ParserQueue.Position.HEAD, clean.get(),
                                    clean.get() ? ParserQueue.FileAction.MARK_REPARSE : ParserQueue.FileAction.MARK_MORE_PARSE);
                        }
                    }
                }
            }
        } finally {
            if (thisProjectUpdateResult) {
                getFileContainer().put();
            }
            if (startProjectUpdateResult) {
                startProject.putIncludedFileStorage(this);
            }
        }
    }
    /**
     * Called after project is restored from repository.
     * Checks if dependent libraries has enough content needed by this project.
     * Returns start files that contribute extra library model needed for this project.
     * @return
     */
    /*package*/Set<FileImpl> checkLibrariesAfterRestore() {
        Set<CharSequence> filesToReparseLibs = new HashSet<>(1024);
        List<CsmProject> libraries = getLibraries();
        for (CsmProject lib : libraries) {
            ProjectBase libProject = (ProjectBase) lib;
            Storage libStorage = getIncludedLibraryStorage(libProject);
            if (libStorage == null) {
                Utils.LOG.log(Level.INFO, "Can not find storage for dependent library {0}\n\tinside project {1}", new Object[]{libProject, this}); //NOI18N
            } else {
                Map<CharSequence, FileEntry> internalMap = libStorage.getInternalMap();
                for (Map.Entry<CharSequence, FileEntry> entry : internalMap.entrySet()) {
                    CharSequence fileName = entry.getKey();
                    FileEntry entryFromLibrary = libProject.getFileContainer().getEntry(fileName);
                    List<PreprocessorStatePair> libCurrentPairs;
                    if (entryFromLibrary != null) {
                        // library already knows about included file
                        synchronized (entryFromLibrary.getLock()) {
                            // check all included states to see if they would contribute to the lib's file model
                            libCurrentPairs = new ArrayList<>(entryFromLibrary.getStatePairs());
                        }
                    } else {
                        // library doesn't know about file yet
                        // assign empty states to be the weakest during comparison
                        libCurrentPairs = Collections.emptyList();
                    }
                    FileEntry includedEntry = entry.getValue();
                    for (PreprocessorStatePair pair : includedEntry.getStatePairs()) {
                        boolean addToReparse = false;
                        if (!pair.state.isValid() || pair.pcState == FilePreprocessorConditionState.PARSING) {
                            addToReparse = true;
                        }
                        if (!addToReparse) {
                            ComparisonResult comResult = fillStatesToKeepBasedOnPCState(pair.pcState, libCurrentPairs, new ArrayList<PreprocessorStatePair>());
                            addToReparse = (comResult != ComparisonResult.DISCARD);
                        }
                        if (addToReparse) {
                            StartEntry se = APTHandlersSupport.extractStartEntry(pair.state);
                            filesToReparseLibs.add(se.getStartFile());
                            includedEntry.invalidateStates();
                        }
                    }
                }
            }
        }
        Set<FileImpl> res = new HashSet<>(filesToReparseLibs.size());
        for (CharSequence path : filesToReparseLibs) {
            FileImpl file = getFile(path, true);
            CndUtils.assertTrueInConsole(file != null, "no fileImpl for ", path);
            if (file != null) {
                res.add(file);
            }
        }
        return res;
    }

    private boolean updateFileEntryForIncludedFile(FileEntry entryToLockOn, ProjectBase includedProject, CharSequence includedFileKey, FileImpl includedFile, PreprocessorStatePair newStatePair, AtomicBoolean newStateFound) {
        boolean startProjectUpdateResult;
        FileContainer.FileEntry includedFileEntryFromStartProject = includedFileContainer.getOrCreateEntryForIncludedFile(entryToLockOn, includedProject, includedFile);
        if (includedFileEntryFromStartProject != null) {
            startProjectUpdateResult = updateFileEntryBasedOnIncludedStatePair(includedFileEntryFromStartProject, newStatePair, includedFileKey, includedFile, null, null, newStateFound);
        } else {
            newStateFound.set(false);
            startProjectUpdateResult = false;
        }
        return startProjectUpdateResult;
    }

    private final IncludedFileContainer includedFileContainer;

    private void putIncludedFileStorage(ProjectBase includedProject) {
        boolean putStorage = includedFileContainer.putStorage(includedProject);
        assert putStorage : "no storage for " + this + " and included " + includedProject;
    }

    void invalidateLibraryStorage(CsmUID<CsmProject> libraryUID) {
        includedFileContainer.invalidateIncludeStorage(libraryUID);
    }

    IncludedFileContainer.Storage getIncludedLibraryStorage(ProjectBase includedProject) {
        return includedFileContainer.getStorageForProject(includedProject);
    }

    void prepareIncludeStorage(ProjectBase includedProject) {
        includedFileContainer.prepareIncludeStorage(includedProject);
    }

    Map<CsmUID<CsmProject> , Collection<PreprocessorStatePair>> getIncludedPreprocStatePairs(FileImpl fileToSearch) {
        return includedFileContainer.getPairsToDump(fileToSearch);
    }

    private void invalidateIncludedPreprocState(Object lock, ProjectBase includedFileOwner, CharSequence absPath) {
        includedFileContainer.invalidate(lock, includedFileOwner, absPath);
    }

    public Collection<State> getIncludedPreprocStates(FileImpl impl) {
        Collection<ProjectBase> dependentProjects = getDependentProjects();
        CharSequence fileKey = FileContainer.getFileKey(impl.getAbsolutePath(), false);
        Object stateLock = getFileContainer().getLock(fileKey);
        Collection<State> states = new ArrayList<>(dependentProjects.size() + 1);
        synchronized (stateLock) {
            Collection<FileEntry> entries = this.getIncludedFileEntries(stateLock, fileKey, dependentProjects);
            for (FileEntry fileEntry : entries) {
                states.addAll(fileEntry.getPrerocStates());
            }
        }
        return states;
    }

    private Collection<FileEntry> getIncludedFileEntries(Object stateLock, CharSequence fileKey, Collection<ProjectBase> dependentProjects) {
        assert Thread.holdsLock(stateLock) : " must hold state lock for " + fileKey;
        Collection<FileEntry> out = new ArrayList<>(dependentProjects.size() + 1);
        FileEntry ownEntry = this.includedFileContainer.getIncludedFileEntry(stateLock, this, fileKey);
        if (ownEntry != null) {
            out.add(ownEntry);
        }
        for (ProjectBase dep : dependentProjects) {
            FileEntry depPrjEntry = dep.includedFileContainer.getIncludedFileEntry(stateLock, this, fileKey);
            if (depPrjEntry != null) {
                out.add(depPrjEntry);
            }
        }
        return out;
    }

    private boolean updateFileEntryBasedOnIncludedStatePair(
            FileContainer.FileEntry entry, PreprocessorStatePair newStatePair, 
            CharSequence file, FileImpl csmFile, 
            AtomicBoolean cleanOut, List<PreprocHandler.State> statesToParse, 
            AtomicBoolean newStateFound) {
        newStateFound.set(false);
        String prefix = statesToParse == null ? "lib update:" : "parsing:"; // NOI18N
        PreprocHandler.State newState = newStatePair.state;
        FilePreprocessorConditionState pcState = newStatePair.pcState;
        List<PreprocessorStatePair> statesToKeep = new ArrayList<>(4);
        Collection<PreprocessorStatePair> entryStatePairs = entry.getStatePairs();
        // Phase 1: check preproc states of entry comparing to current state
        ComparisonResult comparisonResult = fillStatesToKeepBasedOnPPState(newState, entryStatePairs, statesToKeep, newStateFound);
        if (TRACE_FILE && FileImpl.traceFile(file)) {
            traceIncludeStates(prefix+"comparison 2 " + comparisonResult, csmFile, newState, pcState, newStateFound.get(), null, statesToKeep); // NOI18N
        }
        if (comparisonResult == ComparisonResult.DISCARD) {
            if (TRACE_FILE && FileImpl.traceFile(file)) {
                traceIncludeStates(prefix+"worse 2", csmFile, newState, pcState, false, null, statesToKeep); // NOI18N
            }
            return false;
        } else if (comparisonResult == ComparisonResult.KEEP_WITH_OTHERS) {
            if (newStateFound.get()) {
                // we are already in the list and not better than all, can stop
                if (TRACE_FILE && FileImpl.traceFile(file)) {
                    traceIncludeStates(prefix+"state is already here ", csmFile, newState, pcState, false, null, statesToKeep); // NOI18N
                }
                return false;
            }
        }
        // from that point we are NOT interested in what is in the entry:
        // it's locked; "good" states are are in statesToKeep, "bad" states don't matter

        assert comparisonResult != ComparisonResult.DISCARD;

        boolean clean;

        if (comparisonResult == ComparisonResult.REPLACE_OTHERS) {
            clean = true;
            CndUtils.assertTrueInConsole(statesToKeep.isEmpty(), "states to keep must be empty 2"); // NOI18N
            if (TRACE_FILE && FileImpl.traceFile(file)) {
                traceIncludeStates(prefix+"best state", csmFile, newState, pcState, clean, statesToParse, statesToKeep); // NOI18N
            }
        } else {  // comparisonResult == SAME
            clean = false;
            // Phase 2: check preproc conditional states of entry comparing to current conditional state
            comparisonResult = fillStatesToKeepBasedOnPCState(pcState, new ArrayList<>(statesToKeep), statesToKeep);
            if (TRACE_FILE && FileImpl.traceFile(file)) {
                traceIncludeStates(prefix+"pc state comparison " + comparisonResult, csmFile, newState, pcState, clean, statesToParse, statesToKeep); // NOI18N
            }
            switch (comparisonResult) {
                case REPLACE_OTHERS:
                    CndUtils.assertTrueInConsole(statesToKeep.isEmpty(), "states to keep must be empty 3"); // NOI18N
                    clean = true;
                    break;
                case KEEP_WITH_OTHERS:
                    break;
                case DISCARD:
                    return false;
                default:
                    assert false : prefix+"unexpected comparison result: " + comparisonResult; //NOI18N
                    return false;
            }
        }
        if (statesToParse != null && clean) {
            for (PreprocessorStatePair pair : statesToKeep) {
                // if pair has parsing in pair.pcState => it was not valid source file
                // skip it
                if (pair.pcState != FilePreprocessorConditionState.PARSING) {
                    statesToParse.add(pair.state);
                }
            }
        }
        entry.setStates(statesToKeep, newStatePair);
        if (statesToParse != null) {
            if (TRACE_FILE && FileImpl.traceFile(file)
                    && (TraceFlags.TRACE_PC_STATE || TraceFlags.TRACE_PC_STATE_COMPARISION)) {
                traceIncludeStates(prefix+"scheduling", csmFile, newState, pcState, clean, // NOI18N
                        statesToParse, statesToKeep);
            }
        }
        if (cleanOut != null) {
            cleanOut.set(clean);
        }
        return true;
    }

    private void entryNotFoundMessage(CharSequence file) {
        if (Utils.LOG.isLoggable(Level.INFO)) {
            // since file container can return empty container the entry can be null.
            StringBuilder buf = new StringBuilder("File container does not have file "); //NOI18N
            buf.append("[").append(file).append("]"); //NOI18N
            if (getFileContainer() == FileContainer.empty()) {
                buf.append(" because file container is EMPTY."); //NOI18N
            } else {
                buf.append("."); //NOI18N
            }
            if (isDisposing()) {
                buf.append("\n\tIt is very strange but project is disposing."); //NOI18N
            }
            if (!isValid()) {
                buf.append("\n\tIt is very strange but project is invalid."); //NOI18N
            }
            Status st = getStatus();
            if (st != null) {
                buf.append("\n\tProject ").append(toString()).append(" has status ").append(st).append("."); //NOI18N
            }
            Utils.LOG.info(buf.toString());
        }
    }

    private static void traceIncludeStates(CharSequence title,
            FileImpl file, PreprocHandler.State newState, FilePreprocessorConditionState pcState,
            boolean clean, Collection<PreprocHandler.State> statesToParse, Collection<PreprocessorStatePair> statesToKeep) {

        StringBuilder sb = new StringBuilder();
        for (PreprocessorStatePair pair : statesToKeep) {
            if (sb.length() > 0) {
                sb.append(", "); //NOI18N
            }
            sb.append(pair.pcState);
        }


        PreprocHandler preprocHandler = file.getProjectImpl(true).createEmptyPreprocHandler(file.getAbsolutePath());
        preprocHandler.setState(newState);

        System.err.printf("%s %s (1) %s%n\tfrom %s %n\t%s %s %n\t%s keeping [%s]%n", title, //NOI18N
                (clean ? "reparse" : "  parse"), file.getAbsolutePath(), //NOI18N
                APTHandlersSupport.extractStartEntry(newState).getStartFile(),
                TraceUtils.getPreprocStateString(preprocHandler.getState()),
                TraceUtils.getMacroString(preprocHandler, TraceFlags.logMacros),
                pcState, sb);

        if (statesToParse != null) {
            for (PreprocHandler.State state : statesToParse) {
                if (!newState.equals(state)) {
                    FilePreprocessorConditionState currPcState = null;
                    for (PreprocessorStatePair pair : statesToKeep) {
                        if (newState.equals(pair.state)) {
                            currPcState = pair.pcState;
                            break;
                        }
                    }
                    System.err.printf("%s %s (2) %s %n\tfrom %s%n\t valid %b context %b %s%n", title,//NOI18N
                            "  parse", file.getAbsolutePath(), //NOI18N
                            APTHandlersSupport.extractStartEntry(state).getStartFile(),
                            state.isValid(), state.isCompileContext(), currPcState);
                }
            }
        }
    }

    void setParsedPCState(FileImpl csmFile, State ppOrigState, State ppUsedState, FilePreprocessorConditionState pcState) {
        CharSequence fileKey = csmFile.getAbsolutePath();
        FileContainer.FileEntry entry = getFileContainer().getEntry(fileKey);
        if (entry == null) {
            entryNotFoundMessage(fileKey);
            return;
        }
        boolean updateFileContainer;
        Object lock = entry.getLock();
        StartEntry startEntry = APTHandlersSupport.extractStartEntry(ppOrigState);
        boolean updateStartProjectStorage = false;
        ProjectBase startProject = Utils.getStartProject(startEntry);
        // IZ#179861: unstable test RepositoryValidation
        synchronized (lock) {
            // update FileContainer entry if possible
            updateFileContainer = updateFileEntryBasedOnParsedState(entry, fileKey, ppOrigState, ppUsedState, pcState);
            // update include storage of start project
            FileEntry includedEntry = startProject.includedFileContainer.getIncludedFileEntry(lock, this, fileKey);
            if (includedEntry != null) {
                updateStartProjectStorage = updateFileEntryBasedOnParsedState(includedEntry, fileKey, ppOrigState, ppUsedState, pcState);
            }
        }
        if (updateFileContainer) {
            FileContainer fileContainer = getFileContainer();
            fileContainer.put();
        }
        if (updateStartProjectStorage) {
            startProject.includedFileContainer.putStorage(this);
        }
    }

    private static boolean updateFileEntryBasedOnParsedState(FileEntry entry, CharSequence fileKey, State ppOrigState, State ppUsedState, FilePreprocessorConditionState pcState) {
        List<PreprocessorStatePair> statesToKeep = new ArrayList<>(4);
        Collection<PreprocessorStatePair> entryStatePairs = entry.getStatePairs();
        if (TraceFlags.TRACE_182342_BUG) {
            System.err.printf("setParsedPCState: original states for file: %s %n with new state: %s%n and pcState: %s%n", fileKey, ppUsedState, pcState);
            if (entryStatePairs.isEmpty()) {
                System.err.println("NO ORIGINAL STATES");
            } else {
                int i = 0;
                for (PreprocessorStatePair preprocessorStatePair : entryStatePairs) {
                    System.err.printf("setParsedPCState: State %d from original %s%n", i++, preprocessorStatePair);
                }
            }
        }
        List<PreprocessorStatePair> copy = new ArrayList<>();
        boolean entryFound = false;
        // put into copy array all except ourself
        for (PreprocessorStatePair pair : entryStatePairs) {
            assert pair != null : "can not be null element in " + entryStatePairs;
            assert pair.state != null : "state can not be null in pair " + pair + " for file " + fileKey;
            if ((pair.pcState == FilePreprocessorConditionState.PARSING)
                    && // there coud be invalidated state which is in parsing phase now
                    APTHandlersSupport.equalsIgnoreInvalid(pair.state, ppOrigState)) {
                assert !entryFound;
                entryFound = true;
            } else {
                copy.add(pair);
            }
        }
        if (TraceFlags.TRACE_182342_BUG) {
            System.err.printf("setParsedPCState: %s found PARSING entry for file: %s %n", entryFound ? "" : "NOT", fileKey);
            if (copy.isEmpty()) {
                System.err.println("NO KEPT STATES");
            } else {
                int i = 0;
                for (PreprocessorStatePair preprocessorStatePair : copy) {
                    System.err.printf("setParsedPCState: State %d from copy %s%n", i++, preprocessorStatePair);
                }
            }
        }
        if (entryFound) {
            // Phase 2: check preproc conditional states of entry comparing to current conditional state
            ComparisonResult comparisonResult = fillStatesToKeepBasedOnPCState(pcState, copy, statesToKeep);
            switch (comparisonResult) {
                case REPLACE_OTHERS:
                    CndUtils.assertTrueInConsole(statesToKeep.isEmpty(), "states to keep must be empty 3"); // NOI18N
                    entry.setStates(statesToKeep, new PreprocessorStatePair(ppUsedState, pcState));
                    break;
                case KEEP_WITH_OTHERS:
                    assert !statesToKeep.isEmpty();
                    entry.setStates(statesToKeep, new PreprocessorStatePair(ppUsedState, pcState));
                    break;
                case DISCARD:
                    assert !copy.isEmpty();
                    entry.setStates(copy, null);
                    break;
                default:
                    assert false : "unexpected comparison result: " + comparisonResult; //NOI18N
                    break;
            }
        } else {
            // we already were removed, because our ppState was worse
            // or
            // header was parsed with correct context =>
            // no reason to check pcState and replace FilePreprocessorConditionState.PARSING
            // which is not present
        }
        return entryFound;
    }

    void notifyOnWaitParseLock() {
        // notify client waiting for end of fake registration
        synchronized (waitParseLock) {
            waitParseLock.notifyAll();
        }
    }

    public Iterator<CsmUID<CsmFile>> getFilteredFileUIDs(NameAcceptor nameFilter) {
        FileContainer fileContainer = getFileContainer();
        Collection<CsmUID<CsmFile>> filesUID = fileContainer.getFilesUID();
        Collection<CsmUID<CsmFile>> out = new ArrayList<>(filesUID.size());
        for (CsmUID<CsmFile> fileUID : filesUID) {
            CharSequence fileName = FileInfoQueryImpl.getFileName(fileUID);
            if (nameFilter.accept(fileName)) {
                out.add(fileUID);
            }
        }
        return out.iterator();
    }

    /**
     * @return the projectRoots
     */
    protected abstract SourceRootContainer getProjectRoots();

    @Override
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    private static enum ComparisonResult {

        REPLACE_OTHERS,
        KEEP_WITH_OTHERS,
        DISCARD
    }

    /**
     * Checks old states and new one, decides
     * 1. which states to keep
     *    (returns collection of these states)
     * 2. is new state better, worse, or ~same than old ones
     *
     * NB: all OUT parameters are set in this function, so their initial values don't matter
     *
     * @param newState  IN:  new preprocessor state
     *
     * @param oldStates IN:  a collection of old states;
     *                       it might contain newState as well
     *
     * @param statesToKeep  OUT: array to fill with of old states
     *                      (except for new state! - it isn't copied here)
     *                      Unpredictable in the case function returns WORSE
     *
     * @param  newStateFound  OUT: set to true if new state is found among old ones
     *
     * @return  REPLACE_OTHERS - new state is better than old ones
     *          KEEP_WITH_OTHERS - new state is more or less  the same :) as old ones
     *          DISCARD - new state is worse than old ones
     */
    private static ComparisonResult fillStatesToKeepBasedOnPPState(
            PreprocHandler.State newState,
            Collection<PreprocessorStatePair> oldStates,
            Collection<PreprocessorStatePair> statesToKeep,
            AtomicBoolean newStateFound) {

        if (newState == null || !newState.isValid()) {
            return ComparisonResult.DISCARD;
        }

        statesToKeep.clear();
        newStateFound.set(false);
        ComparisonResult result = ComparisonResult.KEEP_WITH_OTHERS;

        for (PreprocessorStatePair pair : oldStates) {
            // newState might already be contained in oldStates
            // it should NOT be added to result
            if (newState.equals(pair.state)) {
                assert !newStateFound.get();
                newStateFound.set(true);
            } else {
                boolean keep = false;
                // check if pure invalid state, but do not consider as invalid
                // the invalidated entry in parsing mode
                if (pair.state != null && (pair.state.isValid())) {
                    if (pair.state.isCompileContext()) {
                        keep = true;
                        if (!newState.isCompileContext()) {
                            return ComparisonResult.DISCARD;
                        }
                    } else {
                        keep = !newState.isCompileContext();
                    }
                }
                if (keep) {
                    if (!pair.state.isCleaned()) {
                        pair = new PreprocessorStatePair(APTHandlersSupport.createCleanPreprocState(pair.state), pair.pcState);
                    }
                    statesToKeep.add(pair);
                    result = ComparisonResult.KEEP_WITH_OTHERS;
                } else {
                    CndUtils.assertTrueInConsole(statesToKeep.isEmpty() || !newState.isCompileContext(), "states to keep must be empty for new compile context entry"); // NOI18N
                    result = statesToKeep.isEmpty() ? ComparisonResult.REPLACE_OTHERS : ComparisonResult.KEEP_WITH_OTHERS;
                }
            }
        }
        if (result == ComparisonResult.REPLACE_OTHERS) {
            CndUtils.assertTrueInConsole(statesToKeep.isEmpty(), "states to keep must be empty "); // NOI18N
        }
        return result;
    }

    /**
     * If it returns WORSE, statesToKeep content is unpredictable!
     *
     * @param newState
     * @param pcState
     * @param oldStates
     * @param statesToKeep
     * @return
     */
    private static ComparisonResult fillStatesToKeepBasedOnPCState(
            FilePreprocessorConditionState pcState,
            List<PreprocessorStatePair> oldStates,
            List<PreprocessorStatePair> statesToKeep) {

        boolean isSuperset = true; // true if this state is a superset of each old state

        // we assume that
        // 1. all oldStates are valid
        // 2. either them all are compileContext
        //    or this one and them all are NOT compileContext
        // so we do *not* check isValid & isCompileContext

        statesToKeep.clear();
        // in this place use direct for loop over list with known size
        // instead of "for (PreprocessorStatePair old : oldStates)"
        // due to performance problem of iterator.hasNext
        int size = oldStates.size();
        for (int i = 0; i < size; i++) {
            PreprocessorStatePair old = oldStates.get(i);
            if (old.pcState == FilePreprocessorConditionState.PARSING) {
                isSuperset = false;
                // not yet filled - file parsing is filling it right now => we don't know what it will be => keep it
                if (!old.state.isCleaned()) {
                    old = new PreprocessorStatePair(APTHandlersSupport.createCleanPreprocState(old.state), old.pcState);
                }
                statesToKeep.add(old);
            } else {
                if (old.pcState.isBetterOrEqual(pcState)) {
                    return ComparisonResult.DISCARD;
                } else if (pcState.isBetterOrEqual(old.pcState)) {
                    // still superset or current can replace old
                } else {
                    // states are not comparable => not superset
                    isSuperset = false;
                    if (!old.state.isCleaned()) {
                        old = new PreprocessorStatePair(APTHandlersSupport.createCleanPreprocState(old.state), old.pcState);
                    }
                    statesToKeep.add(old);
                }
            }
        }
        if (isSuperset) {
            assert statesToKeep.isEmpty() : "should be empty, but it is: " + Arrays.toString(statesToKeep.toArray());
            return ComparisonResult.REPLACE_OTHERS;
        } else {
            return ComparisonResult.KEEP_WITH_OTHERS;
        }
    }

//    private static final boolean isValid(PreprocHandler.State state) {
//        return state != null && state.isValid();
//    }
    public ProjectBase findFileProject(CharSequence absPath, boolean waitFilesCreated) {
        // check own files
        // Wait while files are created. Otherwise project file will be recognized as library file.
        if (waitFilesCreated) {
            ensureFilesCreated();
        }
        if (getFileUID(absPath, false) != null) {
            return this;
        } else {
            // else check in libs
            for (CsmProject prj : getLibraries()) {
                // Wait while files are created. Otherwise project file will be recognized as library file.
                if (waitFilesCreated) {
                    ((ProjectBase) prj).ensureFilesCreated();
                }
                if (((ProjectBase) prj).getFileUID(absPath, false) != null) {
                    return (ProjectBase) prj;
                }
            }
        }
        return null;
    }

    public ProjectBase findFileProject(FSPath fsPath, boolean waitFilesCreated) {
        // check own files
        // Wait while files are created. Otherwise project file will be recognized as library file.
        if (getFileSystem() == fsPath.getFileSystem()){
            if (waitFilesCreated) {
                ensureFilesCreated();
            }
            if (getFileUID(fsPath.getPath(), false) != null) {
                return this;
            }
        }
        // else check in libs
        for (CsmProject prj : getLibraries()) {
            if (((ProjectBase) prj).getFileSystem() == fsPath.getFileSystem()) {
                // Wait while files are created. Otherwise project file will be recognized as library file.
                if (waitFilesCreated) {
                    ((ProjectBase) prj).ensureFilesCreated();
                }
                if (((ProjectBase) prj).getFileUID(fsPath.getPath(), false) != null) {
                    return (ProjectBase) prj;
                }
            }
        }
        return null;
    }

    public final boolean isMySource(CharSequence includePath) {
        return getProjectRoots().isMySource(includePath);
    }

    ////////////////////////////////////////////////////////////////////////////
    // handling events from NativeProject about NativeFileItem manipulations
    ////////////////////////////////////////////////////////////////////////////
    public void onFileItemsRemoved(List<NativeFileItem> items) {
        CndUtils.assertTrueInConsole(!isArtificial(), "library is not expected here ", this);
        try {
            ParserQueue.instance().onStartAddingProjectFiles(this);
            checkForRemoved();
        } finally {
            ParserQueue.instance().onEndAddingProjectFiles(this);
        }
    }

    public void onFileItemRenamed(String oldPath, NativeFileItem newFileIetm) {
        CndUtils.assertTrueInConsole(!isArtificial(), "library is not expected here ", this);
        try {
            ParserQueue.instance().onStartAddingProjectFiles(this);
            // TODO: for now we consider this as pair "remove"/"add" file item
            checkForRemoved();
            onFileItemsAdded(Collections.singletonList(newFileIetm));
        } finally {
            ParserQueue.instance().onEndAddingProjectFiles(this);
        }
    }

    public void onFileItemsAdded(List<NativeFileItem> items) {
        CndUtils.assertTrueInConsole(!isArtificial(), "library is not expected here ", this);
        try {
            ParserQueue.instance().onStartAddingProjectFiles(this);
            for (NativeFileItem item : items) {
                // file object can be invalid for not existing file (#194357)
                assert (item != null && item.getFileObject() != null);
                if (!Utils.acceptNativeItem(item)) {
                    continue;
                }
                PreprocHandler ppHandler = createPreprocHandler(item);
                if (ppHandler != null) {
                    // findFile is good here: for source files it will create it, for header it will not overwrite what we already have
                    // in both cases only really new file is enqueued for parse
                    findFile(item.getAbsolutePath(), true, Utils.getFileType(item), ppHandler, true, ppHandler.getState(), item);
                }
            }
        } finally {
            Notificator.instance().flush();
            ParserQueue.instance().onEndAddingProjectFiles(this);
        }
    }

    public void onFileItemsPropertyChanged(List<NativeFileItem> items, boolean invalidateLibs) {
        CndUtils.assertTrueInConsole(!isArtificial(), "library is not expected here ", this);
        if (!this.isValid()) {
            return;
        }
        if (items.size() > 0) {
            DeepReparsingUtils.reparseOnPropertyChanged(items, this, invalidateLibs);
        }
    }

    ////
    protected abstract ParserQueue.Position getIncludedFileParserQueuePosition();

    public abstract NativeFileItem getNativeFileItem(CsmUID<CsmFile> file);

    protected abstract void putNativeFileItem(CsmUID<CsmFile> file, NativeFileItem nativeFileItem);

    protected abstract NativeFileItem removeNativeFileItem(CsmUID<CsmFile> file);

    protected abstract void clearNativeFileContainer();

    public void onFileImplRemoved(Collection<FileImpl> physicallyRemoved, Collection<FileImpl> excluded) {
        try {
            DeepReparsingUtils.reparseOnRemoved(removeFileImplsFromProjectInternal(physicallyRemoved), removeFileImplsFromProjectInternal(excluded), this);
        } finally {
            Notificator.instance().flush();
        }
    }

    private LinkedList<FileImpl> removeFileImplsFromProjectInternal(Collection<FileImpl> files) {
        LinkedList<FileImpl> removedFromProject = new LinkedList<>();
        for (FileImpl impl : files) {
            if (impl != null) {
                removeNativeFileItem(impl.getUID());
                if (removeFile(impl.getAbsolutePath())) {
                    // this is analogue of synchronization if method was called from different threads,
                    // because removeFile is thread safe and removes only once
                    removedFromProject.addLast(impl);
                    impl.dispose();
                    final FileBuffer buf = impl.getBuffer();
                    APTDriver.invalidateAPT(buf);
                    ClankDriver.invalidate(buf);
                    APTFileCacheManager.getInstance(buf.getFileSystem()).invalidate(buf.getAbsolutePath());
                    ParserQueue.instance().remove(impl);
                    if (CndTraceFlags.TEXT_INDEX) {
                        CndTextIndex.remove(impl.getTextIndexKey());
                    }
                }
                ProgressSupport.instance().fireFileRemoved(impl);
            }
        }
        return removedFromProject;
    }

    public final void onFileObjectExternalCreate(FileObject file) {
        onFileObjectExternalCreate(Arrays.asList(file));
    }

    public final void onFileObjectExternalCreate(Collection<FileObject> files) {
        try {
            ParserQueue.instance().onStartAddingProjectFiles(this);
            CndFileUtils.clearFileExistenceCache();
            // #196664 - Code Model ignores the generated files"
            // when external file was created and assigned to this project =>
            // create csm file for it if possible
            List<NativeFileItem> nativeFileItems = new ArrayList<>();
            // Try to find native file
            if (getPlatformProject() instanceof NativeProject) {
                NativeProject prj = (NativeProject) getPlatformProject();
                for (FileObject fo : files) {
                    if (prj != null) {
                        NativeFileItem item = prj.findFileItem(fo);
                        if (item != null) {
                            nativeFileItems.add(item);
                        }
                    }
                }
            }
            // schedule reparse like added NFI
            if (!nativeFileItems.isEmpty()) {
                onFileItemsAdded(nativeFileItems);
            }
            // allow to fix broken includes
            DeepReparsingUtils.reparseOnAdded(nativeFileItems, this);
        } finally {
            ParserQueue.instance().onEndAddingProjectFiles(this);
        }
    }

    public final void onFileImplExternalChange(FileImpl file) {
        DeepReparsingUtils.tryPartialReparseOnChangedFile(this, file);
    }

    @Override
    public final CsmFile findFile(Object absolutePathOrNativeFileItem, boolean createIfPossible, boolean snapShot) {
        CsmFile res = null;
        if (absolutePathOrNativeFileItem instanceof FSPath) {
            FSPath fsPath = (FSPath)absolutePathOrNativeFileItem;
            if (this.getFileSystem() != fsPath.getFileSystem()) {
                return null;
            }
            absolutePathOrNativeFileItem = fsPath.getPath();
        }
        if (absolutePathOrNativeFileItem instanceof CharSequence) {
            res = findFileByPath((CharSequence) absolutePathOrNativeFileItem, createIfPossible);
        } else if (absolutePathOrNativeFileItem instanceof NativeFileItem) {
            res = findFileByItem((NativeFileItem) absolutePathOrNativeFileItem, createIfPossible);
        }
        if (snapShot && (res instanceof FileImpl)) {
            res = ((FileImpl)res).getSnapshot();
        }
        return res;
    }

    /*package*/final int getFileContainerSize() {
        return getFileContainer().getSize();
    }

    private CsmFile findFileByPath(CharSequence absolutePath, boolean createIfPossible) {
        if (!isValid()) {
            return null;
        }
        absolutePath = CndFileUtils.normalizeAbsolutePath(fileSystem, absolutePath.toString());
        PreprocHandler preprocHandler = null;
        if (getFileContainer().getEntry(absolutePath) == null) {
            if (!createIfPossible) {
                return null;
            }
            NativeFileItem nativeFile = null;
            // Try to find native file
            if (getPlatformProject() instanceof NativeProject) {
                NativeProject prj = (NativeProject) getPlatformProject();
                if (prj != null) {
                    FileObject fo = CndFileUtils.toFileObject(prj.getFileSystem(), absolutePath.toString());
                    if (fo != null) {
                        nativeFile = prj.findFileItem(fo);
                    }
                    if (nativeFile == null) {
                        // if not belong to NB project => not our file
                        return null;
                    // nativeFile = new DefaultFileItem(prj, absolutePath);
                    }
                    if (!Utils.acceptNativeItem(nativeFile)) {
                        return null;
                    }
                    preprocHandler = createPreprocHandler(nativeFile);
                }
            }
            if (preprocHandler != null) {
                return findFile(absolutePath, false, FileImpl.FileType.UNDEFINED_FILE, preprocHandler, true, preprocHandler.getState(), nativeFile);
            }
        }
        // if getPreprocState(file) isn't null, the file alreasy exists, so we may not pass nativeFile
        return findFile(absolutePath, false, FileImpl.FileType.UNDEFINED_FILE, preprocHandler, true, null, null);
    }

    private CsmFile findFileByItem(NativeFileItem nativeFile, boolean createIfPossible) {
        if (!isValid()) {
            return null;
        }
        CharSequence file = nativeFile.getAbsolutePath();
        PreprocHandler preprocHandler = null;
        if (getFileContainer().getEntry(file) == null) {
            if (!createIfPossible || !Utils.acceptNativeItem(nativeFile)) {
                return null;
            }
            // Try to find native file
            if (getPlatformProject() instanceof NativeProject) {
                NativeProject prj = nativeFile.getNativeProject();
                if (prj != null && nativeFile.getFileObject() != null && nativeFile.getFileObject().isValid()) {
                    preprocHandler = createPreprocHandler(nativeFile);
                }
            }
            if (preprocHandler != null) {
                return findFile(file, false, FileImpl.FileType.UNDEFINED_FILE, preprocHandler, true, preprocHandler.getState(), nativeFile);
            }
        }
        // if getPreprocState(file) isn't null, the file alreasy exists, so we may not pass nativeFile
        return findFile(file, false, FileImpl.FileType.UNDEFINED_FILE, preprocHandler, true, null, null);
    }

    protected final FileImpl findFile(CharSequence absPath, boolean treatSymlinkAsSeparateFile, FileImpl.FileType fileType, PreprocHandler preprocHandler,
            boolean scheduleParseIfNeed, PreprocHandler.State initial, NativeFileItem nativeFileItem) {
        FileImpl impl = getFile(absPath, treatSymlinkAsSeparateFile);
        if (impl == null){
            CndUtils.assertTrueInConsole(preprocHandler != null, "null preprocHandler");
            //FIXME:
            if (preprocHandler == null) {
                PreprocHandler.State state = getFirstValidPreprocState(absPath);
                preprocHandler = createPreprocHandlerFromState(absPath, state);
            }
            impl = findFileImpl(absPath, treatSymlinkAsSeparateFile, fileType, preprocHandler, scheduleParseIfNeed, initial, nativeFileItem);
        }
        return impl;
    }

    private FileImpl findFileImpl(CharSequence absPath, boolean treatSymlinkAsSeparateFile, FileImpl.FileType fileType, PreprocHandler preprocHandler,
            boolean scheduleParseIfNeed, PreprocHandler.State initial, NativeFileItem nativeFileItem) {
        FileImpl impl;
        boolean create = false;
        synchronized (fileContainerLock) {
            impl = getFile(absPath, treatSymlinkAsSeparateFile);
            if (impl == null) {
                create = true;
            }
        }
        if (create) {
            // it is expensive in Full Remote mode to create buffer, so do the work out of sync block
            assert preprocHandler != null : "null preprocHandler for " + absPath;
            FileObject fo = CndFileUtils.toFileObject(fileSystem, absPath);
            CndUtils.assertTrueInConsole(fo != null, "file object not found ", absPath); // + " in fs=" + fileSystem); // NOI18N
            if (fo == null) {
                return null;
            }
            FileBuffer fileBuffer = ModelSupport.createFileBuffer(fo);
            // and all other under lock again
            synchronized (fileContainerLock) {
                impl = getFile(absPath, treatSymlinkAsSeparateFile);
                if (impl == null) {
                    try {
                        impl = FileImpl.createFileImpl(fileBuffer, this, fileType, nativeFileItem);
                        if (nativeFileItem != null) {
                            putNativeFileItem(impl.getUID(), nativeFileItem);
                        }
                    // initial can be null here and due to this we have warnings from ParserThread like:
                    // SEVERE [org.netbeans.modules.cnd.modelimpl]: Adding a file with an emty preprocessor state set
                    // TODO: do we need to set up initial value?
//                    if (initial == null) {
//                        initial = APTHandlersSupport.createCleanPreprocState(preprocHandler.getState());
//                    }
                        putFile(impl, initial);
                        // NB: parse only after putting into a map
                        if (scheduleParseIfNeed) {
                            PreprocHandler.State ppState = preprocHandler.getState();
                            ParserQueue.instance().add(impl, ppState, ParserQueue.Position.TAIL);
                        }
                    } finally {
                        Notificator.instance().flush();
                    }
                }
            }
        }

        if (initial == null) {
            if (fileType == FileImpl.FileType.SOURCE_FILE && !impl.isSourceFile()) {
                impl.setSourceFile();
            } else if (fileType == FileImpl.FileType.HEADER_FILE && !impl.isHeaderFile()) {
                impl.setHeaderFile();
            }
        }
        return impl;
    }

    protected final FileImpl createOrFindFileImpl(final FileBuffer buf, final NativeFileItem nativeFile) {
        return createOrFindFileImpl(buf, nativeFile, Utils.getFileType(nativeFile)).fileImpl;
    }

    private static class FileAndHandler {
        private final FileImpl fileImpl;
        private PreprocHandler preprocHandler;

        public FileAndHandler(FileImpl fileImpl, PreprocHandler preprocHandler) {
            this.fileImpl = fileImpl;
            this.preprocHandler = preprocHandler;
        }
    }

    private FileAndHandler createOrFindFileImpl(final FileBuffer buf, final NativeFileItem nativeFile, FileImpl.FileType fileType) {
        PreprocHandler preprocHandler = null;
        CharSequence absPath = buf.getAbsolutePath();
        FileImpl impl = getFile(absPath, true);
        CsmUID<CsmFile> aUid = null;
        if (impl == null) {
            preprocHandler = createPreprocHandler(nativeFile);
            synchronized (fileContainerLock) {
                impl = getFile(absPath, true);
                if (impl == null) {
                    assert preprocHandler != null;
                    impl = FileImpl.createFileImpl(buf, this, fileType, nativeFile);
                    putFile(impl, preprocHandler.getState());
                } else {
                    aUid = impl.getUID();
                    impl.attachToProject(this);
                }
            }
        } else {
            aUid = impl.getUID();
            impl.attachToProject(this);
        }
        if (aUid != null) {
            putNativeFileItem(aUid, nativeFile);
        }
        return new FileAndHandler(impl, preprocHandler);
    }

    public final FileImpl getFile(CharSequence absPath, boolean treatSymlinkAsSeparateFile) {
        return getFileContainer().getFile(absPath, treatSymlinkAsSeparateFile);
    }

    public final CsmUID<CsmFile> getFileUID(CharSequence absPath, boolean treatSymlinkAsSeparateFile) {
        return getFileContainer().getFileUID(absPath, treatSymlinkAsSeparateFile);
    }

    protected final boolean removeFile(CharSequence file) {
        FileContainer fileContainer = getFileContainer();
        FileEntry entry = fileContainer.getEntry(file);
        if (entry == null) {
            return false;
        }
        assert file.toString().contentEquals(UIDUtilities.getFileName(entry.getTestFileUID()));
        Object lock = entry.getLock();
        Collection<ProjectBase> dependentProjects = getDependentProjects();
        synchronized (lock) {
            includedFileContainer.remove(lock, this, file);
            for (ProjectBase prj : dependentProjects) {
                prj.includedFileContainer.remove(lock, this, file);
            }
            synchronized (fileContainerLock) {
                fileContainer.removeFile(file);
            }
        }
        putContainers(dependentProjects, fileContainer);
        return true;
    }

    private void putFile(FileImpl impl, PreprocHandler.State state) {
        assert Thread.holdsLock(fileContainerLock);
        if (state != null && !state.isCleaned()) {
            state = APTHandlersSupport.createCleanPreprocState(state);
        }
        getFileContainer().putFile(impl, state);
    }

    abstract protected Collection<Key> getLibrariesKeys();

    @Override
    public List<CsmProject> getLibraries() {
        List<CsmProject> res = new ArrayList<>();
        if (platformProject instanceof NativeProject) {
            List<NativeProject> dependences = ((NativeProject) platformProject).getDependences();
            int size = dependences.size();
            for (int i = 0; i < size; i++) {
                NativeProject nativeLib = dependences.get(i);
                CsmProject prj = model.findProject(nativeLib);
                if (prj != null) {
                    res.add(prj);
                }
            }
        }
        // Last dependent project is common library.
        //ProjectBase lib = getModel().getLibrary("/usr/include"); // NOI18N
        //if (lib != null) {
        //    res.add(lib);
        //}
        if (!isArtificial()) {
            List<LibProjectImpl> libraries = getLibraryManager().getLibraries((ProjectImpl) this);
            int size = libraries.size();
            for (int i = 0; i < size; i++) {
                res.add(libraries.get(i));
            }
        }
        return res;
    }

    public Collection<ProjectBase> getDependentProjects() {
        List<ProjectBase> res = new ArrayList<>();
        for (CsmProject prj : model.projects()) {
            if (prj instanceof ProjectBase) {
                if (prj.getLibraries().contains(this)) {
                    res.add((ProjectBase) prj);
                }
            }
        }
        return res;
    }


    /**
     * Creates a dummy ClassImpl for unresolved name, stores in map
     * @param nameTokens name
     * @param owner an owner to get file and offset from
     */
    public static CsmClass getDummyForUnresolved(CharSequence[] nameTokens, CsmOffsetable owner) {
        if  (owner != null) {
            CsmFile file = owner.getContainingFile();
            if (file != null) {
                ProjectBase project = (ProjectBase) file.getProject();
                if (project != null) {
                    return project.getDummyForUnresolved(nameTokens, file, owner.getStartOffset());
                }
            }
        }
        return null;
    }

    /**
     * Creates a dummy ClassImpl for unresolved name, stores in map
     * @param nameTokens name
     * @param file file that contains unresolved name (used for the purpose of statistics)
     * @param name offset that contains unresolved name (used for the purpose of statistics)
     */
    private CsmClass getDummyForUnresolved(CharSequence[] nameTokens, CsmFile file, int offset) {
        if (Diagnostic.needStatistics()) {
            Diagnostic.onUnresolvedError(nameTokens, file, offset);
        }
        return getUnresolved().getDummyForUnresolved(nameTokens);
    }

    /**
     * Creates a dummy ClassImpl for unresolved name, stores in map.
     * Should be used only when restoring from persistence:
     * in contrary to getDummyForUnresolved(String[] nameTokens, CsmFile file, int offset),
     * it does not gather statistics!
     * @param nameTokens name
     */
    public final CsmClass getDummyForUnresolved(CharSequence name) {
        return getUnresolved().getDummyForUnresolved(name);
    }

    public final CsmNamespace getUnresolvedNamespace() {
        return getUnresolved().getUnresolvedNamespace();
    }

    public final CsmFile getUnresolvedFile() {
        return getUnresolved().getUnresolvedFile();
    }

    private Unresolved getUnresolved() {
        synchronized (unresolvedLock) {
            if (unresolved == null) {
                unresolved = new Unresolved(this);
            }
            return unresolved;
        }
    }

    @Override
    public final boolean isValid() {
        return platformProject != null && !isDisposing();
    }

    public void setDisposed() {
        disposing.set(true);
        synchronized (initializationTaskLock) {
            if (initializationTask != null) {
                initializationTask.cancel();
                initializationTask = null;
            }
        }
        unregisterProjectListeners();
        ParserQueue.instance().removeAll(this);
    }

    public final boolean isDisposing() {
        return disposing.get();
    }

    /**
     * Returns the disposing flag.
     * Introduced for optimization purposes - for guys like FileImpl to be able to make a fast check
     * without getting the project itself (nor hard-referencing of the project itself either).
     * This strongly relates on the fact that the project stays in memory
     * (we call RepositoryUtils.hang() but never RepositoryUtils.put() for the project)
     */
    AtomicBoolean getDisposingFlag() {
            return disposing;
    }

    /**
     * called under disposeLock.writeLock() to clean up internals if needed
     */
    protected void onDispose() {
    }

    public final void dispose(final boolean cleanPersistent) {

        long time = 0;
        if (TraceFlags.TIMING) {
            System.err.printf("%n%nProject %s: disposing...%n", name);
            time = System.currentTimeMillis();
        }

        // just in case it wasn't called before (it's inexpensive)
        setDisposed();

        try {

            disposeLock.writeLock().lock();
            if (platformProject != null) {
                //if (CndUtils.isDebugMode()) {
                //    checkConsistency(false);
                //}
                getUnresolved().dispose();
                RepositoryUtils.closeUnit(getUID(), getRequiredUnits(), cleanPersistent);
                onDispose();
                platformProject = null;
                unresolved = null;
                uid = null;
            }
        } finally {
            disposeLock.writeLock().unlock();
        }

        if (TraceFlags.TIMING) {
            time = System.currentTimeMillis() - time;
            System.err.printf("Project %s: disposing took %d ms%n", name, time);
        }
    }

    private Set<Integer> getRequiredUnits() {
        Set<Integer> requiredUnits = new HashSet<>();
        for (Key dependent : this.getLibrariesKeys()) {
            requiredUnits.add(dependent.getUnitId());
        }
        return requiredUnits;
    }

//    private void disposeFiles() {
//        Collection<FileImpl> list = getFileContainer().getFileImpls();
//        getFileContainer().clear();
//        for (FileImpl file : list){
//            file.onProjectClose();
//            APTDriver.getInstance().invalidateAPT(file.getBuffer());
//        }
//        //clearNativeFileContainer();
//    }
    private int preventMultiplyDiagnosticExceptionsGlobalNamespace = 0;
    private NamespaceImpl _getGlobalNamespace() {
        NamespaceImpl ns = (NamespaceImpl) UIDCsmConverter.UIDtoNamespace(globalNamespaceUID);
        if (ns == null && preventMultiplyDiagnosticExceptionsGlobalNamespace < 5) {
            DiagnosticExceptoins.registerIllegalRepositoryStateException("Failed to get global namespace by key ", globalNamespaceUID); // NOI18N
            preventMultiplyDiagnosticExceptionsGlobalNamespace++;
        }
        return ns != null ? ns : FAKE_GLOBAL_NAMESPACE;
    }

    private NamespaceImpl _getNamespace(CharSequence key) {
        key = CharSequences.create(key);
        CsmUID<CsmNamespace> nsUID = namespaces.get(key);
        NamespaceImpl ns = (NamespaceImpl) UIDCsmConverter.UIDtoNamespace(nsUID);
        return ns;
    }

    private void _registerNamespace(NamespaceImpl ns) {
        assert (ns != null);
        CharSequence key = ns.getQualifiedName();
        assert CharSequences.isCompact(key);
        CsmUID<CsmNamespace> nsUID = RepositoryUtils.<CsmNamespace>put(ns);
        assert nsUID != null;
        CsmUID<CsmNamespace> prev = namespaces.put(key, nsUID);
        CndUtils.assertTrueInConsole(prev == null || prev == nsUID, "Why replacing " + prev + " by " + nsUID + "?");
    }

    private void _unregisterNamespace(NamespaceImpl ns) {
        assert (ns != null);
        assert !ns.isGlobal();
        CharSequence key = ns.getQualifiedName();
        assert CharSequences.isCompact(key);
        CsmUID<CsmNamespace> nsUID = namespaces.remove(key);
        assert nsUID != null;
        RepositoryUtils.remove(nsUID, ns);
    }

    protected final ModelImpl getModel() {
        return model;
    }

    public void onFileEditStart(FileBuffer buf, NativeFileItem nativeFile) {
    }

    public void onFileEditEnd(FileBuffer buf, NativeFileItem nativeFile, boolean undo) {
    }

    public void onSnapshotChanged(FileImpl file, Snapshot snapshot) {
    }

    private CsmUID<CsmProject> uid = null;
    private final Object uidLock = new Object();

    @Override
    public final CsmUID<CsmProject> getUID() { // final because called from constructor
        CsmUID<CsmProject> out = uid;
        if (out == null) {
            synchronized (uidLock) {
                if (uid == null) {
                    uid = out = UIDUtilities.createProjectUID(this);
                    assert UIDUtilities.getProjectID(uid) == unitId : unitId + " vs. " +  UIDUtilities.getProjectID(uid) + " " + uid;
                    if (TraceFlags.TRACE_CPU_CPP) {System.err.println("getUID for project UID@"+System.identityHashCode(uid) + uid + "on prj@"+System.identityHashCode(this));}
                }
            }
        }
        return out;
    }

    public final Key getUIDKey() {
        // project UID is always key based
        CsmUID<CsmProject> uid = getUID();
        assert uid instanceof KeyHolder : "project UID should be key based ";
        return ((KeyHolder) uid).getKey();
    }

    @Override
    public boolean isStable(CsmFile skipFile) {
        if (status == Status.Ready && !isDisposing()) {
            return !ParserQueue.instance().hasPendingProjectRelatedWork(this, (FileImpl) skipFile);
        }
        return false;
    }

    public final void onParseFinish() {
        onParseFinishImpl(false);
    }

    private void onParseFinishImpl(boolean libsAlreadyParsed) {
        FileImpl.incParseCount();
        synchronized (waitParseLock) {
            waitParseLock.notifyAll();
        }
        // it's ok to move the entire sycle into synchronized block,
        // because from inter-session persistence point of view,
        // if we don't fix fakes, we'll later consider that files are ok,
        // which is incorrect if there are some fakes
        try {
            disposeLock.readLock().lock();

            if (!isDisposing()) {
                if (!hasEditedFiles()) {
                    new FakeRegistrationWorker(this, disposing).fixFakeRegistration(libsAlreadyParsed);
                }
            }
        } catch (Exception e) {
            DiagnosticExceptoins.register(e);
        } finally {
            disposeLock.readLock().unlock();
            // Methods below have no any documented effect.
            // There are possible side-effects (like creating a unit for a key)
            // But they seems to be non relevant here
//            ProjectComponent.setStable(declarationsSorageKey);
//            ProjectComponent.setStable(fileContainerKey);
//            ProjectComponent.setStable(graphStorageKey);
//            ProjectComponent.setStable(classifierStorageKey);
            checkStates(this, libsAlreadyParsed);

            if (!libsAlreadyParsed) {
                ParseFinishNotificator.onParseFinish(this);
            }
        }
        if (TraceFlags.PARSE_STATISTICS) {
            ParseStatistics.getInstance().printResults(this);
            ParseStatistics.getInstance().clear(this);
        }
    }

    private static void checkStates(ProjectBase prj, boolean libsAlreadyParsed){
        if (false) {
            System.err.println("Checking states for project "+prj.getName());
            for(Map.Entry<CharSequence, FileEntry> entry : prj.getFileContainer().getFileStorage().entrySet()){
                for(PreprocessorStatePair pair : entry.getValue().getStatePairs()){
                    if (!pair.state.isValid()){
                        System.err.println("Invalid state for file "+entry.getKey());
                    }
                }
            }
            if (libsAlreadyParsed) {
                for(CsmProject p : prj.getLibraries()){
                    if (p instanceof ProjectBase) {
                        checkStates((ProjectBase) p, false);
                    }
                }
            }
        }
    }

    /* collection to keep fake ASTs during parse phase */
    private final Map<CsmUID<CsmFile>, Map<CsmUID<FunctionImplEx<?>>, Pair<AST, MutableDeclarationsContainer>>> filesFakeFuncData = new WeakHashMap<>();
    /*package*/final void trackFakeFunctionData(CsmUID<CsmFile> fileUID, CsmUID<FunctionImplEx<?>> funUID, Pair<AST, MutableDeclarationsContainer> funData) {
        synchronized (filesFakeFuncData) {
            Map<CsmUID<FunctionImplEx<?>>, Pair<AST, MutableDeclarationsContainer>> fileData = filesFakeFuncData.get(fileUID);
            if (fileData == null) {
                // create always
                fileData = new HashMap<>();
                if (funData != null) {
                    // remember new only if not null data
                    filesFakeFuncData.put(fileUID, fileData);
                }
            }
            if (funData == null) {
                fileData.remove(funUID);
            } else {
                fileData.put(funUID, funData);
            }
        }
    }

    /*package*/final void cleanAllFakeFunctionAST(CsmUID<CsmFile> fileUID) {
        synchronized (filesFakeFuncData) {
            filesFakeFuncData.remove(fileUID);
        }
    }

    /*package*/final void cleanAllFakeFunctionAST() {
        synchronized (filesFakeFuncData) {
            filesFakeFuncData.clear();
        }
    }

    /*package*/Pair<AST, MutableDeclarationsContainer> getFakeFunctionData(CsmUID<CsmFile> fileUID, CsmUID<FunctionImplEx<?>> fakeUid) {
        synchronized (filesFakeFuncData) {
            Map<CsmUID<FunctionImplEx<?>>, Pair<AST, MutableDeclarationsContainer>> fileDatas = filesFakeFuncData.get(fileUID);
            return fileDatas == null ? null : fileDatas.get(fakeUid);
        }
    }

    /*package*/final void onLibParseFinish() {
        onParseFinishImpl(true);
    }

    /**
     * CsmProject implementation
     */
    @Override
    public final Collection<CsmFile> getAllFiles() {
        return getFileContainer().getFiles();
    }

    /**
     * CsmProject implementation
     */
    public final Collection<CsmUID<CsmFile>> getAllFilesUID() {
        return getFileContainer().getFilesUID();
    }

    public final Collection<CsmUID<CsmFile>> getHeaderFilesUID() {
        List<CsmUID<CsmFile>> uids = new ArrayList<>();
        for (FileImpl file : getAllFileImpls()) {
            if (!file.isSourceFile()) {
                uids.add(file.getUID());
            }
        }
        return uids;
    }
    /**
     * We'd better name this getFiles();
     * but unfortunately there already is such method,
     * and it is used intensively
     */
    public final Collection<FileImpl> getAllFileImpls() {
        return getFileContainer().getFileImpls();
    }

    @Override
    public final Collection<CsmFile> getSourceFiles() {
        List<CsmUID<CsmFile>> uids = new ArrayList<>();
        for (FileImpl file : getAllFileImpls()) {
            if (file.isSourceFile()) {
                uids.add(file.getUID());
            }
        }
        return new LazyCsmCollection<>(uids, TraceFlags.SAFE_UID_ACCESS);
    }

    @Override
    public final Collection<CsmFile> getHeaderFiles() {
        return new LazyCsmCollection<>(getHeaderFilesUID(), TraceFlags.SAFE_UID_ACCESS);
    }

    public final long getMemoryUsageEstimation() {
        //TODO: replace with some smart algorythm
        return getFileContainer().getSize();
    }

    @Override
    public final String toString() {
        return getName().toString() + ' ' + getClass().getName() + " @" + hashCode() + ":" + System.identityHashCode(this); // NOI18N
    }

    private volatile int hash = 0;

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = super.hashCode();
        }
        return hash;
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object obj) {
        return obj == this;
    }

    /**
     * Just a struct for the getStartEntryInfo return value:
     * if java allowed passing pointers by reference, we won't create this...
     */
    public static class /*struct*/ StartEntryInfo {

        public final PreprocHandler preprocHandler;
        public final ProjectBase startProject;
        public final FileImpl csmFile;

        public StartEntryInfo(PreprocHandler preprocHandler, ProjectBase startProject, FileImpl csmFile) {
            this.preprocHandler = preprocHandler;
            this.startProject = startProject;
            this.csmFile = csmFile;
        }
    }

    public StartEntryInfo getStartEntryInfo(PreprocHandler fallbackHandler, PreprocHandler.State state) {
        PreprocHandler preprocHandler = null;
        StartEntry startEntry = APTHandlersSupport.extractStartEntry(state);
        ProjectBase startProject = Utils.getStartProject(startEntry);
        FileImpl csmFile = startProject == null ? null : startProject.getFile(startEntry.getStartFile(), false);
        if (csmFile != null) {
            NativeFileItem nativeFile = csmFile.getNativeFileItem();
            if (nativeFile != null && nativeFile.getFileObject() != null && nativeFile.getFileObject().isValid()) {
                preprocHandler = startProject.createPreprocHandler(nativeFile);
            }
        }
        if (preprocHandler == null) {
            preprocHandler = fallbackHandler;
        }
        return new StartEntryInfo(preprocHandler, startProject, csmFile);
    }

    private PreprocHandler restorePreprocHandler(CharSequence interestedFile, PreprocHandler emptyHandler, PreprocHandler.State state, Interrupter interrupter) {
        assert state != null;
        assert state.isCleaned();
        // walk through include stack to restore preproc information
        LinkedList<APTIncludeHandler.IncludeInfo> reverseInclStack = APTHandlersSupport.extractIncludeStack(state);
        assert (reverseInclStack != null);
        if (reverseInclStack.isEmpty()) {
            if (TRACE_PP_STATE_OUT) {
                System.err.println("stack is empty; return default for " + interestedFile);
            }
            return getStartEntryInfo(emptyHandler, state).preprocHandler;
        } else {
            if (TRACE_PP_STATE_OUT) {
                System.err.println("restoring for " + interestedFile);
            }
            if (APTTraceFlags.USE_CLANK) {
                PreprocHandler ppHandler = getStartEntryInfo(null, state).preprocHandler;
                if (ppHandler != null) {
                    ppHandler.setState(state);
                    return ppHandler;
            } else {
                    return this.createDefaultPreprocHandler(interestedFile);
                }
            } else {
                return APTTokenStreamProducer.restorePreprocHandlerFromIncludeStack(this, reverseInclStack, interestedFile, emptyHandler, state, interrupter);
            }
        }
    }


    private NativeProject findNativeProjectHolder(Set<ProjectBase> visited) {
        visited.add(this);
        NativeProject nativeProject = ModelSupport.getNativeProject(getPlatformProject());
        if (nativeProject == null) {
            // try to find dependent projects and ask them
            for (ProjectBase dependentPrj : getDependentProjects()) {
                if (!visited.contains(dependentPrj)) {
                    nativeProject = dependentPrj.findNativeProjectHolder(visited);
                    if (nativeProject != null) {
                        // found
                        break;
                    }
                }
            }
        }
        return nativeProject;
    }

    public PreprocHandler createDefaultPreprocHandler(CharSequence interestedFile) {
        NativeProject nativeProject = findNativeProjectHolder(new HashSet<ProjectBase>(10));
        PreprocHandler out;
        if (nativeProject != null) {
            // we have own native project to get settings from
            NativeFileItem item = new DefaultFileItem(nativeProject, interestedFile.toString());
            out = createPreprocHandler(item);
        } else {
            out = createEmptyPreprocHandler(interestedFile);
        }
        assert out != null : "failed creating default ppState for " + interestedFile;
        return out;
    }

    public final GraphContainer getGraph() {
        return getGraphStorage();
    }

    /**
     * gets all files that direct or indirect include the referenced file.
     */
    public final Set<CsmFile> getParentFiles(CsmFile referencedFile) {
        return getGraphStorage().getParentFiles(referencedFile);
    }

    private final static class DefaultFileItem implements NativeFileItem {

        private final NativeProject project;
        private final String normalizedAbsPath;

        public DefaultFileItem(NativeProject project, String absolutePath) {
            Parameters.notNull("project", project);
            Parameters.notNull("absolutePath", absolutePath);
            this.project = project;
            this.normalizedAbsPath = CndFileUtils.normalizeAbsolutePath(project.getFileSystem(), absolutePath);
        }

        public DefaultFileItem(NativeFileItem nativeFile) {
            Parameters.notNull("nativeFile", nativeFile);
            this.project = nativeFile.getNativeProject();
            this.normalizedAbsPath = nativeFile.getAbsolutePath(); // always normalized
            CndUtils.assertNormalized(project.getFileSystem(), normalizedAbsPath);
            Parameters.notNull("nativeFile.getAbsolutePath()", normalizedAbsPath);
        }

        public static NativeFileItem toDefault(NativeFileItem nativeFile) {
            // if not already fake
            if (!(nativeFile instanceof DefaultFileItem)) {
                nativeFile = new DefaultFileItem(nativeFile);
            }
            return nativeFile;
        }

        @Override
        public List<String> getUserMacroDefinitions() {
            if (project != null) {
                return project.getUserMacroDefinitions();
            }
            return Collections.<String>emptyList();
        }

        @Override
        public List<IncludePath> getUserIncludePaths() {
            if (project != null) {
                return project.getUserIncludePaths();
            }
            return Collections.<IncludePath>emptyList();
        }

        @Override
        public List<FSPath> getIncludeFiles() {
            if (project != null) {
                return project.getIncludeFiles();
            }
            return Collections.emptyList();
        }

        @Override
        public List<String> getSystemMacroDefinitions() {
            if (project != null) {
                return project.getSystemMacroDefinitions();
            }
            return Collections.<String>emptyList();
        }

        @Override
        public List<IncludePath> getSystemIncludePaths() {
            if (project != null) {
                return project.getSystemIncludePaths();
            }
            return Collections.<IncludePath>emptyList();
        }

        @Override
        public List<FSPath> getSystemIncludeHeaders() {
            if (project != null) {
                return project.getSystemIncludeHeaders();
            }
            return Collections.<FSPath>emptyList();
        }

        @Override
        public NativeProject getNativeProject() {
            return project;
        }

        @Override
        public FileObject getFileObject() {
            FileObject fo = CndFileUtils.toFileObject(project.getFileSystem(), normalizedAbsPath);
            if (fo == null) {
                fo = InvalidFileObjectSupport.getInvalidFileObject(project.getFileSystem(), normalizedAbsPath);
            }
            return fo;
        }

        @Override
        public String getAbsolutePath() {
            return normalizedAbsPath;
        }

        @Override
        public String getName() {
            return CndPathUtilities.getBaseName(normalizedAbsPath);
        }

        @Override
        public Language getLanguage() {
            return NativeFileItem.Language.C_HEADER;
        }

        @Override
        public LanguageFlavor getLanguageFlavor() {
            return NativeFileItem.LanguageFlavor.UNKNOWN;
        }

        @Override
        public boolean isExcluded() {
            return false;
        }

        @Override
        public String toString() {
            return normalizedAbsPath + ' ' + project.getFileSystem().getDisplayName(); //NOI18N
        }
    }

    /**
     * Represent the project status.
     *
     * Concerns only initial stage of project life cycle:
     * allows to distinguish just newly-created project,
     * the phase when files are being added to project (and to parser queue)
     * and the phase when all files are already added.
     *
     * It isn't worth tracking further stages (stable/unstable)
     * since it's error prone (it's better to ask, say, parser queue
     * whether it contains files that belong to this project or not)
     */
    protected static enum Status {

        Initial,
        Restored,
        AddingFiles,
        Validating,
        Ready;
    }
    private volatile Status status;
    /** The task that is run in a request processor during project initialization */
    private Cancellable initializationTask;
    /** The lock under which the initializationTask is set */
    private static final class InitializationTaskLock {}
    private final Object initializationTaskLock = new InitializationTaskLock();
    private static final class WaitParseLock {}
    private final Object waitParseLock = new WaitParseLock();
    // to profile monitor usages
    private static final class ClassifierReplaceLock {}
    private final Object classifierReplaceLock = new ClassifierReplaceLock();
    private ModelImpl model;
    private Unresolved unresolved;
    private final Object unresolvedLock = new Object();

    private CharSequence name;
    private CsmUID<CsmNamespace> globalNamespaceUID;
    private NamespaceImpl FAKE_GLOBAL_NAMESPACE;
    /** Either NativeProject or CharSequence */
    private volatile Object platformProject;
    private final FileSystem fileSystem;

    private boolean hasFileSystemProblems;
    private final Object fileSystemProblemsLock = new Object();

    /**
     * Some notes concerning disposing and disposeLock fields.
     *
     * The purpose is not to perform some actions
     * (such as adding new files, continuing initialization, etc)
     * when the project is going to be disposed.
     *
     * The disposing field is changed only once,
     * from false to true (in setDispose() method)
     *
     * When it is changed to true, no lock is acquired, BUT:
     * it is guaranteed that events take place in the following order:
     * 1) disposing is set to true
     * 2) the disposeLock.writeLock() is locked after that
     * and remains locked during the entire project closure.
     *
     * Clients who need to check this, are obliged to
     * act in the following sequence:
     * 1) require disposeLock.readLock()
     * 2) check that the disposing field is still false
     * 3) keep disposeLock.readLock() locked
     * while performing critical actions
     * (the actions that should not be done
     * when the project is being disposed)
     *
     */
    private final AtomicBoolean disposing = new AtomicBoolean(false);
    private final ReadWriteLock disposeLock = new ReentrantReadWriteLock();
    private final CharSequence uniqueName;
    private final int unitId;
    private final LibraryManager libraryManager;
    private final Map<CharSequence, CsmUID<CsmNamespace>> namespaces;
    private final Key classifierStorageKey;

    // collection of sharable system macros and system includes
    private final APTSystemStorage sysAPTData;
    private final APTIncludePathStorage userPathStorage;
    private static final class NamespaceLock {}
    private final Object namespaceLock = new NamespaceLock();
    private final Key declarationsSorageKey;
    private final Key fileContainerKey;
    private static final class FileContainerLock {}
    private final Object fileContainerLock = new FileContainerLock();
    private final Key graphStorageKey;

    // remove as soon as TraceFlags.MERGE_EVENTS is removed
    private volatile NativeProjectListenerImpl projectListener;
    // remove as soon as TraceFlags.MERGE_EVENTS is removed
    private final Object projectListenerLock = new Object();

    // test variables.
    public static final boolean TRACE_PP_STATE_OUT = DebugUtils.getBoolean("cnd.dump.preproc.state", false); // NOI18N
    public static final int GATHERING_MACROS = 0;
    public static final int GATHERING_TOKENS = 1;
    public static final boolean DO_NOT_TRCE_DUMMY_FORWARD_CLASSIFIER = DebugUtils.getBoolean("cnd.dump.skip.dummy.forward.classifier", false); // NOI18N

    ////////////////////////////////////////////////////////////////////////////
    /**
     * for tests only
     */
    public static List<String> testGetRestoredFiles() {
        return APTTokenStreamProducer.testGetRestoredFiles();
    }
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    @Override
    public void write(RepositoryDataOutput aStream) throws IOException {
        assert aStream != null;
        aStream.writeUnitId(unitId);
        PersistentUtils.writeFileSystem(fileSystem, aStream);
        UIDObjectFactory aFactory = UIDObjectFactory.getDefaultFactory();
        assert aFactory != null;
        assert this.name != null;
        aStream.writeFilePathForFileSystem(fileSystem, name);
        //PersistentUtils.writeUTF(RepositoryUtils.getUnitName(getUID()), aStream);
        aFactory.writeUID(this.globalNamespaceUID, aStream);
        aFactory.writeStringToUIDMap(this.namespaces, aStream, false);

        ProjectComponent.writeKey(fileContainerKey, aStream);
        ProjectComponent.writeKey(declarationsSorageKey, aStream);
        ProjectComponent.writeKey(graphStorageKey, aStream);
        ProjectComponent.writeKey(classifierStorageKey, aStream);
        this.includedFileContainer.write(aStream);

        aStream.writeFilePathForFileSystem(fileSystem, this.uniqueName);
        aStream.writeBoolean(hasFileSystemProblems);
        checkUniqueNameConsistency();
    }

    protected ProjectBase(RepositoryDataInput aStream) throws IOException {
        unitId = aStream.readUnitId();
        fileSystem = PersistentUtils.readFileSystem(aStream);
        assert fileSystem != null; // soft check and throwing IOException is moved to repository
        sysAPTData = APTSystemStorage.getInstance();
        userPathStorage = new APTIncludePathStorage();

        setStatus(Status.Restored);

        assert aStream != null;
        UIDObjectFactory aFactory = UIDObjectFactory.getDefaultFactory();
        assert aFactory != null : "default UID factory can not be bull";

        this.name = ProjectNameCache.getManager().getString(aStream.readFilePathForFileSystem(fileSystem));
        assert this.name != null : "project name can not be null";

        //CharSequence unitName = PersistentUtils.readUTF(aStream, DefaultCache.getManager());

        this.globalNamespaceUID = aFactory.readUID(aStream);
        assert globalNamespaceUID != null : "globalNamespaceUID can not be null";

        int collSize = aStream.readInt();
        if (collSize <= 0) {
            namespaces = new ConcurrentHashMap<>(0);
        } else {
            namespaces = new ConcurrentHashMap<>(collSize);
        }
        aFactory.readStringToUIDMap(this.namespaces, aStream, QualifiedNameCache.getManager(), collSize);

        fileContainerKey = ProjectComponent.readKey(aStream);
        assert fileContainerKey != null : "fileContainerKey can not be null";
        weakFileContainer = new WeakContainer<>(this, fileContainerKey);

        declarationsSorageKey = ProjectComponent.readKey(aStream);
        assert declarationsSorageKey != null : "declarationsSorageKey can not be null";
        weakDeclarationContainer = new WeakContainer<>(this, declarationsSorageKey);

        graphStorageKey = ProjectComponent.readKey(aStream);
        assert graphStorageKey != null : "graphStorageKey can not be null";
        weakGraphContainer = new WeakContainer<>(this, graphStorageKey);

        classifierStorageKey = ProjectComponent.readKey(aStream);
        assert classifierStorageKey != null : "classifierStorageKey can not be null";
        weakClassifierContainer = new WeakContainer<>(this, classifierStorageKey);

        includedFileContainer = new IncludedFileContainer(aStream);

        uniqueName = ProjectNameCache.getManager().getString(aStream.readFilePathForFileSystem(fileSystem));
        assert uniqueName != null : "uniqueName can not be null";

        this.model = (ModelImpl) CsmModelAccessor.getModel();

        this.FAKE_GLOBAL_NAMESPACE = NamespaceImpl.create(this, true);
        this.hasFileSystemProblems = aStream.readBoolean();
        this.libraryManager = LibraryManager.getInstance(this.getUnitId());
    }

    public final int getUnitId() {
        return unitId;
    }

    private final WeakContainer<DeclarationContainerProject> weakDeclarationContainer;
    private DeclarationContainerProject getDeclarationsSorage() {
        DeclarationContainerProject dc = weakDeclarationContainer.getContainer();
        return dc != null ? dc : DeclarationContainerProject.empty();
    }

    private final WeakContainer<FileContainer> weakFileContainer;
    private FileContainer getFileContainer() {
        FileContainer fc = weakFileContainer.getContainer();
        return fc != null ? fc : FileContainer.empty();
    }

    private final WeakContainer<GraphContainer> weakGraphContainer;
    public final GraphContainer getGraphStorage() {
        GraphContainer gc = weakGraphContainer.getContainer();
        return gc != null ? gc : GraphContainer.empty();
    }

    private final WeakContainer<ClassifierContainer> weakClassifierContainer;
    private ClassifierContainer getClassifierSorage() {
        ClassifierContainer cc = weakClassifierContainer.getContainer();
        return cc != null ? cc : ClassifierContainer.empty();
    }

    public static void dumpProjectContainers(PrintStream printStream, CsmProject prj, boolean dumpFiles) {
        ProjectBase project = (ProjectBase) prj;
        dumpProjectClassifierContainer(project, printStream, !dumpFiles);
        dumpProjectDeclarationContainer(project, printStream);
        if (dumpFiles) {
            ProjectBase.dumpFileContainer(project, new PrintWriter(printStream));
            ProjectBase.dumpProjectGrapthContainer(project, new PrintWriter(printStream));
        }
    }

    public static void dumpProjectGrapthContainer(CsmProject prj, PrintWriter printStream) {
        ProjectBase project = (ProjectBase) prj;
        GraphContainer container = project.getGraphStorage();
        printStream.println("%n++++++++++ Dumping Graph container " + project.getDisplayName()); // NOI18N
        Map<CharSequence, CsmFile> map = new TreeMap<>();
        for (CsmFile f : project.getAllFiles()) {
            map.put(f.getAbsolutePath(), f);
        }
        for (CsmFile file : map.values()) {
            printStream.println("%n========== Dumping links for file " + file.getAbsolutePath()); // NOI18N
            Map<CharSequence, CsmFile> set = new TreeMap<>();
            for (CsmFile f : container.getInLinks(file)) {
                set.put(f.getAbsolutePath(), (FileImpl) f);
            }
            if (set.size() > 0) {
                printStream.println("\tInput"); // NOI18N
                for (CsmFile f : set.values()) {
                    printStream.println("\t\t" + f.getAbsolutePath()); // NOI18N
                }
                set.clear();
            }
            for (CsmFile f : container.getOutLinks(file)) {
                set.put(f.getAbsolutePath(), (FileImpl) f);
            }
            if (set.size() > 0) {
                printStream.println("\tOutput"); // NOI18N
                for (CsmFile f : set.values()) {
                    printStream.println("\t\t" + f.getAbsolutePath()); // NOI18N
                }
            }
        }
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("NP")
    /*package*/static void dumpProjectClassifierContainer(ProjectBase project, PrintStream printStream, boolean offsetString) {
        ClassifierContainer container = project.getClassifierSorage();
        for (int phase = 0; phase < 3; phase++) {
            Map<CharSequence, CsmClassifier> map = null;
            switch (phase) {
                case 0:
                    printStream.printf("%n========== Dumping %s Project Classifiers%n", project.getName());//NOI18N
                    map = container.getTestClassifiers();
                    break;
                case 1:
                    printStream.printf("%n========== Dumping %s Project Short Classifiers%n", project.getName());//NOI18N
                    map = container.getTestShortClassifiers();
                    break;
                case 2:
                    printStream.printf("%n========== Dumping %s Project Typedefs%n", project.getName());//NOI18N
                    map = container.getTestTypedefs();
                    break;
            }
            for (Map.Entry<CharSequence, CsmClassifier> entry : map.entrySet()) {
                printStream.print("\t" + entry.getKey().toString() + " ");//NOI18N
                CsmClassifier value = entry.getValue();
                if (value == null) {
                    printStream.println("null");//NOI18N
                } else {
                    String pos = offsetString ? CsmTracer.getOffsetString(value, true) : "";//NOI18N
                    printStream.printf("%s %s%n", value.getUniqueName(), pos);//NOI18N
                }
            }
        }
    }

    /*package*/ static void dumpProjectDeclarationContainer(ProjectBase project, PrintStream printStream) {
        printStream.println("%n========== Dumping Project declarations");//NOI18N
        DeclarationContainerProject container = project.getDeclarationsSorage();
        for (Map.Entry<CharSequence, Object> entry : container.getTestDeclarations().entrySet()) {
            printStream.println("\t" + entry.getKey().toString());//NOI18N
            TreeMap<CharSequence, CsmDeclaration> set = new TreeMap<>();
            Object o = entry.getValue();
            if (o instanceof CsmUID<?>[]) {
                // we know the template type to be CsmDeclaration
                @SuppressWarnings("unchecked") // checked //NOI18N
                CsmUID<CsmDeclaration>[] uids = (CsmUID<CsmDeclaration>[]) o;
                for (CsmUID<CsmDeclaration> uidt : uids) {
                    final CsmDeclaration object = uidt.getObject();
                    if (object != null) {
                        set.put(((CsmOffsetableDeclaration) object).getContainingFile().getAbsolutePath(), object);
                    } else {
                        printStream.println("\tNO OBJECT FOR " + entry.getKey().toString() + "%n\t"+uidt);//NOI18N
                    }
                }
            } else if (o instanceof CsmUID<?>) {
                // we know the template type to be CsmDeclaration
                @SuppressWarnings("unchecked") // checked //NOI18N
                CsmUID<CsmDeclaration> uidt = (CsmUID<CsmDeclaration>) o;
                final CsmDeclaration object = uidt.getObject();
                if (object != null) {
                    set.put(((CsmOffsetableDeclaration) object).getContainingFile().getAbsolutePath(), object);
                } else {
                    printStream.println("\tNO OBJECT FOR " + entry.getKey().toString() + "%n\t" + uidt);//NOI18N
                }
            }
            for (Map.Entry<CharSequence, CsmDeclaration> f : set.entrySet()) {
                if (DO_NOT_TRCE_DUMMY_FORWARD_CLASSIFIER) {
                    if (ForwardClass.isForwardClass(f.getValue()) || ForwardEnum.isForwardEnum(f.getValue())) {
                        continue;
                    }
                }
                printStream.println("\t\t" + f.getValue() + " from " + f.getKey()); //NOI18N
            }
        }
        printStream.println("%n========== Dumping Project friends");//NOI18N
        for (Map.Entry<CharSequence, Set<CsmUID<CsmFriend>>> entry : container.getTestFriends().entrySet()) {
            printStream.println("\t" + entry.getKey().toString());//NOI18N
            TreeMap<CharSequence, CsmFriend> set = new TreeMap<>();
            for (CsmUID<? extends CsmFriend> uid : entry.getValue()) {
                CsmFriend f = uid.getObject();
                set.put(f.getQualifiedName(), f);
            }
            for (Map.Entry<CharSequence, CsmFriend> f : set.entrySet()) {
                printStream.println("\t\t" + f.getKey() + " " + f.getValue());//NOI18N
            }
        }
    }

    public static void dumpFileContainer(CsmProject project, PrintWriter printStream) {
        FileContainer fileContainer = ((ProjectBase) project).getFileContainer();
        printStream.println("%n++++++++++ Dumping File container " + project.getDisplayName()); // NOI18N
        Map<CharSequence, Object/*CharSequence or CharSequence[]*/> names = fileContainer.getCanonicalNames();
        //for unit test only
        Map<CharSequence, FileEntry> files = fileContainer.getFileStorage();
        for(Map.Entry<CharSequence, FileEntry> entry : files.entrySet()){
            CharSequence key = entry.getKey();
            printStream.println("\tFile "+key.toString()); // NOI18N
            Object name = names.get(key);
            if (name instanceof CharSequence[]) {
                for(CharSequence alt : (CharSequence[])name) {
                    printStream.println("\t\tAlias "+alt.toString()); // NOI18N
                }
            } else if (name instanceof CharSequence) {
                printStream.println("\t\tAlias "+name.toString()); // NOI18N
            }
            FileEntry file = entry.getValue();
            CsmFile csmFile = file.getTestFileUID().getObject();
            printStream.println("\t\tModel File "+csmFile.getAbsolutePath()); // NOI18N
            printStream.println("\t\tNumber of states "+file.getPrerocStates().size()); // NOI18N
            Collection<PreprocessorStatePair> statePairs = file.getStatePairs();
            List<String> states = new ArrayList<>();
            for (PreprocessorStatePair preprocessorStatePair : statePairs) {
                states.add(FilePreprocessorConditionState.toStringBrief(preprocessorStatePair.pcState));
            }
            Collections.sort(states);
            for (String state : states) {
                StringTokenizer st = new StringTokenizer(state,"%n"); // NOI18N
                boolean first = true;
                while (st.hasMoreTokens()) {
                    if (first) {
                        printStream.println("\t\tState "+st.nextToken()); // NOI18N
                        first = false;
                    } else {
                        printStream.println("\t\t\t"+st.nextToken()); // NOI18N
                    }
                }
            }
            printStream.flush();
        }
        printStream.flush();
    }

    private static final Comparator<NativeFileItem> NATIVE_FILE_ITEMS_COMPARATOR = new Comparator<NativeFileItem>() {
        @Override
        public int compare(NativeFileItem o1, NativeFileItem o2) {
            return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
        }
    };
}
