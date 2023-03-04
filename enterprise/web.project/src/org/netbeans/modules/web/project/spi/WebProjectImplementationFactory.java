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

package org.netbeans.modules.web.project.spi;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.support.ant.AntProjectHelper;

/** The purpose of this interface is to allow a module to provide an alternative
 * implementation of web project support on top of the standard NetBeans web
 * project metadata format. Web project implementation of AntBasedProjectType
 * will look for instances of this interface in lookup and delegate project 
 * creation to them. If no instance accepts a project the default web project
 * implementation will be used.
 *
 * @author Pavel Buzek
 */
public interface WebProjectImplementationFactory {
    /** Recognize if the project should be owned by your module 
     * or if the default implementation should be used.
     *
     * @return true if you want your {@link createProject} to be used for 
     * this project
     */
    boolean acceptProject(AntProjectHelper helper) throws IOException;
    
    /** Create your implementation of Project to completely bypass 
     * the web/project functionality.
     */
    Project createProject(AntProjectHelper helper) throws IOException;
}
