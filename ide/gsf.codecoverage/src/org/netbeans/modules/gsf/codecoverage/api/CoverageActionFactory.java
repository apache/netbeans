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
package org.netbeans.modules.gsf.codecoverage.api;

import javax.swing.Action;
import org.netbeans.modules.gsf.codecoverage.CoverageAction;

/**
 * This factory creates actions suitable for insertion into project context menus
 * to decorate a project type with code coverage capability.
 * (Note - you also have to add a {@link CoverageManager} to the project's
 * lookup as well.)
 *
 * @author Tor Norbye
 */
public class CoverageActionFactory {
    /**
     * Create a project sensitive context menu action named "Code Coverage" which
     * will provide actions for enabling/disable code coverage collection, showing
     * a code coverage report, etc.
     *
     * @param configureAction Usually null/empty, but if not null, pass in a new action
     *  which will be added at the top of the code coverage menu for adding support
     *  for code coverage. This typically provides some kind of framework specific
     *  support. For example, in Ruby, if the "rcov" gem isn't installed, code coverage
     *  will be disabled until it is installed, and this action is a "Install RCov"
     *  action which when executed should do whatever it takes to add in support
     *  for code coverage. It is this action's responsibility to call
     *  {@link CoverageManager#setEnabled()} when it is done to indicate that the
     *  actions should be re-enabled if applicable.
     *
     * @param extraActions An optional set of extra actions to add to the menu after
     *   the regular code coverage actions for extra optional coverage related features
     *   provided by your module.
     * @return An action suitable for inclusion in a project context menu
     */
    public static Action createCollectorAction(Action configureAction, Action[] extraActions) {
        //return CoverageAction.get(CoverageAction.class);
        return new CoverageAction(configureAction, extraActions);
    }
}
