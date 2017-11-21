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

package org.netbeans.modules.hibernate.service.spi;

import org.netbeans.api.project.Project;
import org.netbeans.modules.hibernate.service.api.HibernateEnvironment;
import org.netbeans.modules.j2ee.jpa.verification.api.JPAVerificationWarningIds;
import org.netbeans.modules.j2ee.jpa.verification.api.VerificationWarningOverrider;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ProjectServiceProvider;

/**
 * To override specific verification warning
 * 
 * @author Dongmei Cao
 */
@ProjectServiceProvider(service=VerificationWarningOverrider.class, projectType={
    "org-netbeans-modules-maven",
    "org-netbeans-modules-java-j2seproject",
    "org-netbeans-modules-web-project"
}, projectTypes=@LookupProvider.Registration.ProjectType(id="org-netbeans-modules-ant-freeform", position=701))
public class HibernateVerificationWarningOverrider implements VerificationWarningOverrider {
    
    private Project project;
    
    public HibernateVerificationWarningOverrider(Project project) {
        this.project = project;
    }

    public boolean suppressWarning(String warningId) {
        if(warningId.equals(JPAVerificationWarningIds.NO_PERSISTENCE_UNIT_WARNING)) {
            HibernateEnvironment env = project.getLookup().lookup( HibernateEnvironment.class);
            if(env != null && !env.getAllHibernateConfigFileObjects().isEmpty()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
        
    }


}
