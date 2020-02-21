/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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

