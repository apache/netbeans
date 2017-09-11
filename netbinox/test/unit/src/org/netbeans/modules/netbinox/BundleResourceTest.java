/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Oracle, Inc.
 */
package org.netbeans.modules.netbinox;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
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
        assertEquals(hello, new String(arr, 0, len, "UTF-8"));
    }

}
