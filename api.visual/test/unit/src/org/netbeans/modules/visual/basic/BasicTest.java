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
