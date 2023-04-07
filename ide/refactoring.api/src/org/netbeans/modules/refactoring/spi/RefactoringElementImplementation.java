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
package org.netbeans.modules.refactoring.spi;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;

/** Interface representing a refactoring element (object affected by a refactoring)
 * returned in a collection from {@link org.netbeans.modules.refactoring.api.AbstractRefactoring#prepare} operation.
 * <p>
 *
 * @author Martin Matula
 * @author Jan Becicka
 * @see RefactoringElement
 * @see SimpleRefactoringElementImplementation
 * @see RefactoringSession
 * @see RefactoringElementsBag
 */
public interface RefactoringElementImplementation {
    /** Status corresponding to a normal element */
    int NORMAL = RefactoringElement.NORMAL;
    /** Status corresponding to an element that has a warning associated with it */
    int WARNING = RefactoringElement.WARNING;
    /** Status flag that indicates that the element cannot be enabled (if a fatal
     * problem is associated with it) */
    int GUARDED = RefactoringElement.GUARDED;
    /** This element is in read-only file */
    int READ_ONLY = RefactoringElement.READ_ONLY;
    
    /** Returns text describing the refactoring element.
     * @return Text.
     */
    String getText();
    
    /** Returns text describing the refactoring formatted for display (using HTML tags).
     * @return Formatted text.
     */
    String getDisplayText();
    
    /** Indicates whether this refactoring element is enabled.
     * @return <code>true</code> if this element is enabled, otherwise <code>false</code>.
     */
    boolean isEnabled();
    
    /** Enables/disables this element.
     * @param enabled If <code>true</code> the element is enabled, otherwise it is disabled.
     */
    void setEnabled(boolean enabled);
    
    /** 
     *  Performs the change represented by this refactoring element.
     *  Implementation can be impty if the change is done using some high level
     *  transaction model
     * @see BackupFacility
     * @see RefactoringElementsBag#addFileChange
     * @see RefactoringElementsBag#registerTransaction
     * @see Transaction
     * @see RefactoringElementImplementation#performChange
     * @see RefactoringElementImplementation#undoChange
     */
    void performChange();
    
    /**
     *  Undo change done by performChange
     *  Implementation can be impty if the change is done using some high level
     *  transaction model
     * @see BackupFacility
     * @see RefactoringElementsBag#addFileChange
     * @see RefactoringElementsBag#registerTransaction
     * @see Transaction
     * @see RefactoringElementImplementation#performChange
     * @see RefactoringElementImplementation#undoChange
     */
    void undoChange();
    
    /** 
     * Returns Lookup associated with this element.
     * Lookup items might be used by TreeElementFactories to build refactoring
     * preview trees.
     * @see org.netbeans.modules.refactoring.spi.ui.TreeElement
     * @see org.netbeans.modules.refactoring.spi.ui.TreeElementFactory
     * @see org.netbeans.modules.refactoring.spi.ui.TreeElementFactoryImplementation
     * @return Lookup. Might be empty.
     */
    Lookup getLookup();
    
    /** Returns file that the element affects (relates to)
     * @return File
     */
    @NonNull FileObject getParentFile();
    
    /** Returns position bounds of the text to be affected by this refactoring element.
     * @return position bounds
     */
    PositionBounds getPosition();
    
    /** Returns the status of this refactoring element (whether it is a normal element,
     * or a warning.
     * @return Status of this element.
     */
    int getStatus();
    
    /**
     * Setter for property status
     * @param status new value of propery status
     */
    void setStatus(int status);
    
    /**
     * opens this RefactoringElement in the editor
     * @since 1.5.0
     */
    void openInEditor();
    
    /**
     * Shows this element in refactoring preview are
     * @see org.netbeans.modules.refactoring.spi.ui.UI#setComponentForRefactoringPreview
     */
    void showPreview();
    
}
