/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.core.startup.preferences;

import java.util.prefs.Preferences;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;

/**
 *
 * @author martin
 */
public class TestNbPreferencesThreading extends NbTestCase {
    
    public TestNbPreferencesThreading(String name) {
        super(name);
    }
    
    public void testThreading() throws Exception {
        Preferences prefs = org.openide.util.NbPreferences.forModule(NbPreferences.class);
        final boolean [] fileEventReceived = new boolean[] { false };
        final boolean [] fileEventBlock1 = new boolean[] { false };
        final boolean [] fileEventBlock2 = new boolean[] { true };
        
        PropertiesStorage.TEST_FILE_EVENT = new Runnable() {
            @Override
            public void run() {
                synchronized (fileEventReceived) {
                    fileEventReceived[0] = true;
                    fileEventReceived.notifyAll();
                }
                try {
                    synchronized (fileEventBlock1) {
                        if (!fileEventBlock1[0]) {
                            fileEventBlock1.wait();
                        }
                    }
                    synchronized (fileEventBlock2) {
                        if (fileEventBlock2[0]) {
                            fileEventBlock2.wait();
                        }
                    }
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        prefs.putBoolean("Guest", false);
        assertFalse(prefs.getBoolean("Guest", true));
        synchronized (fileEventReceived) {
            if (!fileEventReceived[0]) {
                fileEventReceived.wait();
            }
            fileEventReceived[0] = false;
        }
        prefs.putBoolean("Guest", true);
        
        assertTrue(prefs.getBoolean("Guest", false));
        
        { // Let process the file event
            synchronized (fileEventBlock1) {
                fileEventBlock1[0] = true;
                fileEventBlock1.notifyAll();
            }
            synchronized (fileEventBlock2) {
                fileEventBlock2[0] = false;
                fileEventBlock2.notifyAll();
            }
            synchronized (fileEventReceived) {
                if (!fileEventReceived[0]) {
                    fileEventReceived.wait();
                }
            }
        } // when done, do the same test again
        
        assertTrue(prefs.getBoolean("Guest", false));
    }
}
