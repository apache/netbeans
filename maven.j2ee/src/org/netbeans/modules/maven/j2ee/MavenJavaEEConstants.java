/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
