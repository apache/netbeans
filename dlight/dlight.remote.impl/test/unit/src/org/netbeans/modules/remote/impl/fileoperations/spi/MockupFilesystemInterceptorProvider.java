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
package org.netbeans.modules.remote.impl.fileoperations.spi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileSystem;

/**
 *
 */
//@org.openide.util.lookup.ServiceProvider(service = FilesystemInterceptorProvider.class, position = 100)
public class MockupFilesystemInterceptorProvider extends FilesystemInterceptorProvider {
    private final Map<FileSystem, FilesystemInterceptor> map = new HashMap<>();

    @Override
    public synchronized FilesystemInterceptor getFilesystemInterceptor(FileSystem fs) {
        FilesystemInterceptor interceptor = map.get(fs);
        if (interceptor == null) {
            interceptor = new FilesystemInterceptorImpl(fs);
            map.put(fs, interceptor);
        }
        return interceptor;
    }

    public static final class FilesystemInterceptorImpl implements FilesystemInterceptor {
        private final FileSystem fs;
        private final ExecutionEnvironment env;
        
        
        private final List<FileProxyI>    beforeCreateFiles = new ArrayList<>();
        private final List<FileProxyI>    doCreateFiles = new ArrayList<>();
        private final List<FileProxyI>    createdFiles = new ArrayList<>();
        
        private final List<FileProxyI>    beforeDeleteFiles = new ArrayList<>();
        private final List<FileProxyI>    doDeleteFiles = new ArrayList<>();
        private final List<FileProxyI>    deletedFiles = new ArrayList<>();
        
        private final List<FileProxyI>    beforeMoveFiles = new ArrayList<>();
        private final List<FileProxyI>    afterMoveFiles = new ArrayList<>();
        
        private final List<FileProxyI>    beforeCopyFiles = new ArrayList<>();
        private final List<FileProxyI>    afterCopyFiles = new ArrayList<>();
        private final List<FileProxyI>    doCopyFiles = new ArrayList<>();
        
        private final List<FileProxyI>    beforeEditFiles = new ArrayList<>();
        private final List<FileProxyI>    beforeChangeFiles = new ArrayList<>();
        private final List<FileProxyI>    afterChangeFiles = new ArrayList<>();
        
        private final List<FileProxyI>    isMutableFiles = new ArrayList<>();
        private final List<FileProxyI>    refreshRecursivelyFiles = new ArrayList<>();
        
        public FilesystemInterceptorImpl(FileSystem fs) {
            this.fs = fs;
            this.env = FileSystemProvider.getExecutionEnvironment(fs);
        }

        @Override
        public boolean canWriteReadonlyFile(FileProxyI file) {
            isMutableFiles.add(file);
            return false;
        }

        @Override
        public Object getAttribute(FileProxyI file, String attrName) {
            return null;
        }

        @Override
        public void beforeChange(FileProxyI file) {
            beforeChangeFiles.add(file);
        }

        @Override
        public void fileChanged(FileProxyI file) {
            afterChangeFiles.add(file);
        }

        @Override
        public IOHandler getDeleteHandler(final FileProxyI file) {
            return new IOHandler() {

                @Override
                public void handle() throws IOException {
                    Future<Integer> rmFile = CommonTasksSupport.rmDir(env, file.getPath(), true, null);
                    try {
                        doDeleteFiles.add(file);
                        if (rmFile.get() != 0) {
                            throw new IOException("Cannot delete "+file.getPath()+" at "+env.getDisplayName());
                        }
                    } catch (Exception ex) {
                        throw new IOException("Cannot delete "+file.getPath()+" at "+env.getDisplayName(), ex);
                    }
                }
            };
        }

        @Override
        public void deleteSuccess(FileProxyI file) {
            deletedFiles.add(file);
        }

        @Override
        public void deletedExternally(FileProxyI file) {
        }

        @Override
        public void beforeCreate(FileProxyI parent, String name, boolean isFolder) {
            beforeCreateFiles.add(toFileProxy(fs, parent.getPath()+'/'+name));
        }

        @Override
        public void createFailure(FileProxyI parent, String name, boolean isFolder) {
        }

        @Override
        public void createSuccess(FileProxyI fo) {
            createdFiles.add(fo);
        }

        @Override
        public void createdExternally(FileProxyI fo) {
        }

        @Override
        public IOHandler getMoveHandler(final FileProxyI from, final FileProxyI to) {
            return new IOHandler() {

                @Override
                public void handle() throws IOException {
                    ExitStatus execute = ProcessUtils.execute(env, "mv", from.getPath(), to.getPath());
                    if (!execute.isOK()) {
                        throw new IOException();
                    }
                }
            };
        }

