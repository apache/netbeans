/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;

/**
 * @author Radek Matous
 */
public class ReplaceForSerialization extends Object implements java.io.Serializable {
    /**
     * generated Serialized Version UID
     */
    static final long serialVersionUID = -7451332135435542113L;
    private final String absolutePath;

    public ReplaceForSerialization(final File file) {
        absolutePath = file.getAbsolutePath();
    }

    public final Object readResolve() {
        final File file = new File(absolutePath);
        final FileObject retVal = FileBasedFileSystem.getInstance().getFileObject(file);               
        return (retVal != null) ? retVal : new Invalid (file);
    }

    private static class Invalid extends BaseFileObj {
        protected Invalid(File file) {
            super(file);
        }

        public void delete(FileLock lock, ProvidedExtensions.IOHandler io) throws IOException {
            throw new IOException(getPath()); 
        }
        
        boolean checkLock(FileLock lock) throws IOException {
            return false;
        }

        protected void setValid(boolean valid) {}

        public boolean isFolder() {
            return false;
        }

        /* Test whether the file is valid. The file can be invalid if it has been deserialized
        * and the file no longer exists on disk; or if the file has been deleted.
        *
        * @return true if the file object is valid
        */
        public boolean isValid() {
            return false;
        }

        public InputStream getInputStream() throws FileNotFoundException {
            throw new FileNotFoundException (getPath());
        }

        public OutputStream getOutputStream(FileLock lock) throws IOException {
            throw new IOException (getPath());
        }

        public FileLock lock() throws IOException {
            throw new IOException (getPath());
        }

        public FileObject[] getChildren() {
            return new FileObject[] {};
        }

        public FileObject getFileObject(String name, String ext) {
            return null;
        }

        public FileObject createFolder(String name) throws IOException {
            throw new IOException (getPath());
        }

        public FileObject createData(String name, String ext) throws IOException {
            throw new IOException (getPath());
        }

        public void refreshImpl(final boolean expected, boolean fire) {
        }

        @Override
        protected boolean noFolderListeners() {
            return true;
        }
    }
}
