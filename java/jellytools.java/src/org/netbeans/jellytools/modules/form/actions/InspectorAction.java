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
package org.netbeans.jellytools.modules.form.actions;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.Action;

/**
 * Used to call "Window|Navigator" main menu item,
 * "org.netbeans.modules.form.actions.InspectorAction" or shortcut.
 *
 * @see Action
 * @author Jiri Skrivanek
 * @deprecated Navigator is used instead of Inspector. Use NavigatorOperator.
 */
@Deprecated
public class InspectorAction extends Action {

    // Window|Navigator
    private static final String inspectorMenu =
            Bundle.getStringTrimmed("org.netbeans.core.windows.resources.Bundle", "Menu/Window")
            + "|Navigator";

    /** Creates new InspectorAction instance */
    public InspectorAction() {
        super(inspectorMenu, null, "org.netbeans.modules.navigator.ShowNavigatorAction");
    }
}
