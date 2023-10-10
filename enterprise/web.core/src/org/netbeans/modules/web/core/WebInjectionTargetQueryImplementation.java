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

package org.netbeans.modules.web.core;

import javax.lang.model.element.TypeElement;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.javaee.injection.spi.InjectionTargetQueryImplementation;
import org.netbeans.modules.j2ee.core.api.support.java.SourceUtils;
import org.netbeans.modules.web.api.webmodule.WebModule;

/**
 *
 * @author Martin Adamek
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.javaee.injection.spi.InjectionTargetQueryImplementation.class)
public class WebInjectionTargetQueryImplementation implements InjectionTargetQueryImplementation {
    
    public WebInjectionTargetQueryImplementation() {
    }

    public boolean isInjectionTarget(CompilationController controller, TypeElement typeElement) {
        if (controller == null || typeElement==null) {
            throw new NullPointerException("Passed null to WebInjectionTargetQueryImplementation.isInjectionTarget(CompilationController, TypeElement)"); // NOI18N
        }
        
        boolean ret = false;
        WebModule webModule = WebModule.getWebModule(controller.getFileObject());
        if (webModule != null &&
                !Profile.J2EE_13.equals(webModule.getJ2eeProfile()) && // NOI18N
                !Profile.J2EE_14.equals(webModule.getJ2eeProfile())) { // NOI18N
            if(webModule.getJ2eeProfile().isAtLeast(Profile.JAKARTA_EE_9_WEB)) {
                ret = SourceUtils.isSubtype(controller, typeElement, "jakarta.servlet.Servlet"); // NOI18N
            } else {
                ret = SourceUtils.isSubtype(controller, typeElement, "javax.servlet.Servlet"); // NOI18N
            }
        }
        return ret;
    }
    
    public boolean isStaticReferenceRequired(CompilationController controller, TypeElement typeElement) {
        return false;
    }

}
