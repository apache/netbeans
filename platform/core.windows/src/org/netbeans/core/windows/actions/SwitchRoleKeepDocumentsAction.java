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
package org.netbeans.core.windows.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;

/**
 * Action to switch the window system to a new role. The name of the new role
 * is <code>ActionEvent</code>'s <code>actionCommand</code> parameter.
 * This role switch differs from the default implementation because it attempts
 * to keep all opened document windows in the new role.
 * 
 * @see ActionEvent#getActionCommand() 
 * @see WindowManagerImpl#setRole(java.lang.String) 
 * 
 * @since 2.34
 * 
 * @author S. Aubrecht
 */
@ActionID( category = "Window",
id = "org.netbeans.core.windows.actions.SwitchRoleKeepDocumentsAction" )
@ActionRegistration( displayName = "#CTL_SwitchRoleKeepDocumentsAction" )
@ActionReferences( {} )
@Messages( "CTL_SwitchRoleKeepDocumentsAction=Switch Role" )
public final class SwitchRoleKeepDocumentsAction implements ActionListener {

    @Override
    public void actionPerformed( ActionEvent e ) {
        String role = e.getActionCommand();
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        wm.setRole( role, true );
    }
}
