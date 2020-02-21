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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import org.netbeans.modules.cnd.makeproject.api.SourceFolderInfo;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.filesystems.FileObject;

public final class FolderEntry implements SourceFolderInfo {

    private final FileObject fileObject;
    private final FSPath fsPath;
    private final String folderName;
    private final boolean addSubfolders;
    //private final FileFilter fileFilter;

    public FolderEntry(FileObject fileObject, String folderName) {
        this.fileObject = fileObject;
        this.fsPath = null;
        this.folderName = folderName;
        addSubfolders = true;
        //fileFilter = null;
    }

    public FolderEntry(FSPath fsPath, String folderName) {
        this.fileObject = null;
        this.fsPath = fsPath;
        this.folderName = folderName;
        addSubfolders = true;
        //fileFilter = null;
    }

    @Override
    public String getFolderName() {
        return folderName;
    }

//    public void setFolderName(String file) {
//        this.folderName = file;
//    }

    @Override
    public boolean isAddSubfoldersSelected() {
        return addSubfolders;
    }

//    public void setAddSubfoldersSelected(boolean selected) {
//        this.addSubfolders = selected;
//    }

    @Override
    public FileObject getFileObject() {
        if (fileObject != null) {
            return fileObject;
        } else {
            return fsPath.getFileObject();
        }
    }

//    public void setFile(File file) {
//        this.file = file;
//    }

//    @Override
//    public FileFilter getFileFilter() {
//        return fileFilter;
//    }

//    public void setFileFilter(FileFilter ff) {
//        fileFilter = ff;
//    }

    @Override
    public String toString() {
        return folderName;
    }
}
