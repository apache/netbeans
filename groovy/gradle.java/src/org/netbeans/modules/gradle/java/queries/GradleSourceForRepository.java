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

package org.netbeans.modules.gradle.java.queries;

import org.netbeans.modules.gradle.api.GradleProjects;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
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
    @ServiceProvider(service = SourceForBinaryQueryImplementation.class, position = 110),
    @ServiceProvider(service = SourceForBinaryQueryImplementation2.class, position = 110),
    @ServiceProvider(service = JavadocForBinaryQueryImplementation.class, position = 110)
})
public class GradleSourceForRepository implements SourceForBinaryQueryImplementation2, JavadocForBinaryQueryImplementation {

    private final Map<URL, SrcRes> srcCache = Collections.synchronizedMap(new WeakHashMap<>());
    private final Map<URL, DocRes> docCache = Collections.synchronizedMap(new WeakHashMap<>());

    @Override
    public Result findSourceRoots2(URL binaryRoot) {
        SrcRes ret = srcCache.get(binaryRoot);
        if ((ret == null) && "jar".equals(binaryRoot.getProtocol())) {
            try {
                File jar = FileUtil.normalizeFile(Utilities.toFile(FileUtil.getArchiveFile(binaryRoot).toURI()));
                File sources = GradleProjects.getSources(jar);
                if (sources != null) {
                    ret = new SrcRes(sources);
                    srcCache.put(binaryRoot, ret);
                }
            } catch (URISyntaxException | IllegalArgumentException uRISyntaxException) {
            }
        }
        return ret;
    }

    @Override
    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        return findSourceRoots2(binaryRoot);
    }

    @Override
    public JavadocForBinaryQuery.Result findJavadoc(URL binaryRoot) {
        DocRes ret = docCache.get(binaryRoot);
        if ((ret == null) && "jar".equals(binaryRoot.getProtocol())) {
            try {
                File jar = FileUtil.normalizeFile(Utilities.toFile(FileUtil.getArchiveFile(binaryRoot).toURI()));
                File doc = GradleProjects.getJavadoc(jar);
                if (doc != null) {
                    ret = new DocRes(doc);
                    docCache.put(binaryRoot, ret);
                }
            } catch (URISyntaxException | IllegalArgumentException uRISyntaxException) {
            }
        }
        return ret;
    }

    private static class SrcRes implements SourceForBinaryQueryImplementation2.Result {

        private final File root;

        public SrcRes(File root) {
            this.root = root;
        }

        @Override
        public boolean preferSources() {
            return false;
        }

        @Override
        public FileObject[] getRoots() {
            FileObject[] ret = new FileObject[0];
            FileObject fo = FileUtil.toFileObject(root);
            if (fo != null) {
                if (FileUtil.isArchiveFile(fo)) {
                    FileObject arch = FileUtil.getArchiveRoot(fo);
                    if (arch != null) {
                        ret = new FileObject[]{arch};
                    }
                } else {
                    ret = new FileObject[]{fo};
                }
            }
            return ret;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }

    }

    private static class DocRes implements JavadocForBinaryQuery.Result {

        private final File root;

        public DocRes(File root) {
            this.root = root;
        }

        @Override
        public URL[] getRoots() {
            URL[] ret = new URL[0];
            FileObject fo = FileUtil.toFileObject(root);
            if (fo != null) {
                FileObject arch = FileUtil.getArchiveRoot(fo);
                if (arch != null) {
                    ret = new URL[]{arch.toURL()};
                }
            }
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
