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
package org.netbeans.core.multitabs.impl;

import java.awt.Point;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.core.multitabs.Controller;

/**
 *
 * @author S. Aubrecht
 */
public final class TabListPopupAction extends AbstractAction {

    private final Controller controller;

    public TabListPopupAction( Controller controller ) {
        this.controller = controller;
    }

    @Override
    public void actionPerformed( ActionEvent ae ) {
        if ("pressed".equals(ae.getActionCommand())) { //NOI18N
            JComponent jc = (JComponent) ae.getSource();
            Point p = new Point(jc.getWidth(), jc.getHeight());
            SwingUtilities.convertPointToScreen(p, jc);
            if (!ButtonPopupSwitcher.isShown()) {
                ButtonPopupSwitcher.showPopup(jc, controller, p.x, p.y);
            } else {
                ButtonPopupSwitcher.hidePopup();
            }
            //Other portion of issue 37487, looks funny if the
            //button becomes pressed
            if (jc instanceof AbstractButton) {
                AbstractButton jb = (AbstractButton) jc;
                jb.getModel().setPressed(false);
                jb.getModel().setRollover(false);
                jb.getModel().setArmed(false);
                jb.repaint();
            }
        }
    }

}
