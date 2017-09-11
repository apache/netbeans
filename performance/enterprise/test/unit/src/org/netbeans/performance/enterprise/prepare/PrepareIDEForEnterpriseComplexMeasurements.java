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

package org.netbeans.performance.enterprise.prepare;

import java.io.File;
import java.util.ArrayList;

import javax.swing.tree.TreePath;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.MainWindowOperator;

import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.EditAction;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;

import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.Operator;

import junit.framework.Test;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.performance.utilities.CommonUtilities;
//import org.netbeans.modules.project.ui.test.ProjectSupport;
import org.netbeans.modules.performance.utilities.MeasureStartupTimeTestCase;


/**
 * Prepare user directory for complex measurements (startup time and memory consumption) of IDE with opened project and 10 files.
 * Open 10 java files and shut down ide.
 * Created user directory will be used to measure startup time and memory consumption of IDE with opened files.
 *
 * @author mmirilovic@netbeans.org, mrkam@netbeans.org
 */
public class PrepareIDEForEnterpriseComplexMeasurements extends JellyTestCase {
    
    /** Error output from the test. */
    protected static java.io.PrintStream err;
    
    /** Logging output from the test. */
    protected static java.io.PrintStream log;
    
    /** If true - at least one test failed */
    protected static boolean test_failed = false;
    
    /** Define testcase
     * @param testName name of the testcase
     */
    public PrepareIDEForEnterpriseComplexMeasurements(String testName) {
        super(testName);
    }
    
    /** Testsuite
     * @return testuite
     */
    public static Test suite() {
//        NbTestSuite suite = new NbTestSuite();
//        suite.addTest(new PrepareIDEForEnterpriseComplexMeasurements("closeAllDocuments"));
//        suite.addTest(new PrepareIDEForEnterpriseComplexMeasurements("closeMemoryToolbar"));
//        //FIXME: Remove manual addition of Application Server
//        suite.addTest(new PrepareIDEForEnterpriseComplexMeasurements("addApplicationServer"));
//        suite.addTest(new PrepareIDEForEnterpriseComplexMeasurements("openProjects"));
//        suite.addTest(new PrepareIDEForEnterpriseComplexMeasurements("openFiles"));
//        suite.addTest(new PrepareIDEForEnterpriseComplexMeasurements("saveStatus"));
//        return suite;
                
        NbTestSuite suite = new NbTestSuite("Prepare IDE For Enterprise Complex Measurements suite");

        suite.addTest(NbModuleSuite.create(
            NbModuleSuite.createConfiguration(PrepareIDEForEnterpriseComplexMeasurements.class)
            .addTest("closeAllDocuments")
            .addTest("closeMemoryToolbar")

            // FIXME: Remove this workaround of manual App Server addition
            .addTest("addApplicationServer")

            .addTest("openProjects")
            .addTest("openFiles")
            .addTest("saveStatus")
            .enableModules(".*")
            .clusters(".*")
        ));    
        
        return suite;
    }
    
    
    @Override
    public void setUp() {
//        err = System.out;
        err = getLog();
        log = getRef();
    }
    
