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
package org.netbeans.modules.xml.catalog.impl;

import java.net.URL;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

public class XCatalogTest {

    @Test
    public void testXCatalogReportedValid() throws Exception {
        XCatalog catalog = new XCatalog();
        URL locationURL = getClass().getResource("xcatalog.xml");
        assertNotNull(locationURL);
        String location = locationURL.toExternalForm();
        catalog.setSource(location);
        catalog.refresh();
        assertTrue(catalog.isValid());
    }

    @Test
    public void testOasisCatalogNotAcceptedAsXCatalog() throws Exception {
        XCatalog catalog = new XCatalog();
        URL locationURL = getClass().getResource("sun/data/catalog.xml");
        assertNotNull(locationURL);
        String location = locationURL.toExternalForm();
        catalog.setSource(location);
        catalog.refresh();
        assertFalse(catalog.isValid());
    }
}
