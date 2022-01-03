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
package org.netbeans.modules.cnd.apt.impl.support.clank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.support.APTFileSearch;
import org.netbeans.modules.cnd.apt.support.ClankDriver;
import org.netbeans.modules.cnd.apt.support.IncludeDirEntry;
import org.netbeans.modules.cnd.apt.support.api.PPIncludeHandler;
import org.netbeans.modules.cnd.apt.support.api.StartEntry;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.CharSequences;
import org.openide.util.Parameters;

/**
 * implementation of include handler based on clank's Preprocessor
 *
 */
public class ClankIncludeHandlerImpl implements PPIncludeHandler {

    private List<IncludeDirEntry> systemIncludePaths;
    private List<IncludeDirEntry> userIncludePaths;
    private List<IncludeDirEntry> userIncludeFilePaths;

    private StartEntry startFile;
    private final APTFileSearch fileSearch;
    private static final ClankDriverImpl.ClankPreprocessorOutputImplementation NO_PP_CONTENT = new ClankPPOutputImpl(-1);

    private int inclStackIndex;
    private ClankDriverImpl.ClankPreprocessorOutputImplementation cachedContent = NO_PP_CONTENT;
    private LinkedList<IncludeInfo> inclStack = null;

    public ClankIncludeHandlerImpl(StartEntry startFile) {
        this(startFile, new ArrayList<IncludeDirEntry>(0), new ArrayList<IncludeDirEntry>(0), new ArrayList<IncludeDirEntry>(0), startFile.getFileSearch());
    }

    public ClankIncludeHandlerImpl(StartEntry startFile,
            List<IncludeDirEntry> systemIncludePaths,
            List<IncludeDirEntry> userIncludePaths,
            List<IncludeDirEntry> userIncludeFilePaths, APTFileSearch fileSearch) {
        assert APTTraceFlags.USE_CLANK;
        Parameters.notNull("startFile", startFile);
        this.startFile = startFile;
        this.systemIncludePaths = systemIncludePaths;
        this.userIncludePaths = userIncludePaths;
        this.userIncludeFilePaths = userIncludeFilePaths;
        this.fileSearch = fileSearch;
        this.inclStackIndex = 0;
    }

    @Override
    public IncludeState pushInclude(FileSystem fs, CharSequence path, int line, int offset, int resolvedDirIndex, int inclDirIndex) {
        return pushIncludeImpl(fs, path, line, offset, resolvedDirIndex, inclDirIndex);
    }

    @Override
    public CharSequence popInclude() {
        return popIncludeImpl();
    }

    @Override
    public StartEntry getStartEntry() {
        return startFile;
    }

    private CharSequence getCurPath() {
        assert (inclStack != null);
        IncludeInfo info = inclStack.getLast();
        return info.getIncludedPath();
    }

    /*package*/
    void resetIncludeStack() {
        this.inclStack = null;
        this.inclStackIndex = 0;
    }
    ////////////////////////////////////////////////////////////////////////////
    // manage state (save/restore)

    @Override
    public State getState() {
        return createStateImpl();
    }

    @Override
    public void setState(State state) {
        if (state instanceof StateImpl) {
            StateImpl stateImpl = ((StateImpl)state);
            assert APTTraceFlags.USE_CLANK;
            // can be cleaned in Clank mode
            // assert !stateImpl.isCleaned();
            stateImpl.restoreTo(this);
        }
    }

    private StateImpl createStateImpl() {
        return new StateImpl(this);
    }

    public List<IncludeDirEntry> getUserIncludeFilePaths() {
        return Collections.unmodifiableList(userIncludeFilePaths);
    }

    public List<IncludeDirEntry> getUserIncludePaths() {
        return Collections.unmodifiableList(userIncludePaths);
    }

    public List<IncludeDirEntry> getSystemIncludePaths() {
        return Collections.unmodifiableList(systemIncludePaths);
    }

    public boolean isFirstLevel() {
        return inclStack == null || inclStack.isEmpty();
    }

