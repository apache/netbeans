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

package org.netbeans.lib.profiler.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;


/**
 *
 * @author Jiri Sedlacek
 */
public class DiscreteProgress extends JPanel {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private Color disabledColor = new Color(220, 220, 220);
    private Color enabledColor = new Color(128, 128, 255);
    private int activeUnits = 0;
    private int totalUnits = 10;
    private int unitHeight = 13;
    private int unitWidth = 10;
    
    private JProgressBar progressDelegate;
    private DefaultBoundedRangeModel progressDelegateModel;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public DiscreteProgress() {
        initComponents();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setActiveUnits(int activeUnits) {
        if (progressDelegateModel != null) {
            this.activeUnits = activeUnits;
            progressDelegateModel.setValue(activeUnits);
        } else if (this.activeUnits != activeUnits) {
            this.activeUnits = activeUnits;
            repaint();
        }
    }

    public int getActiveUnits() {
        return activeUnits;
    }

    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public Dimension getPreferredSize() {
        return new Dimension(((totalUnits * unitWidth) + totalUnits) - 1 + 4, unitHeight + 4);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }

        ;

        DiscreteProgress progress = new DiscreteProgress();

        JFrame testFrame = new JFrame("Decimal Progress Test Frame"); // NOI18N
        testFrame.getContentPane().add(progress);
        testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        testFrame.pack();
        testFrame.setVisible(true);
    }

    public void paintComponent(Graphics g) {
        if (progressDelegate == null) {
            Insets insets = getInsets();
            int offsetX = insets.left;
            int offsetY = insets.top;

            for (int i = 0; i < totalUnits; i++) {
                g.setColor((i < activeUnits) ? enabledColor : disabledColor);
                g.fillRect(offsetX + (i * unitWidth) + i, offsetY, unitWidth, unitHeight);
            }
        } else {
            super.paintComponent(g);
        }
    }
    
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
//        if (UIUtils.isNimbus()) {
            progressDelegateModel = new DefaultBoundedRangeModel(4, 1, 0, 10);
            progressDelegate = new JProgressBar(progressDelegateModel);
            add(progressDelegate, BorderLayout.CENTER);
//        } else {
//            setBorder(new ThinBevelBorder(BevelBorder.LOWERED));
//            setOpaque(false);
//        }
    }
}
