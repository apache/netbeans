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
