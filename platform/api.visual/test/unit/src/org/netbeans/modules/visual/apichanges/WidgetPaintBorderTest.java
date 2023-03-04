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

import java.awt.GraphicsEnvironment;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.visual.framework.VisualTestCase;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * Test for issue #98307 - Widget.paintBorder method added
 * @author David Kaspar
 */
public class WidgetPaintBorderTest extends VisualTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(WidgetPaintBorderTest.class);
    }

    public WidgetPaintBorderTest (String s) {
        super (s);
    }

    public void testPaintWidgetBorder () {
        Scene scene = new Scene ();
        MyWidget widget = new MyWidget (scene);
        scene.addChild (widget);
        takeOneTimeSnapshot (scene, 10, 10);
        assertTrue ("Widget border is not painted", widget.borderPainted);
    }

    private static class MyWidget extends Widget {

        private boolean borderPainted = false;

        public MyWidget (Scene scene) {
            super (scene);
        }

        protected void paintBorder () {
            borderPainted = true;
            super.paintBorder ();
        }

    }

}
