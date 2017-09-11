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

package org.netbeans.modules.db.dataview.output;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author navaneeth
 */
public class DataViewActionHandlerTest extends NbTestCase {
    
    public DataViewActionHandlerTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(DataViewActionHandlerTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testToDo() {
        assertTrue("To Do", true);
    }
    /**
     * Test of cancelEditPerformed method, of class DataViewActionHandler.
     */
//    public void testCancelEditPerformed() {
//        System.out.println("cancelEditPerformed");
//        DataViewActionHandler instance = null;
//        instance.cancelEditPerformed();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setMaxActionPerformed method, of class DataViewActionHandler.
//     */
//    public void testSetMaxActionPerformed() {
//        System.out.println("setMaxActionPerformed");
//        DataViewActionHandler instance = null;
//        instance.setMaxActionPerformed();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of firstActionPerformed method, of class DataViewActionHandler.
//     */
//    public void testFirstActionPerformed() {
//        System.out.println("firstActionPerformed");
//        DataViewActionHandler instance = null;
//        instance.firstActionPerformed();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of previousActionPerformed method, of class DataViewActionHandler.
//     */
//    public void testPreviousActionPerformed() {
//        System.out.println("previousActionPerformed");
//        DataViewActionHandler instance = null;
//        instance.previousActionPerformed();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of nextActionPerformed method, of class DataViewActionHandler.
//     */
//    public void testNextActionPerformed() {
//        System.out.println("nextActionPerformed");
//        DataViewActionHandler instance = null;
//        instance.nextActionPerformed();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of lastActionPerformed method, of class DataViewActionHandler.
//     */
//    public void testLastActionPerformed() {
//        System.out.println("lastActionPerformed");
//        DataViewActionHandler instance = null;
//        instance.lastActionPerformed();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of commitActionPerformed method, of class DataViewActionHandler.
//     */
//    public void testCommitActionPerformed() {
//        System.out.println("commitActionPerformed");
//        boolean selectedOnly = false;
//        DataViewActionHandler instance = null;
//        instance.commitActionPerformed(selectedOnly);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of insertActionPerformed method, of class DataViewActionHandler.
//     */
//    public void testInsertActionPerformed() {
//        System.out.println("insertActionPerformed");
//        DataViewActionHandler instance = null;
//        instance.insertActionPerformed();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of truncateActionPerformed method, of class DataViewActionHandler.
//     */
//    public void testTruncateActionPerformed() {
//        System.out.println("truncateActionPerformed");
//        DataViewActionHandler instance = null;
//        instance.truncateActionPerformed();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of deleteRecordActionPerformed method, of class DataViewActionHandler.
//     */
//    public void testDeleteRecordActionPerformed() {
//        System.out.println("deleteRecordActionPerformed");
//        DataViewActionHandler instance = null;
//        instance.deleteRecordActionPerformed();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of refreshActionPerformed method, of class DataViewActionHandler.
//     */
//    public void testRefreshActionPerformed() {
//        System.out.println("refreshActionPerformed");
//        DataViewActionHandler instance = null;
//        instance.refreshActionPerformed();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}
