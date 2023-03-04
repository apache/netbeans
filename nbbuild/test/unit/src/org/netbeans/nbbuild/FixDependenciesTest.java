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


import java.util.regex.Matcher;
import java.util.regex.Pattern;


/** Behaviour of fixing module dependencies. Knows how to replace old
 * with new ones and remove those that are not needed for compilation.
 *
 * @author Jaroslav Tulach
 */
public class FixDependenciesTest extends TestBase {
    public FixDependenciesTest (String name) {
        super (name);
    }
    public void testWrongIndentation() throws Exception {
        java.io.File xml = extractResource("FixDependencies-cnd-dwarfdiscovery.xml");

        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Replace Openide\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"fix\" classname=\"org.netbeans.nbbuild.FixDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "<fix>" +
            "  <replace codenamebase=\"org.openide.util\">" +
            "    <module codenamebase=\"org.openide.util\" spec=\"8.0\" />" +
            "    <module codenamebase=\"org.openide.util.lookup\" spec=\"8.0\" />" +
            "  </replace>" +
            "  <fileset dir=\"" + xml.getParent () + "\">" +
            "    <include name=\"" + xml.getName () + "\" /> " +
            "  </fileset>" +
            "</fix>" +
            "</target>" +
            "</project>"

        );

        String input = readFile (xml);
        execute (f, new String[] { });
        String result = readFile (xml);

        if (result.indexOf ("org.openide.util") == -1) {
            fail ("org.openide.util should be there: " + result);
        }
        if (result.indexOf ("org.openide.util.lookup") == -1) {
            fail ("org.openide.util.lookup should be there: " + result);
        }

        Pattern p = Pattern.compile("^(.*)<dependency>$", Pattern.MULTILINE);
        Matcher m = p.matcher(result);
        assertTrue("Text found", m.find());
        String spaces = m.group(1);

        int cnt = 1;
        while (m.find()) {
            String otherSpaces = m.group(1);
            assertEquals("sames spaces at " + cnt + "\n" + result, spaces, otherSpaces);
            cnt++;
        }

        // Skip common prefix for input + result, assumption: inputfile is 
        // correctly formatted. A license header can contain empty lines this
        // way.
        int common = 0;
        for(int i = 0; i < input.length() && i < result.length(); i++) {
            if(input.charAt(i) == result.charAt(i)) {
                common = i;
            } else {
                break;
            }
        }

