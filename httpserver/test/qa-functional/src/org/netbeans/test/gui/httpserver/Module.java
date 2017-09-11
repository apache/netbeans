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

package org.netbeans.test.gui.httpserver;

import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.httpserver.*;

import java.awt.Robot;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

import org.openide.awt.*;

import org.netbeans.jellytools.*;
import org.netbeans.jellytools.nodes.*;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.properties.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.junit.AssertionFailedErrorException;
import org.netbeans.junit.NbTest;

public class Module extends JellyTestCase { 

	private String workDir=null;
	private String value=null;
	private String old_value=null;
	private ExplorerOperator explorer=null;
	private static boolean mwm=true;
	private NbDialogOperator dop=null;
	private OptionsOperator optionsOper=null;
	private PropertySheetTabOperator psto=null;
	private String delim="|";	// NOI18N
	private String failMessage="test failed";	// NOI18N
        
        private static String originalPort = "8082";
        
        private HttpServerSettings serverSettings = new HttpServerSettings();

    public Module(String testName) { 
        super(testName); 
    } 
    
    public static NbTestSuite suite() {
    	NbTestSuite suite = new NbTestSuite("test_temp");
        suite.addTest(new Module("test_1_1"));
        /*
        suite.addTest(new Module("test_1_5"));
        suite.addTest(new Module("test_2_1"));
        suite.addTest(new Module("test_2_2"));
        suite.addTest(new Module("test_3_1"));
        suite.addTest(new Module("test_3_2"));
        */
        suite.addTest(new Module("test_4_1_1"));
        suite.addTest(new Module("test_4_2_1"));
        suite.addTest(new Module("test_4_2_2"));
        suite.addTest(new Module("test_4_2_4")); 
        suite.addTest(new Module("test_4_2_5")); 
        suite.addTest(new Module("test_4_2_6")); 
        suite.addTest(new Module("test_4_2_7"));
        
        
        suite.addTest(new Module("test_4_3_1"));
        suite.addTest(new Module("test_4_3_2"));        
        /*
        //suite.addTest(new Module("test_4_5_1"));
        //suite.addTest(new Module("test_4_5_2"));
        //suite.addTest(new Module("test_4_5_3"));
        suite.addTest(new Module("test_4_5_4"));
        //suite.addTest(new Module("test_4_6_1"));
        //suite.addTest(new Module("test_4_6_2"));
        //suite.addTest(new Module("test_4_6_3"));
        //suite.addTest(new Module("test_4_6_4"));
        suite.addTest(new Module("test_4_6_5"));
        //suite.addTest(new Module("test_4_7_1"));
        //suite.addTest(new Module("test_4_7_2"));
        //suite.addTest(new Module("test_4_7_3"));
        //suite.addTest(new Module("test_4_7_4"));
        suite.addTest(new Module("test_4_7_5"));
         */
        
        suite.addTest(new Module("test_4_8_01"));
        suite.addTest(new Module("test_4_8_02"));
        suite.addTest(new Module("test_4_8_03"));
        suite.addTest(new Module("test_4_8_04")); 
        suite.addTest(new Module("test_4_8_05"));
        suite.addTest(new Module("test_4_8_06"));
        suite.addTest(new Module("test_4_8_07"));
        suite.addTest(new Module("test_4_8_08"));
        
        /*
        suite.addTest(new Module("test_4_8_09"));
        suite.addTest(new Module("test_4_8_10"));
        suite.addTest(new Module("test_4_8_11"));
        suite.addTest(new Module("test_4_8_12"));
        suite.addTest(new Module("test_4_8_13"));
        suite.addTest(new Module("test_4_8_14"));
        suite.addTest(new Module("test_4_8_15"));
        suite.addTest(new Module("test_4_8_16"));
        suite.addTest(new Module("test_4_8_17"));
        suite.addTest(new Module("test_4_8_18"));
        suite.addTest(new Module("test_4_8_19"));
        suite.addTest(new Module("test_4_8_20"));
        suite.addTest(new Module("test_4_8_21"));
        suite.addTest(new Module("test_4_8_22"));
        suite.addTest(new Module("test_4_8_23"));
        suite.addTest(new Module("test_4_8_24"));
        */
        
        suite.addTest(new Module("test_4_9_1"));
        suite.addTest(new Module("test_4_9_2"));
        suite.addTest(new Module("test_4_9_3"));
        
        return suite;
    }

    /** Use for execution inside IDE */ 
    public static void main(java.lang.String[] args) { 
        //junit.textui.TestRunner.run(new NbTestSuite(Module.class)); 
        junit.textui.TestRunner.run(suite());
        
    } 

    public void setUp() { 
        System.out.println("#"+getName());
    }

    private void waitFor(int ms) {
//  On W2k k=1, on Solaris/Linux it's better to set k=2-3
	int k=1;
        new EventTool().waitNoEvent(ms*k);
    }

    private AssertionFailedErrorException checkDialog(String name) {

	try {
		dop=new NbDialogOperator(name);
	} catch(Exception ex) {
		return new AssertionFailedErrorException("No '"+name+"' dialog appears",ex);
	}
	dop.close();
	return null;
    }

    private void switchToHTTPServerNode() {

        RuntimeTabOperator runtime = RuntimeTabOperator.invoke();

        Node node=new Node(runtime.tree(),"HTTP Server");
	new PropertiesAction().performPopup(node);
    }

    private void waitServerRunning(boolean running) {
        int i;
        for (i=1;i<=10;i++) {
            if (serverSettings.isRunning() == running)
                break;
            try { Thread.currentThread().sleep(i*300); }
            catch (InterruptedException e) {}
        }
        if (i == 11) 
            fail("Timeout expired when waiting for server to "+(running?"start":"stop")+" running.");
    }
    
