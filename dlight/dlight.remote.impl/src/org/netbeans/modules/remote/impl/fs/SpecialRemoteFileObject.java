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
package org.netbeans.modules.remote.impl.fs;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo.FileType;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.openide.filesystems.FileLock;
import org.openide.util.NbBundle;

/**
 *
 */
public class SpecialRemoteFileObject extends RemoteFileObjectBase {

    private final char fileTypeChar;
            
    /*package*/ SpecialRemoteFileObject(RemoteFileObject wrapper, RemoteFileSystem fileSystem, ExecutionEnvironment execEnv, 
            RemoteDirectory parent, String remotePath, FileType fileType) {
        super(wrapper, fileSystem, execEnv, parent, remotePath);
        fileTypeChar = fileType.toChar(); // TODO: pass when created
    }

    @Override
    public final RemoteFileObject[] getChildren() {
        return new RemoteFileObject[0];
    }

    @Override
    public final boolean isFolder() {
        return false;
    }

    @Override
    public boolean isData() {
        return false;
    }

    @Override
    public final RemoteFileObject getFileObject(String name, String ext, @NonNull Set<String> antiLoop) {
        return null;
    }

    @Override
    public RemoteFileObject getFileObject(String relativePath, @NonNull Set<String> antiLoop) {
        return null;
    }

    @Override
    public InputStream getInputStream(boolean checkLock) throws FileNotFoundException {
        return new ByteArrayInputStream(new byte[] {});
    }

    @Override
    protected RemoteFileObject createDataImpl(String name, String ext, RemoteFileObjectBase orig) throws IOException {
        throw RemoteExceptions.createIOException(NbBundle.getMessage(SpecialRemoteFileObject.class,
                "EXC_UnsupportedSpecial", getDisplayName())); // NOI18N
    }

    @Override
    protected RemoteFileObject createFolderImpl(String name, RemoteFileObjectBase orig) throws IOException {
        throw RemoteExceptions.createIOException(NbBundle.getMessage(SpecialRemoteFileObject.class, 
                "EXC_UnsupportedSpecial", getDisplayName())); // NOI18N
    }

    @Override
    protected void postDeleteOrCreateChild(RemoteFileObject child, DirEntryList entryList) {
        RemoteLogger.getInstance().log(Level.WARNING, "postDeleteChild is called on {0}", getClass().getSimpleName());
    }
    
    @Override
    protected DirEntryList deleteImpl(FileLock lock) throws IOException {
        return RemoteFileSystemTransport.delete(getExecutionEnvironment(), getPath(), false);
    }

    @Override
    protected void renameChild(FileLock lock, RemoteFileObjectBase toRename, String newNameExt, RemoteFileObjectBase orig) 
            throws ConnectException, IOException, InterruptedException, ExecutionException {
        // plain file can not be container of children
        RemoteLogger.assertTrueInConsole(false, "renameChild is not supported on " + this.getClass() + " path=" + getPath()); // NOI18N
    }

    @Override
    protected OutputStream getOutputStreamImpl(FileLock lock, RemoteFileObjectBase orig) throws IOException {
        if (!isValid()) {
            throw RemoteExceptions.createFileNotFoundException(NbBundle.getMessage(SpecialRemoteFileObject.class, 
                    "EXC_InvalidFO", getDisplayName())); //NOI18N
        }
        return new DelegateOutputStream();
    }
   

    @Override
    public FileType getType() {
        return FileType.fromChar(fileTypeChar);
    }

    private static class DelegateOutputStream extends OutputStream {

        public DelegateOutputStream() throws IOException {
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
        }

        @Override
        public void write(int b) throws IOException {
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public void flush() throws IOException {
        }
    }
}

