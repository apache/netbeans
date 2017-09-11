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

import org.netbeans.MockEvents;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.Stamps;
import org.openide.filesystems.FileUtil;

/** Test the NetBeans module installer implementation.
 * Broken into pieces to ensure each runs in its own VM.
 * @author Jesse Glick
 */
public class NbInstallerTest4 extends NbInstallerTestBase {

    public NbInstallerTest4(String name) {
        super(name);
    }

    /** Test #21173/#23609: overriding layers by module dependencies.
     * Version 1: all modules loaded together.
     */
    public void testDependencyLayerOverrides1() throws Exception {
        Main.getModuleSystem (); // init module system
        final MockEvents ev = new MockEvents();
        NbInstaller installer = new NbInstaller(ev);
        ModuleManager mgr = new ModuleManager(installer, ev);
        installer.registerManager(mgr);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(new File(jars, "base-layer-mod.jar"), null, false, false, false);
            Module m2 = mgr.create(new File(jars, "override-layer-mod.jar"), null, false, false, false);
            assertEquals(Collections.EMPTY_SET, m2.getProblems());
            assertEquals(null, slurp("foo/file1.txt"));
            assertEquals(null, slurp("foo/file3.txt"));
            assertEquals(null, slurp("foo/file4.txt"));
            Set<Module> m1m2 = new HashSet<Module>(Arrays.asList(m1, m2));
            mgr.enable(m1m2);
            
            Stamps.getModulesJARs().flush(0);
            Stamps.getModulesJARs().shutdown();
            
            assertEquals("base contents", slurp("foo/file1.txt"));
            assertEquals("customized contents", slurp("foo/file3.txt"));
            assertEquals(null, slurp("foo/file4.txt"));
            assertEquals("someotherval", FileUtil.getConfigFile("foo/file5.txt").getAttribute("myattr"));
            mgr.disable(m1m2);
            assertEquals(null, slurp("foo/file1.txt"));
            mgr.delete(m2);
            mgr.delete(m1);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

}
