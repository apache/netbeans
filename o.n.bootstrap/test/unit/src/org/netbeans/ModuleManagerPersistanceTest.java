/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ModuleManagerPersistanceTest extends NbTestCase {
    private ModuleManager mgr;
    private File sampleModule;
    
    public ModuleManagerPersistanceTest(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        File home = new File(getWorkDir(), "home");
        final File configModules = new File(new File(home, "config"), "Modules");
        configModules.mkdirs();
        new File(configModules, "a-b-c.xml").createNewFile();
        File moduleDir = new File(home, "modules");
        moduleDir.mkdirs();
        System.setProperty("netbeans.home", home.getPath());
        
        File ud = new File(getWorkDir(), "ud");
        ud.mkdirs();
        System.setProperty("netbeans.user", ud.getPath());
        
        Locale.setDefault(Locale.ENGLISH);
        NbBundle.setBranding("nb");
        
        Thread.sleep(100);
        
        
        Stamps.main("clear");
        sampleModule = new File(moduleDir, "m1.jar");
        mgr = createModuleManager();
        mgr.shutDown();
        Stamps.getModulesJARs().shutdown();
        assertTrue("Cache has been created", Stamps.getModulesJARs().exists("all-manifests.dat"));
        Stamps.main("init");
    }
    
    @RandomlyFails // NB-Core-Build #9913, 9915: Unstable
    public void testModuleManagerStoresIsOSGiInfo() throws Exception {
        ModuleManager snd = createModuleManager();
        assertSame("Is not OSGi, but is computed", Boolean.FALSE, snd.isOSGi(sampleModule));
    }

    private ModuleManager createModuleManager() throws Exception {
        MockModuleInstaller mi = new MockModuleInstaller();
        MockEvents me = new MockEvents();
        ModuleManager mm = new ModuleManager(mi, me);
        SetupHid.createJar(sampleModule, Collections.<String, String>emptyMap(), Collections.singletonMap("OpenIDE-Module", "m1/0"));
        Module m = mm.create(sampleModule, this, false, false, false);
        mm.enable(m);
        assertTrue("Successfully enabled", m.isEnabled());
        return mm;
    }
}
