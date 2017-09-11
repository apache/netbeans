/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
        JavaPlatform res;
        JavaPlatform delegate;        
        synchronized (platformsByProject) {
            res = platformsByProject.get(jdkHome);
            delegate = res == null ?
                    platformsByHome.get(jdkHome) :
                    null;
        }
        if (res == null) {
            boolean newDelegate = false;
            if (delegate == null) {
                delegate = Optional.ofNullable(findJavaPlatform(jdkHome, platformType))
                        .orElseGet(() -> createJavaPlatform(jdkHome, platformType));
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
                        homesByProject.put(owner, jdkHome);
                        platformsByHome.put(jdkHome, delegate);
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
