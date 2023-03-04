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

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.modules.editor.fold.ApiPackageAccessor;
import org.netbeans.lib.editor.util.GapList;

//import org.netbeans.spi.lexer.util.GapObjectArray;

/**
 * Manager of the children of a fold.
 * <br>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class FoldChildren extends GapList {
    private static final Logger LOG = Logger.getLogger(FoldChildren.class.getName()); 
    
    /**
     * Initial size of the index gap.
     */
    private static final int INITIAL_INDEX_GAP_LENGTH
        = Integer.MAX_VALUE >> 1;

    /**
     * Parent fold for the folds contained in this children instance.
     */
    Fold parent;

   /**
     * Index where the index gap resides.
     */
    private int indexGapIndex;

    /**
     * Length of the index gap in managed folds.
     * <br>
     * The initial gap length is chosen big enough
     * so that it's never reached.
     */
    private int indexGapLength;

    public FoldChildren(Fold parent) {
        this.parent = parent;
        indexGapLength = INITIAL_INDEX_GAP_LENGTH;
    }
    
    /**
     * Get total count of subfolds contained in this fold.
     *
     * @return count of subfolds contained in this fold.
     *  Zero means there are no subfolds under this fold.
     */
    public int getFoldCount() {
        return size();
    }

    /**
     * Get fold with the given index.
     *
     * @param index &gt;=0 &amp;&amp; &lt;{@link #getFoldCount()}
     *  index of the fold.
     */
    private static AtomicInteger invalidIndexHierarchySnapshot = new AtomicInteger();
    public Fold getFold(int index) {
        if (index >= getFoldCount() && invalidIndexHierarchySnapshot.get() == 0) {
            StringBuilder sb = new StringBuilder(200);
            sb.append("Invalid index=").append(index).append("; foldCount=").append(getFoldCount()).append('\n'); // NOI18N
            if (parent != null) {
                invalidIndexHierarchySnapshot.incrementAndGet();
                sb.append(parent.getHierarchy().toString());
                invalidIndexHierarchySnapshot.decrementAndGet();
            }
            throw new IndexOutOfBoundsException(sb.toString());
        }
        return (Fold)get(index);
    }
    
    public int getFoldIndex(Fold child) {
        int index = getTranslatedFoldIndex(ApiPackageAccessor.get().foldGetRawIndex(child));
        if (index < 0 || index >= getFoldCount() || getFold(index) != child) {
            index = -1;
        }
        return index;
    }
    
    public Fold[] foldsToArray(int index, int length) {
        Fold[] folds = new Fold[length];
        copyElements(index, index + length, folds, 0);
        return folds;
    }
    
    /**
     * Insert the given fold at the requested index.
     */
    public void insert(int index, Fold fold) {
        moveIndexGap(index);
        //ensureCapacity(1);
        insertImpl(index, fold);
    }
   
    /**
     * Insert the given folds at the requested index.
     */
    public void insert(int index, Fold[] folds) {
        moveIndexGap(index);
        insertImpl(index, folds);
    }

    public void remove(int index, int length) {
        moveIndexGap(index + length);
        for (int i = index + length - 1; i >= index; i--) {
            ApiPackageAccessor.get().foldSetParent(getFold(i), null);
        }
        super.remove(index, length);
        indexGapLength += length;
        indexGapIndex -= length;
    }
    
    /**
     * Extract given area of folds into new FoldChildren instance
     * parented by the given fold.
     *
     * @param index start of the area of folds to be extracted.
     * @param length length of the area of folds to be extracted.
     * @param fold fold that will own the newly created fold children.
     */
    public FoldChildren extractToChildren(int index, int length, Fold fold) {
        // sanity check:
        int ps = parent.getStartOffset();
        int pe = parent.getEndOffset();
        int fs = fold.getStartOffset();
        int fe = fold.getEndOffset();
        
        if (fs < ps || fe > pe) {
            throwHierarchyError(fold, index, true,
                    "Illegal attempt to reparent children");
        }
        
        
        FoldChildren foldChildren = new FoldChildren(fold);
        if (length == 1) {
            Fold insertFold = getFold(index);
            remove(index, length); // removal prior insertion to set children parents properly
            foldChildren.insert(0, insertFold);
        } else {
            Fold[] insertFolds = foldsToArray(index, length);
            remove(index, length); // removal prior insertion to set children parents properly
            foldChildren.insert(0, insertFolds);
        }
        
        // Insert the fold into list of current children
        insertImpl(index, fold);
        
        return foldChildren;
    }

    public void replaceByChildren(int index, FoldChildren children) {
        remove(index, 1);
        
        if (children != null) {
            // Index gap already moved by preceding remove()
            int childCount = children.getFoldCount();
            //ensureCapacity(childCount);
            insertImpl(index, children, 0, childCount);
        }
    }    

    private void insertImpl(int index, FoldChildren children,
    int childIndex, int childCount) {

        switch (childCount) {
            case 0: // nothing to do
                break;

            case 1: // single item insert
                insertImpl(index, children.getFold(childIndex));
                break;
                
            default: // multiple items insert
                Fold[] folds = children.foldsToArray(childIndex, childCount);
                insertImpl(index, folds);
                break;
        }
    }

    /**
     * Throws a special runtime, which causes the entire hierarchy to rebuild.
     * @param f the fold in error, or index of the operation
     * @param index index of the fold within the parent
     * @param b true for add, false for remove
     * @param s message
     */
    public void throwHierarchyError(Fold f, int index, boolean b, String s) {
        if (ApiPackageAccessor.get().foldGetExecution(this.parent.getHierarchy()).rebuilding) {
            return;
        }
        throw new HierarchyErrorException(parent, f, index, b, s);
    }

    public void throwHierarchyError(Fold parent, Fold f, int index, boolean b, String s) {
        if (ApiPackageAccessor.get().foldGetExecution(this.parent.getHierarchy()).rebuilding) {
            return;
        }
        throw new HierarchyErrorException(parent, f, index, b, s);
    }

    private void insertImpl(int index, Fold fold) {
        int ps = parent.getStartOffset();
        int pe = parent.getEndOffset();
        int fs = fold.getStartOffset();
        int fe = fold.getEndOffset();
        //sometimesThrow(fold, index, true, "eee");
        if (fs < ps || fe > pe) {
            throwHierarchyError(fold, index, true,
                    "Illegal attempt to insert fold");
        }
        indexGapLength--;
        indexGapIndex++;
        ApiPackageAccessor api = ApiPackageAccessor.get();
        api.foldSetRawIndex(fold, index);
        api.foldSetParent(fold, parent);
        add(index, fold);
    }
    
    private void insertImpl(int index, Fold[] folds) {
        // sanity check
        if (folds.length > 0) {
            int ps = parent.getStartOffset();
            int pe = parent.getEndOffset();
            for (Fold f : folds) {
                int fs = f.getStartOffset();
                int fe = f.getEndOffset();
                if (fs < ps || fe > pe) {
                    throwHierarchyError(f, index, true,
                        "Illegal attempt to insert fold"
                    );
                    break;
                }
            }
        }

        ApiPackageAccessor api = ApiPackageAccessor.get();
        int foldsLength = folds.length;
        indexGapLength -= foldsLength;
        indexGapIndex += foldsLength;
        for (int i = foldsLength - 1; i >= 0; i--) {
            Fold fold = folds[i];
            api.foldSetRawIndex(fold, index + i);
            api.foldSetParent(fold, parent);
        }
        addArray(index, folds);
    }
    
    private int getTranslatedFoldIndex(int rawIndex) {
        if (rawIndex >= indexGapLength) {
            rawIndex -= indexGapLength;
        }
        return rawIndex;
    }

    private void moveIndexGap(int index) {
        if (index != indexGapIndex) {
            ApiPackageAccessor api = ApiPackageAccessor.get();
            int gapLen = indexGapLength; // cache to local var
            if (index < indexGapIndex) { // fix back from indexGapIndex till index
                for (int i = indexGapIndex - 1; i >= index; i--) {
                    api.foldUpdateRawIndex(getFold(i), +gapLen);
                }

            } else { // index > indexGapIndex => fix up from indexGapIndex till index
                for (int i = indexGapIndex; i < index; i++) {
                    api.foldUpdateRawIndex(getFold(i), -gapLen);
                }
            }
            indexGapIndex = index;
        }
    }

}