    private void startHTTPServer() {

	RuntimeTabOperator runtime = RuntimeTabOperator.invoke();
        Node node=new Node(runtime.tree(),"HTTP Server");
        
        if (!serverSettings.isRunning()) {
            new ActionNoBlock(null,"Start HTTP Server").performPopup(node);
            waitServerRunning(true);
            waitFor(500);
        }
    }
    
    private void stopHTTPServer() {
        
        RuntimeTabOperator runtime = RuntimeTabOperator.invoke();
        Node node=new Node(runtime.tree(),"HTTP Server");

        if (serverSettings.isRunning()) {
            new ActionNoBlock(null,"Stop HTTP Server").performPopup(node);
            waitServerRunning(false);
            waitFor(500);
        }
    }
    
    private void restartHTTPServer() {
        
        RuntimeTabOperator runtime = RuntimeTabOperator.invoke();
        Node node=new Node(runtime.tree(),"HTTP Server");

        if (serverSettings.isRunning()) {
            new ActionNoBlock(null,"Stop HTTP Server").performPopup(node);
            waitServerRunning(false);
            waitFor(500);
        }
        new ActionNoBlock(null,"Start HTTP Server").performPopup(node);
        waitServerRunning(true);
        waitFor(500);
    }

    private void checkResult(String path, int index, String output, boolean expectedFail) {
        URL url = null;
        try {
            if (path.startsWith("http://"))
                url = new URL(path);
            else
                url = new URL("http","localhost",serverSettings.getPort(),path);
            URLConnection connection = url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = null;
            for (int i=0; i<index; i++) {
                line = reader.readLine();
                if (line == null) break;
            }
            reader.close();
            if (expectedFail) {
                fail("Expected to unable to read from '" + url.toString() + "', but it's accessible.");
            } else {
                if (line == null) 
                    fail("No line "+index+" found when reading from '" + url.toString() + "'. Expected to read text including with '"+output+"'.");
                if (line.indexOf(output)<0) 
                    fail("Different text read in line "+index+" from '" + url.toString() + "'. Expected to read text including with '"+output+"'.\nLine "+index+": "+line);
            }
        } catch (IOException ioe) {
            if (!expectedFail)
                throw new AssertionFailedErrorException("IOException during reading from '" + (url==null?"null":url.toString()) + "'.",ioe);
        }
    }
      
    /*
    private boolean checkResult(String url, String output) {
     
	HtmlBrowser browser = new HtmlBrowser ();
	browser.setURL(url);
	browser.requestFocus();

	JFrame jw = new JFrame();
	jw.getContentPane().add(browser);
        jw.setSize(200,200);
	jw.show ();

        try { Thread.currentThread().sleep(15000); }
        catch (InterruptedException e) {}
        
	JFrameOperator nfo=new JFrameOperator(jw); 
	waitFor(5000);

	String result=new JTextComponentOperator(nfo, 0).getText();

	if (-1==result.indexOf(output)) {
		nfo.close();
		return false;
	} else {
		nfo.close();
		return true;
	}
    }
    */

// Internal HTTP Server Test Specification:  Test suite: 1. Browsing of User Repository


// 1.1
    public void test_1_1() {

        /*
	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");
        
	TextFieldProperty tf=new TextFieldProperty(psto,"Base Filesystems URL");
	tf.setValue("repository");	// NOI18
	pw.close();
        */
         
	startHTTPServer();

 	checkResult("/",3,"<title>Directory Listing for:/</title>",false);
    }
  
// 1.5    
    public void test_1_5() {
        
        switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");
        
        TextFieldProperty tf=new TextFieldProperty(psto,"Base Filesystems URL");
        tf.setValue("repository");	// NOI18N
        tf=new TextFieldProperty(psto,"Port");
	originalPort = tf.getValue();
        pw.close();
        
        startHTTPServer();
        checkResult("/repository/org/netbeans/test/gui/httpserver/test.txt",1,"This is test file",false);
    }

// Internal HTTP Server Test Specification:  Test suite: 2. Accessing Items on IDE Classpath

// 2.1
    public void test_2_1() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Class Path URL");
	tf.setValue("classpath");	// NOI18N
	pw.close();

	startHTTPServer();

	checkResult("/classpath/",1,"<HTML><HEAD><TITLE>Filesystems</TITLE></HEAD>",false);
    }

// 2.2
    public void test_2_2() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Class Path URL");
	tf.setValue("classpath");	// NOI18N
	pw.close();

	startHTTPServer();

	checkResult("/classpath/org/netbeans/core/resources/templatesFileSystems.html",
                    15,"Select the type of filesystem that you want to mount.",false);
    }

// Internal HTTP Server Test Specification:  Test suite: 3. Accessing Javadoc

// 3.1
    public void test_3_1() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Javadoc URL");
	tf.setValue("javadoc");	// NOI18N
	pw.close();

	startHTTPServer();

	checkResult("/javadoc/",2,"List of Javadoc mounts",false);
    }

// 3.2
    public void test_3_2() {
        
        switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");
        
        TextFieldProperty tf=new TextFieldProperty(psto,"Base Javadoc URL");
        tf.setValue("javadoc");	// NOI18N
        pw.close();
        
        startHTTPServer();
        
        checkResult("/resource/Mount%2FJavadoc%2Forg-netbeans-modules-xml-tools-resources-xml_apis.xml/javax/xml/parsers/SAXParser.html",
        7,"JAXP 1.1, DOM2, SAX2, SAX2-ext 1.0: Class  SAXParser",false);
    }

// Internal HTTP Server Test Specification:  Test suite: 4. Module Properties
 
// 4.1 Hosts with Granted Access

