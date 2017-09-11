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
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.LayerWidget;

import java.awt.*;
import java.awt.image.BufferedImage;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.visual.laf.DefaultLookFeel;

/**
 * @author David Kaspar
 */
public class OffscreenRenderingTest extends VisualTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(OffscreenRenderingTest.class);
    }

    public OffscreenRenderingTest (String testName) {
        super (testName);
    }

    public void testOffscreenRendering () {
        Scene scene = new Scene ();

        LayerWidget layer = new LayerWidget (scene);
        layer.setPreferredBounds (new Rectangle (0, 0, 80, 80));
        scene.addChild (layer);

        LabelWidget widget = new LabelWidget (scene, "Hi");
        widget.setVerticalAlignment (LabelWidget.VerticalAlignment.CENTER);
        widget.setAlignment (LabelWidget.Alignment.CENTER);
        widget.setBorder (BorderFactory.createLineBorder ());
        widget.setPreferredLocation (new Point (20, 20));
        widget.setPreferredBounds (new Rectangle (0, 0, 40, 40));
        layer.addChild (widget);

        BufferedImage image = dumpSceneOffscreenRendering (scene);
        Color backgroundColor = (Color) (new DefaultLookFeel()).getBackground();
        Color foregroundColor = (new DefaultLookFeel()).getForeground();
        assertCleaness (testCleaness (image, backgroundColor, foregroundColor), image, null);

        assertScene (scene, backgroundColor, new Rectangle (19, 19, 42, 42));
    }

    private BufferedImage dumpSceneOffscreenRendering (Scene scene) {
        // validate the scene with a off-screen graphics
        BufferedImage emptyImage = new BufferedImage (1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D emptyGraphics = emptyImage.createGraphics ();
        scene.validate (emptyGraphics);
        emptyGraphics.dispose ();

        // now the scene is calculated using the emptyGraphics, all widgets should be layout and scene has its size resolved
        // paint the scene with a off-screen graphics
        Rectangle viewBounds = scene.convertSceneToView (scene.getBounds ());
        BufferedImage image = new BufferedImage (viewBounds.width, viewBounds.height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics = image.createGraphics ();
        double zoomFactor = scene.getZoomFactor ();
        graphics.scale (zoomFactor, zoomFactor);
        scene.paint (graphics);
        graphics.dispose ();

        return image;
    }

}
