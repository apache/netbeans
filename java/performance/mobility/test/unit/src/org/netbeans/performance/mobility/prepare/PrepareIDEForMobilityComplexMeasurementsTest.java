/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.performance.mobility.prepare;

import java.util.ArrayList;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
//import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.nodes.Node;
//import org.netbeans.jellytools.nodes.ProjectRootNode;

import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.Operator;

import junit.framework.Test;

import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.MeasureStartupTimeTestCase;



/**
 * Prepare user directory for complex measurements (startup time and memory consumption) of IDE with opened project and 10 files.
 * Open 10 java files and shut down ide.
 * Created user directory will be used to measure startup time and memory consumption of IDE with opened files.
 *
 * @author mmirilovic@netbeans.org, mrkam@netbeans.org
 */
public class PrepareIDEForMobilityComplexMeasurementsTest extends JellyTestCase {
    
    /** Error output from the test. */
    protected static java.io.PrintStream err;
    
    /** Logging output from the test. */
    protected static java.io.PrintStream log;
    
    /** If true - at least one test failed */
    protected static boolean test_failed = false;
    
    /** Define testcase
     * @param testName name of the testcase
     */
    public PrepareIDEForMobilityComplexMeasurementsTest(String testName) {
        super(testName);
    }
    
    /** Testsuite
     * @return testuite
     */
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PrepareIDEForMobilityComplexMeasurementsTest("closeAllDocuments"));
        suite.addTest(new PrepareIDEForMobilityComplexMeasurementsTest("closeMemoryToolbar"));
        suite.addTest(new PrepareIDEForMobilityComplexMeasurementsTest("openProjects"));
        suite.addTest(new PrepareIDEForMobilityComplexMeasurementsTest("openFiles"));
        suite.addTest(new PrepareIDEForMobilityComplexMeasurementsTest("saveStatus"));
        return suite;
    }
    
    
    @Override
    public void setUp() {
//        err = System.out;
        err = getLog();
        log = getRef();
    }
    
    /**
     * Close All Documents.
     */
    public void closeAllDocuments(){

	if ( new Action("Window|Close All Documents",null).isEnabled() )
	        try {
        	    new CloseAllDocumentsAction().perform();
	        }catch(Exception exc){
        	    test_failed = true;
	            fail(exc);
        	}
    }
    
    /**
     * Close Memory Toolbar.
     */
    public static void closeMemoryToolbar(){
        CommonUtilities.closeMemoryToolbar();
    }

    /**
     * Open Mobility projects
     */
    public void openProjects() {
        try {
            String projectsLocation = CommonUtilities.getProjectsDir();
         //   Object prj = ProjectSupport.openProject(projectsLocation + "MobileApplicationSwitchConfiguration");
         //   assertNotNull(prj);
         //   prj = ProjectSupport.openProject(projectsLocation + "MobileApplicationVisualMIDlet");
         //   assertNotNull(prj);
        }catch(Exception exc){
            test_failed = true;
            fail(exc);
        }
    }
    
    /**
     * Open 3 selected files from Mobile projects
     */
/*    public void openFiles(){
        String OPEN = "Open";
        String EDIT = "Edit";
        
        try {
            String[][] nodes_path = {
                {"MobileApplicationSwitchConfiguration","Source Packages|switchit","Midlet.java", null, OPEN},
                {"MobileApplicationVisualMIDlet","Source Packages|allComponents","VisualMIDletMIDP20.java", null, OPEN},
                {"MobileApplicationVisualMIDlet","Source Packages|simple","VisualMIDlet.java", null, OPEN}
            };
            
            ArrayList<Node> openFileNodes = new ArrayList<Node>();
            ArrayList<Node> editFileNodes = new ArrayList<Node>();
            Node node, fileNode;
            
            // create exactly (full match) and case sensitively comparing comparator
            Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, true);
            
            for(int i=0; i<nodes_path.length; i++) {
                // try to workarround problems with tooltip on Win2K & WinXP - issue 56825
                ProjectRootNode projectNode = new ProjectsTabOperator().getProjectRootNode(nodes_path[i][0]);
                projectNode.expand();
                
                node = new Node(projectNode,nodes_path[i][1]);
                node.setComparator(comparator);
                node.expand();
                
                fileNode = new Node(node,nodes_path[i][2]);
                //try to avoid issue 56825
                fileNode.select();
                
                if(nodes_path[i][4].equals(OPEN)) {
                    openFileNodes.add(fileNode);
                } else if(nodes_path[i][4].equals(EDIT)) {
                    editFileNodes.add(fileNode);
                } else
                    throw new Exception("Not supported operation [" + nodes_path[i][4] + "] for node: " + fileNode.getPath());
                
                // open file one by one, opening all files at once causes never ending loop (java+mdr)
                //new OpenAction().performAPI(openFileNodes[i]);
            }
            
            // try to come back and open all files at-once, rises another problem with refactoring, if you do open file and next expand folder,
            // it doesn't finish in the real-time -> hard to reproduced by hand
            try {
                new OpenAction().performAPI(openFileNodes.toArray(new Node[0]));
                //new EditAction().performAPI(editFileNodes.toArray(new Node[0]));
            }catch(Exception exc){
                err.println("---------------------------------------");
                err.println("issue 56825 : EXCEPTION catched during OpenAction");
                exc.printStackTrace(err);
                err.println("---------------------------------------");
                err.println("issue 56825 : Try it again");
                new OpenAction().performAPI(openFileNodes.toArray(new Node[0]));
                //new EditAction().performAPI(editFileNodes.toArray(new Node[0]));
                err.println("issue 56825 : Success");
            }
            
            
            // check whether files are opened in editor
            for(int i=0; i<nodes_path.length; i++) {
                if(nodes_path[i][3]!=null)
                    new TopComponentOperator(nodes_path[i][3]);
                else
                    new TopComponentOperator(nodes_path[i][2]);
            }
//        new org.netbeans.jemmy.EventTool().waitNoEvent(60000);
            
        }catch(Exception exc){
            test_failed = true;
            fail(exc);
        }
    }*/
    
    /**
     * Save status, if one of the above defined test failed, this method creates
     * file in predefined path and it means the complex tests will not run.
     */
    public void saveStatus() throws java.io.IOException{
        if(test_failed)
            MeasureStartupTimeTestCase.createStatusFile();
    }
}
