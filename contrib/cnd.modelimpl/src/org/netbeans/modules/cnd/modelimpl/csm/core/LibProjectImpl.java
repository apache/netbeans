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

import java.io.*;
import java.util.*;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.openide.filesystems.FileSystem;
import org.openide.util.CharSequences;

/**
 */
public final class LibProjectImpl extends ProjectBaseWithEditing {

    private final CharSequence includePath;
    private final SourceRootContainer projectRoots = new SourceRootContainer(true);

    private static void cleanLibraryRepository(FileSystem fs, CharSequence platformProject, boolean articicial, int storageID) {
        Key key = createLibraryProjectKey(fs, platformProject, storageID);
        RepositoryUtils.closeUnit(key, null, true);
    }

    private static Key createLibraryProjectKey(FileSystem fs, CharSequence  platfProj, int storageID) {
        return KeyUtilities.createLibraryProjectKey(KeyUtilities.createUnitDescriptor(platfProj, fs), storageID);
    }

    private static ProjectBase readLibraryInstance(ModelImpl model, FileSystem fs, CharSequence platformProject, CharSequence name, int storageID) {
        ProjectBase instance = readInstance(model, createLibraryProjectKey(fs, platformProject, storageID), platformProject, name);
        return instance;
    }

    
    private LibProjectImpl(ModelImpl model, FileSystem fs, CharSequence includePathName, int storageID) {
        super(model, fs, (Object) includePathName, includePathName, createLibraryProjectKey(fs, includePathName, storageID));
        this.includePath = FilePathCache.getManager().getString(includePathName);
        this.projectRoots.fixFolder(includePathName);
        assert this.includePath != null;
    }

    public static LibProjectImpl createInstance(ModelImpl model, FileSystem fs, CharSequence includePathName, int storageID) {
        ProjectBase instance = null;
        assert includePathName != null;
        if (TraceFlags.PERSISTENT_REPOSITORY) {
            try {
                instance = readLibraryInstance(model, fs, includePathName, includePathName, storageID);
            } catch (Exception e) {
                // just report to console;
                // the code below will create project "from scratch"
                cleanLibraryRepository(fs, includePathName, true, storageID);
                DiagnosticExceptoins.register(e);
            }
        }
        if (instance != null && !(instance instanceof LibProjectImpl)) {
            CndUtils.assertTrueInConsole(false, "cannot correctly deserialize library for " + includePathName);
            cleanLibraryRepository(fs, includePathName, true, storageID);
            instance = null;
        }
        if (instance == null) {
            instance = new LibProjectImpl(model, fs, includePathName, storageID);
        }
        if (CndUtils.isDebugMode()) {
            CndUtils.assertTrue(CharSequences.comparator().compare(includePathName, ((LibProjectImpl) instance).includePath) == 0, includePathName + " vs. " + ((LibProjectImpl) instance).includePath);
            CndUtils.assertTrue(instance.getFileSystem() == fs, instance.getFileSystem() + " vs. " + fs);
        }
        return (LibProjectImpl) instance;
    }

    protected CharSequence getPath() {
        return includePath;
    }

    @Override
    protected void ensureFilesCreated() {
    }

    protected boolean isStableStatus() {
        return true;
    }

    @Override
    public Collection<ProjectBase> getDependentProjects() {
        final LibraryManager instance = LibraryManager.getInstance(getUnitId());
        // TODO: looks like not very safe way to get dependencies
        // see issue #211061
        return instance == null ? Collections.<ProjectBase>emptyList() : instance.getProjectsByLibrary(this);
    }

    @Override
    protected Collection<Key> getLibrariesKeys() {
        return Collections.<Key>emptySet();
    }

    /** override parent to avoid infinite recursion */
    @Override
    public List<CsmProject> getLibraries() {
        return Collections.<CsmProject>emptyList();
    }

    @Override
    protected final ParserQueue.Position getIncludedFileParserQueuePosition() {
        return ParserQueue.Position.TAIL;
    }

    @Override
    public boolean isArtificial() {
        return true;
    }

    @Override
    public NativeFileItem getNativeFileItem(CsmUID<CsmFile> file) {
        return null;
    }

    @Override
    protected void putNativeFileItem(CsmUID<CsmFile> file, NativeFileItem nativeFileItem) {
    }

    @Override
    protected NativeFileItem removeNativeFileItem(CsmUID<CsmFile> file) {
        return null;
    }

    @Override
    protected void clearNativeFileContainer() {
    }

    @Override
    public boolean isStable(CsmFile skipFile) {
        if (!isDisposing()) {
            return !ParserQueue.instance().hasPendingProjectRelatedWork(this, (FileImpl) skipFile);
        }
        return false;
    }

    @Override
    protected SourceRootContainer getProjectRoots() {
        return projectRoots;
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    @Override
    public void write(RepositoryDataOutput aStream) throws IOException {
        super.write(aStream);
        assert this.includePath != null;
        aStream.writeFilePathForFileSystem(getFileSystem(), includePath);
    }

    public LibProjectImpl(RepositoryDataInput aStream) throws IOException {
        super(aStream);
        this.includePath = aStream.readFilePathForFileSystem(getFileSystem());
        assert this.includePath != null;
        setPlatformProject(this.includePath);
    }
}
