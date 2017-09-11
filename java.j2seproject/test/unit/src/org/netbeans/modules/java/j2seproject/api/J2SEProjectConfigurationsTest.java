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

package org.netbeans.modules.java.j2seproject.api;

import java.io.File;
import java.util.Properties;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.test.MockLookup;

/**
 * Test of class org.netbeans.modules.java.j2seproject.api.J2SEProjectConfigurations
 * 
 * @author Milan Kubec
 */
public class J2SEProjectConfigurationsTest extends NbTestCase {
    
    public J2SEProjectConfigurationsTest(String testName) {
        super(testName);
    }
    
    /**
     * Test of createConfigurationFiles method
     */
    public void testCreateConfigurationFiles() throws Exception {
        
        System.out.println("createConfigurationFiles");
        
        File proj = getWorkDir();
        clearWorkDir();
        MockLookup.setLayersAndInstances();
        
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.6"));
        AntProjectHelper aph = J2SEProjectGenerator.createProject(proj, "TestProject", null, "manifest.mf", null, false);
        
        Project prj = ProjectManager.getDefault().findProject(aph.getProjectDirectory());
        
        String configName = "TestConfig";
        
        Properties sharedProps = new Properties();
        sharedProps.put("sharedPropName", "sharedPropValue");
        sharedProps.put("$sharedPropNameSpecial", "sharedPropValueSpecial");
        sharedProps.put("sharedPropName2", "${sharedPropName}");
        
        Properties privateProps = new Properties();
        privateProps.put("privatePropName", "privatePropValue");
        privateProps.put("privatePropName2", "${privatePropName}");
        
        J2SEProjectConfigurations.createConfigurationFiles(prj, configName, sharedProps, privateProps);
        
        FileObject prjDirFO = prj.getProjectDirectory();
        
        FileObject sharedPropsFO = prjDirFO.getFileObject("nbproject/configs/" + configName + ".properties");
        Properties loadedSharedProps = new Properties();
        loadedSharedProps.load(sharedPropsFO.getInputStream());
        assertEquals(sharedProps, loadedSharedProps);
        
        FileObject privatePropsFO = prjDirFO.getFileObject("nbproject/private/configs/" + configName + ".properties");
        Properties loadedPrivateProps = new Properties();
        loadedPrivateProps.load(privatePropsFO.getInputStream());
        assertEquals(privateProps, loadedPrivateProps);
        
        configName = "Test_Config2";
        
        EditableProperties sharedProps2 = new EditableProperties(true);
        sharedProps2.put("sharedPropName", "sharedPropValue");
        sharedProps2.put("$sharedPropNameSpecial", "sharedPropValueSpecial");
        sharedProps2.put("sharedPropName2", "${sharedPropName}");
        
        J2SEProjectConfigurations.createConfigurationFiles(prj, configName, sharedProps2, null);
        
        sharedPropsFO = prjDirFO.getFileObject("nbproject/configs/" + configName + ".properties");
        loadedSharedProps = new Properties();
        loadedSharedProps.load(sharedPropsFO.getInputStream());
        assertEquals(sharedProps2, loadedSharedProps);
        
        privatePropsFO = prjDirFO.getFileObject("nbproject/private/configs/" + configName + ".properties");
        assertNull(privatePropsFO);
        
    }
    
}