// 4.1.1
    public void test_4_1_1() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue(originalPort);	// NOI18N
	tf=new TextFieldProperty(psto,"Hosts with Granted Access");
	tf.setValue("Selected Hosts: ");

        psto = pw.getPropertySheetTabOperator("Expert");

	//tf=new TextFieldProperty(psto,"Base Filesystems URL");
	//tf.setValue("repository");	// NOI18N
	pw.close();

	startHTTPServer();

	checkResult("/",3,"<title>Directory Listing for:/</title>",false);
    }

// 4.2 Port

// 4.2.1
    public void test_4_2_1() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue("16384");	// NOI18N

        psto = pw.getPropertySheetTabOperator("Expert");

	//tf=new TextFieldProperty(psto,"Base Filesystems URL");
	//tf.setValue("repository");	// NOI18N
	pw.close();

	startHTTPServer();

	checkResult("http://localhost:16384/",3,"<title>Directory Listing for:/</title>",false);
    }

// 4.2.2
    public void test_4_2_2() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue("16384");	// NOI18N

        psto = pw.getPropertySheetTabOperator("Expert");

	//tf=new TextFieldProperty(psto,"Base Filesystems URL");
	//tf.setValue("repository");	// NOI18N
	pw.close();

	startHTTPServer();

	checkResult("http://localhost:"+originalPort+"/",3,"<title>Directory Listing for:/</title>",true);
    }
 
// 4.2.4
    public void test_4_2_4() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	final TextFieldProperty tf=new TextFieldProperty(psto,"Port");

	value=tf.getValue();
	new Thread() {
            public void run() { tf.setValue("-9999"); }
        }.start();
        
        AssertionFailedErrorException e = checkDialog("Error");
        tf.setValue(value);
        pw.close();
	if (e != null) 
            throw e;
    }

// 4.2.5
    public void test_4_2_5() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	final TextFieldProperty tf=new TextFieldProperty(psto,"Port");

	value=tf.getValue();
	new Thread() {
            public void run() {tf.setValue("0"); }
        }.start();

        AssertionFailedErrorException e = checkDialog("Error");
        tf.setValue(value);
        pw.close();
        if (e != null)
            throw e;
    }

// 4.2.6
    public void test_4_2_6() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	final TextFieldProperty tf=new TextFieldProperty(psto,"Port");

	value=tf.getValue();
	new Thread() {
            public void run() {tf.setValue("65536"); }
        }.start();

        AssertionFailedErrorException e = checkDialog("Error");
        tf.setValue(value);
        pw.close();
        if (e != null)
            throw e;
    }
    
// 4.2.7
    public void test_4_2_7() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue("65535");	// NOI18N

        psto = pw.getPropertySheetTabOperator("Expert");

	//tf=new TextFieldProperty(psto,"Base Filesystems URL");
	//tf.setValue("repository");	// NOI18N
	pw.close();

	startHTTPServer();

	checkResult("http://localhost:65535/",3,"<title>Directory Listing for:/</title>",false);
    }


// 4.3 Running

// 4.3.1
    public void test_4_3_1() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue("16384");	// NOI18N

        psto = pw.getPropertySheetTabOperator("Expert");

	//tf=new TextFieldProperty(psto,"Base Filesystems URL");
	//tf.setValue("repository");	// NOI18N
	pw.close();

	stopHTTPServer();

	checkResult("http://localhost:65535/",3,"<title>Directory Listing for:/</title>",true);
    }

// 4.3.2
    public void test_4_3_2() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue("16384");	// NOI18N
	
        psto = pw.getPropertySheetTabOperator("Expert");

	//tf=new TextFieldProperty(psto,"Base Filesystems URL");
	//tf.setValue("repository");	// NOI18N
	pw.close();

	startHTTPServer();

	checkResult("http://localhost:16384/",3,"<title>Directory Listing for:/</title>",false);
    }

// 4.5 Base Filesystems URL 

// 4.5.1
    public void test_4_5_1() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Filesystems URL");
	tf.setValue("");	// NOI18N

	String value=tf.getValue();
	tf.setValue("/repository/");	// NOI18N
        
        AssertionFailedErrorException e = checkDialog("Information");
        pw.close();

        if (e != null) 
            throw e;

        if (!value.equals("/")) 
		fail("Invalid 'Base Filesystems URL' field value");
    }
    
// 4.5.2
    public void test_4_5_2() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue(originalPort);	// NOI18N

        psto = pw.getPropertySheetTabOperator("Expert");

	tf=new TextFieldProperty(psto,"Base Filesystems URL");
	tf.setValue("");	// NOI18N
        AssertionFailedErrorException e = checkDialog("Information");
        pw.close();
        if (e != null)
            throw e;
	
	startHTTPServer();
        checkResult("http://localhost:"+originalPort+"/repository/",1,"<HTML><HEAD><TITLE>Filesystems</TITLE></HEAD>",true);
    }

// 4.5.3
    public void test_4_5_3() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue(originalPort);	// NOI18N

        psto = pw.getPropertySheetTabOperator("Expert");

	tf=new TextFieldProperty(psto,"Base Filesystems URL");
	tf.setValue("");	// NOI18N
	pw.close();

	startHTTPServer();

        checkResult("http://localhost:"+originalPort+"/",1,"<HTML><HEAD><TITLE>Filesystems</TITLE></HEAD>",false);
    }
    
// 4.5.4
    public void test_4_5_4() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue(originalPort);	// NOI18N

        psto = pw.getPropertySheetTabOperator("Expert");

	tf=new TextFieldProperty(psto,"Base Filesystems URL");
	tf.setValue("newrepository");	// NOI18N
	pw.close();

	startHTTPServer();

        checkResult("http://localhost:"+originalPort+"/newrepository/",1,"<HTML><HEAD><TITLE>Filesystems</TITLE></HEAD>",false);
    }

    
// 4.6 Base Class Path URL 

