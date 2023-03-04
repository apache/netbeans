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

package org.netbeans.modules.db.sql.editor.completion;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.test.api.MetadataTestBase;

/**
 *
 * @author Andrei Badea
 */
public class TestMetadataTest extends MetadataTestBase {

    public TestMetadataTest(String name) {
        super(name);
    }

    public void testSimple() throws Exception {
        TestMetadata metadata = new TestMetadata(new String[] {
                "catalog0*",
                "  schema1*",
                "    table2",
                "      col3",
                "      col4",
                "  schema5",
                "    table6",
                "      col7",
                "      col8",
                "    view3[view]",
                "      col33",
                "      col34",
                "      col35",
                "catalog9"
        });
        assertNames(new HashSet<String>(Arrays.asList("catalog0", "catalog9")), metadata.getCatalogs());
        Catalog defaultCatalog = metadata.getDefaultCatalog();
        assertEquals("catalog0", defaultCatalog.getName());
        assertSame(defaultCatalog, metadata.getCatalog("catalog0"));
        assertNotNull(metadata.getCatalog("catalog9"));
        assertNames(new HashSet<String>(Arrays.asList("schema1", "schema5")), defaultCatalog.getSchemas());
        Schema defaultSchema = metadata.getDefaultSchema();
        assertEquals("schema1", defaultSchema.getName());
        assertNames(Collections.singleton("table2"), defaultSchema.getTables());
        assertNames(Arrays.asList("col7", "col8"), defaultCatalog.getSchema("schema5").getTable("table6").getColumns());
        assertNames(Collections.singleton("view3"), defaultCatalog.getSchema("schema5").getViews());
        assertNames(Arrays.asList("col33", "col34", "col35"), defaultCatalog.getSchema("schema5").getView("view3").getColumns());
    }

    public void testEnsureDefaultCatalog() {
        try {
            new TestMetadata(new String[] {
                    "catalog0"
            });
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testNullCatalog() {
        TestMetadata metadata = new TestMetadata(new String[] {
                "<unknown>",
                "another"
        });
        assertNames(new HashSet<String>(Arrays.asList(null, "another")), metadata.getCatalogs());
    }

    public void testNoSchema() throws Exception {
        TestMetadata metadata = new TestMetadata(new String[] {
                "<unknown>",
                "  <no-schema>",
                "    table1",
                "    table2"
        });
        Catalog defaultCatalog = metadata.getDefaultCatalog();
        Schema defaultSchema = metadata.getDefaultSchema();
        assertTrue(defaultSchema.isSynthetic());
        assertTrue(defaultSchema.isDefault());
        assertSame(defaultSchema, defaultCatalog.getSyntheticSchema());
        assertEquals(0, defaultCatalog.getSchemas().size());
        assertNames(Arrays.asList("table1", "table2"), defaultSchema.getTables());
    }
}
