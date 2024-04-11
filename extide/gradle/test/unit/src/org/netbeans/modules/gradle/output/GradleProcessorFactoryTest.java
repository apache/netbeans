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
package org.netbeans.modules.gradle.output;

import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.gradle.api.output.OutputDisplayerMock;
import static org.netbeans.modules.gradle.output.GradleProcessorFactory.*;

/**
 *
 * @author lkishalmi
 */
public class GradleProcessorFactoryTest {

    private static final String JAVA_8_STACKTRACE_1  = "        at org.junit.jupiter.api.AssertionUtils.fail(AssertionUtils.java:55)";

    private static final String JAVA_COMPILE_ERROR_1 = "/home/netbeans/NetBeansProjects/gradleproject3/common/src/main/java/gradleproject3/common/NewClass1.java:1: error: class, interface, or enum expected";
    private static final String JAVA_COMPILE_ERROR_2 = "D:\\Users\\netbeans\\NetBeansProjects\\gradleproject3\\common\\src\\main\\java\\gradleproject3\\common\\NewClass1.java:1: error: class, interface, or enum expected";

    private static final String GRADLE_SCRIPT_ERROR_1= "Script '/home/netbeans/NetBeansProjects/gradleproject6/build.gradle' line: 6";

    private static final String GROOVY_COMPILE_ERROR_1 = "/home/netbeans/NetBeansProjects/netbeans/groovy/gradle/netbeans-gradle-tooling/src/main/groovy/org/netbeans/modules/gradle/tooling/NetBeansExplodedWarPlugin.groovy: 33: unable to resolve class Prject";
    private static final String GROOVY_COMPILE_ERROR_2 = "/home/netbeans/NetBeansProjects/netbeans/groovy/gradle/netbeans-gradle-tooling/src/main/groovy/org/netbeans/modules/gradle/tooling/NetBeansExplodedWarPlugin.groovy: 43: expecting anything but ''\\n''; got it anyway @ line 43, column 28.";

    private static final String URL_PATTERN_1 = "https://netbeans.apache.org/";
    private static final String URL_PATTERN_2 = "Hello https://netbeans.apache.org";
    private static final String URL_PATTERN_3 = "https://netbeans.apache.org then look at ftp://netbeans.apache.org/";
    private static final String URL_PATTERN_4 = "https://netbeans.apache.org then look at ftp://netbeans.apache.org/ and file:/home/netbeans ";
    private static final String URL_PATTERN_5 = "https://netbeans.apache.org then look at ftp://netbeans.apache.org/ and file:///home/netbeans ";

    @Test
    public void testJavaCompilerProcessor1() {
        OutputDisplayerMock od = new OutputDisplayerMock();
        assertTrue(JAVAC_PROCESSOR.processLine(od, JAVA_COMPILE_ERROR_1));
        OutputDisplayerMock.OutputItem[] outputs = od.getOutputs();
        assertEquals("/home/netbeans/NetBeansProjects/gradleproject3/common/src/main/java/gradleproject3/common/NewClass1.java:1", outputs[0].text);
        assertEquals(": error: class, interface, or enum expected", outputs[1].text);
    }

    @Test
    public void testJavaCompilerProcessor2() {
        OutputDisplayerMock od = new OutputDisplayerMock();
        assertTrue(JAVAC_PROCESSOR.processLine(od, JAVA_COMPILE_ERROR_2));
        OutputDisplayerMock.OutputItem[] outputs = od.getOutputs();
        assertEquals("D:\\Users\\netbeans\\NetBeansProjects\\gradleproject3\\common\\src\\main\\java\\gradleproject3\\common\\NewClass1.java:1", outputs[0].text);
        assertEquals(": error: class, interface, or enum expected", outputs[1].text);
    }

    @Test
    public void testJavaCompilerProcessor3() {
        OutputDisplayerMock od = new OutputDisplayerMock();
        assertFalse(JAVAC_PROCESSOR.processLine(od, GROOVY_COMPILE_ERROR_1));
    }

    @Test
    public void testJavaCompilerProcessor4() {
        OutputDisplayerMock od = new OutputDisplayerMock();
        assertFalse(JAVAC_PROCESSOR.processLine(od, JAVA_8_STACKTRACE_1));
    }

    @Test
    public void testGroovyCompilerProcessor1() {
        OutputDisplayerMock od = new OutputDisplayerMock();
        assertTrue(GROOVYC_PROCESSOR.processLine(od, GROOVY_COMPILE_ERROR_1));
        assertEquals(GROOVY_COMPILE_ERROR_1, od.getOutput());
        OutputDisplayerMock.OutputItem[] outputs = od.getOutputs();
        assertEquals("/home/netbeans/NetBeansProjects/netbeans/groovy/gradle/netbeans-gradle-tooling/src/main/groovy/org/netbeans/modules/gradle/tooling/NetBeansExplodedWarPlugin.groovy: 33", outputs[0].text);
        assertNotNull(outputs[0].action);
        assertEquals(": unable to resolve class Prject", outputs[1].text);
    }

