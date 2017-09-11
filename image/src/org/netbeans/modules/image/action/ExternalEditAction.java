/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
        RP.post(new Runnable() {
            @Override
            public void run() {
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
            }
        });
    }
}
