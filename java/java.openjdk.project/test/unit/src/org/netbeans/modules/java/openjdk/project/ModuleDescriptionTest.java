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
package org.netbeans.modules.java.openjdk.project;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.netbeans.modules.java.openjdk.project.ModuleDescription.Dependency;

/**
 *
 * @author lahvac
 */
public class ModuleDescriptionTest {

    @Test
    public void testModuleInfoParsing() throws IOException {
        ModuleDescription d;

        d = ModuleDescription.parseModuleInfo(new StringReader("/* module wrong { requires wrong; } */ module right { requires right2; }"));

        assertEquals(d, new ModuleDescription("right", Arrays.asList(new Dependency("java.base", false, false), new Dependency("right2", false, false)), Collections.<String, List<String>>emptyMap()));

        d = ModuleDescription.parseModuleInfo(new StringReader("/* module wrong { requires wrong; } */ module right { requires public right2; }"));

        assertEquals(d, new ModuleDescription("right", Arrays.asList(new Dependency("java.base", false, false), new Dependency("right2", true, false)), Collections.<String, List<String>>emptyMap()));

        d = ModuleDescription.parseModuleInfo(new StringReader("/* module wrong { requires wrong; } */ module right { requires static right2; }"));

        assertEquals(d, new ModuleDescription("right", Arrays.asList(new Dependency("java.base", false, false), new Dependency("right2", false, true)), Collections.<String, List<String>>emptyMap()));

        d = ModuleDescription.parseModuleInfo(new StringReader("/* module wrong { requires wrong; } */ module right { requires public static right2; }"));

        assertEquals(d, new ModuleDescription("right", Arrays.asList(new Dependency("java.base", false, false), new Dependency("right2", true, true)), Collections.<String, List<String>>emptyMap()));

        d = ModuleDescription.parseModuleInfo(new StringReader("/* module wrong { requires wrong; } */ module right { requires java.base; }"));

        assertEquals(d, new ModuleDescription("right", Arrays.asList(new Dependency("java.base", false, false)), Collections.<String, List<String>>emptyMap()));

        d = ModuleDescription.parseModuleInfo(new StringReader("/* module wrong { requires wrong; } */ module right { requires right2 ; }"));

        assertEquals(d, new ModuleDescription("right", Arrays.asList(new Dependency("java.base", false, false), new Dependency("right2", false, false)), Collections.<String, List<String>>emptyMap()));

        d = ModuleDescription.parseModuleInfo(new StringReader("/* module wrong { requires wrong; } */ module\nright\n{\nrequires\nright2\n;\n}"));

        assertEquals(d, new ModuleDescription("right", Arrays.asList(new Dependency("java.base", false, false), new Dependency("right2", false, false)), Collections.<String, List<String>>emptyMap()));

        d = ModuleDescription.parseModuleInfo(new StringReader("/* module wrong { requires wrong; } */ module right { requires right.right; }"));

        assertEquals(d, new ModuleDescription("right", Arrays.asList(new Dependency("java.base", false, false), new Dependency("right.right", false, false)), Collections.<String, List<String>>emptyMap()));

        d = ModuleDescription.parseModuleInfo(new StringReader("/* module wrong { requires wrong; } */ module right { requires transitive right.right; }"));

        assertEquals(d, new ModuleDescription("right", Arrays.asList(new Dependency("java.base", false, false), new Dependency("right.right", true, false)), Collections.<String, List<String>>emptyMap()));

        d = ModuleDescription.parseModuleInfo(new StringReader("/* module wrong { requires wrong; } */ module right { requires transitive static right.right; }"));

        assertEquals(d, new ModuleDescription("right", Arrays.asList(new Dependency("java.base", false, false), new Dependency("right.right", true, true)), Collections.<String, List<String>>emptyMap()));

        d = ModuleDescription.parseModuleInfo(new StringReader("/* module wrong { requires wrong; } */ module right { requires transitive static right.right; exports test1; }"));

        assertEquals(d, new ModuleDescription("right", Arrays.asList(new Dependency("java.base", false, false), new Dependency("right.right", true, true)), Collections.<String, List<String>>singletonMap("test1", null)));

        d = ModuleDescription.parseModuleInfo(new StringReader("/* module wrong { requires wrong; } */ module right { requires transitive static right.right; exports test2 to m1, m2; }"));

        assertEquals(d, new ModuleDescription("right", Arrays.asList(new Dependency("java.base", false, false), new Dependency("right.right", true, true)), Collections.<String, List<String>>singletonMap("test2", Arrays.asList("m1", "m2"))));
    }

    private static void assertEquals(ModuleDescription d1, ModuleDescription d2) {
        Assert.assertEquals(d1.name, d2.name);

        Iterator<Dependency> d1Req = d1.depend.iterator();
        Iterator<Dependency> d2Req = d2.depend.iterator();

        while (d1Req.hasNext() && d2Req.hasNext()) {
            assertEquals(d1Req.next(), d2Req.next());
        }

        Assert.assertFalse(d1Req.hasNext());
        Assert.assertFalse(d2Req.hasNext());

        Assert.assertEquals(d1.exports, d2.exports);
    }

    private static void assertEquals(Dependency d1, Dependency d2) {
        Assert.assertEquals(d1.moduleName, d2.moduleName);
        Assert.assertEquals(d1.requiresPublic, d2.requiresPublic);
        Assert.assertEquals(d1.requiresStatic, d2.requiresStatic);
    }
}
