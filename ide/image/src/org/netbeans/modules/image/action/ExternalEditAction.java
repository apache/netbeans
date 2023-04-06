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
package org.netbeans.modules.image.action;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.logging.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/*
 * This action opens external image editor (system default) for given image
 */
@ActionID(category = "Images",
        id = "org.netbeans.modules.image.action.ExternalEditAction")
@ActionRegistration(displayName = "#LBL_ExternalEdit")
@ActionReference(path = "Loaders/image/png-gif-jpeg-bmp/Actions", position = 200)
public final class ExternalEditAction implements ActionListener {

    private final List<FileObject> list;
    private final RequestProcessor RP = new RequestProcessor(ExternalEditAction.class.getName());

    public ExternalEditAction(List<FileObject> list) {
        this.list = list;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        RP.post(() -> {
            boolean showInBrowser = true;
            for (FileObject imageFO : list) {
                File imageFile;
                imageFile = FileUtil.toFile(imageFO);
                if (imageFile == null) {
                    continue;
                }

                // open with Desktop API if its supported
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.EDIT)) {
                        showInBrowser = false;
                        try {
                            desktop.edit(imageFile);
                        } catch (IOException ex) {
                            Logger.getLogger(ExternalEditAction.class.getName()).info(NbBundle.getMessage(ExternalEditAction.class, "ERR_ExternalEditFile"));
                            showInBrowser = true;
                        }
                    }
                }
                // if Desktop API is not supported open in browser
                if (showInBrowser) {
                    try {
                        HtmlBrowser.URLDisplayer.getDefault().showURL(imageFile.toURI().toURL());
                    } catch (MalformedURLException ex) {
                        Logger.getLogger(ExternalEditAction.class.getName()).info(NbBundle.getMessage(ExternalEditAction.class, "ERR_ExternalEditFile"));
                    }
                }
            }
        });
    }
}
