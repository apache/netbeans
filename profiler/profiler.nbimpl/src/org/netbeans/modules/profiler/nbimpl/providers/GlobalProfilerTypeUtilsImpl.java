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
package org.netbeans.modules.profiler.nbimpl.providers;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.profiler.api.java.SourceClassInfo;
import org.netbeans.modules.profiler.spi.java.ProfilerTypeUtilsProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Bachorik
 */
@ServiceProvider(service=ProfilerTypeUtilsProvider.class)
public class GlobalProfilerTypeUtilsImpl extends BaseProfilerTypeUtilsImpl {
    @Override
    public Collection<SourceClassInfo> getMainClasses() {
        return Collections.EMPTY_LIST;
    }
    
    @Override
    protected ClasspathInfo getClasspathInfo() {
        ClassPath[] cps = prepareClassPaths();
        
        return ClasspathInfo.create(cps[0], cps[1], cps[2]);
    }

    @Override
    protected ClasspathInfo getClasspathInfo(boolean subprojects, boolean source, boolean deps) {
        ClassPath[] cps = prepareClassPaths();
        
        return ClasspathInfo.create(deps ? cps[0] : ClassPath.EMPTY, deps ? cps[1] : ClassPath.EMPTY, source ? cps[2] : ClassPath.EMPTY);
    }
    
    private ClassPath[] prepareClassPaths() {
        Set<ClassPath> srcPaths = GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE);
        Set<ClassPath> compilePaths = GlobalPathRegistry.getDefault().getPaths(ClassPath.COMPILE);
        Set<ClassPath> bootPaths = GlobalPathRegistry.getDefault().getPaths(ClassPath.BOOT);
        
        if (bootPaths.isEmpty()) {
            bootPaths = Collections.singleton(JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries());
        }
        
        ClassPath[] cps = new ClassPath[3];
        cps[0] = ClassPathSupport.createProxyClassPath(bootPaths.toArray(new ClassPath[0]));
        cps[1] = ClassPathSupport.createProxyClassPath(compilePaths.toArray(new ClassPath[0]));
        cps[2] = ClassPathSupport.createProxyClassPath(srcPaths.toArray(new ClassPath[0]));
        
        return cps;
    }
}
