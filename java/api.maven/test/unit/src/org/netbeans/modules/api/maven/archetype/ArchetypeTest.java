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

package org.netbeans.modules.api.maven.archetype;

import org.netbeans.api.maven.archetype.Archetype;
import org.netbeans.junit.NbTestCase;

public class ArchetypeTest extends NbTestCase {

    public ArchetypeTest(String n) {
        super(n);
    }

    public void testArchetype() {
        Archetype a = createArchetype("aid", "gid", "1.0");
        
        assertEquals("aid", a.getArtifactId());
        assertEquals("gid", a.getGroupId());
        assertEquals("1.0", a.getVersion());
        assertEquals("aid", a.getName());
        assertNull(a.getDescription());
        assertNull(a.getRepository());
        
        Archetype a2 = createArchetype("aid", "gid", "1.0");
        assertEquals(a, a2);
        assertEquals(a.hashCode(), a2.hashCode());
        
        a = createArchetype("aid", "gid", "1.0", "test", "test desc", "test repository");
        
        assertEquals("aid", a.getArtifactId());
        assertEquals("gid", a.getGroupId());
        assertEquals("1.0", a.getVersion());
        assertEquals("test", a.getName());
        assertEquals("test desc", a.getDescription());
        assertEquals("test repository", a.getRepository());

        a2 = createArchetype("aid", "gid", "1.0", "test", "test desc", "test repository");
        assertEquals(a, a2);
        assertEquals(a.hashCode(), a2.hashCode());
        
    }
    
    public void testGetName() {
        Archetype a = createArchetype("aid", "gid", "1.0");
        assertEquals("aid", a.getName());
        
        a = createArchetype("aid", "gid", "1.0", null, null, null);
        assertEquals("aid", a.getName());
        
        a = createArchetype("aid", "gid", "1.0", "${project.artifactId}", null, null);
        assertEquals("aid", a.getName());

        a = createArchetype("aid", "gid", "1.0", "test name", null, null);
        assertEquals("test name", a.getName());
    }

    private Archetype createArchetype(String aid, String gid, String version) {
        Archetype a = new Archetype();
        a.setArtifactId(aid);
        a.setGroupId(gid);
        a.setVersion(version);
        return a;
    }

    private Archetype createArchetype(String aid, String gid, String version, String name, String desc, String repo) {
        Archetype a = new Archetype();
        a.setArtifactId(aid);
        a.setGroupId(gid);
        a.setVersion(version);
        a.setName(name);
        a.setDescription(desc);
        a.setRepository(repo);
        return a;
    }
    
}
