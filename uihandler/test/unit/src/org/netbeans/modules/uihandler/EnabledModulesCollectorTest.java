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

package org.netbeans.modules.uihandler;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;

/**
 */
public class EnabledModulesCollectorTest extends NbTestCase {
    private Installer installer;
    
    public EnabledModulesCollectorTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDirPath());
        clearWorkDir();
        
        Locale.setDefault(new Locale("te", "ST"));
        MockServices.setServices(EnabledModulesCollector.class, MyModule.class, MyModule2.class, ActivatedDeativatedTest.DD.class);
        installer = Installer.findObject(Installer.class, true);
        installer.restored();
    }

    @Override
    protected void tearDown() throws Exception {
        installer.uninstalled();
    }
    
    public void testSetOfEnabledModulesIsListed() {
        // just log something
        Logger.getLogger(Installer.UI_LOGGER_NAME + ".empty").warning("say anything");

        Installer.logDeactivated();
        assertTrue("ok", installer.closing());
        
        List<LogRecord> rec = ScreenSizeTest.removeExtraLogs(Installer.getLogs());
        if (rec.get(0).getMessage().equals("say anything")) {
            rec.remove(0);
        }
        
        assertEquals("One record for disabled and one for enabled: " + rec, 2, rec.size());
        
        assertEquals("UI_ENABLED_MODULES", rec.get(0).getMessage());
        assertEquals("UI_DISABLED_MODULES", rec.get(1).getMessage());
        assertEquals("one enabled", 1, rec.get(0).getParameters().length);
        assertEquals("one disabled", 1, rec.get(1).getParameters().length);
        assertEquals("the one enabled", MyModule.INSTANCE.getCodeNameBase()+" ["+MyModule.INSTANCE.getSpecificationVersion().toString()+"]", rec.get(0).getParameters()[0]);
        assertEquals("the one disabled", MyModule2.INSTANCE2.getCodeNameBase()+" ["+MyModule2.INSTANCE2.getSpecificationVersion().toString()+"]", rec.get(1).getParameters()[0]);

        assertNotNull("Localized msg0", rec.get(0).getResourceBundle().getString(rec.get(0).getMessage()));
        assertNotNull("Localized msg1", rec.get(1).getResourceBundle().getString(rec.get(1).getMessage()));
    }
    
    public static class MyModule extends ModuleInfo {
        static MyModule INSTANCE;
        
        public MyModule() {
            if (MyModule.class == getClass()) {
                INSTANCE = this;
            }
        }
        
        public String getCodeNameBase() {
            return "my.module";
        }

        public int getCodeNameRelease() {
            return -1;
        }

        public String getCodeName() {
            return getCodeNameBase();
        }

        public SpecificationVersion getSpecificationVersion() {
            return new SpecificationVersion("1.2");
        }

        public boolean isEnabled() {
            return true;
        }

        public Object getAttribute(String attr) {
            return null;
        }

        public Object getLocalizedAttribute(String attr) {
            return null;
        }

        public Set<org.openide.modules.Dependency> getDependencies() {
            return Collections.emptySet();
        }

        public boolean owns(Class clazz) {
            return false;
        }
    } // end of MyModule
    
    public static final class MyModule2 extends MyModule {
        static MyModule2 INSTANCE2;
        
        public MyModule2() {
            INSTANCE2 = this;
        }
        @Override
        public String getCodeNameBase() {
            return "my.module2";
        }
        
        @Override
        public boolean isEnabled() {
            return false;
        }
    }
}
