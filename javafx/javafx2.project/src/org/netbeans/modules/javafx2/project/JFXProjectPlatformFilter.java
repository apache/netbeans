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

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.api.common.ui.PlatformFilter;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.modules.javafx2.platform.api.JavaFXPlatformUtils;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 *
 * @author Petr Somol
 */
@ProjectServiceProvider(service=PlatformFilter.class, projectType={"org-netbeans-modules-java-j2seproject"}) // NOI18N
public class JFXProjectPlatformFilter implements PlatformFilter {

    private final J2SEPropertyEvaluator eval;

    public JFXProjectPlatformFilter(Lookup lkp) {
        Parameters.notNull("lkp", lkp); //NOI18N
        this.eval = lkp.lookup(J2SEPropertyEvaluator.class);
        Parameters.notNull("eval", eval);   //NOI18N
    }
    
    @Override
    public boolean accept(final JavaPlatform platform) {
        if(platform != null) {
            if(isFXProject(eval)) {
                if(JavaFXPlatformUtils.isJavaFXEnabled(platform)) {
                    return true;
                }
                return false;
            }
            return true;
        }
        return false;
    }
    
    /**
     * Checks whether the project represented by its evaluator is a FX project
     * 
     * @param eval
     * @return 
     */
    private static boolean isFXProject(@NonNull final J2SEPropertyEvaluator eval) {
        //Don't use JFXProjectProperties.isTrue to prevent JFXProjectProperties from being loaded
        //JFXProjectProperties.JAVAFX_ENABLED is inlined by compliler
        return isTrue(eval.evaluator().getProperty(JFXProjectProperties.JAVAFX_ENABLED));
    }

    private static boolean isTrue(@NullAllowed final String value) {
        return  value != null && (
           "true".equalsIgnoreCase(value) ||    //NOI18N
           "yes".equalsIgnoreCase(value) ||     //NOI18N
           "on".equalsIgnoreCase(value));       //NOI18N
    }

}
