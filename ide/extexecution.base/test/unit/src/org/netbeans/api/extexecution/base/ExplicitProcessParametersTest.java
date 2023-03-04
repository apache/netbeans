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
package org.netbeans.api.extexecution.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;

/**
 *
 * @author sdedic
 */
public class ExplicitProcessParametersTest extends NbTestCase {

    public ExplicitProcessParametersTest(String name) {
        super(name);
    }
    
    List<String> existingVMArgs = new ArrayList<>(Arrays.asList(
        "-Xmx100m"
    ));
    
    List<String> existingAppArgs = new ArrayList<>(Arrays.asList(
        "File1"
    ));
    
    private void assertContains(List<String> args, String... items) {
        for (String s : items) {
            assertTrue("Must contain: " + s, args.stream().map(String::trim).filter(a -> s.equals(a)).findAny().isPresent());
        }
    }
    
    private void assertNotContains(List<String> args, String... items) {
        for (String s : items) {
            assertFalse("Must NOT contain: " + s, args.stream().map(String::trim).filter(a -> s.equals(a)).findAny().isPresent());
        }
    }
    
    /**
     * Empty params, or params created w/o any content should have no effect when applied.
     * 
     * @throws Exception 
     */
    public void testEmptyExplicitParameters() throws Exception {
        ExplicitProcessParameters empty = ExplicitProcessParameters.empty();
        assertTrue(empty.isEmpty());
        assertFalse(empty.isArgReplacement());
        assertFalse(empty.isLauncherArgReplacement());

        ExplicitProcessParameters empty2 = ExplicitProcessParameters.builder().build();
        assertTrue(empty2.isEmpty());
        assertFalse(empty2.isArgReplacement());
        assertFalse(empty2.isLauncherArgReplacement());
        
        ExplicitProcessParameters base = ExplicitProcessParameters.builder().
                launcherArgs(existingVMArgs).
                args(existingAppArgs).
                build();
        
        ExplicitProcessParameters p = ExplicitProcessParameters.builder().
                combine(base).
                combine(empty).
                build();
        
        ExplicitProcessParameters p2 = ExplicitProcessParameters.builder().
                combine(base).
                combine(empty).
                build();
        
        assertEquals(existingVMArgs, p.getLauncherArguments());
        assertEquals(existingAppArgs, p.getArguments());

        assertEquals(existingVMArgs, p2.getLauncherArguments());
        assertEquals(existingAppArgs, p2.getArguments());
    }
    
    public void testSingleAddVMParams() throws Exception {
        ExplicitProcessParameters extra = ExplicitProcessParameters.builder().
                launcherArg("-Dfoo=bar").
                build();
        
        assertFalse("No override requested", extra.isLauncherArgReplacement());
        assertFalse("No arguments given", extra.isArgReplacement());
        assertNull(extra.getArguments());

        ExplicitProcessParameters p = ExplicitProcessParameters.builder().
                launcherArgs(existingVMArgs).
                args(existingAppArgs).
                combine(extra).
                build();
        
        
        assertContains(p.getLauncherArguments(), "-Xmx100m", "-Dfoo=bar");
    }
    
    public void testSingleReplaceAppParams() throws Exception {
        ExplicitProcessParameters extra = ExplicitProcessParameters.builder().
                arg("avalanche").
                build();
        
        ExplicitProcessParameters p = ExplicitProcessParameters.builder().
                launcherArgs(existingVMArgs).
                args(existingAppArgs).
                combine(extra).
                build();

        assertFalse("No override requested", extra.isLauncherArgReplacement());
        assertTrue("Args must be replaced by default", extra.isArgReplacement());
        assertNull(extra.getLauncherArguments());

        
        assertContains(p.getArguments(), "avalanche");
        assertNotContains(p.getArguments(), "File1");
    }
    
    public void testSingleDefaultLaunchAugmentation() throws Exception {
        ExplicitProcessParameters extra = ExplicitProcessParameters.builder().
                arg("avalanche").
                launcherArg("-Dfoo=bar").
                build();
        
        assertFalse("No prio override requested", extra.isLauncherArgReplacement());
        assertTrue("Args must be replaced by default", extra.isArgReplacement());
        assertNotNull(extra.getLauncherArguments());
        assertNotNull(extra.getArguments());

        ExplicitProcessParameters p = ExplicitProcessParameters.builder().
                launcherArgs(existingVMArgs).
                args(existingAppArgs).
                combine(extra).
                build();
        
        assertContains(p.getLauncherArguments(), "-Xmx100m", "-Dfoo=bar");
        assertContains(p.getArguments(), "avalanche");
        assertNotContains(p.getArguments(), "File1");
    }
    
