/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
