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
 * Information about state changes made in a particular fold.
 * <br>
 * Zero or more of the state change instances can be part of a particular
 * {@link FoldHierarchyEvent}.
 *
 * <p>
 * It can be extended to carry additional information specific to particular fold
 * types.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class FoldStateChange {

    private static final int COLLAPSED_CHANGED_BIT = 1;

    private static final int START_OFFSET_CHANGED_BIT = 2;

    private static final int END_OFFSET_CHANGED_BIT = 4;
    
    private static final int DESCRIPTION_CHANGED_BIT = 8;


    private Fold fold;

    private int stateChangeBits;
    
    private int originalStartOffset = -1;

    private int originalEndOffset = -1;
    
    /**
     * Construct state change.
     * @param fold fold being changed.
     */
    FoldStateChange(Fold fold) {
        this.fold = fold;
    }
    
    /**
     * Get the fold that has changed its state.
     */
    public Fold getFold() {
        return fold;
    }

    /**
     * Has the collapsed flag of the fold
     * (returned by <code>getFold()</code>) changed?
     *
     * @return true if the collapsed flag has changed in the fold
     *  or false otherwise.
     */
    public boolean isCollapsedChanged() {
        return ((stateChangeBits & COLLAPSED_CHANGED_BIT) != 0);
    }
    
    /**
     * Has the start offset of the fold
     * (returned by <code>getFold()</code>) changed?
     *
     * @return true if the start offset has changed in the fold
     *  or false otherwise.
     */
    public boolean isStartOffsetChanged() {
        return ((stateChangeBits & START_OFFSET_CHANGED_BIT) != 0);
    }
    
    /**
     * Return the original start offset of the fold prior
     * to change to the current start offset that the fold has now.
     * <br>
     * @return original start offset or -1 if the start offset was not changed
     *  for the fold.
     */
    public int getOriginalStartOffset() {
        return originalStartOffset;
    }

    /**
     * Has the end offset of the fold
     * (returned by <code>getFold()</code>) changed?
     *
     * @return true if the end offset has changed in the fold
     *  or false otherwise.
     */
    public boolean isEndOffsetChanged() {
        return ((stateChangeBits & END_OFFSET_CHANGED_BIT) != 0);
    }
    
    /**
     * Return the original end offset of the fold prior
     * to change to the current end offset that the fold has now.
     * <br>
     * @return original end offset or -1 if the end offset was not changed
     *  for the fold.
     */
    public int getOriginalEndOffset() {
        return originalEndOffset;
    }

    /**
     * Has the text description of the collapsed fold
     * (returned by <code>getFold()</code>) changed?
     *
     * @return true if the collapsed text description has changed in the fold
     *  or false otherwise.
     */
    public boolean isDescriptionChanged() {
        return ((stateChangeBits & DESCRIPTION_CHANGED_BIT) != 0);
    }

    /**
     * Mark that collapsed flag has changed
     * for the fold.
     */
    void collapsedChanged() {
        stateChangeBits |= COLLAPSED_CHANGED_BIT;
    }
    
    /**
     * Mark that start offset has changed
     * for the fold.
     */
    void startOffsetChanged(int originalStartOffset) {
        stateChangeBits |= START_OFFSET_CHANGED_BIT;
        this.originalStartOffset = originalStartOffset;
    }
    
    /**
     * Subclasses can mark that end offset has changed
     * for the fold.
     */
    void endOffsetChanged(int originalEndOffset) {
        stateChangeBits |= END_OFFSET_CHANGED_BIT;
        this.originalEndOffset = originalEndOffset;
    }
    
    /**
     * Subclasses can mark that collapsed flag has changed
     * for the fold.
     */
    void descriptionChanged() {
        stateChangeBits |= DESCRIPTION_CHANGED_BIT;
    }
    
    public String toString() {
        return org.netbeans.modules.editor.fold.FoldUtilitiesImpl.foldStateChangeToString(this);
    }

}
