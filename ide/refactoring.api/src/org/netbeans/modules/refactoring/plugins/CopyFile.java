/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
