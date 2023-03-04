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
package org.netbeans.modules.visual.basic;

import org.netbeans.modules.visual.framework.VisualTestCase;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.laf.DefaultLookFeel;

/**
 * @author David Kaspar
 */
public class BasicTest extends VisualTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(BasicTest.class);
    }

    public BasicTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testShow () {
        Scene scene = new Scene ();
        
        LayerWidget mainLayer = new LayerWidget (scene);
        scene.addChild(mainLayer);
        
        Widget w1 = new Widget (scene);
        w1.setBorder (BorderFactory.createLineBorder ());
        w1.setPreferredLocation (new Point (100, 100));
        w1.setPreferredSize (new Dimension (40, 20));
        mainLayer.addChild(w1);
        
        Widget w2 = new Widget (scene);
        w2.setBorder (BorderFactory.createLineBorder ());
        w2.setPreferredLocation (new Point (200, 100));
        w2.setPreferredSize (new Dimension (40, 20));
        mainLayer.addChild(w2);
        
        LayerWidget connLayer = new LayerWidget (scene);
        scene.addChild(connLayer);
        
        ConnectionWidget conn = new ConnectionWidget(scene);
        conn.setSourceAnchor(AnchorFactory.createRectangularAnchor(w1));
        conn.setTargetAnchor(AnchorFactory.createRectangularAnchor(w2));
        connLayer.addChild(conn);
        
        Color color = (Color) (new DefaultLookFeel()).getBackground();
        assertScene (scene, color,
                new Rectangle (99, 99, 42, 22),
                new Rectangle (199, 99, 42, 22),
                new Rectangle (138, 108, 64, 4)
        );
    }

}
