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
package org.netbeans.core.startup;

import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

public class MainLookupTest extends NbTestCase {
    public MainLookupTest(String s) {
        super(s);
    }

    public void testInstanceInServicesFolderIsVisible() throws IOException {
        FileObject inst = FileUtil.createData(FileUtil.getConfigRoot(), "Services/Test/X.instance");
        inst.setAttribute("instanceCreate", Integer.valueOf(33));
        assertTrue("Is main lookup", MainLookup.getDefault() instanceof MainLookup);
        Lookup.getDefault().lookup(ModuleInfo.class);
        assertEquals("33 found", Integer.valueOf(33), Lookup.getDefault().lookup(Integer.class));
    }
}
