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

package org.netbeans.modules.hudson.ui.actions;

import javax.swing.Action;
import java.util.Collections;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.openide.util.Exceptions;
import java.util.List;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.modules.hudson.api.ui.OpenableInBrowser;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.NbBundle.Messages;

/**
 * Action which displays selected job in browser.
 */
@ActionID(category="Team", id="org.netbeans.modules.hudson.ui.actions.OpenUrlAction")
@ActionRegistration(displayName="#LBL_OpenInBrowserAction", iconInMenu=false)
@ActionReference(path=HudsonInstance.ACTION_PATH, position=600)
@Messages("LBL_OpenInBrowserAction=&Open in Browser")
public final class OpenUrlAction extends AbstractAction {

    public static Action forOpenable(OpenableInBrowser openable) {
        return new OpenUrlAction(Collections.singletonList(openable));
    }

    private final List<OpenableInBrowser> openables;

    public OpenUrlAction(List<OpenableInBrowser> openables) {
        super(Bundle.LBL_OpenInBrowserAction());
        this.openables = openables;
    }
    
    public @Override void actionPerformed(ActionEvent e) {
        for (OpenableInBrowser openable : openables) {
            try {
                URLDisplayer.getDefault().showURL(new URL(openable.getUrl()));
            } catch (MalformedURLException x) {
                Exceptions.printStackTrace(x);
            }
        }
    }

}