// 4.6.1
    public void test_4_6_1() {
	
	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf1=new TextFieldProperty(psto,"Base Filesystems URL");
	TextFieldProperty tf2=new TextFieldProperty(psto,"Base Class Path URL");

	tf1.setValue("foo1");	// NOI18N
	tf2.setValue("foo1");	// NOI18N

        AssertionFailedErrorException e = checkDialog("Information");
        
        tf1.setValue("repository");	// NOI18N
        tf2.setValue("classpath");	// NOI18N
        pw.close();
        
        if (e != null)
            throw e;
    }

// 4.6.2
    public void test_4_6_2() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Class Path URL");
	tf.setValue("");	// NOI18N

        AssertionFailedErrorException e = checkDialog("Information");
        String value=tf.getValue();
	tf.setValue("/classpath/");
        pw.close();
        
        if (e != null)
            throw e;        
        
	if (!value.equals("/")) {	// NOI18N
		pw.close();
		fail("Invalid 'Base Class Path URL' field value");
	}

	
    }

// 4.6.3
    public void test_4_6_3() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue(originalPort);	// NOI18N

        psto = pw.getPropertySheetTabOperator("Expert");

	tf=new TextFieldProperty(psto,"Base Class Path URL");
	tf.setValue("");	// NOI18N
        
        AssertionFailedErrorException e = checkDialog("Information");
        
	pw.close();
        
        if (e != null)
            throw e; 
        
	startHTTPServer();

        checkResult("http://localhost:"+originalPort+"/classpath/org/netbeans/core/resources/templatesFileSystems.html",
                    15,"Select the type of filesystem that you want to mount.",true);
    }

// 4.6.4
    public void test_4_6_4() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue(originalPort);	// NOI18N

        psto = pw.getPropertySheetTabOperator("Expert");

	tf=new TextFieldProperty(psto,"Base Class Path URL");
	tf.setValue("");	// NOI18N
	pw.close();

	startHTTPServer();

        checkResult("http://localhost:"+originalPort+"/org/netbeans/core/resources/templatesFileSystems.html",
                    15,"Select the type of filesystem that you want to mount.",false);
    }
    
// 4.6.5
    public void test_4_6_5() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue(originalPort);	// NOI18N

        psto = pw.getPropertySheetTabOperator("Expert");

	tf=new TextFieldProperty(psto,"Base Class Path URL");
	tf.setValue("newclasspath");	// NOI18N
	pw.close();

	startHTTPServer();

        checkResult("http://localhost:"+originalPort+"/newclasspath/org/netbeans/core/resources/templatesFileSystems.html",
        15,"Select the type of filesystem that you want to mount.",false);
  
    }

// 4.7 Base Javadoc URL 
   
// 4.7.1
    public void test_4_7_1() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf1=new TextFieldProperty(psto,"Base Filesystems URL");
	TextFieldProperty tf2=new TextFieldProperty(psto,"Base Javadoc URL");

	tf1.setValue("foo2");	// NOI18N
	tf2.setValue("foo2");	// NOI18N

        AssertionFailedErrorException e = checkDialog("Information");
        
        tf1.setValue("repository");	// NOI18N
        tf2.setValue("javadoc");	// NOI18N
        pw.close();

        if (e != null) {
		throw e;
	}
    }

// 4.7.2
    public void test_4_7_2() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Javadoc URL");
	tf.setValue("");	// NOI18N

	String value=tf.getValue();
        AssertionFailedErrorException e = checkDialog("Information");

        tf.setValue("/javadoc/");	// NOI18N
        pw.close();

        if (e != null) {
                throw e;
	}
 
	if (!value.equals("/")) {	// NOI18N
		fail("Invalid 'Base Javadoc URL' field value");
	}
    }

// 4.7.3
    public void test_4_7_3() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue(originalPort);	// NOI18N

        psto = pw.getPropertySheetTabOperator("Expert");

	tf=new TextFieldProperty(psto,"Base Javadoc URL");
	tf.setValue("");	// NOI18N
        
        AssertionFailedErrorException e = checkDialog("Information");
 
	pw.close();
 
        if (e != null) {
                throw e;
        }
 
	startHTTPServer();

        checkResult("http://localhost:"+originalPort+"/javadoc/",2,"List of Javadoc mounts",true);
    }

// 4.7.4
    public void test_4_7_4() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue(originalPort);	// NOI18N

        psto = pw.getPropertySheetTabOperator("Expert");

	tf=new TextFieldProperty(psto,"Base Javadoc URL");
	tf.setValue("");	// NOI18N
	pw.close();

	startHTTPServer();

        checkResult("http://localhost:"+originalPort+"/",2,"List of Javadoc mounts",false);
    }
    
// 4.7.5
    public void test_4_7_5() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Port");
	tf.setValue(originalPort);	// NOI18N

        psto = pw.getPropertySheetTabOperator("Expert");

	tf=new TextFieldProperty(psto,"Base Javadoc URL");
	tf.setValue("newjavadoc");	// NOI18N
	pw.close();

	startHTTPServer();

        checkResult("http://localhost:"+originalPort+"/newjavadoc/",2,"List of Javadoc mounts",false);
    }


// 4.8 General Behavior 

// 4.8.1
    public void test_4_8_01() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Hosts with Granted Access");
	tf.openEditor();

        AssertionFailedErrorException e = checkDialog("Hosts with Granted Access");
        pw.close();
        if (e != null)
            throw e;
    }

// 4.8.2
    public void test_4_8_02() {
	
	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Hosts with Granted Access");
	tf.setValue("Selected Hosts: ");
	tf.openEditor();

	try {
		dop=new NbDialogOperator("Hosts with Granted Access");
	} catch(Exception ex) {
		pw.close();
		throw new AssertionFailedErrorException("No 'Hosts with Granted Access' dialog appears",ex);
	}

	JRadioButtonOperator rb=new JRadioButtonOperator(dop,"Any Host");
	rb.doClick();
	JButtonOperator cancel=new JButtonOperator(dop,org.netbeans.jellytools.Bundle.getString("org.netbeans.core.Bundle", "CANCEL_OPTION_CAPTION"));
	cancel.doClick();

	value=tf.getValue();
	if (!value.equals("Selected Hosts: ")) {
		pw.close();
		fail("Cancel doesn't work.");
	}

	pw.close();
    }

