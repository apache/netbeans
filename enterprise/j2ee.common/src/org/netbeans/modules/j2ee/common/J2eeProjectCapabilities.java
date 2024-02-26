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

package org.netbeans.modules.j2ee.common;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.javaee.specs.support.api.EjbSupport;
import org.netbeans.modules.javaee.specs.support.api.JpaSupport;
import org.netbeans.modules.web.api.webmodule.WebModule;

/**
 * Facade allowing queries for certain capabilities provided by 
 * Java/Jakarta EE runtime.
 *
 * @author Petr Hejl
 * @since 1.58
 */
public final class J2eeProjectCapabilities {

    private final Project project;
    private final J2eeModuleProvider provider;
    private final Profile ejbJarProfile;
    private final Profile webProfile;
    private final Profile carProfile;

    private J2eeProjectCapabilities(Project project, J2eeModuleProvider provider,
            Profile ejbJarProfile, Profile webProfile, Profile carProfile) {
        this.project = project;
        this.provider = provider;
        this.ejbJarProfile = ejbJarProfile;
        this.webProfile = webProfile;
        this.carProfile = carProfile;
    }

    @CheckForNull
    public static J2eeProjectCapabilities forProject(@NonNull Project project) {
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (provider == null) {
            return null;
        }
        Profile ejbJarProfile = null;
        Profile webProfile = null;
        Profile carProfile = null;
        if (provider.getJ2eeModule().getType() == J2eeModule.Type.EJB ||
                provider.getJ2eeModule().getType() == J2eeModule.Type.WAR) {
            EjbJar[] ejbJars = EjbJar.getEjbJars(project);
            if (ejbJars.length > 0) {
                // just use first one to test profile:
                ejbJarProfile =  ejbJars[0].getJ2eeProfile();
            }
            if (provider.getJ2eeModule().getType() == J2eeModule.Type.WAR) {
                WebModule module = WebModule.getWebModule(project.getProjectDirectory());
                if (module != null) {
                    webProfile = module.getJ2eeProfile();
                }
            }
        }
        if (provider.getJ2eeModule().getType() == J2eeModule.Type.CAR) {
            Car car = Car.getCar(project.getProjectDirectory());
            if (car != null) {
                carProfile = car.getJ2eeProfile();
            }
        }
        return new J2eeProjectCapabilities(project, provider, ejbJarProfile, webProfile, carProfile);
    }

    /**
     * EJB 3.0 functionality is supported in EjbJar project which is targeting
     * from Java EE 5 to Jakarta EE 8 platform.
     */
    public boolean isEjb30Supported() {
        J2eeModule.Type moduleType = provider.getJ2eeModule().getType();
        boolean eeOk = ejbJarProfile != null && ejbJarProfile.isFullProfile() && 
                ejbJarProfile.isAtLeast(Profile.JAVA_EE_5) && ejbJarProfile.isAtMost(Profile.JAKARTA_EE_8_FULL);
        return J2eeModule.Type.EJB.equals(moduleType) && eeOk;
    }

    /**
     * EJB 3.1 functionality is supported in EjbJar and Web project which is targeting
     * full platform profiles from Java EE 6 to Jakarta EE 8 platform.
     * @return {@code true} if the project is targeting full Java EE 6 or newer platform
     */
    public boolean isEjb31Supported() {
        J2eeModule.Type moduleType = provider.getJ2eeModule().getType();
        boolean ee6or7 = ejbJarProfile != null && ejbJarProfile.isFullProfile() && 
                ejbJarProfile.isAtLeast(Profile.JAVA_EE_6_FULL) && ejbJarProfile.isAtMost(Profile.JAKARTA_EE_8_FULL);
        return ee6or7 && (J2eeModule.Type.EJB.equals(moduleType) ||
                J2eeModule.Type.WAR.equals(moduleType));
    }

    /**
     * EJB 3.1 Lite functionality is supported in Web projects targeting from 
     * Java EE 6 to Jakarta EE 8 web profile, and wherever full EJB 3.1 is supported.
     */
    public boolean isEjb31LiteSupported() {
        J2eeModule.Type moduleType = provider.getJ2eeModule().getType();
        boolean ee6or7Web = ejbJarProfile != null && 
                ejbJarProfile.isAtLeast(Profile.JAVA_EE_6_WEB) && ejbJarProfile.isAtMost(Profile.JAKARTA_EE_8_WEB);
        return isEjb31Supported() || (J2eeModule.Type.WAR.equals(moduleType) && ee6or7Web);
    }

