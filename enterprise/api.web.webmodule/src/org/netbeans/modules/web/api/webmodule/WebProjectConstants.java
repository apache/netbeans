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

package org.netbeans.modules.web.api.webmodule;

/**
 * Constants useful for web-based projects.
 *
 * @author  Milan Kuchtiak
 */
public final class WebProjectConstants {

    private WebProjectConstants () {}

    /**
     * Document root root sources type (source folders for JSPs, HTML, ...).
     * See <code>org.netbeans.api.project.Sources</code>.
     */
    public static final String TYPE_DOC_ROOT="doc_root"; //NOI18N

    /**
     * WEB-INF sources type (source folders for TLD files, ...).
     * See <code>org.netbeans.api.project.Sources</code>.
     */
    public static final String TYPE_WEB_INF="web_inf"; //NOI18N
    
    /**
     * Standard command for redeploying a web project.
     * See <code>org.netbeans.api.project.ActionProvider</code>.
     */
    public static final String COMMAND_REDEPLOY = "redeploy" ; //NOI18N
    
    /**
     * Standard artifact type representing a WAR file.
     * See <code>org.netbeans.api.project.ant.AntArtifact</code>.
     */
    public static final String ARTIFACT_TYPE_WAR = "war"; // NOI18N
    
    /**
     * Standard artifact type representing a WAR file used for adding
     * Web module into a J2EE Application (ear project).
     * See <code>org.netbeans.api.project.ant.AntArtifact</code>.
     */
    public static final String ARTIFACT_TYPE_WAR_EAR_ARCHIVE = "j2ee_ear_archive"; //NOI18N
}
