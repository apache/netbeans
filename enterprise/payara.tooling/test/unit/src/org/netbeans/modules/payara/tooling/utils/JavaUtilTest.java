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
package org.netbeans.modules.payara.tooling.utils;

import java.io.File;
import java.util.Properties;
import org.netbeans.modules.payara.tooling.CommonTest;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * Test Java related utilities.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
@Test(groups = {"unit-tests"})
public class JavaUtilTest extends CommonTest {

    /**
     * Test <code>JavaVersion.comapreTo</code> functionality.
     */
    @Test
    public void testJavaVersionCompareTo() {
        JavaUtils.JavaVersion version = new JavaUtils.JavaVersion(1, 4, 2, 22);
        // Differs on major numbers.
        assertEquals( 1, version.comapreTo(new JavaUtils.JavaVersion(0, 4, 2, 22)));
        assertEquals(-1, version.comapreTo(new JavaUtils.JavaVersion(2, 4, 2, 22)));
        // Differs on minor numbers.
        assertEquals( 1, version.comapreTo(new JavaUtils.JavaVersion(1, 3, 2, 22)));
        assertEquals(-1, version.comapreTo(new JavaUtils.JavaVersion(1, 5, 2, 22)));
        // Differs on revision numbers.
        assertEquals( 1, version.comapreTo(new JavaUtils.JavaVersion(1, 4, 1, 22)));
        assertEquals(-1, version.comapreTo(new JavaUtils.JavaVersion(1, 4, 3, 22)));
        // Differs on patch numbers.
        assertEquals( 1, version.comapreTo(new JavaUtils.JavaVersion(1, 4, 2, 21)));
        assertEquals(-1, version.comapreTo(new JavaUtils.JavaVersion(1, 4, 2, 23)));
        // Equal values
        assertEquals( 0, version.comapreTo(new JavaUtils.JavaVersion(1, 4, 2, 22)));
    }

    /**
     * Test that <code>javaVmVersion</code> is able to parse Java version
     * output.
     */
    @Test
    public void testJavaVersion() {
        Properties properties = jdkProperties();
        File javaVm = new File(JavaUtils.javaVmExecutableFullPath(
                properties.getProperty(JDKPROP_HOME)));
        Properties p = System.getProperties();
        JavaUtils.JavaVersion version = JavaUtils.javaVmVersion(javaVm);
        assertTrue(version.major > 0);
    }

}
