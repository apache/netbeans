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
import org.netbeans.modules.masterfs.filebasedfs.utils.FSException;
import org.openide.filesystems.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;
import org.openide.util.Enumerations;

public final class RootObjWindows extends FileObject {
    public RootObjWindows() {
    }

    public final String getName() {
        return "";//NOI18N
    }

    public final String getExt() {
        return "";//NOI18N
    }

    public final FileSystem getFileSystem() throws FileStateInvalidException {
        return FileBasedFileSystem.getInstance();
    }

    @Override
    public FileObject getFileObject(String relativePath) {
        return super.getFileObject(relativePath);
    }

    
    public final FileObject getParent() {
        return null;
    }

    public final boolean isFolder() {
        return true;
    }

    public final boolean isData() {
        return !isFolder();
    }

    public final Date lastModified() {
        return new Date(0);
    }

    public final boolean isRoot() {
        return true;
    }


    /* Test whether the file is valid. The file can be invalid if it has been deserialized
    * and the file no longer exists on disk; or if the file has been deleted.
    *
    * @return true if the file object is valid
    */
    public final boolean isValid() {
        return true;
    }

    public final void rename(final FileLock lock, final String name, final String ext) throws IOException {
        //throw new IOException(getPath());
        FSException.io("EXC_CannotRenameRoot", getFileSystem().getDisplayName()); // NOI18N        
    }

    public final void delete(final FileLock lock) throws IOException {
        //throw new IOException(getPath());
        FSException.io("EXC_CannotDeleteRoot", getFileSystem().getDisplayName()); // NOI18N        
    }

    public final Object getAttribute(final String attrName) {        
        return null;
    }

    public final void setAttribute(final String attrName, final Object value) throws IOException {  
        throw new FileStateInvalidException(); // NOI18N        
    }

    public final Enumeration<String> getAttributes() {
        return Enumerations.empty();
    }

    public final void addFileChangeListener(final FileChangeListener fcl) {
        //TODO: adding new FileBasedFS into allInstances should lead to firing event
    }

    public final void removeFileChangeListener(final FileChangeListener fcl) {
        //TODO: adding new FileBasedFS into allInstances should lead to firing event
    }

    public final long getSize() {
        return 0;
    }

    public final InputStream getInputStream() throws FileNotFoundException {
        throw new FileNotFoundException(); // NOI18N        
    }

    public final OutputStream getOutputStream(final FileLock lock) throws IOException {
        throw new FileNotFoundException(); // NOI18N 
    }

    public final FileLock lock() throws IOException {
        throw new FileStateInvalidException(); // NOI18N        
    }

    public final void setImportant(final boolean b) {
    }

    public final FileObject[] getChildren() {
        Collection<? extends FileObjectFactory> all = FileBasedFileSystem.factories().values();
        ArrayList<FileObject> rootChildren = new ArrayList<FileObject>();         
        for (FileObjectFactory fs : all) {
            BaseFileObj root = fs.getRoot();
            if (root != null) { // #252580
                rootChildren.add(root);
            }
        }
        return rootChildren.toArray(new FileObject[rootChildren.size()]);
    }

    @Override
    public final FileObject getFileObject(String name, final String ext) {
        FileObject first = getFileObjectImpl(name, ext);
        if (first != null) {
            return first;
        }
        if (name.length() >= 2 && name.charAt(1) == ':') {
            final File root = new File(name.charAt(0) + ":\\"); // NOI18N
            FileObjectFactory.getInstance(root, true);
        }
        return getFileObjectImpl(name, ext);
    }
    
    private FileObject getFileObjectImpl(String name, String ext) {
        FileObject[] rootChildren =  getChildren();
        for (int i = 0; i < rootChildren.length; i++) {
            FileObject fileObject = rootChildren[i];            
            // UNC absolute path
            if(name.startsWith("//")) { // NOI18N
                // replace '/' by '\'
                name = name.replace('/', '\\');  //NOI18N
            }
            String real = fileObject.getNameExt();
            if (real.endsWith("\\")) {
                real = real.substring(0, real.length() - 1);
            }
            if (FileInfo.composeName(name, ext).equals(real)) {
                return fileObject;
            }
        }
        return null;
    }

    
    public final FileObject createFolder(final String name) throws IOException {
        throw new FileStateInvalidException(); // NOI18N        
    }

    public final FileObject createData(final String name, final String ext) throws IOException {
        throw new FileStateInvalidException(); // NOI18N        
    }

    public final boolean isReadOnly() {
        return true;
    }

    @Override
    public String getPath() {
        return "";
    }

    
    @Override
    public String toString() {
        return "";
    }
}
