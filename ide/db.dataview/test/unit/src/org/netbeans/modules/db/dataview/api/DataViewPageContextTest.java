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
package org.netbeans.modules.db.dataview.api;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author jhavlin
 */
public class DataViewPageContextTest extends NbTestCase {

    public DataViewPageContextTest(String name) {
        super(name);
    }

    public void testSetGetPageSize() {
        int origSize = DataViewPageContext.getStoredPageSize();
        assertTrue(origSize > -1);
        int newSize = origSize + 1;
        DataViewPageContext.setStoredPageSize(newSize);
        assertEquals(newSize, DataViewPageContext.getStoredPageSize());

        boolean exception = false;
        try {
            DataViewPageContext.setStoredPageSize(-1);
        } catch (IllegalArgumentException e) {
            exception = true;
        }
        assertTrue(exception);
    }
}
