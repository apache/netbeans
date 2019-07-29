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
package org.netbeans.modules.gradle.java.output;

import static org.junit.Assert.*;
import org.junit.Test;
import org.netbeans.modules.gradle.api.output.OutputDisplayerMock;

/**
 *
 * @author lkishalmi
 */
public class JavaCompilerProcessorFactoryTest {

    private final String JAVA_8_STACKTRACE_1  = "        at org.junit.jupiter.api.AssertionUtils.fail(AssertionUtils.java:55)";
    private final String JAVA_9_STACKTRACE_1  = "        at jdk.zipfs/jdk.nio.zipfs.JarFileSystem.getVersionMap(JarFileSystem.java:137)";
    private final String JAVA_COMPILE_ERROR_1 = "/home/netbeans/NetBeansProjects/gradleproject3/common/src/main/java/gradleproject3/common/NewClass1.java:1: error: class, interface, or enum expected";

    @Test
    public void testStackTraceProcessor1() {
        JavaCompilerProcessorFactory.StackTraceProcessor stp = new JavaCompilerProcessorFactory.StackTraceProcessor();
        OutputDisplayerMock od = new OutputDisplayerMock();
        assertTrue(stp.processLine(od, JAVA_8_STACKTRACE_1));
        assertEquals(JAVA_8_STACKTRACE_1, od.getOutput());
        OutputDisplayerMock.OutputItem[] outputs = od.getOutputs();
        assertEquals(outputs[2].text, "");
        assertEquals(outputs[3].text, "org.junit.jupiter.api.AssertionUtils.fail");
        assertEquals(outputs[5].text, "AssertionUtils.java:55");
        assertNotNull(outputs[5].action);
    }

    @Test
    public void testStackTraceProcessor2() {
        JavaCompilerProcessorFactory.StackTraceProcessor stp = new JavaCompilerProcessorFactory.StackTraceProcessor();
        OutputDisplayerMock od = new OutputDisplayerMock();
        assertTrue(stp.processLine(od, JAVA_9_STACKTRACE_1));
        assertEquals(JAVA_9_STACKTRACE_1, od.getOutput());
        OutputDisplayerMock.OutputItem[] outputs = od.getOutputs();
        assertEquals(outputs[2].text, "jdk.zipfs/");
        assertEquals(outputs[3].text, "jdk.nio.zipfs.JarFileSystem.getVersionMap");
        assertEquals(outputs[5].text, "JarFileSystem.java:137");
        assertNotNull(outputs[5].action);
    }

    @Test
    public void testStackTraceProcessor3() {
        JavaCompilerProcessorFactory.StackTraceProcessor stp = new JavaCompilerProcessorFactory.StackTraceProcessor();
        OutputDisplayerMock od = new OutputDisplayerMock();
        assertFalse(stp.processLine(od, JAVA_COMPILE_ERROR_1));
    }
}
