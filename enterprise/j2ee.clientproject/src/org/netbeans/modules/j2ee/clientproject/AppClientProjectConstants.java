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

package org.netbeans.modules.j2ee.clientproject;


/** Helper class. Defines constants for properties. Knows the proper
 *  place where to store the properties.
 * 
 * @author Petr Hrebejk
 */
public class AppClientProjectConstants {
    
    
    private AppClientProjectConstants() {
    }
    
    /**
     * Constant representing the ejb jar artifact
     */

    public static final String CAR_ANT_ARTIFACT_ID = "j2ee-module-car"; // NOI18N
        
    /**
     * Standard command for redeploying an ejb module project.
     * @see ActionProvider
     */
    public static final String COMMAND_REDEPLOY = "redeploy" ; //NOI18N
    
    /**
     * Constant representing an j2ee jar artifact to be packaged as a part of an ear archive.
     */
    public static final String ARTIFACT_TYPE_J2EE_MODULE_IN_EAR_ARCHIVE = "j2ee_ear_archive"; //NOI18N
}
