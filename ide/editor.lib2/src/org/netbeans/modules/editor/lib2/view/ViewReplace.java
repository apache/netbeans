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
