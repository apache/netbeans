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
package org.netbeans.modules.editor.fold.ui;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.lib.editor.util.swing.PositionComparator;
import org.netbeans.modules.editor.lib2.caret.CaretFoldExpander;

/**
 * Implementation of fold expander for editor caret.
 *
 * @author Miloslav Metelka
 */
public final class CaretFoldExpanderImpl extends CaretFoldExpander {

    @Override
    public void checkExpandFolds(JTextComponent c, List<Position> posList) {
        if (posList != null) {
            int posListSize = posList.size();
            if (posListSize > 0) {
                FoldHierarchy foldHierarchy = FoldHierarchy.get(c);
                foldHierarchy.lock();
                try {
                    int offset = posList.get(0).getOffset();
                    int endOffset;
                    if (posListSize > 1) {
                        posList.sort(PositionComparator.INSTANCE);
                        endOffset = posList.get(posListSize - 1).getOffset();
                    } else {
                        endOffset = offset;
                    }
                    Iterator<Fold> collapsedFoldIterator = FoldUtilities.collapsedFoldIterator(foldHierarchy, offset, endOffset);
                    List<Fold> foldsToExpand;
                    Fold lastFold;
                    boolean lastFoldExpandAdded = false;
                    if (collapsedFoldIterator.hasNext()) {
                        lastFold = collapsedFoldIterator.next();
                        foldsToExpand = new ArrayList<>(2);
                    } else {
                        lastFold = null;
                        foldsToExpand = null;
                    }
                    if (lastFold != null) {
                        int nextPosIndex = 1;
                        while (true) {
                            if (offset >= lastFold.getEndOffset()) {
                                // Fetch next fold
                                if (collapsedFoldIterator.hasNext()) {
                                    lastFold = collapsedFoldIterator.next();
                                    lastFoldExpandAdded = false;
                                } else {
                                    break;
                                }
                            }
                            if (offset > lastFold.getStartOffset()) { // Offset inside collapsed fold
                                if (!lastFoldExpandAdded) {
                                    lastFoldExpandAdded = true;
                                    foldsToExpand.add(lastFold);
                                }
                            }
                            if (nextPosIndex >= posListSize) {
                                break;
                            }
                            offset = posList.get(nextPosIndex).getOffset();
                            nextPosIndex++;
                        }
                        if (foldsToExpand.size() > 0) {
                            foldHierarchy.expand(foldsToExpand);
                        }
                    }
                } finally {
                    foldHierarchy.unlock();
                }
            }
        }
    }

    @Override
    public boolean checkExpandFold(JTextComponent c, Point p) {
        FoldHierarchy foldHierarchy = FoldHierarchy.get(c);
        foldHierarchy.lock();
        try {
            int offset = c.viewToModel(p);
            Iterator<Fold> collapsedFoldIterator = FoldUtilities.collapsedFoldIterator(foldHierarchy, offset, offset);
            if (collapsedFoldIterator.hasNext()) {
                Fold fold = collapsedFoldIterator.next();
                // Expand even if the offset is at fold's begining/end because that's what viewToModel() will return
                if (offset >= fold.getStartOffset() && offset <= fold.getEndOffset()) {
                    foldHierarchy.expand(fold);
                    return true;
                }
            }
            return false;
        } finally {
            foldHierarchy.unlock();
        }
    }
    
}
