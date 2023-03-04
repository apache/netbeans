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
package org.netbeans.modules.web.client.samples.wizard.iterator;

import org.netbeans.modules.web.clientproject.api.sites.SiteHelper;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.web.clientproject.api.network.NetworkException;
import org.netbeans.modules.web.clientproject.api.network.NetworkSupport;
import org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectProperties;
import org.openide.filesystems.FileObject;


public class OnlineSiteTemplate {

    private static final Logger LOGGER = Logger.getLogger(OnlineSiteTemplate.class.getName());

    private final String name;
    private final String url;
    private final File libFile;

    public OnlineSiteTemplate(String name, String url, String zipName) {
        this.name = name;
        this.url = url;
        this.libFile = new File(SiteHelper.getJsLibsDirectory(), zipName);
    }

    public String getName() {
        return name;
    }

    public boolean isPrepared() {
        return libFile.isFile();
    }

    public void prepare() throws NetworkException, IOException, InterruptedException {
        assert !EventQueue.isDispatchThread();
        assert !isPrepared();
        NetworkSupport.download(url, libFile);
    }

    public void configure(CreateProjectProperties projectProperties) {
        projectProperties.setSiteRootFolder("public_html") // NOI18N
                .setTestFolder("test") // NOI18N
                .setStartFile("index.html"); // NOI18N
    }

    public final void apply(FileObject projectDir, CreateProjectProperties projectProperties, ProgressHandle handle) throws IOException {
        assert !EventQueue.isDispatchThread();
        if (!isPrepared()) {
            // not correctly prepared, user has to know about it already
            LOGGER.info("Template not correctly prepared, nothing to be applied"); //NOI18N
            return;
        }
        SiteHelper.unzipProjectTemplate(getTargetDir(projectDir, projectProperties), libFile, handle);
    }

    protected FileObject getTargetDir(FileObject projectDir, CreateProjectProperties projectProperties) {
        // by default, extract template to site root
        return projectDir.getFileObject(projectProperties.getSiteRootFolder());
    }
}
