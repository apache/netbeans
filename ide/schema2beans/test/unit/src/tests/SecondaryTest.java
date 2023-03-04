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
package tests;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.netbeans.junit.*;

import org.netbeans.modules.schema2beansdev.*;

public class SecondaryTest extends NbTestCase {

    public SecondaryTest(java.lang.String testName) {
        super(testName);
    }

    public void testEntityParser() throws Exception {
        File schemaFile = new File(getDataDir(), "TestEntity.dtd");
        try (InputStream dtdIn = new FileInputStream(schemaFile);
                Reader reader = new InputStreamReader(dtdIn, StandardCharsets.ISO_8859_1);
                InputStream goldenStream = new FileInputStream(getGoldenFile());
                Reader goldenReader = new InputStreamReader(goldenStream, StandardCharsets.ISO_8859_1)) {
            EntityParser ep = new EntityParser();

            ep.parse(reader);

            Field entityMapField = EntityParser.class.getDeclaredField("entityMap");
            entityMapField.setAccessible(true);

            Map entityMap = (Map) entityMapField.get(ep);

            assertEquals(1, entityMap.size());

            StringBuilder testOutput = new StringBuilder();

            try (Reader r = ep.getReader()) {
                int read = 0;
                char[] buffer = new char[4096];
                while ( (read = r.read(buffer)) >= 0) {
                    testOutput.append(buffer, 0, read);
                }
            }

            StringBuilder reference = new StringBuilder();
            {
                int read = 0;
                char[] buffer = new char[4096];
                while ( (read = goldenReader.read(buffer)) >= 0) {
                    reference.append(buffer, 0, read);
                }
            }

            assertEquals(reference.toString(), testOutput.toString());
        }
    }
}
