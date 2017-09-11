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
package org.netbeans.core.startup;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.jar.Manifest;
import org.netbeans.Events;
import org.netbeans.MockEvents;
import org.netbeans.MockModuleInstaller;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.Stamps;
import org.netbeans.StampsTest;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class NbInstallerCacheTest extends NbTestCase {

    public NbInstallerCacheTest(String name) {
        super(name);
    }
    
    public void testValuesCachedAndEmpty() throws Exception {
        clearWorkDir();
        System.setProperty("netbeans.user", getWorkDirPath());
        File conf = new File(new File(new File(getWorkDir(), "config"), "Modules"), "some-mock.xml");
        conf.getParentFile().mkdirs();
        assertTrue("File OK", conf.createNewFile());
        
        MockModuleInstaller mmi = new MockModuleInstaller();
        MockEvents me = new MockEvents();
        ModuleManager mm = new ModuleManager(mmi, me);
        MockModule m = new MockModule(mm, me);
    
        NbInstaller.Cache c = new NbInstaller.Cache();
        assertEquals("1", c.findProperty(m, "one", true));
        assertEquals("One call to module", 1, m.cnt);
        assertNull(c.findProperty(m, "null", true));
        assertEquals("Another call to module", 2, m.cnt);
        
        Stamps.getModulesJARs().flush(0);
        Stamps.getModulesJARs().shutdown();
        m.cnt = 0;
        StampsTest.reset();
        
        NbInstaller.Cache loaded = new NbInstaller.Cache();
        assertEquals("1", loaded.findProperty(m, "one", true));
        assertEquals("No call to module", 0, m.cnt);
        assertNull(loaded.findProperty(m, "null", true));
        assertEquals("Even null is cached", 0, m.cnt);
     
        assertEquals("2", loaded.findProperty(m, "two", true));
        assertEquals("Has to call to module", 1, m.cnt);
    }
    
    private static class MockModule extends Module {

        public MockModule(ModuleManager mgr, Events ev) throws IOException {
            super(mgr, ev, null, false, false, false);
        }
        @Override
        public List<File> getAllJars() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setReloadable(boolean r) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void reload() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void classLoaderUp(Set<Module> parents) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void classLoaderDown() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void cleanup() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void destroy() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isFixed() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        private Manifest mf = new Manifest();
        {
            mf.getMainAttributes().putValue("OpenIDE-Module", "test.mock");
        }
        @Override
        public Manifest getManifest() {
            return mf;
        }

        @Override
        public Object getLocalizedAttribute(String attr) {
            cnt++;
            if ("one".equals(attr)) {
                return "1";
            }
            if ("two".equals(attr)) {
                return "2";
            }
            return null;
        }
        int cnt;
        
    }
}
