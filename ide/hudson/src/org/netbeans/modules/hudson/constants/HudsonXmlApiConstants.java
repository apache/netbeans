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

package org.netbeans.modules.hudson.constants;

/**
 * Constants provider for Hudson XML_API
 *
 * @author Michal Mocnak
 */
public class HudsonXmlApiConstants {

    private HudsonXmlApiConstants() {}
    
    // XML API Suffix
    public static final String XML_API_URL ="api/xml"; // NOI18N
    
    // Hudson Instance Element
    public static final String XML_API_VIEW_ELEMENT = "view"; // NOI18N
    public static final String XML_API_JOB_ELEMENT = "job"; // NOI18N
    public static final String XML_API_SECURED_JOB_ELEMENT = "securedJob"; // NOI18N
    public static final String XML_API_NAME_ELEMENT = "name"; // NOI18N
    public static final String XML_API_URL_ELEMENT = "url"; // NOI18N
    public static final String XML_API_COLOR_ELEMENT = "color"; // NOI18N
    
    public static final String XML_API_DISPLAY_NAME_ELEMENT = "displayName"; // NOI18N
    public static final String XML_API_BUILDABLE_ELEMENT = "buildable"; // NOI18N
    public static final String XML_API_INQUEUE_ELEMENT = "inQueue"; // NOI18N
    public static final String XML_API_LAST_BUILD_ELEMENT = "lastBuild"; // NOI18N
    public static final String XML_API_LAST_STABLE_BUILD_ELEMENT = "lastStableBuild"; // NOI18N
    public static final String XML_API_LAST_SUCCESSFUL_BUILD_ELEMENT = "lastSuccessfulBuild"; // NOI18N
    public static final String XML_API_LAST_FAILED_BUILD_ELEMENT = "lastFailedBuild"; // NOI18N
    public static final String XML_API_LAST_COMPLETED_BUILD_ELEMENT = "lastCompletedBuild"; // NOI18N

}
