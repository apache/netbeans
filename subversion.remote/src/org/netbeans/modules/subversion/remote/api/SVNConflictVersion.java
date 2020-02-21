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
package org.netbeans.modules.subversion.remote.api;

/**
 *
 * 
 */
public class SVNConflictVersion {
    public enum NodeKind {
        none("none"), //NOI18N
        file("file"), //NOI18N
        directory("directory"); //NOI18N

        private final String value;
        private NodeKind(String value) {
            this.value = value;
        }
        public static NodeKind fromString(String s) {
            for(NodeKind r : NodeKind.values()) {
                if (r.value.equals(s)) {
                    return r;
                }
            }
            return null;
        }
    }
    
    private final String reposURL;
    private final long pegRevision;
    private final String pathInRepos;
    private final NodeKind nodeKind;
    
    public SVNConflictVersion(String reposURL, long pegRevision, String pathInRepos, NodeKind nodeKind) {
        this.reposURL = pathInRepos;
        this.pegRevision = pegRevision;
        this.pathInRepos = pathInRepos;
        this.nodeKind = nodeKind;
    }

    public String getReposURL() {
        return reposURL;
    }

    public long getPegRevision() {
        return pegRevision;
    }

    public String getPathInRepos() {
        return pathInRepos;
    }

    public NodeKind getNodeKind() {
        return nodeKind;
    }
}
