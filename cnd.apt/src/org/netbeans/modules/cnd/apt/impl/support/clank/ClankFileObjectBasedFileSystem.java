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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import org.clang.basic.vfs.File;
import org.clang.basic.vfs.Status;
import org.clang.basic.vfs.directory_iterator;
import org.clang.tools.services.support.ClangUtilities;
import org.clank.java.std;
import org.clank.java.std_errors;
import org.clank.java.std_ptr;
import org.clank.support.Casts;
import static org.clank.support.Casts.$char;
import org.clank.support.aliases.char$ptr;
import org.llvm.adt.SmallString;
import org.llvm.adt.StringRef;
import org.llvm.adt.Twine;
import org.llvm.support.ErrorOr;
import org.llvm.support.MemoryBuffer;
import org.llvm.support.llvm;
import org.llvm.support.sys.TimeValue;
import org.llvm.support.sys.fs;
import org.llvm.support.sys.fs.UniqueID;
import org.llvm.support.sys.fs.file_type;
import org.llvm.support.sys.fs.perms;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import static org.netbeans.modules.cnd.apt.impl.support.clank.ClankFileSystemProviderImpl.RFS_PREFIX;
import org.netbeans.modules.cnd.apt.support.APTFileBuffer;
import org.netbeans.modules.cnd.apt.support.spi.APTBufferProvider;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;


/**
 *
 */
public class ClankFileObjectBasedFileSystem extends org.clang.basic.vfs.FileSystem {

    private static final boolean TRACE_TIME = Boolean.getBoolean("clank.fs.trace.time"); //NOI18N
    private static final AtomicLong totalReadTime  = TRACE_TIME ? new AtomicLong() : null;
    private static final AtomicLong totalReadCount = TRACE_TIME ? new AtomicLong() : null;
    private static final AtomicLong totalStatTime  = TRACE_TIME ? new AtomicLong() : null;
    private static final AtomicLong totalStatCount = TRACE_TIME ? new AtomicLong() : null;

    private static final FileSystem LOCAL_FS = CndFileUtils.getLocalFileSystem();

    public static ClankFileObjectBasedFileSystem getInstance() {
        return new ClankFileObjectBasedFileSystem();
    }

    /** if null, then FileObject based file will be used */
    private final APTBufferProvider bufferProvider;

    private ClankFileObjectBasedFileSystem() {
        if (APTTraceFlags.ALWAYS_USE_BUFFER_BASED_FILES) { // NOI18N
            bufferProvider = Lookup.getDefault().lookup(APTBufferProvider.class);
            if (bufferProvider == null) {
                Exceptions.printStackTrace(new IllegalStateException("No providers found for " + APTBufferProvider.class.getName())); //NOI18N
            }
        } else {
            bufferProvider = null;
        }
    }

    private static void printStatistics(std.string Name, long readTime) {
        assert TRACE_TIME;
        StringBuilder sb = new StringBuilder();
        sb.append("Reading ").append(Name).append(" took ").append(readTime).append(" ms") //NOI18N
                .append("\nTotal reads: count=").append(totalReadCount.get()).append(" time=").append(totalReadTime.get()).append(" ms") //NOI18N
                .append("\nTotal stats: count=").append(totalStatCount.get()).append(" time=").append(totalStatTime.get()).append(" ms") //NOI18N
                .append("\n"); //NOI18N
        llvm.errs().$out(sb.toString());
    }

    public void destroy() {        
    }

    /// \brief Get the status of the entry at \p Path, if one exists.
    @Override
    public ErrorOr<Status> status(Twine Path) {
        long time = TRACE_TIME ? System.currentTimeMillis() : 0;
        FileObject fo = getFileObject(Path);
        ErrorOr<Status> status = getStatus(fo);
        if (TRACE_TIME) {
            totalStatTime.addAndGet(System.currentTimeMillis() - time);
            totalStatCount.incrementAndGet();
        }
        return status;
    }

