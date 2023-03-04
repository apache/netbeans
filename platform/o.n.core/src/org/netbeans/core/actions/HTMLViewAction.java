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

package org.netbeans.core.actions;

import org.openide.awt.HtmlBrowser;
import org.openide.windows.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

import org.openide.util.Exceptions;

/** Activates last opened HTML browser or opens a HTML Browser on the home URL
 *  specified in IDESettings using HtmlBrowser.URLDisplayer.showURL().
*
* @author Ian Formanek
*/
public class HTMLViewAction extends CallableSystemAction {

    public HTMLViewAction() {
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
    }
    
    protected String iconResource () {
        return "org/netbeans/core/resources/actions/htmlView.gif"; // NOI18N
    }

    public void performAction() {
        org.openide.awt.StatusDisplayer.getDefault().setStatusText(
            NbBundle.getBundle(HTMLViewAction.class).getString("CTL_OpeningBrowser"));
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(
                    new java.net.URL(HtmlBrowser.getHomePage ()
                    ));
        } catch (java.net.MalformedURLException e) {
            String home = HtmlBrowser.getHomePage ();
            if (!home.startsWith ("http://")) { // NOI18N
                home = "http://" + home; // NOI18N
            }
            try {
                HtmlBrowser.URLDisplayer.getDefault().showURL(
                    new java.net.URL(home));
            } catch (java.net.MalformedURLException e1) {
                Exceptions.printStackTrace(e1);
            }
        }
    }
    
    protected boolean asynchronous() {
        return false;
    }

    public String getName() {
        return NbBundle.getBundle(HTMLViewAction.class).getString("HTMLView");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(HTMLViewAction.class);
    }

}
