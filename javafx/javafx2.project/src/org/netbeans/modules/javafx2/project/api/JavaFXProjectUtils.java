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
package org.netbeans.modules.javafx2.project.api;

import java.util.Arrays;
import javax.swing.ComboBoxModel;
import javax.swing.ListCellRenderer;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.ui.PlatformFilter;
import org.netbeans.modules.java.api.common.ui.PlatformUiSupport;
import org.netbeans.modules.javafx2.platform.api.JavaFXPlatformUtils;
import org.netbeans.modules.javafx2.project.J2SEProjectType;
import org.netbeans.modules.javafx2.project.JFXProjectUtils;
import org.netbeans.modules.javafx2.project.ui.PlatformsComboBoxModel;

/**
 * 
 * @author Anton Chechel
 * @author Petr Somol
 * @author Roman Svitanic
 */
public final class JavaFXProjectUtils {

    public static final String PROP_JAVA_PLATFORM_NAME = "java.platform.name"; // NOI18N
    public static final String PROJECT_CONFIGURATION_NAMESPACE = J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE;

    private JavaFXProjectUtils() {
    }
    
    public static boolean isJavaFxEnabled(Project prj) {
        return JFXProjectUtils.isFXProject(prj);
    }

    public static boolean isMavenFxProject(Project prj) {
        return JFXProjectUtils.isMavenFXProject(prj);
    }

    public static ComboBoxModel createPlatformComboBoxModel() {
        return new PlatformsComboBoxModel(PlatformUiSupport.createPlatformComboBoxModel(JavaFXPlatformUtils.DEFAULT_PLATFORM,  // NOI18N
                Arrays.<PlatformFilter>asList(new PlatformFilter() {
                    @Override
                    public boolean accept(JavaPlatform platform) {
                        return JavaFXPlatformUtils.isJavaFXEnabled(platform);
                    }                    
                })));
    }

    public static ListCellRenderer createPlatformListCellRenderer() {
        return PlatformUiSupport.createPlatformListCellRenderer();
    }

    public static JavaPlatform getPlatform(Object platformKey) {
        return PlatformUiSupport.getPlatform(platformKey);
    }
}
