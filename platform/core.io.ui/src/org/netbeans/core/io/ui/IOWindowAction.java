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

package org.netbeans.core.io.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.windows.IOContainer;

/** The action which shows standard IO component.
*
* @author Tomas Holy
*/
@ActionID(id = "org.netbeans.core.io.ui.IOWindowAction", category = "Window")
@ActionRegistration(displayName = "#IOWindow", iconBase="org/netbeans/core/io/ui/output.png")
@ActionReferences({
    @ActionReference(name = "D-4", path = "Shortcuts"),
    @ActionReference(position = 800, path = "Menu/Window")
})
public final class IOWindowAction implements ActionListener {

    public @Override void actionPerformed(ActionEvent evt) {
        IOContainer container = IOContainer.getDefault();
        container.open();
        container.requestActive();
    }

}
