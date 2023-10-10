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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.apache.tools.ant.Project;
import org.netbeans.junit.RandomlyFails;

/**
 * @author Jaroslav Tulach
 */
public class MakeNBMTest extends TestBase {
    public MakeNBMTest (String name) {
        super (name);
    }
    
    @RandomlyFails // NB-Core-Build #2570
    public void testGenerateNBMForSimpleModule() throws Exception {
        Manifest m;
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "org.my.module/3");
        File simpleJar = generateJar (new String[0], m);

        File parent = simpleJar.getParentFile ();
        File output = new File(parent, "output");
        File ks = generateKeystore("nbm", "netbeans-test");
        if (ks == null) {
            return;
        }
        
        File ut = new File (new File(getWorkDir(), "update_tracking"), "org-my-module.xml");
        ut.getParentFile().mkdirs();
        try (FileWriter w = new FileWriter(ut)) {
            String UTfile =
                    "<?xml version='1.0' encoding='UTF-8'?>" +
                    "<module codename='org.netbeans.modules.autoupdate/1'>" +
                    "    <module_version install_time='1136503038669' last='true' origin='installer' specification_version='2.16.1'>" +
                    "        <file crc='3405032071' name='modules/" + simpleJar.getName() + "'/>" +
                    "    </module_version>" +
                    "</module>";
            w.write(UTfile);
        }
        
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"makenbm\" classname=\"org.netbeans.nbbuild.MakeNBM\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" + 
            "  <makenbm file='" + output + "/x.nbm'" +
            "           productdir='" + getWorkDir() + "'" +
            "           module='modules/" + simpleJar.getName() + "'" +
            "           homepage='http://www.homepage.org'" +
            "           distribution='distro'" +
            "           needsrestart='false'" +
            "           global='false'" +
            "           releasedate='today'" +
            "           moduleauthor='test'>" +
            "     <license file='" + simpleJar + "'/>" +
            "     <signature keystore='" + ks + "' storepass='netbeans-test' alias='nbm'/>" +
            "  </makenbm>" +
      //      "  <fail if='do.fail'/>" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertTrue ("Output exists", output.exists ());
        assertTrue ("Output directory created", output.isDirectory());
        
        String[] files = output.list();
        assertEquals("It has the nbm file", 1, files.length);
        
        if (!files[0].endsWith("x.nbm")) {
            fail("Not the right one: " + files[0]);
        }

        long time = output.listFiles()[0].lastModified();
        
        // wait a while so the NBM file has different timestamp
        // if recreated
        Thread.sleep(1300);

        // execute once again
        execute (f, new String[] { "-debug", "-Ddo.fail=true"});
        
        long newTime = output.listFiles()[0].lastModified();
        
