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
