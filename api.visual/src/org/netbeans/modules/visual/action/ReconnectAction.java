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

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.action.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Override at least isReplacementWidget and setConnectionAnchor methods. reconnectingFinished is always called before setConnectionAnchor.
 *
 * @author David Kaspar
 */
public final class ReconnectAction extends WidgetAction.LockedAdapter {

    private static final int MIN_DIFFERENCE = 5;

    private ReconnectDecorator decorator;
    private ReconnectProvider provider;

    private ConnectionWidget connectionWidget = null;
    private boolean reconnectingSource = false;
    private Point floatPoint = null;
    private Widget replacementWidget = null;
    private Anchor originalAnchor = null;

    public ReconnectAction (ReconnectDecorator decorator, ReconnectProvider provider) {
        this.decorator = decorator;
        this.provider = provider;
    }

    protected boolean isLocked () {
        return connectionWidget != null;
    }

    public State mousePressed (Widget widget, WidgetMouseEvent event) {
        if (isLocked ())
            return State.createLocked (widget, this);
        if (event.getButton () == MouseEvent.BUTTON1  &&  event.getClickCount () == 1) {
            if (widget instanceof ConnectionWidget) {
                ConnectionWidget conn = (ConnectionWidget) widget;
                int index = conn.getControlPointHitAt (event.getPoint ());
                List<Point> controlPoints = conn.getControlPoints ();
                if (index == 0  &&  provider.isSourceReconnectable (conn)) {
                    reconnectingSource = true;
                } else if (controlPoints != null  &&  index == controlPoints.size () - 1  && provider.isTargetReconnectable (conn)) {
                    reconnectingSource = false;
                } else {
                    return State.REJECTED;
                }

                floatPoint = new Point (event.getPoint ());
                replacementWidget = null;
                connectionWidget = conn;
                provider.reconnectingStarted (conn, reconnectingSource);
                if (reconnectingSource)
                    originalAnchor = connectionWidget.getSourceAnchor ();
                else
                    originalAnchor = connectionWidget.getTargetAnchor ();
                return State.createLocked (widget, this);
            }
        }
        return State.REJECTED;
    }

    public State mouseReleased (Widget widget, WidgetMouseEvent event) {
        Point point = event.getPoint ();
        boolean state = move (widget, point);
        if (state) {
            cancel (point);
        }
        return state ? State.CONSUMED : State.REJECTED;
    }

    private void cancel (Point point) {
        if (reconnectingSource)
            connectionWidget.setSourceAnchor (originalAnchor);
        else
            connectionWidget.setTargetAnchor (originalAnchor);
        provider.reconnectingFinished (connectionWidget, reconnectingSource);
        if (point != null)
            if (Math.abs (floatPoint.x - point.x) >= ReconnectAction.MIN_DIFFERENCE  ||  Math.abs (floatPoint.y - point.y) >= ReconnectAction.MIN_DIFFERENCE)
                provider.reconnect (connectionWidget, replacementWidget, reconnectingSource);
        replacementWidget = null;
        floatPoint = null;
        connectionWidget = null;
    }

    public State mouseDragged (Widget widget, WidgetMouseEvent event) {
        return move (widget, event.getPoint ()) ? State.createLocked (widget, this) : State.REJECTED;
    }

    private boolean move (Widget widget, Point point) {
        if (connectionWidget != widget)
            return false;

        Point replacementSceneLocation = widget.convertLocalToScene (point);
        replacementWidget = resolveReplacementWidgetCore (connectionWidget.getScene (), replacementSceneLocation);
        Anchor replacementAnchor = null;
        if (replacementWidget != null)
            replacementAnchor = decorator.createReplacementWidgetAnchor (replacementWidget);
        if (replacementAnchor == null)
            replacementAnchor = decorator.createFloatAnchor (replacementSceneLocation);

        if (reconnectingSource)
            connectionWidget.setSourceAnchor (replacementAnchor);
        else
            connectionWidget.setTargetAnchor (replacementAnchor);

        return true;
    }

    protected Widget resolveReplacementWidgetCore (Scene scene, Point sceneLocation) {
        if (provider != null)
            if (provider.hasCustomReplacementWidgetResolver (scene))
                return provider.resolveReplacementWidget (scene, sceneLocation);
        Point sceneOrigin = scene.getLocation ();
        sceneLocation = new Point (sceneLocation.x + sceneOrigin.x, sceneLocation.y + sceneOrigin.y);
        Widget[] result = new Widget[]{null};
        resolveReplacementWidgetCoreDive (result, scene, sceneLocation);
        return result[0];
    }

    private boolean resolveReplacementWidgetCoreDive (Widget[] result, Widget widget, Point parentLocation) {
        if (widget == connectionWidget)
            return false;

        Point widgetLocation = widget.getLocation ();
        Point location = new Point (parentLocation.x - widgetLocation.x, parentLocation.y - widgetLocation.y);

        if (! widget.getBounds ().contains (location))
            return false;

        java.util.List<Widget> children = widget.getChildren ();
        for (int i = children.size () - 1; i >= 0; i --) {
            if (resolveReplacementWidgetCoreDive (result, children.get (i), location))
                return true;
        }

        if (! widget.isHitAt (location))
            return false;

        ConnectorState state = provider.isReplacementWidget (connectionWidget, widget, reconnectingSource);
        if (state == ConnectorState.REJECT)
            return false;
        if (state == ConnectorState.ACCEPT)
            result[0] = widget;
        return true;
    }


    public State keyPressed (Widget widget, WidgetKeyEvent event) {
        if (isLocked ()  &&  event.getKeyCode () == KeyEvent.VK_ESCAPE) {
            cancel (null);
            return State.CONSUMED;
        }
        return super.keyPressed (widget, event);
    }

}
