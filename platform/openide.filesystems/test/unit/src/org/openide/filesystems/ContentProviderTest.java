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
package org.openide.filesystems;

import java.net.URL;
import java.util.Collection;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.Repository.LayerProvider;
import org.openide.util.Lookup;

public class ContentProviderTest extends NbTestCase {
    static {
        MockServices.setServices(MyProvider.class);
    }

    public ContentProviderTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Lookup.getDefault().lookup(MyProvider.class).reset();
    }

    public void testCheckAFileFromOurLayer() {
        FileObject fo = FileUtil.getConfigFile("foo/bar");
        assertNotNull("foo/bar is provided", fo);
        assertEquals("value is val", "val", fo.getAttribute("x"));
    }
    
    public void testRefreshTheProvider() throws Exception {
        MyProvider my = Lookup.getDefault().lookup(MyProvider.class);
        assertNotNull("My provider found", my);
        
        my.clear();
        
        FileObject fo = FileUtil.getConfigFile("foo/bar");
        assertNull("foo/bar is no longer available", fo);
        
    }
    
    public static final class MyProvider extends LayerProvider {
        private boolean empty;
        
        @Override
        protected void registerLayers(Collection<? super URL> context) {
            assertTrue("Context is empty: " + context, context.isEmpty());
            if (empty) {
                return;
            }
            context.add(ContentProviderTest.class.getResource("test-layer-attribs.xml"));
        }

        final void clear() throws Exception {
            empty = true;
            refresh();
        }
        
        final void reset() {
            empty = false;
            refresh();
        }
    }
}
