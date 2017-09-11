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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.netbinox;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import org.netbeans.*;

public class NetigsoOSGiIsNotFriendTest extends NetigsoHid {
    public NetigsoOSGiIsNotFriendTest(String name) {
        super(name);
    }

    public void testOSGiBundleAreNotImplicitFriendsOfNetBeansModules() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            String mfBar = "Bundle-SymbolicName: org.bar\n" +
                "Bundle-Version: 1.1.0\n" +
                "Bundle-ManifestVersion: 2\n" +
                "Require-Bundle: org.foo\n" +
                "\n\n";

            String mfSimple = "OpenIDE-Module: org.foo/1\n"
                    + "OpenIDE-Module-Specification-Version: 1.2\n"
                    + "OpenIDE-Module-Public-Packages: org.foo.*\n"
                    + "OpenIDE-Module-Friends: few.unknown\n"
                    + "\n\n";

            
            File j1 = changeManifest(new File(jars, "simple-module.jar"), mfSimple);
            File j2 = changeManifest(new File(jars, "depends-on-simple-module.jar"), mfBar);
            Module m1 = mgr.create(j1, null, false, false, false);
            Module m2 = mgr.create(j2, null, false, false, false);
            HashSet<Module> b = new HashSet<Module>(Arrays.asList(m1, m2));
            try {
                mgr.enable(m1);
                mgr.enable(m2);
                fail("InvalidException should be raised!");
            } catch (InvalidException ex) {
                // OK
            }
            assertFalse("We should not be able to enable org.bar bundle!", m2.isEnabled());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

}
