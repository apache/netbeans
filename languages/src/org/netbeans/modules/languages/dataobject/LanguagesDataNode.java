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
                        Class clazz = ic.instanceClass ();
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
                mimeTypeToActions.put (mimeType, actions.toArray (new Action [actions.size ()]));
            else
                mimeTypeToActions.put (mimeType, super.getActions (context));
        }
        return mimeTypeToActions.get(mimeType);
    }
}




