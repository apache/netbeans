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
            Class clazz = ic.instanceClass();

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
                mimeTypeToActions.put(mimeType, actions.toArray(new Action[actions.size()]));
            } else {
                mimeTypeToActions.put(mimeType, super.getActions(context));
            }
        }

        return mimeTypeToActions.get(mimeType);
    }
}
