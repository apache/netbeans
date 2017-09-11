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
package org.netbeans.nbbuild;

import java.io.FileOutputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.io.File;
import java.io.IOException;


/** Check behaviour of ModuleSelector.
 *
 * @author Jaroslav Tulach
 */
public class CreateModuleXMLTest extends TestBase {
    
    public CreateModuleXMLTest(String testName) {
        super(testName);
    }

    public void testIncludesAllModulesByDefault() throws Exception {
        Manifest m = createManifest ();
        m.getMainAttributes().putValue("OpenIDE-Module", "org.my.module");
        File aModule = generateJar(new String[0], m);
        
        File output = new File(getWorkDir(), "output");
        
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"createmodulexml\" classname=\"org.netbeans.nbbuild.CreateModuleXML\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" + 
            "  <createmodulexml xmldir='" + output + "' >" +
            "    <hidden dir='" + aModule.getParent() + "' >" +
            "      <include name='" + aModule.getName() + "' />" +
            "    </hidden>" +
            "  </createmodulexml>" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertTrue ("Output exists", output.exists ());
        assertTrue ("Output directory created", output.isDirectory());
        
        String[] files = output.list();
        assertEquals("It one file", 1, files.length);
        assertEquals("Its name reflects the code name of the module", "org-my-module.xml_hidden", files[0]);
        
    }

