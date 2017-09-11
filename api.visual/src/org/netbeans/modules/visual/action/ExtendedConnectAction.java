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

import org.netbeans.api.visual.action.ConnectDecorator;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Utilities;

/**
 *
 * @author alex
 */
public class ExtendedConnectAction extends ConnectAction {

    private long modifiers;
    private boolean macLocking;

    public ExtendedConnectAction(ConnectDecorator decorator, Widget interractionLayer, ConnectProvider provider, long modifiers) {
        super(decorator, interractionLayer, provider);
        this.modifiers = modifiers;
    }

    protected boolean isLocked () {
        return super.isLocked ()  ||  macLocking;
    }

    public WidgetAction.State mousePressed(Widget widget, WidgetAction.WidgetMouseEvent event) {
        if (isLocked ())
            return State.createLocked (widget, this);
        if ((event.getModifiers () & modifiers) == modifiers) {
            if ((Utilities.getOperatingSystem () & Utilities.OS_MAC) != 0)
                macLocking = true;
            return super.mousePressedCore(widget,event);
        }
        return State.REJECTED;
    }

    public WidgetAction.State mouseReleased(Widget widget, WidgetAction.WidgetMouseEvent event) {
        macLocking = false;
        if (isLocked ())
            return super.mouseReleased(widget,event);
        else
            return State.REJECTED;
    }

    public State mouseMoved (Widget widget, WidgetMouseEvent event) {
        if (macLocking)
            return super.mouseDragged (widget, event);
        return super.mouseMoved (widget, event);
    }
}
