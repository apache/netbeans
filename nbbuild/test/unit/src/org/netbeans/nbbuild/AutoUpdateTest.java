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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.tools.ant.Project;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class AutoUpdateTest extends TestBase {

    public AutoUpdateTest(String name) {
        super(name);
    }

    public void testJustDownloadNBMs() throws Exception {
        File nbm = generateNBM("org-netbeans-api-annotations-common.nbm",
            "netbeans/config/Modules/org-netbeans-api-annotations-common.xml",
            "netbeans/modules/org-netbeans-api-annotations-common.jar");
        assertTrue("NBM file created", nbm.isFile());

        File target = new File(getWorkDir(), "target");
        target.mkdirs();

        execute(
            "autoupdate.xml", "-verbose", "-Ddir=" + nbm.getParent(),
            "-Dincludes=org.netbeans.api.annotations.common",
            "-Dtarget=" + target,
            "mirror"
        );
        
        File[] arr = target.listFiles();
        assertEquals("One item in the array:\n" + getStdOut(), 1, arr.length);
        assertEquals("It is the NBM file", nbm.getName(), arr[0].getName());
        assertEquals("Length is the same", nbm.length(), arr[0].length());
        
        execute(
            "autoupdate.xml", "-verbose", "-Ddir=" + nbm.getParent(),
            "-Dincludes=org.netbeans.api.annotations.common",
            "-Dtarget=" + target,
            "mirror"
        );

        if (getStdOut().contains("get:org.netbeans.api.annotations.common")) {
            fail("No download when latest version present:\n" + getStdOut());
        }
        if (getStdOut().contains("is not present")) {
            fail("Don't say it is not present:\n" + getStdOut());
        }
        
    }
    public void testDirectlySpecifiedNBMs() throws Exception {
        File nbm = generateNBM("org-netbeans-api-annotations-common.nbm",
            "netbeans/config/Modules/org-netbeans-api-annotations-common.xml",
            "netbeans/modules/org-netbeans-api-annotations-common.jar");
        assertTrue("NBM file created", nbm.isFile());

        File target = new File(getWorkDir(), "target");
        target.mkdirs();

        execute(
            "autoupdate.xml", "-verbose", "-Ddir=" + nbm.getParent(),
            "-Dincludes=org.netbeans.api.annotations.common",
            "-Dtarget=" + target,
            "all-nbms"
        );

        File xml = new File(
            new File(target, "update_tracking"),
            "org-netbeans-api-annotations-common.xml"
        );
        assertTrue("xml file created", xml.exists());

        File jar = new File(
            new File(target, "modules"),
            "org-netbeans-api-annotations-common.jar"
        );
        assertTrue("jar file created", jar.exists());

        File lastM = new File(target, ".lastModified");
        assertTrue("Last modified file created", lastM.exists());
        assertTrue("NBM file left untouched", nbm.isFile());
    }
    public void testDownloadAndExtractModule() throws Exception {
        File f = new File(getWorkDir(), "org-netbeans-api-annotations-common.xml");
        extractResource(f, "org-netbeans-api-annotations-common.xml");

        File nbm = generateNBM("org-netbeans-api-annotations-common.nbm",
            "netbeans/config/Modules/org-netbeans-api-annotations-common.xml",
            "netbeans/modules/org-netbeans-api-annotations-common.jar",
            "netbeans/docs/My Manager's So-Called \"README\"");

        File target = new File(getWorkDir(), "target");
        target.mkdirs();

        execute(
            "autoupdate.xml", "-verbose", "-Durl=" + f.toURI().toURL(),
            "-Dincludes=org.netbeans.api.annotations.common",
            "-Dtarget=" + target
        );

        File xml = new File(
            new File(new File(target, "platform"), "update_tracking"),
            "org-netbeans-api-annotations-common.xml"
        );
        assertTrue("xml file created", xml.exists());
        Document doc = XMLUtil.parse(new InputSource(xml.toURI().toString()), false, false, null, null);
        NodeList nl = doc.getElementsByTagName("file");
        assertEquals(3, nl.getLength());
        assertEquals("docs/My Manager's So-Called \"README\"", ((Element) nl.item(2)).getAttribute("name"));

        File jar = new File(
            new File(new File(target, "platform"), "modules"),
            "org-netbeans-api-annotations-common.jar"
        );
        assertTrue("jar file created", jar.exists());

        File lastM = new File(new File(target, "platform"), ".lastModified");
        assertTrue("Last modified file created", lastM.exists());
    }

    public void testDownloadOSGi() throws Exception {
        AutoUpdate au = new AutoUpdate();
        au.setProject(new Project());
        File install = new File(getWorkDir(), "install");
        install.mkdirs();
        au.setInstallDir(install);
        AutoUpdate.Modules modules = au.createModules();
        modules.setIncludes(".*");
        modules.setClusters("platform|ide");
        File catalog = extractString(
                "<module_updates>\n" +
                " <module codenamebase='org.apache.commons.io' distribution='ide/org-apache-commons-io.jar' downloadsize='109043' targetcluster='ide'>\n" +
                "  <manifest AutoUpdate-Show-In-Client='false' OpenIDE-Module='org.apache.commons.io' OpenIDE-Module-Name='Apache Commons IO Bundle' OpenIDE-Module-Specification-Version='1.4'/>\n" +
                " </module>\n" +
                "</module_updates>\n");
        File bundle = new File(getWorkDir(), "ide/org-apache-commons-io.jar");
        bundle.getParentFile().mkdirs();
        OutputStream os = new FileOutputStream(bundle);
        Manifest m = new Manifest();
        m.getMainAttributes().putValue("Manifest-Version", "1.0");
        m.getMainAttributes().putValue("Bundle-SymbolicName", "org.apache.commons.io");
        new JarOutputStream(os, m).close();
        au.setUpdateCenter(catalog.toURI().toURL());
        au.execute();
        File tracking = new File(install, "ide/update_tracking/org-apache-commons-io.xml");
        assertTrue(tracking.isFile());
        Document doc = XMLUtil.parse(new InputSource(tracking.toURI().toString()), false, false, null, null);
        Set<String> files = new TreeSet<>();
        NodeList nl = doc.getElementsByTagName("file");
        for (int i = 0; i < nl.getLength(); i++) {
            files.add(((Element) nl.item(i)).getAttribute("name"));
        }
        assertEquals("[config/Modules/org-apache-commons-io.xml, modules/org-apache-commons-io.jar]", files.toString());
        assertEquals(bundle.length(), new File(install, "ide/modules/org-apache-commons-io.jar").length());
        File config = new File(install, "ide/config/Modules/org-apache-commons-io.xml");
        assertTrue(config.isFile());
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE module PUBLIC \"-//NetBeans//DTD Module Status 1.0//EN\"\n                        \"http://www.netbeans.org/dtds/module-status-1_0.dtd\">\n<module name=\"org.apache.commons.io\">\n    <param name=\"autoload\">true</param>\n    <param name=\"eager\">false</param>\n    <param name=\"jar\">modules/org-apache-commons-io.jar</param>\n    <param name=\"reloadable\">false</param>\n</module>\n", readFile(config));
    }
    
    public void testDownloadAndExtractExternal() throws Exception {
        File f = new File(getWorkDir(), "org-netbeans-api-annotations-common.xml");
        extractResource(f, "org-netbeans-api-annotations-common.xml");
        
        File ext = extractString("external content");
        
        String extRef =
            "URL: " + ext.toURI().toString() + "\n";

        File nbm = generateNBMContent("org-netbeans-api-annotations-common.nbm",
            "netbeans/config/Modules/org-netbeans-api-annotations-common.xml", "empty",
            "netbeans/modules/org-netbeans-api-annotations-common.jar.external", extRef,
            "netbeans/docs/My Manager's So-Called \"README\"", "empty");

        File target = new File(getWorkDir(), "target");
        target.mkdirs();

        execute(
            "autoupdate.xml", "-verbose", "-Durl=" + f.toURI().toURL(),
            "-Dincludes=org.netbeans.api.annotations.common",
            "-Dtarget=" + target
        );

        File xml = new File(
            new File(new File(target, "platform"), "update_tracking"),
            "org-netbeans-api-annotations-common.xml"
        );
        assertTrue("xml file created", xml.exists());
        Document doc = XMLUtil.parse(new InputSource(xml.toURI().toString()), false, false, null, null);
        NodeList nl = doc.getElementsByTagName("file");
        assertEquals(3, nl.getLength());
        assertEquals("docs/My Manager's So-Called \"README\"", ((Element) nl.item(2)).getAttribute("name"));
        String noExternal = readFile(xml);
        if (noExternal.contains(".external")) {
            fail("There shall be no external:\n" + noExternal);
        }

        File jar = new File(
            new File(new File(target, "platform"), "modules"),
            "org-netbeans-api-annotations-common.jar"
        );
        File jarExt = new File(
            new File(new File(target, "platform"), "modules"),
            "org-netbeans-api-annotations-common.jar.external"
        );
        assertFalse("no external file created", jarExt.exists());
        assertTrue("jar file created", jar.exists());
        assertEquals("Contains expected", "external content", readFile(jar));

        File lastM = new File(new File(target, "platform"), ".lastModified");
        assertTrue("Last modified file created", lastM.exists());
    }

    public void testDownloadAndExtractExternalWithFirstURLBroken() throws Exception {
        File f = new File(getWorkDir(), "org-netbeans-api-annotations-common.xml");
        extractResource(f, "org-netbeans-api-annotations-common.xml");
        
        File ext = extractString("external content");
        BufferedInputStream bsrc = null;
        CRC32 crc = new CRC32();
        try {
            bsrc = new BufferedInputStream( new FileInputStream( ext ) );
            byte[] bytes = new byte[1024];
            int i;
            while( (i = bsrc.read(bytes)) != -1 ) {
                crc.update(bytes, 0, i );
            }
        }
        finally {
            if ( bsrc != null )
                bsrc.close();
        }
        
        
        String extRef =
            "CRC: " + crc.getValue() + "\n" +
            "URL: file:/I/dont/exist/At/All\n" +
            "URL: " + ext.toURI().toString() + "\n";

        File nbm = generateNBMContent("org-netbeans-api-annotations-common.nbm",
            "netbeans/config/Modules/org-netbeans-api-annotations-common.xml", "empty",
            "netbeans/modules/org-netbeans-api-annotations-common.jar.external", extRef,
            "netbeans/docs/My Manager's So-Called \"README\"", "empty");

        File target = new File(getWorkDir(), "target");
        target.mkdirs();

        execute(
            "autoupdate.xml", "-verbose", "-Durl=" + f.toURI().toURL(),
            "-Dincludes=org.netbeans.api.annotations.common",
            "-Dtarget=" + target
        );

        File xml = new File(
            new File(new File(target, "platform"), "update_tracking"),
            "org-netbeans-api-annotations-common.xml"
        );
        assertTrue("xml file created", xml.exists());
        Document doc = XMLUtil.parse(new InputSource(xml.toURI().toString()), false, false, null, null);
        NodeList nl = doc.getElementsByTagName("file");
        assertEquals(3, nl.getLength());
        assertEquals("docs/My Manager's So-Called \"README\"", ((Element) nl.item(2)).getAttribute("name"));

        File jar = new File(
            new File(new File(target, "platform"), "modules"),
            "org-netbeans-api-annotations-common.jar"
        );
        File jarExt = new File(
            new File(new File(target, "platform"), "modules"),
            "org-netbeans-api-annotations-common.jar.external"
        );
        assertFalse("no external file created", jarExt.exists());
        assertTrue("jar file created", jar.exists());
        assertEquals("Contains expected", "external content", readFile(jar));

        File lastM = new File(new File(target, "platform"), ".lastModified");
        assertTrue("Last modified file created", lastM.exists());
    }

    public void testFailOnWrongCRCExternal() throws Exception {
        File f = new File(getWorkDir(), "org-netbeans-api-annotations-common.xml");
        extractResource(f, "org-netbeans-api-annotations-common.xml");
        
        File ext = extractString("external content");
        
        String extRef =
            "CRC: 42\n" +
            "URL: " + ext.toURI().toString() + "\n";

        File nbm = generateNBMContent("org-netbeans-api-annotations-common.nbm",
            "netbeans/config/Modules/org-netbeans-api-annotations-common.xml", "empty",
            "netbeans/modules/org-netbeans-api-annotations-common.jar.external", extRef,
            "netbeans/docs/My Manager's So-Called \"README\"", "empty");

        File target = new File(getWorkDir(), "target");
        target.mkdirs();

        try {
            execute(
                "autoupdate.xml", "-verbose", "-Durl=" + f.toURI().toURL(),
                "-Dincludes=org.netbeans.api.annotations.common",
                "-Dtarget=" + target
            );
            fail("Execution shall fail, as the CRC is wrong");
        } catch (ExecutionError ok) {
            // OK
        }
    }
    
    public void testDownloadAndExtractExternalWithProperty() throws Exception {
        File f = new File(getWorkDir(), "org-netbeans-api-annotations-common.xml");
        extractResource(f, "org-netbeans-api-annotations-common.xml");
        
        File ext = extractString("external content");
        System.setProperty("my.ref", ext.getParent());
        
        String extRef =
            "URL: file:${my.ref}/" + ext.getName() + "\n";

        File nbm = generateNBMContent("org-netbeans-api-annotations-common.nbm",
            "netbeans/config/Modules/org-netbeans-api-annotations-common.xml", "empty",
            "netbeans/modules/org-netbeans-api-annotations-common.jar.external", extRef,
            "netbeans/docs/My Manager's So-Called \"README\"", "empty");

        File target = new File(getWorkDir(), "target");
        target.mkdirs();

        execute(
            "autoupdate.xml", "-verbose", "-Durl=" + f.toURI().toURL(),
            "-Dincludes=org.netbeans.api.annotations.common",
            "-Dtarget=" + target
        );

        File xml = new File(
            new File(new File(target, "platform"), "update_tracking"),
            "org-netbeans-api-annotations-common.xml"
        );
        assertTrue("xml file created", xml.exists());
        Document doc = XMLUtil.parse(new InputSource(xml.toURI().toString()), false, false, null, null);
        NodeList nl = doc.getElementsByTagName("file");
        assertEquals(3, nl.getLength());
        assertEquals("docs/My Manager's So-Called \"README\"", ((Element) nl.item(2)).getAttribute("name"));

        File jar = new File(
            new File(new File(target, "platform"), "modules"),
            "org-netbeans-api-annotations-common.jar"
        );
        File jarExt = new File(
            new File(new File(target, "platform"), "modules"),
            "org-netbeans-api-annotations-common.jar.external"
        );
        assertFalse("no external file created", jarExt.exists());
        assertTrue("jar file created", jar.exists());
        assertEquals("Contains expected", "external content", readFile(jar));

        File lastM = new File(new File(target, "platform"), ".lastModified");
        assertTrue("Last modified file created", lastM.exists());
    }

    public void testUpdateAlreadyInstalled() throws Exception {
        File f = new File(getWorkDir(), "org-netbeans-api-annotations-common.xml");
        extractResource(f, "org-netbeans-api-annotations-common.xml");

        File nbm = generateNBM("org-netbeans-api-annotations-common.nbm",
            "netbeans/config/Modules/org-netbeans-api-annotations-common.xml",
            "netbeans/modules/org-netbeans-api-annotations-common.jar");

        File target = new File(getWorkDir(), "target");
        target.mkdirs();
        File m = new File(
            new File(new File(target, "platformXY"), "modules"),
            "org-netbeans-api-annotations-common.jar"
        );
        m.getParentFile().mkdirs();
        m.createNewFile();

        File y = new File(
            new File(new File(new File(target, "platformXY"), "config"), "Modules"),
            "org-netbeans-api-annotations-common.xml"
        );
        y.getParentFile().mkdirs();
        File x = new File(
            new File(new File(target, "platformXY"), "update_tracking"),
            "org-netbeans-api-annotations-common.xml"
        );
        y.createNewFile();
        
        x.getParentFile().mkdirs();
        String txtx =
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<module codename=\"org.netbeans.api.annotations.common/1\">\n" +
"    <module_version install_time=\"10\" specification_version=\"1.3\">\n" +
"       <file crc=\"1\" name=\"config/Modules/org-netbeans-api-annotations-common.xml\"/>\n" +
"       <file crc=\"2\" name=\"modules/org-netbeans-api-annotations-common.jar\"/>\n" +
"    </module_version>\n" +
"</module>\n";


        try (FileOutputStream osx = new FileOutputStream(x)) {
            osx.write(txtx.getBytes());
        }


        execute(
            "autoupdate.xml", "-verbose", "-Durl=" + f.toURI().toURL(),
            "-Dincludes=org.netbeans.api.annotations.common",
            "-Dtarget=" + target + File.separator + "platformXY", "cluster"
        );

        File xml = new File(
            new File(new File(new File(target, "platformXY"), "config"), "Modules"),
            "org-netbeans-api-annotations-common.xml"
        );
        assertTrue("xml file created:\n" + getStdOut(), xml.exists());

        File jar = new File(
            new File(new File(target, "platformXY"), "modules"),
            "org-netbeans-api-annotations-common.jar"
        );
        assertTrue("jar file created", jar.exists());

        if (getStdOut().contains("Writing ")) {
            fail("No writes, the module is already installed:\n" + getStdOut());
        }
    }

    public void testUpdateOldButMissCluster() throws Exception {
        File f = new File(getWorkDir(), "org-netbeans-api-annotations-common.xml");
        extractResource(f, "org-netbeans-api-annotations-common.xml");

        File nbm = generateNBM("org-netbeans-api-annotations-common.nbm",
            "netbeans/config/Modules/org-netbeans-api-annotations-common.xml",
            "netbeans/modules/org-netbeans-api-annotations-common.jar");

        File target = new File(getWorkDir(), "target");
        target.mkdirs();
        File m = new File(
            new File(new File(target, "platformXY"), "modules"),
            "org-netbeans-api-annotations-common.jar"
        );
        m.getParentFile().mkdirs();
        m.createNewFile();

        File y = new File(
            new File(new File(new File(target, "platformXY"), "config"), "Modules"),
            "org-netbeans-api-annotations-common.xml"
        );
        y.getParentFile().mkdirs();
        File x = new File(
            new File(new File(target, "platformXY"), "update_tracking"),
            "org-netbeans-api-annotations-common.xml"
        );
        y.createNewFile();

        x.getParentFile().mkdirs();
        String txtx =
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<module codename=\"org.netbeans.api.annotations.common/1\">\n" +
"    <module_version install_time=\"10\" specification_version=\"1.0.3\">\n" +
"       <file crc=\"1\" name=\"config/Modules/org-netbeans-api-annotations-common.xml\"/>\n" +
"       <file crc=\"2\" name=\"modules/org-netbeans-api-annotations-common.jar\"/>\n" +
"    </module_version>\n" +
"</module>\n";


        try (FileOutputStream osx = new FileOutputStream(x)) {
            osx.write(txtx.getBytes());
        }


        execute(
            "autoupdate.xml", "-verbose", "-Durl=" + f.toURI().toURL(),
            "-Dincludes=org.netbeans.api.annotations.common",
            "-Dtarget=" + target + File.separator + "platformXY",
            "cluster-select",
            "-Dcluster=non.*existing"
        );

        File xml = new File(
            new File(new File(new File(target, "platformXY"), "config"), "Modules"),
            "org-netbeans-api-annotations-common.xml"
        );
        assertTrue("xml file created:\n" + getStdOut(), xml.exists());

        File jar = new File(
            new File(new File(target, "platformXY"), "modules"),
            "org-netbeans-api-annotations-common.jar"
        );
        assertTrue("jar file created", jar.exists());

        if (getStdOut().contains("Writing ")) {
            fail("No writes, the module is already installed:\n" + getStdOut());
        }
    }

    public void testUpdateAlreadyInstalledAndOld() throws Exception {
        File f = new File(getWorkDir(), "org-netbeans-api-annotations-common.xml");
        extractResource(f, "org-netbeans-api-annotations-common.xml");

        File nbm = generateNBM("org-netbeans-api-annotations-common.nbm",
            "netbeans/config/Modules/org-netbeans-api-annotations-common.xml",
            "netbeans/modules/org-netbeans-api-annotations-common.jar");

        File target = new File(getWorkDir(), "target");
        target.mkdirs();
        File m = new File(
            new File(new File(target, "platform"), "modules"),
            "org-netbeans-api-annotations-common.jar"
        );
        m.getParentFile().mkdirs();
        m.createNewFile();
        File e = new File(
            new File(new File(new File(target, "platform"), "modules"), "ext"),
            "extra.jar"
        );
        e.getParentFile().mkdirs();
        e.createNewFile();

        File x = new File(
            new File(new File(target, "platform"), "update_tracking"),
            "org-netbeans-api-annotations-common.xml"
        );
        x.getParentFile().mkdirs();
        String txtx =
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<module codename=\"org.netbeans.api.annotations.common\">\n" +
"    <module_version install_time=\"10\" specification_version=\"1.0.3\">\n" +
"       <file crc=\"1\" name=\"config/Modules/org-netbeans-api-annotations-common.xml\"/>\n" +
"       <file crc=\"2\" name=\"modules/org-netbeans-api-annotations-common.jar\"/>\n" +
"       <file crc=\"3\" name=\"modules/ext/extra.jar\"/>\n" +
"    </module_version>\n" +
"</module>\n";


        try (FileOutputStream osx = new FileOutputStream(x)) {
            osx.write(txtx.getBytes());
        }

        File lastM = new File(new File(target, "platform"), ".lastModified");
        lastM.createNewFile();

        Thread.sleep(1000);
        long last = x.lastModified();
        Thread.sleep(500);

        execute(
            "autoupdate.xml", "-verbose", "-Durl=" + f.toURI().toURL(),
            "-Dincludes=org.netbeans.api.annotations.common",
            "-Dtarget=" + target
        );

        File xml = new File(
            new File(new File(new File(target, "platform"), "config"), "Modules"),
            "org-netbeans-api-annotations-common.xml"
        );
        assertTrue("xml file created", xml.exists());

        File jar = new File(
            new File(new File(target, "platform"), "modules"),
            "org-netbeans-api-annotations-common.jar"
        );
        assertTrue("jar file created", jar.exists());

        if (!getStdOut().contains("Writing ")) {
            fail("Writes should be there:\n" + getStdOut());
        }

        if (last >= jar.lastModified()) {
            fail("Newer timestamp for " + jar);
        }

        assertFalse("extra file has been deleted", e.exists());

        if (last >= lastM.lastModified()) {
            fail(".lastModified file shall be touched");
        }
    }


    public void testUpdateAlreadyOldButForce() throws Exception {
        File f = new File(getWorkDir(), "org-netbeans-api-annotations-common.xml");
        extractResource(f, "org-netbeans-api-annotations-common.xml");

        File nbm = generateNBM("org-netbeans-api-annotations-common.nbm",
            "netbeans/config/Modules/org-netbeans-api-annotations-common.xml",
            "netbeans/modules/org-netbeans-api-annotations-common.jar");

        File target = new File(getWorkDir(), "target");
        target.mkdirs();
        File m = new File(
            new File(new File(target, "platform"), "modules"),
            "org-netbeans-api-annotations-common.jar"
        );
        m.getParentFile().mkdirs();
        m.createNewFile();
        File e = new File(
            new File(new File(new File(target, "platform"), "modules"), "ext"),
            "extra.jar"
        );
        e.getParentFile().mkdirs();
        e.createNewFile();

        File x = new File(
            new File(new File(target, "platform"), "update_tracking"),
            "org-netbeans-api-annotations-common.xml"
        );
        x.getParentFile().mkdirs();
        String txtx =
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<module codename=\"org.netbeans.api.annotations.common\">\n" +
"    <module_version install_time=\"10\" specification_version=\"2.0.3\">\n" +
"       <file crc=\"1\" name=\"config/Modules/org-netbeans-api-annotations-common.xml\"/>\n" +
"       <file crc=\"2\" name=\"modules/org-netbeans-api-annotations-common.jar\"/>\n" +
"       <file crc=\"3\" name=\"modules/ext/extra.jar\"/>\n" +
"    </module_version>\n" +
"</module>\n";


        try (FileOutputStream osx = new FileOutputStream(x)) {
            osx.write(txtx.getBytes());
        }

        File lastM = new File(new File(target, "platform"), ".lastModified");
        lastM.createNewFile();

        Thread.sleep(1000);
        long last = x.lastModified();
        Thread.sleep(500);

        execute(
            "autoupdate.xml", "-verbose", "-Durl=" + f.toURI().toURL(),
            "-Dincludes=org.netbeans.api.annotations.common",
            "-Dtarget=" + target,
            "-Dforce=true"
        );

        File xml = new File(
            new File(new File(new File(target, "platform"), "config"), "Modules"),
            "org-netbeans-api-annotations-common.xml"
        );
        assertTrue("xml file created", xml.exists());

        File jar = new File(
            new File(new File(target, "platform"), "modules"),
            "org-netbeans-api-annotations-common.jar"
        );
        assertTrue("jar file created", jar.exists());

        if (!getStdOut().contains("Writing ")) {
            fail("Writes should be there:\n" + getStdOut());
        }

        if (last >= jar.lastModified()) {
            fail("Newer timestamp for " + jar);
        }

        assertFalse("extra file has been deleted", e.exists());

        if (last >= lastM.lastModified()) {
            fail(".lastModified file shall be touched");
        }
    }

    public File generateNBM (String name, String... files) throws IOException {
        List<String> filesAndContent = new ArrayList<>();
        for (String s : files) {
            filesAndContent.add(s);
            filesAndContent.add("empty");
        }
        return generateNBMContent(name, filesAndContent.toArray(new String[0]));
    }
    public File generateNBMContent (String name, String... filesAndContent) throws IOException {
        File f = new File (getWorkDir (), name);

        try (ZipOutputStream os = new ZipOutputStream (new FileOutputStream (f))) {
            for (int i = 0; i < filesAndContent.length; i += 2) {
                os.putNextEntry(new ZipEntry(filesAndContent[i]));
                os.write(filesAndContent[i + 1].getBytes());
                os.closeEntry();
            }
            os.putNextEntry(new ZipEntry("Info/info.xml"));
            String codeName = name.replaceAll("\\.nbm$", "").replace('-', '.');
            os.write(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<!DOCTYPE module_updates PUBLIC \"-//NetBeans//DTD Autoupdate Info 2.5//EN\" \"http://www.netbeans.org/dtds/autoupdate-info-2_5.dtd\">\n").getBytes());
            String res = "<module codenamebase=\"" + codeName + "\" " +
                    "homepage=\"http://au.netbeans.org/\" distribution=\"wrong-path.hbm\" " +
                    "license=\"standard-nbm-license.txt\" downloadsize=\"98765\" " +
                    "needsrestart=\"false\" moduleauthor=\"\" " +
                    "eager=\"false\" " +
                    "releasedate=\"2006/02/23\">";
            res +=  "<manifest OpenIDE-Module=\"" + codeName + "\" " +
                    "OpenIDE-Module-Name=\"" + codeName + "\" " +
                    "OpenIDE-Module-Specification-Version=\"333.3\"/>";
            res += "</module>";
            os.write(res.getBytes());
            os.closeEntry();
        }

        return f;
    }

}
