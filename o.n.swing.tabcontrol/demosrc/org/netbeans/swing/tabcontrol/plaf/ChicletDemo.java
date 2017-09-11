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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.swing.tabcontrol.plaf;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

/** Basic chiclet demo, for use in write-your-own-ui-delegate demo at JavaOne 2004 */

public class ChicletDemo {
    public static void main (String[] ignored) {
        new TestFrame().setVisible(true);
    }


    private static class TestFrame extends JFrame implements WindowListener {
        private GenericGlowingChiclet thing = new GenericGlowingChiclet();

        public TestFrame() {
            addWindowListener(this);
            setBounds(20, 20, 200, 80);
        }


        public void paint(Graphics g) {
            super.paint(g);
            ColorUtil.setupAntialiasing(g);
//            thing.setArcs(20, 20, 20, 20);
            thing.setArcs(0.5f, 0.5f, 0.5f, 0.5f);
            thing.setNotch(true, false);
    Color[] rollover = new Color[]{
        new Color(222, 222, 227), new Color(220, 238, 255), new Color(190, 247, 255),
        new Color(205, 205, 205)};

//            thing.setState(thing.STATE_ACTIVE | thing.STATE_SELECTED | thing.STATE_);
        thing.setColors(rollover[0], rollover[1], rollover[2], rollover[3]);
            thing.setAllowVertical(true);
            thing.setBounds(25, 25, getWidth() - 120, getHeight() - 40);
            thing.draw((Graphics2D) g);
        }

        public void windowActivated(WindowEvent e) {
        }

        public void windowClosed(WindowEvent e) {
        }

        public void windowClosing(WindowEvent e) {
            System.exit(0);
        }

        public void windowDeactivated(WindowEvent e) {
        }

        public void windowDeiconified(WindowEvent e) {
        }

        public void windowIconified(WindowEvent e) {
        }

        public void windowOpened(WindowEvent e) {
        }
    }}
