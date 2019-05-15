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
        assertEquals(mapping.getName(), "run");
        assertEquals(mapping.getArgs(), "runArgs");
        assertTrue(mapping.isRepeatable());
        assertEquals(mapping.getReloadRule(), ActionMapping.ReloadRule.DEFAULT);
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
        assertEquals(mapping.getDisplayName(), "Build with Arguments");
        assertEquals(mapping.getName(), "custom-1");
        assertEquals(mapping.getArgs(), "runArgs ${test}");
        assertFalse(mapping.isRepeatable());
        assertEquals(mapping.getReloadRule(), ActionMapping.ReloadRule.NEVER);
        assertEquals(mapping.getReloadArgs(), "runArgs");
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
        assertEquals(mapping.getName(), "build");
        assertEquals(mapping.priority, 100);
        assertTrue(mapping.isApplicable(new HashSet<String>(Arrays.asList("groovy", "root", "war"))));
        assertFalse(mapping.isApplicable(new HashSet<String>(Arrays.asList("groovy"))));
    }

}
