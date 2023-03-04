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

package org.netbeans.modules.editor.fold;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.editor.fold.FoldStateChange;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.editor.mimelookup.MimeLookup;

/**
 * Implementations of methods from {@link org.netbeans.api.editor.fold.FoldUtilities}.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class FoldUtilitiesImpl {
    /**
     * Prefix used for initial-collapse folding preferences.
     */
    public static final String PREF_COLLAPSE_PREFIX = "code-folding-collapse-";
    
    /**
     * Preference key name for "use defaults" (default: true)
     */
    public static final String PREF_OVERRIDE_DEFAULTS = "code-folding-use-defaults"; // NOI18N
    
    /**
     * Preference key name for enable code folding (default: true)
     */
    public static final String PREF_CODE_FOLDING_ENABLED = "code-folding-enable"; // NOI18N
    
    /**
     * Preference key for "Content preview" display option (default: true).
     */
    public static final String PREF_CONTENT_PREVIEW = "code-folding-content.preview"; // NOI18N

    /**
     * Preference key for "Show summary" display option (default: true).
     */
    public static final String PREF_CONTENT_SUMMARY = "code-folding-content.summary"; // NOI18N
    
    private FoldUtilitiesImpl() {
        // No instances
    }
    
    public static boolean isFoldingEnabled(String mime) {
        Preferences prefs = MimeLookup.getLookup(mime).lookup(Preferences.class);
        return prefs == null ? false : prefs.getBoolean(PREF_CODE_FOLDING_ENABLED, false);
    }
    
    public static boolean isFoldingEnabled(FoldHierarchy h) {
        Preferences p = ApiPackageAccessor.get().foldGetExecution(h).getFoldPreferences();
        return p.getBoolean(PREF_CODE_FOLDING_ENABLED, false);
    }

    public static boolean isAutoCollapsed(FoldType ft, FoldHierarchy h) {
        Preferences p = ApiPackageAccessor.get().foldGetExecution(h).getFoldPreferences();
        FoldType parent = ft.parent();
        return p.getBoolean(
            PREF_COLLAPSE_PREFIX + ft.code(),
            parent == null ? 
                false : 
                // search for the parent, if parent is defined.
                p.getBoolean(PREF_COLLAPSE_PREFIX + parent.code(), false)
        );
    }
    
    public static void collapseOrExpand(FoldHierarchy hierarchy, Collection foldTypes,
    boolean collapse) {

        Document d = hierarchy.getComponent().getDocument();
        if (!(d instanceof AbstractDocument)) {
            // no op, the folding hierarchy does not work for != AbstractDocument
            return;
        }
        AbstractDocument adoc = (AbstractDocument)d;
        adoc.readLock();
        try {
            hierarchy.lock();
            try {
                List foldList = findRecursive(null,
                    hierarchy.getRootFold(), foldTypes);
                if (collapse) {
                    hierarchy.collapse(foldList);
                } else {
                    hierarchy.expand(foldList);
                }
            } finally {
                hierarchy.unlock();
            }
        } finally {
            adoc.readUnlock();
        }
    }

    public static int findFoldStartIndex(Fold fold, int offset, boolean first) {
        int foldCount = fold.getFoldCount();
        int low = 0;
        int high = foldCount - 1;
        
        while (low <= high) {
            int mid = (low + high) / 2;
            Fold midFold = fold.getFold(mid);
            int midFoldStartOffset = midFold.getStartOffset();
            
            if (midFoldStartOffset < offset) {
                low = mid + 1;
            } else if (midFoldStartOffset > offset) {
                high = mid - 1;
            } else {
                // fold starting exactly at the given offset found
                if (first) { // search for first fold
                    mid--;
                    while (mid >= 0 && fold.getFold(mid).getStartOffset() == offset) {
                        mid--;
                    }
                    mid++;
                    
                } else { // search for last fold
                    mid++;
                    // Search for fold with startOffset greater than offset
                    while (mid < foldCount && fold.getFold(mid).getStartOffset() == offset) {
                        mid++;
                    }
                    mid--;
                }
                return mid;
            }
        }
        return high;
    }

    /**
     * Find a hint index of where a child fold should be inserted in its parent.
     *
     * @param fold fold into which the child fold should be inserted.
     * @param childStartOffset starting offset of the child to be inserted.
     * @return hint index at which the child fold should be inserted.
     *  <br>
     *  The client must additionally check whether the end offset
     *  of the preceding child fold does not overlap with the given child fold
     *  and if so then either remove the clashing fold or stop inserting
     *  the child fold.
     *  <br>
     *  The client must also check whether ending offset of the given child fold
     *  does not overlap with the starting offset of the following child fold.
     */
    public static int findFoldInsertIndex(Fold fold, int childStartOffset) {
        return findFoldStartIndex(fold, childStartOffset, false) + 1;
    }

    public static int findFoldEndIndex(Fold fold, int offset) {
        int foldCount = fold.getFoldCount();
        int low = 0;
        int high = foldCount - 1;
        
        while (low <= high) {
            int mid = (low + high) / 2;
            Fold midFold = fold.getFold(mid);
            int midFoldEndOffset = midFold.getEndOffset();
            
            if (midFoldEndOffset < offset) {
                low = mid + 1;
            } else if (midFoldEndOffset > offset) {
                high = mid - 1;
            } else {
                // fold ending exactly at the given offset found => move to next one above
                mid++;
                while (mid < foldCount && fold.getFold(mid).getEndOffset() <= offset) {
                    mid++;
                }
                return mid;
            }
        }
        return low;
    }

    public static List childrenAsList(Fold fold, int index, int count) {
        List l = new ArrayList(count);
        while (--count >= 0) {
            l.add(fold.getFold(index));
            index++;
        }
        return l;
    }

    public static List find(Fold fold, Collection foldTypes) {
        List l = new ArrayList();
        int foldCount = fold.getFoldCount();
        for (int i = 0; i < foldCount; i++) {
            Fold child = fold.getFold(i);
            if (foldTypes == null || foldTypes.contains(child.getType())) {
                l.add(child);
            }
        }
        return l;
    }
    
    public static List findRecursive(List l, Fold fold, Collection foldTypes) {
        if (l == null) {
            l = new ArrayList();
        }

        int foldCount = fold.getFoldCount();
        for (int i = 0; i < foldCount; i++) {
            Fold child = fold.getFold(i);
            if (foldTypes == null || foldTypes.contains(child.getType())) {
                l.add(child);
            }
            findRecursive(l, child, foldTypes);
        }
        return l;

    }
    
    /** Returns the fold at the specified offset. Returns null in case of root fold */
    public static Fold findOffsetFold(FoldHierarchy hierarchy, int offset) {
        int distance = Integer.MAX_VALUE;
        Fold rootFold = hierarchy.getRootFold();
        Fold fold = rootFold;
        
        boolean inspectNested = true;
        while (inspectNested) {
            int childIndex = findFoldStartIndex(fold, offset, false);
            if (childIndex >= 0) {
                Fold wrapFold = fold.getFold(childIndex);
                int startOffset = wrapFold.getStartOffset();
                int endOffset = wrapFold.getEndOffset();
                // This is not like containsOffset() because of "<= endOffset"
                if (startOffset <= offset && offset <= endOffset) {
                    fold = wrapFold;
                }else{
                    inspectNested = false;
                }
            } else { // no children => break
                inspectNested = false;
            }
        }
        return (fold != rootFold) ? fold : null;
    }
    
    public static Fold findNearestFoldBackwards(FoldHierarchy hierarchy, int offset, int beginMark) {
        offset = -offset;
        Fold nearestFold = null;
        int distance = Integer.MAX_VALUE;
        Fold fold = hierarchy.getRootFold();
        boolean inspectNested = true;
        
        while (inspectNested) {
            int childCount = fold.getFoldCount();
            int childIndex = findFoldStartIndex(fold, offset, true);
            if (childIndex < 0 || childIndex >= childCount) {
                break;
            }
            Fold precedingFold = fold.getFold(childIndex);
            int endOffset = precedingFold.getEndOffset();
            if (endOffset <= beginMark) { 
                break;
            }
            
            int dist = offset - endOffset;
            // equality will cause a child which ends at the same offset to 
            // replace the nearest fold
            if (dist <= distance) {
                nearestFold = precedingFold;
            } else {
                // children must be nested within, so their distance will be greater.
                break;
            }
            fold = precedingFold;
        }
        return nearestFold;
    }
    
    public static Fold findNearestFold(FoldHierarchy hierarchy, int offset, int endOffset) {
        Fold nearestFold = null;
        int distance = Integer.MAX_VALUE;
        Fold fold = hierarchy.getRootFold();
        
        boolean inspectNested = true;
        while (inspectNested) {
            int childCount = fold.getFoldCount();
            int childIndex = findFoldEndIndex(fold, offset);
            if (childIndex < childCount) {
                Fold wrapOrAfterFold = fold.getFold(childIndex);
                int startOffset = wrapOrAfterFold.getStartOffset();
                if (startOffset >= endOffset) { // starts at or after endOffset
                    break;
                }

                Fold afterFold; // fold after the offset
                if (startOffset < offset) { // starts below offset
                    childIndex++;
                    afterFold = (childIndex < childCount) ? fold.getFold(childIndex) : null;
                    // leave inspectNested to be true and prepare fold variable
                    fold = wrapOrAfterFold;
                    
                } else { // starts above offset
                    afterFold = wrapOrAfterFold;
                    inspectNested = false;
                }
                
                // Check whether the afterFold is the nearest
                if (afterFold != null) {
                    int afterFoldDistance = afterFold.getStartOffset() - offset;
                    if (afterFoldDistance < distance) {
                        distance = afterFoldDistance;
                        nearestFold = afterFold;
                    }
                }
                
            } else { // no children => break
                inspectNested = false;
            }
        }
        
        return nearestFold;
    }
    
    public static Fold findFirstCollapsedFold(FoldHierarchy hierarchy,
    int startOffset, int endOffset) {
        
        Fold fold = hierarchy.getRootFold();
        Fold lastFold = null;
        int lastIndex = 0;
        while (true) {
            // Find fold covering the startOffset
            int index = findFoldEndIndex(fold, startOffset);
            if (index >= fold.getFoldCount()) {
                if (lastFold != null) {
                    return findCollapsedRec(lastFold, lastIndex + 1, endOffset);
                } else { // root level - no satisfying folds
                    return null;
                }
                
            } else { // fold index within bounds
                Fold childFold = fold.getFold(index);
                if (childFold.isCollapsed()) { // return it if it's collapsed
                    return childFold;
                }

                if (childFold.getStartOffset() >= startOffset) { // do not nest
                    return findCollapsedRec(fold, index, endOffset);
                } else { // need to inspect children
                    lastFold = fold;
                    lastIndex = index;
                    fold = childFold;
                }
            }
        }
    }

    public static Iterator collapsedFoldIterator(FoldHierarchy hierarchy, int startOffset, int endOffset) {
        return new CollapsedFoldIterator(
            findFirstCollapsedFold(hierarchy, startOffset, endOffset),
            endOffset
        );
    }
    
    private static final class CollapsedFoldIterator implements Iterator {
        
        private Fold nextFold;
        
        private int endOffset;
        
        public CollapsedFoldIterator(Fold nextFold, int endOffset) {
            this.nextFold = nextFold;
            this.endOffset = endOffset;
        }
        
        public boolean hasNext() {
            return (nextFold != null);
        }        
        
        public Object next() {
            Fold result = nextFold;
            nextFold = findNextCollapsedFold(nextFold, endOffset);
            return result;
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
        
    public static Fold findNextCollapsedFold(Fold fold, int endOffset) {
        if (FoldUtilities.isRootFold(fold)) { // start from the begining
            return findCollapsedRec(fold, 0, endOffset);

        } else { // continue from valid fold
            Fold parent = fold.getParent();
            return findCollapsedRec(parent, parent.getFoldIndex(fold) + 1, endOffset);
        }
    }
    
    private static Fold findCollapsedRec(Fold fold,
    int startIndex, int endOffset) {
        return findCollapsedRec(fold, startIndex, endOffset, true);
    }    
    
    private static Fold findCollapsedRec(Fold fold,
    int startIndex, int endOffset, boolean findInUpperLevel) {

        if (fold.getStartOffset() > endOffset) {
            return null;
        }

        int foldCount = fold.getFoldCount();
        while (startIndex < foldCount) {
            Fold child = fold.getFold(startIndex);
            if (child.isCollapsed()) {
                return child;
            } else {
                Fold maybeCollapsed = findCollapsedRec(child, 0, endOffset, false);
                if (maybeCollapsed != null) {
                    return maybeCollapsed;
                }
            }
            startIndex++;
        }

        // No child was found collapsed -> go one level up
        if (FoldUtilities.isRootFold(fold) || !findInUpperLevel) {
            return null;
        } else { // not root fold
            Fold parent = fold.getParent();
            return findCollapsedRec(parent, parent.getFoldIndex(fold) + 1, endOffset, true);
        }
    }
    
    public static String foldToString(Fold fold) {
        return "[" + fold.getType() + "] " // NOI18N
            + (fold.isCollapsed() ? "C" : "E")// NOI18N
            + (FoldUtilities.isRootFold(fold) ? "" : Integer.toString(
                ApiPackageAccessor.get().foldGetOperation(fold).getPriority()))
            + " <" + fold.getStartOffset() // NOI18N
            + "," + fold.getEndOffset() + ">" // NOI18N
            + (FoldUtilities.isRootFold(fold) ? "" : (", desc='" + fold.getDescription() + "'"))
            + ", hash=0x" + Integer.toHexString(System.identityHashCode(fold)); // NOI18N
    }
    
    public static void appendSpaces(StringBuffer sb, int spaces) {
        while (--spaces >= 0) {
            sb.append(' ');
        }
    }

    public static String foldToStringChildren(Fold fold, int indent) {
        indent += 4;
        StringBuffer sb = new StringBuffer();
        sb.append(fold);
        sb.append('\n');
        int foldCount = fold.getFoldCount();
        for (int i = 0; i < foldCount; i++) {
            appendSpaces(sb, indent);
            sb.append('[');
            sb.append(i);
            sb.append("]: "); // NOI18N
            sb.append(foldToStringChildren(fold.getFold(i), indent));
        }
        
        return sb.toString();
    }
    
    public static String foldHierarchyEventToString(FoldHierarchyEvent evt) {
        StringBuffer sb = new StringBuffer();
        int removedFoldCount = evt.getRemovedFoldCount();
        for (int i = 0; i < removedFoldCount; i++) {
            sb.append("R["); // NOI18N
            sb.append(i);
            sb.append("]: "); // NOI18N
            sb.append(evt.getRemovedFold(i));
            sb.append('\n');
        }
        
        int addedFoldCount = evt.getAddedFoldCount();
        for (int i = 0; i < addedFoldCount; i++) {
            sb.append("A["); // NOI18N
            sb.append(i);
            sb.append("]: "); // NOI18N
            sb.append(evt.getAddedFold(i));
            sb.append('\n');
        }
        
        int foldStateChangeCount = evt.getFoldStateChangeCount();
        for (int i = 0; i < foldStateChangeCount; i++) {
            FoldStateChange change = evt.getFoldStateChange(i);
            sb.append("SC["); // NOI18N
            sb.append(i);
            sb.append("]: "); // NOI18N
            sb.append(change);
            sb.append('\n');
        }
        if (foldStateChangeCount == 0) {
            sb.append("No FoldStateChange\n"); // NOI18N
        }
        
        sb.append("affected: <"); // NOI18N
        sb.append(evt.getAffectedStartOffset());
        sb.append(","); // NOI18N
        sb.append(evt.getAffectedEndOffset());
        sb.append(">\n"); // NOI18N
        
        return sb.toString();
    }
    
    public static String foldStateChangeToString(FoldStateChange change) {
        StringBuffer sb = new StringBuffer();
        if (change.isCollapsedChanged()) {
            sb.append("C"); // NOI18N
        }
        if (change.isDescriptionChanged()) {
            sb.append("D"); // NOI18N
        }
        if (change.isEndOffsetChanged()) {
            sb.append("E"); // NOI18N
        }
        sb.append(" fold="); // NOI18N
        sb.append(change.getFold());
        return sb.toString();
    }
    
    /**
     * Flags to encode fold state. A damaged to a fold will be never undone.
     */
    public static final byte FLAG_NOTHING_DAMAGED = 0;
    public static final byte FLAG_COLLAPSED = 1;
    public static final byte FLAG_START_DAMAGED = 1 << 1;
    public static final byte FLAG_END_DAMAGED = 1 << 2;
    public static final byte FLAGS_DAMAGED = FLAG_START_DAMAGED | FLAG_END_DAMAGED;

    /**
     * Determines whether the fold is damaged by insert operation.
     * Returns FLAG_ bitfield, which describes the damaged areas. For insert
     * the fold can be damaged only at the start or end, not at both places
     * 
     * @param f the fold to check
     * @param evt document change event
     * @return bitfield of damages. 0 means no damage.
     */
    public static int isFoldDamagedByInsert(Fold f, DocumentEvent evt) {
        int o = evt.getOffset();
        int s = f.getStartOffset();
        if (o < s) {
            return FLAG_NOTHING_DAMAGED;
        }
        int gs = f.getGuardedStart();
        if (gs == s) {
            gs++;
        }
        if (o >= s && o < gs) {
            return FLAG_START_DAMAGED;
        }
        // if insertion was done before fold's end pos, the end position has advanced by inserion length.
        int e = f.getEndOffset();
        if (o >= e) {
            return FLAG_NOTHING_DAMAGED;
        }
        int l = evt.getLength();
        int gel = ApiPackageAccessor.get().foldEndGuardedLength(f);
        e -= l;
        int ge = e - gel;
        
        if (o <= ge) {
            return FLAG_NOTHING_DAMAGED;
        }
        if (e == ge) {
            ge--;
        }
        if (o >= ge && o < e) {
            return FLAG_END_DAMAGED;
        } else {
            return FLAG_NOTHING_DAMAGED;
        }
    }

    /**
     * Checks whether fold's start or end guarded area become damaged by the edit
     * @param f fold to check
     * @param evt document event
     * @return FLAG_ bitfield
     */
    public static int becomesDamagedByRemove(Fold f, DocumentEvent evt, boolean zero) {
        ApiPackageAccessor api = ApiPackageAccessor.get();
        int fs = f.getStartOffset();
        int fe = f.getEndOffset();
        int gs = fs + api.foldStartGuardedLength(f);
        int ge = fe - api.foldEndGuardedLength(f);
        int removeStart = evt.getOffset();
        int removeEnd = removeStart + evt.getLength();
        
        if (zero) {
            if (gs == fs) {
                gs++;
            } else {
                gs = -1;
            }
            if (ge == fe) {
                ge--;
            } else {
                ge = removeEnd + 1;
            }
        } else {
            if (gs == fs) {
                gs = -1;
            }
            if (ge == fe) {
                ge = removeEnd + 1;
            }
        }
        int ret = FLAG_NOTHING_DAMAGED;
        
        if (removeStart < gs && removeEnd >= fs) {
            ret |= FLAG_START_DAMAGED;
        }
        if (removeStart < fe && removeEnd > ge) {
            ret |= FLAG_END_DAMAGED;
        }
        return ret;
    }

    /**
     * Determines whether the fold becomes empty after removal. Works only for remove events
     * 
     * @param f fold to check
     * @param evt document remove event
     * @return true, if the fold become empty after the mutation
     */
    public static boolean becomesEmptyAfterRemove(Fold f, DocumentEvent evt) {
        int s = evt.getOffset();
        int e = evt.getLength() + s;
        return s <= f.getStartOffset() &&
               e >= f.getEndOffset();
    }
}
