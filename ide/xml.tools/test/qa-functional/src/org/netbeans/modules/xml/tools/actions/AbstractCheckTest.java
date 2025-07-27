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

package org.netbeans.modules.xml.tools.actions;

import java.util.Arrays;
import org.netbeans.tests.xml.XTest;
import org.openide.nodes.Node;

public abstract class AbstractCheckTest extends XTest {

    /** Creates new AbstractCheckTest */
    public AbstractCheckTest(String testName) {
        super(testName);
    }

    /** Check all selected nodes. */
    protected abstract QaIOReporter performAction(Node[] nodes);

    // LIBS

    /** Checks document located in 'data' folder. */
    protected void performAction(String name, int bugCount) {
        QaIOReporter reporter = performAction(name);
        String message = "\nUnexpected bug count, expected: " + bugCount + " reported: "+ reporter.getBugCount();
        assertEquals(message, bugCount, reporter.getBugCount());
    }
    
    /** Checks document located in 'data' folder. */
    protected void performAction(String name, int[] errLines) {
        QaIOReporter reporter = performAction(name);
        int[] report = reporter.getErrLines();
        Arrays.sort(errLines);
        Arrays.sort(report);
        
        if (!!! Arrays.equals(errLines, report)) {
            String pattern = arrayToString(errLines);
            String result = arrayToString(report);
            fail("\nUnexpected Validation result.\nPattern: " + pattern + "\nResult:  " + result);
        }
    }
    
    /** Checks document located in 'data' folder. */
    protected QaIOReporter performAction(String name) {
        Node node = null;
        try {
            node = TestUtil.THIS.findData(name).getNodeDelegate();
        } catch (Exception ex) {
            ex.printStackTrace(dbg);
            fail("Cannot get Node Delegate for 'data/" + name +"' due:\n" + ex);
        }
        QaIOReporter reporter = performAction(new Node[] {node});
        return reporter;
    }
    
    private static String arrayToString(int[] array) {
        StringBuffer buf = new StringBuffer("[");
        for (int i = 0;  i < array.length; i++) {
            buf.append(array[i]);
            buf.append(", ");
        }
        buf.replace(buf.length() - 2, buf.length(), "]");
        return buf.toString();
    }
}
