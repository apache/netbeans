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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author  Jan Becicka
 */
public class FileMovePlugin implements RefactoringPlugin {
    private MoveRefactoring refactoring;
    
    /** Creates a new instance of WhereUsedQuery */
    public FileMovePlugin(MoveRefactoring refactoring) {
        this.refactoring = refactoring;
    }
    
    @Override
    public Problem preCheck() {
        return null;
    }
    
    @Override
    public Problem prepare(RefactoringElementsBag elements) {
        URL targetUrl = ((MoveRefactoring) refactoring).getTarget().lookup(URL.class);        
        if(targetUrl != null) {
            for (FileObject o: refactoring.getRefactoringSource().lookupAll(FileObject.class)) {
                elements.addFileChange(refactoring, new MoveFile(o, elements));
            }
        }
        return null;
    }
    
    @Override
    public Problem fastCheckParameters() {
        return null;
    }
        
    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    public void cancelRequest() {
    }
    
    public class MoveFile extends SimpleRefactoringElementImplementation {
        
        private FileObject fo;
        public MoveFile(FileObject fo, RefactoringElementsBag session) {
            this.fo = fo;
        }
        @Override
        public String getText() {
            return NbBundle.getMessage(FileMovePlugin.class, "TXT_MoveFile", fo.getNameExt());
        }

        @Override
        public String getDisplayText() {
            return getText();
        }
        
        DataFolder sourceFolder;
        DataObject source;
        @Override
        public void performChange() {
            try {
                FileObject target = FileHandlingFactory.getOrCreateFolder(refactoring.getTarget().lookup(URL.class));
                DataFolder targetFolder = DataFolder.findFolder(target);
                if (fo==null) {
                    Logger.getLogger(FileMovePlugin.class.getName()).severe("Invalid FileObject\n. File not found.");
                    return;
                } else if (!fo.isValid()) {
                    String path = FileUtil.getFileDisplayName(fo);
                    Logger.getLogger(FileMovePlugin.class.getName()).log(Level.FINE, "Invalid FileObject {0}.\n Trying to recreate...", path);
                    fo = FileUtil.toFileObject(FileUtil.toFile(fo));
                    if (fo==null) {
                        Logger.getLogger(FileMovePlugin.class.getName()).log(Level.SEVERE, "Invalid FileObject {0}.\n File not found.", path);
                        return;
                    }
                }
                source = DataObject.find(fo);
                sourceFolder = source.getFolder();
                source.move(targetFolder);
            } catch (DataObjectNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        @Override
        public void undoChange() {
            try {
                source.move(sourceFolder);
            } catch (DataObjectNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
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
}
