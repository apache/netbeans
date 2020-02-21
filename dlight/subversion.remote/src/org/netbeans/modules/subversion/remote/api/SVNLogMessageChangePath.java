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
public class SVNLogMessageChangePath implements ISVNLogMessageChangePath {

    private final String path;
    private final SVNRevision.Number copySrcRevision;
    private final String copySrcPath;
    private final char action;

    public SVNLogMessageChangePath(String path, SVNRevision.Number copySrcRevision, String copySrcPath, char action) {
        this.path = path;
        this.copySrcRevision = copySrcRevision;
        this.copySrcPath = copySrcPath;
        this.action = action;
    }

    @Override
    public String getPath() {
       return path;
    }

    @Override
    public SVNRevision.Number getCopySrcRevision() {
        return copySrcRevision;
    }

    @Override
    public String getCopySrcPath() {
        return copySrcPath;
    }

    @Override
    public char getAction() {
        return action;
    }
}