    public ClankDriverImpl.ClankPreprocessorOutputImplementation getPreprocessorOutputImplementation() {
        if (cachedContent == NO_PP_CONTENT) {
            return new ClankPPOutputImpl(inclStackIndex);
        } else {
            return cachedContent;
        }
    }

    public int getInclStackIndex() {
        return inclStackIndex;
    }

    void cachePreprocessorOutputImplementation(ClankDriverImpl.ClankPreprocessorOutputImplementation cache) {
        assert cache != null;
        if (!cache.hasTokenStream()) {
            this.cachedContent = NO_PP_CONTENT;
        } else {
            this.cachedContent = cache;
        }
        this.inclStackIndex = cache.getFileIndex();
    }

    APTFileSearch getFileSearch() {
        return this.fileSearch;
    }

    /**
     * immutable state object of include handler
     */
    // Not SelfPersistent any more because I have to pass unitIndex into write() method
    // It is private, so I don't think it's a problem. VK.
    public final static class StateImpl implements State, Persistent {

        private static final List<IncludeDirEntry> CLEANED_MARKER = Collections.unmodifiableList(new ArrayList<IncludeDirEntry>(0));
        // for now just remember lists
        private final List<IncludeDirEntry> systemIncludePaths;
        private final List<IncludeDirEntry> userIncludePaths;
        private final List<IncludeDirEntry> userIncludeFilePaths;
        private final StartEntry startFile;

        private final int inclStackIndex;
        private final ClankDriverImpl.ClankPreprocessorOutputImplementation cachedContent;
        private static final IncludeInfo[] EMPTY_STACK = new IncludeInfo[0];
        private final IncludeInfo[] inclStack;
        private int hashCode = 0;
        private final boolean cleaned;
        private final boolean alreadyTriedCachePreparation;

        protected StateImpl(ClankIncludeHandlerImpl handler) {
            this.systemIncludePaths = handler.systemIncludePaths;
            this.userIncludePaths = handler.userIncludePaths;
            this.userIncludeFilePaths = handler.userIncludeFilePaths;
            this.startFile = handler.startFile;

            if (handler.inclStack != null && !handler.inclStack.isEmpty()) {
                this.inclStack = handler.inclStack.toArray(new IncludeInfo[handler.inclStack.size()]);
            } else {
                this.inclStack = EMPTY_STACK;
            }

            this.inclStackIndex = handler.inclStackIndex;
            this.cachedContent = handler.cachedContent;
            this.cleaned = false;
            this.alreadyTriedCachePreparation = false;
            assert this.cachedContent != null;
        }

        private StateImpl(StateImpl other, boolean cleanState, boolean prepareCachesIfPossible) {
            assert cleanState == true || prepareCachesIfPossible == true;
            // shared information
            this.startFile = other.startFile;

            // state object is immutable => safe to share stacks
            this.inclStack = other.inclStack;

            this.inclStackIndex = other.inclStackIndex;
            this.cleaned = cleanState;
            if (cleanState) {
                this.systemIncludePaths = CLEANED_MARKER;
                this.userIncludePaths = CLEANED_MARKER;
                this.userIncludeFilePaths = CLEANED_MARKER;
            } else {
                this.systemIncludePaths = other.systemIncludePaths;
                this.userIncludePaths = other.userIncludePaths;
                this.userIncludeFilePaths = other.userIncludeFilePaths;
            }
            // handle cached information
            if (cleanState) {
                this.alreadyTriedCachePreparation = true;
                this.cachedContent = NO_PP_CONTENT;
            } else if (prepareCachesIfPossible) {
                this.alreadyTriedCachePreparation = true;
                this.cachedContent = other.cachedContent.prepareCachesIfPossible();
            } else {
                this.alreadyTriedCachePreparation = other.alreadyTriedCachePreparation;
                this.cachedContent = other.cachedContent;
            }
            assert this.cachedContent != null;
        }

        public int getIncludeStackDepth() {
            return inclStack.length;
        }

