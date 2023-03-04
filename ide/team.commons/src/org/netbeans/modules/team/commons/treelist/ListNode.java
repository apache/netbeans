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
package org.netbeans.modules.team.commons.treelist;

import java.awt.Color;
import javax.swing.Action;
import javax.swing.JComponent;

/**
 * List model item which provides custom renderer.
 * 
 * @author S. Aubrecht
 * @see SelectionList
 * @see TreeList
 */
public abstract class ListNode {

    private ListRendererPanel renderer;

    private int lastRowWidth = -1;

    private ListListener listener;

    void setListener(ListListener listener) {
        this.listener = listener;
    }
    
    /**
     * @return Actions for popup menu, or null to disable popup menu.
     */
    public Action[] getPopupActions() {
        return null;
    }

    final JComponent getListRenderer(Color foreground, Color background, boolean isSelected, boolean hasFocus, int rowHeight, int rowWidth) {
        ListRendererPanel res = null;
        synchronized (this) {
            //hack - in case of resizing TC fire content changed to repaint
            if (lastRowWidth > rowWidth) {
                renderer = null;
            }
            this.lastRowWidth = rowWidth;
            if (null == renderer) {
                renderer = new ListRendererPanel(this);
            }
            res = renderer;
        }

        res.configure(foreground, background, isSelected, hasFocus, rowHeight, rowWidth);

        return res;
    }

    /**
     * Creates component that will render this node in TreeList. The component
     * will be wrapped in another component to add proper background, border and
     * expansion button.
     *
     * @param foreground
     * @param background
     * @param isSelected
     * @param hasFocus
     * @return Component to render this node.
     */
    protected abstract JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus, int rowWidth);

    /**
     * @return Action to invoke when Enter key is pressed on selected node in
     * TreeList.
     */
    protected Action getDefaultAction() {
        return null;
    }

    /**
     * Invoked when the node is added to the model. All listeners should be
     * added here.
     */
    protected void attach() {
    }

    void fireContentChanged() {
        synchronized (this) {
            renderer = null;
        }
        if (null != listener) {
            listener.contentChanged(this);
        }
    }
    
    void fireContentSizeChanged() {
        synchronized (this) {
            renderer = null;
        }
        if (null != listener) {
            listener.contentSizeChanged(this);
        }
    }
}
