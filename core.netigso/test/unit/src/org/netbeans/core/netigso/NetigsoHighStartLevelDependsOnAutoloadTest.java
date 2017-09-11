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
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.SetupHid;
import org.osgi.framework.Bundle;

/**
 * Can not started bundle depend on autoload?
 *
 * @author Jaroslav Tulach
 */
public class NetigsoHighStartLevelDependsOnAutoloadTest extends SetupHid {
    private static Module m1;
    private static ModuleManager mgr;
    private static File activate;
    private File simpleModule;

    public NetigsoHighStartLevelDependsOnAutoloadTest(String name) {
        super(name);
    }
    
    protected boolean autoload() {
        return false;
    }

    protected @Override void setUp() throws Exception {
        if (System.getProperty("netbeans.user") != null) {
            return;
        }
        Locale.setDefault(Locale.US);
        clearWorkDir();
        File ud = new File(getWorkDir(), "ud");
        ud.mkdirs();
        System.setProperty("netbeans.user", ud.getPath());
        
        data = new File(getDataDir(), "jars");
        jars = new File(getWorkDir(), "space in path");
        jars.mkdirs();
        simpleModule = createTestJAR("simple-module", null);
        activate = createTestJAR("depends-on-simple-module", null, simpleModule);
        String mf = "Bundle-SymbolicName: org.snd.module\n" +
            "Bundle-Version: 33.0.3\n" +
            "Bundle-ManifestVersion: 2\n" +
            "Import-Package: org.foo\n" +
            "Export-Package: org.bar\n" +
            "\n\n";
        activate = NetigsoHid.changeManifest(getWorkDir(), activate, mf);
        
    }
    public void testNotStartedBundleDependsOnAutoload() throws Exception {
        ModuleSystem ms = Main.getModuleSystem();
        mgr = ms.getManager();
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            m1 = mgr.createBundle(activate, null, false, false, false, 4);
            Module m2 = mgr.create(simpleModule, null, false, autoload(), false);
            
            mgr.enable(m1);
            Bundle bundle = NetigsoServicesTest.findBundle(m1.getCodeNameBase());

            Class<?> main = bundle.loadClass("org.bar.SomethingElse");
            assertNotNull("Class loaded", main);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    private File createTestJAR(String name, String srcdir, File... classpath) throws IOException {
        return createTestJAR(data, jars, name, srcdir, classpath);
    }
}
