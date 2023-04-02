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
package org.netbeans.jellytools.modules.debugger.actions;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.Action;

/**
 * Used to call "Debug" popup menu item on project's root node.
 *
 * @see Action
 * @see <a href="@org-netbeans-modules-jellytools-java@/org/netbeans/jellytools/nodes/JavaProjectRootNode.html">org.netbeans.jellytools.nodes.JavaProjectRootNode</a>
 * @author Vojtech Sigler
 */
public class DebugProjectAction extends Action {

    private static final String debugProjectPopup = Bundle.getString("org.netbeans.modules.debugger.ui.actions.Bundle",
            "LBL_DebugProjectActionOnProject_Name");

    /** Creates new DebugJavaProjectAction */
    public DebugProjectAction() {
        super(null, debugProjectPopup);
    }
}
