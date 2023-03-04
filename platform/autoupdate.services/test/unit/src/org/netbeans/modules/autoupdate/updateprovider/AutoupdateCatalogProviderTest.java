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
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.autoupdate.UpdateItem;


/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class AutoupdateCatalogProviderTest extends NbTestCase {
    
    public AutoupdateCatalogProviderTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDirPath());
    }

    public void testParseWrongXML() throws IOException {
        URL u = AutoupdateCatalogProviderTest.class.getResource("data/malformed.xml");
        AutoupdateCatalogProvider acp = new AutoupdateCatalogProvider("broken", "really broken", u);
        acp.refresh(true);
        
        try {
            Map<String, UpdateItem> res = acp.getUpdateItems();
            fail("Parsing shall yield I/O exception: " + res);
        } catch (IOException ex) {
            // OK
        }
    }
}
