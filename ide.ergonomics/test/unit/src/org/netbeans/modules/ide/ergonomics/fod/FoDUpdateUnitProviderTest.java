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

package org.netbeans.modules.ide.ergonomics.fod;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import junit.framework.Test;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.autoupdate.UpdateItem;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class FoDUpdateUnitProviderTest extends NbTestCase {

    public FoDUpdateUnitProviderTest(String n) {
        super(n);
    }

    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().
            addTest(FoDUpdateUnitProviderTest.class).
            gui(false).
            clusters(".*")
        );
    }

    public void testGetDisplayName() {
        FoDUpdateUnitProvider instance = new FoDUpdateUnitProvider();
        String result = instance.getDisplayName();
        assertEquals("No name as this provider is hidden", null, result);
    }

    public void testManuallyInstalledModules() throws Exception {
        FoDUpdateUnitProvider instance = new FoDUpdateUnitProvider();
        Map<String, UpdateItem> items = instance.getUpdateItems();

        assertNull("No user installed modules yet", items.get("fod.user.installed"));
        UpdateItem ideKit = items.get("fod.base.ide");
        assertNotNull("Item for FoD found: " + items, ideKit);

        File module = createModule("empty-user-install.jar",
"Manifest-Version", "1.0",
"OpenIDE-Module", "org.netbeans.empty.user.install",
"OpenIDE-Module-Specification-Version", "1.0",
"AutoUpdate-Show-In-Client", "true"
        );
        ModuleManager man = org.netbeans.core.startup.Main.getModuleSystem().getManager();
        try {
            man.mutexPrivileged().enterWriteAccess();
            Module m = man.create(module, this, false, false, false);
            man.enable(m);
            assertTrue("Module is active", m.isEnabled());
        } finally {
            man.mutexPrivileged().exitWriteAccess();
        }

        items = instance.getUpdateItems();
        final UpdateItem userInstalled = items.get("fod.user.installed");
        assertNotNull("No user installed modules yet", userInstalled);
    }

    private File createModule(String fileName, String... attribs) throws IOException {
        File d = new File(getWorkDir(), "modules");
        d.mkdirs();
        File m = new File(d, fileName);
        FileOutputStream out = new FileOutputStream(m);
        Manifest man = new Manifest();
        for (int i = 0; i < attribs.length; i += 2) {
            man.getMainAttributes().putValue(attribs[i], attribs[i + 1]);
        }
        JarOutputStream os = new JarOutputStream(out, man);
        os.close();
        return m;
    }

}
