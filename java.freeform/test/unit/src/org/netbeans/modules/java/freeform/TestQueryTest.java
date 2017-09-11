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

package org.netbeans.modules.java.freeform;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.modules.ant.freeform.TestBase;
import org.openide.filesystems.FileObject;

/**
 * Check that the unit test query works.
 * @author Jesse Glick
 */
public class TestQueryTest extends TestBase {

    public TestQueryTest(String name) {
        super(name);
    }

    private FileObject src1, src1a, src2, test1, test2;

    protected void setUp() throws Exception {
        super.setUp();
        src1 = simple2.getProjectDirectory().getFileObject("src1");
        assertNotNull("have src1", src1);
        src1a = simple2.getProjectDirectory().getFileObject("src1a");
        assertNotNull("have src1a", src1a);
        src2 = simple2.getProjectDirectory().getFileObject("src2");
        assertNotNull("have src2", src2);
        test1 = simple2.getProjectDirectory().getFileObject("test1");
        assertNotNull("have test1", test1);
        test2 = simple2.getProjectDirectory().getFileObject("test2");
        assertNotNull("have test2", test2);
    }
    
    public void testFindUnitTests() throws Exception {
        URL[] tests = new URL[] {
            test1.getURL(),
            test2.getURL(),
        };
        assertEquals("correct tests for src1", Arrays.asList(tests), Arrays.asList(UnitTestForSourceQuery.findUnitTests(src1)));
        assertEquals("correct tests for src1a", Arrays.asList(tests), Arrays.asList(UnitTestForSourceQuery.findUnitTests(src1a)));
        assertEquals("correct tests for src2", Arrays.asList(tests), Arrays.asList(UnitTestForSourceQuery.findUnitTests(src2)));
        assertEquals("no tests for test1", Collections.EMPTY_LIST, Arrays.asList(UnitTestForSourceQuery.findUnitTests(test1)));
    }
    
    public void testFindSources() throws Exception {
        URL[] sources = new URL[] {
            src1.getURL(),
            src1a.getURL(),
            src2.getURL(),
        };
        assertEquals("correct sources for test1", Arrays.asList(sources), Arrays.asList(UnitTestForSourceQuery.findSources(test1)));
        assertEquals("correct sources for test2", Arrays.asList(sources), Arrays.asList(UnitTestForSourceQuery.findSources(test2)));
        assertEquals("no sources for src1", Collections.EMPTY_LIST, Arrays.asList(UnitTestForSourceQuery.findSources(src1)));
    }

}
