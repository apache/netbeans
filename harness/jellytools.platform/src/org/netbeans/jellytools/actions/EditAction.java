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
package org.netbeans.jellytools.actions;

import org.netbeans.jellytools.Bundle;

/** Used to call "Edit" popup menu item or
 * "org.openide.actions.EditAction".
 * @see Action
 * @see <a href="@org-netbeans-modules-jellytools-ide@/org/netbeans/jellytools/nodes/PropertiesNode.html">org.netbeans.jellytools.nodes.PropertiesNode</a>
 * @see <a href="@org-netbeans-modules-jellytools-ide@/org/netbeans/jellytools/nodes/URLNode.html">org.netbeans.jellytools.nodes.URLNode</a>
 * @see <a href="@org-netbeans-modules-jellytools-java@/org/netbeans/jellytools/nodes/FormNode.html">org.netbeans.jellytools.nodes.FormNode</a>
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class EditAction extends Action {

    private static final String editPopup = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Edit");

    /** creates new EditAction instance */    
    public EditAction() {
        super(null, editPopup, "org.openide.actions.EditAction");
    }
}
