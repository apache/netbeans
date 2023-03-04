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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.NetigsoFramework;
import org.netbeans.SetupHid;
import org.netbeans.core.startup.Main;
import org.netbeans.core.startup.ModuleSystem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Item;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.launch.Framework;

/**
 * How does OSGi integration deals with layer registration?
 *
 * @author Jaroslav Tulach
 */
public class NetigsoServicesTest extends SetupHid implements LookupListener {
    private static Module m1;
    private static ModuleManager mgr;

    private int cnt;

    public NetigsoServicesTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        clearWorkDir();

        
        data = new File(getDataDir(), "jars");
        jars = new File(getWorkDir(), "jars");
        jars.mkdirs();
        File simpleModule = createTestJAR("simple-module", null);
        File dependsOnSimpleModule = createTestJAR("depends-on-simple-module", null, simpleModule);

        if (System.getProperty("netbeans.user") == null) {
            File ud = new File(getWorkDir(), "ud");
            ud.mkdirs();

            System.setProperty("netbeans.user", ud.getPath());


            ModuleSystem ms = Main.getModuleSystem();
            mgr = ms.getManager();
            mgr.mutexPrivileged().enterWriteAccess();
            try {
                File j1 = new File(jars, "simple-module.jar");
                m1 = mgr.create(j1, null, false, false, false);
                mgr.enable(Collections.<Module>singleton(m1));
            } finally {
                mgr.mutexPrivileged().exitWriteAccess();
            }
        }

    }
    private File createTestJAR(String name, String srcdir, File... classpath) throws IOException {
        return createTestJAR(data, jars, name, srcdir, classpath);
    }
    public void testOSGiServicesVisibleInLookup() throws Exception {
        mgr.mutexPrivileged().enterWriteAccess();
        FileObject fo;
        try {
            String mfBar = "Bundle-SymbolicName: org.bar\n" +
                "Bundle-Version: 1.1.0\n" +
                "Bundle-ManifestVersion: 2\n" +
                "Import-Package: org.foo\n" +
                "\n\n";

            File j2 = changeManifest(new File(jars, "depends-on-simple-module.jar"), mfBar);
            Module m2 = mgr.create(j2, null, false, false, false);
            mgr.enable(m2);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }

        Bundle b = findBundle("org.bar");
        assertNotNull("Bundle really found", b);
        IOException s = new IOException();
        Hashtable dict = new Hashtable();
        dict.put(Constants.SERVICE_DESCRIPTION, "tristatricettri");
        ServiceRegistration sr = b.getBundleContext().registerService(IOException.class.getName(), s, dict);
        assertBundles("Nobody is using the service yet", 0, sr.getReference().getUsingBundles());
        IOException found = Lookup.getDefault().lookup(IOException.class);
        assertNotNull("Result really found", found);
        assertBundles("Someone is using the service now", 1, sr.getReference().getUsingBundles());
        Result<IOException> res = Lookup.getDefault().lookupResult(IOException.class);
        res.addLookupListener(this);
        assertEquals("One instance found", 1, res.allInstances().size());
        
        Collection<? extends Item<IOException>> items = res.allItems();
        assertEquals("One item found: " + items, 1, items.size());
        Item<IOException> first = items.iterator().next();
        String expectedServiceID = "OSGiService[" + sr.getReference().getProperty(Constants.SERVICE_ID) + "]";
        assertEquals("Proper ID", expectedServiceID, first.getId());
        assertEquals("Right display name", "tristatricettri", first.getDisplayName());
        
        sr.unregister();
        IOException notFound = Lookup.getDefault().lookup(IOException.class);
        assertNull("Result not found", notFound);
        assertEquals("No instance found", 0, res.allInstances().size());
        assertEquals("One change", 1, cnt);
    }

    static void assertBundles(String msg, int len, Bundle[] bundles) {
        if (bundles == null && len == 0) {
            return;
        }
        if (len == bundles.length) {
            return;
        }
        fail(msg + " expected: " + len + " was: " + bundles.length + "\n" + Arrays.toString(bundles));
    }


    public static Bundle findBundle(String bsn) throws Exception {
        Bundle[] arr = findFramework().getBundleContext().getBundles();
        Bundle candidate = null;
        for (Bundle b : arr) {
            if (bsn.equals(b.getSymbolicName())) {
                candidate = b;
                if ((b.getState() & Bundle.ACTIVE) != 0) {
                    return b;
                }
            }
        }
        return candidate;
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        cnt++;
    }

    public static Framework findFramework() {
        try {
            return NetigsoUtil.framework(Main.getModuleSystem().getManager());
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }
}
