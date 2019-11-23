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

import org.netbeans.jellytools.properties.TextFieldProperty;
import org.netbeans.jellytools.properties.PropertySheetTabOperator;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jellytools.properties.editors.FileCustomEditorOperator;




import org.netbeans.test.gui.web.util.BrowserUtils;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.junit.NbTestSuite;
import java.io.File;

public class BrowserRegistry extends JellyTestCase {
    private static String workDir = null;     
    private static String webModule = null;
    private static String wmName = "wm1";
    private static String fSep = System.getProperty("file.separator");
    private static String iSep = "|";
    private static String classes = "Classes";
    private static boolean wa = false;
    private static String ideConfiguration = Bundle.getString("org.netbeans.core.Bundle", "UI/Services/IDEConfiguration");
    private static String sets = Bundle.getString("org.netbeans.core.Bundle", "UI/Services/IDEConfiguration/ServerAndExternalToolSettings");
    private static String browsers = Bundle.getString("org.netbeans.core.Bundle", "Services/Browsers");
    private static String cl = Bundle.getString("org.netbeans.modules.extbrowser.Bundle","CTL_SimpleExtBrowser");
    private static String ub = Bundle.getString("org.netbeans.modules.extbrowser.Bundle","CTL_UnixBrowserName");
    private static String pnameBrowserExecutable = Bundle.getString("org.netbeans.modules.extbrowser.Bundle" ,"PROP_browserExecutable");
    private static String pnameBrowserDescr = Bundle.getString("org.netbeans.modules.extbrowser.Bundle" ,"PROP_Description");
    public BrowserRegistry(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
         
    //method required by JUnit
    public static junit.framework.Test suite() {
	workDir = System.getProperty("extbrowser.workdir").replace('/', fSep.charAt(0));
	webModule = workDir + fSep + wmName;

	//WorkAround for 27177
	 OptionsOperator oo = OptionsOperator.invoke();
	 try {
	     oo.selectOption(ideConfiguration + iSep + sets + iSep + browsers + iSep + cl);
	 }catch(Exception e) {
	     try {
		 sets = "ServerAndExternalToolSettings";
		 oo.selectOption(ideConfiguration + iSep + sets + iSep + browsers + iSep + cl);
		 wa = true;
	     }catch(Exception e1) {
		 System.err.println("BrowserRegistry:suite():Looks like something is wrong with options");
	     }
	 }
	 oo.close();
	 return new NbTestSuite(BrowserRegistry.class);
    }

    /**
       External Browser (Command Line) : Change Browser Description 
     **/
    public void testChangeBrowserDescrEBCL() {
	String newDescr = "CL Browser";
	OptionsOperator oo = OptionsOperator.invoke();
	TextFieldProperty pr = getTFProperty(ideConfiguration + iSep + sets + iSep + browsers + iSep + cl, pnameBrowserDescr);
	pr.setValue(newDescr);
	if (!pr.getValue().equals(newDescr)) {
	    fail("Browser Description field not editable");
	}
	pr = getTFProperty(ideConfiguration + iSep + sets + iSep + browsers + iSep + cl, pnameBrowserDescr);
	oo.close(); //Closing options
	oo = OptionsOperator.invoke();
	pr = getTFProperty(ideConfiguration + iSep + sets + iSep + browsers + iSep + cl, pnameBrowserDescr);
	if (!pr.getValue().equals(newDescr)) {
	    fail("Browser Description field not saved");
	}
    }
   
    /**
       External Browser (Command Line) : Set Process to Browser in Path
     **/
    
    public void testSetProcessToBrowserInPathEBCL() {
	String newExec = "netscape {URL}";
	OptionsOperator oo = OptionsOperator.invoke();
	TextFieldProperty pr = getTFProperty(ideConfiguration + iSep + sets + iSep + browsers + iSep + cl, pnameBrowserExecutable);
	pr.setValue(newExec);
	if (!pr.getValue().equals(newExec)) {
	    fail("Browser Executable field not editable");
	}
	oo.close();
	oo = OptionsOperator.invoke();
	pr = getTFProperty(ideConfiguration + iSep + sets + iSep + browsers + iSep + cl, pnameBrowserExecutable);
	if (!pr.getValue().equals(newExec)) {
	    fail("Browser Executable field not saved");
	}
    }
    /**
       External Browser (Command Line) :  Set Process with Full Path to Browser
                           &
       External Browser (Command Line) :  Use String '{URL}' in Process    
     **/

    public void testSetProcessWithFullPathToBrowserEBCL() {
	String newExec = fullPathCommand();
	OptionsOperator oo = OptionsOperator.invoke();
	TextFieldProperty pr = getTFProperty(ideConfiguration + iSep + sets + iSep + browsers + iSep + cl, pnameBrowserExecutable);
	pr.openEditor();
	NbDialogOperator nbo = new NbDialogOperator(pnameBrowserExecutable);
	if(!(new JTextComponentOperator(nbo, 1)).getText().equals("{URL}")) {
	    fail("Wrong default arguments field: " + (new JTextFieldOperator(nbo, 1)).getText());
	}
	String custom = "..."; //Correct bundle Not found currently
	new JButtonOperator(nbo, custom).pushNoBlock();
	NbDialogOperator nbo1 = new NbDialogOperator("Open");
	new JTextFieldOperator(nbo1, 0).setText(newExec);
	String openTitle =  Bundle.getString("org.openide.explorer.propertysheet.editors.Bundle", "CTL_OpenButtonName");
	new JButtonOperator(nbo1, openTitle).push();
	nbo.ok();
	pr = getTFProperty(ideConfiguration + iSep + sets + iSep + browsers + iSep + cl, pnameBrowserExecutable);
	if (!pr.getValue().equals(newExec + " {URL}")) {
	    fail("Browser Executable not set via editor. \"" + pr.getValue() + "\" instead of \"" + newExec + " {URL}\"");
	}
	oo.close();
	oo = OptionsOperator.invoke();
	pr = getTFProperty(ideConfiguration + iSep + sets + iSep + browsers + iSep + cl, pnameBrowserExecutable);
	if (!pr.getValue().equals(newExec + " {URL}")) {
	    fail("Browser Executable field not saved, if set via editor. "+ pr.getValue() + " instead of \"" + newExec + " {URL}\"");
	}
    }
    /**
	   External Browser (Command Line) : Change process -- "Cancel" 
    **/
    
    public void testChangeProcessCancelEBCL() {
	String newExec = "UnReal";
	OptionsOperator oo = OptionsOperator.invoke();
	TextFieldProperty pr = getTFProperty(ideConfiguration + iSep + sets + iSep + browsers + iSep + cl, pnameBrowserExecutable);
	String origExec = pr.getValue();
	pr.openEditor();
	NbDialogOperator nbo = new NbDialogOperator(pnameBrowserExecutable);
	new JTextFieldOperator(nbo, 0).setText(newExec);
	nbo.cancel();
	pr = getTFProperty(ideConfiguration + iSep + sets + iSep + browsers + iSep + cl, pnameBrowserExecutable);
	if (!pr.getValue().equals(origExec)) {
	    fail("\"" + pr.getValue() + "\" instead of \"" + origExec + "\"");
	}
    }

    /**
	   External Browser (Command Line) : Change arguments -- "Cancel" 
    **/
    
    public void testChangeArgumentsCancelEBCL() {
	String newArg = "UnRealArgs";
	OptionsOperator oo = OptionsOperator.invoke();
	TextFieldProperty pr = getTFProperty(ideConfiguration + iSep + sets + iSep + browsers + iSep + cl, pnameBrowserExecutable);
	String origExec = pr.getValue();
	pr.openEditor();
	NbDialogOperator nbo = new NbDialogOperator(pnameBrowserExecutable);
	new JTextComponentOperator(nbo, 1).setText(newArg);
	nbo.cancel();
	pr = getTFProperty(ideConfiguration + iSep + sets + iSep + browsers + iSep + cl, pnameBrowserExecutable);
	if (!pr.getValue().equals(origExec)) {
	    fail("\"" + pr.getValue() + "\" instead of \"" + origExec + "\"");
	}
    }

    /********************************************
       External Browser (Unix)  section
    *********************************************/
    /**
       External Browser (Unix) : Change Browser Description 
     **/
    public void testChangeBrowserDescrEBU() {
	String newDescr = "CL Browser";
	OptionsOperator oo = OptionsOperator.invoke();
	TextFieldProperty pr = getTFProperty(ideConfiguration + iSep + sets + iSep + browsers + iSep + ub, pnameBrowserDescr);
	if (pr.isEditable()) {
	    fail("Browser Description field is editable");
	}
	oo.close();
	
    }
   
    /**
       External Browser (Unix) : Set Process to Browser in Path
     **/
    
    public void testSetProcessToBrowserInPathEBU() {
	String newExec = "netscape {params}";
	OptionsOperator oo = OptionsOperator.invoke();
	TextFieldProperty pr = getTFProperty(ideConfiguration + iSep + sets + iSep + browsers + iSep + ub, pnameBrowserExecutable);
	pr.setValue(newExec);
	if (!pr.getValue().equals(newExec)) {
	    fail("Browser Executable field not editable");
	}
	oo.close();
	oo = OptionsOperator.invoke();
	pr = getTFProperty(ideConfiguration + iSep + sets + iSep + browsers + iSep + ub, pnameBrowserExecutable);
	if (!pr.getValue().equals(newExec)) {
	    fail("Browser Executable field not saved");
	}
    }
    /**
       External Browser (Unix) :  Set Process with Full Path to Browser
                           &
       External Browser (Unix) :  Use String '{params}' in Process    
     **/

    public void testSetProcessWithFullPathToBrowserEBU() {
	String newExec = fullPathCommand();
	OptionsOperator oo = OptionsOperator.invoke();
	TextFieldProperty pr = getTFProperty(ideConfiguration + iSep + sets + iSep + browsers + iSep + ub, pnameBrowserExecutable);
	pr.openEditor();
	NbDialogOperator nbo = new NbDialogOperator(pnameBrowserExecutable);
	if(!(new JTextComponentOperator(nbo, 1)).getText().equals("{params}")) {
	    fail("Wrong default arguments field: " + (new JTextFieldOperator(nbo, 1)).getText());
	}
	String custom = "..."; //Correct bundle Not found currently
	new JButtonOperator(nbo, custom).pushNoBlock();
	System.out.println("Before wait for open");
	NbDialogOperator nbo1 = new NbDialogOperator("Open");
	System.out.println("After wait for open");
	new JTextFieldOperator(nbo1, 0).setText(newExec);
	String openTitle =  Bundle.getString("org.openide.explorer.propertysheet.editors.Bundle", "CTL_OpenButtonName");
	new JButtonOperator(nbo1, openTitle).push();
	nbo.ok();
	pr = getTFProperty(ideConfiguration + iSep + sets + iSep + browsers + iSep + ub, pnameBrowserExecutable);
	if (!pr.getValue().equals(newExec + " {params}")) {
	    fail("Browser Executable not set via editor. \"" + pr.getValue() + "\" instead of \"" + newExec + " {params}\"");
	}
	oo.close();
	oo = OptionsOperator.invoke();
	pr = getTFProperty(ideConfiguration + iSep + sets + iSep + browsers + iSep + ub, pnameBrowserExecutable);
	if (!pr.getValue().equals(newExec + " {params}")) {
	    fail("Browser Executable field not saved, if set via editor. "+ pr.getValue() + " instead of \"" + newExec + " {params}\"");
	}
    }
    /**
	   External Browser (Unix) : Change process -- "Cancel" 
    **/
    
    public void testChangeProcessCancelEBU() {
	String newExec = "UnReal";
	OptionsOperator oo = OptionsOperator.invoke();
	TextFieldProperty pr = getTFProperty(ideConfiguration + iSep + sets + iSep + browsers + iSep + ub, pnameBrowserExecutable);
	String origExec = pr.getValue();
	pr.openEditor();
	NbDialogOperator nbo = new NbDialogOperator(pnameBrowserExecutable);
	new JTextFieldOperator(nbo, 0).setText(newExec);
	nbo.cancel();
	pr = getTFProperty(ideConfiguration + iSep + sets + iSep + browsers + iSep + ub, pnameBrowserExecutable);
	if (!pr.getValue().equals(origExec)) {
	    fail("\"" + pr.getValue() + "\" instead of \"" + origExec + "\"");
	}
    }

    /**
	   External Browser (Unix) : Change arguments -- "Cancel" 
    **/
    
    public void testChangeArgumentsCancelEBU() {
	String newArg = "UnRealArgs";
	OptionsOperator oo = OptionsOperator.invoke();
	TextFieldProperty pr = getTFProperty(ideConfiguration + iSep + sets + iSep + browsers + iSep + ub, pnameBrowserExecutable);
	String origExec = pr.getValue();
	pr.openEditor();
	NbDialogOperator nbo = new NbDialogOperator(pnameBrowserExecutable);
	new JTextComponentOperator(nbo, 1).setText(newArg);
	nbo.cancel();
	pr = getTFProperty(ideConfiguration + iSep + sets + iSep + browsers + iSep + ub, pnameBrowserExecutable);
	if (!pr.getValue().equals(origExec)) {
	    fail("\"" + pr.getValue() + "\" instead of \"" + origExec + "\"");
	}
    }

    /**
       Please use this method to request properties.
       It allow workaround focus problems.
     */

    private TextFieldProperty getTFProperty(String path, String pname) { 
	OptionsOperator oo = OptionsOperator.invoke();
	oo.selectOption(path);
	PropertySheetOperator pso = PropertySheetOperator.invoke();
        PropertySheetTabOperator psto = new PropertySheetTabOperator(pso);
	return new TextFieldProperty(psto, pname);
    }





    private static String fullPathCommand() {
	String[] paths = null;
	String command = null;
	if(System.getProperty("os.name").indexOf("Windows")!=-1) {
            fail("This test must be extended for Windows platform");
        }else {
	    paths = new String[] {"/usr/bin/netscape","/usr/local/bin/netscape","/bin/netscape"};
	}
	for(int i=0;i<paths.length;i++) {
	    if((new File(paths[i])).exists()) {
		command = paths[i];
		i = paths.length;
	    }
	}
	if(command == null) {
	    StringBuffer reason = new StringBuffer("Nothing of following commands found on your system : ");
	    for(int i=0;i<paths.length;i++) {
		reason.append(paths[i] + ";");
	    }
	    fail(reason.toString());
	}
	return command;
    }
}







