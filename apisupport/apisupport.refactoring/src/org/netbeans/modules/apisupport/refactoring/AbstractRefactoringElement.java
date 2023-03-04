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

package org.netbeans.modules.apisupport.refactoring;

import javax.swing.text.Position;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 *
 * @author Milos Kleint
 */
public abstract class AbstractRefactoringElement extends SimpleRefactoringElementImplementation implements RefactoringElementImplementation {
    
    private int status = RefactoringElementImplementation.NORMAL;

    protected String name;
    protected final FileObject parentFile;
    protected boolean enabled = true;

    protected AbstractRefactoringElement(@NonNull FileObject parentFile) {
        Parameters.notNull("parentFile", parentFile);
        this.parentFile = parentFile;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getText() {
        return getDisplayText();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public FileObject getParentFile() {
        return parentFile;
    }
    
    /** start and end positions of text (must be 2-element array); default [0, 0] */
    protected int[] location() {
        return new int[] {0, 0};
    }
    private int[] loc; // cached

    public PositionBounds getPosition() {
        try {
            DataObject dobj = DataObject.find(getParentFile());
            if (dobj != null) {
                EditorCookie.Observable obs = (EditorCookie.Observable)dobj.getCookie(EditorCookie.Observable.class);
                if (obs instanceof CloneableEditorSupport) {
                    CloneableEditorSupport supp = (CloneableEditorSupport)obs;

                    if (loc == null) {
                        loc = location();
                    }
                PositionBounds bounds = new PositionBounds(
                        supp.createPositionRef(loc[0], Position.Bias.Forward),
                        supp.createPositionRef(Math.max(loc[0], loc[1]), Position.Bias.Forward)
                        );
                
                return bounds;
            }
            }
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void performChange() { }
    
    public void undoChange() { }
    
    public Lookup getLookup() {
        return Lookup.EMPTY;
    }
    
}
