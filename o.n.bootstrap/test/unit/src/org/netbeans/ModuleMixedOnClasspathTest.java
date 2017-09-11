/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Nokia. Portions Copyright 2005 Nokia. All Rights Reserved.
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
 */
package org.netbeans;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.netbeans.modules.openide.util.NbMutexEventProvider;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * These tests verify that the module manager behaves basically the
 * same way with ModuleFactory as without it.
 * @author David Strupl
 */
public class ModuleMixedOnClasspathTest extends SetupHid {

    // #88772: MockServices does not work here, probably because MainLookup ignores CCL.
    static {
        System.setProperty("org.openide.util.Lookup", L.class.getName());
        assertTrue(Lookup.getDefault() instanceof L);
    }
    public static final class L extends ProxyLookup {
        public L() {
            super(new Lookup[] {
                Lookups.fixed(new Object[] {
                    new NbMutexEventProvider()
//                    new MyModuleFactory()
                }),
            });
        }
    }

    public ModuleMixedOnClasspathTest(String name) {
        super(name);
    }

    public static int numberOfStandard = 0;
    public static int numberOfFixed = 0;
    public static boolean testingParentClassloaders = false;
    public static boolean testingDummyModule = false;
    
    public void testFactoryCreatesOurModules() throws Exception {
        // clear the counters before the test!
        numberOfStandard = 0;
        numberOfFixed = 0;
        
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            File j1 = new File(jars, "simple-module.jar");
            File j2 = new File(jars, "depends-on-simple-module.jar");
            File j3 = new File(jars, "dep-on-two-modules.jar");
            URLClassLoader l = new URLClassLoader(new URL[] {Utilities.toURI(j1).toURL()});
            Manifest mani1, mani2;
            JarFile j = new JarFile(j1);
            try {
                mani1 = j.getManifest();
            } finally {
                j.close();
            }
            j = new JarFile(j2);
            try {
                mani2 = j.getManifest();
            } finally {
                j.close();
            }
            Module m1 = mgr.createFixed(mani1, null, l);
            Module m2 = mgr.createFixed(mani2, null, ClassLoader.getSystemClassLoader());
            Module m3 = mgr.create(j3, null, false, false, false);
            mgr.enable(new HashSet<Module>(Arrays.asList(m1, m2, m3)));
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
}
