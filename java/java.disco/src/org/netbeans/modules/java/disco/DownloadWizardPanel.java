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
package org.netbeans.modules.java.disco;

import java.io.File;
import org.checkerframework.checker.guieffect.qual.UIEffect;
import org.openide.WizardDescriptor;

public class DownloadWizardPanel extends AbstractWizardPanel<DownloadPanel> {

    private final WizardState state;

    DownloadWizardPanel(WizardState state) {
        this.state = state;
    }

    @UIEffect
    @Override
    protected DownloadPanel createComponent() {
        DownloadPanel component = DownloadPanel.create(state);
        component.addPropertyChangeListener(DownloadPanel.PROP_DOWNLOAD_FINISHED, (e) -> {
            if (component.getDownload().isFile())
                component.putClientProperty(WizardDescriptor.PROP_WARNING_MESSAGE, "Could not unarchive package, please install it manually");

            fireChangeListeners();
        });
        return component;
    }

    @Override
    public boolean isValid() {
        return getComponent().isDownloadFinished();
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        File file = getComponent().getDownload();
        if (file != null)
            wiz.putProperty(FoojayPlatformIt.PROP_DOWNLOAD, file.getAbsolutePath());
    }

    @Override
    public void readSettings(WizardDescriptor wiz) {
        String folder = (String) wiz.getProperty(FoojayPlatformIt.PROP_DOWNLOAD_FOLDER);
        getComponent().setDownloadFolder(folder);
    }
}
