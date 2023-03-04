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
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.router.CollisionsCollector;
import org.netbeans.api.visual.router.ConnectionWidgetCollisionsCollector;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test for #99054 - CollisionsCollector context
 * @author David Kaspar
 */
public class ConnectionWidgetCollisionsCollectorTest extends VisualTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(ConnectionWidgetCollisionsCollectorTest.class);
    }

    public ConnectionWidgetCollisionsCollectorTest (String name) {
        super (name);
    }

    public void testCollisionsCollector () {
        Scene scene = new Scene ();

        ConnectionWidget widget = new ConnectionWidget (scene);
        widget.setSourceAnchor (AnchorFactory.createFixedAnchor (new Point (100, 100)));
        widget.setTargetAnchor (AnchorFactory.createFixedAnchor (new Point (300, 200)));
        widget.setRouter (RouterFactory.createOrthogonalSearchRouter (new CollisionsCollector() {
            public void collectCollisions (List<Rectangle> verticalCollisions, List<Rectangle> horizontalCollisions) {
                getRef ().println ("CollisionsCollector invoked");
            }
        }));
        scene.addChild (widget);

        JFrame frame = showFrame (scene);
        frame.setVisible(false);
        frame.dispose ();
        
        compareReferenceFiles ();
    }

    public void testConnectionWidgetCollisionsCollector () {
        Scene scene = new Scene ();

        final ConnectionWidget widget = new ConnectionWidget (scene);
        widget.setSourceAnchor (AnchorFactory.createFixedAnchor (new Point (100, 100)));
        widget.setTargetAnchor (AnchorFactory.createFixedAnchor (new Point (300, 200)));
        widget.setRouter (RouterFactory.createOrthogonalSearchRouter (new ConnectionWidgetCollisionsCollector () {
            public void collectCollisions (ConnectionWidget connectionWidget, List<Rectangle> verticalCollisions, List<Rectangle> horizontalCollisions) {
                getRef ().println ("ConnectionWidgetCollisionsCollector invoked - is widget valid: " + (connectionWidget == widget));
            }
        }));
        scene.addChild (widget);

        JFrame frame = showFrame (scene);
        frame.setVisible(false);
        frame.dispose ();

        compareReferenceFiles ();
    }

}
