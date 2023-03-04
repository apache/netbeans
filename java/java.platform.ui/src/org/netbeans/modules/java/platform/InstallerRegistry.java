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

package org.netbeans.modules.java.platform;

import java.lang.ref.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.java.platform.CustomPlatformInstall;
import org.netbeans.spi.java.platform.GeneralPlatformInstall;

import org.netbeans.spi.java.platform.PlatformInstall;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Simple helper class, which keeps track of registered PlatformInstallers.
 * It caches its [singleton] instance for a while.
 *
 * @author Svata Dedic
 */
public class InstallerRegistry {
    
    private static final String INSTALLER_REGISTRY_FOLDER = "org-netbeans-api-java/platform/installers"; // NOI18N    
    private static Reference<InstallerRegistry> defaultInstance = new WeakReference<InstallerRegistry>(null);
    private static final Logger LOG = Logger.getLogger(InstallerRegistry.class.getName());
    
    private final Lookup lookup;
    private final List<GeneralPlatformInstall> platformInstalls;      //Used by unit test
    
    InstallerRegistry() {
        this.lookup = Lookups.forPath(INSTALLER_REGISTRY_FOLDER);
        this.platformInstalls = null;
    }
    
    /**
     * Used only by unit tests
     */
    InstallerRegistry (GeneralPlatformInstall[] platformInstalls) {
        assert platformInstalls != null;
        this.platformInstalls = Arrays.asList(platformInstalls);
        this.lookup = null;
    }
    
    /**
     * Returns all registered Java platform installers, in the order as
     * they are specified by the module layer(s).
     */
    public List<PlatformInstall> getInstallers () {
        return filter(getAllInstallers(),PlatformInstall.class);
    }
    
    public List<CustomPlatformInstall> getCustomInstallers () {
        return filter(getAllInstallers(),CustomPlatformInstall.class);
    }
    
    public List<GeneralPlatformInstall> getAllInstallers () {
        if (this.platformInstalls != null) {
            //In the unit test
            return platformInstalls;
        }
        else {
            this.lookup.lookupAll(CustomPlatformInstall.class);
            this.lookup.lookupAll(PlatformInstall.class);
            final List<GeneralPlatformInstall> installs =
                Collections.unmodifiableList(new ArrayList<GeneralPlatformInstall>(
                    this.lookup.lookupAll(GeneralPlatformInstall.class)));
            LOG.log(
                Level.FINE,
                "Installers: {0}",  //NOI18N
                installs);
            return installs;
        }
    }
    
    

    /**
     * Creates/acquires an instance of InstallerRegistry
     */
    public static InstallerRegistry getDefault() {
        InstallerRegistry regs = defaultInstance.get();
        if (regs != null)
            return regs;
        regs = new InstallerRegistry();
        defaultInstance = new WeakReference<InstallerRegistry>(regs);
        return regs;
    }
    
    
    /**
     * Used only by Unit tests.
     * Sets the {@link InstallerRegistry#defaultInstance} to the new InstallerRegistry instance which 
     * always returns the given GeneralPlatformInstalls
     * @return an instance of InstallerRegistry which has to be hold by strong reference during the test
     */
    static InstallerRegistry prepareForUnitTest (GeneralPlatformInstall[] platformInstalls) {
        InstallerRegistry regs = new InstallerRegistry (platformInstalls);
        defaultInstance = new WeakReference<InstallerRegistry>(regs);
        return regs;
    }
        
    
    private static <T> List<T> filter(List<?> list, Class<T> clazz) {
        List<T> result = new ArrayList<T>(list.size());
        for (Object item : list) {
            if (clazz.isInstance(item)) {
                result.add(clazz.cast(item));
            }
        }
        return result;
    }        
}
