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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.fold.FoldRegistry;
import org.netbeans.modules.editor.fold.FoldUtilitiesImpl;

/**
 * Various utility methods for dealing with the folds.
 *
 * <p>
 * <b>Note:</b> Until explicitly noted all the utility methods
 * require a lock to be held on the {@link FoldHierarchy}
 * during execution of the methods.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class FoldUtilities {

    private FoldUtilities() {
        // No instances
    }
    
    /**
     * Is the given fold a root fold?
     *
     * @param fold non-null fold which is either root fold or a regular fold.
     * @return true if the given fold is root fold or false otherwise.
     */
    public static boolean isRootFold(Fold fold) {
        return fold.isRootFold();
    }
    
    /**
     * Find index of the child of the given fold that 
     * starts right at or below the given offset.
     *
     * <p>
     * This method uses binary search and has log2(n) performance
     * where n is number of children of the given fold.
     * <br>
     * The efficiency may drop to linear if there would be many empty folds
     * at the given offset.
     *
     * @param fold fold which children will be inspected.
     * @param offset &gt;=0 offset in the document for which the representing
     *  child will be searched.
     * @return index of the child fold that represents the given offset.
     *  <br>
     *  An <code>index</code> is returned
     *    if <code>offset &gt;= getFold(index).getStartOffset()</code>
     *    and <code>offset &lt;= getFold(index + 1).getStartOffset()</code>.
     *  <br>
     *  <code>-1</code> is returned
     *    if <code>offset &lt; getFold(0).getStartOffset()</code>
     *    and in case the fold does not have any children.
     */
     public static int findFoldStartIndex(Fold fold, int offset) {
         // The empty folds should be removed immediately (prior to notification
         // to managers) so the "first" param should not matter
         return FoldUtilitiesImpl.findFoldStartIndex(fold, offset, true);
     }
     
    /**
     * Find index of the first child of the given fold that ends
     * above the given offset ("contains" the offset).
     *
     * <p>
     * This method uses binary search and has log2(n) performance
     * where n is number of children of the given fold.
     * <br>
     * The efficiency may drop to linear if there would be many empty folds
     * at the given offset.
     *
     * @param fold fold which children will be inspected.
     * @param offset &gt;=0 offset in the document for which the representing
     *  child will be searched.
     * @return index of the child fold that contains or is above the given offset.
     *  <br>
     *  A highest <code>index</code> is returned for which
     *    <code>offset &lt; getFold(index).getEndOffset()</code>
     *  <br>
     *  or <code>fold.getFoldCount()</code> in case there is no such fold.
     */
     public static int findFoldEndIndex(Fold fold, int offset) {
         return FoldUtilitiesImpl.findFoldEndIndex(fold, offset);
     }
     
     /**
      * Check whether the starting offset of the fold is the same like
      * its ending offset.
      *
      * @param fold fold that should be checked whether it's empty.
      * @return true if the fold is empty or false otherwise.
      */
     public static boolean isEmpty(Fold fold) {
         return (fold.getStartOffset() == fold.getEndOffset());
     }
    
    /**
     * Collapse all folds in the hierarchy.
     * <br>
     * This method does the necessary locking of the document and hierarchy.
     * 
     * @param hierarchy hierarchy under which all folds should be collapsed.
     */
    public static void collapseAll(FoldHierarchy hierarchy) {
        collapse(hierarchy, (Collection)null);
    }

    /**
     * Collapse all folds of the given type.
     * <br>
     * This method does the necessary locking of the document and hierarchy.
     * 
     * @param hierarchy hierarchy under which the folds should be collapsed.
     * @param type folds with this type will be collapsed.
     */
    public static void collapse(FoldHierarchy hierarchy, FoldType type) {
        collapse(hierarchy, Collections.singleton(type));
    }

    /**
     * Collapse all folds that having any
     * of the fold types in the given collection.
     * <br>
     * This method does the necessary locking of the document and hierarchy.
     * 
     * @param hierarchy hierarchy under which the folds should be collapsed.
     * @param foldTypes collection of fold types to search for.
     */
    public static void collapse(FoldHierarchy hierarchy, Collection foldTypes) {
        FoldUtilitiesImpl.collapseOrExpand(hierarchy, foldTypes, true);
    }

    /**
     * Expand all folds in the hierarchy.
     * <br>
     * This method does the necessary locking of the document and hierarchy.
     * 
     * @param hierarchy hierarchy under which all folds should be expanded.
     */
    public static void expandAll(FoldHierarchy hierarchy) {
        expand(hierarchy, (Collection)null);
    }

    /**
     * Expand all folds of the given type.
     * <br>
     * This method does the necessary locking of the document and hierarchy.
     * 
     * @param hierarchy hierarchy under which the folds should be expanded.
     * @param type folds with this type will be expanded.
     */
    public static void expand(FoldHierarchy hierarchy, FoldType type) {
        expand(hierarchy, Collections.singleton(type));
    }

    /**
     * Expand all folds of the given type (or all folds if the type is null)
     * found in the whole fold hierarchy.
     * <br>
     * This method does the necessary locking of the document and hierarchy.
     * 
     * @param hierarchy hierarchy under which the folds should be expanded.
     * @param foldTypes collection of fold types to search for.
     */
    public static void expand(FoldHierarchy hierarchy, Collection foldTypes) {
        FoldUtilitiesImpl.collapseOrExpand(hierarchy, foldTypes, false);
    }

    /**
     * Check whether fold contains the given offset.
     *
     * @param fold fold to be tested for containing the given offset
     * @param offset that will be tested for being contained in the given fold.
     * @return true if <code>offset &gt;= fold.getStartOffset()
     *  &amp;&amp; offset &lt; fold.getEndOffset()</code>
     */
    public static boolean containsOffset(Fold fold, int offset) {
        return (offset < fold.getEndOffset() && offset >= fold.getStartOffset());
    }
    
    /**
     * Return children of the given fold as array.
     *
     * @param fold fold which children will be returned.
     * @return non-null array of all child folds.
     */
    public static Fold[] childrenToArray(Fold fold) {
        return childrenToArray(fold, 0, fold.getFoldCount());
    }

    /**
     * Return children of the given fold as array.
     *
     * @param fold fold which children will be returned.
     * @param index &gt;=0 index of the first child to be returned.
     * @param count &gt;=0 number of children to be returned.
     *  <code>index + count &lt;= {@link Fold#getFoldCount()}</code>.
     * @return non-null array of selected child folds.
     */
    public static Fold[] childrenToArray(Fold fold, int index, int count) {
        return fold.foldsToArray(index, count);
    }
    
    /**
     * Return children of the given fold as modifiable list.
     * <br>
     * {@link #findRecursive(Fold)} can be used
     * to collect children recursively.
     *
     * @param fold fold which children will be returned.
     * @return non-null modifiable list of all child folds.
     */
    public static List childrenAsList(Fold fold) {
        return childrenAsList(fold, 0, fold.getFoldCount());
    }
    
    /**
     * Return children of the given fold as list.
     *
     * @param fold fold which children will be returned.
     * @param index &gt;=0 index of the first child to be returned.
     * @param count &gt;=0 number of children to be returned.
     *  <code>index + count &lt;= {@link Fold#getFoldCount()}</code>.
     * @return non-null list of selected child folds.
     *  <br>
     *  The list can potentially be further modified by the caller without
     *  any effect on the fold hierarchy.
     */
    public static List childrenAsList(Fold fold, int index, int count) {
        return FoldUtilitiesImpl.childrenAsList(fold, index, count);
    }
    
    /**
     * Find direct subfolds of the given fold having certain type.
     * <br>
     * Complexity corresponds to number of direct child folds under the given fold.
     *
     * @param fold direct children of this fold will be searched.
     *  The search is *not* recursive in grandchildren etc.
     * @param foldType non-null fold type to search for.
     * @return non-null list of folds matching the criteria.
     *  <br>
     *  The list can potentially be further modified by the caller without
     *  any effect on the fold hierarchy.
     */
    public static List find(Fold fold, FoldType foldType) {
        return find(fold, Collections.singletonList(foldType));
    }
    
    /**
     * Find direct subfolds of the given fold having any
     * of the fold types in the given collection.
     * <br>
     * Complexity corresponds to number of direct child folds under the given fold.
     *
     * @param fold direct children of this fold will be searched.
     *  The search is *not* recursive in grandchildren etc.
     * @param foldTypes collection of fold types to search for.
     * @return non-null list of folds matching the criteria.
     *  <br>
     *  The list can potentially be further modified by the caller without
     *  any effect on the fold hierarchy.
     */
    public static List find(Fold fold, Collection foldTypes) {
        return FoldUtilitiesImpl.find(fold, foldTypes);
    }
    
    /**
     * Collect all children of the given fold recursively.
     * <br>
     * Complexity corresponds to number of all child folds
     * (including grandchildren etc.) under the given fold.
     *
     * @param fold all children of this fold will be collected.
     * @return non-null list of folds matching the criteria.
     *  <br>
     *  The list can potentially be further modified by the caller without
     *  any effect on the fold hierarchy.
     */
    public static List findRecursive(Fold fold) {
        return findRecursive(fold, (Collection)null);
    }
    
    /**
     * Recursively find any subfolds of the given fold having certain type.
     * <br>
     * Complexity corresponds to number of all child folds
     * (including grandchildren etc.) under the given fold.
     *
     * @param fold all children of this fold will be searched.
     *  The search is recursive into grandchildren etc.
     * @param foldType non-null fold type to search for.
     * @return non-null list of folds matching the criteria.
     *  <br>
     *  The list can potentially be further modified by the caller without
     *  any effect on the fold hierarchy.
     */
    public static List findRecursive(Fold fold, FoldType foldType) {
        return findRecursive(fold, Collections.singletonList(foldType));
    }
    
    /**
     * Recursively find any subfolds of the given fold having any
     * of the fold types in the given collection.
     * <br>
     * Complexity corresponds to number of all child folds
     * (including grandchildren etc.) under the given fold.
     *
     * @param fold all children of this fold will be searched.
     *  The search is recursive into grandchildren etc.
     * @param foldTypes collection of fold types to search for.
     * @return non-null list of folds matching the criteria.
     *  <br>
     *  The list can potentially be further modified by the caller without
     *  any effect on the fold hierarchy.
     */
    public static List findRecursive(Fold fold, Collection foldTypes) {
        return FoldUtilitiesImpl.findRecursive(null, fold, foldTypes);
    }
    
    /**
     * Find the fold nearest to the given offset. If the offset is positive or zero, finds the nearest
     * fold that starts at or follows the offset. If the offset is negative, the method finds the nearest
     * fold that ends at or before the negated offset value. Returns {@code null} if such a fold cannot be found,
     * that is if no fold follows (or precedes, for negative offset values) the offset.
     * <br>
     * The search deep-dives into hierarchy.
     * 
     * @param offset offset in a document, or negated value of offset to indicate backwards search.
     * @return fold in the hierarchy that is the nearest to the input offset.
     *  The most important is the lowest distance of the start (end) of the fold
     *  to the given offset. If there would be a nearest fold having a first child that
     *  starts at the same position like the parent
     *  then the parent would be returned. For backwards search, the deepest child will be returned.
     * 
     * @since 1.45 backward search added
     */
    public static Fold findNearestFold(FoldHierarchy hierarchy, int offset) {
        if (offset < 0) {
            return FoldUtilitiesImpl.findNearestFoldBackwards(hierarchy, offset, Integer.MIN_VALUE);
        } else {
            return FoldUtilitiesImpl.findNearestFold(hierarchy, offset, Integer.MAX_VALUE);
        }
        
    }
    
    /** 
     * Find a deepest fold in the hierarchy which contains the offset
     * or has it as one of its boundaries.
     * <br>
     * The search deep-dives into hierarchy.
     *
     * @param offset &gt=0 offset in a document.
     * @return deepset fold in the hierarchy satisfying
     *  <code>fold.getStartOffset() >= offset && offset <= fold.getEndOffset()</code>
     *  or null if there is no such fold (except the root fold) satisfying the condition.
     *  <br>
     *  For two consecutive folds (one ending at the offset and the next one
     *  starting at the offset) the latter fold would be returned.
     */
    public static Fold findOffsetFold(FoldHierarchy hierarchy, int offset) {
        return FoldUtilitiesImpl.findOffsetFold(hierarchy, offset);
    }

    /**
     * Find a first collapsed fold by going from top-level folds to more nested ones
     * within the requested bounds. Once a collapsed fold is found it is returned
     * (its children even if they would be collapsed are not inspected).
     *
     * @param hierarchy hierarchy in which to search.
     * @param startOffset &gt;=0 only fold ending above it will be returned.
     * @param endOffset &gt;=0 only fold starting below it will be returned.
     * @return collapsed fold satisfying
     *  <code>fold.getEndOffset() > startOffset and fold.getStartOffset() < endOffset</code>
     *  or null if such fold does not exist.
     */
    public static Fold findCollapsedFold(FoldHierarchy hierarchy,
    int startOffset, int endOffset) {

        return FoldUtilitiesImpl.findFirstCollapsedFold(hierarchy, startOffset, endOffset);
    }

    /**
     * Get iterator over the collapsed folds.
     *
     * @param hierarchy hierarchy in which to search.
     * @param startOffset &gt;=0 only folds ending above it will be returned.
     * @param endOffset &gt;=0 only folds starting before it will be returned.
     * @return iterator over collapsed folds satisfying
     *  <code>fold.getEndOffset() > startOffset and fold.getStartOffset() < endOffset</code>
     *  <br>
     *  If a particular collapsed fold gets returned then its children
     *  are not deep-dived for collapsed folds. Instead the search continues
     *  by a following sibling.
     */
    public static Iterator collapsedFoldIterator(FoldHierarchy hierarchy,
    int startOffset, int endOffset) {

        return FoldUtilitiesImpl.collapsedFoldIterator(hierarchy, startOffset, endOffset);
    }
    
    /**
     * Obtains available FoldType values for the specified MIME type.
     * 
     * @param mimeType mime type of the content
     * @return available FoldTypes
     * @since 1.35
     */
    public static FoldType.Domain  getFoldTypes(String mimeType) {
        return FoldRegistry.get().getDomain(MimePath.parse(mimeType));
    }
    
    /**
     * Determines whether folds of that type are should be initially collapsed.
     * The FoldType is evaluated in the context of a specific FoldHierarchy, that is a Component
     * with a content of a certain MIME type.
     * 
     * @param ft FoldType to inspect
     * @param hierarchy context for evaluation
     * @return true, if folds of FoldType should be initially collapsed.
     * @since 1.35
     */
    public static boolean isAutoCollapsed(FoldType ft, FoldHierarchy hierarchy) {
        return FoldUtilitiesImpl.isAutoCollapsed(ft, hierarchy);
    }
    
    /**
     * Determines whether folding is enabled for a given MIME type.
     * Use {@link MimePath#EMPTY}.getMimeType() to query for the default (all languages)
     * setting.
     * 
     * @param mimeType the MIME type of the content. 
     * @return true, if folding is enabled, false otherwise.
     * @since 1.35
     */
    public static boolean isFoldingEnabled(String mimeType) {
        return FoldUtilitiesImpl.isFoldingEnabled(mimeType);
    }
}
