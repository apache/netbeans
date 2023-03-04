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

package org.netbeans.modules.autoupdate.updateprovider;

import java.io.IOException;
import java.net.URL;
import org.xml.sax.SAXException;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author jirka
 */
public class CatalogCacheTest extends NbTestCase {
    
    public CatalogCacheTest (String testName) {
        super (testName);
    }
    
    private URL URL_TO_TEST_CATALOG = null;
    private AutoupdateCatalogCache cache = null;
        
    @Override
    protected void setUp () throws Exception {
        clearWorkDir ();
        super.setUp ();
        
        URL_TO_TEST_CATALOG = this.getClass ().getResource ("data/catalog.xml");
        System.setProperty ("netbeans.user", getWorkDirPath ());
        cache = AutoupdateCatalogCache.getDefault ();
    }
    
    public void testWriteToCache () throws IOException {
        URL catalogInCache = cache.writeCatalogToCache ("test-catalog", URL_TO_TEST_CATALOG);
        assertNotNull ("Cache exists!", catalogInCache);
    }
    
    public void testCompareOriginalAndCache () throws IOException, SAXException {
        assertEquals ("Number of items is same in both places.",
                AutoupdateCatalogParser.getUpdateItems(URL_TO_TEST_CATALOG, null).size (),
                AutoupdateCatalogParser.getUpdateItems (cache.writeCatalogToCache("test-catalog", URL_TO_TEST_CATALOG), null).size ());
    }
    
    public void testGetCatalogURL () throws IOException {
        URL stored1 = cache.writeCatalogToCache ("test-1-catalog", URL_TO_TEST_CATALOG);
        URL stored2 = cache.writeCatalogToCache ("test-2-catalog", URL_TO_TEST_CATALOG);
        assertNotNull (stored1);
        assertNotNull (stored2);
        assertEquals ("Get catalog URL as same as stored", stored1, cache.getCatalogURL ("test-1-catalog"));
        assertEquals ("Get catalog URL as same as stored", stored2, cache.getCatalogURL ("test-2-catalog"));
        assertFalse ("Stored URLs of two cache cannot be same.", stored2.equals(stored1));
        assertFalse ("Stored URLs of two cache cannot be same.", cache.getCatalogURL ("test-2-catalog").equals(cache.getCatalogURL ("test-1-catalog")));
    }
    
}
