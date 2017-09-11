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
