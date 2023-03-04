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
package org.netbeans.modules.java.j2seembedded.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.modules.java.j2seembedded.platform.RemotePlatform;
import org.netbeans.modules.java.j2seproject.api.J2SERuntimePlatformProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author Tomas Zezula
 */
@ProjectServiceProvider(service=J2SERuntimePlatformProvider.class, projectType="org-netbeans-modules-java-j2seproject")
public class RemoteRuntimePlatformProvider implements J2SERuntimePlatformProvider {

    private static final SpecificationVersion JDK_8 = new SpecificationVersion("1.8");  //NOI18N

    @NonNull
    @Override
    public Collection<? extends JavaPlatform> getPlatformType(
        @NonNull final SpecificationVersion targetLevel,
        @NonNull final SourceLevelQuery.Profile profile) {
        final Collection<JavaPlatform> result = new ArrayList<>();
        for (JavaPlatform jp : JavaPlatformManager.getDefault().getPlatforms(
            null,
            new Specification(RemotePlatform.SPEC_NAME, null))) {
            if ((jp instanceof RemotePlatform) &&
                isSupported((RemotePlatform)jp, targetLevel, profile)) {
                result.add(jp);
            }
        }
        return Collections.<JavaPlatform>unmodifiableCollection(result);
    }

    private static boolean isSupported(
        @NonNull final RemotePlatform jp,
        @NonNull final SpecificationVersion targetLevel,
        @NonNull final SourceLevelQuery.Profile profile) {
        final SpecificationVersion platformSpec = jp.getSpecification().getVersion();
        final SourceLevelQuery.Profile platformProfile = jp.getProfile();
        if (targetLevel.compareTo(platformSpec) > 0) {
            return false;
        }
        if (profile.compareTo(platformProfile) > 0) {
            return false;
        }
        if (platformProfile != SourceLevelQuery.Profile.DEFAULT &&
            targetLevel.compareTo(JDK_8)<0) {
            return false;
        }
        return true;
    }
}
