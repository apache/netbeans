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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import org.netbeans.modules.refactoring.api.impl.CannotUndoRefactoring;
import org.netbeans.modules.refactoring.spi.BackupFacility;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Jan Becicka
 */
public class DeleteFile extends SimpleRefactoringElementImplementation {
    private final URL res;
    private final String filename;
    private final RefactoringElementsBag session;

    private BackupFacility.Handle id;

    /**
     * 
     * @param fo
     * @param session
     */
    public DeleteFile(FileObject fo, RefactoringElementsBag session) {
        this.res = fo.toURL();
        this.filename = fo.getNameExt();
        this.session = session;
    }

    @Override
    public String getText() {
        return NbBundle.getMessage(FileDeletePlugin.class, "TXT_DeleteFile", filename);
    }

    @Override
    public String getDisplayText() {
        return getText();
    }

    @Override
    public void performChange() {
        try {
            FileObject fo = URLMapper.findFileObject(res);
            if (fo == null) {
                throw new IOException(res.toString());
            }
            id = BackupFacility.getDefault().backup(fo);
            DataObject.find(fo).delete();
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void undoChange() {
        try {
            try {
                File f = Utilities.toFile(res.toURI());
                if (f.exists()) {
                    throw new CannotUndoRefactoring(Collections.singleton(f.getPath()));
                }
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
            id.restore();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    @Override
    public FileObject getParentFile() {
        return URLMapper.findFileObject(res);
    }

    @Override
    public PositionBounds getPosition() {
        return null;
    }
    
}