        assertEquals("The file has not been modified:\n" + getStdOut(), time, newTime);
        
        
        CHECK_SIGNED: {
            File jar = output.listFiles()[0];
            JarFile signed = new JarFile(jar);
            Enumeration<JarEntry> it = signed.entries();
            while (it.hasMoreElements()) {
                JarEntry entry = it.nextElement();
                if (entry.getName().endsWith(".SF")) {
                    break CHECK_SIGNED;
                }
            }
            fail ("File does not seem to be signed: " + jar);
        }
        
    }
    
    private File createNewJarFile(String prefix) throws IOException {
        if (prefix == null) {
            prefix = "modules";
        }
        String ss = prefix.substring(prefix.length()-1);
                
        File dir = new File(this.getWorkDir(), prefix);
        dir.mkdirs();
        
        int i = 0;
        for (;;) {
            File f = new File (dir, ss + i++ + ".jar");
            if (!f.exists ()) {
                return f;
            }
        }
    }
    
    protected final File generateJar (String[] content, Manifest manifest) throws IOException {
        return generateJar(null, content, manifest, null);
    }
    
    protected final File generateJar (String prefix, String[] content, Manifest manifest, Properties props) throws IOException {
        File f = createNewJarFile (prefix);
        
        if (props != null) {
            manifest.getMainAttributes().putValue("OpenIDE-Module-Localizing-Bundle", "some/fake/prop/name/Bundle.properties");
        }
        
        try (JarOutputStream os = new JarOutputStream (new FileOutputStream (f), manifest)) {
            if (props != null) {
                os.putNextEntry(new JarEntry("some/fake/prop/name/Bundle.properties"));
                props.store(os, "# properties for the module");
                os.closeEntry();
            }
            
            
            for (int i = 0; i < content.length; i++) {
                os.putNextEntry(new JarEntry (content[i]));
                os.closeEntry();
            }
            os.closeEntry ();
        }
        
        return f;
    }
    
    private File generateKeystore(String alias, String password) throws Exception {
        File where = new File(getWorkDir(), "key.ks");
        
        String script = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Generate Keystore\" basedir=\".\" default=\"all\" >" +
            "<target name=\"all\" >" +
            "<genkey \n" +
              "alias='" + alias + "' \n" +
              "keystore='" + where + "' \n" +
              "storepass='" + password + "' \n" +
              "dname='CN=A NetBeans Friend, OU=NetBeans, O=netbeans.org, C=US' \n" +
            "/>\n" +
            "</target></project>\n";
        
        java.io.File f = extractString (script);
        try {
            execute (f, new String[] { });
        } catch (ExecutionError err) {
            if (err.getMessage().indexOf("java.security.ProviderException") != -1) {
                // common error on Sun OS:
                // org.netbeans.nbbuild.PublicPackagesInProjectizedXMLTest$ExecutionError: Execution has to finish without problems was: 1
                // Output: Buildfile: /space/test4u2/testrun/work/tmpdir/res310.xml
                //
                // all:
                // [genkey] Generating Key for nbm
                // [genkey] keytool error: java.security.ProviderException: sun.security.pkcs11.wrapper.PKCS11Exception: CKR_KEY_SIZE_RANGE
                return null;
            }
        }
        
        return where;
    }

    public void testOmitExternalFiles() throws Exception { // #195041
        Project p = new Project();
        p.init();
        MakeNBM task = new MakeNBM();
        task.setProject(p);
        clearWorkDir();
        File w = getWorkDir();
        File cluster = new File(w, "cluster");
        task.setProductDir(cluster);
        File modules = new File(cluster, "modules");
        if (!modules.mkdirs()) {
            throw new IOException();
        }
        Manifest mani = new Manifest();
        mani.getMainAttributes().putValue("Manifest-Version", "1.0");
        mani.getMainAttributes().putValue("OpenIDE-Module", "mod");
        OutputStream os = new FileOutputStream(new File(modules, "mod.jar"));
        try {
            try (JarOutputStream jos = new JarOutputStream(os, mani)) {
                jos.finish();
            }
        } finally {
            os.close();
        }
        task.setModule("modules/mod.jar");
        File nbm = new File(w, "mod.nbm");
        task.setFile(nbm);
        File stuff = new File(cluster, "stuff");
        if (!stuff.mkdir()) {
            throw new IOException();
        }
        os = new FileOutputStream(new File(stuff, "illegal"));
        try {
            os.write("actual content".getBytes());
        } finally {
            os.close();
        }
        os = new FileOutputStream(new File(stuff, "illegal.external"));
        try {
            os.write("URL:http://nowhere.net/illegal\n".getBytes());
        } finally {
            os.close();
        }
        File tracking = new File(cluster, "update_tracking");
        if (!tracking.mkdir()) {
            throw new IOException();
        }
        os = new FileOutputStream(new File(tracking, "mod.xml"));
        try {
            os.write("<module codename='mod'><module_version><file name='modules/mod.jar'/><file name='stuff/illegal'/><file name='stuff/illegal.external'/></module_version></module>".getBytes());
        } finally {
            os.close();
        }
        task.execute();
        assertTrue(nbm.isFile());
        JarFile jf = new JarFile(nbm);
        try {
            JarEntry entry = jf.getJarEntry("netbeans/stuff/illegal.external");
            assertNotNull(entry);
            assertEquals(31, entry.getSize());
            assertNull(jf.getEntry("netbeans/stuff/illegal"));
        } finally {
            jf.close();
        }
    }

}
