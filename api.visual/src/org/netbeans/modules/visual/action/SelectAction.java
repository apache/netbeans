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

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.SelectProvider;

import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.*;

/**
 * @author David Kaspar
 */
public final class SelectAction extends WidgetAction.LockedAdapter {

    private boolean aiming = false;
    private Widget aimedWidget = null;
    private boolean invertSelection;
    private SelectProvider provider;
    private boolean trapRightClick = false ;
    
    public SelectAction (SelectProvider provider, boolean trapRightClick) {
        this.provider = provider ;
        this.trapRightClick = trapRightClick ;
    }
  
    public SelectAction (SelectProvider provider) {
        this.provider = provider;
    }

    protected boolean isLocked () {
        return aiming;
    }

    public State mousePressed (Widget widget, WidgetMouseEvent event) {
        if (isLocked()) {
            return State.createLocked(widget, this);
        }
        
        Point localLocation = event.getPoint();
        
        if (event.getButton() == MouseEvent.BUTTON1 || event.getButton() == MouseEvent.BUTTON2) {
            invertSelection = (event.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0;
            
            if (provider.isSelectionAllowed(widget, localLocation, invertSelection)) {
                aiming = provider.isAimingAllowed(widget, localLocation, invertSelection);
                if (aiming) {
                    updateState(widget, localLocation);
                    return State.createLocked(widget, this);
                } else {
                    provider.select(widget, localLocation, invertSelection);
                    return State.CHAIN_ONLY;
                }
            }
        } else if (trapRightClick && event.getButton() == MouseEvent.BUTTON3) {
            provider.select(widget, localLocation, false);
            return State.CHAIN_ONLY;
        }
        
        return State.REJECTED;
    }

    public State mouseReleased (Widget widget, WidgetMouseEvent event) {
        if (aiming) {
            Point point = event.getPoint ();
            updateState (widget, point);
            if (aimedWidget != null)
                provider.select (widget, point, invertSelection);
            updateState (null, null);
            aiming = false;
            return State.CONSUMED;
        }
        return super.mouseReleased (widget, event);
    }

    private void updateState (Widget widget, Point localLocation) {
        if (widget != null  &&  ! widget.isHitAt (localLocation))
            widget = null;
        if (widget == aimedWidget)
            return;
        if (aimedWidget != null)
            aimedWidget.setState (aimedWidget.getState ().deriveWidgetAimed (false));
        aimedWidget = widget;
        if (aimedWidget != null)
            aimedWidget.setState (aimedWidget.getState ().deriveWidgetAimed (true));
    }

    public State keyTyped (Widget widget, WidgetKeyEvent event) {
        if (! aiming  &&  event.getKeyChar () == KeyEvent.VK_SPACE) {
            provider.select (widget, null, (event.getModifiersEx () & MouseEvent.CTRL_DOWN_MASK) != 0);
            return State.CONSUMED;
        }
        return State.REJECTED;
    }

}
