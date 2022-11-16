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

package org.netbeans.modules.maven.centralsearch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/*
 * Note: dependent on the search query, different fields will be set.
 * https://search.maven.org/solrsearch/select?q=a:jetty-http will have latestVersion set
 * https://search.maven.org/solrsearch/select?q=a:jetty-http&core=gav will have version set
 * the getter returns whatever isn't null.
 *
 * see https://github.com/sonatype-nexus-community/search-maven-org/blob/master/src/app/search/api/doc.ts
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({
    "id",
    "g",
    "a",
    "latestVersion",
    "v",
    "p",
    "timestamp",
    "versionCount",
//    "ec",
//    "tags"
})
public class JSONArtifactDoc {

    @JsonProperty("id")
    private String id;

    @JsonProperty("g")
    private String group;

    @JsonProperty("a")
    private String artifact;

    @JsonProperty("v")
    private String version;

    @JsonProperty("latestVersion")
    private String latestVersion;

    @JsonProperty("p")
    private String pgk;

    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("versionCount")
    private Long versionCount;

//    @JsonProperty("text")
//    private List<String> text = Collections.emptyList();

//    @JsonProperty("ec")
//    private List<String> ec = Collections.emptyList();

//    @JsonProperty("tags")
//    private List<String> tags = Collections.emptyList();

    public String getId() {
        return id;
    }

    public String getGroup() {
        return group;
    }

    public String getArtifact() {
        return artifact;
    }

    public String getVersion() {
        return version != null ? version : latestVersion;
    }

    public String getPackage() {
        return pgk;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Long getVersionCount() {
        return versionCount;
    }

//    public List<String> getEc() {
//        return ec;
//    }
//
//    public List<String> getTags() {
//        return tags;
//    }

    @Override
    public String toString() {
        return JSONArtifactDoc.class.getName()+"{" + "id=" + id + ", group=" + group + ", artifact=" + artifact + ", version=" + getVersion() + ", pgk=" + pgk + ", timestamp=" + timestamp + '}';
    }


}
