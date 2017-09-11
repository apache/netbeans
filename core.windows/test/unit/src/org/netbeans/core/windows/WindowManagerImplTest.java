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
 * Software is Sun Microsystems, Inc. Portions Copyright 2002 Sun
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

package org.netbeans.core.windows;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.*;

import org.openide.util.RequestProcessor;
import org.openide.util.Task;


/** 
 * 
 * @author Dafe Simonek
 */
public class WindowManagerImplTest extends NbTestCase {

    public WindowManagerImplTest (String name) {
        super (name);
    }

    public void testEDTAssert () {
        // run off EQ thread and check
        Task task = RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                // test both versions, assertions on and off
                checkEDTAssert(WindowManagerImpl.assertsEnabled);
                WindowManagerImpl.assertsEnabled = !WindowManagerImpl.assertsEnabled;
                checkEDTAssert(WindowManagerImpl.assertsEnabled);
            }
        });

        task.waitFinished();

        assertTrue(failMsg, checkOK);
    }

    private boolean checkOK = false;
    private String failMsg = null;

    private void checkEDTAssert (final boolean assertionsEnabled) {
        Logger logger = Logger.getLogger(WindowManagerImpl.class.getName());

        logger.setFilter(new java.util.logging.Filter() {
            public boolean isLoggable(LogRecord record) {
                Level level = record.getLevel();

                if (assertionsEnabled && !level.equals(Level.WARNING)) {
                    checkOK = false;
                    failMsg = "Logging on Level WARNING expected when assertions are enabled";
                    return true;
                }

                if (!assertionsEnabled && !level.equals(Level.FINE)) {
                    checkOK = false;
                    failMsg = "Logging on Level FINE expected when assertions are disabled";
                    return true;
                }

                checkOK = true;

                // don't log anything if test passes
                return false;
            }
        });

        WindowManagerImpl.warnIfNotInEDT();
    }


    
}
