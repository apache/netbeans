/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
