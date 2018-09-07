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
package org.netbeans.modules.java.source.remoteapi;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.java.source.remote.spi.RemotePlatform;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
public class DefaultRemotePlatform implements RemotePlatform {
    
    private static final boolean USE_REMOTE_PLATFORM = Boolean.getBoolean("java.use.remote.platform");

    private final String javaLauncher;

    public DefaultRemotePlatform(String javaLauncher) {
        this.javaLauncher = javaLauncher;
    }

    @Override
    public String getJavaCommand() {
        return javaLauncher;
    }

    @Override
    public List<String> getJavaArguments() {
        return Collections.emptyList();
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        //todo: needs to listen on changes???
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }
    
    @ServiceProvider(service=Provider.class, position=10000)
    public static class ProviderImpl implements Provider {
        private final Map<JavaPlatform, DefaultRemotePlatform> platform2Remote = new WeakHashMap<>();
        private final boolean useRemotePlatform;

        public ProviderImpl() {
            this(USE_REMOTE_PLATFORM);
        }

        public ProviderImpl(boolean useRemotePlatform) {
            this.useRemotePlatform = useRemotePlatform;
        }

        public RemotePlatform findPlatform(FileObject source) {
            if (!useRemotePlatform) {
                return null;
            }

            ClassPath systemCP = ClassPath.getClassPath(source, ClassPath.BOOT);
            FileObject jlObject = systemCP.findResource("java/lang/Object.class");

            for (JavaPlatform p : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
                FileObject platformJLObject = p.getBootstrapLibraries().findResource("java/lang/Object.class");
                if (Objects.equals(jlObject, platformJLObject)) {
                    return platform2Remote.computeIfAbsent(p,
                                                           pp -> new DefaultRemotePlatform(pp.findTool("java").getPath()));
                }
            }

            return null;
        }
    }

}
