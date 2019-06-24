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
package org.netbeans.spi.java.project.support;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.platform.JavaPlatformFactory;
import org.netbeans.spi.java.platform.support.ForwardingJavaPlatform;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * A factory for transient {@link JavaPlatform} defined within a project.
 * @author Tomas Zezula
 * @since 1.66
 */
public final class ProjectPlatform {
    private static final String PLATFORM_ACTIVE = "platform.active";    //NOI18N
    //@GuardedBy("platformsByProject")
    private static final Map<Project,JavaPlatform> platformsByProject = new WeakHashMap<>();
    //@GuardedBy("platformsByProject")
    private static final Map<Project,FileObject> homesByProject = new WeakHashMap<>();
    //@GuardedBy("platformsByProject")
    private static final Map<FileObject,JavaPlatform> platformsByHome = new WeakHashMap<>();
    
    private ProjectPlatform() {
        throw new IllegalStateException("No instance allowed"); //NOI18N
    }
    /**
     * Creates a transient {@link JavaPlatform} defined in given {@link Project}.
     * @param owner the project to return the {@link JavaPlatform} for
     * @param eval property evaluator
     * @param platformType the type of the platform, eg. "j2se"
     * @return a {@link JavaPlatform} for given project or null when platform cannot
     * be created
     */
    @CheckForNull
    public static JavaPlatform forProject(
            @NonNull final Project owner,
            @NonNull final PropertyEvaluator eval,
            @NonNull final String platformType) {
        Parameters.notNull("owner", owner); //NOI18N
        Parameters.notNull("eval", eval); //NOI18N
        Parameters.notNull("platformType", platformType);   //NOI18N
        final String platformName = eval.getProperty(PLATFORM_ACTIVE);
        final FileObject jdkHome = resolvePlatformHome(
                platformName,
                owner.getProjectDirectory(),
                eval);
        if (jdkHome == null) {
            return null;
        }
        return forProject(owner, jdkHome, platformName, platformType);
    }

    /**
     * Creates a transient {@link JavaPlatform} defined in given {@link Project}.
     * @param owner the project to return the {@link JavaPlatform} for
     * @param platformHome the {@link JavaPlatform} install folder
     * @param platformName the {@link JavaPlatform} system name uniquely identifying the platform.
     * The name has to be valid property name, use the {@link PropertyUtils#getUsablePropertyName(java.lang.String)} to create the name.
     * For the Ant based project the platform name is a value of {@code platform.active} property.
     * @param platformType the type of the platform, eg. "j2se"
     * @return a {@link JavaPlatform} for given project or null when platform cannot
     * be created
     * @since 1.77
     */
    @CheckForNull
    public static JavaPlatform forProject(
            @NonNull final Project owner,
            @NonNull final FileObject platformHome,
            @NonNull final String platformName,
            @NonNull final String platformType) {
        Parameters.notNull("owner", owner); //NOI18N
        Parameters.notNull("platformHome", platformHome); //NOI18N
        Parameters.notNull("platformName", platformName);   //NOI18N
        Parameters.notNull("platformType", platformType);   //NOI18N
        JavaPlatform res;
        JavaPlatform delegate;
        synchronized (platformsByProject) {
            res = platformsByProject.get(platformHome);
            delegate = res == null ?
                    platformsByHome.get(platformHome) :
                    null;
        }
        if (res == null) {
            boolean newDelegate = false;
            if (delegate == null) {
                delegate = Optional.ofNullable(findJavaPlatform(platformHome, platformType))
                        .orElseGet(() -> createJavaPlatform(platformHome, platformType));
                newDelegate = true;
            }
            if (delegate != null) {
                res = new ForwardingJavaPlatform(delegate) {
                    private AtomicReference<Map<String,String>> propsCache =
                            new AtomicReference<>();
                    @Override
                    public Map<String, String> getProperties() {
                        Map<String,String> props = propsCache.get();
                        if (props == null) {
                            props = new HashMap<>(super.getProperties());
                            props.put("platform.ant.name", platformName); //NOI18N
                            props = Collections.unmodifiableMap(props);
                            propsCache.set(props);
                        }
                        return props;
                    }
                    @Override
                    public String getDisplayName() {
                        return platformName;
                    }
                };
            }
            if (res != null) {
                synchronized (platformsByProject) {
                    platformsByProject.put(owner, res);
                    assert delegate != null;
                    if (newDelegate) {
                        homesByProject.put(owner, platformHome);
                        platformsByHome.put(platformHome, delegate);
                    }
                }
            }
        }
        return res;
    }
    
    @CheckForNull
    private static FileObject resolvePlatformHome(
            @NullAllowed final String platformName,
            @NonNull final FileObject projectDir,
            @NonNull final PropertyEvaluator eval) {
        if (platformName != null) {
            final String homeProp = String.format("platforms.%s.home", platformName);   //NOI18N
            final String path = eval.getProperty(homeProp);
            if (path != null && !path.isEmpty()) {
                final File basedir = FileUtil.toFile(projectDir);
                if (basedir != null) {
                    return FileUtil.toFileObject(PropertyUtils.resolveFile(basedir, path));
                }
            }
        }
        return null;
    }
    
    @CheckForNull
    private static JavaPlatform findJavaPlatform(
            @NonNull final FileObject jdkHome,
            @NonNull final String platformType) {
        return Arrays.stream(JavaPlatformManager.getDefault().getPlatforms(null, new Specification(platformType, null)))
                .filter((jp) -> jp.getInstallFolders().contains(jdkHome))
                .findFirst()
                .orElse(null);
    }
    
    @CheckForNull
    private static JavaPlatform createJavaPlatform(
            @NonNull final FileObject jdkHome,
            @NonNull final String platformType) {
        try {
            for (JavaPlatformFactory.Provider p : Lookup.getDefault().lookupAll(JavaPlatformFactory.Provider.class)) {
                final JavaPlatformFactory f = p.forType(platformType);
                if (f != null) {
                    return f.create(
                            jdkHome,
                            FileUtil.getFileDisplayName(jdkHome),
                            false);
                }
            }
            return null;
        } catch (IOException | IllegalArgumentException e) {
            return null;
        }
    }
    
}
