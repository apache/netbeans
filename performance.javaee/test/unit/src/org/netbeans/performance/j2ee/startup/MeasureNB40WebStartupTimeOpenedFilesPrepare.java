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

package org.netbeans.performance.j2ee.startup;

import java.io.PrintStream;

import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.EditAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;


/**
 * Prepare user directory for measurement of startup time of IDE with opened files.
 * Open 10 java files and shut down ide. 
 * Created user directory will be used to measure startup time of IDE with opened files. 
 *
 * @author Marian.Mirilovic@sun.com
 */
public class MeasureNB40WebStartupTimeOpenedFilesPrepare extends JellyTestCase {
    
    
    /** Error output from the test. */
    protected static PrintStream err;
    
    /** Logging output from the test. */
    protected static PrintStream log;

    public static final String suiteName="J2EE Startup suite";    
    
    
    /** Define testcase
     * @param testName name of the testcase
     */    
    public MeasureNB40WebStartupTimeOpenedFilesPrepare(String testName) {
        super(testName);
    }

    
    @Override
    public void setUp() {
//        err = System.out;
        err = getLog();
        log = getRef();
    }

    
    
    
    public void testOpenProjects() {
//        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/startup_nb40/TestWebApp");
  //      ProjectSupport.waitScanFinished();
        //waitForScan();
    }
    
    private void waitForScan() {
        // "Scanning Project Classpaths"
        String titleScanning = Bundle.getString("org.netbeans.modules.javacore.Bundle", "TXT_ApplyingPathsTitle");
        NbDialogOperator scanningDialogOper = new NbDialogOperator(titleScanning);
        // scanning can last for a long time => wait max. 5 minutes
        scanningDialogOper.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 300000);
        scanningDialogOper.waitClosed();
    }  
    
    /** 
     * Open 10 selected files from jEdit project. 
     */
    public void openFiles(){
        
        new org.netbeans.jemmy.EventTool().waitNoEvent(10000);
        
        String[] files_path = { 
            "Web Pages|index.jsp",
            "Source Packages|test|TestServlet.java"
        };
        
        Node root = new ProjectsTabOperator().getProjectRootNode("TestWebApp");
        Node[] openFileNodes = new Node[files_path.length];
        
        for(int i=0; i<files_path.length; i++) {
                
                openFileNodes[i] = new Node(root, files_path[i]);
                // open file one by one, opening all files at once causes never ending loop (java+mdr)
                //new OpenAction().performAPI(openFileNodes[i]);
        }
        
        // try to come back and open all files at-once, rises another problem with refactoring, if you do open file and next expand folder, 
        // it doesn't finish in the real-time -> hard to reproduced by hand
        new EditAction().performAPI(new Node(root,"Web Pages|WEB-INF|web.xml"));
        new org.netbeans.jemmy.EventTool().waitNoEvent(1000);
        new OpenAction().performAPI(openFileNodes);        
        
        new org.netbeans.jemmy.EventTool().waitNoEvent(60000);
        
    }
    
  
}
