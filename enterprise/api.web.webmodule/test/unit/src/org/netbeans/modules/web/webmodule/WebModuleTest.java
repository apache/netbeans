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

package org.netbeans.modules.web.webmodule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleFactory;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Pavel Buzek, Andrei Badea
 */
public class WebModuleTest extends NbTestCase {

    private FileObject datadir;

    public WebModuleTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(WebModuleProviderImpl.class);
        datadir = FileUtil.toFileObject(getDataDir());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        MockServices.setServices();
    }

    public void testProviders() throws Exception {
        Collection<? extends WebModuleProvider> providers = Lookup.getDefault().lookupAll(WebModuleProvider.class);
        assertEquals("there should be 2 instances - one from web/webapi and one from tests", 2, providers.size());
    }

    public void testGetWebModule() throws Exception {
        FileObject foo = datadir.getFileObject("a.foo");
        FileObject bar = datadir.getFileObject("b.bar");
        WebModule wm1 = WebModule.getWebModule(foo);
        assertNotNull("found web module", wm1);
        WebModule wm2 = WebModule.getWebModule(bar);
        assertNull("no web module", wm2);
    }

    public static final class WebModuleProviderImpl implements WebModuleProvider {

        private final Map<FileObject, WebModule> cache = new HashMap<FileObject, WebModule>();

        public WebModuleProviderImpl() {}

        public WebModule findWebModule(FileObject file) {
            if (file.getExt().equals("foo")) {
                WebModule wm = cache.get(file.getParent());
                if (wm == null) {
                    wm = WebModuleFactory.createWebModule(new SimpleWebModuleImpl());
                    cache.put(file.getParent(), wm);
                }
                return wm;
            }
            return null;
        }
    }
}
