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

package org.netbeans.modules.db.explorer;

import junit.framework.TestCase;

/**
 *
 * @author Andrei Badea
 */
public class DbUtilitiesTest extends TestCase {

    public DbUtilitiesTest(String testName) {
        super(testName);
    }

    public void testFormatError() {
        assertEquals("Error. The exception which caused this error has been logged to the message log.", DbUtilities.formatError("Error.", null));
        assertEquals("Error. Reason.", DbUtilities.formatError("Error.", "reason"));
        assertEquals("Error. Reason.", DbUtilities.formatError("Error.", "Reason."));
        assertEquals("Error. Reason.", DbUtilities.formatError("Error.", "Reason!"));
    }
}
