/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.test.gui.web.extbrowser.settings;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.ExplorerOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.NbDialogOperator;

import org.netbeans.jellytools.properties.ComboBoxProperty;
import org.netbeans.jellytools.properties.PropertySheetTabOperator;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jellytools.properties.editors.FileCustomEditorOperator;




import org.netbeans.test.gui.web.util.BrowserUtils;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.junit.NbTestSuite;
import java.io.File;

public class JspAndServletSettings extends JellyTestCase {
    private static String fSep = System.getProperty("file.separator");
    private static String iSep = "|";
   

    public JspAndServletSettings(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
         
    //method required by JUnit
    public static junit.framework.Test suite() {
	return new NbTestSuite(JspAndServletSettings.class);
    }

    /**
       System settings : External Browser(Unix)
     **/
    public void testEBU() {
	String newVal = Bundle.getString("org.netbeans.modules.extbrowser.Bundle" ,"Services/Browsers/UnixWebBrowser.settings");
	testSystemValue(newVal);
    }

    /**
       System settings : External Browser(Command Line)
    **/
    public void testEBCL() {
	String newVal = Bundle.getString("org.netbeans.modules.extbrowser.Bundle" ,"Services/Browsers/SimpleExtBrowser.settings");
	testSystemValue(newVal);
    }

    /**
       System settings : Swing HTML Browser
    **/
    public void testSwing() {
	String newVal = Bundle.getString("org.netbeans.core.ui.Bundle" ,"Services/Browsers/SwingBrowser.ser");
	testSystemValue(newVal);
    }


    private void testSystemValue(String newVal) {
	OptionsOperator oo = OptionsOperator.invoke();
	String dae = Bundle.getString("org.netbeans.core.Bundle", "UI/Services/DebuggingAndExecuting");
	String sett = Bundle.getString("org.netbeans.modules.web.core.Bundle","Services/JSP_Servlet/org-netbeans-modules-web-core-ServletSettings.settings");
	oo.selectOption(dae + iSep + sett);
	PropertySheetOperator pso = PropertySheetOperator.invoke();
        PropertySheetTabOperator psto = new PropertySheetTabOperator(pso);
	String pnameWebBrowser = Bundle.getString("org.netbeans.modules.web.core.Bundle" ,"PROP_WWWBrowser");
	ComboBoxProperty pr = new ComboBoxProperty(psto, pnameWebBrowser);
	pr.setValue(newVal);
	if (!pr.getValue().equals(newVal)) {
	    fail("Web Browser is not changed");
	}
	oo.close();
	oo = OptionsOperator.invoke();
	oo.selectOption(dae + iSep + sett);
	pso = PropertySheetOperator.invoke();
        psto = new PropertySheetTabOperator(pso);
	pr = new ComboBoxProperty(psto, pnameWebBrowser);
	if (!pr.getValue().equals(newVal)) {
	    fail("Web Browser property not saved");
	}
    }
}










