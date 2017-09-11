/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
     * @see org.netbeans.modules.refactoring.api.ui.UI#setComponentForRefactoringPreview
     */
    void showPreview();
    
}
