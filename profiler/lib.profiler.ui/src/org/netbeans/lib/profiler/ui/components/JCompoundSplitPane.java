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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;


/**
 *
 * @author Jiri Sedlacek
 */
public class JCompoundSplitPane extends JExtendedSplitPane {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private class DividerMouseListener extends MouseAdapter {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private double firstResizeWeight = 0;
        private double secondResizeWeight = 1;

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void mouseEntered(MouseEvent e) {
            configureComponents();
        }

        public void mousePressed(MouseEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        JSplitPane firstSplit = (JSplitPane) getFirstComponent();
                        JSplitPane secondSplit = (JSplitPane) getSecondComponent();
                        firstResizeWeight = firstSplit.getResizeWeight();
                        secondResizeWeight = secondSplit.getResizeWeight();
                        firstSplit.setResizeWeight(0);
                        secondSplit.setResizeWeight(1);
                    }
                });
        }

        public void mouseReleased(MouseEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        ((JSplitPane) getFirstComponent()).setResizeWeight(firstResizeWeight);
                        ((JSplitPane) getSecondComponent()).setResizeWeight(secondResizeWeight);
                    }
                });
        }

        private void configureComponents() {
            configureFirstComponent();
            configureSecondComponent();
        }

        private void configureFirstComponent() {
            JSplitPane firstSplit = (JSplitPane) getFirstComponent();
            int newWidth;
            int newHeight;

            newWidth = firstSplit.getMinimumSize().width;
            newHeight = 0;

            if (getFirstComponent(firstSplit).isVisible() && getSecondComponent(firstSplit).isVisible()) {
                newHeight = getFirstComponent(firstSplit).getSize().height
                            + getSecondComponent(firstSplit).getMinimumSize().height + firstSplit.getDividerSize();
            } else if (getFirstComponent(firstSplit).isVisible()) {
                newHeight = getFirstComponent(firstSplit).getMinimumSize().height;
            } else {
                newHeight = getSecondComponent(firstSplit).getMinimumSize().height;
            }

            firstSplit.setMinimumSize(new Dimension(newWidth, newHeight));
        }

        private void configureSecondComponent() {
            JSplitPane secondSplit = (JSplitPane) getSecondComponent();
            int newWidth = secondSplit.getMinimumSize().width;
            int newHeight = 0;

            if (getFirstComponent(secondSplit).isVisible() && getSecondComponent(secondSplit).isVisible()) {
                newHeight = getSecondComponent(secondSplit).getSize().height
                            + (getFirstComponent(secondSplit).isVisible()
                               ? (getFirstComponent(secondSplit).getMinimumSize().height + secondSplit.getDividerSize()) : 0);
            } else if (getFirstComponent(secondSplit).isVisible()) {
                newHeight = getFirstComponent(secondSplit).getMinimumSize().height;
            } else {
                newHeight = getSecondComponent(secondSplit).getMinimumSize().height;
            }

            secondSplit.setMinimumSize(new Dimension(newWidth, newHeight));
        }
    }

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public JCompoundSplitPane() {
        super();
        tweakUI();
    }

    public JCompoundSplitPane(int newOrientation) {
        super(newOrientation);
        tweakUI();
    }

    public JCompoundSplitPane(int newOrientation, boolean newContinuousLayout) {
        super(newOrientation, newContinuousLayout);
        tweakUI();
    }

    public JCompoundSplitPane(int newOrientation, boolean newContinuousLayout, Component newLeftComponent,
                              Component newRightComponent) {
        super(newOrientation, newContinuousLayout, newLeftComponent, newRightComponent);
        tweakUI();
    }

    public JCompoundSplitPane(int newOrientation, Component newLeftComponent, Component newRightComponent) {
        super(newOrientation, newLeftComponent, newRightComponent);
        tweakUI();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    private Component getFirstComponent() {
        return getFirstComponent(this);
    }

    private Component getFirstComponent(JSplitPane splitPane) {
        if (splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
            return splitPane.getLeftComponent();
        } else {
            return splitPane.getTopComponent();
        }
    }

    private Component getSecondComponent() {
        return getSecondComponent(this);
    }

    private Component getSecondComponent(JSplitPane splitPane) {
        if (splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
            return splitPane.getRightComponent();
        } else {
            return splitPane.getBottomComponent();
        }
    }

    private void tweakUI() {
        if (!(getUI() instanceof BasicSplitPaneUI)) {
            return;
        }

        BasicSplitPaneDivider divider = ((BasicSplitPaneUI) getUI()).getDivider();

        if (divider != null) {
            divider.addMouseListener(new DividerMouseListener());
        }
    }
}
