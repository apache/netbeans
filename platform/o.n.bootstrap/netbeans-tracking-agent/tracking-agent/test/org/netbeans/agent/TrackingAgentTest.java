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
                "going to write using File: checkFileWrite: TEMP-FILE\n" +
                "File.canExecute: checkFileRead: TEMP-FILE\n" +
                "File.canRead: checkFileRead: TEMP-FILE\n" +
                "File.canWrite: checkFileRead: TEMP-FILE\n" +
                "File.createNewFile: checkFileWrite: TEMP-FILE\n" +
                "File.isDirectory: checkFileRead: TEMP-FILE\n" +
                "File.isFile: checkFileRead: TEMP-FILE\n" +
                "File.isHidden: checkFileRead: TEMP-FILE\n" +
                "File.length: checkFileRead: TEMP-FILE\n" +
                "File.exists: checkFileRead: TEMP-FILE\n" +
                "File.setExecutable(boolean): checkFileWrite: TEMP-FILE\n" +
                "File.setExecutable(boolean, boolean): checkFileWrite: TEMP-FILE\n" +
                "File.setReadable(boolean): checkFileWrite: TEMP-FILE\n" +
                "File.setReadable(boolean, boolean): checkFileWrite: TEMP-FILE\n" +
                "File.setWritable(boolean): checkFileWrite: TEMP-FILE\n" +
                "File.setWritable(boolean, boolean): checkFileWrite: TEMP-FILE\n" +
                "File.setReadOnly(): checkFileWrite: TEMP-FILE\n" +
                "File.setLastModified(): going to delete using File: checkDelete: TEMP-FILE\n" +
                "going to mkdir using File: checkFileWrite: TEMP-FILE\n" +
                "going to mkdirs using File: checkFileWrite: TEMP-FILE\n" +
                "checkFileRead: TEMP-FILE\n" +
                "going to list using File: \n" +
                "1: checkFileRead: TEMP-FILE\n" +
                "2: checkFileRead: TEMP-FILE\n" +
                "3: checkFileRead: TEMP-FILE\n" +
                "4: checkFileRead: TEMP-FILE\n" +
                "5: checkFileRead: TEMP-FILE\n" +
                "going to delete using File: checkDelete: TEMP-FILE\n" +
                "going to mkdirs using File: checkFileWrite: TEMP-FILE\n" +
                "going to delete using File: checkDelete: TEMP-FILE\n" +
                "going to write using Path: checkFileWrite: TEMP-FILE\n" +
                "checkFileRead: /home/lahvac/tools/netbeans/trunk/jdk-platform/lib/libnet.so\n" +
                "checkFileRead: /home/lahvac/tools/netbeans/trunk/jdk-platform/lib/libnio.so\n" +
                "delete using Path: checkDelete: TEMP-FILE\n" +
                "going to mkdir using Path: checkFileWrite: TEMP-FILE\n" +
                "going to mkdirs using Path: checkFileWrite: TEMP-FILE\n" +
                "checkFileRead: TEMP-FILE\n" +
                "checkFileRead: TEMP-FILE\n" +
                "going to list using Path: \n" +
                "1: checkFileRead: TEMP-FILE\n" +
                "2: checkFileRead: TEMP-FILE\n" +
                "3: checkFileRead: TEMP-FILE\n" +
                "delete using Path: checkDelete: TEMP-FILE\n" +
                "going to write using String: checkFileWrite: TEMP-FILE\n" +
                "going to read using File: checkFileRead: TEMP-FILE\n" +
                "going to read using Path: checkFileRead: TEMP-FILE\n" +
                "going to read using String: checkFileRead: TEMP-FILE\n" +
                "going to open using RandomAccessFile using String: checkFileRead: TEMP-FILE\n" +
                "checkFileWrite: TEMP-FILE\n" +
                "going to open using RandomAccessFile using File: checkFileRead: TEMP-FILE\n" +
                "checkFileWrite: TEMP-FILE\n" +
                "going to open using newByteChannel: \n" +
                "1a: checkFileRead: TEMP-FILE\n" +
                "1b: checkFileRead: TEMP-FILE\n" +
                "1c: checkFileWrite: TEMP-FILE\n" +
                "1d: checkFileRead: TEMP-FILE\n" +
                "checkFileWrite: TEMP-FILE\n" +
                "2a: checkFileRead: TEMP-FILE\n" +
                "2b: checkFileRead: TEMP-FILE\n" +
                "2c: checkFileWrite: TEMP-FILE\n" +
                "2d: checkFileRead: TEMP-FILE\n" +
                "checkFileWrite: TEMP-FILE\n" +
                "Files.readAttributes(Path, Class): checkFileRead: TEMP-FILE\n" +
                "Files.readAttributes(Path, String): checkFileRead: TEMP-FILE\n" +
                "Files.getAttribute: checkFileRead: TEMP-FILE\n" +
                "Files.getFileAttributeView: checkFileRead: TEMP-FILE\n" +
                "Files.isDirectory: checkFileRead: TEMP-FILE\n" +
                "Files.isExecutable: checkFileRead: TEMP-FILE\n" +
                "Files.isHidden: checkFileRead: TEMP-FILE\n" +
                "Files.isReadable: checkFileRead: TEMP-FILE\n" +
                "Files.isRegularFile: checkFileRead: TEMP-FILE\n" +
                "Files.isSameFile: checkFileRead: TEMP-FILE\n" +
                "checkFileRead: TEMP-FILE\n" +
                "Files.isWritable: checkFileRead: TEMP-FILE\n" +
                "Files.getLastModifiedTime: checkFileRead: TEMP-FILE\n" +
                "Files.getOwner: checkFileRead: TEMP-FILE\n" +
                "Files.size: checkFileRead: TEMP-FILE\n" +
                "Files.setAttribute: checkFileWrite: TEMP-FILE\n" +
                "Files.setLastModifiedTime: checkFileRead: TEMP-FILE\n" +
                "Files.setOwner: checkFileRead: TEMP-FILE\n");
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
