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
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.explorer.test.api.SQLIdentifiersTestUtilities;
import org.netbeans.modules.db.metadata.model.api.Metadata;

/** Base class for completion test cases.
 *
 * @author Jiri Skrivanek
 */
public class CompletionQueryTestCase extends NbTestCase {

    protected static final String[] model = {
        "catalog_1*",
        "  sch_customers*",
        "    tab_customer",
        "      col_customer_id",
        "  sch_accounting",
        "    tab_invoice",
        "      col_invoice_id",
        "    tab_purchase_order",
        "      col_order_id",
        "catalog_2"
    };
    private static List<String> modelData = Arrays.asList(model);
    private static Metadata metadata = TestMetadata.create(modelData);

    public CompletionQueryTestCase(String testName) {
        super(testName);
    }

    static void assertItems(SQLCompletionItems items, String... expected) {
        int index = 0;
        for (SQLCompletionItem item : items) {
            if (expected == null || index == expected.length) {
                // more items than expected
                index++;
                break;
            }
            assertTrue("Expected<" + expected[index] + "> not found in <" + item + ">", item.toString().contains(expected[index]));
            index++;
        }
        assertEquals("Wrong number of items returned.", expected.length, index);
    }

    static SQLCompletionItems doQuery(String sql) {
        return doQuery(sql, metadata);
    }

    static SQLCompletionItems doQuery(String sql, Metadata metadata) {
        int caretOffset = sql.indexOf('|');
        if (caretOffset >= 0) {
            sql = sql.replace("|", "");
        } else {
            throw new IllegalArgumentException("Missing | in SQL staement.");
        }
        SQLCompletionQuery query = new SQLCompletionQuery(null);
        SQLCompletionEnv env = SQLCompletionEnv.forScript(sql, caretOffset);
        return query.doQuery(env, metadata, SQLIdentifiersTestUtilities.createNonASCIIQuoter("\""));
    }
}
