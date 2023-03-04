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
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

/**
 * Test {@link ParseProjectXml}.
 * @author Jaroslav Tulach
 */
public class ParseProjectXmlTest extends TestBase {
    public ParseProjectXmlTest(String name) {
        super(name);
    }

    private File nball;

    private File file(File root, String relpath) {
        return new File(root, relpath.replace('/', File.separatorChar));
    }
    
    private String filePath(File root, String relpath) {
        return file(root, relpath).getAbsolutePath();
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        String prop = System.getProperty("nb_all");
        assertNotNull("${nb_all} defined", prop);
        nball = new File(prop);
        new File(nball, "nbbuild/nbproject/private/scan-cache-full.ser").delete();
        new File(nball, "nbbuild/nbproject/private/scan-cache-standard.ser").delete();
    }

    public void testScanBinariesForOSGi() throws Exception {
        doScanBinariesForOSGi("osgi", "netigso.test");
    }

    public void testScanBinariesForOSGiInModulesDir() throws Exception {
        doScanBinariesForOSGi("modules", "netigso.test_repackaged");
    }

    public void testScanBinariesForOSGiWithDash() throws Exception {
        doScanBinariesForOSGi("osgi", "netigso.test-dash");
    }

    private void doScanBinariesForOSGi(String whereTo, String cnb) throws Exception {
        Project fakeproj = new Project();
        fakeproj.addBuildListener(new BuildListener() {
            public void messageLogged(BuildEvent buildEvent) {
                if (buildEvent.getPriority() <= Project.MSG_VERBOSE) {
                    System.err.println(buildEvent.getMessage());
                }
            }
            public void taskStarted(BuildEvent buildEvent) {}
            public void taskFinished(BuildEvent buildEvent) {}
            public void targetStarted(BuildEvent buildEvent) {}
            public void targetFinished(BuildEvent buildEvent) {}
            public void buildStarted(BuildEvent buildEvent) {}
            public void buildFinished(BuildEvent buildEvent) {}
        });

        File osgiRepo = new File(getWorkDir(), whereTo);
        osgiRepo.mkdirs();
        Manifest man = createManifest();
        man.getMainAttributes().putValue("Bundle-SymbolicName", cnb);
        man.getMainAttributes().putValue("Bundle-Version", "7.0.1.Prelude");
        String dashCnb = cnb.replace('-', '_').replace('.', '-');
        generateJar(new File(osgiRepo, dashCnb + ".jar"), new String[0], man);

        CreateModuleXML cmxml = new CreateModuleXML();
        cmxml.setProject(fakeproj);
        final File configDir = new File(new File(getWorkDir(), "config"), "Modules");
        configDir.mkdirs();
        cmxml.setXmldir(configDir);
        FileSet fs = new FileSet();
        fs.setDir(getWorkDir());
        fs.setIncludes("**/*.jar");
        cmxml.addAutoload(fs);
        cmxml.execute();

        String[] arr = configDir.list();
        assertEquals("One file generated", 1, arr.length);
        assertEquals(dashCnb + ".xml", arr[0]);

        fakeproj.setProperty("cluster.path.final", filePath(nball, "nbbuild/netbeans/platform")
                + File.pathSeparator + getWorkDir());
        final String prj = filePath(nball, "apisupport/apisupport.ant/test/unit/data/example-external-projects/suite1/action-project");
        fakeproj.setProperty("basedir",prj);
        fakeproj.setProperty("suite.dir", filePath(nball, "apisupport/apisupport.ant/test/unit/data/example-external-projects/suite1"));
        long start = System.currentTimeMillis();

        ParseProjectXml p = new ParseProjectXml();
        p.setProject(fakeproj);
        p.setProject(new File(prj));
        p.setModuleClassPathProperty("path");
        String prjXML = "<?xml version='1.0' encoding='UTF-8'?>" +
                "<project xmlns='http://www.netbeans.org/ns/project/1'>" +
                "<type>org.netbeans.modules.apisupport.project</type>" +
                "<configuration>" +
                "<data xmlns='http://www.netbeans.org/ns/nb-module-project/2'>" +
                "<code-name-base>org.netbeans.examples.modules.action</code-name-base>" +
                "<suite-component/>" +
                "<module-dependencies>" +
                "<dependency>" +
                "  <code-name-base>" + cnb + "</code-name-base>" +
                "  <build-prerequisite/>" +
                "  <compile-dependency/>" +
                "  <run-dependency>" +
                "    <specification-version>6.2</specification-version>" +
                "  </run-dependency>" +
                "</dependency>" +
                "</module-dependencies>" +
                "<public-packages/>" +
                "</data>" +
                "</configuration>" +
                "</project>";
        p.setProjectFile(extractFile(prjXML, "project.xml"));
        p.execute();
        String path = p.getProject().getProperty("path");
        if (!path.contains(dashCnb)) {
            fail(path);
        }
    }

    private File extractFile(String content, String fileName) throws IOException {
        File f = new File(getWorkDir(),fileName);
        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write(content.getBytes("UTF-8"));
        }
        return f;
    }

    private File generateJar (File f, String[] content, Manifest manifest) throws IOException {
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