    @Override
    public ErrorOr<std_ptr.unique_ptr<File>> openFileForRead(Twine Path) {
        long time = TRACE_TIME ? System.currentTimeMillis() : 0;
        ErrorOr<std_ptr.unique_ptr<File>> result;
        FileObject fo = getFileObject(Path);
        if (fo == null || !fo.isValid()) {
            result = new ErrorOr(std_errors.errc.no_such_file_or_directory);
        } else {
            if (bufferProvider == null) {
                ClankFileObjectBasedFile file = new ClankFileObjectBasedFile(fo);
                result = new ErrorOr(new std_ptr.unique_ptr<File>(file));
            } else {
                APTFileBuffer buffer = bufferProvider.getOrCreateFileBuffer(fo);
                if (buffer == null) {
                    result = new ErrorOr(std_errors.errc.no_such_file_or_directory);
                } else {
                    ClankAPTFileBufferBasedFile file = new ClankAPTFileBufferBasedFile(fo, buffer);
                    result = new ErrorOr(new std_ptr.unique_ptr<File>(file));
                }
            }
        }
        if (TRACE_TIME) {
            totalReadTime.addAndGet(System.currentTimeMillis() - time);
            totalReadCount.incrementAndGet();
        }
        return result;
    }

    @Override
    public directory_iterator dir_begin(Twine Dir, std_errors.error_code EC) {
        throw new UnsupportedOperationException("Not supported yet."); // TODO: implement? // NOI18N
    }

    private final SmallString BufString = new SmallString(512);
    private final StringRef BufRef = new StringRef();
    private final StringBuilder BufBuilder = new StringBuilder();
    private CharSequence toCharSequence(Twine twine) {
        BufString.clear();
        BufBuilder.setLength(0);
        StringRef StrRef = twine.toStringRef(BufString, BufRef);        
        int Len = StrRef.size();
        if (BufString.size() > 0) {
            assert Len == BufString.size();
            BufString.toStringBuilder(BufBuilder);
        } else {
            char$ptr data = StrRef.data();
            Casts.toStringBuilder(BufBuilder, data, Len);
        }
        return BufBuilder;
    }
    

    private UniqueID getUniqueID(FileObject fo) {
        CndFileSystemProvider.CndStatInfo stat = CndFileSystemProvider.getStatInfo(fo);
        if (stat.isValid()) {
            return new fs.UniqueID(stat.device, stat.inode);
        } else {
            return null;
        }
    }

    private FileObject getFileObject(Twine Path) {
        String url = toCharSequence(Path).toString();
        String path;
        FileSystem fs;
        if (CharSequenceUtils.startsWith(url, RFS_PREFIX)) {
            path = ClankFileSystemProviderImpl.getPathFromUrl(url);
            fs = CndFileSystemProvider.urlToFileSystem(url);
        } else {
            path = url;
            fs = LOCAL_FS;
        }
        if (fs != null) {
            path = CndFileUtils.normalizeAbsolutePath(fs, path);
            if (CndFileUtils.exists(fs, path)) {
                return CndFileUtils.toFileObject(fs, path);
            }
        }
        return null;
    }

    private ErrorOr<Status> getStatus(ClankFileObjectBasedFile file) {
        long time = TRACE_TIME ? System.currentTimeMillis() : 0;
        ErrorOr<Status> result = getStatus(file.getFileObject());
        if (TRACE_TIME) {
            totalStatTime.addAndGet(System.currentTimeMillis() - time);
            totalStatCount.incrementAndGet();
        }
        return result;
    }