    /**
     * Checks that VM parmeters can be replaced.
     * @throws Exception 
     */
    public void testReplaceLauncherArgs() throws Exception {
        ExplicitProcessParameters extra = ExplicitProcessParameters.builder().
                launcherArg("-Dfoo=bar").
                replaceLauncherArgs(true).
                build();
        
        ExplicitProcessParameters extra2 = ExplicitProcessParameters.builder().
                launcherArg("-Dsun=shines").
                build();

        assertTrue("Must replace launcher args", extra.isLauncherArgReplacement());
        assertFalse("No arguments were specified", extra.isArgReplacement());
        assertNull(extra.getArguments());

        
        ExplicitProcessParameters p = ExplicitProcessParameters.builder().
                launcherArgs(existingVMArgs).
                args(existingAppArgs).
                combine(extra).
                build();
        
        assertContains(p.getLauncherArguments(), "-Dfoo=bar");
        assertNotContains(p.getLauncherArguments(), "-Xmx100m");
        
        ExplicitProcessParameters p2 = ExplicitProcessParameters.builder().
                launcherArgs(existingVMArgs).
                args(existingAppArgs).
                combine(
                    ExplicitProcessParameters.buildExplicitParameters(Arrays.asList(extra, extra2))
                ).
                build();
        
        assertContains(p2.getLauncherArguments(), "-Dfoo=bar", "-Dsun=shines");
        assertNotContains(p2.getLauncherArguments(), "-Xmx100m");
    }
    
    public void testAppendNormalArgs() throws Exception {
        ExplicitProcessParameters extra = ExplicitProcessParameters.builder().
                replaceArgs(false).
                args("File2", "File3").
                build();
        
        assertFalse("Must append args", extra.isArgReplacement());
        assertFalse("No prio arguments were specified", extra.isLauncherArgReplacement());
        assertNull(extra.getLauncherArguments());

        
        ExplicitProcessParameters p = ExplicitProcessParameters.builder().
                launcherArgs(existingVMArgs).
                args(existingAppArgs).
                combine(extra).
                build();
        
        assertContains(p.getLauncherArguments(), "-Xmx100m");
        assertEquals(Arrays.asList("File1", "File2", "File3"), p.getArguments());
    }
    
    public void testAddMoreLauncherArgs() throws Exception {
        ExplicitProcessParameters extra = ExplicitProcessParameters.builder().
                launcherArg("-Dfoo=bar").
                launcherArg("-Xms=200m").
                replaceLauncherArgs(true).
                build();
        
        ExplicitProcessParameters p = ExplicitProcessParameters.builder().
                launcherArgs(existingVMArgs).
                args(existingAppArgs).
                combine(extra).
                build();
        
        assertContains(p.getLauncherArguments(), "-Dfoo=bar", "-Xms=200m");
        assertNotContains(p.getLauncherArguments(), "-Xmx100m");
    }
    
    public void testReplaceWithMoreArgs() throws Exception {
        ExplicitProcessParameters extra = ExplicitProcessParameters.builder().
                arg("avalanche").
                arg("storm").
                build();
        
        ExplicitProcessParameters p = ExplicitProcessParameters.builder().
                launcherArgs(existingVMArgs).
                args(existingAppArgs).
                combine(extra).
                build();
        
        assertContains(p.getArguments(), "avalanche", "storm");
        assertNotContains(p.getArguments(), "File1");
    }
    
    public void testJustClearArguments() throws Exception {
        ExplicitProcessParameters extra = ExplicitProcessParameters.builder().
                replaceArgs(true).
                replaceLauncherArgs(true).
                build();
        
        ExplicitProcessParameters p = ExplicitProcessParameters.builder().
                launcherArgs(existingVMArgs).
                args(existingAppArgs).
                combine(extra).
                build();
        
        assertNull(p.getLauncherArguments());
        assertNull(p.getArguments());
    }
    
    public void testReplaceDiscardAndAddMorePriortyArgs() throws Exception {
        ExplicitProcessParameters extra = ExplicitProcessParameters.builder().
                launcherArg("-Dfoo=bar").
                replaceLauncherArgs(true).
                build();
        
        ExplicitProcessParameters extra2 = ExplicitProcessParameters.builder().
                launcherArg("-Xms=200m").
                build();
     
        ExplicitProcessParameters check1 = ExplicitProcessParameters.buildExplicitParameters(Arrays.asList(extra, extra2));
        
        assertFalse(check1.isArgReplacement());
        assertTrue(check1.isLauncherArgReplacement());
        assertEquals(2, check1.getLauncherArguments().size());
        assertNull(check1.getArguments());
        
        ExplicitProcessParameters base = ExplicitProcessParameters.builder().
                launcherArgs(existingVMArgs).
                args(existingAppArgs).
                build();
        
        ExplicitProcessParameters res;
        
        res = ExplicitProcessParameters.builder().
                combine(base).
                combine(check1).
                build();
        
        assertEquals(Arrays.asList("-Dfoo=bar", "-Xms=200m", "~", "File1"),
                res.getAllArguments("~"));
    }
    
