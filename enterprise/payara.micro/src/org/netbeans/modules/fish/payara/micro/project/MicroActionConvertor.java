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

import org.netbeans.api.project.Project;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.COMPILE_EXPLODE_ACTION;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.DEBUG_SINGLE_ACTION;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.EXPLODE_ACTION;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.PROFILE_SINGLE_ACTION;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.RUN_SINGLE_ACTION;
import static org.netbeans.modules.fish.payara.micro.project.Actions.COMMAND_MICRO_RELOAD;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.spi.actions.ActionConvertor;
import static org.netbeans.spi.project.ActionProvider.COMMAND_DEBUG;
import static org.netbeans.spi.project.ActionProvider.COMMAND_PROFILE;
import static org.netbeans.spi.project.ActionProvider.COMMAND_RUN;
import org.openide.util.Lookup;

/**
 *
 * @author lkishalmi
 */
public class MicroActionConvertor implements ActionConvertor {

    private final Project project;

    public MicroActionConvertor(Project project) {
        this.project = project;
    }
    
    @Override
    public String convert(String action, Lookup lookup) {
        MicroApplication microApplication = MicroApplication.getInstance(project);
        String convertedAction = null;
        if (microApplication != null) {
            switch (action) {
                case COMMAND_RUN:
                case COMMAND_DEBUG:
                case COMMAND_PROFILE:
                case RUN_SINGLE_ACTION:
                case DEBUG_SINGLE_ACTION:
                case PROFILE_SINGLE_ACTION:
                    convertedAction = "micro." + action; //NOI18N
                    break;
                case COMMAND_MICRO_RELOAD:
                    convertedAction = RunUtils.isCompileOnSaveEnabled(project) ? EXPLODE_ACTION : COMPILE_EXPLODE_ACTION;
            }
        }
        return convertedAction;
    }

}
