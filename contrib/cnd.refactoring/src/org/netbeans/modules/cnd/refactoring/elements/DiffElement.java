/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.refactoring.elements;

import org.netbeans.modules.cnd.refactoring.support.*;
import java.io.IOException;
import java.lang.ref.WeakReference;
import org.netbeans.modules.cnd.refactoring.support.ModificationResult.Difference;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
/**
 * copy of org.netbeans.modules.refactoring.java.spi.DiffElement
 * 
 * Implementatation of RefactoringElementImplementation specific to refactoring
 * in c/c++ files.
 * 
 */
public class DiffElement extends SimpleRefactoringElementImplementation {
    private final PositionBounds bounds;
    private final String displayText;
    private final FileObject parentFile;
    private final Difference diff;
    private final ModificationResult modification;
    private WeakReference<String> newFileContent;
    
    private DiffElement(Difference diff, PositionBounds bounds, FileObject parentFile, ModificationResult modification) {
        this.bounds = bounds;
        this.displayText = diff.getDescription();
        this.parentFile = parentFile;
        this.diff = diff;
        this.modification = modification;
    }

    @Override
    public String getDisplayText() {
        return displayText;
    }

    @Override
    public Lookup getLookup() {
        Object composite = null;
        if (bounds!=null) {
            composite = ElementGripFactory.getDefault().get(parentFile, bounds.getBegin().getOffset());
        }
        if (composite==null) {
            composite = parentFile;
        }
        return Lookups.fixed(composite, diff);
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        diff.exclude(!enabled);
        newFileContent = null;
        super.setEnabled(enabled);
    }

    @Override
    public PositionBounds getPosition() {
        return bounds;
    }

    @Override
    public String getText() {
        return displayText;
    }

    @Override
    public void performChange() {
    }

    @Override
    public FileObject getParentFile() {
        if (diff.getKind() == Difference.Kind.CREATE) {
            return parentFile.getParent();
        }
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
            if (diff.getKind()==Difference.Kind.CREATE) {
                result = diff.getNewText();
            } else {
                result = modification.getResultingSource(parentFile);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        newFileContent = new WeakReference<>(result);
        return result;
    }
    
    /**
     * Factory method for DiffElement
     * @param diff diff instance corresponding to thid Element
     * @param fileObject fileObject corresponding to this Element
     * @param modification 
     * @return ModificationResult corresponding to this change
     */
    public static DiffElement create(Difference diff, FileObject fileObject, ModificationResult modification) {
        PositionRef start = diff.getStartPosition();
        PositionRef end = diff.getEndPosition();
        PositionBounds bounds = null;
        if (diff.getKind() != Difference.Kind.CREATE) {
            bounds = new PositionBounds(start, end);
        }
        return new DiffElement(diff, bounds, fileObject, modification);
    }       
}
