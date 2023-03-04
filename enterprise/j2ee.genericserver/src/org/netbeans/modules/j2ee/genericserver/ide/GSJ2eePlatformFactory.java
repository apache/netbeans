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

package org.netbeans.modules.j2ee.genericserver.ide;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/**
 *
 * @author Martin Adamek
 */
public class GSJ2eePlatformFactory extends J2eePlatformFactory {
    
    public J2eePlatformImpl getJ2eePlatformImpl(DeploymentManager dm) {
        return new J2eePlatformImplImpl();
    }
    
    private class J2eePlatformImplImpl extends J2eePlatformImpl {
        
        public boolean isToolSupported(String toolName) {
            return false;
        }
        
        public File[] getToolClasspathEntries(String toolName) {
            return new File[0];
        }
        
        public Set getSupportedSpecVersions() {
            Set<String> result = new HashSet<>();
            result.add(J2eeModule.J2EE_14);
            return result;
        }
        
        public Set getSupportedModuleTypes() {
            Set<Object> result = new HashSet<>();
//            result.add(J2eeModule.EAR);
//            result.add(J2eeModule.WAR);
            result.add(J2eeModule.EJB);
//            result.add(J2eeModule.CONN);
//            result.add(J2eeModule.CLIENT);
            return result;
        }
        
        public Set/*<String>*/ getSupportedJavaPlatformVersions() {
            Set<String> versions = new HashSet<>();
            versions.add("1.4"); // NOI18N
            versions.add("1.5"); // NOI18N
            versions.add("1.6"); // NOI18N
            return versions;
        }
        
        public JavaPlatform getJavaPlatform() {
            return null;
        }
        
        public java.io.File[] getPlatformRoots() {
            return new File[0];
        }
        
        public LibraryImplementation[] getLibraries() {
            return new LibraryImplementation[0];
        }
        
        public java.awt.Image getIcon() {
            return ImageUtilities.loadImage("org/netbeans/modules/j2ee/genericserver/resources/GSInstanceIcon.gif"); // NOI18N
        }
        
        public String getDisplayName() {
            return "Generic Server Platform";
        }
        
    }
    
}
