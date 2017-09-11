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

package org.netbeans.api.editor.fold;

/**
 * Event describing the changes done in the hierarchy.
 * <br>
 * The structural changes are described by the lists of added and removed folds.
 * <br>
 * State changes are described by a list of {@link FoldStateChange}s.
 * <br>
 * In addition there is a description of the offset range that was
 * affected by the change. This is useful for the editor
 * to recreate the affected views and repaint the affected area.
 * <p>
 * Added folds can have either collapsed or expanded initial state.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class FoldHierarchyEvent extends java.util.EventObject {
    
    private Fold[] removedFolds;
    
    private Fold[] addedFolds;
    
    private FoldStateChange[] foldStateChanges;

    private int affectedStartOffset;
    
    private int affectedEndOffset;

    /**
     * Create new FoldHierarchyEvent.
     * <br>
     * It's guaranteed that no passed arrays contents will be modified.
     *
     * @param source FoldHierarchy for which this event gets created.
     * @param removedFolds non-null array of removed folds.
     * @param addedFolds non-null array of added folds.
     * @param foldStateChanges changes describing changes in the state
     *  of particular folds.
     * @param affectedStartOffset first offset in the document affected by this change.
     * @param affectedEndOffset end of the offset area affected by this change.
     */
    FoldHierarchyEvent(FoldHierarchy source,
    Fold[] removedFolds, Fold[] addedFolds,
    FoldStateChange[] foldStateChanges, int affectedStartOffset, int affectedEndOffset) {

        super(source);

        this.removedFolds = removedFolds;
        this.addedFolds = addedFolds;
        this.foldStateChanges = foldStateChanges;
        this.affectedStartOffset = affectedStartOffset;
        this.affectedEndOffset = affectedEndOffset;
    }
    
    /**
     * Get the number of folds removed from the hierarchy.
     *
     * @return &gt;=0 number of removed folds.
     */
    public int getRemovedFoldCount() {
        return removedFolds.length;
    }

    /**
     * Get the fold with the given index removed
     * from the fold hierarchy.
     *
     * @param removedFoldIndex &gt;=0 and &lt;{@link #getRemovedFoldCount()}
     *   index of the removed fold.
     */
    public Fold getRemovedFold(int removedFoldIndex) {
        return removedFolds[removedFoldIndex];
    }

    /**
     * Get the number of folds that were added to the hierarchy.
     *
     * @return &gt;=0 number of added folds.
     */
    public int getAddedFoldCount() {
        return addedFolds.length;
    }

    /**
     * Get the fold with the given index added
     * to the hierarchy.
     *
     * @param addedFoldIndex &gt;=0 and &lt;{@link #getAddedFoldCount()}
     *   index of the added fold.
     */
    public Fold getAddedFold(int addedFoldIndex) {
        return addedFolds[addedFoldIndex];
    }

    /**
     * Get the number of the fold state changes contained in this event.
     *
     * @return &gt;=0 number of fold state changes.
     */
    public int getFoldStateChangeCount() {
        return foldStateChanges.length;
    }
    
    /**
     * Get the fold state change at the given index.
     *
     * @param index &gt;=0 and &lt;{@link #getFoldStateChangeCount()}
     *  index of the fold state change.
     */
    public FoldStateChange getFoldStateChange(int index) {
        return foldStateChanges[index];
    }
    
    /**
     * Get the first offset in the underlying document affected
     * by this change.
     *
     * @return &gt;=0 first offset affected by the change.
     */
    public int getAffectedStartOffset() {
        return affectedStartOffset;
    }
    
    /**
     * Get the ending offset in the offset area affected
     * by this change.
     *
     * @return &gt;={@link #getAffectedStartOffset()} 
     *   end of the offset area affected by the change.
     */
    public int getAffectedEndOffset() {
        return affectedEndOffset;
    }

    public String toString() {
        return org.netbeans.modules.editor.fold.FoldUtilitiesImpl.foldHierarchyEventToString(this);
    }

}
