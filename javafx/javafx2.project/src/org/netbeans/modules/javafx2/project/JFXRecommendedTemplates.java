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
package org.netbeans.modules.javafx2.project;

import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.RecommendedTemplates;

/**
 *
 * @author Tomas Zezula
 */
@ProjectServiceProvider(
    service=RecommendedTemplates.class,
    projectType={"org-netbeans-modules-java-j2seproject", "org-netbeans-modules-maven"})
public class JFXRecommendedTemplates implements RecommendedTemplates {

    private static final String[] RECOMMENDED_TEMPLATES = {
        "javafx"    //NOI18N
    };

    /**
     * Returns template types for JFX.
     * This makes JavaFX file templates intentionally available in JavaSE, JavaFX and Maven projects.
     * (many users prefer to use FX classes without the FX deployment model provided in JavaFX project type)
     * @return JFX template tape
     */
    @Override
    public String[] getRecommendedTypes() {
        return RECOMMENDED_TEMPLATES;
    }

}
