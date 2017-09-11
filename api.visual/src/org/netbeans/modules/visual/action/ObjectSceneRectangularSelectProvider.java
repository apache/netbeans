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

import org.netbeans.api.visual.action.RectangularSelectProvider;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.model.ObjectScene;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.*;

/**
 * @author David Kaspar
 */
public final class ObjectSceneRectangularSelectProvider implements RectangularSelectProvider {

    private ObjectScene scene;

    public ObjectSceneRectangularSelectProvider (ObjectScene scene) {
        this.scene = scene;
    }

    public void performSelection (Rectangle sceneSelection) {
        boolean entirely = sceneSelection.width > 0;
        int w = sceneSelection.width;
        int h = sceneSelection.height;
        Rectangle rect = new Rectangle (w >= 0 ? 0 : w, h >= 0 ? 0 : h, w >= 0 ? w : -w, h >= 0 ? h : -h);
        rect.translate (sceneSelection.x, sceneSelection.y);

        HashSet<Object> set = new HashSet<Object> ();
        Set<?> objects = scene.getObjects ();
        for (Object object : objects) {
            Widget widget = scene.findWidget (object);
            if (widget == null)
                continue;
            if (entirely) {
                Rectangle widgetRect = widget.convertLocalToScene (widget.getBounds ());
                if (rect.contains (widgetRect))
                    set.add (object);
            } else {
                if (widget instanceof ConnectionWidget) {
                    ConnectionWidget conn = (ConnectionWidget) widget;
                    java.util.List<Point> points = conn.getControlPoints ();
                    for (int i = points.size () - 2; i >= 0; i --) {
                        Point p1 = widget.convertLocalToScene (points.get (i));
                        Point p2 = widget.convertLocalToScene (points.get (i + 1));
                        if (new Line2D.Float (p1, p2).intersects (rect))
                            set.add (object);
                    }
                } else {
                    Rectangle widgetRect = widget.convertLocalToScene (widget.getBounds ());
                    if (rect.intersects (widgetRect))
                        set.add (object);
                }
            }
        }
        Iterator<Object> iterator = set.iterator ();
        scene.setFocusedObject (iterator.hasNext () ? iterator.next () : null);
        scene.userSelectionSuggested (set, false);
    }

}
