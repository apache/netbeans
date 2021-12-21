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
package org.netbeans.agent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;
import org.netbeans.agent.hooks.TrackingHooks;
import static org.junit.Assert.*;

public class TrackingAgentTest {

    public TrackingAgentTest() {
    }

    @Test
    public void testSecurityManager() throws Exception {
        runTest(TestSecurityManager.class,
                "check exit called with: -2\n" +
                "caught expected SecurityException\n");
    }

    @Test
    public void testIO() throws Exception {
        runTest(TestIO.class,
                "going to write using File:\n" +
                "checkFileWrite: TEMP-FILE\n" +
                "going to delete using File:\n" +
                "checkDelete: TEMP-FILE\n" +
                "going to write using Path:\n" +
                "checkFileWrite: TEMP-FILE\n" +
                "delete using Path:\n" +
                "checkDelete: TEMP-FILE\n" +
                "going to write using String:\n" +
                "checkFileWrite: TEMP-FILE\n" +
                "going to read using File:\n" +
                "checkFileRead: TEMP-FILE\n" +
                "going to read using Path:\n" +
                "checkFileRead: TEMP-FILE\n" +
                "going to read using String:\n" +
                "checkFileRead: TEMP-FILE\n");
    }

    @Test
    public void testSystemProperty() throws Exception {
        runTest(TestSystemProperty.class,
                "going to read property without default:\n" +
                "checkSystemProperty: property\n" +
                "going to read property with default:\n" +
                "checkSystemProperty: property\n" +
                "going to clear property:\n" +
                "checkSystemProperty: property\n");
    }

    @Test
    public void testSetAccessible() throws Exception {
        runTest(TestSetAccessible.class,
                "going to make a field accessible:\n" +
                "checkSetAccessible: java.lang.reflect.Field\n" +
                "going to make a constructor accessible:\n" +
                "checkSetAccessible: java.lang.reflect.Constructor\n" +
                "going to make a method accessible:\n" +
                "checkSetAccessible: java.lang.reflect.Method\n");
    }

    @Test
    public void testSetSecurityManager() throws Exception {
        runTest(TestSetSecurityManager.class,
                "going to set SecurityManager:\n" +
                "setSecurityManager: testing SecurityManager\n" +
                "got SecurityException.\n");
    }

    @Test
    public void testNewAWTWindow() throws Exception {
        runTest(TestWindow.class,
                "going to create new Window(Frame):\n" +
                "checkNewAWTWindow\n" +
                "going to create new Window(Window):\n" +
                "checkNewAWTWindow\n" +
                "going to create new Window(Window, GraphicsConfiguration):\n" +
                "checkNewAWTWindow\n");
    }

    private void runTest(Class<?> testClass, String expected) throws Exception {
        Path javaHome = findJavaHome();
        String agentPath = Paths.get(TrackingAgent.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent().getParent().resolve("dist").resolve("tracking-agent.jar").toString();
        String hooksPath = Paths.get(TrackingHooks.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        String testPath = Paths.get(TrackingAgentTest.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        Process p = new ProcessBuilder(javaHome.resolve("bin").resolve("java").toString(), "-javaagent:" + agentPath, "-Xbootclasspath/a:" + hooksPath, "-classpath", testPath, testClass.getName()).start();
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        Thread c1 = copy(p.getInputStream(), dataStream);
        Thread c2 = copy(p.getErrorStream(), dataStream);
        p.waitFor();
        c1.join();
        c2.join();
        String data = new String(dataStream.toByteArray());
        assertEquals(expected, data);
    }

    private static Path findJavaHome() {
        //XXX: surround with try-catch fix in lambda bodies!
        return Paths.get(System.getProperty("java.home"));
    }

    private static Thread copy(InputStream from, OutputStream to) {
        Thread t = new Thread(() -> {
            try {
                int r;
                while ((r = from.read()) != (-1)) {
                    to.write(r);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        t.start();
        return t;
    }

}
