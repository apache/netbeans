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

package org.netbeans.modules.editor.lib.drawing;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import org.netbeans.lib.editor.view.GapMultiLineView;

/**
 * Possibly multi-line view containing one or more folds
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

/* package */ class FoldMultiLineView extends GapMultiLineView {
    
    /**
     * List containing pairs [fold, ending-line-elem]
     * where ending-line-elem is a line element
     * in which the fold ends.
     */
    private List foldAndEndLineElemList;
    
    FoldMultiLineView(Element firstLineElement, List foldAndEndLineElemList) {
        super(firstLineElement);
        this.foldAndEndLineElemList = foldAndEndLineElemList;
        
        int foldAndEndLineElemListSize = foldAndEndLineElemList.size();
// TODO uncomment        assert (foldAndEndLineElemListSize != 0 // non-empty
            // && ((foldAndEndLineElemListSize & 1) == 0)); // even
  
        setLastLineElement((Element)foldAndEndLineElemList.get(
            foldAndEndLineElemList.size() - 1));
    }

    private JTextComponent getTextComponent() {
        return (JTextComponent)getContainer();
    }
    
    protected @Override boolean useCustomReloadChildren() {
        return true;
    }
    
    protected @Override void reloadChildren(int index, int removeLength, int startOffset, int endOffset) {
        // TODO uncomment assert (index == 0 && removeLength == 0
            // && startOffset == getStartOffset() && endOffset == getEndOffset());

        // Rebuild all the present child views completely
        index = 0;
        removeLength = getViewCount();

        Element lineElem = getElement(); // starting line element
        View[] added = null;
        ViewFactory f = getViewFactory();
        if (f != null) {
            int lineElemEndOffset = lineElem.getEndOffset();
            // Ending offset of the previously created view - here start with
            //   begining of the first line
            int lastViewEndOffset = lineElem.getStartOffset();

            List childViews = new ArrayList();
            // Append ending fragment if necessary
            // asserted non-empty list => foldEndOffset populated
            if (lastViewEndOffset < lineElemEndOffset) { // need ending fragment
                View lineView = f.create(lineElem);
                View endingFrag = lineView.createFragment(lastViewEndOffset, lineElemEndOffset);
                childViews.add(endingFrag);
                // lastViewEndOffset = lineElemEndOffset;  <- can be ignored here
            }

            added = new View[childViews.size()];
            childViews.toArray(added);
        }

        
        replace(index, removeLength, added);
    }
    
}
