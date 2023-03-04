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

package org.netbeans.modules.maven.j2ee;

public class MavenJavaEEConstants {

    private MavenJavaEEConstants() {}

    public static final String SELECTED_BROWSER = "netbeans.selected.browser"; //NOI18N
    
    public static final String HINT_DEPLOY_J2EE_SERVER_ID = "netbeans.deployment.server.id"; //NOI18N

    public static final String HINT_DEPLOY_J2EE_SERVER = "netbeans.hint.deploy.server"; //NOI18N

    /**
     * values according to the org.netbeans.api.j2ee.core.Profile class.
     * 1.4, 1.5, 1.6, 1.6-web
     */
    public static final String HINT_J2EE_VERSION = "netbeans.hint.j2eeVersion"; //NOI18N

    /**
     * Optional property, if defined the project type will attempt to redirect meaningful
     * run/debug/profile/test action invocations to the deploy on save infrastructure.
     * Possible values
     * <ul>
     * <li>true  - deploy on save is enabled - default value</li>
     * <li>false  - deploy on save is disabled</li>
     * </ul>
     * @since NetBeans 7.0
     */
    public static final String HINT_DEPLOY_ON_SAVE = "netbeans.deploy.on.save"; //NOI18N

    /**
     * Optional property, if static resources should be copy to the target directory on save.
     * <ul>
     * <li>true  - copy static resources on save is enabled - default value</li>
     * <li>false - copy static resources on save is disabled</li>
     * </ul>
     */
    public static final String HINT_COPY_STATIC_RESOURCES_ON_SAVE = "netbeans.copy.static.resources.on.save"; //NOI18N
    
    /**
     * when present, will deploy the web/ejb/ear project to an app server
     * defined in netbeans.
     * only meaningful value is "true"
     */
    public static final String ACTION_PROPERTY_DEPLOY = "netbeans.deploy"; //NOI18N

    /**
     * denotes wheater the netbeans app server deployment shall be performed in
     * debug mode. Optional property, complementary to ACTION_PROPERTY_DEPLOY.
     */
    public static final String ACTION_PROPERTY_DEPLOY_DEBUG_MODE = "netbeans.deploy.debugmode"; //NOI18N

    /**
     * Denotes wheather the NetBeans application server deployment shall be performed in debug mode.
     * Optional property, complementary to ACTION_PROPERTY_DEPLOY.
     */
    public static final String ACTION_PROPERTY_DEPLOY_PROFILE_MODE = "netbeans.deploy.profilemode"; //NOI18N

    /**
     * Optional property, complementary to ACTION_PROPERTY_DEPLOY.
     */
    public static final String ACTION_PROPERTY_DEPLOY_REDEPLOY = "netbeans.deploy.forceRedeploy"; //NOI18N
    
    public static final String ACTION_PROPERTY_DEPLOY_OPEN = "netbeans.deploy.open.in.browser"; //NOI18N

}