// 4.8.3
    public void test_4_8_03() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Hosts with Granted Access");
	tf.setValue("Selected Hosts: ");
	tf.openEditor();

	try {
		dop=new NbDialogOperator("Hosts with Granted Access");
	} catch(Exception ex) {
		pw.close();
		throw new AssertionFailedErrorException("No 'Hosts with Granted Access' dialog appears",ex);
	}

	JRadioButtonOperator rb=new JRadioButtonOperator(dop,"Any Host");
	rb.doClick();
	JButtonOperator ok=new JButtonOperator(dop,org.netbeans.jellytools.Bundle.getString("org.netbeans.core.Bundle", "OK_OPTION_CAPTION"));
	ok.doClick();

	value=tf.getValue();
	if (!value.equals("Any Host")){
		pw.close();
		fail("'Any Host' isn't set.");
	}

	pw.close();
    }

// 4.8.4
    public void test_4_8_04() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Hosts with Granted Access");
        tf.setValue("foo");	// NOI18N

        value=tf.getValue();
	pw.close();
	if (!value.equals("Selected Hosts: foo")) {
		pw.close();
		fail("'Selected Hosts: foo' isn't set.");
	}
    }

// 4.8.5
    public void test_4_8_05() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Hosts with Granted Access");
	tf.setValue("Any Host");
	tf.openEditor();

	try {
		dop=new NbDialogOperator("Hosts with Granted Access");
	} catch(Exception ex) {
		pw.close();
		throw new AssertionFailedErrorException("No 'Hosts with Granted Access' dialog appears",ex);
	}

	JRadioButtonOperator rb=new JRadioButtonOperator(dop,"Selected Hosts");
	rb.doClick();
	JButtonOperator ok=new JButtonOperator(dop,org.netbeans.jellytools.Bundle.getString("org.netbeans.core.Bundle", "OK_OPTION_CAPTION"));
	ok.doClick();

	value=tf.getValue();
	pw.close();
	if (!value.equals("Selected Hosts: ")) {
		pw.close();
		fail("'Selected Hosts: ' isn't set.");
	}

    }

// 4.8.6
    public void test_4_8_06() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	Property tf=new Property(psto,"Hosts with Granted Access");
	old_value=tf.getValue();

        
	//JTextFieldOperator to=tf.textField();
        //to.typeText("Selected Hosts: localhost");
        tf.setValue("Selected Hosts: localhost");

        pw.pushKey(java.awt.event.KeyEvent.VK_TAB);
        pw.pushKey(java.awt.event.KeyEvent.VK_TAB);

        /*
	try {
		Robot rb=new java.awt.Robot();
		rb.keyPress(java.awt.event.KeyEvent.VK_TAB);
		rb.keyPress(java.awt.event.KeyEvent.VK_TAB);
	} catch (Exception AWTException) {
		System.out.println("AWTException in test_4_8_6");	// NOI18N
	}
        */

	value=tf.getValue();
	if (!value.equals("Selected Hosts: localhost")) {
		pw.close();
		fail("Invalid 'Hosts with Granted Access' field value ("+value+")");
	}

	tf.setValue(old_value);
	pw.close();
    }

// 4.8.7
    public void test_4_8_07() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Hosts with Granted Access");
	old_value=tf.getValue();

	//JTextFieldOperator to=tf.textField();
        //to.typeText("Selected Hosts: localhost, boo");
        tf.setValue("Selected Hosts: localhost, boo");

        pw.pushKey(java.awt.event.KeyEvent.VK_TAB);
        pw.pushKey(java.awt.event.KeyEvent.VK_TAB);
        /*
	try {
		Robot rb=new java.awt.Robot();
		rb.keyPress(java.awt.event.KeyEvent.VK_TAB);
		rb.keyPress(java.awt.event.KeyEvent.VK_TAB);
	} catch (Exception AWTException) {
		System.out.println("AWTException in test_4_8_7");	// NOI18N
	}
        */

	value=tf.getValue();
	if (!value.equals("Selected Hosts: localhost, boo")) {
		pw.close();
		fail("Invalid 'Hosts with Granted Access' field value ("+value+")");
	}

	pw.close();
    }

// 4.8.8
    public void test_4_8_08() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Hosts with Granted Access");
        tf.setValue("Selected Hosts: localhost, boo");
	tf.openEditor();

	try {
		dop=new NbDialogOperator("Hosts with Granted Access");
	} catch(Exception ex) {
		//dop.close();
		pw.close();
		throw new AssertionFailedErrorException("No 'Hosts with Granted Access' dialog appears",ex);
	}

	JTextAreaOperator jt=new JTextAreaOperator(dop,0);
	value=jt.getText();
	if (!value.equals("localhost, boo")) {
		dop.close();
        	pw.close();
		fail("Invalid 'Grant Access to:' textarea value ("+value+")");
	}

	dop.close();
	pw.close();
    }

// 4.8.9
    public void test_4_8_09() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Class Path URL");
	tf.openEditor();

        
        AssertionFailedErrorException e = checkDialog("Base Class Path URL");
        // What is in value ????
        //tf.setValue(value);        
        pw.close();
        if (e != null)
            throw e;       
        
    }

