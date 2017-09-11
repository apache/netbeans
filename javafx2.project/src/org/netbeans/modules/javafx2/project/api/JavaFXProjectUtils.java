/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
