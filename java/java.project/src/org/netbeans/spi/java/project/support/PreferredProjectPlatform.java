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
package org.netbeans.spi.java.project.support;

import java.text.MessageFormat;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.openide.util.NbPreferences;
import org.openide.util.Parameters;

/**
 * Returns a preferred {@link JavaPlatform} for a new project.
 * @author Tomas Zezula
 * @since 1.46
 */
public final  class PreferredProjectPlatform {
    
    private static final String PREFERRED_PLATFORM = "preferred.platform.{0}";  //NOI18N
    private static final String PLATFORM_ANT_NAME = "platform.ant.name";    //NOI18N
    
    private PreferredProjectPlatform() {
        throw new AssertionError();
    }
    
    /**
     * Returns a preferred {@link JavaPlatform} for a new project.
     * @param platformType the platform type as specified by {@link Specification#getName()}
     * @return the preferred {@link JavaPlatform}
     */
    @CheckForNull
    public static JavaPlatform getPreferredPlatform(@NonNull final String platformType) {
        Parameters.notNull("platformType", platformType);   //NOI18N
        final String platformId = NbPreferences.forModule(PreferredProjectPlatform.class).get(
                MessageFormat.format(PREFERRED_PLATFORM, platformType),
                null);
        final JavaPlatformManager jpm = JavaPlatformManager.getDefault();
        if (platformId != null) {
            for (JavaPlatform jp : jpm.getInstalledPlatforms()) {
                if (platformId.equals(jp.getProperties().get(PLATFORM_ANT_NAME)) &&
                    platformType.equals(jp.getSpecification().getName()) &&
                    jp.isValid()) {
                    return jp;
                }
            }
        }
        final JavaPlatform defaultPlatform = jpm.getDefaultPlatform();
        return platformType.equals(defaultPlatform.getSpecification().getName())?
               defaultPlatform:
               null;
    }
    
    /**
     * Sets a preferred {@link JavaPlatform} for a new project.
     * @param platform the preferred {@link JavaPlatform}
     */
    public static void setPreferredPlatform(@NonNull final JavaPlatform platform) {
        Parameters.notNull("platform", platform);   //NOI18N
        final String platformId = platform.getProperties().get(PLATFORM_ANT_NAME);
        if (platformId == null) {
            throw new IllegalArgumentException("Invalid platform, the platform has no platform.ant.name");  //NOI18N
        }
        final String platformType = platform.getSpecification().getName();
        NbPreferences.forModule(PreferredProjectPlatform.class).put(
                MessageFormat.format(PREFERRED_PLATFORM, platformType),
                platformId);
    }
}
