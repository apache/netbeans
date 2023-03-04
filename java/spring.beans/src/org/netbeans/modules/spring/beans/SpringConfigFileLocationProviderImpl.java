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

package org.netbeans.modules.spring.beans;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.spring.spi.beans.SpringConfigFileLocationProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Andrei Badea
 */
@ProjectServiceProvider(service=SpringConfigFileLocationProvider.class, projectType={
    "org-netbeans-modules-java-j2seproject",
    "org-netbeans-modules-j2ee-ejbjarproject",
    "org-netbeans-modules-web-project"
})
public class SpringConfigFileLocationProviderImpl implements SpringConfigFileLocationProvider {

    private final Project project;

    public SpringConfigFileLocationProviderImpl(Project project) {
        this.project = project;
    }

    public FileObject getLocation() {
        SourceGroup[] sourceGroups = SourceGroups.getJavaSourceGroups(project);
        if (sourceGroups.length > 0) {
            return sourceGroups[0].getRootFolder();
        }
        return null;
    }
}
