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
                public void setVisible(boolean v) {
                }
            };
        }
        
    }
}
