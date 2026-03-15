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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Is generation of Jnlp files correct?
 *
 * @author Jaroslav Tulach, Jesse Glick
 */
public class MakeJNLPTest extends TestBase {
    public MakeJNLPTest (String name) {
        super (name);
    }
    
    private static void assertFilenames(File dir, String... contents) {
        assertTrue(dir + " is a directory", dir.isDirectory());
        SortedSet<String> expected = new TreeSet<>(Arrays.asList(contents));
        SortedSet<String> actual = new TreeSet<>();
        findFilenames(dir, "", actual);
        assertEquals("correct contents of " + dir, expected/*.toString()*/, actual/*.toString()*/);
    }
    private static void findFilenames(File dir, String prefix, Set<String> names) {
        for (File f : dir.listFiles()) {
            if (f.isFile()) {
                names.add(prefix + f.getName());
            } else if (f.isDirectory()) {
                findFilenames(f, prefix + f.getName() + "/", names);
            }
        }
    }
    
    public void testGenerateJNLPAndSignedJarForSimpleModule() throws Exception {
        Manifest m;
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "org.my.module/3");
        File simpleJar = generateJar (new String[0], m);

        File parent = simpleJar.getParentFile ();
        File output = new File(parent, "output");
        File ks = generateKeystore("jnlp", "netbeans-test");
        
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"jnlp\" classname=\"org.netbeans.nbbuild.MakeJNLP\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" + 
            "  <jnlp dir='" + output + "' alias='jnlp' storepass='netbeans-test' keystore='" + ks + "' >" +
            "    <modules dir='" + parent + "' >" +
            "      <include name='" + simpleJar.getName() + "' />" +
            "    </modules>" +
            "  </jnlp>" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertFilenames(output, "org-my-module.jnlp", "org-my-module/s0.jar");
        
        File jnlp = new File(output, "org-my-module.jnlp");
        String res = readFile (jnlp);
        
        assertTrue ("Component JNLP type: " + res, res.indexOf ("<component-desc/>") >= 0);
        assertTrue ("We support all permissions by default: " + res, res.indexOf ("<all-permissions/>") >= 0);
        
        Matcher match = Pattern.compile(".*codebase=['\\\"]([^'\\\"]*)['\\\"]").matcher(res);
        assertTrue("codebase is there", match.find());
        assertEquals("one group found", 1, match.groupCount());
        String base = match.group(1);
        
        assertEquals("By default the dest directory is $$codebase: ", "$$codebase", base);

        CHECK_SIGNED: {
            File jar = new File(output, "org-my-module/s0.jar");
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

    public void testHandlesOSGi() throws Exception {
        Manifest m;

        m = createManifest ();
        m.getMainAttributes ().putValue ("Bundle-SymbolicName", "org.my.module");
        File simpleJar = generateJar (new String[0], m);

        File parent = simpleJar.getParentFile ();
        File output = new File(parent, "output");
        File ks = generateKeystore("jnlp", "netbeans-test");

        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"jnlp\" classname=\"org.netbeans.nbbuild.MakeJNLP\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" +
            "  <jnlp dir='" + output + "' alias='jnlp' storepass='netbeans-test' keystore='" + ks + "' >" +
            "    <modules dir='" + parent + "' >" +
            "      <include name='" + simpleJar.getName() + "' />" +
            "    </modules>" +
            "  </jnlp>" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });

        assertFilenames(output, "org-my-module.jnlp", "org-my-module/s0.jar");

        File jnlp = new File(output, "org-my-module.jnlp");
        String res = readFile (jnlp);

        assertTrue ("Component JNLP type: " + res, res.indexOf ("<component-desc/>") >= 0);
        assertTrue ("We support all permissions by default: " + res, res.indexOf ("<all-permissions/>") >= 0);

        Matcher match = Pattern.compile(".*codebase=['\\\"]([^'\\\"]*)['\\\"]").matcher(res);
        assertTrue("codebase is there", match.find());
        assertEquals("one group found", 1, match.groupCount());
        String base = match.group(1);