    @Test
    public void testGroovyCompilerProcessor2() {
        OutputDisplayerMock od = new OutputDisplayerMock();
        assertTrue(GROOVYC_PROCESSOR.processLine(od, GROOVY_COMPILE_ERROR_2));
        assertEquals(GROOVY_COMPILE_ERROR_2, od.getOutput());
        OutputDisplayerMock.OutputItem[] outputs = od.getOutputs();
        assertEquals("/home/netbeans/NetBeansProjects/netbeans/groovy/gradle/netbeans-gradle-tooling/src/main/groovy/org/netbeans/modules/gradle/tooling/NetBeansExplodedWarPlugin.groovy: 43", outputs[0].text);
        assertNotNull(outputs[0].action);
        assertEquals(": expecting anything but ''\\n''; got it anyway @ line 43, column 28.", outputs[1].text);
    }

    @Test
    public void testGroovyCompilerProcessor3() {
        OutputDisplayerMock od = new OutputDisplayerMock();
        assertFalse(GROOVYC_PROCESSOR.processLine(od, JAVA_COMPILE_ERROR_1));
    }

    @Test
    public void testURLOutputProcessor1() {
        OutputDisplayerMock od = new OutputDisplayerMock();
        assertFalse(URL_PROCESSOR.processLine(od, JAVA_8_STACKTRACE_1));
    }

    @Test
    public void testURLOutputProcessor2() {
        OutputDisplayerMock od = new OutputDisplayerMock();
        assertFalse(URL_PROCESSOR.processLine(od, JAVA_COMPILE_ERROR_1));
    }

    @Test
    public void testURLOutputProcessor3() {
        OutputDisplayerMock od = new OutputDisplayerMock();
        assertTrue(URL_PROCESSOR.processLine(od, URL_PATTERN_1));
        assertEquals(URL_PATTERN_1, od.getOutput());
        OutputDisplayerMock.OutputItem[] outputs = od.getOutputs();
        assertEquals(3, outputs.length);
        assertNotNull(outputs[1].action);
    }

    @Test
    public void testURLOutputProcessor4() {
        OutputDisplayerMock od = new OutputDisplayerMock();
        assertTrue(URL_PROCESSOR.processLine(od, URL_PATTERN_2));
        assertEquals(URL_PATTERN_2, od.getOutput());
        OutputDisplayerMock.OutputItem[] outputs = od.getOutputs();
        assertNotNull(outputs[1].action);
        assertEquals(3, outputs.length);
    }

    @Test
    public void testURLOutputProcessor5() {
        OutputDisplayerMock od = new OutputDisplayerMock();
        assertTrue(URL_PROCESSOR.processLine(od, URL_PATTERN_3));
        assertEquals(URL_PATTERN_3, od.getOutput());
        OutputDisplayerMock.OutputItem[] outputs = od.getOutputs();
        assertEquals(5, outputs.length);
        assertEquals(" then look at ", outputs[2].text);
        assertNotNull(outputs[1].action);
        assertNotNull(outputs[3].action);
    }

    @Test
    public void testURLOutputProcessor6() {
        OutputDisplayerMock od = new OutputDisplayerMock();
        assertTrue(URL_PROCESSOR.processLine(od, URL_PATTERN_4));
        assertEquals(URL_PATTERN_4, od.getOutput());
        OutputDisplayerMock.OutputItem[] outputs = od.getOutputs();
        assertEquals(7, outputs.length);
        assertEquals(" then look at ", outputs[2].text);
        assertNotNull(outputs[1].action);
        assertNotNull(outputs[3].action);
        assertNotNull(outputs[5].action);
        assertEquals("file:/home/netbeans", outputs[5].text);
    }

    @Test
    public void testURLOutputProcessor7() {
        OutputDisplayerMock od = new OutputDisplayerMock();
        assertTrue(URL_PROCESSOR.processLine(od, URL_PATTERN_5));
        assertEquals(URL_PATTERN_5, od.getOutput());
        OutputDisplayerMock.OutputItem[] outputs = od.getOutputs();
        assertEquals(7, outputs.length);
        assertEquals(" then look at ", outputs[2].text);
        assertNotNull(outputs[1].action);
        assertNotNull(outputs[3].action);
        assertNotNull(outputs[5].action);
        assertEquals("file:///home/netbeans", outputs[5].text);
    }

    @Test
    public void testGradleOutputProcessor1() {
        OutputDisplayerMock od = new OutputDisplayerMock();
        assertTrue(GRADLE_PROCESSOR.processLine(od, GRADLE_SCRIPT_ERROR_1));
        assertEquals(GRADLE_SCRIPT_ERROR_1, od.getOutput());
        OutputDisplayerMock.OutputItem[] outputs = od.getOutputs();
        assertEquals(1, outputs.length);
        assertEquals(GRADLE_SCRIPT_ERROR_1, outputs[0].text);
    }

    @Test
    public void testGradleOutputProcessor2() {
        OutputDisplayerMock od = new OutputDisplayerMock();
        assertFalse(GRADLE_PROCESSOR.processLine(od, JAVA_COMPILE_ERROR_1));
    }

    @Test
    public void testGradleOutputProcessor3() {
        OutputDisplayerMock od = new OutputDisplayerMock();
        assertFalse(GRADLE_PROCESSOR.processLine(od, JAVA_8_STACKTRACE_1));
    }
}
