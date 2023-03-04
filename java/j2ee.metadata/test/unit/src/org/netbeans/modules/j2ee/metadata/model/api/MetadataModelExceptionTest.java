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

package org.netbeans.modules.j2ee.metadata.model.api;

import junit.framework.TestCase;
import org.openide.util.Exceptions;

/**
 *
 * @author Andrei Badea
 */
public class MetadataModelExceptionTest extends TestCase {

    public MetadataModelExceptionTest(String testName) {
        super(testName);
    }

    public void testCause() throws Exception {
        MetadataModelException mme = new MetadataModelException(new RuntimeException("foo"));
        assertTrue(mme.getMessage().contains("foo"));
        assertFalse(mme.getMessage().contains("RuntimeException"));
        assertTrue(mme.getCause().getClass().equals(RuntimeException.class));
    }
}
