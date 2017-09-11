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

package org.netbeans.modules.editor.lib2.view;

import java.util.ArrayList;
import java.util.List;

/**
 * Information about views being replaced in a view.
 * 
 * @author Miloslav Metelka
 */

final class ViewReplace<V extends EditorView, CV extends EditorView> {

    final V view; // 8=super + 4 = 12 bytes

    int index; // 12 + 4 = 16 bytes

    private int removeCount; // 16 + 4 = 20 bytes

    private List<CV> added; // 20 + 4 = 24 bytes
    
    private final int childViewCount; // 24 + 4 = 28 bytes

    ViewReplace(V view) {
        assert (view != null);
        this.view = view;
        // Cache child view count since the view is not going to change during views rebuild.
        this.childViewCount = view.getViewCount();
    }
    
    void add(CV childView) {
        if (added == null) {
            added = new ArrayList<CV>();
        }
        added.add(childView);
    }

    int addedSize() {
        return (added != null) ? added.size() : 0;
    }

    List<CV> added() {
        return added;
    }

    EditorView[] addedViews() {
        EditorView[] views;
        if (added != null) {
            views = new EditorView[added.size()];
            added.toArray(views);
        } else {
            views = null;
        }
        return views;
    }

    int getRemoveCount() {
        return removeCount;
    }

    void setRemoveCount(int removeCount) {
        if (index + removeCount > childViewCount) {
            throw new IllegalStateException("removeCount=" + removeCount + ", this:\n" + this);
        }
        this.removeCount = removeCount;
    }

    int removeEndIndex() {
        return index + getRemoveCount();
    }
    
    int addEndIndex() {
        return index + addedSize();
    }
    
    void removeTillEnd() {
        setRemoveCount(childViewCount - index);
    }
    
    boolean isRemovedTillEnd() {
        return (index + removeCount == childViewCount);
    }

    boolean isChanged() {
        return (added != null) || (getRemoveCount() > 0);
    }
    
    boolean isMakingViewEmpty() {
        return index == 0 && getRemoveCount() == childViewCount && addedSize() == 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append(view.getDumpId());
        sb.append(": index=").append(index); // NOI18N
        sb.append(", remove=").append(getRemoveCount()); // NOI18N
        EditorView[] addedViews = addedViews();
        sb.append(", added="); // NOI18N
        if (addedViews != null && addedViews.length > 0) {
            sb.append(addedViews.length);
//            sb.append(", Added Views:\n");
//            int maxDigitCount = ArrayUtilities.digitCount(addedViews.length);
//            for (int i = 0; i < addedViews.length; i++) {
//                sb.append("    ");
//                ArrayUtilities.appendBracketedIndex(sb, i, maxDigitCount);
//                sb.append(addedViews[i].toString());
//                sb.append('\n');
//            }
        } else {
            sb.append("0");
        }
        if (!isChanged()) {
            sb.append(", NonChanged"); // NOI18N
        }
        sb.append('\n');
        return sb.toString();
    }

}
