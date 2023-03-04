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
package org.netbeans.modules.maven.modelcache;

import java.util.Collections;
import java.util.SortedSet;
import static org.netbeans.modules.maven.configurations.ConfigurationPersistenceUtils.*;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.filesystems.FileObject;


/**
 *
 * @author mkleint
 */
final class ActiveConfigurationProvider {
    private final AuxiliaryConfiguration aux;
    private final FileObject projectDirectory;

    public ActiveConfigurationProvider(FileObject projectDirectory, AuxiliaryConfiguration aux) {
        this.aux = aux;
        this.projectDirectory = projectDirectory;
    }
    
    public M2Configuration getActiveConfiguration() {
        String active = readActiveConfigurationName(aux);
        if (active == null) {
            return M2Configuration.createDefault(projectDirectory);
        } else {
            SortedSet<M2Configuration> configs = readConfigurations(aux, projectDirectory, true);
            for (M2Configuration c : configs) {
                if (c.getId().equals(active)) {
                    return c;
                }
            }
            configs = readConfigurations(aux, projectDirectory, false);
            for (M2Configuration c : configs) {
                if (c.getId().equals(active)) {
                    return c;
                }
            }
            // attempt to find the stored configuration, if not found it's a profile
            M2Configuration toRet = new M2Configuration(active, projectDirectory);
            toRet.setActivatedProfiles(Collections.singletonList(active));
            return toRet;
        }
    }

}
