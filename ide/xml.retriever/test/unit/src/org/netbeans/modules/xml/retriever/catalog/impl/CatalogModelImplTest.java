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
package org.netbeans.modules.xml.retriever.catalog.impl;

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Assert;
import org.junit.Test;

public class CatalogModelImplTest {

    public CatalogModelImplTest() {
    }

    @Test
    public void testUriDecoding() throws URISyntaxException {
        URI[] inputUri = new URI[] {
            new URI("https://netbeans.apache.org/dummy?queryString=1"),
            new URI("https://netbeans.apache.org/dummy?queryString=1&fetch=false&sync=true"),
            new URI("file:///localfile"),
            new URI("file:///localfile?fetch=false&sync=true"),
            new URI("urn:demo:urn"),
            new URI("urn:demo:urn?fetch=false&sync=false"),
        };
        URI[] outputURI = new URI[] {
            new URI("https://netbeans.apache.org/dummy?queryString=1"),
            new URI("https://netbeans.apache.org/dummy?queryString=1"),
            new URI("file:///localfile"),
            new URI("file:///localfile"),
            new URI("urn:demo:urn"),
            new URI("urn:demo:urn"),
        };
        boolean[] doFetch = new boolean[]{
            true,
            false,
            true,
            false,
            true,
            false
        };
        boolean[] fetchSynchronous = new boolean[] {
            false,
            true,
            false,
            true,
            false,
            false
        };
        for (int i = 0; i < inputUri.length; i++) {
            CatalogModelImpl cmi = new CatalogModelImpl();
            URI extractedURI = cmi.extractRealURI(inputUri[i]);
            Assert.assertEquals("Entry " + i + " (URI)", outputURI[i], extractedURI);
            Assert.assertEquals("Entry " + i + " (doFetch)", doFetch[i], cmi.doFetch);
            Assert.assertEquals("Entry " + i + " (fetchSynchronous)", fetchSynchronous[i], cmi.fetchSynchronous);
        }
    }

}
