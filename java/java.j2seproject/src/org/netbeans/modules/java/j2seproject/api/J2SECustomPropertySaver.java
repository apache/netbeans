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
package org.netbeans.modules.java.j2seproject.api;

import org.netbeans.api.project.Project;

/**
 * Property saver to be implemented by J2SE Project extension modules
 * that introduce new project properties. Registered savers are
 * used to save extended propertes in addition to standard J2SE Project properties
 * if modified by user in Project Properties dialog.
 * Implementation of the interface should be registered using {@link org.netbeans.spi.project.ProjectServiceProvider}.
 * 
 * Note: alternatively use org.netbeans.spi.project.ui.support.ProjectCustomizer.Category.setStoreListener
 * 
 * @author Petr Somol
 * @since 1.46
 */
public interface J2SECustomPropertySaver {

    /**
     * Method is called when OK is pressed in JSE Project Properties dialog
     * and properties supplied by JSE Project extension module 
     * (thus not handled by JSE Project itself)
     * need to be stored in project.properties and private.properties.
     * 
     * @param p project whose extension properties are to be saved
     */
    void save(Project p);

}
