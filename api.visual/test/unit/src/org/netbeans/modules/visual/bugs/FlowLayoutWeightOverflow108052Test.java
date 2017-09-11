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
