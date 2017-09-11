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

import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;
import java.awt.*;

/**
 * @author David Kaspar
 */
public final class MouseCenteredZoomAction extends WidgetAction.Adapter {

    private double zoomMultiplier;

    public MouseCenteredZoomAction (double zoomMultiplier) {
        this.zoomMultiplier = zoomMultiplier;
    }

    public State mouseWheelMoved (Widget widget, WidgetMouseWheelEvent event) {
        Scene scene = widget.getScene ();

        int modifiers = scene.getInputBindings ().getZoomActionModifiers ();
        if ((event.getModifiers () & modifiers) != modifiers)
            return State.REJECTED;

        int amount = event.getWheelRotation ();

        double scale = 1.0;
        while (amount > 0) {
            scale /= zoomMultiplier;
            amount --;
        }
        while (amount < 0) {
            scale *= zoomMultiplier;
            amount ++;
        }

        JComponent view = scene.getView ();
        if (view != null) {
            Rectangle viewBounds = view.getVisibleRect ();

            Point center = widget.convertLocalToScene (event.getPoint ());
            Point mouseLocation = scene.convertSceneToView (center);

            scene.setZoomFactor (scale * scene.getZoomFactor ());
            scene.validate (); // HINT - forcing to change preferred size of the JComponent view

            center = scene.convertSceneToView (center);

            view.scrollRectToVisible (new Rectangle (
                    center.x - (mouseLocation.x - viewBounds.x),
                    center.y - (mouseLocation.y - viewBounds.y),
                    viewBounds.width,
                    viewBounds.height
            ));
        } else
            scene.setZoomFactor (scale * scene.getZoomFactor ());

        return State.CONSUMED;
    }

}