    private ErrorOr<Status> getStatus(FileObject fo) {
        if (fo == null || !fo.isValid()) {
            // TODO: should we set errno?
            return new ErrorOr(std_errors.errc.no_such_file_or_directory);
        } else {
            StringRef name = ClangUtilities.createPathStringRef(CndFileSystemProvider.toUrl(FSPath.toFSPath(fo)));
            UniqueID uid = getUniqueID(fo);
            if (uid == null) {
                return new ErrorOr(std_errors.errc.io_error);
            }
            long ms = fo.lastModified().getTime();
            TimeValue time = new TimeValue(ms/1000, (int) (ms%1000)*1000000);
            int user = 0; // TODO: provide access to user if needed
            int group = 0; // TODO: provide access to group if needed
            /*fs.prems*/int permissions = perms.all_all; // TODO: get real permissions
            file_type type;
            // follow links! remote isFolder does
            if (fo.isFolder()) {
                type = file_type.directory_file;
            } else if (fo.isData()) {
                type = file_type.regular_file;
            } else {
                type = file_type.type_unknown;
            }
            //= fo.isFolder() ? fs.file_type.directory_file : fs.file_type.;
            Status st = new Status(name, uid, time, user, group, fo.getSize(), type, permissions);
            return new ErrorOr<Status>(st);
        }
    }

    @Override
    public std_errors.error_code setCurrentWorkingDirectory(Twine Path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ErrorOr<std.string> getCurrentWorkingDirectory() {
        throw new UnsupportedOperationException();
    }

    private class ClankFileObjectBasedFile extends org.clang.basic.vfs.File {

        private final FileObject fo;
        private final CharSequence foURL;

        public ClankFileObjectBasedFile(FileObject fo) {
            this.fo = fo;
            foURL = CndFileSystemProvider.toUrl(FSPath.toFSPath(fo));
        }

        /*package*/ FileObject getFileObject() {
            return fo;
        }

        /*package*/ CharSequence getURL() {
            return foURL;
        }

        @Override
        public ErrorOr<Status> status() {
            return ClankFileObjectBasedFileSystem.this.getStatus(this);
        }

        protected ClankMemoryBufferImpl createMemoryBufferImpl() throws IOException {
            return ClankMemoryBufferImpl.create(fo, getURL());
        }

        @Override
        public ErrorOr<std_ptr.unique_ptr<MemoryBuffer>> getBuffer(/*const*/ Twine /*&*/ Name, long/*int64_t*/ FileSize/*= -1*/, 
           boolean RequiresNullTerminator/*= true*/, boolean IsVolatile/*= false*/) {
            long time = TRACE_TIME ? System.currentTimeMillis() : 0;
            ErrorOr<std_ptr.unique_ptr<MemoryBuffer>> buf;
            if (CndUtils.isDebugMode()) {
                String pathAsUrl = Name.str().toJavaString();
                if (!pathAsUrl.contentEquals(foURL)) {
                    CndUtils.assertTrueInConsole(false, "Unexpected Name parameter: '" + pathAsUrl + " expected own " + pathAsUrl + " for wrapped " + fo);//NOI18N
                }                
            }
            try {
                ClankMemoryBufferImpl mb = createMemoryBufferImpl();
                std_ptr.unique_ptr<MemoryBuffer> p = new std_ptr.unique_ptr<MemoryBuffer>(mb);
                buf = new ErrorOr<std_ptr.unique_ptr<MemoryBuffer>>(p);
            } catch (FileNotFoundException ex) {
                buf = new ErrorOr(std_errors.errc.no_such_file_or_directory);
            } catch (IOException ex) {
                buf = new ErrorOr(std_errors.errc.io_error);
            }
            if (TRACE_TIME) {
                time = System.currentTimeMillis() - time;
                totalReadCount.incrementAndGet();
                totalReadTime.addAndGet(time);
                printStatistics(Name.str(), time);
            }
            return buf;
        }

        @Override
        public std_errors.error_code close() {
            return std.error_code.success();
        }
    }

    private final class ClankAPTFileBufferBasedFile extends ClankFileObjectBasedFile {

        private final APTFileBuffer buffer;

        public ClankAPTFileBufferBasedFile(FileObject fo, APTFileBuffer buffer) {
            super(fo);
            this.buffer = buffer;
        }

        @Override
        protected ClankMemoryBufferImpl createMemoryBufferImpl() throws IOException {
            return ClankMemoryBufferImpl.create(getURL(), buffer.getCharBuffer());
        }
    }
}