// 4.8.10
    public void test_4_8_10() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Class Path URL");
	tf.openEditor();
	
	try {
		dop=new NbDialogOperator("Base Class Path URL");
	} catch(Exception ex) {
                //dop.close();
		pw.close();
		throw new AssertionFailedErrorException("No 'Base Class Path URL' dialog appears",ex);
	}

	JTextAreaOperator jt=new JTextAreaOperator(dop,0);
	value=jt.getText();
	jt.setText("/qqqqqqq/");	// NOI18N
	JButtonOperator cancel=new JButtonOperator(dop,org.netbeans.jellytools.Bundle.getString("org.netbeans.core.Bundle", "CANCEL_OPTION_CAPTION"));
	cancel.doClick();

	if (!value.equals(tf.getValue())) {
		pw.close();
		fail("Cancel in 'Base Class Path URL' dialog doesn't work");
	}

	pw.close();
    }

// 4.8.11
    public void test_4_8_11() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Class Path URL");
	old_value=tf.getValue();

	//JTextFieldOperator to=tf.textField();
        //to.typeText("/testvalue_cp/");	// NOI18N
        tf.setValue("/testvalue_cp/");	// NOI18N

        pw.pushKey(java.awt.event.KeyEvent.VK_TAB);
        pw.pushKey(java.awt.event.KeyEvent.VK_TAB);
        /*
	try {
		Robot rb=new java.awt.Robot();
		rb.keyPress(java.awt.event.KeyEvent.VK_TAB);
		rb.keyPress(java.awt.event.KeyEvent.VK_TAB);
	} catch (Exception AWTException) {
		System.out.println("AWTException in test_4_8_12");	// NOI18N
	}
        */

	value=tf.getValue();
	if (!value.equals("/testvalue_cp/")) {	// NOI18N
		pw.close();
		fail("Invalid 'Base Class Path URL' field value ("+value+")");
	}

	pw.close();
    }

// 4.8.12
    public void test_4_8_12() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Class Path URL");
        tf.setValue("/testvalue_cp/");	// NOI18N
	tf.openEditor();
	
	try {
		dop=new NbDialogOperator("Base Class Path URL");
	} catch(Exception ex) {
		//dop.close();
		pw.close();
		throw new AssertionFailedErrorException("No 'Base Class Path URL' dialog appears",ex);
	}

	JTextAreaOperator jt=new JTextAreaOperator(dop,0);
	value=jt.getText();
	if (!value.equals("/testvalue_cp/")) {	// NOI18N
		dop.close();
		pw.close();
		fail("Invalid 'Base Class Path URL' field value ("+value+")");
	}

	dop.close();	
	pw.close();
    }

// 4.8.13
    public void test_4_8_13() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Class Path URL");
	tf.openEditor();
	
	try {
		dop=new NbDialogOperator("Base Class Path URL");
	} catch(Exception ex) {
		//dop.close();
		pw.close();
		throw new AssertionFailedErrorException("No 'Base Class Path URL' dialog appears",ex);
	}

	JTextAreaOperator jt=new JTextAreaOperator(dop,0);
	jt.setText("classpath");	// NOI18N

	JButtonOperator ok=new JButtonOperator(dop,org.netbeans.jellytools.Bundle.getString("org.netbeans.core.Bundle", "OK_OPTION_CAPTION"));
	ok.doClick();
	value=tf.getValue();

	if (!value.equals("/classpath/")) {	// NOI18N
		dop.close();
		pw.close();
		fail("Invalid 'Base Class Path URL' field value ("+value+")");
	}

	pw.close();
    }

// 4.8.14
    public void test_4_8_14() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Filesystems URL");
	tf.openEditor();

        AssertionFailedErrorException e = checkDialog("Base Filesystems URL");
        // what is in value???
        //tf.setValue(value);
        pw.close();
        if (e != null)
            throw e;          
    }

// 4.8.15
    public void test_4_8_15() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Filesystems URL");
	tf.openEditor();
	
	try {
		dop=new NbDialogOperator("Base Filesystems URL");
	} catch(Exception ex) {
		//dop.close();
		pw.close();
		throw new AssertionFailedErrorException("No 'Base Filesystems URL' dialog appears",ex);
	}

	JTextAreaOperator jt=new JTextAreaOperator(dop,0);
	value=jt.getText();
	jt.setText("/qqqqqqq/");	// NOI18N
	JButtonOperator cancel=new JButtonOperator(dop,org.netbeans.jellytools.Bundle.getString("org.netbeans.core.Bundle", "CANCEL_OPTION_CAPTION"));
	cancel.doClick();

	if (!value.equals(tf.getValue())) {
		dop.close();
		pw.close();
		fail("Cancel in 'Base Filesystems URL' dialog doesn't work");
	}

	pw.close();
    }

// 4.8.16
    public void test_4_8_16() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Filesystems URL");
	old_value=tf.getValue();

	//JTextFieldOperator to=tf.textField();
        //to.typeText("/testvalue_fs/");	// NOI18N
        tf.setValue("/testvalue_fs/");	// NOI18N

        pw.pushKey(java.awt.event.KeyEvent.VK_TAB);
        pw.pushKey(java.awt.event.KeyEvent.VK_TAB);
        /*
	try {
		Robot rb=new java.awt.Robot();
		rb.keyPress(java.awt.event.KeyEvent.VK_TAB);
		rb.keyPress(java.awt.event.KeyEvent.VK_TAB);
	} catch (Exception AWTException) {
		System.out.println("AWTException in test_4_8_17");	// NOI18N
	}
        */

	value=tf.getValue();
	if (!value.equals("/testvalue_fs/")) {	// NOI18N
		pw.close();
		fail("Invalid 'Base Filesystems URL' field value ("+value+")");
	}

	pw.close();
    }

