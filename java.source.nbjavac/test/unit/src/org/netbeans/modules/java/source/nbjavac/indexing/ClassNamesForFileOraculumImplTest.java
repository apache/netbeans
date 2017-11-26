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

package org.netbeans.modules.java.source.nbjavac.indexing;

import com.sun.tools.javac.api.ClassNamesForFileOraculum;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author lahvac
 */
public class ClassNamesForFileOraculumImplTest {

    public ClassNamesForFileOraculumImplTest() {
    }

    @Test
    public void testDivineSources() {
        TestJavaFileObject fo1 = new TestJavaFileObject();
        TestJavaFileObject fo2 = new TestJavaFileObject();
        Map<JavaFileObject, List<String>> fo2FQNs = new HashMap<JavaFileObject, List<String>>();

        fo2FQNs.put(fo1, Arrays.asList("a.b.c.Class1"));
        fo2FQNs.put(fo2, Arrays.asList("e.f.g"));

        ClassNamesForFileOraculum oraculum = new ClassNamesForFileOraculumImpl(fo2FQNs);

        assertArrayEquals(new JavaFileObject[] {fo1}, oraculum.divineSources("a.b.c"));
        assertNull(oraculum.divineSources("a.b"));
        assertNull(oraculum.divineSources("e.f.g"));
    }

    private static final class TestJavaFileObject extends SimpleJavaFileObject {

        public TestJavaFileObject() {
            super(URI.create("test://test.java"), Kind.SOURCE);
        }

    }

}