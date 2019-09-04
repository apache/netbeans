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
package org.netbeans.modules.fish.payara.micro.project;

import java.util.ArrayList;
import static java.util.Collections.unmodifiableList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Gaurav Gupta <gaurav.gupta@payara.fish>
 */
public class VersionRepository {

    private static VersionRepository versionRepository;
    private static final List<MicroVersion> MICRO_VERSIONS = new ArrayList<>();

    private VersionRepository() {
        MICRO_VERSIONS.add(new MicroVersion("5.193", "8.0"));
        MICRO_VERSIONS.add(new MicroVersion("5.192", "8.0"));
        MICRO_VERSIONS.add(new MicroVersion("5.191", "8.0"));
        MICRO_VERSIONS.add(new MicroVersion("5.184", "8.0"));
        MICRO_VERSIONS.add(new MicroVersion("5.183", "8.0"));
        MICRO_VERSIONS.add(new MicroVersion("5.182", "8.0"));
        MICRO_VERSIONS.add(new MicroVersion("5.181", "8.0"));
        MICRO_VERSIONS.add(new MicroVersion("4.1.2.181", "7.0"));
        MICRO_VERSIONS.add(new MicroVersion("4.1.2.174", "7.0"));
    }

    public static VersionRepository getInstance() {
        if (versionRepository == null) {
            versionRepository = new VersionRepository();
        }
        return versionRepository;
    }

    public List<MicroVersion> getMicroVersion() {
        return unmodifiableList(MICRO_VERSIONS);
    }
    
    public static Optional<MicroVersion> toMicroVersion(String microVersion) {
        return MICRO_VERSIONS
                .stream()
                .filter(micro -> micro.getVersion().equals(microVersion))
                .findAny();
    }

    public String getJavaEEVersion(String microVersion) {
        return MICRO_VERSIONS.stream()
                .filter(micro -> micro.getVersion().equals(microVersion))
                .map(MicroVersion::getJavaeeVersion)
                .findAny().get();
    }

}
