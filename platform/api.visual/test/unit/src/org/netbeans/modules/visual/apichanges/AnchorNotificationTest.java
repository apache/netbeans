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
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.anchor.Anchor;

import javax.swing.*;
import java.awt.*;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test for #111987 - VMDNodeAnchor recalculates unnecessarily
 * @author David Kaspar
 */
public class AnchorNotificationTest extends VisualTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(AnchorNotificationTest.class);
    }

    public AnchorNotificationTest (String testName) {
        super (testName);
    }

    public void testNotify () {
        StringBuffer log = new StringBuffer ();
        Scene scene = new Scene ();

        Widget w = new Widget (scene);
        scene.addChild (w);

        ConnectionWidget c = new ConnectionWidget (scene);
        scene.addChild (c);
        TestAnchor testAnchor = new TestAnchor (w, log);
        c.setSourceAnchor (testAnchor);
        c.setTargetAnchor (testAnchor);

        JFrame frame = showFrame (scene);

        c.setSourceAnchor (null);
        c.setTargetAnchor (null);
        scene.validate ();

        frame.setVisible (false);
        frame.dispose ();

        assertEquals (log.toString (),
                "notifyEntryAdded\n" +
                "notifyUsed\n" +
                "notifyRevalidate\n" +
                "notifyEntryAdded\n" +
                "notifyRevalidate\n" +
                "notifyRevalidate\n" +
                "compute\n" +
                "compute\n" +
                "notifyEntryRemoved\n" +
                "notifyRevalidate\n" +
                "notifyEntryRemoved\n" +
                "notifyUnused\n" +
                "notifyRevalidate\n"
                );
    }

    private class TestAnchor extends Anchor {

        private StringBuffer log;

        protected TestAnchor (Widget relatedWidget, StringBuffer log) {
            super (relatedWidget);
            this.log = log;
        }

        protected void notifyEntryAdded (Entry entry) {
            log.append ("notifyEntryAdded\n");
        }

        protected void notifyEntryRemoved (Entry entry) {
            log.append ("notifyEntryRemoved\n");
        }

        protected void notifyUsed () {
            log.append ("notifyUsed\n");
        }

        protected void notifyUnused () {
            log.append ("notifyUnused\n");
        }

        protected void notifyRevalidate () {
            log.append ("notifyRevalidate\n");
        }

        public Result compute (Entry entry) {
            log.append ("compute\n");
            return new Result (new Point (0, 0), DIRECTION_ANY);
        }
    }

}
