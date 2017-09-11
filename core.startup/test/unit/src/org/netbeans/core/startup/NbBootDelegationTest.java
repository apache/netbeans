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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.core.startup;

import java.io.File;
import java.util.Collections;
import org.netbeans.Module;
import org.netbeans.ModuleManager;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class NbBootDelegationTest extends NbInstallerTestBase {

    public NbBootDelegationTest(String n) {
        super(n);
    }
    
    public void testNetBeansBootDelegation() throws Exception {
        ModuleManager mgr = Main.getModuleSystem().getManager();
        mgr.mutexPrivileged().enterWriteAccess();
        System.setProperty("netbeans.bootdelegation", "javax.swing, javax.naming.*");
        try {
            Module m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, false, false);
            mgr.enable(Collections.singleton(m1));
            final ClassLoader ldr = m1.getClassLoader();
            Class<?> jtree = Class.forName("javax.swing.JTree", true, ldr);
            assertNotNull("JTree found", jtree);
            String tableModel = "javax.swing.table.TableModel";
            try {
                Class<?> model = Class.forName(tableModel, true, ldr);
                fail("Model shall not be accessible: " + model);
            } catch (ClassNotFoundException ex) {
                // OK
                assertNotNull("The class exists on boot path", ClassLoader.getSystemClassLoader().loadClass(tableModel));
            }
            Class<?> list = Class.forName("java.util.ArrayList", true, ldr);
            assertNotNull("java packages are always accessible", list);
            Class<?> naming = Class.forName("javax.naming.event.EventContext", true, ldr);
            assertNotNull("naming is recursively visible", naming);
        } finally {
            System.getProperties().remove("netbeans.bootdelegation");
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }


}
