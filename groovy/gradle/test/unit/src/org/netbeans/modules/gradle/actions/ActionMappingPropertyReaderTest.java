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
package org.netbeans.modules.gradle.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.gradle.api.execute.ActionMapping;

/**
 *
 * @author lkishalmi
 */
public class ActionMappingPropertyReaderTest {

    /**
     * Test of loadMappings method, of class ActionMappingPropertyReader.
     */
    @Test
    public void testLoadMappings1() {
        Properties props = new Properties();
        Set<ActionMapping> result = ActionMappingPropertyReader.loadMappings(props);
        assertEquals(result.size(), 0);
    }

    @Test
    public void testLoadMappings2() {
        Properties props = new Properties();
        props.put("action.run.args", "runArgs");
        Set<ActionMapping> result = ActionMappingPropertyReader.loadMappings(props);
        assertEquals(result.size(), 1);
        ActionMapping mapping = result.iterator().next();
        assertEquals("run", mapping.getName());
        assertEquals("runArgs", mapping.getArgs());
        assertTrue(mapping.isRepeatable());
        assertEquals(ActionMapping.ReloadRule.DEFAULT, mapping.getReloadRule());
        assertTrue(mapping.getReloadArgs().isEmpty());
    }

    @Test
    public void testLoadMappings3() {
        Properties props = new Properties();
        props.put("action.custom-1", "Build with Arguments");
        props.put("action.custom-1.args", "runArgs ${test}");
        props.put("action.custom-1.reload.args", "runArgs");
        props.put("action.custom-1.reload.rule", "NEVER");
        props.put("action.custom-1.repeatable", "false");
        Set<ActionMapping> result = ActionMappingPropertyReader.loadMappings(props);
        assertEquals(result.size(), 1);
        ActionMapping mapping = result.iterator().next();
        assertEquals("Build with Arguments", mapping.getDisplayName());
        assertEquals("custom-1", mapping.getName());
        assertEquals("runArgs ${test}", mapping.getArgs());
        assertFalse(mapping.isRepeatable());
        assertEquals(ActionMapping.ReloadRule.NEVER, mapping.getReloadRule());
        assertEquals("runArgs", mapping.getReloadArgs());
    }

    @Test
    public void testLoadMappings4() {
        Properties props = new Properties();
        props.put("action.build.args", "build");
        props.put("action.build.priority", "100");
        props.put("action.build.plugins", "groovy, war");
        Set<ActionMapping> result = ActionMappingPropertyReader.loadMappings(props);
        assertEquals(result.size(), 1);
        DefaultActionMapping mapping = (DefaultActionMapping) result.iterator().next();
        assertEquals("build", mapping.getName());
        assertEquals(100, mapping.priority);
        assertTrue(mapping.isApplicable(new HashSet<String>(Arrays.asList("groovy", "root", "war"))));
        assertFalse(mapping.isApplicable(new HashSet<String>(Arrays.asList("groovy"))));
    }

    @Test
    public void testLoadMappings5() {
        Properties props = new Properties();
        props.put("action.test.single.args", "cleanTest test --tests ${selectedClass}");
        Set<ActionMapping> result = ActionMappingPropertyReader.loadMappings(props);
        assertEquals(result.size(), 1);
        DefaultActionMapping mapping = (DefaultActionMapping) result.iterator().next();
        assertEquals("test.single", mapping.getName());
    }

    @Test
    public void testLoadMappings6() {
        Properties props = new Properties();
        props.put("action.download.javadoc.reload.args", "-PdownloadJavadoc={0}");
        Set<ActionMapping> result = ActionMappingPropertyReader.loadMappings(props);
        assertEquals(result.size(), 1);
        DefaultActionMapping mapping = (DefaultActionMapping) result.iterator().next();
        assertEquals("download.javadoc", mapping.getName());
    }

    @Test
    public void testLoadMappings7() {
        Properties props = new Properties();
        props.put("action.download.javadoc_args", "-PdownloadJavadoc={0}");
        Set<ActionMapping> result = ActionMappingPropertyReader.loadMappings(props);
        assertEquals(result.size(), 0);
    }
}
