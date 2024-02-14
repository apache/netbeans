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

package org.netbeans.modules.languages.dataobject;

import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LanguagesDataNode extends DataNode {

    public LanguagesDataNode(LanguagesDataObject obj) {
        super(obj, Children.LEAF);
        String mimeType = obj.getPrimaryFile ().getMIMEType ();
        FileObject fo = Repository.getDefault ().getDefaultFileSystem ().
            findResource ("Editors/" + mimeType + "/language.nbs");
        String icon = (String) fo.getAttribute ("icon");
        if (icon == null)
            icon = "org/netbeans/modules/languages/resources/defaultIcon.png";
        setIconBaseWithExtension (icon);
    }

//    /** Creates a property sheet. */
//    protected Sheet createSheet() {
//        Sheet s = super.createSheet();
//        Sheet.Set ss = s.get(Sheet.PROPERTIES);
//        if (ss == null) {
//            ss = Sheet.createPropertiesSet();
//            s.put(ss);
//        }
//        // TODO add some relevant properties: ss.put(...)
//        return s;
//    }

    private Map<String,Action[]> mimeTypeToActions = new HashMap<String,Action[]> ();
    
    /** Get actions for this data object.
    * @see DataLoader#getActions
    * @return array of actions or <code>null</code>
    */
    public Action[] getActions (boolean context) {
        String mimeType = getDataObject ().getPrimaryFile ().getMIMEType ();
        if (!mimeTypeToActions.containsKey (mimeType)) {
            List<Action> actions = new ArrayList<Action> ();
            try {
                FileObject fo = Repository.getDefault ().getDefaultFileSystem ().
                    findResource ("Loaders/" + mimeType + "/Actions");
                if (fo != null) {
                    DataFolder df = DataFolder.findFolder (fo);
                    DataObject[] dob = df.getChildren ();
                    int i, k = dob.length;
                    for (i = 0; i < k; i++) {
                        InstanceCookie ic = dob [i].getCookie(InstanceCookie.class);
                        Class<?> clazz = ic.instanceClass ();
                        if (JSeparator.class.isAssignableFrom (clazz))
                            actions.add (null);
                        else
                            actions.add ((Action) ic.instanceCreate ());
                    }
                }
            } catch (ClassNotFoundException ex) {
                ErrorManager.getDefault ().notify (ex);
            } catch (IOException ex) {
                ErrorManager.getDefault ().notify (ex);
            }
            if (!actions.isEmpty ())
                mimeTypeToActions.put (mimeType, actions.toArray (new Action [0]));
            else
                mimeTypeToActions.put (mimeType, super.getActions (context));
        }
        return mimeTypeToActions.get(mimeType);
    }
}




