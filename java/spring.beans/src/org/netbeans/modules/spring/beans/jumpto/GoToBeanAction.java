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
package org.netbeans.modules.spring.beans.jumpto;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.netbeans.api.jumpto.type.TypeBrowser;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author Rohan Ranade
 */
@ActionID(category="Edit", id="org.netbeans.modules.spring.beans.jumpto.type.GoToBean")
@ActionRegistration(displayName="#TXT_GoToSpringBean")
@ActionReference(path="Menu/GoTo", position=250)
public class GoToBeanAction extends AbstractAction {

    public GoToBeanAction() {
        super(NbBundle.getMessage(GoToBeanAction.class, "TXT_GoToSpringBean"));
        putValue("PopupMenuText", NbBundle.getBundle(GoToBeanAction.class).getString("editor-popup-TXT_GoToSpringBean")); // NOI18N
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        TypeDescriptor typeDescriptor = TypeBrowser.browse(NbBundle.getMessage(GoToBeanAction.class, "DLG_GoToSpringBean"), null, new SpringBeansTypeProvider());
        if (typeDescriptor != null) {
            typeDescriptor.open();
        }
    }
}
