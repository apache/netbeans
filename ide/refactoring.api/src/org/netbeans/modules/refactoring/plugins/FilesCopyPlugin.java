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

import java.net.URL;
import java.util.Collection;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.CopyRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.SingleCopyRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ralph Ruijs
 */
public class FilesCopyPlugin implements RefactoringPlugin {
    private AbstractRefactoring refactoring;
    
    /** Creates a new instance of WhereUsedQuery */
    public FilesCopyPlugin(AbstractRefactoring refactoring) {
        this.refactoring = refactoring;
    }
    
    @Override
    public Problem preCheck() {
        return null;
    }
    
    @Override
    public Problem prepare(RefactoringElementsBag elements) {
        URL target = null;
        String newName = null;
        if(refactoring instanceof SingleCopyRefactoring) {
            SingleCopyRefactoring scr = (SingleCopyRefactoring) refactoring;
            target = scr.getTarget().lookup(URL.class);
            newName = scr.getNewName();
        } else if(refactoring instanceof CopyRefactoring) {
            CopyRefactoring scr = (CopyRefactoring) refactoring;
            target = scr.getTarget().lookup(URL.class);
        }
        
        Collection<? extends FileObject> fileObjects = refactoring.getRefactoringSource().lookupAll(FileObject.class);
        for (FileObject fileObject : fileObjects) {
            elements.add(refactoring, new CopyFile(fileObject, target, newName, refactoring.getContext()));
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
}
