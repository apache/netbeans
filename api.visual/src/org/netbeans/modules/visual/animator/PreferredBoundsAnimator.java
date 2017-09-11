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
package org.netbeans.modules.visual.animator;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.animator.Animator;
import org.netbeans.api.visual.animator.SceneAnimator;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author David Kaspar
 */
public final class PreferredBoundsAnimator extends Animator {

    private HashMap<Widget, Rectangle> sourceBounds = new HashMap<Widget, Rectangle> ();
    private HashMap<Widget, Rectangle> targetBounds = new HashMap<Widget, Rectangle> ();
    private HashMap<Widget, Boolean> nullBounds = new HashMap<Widget, Boolean> ();

    public PreferredBoundsAnimator (SceneAnimator sceneAnimator) {
        super (sceneAnimator);
    }

    public void setPreferredBounds (Widget widget, Rectangle preferredBounds) {
        assert widget != null;
        if (!sourceBounds.isEmpty()) {
            sourceBounds.clear ();
        }
        boolean extra = preferredBounds == null;
        nullBounds.put (widget, extra);
        Rectangle rect = null;
        if (extra  &&  widget.isPreferredBoundsSet ()) {
            rect = widget.getPreferredBounds ();
            widget.setPreferredBounds (null);
        }
        targetBounds.put (widget, extra ? widget.getPreferredBounds () : preferredBounds);
        if (rect != null)
            widget.setPreferredBounds (rect);
        start ();
    }

    protected void tick (double progress) {
        for (Map.Entry<Widget, Rectangle> entry : targetBounds.entrySet ()) {
            Widget widget = entry.getKey ();
            Rectangle sourceBoundary = sourceBounds.get (widget);
            if (sourceBoundary == null) {
                sourceBoundary = widget.getBounds ();
                if (sourceBoundary == null)
                    sourceBoundary = new Rectangle ();
                sourceBounds.put (widget, sourceBoundary);
            }
            Rectangle targetBoundary = entry.getValue ();
            Rectangle boundary;
            if (progress >= 1.0) {
                boundary = nullBounds.get (widget) ? null : targetBoundary;
            } else
                boundary = new Rectangle (
                        (int) (sourceBoundary.x + progress * (targetBoundary.x - sourceBoundary.x)),
                        (int) (sourceBoundary.y + progress * (targetBoundary.y - sourceBoundary.y)),
                        (int) (sourceBoundary.width + progress * (targetBoundary.width - sourceBoundary.width)),
                        (int) (sourceBoundary.height + progress * (targetBoundary.height - sourceBoundary.height)));
            widget.setPreferredBounds (boundary);
        }
        if (progress >= 1.0) {
            sourceBounds.clear ();
            targetBounds.clear ();
            nullBounds.clear ();
        }
    }

}
