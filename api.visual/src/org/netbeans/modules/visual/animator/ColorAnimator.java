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
public final class ColorAnimator extends Animator {

    private HashMap<Widget, Color> sourceBackgroundColors = new HashMap<Widget, Color> ();
    private HashMap<Widget, Color> targetBackgroundColors = new HashMap<Widget, Color> ();
    private HashMap<Widget, Color> sourceForegroundColors = new HashMap<Widget, Color> ();
    private HashMap<Widget, Color> targetForegroundColors = new HashMap<Widget, Color> ();

    public ColorAnimator (SceneAnimator sceneAnimator) {
        super (sceneAnimator);
    }

    public void setBackgroundColor (Widget widget, Color backgroundColor) {
        assert widget != null;
        assert backgroundColor != null;
        if (!sourceBackgroundColors.isEmpty()) {
            sourceBackgroundColors.clear ();
        }
        targetBackgroundColors.put (widget, backgroundColor);
        start ();
    }

    public void setForegroundColor (Widget widget, Color foregroundColor) {
        assert widget != null;
        assert foregroundColor != null;
        if (!sourceForegroundColors.isEmpty()) {
            sourceForegroundColors.clear ();
        }
        targetForegroundColors.put (widget, foregroundColor);
        start ();
    }

    protected void tick (double progress) {
        for (Map.Entry<Widget, Color> entry : targetBackgroundColors.entrySet ()) {
            Widget widget = entry.getKey ();
            Color sourceColor = sourceBackgroundColors.get (widget);
            if (sourceColor == null) {
                Paint background = widget.getBackground ();
                sourceColor = background instanceof Color ? (Color) background : Color.WHITE;
                sourceBackgroundColors.put (widget, sourceColor);
            }
            Color targetColor = entry.getValue ();

            Color color;
            if (progress >= 1.0)
                color = targetColor;
            else
                color = new Color (
                        (int) (sourceColor.getRed () + progress * (targetColor.getRed () - sourceColor.getRed ())),
                        (int) (sourceColor.getGreen () + progress * (targetColor.getGreen () - sourceColor.getGreen ())),
                        (int) (sourceColor.getBlue () + progress * (targetColor.getBlue () - sourceColor.getBlue ()))
                );
            widget.setBackground (color);
        }

        for (Map.Entry<Widget, Color> entry : targetForegroundColors.entrySet ()) {
            Widget widget = entry.getKey ();
            Color sourceColor = sourceForegroundColors.get (widget);
            if (sourceColor == null) {
                sourceColor = widget.getForeground ();
                sourceForegroundColors.put (widget, sourceColor);
            }
            Color targetColor = entry.getValue ();

            Color color;
            if (progress >= 1.0)
                color = targetColor;
            else
                color = new Color (
                        (int) (sourceColor.getRed () + progress * (targetColor.getRed () - sourceColor.getRed ())),
                        (int) (sourceColor.getGreen () + progress * (targetColor.getGreen () - sourceColor.getGreen ())),
                        (int) (sourceColor.getBlue () + progress * (targetColor.getBlue () - sourceColor.getBlue ()))
                );
            widget.setForeground (color);
        }

        if (progress >= 1.0) {
            sourceBackgroundColors.clear ();
            targetBackgroundColors.clear ();
            sourceForegroundColors.clear ();
            targetForegroundColors.clear ();
        }
    }

}
