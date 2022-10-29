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

package org.netbeans.modules.j2ee.ejbjar;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.j2ee.spi.ejbjar.CarProvider;

@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.j2ee.spi.ejbjar.CarProvider.class)
public class ProjectCarProvider implements CarProvider {
    
    public ProjectCarProvider () {
    }
    
    public org.netbeans.modules.j2ee.api.ejbjar.Car findCar (org.openide.filesystems.FileObject file) {
        Project project = FileOwnerQuery.getOwner (file);
        if (project != null) {
            CarProvider provider = project.getLookup().lookup(CarProvider.class);
            if (provider != null) {
                return provider.findCar(file);
            }
        }
        return null;
    }
}
