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
