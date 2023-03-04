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

package org.netbeans.tests.j2eeserver.plugin;

import java.awt.Image;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule.Type;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Petr Hejl
 */
public class TestPlatform extends J2eePlatformImpl {

    private final File platformRoot;

    public TestPlatform(File platformRoot) {
        this.platformRoot = platformRoot;
    }

    @Override
    public String getDisplayName() {
        return "Test platform";
    }

    @Override
    public Image getIcon() {
        return ImageUtilities.loadImage("org/netbeans/tests/j2eeserver/plugin/registry/plugin.gif");
    }

    @Override
    public JavaPlatform getJavaPlatform() {
        return JavaPlatformManager.getDefault().getDefaultPlatform();
    }

    @Override
    public LibraryImplementation[] getLibraries() {
        return new LibraryImplementation[]{};
    }

    @Override
    public File[] getPlatformRoots() {
        return new File[] {platformRoot};
    }

    @Override
    public Set getSupportedJavaPlatformVersions() {
        Set versions = new HashSet();
        versions.add("1.4"); // NOI18N
        versions.add("1.5"); // NOI18N
        return versions;
    }

    @Override
    public Set<Type> getSupportedTypes() {
        Set<Type> types = new HashSet<Type>();
        types.add(Type.WAR);
        types.add(Type.EJB);
        types.add(Type.EAR);
        types.add(Type.CAR);
        types.add(Type.RAR);
        return types;
    }

    @Override
    public Set<Profile> getSupportedProfiles() {
        Set<Profile> profiles = new HashSet<Profile>();
        profiles.add(Profile.J2EE_13);
        profiles.add(Profile.J2EE_14);
        profiles.add(Profile.JAVA_EE_5);
        profiles.add(Profile.JAVA_EE_6_FULL);
        profiles.add(Profile.JAVA_EE_6_WEB);
        return profiles;
    }

    @Override
    public File[] getToolClasspathEntries(String toolName) {
        return new File[] {};
    }

    @Override
    public boolean isToolSupported(String toolName) {
        return false;
    }

}