    public void testGeneratesDataForDisabledModule() throws Exception {
        Manifest m = createManifest ();
        m.getMainAttributes().putValue("OpenIDE-Module", "org.my.module");
        File aModule = generateJar(new String[0], m);

        File output = new File(getWorkDir(), "output");

        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"createmodulexml\" classname=\"org.netbeans.nbbuild.CreateModuleXML\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" +
            "  <createmodulexml xmldir='" + output + "' >" +
            "    <disabled dir='" + aModule.getParent() + "' >" +
            "      <include name='" + aModule.getName() + "' />" +
            "    </disabled>" +
            "  </createmodulexml>" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });

        assertTrue ("Output exists", output.exists ());
        assertTrue ("Output directory created", output.isDirectory());

        String[] files = output.list();
        assertEquals("It one file", 1, files.length);
        assertEquals("Its name reflects the code name of the module", "org-my-module.xml", files[0]);

    }
    
    public void testStartLevelFailsForNormalModules() throws Exception {
        Manifest m = createManifest ();
        m.getMainAttributes().putValue("OpenIDE-Module", "org.my.module");
        File aModule = generateJar(new String[0], m);

        File output = new File(getWorkDir(), "output");

        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"createmodulexml\" classname=\"org.netbeans.nbbuild.CreateModuleXML\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' startlevel='3'/>" +
            "  <createmodulexml xmldir='" + output + "' >" +
            "    <enabled dir='" + aModule.getParent() + "' >" +
            "      <include name='" + aModule.getName() + "' />" +
            "    </enabled>" +
            "  </createmodulexml>" +
            "</target>" +
            "</project>"
        );
        try {
            execute(f, new String[] { "-verbose" });
        } catch (ExecutionError ex) {
            // OK
            return;
        }
        fail("Execution should fail");

    }
    public void testStartLevelIsIgnoredForNormalModulesWhenRequested() throws Exception {
        Manifest m = createManifest ();
        m.getMainAttributes().putValue("OpenIDE-Module", "org.my.module");
        File aModule = generateJar(new String[0], m);

        File output = new File(getWorkDir(), "output");

        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"createmodulexml\" classname=\"org.netbeans.nbbuild.CreateModuleXML\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "'/>" +
            "  <createmodulexml xmldir='" + output + "' startlevel='3' strictcheck='false'>" +
            "    <enabled dir='" + aModule.getParent() + "' >" +
            "      <include name='" + aModule.getName() + "' />" +
            "    </enabled>" +
            "  </createmodulexml>" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });

        assertTrue ("Output exists", output.exists ());
        assertTrue ("Output directory created", output.isDirectory());

        File[] files = output.listFiles();
        assertEquals("It one file", 1, files.length);
        assertEquals("Its name reflects the code name of the module", "org-my-module.xml", files[0].getName());
        
        String content = readFile(files[0]);
        assertFalse("startlevel tag is not there: " + content, content.contains("\"startlevel\""));
    }
    
    public void testStartLevelOKForBundles() throws Exception {
        Manifest m = createManifest ();
        m.getMainAttributes().putValue("Bundle-SymbolicName", "org.my.module");
        File aModule = generateJar(new String[0], m);

        File output = new File(getWorkDir(), "output");

        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"createmodulexml\" classname=\"org.netbeans.nbbuild.CreateModuleXML\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" +
            "  <createmodulexml xmldir='" + output + "' startlevel='3'>" +
            "    <enabled dir='" + aModule.getParent() + "' >" +
            "      <include name='" + aModule.getName() + "' />" +
            "    </enabled>" +
            "  </createmodulexml>" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });

        assertTrue ("Output exists", output.exists ());
        assertTrue ("Output directory created", output.isDirectory());

        File[] files = output.listFiles();
        assertEquals("It one file", 1, files.length);
        assertEquals("Its name reflects the code name of the module", "org-my-module.xml", files[0].getName());
        
        String content = readFile(files[0]);
        assertTrue("startlevel tag expected: " + content, content.contains("\"startlevel\""));

    }
    public void testEmptyStartLevelIsOKForNormalModules() throws Exception {
        Manifest m = createManifest ();
        m.getMainAttributes().putValue("OpenIDE-Module", "org.my.module");
        File aModule = generateJar(new String[0], m);

        File output = new File(getWorkDir(), "output");

        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"createmodulexml\" classname=\"org.netbeans.nbbuild.CreateModuleXML\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "'/>" +
            "  <createmodulexml xmldir='" + output + "' startlevel=''>" +
            "    <enabled dir='" + aModule.getParent() + "' >" +
            "      <include name='" + aModule.getName() + "' />" +
            "    </enabled>" +
            "  </createmodulexml>" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });

        assertTrue ("Output exists", output.exists ());
        assertTrue ("Output directory created", output.isDirectory());

        String[] files = output.list();
        assertEquals("It one file", 1, files.length);
        assertEquals("Its name reflects the code name of the module", "org-my-module.xml", files[0]);

    }

    public void testGenerateUpdateTrackingMode() throws Exception {
        Manifest m = createManifest ();
        m.getMainAttributes().putValue("OpenIDE-Module", "org.my.module");
        m.getMainAttributes().putValue("OpenIDE-Module-Specification-Version", "10.15");
        File aModule = generateJar(new String[0], m);

        File output = new File(getWorkDir(), "output");
        File tracking = new File(getWorkDir(), "update_tracking");

        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"createmodulexml\" classname=\"org.netbeans.nbbuild.CreateModuleXML\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" +
            "  <createmodulexml xmldir='" + output + "' updatetrackingroot='" + getWorkDir() +"' >" +
            "    <disabled dir='" + aModule.getParent() + "' >" +
            "      <include name='" + aModule.getName() + "' />" +
            "    </disabled>" +
            "  </createmodulexml>" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });

        assertTrue ("Output exists", output.exists ());
        assertTrue ("Output directory created", output.isDirectory());

        String[] files = output.list();
        assertEquals("It one file", 1, files.length);
        assertEquals("Its name reflects the code name of the module", "org-my-module.xml", files[0]);

        assertTrue ("Update tracking exists", tracking.exists ());
        assertTrue ("Update tracking directory created", tracking.isDirectory());

        File[] arr = tracking.listFiles();
        assertEquals("It one file", 1, arr.length);
        assertEquals("Its name reflects the code name of the module", "org-my-module.xml", arr[0].getName());

        String conf = readFile(arr[0]);

        {
            int first = conf.indexOf("<file");
            int snd = conf.indexOf("<file", first + 10);
            if (snd == -1) {
                fail("There shall be two <file/> sections:\n" + conf);
            }
        }

        {
            int first = conf.indexOf("name=\"modules/0.jar");
            int snd = conf.indexOf("name=\"output/org-my-moodule.xml");
            if (snd == -1 && first == -1) {
                fail("Paths shall be relative:\n" + conf);
            }
        }
    }
    
    
    private File createNewJarFile() throws IOException {
        File dir = new File(this.getWorkDir(), "modules");
        dir.mkdirs();
        
        int i = 0;
        for (;;) {
            File f = new File (dir, i++ + ".jar");
            if (!f.exists()) {
                return f;
            }
        }
    }
    
    protected final File generateJar (String[] content, Manifest manifest) throws IOException {
        File f = createNewJarFile ();
        
        JarOutputStream os;
        if (manifest != null) {
            os = new JarOutputStream (new FileOutputStream (f), manifest);
        } else {
            os = new JarOutputStream (new FileOutputStream (f));
        }
        
        for (int i = 0; i < content.length; i++) {
            os.putNextEntry(new JarEntry (content[i]));
            os.closeEntry();
        }
        os.closeEntry ();
        os.close();
        
        return f;
    }
    
}
