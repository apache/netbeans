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


package org.netbeans.modules.debugger.jpda.heapwalk.actions;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import org.netbeans.modules.debugger.jpda.heapwalk.views.ClassesCountsView;

import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/** Opens classes counts TopComponent.
 *
 * @author   Martin Entlicher
 */
public class ClassesCountsViewAction extends AbstractAction {

    public ClassesCountsViewAction () {
        // When changed, update also mf-layer.xml, where are the properties duplicated because of Actions.alwaysEnabled()
        putValue (
            Action.NAME,
            NbBundle.getMessage (
                ClassesCountsViewAction.class,
                "CTL_ClassesAction"
            )
        );
        putValue (
            "iconbase",
            "org/netbeans/modules/debugger/resources/classesView/Classes.png" // NOI18N
        );
    }

    public void actionPerformed (ActionEvent evt) {
        if (activateComponent ("classes")) return;
        ClassesCountsView v = new ClassesCountsView ();
        v.open ();
        v.requestActive ();
    }

    private static boolean activateComponent (String componentName) {
        TopComponent tc = WindowManager.getDefault().findTopComponent(componentName);
        if (tc != null) {
            tc.open ();
            tc.requestActive ();
            return true;
        }
        return false;
    }
}

