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

package org.netbeans.modules.form.refactoring;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * Holds information about one refactoring. Knows the type of refactoring change,
 * the originating source file and its corresponding class name. For each
 * affected source file (form) keeps the transaction that loads and updates
 * the form. When refactoring starts, an instance of RefactoringInfo is attached
 * to the refactoring's context - so it can be accessed from different places
 * (refactoring plugin, guarded block handler).
 * 
 * @author Tomas Pavek
 */
public class RefactoringInfo {

    public enum ChangeType {
        VARIABLE_RENAME,  // field or local variable in initComponents
        CLASS_RENAME, CLASS_MOVE,  // can be a form, or a component class, or both
        CLASS_COPY,  // a form class
        CLASS_DELETE, // a form class (safe delete)
        PACKAGE_RENAME, FOLDER_RENAME,  // non-recursive folder and folder with subfolders
        EVENT_HANDLER_RENAME,  // method in a form class
        OTHER_FORM_CHANGE
    }

    private AbstractRefactoring refactoring;
    private ChangeType changeType;
    private FileObject[] origFiles;
    private Map<FileObject, String> originalFiles; // original files and associated name
    private Map<FileObject,FormRefactoringUpdate> fileToUpdateMap = new HashMap<FileObject,FormRefactoringUpdate>();

    RefactoringInfo(AbstractRefactoring refactoring, ChangeType changeType,
                    FileObject[] files,
                    String[] oldNames)
    {
        this.refactoring = refactoring;
        this.changeType = changeType;
        Map<FileObject, String> fileMap = new HashMap<FileObject, String>();
        for (int i=0; i < files.length; i++) {
            fileMap.put(files[i], oldNames[i]);
        }
        this.originalFiles = Collections.unmodifiableMap(fileMap);
        this.origFiles = new FileObject[files.length];
        System.arraycopy(files, 0, origFiles, 0, files.length);
    }

    AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public boolean containsOriginalFile(FileObject file) {
        return originalFiles.containsKey(file);
    }

    public FileObject[] getOriginalFiles() {
        return origFiles;
    }

    String getOldName(FileObject file) {
        return originalFiles.get(file);
    }

    String getNewName() {
        return getNewName(null);
    }

    String getNewName(FileObject file) {
        if (refactoring instanceof RenameRefactoring) {
            // return the new name of the file/element
            return ((RenameRefactoring)refactoring).getNewName();
        } else if (refactoring instanceof MoveRefactoring
                   && file != null && containsOriginalFile(file)) {
            // return full class name of the java file on its new location
            return getTargetName((MoveRefactoring)refactoring, file.getName());
        }
        return null;
    }

    private String getTargetName(MoveRefactoring refactoring, String fileName) {
        URL targetURL = refactoring.getTarget().lookup(URL.class);
        File f = null;
        try {
            if (targetURL != null) {
                f = FileUtil.normalizeFile(new File(targetURL.toURI()));
            }
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        }
        LinkedList<String> nonExisting = null; // the path to target folder may not exist yet
        while (f != null && !f.exists()) {
            if (nonExisting == null) {
                nonExisting = new LinkedList<String>();
            }
            nonExisting.addFirst(f.getName());
            f = f.getParentFile();
        }
        FileObject targetFolder = (f != null) ? FileUtil.toFileObject(f) : null;
        if (targetFolder != null && targetFolder.isFolder()) {
            ClassPath cp = ClassPath.getClassPath(targetFolder, ClassPath.SOURCE);
            if (cp != null) {
                String pkg = cp.getResourceName(targetFolder, '.', false);
                StringBuilder buf = new StringBuilder();
                if (pkg != null) {
                    buf.append(pkg);
                    if (buf.length() > 0) {
                        buf.append('.');
                    }
                }
                if (nonExisting != null && !nonExisting.isEmpty()) {
                    for (String s : nonExisting) {
                        buf.append(s);
                        buf.append('.');
                    }
                }
                buf.append(fileName);
                return buf.toString();
            }
        }
        return null;
    }

    public FormRefactoringUpdate getUpdateForFile(FileObject fo) {
        FormRefactoringUpdate update = fileToUpdateMap.get(fo);
        if (update == null) {
            assert isJavaFileOfForm(fo);
            update = new FormRefactoringUpdate(this, fo);
            fileToUpdateMap.put(fo, update);
        }
        return update;
    }

    // -----

    static boolean isJavaFile(FileObject fo) {
        return "text/x-java".equals(fo.getMIMEType()); // NOI18N
    }

    static boolean isJavaFileOfForm(FileObject fo) {
        return isJavaFile(fo) && fo.existsExt("form"); // NOI18N
    }
}
