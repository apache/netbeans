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
package org.netbeans.modules.web.freeform;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ui.PrivilegedTemplates;

import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Special lookup provider to provide 'Folder' template as the last item in New
 * context menu of Web Freeform project
 * 
 * @author Milan Kubec
 */
@LookupProvider.Registration(projectTypes=@LookupProvider.Registration.ProjectType(id="org-netbeans-modules-ant-freeform", position=500))
public class FolderTemplateLookupProviderImpl implements LookupProvider {

    public Lookup createAdditionalLookup(Lookup baseContext) {
        
        Lookup retVal = Lookup.EMPTY;
        
        assert baseContext.lookup(Project.class) != null;
        AuxiliaryConfiguration aux = baseContext.lookup(AuxiliaryConfiguration.class);
        assert aux != null;
        
        if (isWebFFProject(aux)) {
            retVal = Lookups.fixed(new Object[] {
                new PrivilegedTemplatesImpl(),
            });
        }
        
        return retVal;
        
    }
    
    private boolean isWebFFProject(AuxiliaryConfiguration aux) {
        return aux.getConfigurationFragment(WebProjectNature.EL_WEB, WebProjectNature.NS_WEB_1, true) != null // NOI18N
               || aux.getConfigurationFragment(WebProjectNature.EL_WEB, WebProjectNature.NS_WEB_2, true) != null; // NOI18N
    }
    
    private static final class PrivilegedTemplatesImpl implements PrivilegedTemplates {
        
        private static final String[] PRIVILEGED_NAMES = new String[] {
            "Templates/Other/Folder",
        };
        
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }
        
    }
    
}
