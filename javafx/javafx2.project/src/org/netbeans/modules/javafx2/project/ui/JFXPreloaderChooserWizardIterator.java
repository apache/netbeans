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
package org.netbeans.modules.javafx2.project.ui;

import java.awt.Component;
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;

public final class JFXPreloaderChooserWizardIterator implements WizardDescriptor.Iterator {

    private int index;
    private WizardDescriptor.Panel[] panels = new WizardDescriptor.Panel[] {null, null};
    String[] steps = new String[] {null, null};

    private WizardDescriptor.Panel getPanel(int index) {
        assert 0 <= index && index <= 1;
        if(panels[index] == null) {
            WizardDescriptor.Panel panel = null;
            switch(index) {
                case 0 : 
                    panel = new JFXPreloaderChooserWizardPanel1(); break;
                case 1 : 
                    JFXPreloaderChooserVisualPanel1 c = (JFXPreloaderChooserVisualPanel1)panels[0].getComponent();
                    panel = new JFXPreloaderChooserWizardPanel2(c.getSelectedType()); 
                    break;
            }
            panels[index] = panel;
            Component c = panel.getComponent();
            steps[index] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Sets step number of a component
                // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_*:
                jc.putClientProperty("WizardPanel_contentSelectedIndex", index);
                // Sets steps names for a panel
                jc.putClientProperty("WizardPanel_contentData", steps);
                // Turn on subtitle creation on each step
                jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
                // Show steps on the left side with the image on the background
                jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
                // Turn on numbering of all steps
                jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
            }        
        }
        assert panels[index] != null;
        return panels[index];
    }
        
//    /**
//     * Initialize panels representing individual wizard's steps and sets
//     * various properties for them influencing wizard appearance.
//     */
//    private WizardDescriptor.Panel[] getPanels() {
//        if (panels == null) {
//            panels = new WizardDescriptor.Panel[]{
//                new JFXPreloaderChooserWizardPanel1(),
//                new JFXPreloaderChooserWizardPanel2(),
//                new JFXPreloaderChooserWizardPanel3JAR()
//            };
//            String[] steps = new String[panels.length];
//            for (int i = 0; i < panels.length; i++) {
//                Component c = panels[i].getComponent();
//                // Default step name to component name of panel.
//                steps[i] = c.getName();
//                if (c instanceof JComponent) { // assume Swing components
//                    JComponent jc = (JComponent) c;
//                    // Sets step number of a component
//                    // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_*:
//                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
//                    // Sets steps names for a panel
//                    jc.putClientProperty("WizardPanel_contentData", steps);
//                    // Turn on subtitle creation on each step
//                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
//                    // Show steps on the left side with the image on the background
//                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
//                    // Turn on numbering of all steps
//                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
//                }
//            }
//        }
//        return panels;
//    }

    @Override
    public WizardDescriptor.Panel current() {
//        WizardDescriptor.Panel panel = null;
//        if(index == 0) {
//            panel = getPanel(0);
//        } else {
//            JFXProjectProperties.PreloaderSourceType selectedType=((JFXPreloaderChooserVisualPanel1)getPanel(0).getComponent()).getSelectedType();
//            panel = selectedType == JFXProjectProperties.PreloaderSourceType.PROJECT ? getPanel(1) : getPanel(2) ;
//        }
//        assert panel != null;
//        return panel;
        assert 0 <= index && index <= 1;
        return getPanel(index);
    }

    @Override
    public String name() {
        return index + 1 + ". from 2";
    }

    @Override
    public boolean hasNext() {
        return index < 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
        if(index == 1 && panels[index] != null) {
            JFXPreloaderChooserWizardPanel2 panel = (JFXPreloaderChooserWizardPanel2)panels[index];
            JFXPreloaderChooserVisualPanel1 c = (JFXPreloaderChooserVisualPanel1)panels[0].getComponent();
            panel.setSourceType(c.getSelectedType());
        }
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }
    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then uncomment
    // the following and call when needed: fireChangeEvent();
    /*
    private Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0
    public final void addChangeListener(ChangeListener l) {
    synchronized (listeners) {
    listeners.add(l);
    }
    }
    public final void removeChangeListener(ChangeListener l) {
    synchronized (listeners) {
    listeners.remove(l);
    }
    }
    protected final void fireChangeEvent() {
    Iterator<ChangeListener> it;
    synchronized (listeners) {
    it = new HashSet<ChangeListener>(listeners).iterator();
    }
    ChangeEvent ev = new ChangeEvent(this);
    while (it.hasNext()) {
    it.next().stateChanged(ev);
    }
    }
     */
}
