/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
