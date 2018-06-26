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
package org.netbeans.test.php.project;

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jellytools.OutputTabOperator;

import java.io.*;
import java.net.*;
import org.netbeans.jemmy.util.Dumper;

/**
 *
 * @author michaelnazarov@netbeans.org
 */
public class testBrowserAndIDE extends project {

    static final String TEST_PHP_NAME = "PhpProject_project_0002";
    static final String TEST_URL = "http://netbeans.org";
    static final int TEST_CB_PORT = 80;
    
    public testBrowserAndIDE(String arg0) {
        super(arg0);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(testBrowserAndIDE.class).addTest(
                "CreateSimpleApplication",
                "SetCustomBrowserPath",
                "ExecuteInBrowser",
                "ExecuteInConsole" // Execute different ways
                ).enableModules(".*").clusters(".*") //.gui( true )
                );
    }

    // Custom name
    public void CreateSimpleApplication() {
        startTest();

        String sProjectName = CreatePHPApplicationInternal(8080);

        // Check created in tree
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(
                sProjectName + "|Source Files|" + "index.php");
        prn.select();

        // Check index.php in editor
        new EditorOperator("index.php");

        endTest();
    }
    
    private ServerSocket sBackServer = null;

    public void SetCustomBrowserPath() {
        startTest();

        // Test execution
        new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenuNoBlock("Tools|Options");

        JDialogOperator jdOptions = new JDialogOperator("Options");
        JButtonOperator jbEdit = new JButtonOperator(jdOptions, "Edit...");
        jbEdit.pushNoBlock();
        JDialogOperator jdBrowsers = new JDialogOperator("Web Browsers");
        JButtonOperator jbAdd = new JButtonOperator(jdBrowsers, "Add...");
        jbAdd.push();
        JTextFieldOperator jtPath = new JTextFieldOperator(jdBrowsers, 1);
        jtPath.setText("java");

        //try{ Dumper.dumpAll( "c:\\aaa.zzz" ); } catch( IOException ex ) { }

        JTextAreaOperator jtArgs = new JTextAreaOperator(jdBrowsers, 0);

        //try { System.getProperties( ).store( new FileOutputStream( "c:\\prop.txt" ), "COMMENT" ); } catch( IOException ex ) { }

        // Establish server and get port number
        try {
            sBackServer = new ServerSocket(0);

            jtArgs.setText(
                    "-cp \"" + System.getProperty("java.class.path") + "\" org.netbeans.test.php.project.project_0002 {URL} " + sBackServer.getLocalPort());
            Sleep(5000);
            JButtonOperator jbOk = new JButtonOperator(jdBrowsers, "OK");
            jbOk.push();
            jdBrowsers.waitClosed();
            Sleep(5000);
            jbOk = new JButtonOperator(jdOptions, "OK");
            jbOk.push();
            jdOptions.waitClosed();
        } catch (IOException ex) {
            fail("Exception: " + ex.getMessage());
        }

        endTest();
    }

    public void ExecuteInBrowser() {
        SetCustomBrowserPath();

        startTest();

        // Test execution
        Sleep(5000);
        new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenuNoBlock("Run|Run");

        try {

            sBackServer.setSoTimeout(30000); // 30 second wait should be enough
            Socket sBackClient = sBackServer.accept();
            InputStream is = sBackClient.getInputStream();
            String sContent = "";
            byte[] b = new byte[1024];
            int iReaden;
            while (-1 != (iReaden = is.read(b))) {
                sContent = sContent + new String(b, 0, iReaden);
            }
            is.close();
            sBackClient.close();
            sBackServer.close();

            sContent = sContent.replaceAll("[ \t\r\n]", "");
            System.out.println(">>>" + sContent + "<<<");

            // Check result
            // ToDo

        } catch (IOException ex) {
            fail("Exception: " + ex.getMessage());
        }

        endTest();
    }

    public void ExecuteInConsole() {
        startTest();

        // Set new execution type
        new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenuNoBlock("File|Project Properties");
        JDialogOperator jdProperties = new JDialogOperator("Project Properties - ");
        JTreeOperator jtSections = new JTreeOperator(jdProperties, 0);
        jtSections.selectPath(jtSections.findPath("Run Configuration"));
        Sleep(500);

        try {
            Dumper.dumpAll("c:\\dump.txt");
        } catch (IOException ex) {
        }

        JComboBoxOperator jcType = new JComboBoxOperator(jdProperties, 0);
        jcType.selectItem("Script (run in command line)");
        JButtonOperator jbOk = new JButtonOperator(jdProperties, "OK");
        jbOk.push();
        jdProperties.waitClosed();

        // Test execution
        Sleep(5000);
        new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenuNoBlock("Run|Run");
        Sleep(5000);

        // Get output
        OutputTabOperator oto = new OutputTabOperator(" - index.php");
        String sContent = oto.getText().replaceAll("[ \t\r\n]", "");
        System.out.println(">>>" + sContent + "<<<");

        endTest();
    }

    public static void main(String[] args) throws IOException {
        // Out own web browser
        // First parameter  :  URL
        // Second parameter : callback port

        // Connect to server
        // Get data
        String sContent = "";
        boolean bRedo = true;
        int iRecount = 0;
        while (bRedo) {
            try {
                URL u = null; 
                try {
                    u = new URL(args[0]);
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                    u = new URL(TEST_URL);
                }
                HttpURLConnection http = (HttpURLConnection) u.openConnection();
                http.setConnectTimeout(30000);
                http.setReadTimeout(60000);
                http.connect();
                InputStream is = http.getInputStream();
                byte[] b = new byte[1024];
                int iReaden;
                sContent = "";
                while (-1 != (iReaden = is.read(b))) {
                    sContent = sContent + new String(b, 0, iReaden);
                }
                http.disconnect();
                is.close();

                bRedo = false;
            } catch (java.net.MalformedURLException ex) {
                System.out.println("Error: " + ex.getMessage() + "\n");
                return;
            } catch (IOException ex) {
                System.out.println("Error: " + ex.getMessage() + "\n");
            }
        }
        // Send result back to test
//        Socket sSocketBack = new Socket("127.0.0.1", Integer.parseInt(args[ 1]));
        Socket sSocketBack = new Socket("127.0.0.1", TEST_CB_PORT);
        OutputStream sStreamBack = sSocketBack.getOutputStream();
        sStreamBack.write(sContent.getBytes());
        sStreamBack.flush();
        sStreamBack.close();

        return;
    }
}
