/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.visual.action;

import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author David Kaspar
 */
public class ActionMapAction extends WidgetAction.Adapter {

    private InputMap inputMap;
    private ActionMap actionMap;

    public ActionMapAction (InputMap inputMap, ActionMap actionMap) {
        this.inputMap = inputMap;
        this.actionMap = actionMap;
    }

    public State keyPressed (Widget widget, WidgetKeyEvent event) {
        return handleKeyEvent (widget, event, KeyStroke.getKeyStroke (event.getKeyCode (), event.getModifiers ()));
    }

    public State keyReleased (Widget widget, WidgetKeyEvent event) {
        return handleKeyEvent (widget, event, KeyStroke.getKeyStroke (event.getKeyCode (), event.getModifiers (), true));
    }

    public State keyTyped (Widget widget, WidgetKeyEvent event) {
        return handleKeyEvent (widget, event, KeyStroke.getKeyStroke (event.getKeyCode (), event.getModifiers ()));
    }

    private State handleKeyEvent (Widget widget, WidgetKeyEvent event, KeyStroke keyStroke) {
        if (keyStroke == null)
            return State.REJECTED;
        ActionListener action;
        if (actionMap != null && inputMap != null) {
            Object o = inputMap.get (keyStroke);
            action = o != null ? actionMap.get (o) : null;
        } else {
            JComponent view = widget.getScene ().getView ();
            action = view != null ? view.getActionForKeyStroke (keyStroke) : null;
        }
        if (action != null) {
            action.actionPerformed (new ActionEvent (widget, (int) event.getEventID (), null, event.getWhen (), event.getModifiers ())); // TODO - action-event command
            return State.CONSUMED;
        }
        return State.REJECTED;
    }

}
