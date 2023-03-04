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

package org.netbeans.modules.gradle.java;

import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;

/**
 *
 * @author Laszlo Kishalmi
 */
public class RecommendedPrivilegedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {

    // List of primarily supported templates categories
    private static final String[] TYPES = new String[]{
        "java-classes",
        "java-main-class",
        "java-forms",
        "gui-java-application",
        "java-beans",
        "oasis-XML-catalogs",
        "XML",
        "junit",
        "simple-files"
    };

    private static final String[] TEMPLATES = new String[]{
        "Templates/Classes/Class.java",
        "Templates/Classes/Interface.java",
        "Templates/Other/properties.properties",};

    @Override
    public String[] getRecommendedTypes() {
        return TYPES;
    }

    @Override
    public String[] getPrivilegedTemplates() {
        return TEMPLATES;
    }
}
