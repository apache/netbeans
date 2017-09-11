/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.junit.internal;

import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.Exceptions;

@RandomlyFails // NB-Core-Build #3539
public class NbModuleLogHandlerTest extends NbTestCase {

    public NbModuleLogHandlerTest(String n) {
        super(n);
    }

    public static Test suite() {
        // XXX maybe simpler to disable org.netbeans.core hence NotifyExcPanel, but enableClasspathModules(false) does nothing
        // (perhaps these properties should be set automatically by NbModuleSuite and/or NbTestCase?)
        System.setProperty("netbeans.exception.alert.min.level", "999999");
        System.setProperty("netbeans.exception.report.min.level", "999999");
        return NbModuleSuite.createConfiguration(NbModuleLogHandlerTest.class).gui(false).failOnException(Level.WARNING).suite();
    }

    public void testIgnoreOutOfMemoryErrorFromAssertGC() throws Exception {
        new Thread("background") {
            public @Override void run() {
                // try to get OOME reported
                while (true) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    try {
                        Object _ = new Object[999999];
                    } catch (Throwable t) {
                        Logger.getLogger(NbModuleLogHandlerTest.class.getName()).log(Level.WARNING, null, t);
                    }
                }
            }
        }.start();
        try {
            assertGC("this will fail", new WeakReference<Object>("interned"));
            throw new IllegalStateException("should not succeed");
        } catch (AssertionFailedError x) {
            // expected
        }
    }

}
