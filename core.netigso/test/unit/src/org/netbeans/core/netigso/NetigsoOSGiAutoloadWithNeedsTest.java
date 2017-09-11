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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.core.netigso;

import org.netbeans.core.startup.*;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import org.netbeans.InvalidException;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.SetupHid;

/**
 * Autoload with needs should be properly notified.
 *
 * @author Jaroslav Tulach
 */
public class NetigsoOSGiAutoloadWithNeedsTest extends SetupHid {
    private static Module m1;
    private static Module m2;
    private static ModuleManager mgr;
    private static File needsButDoesNotHave;
    private static File bundleRequires;

    public NetigsoOSGiAutoloadWithNeedsTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() {
    }
    
    public void testBundleRequiresAutoloadWithoutSatisfiedNeeds()
    throws Exception {
        Locale.setDefault(Locale.US);
        clearWorkDir();
        File ud = new File(getWorkDir(), "ud");
        ud.mkdirs();
        System.setProperty("netbeans.user", ud.getPath());
        
        data = new File(getDataDir(), "jars");
        jars = new File(getWorkDir(), "space in path");
        jars.mkdirs();
        File simpleModule = createTestJAR("simple-module", null);
        assertNotNull("simpleModule created", simpleModule);
        
        String bundle = "Bundle-SymbolicName: org.my.bundle\n"
                + "Bundle-ManifestVersion: 2\n"
                + "Require-Bundle: org.snd.module\n"
                + "\n"
                + "\n"
                + "";
        bundleRequires = NetigsoHid.changeManifest(
            getWorkDir(), simpleModule, bundle
        );
        
        String mf = "OpenIDE-Module: org.snd.module\n" +
            "OpenIDE-Module-Specification-Version: 33.0.3\n" +
            "OpenIDE-Module-Needs: non.existing.token\n"
                + "\n\n";
        needsButDoesNotHave = NetigsoHid.changeManifest(getWorkDir(), simpleModule, mf);
        
        ModuleSystem ms = Main.getModuleSystem();
        mgr = ms.getManager();
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            m1 = mgr.create(bundleRequires, null, false, false, false);
            m2 = mgr.create(needsButDoesNotHave, null, false, true, false);
            
            mgr.enable(m1);
            fail("m1 cannot be really enabled");
        } catch (InvalidException ex) {
            // OK
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        assertFalse("module m2 is disabled", m2.isEnabled());
    }
    
    private File createTestJAR(String name, String srcdir, File... classpath) throws IOException {
        return createTestJAR(data, jars, name, srcdir, classpath);
    }
}
