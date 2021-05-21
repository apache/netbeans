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
package org.netbeans.modules.fish.payara.micro.project;

import static org.netbeans.modules.fish.payara.micro.plugin.Constants.BUILD_ICON;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.CLEAN_ICON;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.DEBUG_ICON;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.PROFILE_ICON;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.REBUILD_ICON;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.RELOAD_ICON;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.START_ICON;

/**
 *
 * @author Gaurav Gupta <gaurav.gupta@payara.fish>
 */
public enum ActionType {

    CLEAN(CLEAN_ICON), 
    BUILD(BUILD_ICON), 
    BUILD_WITH_DEPENDENCIES(BUILD_ICON), 
    REBUILD(REBUILD_ICON),
    
    MICRO_COMPLIE_EXPLODE(RELOAD_ICON),
    MICRO_EXPLODE(RELOAD_ICON),
    
    RUN(START_ICON), 
    DEBUG(DEBUG_ICON), 
    PROFILE(PROFILE_ICON);
   
    private final String icon;

    private ActionType(String icon) {
        this.icon = icon;
    }

    public String getIcon() {
        return icon;
    }
    
    public static ActionType toAction(String actionType) {
        return ActionType.valueOf(actionType.toUpperCase());
    }

}
