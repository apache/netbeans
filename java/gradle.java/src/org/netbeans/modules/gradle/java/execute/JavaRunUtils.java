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
package org.netbeans.modules.gradle.java.execute;

import java.util.prefs.Preferences;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.NbGradleProject;
import static org.netbeans.modules.gradle.api.execute.RunUtils.PROP_COMPILE_ON_SAVE;
import static org.netbeans.modules.gradle.api.execute.RunUtils.PROP_JDK_PLATFORM;
import org.openide.util.Pair;

/**
 *
 * @author lkishalmi
 */
public class JavaRunUtils {

    private JavaRunUtils() {}

    public static boolean isCompileOnSaveEnabled(Project project) {
        return isOptionEnabled(project, PROP_COMPILE_ON_SAVE, false);
    }

    /**
     * Returns the active platform used by the project or null if the active
     * project platform is broken.
     * @param activePlatformId the name of platform used by Ant script or null
     * for default platform.
     * @return active {@link JavaPlatform} or null if the project's platform
     * is broken
     */
    public static Pair<String, JavaPlatform> getActivePlatform(final String activePlatformId) {
        final JavaPlatformManager pm = JavaPlatformManager.getDefault();
        if (activePlatformId == null) {
            JavaPlatform p = pm.getDefaultPlatform();
            return Pair.of(p.getProperties().get("platform.ant.name"), p);
        } else {
            JavaPlatform[] installedPlatforms = pm.getPlatforms(null, new Specification("j2se", null)); //NOI18N
            for (JavaPlatform installedPlatform : installedPlatforms) {
                String antName = installedPlatform.getProperties().get("platform.ant.name"); //NOI18N
                if (antName != null && antName.equals(activePlatformId)) {
                    return Pair.of(activePlatformId, installedPlatform);
                }
            }
            return Pair.of(activePlatformId, null);
        }
    }

    public static Pair<String, JavaPlatform> getActivePlatform(Project project) {
        Preferences prefs = NbGradleProject.getPreferences(project, false);
        String platformId = prefs.get(PROP_JDK_PLATFORM, null);
        if (platformId == null) {
            GradleBaseProject gbp = GradleBaseProject.get(project);
            platformId = gbp != null ? gbp.getNetBeansProperty(PROP_JDK_PLATFORM) : null;
        }
        return getActivePlatform(platformId);
    }

    private static boolean isOptionEnabled(Project project, String option, boolean defaultValue) {
        GradleBaseProject gbp = GradleBaseProject.get(project);
        if (gbp != null) {
            String value = gbp.getNetBeansProperty(option);
            if (value != null) {
                return Boolean.valueOf(value);
            } else {
                return NbGradleProject.getPreferences(project, false).getBoolean(option, defaultValue);
            }
        }
        return false;
    }

}
