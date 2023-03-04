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
package org.netbeans.modules.db.explorer.dlg;

import org.netbeans.modules.db.test.DDLTestBase;

public class AddViewDDLTest extends DDLTestBase {

    public AddViewDDLTest(String name) {
        super(name);
    }

    public void testAddView() throws Exception {
        String table1 = "table1";
        createBasicTable(table1, "id");
        
        String viewname = "testview";
        String query = "SELECT * FROM " + table1;
        boolean wasException = AddViewDDL.addView(getSpecification(), getSchema(), viewname, query);
        
        assertFalse(wasException);
        
        assertTrue(viewExists(viewname));
    }

}