    public void testDiscardAllEffects() throws Exception {
        ExplicitProcessParameters extra = ExplicitProcessParameters.builder().
                launcherArg("-Dfoo=bar").
                arg("avalanche").
                build();
        
        ExplicitProcessParameters discard = ExplicitProcessParameters.builder().
                replaceArgs(true).
                replaceLauncherArgs(true).
                build();
        
        ExplicitProcessParameters override = ExplicitProcessParameters.buildExplicitParameters(Arrays.asList(extra, discard));
        
        assertTrue(override.isEmpty());
        
        ExplicitProcessParameters result = ExplicitProcessParameters.builder().
                launcherArgs(existingVMArgs).
                args(existingAppArgs).
                combine(override).
                build();
        
        assertEquals(Arrays.asList("-Xmx100m"), result.getLauncherArguments());
        assertEquals(Arrays.asList("File1"), result.getArguments());
    }
    
    public void testRankOrdering() throws Exception {
        ExplicitProcessParameters p1 = ExplicitProcessParameters.builder().
                replaceArgs(false).
                args("P1").build();
        ExplicitProcessParameters p2 = ExplicitProcessParameters.builder().
                replaceArgs(false).
                position(-5).
                args("P2").build();
        ExplicitProcessParameters p3 = ExplicitProcessParameters.builder().
                replaceArgs(false).
                args("P3").build();
        ExplicitProcessParameters p4 = ExplicitProcessParameters.builder().
                replaceArgs(false).
                position(2).
                args("P4").build();
        
        ExplicitProcessParameters r = ExplicitProcessParameters.buildExplicitParameters(Arrays.asList(
                p4, p1, p2, p3
        ));
        assertEquals(Arrays.asList("P2", "P1", "P3", "P4"), r.getArguments());
    }
    
    //
    //================ samples =================
    public void testDiscardDefaultVMParametersAppendAppParameters() throws Exception {
        String[] projectVMArgs = new String[] { "-Dfile.encoding=UTF8" };
        String[] projectAppArgs = new String[] { "processFiles" };
        
        String[] files = new String[] { "fileA", "fileB" };
        
        // @start region="testDiscardDefaultVMParametersAppendAppParameters"
        ExplicitProcessParameters override = ExplicitProcessParameters.builder().
                // override the default: do not append to the base ones, but discard them
                replaceLauncherArgs(true).
                // ... and insist on empty launcher args
                launcherArgs().
                // ... or some other specific one(s)
                launcherArgs("-Xmx1000m").
                
                // override the default: keep base application arguments
                replaceArgs(false).
                
                // and add file list
                args(files).
                build();
        
        // @end region="testDiscardDefaultVMParametersAppendAppParameters"
        
        ExplicitProcessParameters result = ExplicitProcessParameters.builder().
                launcherArgs(projectVMArgs).
                args(projectAppArgs).
                combine(override).
                build();
        
        assertEquals(Arrays.asList("-Xmx1000m"), result.getLauncherArguments());
        assertEquals(Arrays.asList("processFiles", "fileA", "fileB"), result.getArguments());
    }

    private static String[] getProjectVMParams() {
        return null;
    }
    
    private static String[] getProjectAppParams() {
        return null;
    }
    
    public static void decorateWithExplicitParametersSample() {
        Lookup runContext = Lookup.getDefault();
        // @start region="decorateWithExplicitParametersSample"
        // collect all instructions from the Lookup and build decorating parameters
        ExplicitProcessParameters decorator = ExplicitProcessParameters.buildExplicitParameters(runContext);
        
        ExplicitProcessParameters params = ExplicitProcessParameters.builder().
                // include project's (or pre-configured) application parameters
                args(getProjectAppParams()).
                // include project's (or pre-configured) VM parameters as launcher ones
                launcherArgs(getProjectVMParams()).
                
                // now combine with the decorating instructions: will append or reset args or launcher args
                combine(decorator).
                build();
        
        // build a commandline for e.g. launcher or VM: include launcher args first
        // then the fixed "middle part" (e.g. the main class name), then application arguments.
        List<String> commandLine = params.getAllArguments("theMainClass");
        // @end region="decorateWithExplicitParametersSample"
    }
}
