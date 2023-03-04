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
package org.netbeans.jellytools.modules.form;

import org.netbeans.jellytools.Bundle;

import org.netbeans.jemmy.operators.JCheckBoxOperator;

/**
 * Keeps methods to access component palette of form editor.
 * <p>
 * Usage:<br>
 * <pre>
        ComponentPaletteOperator cpo = new ComponentPaletteOperator();
        cpo.expandAWT();
        cpo.selectComponent("Label");
 * </pre>
 *
 * @author Jiri.Skrivanek@sun.com, mmirilovic@netbeans.org
 */
public class ComponentPaletteOperator extends org.netbeans.jellytools.PaletteOperator {
    
    private JCheckBoxOperator _cbSwingContainers;
    private JCheckBoxOperator _cbSwingControls;
    private JCheckBoxOperator _cbSwingMenus;
    private JCheckBoxOperator _cbSwingWindows;
    private JCheckBoxOperator _cbAWT;
    private JCheckBoxOperator _cbBeans;

    /** Waits for the Component Palette appearence and creates operator for it.
     */
    public ComponentPaletteOperator() {
        super();
    }

    //subcomponents
    
    /** Waits for "Swing Containers" check box button.
     * @return JCheckBoxOperator instance
     */
    public JCheckBoxOperator cbSwingContainers() {
        if(_cbSwingContainers == null) {
            _cbSwingContainers = new JCheckBoxOperator(
                                    this,
                                    Bundle.getString("org.netbeans.modules.form.resources.Bundle",
                                                     "FormDesignerPalette/SwingContainers"));  // NOI18N
        }
        return _cbSwingContainers;
    }
        
    /** Waits for ""Swing Controls check box button.
     * @return JCheckBoxOperator instance
     */
    public JCheckBoxOperator cbSwingControls() {
        if(_cbSwingControls == null) {
            _cbSwingControls = new JCheckBoxOperator(
                                    this,
                                    Bundle.getString("org.netbeans.modules.form.resources.Bundle",
                                                     "FormDesignerPalette/SwingControls"));  // NOI18N
        }
        return _cbSwingControls;
    }

    /** Waits for "Swing Menus" check box button.
     * @return JCheckBoxOperator instance
     */
    public JCheckBoxOperator cbSwingMenus() {
        if(_cbSwingMenus == null) {
            _cbSwingMenus = new JCheckBoxOperator(
                                    this,
                                    Bundle.getString("org.netbeans.modules.form.resources.Bundle",
                                                     "FormDesignerPalette/SwingMenus"));  // NOI18N
        }
        return _cbSwingMenus;
    }

    /** Waits for "Swing Windows" check box button.
     * @return JCheckBoxOperator instance
     */
    public JCheckBoxOperator cbSwingWindows() {
        if(_cbSwingWindows == null) {
            _cbSwingWindows = new JCheckBoxOperator(
                                    this,
                                    Bundle.getString("org.netbeans.modules.form.resources.Bundle",
                                                     "FormDesignerPalette/SwingWindows"));  // NOI18N
        }
        return _cbSwingWindows;
    }

    /** Waits for "AWT" check box button.
     * @return JCheckBoxOperator instance
     */
    public JCheckBoxOperator cbAWT() {
        if(_cbAWT == null) {
            _cbAWT = new JCheckBoxOperator(
                                    this, 
                                    Bundle.getString("org.netbeans.modules.form.resources.Bundle",
                                                     "FormDesignerPalette/AWT"));  // NOI18N
        }
        return _cbAWT;
    }
    
    /** Waits for "Beans" check box button.
     * @return JCheckBoxOperator instance
     */
    public JCheckBoxOperator cbBeans() {
        if(_cbBeans == null) {
            _cbBeans = new JCheckBoxOperator(
                                    this, 
                                    Bundle.getString("org.netbeans.modules.form.resources.Bundle",
                                                     "FormDesignerPalette/Beans"));  // NOI18N
        }
        return _cbBeans;
    }

    //shortcuts

    /** Expands Swing Containers and collapses all others. */
    public void expandSwingContainers() {
        collapseSwingControls();
        collapseSwingMenus();
        collapseSwingWindows();
        collapseAWT();
        collapseBeans();
        expand(cbSwingContainers(), true);
    }
    
    /** Expands Swing Controls and collapses all others. */
    public void expandSwingControls() {
        collapseSwingContainers();
        collapseSwingMenus();
        collapseSwingWindows();
        collapseAWT();
        collapseBeans();
        expand(cbSwingControls(), true);
    }

    /** Expands Swing Menus and collapses all others. */
    public void expandSwingMenus() {
        collapseSwingContainers();
        collapseSwingControls();
        collapseSwingWindows();
        collapseAWT();
        collapseBeans();
        expand(cbSwingMenus(), true);
    }
    /** Expands Swing Windows and collapses all others. */
    public void expandSwingWindows() {
        collapseSwingContainers();
        collapseSwingControls();
        collapseSwingMenus();
        collapseAWT();
        collapseBeans();
        expand(cbSwingWindows(), true);
    }

    /** Expands AWT components palette and collapses all others. */
    public void expandAWT() {
        collapseSwingContainers();
        collapseSwingControls();
        collapseSwingMenus();
        collapseSwingWindows();
        collapseBeans();
        expand(cbAWT(), true);
    }
    
    /** Expands Beans components palette and collapses all others. */
    public void expandBeans() {
        collapseSwingContainers();
        collapseSwingControls();
        collapseSwingMenus();
        collapseSwingWindows();
        collapseAWT();
        expand(cbBeans(), true);
    }

    /** Collapses Swing Containers palette. */
    public void collapseSwingContainers() {
        expand(cbSwingContainers(), false);
    }

    /** Collapses Swing Controls palette. */
    public void collapseSwingControls() {
        expand(cbSwingControls(), false);
    }
 
    /** Collapses Swing Menus palette. */
    public void collapseSwingMenus() {
        expand(cbSwingMenus(), false);
    }

    /** Collapses Swing Windows palette. */
    public void collapseSwingWindows() {
        expand(cbSwingWindows(), false);
    }

    /** Collapses AWT components palette. */
    public void collapseAWT() {
        expand(cbAWT(), false);
    }
    
    /** Collapses Beans components palette. */
    public void collapseBeans() {
        expand(cbBeans(), false);
    }

    /** Performs verification by accessing all sub-components */    
    @Override
    public void verify() {
        lstComponents();
        cbSwingContainers();
        cbSwingControls();
        cbSwingMenus();
        cbSwingWindows();
        cbAWT();
        cbBeans();
    }
}
