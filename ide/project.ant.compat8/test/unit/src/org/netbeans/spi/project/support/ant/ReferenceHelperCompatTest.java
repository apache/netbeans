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
package org.netbeans.spi.project.support.ant;

import java.lang.reflect.Method;
import java.net.URL;
import junit.framework.Test;
import junit.framework.TestCase;
import org.netbeans.api.project.libraries.LibraryChooser;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;

/**
 *
 */
public class ReferenceHelperCompatTest extends NbTestCase {
    
    public ReferenceHelperCompatTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return NbModuleSuite.createConfiguration(ReferenceHelperCompatTest.class).
            gui(false).suite();
    }

    public void testLibraryChooserImportHandler() throws Exception {
        final Class<?> ret = LibraryChooser.LibraryImportHandler.class;
        ClassLoader c1 = ReferenceHelper.class.getClassLoader();
        ClassLoader c2 = ReferenceHelper.class.getSuperclass().getClassLoader();
        
        Class<?> snd = c2.loadClass(ret.getName());
        assertEquals(ret, snd);
        
        Method m = ReferenceHelper.class.getSuperclass().getMethod("getLibraryChooserImportHandler");
        assertEquals(ret, m.getReturnType());
    }
    public void testLibraryChooserImportHandlerWithURL() throws Exception {
        final Class<?> ret = LibraryChooser.LibraryImportHandler.class;
        ClassLoader c1 = ReferenceHelper.class.getClassLoader();
        ClassLoader c2 = ReferenceHelper.class.getSuperclass().getClassLoader();
        
        Class<?> snd = c2.loadClass(ret.getName());
        assertEquals(ret, snd);
        
        Method m = ReferenceHelper.class.getSuperclass().getMethod("getLibraryChooserImportHandler", URL.class);
        assertEquals(ret, m.getReturnType());
    }
}
