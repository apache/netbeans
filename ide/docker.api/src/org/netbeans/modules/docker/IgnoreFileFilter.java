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
package org.netbeans.modules.docker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Hejl
 */
public class IgnoreFileFilter implements FileFilter {

    private final FileObject buildContext;

    private final FileObject dockerfile;

    private final FileObject dockerignore;

    private final List<IgnorePattern> patterns = new ArrayList<>();

    public IgnoreFileFilter(FileObject buildContext, FileObject dockerfile, char separator) throws IOException {
        this.buildContext = buildContext;
        if (dockerfile != null) {
            this.dockerfile = dockerfile;
        } else {
            this.dockerfile = buildContext.getFileObject(DockerUtils.DOCKER_FILE);
        }
        this.dockerignore = buildContext.getFileObject(".dockerignore");
        if (dockerignore != null && dockerignore.isData()) {
            patterns.addAll(load(dockerignore, separator));
        }
    }

    @Override
    public boolean accept(File pathname) {
        if (pathname.equals(dockerfile) || pathname.equals(dockerignore)) {
            return true;
        }
        assert pathname.getAbsolutePath().startsWith(buildContext.getPath())
                && !pathname.equals(buildContext);
        String path = pathname.getAbsolutePath().substring(buildContext.getPath().length());
        if (path.startsWith(File.separator)) {
            path = path.substring(1);
        }
        if (path.endsWith(File.separator)) {
            path = path.substring(0, path.length() - 1);
        }
        return accept(path);
    }

    public boolean accept(String relativePath) {
        assert !relativePath.startsWith("/") && !relativePath.endsWith("/") && !relativePath.isEmpty();
        String path = relativePath;
        boolean include = true;
        for (IgnorePattern p : patterns) {
            if (p.matches(path)) {
                include = p.isNegative();
            }
        }
        return include;
    }

    private List<IgnorePattern> load(FileObject dockerignore, char separator) throws IOException {
        List<IgnorePattern> ret = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(dockerignore.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                String trimmed = line.trim();
                // it seems dockerignore supports # comments
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                // FIXME exclusion supported since 1.7.0
                ret.add(IgnorePattern.compile(line, separator, true));
            }
        }
        return ret;
    }
}
