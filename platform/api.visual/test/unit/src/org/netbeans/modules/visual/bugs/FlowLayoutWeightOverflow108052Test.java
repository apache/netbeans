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
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.LayerWidget;

import java.awt.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.visual.laf.DefaultLookFeel;

/**
 * @author David Kaspar
 */
public class FlowLayoutWeightOverflow108052Test extends VisualTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(FlowLayoutWeightOverflow108052Test.class);
    }

    public FlowLayoutWeightOverflow108052Test (String testName) {
        super (testName);
    }

    public void testFlowLayoutWeightOverflow () {
        Scene scene = new Scene ();
        LayerWidget layer = new LayerWidget (scene);
        layer.setMinimumSize (new Dimension (300, 200));
        scene.addChild (layer);

        Widget vbox = new Widget (scene);
        vbox.setBorder (BorderFactory.createLineBorder (1, Color.BLACK));
        vbox.setLayout (LayoutFactory.createVerticalFlowLayout (LayoutFactory.SerialAlignment.JUSTIFY, 0));
        layer.addChild (vbox);

        Widget hbox1 = new Widget (scene);
        hbox1.setBorder (BorderFactory.createLineBorder (1, Color.BLUE));
        hbox1.setLayout (LayoutFactory.createHorizontalFlowLayout ());
        vbox.addChild (hbox1);

        Widget item1 = new LabelWidget (scene, "Item1");
        item1.setBorder (BorderFactory.createLineBorder (1, Color.GREEN));
        hbox1.addChild (item1);

        Widget item2 = new LabelWidget (scene, "Item2");
        item2.setBorder (BorderFactory.createLineBorder (1, Color.YELLOW));
        hbox1.addChild (item2, 1000);

        Widget item3 = new LabelWidget (scene, "Item3");
        item3.setBorder (BorderFactory.createLineBorder (1, Color.RED));
        hbox1.addChild (item3);

        Widget hbox2 = new Widget (scene);
        hbox2.setBorder (BorderFactory.createLineBorder (1, Color.BLUE));
        hbox2.setPreferredSize (new Dimension (200, 20));
        vbox.addChild (hbox2);
        
        Color color = (Color) (new DefaultLookFeel()).getBackground();
        assertScene (scene, color, new Rectangle (-5, -5, 210, 100));
    }

}
