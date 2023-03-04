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
import java.util.Collections;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.impl.CannotUndoRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import static org.netbeans.modules.refactoring.plugins.Bundle.*;

/**
 *
 * @author  Jan Becicka
 */
public class FileRenamePlugin implements RefactoringPlugin {
    private RenameRefactoring refactoring;
    
    /** Creates a new instance of WhereUsedQuery */
    public FileRenamePlugin(RenameRefactoring refactoring) {
        this.refactoring = refactoring;
    }
    
    @Override
    public Problem preCheck() {
        return null;
    }
    
    @Override
    public Problem prepare(RefactoringElementsBag elements) {
        elements.addFileChange(refactoring, new RenameFile(refactoring.getRefactoringSource().lookup(FileObject.class), elements));
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
    
    public class RenameFile extends SimpleRefactoringElementImplementation {
        
        private FileObject fo;
        public RenameFile(FileObject fo, RefactoringElementsBag bag) {
            this.fo = fo;
        }
        
        @Override
        @NbBundle.Messages({"TXT_RenameFile=Rename file {0}",
                            "TXT_RenameFolder=Rename folder {0}"})
        public String getText() {
            return fo.isFolder()? TXT_RenameFolder(fo.getNameExt()) :
                                  TXT_RenameFile(fo.getNameExt());
        }

        @Override
        public String getDisplayText() {
            return getText();
        }

        private String oldName;
        
        @Override
        public void performChange() {
            try {
                oldName = fo.getName();
                DataObject.find(fo).rename(refactoring.getNewName());
            } catch (DataObjectNotFoundException ex) {
                throw new IllegalStateException(ex);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        
        @Override
        public void undoChange(){
            try {
                if (!fo.isValid()) {
                    throw new CannotUndoRefactoring(Collections.singleton(fo.getPath()));
                }
                DataObject.find(fo).rename(oldName);
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
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
            return fo;
        }

        @Override
        public PositionBounds getPosition() {
            return null;
        }
    }

}