    /**
     * EJB 3.2 functionality is supported in EjbJar and Web project which is targeting
     * full platform profiles from Java EE 7 to Jakarta EE 8 platform.
     *
     * @return {@code true} if the project is targeting full Java EE 7 or newer platform
     * @since 1.76
     */
    public boolean isEjb32Supported() {
        J2eeModule.Type moduleType = provider.getJ2eeModule().getType();
        boolean ee7 = ejbJarProfile != null && ejbJarProfile.isFullProfile() && 
                ejbJarProfile.isAtLeast(Profile.JAVA_EE_7_FULL) && ejbJarProfile.isAtMost(Profile.JAKARTA_EE_8_FULL);
        return ee7 && (J2eeModule.Type.EJB.equals(moduleType) || J2eeModule.Type.WAR.equals(moduleType));
    }

    /**
     * EJB 3.2 Lite functionality is supported in Web projects targeting from 
     * Java EE 7 to Jakarta EE 8 web profile, and wherever full EJB 3.2 is supported.
     *
     * @return {@code true} if the project is targeting full or web profile Java EE 7 or newer platform
     * @since 1.76
     */
    public boolean isEjb32LiteSupported() {
        J2eeModule.Type moduleType = provider.getJ2eeModule().getType();
        boolean ee7Web = ejbJarProfile != null && 
                ejbJarProfile.isAtLeast(Profile.JAVA_EE_7_WEB) && ejbJarProfile.isAtMost(Profile.JAKARTA_EE_8_WEB);
        return isEjb32Supported() || (J2eeModule.Type.WAR.equals(moduleType) && ee7Web);
    }

    /**
     * EJB 4.0 functionality is supported in EjbJar and Web project which is targeting
     * full platform profiles from Jakarta EE 9 to Jakarta EE 11 platform.
     *
     * @return {@code true} if the project is targeting full Jakarta EE 9/9.1 or newer platform
     * @since 1.76
     */
    public boolean isEjb40Supported() {
        J2eeModule.Type moduleType = provider.getJ2eeModule().getType();
        boolean ee9 = ejbJarProfile != null && ejbJarProfile.isFullProfile() && 
                ejbJarProfile.isAtLeast(Profile.JAKARTA_EE_9_FULL) && ejbJarProfile.isAtMost(Profile.JAKARTA_EE_11_FULL);
        return ee9 && (J2eeModule.Type.EJB.equals(moduleType) || J2eeModule.Type.WAR.equals(moduleType));
    }
    
    /**
     * EJB 4.0 Lite functionality is supported in Web projects targeting from 
     * Jakarta EE 9 to Jakarta EE 11 web profile, and wherever full EJB 4.0 is supported.
     *
     * @return {@code true} if the project is targeting full or web profile Jakarta EE 9/9.1 or newer platform
     * @since 1.76
     */
    public boolean isEjb40LiteSupported() {
        J2eeModule.Type moduleType = provider.getJ2eeModule().getType();
        boolean ee9Web = ejbJarProfile != null && 
                ejbJarProfile.isAtLeast(Profile.JAKARTA_EE_9_WEB) && ejbJarProfile.isAtMost(Profile.JAKARTA_EE_11_WEB);
        return isEjb40Supported() || (J2eeModule.Type.WAR.equals(moduleType) && ee9Web);
    }
    
    /**
     * Is CDI 1.0 supported in this project?
     * @return {@code true} if the project targets EE6 profile, {@code false} otherwise
     * @since 1.113
     */
    public boolean isCdi10Supported() {
        return Profile.JAVA_EE_6_FULL.equals(ejbJarProfile) ||
            Profile.JAVA_EE_6_WEB.equals(webProfile) ||
            Profile.JAVA_EE_6_FULL.equals(carProfile);
    }
    
