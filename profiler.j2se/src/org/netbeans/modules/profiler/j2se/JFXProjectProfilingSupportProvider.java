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
package org.netbeans.modules.profiler.j2se;

import org.netbeans.api.project.Project;
import org.netbeans.lib.profiler.common.SessionSettings;
import org.netbeans.modules.profiler.spi.project.ProjectProfilingSupportProvider;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

/**
 *
 * @author Tomas Hurka
 * @author Tomas Zezula
 */
@ProjectServiceProvider(service=ProjectProfilingSupportProvider.class, 
                        projectTypes={@ProjectType(id="org-netbeans-modules-java-j2seproject",position=540)}) // NOI18N
public class JFXProjectProfilingSupportProvider extends J2SEProjectProfilingSupportProvider {

    static final String JAVAFX_ENABLED = "javafx.enabled"; // NOI18N

    public JFXProjectProfilingSupportProvider(Project p) {
        super(p);
    }

////    @Override
////    public boolean checkProjectCanBeProfiled(FileObject profiledClassFile) {
////        if (profiledClassFile == null && isFXProject()) {
////            Project p = getProject();
////            final PropertyEvaluator pp = getProjectProperties(p);
////            String profiledClass = pp.getProperty("main.class"); // NOI18N
////
////            if ((profiledClass == null) || "".equals(profiledClass)) {
////                return false;
////            }
////            return true;
////        }
////        return super.checkProjectCanBeProfiled(profiledClassFile);
////    }

    @Override
    protected void setMainClass(final PropertyEvaluator pp, SessionSettings ss) {
        String jdkVersion=getProjectJavaPlatform().getPlatformJDKVersion();        
        String fxMainClassProp=pp.getProperty("javafx.main.class"); // NOI18N
////        if (mainClassSetManually == null) {
            String mainClass;
            if ((jdkVersion!=null)&&((jdkVersion.equals("jdk18"))||(jdkVersion.equals("jdk19"))) && (fxMainClassProp!=null)) { // NOI18N
                mainClass=fxMainClassProp;
            } else {
                mainClass=pp.getProperty("main.class"); // NOI18N
            }
            ss.setMainClass((mainClass != null) ? mainClass : ""); // NOI18N
////        } else {
////            ss.setMainClass(mainClassSetManually);
////        }
    }
    
////    private boolean isFXProject() {
////        final PropertyEvaluator ep = getProjectProperties(getProject());
////        if (ep == null) {
////            return false;
////        }
////        return isTrue(ep.getProperty(JAVAFX_ENABLED));
////    }

////    private static boolean isTrue(final String value) {
////        return value != null
////                && (value.equalsIgnoreCase("true") || //NOI18N
////                value.equalsIgnoreCase("yes") || //NOI18N
////                value.equalsIgnoreCase("on"));     //NOI18N
////    }
}
