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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.profiler.spi.JavaPlatformManagerProvider;
import org.netbeans.modules.profiler.spi.JavaPlatformProvider;

/**
 *
 * @author Tomas Hurka
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.profiler.spi.JavaPlatformManagerProvider.class)
public class JavaPlatformManagerImpl extends JavaPlatformManagerProvider {

    public static final Specification J2SE = new Specification("j2se", null);
    public static final Specification REMOTE_J2SE = new Specification("j2se-remote", null);
    
    @Override
    public List<JavaPlatformProvider> getPlatforms() {
        return getPlatforms(J2SE, REMOTE_J2SE);
    }

    @Override
    public JavaPlatformProvider getDefaultPlatform() {
        return new JavaPlatformImpl(JavaPlatformManager.getDefault().getDefaultPlatform());
    }

    @Override
    public void showCustomizer() {
        PlatformsCustomizer.showCustomizer(null);
    }
    
    public List<JavaPlatformProvider> getPlatforms(final Specification... specs) {
        final List<JavaPlatformProvider> platforms = new ArrayList<JavaPlatformProvider>();
        for (Specification spec : specs) {
            for (JavaPlatform jp : JavaPlatformManager.getDefault().getPlatforms(null, spec)) {
                platforms.add(new JavaPlatformImpl(jp));
            }
        }
        return platforms;
    }
    
    public JavaPlatform getPlatformDelegate(JavaPlatformProvider jpp) {
        if (jpp instanceof JavaPlatformImpl) {
            return ((JavaPlatformImpl)jpp).getDelegate();
        } else {
            return null;
        }
    }
}
