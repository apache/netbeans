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

package org.netbeans.modules.web.debug;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class EngineContextProviderImplTest extends NbTestCase {

    public EngineContextProviderImplTest(String name) {
        super(name);
    }

    public void testGetUrl() throws MalformedURLException, URISyntaxException {
        EngineContextProviderImpl provider = new EngineContextProviderImpl(null);
        assertNull(provider.getURL(null, false));
        assertNull(provider.getURL(null, true));

        assertNull(provider.getURL("SomeClass.java", false));
        assertNull(provider.getURL("SomeClass.java", true));

        assertUrl(provider.getURL("org", false));
        assertNull(provider.getURL("org/SomeClass.java", false));
        assertUrl(provider.getURL("org/apache", false));
        assertNull(provider.getURL("org/apache/SomeClass.java", false));
        assertUrl(provider.getURL("org/apache/jsp", false));
        assertNull(provider.getURL("org/apache/jsp/SomeClass.java", false));
    }
    
    private static void assertUrl(String urlString) throws MalformedURLException, URISyntaxException {
        assertNotNull(urlString);
        URL url = new URL(urlString);
        if ("file".equals(url.getProtocol())) {
            File file = new File(url.toURI());
            assertFalse(file.exists());
        }
    }
}
