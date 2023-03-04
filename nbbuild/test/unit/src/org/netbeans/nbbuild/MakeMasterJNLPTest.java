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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/** Is generation of Jnlp files correct?
 *
 * @author Jaroslav Tulach
 */
public class MakeMasterJNLPTest extends TestBase {
    public MakeMasterJNLPTest (String name) {
        super (name);
    }
    
    public void testOSGiModule() throws Exception {
        int cnt = 3;
        Manifest m;

        m = createManifest ();
        m.getMainAttributes ().putValue ("Bundle-SymbolicName", "org.my.module");
        File simpleJar = generateJar (new String[0], m);

        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "org.second.module/3");
        File secondJar = generateJar (new String[0], m);

        File parent = simpleJar.getParentFile ();
        assertEquals("They are in the same folder", parent, secondJar.getParentFile());

        File output = new File(parent, "output");

        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"jnlp\" classname=\"org.netbeans.nbbuild.MakeMasterJNLP\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" +
            "  <jnlp dir='" + output + "'  >" +
            "    <modules dir='" + parent + "' >" +
            "      <include name='" + simpleJar.getName() + "' />" +
            "      <include name='" + secondJar.getName() + "' />" +
            "    </modules>" +
            "  </jnlp>" +
            "</target>" +
            "</project>"
        );
        while (cnt-- > 0) {
            execute (f, new String[] { "-verbose" });
        }

        assertTrue ("Output exists", output.exists ());
        assertTrue ("Output directory created", output.isDirectory());

        String[] files = output.list();
        assertEquals("It has two files", 2, files.length);

        java.util.Arrays.sort(files);

        assertEquals("The res1 file: " + files[0], "org-my-module.ref", files[0]);
        assertEquals("The res2 file: "+ files[1], "org-second-module.ref", files[1]);

        File r1 = new File(output, "org-my-module.ref");
        String res1 = readFile (r1);

        File r2 = new File(output, "org-second-module.ref");
        String res2 = readFile (r2);

        assertExt(res1, "org.my.module");
        assertExt(res2, "org.second.module");
    }
    
    public void testGenerateReferenceFilesOnce() throws Exception {
        doGenerateReferenceFiles(1);
    }
    public void testGenerateReferenceFilesThrice() throws Exception {
        doGenerateReferenceFiles(3);
    }
    
    private void doGenerateReferenceFiles(int cnt) throws Exception {
        Manifest m;
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "org.my.module/3");
        File simpleJar = generateJar (new String[0], m);

        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "org.second.module/3");
        File secondJar = generateJar (new String[0], m);
        
        File parent = simpleJar.getParentFile ();
        assertEquals("They are in the same folder", parent, secondJar.getParentFile());
        
        File output = new File(parent, "output");
        
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"jnlp\" classname=\"org.netbeans.nbbuild.MakeMasterJNLP\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" + 
            "  <jnlp dir='" + output + "'  >" +
            "    <modules dir='" + parent + "' >" +
            "      <include name='" + simpleJar.getName() + "' />" +
            "      <include name='" + secondJar.getName() + "' />" +
            "    </modules>" +
            "  </jnlp>" +
            "</target>" +
            "</project>"
        );
        while (cnt-- > 0) {
            execute (f, new String[] { "-verbose" });
        }
        
        assertTrue ("Output exists", output.exists ());
        assertTrue ("Output directory created", output.isDirectory());
        
        String[] files = output.list();
        assertEquals("It has two files", 2, files.length);

        java.util.Arrays.sort(files);
        
        assertEquals("The res1 file: " + files[0], "org-my-module.ref", files[0]);
        assertEquals("The res2 file: "+ files[1], "org-second-module.ref", files[1]);
        
        File r1 = new File(output, "org-my-module.ref");
        String res1 = readFile (r1);

        File r2 = new File(output, "org-second-module.ref");
        String res2 = readFile (r2);
        
        assertExt(res1, "org.my.module");
        assertExt(res2, "org.second.module");
    }
    
    private static void assertExt(String res, String module) {
        int ext = res.indexOf("<extension");
        if (ext == -1) {
            fail ("<extension tag shall start there: " + res);
        }
        
        assertEquals("Just one extension tag", -1, res.indexOf("<extension", ext + 1));

        int cnb = res.indexOf(module);
        if (cnb == -1) {
            fail("Cnb has to be there: " + module + " but is " + res);
        }
        assertEquals("Just one cnb", -1, res.indexOf(module, cnb + 1));
        
        String dashcnb = module.replace('.', '-');
        
        int dcnb = res.indexOf(dashcnb);
        if (dcnb == -1) {
            fail("Dash Cnb has to be there: " + dashcnb + " but is " + res);
        }
        assertEquals("Just one dash cnb", -1, res.indexOf(dashcnb, dcnb + 1));
    }

    private File createNewJarFile() throws IOException {
        int i = 0;
        for (;;) {
            File f = new File (this.getWorkDir(), i++ + ".jar");
            if (!f.exists()) {
                return f;
            }
        }
    }
    
    protected final File generateJar (String[] content, Manifest manifest) throws IOException {
        File f = createNewJarFile ();
        
        try (JarOutputStream os = new JarOutputStream (new FileOutputStream (f), manifest)) {
            for (int i = 0; i < content.length; i++) {
                os.putNextEntry(new JarEntry (content[i]));
                os.closeEntry();
            }
            os.closeEntry ();
        }
        
        return f;
    }

}
