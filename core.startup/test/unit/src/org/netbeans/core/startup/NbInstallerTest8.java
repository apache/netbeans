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
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestSuite;

/** Test the NetBeans module installer implementation.
 * Broken into pieces to ensure each runs in its own VM.
 * @author Jesse Glick
 */
public class NbInstallerTest8 extends SetupHid {
    
    public NbInstallerTest8(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        // Turn on verbose logging while developing tests:
        System.setProperty("org.netbeans.core.modules", "0");
        // In case run standalone, need a work dir.
        if (System.getProperty("nbjunit.workdir") == null) {
            // Hope java.io.tmpdir is set...
            System.setProperty("nbjunit.workdir", System.getProperty("java.io.tmpdir"));
        }
        TestRunner.run(new NbTestSuite(NbInstallerTest8.class));
    }
    
    private File moduleJar;
    protected @Override void setUp() throws Exception {
        super.setUp();
        System.setProperty("org.netbeans.core.modules.NbInstaller.noAutoDeps", "true");
        // leave NO_COMPAT_AUTO_TRANSITIVE_DEPS=false
        moduleJar = new File(jars, "look-for-myself.jar");
    }
    
    /** Test #28465: Lookup<ModuleInfo> should be ready soon, even while
     * modules are still loading. The ModuleInfo need not claim to be enabled
     * during this time, but it must exist.
     */
    public void testEarlyModuleInfoLookup() throws Exception {
        // Ought to load these modules:
        ModuleManager mgr = Main.getModuleSystem().getManager();
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m = mgr.get("lookformyself");
            assertNull(m);
            m = mgr.create(moduleJar, new ModuleHistory(moduleJar.getAbsolutePath()), false, false, false);
            assertEquals("look-for-myself.jar can be enabled", Collections.EMPTY_SET, m.getProblems());
            mgr.enable(m);
            Class<?> c = m.getClassLoader().loadClass("lookformyself.Loder");
            Method meth = c.getMethod("foundNow");
            assertTrue("ModuleInfo is found after startup", (Boolean) meth.invoke(null));
            Field f = c.getField("foundEarly");
            assertTrue("ModuleInfo is found during dataloader section initialization", (Boolean) f.get(null));
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
}
