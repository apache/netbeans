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

package org.netbeans.modules.ant.freeform.spi;

/**
 * Miscellaneous constants.
 * @author David Konecny
 */
public class ProjectConstants {

    private ProjectConstants() {}

    /**
     * This property is stored in project.xml iff Ant script is not in
     * default location, that is not in parent folder of nbproject directory.
     */
    public static final String PROP_ANT_SCRIPT = "ant.script"; // NOI18N

    /**
     * Location of original project. This property exist only when NB
     * project metadata are stored in different folder.
     */
    public static final String PROP_PROJECT_LOCATION = "project.dir"; // NOI18N
    
    /** 
     * Prefix used in paths to refer to project location.
     */
    public static final String PROJECT_LOCATION_PREFIX = "${" + PROP_PROJECT_LOCATION + "}/"; // NOI18N

}
