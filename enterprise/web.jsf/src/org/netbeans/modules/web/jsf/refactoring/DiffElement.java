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
package org.netbeans.modules.web.jsf.refactoring;

import java.io.IOException;
import java.lang.ref.WeakReference;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.web.jsf.refactoring.Modifications.Difference;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Petr Pisl
 */
public abstract class DiffElement extends SimpleRefactoringElementImplementation {
    private final Difference diff;
    private final Modifications modification;
    private WeakReference<String> newFileContent;
    private final FileObject parentFile;
    
    public DiffElement(Difference diff, FileObject parentFile, Modifications modification) {
        this.diff = diff;
        this.modification = modification;
        this.parentFile = parentFile;
    }
    
    public String getDisplayText(){
        return diff.getDesription();
    }
    
    public String getText(){
        return diff.getDesription();
    }
    
    public Lookup getLookup() {
        return Lookups.fixed(parentFile, diff);
    }
    
    public void setEnabled(boolean enabled) {
        diff.setExclude(!enabled);
        newFileContent = null;
        super.setEnabled(enabled);
    }
    
    public FileObject getParentFile() {
        return parentFile;
    }
    
    @Override
    protected String getNewFileContent() {
        String result;
        if (newFileContent !=null) {
            result = newFileContent.get();
            if (result!=null)
                return result;
        }
        try {
            result = modification.getResultingSource(parentFile);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        newFileContent = new WeakReference(result);
        return result;
    }
    
    
    public static class ChangeFQCNElement extends DiffElement{
        private final Occurrences.OccurrenceItem occurence;
        
        public ChangeFQCNElement(Difference diff, Occurrences.OccurrenceItem occurence, Modifications modification) {
            super(diff, occurence.getFacesConfig(), modification);
            this.occurence = occurence;
        }
        
        public void performChange() {
            try {
                occurence.performChange();
            } catch (IllegalStateException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        @Override
        public void undoChange() {
            try {
                occurence.undoChange();
            } catch (IllegalStateException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        public PositionBounds getPosition() {
            return occurence.getChangePosition();
        }
    }
}
