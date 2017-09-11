/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.core.startup;

import org.netbeans.SetupHid;
import org.netbeans.ModuleManagerTest;
import org.netbeans.MockEvents;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.Events;
import org.netbeans.Module;
import org.netbeans.ModuleManager;

/**
 * Checks that OpenIDE-Module-Hide-Classpath-Packages works.
 */
public class NbInstallerHideClasspathPackagesTest extends SetupHid {

    public NbInstallerHideClasspathPackagesTest(String n) {
        super(n);
    }

    public void testHideClasspathPackages() throws Exception {
        File m1j = new File(getWorkDir(), "m1.jar");
        Map<String,String> contents = new  HashMap<String,String>();
        contents.put("javax/net/SocketFactory.class", "ignored");
        contents.put("javax/swing/JPanel.class", "overrides");
        contents.put("javax/swing/text/Document.class", "overrides");
        contents.put("javax/naming/Context.class", "overrides");
        contents.put("javax/naming/spi/Resolver.class", "ignored");
        Map<String,String> mani = new HashMap<String,String>();
        mani.put("OpenIDE-Module", "m1");
        mani.put("OpenIDE-Module-Hide-Classpath-Packages", "javax.swing.**, javax.naming.*");
        createJar(m1j, contents, mani);
        File m2j = new File(getWorkDir(), "m2.jar");
        mani = new HashMap<String,String>();
        mani.put("OpenIDE-Module", "m2");
        mani.put("OpenIDE-Module-Module-Dependencies", "m1");
        // Just to check early attempts to load packages:
        mani.put("OpenIDE-Module-Layer", "m2/layer.xml");
        mani.put("OpenIDE-Module-Package-Dependencies", "javax.management[Descriptor]");
        createJar(m2j, Collections.singletonMap("m2/layer.xml", "<filesystem/>"), mani);
        File m3j = new File(getWorkDir(), "m3.jar");
        createJar(m3j, Collections.<String,String>emptyMap(), Collections.singletonMap("OpenIDE-Module", "m3"));
        Events ev = new MockEvents();
        NbInstaller inst = new NbInstaller(ev);
        ModuleManager mgr = new ModuleManager(inst, ev);
        inst.registerManager(mgr);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(m1j, null, false, false, false);
            Module m2 = mgr.create(m2j, null, false, false, false);
            Module m3 = mgr.create(m3j, null, false, false, false);
            mgr.enable(new HashSet<Module>(Arrays.asList(m1, m2, m3)));
            ModuleManagerTest.assertDoesNotOverride(m1, "javax.net.SocketFactory");
            ModuleManagerTest.assertOverrides(m1, "javax.swing.JPanel");
            ModuleManagerTest.assertOverrides(m1, "javax.swing.text.Document");
            ModuleManagerTest.assertOverrides(m1, "javax.naming.Context");
            ModuleManagerTest.assertDoesNotOverride(m1, "javax.naming.spi.Resolver");
            ModuleManagerTest.assertDoesNotOverride(m2, "javax.net.SocketFactory");
            ModuleManagerTest.assertOverrides(m2, "javax.swing.JPanel");
            ModuleManagerTest.assertOverrides(m2, "javax.swing.text.Document");
            ModuleManagerTest.assertOverrides(m2, "javax.naming.Context");
            ModuleManagerTest.assertDoesNotOverride(m2, "javax.naming.spi.Resolver");
            ModuleManagerTest.assertDoesNotOverride(m3, "javax.net.SocketFactory");
            ModuleManagerTest.assertDoesNotOverride(m3, "javax.swing.JPanel");
            ModuleManagerTest.assertDoesNotOverride(m3, "javax.swing.text.Document");
            ModuleManagerTest.assertDoesNotOverride(m3, "javax.naming.Context");
            ModuleManagerTest.assertDoesNotOverride(m3, "javax.naming.spi.Resolver");
            // #159586: masked JRE classes should not be accessible from SCL either.
            ClassLoader scl = mgr.getClassLoader();
            ModuleManagerTest.assertDoesNotOverride(scl, "javax.net.SocketFactory");
            ModuleManagerTest.assertOverrides(scl, "system class loader", "javax.swing.JPanel");
            ModuleManagerTest.assertOverrides(scl, "system class loader", "javax.swing.text.Document");
            ModuleManagerTest.assertOverrides(scl, "system class loader", "javax.naming.Context");
            ModuleManagerTest.assertDoesNotOverride(scl, "javax.naming.spi.Resolver");
            mgr.disable(new HashSet<Module>(Arrays.asList(m1, m2, m3)));
            scl = mgr.getClassLoader();
            ModuleManagerTest.assertDoesNotOverride(scl, "javax.net.SocketFactory");
            ModuleManagerTest.assertDoesNotOverride(scl, "javax.swing.JPanel");
            ModuleManagerTest.assertDoesNotOverride(scl, "javax.swing.text.Document");
            ModuleManagerTest.assertDoesNotOverride(scl, "javax.naming.Context");
            ModuleManagerTest.assertDoesNotOverride(scl, "javax.naming.spi.Resolver");
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    @Override
    protected Level logLevel() {
        return Level.FINER;
    }

}
