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
package org.netbeans.modules.csl.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.JSeparator;
import org.netbeans.api.editor.mimelookup.MimePath;

import org.openide.ErrorManager;
import org.openide.actions.OpenAction;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.util.actions.SystemAction;


public class GsfDataNode extends DataNode {
    private static final Logger LOG = Logger.getLogger(GsfDataNode.class.getName());
    
    private static Map<String, Action[]> mimeTypeToActions = new HashMap<String, Action[]>();

    public GsfDataNode(GsfDataObject basDataObject, Language language) {
        super(basDataObject, Children.LEAF);
        if (language != null && language.getIconBase() != null) {
            setIconBaseWithExtension(language.getIconBase());
        }
    }

    @Override
    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }
    
    private void loadActions(List<Action> actions, DataFolder df) throws IOException, ClassNotFoundException {
        DataObject[] dob = df.getChildren();
        int i;
        int k = dob.length;

        for (i = 0; i < k; i++) {
            InstanceCookie ic = dob[i].getCookie(InstanceCookie.class);
            if (ic == null) {
                LOG.log(Level.WARNING, "Not an action instance, or broken action: {0}", dob[i].getPrimaryFile());
                continue;
            }
            Class<?> clazz = ic.instanceClass();

            if (JSeparator.class.isAssignableFrom(clazz)) {
                actions.add(null);
            } else {
                actions.add((Action)ic.instanceCreate());
            }
        }
    }

    /** Get actions for this data object.
     * (Copied from LanguagesDataNode in languages/engine)
    * @see DataLoader#getActions
    * @return array of actions or <code>null</code>
    */
    @Override
    public Action[] getActions(boolean context) {
        String mimeType = getDataObject().getPrimaryFile().getMIMEType();

        if (!mimeTypeToActions.containsKey(mimeType)) {
            List<Action> actions = new ArrayList<Action>();

            try {
                FileObject fo = FileUtil.getConfigFile("Loaders/" + mimeType + "/Actions"); // NOI18N

                if (fo != null) {
                    DataFolder df = DataFolder.findFolder(fo);
                    loadActions(actions, df);
                }
                MimePath mp = MimePath.get(mimeType);
                String s = mp.getInheritedType();
                if (s != null && !s.isEmpty()) {
                    fo = FileUtil.getConfigFile("Loaders/" + s + "/Actions"); // NOI18N
                    if (fo != null) {
                        DataFolder df = DataFolder.findFolder(fo);
                        loadActions(actions, df);
                    }
                }
            } catch (ClassNotFoundException ex) {
                ErrorManager.getDefault().notify(ex);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }

            if (!actions.isEmpty()) {
                mimeTypeToActions.put(mimeType, actions.toArray(new Action[0]));
            } else {
                mimeTypeToActions.put(mimeType, super.getActions(context));
            }
        }

        return mimeTypeToActions.get(mimeType);
    }
}
