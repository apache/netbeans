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
package org.netbeans.core.startup.layers;

import java.net.URL;
import java.util.Collection;
import junit.framework.Test;
import org.netbeans.core.startup.MainLookup;
import org.netbeans.core.startup.NbRepository;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.filesystems.Repository.LayerProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ContentProviderTest extends NbTestCase {
    public ContentProviderTest(String name) {
        super(name);
    }
    
    public static Test suite() {
        return NbModuleSuite.createConfiguration(ContentProviderTest.class)
            .gui(false).addTest(
                "testBeforeRest", 
                "testUsingNbRepositoryAndInit",
                "testCheckAFileFromOurLayer",
                "testReturnEmptyLayers"
            ).suite();
    }
    
    public void testBeforeRest() {
        FileObject fo = FileUtil.getConfigFile("foo/bar");
        assertNull("no foo/bar provided", fo);
    }
    
    public void testUsingNbRepositoryAndInit() {
        if (Repository.getDefault() instanceof NbRepository) {
            MainLookup.register(new MyProvider());
            return;
        }
        fail("Wrong repository: " + Repository.getDefault());
    }
    
    public void testCheckAFileFromOurLayer() {
        FileObject fo = FileUtil.getConfigFile("foo/bar");
        assertNotNull("foo/bar is provided", fo);
        assertEquals("value is val", "val", fo.getAttribute("x"));
    }
    
    public void testReturnEmptyLayers() throws Exception {
        MyProvider my = Lookup.getDefault().lookup(MyProvider.class);
        my.makeEmpty();
        FileObject fo = FileUtil.getConfigFile("foo/bar");
        assertNull("no foo/bar is provided anymore", fo);
        
    }

    public static final class MyProvider extends LayerProvider {
        private boolean empty;
        
        final void makeEmpty() {
            empty = true;
            refresh();
        }
        
        @Override
        protected void registerLayers(Collection<? super URL> context) {
            assertTrue("Context is empty: " + context, context.isEmpty());
            if (empty) {
                return;
            }
            context.add(ContentProviderTest.class.getResource("ContentProviderTest.xml"));
            assertFalse("No nulls", context.contains(null));
        }
    }
    
}
