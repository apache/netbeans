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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.junit;


import test.pkg.not.in.junit.NbModuleSuiteT;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import org.netbeans.junit.NbModuleSuite.Configuration;
import test.pkg.not.in.junit.NbModuleSuiteMeta;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class NbModuleSuite2Test extends TestCase {
    
    public NbModuleSuite2Test(String testName) {
        super(testName);
    }            
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    
    public void testServices() throws Exception{
        Configuration conf = NbModuleSuite.createConfiguration(NbModuleSuiteMeta.class).gui(false);
        Test test = conf.suite();
        test.run(new TestResult());
        assertNotNull("The test was running", System.getProperty("meta"));
        assertEquals("result" + System.getProperty("meta"), "ok", System.getProperty("meta"));
    }

    public void testRun() {
        System.setProperty("t.one", "no");
        Test instance = NbModuleSuite.createConfiguration(NbModuleSuiteT.class).gui(false).suite();
        junit.textui.TestRunner.run(instance);
        
        assertEquals("OK", System.getProperty("t.one"));
        NbModuleSuiteTest.assertProperty("netbeans.full.hack", "true");
    }

    public void testRunEmptyConfig() {
        System.setProperty("t.one", "no");
        
        Test instance = NbModuleSuite.emptyConfiguration().gui(false).suite();
        junit.textui.TestRunner.run(instance);
        
        assertEquals("nothing has been executed", "no", System.getProperty("t.one"));
        NbModuleSuiteTest.assertProperty("netbeans.full.hack", "true");
    }

    public void testRunEmptyConfigWithOneAdd() {
        System.setProperty("t.one", "no");
        
        Test instance = NbModuleSuite.emptyConfiguration().addTest(NbModuleSuiteT.class).gui(false).suite();
        junit.textui.TestRunner.run(instance);
        
        assertEquals("OK", System.getProperty("t.one"));
        NbModuleSuiteTest.assertProperty("netbeans.full.hack", "true");
    }

    public void testRunEmptyConfigFails() {
        try {
            NbModuleSuite.emptyConfiguration().addTest("ahoj");
            fail("Shall fail as there is no class registered yet");
        } catch (IllegalStateException ex) {
            // ok
        }
    }

    public void testTestCount() throws Exception{
        Test test  = NbModuleSuite.createConfiguration(NbModuleSuiteT.class).gui(false).suite();
        assertEquals(0, test.countTestCases());
        test.run(new TestResult());
        assertEquals("one+fullhack+startuparg", 3, test.countTestCases());
    }
}
