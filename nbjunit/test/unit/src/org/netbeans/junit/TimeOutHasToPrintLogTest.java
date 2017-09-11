/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2011 Sun
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

package org.netbeans.junit;

import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestFailure;
import junit.framework.TestResult;

/** Provides an example of randomized test.
 */
public class TimeOutHasToPrintLogTest extends NbTestCase {
    private Logger LOG = Logger.getLogger("my.log.for.test");
    
    public TimeOutHasToPrintLogTest(String testName) {
        super(testName);
    }
    
    private boolean isSpecial() {
        return getName().equals("printAhojAndTimeOut") || getName().equals("justTimeOutInOneOfMyMethods");
    }
    
    @Override
    protected Level logLevel() {
        if (isSpecial()) {
            return null;
        } else {
            return Level.FINE;
        }
    }
    
    @Override
    protected int timeOut() {
        if (isSpecial()) {
            return 700;
        } else {
            return 0;
        }
    }
    
    public void printAhojAndTimeOut() throws Exception {
        LOG.fine("Ahoj");
        Thread.sleep(10000);
    }
    
    public void justTimeOutInOneOfMyMethods() throws Exception {
        Thread.sleep(10000);
    }

    public void testThatTheTimeOutStillPrintsTheWarning() throws Exception {
        TimeOutHasToPrintLogTest t = new TimeOutHasToPrintLogTest("printAhojAndTimeOut");
        
        CharSequence seq = Log.enable(LOG.getName(), Level.FINE);
        
        TestResult res = t.run();
        
        assertEquals("One test has been run", 1, res.runCount());
        
        String s = seq.toString();
        
        if (s.indexOf("Ahoj") == -1) {
            fail("Ahoj has to be logged:\n" + s);
        }
        
        assertEquals("No error", 0, res.errorCount());
        assertEquals("One failure", 1, res.failureCount());
        
        TestFailure f = (TestFailure)res.failures().nextElement();
        s = f.exceptionMessage();
        if (s.indexOf("Ahoj") == -1) {
            fail("Ahoj has to be part of the message:\n" + s);
        }
    }

    public void testThreadDumpPrinted() throws Exception {
        TimeOutHasToPrintLogTest t = new TimeOutHasToPrintLogTest("justTimeOutInOneOfMyMethods");
        
        TestResult res = t.run();
        
        assertEquals("One test has been run", 1, res.runCount());
        TestFailure failure = (TestFailure)res.failures().nextElement();
        String s = failure.exceptionMessage();

        if (s.indexOf("justTimeOutInOneOfMyMethods") == -1) {
            fail("There should be thread dump reported in case of timeout:\n" + s);
        }
        
        assertEquals("No error", 0, res.errorCount());
        assertEquals("One failure", 1, res.failureCount());
    }
}
