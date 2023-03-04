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
import java.io.FileWriter;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.types.Parameter;


/** Check behaviour of ModuleStateSelector.
 *
 * @author Jaroslav Tulach
 */
public class ModuleStateSelectorTest extends TestBase {
    private ModuleStateSelector selector;
    
    public ModuleStateSelectorTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        selector = new ModuleStateSelector();
    }

    public void testModuleRejectedIfNotEager() throws Exception {
        File cfg = new File(new File(getWorkDir(), "config"), "Modules");
        cfg.mkdirs();
        assertTrue("Created", cfg.isDirectory());
        
        Manifest m = createManifest ();
        m.getMainAttributes().putValue("OpenIDE-Module", "org.my.module");
        File aModule = generateJar("org.my.module", new String[0], m);
        
        File trackingFile = new File(cfg, "org-my-module.xml");
        try (FileWriter w = new FileWriter(trackingFile)) {
            w.write(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "<!DOCTYPE module PUBLIC \"-//NetBeans//DTD Module Status 1.0//EN\"\n" +
                            "                        \"http://www.netbeans.org/dtds/module-status-1_0.dtd\">\n" +
                            "<module name=\"org.my.module\">\n" +
                            "    <param name=\"autoload\">true</param>\n" +
                            "    <param name=\"eager\">false</param>\n" +
                            "    <param name=\"jar\">modules/org-openide-awt.jar</param>\n" +
                            "    <param name=\"reloadable\">false</param>\n" +
                            "    <param name=\"specversion\">7.4.0.1</param>\n" +
                            "</module>\n"
            );
        }

        Parameter p = new Parameter();
        p.setName("acceptEager");
        p.setValue("true");
        Parameter p2 = new Parameter();
        p2.setName("acceptAutoload");
        p2.setValue("false");
        selector.setParameters(new Parameter[] { p, p2 });
        
        assertFalse("module not accepted", selector.isSelected(getWorkDir(), aModule.toString(), aModule));
    }
    
    public void testModuleAcceptedIfEager() throws Exception {
        File cfg = new File(new File(getWorkDir(), "config"), "Modules");
        cfg.mkdirs();
        assertTrue("Created", cfg.isDirectory());

        Manifest m = createManifest ();
        m.getMainAttributes().putValue("OpenIDE-Module", "org.my.module/1");
        File aModule = generateJar("org.my.module", new String[0], m);

        File trackingFile = new File(cfg, "org-my-module.xml");
        try (FileWriter w = new FileWriter(trackingFile)) {
            w.write(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "<!DOCTYPE module PUBLIC \"-//NetBeans//DTD Module Status 1.0//EN\"\n" +
                            "                        \"http://www.netbeans.org/dtds/module-status-1_0.dtd\">\n" +
                            "<module name=\"org.my.module\">\n" +
                            "    <param name=\"autoload\">false</param>\n" +
                            "    <param name=\"eager\">true</param>\n" +
                            "    <param name=\"jar\">modules/org-openide-awt.jar</param>\n" +
                            "    <param name=\"reloadable\">false</param>\n" +
                            "    <param name=\"specversion\">7.4.0.1</param>\n" +
                            "</module>\n"
            );
        }

        Parameter p = new Parameter();
        p.setName("acceptEager");
        p.setValue("true");
        Parameter p2 = new Parameter();
        p2.setName("acceptAutoload");
        p2.setValue("false");
        selector.setParameters(new Parameter[] { p, p2 });

        assertTrue("module accepted", selector.isSelected(getWorkDir(), aModule.toString(), aModule));
    }
    
    private File createNewJarFile (String cnb) throws IOException {
        File f = new File (new File(this.getWorkDir(), "modules"), cnb.replace('.', '-') + ".jar");
        f.delete();
        f.deleteOnExit();
        return f;
    }

    protected final File generateJar (String cnb, String[] content, Manifest manifest) throws IOException {
        File f = createNewJarFile (cnb);
        f.getParentFile().mkdirs();
        
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
