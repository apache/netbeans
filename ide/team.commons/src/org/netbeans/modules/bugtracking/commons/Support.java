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

package org.netbeans.modules.bugtracking.commons;

import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.modules.team.ide.spi.IDEServices;
import org.netbeans.modules.team.ide.spi.ProjectServices;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
class Support {
    private static Support instance;
    static final Logger LOG = Logger.getLogger(Support.class.getName());
    private RequestProcessor parallelRP;

    public static synchronized Support getInstance() {
        if(instance == null) {
            instance = new Support();
        }
        return instance;
    }
    private IDEServices ideServices;
    private ProjectServices projectServices;
    
    synchronized IDEServices getIDEServices() {
        if(ideServices == null) {
            ideServices = Lookup.getDefault().lookup(IDEServices.class);
        }
        return ideServices;
    }

    synchronized ProjectServices getProjectServices() {
        if(projectServices == null) {
            projectServices = Lookup.getDefault().lookup(ProjectServices.class);
        }
        return projectServices;
    }  

    static Preferences getPreferences() {
        return NbPreferences.forModule(Support.class);
    }    
    
    public RequestProcessor getParallelRP () {
        if (parallelRP == null) {
            parallelRP = new RequestProcessor("Bugtracking commons parallel tasks", 5, true); //NOI18N
        }
        return parallelRP;
    }    
      
}
