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

package org.netbeans.modules.cnd.modelimpl.content.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.debug.DebugUtils;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler.State;
import org.netbeans.modules.cnd.apt.utils.APTSerializeUtils;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FilePreprocessorConditionState;
import org.netbeans.modules.cnd.modelimpl.csm.core.PreprocessorStatePair;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.FileContainerKey;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.DefaultCache;
import org.netbeans.modules.cnd.modelimpl.uid.KeyBasedUID;
import org.netbeans.modules.cnd.modelimpl.uid.LazyCsmCollection;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 * Storage for files and states. Class was extracted from ProjectBase.
 */
public class FileContainer extends ProjectComponent {
    private static final boolean TRACE_PP_STATE_OUT = DebugUtils.getBoolean("cnd.dump.preproc.state", false);
    private static final Logger LOG = Logger.getLogger("repository.support.filecreate.logger"); //NOI18N

    private static final class Lock {}
    private final Object lock = new Lock();
    private final Map<CharSequence, FileEntry> myFiles = new ConcurrentHashMap<>();
    private final ConcurrentMap<CharSequence, Object/*CharSequence or CharSequence[]*/> canonicFiles = new ConcurrentHashMap</*CharSequence or CharSequence[]*/>();
    private final FileSystem fileSystem;

    // empty stub
    private static final FileContainer EMPTY = new FileContainer() {

        @Override
        public void put() {
            // do nothing
        }

        @Override
        public void putFile(FileImpl impl, State state) {
            // do nothing
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Put in the empty FileContainer the file {0}", impl); //NOI18N
            }
        }

