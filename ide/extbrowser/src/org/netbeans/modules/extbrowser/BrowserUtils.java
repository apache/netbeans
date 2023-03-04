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
package org.netbeans.modules.extbrowser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Miscellaneous browser utility methods.
 * @since 1.44
 */
public final class BrowserUtils {

    @StaticResource
    private static final String BROWSER_ICON = "org/netbeans/modules/extbrowser/resources/browser_generic_16x.png"; // NOI18N


    private BrowserUtils() {
    }

    /**
     * Show notification about browser which cannot be run. Also adds a link that opens Tools > Options > General dialog.
     * @param browserName name of browser which cannot be run
     */
    @NbBundle.Messages({
        "# {0} - browser name",
        "BrowserUtils.cannot.run.title=Cannot run {0}",
        "BrowserUtils.cannot.run.detail=Selected browser cannot be run.",
    })
    public static void notifyMissingBrowser(String browserName) {
        NotificationDisplayer.getDefault().notify(Bundle.BrowserUtils_cannot_run_title(browserName), ImageUtilities.loadImageIcon(BROWSER_ICON, false),
                Bundle.BrowserUtils_cannot_run_detail(), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OptionsDisplayer.getDefault().open(OptionsDisplayer.GENERAL);
            }
        });
    }

}
