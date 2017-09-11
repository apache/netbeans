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

package org.netbeans.modules.netbinox;

import java.net.URL;
import org.netbeans.core.startup.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.SetupHid;
import org.netbeans.core.netigso.NetigsoServicesTest;
import org.openide.util.Lookup;
import org.openide.util.Mutex.ExceptionAction;
import org.osgi.framework.Bundle;

/**
 * Can we read resources from NetBeans modules?
 * This time from public packages...
 *
 * @author Jaroslav Tulach
 */
public class BundleGetEntryTest extends SetupHid {
    private static Module m1;
    private static ModuleManager mgr;
    private File jar;
    private File simpleModule;

    public BundleGetEntryTest(String name) {
        super(name);
    }

    protected String moduleManifest() {
        return "OpenIDE-Module: can.read.metainf\n"
           + "OpenIDE-Public-Packages: org.activate\n"
           + "\n"
           + "\n";
    }

    protected final @Override void setUp() throws Exception {
        clearWorkDir();
        File ud = new File(getWorkDir(), "ud");
        ud.mkdirs();
        System.setProperty("netbeans.user", ud.getPath());
        
        data = new File(getDataDir(), "jars");
        jars = new File(getWorkDir(), "space in path");
        jars.mkdirs();
        simpleModule = createTestJAR("activate", null);
        String mf = moduleManifest();
       jar = NetigsoHid.changeManifest(getWorkDir(), simpleModule, mf);
    }


    public void testActivation() throws Exception {
        ModuleSystem ms = Main.getModuleSystem();
        mgr = ms.getManager();
        mgr.mutex().writeAccess(new ExceptionAction<Void>() {
            @Override
            public Void run() throws Exception {
                Callable<?> fr = Lookup.getDefault().lookup(Callable.class);
                assertNull("No registration found yet", fr);


                m1 = mgr.create(jar, null, false, false, false);
                mgr.enable(m1);

                Module m2 = mgr.create(simpleModule, null, false, false, false);
                mgr.enable(m2);

                
                Bundle bundle = NetigsoServicesTest.findBundle("can.read.metainf");
                assertNotNull("Bundle found", bundle);
                URL res = bundle.getEntry("org/activate/entry.txt");
                assertNotNull("Entry found", res);
                byte[] arr = new byte[1000];
                int len = res.openStream().read(arr);
                String s = new String(arr, 0, len);
                assertEquals("Ahoj", s.substring(0,4));

                mgr.disable(m1);

                return null;
            }
        });
    }
    private File createTestJAR(String name, String srcdir, File... classpath) throws IOException {
        return createTestJAR(data, jars, name, srcdir, classpath);
    }
}
