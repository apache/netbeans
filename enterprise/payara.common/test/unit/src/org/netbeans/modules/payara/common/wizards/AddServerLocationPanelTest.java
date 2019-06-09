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

package org.netbeans.modules.payara.common.wizards;

import java.io.File;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

public class AddServerLocationPanelTest {

    public AddServerLocationPanelTest() {
    }


    /**
     * Test of canCreate method, of class AddServerLocationPanel.
     */
    @Test
    public void testCanCreate() throws IOException {
        System.out.println("canCreate");  // NOI18N
        File dir = File.createTempFile("foo", "bar");  // NOI18N
        boolean expResult = false;
        boolean result = AddServerLocationPanel.canCreate(dir);
        assertEquals(expResult, result);
        dir.delete();
        expResult = true;
        result = AddServerLocationPanel.canCreate(dir);
        assertEquals(expResult, result);
    }

}
