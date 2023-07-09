/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.uihandler;

import java.awt.Dialog;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import org.netbeans.junit.Log;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.uihandler.api.Activated;
import org.netbeans.modules.uihandler.api.Deactivated;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author Jaroslav Tulach
 */
public class ActivatedDeativatedTest extends NbTestCase {
    private Installer o;
    
    public ActivatedDeativatedTest(String testName) {
        super(testName);
    }
    
    protected boolean runInEQ() {
        return true;
    }

    protected void setUp() throws Exception {
        Locale.setDefault(new Locale("te", "ST"));
        o = Installer.findObject(Installer.class, true);
        assertNotNull("Installer created", o);
        MockServices.setServices(A.class, D.class/*); //*/, DD.class);
    }

    protected void tearDown() throws Exception {
        o = Installer.findObject(Installer.class, true);
        o.uninstalled();
    }
    
    public void testActivatedAndDeativated() {
        CharSequence log = Log.enable(Installer.UI_LOGGER_NAME, Level.ALL);
        
        o.restored();
        if (log.toString().indexOf("A start") == -1) {
            fail("A shall start: " + log);
        }
        Installer.logDeactivated();
        
        assertTrue("Allowed to close", o.closing());
        if (log.toString().indexOf("D stop") == -1) {
            fail("D shall stop: " + log);
        }
    }
    
    
    public static final class A implements Activated {
        public void activated(Logger uiLogger) {
            uiLogger.config("A started");
        }
    }
    public static final class D implements Deactivated {
        public void deactivated(Logger uiLogger) {
            uiLogger.config("D stopped");
        }
    }
    public static final class DD extends DialogDisplayer {
        public Object notify(NotifyDescriptor descriptor) {
            // last options allows to close usually
            return descriptor.getOptions()[descriptor.getOptions().length - 1];
        }

        public Dialog createDialog(DialogDescriptor descriptor) {
            return new JDialog() {
                @Override
                public void setVisible(boolean v) {
                }
            };
        }
        
    }
}
