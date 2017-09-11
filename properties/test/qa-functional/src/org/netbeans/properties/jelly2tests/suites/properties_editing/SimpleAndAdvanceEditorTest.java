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

package org.netbeans.properties.jelly2tests.suites.properties_editing;

import lib.PropertiesEditorTestCase;
import junit.framework.Test;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.nodes.PropertiesNode;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author Admin
 */
public class SimpleAndAdvanceEditorTest extends PropertiesEditorTestCase{
    
    ProjectRootNode prn;
    ProjectsTabOperator pto;
    PropertiesNode pn;
    String bundleName = "Test";

    public SimpleAndAdvanceEditorTest(String name) {
        super(name);
    }
     
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(SimpleAndAdvanceEditorTest.class).addTest(
                "testCreateNewBundle",
                "testOpenningSimpleEditorFromFileExplorer",
                "testOpenningAdvanceEditorFromFileExplorer",
                "testCreateNewBundleInDefaultPackage",
                "testOpenningSimpleEditorFromProjectTab",
                "testOpenningAdvanceEditorFromProjectTab").enableModules(".*").clusters(".*"));
    }
    
    //This test create new bundle file in the root of the project
    public void testCreateNewBundle() throws Exception{
        //open default project
        openDefaultProject();
        
        //create new bundle in default package
        pto = new ProjectsTabOperator().invoke();
        prn = new ProjectRootNode(new JTreeOperator(pto), DEFAULT_PROJECT_NAME);
        createNewPropertiesFile(prn);
        
        //check that new properties file was created and is accessible via file tab
        boolean result = existsFileInFilesTab(WIZARD_DEFAULT_PROPERTIES_FILE_NAME);
        
        if (result) {
           log(WIZARD_DEFAULT_PROPERTIES_FILE_NAME + ".properties file was created and was found in File explorer");
        } else {
            throw new Exception(WIZARD_DEFAULT_PROPERTIES_FILE_NAME + ".properties file was not found in File explorer");
        }
        
        //close Simple editor
        closePropertiesFile(WIZARD_DEFAULT_PROPERTIES_FILE_NAME);
    }
    
    public void testOpenningSimpleEditorFromFileExplorer() throws Exception{
        pn=getNodeFilesTab(DEFAULT_PROJECT_NAME, WIZARD_DEFAULT_PROPERTIES_FILE_NAME+".properties");
        pn.edit();
        
        //Check if properties file was opened in simple editor
        boolean result=existsFileInEditor(WIZARD_DEFAULT_PROPERTIES_FILE_NAME);
        
        if (result) {
            log(WIZARD_DEFAULT_PROPERTIES_FILE_NAME + " was opened in simple editor");
        }else {
            throw new Exception(WIZARD_DEFAULT_PROPERTIES_FILE_NAME +  " was not opened in simple editor");
        }
    }
    
    public void testOpenningAdvanceEditorFromFileExplorer() throws Exception{
        pn=getNodeFilesTab(DEFAULT_PROJECT_NAME, WIZARD_DEFAULT_PROPERTIES_FILE_NAME+".properties");
        pn.open();
        
        //Check if properties file was opened in advance editor
        boolean result=existsFileInAdvanceEditor(WIZARD_DEFAULT_PROPERTIES_FILE_NAME);
        
        if (result) {
            log(WIZARD_DEFAULT_PROPERTIES_FILE_NAME + " was opened in advance editor");
        }else {
            throw new Exception(WIZARD_DEFAULT_PROPERTIES_FILE_NAME +  " was not opened in advance editor");
        }
    }
    
    public void testCreateNewBundleInDefaultPackage() throws Exception{
        pto = new ProjectsTabOperator().invoke();
        prn = pto.getProjectRootNode(DEFAULT_PROJECT_NAME);
        prn.select();
        
        //Create new properties file
        createNewPropertiesFile(prn, defaultPackageDir, bundleName);
        
        //Check if bundle was created and opened
        boolean result = existsFileInExplorer(defaultPackage, bundleName);
        
        if (result) {
            log("Properties file" + bundleName + ".properties was created in default package");
        } else {
            throw new Exception("Properties file" + bundleName + ".properties is not reachable in Project Tree");
        }
        
        //Check if bundle was automatically opened in simple editor
        result = closePropertiesFile(bundleName);
        
        if (result) {
            log("Properties file" + bundleName + ".properties was automatically opened in simple editor and finally closed");
        } else {
            throw new Exception("Properties file" + bundleName + ".properties was not automatically opened in simple editor while creating");
        }
 
    }
    
    public void testOpenningSimpleEditorFromProjectTab() throws Exception{
        pn = getNode(DEFAULT_PROJECT_NAME, rootPackageName + TREE_SEPARATOR + defaultPackage + TREE_SEPARATOR + bundleName + ".properties");
        pn.edit();
        
        //Check if bundle was opened in simple editor
        boolean result = closePropertiesFile(bundleName);
        
        if (result) {
            log("Properties file" + bundleName + ".properties was automatically opened in simple editor and finally closed");
        } else {
            throw new Exception("Properties file" + bundleName + ".properties was not automatically opened in simple editor while creating");
        }
    }
    
    public void testOpenningAdvanceEditorFromProjectTab() throws Exception{
        pn = getNode(DEFAULT_PROJECT_NAME, rootPackageName + TREE_SEPARATOR + defaultPackage + TREE_SEPARATOR + bundleName + ".properties");
        pn.open();
        
        //Check if properties file was opened in advance editor
        boolean result=existsFileInAdvanceEditor(bundleName);
        
        if (result) {
            log("Properties file" + bundleName + " was opened in advance editor");
        }else {
            throw new Exception("Properties file" + bundleName + " was not opened in advance editor");
        }
    }

}
