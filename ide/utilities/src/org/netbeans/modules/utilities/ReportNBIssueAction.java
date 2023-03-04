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
package org.netbeans.modules.utilities;

import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * 
 * @author Tomas Stupka
 */
@ActionID(id = "org.netbeans.modules.utilities.ReportNBIssueAction", category = "Help")
@ActionRegistration(lazy = false, displayName = "#CTL_ReportIssueAction")
@ActionReference(path = "Menu/Help", position = 450)
public class ReportNBIssueAction extends SystemAction {

    private static final String ISSUE_REPORTER_LINK="https://netbeans.apache.org/nb/report-issue";

    public ReportNBIssueAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ReportNBIssueAction.class, "CTL_ReportIssueAction"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.bugzilla.exceptionreporter.ReportNBIssueAction"); // NOI18N
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(new URL(ISSUE_REPORTER_LINK));
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }

    }
}
