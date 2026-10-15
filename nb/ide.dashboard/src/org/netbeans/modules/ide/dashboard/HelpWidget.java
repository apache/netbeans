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
package org.netbeans.modules.ide.dashboard;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.List;
import javax.swing.AbstractAction;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.spi.dashboard.DashboardDisplayer;
import org.netbeans.spi.dashboard.DashboardWidget;
import org.netbeans.spi.dashboard.WidgetElement;
import org.openide.util.NbBundle.Messages;

/**
 * Community links.
 */
@Messages({
    "TITLE_Help=Learn & Discover",
    "LBL_ShowShortcuts=Keyboard Shortcuts",
    "STATUS_ShowShortcuts=Show and edit keyboard shortcuts.",
    "LBL_OnlineLinks=Useful Links",
    "LBL_WebsiteLink=Apache NetBeans website",
    "URL_WebsiteLink=https://netbeans.apache.org",
    "LBL_CommunityLink=Community Help",
    "URL_CommunityLink=https://netbeans.apache.org/community/",
    "LBL_BlogLink=What's New?",
    "URL_BlogLink=https://netbeans.apache.org/blogs/"
})
public class HelpWidget implements DashboardWidget {

    @Override
    public String title(DashboardDisplayer.Panel panel) {
        return Bundle.TITLE_Help();
    }

    @Override
    public List<WidgetElement> elements(DashboardDisplayer.Panel panel) {
        return List.of(
                WidgetElement.actionLink(new ShowShortcutsAction()),
                WidgetElement.separator(),
                WidgetElement.subheading(Bundle.LBL_OnlineLinks()),
                WidgetElement.link(Bundle.LBL_WebsiteLink(), URI.create(Bundle.URL_WebsiteLink())),
                WidgetElement.link(Bundle.LBL_CommunityLink(), URI.create(Bundle.URL_CommunityLink())),
                WidgetElement.link(Bundle.LBL_BlogLink(), URI.create(Bundle.URL_BlogLink()))
        );
    }

    private static class ShowShortcutsAction extends AbstractAction {

        private ShowShortcutsAction() {
            super(Bundle.LBL_ShowShortcuts());
            putValue(SHORT_DESCRIPTION, Bundle.STATUS_ShowShortcuts());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OptionsDisplayer.getDefault().open(OptionsDisplayer.KEYMAPS);
        }

    }

}
