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
package org.netbeans.modules.debugger.jpda.breakpoints;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class MethodBreakpointImplTest {
    @Test
    public void testRecognizeSimpleConstructor() {
        String res = MethodBreakpointImpl.checkForConstructor("java.lang.String", "String");
        assertEquals("It is constructor", "<init>", res);
    }
    @Test
    public void testRecognizeInnerConstructor() {
        String res = MethodBreakpointImpl.checkForConstructor("java.util.Map$Entry", "Entry");
        assertEquals("It is constructor", "<init>", res);
    }
    @Test
    public void testRecognizeScalaConstructor() {
        String res = MethodBreakpointImpl.checkForConstructor("org.enso.compiler.core.IR$CallArgument$Specified", "IR$CallArgument$Specified");
        assertEquals("It is constructor", "<init>", res);
    }
}
