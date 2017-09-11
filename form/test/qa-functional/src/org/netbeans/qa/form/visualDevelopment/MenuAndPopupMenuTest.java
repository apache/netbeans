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
package org.netbeans.qa.form.visualDevelopment;

import java.io.IOException;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import java.util.*;
import junit.framework.Test;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.qa.form.ExtJellyTestCase;

/**
 * Menu and popup menu tests from NetBeans 5.5.1 Form Test Specification
 * from  Visual Development Test Specification
 * @see <a href="http://qa.netbeans.org/modules/form/promo-f/testspecs/visualDevelopment.html">Test specification</a>
 *
 * @author Jiri Vagner
 * 
 * <b>Adam Senk</b>
 * 20 April 2011 WORKS
 */
public class MenuAndPopupMenuTest extends ExtJellyTestCase {

    /** Constructor required by JUnit */
    public MenuAndPopupMenuTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws IOException {
        openDataProjects(_testProjectName);
    }

    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(MenuAndPopupMenuTest.class).addTest(
                "testMenuCreation",
                "testPopupMenuCreation").clusters(".*").enableModules(".*").gui(true));

    }

    public void testMenuCreation() {
        p("testMenuCreation");

        String frameName = createJFrameFile()+".java";
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode("SampleProject");
        prn.select();
        Node formnode = new Node(prn, "Source Packages|data|" + frameName);
        FormDesignerOperator designer = new FormDesignerOperator(frameName);
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                Node node = new Node(inspector.treeComponents(), "JFrame");
                runNoBlockPopupOverNode("Add From Palette|Swing Menus|Menu Bar", node);

            }
        });

        //Node node = new Node(inspector.treeComponents(), "JFrame"); // NOI18N

        Action openAction = new OpenAction();
        openAction.perform(formnode);
        designer = new FormDesignerOperator(frameName);
        //new Action(null, "Add From Palette|Swing Menus|Menu Bar").perform(node); // NOI18N
        findInCode("jMenuBar1 = new javax.swing.JMenuBar();", designer); // NOI18N



        inspector = new ComponentInspectorOperator();
        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                String menuPalettePath = "Add From Palette|";

                ArrayList<String> items = new ArrayList<String>();
                items.add(menuPalettePath + "Menu Item"); // NOI18N
                items.add(menuPalettePath + "Menu Item / CheckBox"); // NOI18N
                items.add(menuPalettePath + "Menu Item / RadioButton"); // NOI18N
                items.add(menuPalettePath + "Separator"); // NOI18N
                items.add(menuPalettePath + "Menu"); // NOI18N

                ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, false);

                Node node = new Node(inspector.treeComponents(), "[JFrame]|jMenuBar1 [JMenuBar]|jMenu1 [JMenu]"); // NOI18N
                runPopupOverNode(items, node, comparator);

                node = new Node(inspector.treeComponents(), "[JFrame]|jMenuBar1 [JMenuBar]|jMenu1 [JMenu]|jMenu3 [JMenu]"); // NOI18N
                runPopupOverNode(items, node, comparator);

            }
        });

        ArrayList<String> lines = new ArrayList<String>();
        lines.add("jMenu3.add(jMenu4);"); // NOI18N
        lines.add("jMenu1.add(jMenu3);"); // NOI18N
        lines.add("jMenuBar1.add(jMenu1);"); // NOI18N
        lines.add("setJMenuBar(jMenuBar1);"); // NOI18N

        openAction = new OpenAction();
        openAction.perform(formnode);
        designer = new FormDesignerOperator(frameName);

        findInCode(lines, designer);
        closeOpenedProjects();
        //removeFile(frameName);
    }

    public void testPopupMenuCreation() throws IOException {
        openDataProjects(_testProjectName);
        p("testPopupMenuCreation");


        String frameName = createJFrameFile()+".java";
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode("SampleProject");
        prn.select();
        Node formnode = new Node(prn, "Source Packages|data|" + frameName);
        FormDesignerOperator designer = new FormDesignerOperator(frameName);
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                String menuPalettePath = "Add From Palette|Swing Menus|";
                Node node = new Node(inspector.treeComponents(), "Other Components"); // NOI18N

                runPopupOverNode(menuPalettePath + "Popup Menu", node);

            }
        });

        Action openAction = new OpenAction();
        openAction.perform(formnode);
        designer = new FormDesignerOperator(frameName);
        findInCode("jPopupMenu1 = new javax.swing.JPopupMenu();", designer); // NOI18N

        inspector = new ComponentInspectorOperator();
        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                String popupMenuPalettePath = "Add From Palette|";
                ArrayList<String> items = new ArrayList<String>();
                items.add(popupMenuPalettePath + "Menu Item"); // NOI18N
                items.add(popupMenuPalettePath + "Menu Item / CheckBox"); // NOI18N
                items.add(popupMenuPalettePath + "Menu Item / RadioButton"); // NOI18N
                items.add(popupMenuPalettePath + "Separator"); // NOI18N
                items.add(popupMenuPalettePath + "Menu"); // NOI18N

                Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, false);
                Node node = new Node(inspector.treeComponents(), "Other Components|jPopupMenu1 [JPopupMenu]"); // NOI18N

                runPopupOverNode(items, node, comparator);

            }
        });

        ArrayList<String> lines = new ArrayList<String>();
        lines.add("jPopupMenu1.add(jMenuItem1);"); // NOI18N
        lines.add("jPopupMenu1.add(jCheckBoxMenuItem1)"); // NOI18N
        lines.add("jPopupMenu1.add(jRadioButtonMenuItem1);");
        lines.add("jPopupMenu1.add(jSeparator1);"); // NOI18N
        lines.add("jPopupMenu1.add(jMenu1);"); // NOI18N

        openAction = new OpenAction();
        openAction.perform(formnode);
        designer = new FormDesignerOperator(frameName);

        findInCode(lines, designer);

        //removeFile(frameName);

    }
}
