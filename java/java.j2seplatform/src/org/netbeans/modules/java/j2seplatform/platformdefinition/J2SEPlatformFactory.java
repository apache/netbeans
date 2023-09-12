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
package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.io.IOException;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.java.j2seplatform.wizard.NewJ2SEPlatform;
import org.netbeans.spi.java.platform.JavaPlatformFactory;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
public class J2SEPlatformFactory implements JavaPlatformFactory {
    
    private static volatile J2SEPlatformFactory instance;
    
    private J2SEPlatformFactory() {}

    @Override
    @NonNull
    public JavaPlatform create(
            @NonNull final FileObject installFolder,
            @NonNull final String name,
            final boolean persistent) throws IOException {
        Parameters.notNull("installFolder", installFolder); //NOI18N
        Parameters.notNull("name", name);   //NOI18N
        assertValidPlatformName(name);
        return createImpl(installFolder, name, persistent);
    }
    
    @NonNull
    public JavaPlatform create(@NonNull final FileObject installFolder) throws IOException {
        Parameters.notNull("installFolder", installFolder); //NOI18N
        return createImpl(installFolder, null, true);
    }
    
    private JavaPlatform createImpl (
            @NonNull final FileObject installFolder,
            @NullAllowed String name,
            final boolean persistent) throws IOException {
        NewJ2SEPlatform plat = NewJ2SEPlatform.create(installFolder);
        plat.run();
        if (!plat.isValid()) {
            throw new IOException("Invalid J2SE platform in " + installFolder); // NOI18N
        }
        if (name != null) {
            installFolder.setAttribute(NewJ2SEPlatform.DISPLAY_NAME_FILE_ATTR, name);
        } else {
            Object attr = installFolder.getAttribute(NewJ2SEPlatform.DISPLAY_NAME_FILE_ATTR);
            if (attr instanceof String) {
                name = (String) attr;
            } else {
                name = createPlatformDisplayName(plat);
            }
        }
        final String antName = createPlatformAntName(name);
        plat.setDisplayName(name);
        plat.setAntName(antName);
        return persistent ?
                PlatformConvertor.create(plat):
                plat;
    }
    
    private static void assertValidPlatformName(@NonNull final String platformName) {        
        for (JavaPlatform jp : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
            if (platformName.equals(jp.getDisplayName())) {
                throw new IllegalArgumentException(platformName);
            }
        }
    }
    
    @NonNull
    private static String createPlatformDisplayName(@NonNull final JavaPlatform plat) {
        final Map<String, String> m = plat.getSystemProperties();
        final String vmVersion = m.get("java.specification.version"); // NOI18N
        final StringBuilder displayName = new StringBuilder("JDK "); // NOI18N
        if (vmVersion != null) {
            displayName.append(vmVersion);
        }
        return displayName.toString();
    }
    
    @NonNull
    private static String createPlatformAntName(@NonNull final String displayName) {
        assert displayName != null && displayName.length() > 0;
        String antName = PropertyUtils.getUsablePropertyName(displayName);
        if (platformExists(antName)) {
            final String baseName = antName;
            int index = 1;
            antName = baseName + Integer.toString(index);
            while (platformExists(antName)) {
                index ++;
                antName = baseName + Integer.toString(index);
            }
        }
        return antName;
    }
    
    /**
     * Checks if the platform of given antName is already installed
     */
    private static boolean platformExists(@NonNull final String antName) {
        assert antName != null && antName.length() > 0;
        for (JavaPlatform p : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
            final String otherName = p.getProperties().get("platform.ant.name");  // NOI18N
            if (antName.equals(otherName)) {
                return true;
            }
        }
        return false;
    }
    
    @NonNull
    public static J2SEPlatformFactory getInstance() {
        J2SEPlatformFactory res = instance;
        if (res == null) {
            res = instance = new J2SEPlatformFactory();
        }
        return res;
    }
    
    @ServiceProvider(service = JavaPlatformFactory.Provider.class)
    public static final class Provider implements JavaPlatformFactory.Provider {
        @CheckForNull
        @Override
        public JavaPlatformFactory forType(@NonNull final String platformType) {
            if (J2SEPlatformImpl.PLATFORM_J2SE.equals(platformType)) {
                return J2SEPlatformFactory.getInstance();
            }
            return null;
        }        
    }
}
