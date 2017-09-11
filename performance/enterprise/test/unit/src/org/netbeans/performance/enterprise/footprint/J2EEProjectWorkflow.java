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

package org.netbeans.performance.enterprise.footprint;

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.EditAction;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.utilities.MemoryFootprintTestCase;
import org.netbeans.modules.performance.utilities.CommonUtilities;


/**
 * Measure J2EE Project Workflow Memory footprint
 *
 * @author  mmirilovic@netbeans.org, mrkam@netbeans.org
 */
public class J2EEProjectWorkflow extends MemoryFootprintTestCase {
    
    private String j2eeproject, j2eeproject_ejb, j2eeproject_war, j2eeproject_app;
    
    /**
     * Creates a new instance of J2EEProjectWorkflow
     *
     * @param testName the name of the test
     */
    public J2EEProjectWorkflow(String testName) {
        super(testName);
        prefix = "J2EE Project Workflow |";
    }
    
    /**
     * Creates a new instance of J2EEProjectWorkflow
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public J2EEProjectWorkflow(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        prefix = "J2EE Project Workflow |";
    }
    
    @Override
    public void setUp() {
        // do nothing
    }
    
    public void prepare() {
    }
    
    @Override
    public void initialize() {
        super.initialize();
        EPFootprintUtilities.closeAllDocuments();
        EPFootprintUtilities.closeMemoryToolbar();
    }
    
    public ComponentOperator open(){
        // Create, edit, build and execute a sample J2EE project
        // Create, edit, build and execute a sample J2EE project
        j2eeproject = EPFootprintUtilities.creatJ2EEeproject("Enterprise", "Enterprise Application", true);  // NOI18N
        j2eeproject_ejb = j2eeproject + "-ejb";
        j2eeproject_war = j2eeproject + "-war";
        j2eeproject_app = j2eeproject + "-app-client";
        
        //EPFootprintUtilities.openFile(new Node(new ProjectsTabOperator().getProjectRootNode(j2eeproject_war), EPFootprintUtilities.WEB_PAGES + "|index.jsp"),"index.jsp", true);
        EPFootprintUtilities.insertToFile("index.jsp", 11, "Hello World", true);
        
        new EditAction().perform(new Node(new ProjectsTabOperator().getProjectRootNode(j2eeproject_war), "Configuration Files|sun-web.xml")); // NOI18N
        TopComponentOperator xmlEditor = new TopComponentOperator("sun-web.xml");

        EPFootprintUtilities.insertToFile("sun-web.xml", 10, "    <property name=\"javaEncoding\" value=\"UTF8\">", true);
        EPFootprintUtilities.insertToFile("sun-web.xml", 10, "      <description>Encoding for generated Java servlet.</description>", true);
        EPFootprintUtilities.insertToFile("sun-web.xml", 10, "    </property>", true);

       
        if (xmlEditor.isModified() )
            xmlEditor.save();
        
        Node node = new Node(new SourcePackagesNode(j2eeproject_app), new SourcePackagesNode(j2eeproject_app).getChildren()[0]+"|Main.java" );
        EPFootprintUtilities.openFile(node,"Main.java",true);
        EPFootprintUtilities.insertToFile("Main.java", 19, "System.out.println(\"Hello World\");",true);
        
        new SaveAllAction().performAPI();
        
        EPFootprintUtilities.buildProject(j2eeproject);
        //runProject(j2seproject,true);
        //debugProject(j2seproject,true);
        //testProject(j2seproject);
        //collapseProject(j2seproject);
        
        return null;
    }
    
    @Override
    public void close(){
        if (j2eeproject != null) {
            EPFootprintUtilities.deleteProject(j2eeproject);
            EPFootprintUtilities.deleteProject(j2eeproject_war);
            EPFootprintUtilities.deleteProject(j2eeproject_ejb);
            EPFootprintUtilities.deleteProject(j2eeproject_app,false);
        }
    }
    
//    public static void main(java.lang.String[] args) {
//        junit.textui.TestRunner.run(new J2EEProjectWorkflow("measureMemoryFooprint"));
//    }
    
}
