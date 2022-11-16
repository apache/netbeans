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

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Collections;

// see https://github.com/sonatype-nexus-community/search-maven-org/blob/master/src/app/artifact/api/artifact-search-response.ts
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "numFound",
    "start",
    "docs"
})
public class JSONSearchResponse {

    @JsonProperty("numFound")
    private Integer numFound;

    @JsonProperty("start")
    private Integer start;

    @JsonProperty("docs")
    private List<JSONArtifactDoc> docs = Collections.emptyList();

    public Integer getNumFound() {
        return numFound;
    }

    public Integer getStart() {
        return start;
    }

    public List<JSONArtifactDoc> getDocs() {
        return docs;
    }

    @Override
    public String toString() {
        return "Response{" + "numFound=" + numFound + ", numDocs=" + docs.size() + ", start=" + start + '}';
    }

}
