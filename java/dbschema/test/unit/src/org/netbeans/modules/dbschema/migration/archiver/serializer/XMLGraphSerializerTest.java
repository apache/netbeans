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

package org.netbeans.modules.dbschema.migration.archiver.serializer;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Andrei Badea
 */
public class XMLGraphSerializerTest extends NbTestCase {

    public XMLGraphSerializerTest(String name) {
        super(name);
    }

    public void testIssue80307() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new XMLGraphSerializer(outputStream).writeObject(new FooList());

        String string = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(string.contains("XMLGraphSerializerTest$Foo42"));
        assertTrue(string.contains("XMLGraphSerializerTest$Foo42#1"));
    }

    private static final class Foo {

        @Override
        public int hashCode() {
            return 42;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Foo;
        }
    }

    private static final class FooList {

        private final Foo foo1 = new Foo();
        private final Foo foo2 = new Foo();
    }
}
