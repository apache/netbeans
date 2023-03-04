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

package org.netbeans.modules.apisupport.project.layers;

import java.io.IOException;
import javax.swing.JFileChooser;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.windows.WindowManager;

/**
 * Lets user pick an icon for a given layer file.
 * @author Jesse Glick
 */
public class PickIconAction extends CookieAction {
    
    protected @Override void performAction(Node[] activatedNodes) {
        FileObject f = PickNameAction.findFile(activatedNodes);
        if (f == null) {
            return;
        }
        NbModuleProvider p = PickNameAction.findProject(f);
        if (p == null) {
            return;
        }
        FileObject src = p.getSourceDirectory();
        JFileChooser chooser = UIUtil.getIconFileChooser();
        chooser.setCurrentDirectory(FileUtil.toFile(src));
        if (chooser.showOpenDialog(WindowManager.getDefault().getMainWindow()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        FileObject icon = FileUtil.toFileObject(chooser.getSelectedFile());
        // XXX might instead get WritableXMLFileSystem.cp and search for it in there:
        String iconPath = FileUtil.getRelativePath(src, icon);
        try {
            if (iconPath == null) {
                String folderPath;
                String layerPath = ManifestManager.getInstance(Util.getManifest(p.getManifestFile()), false).getLayer();
                if (layerPath != null) {
                    folderPath = layerPath.substring(0, layerPath.lastIndexOf('/'));
                } else {
                    folderPath = p.getCodeNameBase().replace('.', '/') + "/resources"; // NOI18N
                }
                FileObject folder = FileUtil.createFolder(src, folderPath);
                FileUtil.copyFile(icon, folder, icon.getName(), icon.getExt());
                iconPath = folderPath + '/' + icon.getNameExt();
            }
            f.setAttribute("iconBase", iconPath); // NOI18N
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    protected @Override boolean enable(Node[] activatedNodes) {
        if (!super.enable(activatedNodes)) {
            return false;
        }
        FileObject f = PickNameAction.findFile(activatedNodes);
        if (f == null) {
            return false;
        }
        NbModuleProvider p = PickNameAction.findProject(f);
        if (p == null) {
            return false;
        }
        return true;
    }

    public @Override String getName() {
        return NbBundle.getMessage(PickIconAction.class, "LBL_pick_icon");
    }
    
    protected @Override Class<?>[] cookieClasses() {
        return new Class<?>[] {DataObject.class};
    }
    
    protected @Override int mode() {
        return MODE_EXACTLY_ONE;
    }
    
    public @Override HelpCtx getHelpCtx() {
        return null;
    }
    
    protected @Override boolean asynchronous() {
        return false;
    }

}
