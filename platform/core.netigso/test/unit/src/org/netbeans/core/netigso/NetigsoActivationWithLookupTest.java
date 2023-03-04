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

package org.netbeans.core.netigso;

import java.awt.Component;
import org.netbeans.core.startup.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.SetupHid;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 * Do we correctly call the BundleActivators?
 *
 * @author Jaroslav Tulach
 */
public class NetigsoActivationWithLookupTest extends SetupHid {
    public NetigsoActivationWithLookupTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        clearWorkDir();
        File ud = new File(getWorkDir(), "ud");
        ud.mkdirs();
        System.setProperty("netbeans.user", ud.getPath());
        
        data = new File(getDataDir(), "jars");
        jars = new File(getWorkDir(), "jars");
        jars.mkdirs();
        File activate = createTestJAR("activate", null);

        File lkp = Utilities.toFile(Lookup.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        File register = createTestJAR("register", null, lkp);

        registerModule(ud, activate, "org.activate");
        registerModule(ud, register, "org.register");
    }

    public void testThatTriesToLookupDuringActivation() throws Exception {
        System.setProperty("start.class", Impl.class.getName());
        ModuleSystem ms = Main.getModuleSystem();
        assertEquals("OK", System.getProperty("lookup.status"));
    }
    private File createTestJAR(String name, String srcdir, File... classpath) throws IOException {
        return createTestJAR(data, jars, name, srcdir, classpath);
    }

    private static void registerModule(File ud, File jar, String cnb) throws Exception {
        File md = new File(new File(ud, "config"), "Modules");
        md.mkdirs();
        FileOutputStream os = new FileOutputStream(new File(md, cnb.replace('.', '-') + ".xml"));
        os.write((
"<?xml version='1.0' encoding='UTF-8'?>\n" +
"<!DOCTYPE module PUBLIC '-//NetBeans//DTD Module Status 1.0//EN' 'http://www.netbeans.org/dtds/module-status-1_0.dtd'>\n" +
"<module name='" + cnb + "'>\n" +
"    <param name='autoload'>false</param>\n" +
"    <param name='eager'>false</param>\n" +
"    <param name='enabled'>true</param>\n" +
"    <param name='reloadable'>false</param>\n" +
"    <param name='jar'>" + jar + "</param>\n" +
"</module>\n"
        ).getBytes());
        os.close();
    }

    public static final class Impl extends Object {
        public Impl() throws Exception {
            Component res = Lookup.getDefault().lookup(java.awt.Component.class);
            assertNotNull("result found", res);
            assertEquals("Class name is Main", "Main", res.getClass().getSimpleName());
            // OK
            System.setProperty("lookup.status", "OK");
        }
    }
}
