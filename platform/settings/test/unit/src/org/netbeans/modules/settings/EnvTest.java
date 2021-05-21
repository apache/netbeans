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

package org.netbeans.modules.settings;

import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 *
 * @author  Jan Pokorsky
 */
public class EnvTest extends NbTestCase {

    /** Creates a new instance of EnvTest */
    public EnvTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();

        Lookup.getDefault().lookup(ModuleInfo.class);
    }
    
    public void testFindEntityRegistration() throws Exception {
        String provider = "xml/lookups/NetBeans_org_netbeans_modules_settings_xtest/DTD_XML_FooSetting_1_0.instance";
        FileObject fo = FileUtil.getConfigFile(provider);
        assertNotNull("provider registration not found: " + provider, fo);
        assertNotNull("entity registration not found for " + provider, Env.findEntityRegistration(fo));
    }
    
    public void testFindProvider() throws Exception {
        Class<?> clazz = org.netbeans.modules.settings.convertors.FooSetting.class;
        FileObject fo = Env.findProvider(clazz);
        assertNotNull("xml/memory registration not found: " + clazz.getName(), fo);
    }
    
    public void testFindProviderFromSuperClass() throws Exception {
        Class<?> clazz = org.netbeans.modules.settings.convertors.Bar3Setting.class;
        FileObject fo = Env.findProvider(clazz);
        assertNotNull("xml/memory registration not found: " + clazz.getName(), fo);
    }
    
    public void testFindProviderFromSuperClass2() throws Exception {
        Class<?> clazz = org.netbeans.modules.settings.convertors.Bar4Setting.class;
        FileObject fo = Env.findProvider(clazz);
        assertNull("xml/memory registration found: " + clazz.getName(), fo);
    }
}
