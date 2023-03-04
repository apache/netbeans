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

package org.netbeans.api.editor.fold;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.modules.editor.fold.FoldOperationImpl;
import org.netbeans.modules.editor.fold.FoldChildren;
import org.netbeans.modules.editor.fold.FoldUtilitiesImpl;
import org.netbeans.modules.editor.fold.HierarchyErrorException;

/**
 * Fold is a building block of the code folding tree-based hierarchy.
 * <br>
 * Folds cannot overlap but they can be nested arbitrarily.
 * <br>
 * It's possible to determine the fold's type, description
 * and whether it is collapsed or expanded
 * and explore the nested (children) folds.
 * <br>
 * There are various useful utility methods for folds in the {@link FoldUtilities}.
 *
 * <p>
 * There is one <i>root fold</i> at the top of the code folding hierarchy.
 * <br>
 * The root fold is special uncollapsable fold covering the whole document.
 * <br>
 * It can be obtained by using {@link FoldHierarchy#getRootFold()}.
 * <br>
 * The regular top-level folds are children of the root fold.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class Fold {
    
    private static final Fold[] EMPTY_FOLD_ARRAY = new Fold[0];
    
    private static final String DEFAULT_DESCRIPTION = "..."; // NOI18N
    
    private final FoldOperationImpl operation; // + 4

    private final FoldType type;   // + 4 = 8
    
    /**
     * Flags to encode fold state. A damaged to a fold will be never undone.
     */
    private static final byte FLAG_COLLAPSED = FoldUtilitiesImpl.FLAG_COLLAPSED;
    private static final byte FLAG_START_DAMAGED = FoldUtilitiesImpl.FLAG_START_DAMAGED;
    private static final byte FLAG_END_DAMAGED = FoldUtilitiesImpl.FLAG_END_DAMAGED;
    
    private volatile byte flags;     // + 4 = 12
    
    // TODO: consider offloading description to FoldTemplate, especially if a FoldTemplate pointer is added
    // to Fold object.
    private String description;    // + 4 = 16
    
    private Fold parent;           // + 4 = 20
    
    private FoldChildren children; // + 4 = 24
    
    private int rawIndex;          // + 4 = 28
     
    // TODO: these two are typically provided by a FoldTemplate; especially after the Fold receives
    // a FoldTemplate pointer, we could get rid of these two numbers.
    
    private int startGuardedLength; // +4 = 32
    private int endGuardedLength;   // + 4 = 36
    
    private Position startPos;  // + 4 = 40
    private Position endPos;    // + 4 = 44
    
    private Object extraInfo;
    

    Fold(FoldOperationImpl operation,
    FoldType type, String description, boolean collapsed,
    Document doc, int startOffset, int endOffset,
    int startGuardedLength, int endGuardedLength,
    Object extraInfo)
    throws BadLocationException {

        if (startGuardedLength < 0) {
            throw new IllegalArgumentException("startGuardedLength=" // NOI18N
                + startGuardedLength + " < 0"); // NOI18N
        }
        if (endGuardedLength < 0) {
            throw new IllegalArgumentException("endGuardedLength=" // NOI18N
                + endGuardedLength + " < 0"); // NOI18N
        }
        if (startOffset >= endOffset) {
            throw new IllegalArgumentException("startOffset=" + startOffset + " >= endOffset=" + endOffset); // NOI18N
        }
        if ((endOffset - startOffset) < (startGuardedLength + endGuardedLength)) {
            throw new IllegalArgumentException("(endOffset=" + endOffset // NOI18N
                + " - startOffset=" + startOffset + ") < " // NOI18N
                + "(startGuardedLength=" + startGuardedLength // NOI18N
                + " + endGuardedLength=" + endGuardedLength + ")" // NOI18N
            ); // NOI18N
        }
        
        this.operation = operation;
        this.type = type;

        this.flags = collapsed ? FLAG_COLLAPSED : 0;
        this.description = description;

        this.startPos = doc.createPosition(startOffset);
        this.endPos = doc.createPosition(endOffset);

        this.startGuardedLength = startGuardedLength;
        this.endGuardedLength = endGuardedLength;
        
        this.extraInfo = extraInfo;
    }

    /**
     * Get type of this fold.
     *
     * @return non-null type identification of this fold.
     */
    public FoldType getType() {
        return type;
    }
    
    /**
     * Get parent fold of this fold.
     *
     * @return parent fold of this fold or null if this is root fold or if this
     *  fold was removed from the code folding hierarchy.
     *  <br>
     *  {@link FoldUtilities#isRootFold(Fold)} can be used to check
     *  whether this is root fold.
     */
    public Fold getParent() {
    	return parent;
    }
    
    void setParent(Fold parent) {
        if (isRootFold()) {
            throw new IllegalArgumentException("Cannot set parent on root"); // NOI18N
        } else {
            this.parent = parent;
        }
    }
    /**
     * Get the code folding hierarchy for which this fold was created.
     *
     * @return non-null code folding hierarchy for which this fold was constructed.
     */
    public FoldHierarchy getHierarchy() {
    	return operation.getHierarchy();
    }
    
    FoldOperationImpl getOperation() {
        return operation;
    }
    
    /**
     * Check whether this fold is currently a part of the hierarchy.
     * <br>
     * The fold may be temporarily removed from the hierarchy because
     * it became blocked by another fold. Once the blocking fold gets
     * removed the original fold becomes a part of the hierarchy again.
     *
     * @return true if the fold is actively a part of the hierarchy.
     */
