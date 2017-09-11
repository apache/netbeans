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
package org.netbeans.modules.xml.util;

import java.io.*;
import java.util.*;

import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;

/**
 * This file object represents an InputStream.
 *
 * @author  Petr Kuzel
 * @version untested draft
 */
public class StreamFileObject extends FileObject {

    /** Serial Version UID */
    private static final long serialVersionUID =8966806836211837503L;


    private org.openide.filesystems.FileObject[] files; //kids

    private boolean isRoot; //does it represent folder (root);
    
    private InputStream peer; //wrapped input stream
    
    private FileSystem fs;
    
    
    //my filesystem
    
    /** Creates new StreamFileObject */
    public StreamFileObject(InputStream in) {
        this(in, false);
    }
    
    public StreamFileObject(InputStream in, boolean isRoot) {
        this.isRoot = isRoot;
        peer = in;
        if (isRoot) {
            files = new org.openide.filesystems.FileObject[] {
                new StreamFileObject(in)
            };
        } else {
            files = new org.openide.filesystems.FileObject[0];
        }
        fs = new StreamFileSystem(this);
    }

    public org.openide.filesystems.FileObject[] getChildren() {
        return files;
    }
    
    public void removeFileChangeListener(org.openide.filesystems.FileChangeListener fileChangeListener) {
    }
    
    public org.openide.filesystems.FileLock lock() throws java.io.IOException {
        return FileLock.NONE;
    }
    
    public java.lang.Object getAttribute(java.lang.String str) {
        return null;
    }
    
    public java.util.Date lastModified() {
        return new Date(0L);
    }
    
    public java.lang.String getExt() {
        return "InputStream"; // NOI18N
    }
    
    @Deprecated
    public boolean isReadOnly() {
        return true;
    }
    
    public org.openide.filesystems.FileObject createData(java.lang.String str, java.lang.String str1) throws java.io.IOException {
        return null;
    }
    
    public void delete(org.openide.filesystems.FileLock fileLock) throws java.io.IOException {
    }
    
    public org.openide.filesystems.FileObject createFolder(java.lang.String str) throws java.io.IOException {
        return null;
    }
    
    public void rename(org.openide.filesystems.FileLock fileLock, java.lang.String str, java.lang.String str2) throws java.io.IOException {        
    }
    
    public boolean isData() {
        return isRoot == false; 
    }
    
    public java.io.OutputStream getOutputStream(org.openide.filesystems.FileLock fileLock) throws java.io.IOException {
        throw new IOException("r/o"); // NOI18N
    }
    
    public java.io.InputStream getInputStream() throws java.io.FileNotFoundException {
        return peer;
    }
    
    public boolean isValid() {
        return true;
    }
    
    public java.util.Enumeration<String> getAttributes() {
        return org.openide.util.Enumerations.empty();
    }
    
    public java.lang.String getName() {
        return "StreamFileObject"; // NOI18N
    }
    
    @Deprecated
    public void setImportant(boolean param) {
    }
    
    public boolean isFolder() {
        return isRoot;
    }
    
    public void setAttribute(java.lang.String str, java.lang.Object obj) throws java.io.IOException {
    }
    
    public void addFileChangeListener(org.openide.filesystems.FileChangeListener fileChangeListener) {
    }
    
    public long getSize() {
        return 766; //!!!
    }
    
    public org.openide.filesystems.FileObject getParent() {
        if (isRoot) return null;
        return fs.getRoot();
    }
    
    public boolean isRoot() {
        return isRoot;
    }
    
    public org.openide.filesystems.FileObject getFileObject(java.lang.String str, java.lang.String str1) {
        return null;
    }
    
    public org.openide.filesystems.FileSystem getFileSystem() throws org.openide.filesystems.FileStateInvalidException {
        return fs;
    }
    
}