        @Override
        public void removeFile(CharSequence file) {
            // do nothing
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Remove from the empty FileContainer the file {0}", file); //NOI18N
            }
        }
    };

    /** Creates a new instance of FileContainer */
    public FileContainer(ProjectBase project) {
	super(new FileContainerKey(project.getUnitId()));
        fileSystem = project.getFileSystem();
	put();
    }

    public FileContainer (RepositoryDataInput input) throws IOException {
	super(input);
        fileSystem = PersistentUtils.readFileSystem(input);
        readFilePathsForFileSystemToFileEntryMap(fileSystem, input, myFiles);
        readFilePathsForFileSystemToStringsArrMap(fileSystem, input, canonicFiles);
	//trace(canonicFiles, "Read in ctor:");
        if (CndUtils.isDebugMode()) {
            checkConsistency();
        }
    }

    // only for creating EMPTY stub
    private FileContainer() {
        super((org.netbeans.modules.cnd.repository.spi.Key) null);
        fileSystem = null;
    }

    public static FileContainer empty() {
        return EMPTY;
    }

    private void trace(Map<CharSequence, Object/*String or CharSequence[]*/> map, String title) {
	System.err.printf("%s%n", title);
	for( Map.Entry<CharSequence, Object> entry : map.entrySet() ) {
	    System.err.printf("%s ->%n%s%n%n", entry.getKey(), entry.getValue());
	}
    }

    public void putFile(FileImpl impl, PreprocHandler.State state) {
        if (CndUtils.isDebugMode()) {
            checkConsistency();
        }
        CharSequence path = getFileKey(impl.getAbsolutePath(), true);
        CharSequence canonicalPath = getCanonicalKey(path);
        FileEntry newEntry;
        CsmUID<CsmFile> uid = RepositoryUtils.<CsmFile>put(impl);
        newEntry = new FileEntry(uid, state, path, impl.getFileSystem(), canonicalPath);
        FileEntry old;

        old = myFiles.put(path, newEntry);
        addAlternativeFileKey(path, newEntry.canonical);

        if (old != null){
            System.err.println("Replace file info for "+ old.fileNew + " with " + impl);
        }
        if (CndUtils.isDebugMode()) {
            checkConsistency();
        }
	put();
    }

    public void removeFile(CharSequence file) {
        CharSequence path = getFileKey(file, false);
        FileEntry f;
        if (CndUtils.isDebugMode()) {
            checkConsistency();
        }
        f = myFiles.remove(path);
        if (f != null) {
            removeAlternativeFileKey(f.canonical, path);
        }

        if (f != null) {
            if (f.fileNew != null){
                // clean repository
                if (false) { RepositoryUtils.remove(f.fileNew, null) ;}
            }
        }
        if (CndUtils.isDebugMode()) {
            checkConsistency();
        }
	put();
    }

    public FileImpl getFile(CharSequence absPath, boolean treatSymlinkAsSeparateFile) {
        FileEntry f = getFileEntry(absPath, treatSymlinkAsSeparateFile, false);
        return getFile(f);
    }

    private FileImpl getFile(FileEntry f) {
        if (f == null) {
            return null;
        }
        CsmUID<CsmFile> fileUID = f.fileNew;
        FileImpl impl = (FileImpl) UIDCsmConverter.UIDtoFile(f.fileNew);
        if( impl == null ) {
            String postfix = ""; // NOI18N
            if (CndUtils.isDebugMode() || CndUtils.isUnitTestMode()) {
                FileImpl impl2 = (FileImpl) UIDCsmConverter.UIDtoFile(f.fileNew);
                if (impl2 == null) {
                    postfix = " TWICE"; // NOI18N
                } else {
                    postfix = " second attempt OK"; // NOI18N
                }
            }
            if (fileUID instanceof KeyBasedUID) {
                DiagnosticExceptoins.registerIllegalRepositoryStateException("no file for UID " + postfix, fileUID); // NOI18N
            }
        }
        return impl;
    }

    public CsmUID<CsmFile> getFileUID(CharSequence absPath, boolean treatSymlinkAsSeparateFile) {
        FileEntry f = getFileEntry(absPath, treatSymlinkAsSeparateFile, false);
        if (f == null) {
            return null;
        }
        return f.fileNew;
    }

    public void invalidatePreprocState(CharSequence absPath) {
        FileEntry f = getFileEntry(absPath, false, false);
        if (f == null){
            return;
        }
        synchronized (f) {
            f.invalidateStates();
        }
        if (TRACE_PP_STATE_OUT) {
            CharSequence path = getFileKey(absPath, false);
            System.err.println("%nInvalidated state for file" + path + "%n");
        }
    }

    public FileEntry getEntry(CharSequence absPath) {
        CndUtils.assertTrue(CndPathUtilities.isPathAbsolute(absPath), "Path should be absolute: ", absPath); //NOI18N
        return getFileEntry(absPath, false, false);
    }

    public Object getLock(CharSequence absPath) {
        FileEntry f = getFileEntry(absPath, false, false);
        return f == null ? lock : f.getLock();
    }

    public void debugClearState(){
        List<FileEntry> files;
        files = new ArrayList<>(myFiles.values());
        for (FileEntry file : files){
            synchronized (file.getLock()) {
                file.debugClearState();
            }
        }
	put();
    }

    public Collection<CsmFile> getFiles() {
	List<CsmUID<CsmFile>> uids = new ArrayList<>(myFiles.values().size());
	getFiles2(uids);
	return new LazyCsmCollection<>(uids, TraceFlags.SAFE_UID_ACCESS);
    }

    public Collection<CsmUID<CsmFile>> getFilesUID() {
        List<CsmUID<CsmFile>> uids = new ArrayList<>(myFiles.values().size());
        getFiles2(uids);
        return uids;
    }

    public Collection<FileImpl> getFileImpls() {
	List<CsmUID<CsmFile>> uids = new ArrayList<>(myFiles.values().size());
	getFiles2(uids);
	return new LazyCsmCollection<>(uids, TraceFlags.SAFE_UID_ACCESS);
    }

    private void getFiles2(List<CsmUID<CsmFile>> res) {
        List<FileEntry> files;
        files = new ArrayList<>(myFiles.values());
        for(FileEntry f : files){
            res.add(f.fileNew);
        }
    }

    public int getSize(){
        return myFiles.size();
    }

    @Override
    public void write(RepositoryDataOutput aStream) throws IOException {
	super.write(aStream);
        PersistentUtils.writeFileSystem(fileSystem, aStream);
	// maps are concurrent, so we don't need synchronization here
        writeFilePathsForFileSystemToFileEntryMap(fileSystem, aStream, myFiles);
        writeFilePathsForFileSystemToStringsArrMap(fileSystem, aStream, canonicFiles);
	//trace(canonicFiles, "Wrote in write()");
    }

    public static CharSequence getFileKey(CharSequence file, boolean sharedText) {
        return sharedText ? FilePathCache.getManager().getString(file) : DefaultCache.getManager().getString(file);
    }

    private CharSequence getAlternativeFileKey(CharSequence primaryKey) {
        Object out = canonicFiles.get(primaryKey);
        if (out instanceof CharSequence) {
            return (CharSequence)out;
        } else if (out != null) {
            assert ((CharSequence[])out).length >= 2;
            return ((CharSequence[])out)[0];
        }
        return null;
    }

    private FileEntry getFileEntry(CharSequence absPath, boolean treatSymlinkAsSeparateFile, boolean sharedText) {
        return getFileEntryImpl(getFileKey(absPath, sharedText), treatSymlinkAsSeparateFile);
    }

    /**
     * NB: path should be got via getFileKey!
     * to be called only from within getFileEntry
     */
    private FileEntry getFileEntryImpl(CharSequence path, boolean treatSymlinkAsSeparateFile) {
        FileEntry f = myFiles.get(path);
        if (f == null && (!treatSymlinkAsSeparateFile || !TraceFlags.SYMLINK_AS_OWN_FILE)) {
            // check alternative expecting that 'path' is canonical path
            CharSequence path2 = getAlternativeFileKey(path);
            f = (path2 == null) ? null : myFiles.get(path2);
            if (f != null) {
                if (TraceFlags.TRACE_CANONICAL_FIND_FILE) {
                    CndUtils.assertTrueInConsole(false, "alternative for " + path + " is " + path2); // NOI18N
                }
            }
        }
        return f;
    }

    private void addAlternativeFileKey(CharSequence primaryKey, CharSequence canonicKey) {
        Object out = canonicFiles.get(canonicKey);
        Object newVal;
        if (out == null) {
            newVal = primaryKey;
        } else {
            if (out instanceof CharSequence) {
                if (out.equals(primaryKey)) {
                    return;
                }
                newVal = new CharSequence[] {(CharSequence)out, primaryKey};
            } else {
                CharSequence[] oldAr = (CharSequence[])out;
                for(CharSequence what:oldAr){
                    if (what.equals(primaryKey)){
                        return;
                    }
                }
                CharSequence[] newAr = new CharSequence[oldAr.length + 1];
                System.arraycopy(oldAr, 0, newAr, 0, oldAr.length);
                newAr[oldAr.length] = primaryKey;
                newVal = newAr;
            }
        }
        canonicFiles.put(canonicKey, newVal);
        if (TraceFlags.TRACE_CANONICAL_FIND_FILE) {
            if (newVal instanceof CharSequence[]) {
                System.err.println("entry for " + canonicKey + " while adding " + primaryKey + " is " + Arrays.asList((CharSequence[])newVal).toString());
            } else {
                System.err.println("entry for " + canonicKey + " while adding " + primaryKey + " is " + newVal);
            }
        }
    }

    private void removeAlternativeFileKey(CharSequence canonicKey, CharSequence primaryKey) {
        Object out = canonicFiles.get(canonicKey);
        assert out != null : "no entry for " + canonicKey + " of " + primaryKey;
        Object newVal;
        if (out instanceof CharSequence) {
            assert primaryKey.equals(out) : " primaryKey " + primaryKey + " have to be " + out;
            newVal = null;
        } else {
            CharSequence[] oldAr = (CharSequence[])out;
            assert oldAr.length >= 2;
            if (oldAr.length == 2) {
                assert oldAr[0].equals(primaryKey) || oldAr[1].equals(primaryKey) : "no primaryKey " + primaryKey + " in " + Arrays.toString(oldAr);
                newVal = oldAr[0].equals(primaryKey) ? oldAr[1] : oldAr[0];
            } else {
                CharSequence[] newAr = new CharSequence[oldAr.length - 1];
                int k = 0;
                boolean found = false;
                for(CharSequence cur : oldAr){
                    if (!cur.equals(primaryKey)){
                        newAr[k++]=cur;
                    } else {
                        found = true;
                    }
                }
                assert found : " not found " + primaryKey + " in " + Arrays.toString(oldAr);
                newVal = newAr;
            }
        }
        if (newVal == null) {
            boolean removed = canonicFiles.remove(canonicKey, out);
            CndUtils.assertTrue(removed, "inconsistent state for ", primaryKey);
            if (TraceFlags.TRACE_CANONICAL_FIND_FILE) {
                System.err.println("removed entry for " + canonicKey + " while removing " + primaryKey);
            }
        } else {
            Object prevValue = canonicFiles.put(canonicKey, newVal);
            CndUtils.assertTrue(prevValue == out, "inconsistent state for ", primaryKey);
            if (TraceFlags.TRACE_CANONICAL_FIND_FILE) {
                System.err.println("change entry for " + canonicKey + " while removing " + primaryKey + " to " + newVal);
            }
        }
    }

    private void checkConsistency() {
        int valuesSize = 0;
        for (Map.Entry<CharSequence, Object> entry : this.canonicFiles.entrySet()) {
            Object value = entry.getValue();
            assert value != null;
            CharSequence[] absPaths;
            if (value instanceof CharSequence) {
                absPaths = new CharSequence[] { (CharSequence)value };
            } else {
                absPaths = (CharSequence[]) value;
            }
            valuesSize += absPaths.length;
            for (CharSequence path : absPaths) {
                if (!myFiles.containsKey(path)) {
                    CndUtils.assertTrueInConsole(false, "no Entry for registered absPath " + path + " with canonical " + entry.getKey());
                }
            }
        }
        assert valuesSize == myFiles.size() : "different number of elements " + myFiles.size() + " vs " + valuesSize;
    }

    /*package*/ static void writeFilePathsForFileSystemToFileEntryMap (FileSystem fs,
            final RepositoryDataOutput output, Map<CharSequence, FileEntry> aMap) throws IOException {
        assert output != null;
        assert aMap != null;
        int size = aMap.size();

        //write size
        output.writeInt(size);

        // write the map
        final Set<Map.Entry<CharSequence, FileEntry>> entrySet = aMap.entrySet();
        final Iterator <Map.Entry<CharSequence, FileEntry>> setIterator = entrySet.iterator();
        while (setIterator.hasNext()) {
            final Map.Entry<CharSequence, FileEntry> anEntry = setIterator.next();
            output.writeFilePathForFileSystem(fs, anEntry.getKey());
            // TODO: replace call above by the next line if no failures in following asserts
            // it allows to eleminate charSeq->fileIndex conversion when fileIndex is
            // known from fileNew UID kept in value's FileEntry
//            output.writeInt(UIDUtilities.getFileID(anEntry.getValue().fileNew));
            assert anEntry.getValue() != null;
            anEntry.getValue().write(output);
        }
    }

    /*package*/ static void  readFilePathsForFileSystemToFileEntryMap(FileSystem fs,
            RepositoryDataInput input, Map<CharSequence, FileEntry> aMap) throws IOException {

        assert input != null;
        assert aMap != null;

        aMap.clear();
        final int size = input.readInt();

        for (int i = 0; i < size; i++) {
            CharSequence key = input.readFilePathForFileSystem(fs);
            FileEntry value = new FileEntry(input);

            assert key != null;
            assert value != null;

            aMap.put(key, value);
        }
    }

    private static void writeFilePathsForFileSystemToStringsArrMap (FileSystem fs,
            final RepositoryDataOutput output,
            final Map<CharSequence, Object/*CharSequence or CharSequence[]*/> aMap) throws IOException {

        assert output != null;
        assert aMap != null;

        final int size = aMap.size();
        output.writeInt(size);

        final Set<Map.Entry<CharSequence, Object>> entrySet = aMap.entrySet();
        final Iterator<Map.Entry<CharSequence, Object>> setIterator = entrySet.iterator();

        while (setIterator.hasNext()) {
            final Map.Entry<CharSequence, Object> anEntry = setIterator.next();
            assert anEntry != null;

            final CharSequence key = anEntry.getKey();
            final Object value = anEntry.getValue();
            assert key != null;
            assert value != null;
            assert ((value instanceof CharSequence) || (value instanceof CharSequence[]));

            output.writeFilePathForFileSystem(fs, key);

            if (value instanceof CharSequence ) {
                output.writeInt(1);
                output.writeFilePathForFileSystem(fs, (CharSequence)value);
            } else if (value instanceof CharSequence[]) {

                final CharSequence[] array = (CharSequence[]) value;

                output.writeInt(array.length);
                for (int j = 0; j < array.length; j++) {
                    output.writeFilePathForFileSystem(fs, array[j]);
                }
            }
        }
    }

    private static void readFilePathsForFileSystemToStringsArrMap(FileSystem fs,
            final RepositoryDataInput input, Map<CharSequence,
                    Object/*CharSequence or CharSequence[]*/> aMap) throws IOException {
        assert input != null;
        assert aMap != null;

        aMap.clear();

        final int size = input.readInt();

        for (int i = 0; i < size; i++) {
            CharSequence key = input.readFilePathForFileSystem(fs);
            assert key != null;

            final int arraySize = input.readInt();
            assert arraySize != 0;

            if (arraySize == 1) {
                aMap.put(key, input.readFilePathForFileSystem(fs));
            } else {
                final CharSequence[] value = new CharSequence[arraySize];
                for (int j = 0; j < arraySize; j++) {
                    CharSequence path = input.readFilePathForFileSystem(fs);
                    assert path != null;

                    value[j] = path;
                }
                aMap.put(key, value);
            }
        }
    }

    //for unit test only
    public Map<CharSequence, FileEntry> getFileStorage() {
        return new TreeMap<>(myFiles);
    }

    //for unit test only
    public Map<CharSequence, Object/*CharSequence or CharSequence[]*/> getCanonicalNames(){
        return new TreeMap<>(canonicFiles);
    }

    public static FileEntry createFileEntryForMerge(FileSystem fs, CharSequence fileKey) {
        return new FileEntry(null, null, fileKey, fs, fileKey);
    }

    /*package*/ static FileEntry createFileEntry(FileImpl fileImpl) {
        CharSequence path = getFileKey(fileImpl.getAbsolutePath(), false);
        return new FileEntry(fileImpl.getUID(), null, path, fileImpl.getFileSystem(), path);
    }
    private static final boolean TRACE = false;
    public static final class FileEntry {

        private final CsmUID<CsmFile> fileNew;
        private final FileSystem fs;
        private final CharSequence canonical;
        private volatile Object data; // either StatePair or List<StatePair>
        private volatile int modCount;

        @SuppressWarnings("unchecked")
        private FileEntry (RepositoryDataInput input) throws IOException {
            fileNew = UIDObjectFactory.getDefaultFactory().readUID(input);
            fs = input.readFileSystem();
            canonical = input.readFilePathForFileSystem(fs);
            modCount = input.readInt();
            if (input.readBoolean()) {
                int cnt = input.readInt();
                assert cnt > 0;
                if (cnt == 1) {
                    PreprocessorStatePair pair = readStatePair(input);
                    if (TRACE && pair != null && !pair.state.isValid()) {
                        CndUtils.assertTrueInConsole(false, "read INVALID state for ", this.canonical);
                    }
                    data = pair;
                } else {
                    data = new ArrayList<>(cnt);
                    for (int i = 0; i < cnt; i++) {
                        PreprocessorStatePair pair = readStatePair(input);
                        if (TRACE && pair != null && !pair.state.isValid()) {
                            CndUtils.assertTrueInConsole(false, "read INVALID state for ", this.canonical);
                        }
                        ((List<PreprocessorStatePair>) data).add(pair);
                    }
                }
            } else {
                data = null;
            }
        }

        private FileEntry(CsmUID<CsmFile> fileNew, PreprocHandler.State state, CharSequence fileKey, FileSystem fs, CharSequence canonicalFileKey) {
            this.fileNew = fileNew;
            this.data = (state == null) ? null : new PreprocessorStatePair(state, FilePreprocessorConditionState.PARSING);
//            if (state == null) {
//                if (CndUtils.isDebugMode()) {
//                    CndUtils.assertTrueInConsole(false, "creating null based entry for " + fileKey); // NOI18N
//                }
//            }
            this.fs = fs;
            this.canonical = canonicalFileKey;
            this.modCount = 0;
        }

        private void write(final RepositoryDataOutput output) throws IOException {
            UIDObjectFactory.getDefaultFactory().writeUID(fileNew, output);
            output.writeFileSystem(fs);
            output.writeFilePathForFileSystem(fs, canonical);
            output.writeInt(modCount);
            Object aData = data;
            output.writeBoolean(aData != null);
            if (aData != null) {
                if(aData instanceof PreprocessorStatePair) {
                    output.writeInt(1);
                    PreprocessorStatePair pair = (PreprocessorStatePair) aData;
                    if (TRACE && !pair.state.isValid()) {
                        CndUtils.assertTrueInConsole(false, "write INVALID state for ", this.canonical);
                    }
                    writeStatePair(output, pair);
                } else {
                    @SuppressWarnings("unchecked")
                    Collection<PreprocessorStatePair> pairs = (Collection<PreprocessorStatePair>)aData;
                    output.writeInt(pairs.size());
                    for (PreprocessorStatePair pair : pairs) {
                        if (TRACE && pair != null && !pair.state.isValid()) {
                            CndUtils.assertTrueInConsole(false, "write INVALID state for ", this.canonical);
                        }
                        writeStatePair(output, pair);
                    }
                }
            }
        }

        private static PreprocessorStatePair readStatePair(RepositoryDataInput input) throws IOException {
            if (input.readBoolean()) {
                PreprocHandler.State state = null;
                if (input.readBoolean()){
                    state = PersistentUtils.readPreprocState(input);
                }
                FilePreprocessorConditionState pcState = null;
                if (input.readBoolean()){
                    pcState = new FilePreprocessorConditionState(input);
                } else {
                    pcState = FilePreprocessorConditionState.PARSING;
                }
                return new PreprocessorStatePair(state, pcState);
            }
            return null;


        }

        private static void writeStatePair(RepositoryDataOutput output, PreprocessorStatePair pair) throws IOException {
            output.writeBoolean(pair != null);
            if (pair != null) {
                output.writeBoolean(pair.state != null);
                if (pair.state != null) {
                    PersistentUtils.writePreprocState(pair.state, output);
                }
                output.writeBoolean(pair.pcState != FilePreprocessorConditionState.PARSING);
                if (pair.pcState != FilePreprocessorConditionState.PARSING) {
                    pair.pcState.write(output);
                }
            }
        }

        public final synchronized int getModCount() {
            return modCount;
        }

        /**
         * @return lock under which all sequances read-decide-modify should be done
         * get* and replace* methods are synchronize on the same lock
         */
        public Object getLock() {
            return this;
        }

        /*tests-only*/public synchronized void debugClearState() {
            data = null;
        }

        /**
         * This should only be called if we are sure this is THE ONLY correct state:
         * e.g., when creating new file, when invalidating state of a *source* (not a header) file, etc
         */
        public final synchronized void setState(PreprocHandler.State state, FilePreprocessorConditionState pcState) {
            if (TraceFlags.TRACE_182342_BUG) {
                new Exception("setState replacing:\n" + toString()).printStackTrace(System.err); //NOI18N
            }
            State oldState = null;
            if( state != null && ! state.isCleaned() ) {
                state = APTHandlersSupport.createCleanPreprocState(state);
            }
            if ((data instanceof Collection<?>)) {
                @SuppressWarnings("unchecked")
                Collection<PreprocessorStatePair> states = (Collection<PreprocessorStatePair>) data;
                // check how many good old states are there
                // and pick a valid one to check that we aren't replacing better state by worse one
                int oldGoodStatesCount = 0;
                for (PreprocessorStatePair pair : states) {
                    if (pair.state != null && pair.state.isValid()) {
                        oldGoodStatesCount++;
                        if (oldState == null || state.isCompileContext()) {
                            oldState = state;
                        }
                    }
                }
                if (oldGoodStatesCount > 1) {
                    if (CndUtils.isDebugMode()) {
                        StringBuilder sb = new StringBuilder("Attempt to set state while there are multiple states: " + canonical); // NOI18N
                        for (PreprocessorStatePair pair : states) {
                            sb.append(String.format("%nvalid: %b context: %b %s", pair.state.isValid(), pair.state.isCompileContext(), pair.pcState)); //NOI18N
                        }
                        Utils.LOG.log(Level.SEVERE, sb.toString(), new Exception(sb.toString()));
                    }
                    //return;
                }
            } else if(data instanceof PreprocessorStatePair) {
                oldState = ((PreprocessorStatePair) data).state;
            }

            incrementModCount();

            if (oldState != null
                    && oldState.isValid()
                    && oldState.isCompileContext()
                    && !state.isCompileContext()) {
                if (CndUtils.isDebugMode()) {
                    String message = "Replacing correct state to incorrect " + canonical; // NOI18N
                    Utils.LOG.log(Level.SEVERE, message, new Exception());
                }
                return;
            }

            if (TRACE_PP_STATE_OUT) {
                System.err.println("%nPut state for file" + canonical + "%n");
                System.err.println(state);
            }

            data = new PreprocessorStatePair(state, pcState);
            if (TraceFlags.TRACE_182342_BUG) {
                new Exception("setState at the end:\n"+toString()).printStackTrace(System.err); //NOI18N
            }
        }

        public synchronized void setStates(Collection<PreprocessorStatePair> pairs, PreprocessorStatePair yetOneMore) {
            if (TraceFlags.TRACE_182342_BUG) {
                new Exception("setStates replacing:\n" + toString()).printStackTrace(System.err); //NOI18N
            }
            incrementModCount();
            if (yetOneMore != null && yetOneMore.state != null && !yetOneMore.state.isCleaned()) {
                yetOneMore = new PreprocessorStatePair(APTHandlersSupport.createCleanPreprocState(yetOneMore.state), yetOneMore.pcState);
            }
            // to save memory consumption
            if (yetOneMore == null && pairs.size() == 1) {
                yetOneMore = pairs.iterator().next();
                pairs = Collections.<PreprocessorStatePair>emptyList();
            }
            if (pairs.isEmpty()) {
                assert yetOneMore != null;
                data = yetOneMore;
            } else {
                ArrayList<PreprocessorStatePair> newData = new ArrayList<>(pairs.size()+1);
                newData.addAll(pairs);
                if (yetOneMore != null) {
                    newData.add(yetOneMore);
                }
                if (TraceFlags.DYNAMIC_TESTS_TRACE) {
                    for (int i = 0; i < newData.size(); i++) {
                        PreprocessorStatePair first = newData.get(i);
                        for (int j = i + 1; j < newData.size(); j++) {
                            PreprocessorStatePair second = newData.get(j);
                            if (first.pcState == FilePreprocessorConditionState.PARSING
                                    || second.pcState == FilePreprocessorConditionState.PARSING) {
                                if (APTHandlersSupport.equalsIgnoreInvalid(first.state, second.state)) {
                                    new Exception("setStates :\n" + newData).printStackTrace(System.err); //NOI18N
                                }
                            }
                        }
                    }
                }
                data = newData;
            }
            if (CndUtils.isDebugMode()) {
                checkConsistency();
            }
            if (TraceFlags.TRACE_182342_BUG) {
                new Exception("setStates at the end:\n"+toString()).printStackTrace(System.err); //NOI18N
            }
        }

        private void checkConsistency() {
            Collection<PreprocessorStatePair> pairs = getStatePairs();
            if (!pairs.isEmpty()) {
                boolean alarm = false;

                State firstState = null;
                FilePreprocessorConditionState firstPCState = null;
                boolean first = true;
                for (PreprocessorStatePair pair : getStatePairs()) {
                    if (first) {
                        first = false;
                        firstState = pair.state;
                        firstPCState = pair.pcState;
                    } else {
                        if ((firstState == null) != (pair.state == null)) {
                            alarm = true;
                            break;
                        }
                        if (firstState != null) {
                            if (firstState.isCompileContext() != pair.state.isCompileContext()) {
                                alarm = true;
                                break;
                            }
                            if (firstState.isValid() != pair.state.isValid()) {
                                // there coud be invalidated state which is in parsing phase now
                                alarm = !firstState.isValid() && (firstPCState != FilePreprocessorConditionState.PARSING);
                                alarm |= !pair.state.isValid() && (pair.pcState != FilePreprocessorConditionState.PARSING);
                                if (alarm) {
                                    break;
                                }
                            }
                        }
                    }
                }
                if (alarm) {
                    StringBuilder sb = new StringBuilder("Mixed preprocessor states: " + canonical); // NOI18N
                    for (PreprocessorStatePair pair : getStatePairs()) {
                        if (pair.state == null) {
                            sb.append(String.format(" (null, %s)", pair.pcState)); //NOI18N
                        } else {
                            sb.append(String.format(" (valid: %b, context: %b, %s) ", //NOI18N
                                    pair.state.isValid(), pair.state.isCompileContext(), pair.pcState));
                        }
                    }
                    Utils.LOG.log(Level.INFO, sb.toString(), new Exception(sb.toString()));
                }
            }
        }

        private synchronized void incrementModCount() {
            modCount = (modCount == Integer.MAX_VALUE) ? 0 : modCount+1;
        }

        @SuppressWarnings("unchecked")
        public synchronized void invalidateStates() {
            incrementModCount();
            if (data != null) {
                if (data instanceof PreprocessorStatePair) {
                    data = createInvalidState((PreprocessorStatePair) data);
                } else {
                    Collection<PreprocessorStatePair> newData = new ArrayList<>();
                    for (PreprocessorStatePair pair : (Collection<PreprocessorStatePair>) data) {
                        newData.add(createInvalidState(pair));
                    }
                    data = newData;
                }
            }
            if (TraceFlags.TRACE_182342_BUG) {
                new Exception("invalidateStates:\n"+toString()).printStackTrace(System.err); //NOI18N
            }
        }

        @SuppressWarnings("unchecked")
        public synchronized void markAsParsingPreprocStates() {
            incrementModCount();
            if (data != null) {
                if (data instanceof PreprocessorStatePair) {
                    data = createMarkedAsParsingState((PreprocessorStatePair) data);
                } else {
                    Collection<PreprocessorStatePair> newData = new ArrayList<>();
                    for (PreprocessorStatePair pair : (Collection<PreprocessorStatePair>) data) {
                        newData.add(createMarkedAsParsingState(pair));
                    }
                    data = newData;
                }
            }
            if (TraceFlags.TRACE_182342_BUG) {
                new Exception("invalidateStates:\n" + toString()).printStackTrace(System.err); //NOI18N
            }
        }

        private static PreprocessorStatePair createMarkedAsParsingState(PreprocessorStatePair pair) {
            if (pair == null) {
                return pair;
            } else {
                if (pair.state == null) {
                    return pair;
                } else {
                    return new PreprocessorStatePair(pair.state, FilePreprocessorConditionState.PARSING);
                }
            }
        }

        private static PreprocessorStatePair createInvalidState(PreprocessorStatePair pair) {
            if (pair == null) {
                return pair;
            } else {
                if (pair.state == null) {
                    return pair;
                } else {
                    return new PreprocessorStatePair(APTHandlersSupport.createInvalidPreprocState(pair.state), pair.pcState);
                }
            }
        }

        public synchronized Collection<PreprocessorStatePair> getStatePairs() {
            if (data == null) {
                return Collections.<PreprocessorStatePair>emptyList();
            } else if(data instanceof PreprocessorStatePair) {
                return Collections.singleton((PreprocessorStatePair) data);
            } else {
                @SuppressWarnings("unchecked")
                Collection<PreprocessorStatePair> array = (Collection<PreprocessorStatePair>) data;
                return new ArrayList<>(array);
            }
        }
        
        public synchronized Collection<PreprocHandler.State> getPrerocStates() {
            if (data == null) {
                return Collections.emptyList();
            } else if(data instanceof PreprocessorStatePair) {
                return Collections.singleton(((PreprocessorStatePair) data).state);
            } else {
                @SuppressWarnings("unchecked")
                Collection<PreprocessorStatePair> pairs = (Collection<PreprocessorStatePair>) data;
                Collection<PreprocHandler.State> result = new ArrayList<>(pairs.size());
                for (PreprocessorStatePair pair : pairs) {
                    result.add(pair.state);
                }
                return result;
            }
        }

        //for unit test only
        public CsmUID<CsmFile> getTestFileUID(){
            return fileNew;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(fileNew);
            sb.append("\nstates:\n"); //NOI18N
            for (PreprocessorStatePair pair : getStatePairs()) {
                sb.append(pair);
                sb.append('\n'); //NOI18N
            }
            return sb.toString();
        }

    }

    private CharSequence getCanonicalKey(CharSequence fileKey) {
        try {
            CharSequence res = CndFileSystemProvider.getCanonicalPath(fileSystem, fileKey);
            res = FilePathCache.getManager().getString(res);
            if (fileKey.equals(res)) {
                return fileKey;
            }
            return res;
        } catch (IOException e) {
            // skip exception
            return fileKey;
        }
    }

    private static CharSequence getCanonicalKey(FileObject fileObject, CharSequence fileKey) {
        try {
            CharSequence res = CndFileUtils.getCanonicalPath(fileObject);
            res = FilePathCache.getManager().getString(res);
            if (fileKey.equals(res)) {
                return fileKey;
            }
            return res;
        } catch (IOException e) {
            // skip exception
            return fileKey;
        }
    }

}
