/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.web.project;

import org.netbeans.junit.NbTestCase;

/**
 * Test case for {@link Utils}.
 * @author Tomas Mysik
 */
public class UtilsTest extends NbTestCase {

    public UtilsTest(String testName) {
        super(testName);
    }
    
    /**
     * Test for correcting given <tt>debug.classpath</tt>.
     */
    public void testCorrectDebugClassPath() throws Exception { //#118187
        final String NB_55 = "${javac.classpath}:${build.classes.dir}:${build.ear.classes.dir}";
        final String NB_55_EXPECTED = "${javac.classpath}:${build.classes.dir}";
        assertEquals(NB_55_EXPECTED, Utils.correctDebugClassPath(NB_55));
        
        // notice semicolon usage
        final String CASE_1 = "${some.directory};${build.classes.dir}:${javac.classpath}:${another.directory}";
        final String CASE_1_EXPECTED = "${some.directory};${build.classes.dir}:${javac.classpath}:${another.directory}";
        assertEquals(CASE_1_EXPECTED, Utils.correctDebugClassPath(CASE_1));
        
        final String CASE_2 = null;
        final String CASE_2_EXPECTED = Utils.getDefaultDebugClassPath();
        assertEquals(CASE_2_EXPECTED, Utils.correctDebugClassPath(CASE_2));
        
        final String CASE_3 = "${j2ee.platform.classpath}:${build.ear.classes.dir}";
        final String CASE_3_EXPECTED = "${j2ee.platform.classpath}";
        assertEquals(CASE_3_EXPECTED, Utils.correctDebugClassPath(CASE_3));
        
        // incorrect classpath => remains incorrect
        final String CASE_4 = "defect";
        final String CASE_4_EXPECTED = "defect";
        assertEquals(CASE_4_EXPECTED, Utils.correctDebugClassPath(CASE_4));
        
        final String CASE_5 = "${some.directory}";
        final String CASE_5_EXPECTED = "${some.directory}";
        assertEquals(CASE_5_EXPECTED, Utils.correctDebugClassPath(CASE_5));
    }

}
