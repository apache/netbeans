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
package org.netbeans.modules.maven.j2ee.utils;

import java.io.IOException;
import org.netbeans.modules.maven.j2ee.JavaEEMavenTestBase;

/**
 * 
 * @author Martin Janicek
 */
public class IsWebSupportedTest extends JavaEEMavenTestBase {
    
    public IsWebSupportedTest(String name) {
        super(name);
    }
    
    public void testIsWebSupported_packagingWar() {
        assertEquals(true, MavenProjectSupport.isWebSupported(project, "war")); //NOI18N
    }
    
    public void testIsWebSupported_packagingBundle_existingWebAppDir() {
        assertEquals(true, MavenProjectSupport.isWebSupported(project, "bundle")); //NOI18N
    }
    
    public void testIsWebSupported_packagingBundle_notExistingWebAppDir() throws IOException {
        project.getProjectDirectory().getFileObject("src/main/webapp").delete(); //NOI18N
            
        assertEquals(false, MavenProjectSupport.isWebSupported(project, "bundle")); //NOI18N
    }
    
    public void testIsWebSupported_packagingAppClient() {
        assertEquals(false, MavenProjectSupport.isWebSupported(project, "app-client")); //NOI18N
    }
    
    public void testIsWebSupported_packagingEjb() {
        assertEquals(false, MavenProjectSupport.isWebSupported(project, "ejb")); //NOI18N
    }
    
    public void testIsWebSupported_packagingEar() {
        assertEquals(false, MavenProjectSupport.isWebSupported(project, "ear")); //NOI18N
    }
}
