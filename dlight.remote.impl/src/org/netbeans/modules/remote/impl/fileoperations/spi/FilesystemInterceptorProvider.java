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
package org.netbeans.modules.remote.impl.fileoperations.spi;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 */
abstract public class FilesystemInterceptorProvider {
    private static FilesystemInterceptorProvider defaultProvider;
    
    protected FilesystemInterceptorProvider() {
    }

    public abstract FilesystemInterceptor getFilesystemInterceptor(FileSystem fs);

    public interface FilesystemInterceptor extends QueryOperations, ChangeOperations, DeleteOperations, CreateOperations, MoveOperations, CopyOperations, MiscOperations {
    }

    public interface FileProxyI {

        String getPath();
        
        FileSystem getFileSystem();
    }

    public interface QueryOperations {
        boolean canWriteReadonlyFile(FileProxyI file);
        Object getAttribute(FileProxyI file, String attrName);
    }

    public interface ChangeOperations {
        void beforeChange(FileProxyI file);
        public void fileChanged(FileProxyI file);
    }

    public interface DeleteOperations {
        IOHandler getDeleteHandler(FileProxyI file);
        void deleteSuccess(FileProxyI file);
        void deletedExternally(FileProxyI file);
    }

    public interface CreateOperations {
        void beforeCreate(FileProxyI parent, String name, boolean isFolder);
        void createFailure(FileProxyI parent, String name, boolean isFolder);
        void createSuccess(FileProxyI fo);
        void createdExternally(FileProxyI fo);
    }

    public interface MoveOperations {
        IOHandler getMoveHandler(FileProxyI from, FileProxyI to);
        IOHandler getRenameHandler(FileProxyI from, String newName);
        void afterMove(FileProxyI from, FileProxyI to);
    }

    public interface CopyOperations {
        IOHandler getCopyHandler(FileProxyI from, FileProxyI to);
        void beforeCopy(FileProxyI from, FileProxyI to);
        void copySuccess(FileProxyI from, FileProxyI to);
    }

    public interface MiscOperations {
        void fileLocked(FileProxyI fo) throws IOException;
        long refreshRecursively(FileProxyI dir, long lastTimeStamp, List<? super FileProxyI> children);
    }

    public interface IOHandler {
        /**
        * @throws java.io.IOException if handled operation isn't successful
        */
        void handle() throws IOException;
    }
    
    public static FileProxyI toFileProxy(FileObject file, String name, String ext) {
        return new FileObjectFileProxyI(file, name, ext);
    }

    public static FileProxyI toFileProxy(FileObject file) {
        return new FileObjectFileProxyI(file);
    }

    public static FileProxyI toFileProxy(FileSystem fs, String path) {
        return new FileObjectFileProxyI(fs, path);
    }
    
    private static final class FileObjectFileProxyI implements FileProxyI {
        private final FileSystem fs;
        private final String path;
        private FileObjectFileProxyI(FileObject fo) {
            FileSystem aFS = null;
            try {
                aFS = fo.getFileSystem();
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
            this.fs = aFS;
            this.path = fo.getPath();
        }

        private FileObjectFileProxyI(FileSystem fs, String path) {
            this.fs = fs;
            this.path = path;
        }

        private FileObjectFileProxyI(FileObject fo, String name, String ext) {
            FileSystem aFS = null;
            try {
                aFS = fo.getFileSystem();
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
            this.fs = aFS;
            String aPath = fo.getPath();
            if (!aPath.endsWith("/")) { //NOI18N
                aPath = aPath + '/' + name;
            } else {
                aPath = aPath + name;
            }
            if (ext != null && ext.length() > 0) {
                aPath = aPath + '.' + ext;
            }
            this.path = aPath;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public FileSystem getFileSystem() {
            return fs;
        }

        @Override
        public String toString() {
            return path;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 79 * hash + (this.fs != null ? this.fs.hashCode() : 0);
            hash = 79 * hash + (this.path != null ? this.path.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FileObjectFileProxyI other = (FileObjectFileProxyI) obj;
            if (this.fs != other.fs && (this.fs == null || !this.fs.equals(other.fs))) {
                return false;
            }
            if ((this.path == null) ? (other.path != null) : !this.path.equals(other.path)) {
                return false;
            }
            return true;
        }
    }
    
    /**
     * Static method to obtain the provider.
     *
     * @return the provider
     */
    public static FilesystemInterceptorProvider getDefault() {
        /*
         * no need for sync synchronized access
         */
        if (defaultProvider != null) {
            return defaultProvider;
        }
        Collection<? extends FilesystemInterceptorProvider> lookupAll = Lookup.getDefault().lookupAll(FilesystemInterceptorProvider.class);
        if (lookupAll == null || lookupAll.isEmpty()) {
            defaultProvider = NullProvider;
        } else {
            final Iterator<? extends FilesystemInterceptorProvider> iterator = lookupAll.iterator();
            if (lookupAll.size() == 1) {
                defaultProvider = iterator.next();
            } else {
                while(iterator.hasNext()) {
                    FilesystemInterceptorProvider next = iterator.next();
                    if (next.getClass().getName().indexOf("Mockup") >= 0) { //NOI18N
                        defaultProvider = next;
                    }
                }
                if (defaultProvider == null) {
                    defaultProvider = lookupAll.iterator().next();
                }
            }
        }
        return defaultProvider;
    }
    
    private static final FilesystemInterceptorProvider NullProvider = new FilesystemInterceptorProvider() {

        @Override
        public FilesystemInterceptor getFilesystemInterceptor(FileSystem fs) {
            return null;
        }
    };
}