    public void addApplicationServer() {
        // FIXME: Make a call of the following method
//        Utilities.addApplicationServer();
       String appServerPath = System.getProperty("com.sun.aas.installRoot");
        
        if (appServerPath == null) {
            throw new Error("Can't add application server. com.sun.aas.installRoot property is not set.");
        }

        String addServerMenuItem = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.actions.Bundle", "LBL_Add_Server_Instance"); // Add Server...
        String addServerInstanceDialogTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.wizard.Bundle", "LBL_ASIW_Title"); //"Add Server Instance"
        String glassFishV2ListItem = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.sun.ide.Bundle", "LBL_GlassFishV2");
        String nextButtonCaption = Bundle.getStringTrimmed("org.openide.Bundle", "CTL_NEXT");
        String finishButtonCaption = Bundle.getStringTrimmed("org.openide.Bundle", "CTL_FINISH");

        RuntimeTabOperator rto = RuntimeTabOperator.invoke();        
        JTreeOperator runtimeTree = rto.tree();
        
        long oldTimeout = runtimeTree.getTimeouts().getTimeout("JTreeOperator.WaitNextNodeTimeout");
        runtimeTree.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", 60000);
        
        TreePath path = runtimeTree.findPath("Servers");
        runtimeTree.selectPath(path);
        
        new JPopupMenuOperator(runtimeTree.callPopupOnPath(path)).pushMenuNoBlock(addServerMenuItem);
       
        NbDialogOperator addServerInstanceDialog = new NbDialogOperator(addServerInstanceDialogTitle);
        
        new JListOperator(addServerInstanceDialog, 1).selectItem(glassFishV2ListItem);
        
        new JButtonOperator(addServerInstanceDialog,nextButtonCaption).push();
        
        new JTextFieldOperator(addServerInstanceDialog).enterText(appServerPath);
        
        new JButtonOperator(addServerInstanceDialog,finishButtonCaption).push();
        
        runtimeTree.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", oldTimeout);
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
     * Open Travel Reservation projects
     */
    public void openProjects() {
        try {
            String projectsLocation = CommonUtilities.getProjectsDir() + "TravelReservationService" + File.separator;
//            ProjectSupport.openProject(projectsLocation + "ReservationPartnerServices");
            // TODO
//            ProjectSupport.waitScanFinished();
  //          ProjectSupport.openProject(projectsLocation + "TravelReservationService");
//            ProjectSupport.waitScanFinished();
    //        ProjectSupport.openProject(projectsLocation + "TravelReservationServiceApplication");
//            ProjectSupport.waitScanFinished();
            // TODO: Remove this workaround: closing all modals
            closeAllModal();
        }catch(Exception exc){
            test_failed = true;
            fail(exc);
        }
    }
    
    /**
     * Open 10 selected files from Travel Reservation projects
     */
    public void openFiles(){
        String OPEN = "Open";
        String EDIT = "Edit";
        
        try {
            String[][] nodes_path = {
                {"ReservationPartnerServices","Enterprise Beans","ReservationCallbackProviderMDB", "ReservationCallbackProviderBean.java", OPEN},
                {"ReservationPartnerServices","Source Packages","partnerservices|AirlineReservationPortType_Impl.java", "AirlineReservationPortType_Impl.java", OPEN},
                {"ReservationPartnerServices","Source Packages","partnerservices|HotelReservationPortType_Impl.java", "HotelReservationPortType_Impl.java", OPEN},
                {"ReservationPartnerServices","Source Packages","partnerservices|VehicleReservationPortType_Impl.java", "VehicleReservationPortType_Impl.java", OPEN},
                {"ReservationPartnerServices","Configuration Files","ejb-jar.xml", null, OPEN},
                {"ReservationPartnerServices","Configuration Files","sun-ejb-jar.xml", null, EDIT},
                {"TravelReservationService","Process Files","AirlineReservationService.wsdl", null, EDIT},
                {"TravelReservationService","Process Files","HotelReservationService.wsdl", null, EDIT},
                {"TravelReservationService","Process Files","OTA_TravelItinerary.xsd", null, OPEN},
                {"TravelReservationService","Process Files","TravelReservationService.bpel", null, OPEN}
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
                new EditAction().performAPI(editFileNodes.toArray(new Node[0]));
            }catch(Exception exc){
                err.println("---------------------------------------");
                err.println("issue 56825 : EXCEPTION catched during OpenAction");
                exc.printStackTrace(err);
                err.println("---------------------------------------");
                err.println("issue 56825 : Try it again");
                new OpenAction().performAPI(openFileNodes.toArray(new Node[0]));
                new EditAction().performAPI(editFileNodes.toArray(new Node[0]));
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
    }
    
    /**
     * Save status, if one of the above defined test failed, this method creates
     * file in predefined path and it means the complex tests will not run.
     */
    public void saveStatus() throws java.io.IOException{
        if(test_failed)
            MeasureStartupTimeTestCase.createStatusFile();
    }
}
