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
package org.netbeans.modules.maven.api.output;

import org.junit.Test;
import org.netbeans.modules.maven.api.output.OutputUtils.StacktraceAttributes;

import static org.junit.Assert.*;

/**
 *
 * @author jhavlin
 */
public class OutputUtilsTest {

    @Test
    public void testStackTraceLine1() {
        checkStackTraceLineAttributes("\tat x.y.Test.z(Test.java:123)", "x.y.Test.z", "Test", "123");
    }

    @Test
    public void testStackTraceLine2() {
        checkStackTraceLineAttributes("[catch]\tat x.y.z.Test.z(Test.java:789)", "x.y.z.Test.z", "Test", "789");
    }

    @Test
    public void testStackTraceLine3() {
        checkStackTraceLineAttributes(" at Mavenproject1@0.1-SNAPSHOT/dev.mbien.mavenproject1.Mavenproject1.foo(Mavenproject1.java:11)",
                "Mavenproject1@0.1-SNAPSHOT/dev.mbien.mavenproject1.Mavenproject1.foo", "Mavenproject1", "11");
    }

    @Test
    public void testStackTraceLineWithNoLinkAttributes1() {
        checkNoStackTraceLineAttributes("\tat some.other.line(Example)");
    }

    @Test
    public void testStackTraceLineWithNoLinkAttributes2() {
        checkNoStackTraceLineAttributes("\tat x.y.Test.native(Native Method)");
    }

    private static void checkStackTraceLineAttributes(String line, String method, String file, String lineNum) {
        StacktraceAttributes attribs = OutputUtils.matchStackTraceLine(line);
        assertNotNull(attribs);
        assertEquals(method, attribs.method);
        assertEquals(file, attribs.file);
        assertEquals(lineNum, attribs.lineNum);
    }
    
    private static void checkNoStackTraceLineAttributes(String line) {
        assertNull(OutputUtils.matchStackTraceLine(line));
    } 
    
}
