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

package org.netbeans.modules.php.project;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.junit.NbTestCase;

public class SourceRootsTest extends NbTestCase {

    public SourceRootsTest(String name) {
        super(name);
    }

    public void testSourceRootsNames() {
        List<String> paths = Arrays.asList(
                makeOsDependentPath("/project1/test")
        );
        List<String> names = SourceRoots.getPureSourceRootsNames(paths);
        assertEquals(1, names.size());
        assertEquals("", names.get(0));

        paths = Arrays.asList(
                (String) null
        );
        names = SourceRoots.getPureSourceRootsNames(paths);
        assertEquals(1, names.size());
        assertEquals("", names.get(0));

        paths = Arrays.asList(
                (String) null,
                (String) null
        );
        names = SourceRoots.getPureSourceRootsNames(paths);
        assertEquals(2, names.size());
        assertEquals("", names.get(0));
        assertEquals("", names.get(1));

        paths = Arrays.asList(
                (String) null,
                (String) null,
                makeOsDependentPath("/project1/test")
        );
        names = SourceRoots.getPureSourceRootsNames(paths);
        assertEquals(3, names.size());
        assertEquals("", names.get(0));
        assertEquals("", names.get(1));
        assertEquals("test", names.get(2));

        paths = Arrays.asList(
                makeOsDependentPath("/project1/test1"),
                makeOsDependentPath("/project1/test2")
        );
        names = SourceRoots.getPureSourceRootsNames(paths);
        assertEquals(2, names.size());
        assertEquals("test1", names.get(0));
        assertEquals("test2", names.get(1));

        paths = Arrays.asList(
                makeOsDependentPath("/project1/bundle1/test"),
                makeOsDependentPath("/project1/bundle2/test")
        );
        names = SourceRoots.getPureSourceRootsNames(paths);
        assertEquals(names.toString(), 2, names.size());
        assertEquals("bundle1", names.get(0));
        assertEquals("bundle2", names.get(1));

        paths = Arrays.asList(
                makeOsDependentPath("/project1/bundle1/test"),
                makeOsDependentPath("/project1/bundle2/test"),
                makeOsDependentPath("/tmp/test")
        );
        names = SourceRoots.getPureSourceRootsNames(paths);
        assertEquals(3, names.size());
        assertEquals("bundle1", names.get(0));
        assertEquals("bundle2", names.get(1));
        assertEquals("tmp", names.get(2));

        paths = Arrays.asList(
                makeOsDependentPath("/project1/bundle1/test"),
                makeOsDependentPath("/project1/bundle2/test"),
                makeOsDependentPath("/project1/alltests")
        );
        names = SourceRoots.getPureSourceRootsNames(paths);
        assertEquals(3, names.size());
        assertEquals("bundle1", names.get(0));
        assertEquals("bundle2", names.get(1));
        assertEquals("alltests", names.get(2));
    }

    public void testEmptySourceRootsNames() {
        List<String> names = SourceRoots.getPureSourceRootsNames(Collections.<String>emptyList());
        assertTrue(names.isEmpty());
    }

    public void testInvalidSourceRootsNames() {
        List<String> paths = Arrays.asList(
                makeOsDependentPath("/project1/bundle1/test"),
                makeOsDependentPath("/project1/bundle1/test")
        );
        List<String> names = SourceRoots.getPureSourceRootsNames(paths);
        assertEquals(names.toString(), 2, names.size());
        assertEquals(makeOsDependentPath("/project1/bundle1/test"), names.get(0));
        assertEquals(makeOsDependentPath("/project1/bundle1/test"), names.get(1));

        paths = Arrays.asList(
                makeOsDependentPath("/project1/bundle1/test"),
                makeOsDependentPath("/project1/bundle1/test"),
                makeOsDependentPath("/tmp/test")
        );
        names = SourceRoots.getPureSourceRootsNames(paths);
        assertEquals(names.toString(), 3, names.size());
        assertEquals(makeOsDependentPath("/project1/bundle1/test"), names.get(0));
        assertEquals(makeOsDependentPath("/project1/bundle1/test"), names.get(1));
        assertEquals(makeOsDependentPath("/tmp/test"), names.get(2));
    }

    private String makeOsDependentPath(String path) {
        return path.replace('/', File.separatorChar);
    }

}
