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
package org.netbeans.modules.maven.indexer.spi;

import java.util.List;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;

/**
 * The basic set of queries. To be implemented by all index managers.
 * 
 * <p><b>Note</b> that all calls might be made from the IDE-s user interface and
 * even if the return time isn't crucial for the UI responsiveness, it still should be
 * "reasonably" fast.</p>
 * 
 * @author Milos Kleint
 */
public interface BaseQueries {

    /**
     * Returns all group id-s available.
     * 
     * @param repos repositories to search in
     * @return groupId-s
     */
    ResultImplementation<String> getGroups(List<RepositoryInfo> repos);

    /**
     * Returns versions given by GAV.
     * 
     * @param groupId
     * @param artifactId
     * @param version
     * @param repos repositories to search in
     * @return NBVersionInfo-s
     */
    ResultImplementation<NBVersionInfo> getRecords(String groupId, String artifactId, String version, List<RepositoryInfo> repos);

    /**
     * Returns versions given by a groupId, artifactId.
     * 
     * @param groupId
     * @param artifactId
     * @param repos repositories to search in
     * @return NBVersionInfo-s
     */
    ResultImplementation<NBVersionInfo> getVersions(String groupId, String artifactId, List<RepositoryInfo> repos);

    /**
     * Returns all artifactId-s given by a groupId.
     * 
     * @param groupId
     * @param repos repositories to search in
     * @return artifactId-s
     */
    ResultImplementation<String> getArtifacts(String groupId, List<RepositoryInfo> repos);

    /**
     * Returns all artifactId-s given by a groupId and artifactId prefix. 
     * 
     * @param groupId 
     * @param artifactIdPrefix
     * @param repos repositories to search in
     * @return artifactId-s
     */
    ResultImplementation<String> filterPluginArtifactIds(String groupId, String artifactIdPrefix, List<RepositoryInfo> repos);

    /**
     * Returns all groupId-s given by a groupId prefix. 
     * 
     * @param prefix
     * @param repos repositories to search in
     * @return groupId-s
     */
    ResultImplementation<String> filterPluginGroupIds(String prefix, List<RepositoryInfo> repos);

    /**
     * Return all GAV-s given by a packaging.
     * 
     * @param packaging
     * @param repos repositories to search in
     * @return GAV-s
     */
    ResultImplementation<String> getGAVsForPackaging(String packaging, List<RepositoryInfo> repos);

}
