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
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;


/**
 * Not used now, getting component2 image on Mac OS X doesn't work
 *
 * @author Jiri Sedlacek
 */
public class ComponentMorpher2 extends JComponent {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private class MorpherThread extends Thread {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void run() {
            setupMorphing();

            while (isMorphing()) {
                morphingStep();

                try {
                    Thread.sleep(morphingDelay);
                } catch (InterruptedException ie) {
                }
            }
        }
    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private Image endComponentImage;
    private Image startComponentImage;
    private ImageBlenderPanel blenderPanel;
    private JComponent component1;
    private JComponent component2;
    private JComponent currentComponent;
    private JComponent endComponent;

    // --- Morphing stuff --------------------------------------------------------
    private JComponent startComponent;
    private boolean isMorphing = false;
    private float heightDelta;
    private int morphingDelay;
    private int morphingStep;
    private int morphingSteps;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ComponentMorpher2(JComponent component1, JComponent component2) {
        this(component1, component2, 10, 15);
    }

    public ComponentMorpher2(JComponent component1, JComponent component2, int morphingSteps, int morphingDelay) {
        this.component1 = component1;
        this.component2 = component2;

        setMorphingSteps(morphingSteps);
        setMorphingDelay(morphingDelay);

        setLayout(new BorderLayout());
        setCurrentComponent(component1);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setBorder(Border border) {
        super.setBorder(border);
        setClientPreferredSize(currentComponent.getPreferredSize());
    }

    public boolean isExpanded() {
        return currentComponent == component2;
    }

    public boolean isMorphing() {
        return isMorphing;
    }

    public void setMorphingDelay(int morphingDelay) {
        this.morphingDelay = morphingDelay;
    }

    public int getMorphingDelay() {
        return morphingDelay;
    }

    public void setMorphingSteps(int morphingSteps) {
        this.morphingSteps = morphingSteps;
    }

    public int getMorphingSteps() {
        return morphingSteps;
    }

    public void morph() {
        if (!isMorphing()) {
            new MorpherThread().start();
        }
    }

    public void morphingStep() {
        if (morphingStep > morphingSteps) {
            return;
        }

        if (morphingStep == 0) { // First step
            setCurrentComponent(blenderPanel);
        } else if (morphingStep == morphingSteps) { // Last step
            setCurrentComponent(endComponent);
            isMorphing = false;
        } else { // Intermediate step

            Dimension newDim = new Dimension(getClientSize().width,
                                             startComponentImage.getHeight(null) + (int) (morphingStep * heightDelta));
            blenderPanel.setBlendAlpha((float) morphingStep / (float) morphingSteps);
            blenderPanel.setSize(newDim);
            setClientPreferredSize(newDim);
        }

        refresh();
        morphingStep++;
    }

    public void refresh() {
        revalidate();
        repaint();
    }

    public void setupMorphing() {
        startComponent = (currentComponent == component1) ? component1 : component2;
        endComponent = (currentComponent == component1) ? component2 : component1;

        startComponentImage = getComponentImage(startComponent);
        endComponentImage = getComponentImage(endComponent);

        heightDelta = (float) (endComponentImage.getHeight(null) - startComponentImage.getHeight(null)) / (float) morphingSteps;

        blenderPanel = new ImageBlenderPanel(startComponentImage, endComponentImage, component1.getBackground(), 0);
        blenderPanel.setPreferredSize(new Dimension(startComponentImage.getWidth(null), startComponentImage.getHeight(null)));

        morphingStep = 0;
        isMorphing = true;
    }

    private void setClientPreferredSize(Dimension size) {
        Insets insets = getInsets();
        setPreferredSize(new Dimension(size.width + insets.left + insets.right, size.height + insets.top + insets.bottom));
    }

    private Dimension getClientSize() {
        Dimension size = getSize();
        Insets insets = getInsets();

        return new Dimension(size.width - insets.left - insets.right, size.height - insets.top - insets.bottom);
    }

    private Image getComponentImage(JComponent component) {
        // Initial component sizing & layout
        component.setSize((getClientSize().width == 0) ? component.getPreferredSize() : getClientSize()); // try to fit the component to ComponentMorpher
        component.doLayout(); // layout component

        // Correct component sizing & layout
        component.setSize(new Dimension(getClientSize().width, component.getPreferredSize().height)); // Width of component is fixed, update height
        component.doLayout(); // layout component

        // One more iteration because of nested JTextAreas
        component.setSize(new Dimension(getClientSize().width, component.getPreferredSize().height)); // Width of component is fixed, update height
        component.doLayout(); // layout component

        // Paint component into BufferedImage
        BufferedImage componentImage = new BufferedImage(component.getSize().width, component.getSize().height,
                                                         BufferedImage.TYPE_INT_RGB);
        component.printAll(componentImage.getGraphics());

        return componentImage;
    }

    private void setCurrentComponent(JComponent component) {
        if (currentComponent != null) {
            remove(currentComponent);
        }

        currentComponent = component;
        add(currentComponent, BorderLayout.CENTER);
        setClientPreferredSize(currentComponent.getPreferredSize());
    }
}
