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
    abstract protected QaIOReporter performAction(Node[] nodes);

    // LIBS ////////////////////////////////////////////////////////////////////

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
