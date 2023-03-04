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

package org.netbeans.modules.spring.webmvc;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.spring.api.beans.SpringConstants;
import org.netbeans.modules.spring.spi.beans.SpringConfigFileLocationProvider;
import org.netbeans.modules.spring.spi.beans.SpringConfigFileProvider;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbCollections;

/**
 *
 * @author Andrei Badea
 */
@ProjectServiceProvider(service={SpringConfigFileProvider.class, SpringConfigFileLocationProvider.class},
projectType="org-netbeans-modules-web-project")
public class WebProjectSpringConfigFileProvider implements SpringConfigFileProvider, SpringConfigFileLocationProvider {

    private final Project project;

    public WebProjectSpringConfigFileProvider(Project project) {
        this.project = project;
    }

    @Override
    public Set<File> getConfigFiles() {
        FileObject webInf = getWebInf();
        if (webInf == null) {
            return Collections.emptySet();
        }
        Set<File> result = new HashSet<>();
        addFilesInWebInf(webInf, result);
        return Collections.unmodifiableSet(result);
    }

    private static void addFilesInWebInf(FileObject webInf, Set<File> result) {
        for (FileObject fo : NbCollections.iterable(webInf.getChildren(true))) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            if (!SpringConstants.CONFIG_MIME_TYPE.equals(fo.getMIMEType())) {
                continue;
            }
            File file = FileUtil.toFile(fo);
            if (file == null) {
                continue;
            }
            result.add(file);
        }
    }

    @Override
    public FileObject getLocation() {
        return getWebInf();
    }

    private FileObject getWebInf() {
        WebModuleProvider provider = project.getLookup().lookup(WebModuleProvider.class);
        if (provider == null) {
            return null;
        }
        WebModule webModule = provider.findWebModule(project.getProjectDirectory());
        if (webModule == null) {
            return null;
        }
        return webModule.getWebInf();
    }
}
