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

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.JComponent;
import javax.swing.border.Border;


/**
 *
 * @author Jiri Sedlacek
 */
public class ComponentMorpher extends JComponent implements ComponentListener, Accessible {
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

    public ComponentMorpher(JComponent component1, JComponent component2) {
        this(component1, component2, 10, 15);
    }

    public ComponentMorpher(JComponent component1, JComponent component2, int morphingSteps, int morphingDelay) {
        this.component1 = component1;
        this.component2 = component2;

        setMorphingSteps(morphingSteps);
        setMorphingDelay(morphingDelay);

        setLayout(null);
        setCurrentComponent(component1);

        addComponentListener(this);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null)
            accessibleContext = new AccessibleComponentMorpher();
        return accessibleContext;
    }
    
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

    public void componentHidden(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentResized(ComponentEvent e) {
        if (!isMorphing && (currentComponent != null)) {
            setCurrentComponent(currentComponent);
            revalidate();
        }
    }

    public void componentShown(ComponentEvent e) {
    }

    public void expand() {
        if (currentComponent != component2) {
            setCurrentComponent(component2);
        }
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

        // --- Workaround to update data for incorrectly sized endComponent due to nested multiline textcomponent (HTMLTextArea, JEditorPane, JTextArea...)
        if (endComponent.getSize().height != endComponent.getPreferredSize().height) {
            endComponent.setSize(new Dimension(getClientSize().width, endComponent.getPreferredSize().height));
            heightDelta = (float) (endComponent.getSize().height - startComponent.getSize().height) / (float) ((morphingStep == 0)
                                                                                                               ? morphingSteps
                                                                                                               : (morphingSteps
                                                                                                               - morphingStep + 1));
        }

        // ---
        if (morphingStep == 0) { // First step

            if (endComponent == component2) {
                setCurrentComponent(endComponent);
            }

            setClientPreferredSize(startComponent.getSize());
        } else if (morphingStep == morphingSteps) { // Last step

            if (endComponent == component1) {
                setCurrentComponent(endComponent);
            }

            setClientPreferredSize(endComponent.getSize());
            isMorphing = false;
        } else { // Intermediate step
            setClientPreferredSize(new Dimension(endComponent.getSize().width,
                                                 startComponent.getSize().height + (int) (morphingStep * heightDelta)));
        }

        revalidate();
        morphingStep++;
    }

    public void refreshLayout() {
        if (currentComponent != null) {
            setCurrentComponent(currentComponent);
        }

        revalidate();
    }

    public void reset() {
        if (currentComponent != component1) {
            setCurrentComponent(component1);
        }
    }

    public void setupMorphing() {
        startComponent = layoutComponent((currentComponent == component1) ? component1 : component2);
        endComponent = layoutComponent((currentComponent == component1) ? component2 : component1);

        heightDelta = (float) (endComponent.getSize().height - startComponent.getSize().height) / (float) morphingSteps;

        morphingStep = 0;
        isMorphing = true;

        setCurrentComponent(startComponent);
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

    private void setCurrentComponent(JComponent component) {
        boolean sameComponents = component == currentComponent;

        if (!sameComponents && (currentComponent != null)) {
            remove(currentComponent);
        }

        component = layoutComponent(component);
        currentComponent = component;

        if (!sameComponents) {
            add(currentComponent);
        }

        Insets insets = getInsets();
        currentComponent.setLocation(insets.left, insets.top);
        setClientPreferredSize(component.getSize());
    }

    private JComponent layoutComponent(JComponent component) {
        // Initial component sizing & layout
        if (getClientSize().width > 0) {
            component.setSize(getClientSize()); // try to fit the component to ComponentMorpher
            component.validate(); // layout component

            // Correct component sizing & layout
            component.setSize(new Dimension(getClientSize().width, component.getPreferredSize().height)); // Width of component is fixed, update height
            component.validate(); // layout component
        }

        return component;
    }
    
    
    protected class AccessibleComponentMorpher extends AccessibleJComponent {
        
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.PUSH_BUTTON;
        }
        
    }
}
