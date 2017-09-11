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

import java.awt.Component;
import org.netbeans.core.startup.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.SetupHid;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 * Do we correctly call the BundleActivators?
 *
 * @author Jaroslav Tulach
 */
public class NetigsoActivationWithLookupTest extends SetupHid {
    public NetigsoActivationWithLookupTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        clearWorkDir();
        File ud = new File(getWorkDir(), "ud");
        ud.mkdirs();
        System.setProperty("netbeans.user", ud.getPath());
        
        data = new File(getDataDir(), "jars");
        jars = new File(getWorkDir(), "jars");
        jars.mkdirs();
        File activate = createTestJAR("activate", null);

        File lkp = Utilities.toFile(Lookup.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        File register = createTestJAR("register", null, lkp);

        registerModule(ud, activate, "org.activate");
        registerModule(ud, register, "org.register");
    }

    public void testThatTriesToLookupDuringActivation() throws Exception {
        System.setProperty("start.class", Impl.class.getName());
        ModuleSystem ms = Main.getModuleSystem();
        assertEquals("OK", System.getProperty("lookup.status"));
    }
    private File createTestJAR(String name, String srcdir, File... classpath) throws IOException {
        return createTestJAR(data, jars, name, srcdir, classpath);
    }

    private static void registerModule(File ud, File jar, String cnb) throws Exception {
        File md = new File(new File(ud, "config"), "Modules");
        md.mkdirs();
        FileOutputStream os = new FileOutputStream(new File(md, cnb.replace('.', '-') + ".xml"));
        os.write((
"<?xml version='1.0' encoding='UTF-8'?>\n" +
"<!DOCTYPE module PUBLIC '-//NetBeans//DTD Module Status 1.0//EN' 'http://www.netbeans.org/dtds/module-status-1_0.dtd'>\n" +
"<module name='" + cnb + "'>\n" +
"    <param name='autoload'>false</param>\n" +
"    <param name='eager'>false</param>\n" +
"    <param name='enabled'>true</param>\n" +
"    <param name='reloadable'>false</param>\n" +
"    <param name='jar'>" + jar + "</param>\n" +
"</module>\n"
        ).getBytes());
        os.close();
    }

    public static final class Impl extends Object {
        public Impl() throws Exception {
            Component res = Lookup.getDefault().lookup(java.awt.Component.class);
            assertNotNull("result found", res);
            assertEquals("Class name is Main", "Main", res.getClass().getSimpleName());
            // OK
            System.setProperty("lookup.status", "OK");
        }
    }
}
