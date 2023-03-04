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
package org.netbeans.modules.netbinox;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.MockEvents;
import org.netbeans.MockModuleInstaller;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.core.netigso.NetigsoServicesTest;
import org.netbeans.core.startup.Main;
import org.netbeans.core.startup.ModuleSystem;
import org.netbeans.junit.RandomlyFails;
import org.osgi.framework.Bundle;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class BundleResourceTest extends NetigsoHid {
    private static final Logger LOG = Logger.getLogger(BundleResourceTest.class.getName());

    public BundleResourceTest(String name) {
        super(name);
    }

    @RandomlyFails
    public void testBundleResourceProtocol() throws Exception {
        ModuleSystem ms = Main.getModuleSystem();
        ModuleManager mgr = ms.getManager();
        mgr.mutexPrivileged().enterWriteAccess();
        HashSet<Module> both = null;
        try {
            String mfBar = "Bundle-SymbolicName: org.bar\n" +
                "Bundle-Version: 1.1.0\n" +
                "Bundle-ManifestVersion: 2\n" +
                "Export-Package: org.bar\n" +
                "Require-Bundle: org.foo;bundle-version=\"[100.0,102.0)\"\n" +
                "\n\n";

            File j1 = new File(jars, "simple-module.jar");
            File j2 = changeManifest(new File(jars, "depends-on-simple-module.jar"), mfBar);
            LOG.log(Level.INFO, "Create {0}", j2);
            Module m1 = mgr.create(j1, null, false, false, false);
            LOG.log(Level.INFO, "Create {0}", j2);
            Module m2 = mgr.create(j2, null, false, false, false);
            HashSet<Module> b = new HashSet<Module>(Arrays.asList(m1, m2));
            LOG.log(Level.INFO, "enable {0}", b);
            mgr.enable(b);
            LOG.info("enabled Ok");
            both = b;

            Bundle bundle = NetigsoServicesTest.findBundle("org.foo");
            LOG.log(Level.INFO, "bundle org.foo: {0}", bundle);
            URL root = bundle.getEntry("/");
            LOG.log(Level.INFO, "Root entry {0}", root);
            assertNotNull("Root URL found", root);
            URL resRoot = new URL("bundleresource", root.getHost(), root.getPath());
            LOG.log(Level.INFO, "res root: {0}", resRoot);
            URL helloRes = new URL(resRoot, "org/foo/hello.txt");
            LOG.log(Level.INFO, "Assert content {0}", helloRes);
            assertURLContent(helloRes, "Hello resources!");
        } finally {
            LOG.info("Finally block started");
            if (both != null) {
                mgr.disable(both);
            }
            mgr.mutexPrivileged().exitWriteAccess();
            LOG.info("Finally block finished");
        }
    }

    public static void assertURLContent(URL txt, String hello) throws IOException {
        assertNotNull("URL found", txt);
        URLConnection c = txt.openConnection();
        InputStream is = c.getInputStream();
        byte[] arr = new byte[400];
        int len = is.read(arr);
        assertTrue(len > 0);
        assertEquals(hello, new String(arr, 0, len, StandardCharsets.UTF_8));
    }

}
