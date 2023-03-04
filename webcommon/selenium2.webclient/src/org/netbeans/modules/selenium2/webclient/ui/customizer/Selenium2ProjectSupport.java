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
package org.netbeans.modules.selenium2.webclient.ui.customizer;

import java.beans.PropertyChangeSupport;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
//import org.netbeans.modules.selenium2.webclient.mocha.preferences.Selenium2Preferences;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Theofanis Oikonomou
 */
public final class Selenium2ProjectSupport {

    static final Logger LOGGER = Logger.getLogger(Selenium2ProjectSupport.class.getName());

    static final RequestProcessor RP = new RequestProcessor(Selenium2ProjectSupport.class);

    final Project project;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
//    final Selenium2Preferences preferences;
    
    private Selenium2ProjectSupport(Project project) {
        assert project != null;
        this.project = project;
//        preferences = new Selenium2Preferences(project);
    }

    @ProjectServiceProvider(service = Selenium2ProjectSupport.class, projectType = "org-netbeans-modules-web-clientproject") // NOI18N
    public static Selenium2ProjectSupport create(Project project) {
        Selenium2ProjectSupport support = new Selenium2ProjectSupport(project);
        return support;
    }

    public static Selenium2ProjectSupport forProject(Project project) {
        Selenium2ProjectSupport support = project.getLookup().lookup(Selenium2ProjectSupport.class);
        assert support != null : "Selenium2ProjectSupport should be found in project " + project.getClass().getName() + " (lookup: " + project.getLookup() + ")";
        return support;
    }

//    public Selenium2Preferences getPreferences() {
//        return preferences;
//    }
    
}
