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
package org.netbeans.modules.refactoring.java.spi;

import java.io.IOException;
import java.lang.ref.WeakReference;
import javax.swing.text.Position;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.modules.refactoring.java.ui.tree.ElementGripFactory;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Implementation of RefactoringElementImplementation specific to refactoring
 * in java files.
 * 
 * @author Jan Becicka
 */
 public final class DiffElement extends SimpleRefactoringElementImplementation {
    private PositionBounds bounds;
    private String displayText;
    private FileObject parentFile;
    private Difference diff;
    private ModificationResult modification;
    private WeakReference<String> newFileContent;
    
    private DiffElement(Difference diff, PositionBounds bounds, FileObject parentFile, ModificationResult modification) {
        this.bounds = bounds;
        final String description = diff.getDescription();
        if (description == null) {
            displayText = NbBundle.getMessage(DiffElement.class, "LBL_NotAvailable");
        } else {
            this.displayText = description;
        }
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
            if (result!=null) {
                return result;
            }
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
        newFileContent = new WeakReference<String>(result);
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
        Position start = diff.getStartPosition();
        Position end = diff.getEndPosition();
        PositionBounds bounds = null;
        if (diff.getKind() != Difference.Kind.CREATE) {
            // FIXME - unnecessary dependency on openide.text
            bounds = new PositionBounds((PositionRef)start, (PositionRef)end);
        }
        return new DiffElement(diff, bounds, fileObject, modification);
    }    
}