        assertEquals("There are three dependencies\n" + result, 3, cnt);
        for (String line : result.substring(common).split("\n")) {
            if (line.trim().length() == 0) {
                fail("No empty lines:\n" + result);
            }
        }
    }
    public void testCanFixXmlWsdlModel() throws Exception {
        java.io.File xml = extractResource("FixDependencies-xml.wsdl.model.xml");

        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Replace Openide\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"fix\" classname=\"org.netbeans.nbbuild.FixDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "<fix>" +
            "  <replace codenamebase=\"org.openide.util\">" +
            "    <module codenamebase=\"org.openide.util\" spec=\"8.0\" />" +
            "    <module codenamebase=\"org.openide.util.lookup\" spec=\"8.0\" />" +
            "  </replace>" +
            "  <fileset dir=\"" + xml.getParent () + "\">" +
            "    <include name=\"" + xml.getName () + "\" /> " +
            "  </fileset>" +
            "</fix>" +
            "</target>" +
            "</project>"

        );

        execute (f, new String[] { });
        String result = readFile (xml);

        if (result.indexOf ("org.openide.util") == -1) {
            fail ("org.openide.util should be there: " + result);
        }
        if (result.indexOf ("org.openide.util.lookup") == -1) {
            fail ("org.openide.util.lookup should be there: " + result);
        }
    }
    public void testCanParseCoreKit () throws Exception {
        java.io.File xml = extractResource("FixDependencies-core.kit.xml");

        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Replace Openide\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"fix\" classname=\"org.netbeans.nbbuild.FixDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "<fix>" +
            "  <replace codenamebase=\"org.openide.util\">" +
            "    <module codenamebase=\"org.openide.util\" spec=\"8.0\" />" +
            "    <module codenamebase=\"org.openide.util.lookup\" spec=\"8.0\" />" +
            "  </replace>" +
            "  <fileset dir=\"" + xml.getParent () + "\">" +
            "    <include name=\"" + xml.getName () + "\" /> " +
            "  </fileset>" +
            "</fix>" +
            "</target>" +
            "</project>"

        );

        String before = readFile(xml);
        execute (f, new String[] { });
        String after = readFile(xml);

        assertEquals("No change", before, after);
    }
    public void testCanParseOpenideUtil () throws Exception {
        java.io.File xml = extractResource("FixDependencies-openide.util.xml");

        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Replace Openide\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"fix\" classname=\"org.netbeans.nbbuild.FixDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "<fix>" +
            "  <replace codenamebase=\"org.openide.util\">" +
            "    <module codenamebase=\"org.openide.util\" spec=\"8.0\" />" +
            "    <module codenamebase=\"org.openide.util.lookup\" spec=\"8.0\" />" +
            "  </replace>" +
            "  <fileset dir=\"" + xml.getParent () + "\">" +
            "    <include name=\"" + xml.getName () + "\" /> " +
            "  </fileset>" +
            "</fix>" +
            "</target>" +
            "</project>"

        );

        String before = readFile(xml);
        execute (f, new String[] { });
        String after = readFile(xml);

        assertEquals("No change", before, after);
    }
    public void testNoModuleDependenciesDoesNotCrash() throws Exception {
        java.io.File xml = extractString(
                "<?xml version='1.0' encoding='UTF-8'?>" +
                "<project xmlns='http://www.netbeans.org/ns/project/1'>" +
                "<type>org.netbeans.modules.apisupport.project</type>" +
                "<configuration>" +
                "<data xmlns='http://www.netbeans.org/ns/nb-module-project/3'>" +
                "<code-name-base>org.netbeans.api.annotations.common</code-name-base>" +
                "<module-dependencies/>" +
                "<public-packages/>" +
                "</data>" +
                "</configuration>" +
                "</project>"
        );

        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Replace Openide\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"fix\" classname=\"org.netbeans.nbbuild.FixDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "<fix>" +
            "  <replace codenamebase=\"org.openide.util\">" +
            "    <module codenamebase=\"org.openide.util\" spec=\"8.0\" />" +
            "    <module codenamebase=\"org.openide.util.lookup\" spec=\"8.0\" />" +
            "  </replace>" +
            "  <fileset dir=\"" + xml.getParent () + "\">" +
            "    <include name=\"" + xml.getName () + "\" /> " +
            "  </fileset>" +
            "</fix>" +
            "</target>" +
            "</project>"

        );

        String before = readFile(xml);
        execute (f, new String[] { });
        String after = readFile(xml);

        assertEquals("No change", before, after);
    }
    public void testReplaceOpenideDepWithSmallerOnes () throws Exception {
        java.io.File xml = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project>" +
            "  <module-dependencies>" +
            "    <dependency>" +
            "        <code-name-base>org.openide</code-name-base>" +
            "        <build-prerequisite/> " +
            "        <compile-dependency/> " +
            "        <run-dependency>" + 
            "            <specification-version>3.17</specification-version> " +
            "        </run-dependency>" + 
            "    </dependency>" +
            "  </module-dependencies>" +
            "</project>"
        );
        
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Replace Openide\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"fix\" classname=\"org.netbeans.nbbuild.FixDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "<fix>" +
            "  <replace codenamebase=\"org.openide\" >" +
            "    <module codenamebase=\"org.openide.util\" spec=\"6.2\" />" +
            "    <module codenamebase=\"org.openide.awt\" spec=\"6.2\" />" +
            "  </replace>" +
            "  <fileset dir=\"" + xml.getParent () + "\">" +
            "    <include name=\"" + xml.getName () + "\" /> " +
            "  </fileset>" +
            "</fix>" +
            "</target>" +
            "</project>"

        );
        execute (f, new String[] { });
        
        String result = readFile (xml);
        
        if (result.indexOf ("org.openide.util") == -1) {
            fail ("org.openide.util should be there: " + result);
        }
        if (result.indexOf ("org.openide.awt") == -1) {
            fail ("org.openide.awt should be there: " + result);
        }
        
        if (result.indexOf ("<specification-version>6.2</specification-version>") == -1) {
            fail ("Spec version must be updated to 6.2: " + result);
        }
    }

    public void testReplaceOpenideUtilWithUtilAndLookup () throws Exception {
        java.io.File xml = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project>" +
            "  <module-dependencies>" +
            "    <dependency>" +
            "        <code-name-base>org.openide.util</code-name-base>" +
            "        <build-prerequisite/> " +
            "        <compile-dependency/> " +
            "        <run-dependency>" +
            "            <specification-version>7.28</specification-version> " +
            "        </run-dependency>" +
            "    </dependency>" +
            "  </module-dependencies>" +
            "</project>"
        );

        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Replace Openide\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"fix\" classname=\"org.netbeans.nbbuild.FixDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "<fix>" +
            "  <replace codenamebase=\"org.openide.util\">" +
            "    <module codenamebase=\"org.openide.util\" spec=\"8.0\" />" +
            "    <module codenamebase=\"org.openide.util.lookup\" spec=\"8.0\" />" +
            "  </replace>" +
            "  <fileset dir=\"" + xml.getParent () + "\">" +
            "    <include name=\"" + xml.getName () + "\" /> " +
            "  </fileset>" +
            "</fix>" +
            "</target>" +
            "</project>"

        );
        execute (f, new String[] { });

        String result = readFile (xml);

        if (result.indexOf ("org.openide.util") == -1) {
            fail ("org.openide.util should be there: " + result);
        }
        if (result.indexOf ("org.openide.util.lookup") == -1) {
            fail ("org.openide.util.lookup should be there: " + result);
        }

        int where;
        if ((where = result.indexOf ("<specification-version>8.0</specification-version>")) == -1) {
            fail ("Spec version must be updated to 8.0: " + result);
        }
        if (result.indexOf("<specification-version>8.0</specification-version>", where + 1) == -1) {
            fail ("Snd Spec version must be updated to 8.0: " + result);
        }
    }
    public void testDontReplaceNewerVersionOfItself() throws Exception {
        doDontReplaceNewerVersionOfItself("8.1");
    }
    public void testDontReplaceNewerVersionOfItself9() throws Exception {
        doDontReplaceNewerVersionOfItself("9.0");
    }

    private void doDontReplaceNewerVersionOfItself(String version) throws Exception {
        java.io.File xml = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project>" +
            "  <module-dependencies>" +
            "    <dependency>" +
            "        <code-name-base>org.openide.util</code-name-base>" +
            "        <build-prerequisite/> " +
            "        <compile-dependency/> " +
            "        <run-dependency>" +
            "            <specification-version>" + version + "</specification-version> " +
            "        </run-dependency>" +
            "    </dependency>" +
            "  </module-dependencies>" +
            "</project>"
        );

        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Replace Openide\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"fix\" classname=\"org.netbeans.nbbuild.FixDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "<fix>" +
            "  <replace codenamebase=\"org.openide.util\">" +
            "    <module codenamebase=\"org.openide.util\" spec=\"8.0\" />" +
            "    <module codenamebase=\"org.openide.util.lookup\" spec=\"8.0\" />" +
            "  </replace>" +
            "  <fileset dir=\"" + xml.getParent () + "\">" +
            "    <include name=\"" + xml.getName () + "\" /> " +
            "  </fileset>" +
            "</fix>" +
            "</target>" +
            "</project>"

        );
        execute (f, new String[] { });

        String result = readFile (xml);

        if (result.indexOf ("org.openide.util") == -1) {
            fail ("org.openide.util should be there: " + result);
        }
        if (result.indexOf ("org.openide.util.lookup") >= 0) {
            fail ("org.openide.util.lookup should not be there: " + result);
        }

        if (result.indexOf ("<specification-version>" + version + "</specification-version>") == -1) {
            fail ("Spec version stays at updated to kept: " + result);
        }
    }
    
    
    public void testVerificationOfRemovedDependencies () throws Exception {
        java.io.File xml = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project>" +
            "  <module-dependencies>" +
            "    <dependency>" +
            "        <code-name-base>org.openide.keep1</code-name-base>" +
            "        <build-prerequisite/> " +
            "        <compile-dependency/> " +
            "        <run-dependency>" + 
            "            <specification-version>6.2</specification-version> " +
            "        </run-dependency>" + 
            "    </dependency>" +
            "    <dependency>" +
            "        <code-name-base>org.openide.remove</code-name-base>" +
            "        <build-prerequisite/> " +
            "        <compile-dependency/> " +
            "        <run-dependency>" + 
            "            <specification-version>6.2</specification-version> " +
            "        </run-dependency>" + 
            "    </dependency>" +
            "    <dependency>" +
            "        <code-name-base>org.openide.keep2</code-name-base>" +
            "        <build-prerequisite/> " +
            "        <compile-dependency/> " +
            "        <run-dependency>" + 
            "            <specification-version>6.2</specification-version> " +
            "        </run-dependency>" + 
            "    </dependency>" +
            "  </module-dependencies>" +
            "</project>"
        );
        
        java.io.File out = extractString ("");
        
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Replace Openide\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"fix\" classname=\"org.netbeans.nbbuild.FixDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "<fix antfile=\"${buildscript}\" buildtarget=\"verify\" cleantarget=\"clean\" >" +
            "  <replace codenamebase=\"org.openide\" >" +
            "    <module codenamebase=\"org.openide.util\" spec=\"6.2\" />" +
            "    <module codenamebase=\"org.openide.awt\" spec=\"6.2\" />" +
            "  </replace>" +
            "  <fileset dir=\"" + xml.getParent () + "\">" +
            "    <include name=\"" + xml.getName () + "\" /> " +
            "  </fileset>" +
            "</fix>" +
            "</target>" +
            "" +
            "<target name=\"verify\" >" +
            "  <echo message=\"v\" file=\"" + out.getPath () + "\" append='true' />" +
            "  <loadfile property=\"p\" srcFile=\"" + xml.getPath () + "\" />" +
            "  <condition property=\"remove\" >" +
            "    <and>" +    
            "      <not>" + 
            "        <and>" +    
            "          <contains string=\"${p}\" substring=\"org.openide.keep1\"  />" +
            "          <contains string=\"${p}\" substring=\"org.openide.keep2\"  />" +
            "        </and>" + 
            "      </not>" + 
            "      <contains string=\"${p}\" substring=\"org.openide.remove\"  />" +
            "    </and>" +
            "  </condition>" +
            // fail if there is org.openide.remove and at least one 
            // of org.openide.keep is missing
            "  <fail if=\"remove\" /> " + 
            "</target>" +
            "<target name=\"clean\" >" +
            "  <echo message=\"c\" file=\"" + out.getPath () + "\" append='true' />" +
            "</target>" +
            "</project>"

        );
        execute (f, new String[] { "-Dbuildscript=" + f.getPath () });
        
        String result = readFile (xml);
        
        if (result.indexOf ("org.openide.keep") == -1) {
            fail ("org.openide.keep should be there: " + result);
        }
        if (result.indexOf ("org.openide.remove") != -1) {
            fail ("org.openide.remove should not be there: " + result);
        }

        String written = readFile (out);
        assertEquals ("First we do clean, test verify, then clean and verify three times as there are three <dependency> tags"
                , "cvcvcvcvcv", written);
    }

    public void testBrokenCoreSettingsReplacement () throws Exception {
        
        String projectXML = 
"<?xml version='1.0' encoding='UTF-8'?>\n" +
"<!--\n" +
"                CDDL Notice\n" +
"\n" +
"The Original Code is NetBeans. The Initial Developer of the Original\n" +
"Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun\n" +
"Microsystems, Inc. All Rights Reserved.\n" +
"-->\n" +
"<project xmlns='http://www.netbeans.org/ns/project/1'>\n" +
    "<type>org.netbeans.modules.apisupport.project</type>\n" +
    "<configuration>\n" +
        "<data xmlns='http://www.netbeans.org/ns/nb-module-project/2'>\n" +
            "<code-name-base>org.netbeans.modules.settings</code-name-base>\n" +
            "<module-dependencies>\n" +
                "<dependency>\n" +
                    "<code-name-base>org.openide</code-name-base>\n" +
                    "<build-prerequisite/>\n" +
                    "<compile-dependency/>\n" +
                    "<run-dependency>\n" +
                        "<release-version>1</release-version>\n" +
                        "<specification-version>3.17</specification-version>\n" +
                    "</run-dependency>\n" +
                "</dependency>\n" +
                "<dependency>\n" +
                    "<code-name-base>org.openide.loaders</code-name-base>\n" +
                    "<build-prerequisite/>\n" +
                    "<compile-dependency/>\n" +
                    "<run-dependency/>\n" +
                "</dependency>\n" +
            "</module-dependencies>\n" +
            "<public-packages>\n" +
                "<package>org.netbeans.spi.settings</package>\n" +
            "</public-packages>\n" +
            "<javadoc/>\n" +
        "</data>\n" +
    "</configuration>\n" +
"</project>\n";
        
        
        
        java.io.File xml = extractString (projectXML);
        
        
        java.io.File out = extractString ("");
        
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Replace Openide\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"fix\" classname=\"org.netbeans.nbbuild.FixDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "<fix >" +
            "  <replace codenamebase='org.openide'>\n" +  
            "   <module codenamebase='org.openide.filesystems' spec='6.2'/>\n" +
            "   <module codenamebase='org.openide.util' spec='6.2'/>\n" +
            "   <module codenamebase='org.openide.modules' spec='6.2'/>\n" +
            "   <module codenamebase='org.openide.nodes' spec='6.2'/>\n" +
            "   <module codenamebase='org.openide.explorer' spec='6.2'/>\n" +
            "   <module codenamebase='org.openide.awt' spec='6.2'/>\n" +
            "   <module codenamebase='org.openide.dialogs' spec='6.2'/>\n" +
            "   <module codenamebase='org.openide.compat' spec='6.2'/>\n" +
            "   <module codenamebase='org.openide.options' spec='6.2'/>\n" +
            "   <module codenamebase='org.openide.windows' spec='6.2'/>\n" +
            "   <module codenamebase='org.openide.text' spec='6.2'/>\n" +
            "   <module codenamebase='org.openide.actions' spec='6.2'/>\n" +
            "   <module codenamebase='org.openide.loaders' spec='6.2'/>\n" +
            "  </replace>\n" +
            "  <fileset dir=\"" + xml.getParent () + "\">" +
            "    <include name=\"" + xml.getName () + "\" /> " +
            "  </fileset>" +
            "</fix>" +
            "</target>" +
            "" +
            "<target name=\"verify\" >" +
            "  <echo message=\"v\" file=\"" + out.getPath () + "\" append='true' />" +
            "  <loadfile property=\"p\" srcFile=\"" + xml.getPath () + "\" />" +
            "</target>" +
            "<target name=\"clean\" >" +
            "  <echo message=\"c\" file=\"" + out.getPath () + "\" append='true' />" +
            "</target>" +
            "</project>"

        );
        org.w3c.dom.Document doc;
        doc = javax.xml.parsers.DocumentBuilderFactory.newInstance ().newDocumentBuilder ().parse (xml);
        assertNotNull ("Originally can be parsed", doc);
        
        
        execute (f, new String[] { "-Dbuildscript=" + f.getPath () });
        
        doc = javax.xml.parsers.DocumentBuilderFactory.newInstance ().newDocumentBuilder ().parse (xml);
        
        assertNotNull ("Still can be parsed", doc);
        
        String r = readFile (xml);
        assertEquals ("No release version used as modules do not have it", -1, r.indexOf ("release-version"));
        
        
        int idx = r.indexOf ("<code-name-base>org.openide.loaders</code-name-base>");
        if (idx == -1) {
            fail ("One dep on loaders should be there: " + r);
        }
        
        assertEquals ("No next loader dep", -1, r.indexOf ("<code-name-base>org.openide.loaders</code-name-base>", idx + 10));
    }

    public void testPropertiesAreNotInfluencedByPreviousExecution () throws Exception {
        java.io.File xml = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project>" +
            "  <module-dependencies>" +
            "    <dependency>" +
            "        <code-name-base>org.openide.keep1</code-name-base>" +
            "        <build-prerequisite/> " +
            "        <compile-dependency/> " +
            "        <run-dependency>" + 
            "            <specification-version>6.2</specification-version> " +
            "        </run-dependency>" + 
            "    </dependency>" +
            "    <dependency>" +
            "        <code-name-base>org.openide.remove</code-name-base>" +
            "        <build-prerequisite/> " +
            "        <compile-dependency/> " +
            "        <run-dependency>" + 
            "            <specification-version>6.2</specification-version> " +
            "        </run-dependency>" + 
            "    </dependency>" +
            "    <dependency>" +
            "        <code-name-base>org.openide.keep2</code-name-base>" +
            "        <build-prerequisite/> " +
            "        <compile-dependency/> " +
            "        <run-dependency>" + 
            "            <specification-version>6.2</specification-version> " +
            "        </run-dependency>" + 
            "    </dependency>" +
            "  </module-dependencies>" +
            "</project>"
        );
        
        java.io.File out = extractString ("");
        
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Separate namespaces\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"fix\" classname=\"org.netbeans.nbbuild.FixDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "<fix antfile=\"${buildscript}\" buildtarget=\"verify\" cleantarget='verify'>" +
            "  <fileset dir=\"" + xml.getParent () + "\">" +
            "    <include name=\"" + xml.getName () + "\" /> " +
            "  </fileset>" +
            "</fix>" +
            "</target>" +
            "" +
            "<target name=\"verify\" >" +
            "  <fail if=\"remove\" /> " + 
            "  <property name='remove' value='some' />" +
            "  <fail unless=\"remove\" /> " + 
            "  <echo message=\"v\" file=\"" + out.getPath () + "\" append='true' />" +
            "</target>" +
            "</project>"

        );
        execute (f, new String[] { "-Dbuildscript=" + f.getPath () });
        
        String result = readFile (xml);

        String written = readFile (out);
        assertEquals ("The property remove is never set", "vvvvvvvvvv", written);
    }

    
  public void testOnlyCompileTimeDependenciesCanBeRemoved () throws Exception {
        java.io.File xml = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project>" +
            "  <module-dependencies>" +
            "    <dependency>" +
            "        <code-name-base>org.openide.keep1</code-name-base>" +
            "        <build-prerequisite/> " +
            "        <compile-dependency/> " +
            "        <run-dependency>" + 
            "            <specification-version>6.2</specification-version> " +
            "        </run-dependency>" + 
            "    </dependency>" +
            "    <dependency>" +
            "        <code-name-base>org.openide.remove</code-name-base>" +
            "        <build-prerequisite/> " +
// This changes the meaning:            "        <compile-dependency/> " +
            "        <run-dependency>" + 
            "            <specification-version>6.2</specification-version> " +
            "        </run-dependency>" + 
            "    </dependency>" +
            "    <dependency>" +
            "        <code-name-base>org.openide.keep2</code-name-base>" +
            "        <build-prerequisite/> " +
            "        <compile-dependency/> " +
            "        <run-dependency>" + 
            "            <specification-version>6.2</specification-version> " +
            "        </run-dependency>" + 
            "    </dependency>" +
            "  </module-dependencies>" +
            "</project>"
        );
        
        java.io.File out = extractString ("");
        
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Replace Openide\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"fix\" classname=\"org.netbeans.nbbuild.FixDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "<fix antfile=\"${buildscript}\" buildtarget=\"verify\" cleantarget=\"clean\" >" +
            "  <replace codenamebase=\"org.openide\" >" +
            "    <module codenamebase=\"org.openide.util\" spec=\"6.2\" />" +
            "    <module codenamebase=\"org.openide.awt\" spec=\"6.2\" />" +
            "  </replace>" +
            "  <fileset dir=\"" + xml.getParent () + "\">" +
            "    <include name=\"" + xml.getName () + "\" /> " +
            "  </fileset>" +
            "</fix>" +
            "</target>" +
            "" +
            "<target name=\"verify\" >" +
            "  <echo message=\"v\" file=\"" + out.getPath () + "\" append='true' />" +
            "  <loadfile property=\"p\" srcFile=\"" + xml.getPath () + "\" />" +
            "  <condition property=\"remove\" >" +
            "    <and>" +    
            "      <not>" + 
            "        <and>" +    
            "          <contains string=\"${p}\" substring=\"org.openide.keep1\"  />" +
            "          <contains string=\"${p}\" substring=\"org.openide.keep2\"  />" +
            "        </and>" + 
            "      </not>" + 
            "      <contains string=\"${p}\" substring=\"org.openide.remove\"  />" +
            "    </and>" +
            "  </condition>" +
            // fail if there is org.openide.remove and at least one 
            // of org.openide.keep is missing
            "  <fail if=\"remove\" /> " + 
            "</target>" +
            "<target name=\"clean\" >" +
            "  <echo message=\"c\" file=\"" + out.getPath () + "\" append='true' />" +
            "</target>" +
            "</project>"

        );
        execute (f, new String[] { "-Dbuildscript=" + f.getPath () });
        
        String result = readFile (xml);
        
        if (result.indexOf ("org.openide.keep") == -1) {
            fail ("org.openide.keep should be there: " + result);
        }
        if (result.indexOf ("org.openide.remove") == -1) {
            fail ("org.openide.remove should be there: " + result);
        }

        String written = readFile (out);
        assertEquals ("The remove dependency is not even asked for"
                , "cvcvccvcv", written);
    }
    
  public void testRuntimeDepOnOpenideIsSpecial () throws Exception {
        java.io.File xml = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project>\n" +
            "  <module-dependencies>\n" +
            "    <dependency>\n" +
            "        <code-name-base>org.openide</code-name-base>\n" +
            "        <run-dependency>\n" +
            "            <specification-version>5.1</specification-version>\n" +
            "        </run-dependency>\n" +
            "    </dependency>\n" +
            "  </module-dependencies>\n" +
            "</project>\n"
        );
        
        java.io.File out = extractString ("");
        
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Replace Openide\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"fix\" classname=\"org.netbeans.nbbuild.FixDependencies\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "<fix antfile=\"${buildscript}\" buildtarget=\"verify\" cleantarget=\"clean\" >" +
            "  <replace codenamebase=\"org.openide\" addcompiletime='true' >" +
            "    <module codenamebase=\"org.openide.util\" spec=\"6.2\" />" +
            "    <module codenamebase=\"org.openide.awt\" spec=\"6.2\" />" +
            "  </replace>" +
            "  <fileset dir=\"" + xml.getParent () + "\">" +
            "    <include name=\"" + xml.getName () + "\" /> " +
            "  </fileset>" +
            "</fix>" +
            "</target>" +
            "" +
            "<target name=\"verify\" >" + // always succeed
            "</target>" +
            "<target name=\"clean\" >" +
            "  <echo message=\"c\" file=\"" + out.getPath () + "\" append='true' />" +
            "</target>" +
            "</project>"

        );
        execute (f, new String[] { "-Dbuildscript=" + f.getPath () });
        
        String result = readFile (xml);
        
        if (result.indexOf ("org.openide") > -1) {
            fail ("No org.openide should be there: " + result);
        }
        if (result.indexOf ("<dependency>") > -1) {
            fail ("No dependency should be there: " + result);
        }
    }

}