        private void restoreTo(ClankIncludeHandlerImpl handler) {
            // rewrite by what we kept, otherwise start entry
            // was responsible to initialize paths correctly
            if (this.cleaned) {
                // keep all settings initialized in handler as is
                // do not clear them by own cleared empty collections
                assert areStartEntriesEqual(handler.startFile, this.startFile) :
                        "handler's startFile " + handler.startFile + // NOI18N
                        " vs. state startFile " + this.startFile; // NOI18N
            } else {
                handler.userIncludePaths = this.userIncludePaths;
                handler.userIncludeFilePaths = this.userIncludeFilePaths;
                handler.systemIncludePaths = this.systemIncludePaths;
                handler.startFile = this.startFile;
            }

            // TODO: if requested we restore (it is different from APT-based state)
            if (this.inclStack.length > 0) {
                // out stack is valid only in the context of our startFile
                handler.startFile = this.startFile;
                handler.inclStack = new LinkedList<IncludeInfo>();
                handler.inclStack.addAll(Arrays.asList(this.inclStack));
//                    if (CHECK_INCLUDE_DEPTH < 0) {
//                        handler.recurseIncludes = new HashMap<CharSequence, Integer>();
//                        for (IncludeInfo includeInfo : this.inclStack) {
//                            CharSequence path = includeInfo.getIncludedPath();
//                            Integer counter = handler.recurseIncludes.get(path);
//                            counter = (counter == null) ? Integer.valueOf(1) : Integer.valueOf(counter.intValue() + 1);
//                            handler.recurseIncludes.put(path, counter);
//                        }
//                    }
            }
            handler.inclStackIndex = this.inclStackIndex;
            handler.cachedContent = this.cachedContent;
        }
        
        private static boolean areStartEntriesEqual(StartEntry first, StartEntry second) {
            try {
                if (first.equals(second)) {
                    return true;
                }
                FSPath fsFirst = new FSPath(first.getFileSystem(), first.getStartFile().toString());
                FSPath fsSecond = new FSPath(second.getFileSystem(), second.getStartFile().toString());
                if (!fsFirst.getFileObject().getCanonicalFileObject().equals(fsSecond.getFileObject().getCanonicalFileObject())) {
                    return false;
                }
            } catch (Throwable ex) {
                // do nothing
            }
            return true;
        }

        @Override
        public String toString() {
            return ClankIncludeHandlerImpl.toString(startFile.getStartFile(),
                    systemIncludePaths, userIncludePaths, userIncludeFilePaths,
                    inclStackIndex, Arrays.asList(inclStack), cachedContent);
        }

        public void write(RepositoryDataOutput output) throws IOException {
            assert output != null;
            startFile.write(output);

            output.writeInt(inclStackIndex);

            output.writeInt(inclStack.length);
            for (IncludeInfo inclInfo : inclStack) {
                assert inclInfo != null;
                final IncludeInfoImpl inclInfoImpl;
                if (inclInfo instanceof IncludeInfoImpl) {
                    inclInfoImpl = (IncludeInfoImpl)inclInfo;
                } else {
                    inclInfoImpl = new IncludeInfoImpl(
                            inclInfo.getFileSystem(),
                            inclInfo.getIncludedPath(),
                            inclInfo.getIncludeDirectiveLine(),
                            inclInfo.getIncludeDirectiveOffset(),
                            inclInfo.getResolvedDirectoryIndex(),
                            inclInfo.getIncludeDirectiveIndex());
                }
                assert inclInfoImpl != null;
                inclInfoImpl.write(output);
            }
        }

        public StateImpl(final RepositoryDataInput input) throws IOException {
            assert input != null;

            startFile = new StartEntry(input);

            // read state is always clean
            this.cleaned = true;
            this.alreadyTriedCachePreparation = true;
            this.systemIncludePaths = CLEANED_MARKER;
            this.userIncludePaths = CLEANED_MARKER;
            this.userIncludeFilePaths = CLEANED_MARKER;

            inclStackIndex = input.readInt();
            cachedContent = NO_PP_CONTENT;
            assert this.cachedContent != null;

            int size = input.readInt();

            if (size == 0) {
                inclStack = EMPTY_STACK;
            } else {
                inclStack = new IncludeInfo[size];
                for (int i = 0; i < size; i++) {
                    final IncludeInfo impl = new IncludeInfoImpl(input);
                    assert impl != null;
                    inclStack[i] = impl;
                }
            }
        }

