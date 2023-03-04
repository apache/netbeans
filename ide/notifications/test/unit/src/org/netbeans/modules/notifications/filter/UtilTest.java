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
package org.netbeans.modules.notifications.filter;

import org.junit.Test;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Hector Espert
 */
public class UtilTest extends NbTestCase {

    public UtilTest(String name) {
        super(name);
    }

    @Test
    public void testGetString() {
        assertEquals("No Filter", Util.getString("no-filter"));
    }

    @Test
    public void testGetChar() {
        assertEquals('N', Util.getChar("no-filter"));
    }

    @Test
    public void testGetMessage_String_Object() {
        assertEquals("No Filter", Util.getMessage("no-filter", "test"));
    }

    @Test
    public void testGetMessage_3args() {
        assertEquals("No Filter", Util.getMessage("no-filter", "test", "object"));
    }
    
}