        @Override
        public IOHandler getRenameHandler(final FileProxyI from, final String newName) {
            return new IOHandler() {

                @Override
                public void handle() throws IOException {
                    int i = from.getPath().lastIndexOf('/');
                    ExitStatus execute = ProcessUtils.execute(env, "mv", from.getPath(), from.getPath().substring(0,i)+'/'+newName);
                    if (!execute.isOK()) {
                        throw new IOException();
                    }
                }
            };
        }

        @Override
        public void afterMove(FileProxyI from, FileProxyI to) {
            afterMoveFiles.add(from);
        }

        @Override
        public FilesystemInterceptorProvider.IOHandler getCopyHandler(final FileProxyI from, final FileProxyI to) {
            return new FilesystemInterceptorProvider.IOHandler() {

                @Override
                public void handle() throws IOException {
                    doCopyFiles.add(from);
                    doCopyFiles.add(to);
                    ExitStatus execute = ProcessUtils.execute(env, "cp", "-r", from.getPath(), to.getPath());
                    if (!execute.isOK()) {
                        throw new IOException();
                    }
                }
            };
        }

        @Override
        public void beforeCopy(FileProxyI from, FileProxyI to) {
            beforeCopyFiles.add(from);
            beforeCopyFiles.add(to);
        }

        @Override
        public void copySuccess(FileProxyI from, FileProxyI to) {
            afterCopyFiles.add(from);
            afterCopyFiles.add(to);
        }

        @Override
        public void fileLocked(FileProxyI fo) throws IOException {
        }

        @Override
        public long refreshRecursively(FileProxyI dir, long lastTimeStamp, List<? super FileProxyI> children) {
            refreshRecursivelyFiles.add(dir);
            return -1;
        }

///////////////////////        
//    public void doCreate(File file, boolean isDirectory) throws IOException {
//        doCreateFiles.add(file);
//        if (!file.exists()) {
//            if (isDirectory) {
//                file.mkdirs();
//            } else {
//                file.getParentFile().mkdirs();
//                file.createNewFile();
//            }
//        }
//    }
//
//    public void doMove(File from, File to) throws IOException {
//        from.renameTo(to);
//    }
//
//    public void beforeEdit(File file) {
//        beforeEditFiles.add(file);
//    }
//////////////////////        
        

    public List<FileProxyI> getIsMutableFiles() {
        return isMutableFiles;
    }

    public List<FileProxyI> getBeforeCreateFiles() {
        return beforeCreateFiles;
    }

    public List<FileProxyI> getDoCreateFiles() {
        return doCreateFiles;
    }

    public List<FileProxyI> getCreatedFiles() {
        return createdFiles;
    }

    public List<FileProxyI> getBeforeDeleteFiles() {
        return beforeDeleteFiles;
    }

    public List<FileProxyI> getDoDeleteFiles() {
        return doDeleteFiles;
    }

    public List<FileProxyI> getDeletedFiles() {
        return deletedFiles;
    }

    public List<FileProxyI> getBeforeMoveFiles() {
        return beforeMoveFiles;
    }

    public List<FileProxyI> getAfterMoveFiles() {
        return afterMoveFiles;
    }

    public List<FileProxyI> getBeforeCopyFiles() {
        return beforeCopyFiles;
    }

    public List<FileProxyI> getDoCopyFiles() {
        return doCopyFiles;
    }

    public List<FileProxyI> getAfterCopyFiles() {
        return afterCopyFiles;
    }

    public List<FileProxyI> getBeforeEditFiles() {
        return beforeEditFiles;
    }

    public List<FileProxyI> getBeforeChangeFiles() {
        return beforeChangeFiles;
    }

    public List<FileProxyI> getAfterChangeFiles() {
        return afterChangeFiles;
    }

    public List<FileProxyI> getRefreshRecursivelyFiles() {
        return refreshRecursivelyFiles;
    }

        public void clearTestData() {
            beforeCreateFiles.clear();
            doCreateFiles.clear();
            createdFiles.clear();
            beforeDeleteFiles.clear();
            doDeleteFiles.clear();
            deletedFiles.clear();
            beforeMoveFiles.clear();
            afterMoveFiles.clear();
            beforeEditFiles.clear();
            beforeChangeFiles.clear();
            afterChangeFiles.clear();
            isMutableFiles.clear();
        }
        
        @Override
        public String toString() {
            return fs.getDisplayName();
        }
    }
}
