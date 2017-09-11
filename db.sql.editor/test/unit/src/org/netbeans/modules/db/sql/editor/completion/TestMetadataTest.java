/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
