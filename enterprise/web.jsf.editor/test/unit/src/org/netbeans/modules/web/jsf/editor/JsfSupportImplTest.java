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
package org.netbeans.modules.web.jsf.editor;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.web.jsfapi.spi.JsfSupportProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author marekfukala
 */
public class JsfSupportImplTest extends TestBaseForTestProject {

    public JsfSupportImplTest(String testName) {
        super(testName);
    }

    public static Test xsuite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new JsfSupportImplTest("testELErrorReporting"));
        return suite;
    }

    public void testJsfSupportProviderInGlobalLookup() {
        JsfSupportProvider instance = Lookup.getDefault().lookup(JsfSupportProvider.class);
        assertNotNull(instance);
        assertTrue(instance instanceof JsfSupportProviderImpl);
    }

    public void testGetJsfSupportInstance() throws Exception {
        FileObject file = getWorkFile("testWebProject/web/index.xhtml");
        assertNotNull(file);
        JsfSupportImpl instance = JsfSupportImpl.findFor(file);
        assertNotNull(instance);
    }

    public void testIsTheJsfSupportInstanceCached() throws Exception{
        FileObject file = getWorkFile("testWebProject/web/index.xhtml");
        assertNotNull(file);
        JsfSupportImpl instance1 = JsfSupportImpl.findFor(file);
        assertNotNull(instance1);
        JsfSupportImpl instance2 = JsfSupportImpl.findFor(file);
        assertNotNull(instance2);

        assertSame(instance1, instance2);

    }

}
