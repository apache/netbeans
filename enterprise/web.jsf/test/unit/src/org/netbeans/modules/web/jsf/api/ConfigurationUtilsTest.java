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

package org.netbeans.modules.web.jsf.api;

import junit.framework.TestCase;

/**
 *
 * @author Petr Pisl
 */
public class ConfigurationUtilsTest extends TestCase {
    
    public ConfigurationUtilsTest(String testName) {
        super(testName);
    }
    
    public void testTranslateURI() {
        assertEquals("/test.jsf", ConfigurationUtils.translateURI("*.jsf", "/test.jsp"));
        assertEquals("test.jsf", ConfigurationUtils.translateURI("*.jsf", "test.jsp"));
        assertEquals("faces/test.jsp", ConfigurationUtils.translateURI("/faces/*", "test.jsp"));
    }
}
