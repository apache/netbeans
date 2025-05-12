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
package org.netbeans.modules.ide.ergonomics.fod;

import java.beans.PropertyVetoException;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import org.netbeans.modules.ide.ergonomics.Utilities;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.actions.CookieAction;

public final class OpenAdvancedAction extends CookieAction {
    private static Set<FileObject> candidates = Collections.newSetFromMap(new WeakHashMap<>());

    public static void registerCandidate(FileObject fo) {
        synchronized (candidates) {
            candidates.add(fo);
        }
    }

    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length == 0) {
            return;
        }
        final DataObject obj = activatedNodes[0].getLookup().lookup(DataObject.class);
        FileObject mimeDefinition = FileUtil.getConfigFile(
            "Loaders/" + obj.getPrimaryFile().getMIMEType() + "/Factories/Ergonomics.instance"
        );
        if (mimeDefinition == null) {
            return;
        }
        final FeatureInfo info = FoDLayersProvider.getInstance().whichProvides(mimeDefinition);
        if (info == null || info.isEnabled()) {
            return;
        }

        Task task = FeatureManager.getInstance().create(new Runnable() {
            public void run() {
                boolean success = Utilities.featureNotFoundDialog(
                    info,
                    NbBundle.getMessage(OpenAdvancedAction.class, "CTL_OpenAdvanced")
                );
                if (success) {
                    try {
                        obj.setValid(false);
                        DataObject newO = DataObject.find(obj.getPrimaryFile());
                        if (newO != obj) {
                            OpenCookie oc = newO.getLookup().lookup(OpenCookie.class);
                            if (oc != null) {
                                oc.open();
                            }
                        }
                    } catch (PropertyVetoException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (DataObjectNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });
        task.schedule(0);

    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (!super.enable(activatedNodes) || activatedNodes.length != 1) {
            return false;
        }
        DataObject obj = activatedNodes[0].getLookup().lookup(DataObject.class);
        return candidates.contains(obj.getPrimaryFile());
    }

    protected int mode() {
        return CookieAction.MODE_ONE;
    }

    public String getName() {
        return NbBundle.getMessage(OpenAdvancedAction.class, "CTL_OpenAdvancedAction");
    }

    protected Class[] cookieClasses() {
        return new Class[]{DataObject.class};
    }

    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() Javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}

