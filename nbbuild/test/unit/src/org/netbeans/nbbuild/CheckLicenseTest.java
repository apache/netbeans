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

package org.netbeans.nbbuild;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jaroslav Tulach
 */
public class CheckLicenseTest extends TestBase {

    public CheckLicenseTest(String testName) {
        super(testName);
    }

    public void testWeCanSearchForSunPublicLicense() throws Exception {
        java.io.File license = extractString(
            "<!-- Sun Public License -->\n" +
            "<head></head><body>\n" +
            "<a href=\"http://www.netbeans.org/download/dev/javadoc/OpenAPIs/index.hml\">Forbidden link</a>\n" +
            "</body>"
        );
      
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"checkl\" classname=\"org.netbeans.nbbuild.CheckLicense\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <checkl fragment='Sun Public' >" +
            "   <fileset dir='" + license.getParent() + "'>" +
            "    <include name=\"" + license.getName () + "\" />" +
            "   </fileset>\n" +
            "  </checkl>" +
            "</target>" +
            "</project>"
        );
        // success
        execute (f, new String[] { });

        if (getStdErr().indexOf(license.getPath()) > - 1) {
            fail("file name shall not be there: " + getStdErr());
        }
        if (getStdErr().indexOf("no license") > - 1) {
            fail("warning shall not be there: " + getStdErr());
        }
    }        

    public void testTheTaskFailsIfItIsPresent() throws Exception {
        java.io.File license = extractString(
            "<!-- Sun Public License -->\n" +
            "<head></head><body>\n" +
            "<a href=\"http://www.netbeans.org/download/dev/javadoc/OpenAPIs/index.hml\">Forbidden link</a>\n" +
            "</body>"
        );
        java.io.File license2 = extractString(
            "<!-- Sun Public License -->\n" +
            "<head></head><body>\n" +
            "<a href=\"http://www.netbeans.org/download/dev/javadoc/OpenAPIs/index.hml\">Forbidden link</a>\n" +
            "</body>"
        );
        assertEquals(license.getParent(), license2.getParent());
      
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"checkl\" classname=\"org.netbeans.nbbuild.CheckLicense\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <checkl fragment='Sun Public' fail='whenpresent' >" +
            "   <fileset dir='" + license.getParent() + "'>" +
            "    <include name=\"" + license.getName () + "\" />" +
            "    <include name=\"" + license2.getName () + "\" />" +
            "   </fileset>\n" +
            "  </checkl>" +
            "</target>" +
            "</project>"
        );
        try {
            execute (f, new String[] { });
            fail("Should fail as the license is missing");
        } catch (ExecutionError ex) {
            // ok
        }
        
        String out = getStdErr();
        if (out.indexOf(license.getName()) == -1) {
            fail(license.getName() + " should be there: " + out);
        }
        if (out.indexOf(license2.getName()) == -1) {
            fail(license2.getName() + " should be there: " + out);
        }
    }        
    
    public void testTheTaskReportsIfItIsMissing() throws Exception {
        java.io.File license = extractString(
            "<head></head><body>\n" +
            "<a href=\"http://www.netbeans.org/download/dev/javadoc/OpenAPIs/index.hml\">Forbidden link</a>\n" +
            "</body>"
        );
      
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"checkl\" classname=\"org.netbeans.nbbuild.CheckLicense\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <checkl fragment='Sun Public' >" +
            "   <fileset dir='" + license.getParent() + "'>" +
            "    <include name=\"" + license.getName () + "\" />" +
            "   </fileset>\n" +
            "  </checkl>" +
            "</target>" +
            "</project>"
        );
        // success
        execute (f, new String[] { });
        
        if (getStdErr().indexOf(license.getPath()) == - 1) {
            fail("file name shall be there: " + getStdErr());
        }
        if (getStdErr().indexOf("no license") == - 1) {
            fail("warning shall be there: " + getStdErr());
        }
    }        

    public void testNoReportsWhenInFailMode() throws Exception {
        java.io.File license = extractString(
            "<head></head><body>\n" +
            "<a href=\"http://www.netbeans.org/download/dev/javadoc/OpenAPIs/index.hml\">Forbidden link</a>\n" +
            "</body>"
        );
      
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"checkl\" classname=\"org.netbeans.nbbuild.CheckLicense\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <checkl fragment='Sun Public' fail='whenpresent'>" +
            "   <fileset dir='" + license.getParent() + "'>" +
            "    <include name=\"" + license.getName () + "\" />" +
            "   </fileset>\n" +
            "  </checkl>" +
            "</target>" +
            "</project>"
        );
        // success
        execute (f, new String[] { });
        
        if (getStdErr().indexOf(license.getPath()) != - 1) {
            fail("file name shall not be there: " + getStdErr());
        }
        if (getStdErr().indexOf("no license") != - 1) {
            fail("warning shall not be there: " + getStdErr());
        }
    }        
    
    public void testTheTaskFailsIfItIsMissing() throws Exception {
        java.io.File license = extractString(
            "<head></head><body>\n" +
            "<a href=\"http://www.netbeans.org/download/dev/javadoc/OpenAPIs/index.hml\">Forbidden link</a>\n" +
            "</body>"
        );
      
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"checkl\" classname=\"org.netbeans.nbbuild.CheckLicense\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <checkl fragment='Sun Public' fail='whenmissing' >" +
            "   <fileset dir='" + license.getParent() + "'>" +
            "    <include name=\"" + license.getName () + "\" />" +
            "   </fileset>\n" +
            "  </checkl>" +
            "</target>" +
            "</project>"
        );
        try {
            execute (f, new String[] { });
            fail("Should fail as the license is missing");
        } catch (ExecutionError ex) {
            // ok
        }
    }        
    
    public void testReplaceJavaLicense() throws Exception {
        java.io.File tmp = extractString(
"/**\n" +
" * Licensed to the Apache Software Foundation (ASF) under one\n" +
" * or more contributor license agreements.  See the NOTICE file\n" +
" * distributed with this work for additional information\n" +
" * regarding copyright ownership.  The ASF licenses this file\n" +
" * to you under the Apache License, Version 2.0 (the\n" +
" * \"License\"); you may not use this file except in compliance\n" +
" * with the License.  You may obtain a copy of the License at\n" +
" *\n" +
" *   https://www.apache.org/licenses/LICENSE-2.0\n" +
" *\n" +
" * Unless required by applicable law or agreed to in writing,\n" +
" * software distributed under the License is distributed on an\n" +
" * \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY\n" +
" * KIND, either express or implied.  See the License for the\n" +
" * specific language governing permissions and limitations\n" +
" * under the License.\n" +
" */"
        );
        File java = new File(tmp.getParentFile(), "MyTest.java");
        tmp.renameTo(java);
        assertTrue("File exists", java.exists());
      
        java.io.File f = extractResource("CheckLicenseAnt.xml");

        execute (f, new String[] { 
            "-verbose", 
            "-Ddir=" + java.getParent(),  
            "-Dinclude=" + java.getName(),
        });
        
        if (getStdOut().indexOf(java.getPath()) == - 1) {
            fail("file name shall be there: " + getStdOut());
        }
        
        
        assertTrue("Still exists", java.exists());
        
        String content = readFile(java);
        {
            Matcher m = Pattern.compile("\\* *Ahoj *\\* *Jardo").matcher(content.replace('\n', ' '));
            if (!m.find()) {
                fail("Replacement shall be there together with prefix:\n" + content);
            }
        }
        
        {
            Matcher m = Pattern.compile("^ \\*New. \\*Warning", Pattern.MULTILINE | Pattern.DOTALL).matcher(content);
            if (!m.find()) {
                fail("warning shall be there:\n" + content);
            }
        }
        
        {
            String[] lines = content.split("\n");
            if (lines.length < 5) {
                fail("There should be more than five lines: " + content);
            }
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].length() == 0) {
                    fail("There is an empty line: " + content);
                }
                if (lines[i].equals(" */")) {
                    break;
                }
                if (lines[i].endsWith(" ")) {
                    fail("Ends with space: '" + lines[i] + "' in:\n" + content);
                }
            }
        }
    }        

    
    public void testReplaceHTMLLicense() throws Exception {
        java.io.File f = extractResource("CheckLicenseAnt.xml");

        java.io.File tmp = extractResource("CheckLicenseHtmlExample.xml");
        File html = new File(tmp.getParentFile(), "MyTest.html");
        tmp.renameTo(html);
        assertTrue("File exists", html.exists());
      

        execute (f, new String[] { 
            "-verbose", 
            "-Ddir=" + html.getParent(),  
            "-Dinclude=" + html.getName(),
        });
        
        if (getStdOut().indexOf(html.getPath()) == - 1) {
            fail("file name shall be there: " + getStdOut());
        }
        
        
        assertTrue("Still exists", html.exists());
        
        String content = readFile(html);
        {
            Matcher m = Pattern.compile(" *- *Ahoj *- *Jardo").matcher(content.replace('\n', ' '));
            if (!m.find()) {
                fail("Replacement shall be there together with prefix:\n" + content);
            }
        }
        
        {
            Matcher m = Pattern.compile("^ *-New. *-Warning", Pattern.MULTILINE | Pattern.DOTALL).matcher(content);
            if (!m.find()) {
                fail("warning shall be there:\n" + content);
            }
        }
        
        {
            String[] lines = content.split("\n");
            if (lines.length < 5) {
                fail("There should be more than five lines: " + content);
            }
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].indexOf("-->") >= 0) {
                    break;
                }
                if (lines[i].endsWith(" ")) {
                    fail("Ends with space: '" + lines[i] + "' in:\n" + content);
                }
            }
        }
    }        

    public void testNoReplaceWhenNoHTMLLicense() throws Exception {
        java.io.File f = extractResource("CheckLicenseAnt.xml");

        java.io.File tmp = extractString(
            "<head></head><body>\n" +
            "<a href=\"http://www.netbeans.org/download/dev/javadoc/OpenAPIs/index.hml\">Forbidden link</a>\n" +
            "1997-2006" +
            "</body>"
        );
        File html = new File(tmp.getParentFile(), "MyTest.html");
        tmp.renameTo(html);
        assertTrue("File exists", html.exists());
      

        execute (f, new String[] { 
            "-Ddir=" + html.getParent(),  
            "-Dinclude=" + html.getName(),
        });
        
        if (getStdOut().indexOf(html.getPath()) != - 1) {
            fail("file name shall not be there: " + getStdOut());
        }
        
    }        

    public void testMayReplaces() throws Exception {
        if (isWindows()) {
            return;
        }
        
        java.io.File f = extractResource("CheckLicenseAnt.xml");

        java.io.File tmp = extractString(
            "<head></head><body>\n" +
            "<a href=\"http://www.netbeans.org/download/dev/javadoc/OpenAPIs/index.hml\">Forbidden link</a>\n" +
            "Original Code\n" +
            "Original Code\n" +
            "Original Code\n" +
            "Original Code\n" +
            "</body>"
        );
        File html = new File(tmp.getParentFile(), "MyTest.html");
        tmp.renameTo(html);
        assertTrue("File exists", html.exists());
      

        execute (f, new String[] { 
            "-Ddir=" + html.getParent(),  
            "-Dinclude=" + html.getName(),
        });
        
        if (getStdOut().indexOf("Original Code") != - 1) {
            fail("Original Code shall not be there: " + getStdOut());
        }

        String out = readFile(html);
        int first = out.indexOf("Original Software");
        if (first == - 1) {
            fail("Original Software shall be there: " + out);
        }
        if (out.indexOf("Original Software", first + 1) == - 1) {
            fail("Original Software shall be there: " + out);
        }
    }    
    
    
    public void testWrongLineBeginningsWhenNoPrefix() throws Exception {
        String txt = "<!--\n" +
        "                 Sun Public License Notice\n" +
        "\n" +
        "The contents of this file are subject to the Sun Public License\n" +
        "Version 1.0 (the 'License'). You may not use this file except in\n" +
        "compliance with the License. A copy of the License is available at\n" +
        "http://www.sun.com/\n" +
        "\n" +
        "The Original Code is NetBeans. The Initial Developer of the Original\n" +
        "Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun\n" +
        "Microsystems, Inc. All Rights Reserved.\n" +
        "-->\n";
        String script = createScript();
        
    
        File fileScript = extractString(script);
        File fileTxt = extractString(txt);
        
        execute (fileScript, new String[] { 
            "-Ddir=" + fileTxt.getParent(),  
            "-Dinclude=" + fileTxt.getName(),
        });
        
        if (getStdOut().indexOf("Original Code") != - 1) {
            fail("Original Code shall not be there: " + getStdOut());
        }

        String out = readFile(fileTxt);
        
        String[] arr = out.split("\n");
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].endsWith(" ")) {
                fail("Ends with space: '" + arr[i] + "' in:\n" + out);
            }
            if (arr[i].length() < 2) {
                continue;
            }
            if (arr[i].charAt(0) != ' ') {
                continue;
            }
            
            fail("This line seems to start with space:\n" + arr[i] + "\nwhich is wrong in whole output:\n" + out);
        }
    }

    private static String createScript() {
        String script =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
    "<project name=\"Test\" basedir=\".\" default=\"all\" >" +
    "  <taskdef name=\"checklicense\" classname=\"org.netbeans.nbbuild.CheckLicense\" classpath=\"${nbantext.jar}\"/>" +
    "<target name=\"all\" >" +
"        <checklicense >\n" +
"            <fileset dir='${dir}'>\n" +
"              <include name='${include}'/>" + 
"            </fileset>\n" +
"\n" +            
"    <convert \n" +
"        token='^([ \\t]*[^ \\n]+[ \\t]?)?[ \\t]*Sun Public License Notice' \n" +
"        prefix='true'\n" +
"    >\n" +
"        <line text='The contents of this file are subject to the terms of the Common Development'/>\n" +
"        <line text='and Distribution License (the License). You may not use this file except in'/>\n" +
"        <line text='compliance with the License.'/>\n" +
"    </convert>\n" +
"    <convert \n" +
"     token='The *contents *of *this *file *are\n" +
" *subject *to *the *Sun *Public.*available.*at.*([hH][tT][tT][pP]://www.sun.com/|http://jalopy.sf.net/license-spl.html)'\n" +
"    >\n" +
"        <line text='You can obtain a copy of the License at http://www.netbeans.org/cddl.html'/>\n" +
"        <line text='or http://www.netbeans.org/cddl.txt.'/>\n" +
"        <line text=''/>\n" +
"        <line text='When distributing Covered Code, include this CDDL Header Notice in each file'/>\n" +
"        <line text='and include the License file at http://www.netbeans.org/cddl.txt.'/>\n" +
"        <line text='If applicable, add the following below the CDDL Header, with the fields'/>\n" +
"        <line text='enclosed by brackets [] replaced by your own identifying information:'/>\n" +
"        <line text='\"Portions Copyrighted [year] [name of copyright owner]\"'/>\n" +
"   </convert>\n" +
"   <convert token='1997-[0-2][09][09][0-9]' replace='1997-2006'/>\n" +
"   <convert token='Original\\n[^A-Za-z]*Code' replace='Original\\nSoftware' replaceall='true'/>\n" +
"   <convert token='Original Code' replace='Original Software' replaceall='true'/>\n" +
"        </checklicense>\n" +
"     </target>\n" +
"   </project>\n";
        return script;
    }
    
    public void testReplacesTextSeparatedByNewLine() throws Exception {
        if (isWindows()) {
            return;
        }
        java.io.File f = extractResource("CheckLicenseAnt.xml");

        java.io.File tmp = extractString(
            "/*\n" +
            " *                 Sun Public License Notice\n" +
            " * \n" +
            " * The contents of this file are subject to the Sun Public License\n" +
            " * Version 1.0 (the 'License'). You may not use this file except in\n" +
            " * compliance with the License. A copy of the License is available at\n" +
            " * http://www.sun.com/\n" +
            " * \n" +
            " * The Original Code is NetBeans. The Initial Developer of the Original\n" +
            " * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun\n" +
            " * Microsystems, Inc. All Rights Reserved.\n" +
            " */\n" +
            "\n" +
            "\n" +
            "package org.openide.text;\n"
        );
        File java = new File(tmp.getParentFile(), "MyTest.html");
        tmp.renameTo(java);
        assertTrue("File exists", java.exists());
      

        execute (f, new String[] { 
            "-Ddir=" + java.getParent(),  
            "-Dinclude=" + java.getName(),
        });
        
        if (getStdOut().indexOf("Code") != - 1) {
            fail("Original Code shall not be there: " + getStdOut());
        }

        String out = readFile(java);
        int first = out.indexOf("Original Software");
        if (first == - 1) {
            fail("Original Software shall be there: " + out);
        }
        if (out.indexOf("Software", first + 25) == - 1) {
            fail("Original Software shall be there: " + out);
        }
        
        String[] lines = out.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].length() > 80) {
                fail("Too long line:\n" + lines[i] + "\n in file:\n" + out);
            }
            if (lines[i].endsWith(" ")) {
                fail("Ends with space: '" + lines[i] + "' in:\n" + out);
            }
        }
    }    
    
    
    
    public void testWorksOnEmptyFile() throws Exception {
        java.io.File f = extractResource("CheckLicenseAnt.xml");

        java.io.File tmp = extractString("");
        File html = new File(tmp.getParentFile(), "MyTest.html");
        tmp.renameTo(html);
        assertTrue("File exists", html.exists());
      

        execute (f, new String[] { 
            "-Ddir=" + html.getParent(),  
            "-Dinclude=" + html.getName(),
        });
        
        if (getStdOut().indexOf(html.getPath()) != - 1) {
            fail("file name shall not be there: " + getStdOut());
        }
        
    }        
    
    public void testReplacePropertiesLicense() throws Exception {
        if (isWindows()) {
            return;
        }
        java.io.File f = extractResource("CheckLicenseAnt.xml");

        java.io.File tmp = extractResource("CheckLicensePropertiesExample.properties");
        File html = new File(tmp.getParentFile(), "MyTest.html");
        tmp.renameTo(html);
        assertTrue("File exists", html.exists());
      

        execute (f, new String[] { 
            "-verbose", 
            "-Ddir=" + html.getParent(),  
            "-Dinclude=" + html.getName(),
        });
        
        if (getStdOut().indexOf(html.getPath()) == - 1) {
            fail("file name shall be there: " + getStdOut());
        }
        
        
        assertTrue("Still exists", html.exists());
        
        
        String content = readFile(html);
        
        if (!content.startsWith("#")) {
            fail("Shall start with #:\n" + content);
        }
        
        {
            Matcher m = Pattern.compile(" *\\# *Ahoj *\\# *Jardo").matcher(content.replace('\n', ' '));
            if (!m.find()) {
                fail("Replacement shall be there together with prefix:\n" + content);
            }
        }
        
        {
            Matcher m = Pattern.compile("^ *\\#New. *\\#Warning", Pattern.MULTILINE | Pattern.DOTALL).matcher(content);
            if (!m.find()) {
                fail("warning shall be there:\n" + content);
            }
        }
        
        {
            String[] lines = content.split("\n");
            if (lines.length < 5) {
                fail("There should be more than five lines: " + content);
            }
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].endsWith(" ")) {
                    fail("Ends with space: '" + lines[i] + "' in:\n" + content);
                }
                if (lines[i].length() == 0) {
                    fail("There is an empty line: " + content);
                }
                if (lines[i].indexOf("Portions Copyrighted 2012 Sun Microsystems") >= 0) {
                    break;
                }
            }
        }
    }        

    private static boolean isWindows() {
        String name = System.getProperty("os.name");
        return name != null && name.toLowerCase().indexOf("windows") >= 0;
    }
    
    public void testReplaceXMLLicense() throws Exception {
        java.io.File f = extractResource("CheckLicenseAnt.xml");

        java.io.File tmp = extractResource("CheckLicenseXmlExample.xml");
        File xml = new File(tmp.getParentFile(), "MyTest.xml");
        tmp.renameTo(xml);
        assertTrue("File exists", xml.exists());
      

        execute (f, new String[] { 
            "-verbose", 
            "-Ddir=" + xml.getParent(),  
            "-Dinclude=" + xml.getName(),
        });
        
        if (getStdOut().indexOf(xml.getPath()) == - 1) {
            fail("file name shall be there: " + getStdOut());
        }
        
        
        assertTrue("Still exists", xml.exists());
        
        
        String content = readFile(xml);
        
        if (!content.startsWith("<")) {
            fail("Shall start with <:\n" + content);
        }
        
        {
            Matcher m = Pattern.compile(" *Ahoj *Jardo").matcher(content.replace('\n', ' '));
            if (!m.find()) {
                fail("Replacement shall be there together with prefix:\n" + content);
            }
        }
        
        {
            Matcher m = Pattern.compile("^ *New. *Warning", Pattern.MULTILINE | Pattern.DOTALL).matcher(content);
            if (!m.find()) {
                fail("warning shall be there:\n" + content);
            }
        }
    }        

    public void testProblemsWithTermEmulator() throws Exception {
        String txt =  
            "/*   \n" +
            " *			Sun Public License Notice\n" +
            " *\n" +
            " * The contents of this file are subject to the Sun Public License Version\n" +
            " * 1.0 (the \"License\"). You may not use this file except in compliance\n" +
            " * with the License. A copy of the License is available at\n" +
            " * http://www.sun.com/\n" +
            " * \n" +
            " * The Original Code is Terminal Emulator.\n" +
            " * The Initial Developer of the Original Code is Sun Microsystems, Inc..\n" +
            " * Portions created by Sun Microsystems, Inc. are Copyright (C) 2001.\n" +
            " * All Rights Reserved.\n" +
            " *\n" +
            " * Contributor(s): Ivan Soleimanipour.\n" +
            " */\n";
        String script = createScript();
        
    
        File fileScript = extractString(script);
        File fileTxt = extractString(txt);
        
        execute (fileScript, new String[] { 
            "-Ddir=" + fileTxt.getParent(),  
            "-Dinclude=" + fileTxt.getName(),
        });
        
        if (getStdOut().indexOf("Original Code") != - 1) {
            fail("Original Code shall not be there: " + getStdOut());
        }

        String out = readFile(fileTxt);


        if (out.indexOf("Sun Public") >= 0) {
            fail(out);
        }
    }

    
    public void testDoubleHtmlComments() throws Exception {
        java.io.File f = extractString(createScript());

        java.io.File tmp = extractString(
"<!--\n" +
"  --                 Sun Public License Notice\n" +
"  --\n" +
"  -- The contents of this file are subject to the Sun Public License\n" +
"  -- Version 1.0 (the \"License\"). You may not use this file except in\n" +
"  -- compliance with the License. A copy of the License is available at\n" +
"  -- http://www.sun.com/\n" +
"  --\n" +
"  -- The Original Code is NetBeans. The Initial Developer of the Original\n" +
"  -- Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun\n" +
"  -- Microsystems, Inc. All Rights Reserved.\n" +
"  -->\n"
        );
        File file = new File(tmp.getParentFile(), "MyTest.html");
        tmp.renameTo(file);
        assertTrue("File exists", file.exists());
      

        execute (f, new String[] { 
            "-Ddir=" + file.getParent(),  
            "-Dinclude=" + file.getName(),
        });
        
        String out = readFile(file);
        int first = out.indexOf("Sun Public");
        if (first != - 1) {
            fail("Sun Public shall not  be there:\n" + out);
        }
    }    
    
    public void testDoNotReplaceSpacesBeyondTheLicense() throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append('A');
        for (int i = 0; i < 10000; i++) {
            sb.append(' ');
        }
        sb.append('B');
        
        java.io.File license = extractString(
            "<!-- Sun Public License Notice -->\n" +
            "<head></head><body>\n" +
            "<a href=\"http://www.netbeans.org/download/dev/javadoc/OpenAPIs/index.hml\">Forbidden link</a>\n" +
            "</body>" +
            sb
        );
        String script = createScript();
        
    
        execute (
            extractString(script), 
            new String[] { 
            "-Ddir=" + license.getParent(),  
            "-Dinclude=" + license.getName(),
        });
        
        String out = readFile(license);


        if (out.indexOf("Sun Public") >= 0) {
            fail(out);
        }
        
        Matcher m = Pattern.compile("A( *)B").matcher(out);
        if (!m.find()) {
            fail("There should be long line:\n" + out);
        }
        if (m.group(1).length() != 10000) {
            fail("There should be 10000 spaces, but is only: " + m.group(1).length() + "\n" + out);
        }
    }    
}

      
