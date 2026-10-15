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

package org.netbeans.modules.gradle.java.classpath;

import org.netbeans.modules.gradle.java.spi.support.JavaToolchainSupport;
import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.java.api.GradleJavaSourceSet;
import static org.netbeans.modules.gradle.java.api.GradleJavaSourceSet.SourceType.JAVA;
import org.netbeans.modules.gradle.java.execute.JavaRunUtils;

/**
 *
 * @author Laszlo Kishalmi
 */
public final class BootClassPathImpl extends AbstractSourceSetClassPathImpl {
    private static final String PROTOCOL_NBJRT = "nbjrt";   //NOI18N

    final boolean modulesOnly;

    public BootClassPathImpl(Project proj, String group) {
        this(proj, group, false);
    }

    @SuppressWarnings("LeakingThisInConstructor")
    public BootClassPathImpl(Project proj, String group, boolean modulesOnly) {
        super(proj, group);
        this.modulesOnly = modulesOnly;
    }

    @Override
    protected List<URL> createPath() {
        JavaToolchainSupport toolchain = JavaToolchainSupport.getDefault();
        GradleJavaSourceSet ss = getSourceSet();
        File jh = ss != null ? ss.getCompilerJavaHome(JAVA) : null;
        
        JavaPlatform platform = jh != null ? toolchain.platformByHome(jh) : JavaRunUtils.getActivePlatform(project).second();
        List<URL> ret = new LinkedList<>();
        if (platform != null) {
            for (ClassPath.Entry entry : platform.getBootstrapLibraries().entries()) {
                URL root = entry.getURL();
                if (!modulesOnly || PROTOCOL_NBJRT.equals(root.getProtocol())) {
                    ret.add(root);
                }
            }
        }
        return ret;
    }
}
