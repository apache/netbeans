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
