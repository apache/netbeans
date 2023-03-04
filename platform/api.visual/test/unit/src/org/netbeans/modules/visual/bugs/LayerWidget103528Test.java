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
package org.netbeans.modules.visual.bugs;

import org.netbeans.modules.visual.framework.VisualTestCase;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.visual.laf.DefaultLookFeel;

/**
 * @author David Kaspar
 */
public class LayerWidget103528Test extends VisualTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(LayerWidget103528Test.class);
    }

    public LayerWidget103528Test (String testName) {
        super (testName);
    }

    public void testLayerPreferredLocation () {
        Scene scene = new Scene ();

        scene.addChild (new LayerWidget (scene));

        LayerWidget layer = new LayerWidget (scene);
        layer.setPreferredLocation (new Point (100, 100));
        scene.addChild (layer);

        Widget widget = new Widget (scene);
        widget.setPreferredBounds (new Rectangle (-20, -10, 100, 50));
        widget.setOpaque (true);
        widget.setBackground (Color.RED);
        layer.addChild (widget);

        Color color = (Color) (new DefaultLookFeel()).getBackground();
        assertScene (scene, color, new Rectangle (80, 90, 100, 50));
    }

}
