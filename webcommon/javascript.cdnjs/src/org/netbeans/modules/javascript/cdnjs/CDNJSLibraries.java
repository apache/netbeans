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
package org.netbeans.modules.javascript.cdnjs;

import org.netbeans.spi.project.ui.support.ProjectCustomizer;

/**
 * Factory for CDNJS libraries in Project Properties dialogs.
 *
 * @author Jan Stola
 */
public final class CDNJSLibraries {

    private CDNJSLibraries() {
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registrations({
        @ProjectCustomizer.CompositeCategoryProvider.Registration(
                projectType = "org.netbeans.modules.web.clientproject", // NOI18N
                category = "JsLibs", // NOI18N
                position = 300),
        @ProjectCustomizer.CompositeCategoryProvider.Registration(
                projectType = "org-netbeans-modules-php-project", // NOI18N
                category = "JsLibs", // NOI18N
                position = 300),
        @ProjectCustomizer.CompositeCategoryProvider.Registration(
                projectType = "org-netbeans-modules-web-project", // NOI18N
                category = "JsLibs", // NOI18N
                position = 300),
    })
    public static ProjectCustomizer.CompositeCategoryProvider forWebProjects() {
        return new LibraryCustomizer();
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registrations({
        @ProjectCustomizer.CompositeCategoryProvider.Registration(
                projectType = "org-netbeans-modules-maven", // NOI18N
                category = "JsLibs", // NOI18N
                position = 300),
        @ProjectCustomizer.CompositeCategoryProvider.Registration(
                projectType = "org-netbeans-modules-gradle", // NOI18N
                category = "JsLibs", // NOI18N
                position = 300),
    })
    public static ProjectCustomizer.CompositeCategoryProvider forOtherProjects() {
        return new LibraryCustomizer(true);
    }

}
