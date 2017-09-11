/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.project.ant.ui;

import org.netbeans.modules.project.ant.ui.VariablesModel;
import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ant.ui.VariablesModel.Variable;
import org.netbeans.spi.project.support.ant.PropertyUtils;

/**
 *
 */
public class VariablesModelTest extends NbTestCase {
    
    public VariablesModelTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of getVariables method, of class VariablesModel.
     */
    public void testBasicFunctionality() throws IOException {
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        VariablesModel model = new VariablesModel();
        assertEquals(0, model.getVariables().size());
        int baseCount = PropertyUtils.getGlobalProperties().entrySet().size();
        File var1 = new File(getWorkDir(), "var1_root");
        var1.mkdir();
        File var2 = new File(getWorkDir(), "var2_root");
        var2.mkdir();
        model.add("TEST_BASE_1", var1);
        model.add("TEST_BASE_2", var2);
        assertEquals(new Variable("TEST_BASE_1", var1), model.find("TEST_BASE_1"));
        assertEquals(new Variable("TEST_BASE_2", var2), model.find("TEST_BASE_2"));
        assertEquals("TEST_BASE_1/", model.getRelativePath(var1, true));
        assertEquals("${var.TEST_BASE_2}/", model.getRelativePath(var2, false));
        assertEquals(2, model.getVariables().size());
        model.save();
        assertEquals(baseCount+2, PropertyUtils.getGlobalProperties().entrySet().size());
        model.remove(new Variable("TEST_BASE_2", var2));
        assertEquals(1, model.getVariables().size());
        model.save();
        assertEquals(baseCount+1, PropertyUtils.getGlobalProperties().entrySet().size());
        File f = new File(var1, "folder");
        f.mkdir();
        File f2 = new File(f, "folder2");
        f2.mkdir();
        assertEquals("TEST_BASE_1/folder/folder2", model.getRelativePath(f2, true));
        assertEquals("${var.TEST_BASE_1}/folder", model.getRelativePath(f, false));
        assertEquals(var1.getAbsolutePath(), PropertyUtils.getGlobalProperties().getProperty("var.TEST_BASE_1"));
    }

}
