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
package org.netbeans.modules.visual.apichanges;

import org.netbeans.modules.visual.framework.VisualTestCase;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.awt.image.BufferedImage;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.visual.laf.DefaultLookFeel;

/**
 * Test for #98641 - Missing vertical labels
 * @author David Kaspar
 */
public class LabelOrientationTest extends VisualTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(LabelOrientationTest.class);
    }

    public LabelOrientationTest (String testName) {
        super (testName);
    }

    public void testLabelOrientations () {
        Scene scene = new Scene ();
        LayerWidget layer = new LayerWidget (scene);
        scene.addChild(layer);
        layer.addChild (new Widget (scene));

        createLabel (layer, "N O R M A L", 100, 100, LabelWidget.Orientation.NORMAL, LabelWidget.Alignment.LEFT, LabelWidget.VerticalAlignment.BASELINE).setPreferredBounds (null);
        createLabel (layer, "R O T A T E 9 0", 100, 100, LabelWidget.Orientation.ROTATE_90, LabelWidget.Alignment.LEFT, LabelWidget.VerticalAlignment.BASELINE).setPreferredBounds (null);

        createLabel (layer, "NORMAL BASELINE", 200, 100, LabelWidget.Orientation.NORMAL, LabelWidget.Alignment.BASELINE, LabelWidget.VerticalAlignment.BASELINE);
        createLabel (layer, "ROTATE90 BASELINE", 200, 300, LabelWidget.Orientation.ROTATE_90, LabelWidget.Alignment.BASELINE, LabelWidget.VerticalAlignment.BASELINE);

        createLabel (layer, "NORMAL LEFT,TOP", 400, 100, LabelWidget.Orientation.NORMAL, LabelWidget.Alignment.LEFT, LabelWidget.VerticalAlignment.TOP);
        createLabel (layer, "ROTATE90 LEFT,TOP", 400, 300, LabelWidget.Orientation.ROTATE_90, LabelWidget.Alignment.LEFT, LabelWidget.VerticalAlignment.TOP);

        createLabel (layer, "NORMAL CENTER", 600, 100, LabelWidget.Orientation.NORMAL, LabelWidget.Alignment.CENTER, LabelWidget.VerticalAlignment.CENTER);
        createLabel (layer, "ROTATE90 CENTER", 600, 300, LabelWidget.Orientation.ROTATE_90, LabelWidget.Alignment.CENTER, LabelWidget.VerticalAlignment.CENTER);

        createLabel (layer, "NORMAL RIGHT,BOTTOM", 800, 100, LabelWidget.Orientation.NORMAL, LabelWidget.Alignment.RIGHT, LabelWidget.VerticalAlignment.BOTTOM);
        createLabel (layer, "ROTATE90 RIGHT,BOTTOM", 800, 300, LabelWidget.Orientation.ROTATE_90, LabelWidget.Alignment.RIGHT, LabelWidget.VerticalAlignment.BOTTOM);

        BufferedImage snapshot = takeOneTimeSnapshot (scene);
        BufferedImage clean = clearRegions (snapshot, Color.RED,
                new Rectangle (100, 80, 100, 30),
                new Rectangle (80, 0, 30, 110),

                new Rectangle (190, 90, 130, 30),
                new Rectangle (380, 70, 110, 30),
                new Rectangle (610, 150, 120, 30),
                new Rectangle (810, 230, 150, 30),

                new Rectangle (180, 270, 30, 130),
                new Rectangle (380, 270, 30, 160),
                new Rectangle (650, 290, 30, 150),
                new Rectangle (945, 275, 30, 190)

        );
        Color color = (Color) (new DefaultLookFeel()).getBackground();
        assertCleaness (testCleaness (clean, color, Color.YELLOW, Color.RED), snapshot, clean);
    }

    private static LabelWidget createLabel (LayerWidget layer, String label, int x, int y, LabelWidget.Orientation orientation, LabelWidget.Alignment alignment, LabelWidget.VerticalAlignment verticalAlignment) {
        LabelWidget widget = new LabelWidget (layer.getScene (), label);
        widget.setOpaque (true);
        widget.setBackground (Color.YELLOW);
        widget.setPreferredLocation (new Point (x, y));
        widget.setPreferredBounds (new Rectangle (-20, -30, 180, 180));
        widget.setOrientation (orientation);
        widget.setAlignment (alignment);
        widget.setVerticalAlignment (verticalAlignment);
        layer.addChild (widget);
        return widget;
    }

}
