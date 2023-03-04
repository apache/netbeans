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
package org.netbeans.modules.maven.api.output;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jhavlin
 */
public class OutputUtilsTest {

    @Test
    public void testStackTraceLinePattern() {
        assertTrue(isStackTraceLine("\tat x.y.Test.z(Test.java:123)"));
        assertTrue(isStackTraceLine("\tat x.y.Test.native(Native Method)"));
        assertTrue(isStackTraceLine("[catch]\tat x.y.z.Test.z(Test.java:789)"));
        assertFalse(isStackTraceLine("\tat some.other.line(Example)"));
    }

    private boolean isStackTraceLine(String line) {
        return OutputUtils.linePattern.matcher(line).find();
    }
}
