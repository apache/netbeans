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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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

package org.netbeans.core.startup;

import org.netbeans.SetupHid;
import org.netbeans.MockEvents;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.netbeans.Module;
import org.netbeans.ModuleManager;

/** Checks whether a modules are provided with ModuleFormat1 token.
 *
 * @author Jaroslav Tulach
 */
public class ModuleFormatSatisfiedTest extends SetupHid {
    private File moduleJarFile;

    public ModuleFormatSatisfiedTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("org.netbeans.core.modules.NbInstaller.noAutoDeps", "true");
        
        Manifest man = new Manifest ();
        man.getMainAttributes ().putValue ("Manifest-Version", "1.0");
        man.getMainAttributes ().putValue ("OpenIDE-Module", "org.test.FormatDependency/1");
        String req = "org.openide.modules.ModuleFormat1, org.openide.modules.ModuleFormat2";
        man.getMainAttributes ().putValue ("OpenIDE-Module-Requires", req);
        
        clearWorkDir();
        moduleJarFile = new File(getWorkDir(), "ModuleFormatTest.jar");
        JarOutputStream os = new JarOutputStream(new FileOutputStream(moduleJarFile), man);
        os.putNextEntry (new JarEntry ("empty/test.txt"));
        os.close ();
    }
    
    public void testTryToInstallTheModule () throws Exception {
        final MockEvents ev = new MockEvents();
        NbInstaller installer = new NbInstaller(ev);
        ModuleManager mgr = new ModuleManager(installer, ev);
        installer.registerManager(mgr);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            addOpenideModules(mgr);
            Module m1 = mgr.create(moduleJarFile, null, false, false, false);
            assertEquals(Collections.EMPTY_SET, m1.getProblems());
            mgr.enable(m1);
            mgr.disable(m1);
            mgr.delete(m1);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    static void addOpenideModules (ModuleManager mgr) throws Exception {
        ClassLoader l = SetupHid.class.getClassLoader();
        String openide =
"Manifest-Version: 1.0\n" +
"OpenIDE-Module: org.openide.modules\n" +
"OpenIDE-Module-Localizing-Bundle: org/openide/modules/Bundle.properties\n" +
"Specification-Title: NetBeans\n" +
"OpenIDE-Module-Specification-Version: 6.2\n" +
"\n" +
"Name: /org/openide/modules/\n" +
"Package-Title: org.openide.modules\n";
               
        Manifest mani = new Manifest (new java.io.ByteArrayInputStream (openide.getBytes ()));
        mgr.enable(mgr.createFixed(mani, null, l));
     }
    
}
