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

package org.netbeans.modules.gradle.api.execute;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Laszlo Kishalmi
 */
public class GradleCommandLineTest {
    
    @Test
    public void testGetSupportedCommandLine() {
        System.out.println("getSupportedCommandLine");
        GradleCommandLine instance = new GradleCommandLine("--offline", "--no-daemon");
        List<String> expResult = Arrays.asList("--offline");
        List<String> result = instance.getSupportedCommandLine();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetFullCommandLine() {
        System.out.println("getFullCommandLine");
        GradleCommandLine instance = new GradleCommandLine("--offline", "--no-daemon");
        List<String> expResult = Arrays.asList("--offline", "--no-daemon");
        List<String> result = instance.getFullCommandLine();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetTasks() {
        System.out.println("getTasks");
        GradleCommandLine instance = new GradleCommandLine("-a", "clean", "build");
        Set<String> expResult = new HashSet<>(Arrays.asList("clean", "build"));
        Set<String> result = instance.getTasks();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetTasks() {
        System.out.println("setTasks");
        Set<String> tasks = Collections.singleton("assemble");
        GradleCommandLine instance = new GradleCommandLine("-a", "clean", "build");
        instance.setTasks(tasks);
        assertEquals(tasks, instance.getTasks());
        assertTrue(instance.hasFlag(GradleCommandLine.Flag.NO_REBUILD));
    }

    @Test
    public void testRemoveTask() {
        System.out.println("removeTask");
        GradleCommandLine instance = new GradleCommandLine("-a", "clean", "build");
        instance.removeTask("clean");
        assertEquals(Collections.singleton("build"), instance.getTasks());
    }

    @Test
    public void testAddTask() {
        System.out.println("addTask");
        GradleCommandLine instance = new GradleCommandLine("-a", "clean");
        instance.addTask("build");
        assertEquals(new HashSet<String>(Arrays.asList("clean", "build")), instance.getTasks());
    }

    @Test
    public void testHasTask() {
        System.out.println("hasTask");
        GradleCommandLine instance = new GradleCommandLine("-a", "clean");
        assertTrue(instance.hasTask("clean"));
        assertFalse(instance.hasTask("build"));
    }

    @Test
    public void testHasFlag() {
        System.out.println("hasFlag");
        GradleCommandLine.Flag flag = GradleCommandLine.Flag.CONFIGURE_ON_DEMAND;
        GradleCommandLine instance = new GradleCommandLine("--configure-on-demand", "build");
        assertTrue(instance.hasFlag(flag));
        assertFalse(instance.hasFlag(GradleCommandLine.Flag.OFFLINE));
    }

    @Test
    public void testAddFlag() {
        System.out.println("addFlag");
        GradleCommandLine.Flag flag = GradleCommandLine.Flag.NO_REBUILD;
        GradleCommandLine instance = new GradleCommandLine("--configure-on-demand", "build");
        instance.addFlag(flag);
        assertTrue(instance.hasFlag(flag));
        assertTrue(instance.hasFlag(GradleCommandLine.Flag.CONFIGURE_ON_DEMAND));
        assertFalse(instance.hasFlag(GradleCommandLine.Flag.OFFLINE));
    }

    @Test
    public void testRemoveFlag() {
        System.out.println("removeFlag");
        GradleCommandLine.Flag flag = GradleCommandLine.Flag.NO_REBUILD;
        GradleCommandLine instance = new GradleCommandLine("-a", "--configure-on-demand", "build");
        instance.removeFlag(flag);
        assertFalse(instance.hasFlag(flag));
        assertTrue(instance.hasFlag(GradleCommandLine.Flag.CONFIGURE_ON_DEMAND));
        assertFalse(instance.hasFlag(GradleCommandLine.Flag.OFFLINE));
    }

    @Test
    public void testSetFlag() {
        System.out.println("setFlag");
        GradleCommandLine instance = new GradleCommandLine("--configure-on-demand", "build");
        instance.setFlag(GradleCommandLine.Flag.CONFIGURE_ON_DEMAND, false);
        instance.setFlag(GradleCommandLine.Flag.NO_REBUILD, true);
        assertEquals(Arrays.asList("-a", "build"), instance.getSupportedCommandLine());
    }

    @Test
    public void testAddParameter() {
        System.out.println("addParameter");
        GradleCommandLine instance = new GradleCommandLine("--configure-on-demand", "build");
        instance.addParameter(GradleCommandLine.Parameter.EXCLUDE_TASK, "test");
        assertEquals(Arrays.asList("--configure-on-demand", "-x", "test", "build"), instance.getSupportedCommandLine());
    }

    @Test
    public void testGetFirstParameter() {
        System.out.println("getFirstParameter");
        GradleCommandLine instance = new GradleCommandLine("build", "-x", "test", "-x", "check");
        String result = instance.getFirstParameter(GradleCommandLine.Parameter.EXCLUDE_TASK);
        assertEquals("test", result);
    }

    @Test
    public void testHasParameter() {
        System.out.println("hasParameter");
        GradleCommandLine instance = new GradleCommandLine("build", "-x", "test", "-x", "check");
        assertTrue(instance.hasParameter(GradleCommandLine.Parameter.EXCLUDE_TASK));
        assertFalse(instance.hasParameter(GradleCommandLine.Parameter.CONSOLE));
    }

    @Test
    public void testGetParameters() {
        System.out.println("getParameters");
        GradleCommandLine instance = new GradleCommandLine("build", "-x", "test", "-x", "check");
        assertTrue(instance.hasParameter(GradleCommandLine.Parameter.EXCLUDE_TASK));
        assertFalse(instance.hasParameter(GradleCommandLine.Parameter.INIT_SCRIPT));
    }

    @Test
    public void testGetExcludedTasks() {
        System.out.println("getExcludedTasks");
        GradleCommandLine instance = new GradleCommandLine("build", "-x", "test", "-x", "check");
        Set<String> result = instance.getExcludedTasks();
        assertTrue(result.containsAll(Arrays.asList("test", "check")));
    }

    @Test
    public void testSetExcludedTasks() {
        System.out.println("setExcludedTasks");
        GradleCommandLine instance = new GradleCommandLine("build");
        instance.setExcludedTasks(Arrays.asList("check", "test"));
        assertEquals(Arrays.asList("-x", "check", "-x", "test", "build"), instance.getSupportedCommandLine());
    }

    @Test
    public void testRemoveParameters() {
        System.out.println("removeParameters");
        GradleCommandLine.Parameter param = GradleCommandLine.Parameter.INIT_SCRIPT;
        GradleCommandLine instance = new GradleCommandLine("--init-script", "init.gradle");
        instance.removeParameters(param);
        assertFalse(instance.hasParameter(param));
    }

    @Test
    public void testGetProperty() {
        System.out.println("getProperty");
        GradleCommandLine instance = new GradleCommandLine("--system-prop", "HELLO=NetBeans", "-Pgreet=World");
        assertEquals("NetBeans", instance.getProperty(GradleCommandLine.Property.SYSTEM, "HELLO"));
        assertEquals("World", instance.getProperty(GradleCommandLine.Property.PROJECT, "greet"));
    }

    @Test
    public void testGetLoglevel() {
        System.out.println("getLoglevel");
        assertEquals(GradleCommandLine.LogLevel.WARN, new GradleCommandLine().getLoglevel());
        assertEquals(GradleCommandLine.LogLevel.INFO, new GradleCommandLine("--info").getLoglevel());
        assertEquals(GradleCommandLine.LogLevel.DEBUG, new GradleCommandLine("-d").getLoglevel());
        assertEquals(GradleCommandLine.LogLevel.QUIET, new GradleCommandLine("-q").getLoglevel());
    }

    @Test
    public void testSetLogLevel() {
        System.out.println("setLogLevel");
        GradleCommandLine.LogLevel level = GradleCommandLine.LogLevel.QUIET;
        GradleCommandLine instance = new GradleCommandLine("--debug");
        instance.setLogLevel(level);
        assertEquals(GradleCommandLine.LogLevel.QUIET, instance.getLoglevel());
    }

    @Test
    public void testAddProjectProperty() {
        System.out.println("addProjectProperty");
        GradleCommandLine instance = new GradleCommandLine("build");
        instance.addProjectProperty("version", "1.0.0");
        assertEquals(Arrays.asList("-Pversion=1.0.0", "build"), instance.getFullCommandLine());
    }

    @Test
    public void testAddSystemProperty() {
        System.out.println("addSystemProperty");
        GradleCommandLine instance = new GradleCommandLine("build");
        instance.addSystemProperty("hello", "NetBeans");
        assertEquals(Arrays.asList("-Dhello=NetBeans", "build"), instance.getSupportedCommandLine());
    }

    @Test
    public void testGetStackTrace() {
        System.out.println("getStackTrace");
        assertEquals(GradleCommandLine.StackTrace.NONE, new GradleCommandLine().getStackTrace());
        assertEquals(GradleCommandLine.StackTrace.SHORT, new GradleCommandLine("-s").getStackTrace());
        assertEquals(GradleCommandLine.StackTrace.FULL, new GradleCommandLine("-S").getStackTrace());
    }

    @Test
    public void testSetStackTrace() {
        System.out.println("setStackTrace");
        GradleCommandLine.StackTrace st = GradleCommandLine.StackTrace.NONE;
        GradleCommandLine instance = new GradleCommandLine("-S");
        instance.setStackTrace(st);
        assertEquals(GradleCommandLine.StackTrace.NONE, instance.getStackTrace());
    }

    @Test
    public void testCombine1() {
        GradleCommandLine first = new GradleCommandLine("-x", "test", "build");
        GradleCommandLine second = new GradleCommandLine("test");
        GradleCommandLine cmd = GradleCommandLine.combine(first, second);
        assertTrue(cmd.getExcludedTasks().isEmpty());
        assertTrue(cmd.getTasks().contains("test"));
        assertTrue(cmd.getTasks().contains("build"));
    }

    @Test
    public void testCombine2() {
        GradleCommandLine first = new GradleCommandLine("-x", "test", "build");
        GradleCommandLine second = new GradleCommandLine("test");
        GradleCommandLine cmd = GradleCommandLine.combine(second, first);
        assertTrue(cmd.getExcludedTasks().contains("test"));
        assertFalse(cmd.getTasks().contains("test"));
        assertTrue(cmd.getTasks().contains("build"));
    }

    @Test
    public void testCombine3() {
        GradleCommandLine first = new GradleCommandLine("-Pversion=1.0", "build");
        GradleCommandLine second = new GradleCommandLine("-Pversion=2.0");
        GradleCommandLine cmd = GradleCommandLine.combine(first, second);
        assertEquals(Arrays.asList("-Pversion=2.0", "build"), cmd.getSupportedCommandLine());
    }

    @Test
    public void testJVMArgs1() throws IOException {
        TemporaryFolder root = new TemporaryFolder();
        root.create();
        File props = root.newFile("gradle.properties");
        Files.write(props.toPath(), Arrays.asList("org.gradle.jvmargs=\"-Dfile.encoding=UTF-8\" -Xmx2g"));
        List<String> jvmargs = new ArrayList<>();
        GradleCommandLine.addGradleSettingJvmargs(root.getRoot(), jvmargs);
        assertEquals(Arrays.asList("-Dfile.encoding=UTF-8", "-Xmx2g"), jvmargs);
    }

    @Test
    public void testJVMArgs2() throws IOException {
        TemporaryFolder root = new TemporaryFolder();
        root.create();
        File props = root.newFile("gradle.properties");
        Files.write(props.toPath(), Arrays.asList("org.gradle.jvmargs=\"-Dfile.encoding=UTF-8\" -Dsomething=\"space value\""));
        List<String> jvmargs = new ArrayList<>();
        GradleCommandLine.addGradleSettingJvmargs(root.getRoot(), jvmargs);
        assertEquals(Arrays.asList("-Dfile.encoding=UTF-8", "-Dsomething=space value"), jvmargs);
    }
    
    @Test
    public void testDescriptions() {
        Set<GradleCommandLine.GradleOptionItem> all = new HashSet<>();
        all.addAll(Arrays.asList(GradleCommandLine.Flag.values()));
        all.addAll(Arrays.asList(GradleCommandLine.Parameter.values()));
        all.addAll(Arrays.asList(GradleCommandLine.Property.values()));
        all.remove(GradleCommandLine.Parameter.IMPORT_BUILD);
        List<GradleCommandLine.GradleOptionItem> missing = new ArrayList<>();
        for (GradleCommandLine.GradleOptionItem item : all) {
            try {
                assertNotNull(item.getDescription());
            } catch (MissingResourceException ex){
                missing.add(item);
            }
        }
        assertTrue(missing.toString(), missing.isEmpty());
    }

    @Test
    public void testUnsupportedArg1() {
        GradleCommandLine cmd = new GradleCommandLine("--gui");
        cmd = new GradleCommandLine(GradleDistributionManager.get().defaultDistribution(), cmd);
        assertTrue(cmd.getFullCommandLine().isEmpty());
    }

    @Test
    public void testUnsupportedArg2() {
        GradleCommandLine cmd = new GradleCommandLine("--include-build", "../something");
        cmd = new GradleCommandLine(GradleDistributionManager.get().distributionFromVersion("2.2"), cmd);
        assertTrue(cmd.getFullCommandLine().isEmpty());
    }

}
