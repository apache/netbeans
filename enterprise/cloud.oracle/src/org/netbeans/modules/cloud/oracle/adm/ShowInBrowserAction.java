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
package org.netbeans.modules.cloud.oracle.adm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
@ActionID(
        category = "Tools",
        id = "org.netbeans.modules.cloud.oracle.adm.ShowInBrowserAction"
)
@ActionRegistration(
        displayName = "#CTL_ShowInBrowserAction",
        asynchronous = true
)

@ActionReferences(value = {
    @ActionReference(path = "Cloud/Oracle/KnowledgeBase/Actions", position = 260),
    @ActionReference(path = "Cloud/Oracle/Vulnerability/Actions", position = 260),
    @ActionReference(path = "Cloud/Oracle/VulnerabilityAudit/Actions", position = 260),
})

@NbBundle.Messages({
    "CTL_ShowInBrowserAction=Show in Browser",})
public class ShowInBrowserAction implements ActionListener {


    private final URLProvider urlProvider;

    public ShowInBrowserAction(URLProvider provider) {
        this.urlProvider = provider;
    }
    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        URL url = urlProvider.getURL();
        if (url != null) {
            URLDisplayer.getDefault().showURL(urlProvider.getURL());
        }
    }

}