        public final StartEntry getStartEntry() {
            return startFile;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || (obj.getClass() != this.getClass())) {
                return false;
            }
            StateImpl other = (StateImpl)obj;
            return this.startFile.equals(other.startFile)
                    && (this.inclStackIndex == other.inclStackIndex)
                    && compareStacks(this.inclStack, other.inclStack);
        }

        @Override
        public int hashCode() {
            int hash = hashCode;
            if (hash == 0) {
                hash = 5;
                hash = 67 * hash + (this.startFile != null ? this.startFile.hashCode() : 0);
                hash = 67 * hash + this.inclStackIndex;
                hashCode = hash;
            }
            return hash;
        }

        private boolean compareStacks(IncludeInfo[] inclStack1, IncludeInfo[] inclStack2) {
            if (inclStack1 == inclStack2) {
                return true;
            }
            if (inclStack1.length != inclStack2.length) {
                return false;
            }
            for (int i = 0; i < inclStack1.length; i++) {
                IncludeInfo cur1 = inclStack1[i];
                IncludeInfo cur2 = inclStack2[i];
                if (!cur1.equals(cur2)) {
                    return false;
                }
            }
            return true;
        }

        public Collection<IncludeInfo> getIncludeStack() {
            return Arrays.asList(this.inclStack);
        }

//        public  boolean isCleaned() {
//            assert false : "must not be used, because it doesn't have any logic";
//            return this.cleaned;
//        }
        public ClankIncludeHandlerImpl.State copyCleaned() {
            return this.cleaned ? this : new StateImpl(this, true, false);
        }

        public ClankIncludeHandlerImpl.State prepareCachesIfPossible() {
            return this.alreadyTriedCachePreparation ? this : new StateImpl(this, this.cleaned, true);
        }

        public List<IncludeDirEntry> getSysIncludePaths() {
            return this.systemIncludePaths;
        }

        public List<IncludeDirEntry> getUserIncludePaths() {
            return this.userIncludePaths;
        }

        public List<IncludeDirEntry> getUserIncludeFilePaths() {
            return this.userIncludeFilePaths;
        }

