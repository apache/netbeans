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

package org.netbeans.modules.team.ide;

import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.team.ide.spi.SettingsServices;

/**
 *
 * @author tomas
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.team.ide.spi.SettingsServices.class)
public class SettingsServicesImpl implements SettingsServices {

    @Override
    public boolean providesOpenSection(Section section) {
        return true;
    }

    @Override
    public void openSection(Section section) {
        switch(section) {
            case PROXY:
                OptionsDisplayer.getDefault().open("General"); // NOI18N
                break;
            case TASKS:
                OptionsDisplayer.getDefault().open(SettingsServices.TEAM_SETTINGS_LOCATION + "/" + SettingsServices.TASKS_SETTINGS_ID); // NOI18N
                break;
            case ODCS:
                OptionsDisplayer.getDefault().open(SettingsServices.TEAM_SETTINGS_LOCATION + "/" + SettingsServices.ODCS_SETTINGS_ID); // NOI18N
                break;
        }
    }
}
