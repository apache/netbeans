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
package org.netbeans.qa.form.beans;

import java.awt.Component;
import junit.framework.Test;
import org.netbeans.qa.form.*;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.*;
import org.netbeans.qa.form.ExtJellyTestCase;
import org.netbeans.jellytools.modules.form.ComponentPaletteOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbModuleSuite;

/**
 * Tests adding and removing beans into/from palette
 *
 * @author Jiri Vagner
 * 
 * <b>Adam Senk</b>
 * 20 April 2011 WORKS
 */
public class AddAndRemoveBeansTest extends ExtJellyTestCase {

    public static String VISUAL_BEAN_NAME = "TestVisualBean"; // NOI18N
    public static String NONVISUAL_BEAN_NAME = "TestNonVisualBean"; // NOI18N
    public static String TESTED_BEAN_TEXT = "Lancia Lybra"; // NOI18N
    public static String TESTED_BEAN_POWER = "140"; // NOI18N

    /**
     * Constructor required by JUnit
     */
    public AddAndRemoveBeansTest(String testName) {
        super(testName);
    }

    /**
     * Creates suite from particular test cases.
     */
    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(AddAndRemoveBeansTest.class).addTest(
                "testAddingBeans", 
                "testRemovingBeans"
                ).gui(true).clusters(".*").enableModules(".*"));

    }

    /**
     *  Tests "Add Bean" dialog
     */
    public void testAddingBeans() {
        addBean(VISUAL_BEAN_NAME + ".java"); // NOI18N
        addBean(NONVISUAL_BEAN_NAME + ".java"); // NOI18N
    }

    /**
     * Tests removing bean using Palette Manager
     */
    public void testRemovingBeans() {
        Action ac = new Action("Tools|Palette|Swing/AWT Components", null);
        ac.setComparator(new DefaultStringComparator(true, true));
        ac.perform();

        PaletteManagerOperator manOp = new PaletteManagerOperator();
        JTreeOperator treeOp = manOp.treePaletteContentsTree();

        treeOp.clickOnPath(treeOp.findPath("Beans|" + VISUAL_BEAN_NAME, "|")); // NOI18N
        manOp.remove();
        new NbDialogOperator("Confirm").yes(); // NOI18N

        treeOp.clickOnPath(treeOp.findPath("Beans|" + NONVISUAL_BEAN_NAME, "|")); // NOI18N
        manOp.remove();
        new NbDialogOperator("Confirm").yes(); // NOI18N

        manOp.close();
    }

    /**
     * Tests removing beans using popup menu from palette
     */
    public void testRemovingBeansFromPalette() {
        openFile("clear_Frame.java");

        ComponentPaletteOperator palette = new ComponentPaletteOperator();
        palette.expandBeans();
        palette.collapseSwingContainers();
        palette.collapseSwingMenus();
        palette.collapseSwingWindows();
        palette.collapseAWT();
        palette.collapseSwingControls();

        JListOperator list = palette.lstComponents();
        list.clickOnItem(NONVISUAL_BEAN_NAME, new Operator.DefaultStringComparator(true, false));

        // TODO: I'm not able to invoke popup menu :(
        int i = list.findItemIndex(NONVISUAL_BEAN_NAME, new Operator.DefaultStringComparator(true, false));
        p(i);

        Component[] comps = list.getComponents();
        p(comps.length);
        for (Component comp : comps) {
            p(comp.toString());
        }
    }
}