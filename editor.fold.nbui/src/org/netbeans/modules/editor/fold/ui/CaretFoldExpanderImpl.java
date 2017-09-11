/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
                        Collections.sort(posList, PositionComparator.INSTANCE);
                        endOffset = posList.get(posListSize - 1).getOffset();
                    } else {
                        endOffset = offset;
                    }
                    Iterator collapsedFoldIterator = FoldUtilities.collapsedFoldIterator(foldHierarchy, offset, endOffset);
                    List foldsToExpand;
                    Fold lastFold;
                    boolean lastFoldExpandAdded = false;
                    if (collapsedFoldIterator.hasNext()) {
                        lastFold = (Fold) collapsedFoldIterator.next();
                        foldsToExpand = new ArrayList(2);
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
                                    lastFold = (Fold) collapsedFoldIterator.next();
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
            Iterator collapsedFoldIterator = FoldUtilities.collapsedFoldIterator(foldHierarchy, offset, offset);
            if (collapsedFoldIterator.hasNext()) {
                Fold fold = (Fold) collapsedFoldIterator.next();
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
