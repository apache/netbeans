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
package org.netbeans.xml.test.core;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.ErrorManager;
/**
 * <P>
 * <P>
 * <FONT COLOR="#CC3333" FACE="Courier New, Monospaced" SIZE="+1">
 * <B>
 * <BR> XML Module Jemmy Test: NewFromTemplate
 * </B>
 * </FONT>
 * <BR><BR><B>What it tests:</B><BR>
 *
 * This test tests New From Template action on all XML's templates.
 *
 * <BR><BR><B>How it works:</B><BR>
 *
 * 1) create new documents from template<BR>
 * 2) write the created documents to output<BR>
 * 3) close source editor<BR>
 *
 * <BR><BR><B>Settings:</B><BR>
 * none<BR>
 *
 * <BR><BR><B>Output (Golden file):</B><BR>
 * Set XML documents.<BR>
 *
 * <BR><B>To Do:</B><BR>
 * none<BR>
 *
 * <P>Created on Januar 09, 2001, 12:33 PM
 * <P>
 */

public class Fail extends NbTestCase {
    public Fail(String testName) {
	super(testName);
    }
    
// ----------------------- MAIN ---------------------------//
    
    public static Test suite() {
	TestSuite suite = new NbTestSuite();
	System.out.println("");
	System.out.println("SUIT, ktera nahlasi exception");
	int i = 1;
	while (i-- > 0){
	    try {
		Thread.currentThread().sleep(3000);
	    } catch (InterruptedException ex) {
		ex.printStackTrace();
	    }
	    System.out.print("jeste zbyva");
	    System.out.println(Integer.toString(i));
	}
	System.out.println("");
	ErrorManager errMan = ErrorManager.getDefault();
	errMan.notify(errMan.USER,new NullPointerException("MOJE EXCEPTION"));
	try {
	    Thread.currentThread().sleep(10000);
	} catch (InterruptedException ex) {
	    ex.printStackTrace();
	}
//  suite.addTest(new CoreTemplatesTest("testNewXMLFromTemplate"));
	return suite;
    }
    
    public static void main(String[] args) throws Exception {
	//DEBUG = true;
	//JemmyProperties.getCurrentTimeouts().loadDebugTimeouts();
	TestRunner.run(suite());
    }
    
}



