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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.visual.action;

import org.netbeans.api.visual.action.ContiguousSelectEvent;
import org.netbeans.api.visual.action.ContiguousSelectProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * @author David Kaspar
 */
public final class ContiguousSelectAction extends WidgetAction.Adapter {

    private ContiguousSelectProvider provider;
    private Widget previousWidget;
    private Point previousLocalLocation;

    public ContiguousSelectAction (ContiguousSelectProvider provider) {
        this.provider = provider;
    }

    @Override
    public State mousePressed (Widget widget, WidgetMouseEvent event) {
        Point localLocation = event.getPoint();
        if ((event.getButton() & (MouseEvent.BUTTON1  | MouseEvent.BUTTON2  | MouseEvent.BUTTON3)) != 0) {
            if (process (widget, localLocation, event.getModifiersEx ()))
                return State.CHAIN_ONLY;
        }
        return State.REJECTED;
    }

    private boolean process (Widget widget, Point localLocation, int modifiers) {
        boolean ctrl = (modifiers & MouseEvent.CTRL_DOWN_MASK) != 0;
        boolean shift = (modifiers & MouseEvent.SHIFT_DOWN_MASK) != 0;
        ContiguousSelectEvent.SelectionType type = ctrl
                ? (shift ? ContiguousSelectEvent.SelectionType.ADDITIVE_CONTIGUOUS : ContiguousSelectEvent.SelectionType.ADDITIVE_NON_CONTIGUOUS)
                : (shift ? ContiguousSelectEvent.SelectionType.REPLACE_CONTIGUOUS : ContiguousSelectEvent.SelectionType.REPLACE_NON_CONTIGUOUS);
        ContiguousSelectEvent providerEvent = ContiguousSelectEvent.create (previousWidget, previousLocalLocation, widget, localLocation, type);
        if (provider.isSelectionAllowed (providerEvent)) {
            provider.select(providerEvent);
            if (! shift) {
                previousWidget = widget;
                previousLocalLocation = localLocation;
            }
            return true;
        }
        return false;
    }

    @Override
    public State keyTyped (Widget widget, WidgetKeyEvent event) {
        if (event.getKeyChar () == KeyEvent.VK_SPACE)
            if (process (widget, null, event.getModifiers ()))
                return State.CONSUMED;
        return State.REJECTED;
    }

}
