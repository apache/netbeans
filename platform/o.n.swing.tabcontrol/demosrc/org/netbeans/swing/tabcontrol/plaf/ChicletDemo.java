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


        @Override
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
