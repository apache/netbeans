/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.testrunner.providers;

import java.awt.Color;
import java.util.HashMap;
import java.util.ResourceBundle;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.gsf.testrunner.plugin.GuiUtilsProvider;
import org.netbeans.modules.java.testrunner.GuiUtils;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Theofanis Oikonomou
 */
@ServiceProvider(service=GuiUtilsProvider.class, position=10)
public class JavaGuiUtilsProvider extends GuiUtilsProvider {
    
    private HashMap<String, String> checkboxes = new HashMap<String, String>();
    {
        checkboxes.put("CHK_PUBLIC", GuiUtils.CHK_PUBLIC);
        checkboxes.put("CHK_PROTECTED", GuiUtils.CHK_PROTECTED);
        checkboxes.put("CHK_PACKAGE", GuiUtils.CHK_PACKAGE);
        checkboxes.put("CHK_PACKAGE_PRIVATE_CLASSES", GuiUtils.CHK_PACKAGE_PRIVATE_CLASSES);
        checkboxes.put("CHK_ABSTRACT_CLASSES", GuiUtils.CHK_ABSTRACT_CLASSES);
        checkboxes.put("CHK_EXCEPTION_CLASSES", GuiUtils.CHK_EXCEPTION_CLASSES);
        checkboxes.put("CHK_SUITES", GuiUtils.CHK_SUITES);
        checkboxes.put("CHK_SETUP", GuiUtils.CHK_SETUP);
        checkboxes.put("CHK_TEARDOWN", GuiUtils.CHK_TEARDOWN);
        checkboxes.put("CHK_BEFORE_CLASS", GuiUtils.CHK_BEFORE_CLASS);
        checkboxes.put("CHK_AFTER_CLASS", GuiUtils.CHK_AFTER_CLASS);
        checkboxes.put("CHK_METHOD_BODIES", GuiUtils.CHK_METHOD_BODIES);
        checkboxes.put("CHK_JAVADOC", GuiUtils.CHK_JAVADOC);
        checkboxes.put("CHK_HINTS", GuiUtils.CHK_HINTS);
        checkboxes.put("CHK_INTEGRATION_TESTS", GuiUtils.CHK_INTEGRATION_TESTS);
    }

    @Override
    public String getMessageFor(String key) {
        return NbBundle.getMessage(GuiUtils.class, key);
    }

    @Override
    public ResourceBundle getBundle() {
        return NbBundle.getBundle(GuiUtils.class);
    }

    @Override
    public JTextComponent createMultilineLabel(String text, Color color) {
        return GuiUtils.createMultilineLabel(text, color);
    }

    @Override
    public String getCheckboxText(String key) {        
        return checkboxes.get(key);
    }

    @Override
    public JCheckBox[] createCheckBoxes(String[] ids) {
        return GuiUtils.createCheckBoxes(ids);
    }

    @Override
    public JComponent createChkBoxGroup(String title, JCheckBox[] elements) {
        return GuiUtils.createChkBoxGroup(title, elements);
    }

    @Override
    public String getTestngFramework() {
        return GuiUtils.TESTNG_TEST_FRAMEWORK;
    }

    @Override
    public String getJunitFramework() {
        return GuiUtils.JUNIT_TEST_FRAMEWORK;
    }
    
}
