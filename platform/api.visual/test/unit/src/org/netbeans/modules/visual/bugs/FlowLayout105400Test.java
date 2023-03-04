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
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;

import java.awt.*;
import org.netbeans.junit.RandomlyFails;

/**
 * @author David Kaspar
 */
@RandomlyFails // timeout in NB-Core-Build #2574
public class FlowLayout105400Test extends VisualTestCase {

    public FlowLayout105400Test (String testName) {
        super (testName);
    }

    public void testFlowLayoutInsets () {
        Scene scene = new Scene ();
        Widget parent = new Widget (scene);
        parent.setBorder (BorderFactory.createResizeBorder (10));
        parent.setLayout (LayoutFactory.createVerticalFlowLayout ());
        scene.addChild (parent);

        Widget child = new Widget (scene);
        child.setBackground (Color.BLUE);
        child.setOpaque (true);
        child.setPreferredBounds (new Rectangle (-50, -30, 30, 20));
        parent.addChild (child);

        assertScene (scene, Color.WHITE, new Rectangle (-1, -1, 52, 42));
    }

}