// 4.8.17
    public void test_4_8_17() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Filesystems URL");
        tf.setValue("/testvalue_fs/");	// NOI18N
	tf.openEditor();
	
	try {
		dop=new NbDialogOperator("Base Filesystems URL");
	} catch(Exception ex) {
		//dop.close();
		pw.close();
		throw new AssertionFailedErrorException("No 'Base Filesystems URL' dialog appears",ex);
	}

	JTextAreaOperator jt=new JTextAreaOperator(dop,0);
	value=jt.getText();
	if (!value.equals("/testvalue_fs/")) {	// NOI18N
		dop.close();
		pw.close();
		fail("Invalid 'Base Filesystems URL' field value ("+value+")");
	}

	dop.close();
	pw.close();
    }

// 4.8.18
    public void test_4_8_18() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Filesystems URL");
	tf.openEditor();
	
	try {
		dop=new NbDialogOperator("Base Filesystems URL");
	} catch(Exception ex) {
		//dop.close();
		pw.close();
		throw new AssertionFailedErrorException("No 'Base Filesystems URL' dialog appears",ex);
	}

	JTextAreaOperator jt=new JTextAreaOperator(dop,0);
	jt.setText("repository");	// NOI18N

	JButtonOperator ok=new JButtonOperator(dop,org.netbeans.jellytools.Bundle.getString("org.netbeans.core.Bundle", "OK_OPTION_CAPTION"));
	ok.doClick();
	value=tf.getValue();

	if (!value.equals("/repository/")) {	// NOI18N
		dop.close();
		pw.close();
		fail("Invalid 'Base Filesystems URL' field value ("+value+")");
	}

	pw.close();
    }

// 4.8.19
    public void test_4_8_19() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Javadoc URL");
	tf.openEditor();

	
        AssertionFailedErrorException e = checkDialog("Base Javadoc URL");
        // what is in value???
        //tf.setValue(value);
        pw.close();
        if (e != null)
            throw e; 
    }

// 4.8.20
    public void test_4_8_20() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Javadoc URL");
	tf.openEditor();
	
	try {
		dop=new NbDialogOperator("Base Javadoc URL");
	} catch(Exception ex) {
		//dop.close();
		pw.close();
		throw new AssertionFailedErrorException("No 'Base Javadoc URL' dialog appears",ex);
	}

	JTextAreaOperator jt=new JTextAreaOperator(dop,0);
	value=jt.getText();
	jt.setText("/qqqqqqq/");	// NOI18N
	JButtonOperator cancel=new JButtonOperator(dop,org.netbeans.jellytools.Bundle.getString("org.netbeans.core.Bundle", "CANCEL_OPTION_CAPTION"));
	cancel.doClick();

	if (!value.equals(tf.getValue())) {
		dop.close();
		pw.close();
		fail("Cancel in 'Base Javadoc URL' dialog doesn't work");
	}

	pw.close();
    }

// 4.8.21
    public void test_4_8_21() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Javadoc URL");
	old_value=tf.getValue();

	//JTextFieldOperator to=tf.textField();
        //to.typeText("/testvalue_jd/");	// NOI18N
        tf.setValue("/testvalue_jd/");	// NOI18N

        pw.pushKey(java.awt.event.KeyEvent.VK_TAB);
        pw.pushKey(java.awt.event.KeyEvent.VK_TAB);
        /*
	try {
		Robot rb=new java.awt.Robot();
		rb.keyPress(java.awt.event.KeyEvent.VK_TAB);
		rb.keyPress(java.awt.event.KeyEvent.VK_TAB);
	} catch (Exception AWTException) {
		System.out.println("AWTException in test_4_8_22");	// NOI18N
	}
        */

	value=tf.getValue();
	if (!value.equals("/testvalue_jd/")) {	// NOI18N
		pw.close();
		fail("Invalid 'Base Javadoc URL' field value ("+value+")");
	}

	pw.close();
    }

// 4.8.22
    public void test_4_8_22() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Javadoc URL");
        tf.setValue("/testvalue_jd/");	// NOI18N
	tf.openEditor();
	
	try {
		dop=new NbDialogOperator("Base Javadoc URL");
	} catch(Exception ex) {
		//dop.close();
		pw.close();
		throw new AssertionFailedErrorException("No 'Base Javadoc URL' dialog appears",ex);
	}

	JTextAreaOperator jt=new JTextAreaOperator(dop,0);
	value=jt.getText();
	if (!value.equals("/testvalue_jd/")) {	// NOI18N
		dop.close();
		pw.close();
		fail("Invalid 'Base Javadoc URL' field value ("+value+")");
	}

	dop.close();
	pw.close();
    }

// 4.8.23
    public void test_4_8_23() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Expert");

	TextFieldProperty tf=new TextFieldProperty(psto,"Base Javadoc URL");
	tf.openEditor();
	
	try {
		dop=new NbDialogOperator("Base Javadoc URL");
	} catch(Exception ex) {
		//dop.close();
		pw.close();
		throw new AssertionFailedErrorException("No 'Base Javadoc URL' dialog appears",ex);
	}

	JTextAreaOperator jt=new JTextAreaOperator(dop,0);
	jt.setText("javadoc");	// NOI18N

	JButtonOperator ok=new JButtonOperator(dop,org.netbeans.jellytools.Bundle.getString("org.netbeans.core.Bundle", "OK_OPTION_CAPTION"));
	ok.doClick();
	value=tf.getValue();

	if (!value.equals("/javadoc/")) {	// NOI18N
		dop.close();
		pw.close();
		fail("Invalid 'Base Javadoc URL' field value ("+value+")");
	}

	pw.close();
    }