        public int getIncludeStackIndex() {
            return this.inclStackIndex;
        }
    }

    private IncludeState pushIncludeImpl(FileSystem fs, CharSequence path, int directiveLine, int directiveOffset, int resolvedDirIndex, int inclDirIndex) {
        assert CharSequences.isCompact(path) : "must be char sequence key " + path; // NOI18N
        boolean okToPush = true;
//        if (CHECK_INCLUDE_DEPTH > 0) {
//            // variant without hash map
//            if (inclStack == null) {
//                inclStack = new LinkedList<IncludeInfo>();
//            }
//            if (inclStack.size() > CHECK_INCLUDE_DEPTH) {
//                APTUtils.LOG.log(Level.WARNING, "Deep inclusion:{0} in {1} on level {2}", new Object[] { path , getCurPath() , inclStack.size() }); // NOI18N
//                // check recurse inclusion
//                int counter = 0;
//                for (IncludeInfo includeInfo : inclStack) {
//                    if (includeInfo.getIncludedPath().equals(path)) {
//                        counter++;
//                        if (counter > MAX_INCLUDE_FILE_DEEP) {
//                            okToPush = false;
//                            break;
//                        }
//                    }
//                }
//            }
//        } else {
//            // variant with old hash map
//            if (recurseIncludes == null) {
//                assert (inclStack == null) : inclStack.toString() + " started on " + startFile;
//                inclStack = new LinkedList<IncludeInfo>();
//                recurseIncludes = new HashMap<CharSequence, Integer>();
//            }
//            Integer counter = recurseIncludes.get(path);
//            counter = (counter == null) ? Integer.valueOf(1) : Integer.valueOf(counter.intValue() + 1);
//            if (counter.intValue() < MAX_INCLUDE_FILE_DEEP) {
//                recurseIncludes.put(path, counter);
//            } else {
//                okToPush = false;
//            }
//        }
        if (inclStack == null) {
            inclStack = new LinkedList<IncludeInfo>();
        }
        if (okToPush) {
            inclStack.addLast(new IncludeInfoImpl(fs, path, directiveLine, directiveOffset, resolvedDirIndex, inclDirIndex));
            return IncludeState.Success;
        } else {
            APTUtils.LOG.log(Level.WARNING, "RECURSIVE inclusion:\n\t{0}\n\tin {1}\n", new Object[]{path, getCurPath()}); // NOI18N
            return IncludeState.Recursive;
        }
    }

    private static final class IncludeInfoImpl implements IncludeInfo, SelfPersistent {

        private final FileSystem fs;
        private final CharSequence path;
        private final int directiveLine;
        private final int directiveOffset;
        private final int resolvedDirectoryIndex;
        private final int includeDirectiveIndex;

        public IncludeInfoImpl(FileSystem fs, CharSequence path, int directiveLine, int directiveOffset, int resolvedDirectoryIndex, int includeDirectiveIndex) {
            assert path != null;
            this.fs = fs;
            this.path = path;
            // in case of -include file we have negative line/offset
            assert directiveLine >= 0 || (directiveLine < 0 && directiveOffset < 0);
            this.directiveLine = directiveLine;
            this.directiveOffset = directiveOffset;
            this.resolvedDirectoryIndex = resolvedDirectoryIndex;
            this.includeDirectiveIndex = includeDirectiveIndex;
        }

        public IncludeInfoImpl(final RepositoryDataInput input) throws IOException {
            assert input != null;
            this.fs = input.readFileSystem();
            this.path = input.readFilePathForFileSystem(fs);
            directiveLine = input.readInt();
            directiveOffset = input.readInt();
            resolvedDirectoryIndex = input.readInt();
            includeDirectiveIndex = input.readInt();
        }

        @Override
        public FileSystem getFileSystem() {
            return fs;
        }

        @Override
        public CharSequence getIncludedPath() {
            return path;
        }

        @Override
        public int getIncludeDirectiveLine() {
            return directiveLine;
        }

        @Override
        public int getIncludeDirectiveOffset() {
            return directiveOffset;
        }

        @Override
        public String toString() {
            String retValue;

            retValue = "(" + getIncludeDirectiveLine() + "/" + getIncludeDirectiveOffset() + ": " + // NOI18N
                    getIncludedPath() + ":" + getResolvedDirectoryIndex() + ";#" + getIncludeDirectiveIndex() + ")"; // NOI18N
            return retValue;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || (obj.getClass() != this.getClass())) {
                return false;
            }
            IncludeInfoImpl other = (IncludeInfoImpl)obj;
            return  (resolvedDirectoryIndex == other.resolvedDirectoryIndex) && (this.includeDirectiveIndex == other.includeDirectiveIndex)
                    && this.path.equals(other.path);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 73 * hash + (this.path != null ? this.path.hashCode() : 0);
            hash = 73 * hash + this.resolvedDirectoryIndex;
            hash = 73 * hash + this.includeDirectiveIndex;
            return hash;
        }

        public void write(final RepositoryDataOutput output) throws IOException {
            assert output != null;
            output.writeFileSystem(fs);
            output.writeFilePathForFileSystem(fs, path);
            output.writeInt(directiveLine);
            output.writeInt(directiveOffset);
            output.writeInt(resolvedDirectoryIndex);
            output.writeInt(includeDirectiveIndex);
        }

        @Override
        public int getResolvedDirectoryIndex() {
            return this.resolvedDirectoryIndex;
        }

        @Override
        public int getIncludeDirectiveIndex() {
            return this.includeDirectiveIndex;
        }
        
    }

    private CharSequence popIncludeImpl() {
        assert (inclStack != null);
        assert (!inclStack.isEmpty());
        IncludeInfo inclInfo = inclStack.removeLast();
        CharSequence path = inclInfo.getIncludedPath();
//        if (CHECK_INCLUDE_DEPTH < 0) {
//            assert (recurseIncludes != null);
//            Integer counter = recurseIncludes.remove(path);
//            assert (counter != null) : "must be added before"; // NOI18N
//            // decrease include counter
//            counter = Integer.valueOf(counter.intValue()-1);
//            assert (counter.intValue() >= 0) : "can't be negative"; // NOI18N
//            if (counter.intValue() != 0) {
//                recurseIncludes.put(path, counter);
//            }
//        }
        return path;
    }

    @Override
    public String toString() {
        return ClankIncludeHandlerImpl.toString(startFile.getStartFile(),
                systemIncludePaths, userIncludePaths, userIncludeFilePaths,
                inclStackIndex, this.inclStack, this.cachedContent);
    }

    private static String toString(CharSequence startFile,
            List<IncludeDirEntry> systemIncludePaths,
            List<IncludeDirEntry> userIncludePaths,
            List<IncludeDirEntry> userIncludeFilePaths,
            int inclStackIndex,
            Collection<IncludeInfo> inclStack,
            ClankDriver.ClankPreprocessorOutput cachedTokens) {
        StringBuilder retValue = new StringBuilder();
        retValue.append(cachedTokens.toString()).append("\n"); // NOI18N
        if (!userIncludeFilePaths.isEmpty()) {
            retValue.append("User File Includes:\n"); // NOI18N
            retValue.append(APTUtils.includes2String(userIncludeFilePaths)).append("\n"); // NOI18N
        }
        retValue.append("User includes:\n"); // NOI18N
        retValue.append(APTUtils.includes2String(userIncludePaths));
        retValue.append("\nSys includes:\n"); // NOI18N
        retValue.append(APTUtils.includes2String(systemIncludePaths));
        retValue.append("\nInclude Stack starting from:\n"); // NOI18N
        retValue.append(startFile).append("\n"); // NOI18N
        retValue.append(includesStack2String(inclStackIndex, inclStack));
        return retValue.toString();
    }

    private static String includesStack2String(int inclStackIndex, Collection<IncludeInfo> inclStack) {
        StringBuilder retValue = new StringBuilder();
        if (inclStackIndex == 0) {
            retValue.append("<not from #include>"); // NOI18N
        } else {
            retValue.append("from inclStackIndex=").append(inclStackIndex); // NOI18N
            if (inclStack != null && !inclStack.isEmpty()) {
                retValue.append("\n"); // NOI18N
                for (Iterator<IncludeInfo> it = inclStack.iterator(); it.hasNext();) {
                    IncludeInfo info = it.next();
                    retValue.append(info);
                    if (it.hasNext()) {
                        retValue.append("->\n"); // NOI18N
                    }
                }
            }
        }
        return retValue.toString();
    }

    private static class ClankPPOutputImpl implements ClankDriverImpl.ClankPreprocessorOutputImplementation {

        private final int fileIndexInTranslationUnit;

        public ClankPPOutputImpl(int inclStackIndex) {
            this.fileIndexInTranslationUnit = inclStackIndex;
        }

        @Override
        public int getFileIndex() {
            return fileIndexInTranslationUnit;
        }

        @Override
        public int[] getSkippedRanges() {
            return null;
        }

        @Override
        public TokenStream getTokenStream() {
            return null;
        }

        @Override
        public boolean hasTokenStream() {
            return false;
        }

        @Override
        public String toString() {
            return fileIndexInTranslationUnit == -1 ? "<DUMMY>" : "no cache for " + fileIndexInTranslationUnit; // NOI18N
        }

        @Override
        public Collection<ClankDriver.ClankPreprocessorDirective> getPreprocessorDirectives() {
            return Collections.emptyList();
        }

        @Override
        public Collection<ClankDriver.MacroExpansion> getMacroExpansions() {
            return Collections.emptyList();
        }

        @Override
        public Collection<ClankDriver.MacroUsage> getMacroUsages() {
            return Collections.emptyList();
        }

        @Override
        public ClankDriver.FileGuard getFileGuard() {
            return null;
        }

        @Override
        public ClankDriverImpl.ClankPreprocessorOutputImplementation prepareCachesIfPossible() {
            // nothing to prepare
            return this;
        }
    }
}
