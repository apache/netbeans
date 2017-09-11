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

package org.netbeans.modules.apisupport.project.suite;

import static junit.framework.Assert.*;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.spi.BrandingModel;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.modules.apisupport.project.ui.customizer.SuitePropertiesTest;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.util.test.MockLookup;

public class SuiteBrandingModelTest extends NbTestCase {

    public SuiteBrandingModelTest(String name) {
        super(name);
    }

    @Override protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockLookup.setLayersAndInstances();
    }

    public void testBrandingToken() throws Exception { // #197066
        SuiteProject p = TestBase.generateSuite(getWorkDir(), "s");
        // Adding branding:
        SuiteProperties sp = SuitePropertiesTest.getSuiteProperties(p);
        BrandingModel m = sp.getBrandingModel();
        m.setBrandingEnabled(true);
        m.setName("myapp");
        m.setTitle("My App");
        m.doSave();
        ProjectManager.getDefault().saveProject(p);
        EditableProperties ep = p.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals(null, ep.getProperty(SuiteBrandingModel.BRANDING_TOKEN_PROPERTY));
        assertEquals("${" + SuiteBrandingModel.BRANDING_TOKEN_PROPERTY + "}", ep.getProperty(SuiteBrandingModel.NAME_PROPERTY));
        assertEquals("My App", ep.getProperty(SuiteBrandingModel.TITLE_PROPERTY));
        ep = p.getHelper().getProperties("nbproject/platform.properties");
        assertEquals("myapp", ep.getProperty(SuiteBrandingModel.BRANDING_TOKEN_PROPERTY));
        // Removing branding:
        sp = SuitePropertiesTest.getSuiteProperties(p);
        m = sp.getBrandingModel();
        assertEquals("myapp", m.getName());
        m.setBrandingEnabled(false);
        m.doSave();
        ProjectManager.getDefault().saveProject(p);
        ep = p.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals(null, ep.getProperty(SuiteBrandingModel.BRANDING_TOKEN_PROPERTY));
        assertEquals(null, ep.getProperty(SuiteBrandingModel.NAME_PROPERTY));
        assertEquals(null, ep.getProperty(SuiteBrandingModel.TITLE_PROPERTY));
        ep = p.getHelper().getProperties("nbproject/platform.properties");
        assertEquals(null, ep.getProperty(SuiteBrandingModel.BRANDING_TOKEN_PROPERTY));
        // Updating branding:
        ep = p.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.put(SuiteBrandingModel.NAME_PROPERTY, "myoldapp");
        ep.put(SuiteBrandingModel.TITLE_PROPERTY, "My Old App");
        ep.put(SuiteBrandingModel.BRANDING_TOKEN_PROPERTY, "${" + SuiteBrandingModel.NAME_PROPERTY + "}");
        p.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        sp = SuitePropertiesTest.getSuiteProperties(p);
        m = sp.getBrandingModel();
        assertTrue(m.isBrandingEnabled());
        assertEquals("myoldapp", m.getName());
        m.setName("myupdatedapp");
        m.setTitle("My Updated App");
        m.doSave();
        ProjectManager.getDefault().saveProject(p);
        ep = p.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals(null, ep.getProperty(SuiteBrandingModel.BRANDING_TOKEN_PROPERTY));
        assertEquals("${" + SuiteBrandingModel.BRANDING_TOKEN_PROPERTY + "}", ep.getProperty(SuiteBrandingModel.NAME_PROPERTY));
        assertEquals("My Updated App", ep.getProperty(SuiteBrandingModel.TITLE_PROPERTY));
        ep = p.getHelper().getProperties("nbproject/platform.properties");
        assertEquals("myupdatedapp", ep.getProperty(SuiteBrandingModel.BRANDING_TOKEN_PROPERTY));
    }

}