// 4.8.24
    public void test_4_8_24() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Hosts with Granted Access");
	tf.setValue("Any Host");
	tf.openEditor();

	try {
		dop=new NbDialogOperator("Hosts with Granted Access");
	} catch(Exception ex) {
		pw.close();
		throw new AssertionFailedErrorException("No 'Hosts with Granted Access' dialog appears",ex);
	}

	JRadioButtonOperator rb=new JRadioButtonOperator(dop,"Selected Hosts");
	rb.doClick();

	JTextAreaOperator jt=new JTextAreaOperator(dop,0);
	jt.setText("boo");	// NOI18N

	JButtonOperator ok=new JButtonOperator(dop,org.netbeans.jellytools.Bundle.getString("org.netbeans.core.Bundle", "OK_OPTION_CAPTION"));
	ok.doClick();

	value=tf.getValue();
	if (!value.equals("Selected Hosts: boo")) {
		pw.close();
		fail("'Selected Hosts: ' isn't set. ("+value+")");
	}

	pw.close();
    }

// 4.9 Accessibility

// 4.9.1
    public void test_4_9_1() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Hosts with Granted Access");
	tf.setValue("Selected Hosts: ");
	tf.openEditor();

	try {
		dop=new NbDialogOperator("Hosts with Granted Access");
	} catch(Exception ex) {
		//dop.close();
		pw.close();
		throw new AssertionFailedErrorException("No 'Hosts with Granted Access' dialog appears",ex);
	}

        dop.pushKey(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.ALT_MASK);
        dop.releaseKey(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.ALT_MASK);
        
        /*
	try {
		Robot rb=new java.awt.Robot();
		rb.keyPress(java.awt.event.KeyEvent.VK_ALT);
		rb.keyPress(java.awt.event.KeyEvent.VK_N);
		rb.keyRelease(java.awt.event.KeyEvent.VK_N);
		rb.keyRelease(java.awt.event.KeyEvent.VK_ALT);
	} catch (Exception AWTException) {
		System.out.println("AWTException in test_4_9_1");	// NOI18N
	}
         */
        

	dop.ok();
	value=tf.getValue();

	if (!value.equals("Any Host")) {
		pw.close();
		fail("Invalid 'Hosts with Granted Access' field value ("+value+")");
	}

	pw.close();
    }

// 4.9.2
    public void test_4_9_2() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Hosts with Granted Access");
	tf.setValue("Any Host");
	tf.openEditor();

	try {
		dop=new NbDialogOperator("Hosts with Granted Access");
	} catch(Exception ex) {
		//dop.close();
		pw.close();
		throw new AssertionFailedErrorException("No 'Hosts with Granted Access' dialog appears",ex);
	}

        dop.pushKey(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK);
        dop.releaseKey(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK);
        
        /*
	try {
		Robot rb=new java.awt.Robot();
		rb.keyPress(java.awt.event.KeyEvent.VK_ALT);
		rb.keyPress(java.awt.event.KeyEvent.VK_S);
		rb.keyRelease(java.awt.event.KeyEvent.VK_S);
		rb.keyRelease(java.awt.event.KeyEvent.VK_ALT);
	} catch (Exception AWTException) {
		System.out.println("AWTException in test_4_9_2");	// NOI18N
	}
        */

	dop.ok();
	value=tf.getValue();

	if (!value.equals("Selected Hosts: ")) {
		pw.close();
		fail("Invalid 'Hosts with Granted Access' field value");
	}

	pw.close();
    }

// 4.9.3
    public void test_4_9_3() {

	switchToHTTPServerNode();
        PropertySheetOperator pw = new PropertySheetOperator("HTTP Server");
        psto = pw.getPropertySheetTabOperator("Properties");

	TextFieldProperty tf=new TextFieldProperty(psto,"Hosts with Granted Access");
	tf.setValue("Selected Hosts: ");
	tf.openEditor();

	try {
		dop=new NbDialogOperator("Hosts with Granted Access");
	} catch(Exception ex) {
		//dop.close();
		pw.close();
		throw new AssertionFailedErrorException("No 'Hosts with Granted Access' dialog appears",ex);
	}

        dop.pushKey(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.ALT_MASK);
        dop.releaseKey(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.ALT_MASK);
        
        /*
        JTextAreaOperator jt = new JTextAreaOperator(dop);
        jt.pushKey(java.awt.event.KeyEvent.VK_T);
        jt.releaseKey(java.awt.event.KeyEvent.VK_T);
        jt.pushKey(java.awt.event.KeyEvent.VK_E);
        jt.releaseKey(java.awt.event.KeyEvent.VK_E);
        jt.pushKey(java.awt.event.KeyEvent.VK_S);
        jt.releaseKey(java.awt.event.KeyEvent.VK_S);
        jt.pushKey(java.awt.event.KeyEvent.VK_T);
        jt.releaseKey(java.awt.event.KeyEvent.VK_T);
        */
                
        
        try {
		Robot rb=new java.awt.Robot();
                /*
		rb.keyPress(java.awt.event.KeyEvent.VK_ALT);
		rb.keyPress(java.awt.event.KeyEvent.VK_G);
		rb.keyRelease(java.awt.event.KeyEvent.VK_G);
		rb.keyRelease(java.awt.event.KeyEvent.VK_ALT);
                */
		rb.keyPress(java.awt.event.KeyEvent.VK_T);
		rb.keyRelease(java.awt.event.KeyEvent.VK_T);
		rb.keyPress(java.awt.event.KeyEvent.VK_E);
		rb.keyRelease(java.awt.event.KeyEvent.VK_E);
		rb.keyPress(java.awt.event.KeyEvent.VK_S);
		rb.keyRelease(java.awt.event.KeyEvent.VK_S);
		rb.keyPress(java.awt.event.KeyEvent.VK_T);
		rb.keyRelease(java.awt.event.KeyEvent.VK_T);
	} catch (Exception AWTException) {
		System.out.println("AWTException in test_4_9_3");	// NOI18N
	}
        

	dop.ok();
	value=tf.getValue();

	if (!value.equals("Selected Hosts: test")) {
		pw.close();
		fail("Invalid 'Hosts with Granted Access' field value ("+value+")");
	}

	pw.close();
    }
  
} 
