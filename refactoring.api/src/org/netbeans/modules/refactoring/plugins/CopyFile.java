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
package org.netbeans.modules.refactoring.plugins;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.refactoring.api.Context;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Ralph Ruijs
 */
public class CopyFile extends SimpleRefactoringElementImplementation {

    private FileObject fo;
    private DataObject newOne;
    private final URL target;
    private final String newName;
    private final Context context;

    public CopyFile(FileObject fo, URL target, String newName, Context context) {
        this.fo = fo;
        this.target = target;
        this.newName = newName;
        this.context = context;
    }

    @Override
    public String getText() {
        return NbBundle.getMessage(CopyFile.class, "TXT_CopyFile", fo.getNameExt());
    }

    @Override
    public String getDisplayText() {
        return getText();
    }

    @Override
    public void performChange() {
        try {
            FileObject targetFo = FileHandlingFactory.getOrCreateFolder(target);
            FileObject Fo = fo;
            DataObject dob = DataObject.find(Fo);
            newOne = dob.copy(DataFolder.findFolder(targetFo));
            if(newName != null) {
                newOne.rename(newName);
            }
            FileObject[] newFiles = context.lookup(FileObject[].class);
            FileObject newFile = newOne.getPrimaryFile();
            newFile.setAttribute("originalFile", fo.getNameExt()); //NOI18N
            if (newFiles == null) {
                newFiles = new FileObject[]{newFile};
            } else {
                // rather a special case: there can be files from former run of the refactoring,
                // which had been undone. In that case, those files may be invalid and will cause
                // parser errors if processed.
                List<FileObject> stillValidFiles = new ArrayList<>(newFiles.length);
                for (FileObject f : newFiles) {
                    if (f.isValid()) {
                        stillValidFiles.add(f);
                    }
                }
                newFiles = new FileObject[stillValidFiles.size() + 1];
                stillValidFiles.toArray(newFiles);
                newFiles[newFiles.length - 1] = newFile;
            }
            context.add(newFiles);
            context.add(newFile);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void undoChange() {
        try {
            if (newOne != null) {
                newOne.delete();
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    @Override
    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    @Override
    public FileObject getParentFile() {
        return fo;
    }

    @Override
    public PositionBounds getPosition() {
        return null;
    }
}
