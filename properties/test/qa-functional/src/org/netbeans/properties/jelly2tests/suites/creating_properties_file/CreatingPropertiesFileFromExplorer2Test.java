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

/*
 * CreatingPropertiesFileFromExplorer2.java
 *
 * This is autometed test for netBeans version 40.
 *
 * Created on 19. brezen 2002, 11:07
 *
 */

package org.netbeans.properties.jelly2tests.suites.creating_properties_file;

import org.netbeans.jellytools.*;
import lib.PropertiesEditorTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;


/**
 *
 * @author  Petr Felenda - QA Engineer
 */
public class CreatingPropertiesFileFromExplorer2Test extends PropertiesEditorTestCase {

    /*
     * Definition of member variables and objects
     */
    final String PACKAGE_PATH = "examples";
    final String FILE_NAME = "testFileExplorer2";
    
    
    /**
     * Constructor - Creates a new instance of CreatingPropertiesFileFromExplorer2
     */
    public CreatingPropertiesFileFromExplorer2Test(String name) {
        super(name);
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(CreatingPropertiesFileFromExplorer2Test.class).addTest("testCreatingPropertiesFileFromExplorer2").enableModules(".*").clusters(".*"));
    }
    
    /**
     * This method contain body of test
     * @return void
     */
    public void testCreatingPropertiesFileFromExplorer2() {
        
        
        // open project
        openDefaultProject();
        
        /*
         * 1st step of testcase
         * In explorer create new properties file. Right click on any directory and
         * select in appeared context menu New|Other|Properties File.
         */
        Node node = new Node(new SourcePackagesNode(DEFAULT_PROJECT_NAME),PACKAGE_PATH);
        node.select();
        node.callPopup().pushMenuNoBlock("New"+menuSeparator+"Other...",menuSeparator);
        NewFileWizardOperator newWizard = new NewFileWizardOperator();
        newWizard.selectCategory(WIZARD_CATEGORY_FILE);
        newWizard.selectFileType(WIZARD_FILE_TYPE);
        newWizard.next();
        //type class name
        NewJavaFileNameLocationStepOperator nfnlso = new NewJavaFileNameLocationStepOperator();
        nfnlso.setObjectName(FILE_NAME);
        //push finish
        newWizard.finish();
        
       
        /*
         * 2nd step of testcase
         * In explorer create new properties file. Right click on any directory and select
         * in appeared context menu New|Other|Properties File
         */
        node.select();
        node.callPopup().pushMenuNoBlock("New"+menuSeparator+"Other...",menuSeparator);
        newWizard = new NewFileWizardOperator();
        newWizard.selectCategory(WIZARD_CATEGORY_FILE);
        newWizard.selectFileType(WIZARD_FILE_TYPE);
        newWizard.next();
        
        /*
         * 3th step of testcase
         * Type name to appeared wizard.(as same name as previous case)
         */
        nfnlso = new NewJavaFileNameLocationStepOperator();
        nfnlso.setObjectName(FILE_NAME);
        
        
        /*
         * 4th step of testcase - Result
         * Try confirm wizard. 'Finish' button should be disabled.
         */
        if ( nfnlso.btFinish().isEnabled() == true )
            fail("Button finish is enabled and should be disabled.Because file with this name exist.");
        else
            log("Button is disabled. (Ok)");
        
        
        /*
         * 5th step of testcase
         * Cancel wizard. Click to 'Cancel' button.
         */
        nfnlso.btCancel().push();
        
    }
    
    public void tearDown() {
        log("Teardown");
        //closePropertiesFileWithoutCheck(FILE_NAME); 
        closeOpenedProjects();
    }
    
    
    
}
