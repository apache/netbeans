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
package org.netbeans.modules.refactoring.api;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.refactoring.spi.FiltersManager;
import org.netbeans.modules.refactoring.spi.FiltersManager.Filterable;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/** Interface representing a refactoring element (object affected by a refactoring)
 * returned in a collection from {@link org.netbeans.modules.refactoring.api.AbstractRefactoring#prepare} operation.
 * <p>
 *
 * @see RefactoringElementImplementation
 * @author Martin Matula
 */
public final class RefactoringElement {
    /** Status corresponding to a normal element */
    public static final int NORMAL = 0;
    /** Status corresponding to an element that has a warning associated with it */
    public static final int WARNING = 1;
    /** Status flag that indicates that the element cannot be enabled (if a fatal
     * problem is associated with it) */
    public static final int GUARDED = 2;
    /** This element is in read-only file */
    public static final int READ_ONLY = 3;
    
    // delegate
    final RefactoringElementImplementation impl;
    
    RefactoringElement(RefactoringElementImplementation impl) {
        Parameters.notNull("impl", impl); // NOI18N
        this.impl = impl;
    }
    
    /** Returns text describing the refactoring element.
     * @return Text.
     */
    @NonNull
    public String getText() {
        return impl.getText();
    }
    
    /** Returns text describing the refactoring formatted for display (using HTML tags).
     * @return Formatted text.
     */
    @NonNull
    public String getDisplayText() {
        return impl.getDisplayText();
    }
    
    /** Indicates whether this refactoring element is enabled.
     * @return <code>true</code> if this element is enabled, otherwise <code>false</code>.
     */
    public boolean isEnabled() {
        return impl.isEnabled();
    }
    
    /** Enables/disables this element.
     * @param enabled If <code>true</code> the element is enabled, otherwise it is disabled.
     */
    public void setEnabled(boolean enabled) {
        impl.setEnabled(enabled);
    }
    
    /** 
     * Returns Lookup associated with this element.
     * Lookup items might be used by TreeElementFactories to build refactoring
     * preview trees.
     * @see org.netbeans.modules.refactoring.spi.ui.TreeElement
     * @see org.netbeans.modules.refactoring.spi.ui.TreeElementFactoryImplementation 
     * @return Lookup.
     */
    @NonNull
    public Lookup getLookup() {
        return impl.getLookup();
    }
    
    /** Returns file that the element affects (relates to)
     * @return File
     */
    public FileObject getParentFile() {
        return impl.getParentFile();
    }
    
    /** Returns position bounds of the text to be affected by this refactoring element.
     * @return position bounds
     */
    public PositionBounds getPosition() {
        return impl.getPosition();
    }
    
    /** Returns the status of this refactoring element (whether it is a normal element,
     * or a warning.
     * @return Status of this element.
     */
    public int getStatus() {
        return impl.getStatus();
    }
    
    /**
     * Shows this element in refactoring preview are
     * @see org.netbeans.modules.refactoring.spi.ui.UI#setComponentForRefactoringPreview
     */
    public void showPreview() {
        impl.showPreview();
    }
    
    /**
     * opens this RefactoringElement in the editor
     */
    public void openInEditor() {
        impl.openInEditor();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RefactoringElement other = (RefactoringElement) obj;
        if (this.impl != other.impl && (this.impl == null || !this.impl.equals(other.impl))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (this.impl != null ? this.impl.hashCode() : 0);
        return hash;
    }

    /**
     * Indicates if this element should be included in the results.
     * @param filtersManager the FiltersManager to use
     * @return true if this element should be included
     * @since 1.29
     */
    public boolean include(FiltersManager filtersManager) {
        if(impl instanceof FiltersManager.Filterable) {
            Filterable filterable = (Filterable) impl;
            return filterable.filter(filtersManager);
        }
        return true;
    }
}
