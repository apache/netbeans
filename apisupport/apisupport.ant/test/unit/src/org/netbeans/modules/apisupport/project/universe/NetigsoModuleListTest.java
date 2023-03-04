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

package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.ui.customizer.ClusterInfo;
import org.openide.util.test.TestFileUtils;

/**
 * Test functionality of ModuleList needed by Netigso.
 * @author Jesse Glick
 */
public class NetigsoModuleListTest extends TestBase {
    
    public NetigsoModuleListTest(String name) {
        super(name);
    }

    public void testParseConfigXML() throws Exception {
        File c = new File(new File(getWorkDir(), "config"), "Modules");
        c.mkdirs();
        File j = new File(new File(getWorkDir(), "subdir"), "x-y-z.jar");
        j.getParentFile().mkdirs();
        Manifest mf = new Manifest();
        mf.getMainAttributes().putValue("Manifest-Version", "1.0");
        mf.getMainAttributes().putValue("Bundle-SymbolicName", "x.y.z;singleton:=true");
        JarOutputStream os = new JarOutputStream(new FileOutputStream(j), mf);
        os.close();

        String cnt = "<?xml version='1.0' encoding='UTF-8'?>" +
                "<!DOCTYPE module PUBLIC '-//NetBeans//DTD Module Status 1.0//EN'" +
                "   'http://www.netbeans.org/dtds/module-status-1_0.dtd'>" +
                "<module name='x.y.z'>" +
                "  <param name='autoload'>true</param>" +
                "  <param name='eager'>false</param>" +
                "  <param name='jar'>subdir/x-y-z.jar</param>" +
                "</module>";
        TestFileUtils.writeFile(new File(c, "x-y-z.xml"), cnt);

        ClusterInfo ci = ClusterInfo.createExternal(getWorkDir(), new URL[0], new URL[0], true);
        ModuleList ml = ModuleList.scanCluster(getWorkDir(), getWorkDir(), true, ci);

        assertEquals("One found", 1, ml.getAllEntries().size());
        ModuleEntry me = ml.getEntry("x.y.z");
        assertNotNull("Correct entry found", me);
        assertEquals("Cnb is OK", "x.y.z", me.getCodeNameBase());
        assertEquals("Jar file OK", j, me.getJarLocation());
    }


}
