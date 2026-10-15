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

//    @Override
//    public boolean checkProjectCanBeProfiled(FileObject profiledClassFile) {
//        if (profiledClassFile == null && isFXProject()) {
//            Project p = getProject();
//            final PropertyEvaluator pp = getProjectProperties(p);
//            String profiledClass = pp.getProperty("main.class"); // NOI18N
//
//            if ((profiledClass == null) || "".equals(profiledClass)) {
//                return false;
//            }
//            return true;
//        }
//        return super.checkProjectCanBeProfiled(profiledClassFile);
//    }

    @Override
    protected void setMainClass(final PropertyEvaluator pp, SessionSettings ss) {
        String jdkVersion=getProjectJavaPlatform().getPlatformJDKVersion();        
        String fxMainClassProp=pp.getProperty("javafx.main.class"); // NOI18N
//        if (mainClassSetManually == null) {
            String mainClass;
            if ((jdkVersion!=null)&&((jdkVersion.equals("jdk18"))||(jdkVersion.equals("jdk19"))) && (fxMainClassProp!=null)) { // NOI18N
                mainClass=fxMainClassProp;
            } else {
                mainClass=pp.getProperty("main.class"); // NOI18N
            }
            ss.setMainClass((mainClass != null) ? mainClass : ""); // NOI18N
//        } else {
//            ss.setMainClass(mainClassSetManually);
//        }
    }
    
//    private boolean isFXProject() {
//        final PropertyEvaluator ep = getProjectProperties(getProject());
//        if (ep == null) {
//            return false;
//        }
//        return isTrue(ep.getProperty(JAVAFX_ENABLED));
//    }

//    private static boolean isTrue(final String value) {
//        return value != null
//                && (value.equalsIgnoreCase("true") || //NOI18N
//                value.equalsIgnoreCase("yes") || //NOI18N
//                value.equalsIgnoreCase("on"));     //NOI18N
//    }
}
