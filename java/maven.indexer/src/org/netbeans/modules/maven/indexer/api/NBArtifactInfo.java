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
/*
 * Contributor(s): theanuradha@netbeans.org
 */

package org.netbeans.modules.maven.indexer.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;

/**
 *
 * @author Anuradha
 */
public class NBArtifactInfo {

    private final String name;
    private final List<NBVersionInfo> versionInfos = new ArrayList<>();

    public NBArtifactInfo(String name) {
        this.name = name;
    }

    public boolean removeVersionInfo(Object o) {
        return versionInfos.remove(o);
    }

    public boolean addAllVersionInfos(Collection<? extends NBVersionInfo> c) {
        return versionInfos.addAll(c);
    }

    public boolean addVersionInfo(NBVersionInfo e) {
        return versionInfos.add(e);
    }

    public @NonNull List<NBVersionInfo> getVersionInfos() {
        return new ArrayList<>(versionInfos);
    }

    public String getName() {
        return name;
    }
  
}