    /**
     * Is CDI 1.1 supported in this project?
     * @return {@code true} if the project targets EE7 profile, {@code false} otherwise
     * @since 1.86
     */
    public boolean isCdi11Supported() {
        return 
            Profile.JAKARTA_EE_9_FULL.equals(ejbJarProfile) ||
            Profile.JAKARTA_EE_8_FULL.equals(ejbJarProfile) ||
            Profile.JAVA_EE_8_FULL.equals(ejbJarProfile) ||
            Profile.JAVA_EE_8_WEB.equals(webProfile) ||
            Profile.JAVA_EE_7_FULL.equals(ejbJarProfile) ||
            Profile.JAVA_EE_7_WEB.equals(webProfile) ||
            Profile.JAVA_EE_7_FULL.equals(carProfile) ||
            Profile.JAVA_EE_8_FULL.equals(carProfile);
    }
    
    /**
     * Is CDI 2.0 supported in this project?
     * @return {@code true} if the project targets EE8 profile, {@code false} otherwise
     * @since 1.113
     */
    public boolean isCdi20Supported() {
        return Profile.JAVA_EE_8_FULL.equals(ejbJarProfile) ||
            Profile.JAVA_EE_8_WEB.equals(webProfile) ||
            Profile.JAVA_EE_8_FULL.equals(carProfile) ||
            Profile.JAKARTA_EE_8_FULL.equals(ejbJarProfile) ||
            Profile.JAKARTA_EE_8_WEB.equals(webProfile) ||
            Profile.JAKARTA_EE_8_FULL.equals(carProfile);
    }
    
    /**
     * Is CDI 3.0 supported in this project?
     * @return {@code true} if the project targets Jakarta EE 9/9.1 profile,
     * {@code false} otherwise
     * @since 1.113
     */
    public boolean isCdi30Supported() {
        return Profile.JAKARTA_EE_9_FULL.equals(ejbJarProfile) ||
            Profile.JAKARTA_EE_9_WEB.equals(webProfile) ||
            Profile.JAKARTA_EE_9_FULL.equals(carProfile) ||
            Profile.JAKARTA_EE_9_1_FULL.equals(ejbJarProfile) ||
            Profile.JAKARTA_EE_9_1_WEB.equals(webProfile) ||
            Profile.JAKARTA_EE_9_1_FULL.equals(carProfile);
    }

    /**
     * Is CDI 4.0 supported in this project?
     *
     * @return {@code true} if the project targets Jakarta EE 10 profile,
     * {@code false} otherwise
     */
    public boolean isCdi40Supported() {
        return Profile.JAKARTA_EE_10_FULL.equals(ejbJarProfile)
                || Profile.JAKARTA_EE_10_WEB.equals(webProfile)
                || Profile.JAKARTA_EE_10_FULL.equals(carProfile);
    }
    
    /**
     * Is CDI 4.1 supported in this project?
     *
     * @return {@code true} if the project targets Jakarta EE 11 profile,
     * {@code false} otherwise
     */
    public boolean isCdi41Supported() {
        return Profile.JAKARTA_EE_11_FULL.equals(ejbJarProfile)
                || Profile.JAKARTA_EE_11_WEB.equals(webProfile)
                || Profile.JAKARTA_EE_11_FULL.equals(carProfile);
    }

    /**
     * Returns <code>true</code> if the server used by project supports EJB lite.
     *
     * @return <code>true</code> if the server used by project supports EJB lite
     * @since 1.66
     * @deprecated use {@link EjbSupport} instead
     */
    @Deprecated
    public boolean isEjbLiteIncluded() {
        J2eePlatform platform = ProjectUtil.getPlatform(project);
        if (platform == null) {
            return false;
        }

        return EjbSupport.getInstance(platform).isEjb31LiteSupported(platform);
    }

    public boolean hasDefaultPersistenceProvider() {
        J2eePlatform platform  = ProjectUtil.getPlatform(project);
        if (platform == null) {
            // server probably not registered, can't resolve whether default provider is supported (see #79856)
            return false;
        }

        Set<Profile> profiles = new HashSet<Profile>(platform.getSupportedProfiles(provider.getJ2eeModule().getType()));
        profiles.remove(Profile.J2EE_13);
        profiles.remove(Profile.J2EE_14);
        if (profiles.isEmpty()) {
            return false;
        }
        JpaSupport support = JpaSupport.getInstance(platform);
        return support != null && support.getDefaultProvider() != null;
    }
}
