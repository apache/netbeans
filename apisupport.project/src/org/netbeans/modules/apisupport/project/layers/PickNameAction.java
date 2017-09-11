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
import java.net.URI;
import java.net.URL;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.spi.LayerUtil;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 * Lets user pick a localized display name for a given layer file.
 * @author Jesse Glick
 */
public class PickNameAction extends CookieAction {
    
    static FileObject findFile(Node[] activatedNodes) {
        return activatedNodes[0].getCookie(DataObject.class).getPrimaryFile();
    }
    
    static NbModuleProvider findProject(FileObject f) {
        URL[] location = (URL[]) f.getAttribute("layers"); // NOI18N
        if (location == null || location.length != 1) {
            return null;
        }
        Project p = FileOwnerQuery.getOwner(URI.create(location[0].toExternalForm()));
        if (p == null) {
            return null;
        }
        return p.getLookup().lookup(NbModuleProvider.class);
    }
    
    private static String findBundlePath(NbModuleProvider p) {
        FileObject src = p.getSourceDirectory();
        ManifestManager mm = ManifestManager.getInstance(Util.getManifest(p.getManifestFile()), false);
        String bundlePath = mm.getLocalizingBundle();
        if (bundlePath != null && bundlePath.endsWith(".properties") && src.getFileObject(bundlePath) != null) {
            return bundlePath;
        } else {
            return null;
        }
    }
    
    protected @Override void performAction(Node[] activatedNodes) {
        NotifyDescriptor.InputLine d = new NotifyDescriptor.InputLine(
                NbBundle.getMessage(PickNameAction.class, "PickNameAction_dialog_label"),
                NbBundle.getMessage(PickNameAction.class, "PickNameAction_dialog_title"));
        if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.OK_OPTION) {
            return;
        }
        String name = d.getInputText();
        FileObject f = findFile(activatedNodes);
        if (f == null) {
            return;
        }
        NbModuleProvider p = findProject(f);
        if (p == null) {
            return;
        }
        String bundlePath = findBundlePath(p);
        if (bundlePath == null) {
            return;
        }
        try {
            FileObject properties = p.getSourceDirectory().getFileObject(bundlePath);
            EditableProperties ep = Util.loadProperties(properties);
            final String key = LayerUtil.generateBundleKeyForFile(f.getPath());
            ep.setProperty(key, name);
            Util.storeProperties(properties, ep);
            f.setAttribute("displayName", "bundlevalue:"
                    + bundlePath.substring(0, bundlePath.length() - ".properties".length())   // NOI18N
                    .replace('/', '.')  // NOI18N
                    + "#" + key); // NOI18N
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (!super.enable(activatedNodes)) {
            return false;
        }
        FileObject f = findFile(activatedNodes);
        if (f == null) {
            return false;
        }
        NbModuleProvider p = findProject(f);
        if (p == null) {
            return false;
        }
        return findBundlePath(p) != null;
    }

    public @Override String getName() {
        return NbBundle.getMessage(PickIconAction.class, "LBL_pick_name");
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
    
    @Override
    protected boolean asynchronous() {
        return false;
    }

}
