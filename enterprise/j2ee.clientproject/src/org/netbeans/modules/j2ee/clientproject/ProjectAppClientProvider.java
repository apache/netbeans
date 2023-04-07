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

package org.netbeans.modules.j2ee.clientproject;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.j2ee.spi.ejbjar.CarProvider;
import org.netbeans.modules.j2ee.spi.ejbjar.CarsInProject;
import org.openide.filesystems.FileObject;

public class ProjectAppClientProvider implements CarProvider, CarsInProject {
    
    private AppClientProject project;
    
    public ProjectAppClientProvider (AppClientProject project) {
        this.project = project;
    }
    
    public Car findCar (FileObject file) {
        Project project = FileOwnerQuery.getOwner (file);
        if (project instanceof AppClientProject) {
            return ((AppClientProject) project).getAPICar();
        }
        return null;
    }

    public Car[] getCars() {
        return new Car [] {project.getAPICar()};
    }

}
