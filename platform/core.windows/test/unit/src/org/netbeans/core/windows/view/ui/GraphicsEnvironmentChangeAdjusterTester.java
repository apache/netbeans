/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.core.windows.view.ui;

import java.awt.BorderLayout;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Standalone app for manual testing of {@link GraphicsEnvironmentChangeAdjuster}.
 */
public class GraphicsEnvironmentChangeAdjusterTester {
    private static final class MyLabel extends JLabel {
        public MyLabel(String s) {
            super(s);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            GraphicsEnvironmentChangeAdjuster.notifyPossibleGraphicsEnvironmentChange();
        }
    }

    public static final void main(String args[]) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            JFrame frame = new JFrame("Hello");
            GraphicsEnvironmentChangeAdjuster.registerWindow(frame);
            frame.setLayout(new BorderLayout());
            JLabel label = new MyLabel("Hello, World");
            label.setFont(label.getFont().deriveFont(30f));
            label.setHorizontalAlignment(JLabel.CENTER);
            frame.add(label, BorderLayout.CENTER);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setVisible(true);
        });
    }
}
