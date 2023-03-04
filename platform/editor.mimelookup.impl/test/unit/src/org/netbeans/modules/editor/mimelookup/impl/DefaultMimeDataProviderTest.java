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

package org.netbeans.modules.editor.mimelookup.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.openide.util.Lookup;

/**
 *
 * @author vita
 */
public class DefaultMimeDataProviderTest extends NbTestCase {

    /** Creates a new instance of DefaultMimeDataProviderTest */
    public DefaultMimeDataProviderTest(String name) {
        super(name);
    }

    public void testProviderRegistration() {
        Collection providers = Lookup.getDefault().lookupAll(MimeDataProvider.class);
        assertTrue("No providers registered", providers.size() > 0);
        
        ArrayList defaultProviders = new ArrayList();
        for (Iterator i = providers.iterator(); i.hasNext(); ) {
            MimeDataProvider provider = (MimeDataProvider) i.next();
            if (provider instanceof DefaultMimeDataProvider) {
                defaultProviders.add(provider);
            }
        }
        
        assertTrue("No default provider registered", defaultProviders.size() > 0);
        if (defaultProviders.size() > 1) {
            String msg = "Too many default providers registered:\n";
            
            for (Iterator i = defaultProviders.iterator(); i.hasNext();) {
                DefaultMimeDataProvider provider = (DefaultMimeDataProvider) i.next();
                msg += provider + "\n";
            }
            
            fail(msg);
        }
    }
}
