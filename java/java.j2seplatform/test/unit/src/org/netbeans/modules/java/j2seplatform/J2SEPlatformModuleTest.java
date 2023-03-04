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

package org.netbeans.modules.java.j2seplatform;

import org.netbeans.api.project.TestUtil;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seplatform.platformdefinition.JavaPlatformProviderImpl;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 * Test that module restore works as expected.
 *
 * @author David Konecny
 */
public class J2SEPlatformModuleTest extends NbTestCase {
    
    static {
        Class c = TestUtil.class; // force lookup init before anyone else
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public J2SEPlatformModuleTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        this.clearWorkDir();
        System.setProperty("netbeans.user", FileUtil.normalizeFile(getWorkDir()).getPath());
        MockLookup.setInstances(new JavaPlatformProviderImpl());
    }
    
    public void testRestored() throws Exception {
        UpdateTask.getDefault().run();
        EditableProperties ep = PropertyUtils.getGlobalProperties();
        JavaPlatform platform = JavaPlatformManager.getDefault().getDefaultPlatform();
        String ver = platform.getSpecification().getVersion().toString();
        assertEquals("Default source level must be set up", ver, ep.getProperty("default.javac.source"));
        assertEquals("Default source level must be set up", ver, ep.getProperty("default.javac.target"));
    }
    
}
