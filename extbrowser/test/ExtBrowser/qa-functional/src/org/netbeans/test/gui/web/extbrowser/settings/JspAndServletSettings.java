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