/*    public boolean isHierarchyPart() {
    	return (getParent() != null) || isRootFold();
    }
 */
    
    boolean isRootFold() {
        return (operation.getManager() == null);
    }
    
    /**
     * Get an absolute starting offset of this fold in the associated document.
     * <br>
     * The starting offset is expected to track possible changes in the underlying
     * document (i.e. it's maintained
     * in {@link javax.swing.text.Position}-like form).
     *
     * @return &gt;=0 absolute starting offset of this fold in the document.
     */
    public int getStartOffset() {
        return (isRootFold()) ? 0 : startPos.getOffset();
    }
    
    void setStartOffset(Document doc, int startOffset)
    throws BadLocationException {
        if (isRootFold()) {
            throw new IllegalStateException("Cannot set endOffset of root fold"); // NOI18N
        } else {
            this.startPos = doc.createPosition(startOffset);
        }
    }

    /**
     * Get an absolute ending offset of this fold in the associated document.
     * <br>
     * The ending offset is expected to track possible changes in the underlying
     * document (i.e. it's maintained
     * in {@link javax.swing.text.Position}-like form).
     *
     * @return <code>&gt;=getStartOffset()</code>
     *  offset of the first character in the document that is not part
     *  of this fold.
     */
    public int getEndOffset() {
        return isRootFold()
            ? operation.getHierarchy().getComponent().getDocument().getLength() + 1
            : endPos.getOffset();
    }
    
    void setEndOffset(Document doc, int endOffset)
    throws BadLocationException {
        if (isRootFold()) {
            throw new IllegalStateException("Cannot set endOffset of root fold"); // NOI18N
        } else {
            this.endPos = doc.createPosition(endOffset);
        }
    }

    /**
     * Return whether this fold is collapsed or expanded.
     * <br>
     * To collapse fold {@link FoldHierarchy#collapse(Fold)}
     * can be used.
     *
     * @return true if this fold is collapsed or false if it's expanded.
     */
    public boolean isCollapsed() {
    	return (flags & FLAG_COLLAPSED) > 0;
    }
    
    void setCollapsed(boolean collapsed) {
        if (isRootFold()) {
            throw new IllegalStateException("Cannot set collapsed flag on root fold."); // NOI18N
        }
        this.flags = (byte)((this.flags & ~FLAG_COLLAPSED) | (collapsed ? FLAG_COLLAPSED : 0));
    }

    /**
     * Get text description that should be displayed when the fold
     * is collapsed instead of the text contained in the fold.
     * <br>
     * If there is no specific description the "..." is returned.
     *
     * @return non-null description of the fold.
     */
    public String getDescription() {
    	return (description != null) ? description : DEFAULT_DESCRIPTION;
    }
    
    void setDescription(String description) {
    	this.description = description;
    }

    /**
     * Get total count of child folds contained in this fold.
     *
     * @return count of child folds contained in this fold.
     *  Zero means there are no children folds under this fold.
     */
    public int getFoldCount() {
    	return (children != null) ? children.getFoldCount() : 0;
    }

    /**
     * Get child fold of this fold at the given index.
     *
     * @param index &gt;=0 &amp;&amp; &lt;{@link #getFoldCount()}
     *  index of child of this fold.
     */
    public Fold getFold(int index) {
        if (children != null) {
            return children.getFold(index);
        } else { // no children exist
            throw new IndexOutOfBoundsException("index=" + index // NOI18N
            + " but no children exist."); // NOI18N
        }
    }
    
    Fold[] foldsToArray(int index, int count) {
        if (children != null) {
            return children.foldsToArray(index, count);
        } else { // no children
            if (count == 0) {
                return EMPTY_FOLD_ARRAY;
            } else { // invalid count
                throw new IndexOutOfBoundsException("No children but count=" // NOI18N
                    + count);
            }
        }
    }

    /**
     * Remove the given folds and insert them as children
     * of the given fold which will be put to their place.
     *
     * @param index index at which the starts the area of child folds to wrap.
     * @param length number of child folds to wrap.
     * @param fold fold to insert at place of children. The removed children
     *  become children of the fold.
     */
    void extractToChildren(int index, int length, Fold fold) {
        if (fold.getFoldCount() != 0 || fold.getParent() != null) {
            throw new IllegalStateException();
        }
        if (length != 0) { // create FoldChildren instance for the extracted folds
            fold.setChildren(children.extractToChildren(index, length, fold));
        } else { // no children to extract -> insert the single child
            if (children == null) {
                children = new FoldChildren(this);
            }
            children.insert(index, fold); // insert the single child fold
        }
    }

    private static final Logger LOG = Logger.getLogger(Fold.class.getName()); 
    /**
     * Remove the fold at the given index
     * and put its children at its place.
     *
     * @param index index at which the child should be removed
     * @return the removed child at the index.
     */
    Fold replaceByChildren(int index) {
        Fold fold = getFold(index);
        FoldChildren foldChildren = fold.getChildren();
        boolean check = false;
        assert check = true;
        if (check && foldChildren != null) {
            Fold[] folds = foldChildren.foldsToArray(0, foldChildren.getFoldCount());
            Fold toRemove = fold;
            if (folds.length > 0) {
                int ps = getStartOffset();
                int pe = getEndOffset();
                for (Fold f : folds) {
                    int fs = f.getStartOffset();
                    int fe = f.getEndOffset();
                    if (fs < ps || fe > pe) {
                        foldChildren.throwHierarchyError(toRemove, index, false, "Illegal attempt to replace-by-children fold");
                    }
                }
            }
        }
        fold.setChildren(null);
        children.replaceByChildren(index, foldChildren);
        return fold;
    }
    
    private FoldChildren getChildren() {
        return children;
    }
    
    void setChildren(FoldChildren children) {
        this.children = children;
    }
    
    Object getExtraInfo() {
        return extraInfo;
    }
    
    void setExtraInfo(Object info) {
        this.extraInfo = info;
    }

    /**
     * Get index of the given child fold in this fold.
     * <br>
     * The method has constant-time performance.
     *
     * @param child non-null child fold of this fold but in general
     *  it can be any non-null fold (see return value).
     * @return &gt;=0 index of the child fold in this fold
     *  or -1 if the given child fold is not a child of this fold.
     */
    public int getFoldIndex(Fold child) {
        return (children != null) ? children.getFoldIndex(child) : -1;
    }

    /**
     * Start of the fold content past the guarded area.
     * For root fold, returns 0. For other folds, returns the offset
     * following the guarded start of the fold (typically a delimiter).
     * 
     * @return offset in document
     * @since 1.35
     */
    public int getGuardedStart() {
        if (isRootFold()) {
            return 0;
        } else if (isZeroStartGuardedLength()) {
            return getStartOffset();
        } else {
            return getStartOffset() + startGuardedLength;
        }
    }
    
    /**
     * End of the fold content before the guarded area.
     * For root fold, returns 0. For other folds, returns the last offset
     * preceding the guarded start of the fold (typically a delimiter).
     * 
     * @return offset in document
     * @since 1.35
     */
    public int getGuardedEnd() {
        if (isRootFold()) {
            return getEndOffset();
        } else if (isZeroEndGuardedLength()) {
            return getEndOffset();
        } else {
            return getEndOffset() - endGuardedLength;
        }
    }
    
    int getStartGuardedLength() {
        return startGuardedLength;
    }
    
    int getEndGuardedLength() {
        return endGuardedLength;
    }

    private boolean isZeroStartGuardedLength() {
        return (startGuardedLength == 0);
    }
    
    private boolean isZeroEndGuardedLength() {
        return (endGuardedLength == 0);
    }
    
    /**
     * Return true if the starting guarded area is damaged by a document modification.
     */
    boolean isStartDamaged() {
        return (!isZeroStartGuardedLength() // no additional check if zero guarded length
                && (flags & FLAG_START_DAMAGED) > 0);
    }
    
    /**
     * Return true if the ending guarded area is damaged by a document modification.
     */
    boolean isEndDamaged() {
        return (!isZeroEndGuardedLength() // no additional check if zero guarded length
                && (flags & FLAG_END_DAMAGED) > 0);
    }
    
    /**
     * Get the raw index of this fold in the parent.
     * <br>
     * The SPI clients should never call this method.
     */
    int getRawIndex() {
        return rawIndex;
    }
    
    /**
     * Set the raw index of this fold in the parent.
     * <br>
     * The SPI clients should never call this method.
     */
    void setRawIndex(int rawIndex) {
        this.rawIndex = rawIndex;
    }
    
    /**
     * Update the raw index of this fold in the parent by a given delta.
     * <br>
     * The SPI clients should never call this method.
     */
    void updateRawIndex(int rawIndexDelta) {
        this.rawIndex += rawIndexDelta;
    }
    
    public String toString() {
        return FoldUtilitiesImpl.foldToString(this) + ", [" + getStartOffset()// NOI18N
            + ", " + getEndOffset() + "] {" // NOI18N
            + getGuardedStart() + ", " // NOI18N
            + getGuardedEnd() + '}'; // NOI18N
    }
    
    void setDamaged(byte f) {
        flags = (byte)((flags & (byte)~FoldUtilitiesImpl.FLAGS_DAMAGED) | f);
    }
    
}
