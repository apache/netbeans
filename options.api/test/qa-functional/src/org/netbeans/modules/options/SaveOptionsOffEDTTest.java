/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.options;

import java.util.Arrays;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author theofanis
 */
public class SaveOptionsOffEDTTest extends JellyTestCase {

    private OptionsOperator optionsOperator;
    private JTabbedPaneOperator jTabbedPaneOperator;
    
    /**
     * Constructor required by JUnit
     */
    public SaveOptionsOffEDTTest(String testName) {
        super(testName);
    }

    @Override
    protected int timeOut() {
        return 700000;
    }

    /**
     * Creates suite from particular test cases.
     */
    public static Test suite() {
        return NbModuleSuite.createConfiguration(SaveOptionsOffEDTTest.class).
                addTest("testSaveOptionsOffEDT").
                addTest("testCorrectTabSelected").clusters(".*").enableModules(".*").gui(true).suite();
    }
    
    public void testSaveOptionsOffEDT() {
        for(String category: Arrays.asList("General", "FontsAndColors", "Editor", "Keymaps", "Java", "PHP", "C/C++", "Miscellaneous")) {
            selectTabAndSave(category);
        }
    }
    
    public void testCorrectTabSelected() {
        for(String category: Arrays.asList("General", "FontsAndColors", "Editor", "Keymaps", "Java", "PHP", "C/C++", "Miscellaneous")) {
            selectTabAndClose(category);
        }
    }
    
    private void selectTabAndClose(String category) {
        open(category, true);
        if (category.equals("General") || category.equals("Keymaps")) {
            optionsOperator.btOK().doClick();
        } else {
            jTabbedPaneOperator = new JTabbedPaneOperator(optionsOperator);
            for (int i = 0; i < jTabbedPaneOperator.getTabCount(); i++) {
                if (i > 0) {
                    open(category, false);
                    jTabbedPaneOperator = new JTabbedPaneOperator(optionsOperator);
                    assertEquals("Wrong selected tab index", i - 1, jTabbedPaneOperator.getSelectedIndex());
                }
                jTabbedPaneOperator.selectPage(i);
                new EventTool().waitNoEvent(500);
                if(i % 2 == 0) {
                    optionsOperator.btOK().doClick();
                } else {
                    optionsOperator.btCancel().doClick();
                }
            }
        }
    }
    
    private void selectTabAndSave(String category) {
        open(category, true);
        if (category.equals("General") || category.equals("Keymaps")) {
            optionsOperator.btOK().doClick();
        } else {
            jTabbedPaneOperator = new JTabbedPaneOperator(optionsOperator);
            for (int i = 0; i < jTabbedPaneOperator.getTabCount(); i++) {
                if (i > 0) {
                    open(category, false);
                    jTabbedPaneOperator = new JTabbedPaneOperator(optionsOperator);
                }
                jTabbedPaneOperator.selectPage(i);
                new EventTool().waitNoEvent(500);
                optionsOperator.btOK().doClick();
            }
        }
    }
    
    private void open(String category, boolean selectCategory) {
        optionsOperator = OptionsOperator.invoke();
        optionsOperator.setLocation(100, 100);
        new EventTool().waitNoEvent(500);
        
        if (selectCategory) {
            if (category.equals("General")) {
                optionsOperator.selectGeneral();
            } else if (category.equals("Editor")) {
                optionsOperator.selectEditor();
            } else if (category.equals("FontsAndColors")) {
                optionsOperator.selectFontAndColors();
            } else if (category.equals("Keymaps")) {
                optionsOperator.selectKeymap();
            } else if (category.equals("Java")) {
                optionsOperator.selectJava();
            } else if (category.equals("Miscellaneous")) {
                optionsOperator.selectMiscellaneous();
            } else if (category.equals("PHP") || category.equals("C/C++")) {
                optionsOperator.selectCategory(category);
            }
            new EventTool().waitNoEvent(500);
        }
    }
}
