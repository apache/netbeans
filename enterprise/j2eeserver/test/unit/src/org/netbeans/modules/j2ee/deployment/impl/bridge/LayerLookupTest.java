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

package org.netbeans.modules.j2ee.deployment.impl.bridge;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Petr Hejl
 */
public class LayerLookupTest extends NbTestCase {

    private static final String TEST_FOLDER = "LayerLookupTest"; // NOI18N

    public LayerLookupTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Lookup.getDefault().lookup(ModuleInfo.class);
    }

    public void testLookupConsulted() throws IOException {
        FileObject servers = FileUtil.createFolder(FileUtil.getConfigRoot(),
                TEST_FOLDER);
        FileObject testLookup = FileUtil.createData(servers, "TestLookup.instance"); // NOI18N

        Map<Class<?>, Object> lookups = new HashMap<Class<?>, Object>();
        lookups.put(String.class, "Test"); // NOI18N
        lookups.put(Integer.class, 0);
        lookups.put(Character.class, 'a'); // NOI18N
        lookups.put(Double.class, 0.0);

        TestLookup lookupInstance = new TestLookup(lookups);

        testLookup.setAttribute("instanceOf", Lookup.class.getName()); // NOI18N
        testLookup.setAttribute("instanceCreate", lookupInstance); // NOI18N

        Lookup lookup = Lookups.forPath(TEST_FOLDER);

        lookup(lookup, (String) lookups.get(String.class), String.class);
        lookup(lookup, (Integer) lookups.get(Integer.class), Integer.class);
        lookup(lookup, (Character) lookups.get(Character.class), Character.class);
        lookup(lookup, (Double) lookups.get(Double.class), Double.class);
        lookup(lookup, (String) lookups.get(String.class), String.class);
    }

    private <T> void lookup(Lookup lookup, T expected, Class<T> clazz) {
        assertEquals(expected, lookup.lookup(clazz));

        Collection<? extends T> instances = lookup.lookup(new Lookup.Template<T>(clazz)).allInstances();
        assertEquals(1, instances.size());
        assertEquals(expected, instances.iterator().next());
    }
}
