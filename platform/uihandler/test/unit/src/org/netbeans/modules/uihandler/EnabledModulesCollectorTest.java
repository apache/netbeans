/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
