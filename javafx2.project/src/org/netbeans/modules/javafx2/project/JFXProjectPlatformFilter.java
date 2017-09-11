/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
