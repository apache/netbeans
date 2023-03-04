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

package org.netbeans.modules.gradle.queries;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.gradle.api.execute.GradleDistributionManager;
import org.netbeans.modules.gradle.spi.GradleSettings;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author Laszlo Kishalmi
 */
@ServiceProviders({
    @ServiceProvider(service = SourceForBinaryQueryImplementation.class, position = 68),
    @ServiceProvider(service = SourceForBinaryQueryImplementation2.class, position = 68)
})
public class GradleSourceForBinary implements SourceForBinaryQueryImplementation2 {

    private static final String GRADLE_JAR_PREFIX = "gradle-";

    private final Map<URL, Res> cache = new HashMap<>();

    public GradleSourceForBinary() {
    }

    @Override
    public Result findSourceRoots2(URL binaryRoot) {
        Res ret = cache.get(binaryRoot);
        GradleDistributionManager.GradleDistribution dist = GradleDistributionManager.get(GradleSettings.getDefault().getGradleUserHome()).defaultDistribution();
        if ((ret == null) && (dist.isAvailable())) {
            FileObject distDir = FileUtil.toFileObject(dist.getDistributionDir());
            FileObject srcDir = distDir == null ? null : distDir.getFileObject("src"); //NOI18N
            if ((srcDir != null) && ("jar".equals(binaryRoot.getProtocol()))) {  //NOI18N

                try {
                    URI uri = FileUtil.getArchiveFile(binaryRoot).toURI();
                    if ("file".equals(uri.getScheme())) {
                        FileObject jar = FileUtil.toFileObject(FileUtil.normalizeFile(Utilities.toFile(uri)));
                        if ((jar != null) && FileUtil.isParentOf(distDir, jar)) {
                            String srcName = jar.getName();
                            if (srcName.startsWith(GRADLE_JAR_PREFIX)) {
                                srcName = srcName.substring(GRADLE_JAR_PREFIX.length(), srcName.lastIndexOf('-'));
                                final FileObject jarSrc = srcDir.getFileObject(srcName);
                                if (jarSrc != null) {
                                    ret = new Res(jarSrc);
                                }
                            }
                        }
                    }
                } catch (URISyntaxException ex) {
                }
            }
            if (ret != null) {
                cache.put(binaryRoot, ret);
            }

        }
        return ret;
    }

    @Override
    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        return findSourceRoots2(binaryRoot);
    }

    public static class Res implements Result {

        private final FileObject[] ret;

        public Res(FileObject fo) {
            this.ret = new FileObject[]{fo};
        }

        @Override
        public boolean preferSources() {
            return false;
        }

        @Override
        public FileObject[] getRoots() {
            return ret;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }

    }
}