        assertEquals("By default the dest directory is $$codebase: ", "$$codebase", base);
    }

    public void testGenerateJNLPAndUnSignedJarForSimpleModule() throws Exception {
        Manifest m;
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "org.my.module/3");
        File simpleJar = generateJar (new String[0], m);

        File parent = simpleJar.getParentFile ();
        File output = new File(parent, "output");
        File ks = generateKeystore("jnlp", "netbeans-test");
        
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"jnlp\" classname=\"org.netbeans.nbbuild.MakeJNLP\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" + 
            "  <jnlp dir='" + output + "' alias='jnlp' storepass='netbeans-test' keystore='" + ks + "' signjars='false' >" +
            "    <modules dir='" + parent + "' >" +
            "      <include name='" + simpleJar.getName() + "' />" +
            "    </modules>" +
            "  </jnlp>" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertFilenames(output, "org-my-module.jnlp", "org-my-module/s0.jar");
        
        File jnlp = new File(output, "org-my-module.jnlp");
        String res = readFile (jnlp);
        
        assertTrue ("Component JNLP type: " + res, res.indexOf ("<component-desc/>") >= 0);
        assertTrue ("We support all permissions by default: " + res, res.indexOf ("<all-permissions/>") >= 0);
        
        Matcher match = Pattern.compile(".*codebase=['\\\"]([^'\\\"]*)['\\\"]").matcher(res);
        assertTrue("codebase is there", match.find());
        assertEquals("one group found", 1, match.groupCount());
        String base = match.group(1);
        
        assertEquals("By default the dest directory is $$codebase: ", "$$codebase", base);

        File jar = new File(output, "org-my-module/s0.jar");
        JarFile signed = new JarFile(jar);
        Enumeration<JarEntry> it = signed.entries();
        while (it.hasMoreElements()) {
            JarEntry entry = it.nextElement();
            if (entry.getName().endsWith(".SF")) {
                fail ("File should not be signed: " + jar);
            }
        }
        
    }

    public void testGenerateMacOSOnlySimpleModule() throws Exception {
        doGenerateOSOnlySimpleModule("org.openide.modules.os.MacOSX", "<resources os='Mac OS X'>");
    }

    public void testGenerateLinuxOSOnlySimpleModule() throws Exception {
        doGenerateOSOnlySimpleModule("org.openide.modules.os.Linux", "<resources os='Linux'>");
    }

    public void testGenerateWindowsOSOnlySimpleModule() throws Exception {
        doGenerateOSOnlySimpleModule("org.openide.modules.os.Windows", "<resources os='Windows'>");
    }
    public void testGenerateSolarisOSOnlySimpleModule() throws Exception {
        doGenerateOSOnlySimpleModule("org.openide.modules.os.Solaris", "<resources os='Solaris'>");
    }
    
    private void doGenerateOSOnlySimpleModule(String tok, String find) throws Exception {
        Manifest m;
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "org.my.module/3");
        m.getMainAttributes ().putValue ("OpenIDE-Module-Requires", tok + ", pepa.z.bota");
        File simpleJar = generateJar (new String[0], m);

        File parent = simpleJar.getParentFile ();
        File output = new File(parent, "output");
        File ks = generateKeystore("jnlp", "netbeans-test");
        
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"jnlp\" classname=\"org.netbeans.nbbuild.MakeJNLP\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" + 
            "  <jnlp dir='" + output + "' alias='jnlp' storepass='netbeans-test' keystore='" + ks + "' signjars='false' >" +
            "    <modules dir='" + parent + "' >" +
            "      <include name='" + simpleJar.getName() + "' />" +
            "    </modules>" +
            "  </jnlp>" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertFilenames(output, "org-my-module.jnlp", "org-my-module/s0.jar");
        
        File jnlp = new File(output, "org-my-module.jnlp");
        String res = readFile (jnlp);
        
        assertTrue ("Component JNLP type: " + res, res.indexOf ("<component-desc/>") >= 0);
        assertTrue ("Resource is os dependant: " + res, res.indexOf (find) >= 0);
        assertTrue ("We support all permissions by default: " + res, res.indexOf ("<all-permissions/>") >= 0);
        
        Matcher match = Pattern.compile(".*codebase=['\\\"]([^'\\\"]*)['\\\"]").matcher(res);
        assertTrue("codebase is there", match.find());
        assertEquals("one group found", 1, match.groupCount());
        String base = match.group(1);
        
        assertEquals("By default the dest directory is $$codebase: ", "$$codebase", base);

        File jar = new File(output, "org-my-module/s0.jar");
        JarFile signed = new JarFile(jar);
        Enumeration<JarEntry> it = signed.entries();
        while (it.hasMoreElements()) {
            JarEntry entry = it.nextElement();
            if (entry.getName().endsWith(".SF")) {
                fail ("File should not be signed: " + jar);
            }
        }
        
    }
    
    public void testTheLocalizedAutoupdateProblem() throws Exception {
        String UTfile =   
            "<?xml version='1.0' encoding='UTF-8'?>" +
            "<module codename='org.netbeans.modules.autoupdate/1'>" +
            "    <module_version install_time='1136503038669' last='true' origin='installer' specification_version='2.16.1'>" +
            "        <file crc='746562502' name='config/Modules/org-netbeans-modules-autoupdate.xml'/>" +
            "        <file crc='3552349255' name='modules/ext/locale/updater_ja.jar'/>" +
            "        <file crc='72601456' name='modules/ext/locale/updater_zh_CN.jar'/>" +
            "        <file crc='3405032071' name='modules/ext/updater.jar'/>" +
            "        <file crc='2409221434' name='modules/locale/org-netbeans-modules-autoupdate_ja.jar'/>" +
            "        <file crc='1180043929' name='modules/locale/org-netbeans-modules-autoupdate_zh_CN.jar'/>" +
            "        <file crc='3477298901' name='modules/org-netbeans-modules-autoupdate.jar'/>" +
            "    </module_version>" +
            "</module>";
      
        Manifest m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "org.netbeans.modules.autoupdate/1");
        m.getMainAttributes ().putValue ("Class-Path", "ext/updater.jar");
        File simpleJar = generateJar ("modules/", new String[0], m, null);
        File moduleJar = new File(simpleJar.getParentFile(), "org-netbeans-modules-autoupdate.jar");
        simpleJar.renameTo(moduleJar);
        
        File p = simpleJar.getParentFile();
        
        simpleJar = generateJar ("modules/locale/", new String[0], createManifest(), null);
        simpleJar.renameTo(new File(simpleJar.getParentFile(), "org-netbeans-modules-autoupdate_ja.jar"));

        simpleJar = generateJar ("modules/locale/", new String[0], createManifest(), null);
        simpleJar.renameTo(new File(simpleJar.getParentFile(), "org-netbeans-modules-autoupdate_zh_CN.jar"));
        
        simpleJar = generateJar ("modules/ext/", new String[0], createManifest(), null);
        simpleJar.renameTo(new File(simpleJar.getParentFile(), "updater.jar"));

        simpleJar = generateJar ("modules/ext/locale/", new String[0], createManifest(), null);
        simpleJar.renameTo(new File(simpleJar.getParentFile(), "updater_ja.jar"));

        simpleJar = generateJar ("modules/ext/locale/", new String[0], createManifest(), null);
        simpleJar.renameTo(new File(simpleJar.getParentFile(), "updater_zh_CN.jar"));

        File xml = new File(p, "config/Modules/org-netbeans-modules-autoupdate.xml");
        xml.getParentFile().mkdirs();
        xml.createNewFile();
        
        File updateTracking = new File(getWorkDir(), "update_tracking");
        updateTracking.mkdirs();
        assertTrue("Created", updateTracking.isDirectory());
        
        File trackingFile = new File(updateTracking, "org-netbeans-modules-autoupdate.xml");
        try (FileWriter w = new FileWriter(trackingFile)) {
            w.write(UTfile);
        }

        File output = new File(getWorkDir(), "output");
        File ks = generateKeystore("jnlp", "netbeans-test");
        
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"jnlp\" classname=\"org.netbeans.nbbuild.MakeJNLP\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" + 
            "  <jnlp dir='" + output + "' alias='jnlp' storepass='netbeans-test' keystore='" + ks + "' verify='true' >" +
            "    <modules dir='" + p + "' >" +
            "      <include name='" + moduleJar.getName() + "' />" +
            "    </modules>" +
            "  </jnlp>" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertFilenames(output, "org-netbeans-modules-autoupdate.jnlp",
                "org-netbeans-modules-autoupdate/org-netbeans-modules-autoupdate.jar",
                "org-netbeans-modules-autoupdate/locale-org-netbeans-modules-autoupdate_zh_CN.jar",
                "org-netbeans-modules-autoupdate/locale-org-netbeans-modules-autoupdate_ja.jar",
                "org-netbeans-modules-autoupdate/ext-locale-updater_zh_CN.jar",
                "org-netbeans-modules-autoupdate/ext-locale-updater_ja.jar",
                "org-netbeans-modules-autoupdate/ext-updater.jar");
        
        File jnlp = new File(output, "org-netbeans-modules-autoupdate.jnlp");
        String res = readFile (jnlp);
        
        
        assertTrue ("Component JNLP type: " + res, res.indexOf ("<component-desc/>") >= 0);
        assertTrue ("We support all permissions by default: " + res, res.indexOf ("<all-permissions/>") >= 0);
        
        Matcher match = Pattern.compile(".*codebase=['\\\"]([^'\\\"]*)['\\\"]").matcher(res);
        assertTrue("codebase is there", match.find());
        assertEquals("one group found", 1, match.groupCount());
        String base = match.group(1);
        
        assertEquals("By default the dest directory is $$codebase: ", "$$codebase", base);
        
        assertResource(res, "ja", "org-netbeans-modules-autoupdate/ext-locale-updater_ja.jar");
        assertResource(res, "zh_CN", "org-netbeans-modules-autoupdate/ext-locale-updater_zh_CN.jar");
        assertResource(res, "ja", "org-netbeans-modules-autoupdate/locale-org-netbeans-modules-autoupdate_ja.jar");
        assertResource(res, "zh_CN", "org-netbeans-modules-autoupdate/locale-org-netbeans-modules-autoupdate_zh_CN.jar");

        CHECK_SIGNED: for (File jar : new File(output, "org-netbeans-modules-autoupdate").listFiles()) {
            if (!jar.getName().endsWith(".jar")) {
                continue;
            }
            JarFile signed = new JarFile(jar);
            Enumeration<JarEntry> it = signed.entries();
            while (it.hasMoreElements()) {
                JarEntry entry = it.nextElement();
                if (entry.getName().endsWith(".SF")) {
                    continue CHECK_SIGNED;
                }
            }
            fail ("File does not seem to be signed: " + jar);
        }
        
    }
    public void testGenerateJNLPForMissingRegularModule() throws Exception {
        Manifest m;
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "org.netbeans.core.startup");
        File simpleJar = generateJar ("modules/", new String[0], m, null);
        File coreJar = new File(simpleJar.getParentFile(), "core.jar");
        simpleJar.renameTo(coreJar);
        simpleJar = coreJar;

        File parent = simpleJar.getParentFile ();
        File localizedJarCZ = generateJar("modules/locale/", new String[0], createManifest(), null);
        assertTrue("Successful rename", localizedJarCZ.renameTo(new File(localizedJarCZ.getParent(), "core_cs.jar")));
        
        File updateTracking = new File(getWorkDir(), "update_tracking");
        updateTracking.mkdirs();
        assertTrue("Created", updateTracking.isDirectory());
        
        File trackingFile = new File(updateTracking, "org-netbeans-core-startup.xml");
        try (FileWriter w = new FileWriter(trackingFile)) {
            w.write(
                    "<?xml version='1.0' encoding='UTF-8'?>\n" +
                            "<module codename='org.my.module/3'>\n" +
                            "<module_version specification_version='3.22' origin='installer' last='true' install_time='1124194231878'>\n" +
                            "<file name='modules/" + simpleJar.getName() + "' crc='3245456472'/>\n" +
                                    "<file name='config/Modules/org-netbeans-core-startup.xml' crc='43434' />\n" +
                                    "<file name='modules/locale/core_cs.jar' crc='454244' />\n" +
                                    "<file name='modules/locale/core_ja.jar' crc='779831' />\n" +
                                    "<file name='modules/locale/core_zh_CN.jar' crc='475345' />\n" +
                                    "    </module_version>\n" +
                                    "</module>\n"
            );
        }
        
        
        File output = new File(parent, "output");
        File ks = generateKeystore("jnlp", "netbeans-test");
        
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"jnlp\" classname=\"org.netbeans.nbbuild.MakeJNLP\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" + 
            "  <jnlp dir='" + output + "' alias='jnlp' storepass='netbeans-test' keystore='" + ks + "' verify='true' >" +
            "    <modules dir='" + parent + "' >" +
            "      <include name='" + simpleJar.getName() + "' />" +
            "    </modules>" +
            "  </jnlp>" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });

        assertFilenames(output, "org-netbeans-core-startup.jnlp",
                "org-netbeans-core-startup/core.jar",
                "org-netbeans-core-startup/locale-core_cs.jar");
    }
    
    public void testGenerateJNLPAndSignedJarForSimpleLocalizedModule() throws Exception {
        Manifest m;
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "org.my.module/3");
        File simpleJar = generateJar ("modules/", new String[0], m, null);

        File parent = simpleJar.getParentFile ();
        File localizedJarCZ = generateJar("modules/locale/", new String[0], createManifest(), null);
        assertEquals("There need to have the same name", simpleJar.getName(), localizedJarCZ.getName());
        assertTrue("Successful rename", localizedJarCZ.renameTo(new File(localizedJarCZ.getParent(), "0_cs.jar")));
        
        File localizedJarZH = generateJar("modules/locale/", new String[0], createManifest(), null);
        assertEquals("There need to have the same name", simpleJar.getName(), localizedJarZH.getName());
        assertTrue("Successful rename", localizedJarZH.renameTo(new File(localizedJarCZ.getParent(), "0_zh_CN.jar")));
        
        File localizedJarJA = generateJar("modules/locale/", new String[0], createManifest(), null);
        assertEquals("There need to have the same name", simpleJar.getName(), localizedJarJA.getName());
        assertTrue("Successful rename", localizedJarJA.renameTo(new File(localizedJarCZ.getParent(), "0_ja.jar")));

        File updateTracking = new File(getWorkDir(), "update_tracking");
        updateTracking.mkdirs();
        assertTrue("Created", updateTracking.isDirectory());
        
        File trackingFile = new File(updateTracking, "org-my-module.xml");
        try (FileWriter w = new FileWriter(trackingFile)) {
            w.write(
                    "<?xml version='1.0' encoding='UTF-8'?>\n" +
                            "<module codename='org.my.module/3'>\n" +
                            "<module_version specification_version='3.22' origin='installer' last='true' install_time='1124194231878'>\n" +
                            "<file name='modules/" + simpleJar.getName() + "' crc='3245456472'/>\n" +
                                    "<file name='config/Modules/org-my-module.xml' crc='43434' />\n" +
                                    "<file name='modules/locale/0_cs.jar' crc='454244' />\n" +
                                    "<file name='modules/locale/0_ja.jar' crc='779831' />\n" +
                                    "<file name='modules/locale/0_zh_CN.jar' crc='475345' />\n" +
                                    "    </module_version>\n" +
                                    "</module>\n"
            );
        }
        
        
        File output = new File(parent, "output");
        File ks = generateKeystore("jnlp", "netbeans-test");
        
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"jnlp\" classname=\"org.netbeans.nbbuild.MakeJNLP\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" + 
            "  <jnlp dir='" + output + "' alias='jnlp' storepass='netbeans-test' keystore='" + ks + "' verify='true' >" +
            "    <modules dir='" + parent + "' >" +
            "      <include name='" + simpleJar.getName() + "' />" +
            "    </modules>" +
            "  </jnlp>" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });

        assertFilenames(output, "org-my-module.jnlp",
                "org-my-module/0.jar",
                "org-my-module/locale-0_cs.jar",
                "org-my-module/locale-0_zh_CN.jar",
                "org-my-module/locale-0_ja.jar");

        File jnlp = new File(output, "org-my-module.jnlp");
        String res = readFile (jnlp);
        
        assertTrue ("Component JNLP type: " + res, res.indexOf ("<component-desc/>") >= 0);
        assertTrue ("We support all permissions by default: " + res, res.indexOf ("<all-permissions/>") >= 0);
        
        Matcher match = Pattern.compile(".*codebase=['\\\"]([^'\\\"]*)['\\\"]").matcher(res);
        assertTrue("codebase is there", match.find());
        assertEquals("one group found", 1, match.groupCount());
        String base = match.group(1);
        
        assertEquals("By default the dest directory is $$codebase: ", "$$codebase", base);
        
        assertResource(res, "cs", "org-my-module/locale-0_cs.jar");
        assertResource(res, "ja", "org-my-module/locale-0_ja.jar");
        assertResource(res, "zh_CN", "org-my-module/locale-0_zh_CN.jar");

        CHECK_SIGNED: for (File jar : new File(output, "org-my-module").listFiles()) {
            if (!jar.getName().endsWith(".jar")) {
                continue;
            }
            
            JarFile signed = new JarFile(jar);
            Enumeration<JarEntry> it = signed.entries();
            while (it.hasMoreElements()) {
                JarEntry entry = it.nextElement();
                if (entry.getName().endsWith(".SF")) {
                    continue CHECK_SIGNED;
                }
            }
            fail ("File does not seem to be signed: " + jar);
        }
    }
    public void testGenerateJNLPForMissingCoreIssue103301() throws Exception {
        Manifest m;
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "org.netbeans.core.startup");
        File simpleJar = generateJar ("core/", new String[0], m, null);
        File coreJar = new File(simpleJar.getParentFile(), "core.jar");
        simpleJar.renameTo(coreJar);
        simpleJar = coreJar;

        File parent = simpleJar.getParentFile ();
        File localizedJarCZ = generateJar("core/locale/", new String[0], createManifest(), null);
        assertTrue("Successful rename", localizedJarCZ.renameTo(new File(localizedJarCZ.getParent(), "core_cs.jar")));
        
        File updateTracking = new File(getWorkDir(), "update_tracking");
        updateTracking.mkdirs();
        assertTrue("Created", updateTracking.isDirectory());
        
        File trackingFile = new File(updateTracking, "org-netbeans-core-startup.xml");
        try (FileWriter w = new FileWriter(trackingFile)) {
            w.write(
                    "<?xml version='1.0' encoding='UTF-8'?>\n" +
                            "<module codename='org.my.module/3'>\n" +
                            "<module_version specification_version='3.22' origin='installer' last='true' install_time='1124194231878'>\n" +
                            "<file name='core/" + simpleJar.getName() + "' crc='3245456472'/>\n" +
                                    "<file name='config/Modules/org-netbeans-core-startup.xml' crc='43434' />\n" +
                                    "<file name='core/locale/core_cs.jar' crc='454244' />\n" +
                                    "<file name='core/locale/core_ja.jar' crc='779831' />\n" +
                                    "<file name='core/locale/core_zh_CN.jar' crc='475345' />\n" +
                                    "    </module_version>\n" +
                                    "</module>\n"
            );
        }
        
        
        File output = new File(parent, "output");
        File ks = generateKeystore("jnlp", "netbeans-test");
        
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"jnlp\" classname=\"org.netbeans.nbbuild.MakeJNLP\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" + 
            "  <jnlp dir='" + output + "' alias='jnlp' storepass='netbeans-test' keystore='" + ks + "' verify='true' >" +
            "    <modules dir='" + parent + "' >" +
            "      <include name='" + simpleJar.getName() + "' />" +
            "    </modules>" +
            "  </jnlp>" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });

        assertFilenames(output, "org-netbeans-core-startup.jnlp",
                "org-netbeans-core-startup/core.jar",
                "org-netbeans-core-startup/locale-core_cs.jar");
    }
    
    public void testGenerateJNLPAndUnSignedJarForSimpleLocalizedModule() throws Exception {
        Manifest m;
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "org.my.module/3");
        File simpleJar = generateJar ("modules/", new String[0], m, null);

        File parent = simpleJar.getParentFile ();
        File localizedJarCZ = generateJar("modules/locale/", new String[0], createManifest(), null);
        assertEquals("There need to have the same name", simpleJar.getName(), localizedJarCZ.getName());
        assertTrue("Successful rename", localizedJarCZ.renameTo(new File(localizedJarCZ.getParent(), "0_cs.jar")));
        
        File localizedJarZH = generateJar("modules/locale/", new String[0], createManifest(), null);
        assertEquals("There need to have the same name", simpleJar.getName(), localizedJarZH.getName());
        assertTrue("Successful rename", localizedJarZH.renameTo(new File(localizedJarCZ.getParent(), "0_zh_CN.jar")));
        
        File localizedJarJA = generateJar("modules/locale/", new String[0], createManifest(), null);
        assertEquals("There need to have the same name", simpleJar.getName(), localizedJarJA.getName());
        assertTrue("Successful rename", localizedJarJA.renameTo(new File(localizedJarCZ.getParent(), "0_ja.jar")));

        File updateTracking = new File(getWorkDir(), "update_tracking");
        updateTracking.mkdirs();
        assertTrue("Created", updateTracking.isDirectory());
        
        File trackingFile = new File(updateTracking, "org-my-module.xml");
        try (FileWriter w = new FileWriter(trackingFile)) {
            w.write(
                    "<?xml version='1.0' encoding='UTF-8'?>\n" +
                            "<module codename='org.my.module/3'>\n" +
                            "<module_version specification_version='3.22' origin='installer' last='true' install_time='1124194231878'>\n" +
                            "<file name='modules/" + simpleJar.getName() + "' crc='3245456472'/>\n" +
                                    "<file name='config/Modules/org-my-module.xml' crc='43434' />\n" +
                                    "<file name='modules/locale/0_cs.jar' crc='454244' />\n" +
                                    "<file name='modules/locale/0_ja.jar' crc='779831' />\n" +
                                    "<file name='modules/locale/0_zh_CN.jar' crc='475345' />\n" +
                                    "    </module_version>\n" +
                                    "</module>\n"
            );
        }
        
        
        File output = new File(parent, "output");
        File ks = generateKeystore("jnlp", "netbeans-test");
        
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"jnlp\" classname=\"org.netbeans.nbbuild.MakeJNLP\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" + 
            "  <jnlp dir='" + output + "' alias='jnlp' storepass='netbeans-test' keystore='" + ks + "' verify='true' signjars='false' >" +
            "    <modules dir='" + parent + "' >" +
            "      <include name='" + simpleJar.getName() + "' />" +
            "    </modules>" +
            "  </jnlp>" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });

        assertFilenames(output, "org-my-module.jnlp",
                "org-my-module/0.jar",
                "org-my-module/locale-0_cs.jar",
                "org-my-module/locale-0_zh_CN.jar",
                "org-my-module/locale-0_ja.jar");

        File jnlp = new File(output, "org-my-module.jnlp");
        String res = readFile (jnlp);
        
        assertTrue ("Component JNLP type: " + res, res.indexOf ("<component-desc/>") >= 0);
        assertTrue ("We support all permissions by default: " + res, res.indexOf ("<all-permissions/>") >= 0);
        
        Matcher match = Pattern.compile(".*codebase=['\\\"]([^'\\\"]*)['\\\"]").matcher(res);
        assertTrue("codebase is there", match.find());
        assertEquals("one group found", 1, match.groupCount());
        String base = match.group(1);
        
        assertEquals("By default the dest directory is $$codebase: ", "$$codebase", base);
        
        assertResource(res, "cs", "org-my-module/locale-0_cs.jar");
        assertResource(res, "ja", "org-my-module/locale-0_ja.jar");
        assertResource(res, "zh_CN", "org-my-module/locale-0_zh_CN.jar");

        for (File jar : new File(output, "org-my-module").listFiles()) {
            if (!jar.getName().endsWith(".jar")) {
                continue;
            }
            
            JarFile signed = new JarFile(jar);
            Enumeration<JarEntry> it = signed.entries();
            while (it.hasMoreElements()) {
                JarEntry entry = it.nextElement();
                if (entry.getName().endsWith(".SF")) {
                    fail ("File does not seem to be signed: " + jar);
                }
            }
        }
    }
    
    private static void assertResource(String where, String locale, String file) {
        where = where.replace('\n', ' ');
        Matcher match = Pattern.compile("<resources *locale='" + locale + "' *>.*<jar href='" + file + "' */>.*</resources>").matcher(where);
        assertTrue("File really referenced " + file + " in locale " + locale + "\n" + where, match.find());
    }
    
    public void testOneCanChangeTheCodeBase() throws Exception {
        Manifest m;
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "org.my.module/3");
        File simpleJar = generateJar (new String[0], m);

        File parent = simpleJar.getParentFile ();
        File output = new File(parent, "output");
        File ks = generateKeystore("jnlp", "netbeans-test");
        
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"jnlp\" classname=\"org.netbeans.nbbuild.MakeJNLP\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" + 
            "  <jnlp dir='" + output + "' alias='jnlp' storepass='netbeans-test' keystore='" + ks + "' codebase='http://www.my.org/' >" +
            "    <modules dir='" + parent + "' >" +
            "      <include name='" + simpleJar.getName() + "' />" +
            "    </modules>" +
            "  </jnlp>" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { "-verbose" });
        
        assertFilenames(output, "org-my-module.jnlp",
                "org-my-module/s0.jar");
        
        File jnlp = new File(output, "org-my-module.jnlp");
        String res = readFile (jnlp);
        
        assertTrue ("Component JNLP type: " + res, res.indexOf ("<component-desc/>") >= 0);
        assertTrue ("We support all permissions by default: " + res, res.indexOf ("<all-permissions/>") >= 0);
        
        Matcher match = Pattern.compile(".*codebase=['\\\"]([^'\\\"]*)['\\\"]").matcher(res);
        assertTrue("codebase is there", match.find());
        assertEquals("one group found", 1, match.groupCount());
        String base = match.group(1);
        
        assertEquals("By default the codebases can be changed: ", "http://www.my.org/", base);
    }

    public void testGenerateJNLPAndSignedJarForModuleWithClassPath() throws Exception {
        File output = doClassPathModuleCheck(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"jnlp\" classname=\"org.netbeans.nbbuild.MakeJNLP\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='${test.output}' />" + 
            "  <jnlp dir='${test.output}' alias='jnlp' storepass='netbeans-test' keystore='${test.ks}' >" +
            "    <modules dir='${test.parent}' >" +
            "      <include name='${test.name}' />" +
            "    </modules>" +
            "  </jnlp>" +
            "</target>" +
            "</project>"
        );
        
        assertFilenames(output, "aaa-my-module.jnlp", "aaa-my-module/ext-t0.jar", "aaa-my-module/s0.jar");
        
        File jnlp = new File(output, "aaa-my-module.jnlp");
        String res = readFile (jnlp);

        
        Matcher m = Pattern.compile("<jar href='(.*)' */>").matcher(res);
        for (int x = 0; x < 2; x++) {
            assertTrue("Matches at least one" + "(" + x + ")", m.find());
            assertEquals("Found a group" + "(" + x + ")", m.groupCount(), 1);
            File f = new File (jnlp.getParentFile(), m.group(1));
            assertTrue("The file " + f + " exists" + "(" + x + ")", f.exists());
        }
        
    }

    public void testGenerateJNLPAndSignedJarForModuleWithClassPathAndSignedJar() throws Exception {
        File ks = generateKeystore("external", "netbeans-test");
        
        File output = doClassPathModuleCheck(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"jnlp\" classname=\"org.netbeans.nbbuild.MakeJNLP\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='${test.output}' />" + 
            "  <signjar jar='${test.ext}' alias='external' storepass='netbeans-test' keystore='${test.ks}' />\n" +
            "  <jnlp dir='${test.output}' alias='jnlp' storepass='netbeans-test' keystore='${test.ks}' >" +
            "    <modules dir='${test.parent}' >" +
            "      <include name='${test.name}' />" +
            "    </modules>" +
            "  </jnlp>" +
            "</target>" +
            "</project>"
        );
        
        assertFilenames(output, "aaa-my-module.jnlp", "aaa-my-module/s0.jar",
                "aaa-my-module/ext-t0.jar",
                "aaa-my-module-ext-t0.jnlp");
        
        JarFile f = new JarFile(new File(output, "aaa-my-module/ext-t0.jar"));
        Enumeration<JarEntry> en = f.entries();
        StringBuffer sb = new StringBuffer();
        int cnt = 0;
        while (en.hasMoreElements()) {
            JarEntry e = en.nextElement();
            if (e.getName().endsWith("SF")) {
                cnt++;
                if (!e.getName().equals("META-INF/EXTERNAL.SF")) {
                    fail("Signed with wrong entity: " + e.getName());
                }
            }
            sb.append(e.getName());
            sb.append('\n');
        }

        if (cnt == 0) {
            fail("Signed with wrong file:\n" + sb);
        }
        
        File jnlp = new File(output, "aaa-my-module.jnlp");
        
        String res = readFile (jnlp);

        int first = res.indexOf("jar href");
        assertEquals("Just one jar href ", -1, res.indexOf("jar href", first + 1));
        
        String extRes = readFile(new File(output, "aaa-my-module-ext-t0.jnlp"));
        
        Matcher m = Pattern.compile("<title>(.*)</title>").matcher(extRes);
        assertTrue("title is there: " + extRes, m.find());
        assertEquals("Name of file is used for title", "t0", m.group(1));
    }
    
    public void testInformationIsTakenFromLocalizedBundle() throws Exception {
        Manifest m;
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "org.my.module/3");
        
        Properties props = new Properties();
        props.put("OpenIDE-Module-Name", "Module Build Harness");
        props.put("OpenIDE-Module-Display-Category", "Developing NetBeans");
        props.put("OpenIDE-Module-Short-Description", "Lets you build external plug-in modules from sources.");
        props.put("OpenIDE-Module-Long-Description", "XXX");
        
        File simpleJar = generateJar (null, new String[0], m, props);

        File parent = simpleJar.getParentFile ();
        File output = new File(parent, "output");
        File ks = generateKeystore("jnlp", "netbeans-test");
        
        java.io.File f = extractString (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"jnlp\" classname=\"org.netbeans.nbbuild.MakeJNLP\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='" + output + "' />" + 
            "  <jnlp dir='" + output + "' alias='jnlp' storepass='netbeans-test' keystore='" + ks + "' >" +
            "    <modules dir='" + parent + "' >" +
            "      <include name='" + simpleJar.getName() + "' />" +
            "    </modules>" +
            "  </jnlp>" +
            "</target>" +
            "</project>"
        );
        execute (f, new String[] { });
    
        assertFilenames(output, "org-my-module.jnlp", "org-my-module/s0.jar");
        
        File jnlp = new File(output, "org-my-module.jnlp");
        String res = readFile (jnlp);

        int infoBegin = res.indexOf("<information>");
        int infoEnd = res.indexOf("</information>");
        
        if (infoEnd == -1 || infoBegin == -1) {
            fail ("Both information tags must be present: " + res);
        }
        
        String info = res.substring(infoBegin, infoEnd);
        
        if (info.indexOf("<title>Module Build Harness</title>") == -1) {
            fail("Title should be there with Module Build Harness inside itself: " + info);
        }
        
        if (info.indexOf("<description kind='one-line'>Lets you build external plug-in modules from sources.</description>") == -1) {
            fail("one-line should be there with 'lets you...' inside itself: " + info);
        }
        
        if (info.indexOf("<description kind='short'>XXX</description>") == -1) {
            fail("short should be there with XXX inside itself: " + info);
        }
    }
    
    public void testGenerateJNLPFailsForModulesWithExtraFiles() throws Exception {
        doCompareJNLPFileWithUpdateTracking(true, null, "");
    }
    public void testGenerateJNLPSucceedsWithExtraFiles() throws Exception {
        doCompareJNLPFileWithUpdateTracking(false, null, "");
    }
    public void testGenerateJNLPSucceedsWhenExtraFileIsExcluded() throws Exception {
        doCompareJNLPFileWithUpdateTracking(false, "lib/nbexec", " verifyexcludes=' one, lib/nbexec, three ' ");
    }
    public void testGenerateJNLPSucceedsWhenModuleAutoDepsArePresent() throws Exception {
        doCompareJNLPFileWithUpdateTracking(false, "config/ModuleAutoDeps/aaa-my-module.xml", " verifyexcludes=' none ' ");
    }
    
    private void doCompareJNLPFileWithUpdateTracking(boolean useNonModule, String fakeEntry, String extraScript) throws Exception {
        File nonModule = generateJar (new String[0], createManifest());
        
        Manifest m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "aaa.my.module/3");
        File module = generateJar (new String[0], m);
        
        File updateTracking = new File(getWorkDir(), "update_tracking");
        updateTracking.mkdirs();
        assertTrue("Created", updateTracking.isDirectory());

        File enableXML = new File(new File(getWorkDir(), "config"), "Modules");
        enableXML.getParentFile().mkdirs();
        enableXML.createNewFile();
        
        File trackingFile = new File(updateTracking, "aaa-my-module.xml");
        try (FileWriter w = new FileWriter(trackingFile)) {
            w.write(
                    "<?xml version='1.0' encoding='UTF-8'?>\n" +
                            "<module codename='org.apache.tools.ant.module/3'>\n" +
                            "<module_version specification_version='3.22' origin='installer' last='true' install_time='1124194231878'>\n" +
                            (useNonModule ? ("<file name='modules/" + nonModule.getName() + "' crc='1536373800'/>\n") : "") +
                            "<file name='modules/" + module.getName() + "' crc='3245456472'/>\n" +
                                    "<file name='config/Modules/aaa-my-module.xml' crc='43434' />\n" +
                            (fakeEntry != null ? "<file name='" + fakeEntry + "' crc='43222' />\n" : "") +
                            "    </module_version>\n" +
                                    "</module>\n"
            );
        }
        
        
        
        String script =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
            "  <taskdef name=\"jnlp\" classname=\"org.netbeans.nbbuild.MakeJNLP\" classpath=\"${nbantext.jar}\"/>" +
            "<target name=\"all\" >" +
            "  <mkdir dir='${test.output}' />" + 
            "  <jnlp dir='${test.output}' alias='jnlp' storepass='netbeans-test' keystore='${test.ks}' verify='true' " + extraScript + " >" +
            "    <modules dir='${test.parent}' >" +
            "      <include name='${test.name}' />" +
            "    </modules>" +
            "  </jnlp>" +
            "</target>" +
            "</project>";

        assertEquals("Both modules in the same dir", module.getParentFile(), nonModule.getParentFile());
        
        File output = new File(getWorkDir(), "output");
        File ks = generateKeystore("jnlp", "netbeans-test");
        
        java.io.File f = extractString (script);
        try {
            execute (f, new String[] { 
                "-Dtest.output=" + output, 
                "-Dtest.parent=" + module.getParent(), 
                "-Dtest.name=" + module.getName(),
                "-Dtest.ks=" + ks,
            });
            if (useNonModule) {
                fail("The task has to fail");   
            }
            
            assertTrue ("Output exists", output.exists ());
            assertTrue ("Output directory created", output.isDirectory());

            File ext = new File (output, module.getName());


            String[] files = ext.getParentFile().list();
            assertEquals("Two files are there", 2, files.length);
        } catch (ExecutionError ex) {
            if (!useNonModule) {
                throw ex;
            } else {
                // ok, this is fine
                assertTrue ("Output exists", output.exists ());
                assertTrue ("Output directory created", output.isDirectory());

                File ext = new File (output, module.getName());


                String[] files = ext.getParentFile().list();
                assertEquals("Output dir is empty as nothing has been generated", 0, files.length);
            }
        }
        
    }

    private File doClassPathModuleCheck(String script) throws Exception {
        Manifest m;

        File extJar = generateJar ("modules/ext", new String[0], createManifest(), null);
        
        m = createManifest ();
        m.getMainAttributes ().putValue ("OpenIDE-Module", "aaa.my.module/3");
        m.getMainAttributes ().putValue ("Class-Path", "ext/" + extJar.getName());
        File simpleJar = generateJar ("modules", new String[0], m, null);

        File parent = simpleJar.getParentFile ();
        
        File output = new File(parent, "output");
        File ks = generateKeystore("jnlp", "netbeans-test");
        
        java.io.File f = extractString (script);
        execute (f, new String[] { 
            "-Dtest.output=" + output, 
            "-Dtest.parent=" + parent, 
            "-Dtest.name=" + simpleJar.getName(),
            "-Dtest.ks=" + ks,
            "-Dtest.ext=" + extJar
        });
        
        return output;
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
            if (!f.exists()) {
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
    
    @SuppressWarnings("SleepWhileInLoop")
    private File generateKeystore(String alias, String password) throws Exception {
        Error lastEx = null;
        for (int i = 0; i < 10; i++) {
            File where = new File(getWorkDir(), "key" + i + ".ks");

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
            } catch (ExecutionError ex) {
                Logger.getAnonymousLogger().log(Level.WARNING, "Failed for " + i, ex);
                lastEx = ex;
                if (ex.getMessage().indexOf("CKR_KEY_SIZE_RANGE") >= 0) {
                    Thread.sleep(new Random().nextInt(1000));
                    continue;
                }
                throw ex;
            }
            return where;
        }
        throw lastEx;
    }

    public void testIndirectJars() throws Exception {
        Manifest m = createManifest();
        m.getMainAttributes().putValue("OpenIDE-Module", "me");
        generateJar(new String[0], m);
        generateJar("lib", new String[0], new Manifest(), null);
        assertTrue(new File(getWorkDir(), "lib/b0.jar").isFile());
        File output = new File(getWorkDir(), "output");
        File ks = generateKeystore("jnlp", "netbeans-test");
        File f = extractString(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<project name=\"Test Arch\" basedir=\".\" default=\"all\" >" +
                "  <taskdef name=\"jnlp\" classname=\"org.netbeans.nbbuild.MakeJNLP\" classpath=\"${nbantext.jar}\"/>" +
                "<target name=\"all\" >" +
                "  <mkdir dir='" + output + "' />" +
                "  <jnlp dir='" + output + "' alias='jnlp' storepass='netbeans-test' keystore='" + ks + "' >" +
                "    <modules dir='" + getWorkDir() + "'>" +
                "      <include name='modules/s0.jar'/>" +
                "    </modules>" +
                "    <indirectjars dir='" + getWorkDir() + "'>" +
                "      <include name='lib/b0.jar'/>" +
                "    </indirectjars>" +
                "  </jnlp>" +
                "</target>" +
                "</project>"
                );
        execute(f, new String[] { "-verbose" });
        assertFilenames(output, "me.jnlp", "me/s0.jar", "me/lib-b0.jar");
        File jnlp = new File(output, "me.jnlp");
        String res = readFile(jnlp);
        assertTrue(res, res.contains("me/lib-b0.jar"));
        JarFile otherJar = new JarFile(new File(output, "me/lib-b0.jar"));
        assertNotNull(otherJar.getEntry("META-INF/clusterpath/lib/b0.jar"));
    }

}
