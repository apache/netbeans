/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
